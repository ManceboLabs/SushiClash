package com.mancebolabs.sushiclash.data.repository

import com.mancebolabs.sushiclash.data.datastore.AppPreferencesDataStore
import com.mancebolabs.sushiclash.data.datastore.DecodedGameState
import com.mancebolabs.sushiclash.data.datastore.FinishGamePersistenceResult
import com.mancebolabs.sushiclash.data.datastore.RestoreGamePersistenceResult
import com.mancebolabs.sushiclash.domain.model.CorruptGameHistoryException
import com.mancebolabs.sushiclash.domain.model.FinishGameResult
import com.mancebolabs.sushiclash.domain.model.GameMode
import com.mancebolabs.sushiclash.domain.model.GameSetupConfig
import com.mancebolabs.sushiclash.domain.model.GameState
import com.mancebolabs.sushiclash.domain.model.GameStateValidator
import com.mancebolabs.sushiclash.domain.model.IncrementResult
import com.mancebolabs.sushiclash.domain.model.InvalidActiveGameException
import com.mancebolabs.sushiclash.domain.model.PersistenceReadState
import com.mancebolabs.sushiclash.domain.model.Player
import com.mancebolabs.sushiclash.domain.model.RandomRouletteLogic
import com.mancebolabs.sushiclash.domain.model.RandomRouletteTriggerType
import com.mancebolabs.sushiclash.domain.model.RestoreGameResult
import com.mancebolabs.sushiclash.domain.repository.GameRepository
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class GameRepositoryImpl(
    private val dataStore: AppPreferencesDataStore,
    private val randomRouletteLogic: RandomRouletteLogic = RandomRouletteLogic.Default,
    private val sessionIdProvider: () -> String = { UUID.randomUUID().toString() },
    private val clock: () -> Long = System::currentTimeMillis,
) : GameRepository {

    // Serialize repository-local operations; DataStore transactions provide cross-instance atomicity.
    private val gameMutationMutex = Mutex()

    override val gameState: Flow<GameState> = dataStore.decodedGameState.map { readState ->
        when (readState) {
            is PersistenceReadState.Data -> readState.value.toSafeGameState()
            PersistenceReadState.Missing,
            PersistenceReadState.Corrupted,
            // Read I/O is not missing data; do not clear persistence here.
            PersistenceReadState.Unavailable -> GameState()
        }
    }

    override suspend fun restoreGameState(): RestoreGameResult {
        return gameMutationMutex.withLock {
            dataStore.clearActiveGameAfterBackupRestoreIfNeeded()
            when (val result = dataStore.restoreGameState(migratedSessionId = sessionIdProvider())) {
                is RestoreGamePersistenceResult.Restored -> RestoreGameResult.Restored(result.gameState)
                // I/O failure is not a missing game; persistence is left untouched for a later retry.
                RestoreGamePersistenceResult.Unavailable -> RestoreGameResult.Unavailable
            }
        }
    }

    override suspend fun finishGameWithSaving(): FinishGameResult {
        return gameMutationMutex.withLock {
            try {
                when (
                    dataStore.finishGameWithSaving(
                        legacySessionId = sessionIdProvider(),
                        finishedAt = clock(),
                    )
                ) {
                    FinishGamePersistenceResult.Saved -> FinishGameResult.Success
                    FinishGamePersistenceResult.NoActiveGame -> FinishGameResult.NoActiveGame
                    FinishGamePersistenceResult.InvalidActiveGame -> {
                        FinishGameResult.Failure(InvalidActiveGameException())
                    }
                    FinishGamePersistenceResult.CorruptHistory -> {
                        FinishGameResult.Failure(CorruptGameHistoryException())
                    }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: IOException) {
                FinishGameResult.Failure(failure)
            } catch (failure: Exception) {
                FinishGameResult.Failure(failure)
            }
        }
    }

    override suspend fun finishGameWithoutSaving(): FinishGameResult {
        return gameMutationMutex.withLock {
            try {
                val currentState = gameState.first()
                if (!currentState.hasActiveGame) {
                    return@withLock FinishGameResult.NoActiveGame
                }
                dataStore.clearActiveGame()
                FinishGameResult.Success
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: Exception) {
                FinishGameResult.Failure(failure)
            }
        }
    }

    override suspend fun completeSetup(config: GameSetupConfig) {
        val fixedThreshold = config.randomRouletteFixedThreshold.coerceIn(
            GameState.MIN_RANDOM_ROULETTE_THRESHOLD,
            GameState.MAX_RANDOM_ROULETTE_THRESHOLD,
        )
        val players = when (config.gameMode) {
            GameMode.SOLO -> listOf(
                createPlayer(
                    id = AppPreferencesDataStore.SOLO_PLAYER_ID,
                    name = "",
                    config = config,
                ),
            )
            GameMode.GROUP -> config.playerNames.map { name ->
                createPlayer(
                    id = UUID.randomUUID().toString(),
                    name = name.trim(),
                    config = config,
                )
            }
        }

        gameMutationMutex.withLock {
            dataStore.saveGameState(
                sessionId = sessionIdProvider(),
                gameMode = config.gameMode,
                players = players,
                randomRouletteEnabled = config.randomRouletteEnabled,
                randomRouletteTriggerType = config.randomRouletteTriggerType,
                randomRouletteFixedThreshold = fixedThreshold,
            )

            dataStore.setParticipants(emptyList())
            if (config.gameMode == GameMode.GROUP) {
                dataStore.setParticipants(players.map { it.name })
            }
        }
    }

    override suspend fun incrementPlayerCount(playerId: String): IncrementResult {
        return gameMutationMutex.withLock {
            // Mutex orders calls on this instance; DataStore.edit is the cross-instance read-modify-write.
            dataStore.incrementPlayerCount(playerId) { player, currentState ->
                val newCount = player.sushiCount + 1
                var shouldTrigger = false
                var updatedPlayer = player.copy(sushiCount = newCount)

                if (currentState.randomRouletteEnabled) {
                    when (currentState.randomRouletteTriggerType) {
                        RandomRouletteTriggerType.FIXED -> {
                            val threshold = currentState.randomRouletteFixedThreshold.coerceIn(
                                GameState.MIN_RANDOM_ROULETTE_THRESHOLD,
                                GameState.MAX_RANDOM_ROULETTE_THRESHOLD,
                            )
                            shouldTrigger = randomRouletteLogic.shouldTriggerFixed(newCount, threshold)
                        }
                        RandomRouletteTriggerType.RANDOM -> {
                            val target = player.nextRandomRouletteTarget
                                ?: randomRouletteLogic.generateInitialTarget()
                            shouldTrigger = randomRouletteLogic.shouldTriggerRandom(newCount, target)
                            updatedPlayer = if (shouldTrigger) {
                                updatedPlayer.copy(
                                    lastRandomRouletteTrigger = newCount,
                                    nextRandomRouletteTarget = randomRouletteLogic.generateNextTargetAfterTrigger(
                                        lastTrigger = newCount,
                                    ),
                                )
                            } else {
                                updatedPlayer.copy(nextRandomRouletteTarget = target)
                            }
                        }
                    }
                }

                updatedPlayer to IncrementResult(
                    newCount = newCount,
                    shouldTriggerRoulette = shouldTrigger,
                )
            }
        }
    }

    override suspend fun resetSoloCount() {
        gameMutationMutex.withLock {
            val currentState = gameState.first()
            if (!currentState.hasActiveGame || currentState.gameMode != GameMode.SOLO) {
                return@withLock
            }

            val updatedPlayers = currentState.players.map { player ->
                resetRandomRoulettePlayer(
                    player = player.copy(sushiCount = 0),
                    state = currentState,
                )
            }
            dataStore.setPlayers(updatedPlayers)
        }
    }

    override suspend fun resetPlayerCount(playerId: String) {
        gameMutationMutex.withLock {
            val currentState = gameState.first()
            if (!currentState.hasActiveGame || currentState.gameMode != GameMode.GROUP) {
                return@withLock
            }

            val updatedPlayers = currentState.players.map { player ->
                if (player.id == playerId) {
                    resetRandomRoulettePlayer(
                        player = player.copy(sushiCount = 0),
                        state = currentState,
                    )
                } else {
                    player
                }
            }
            dataStore.setPlayers(updatedPlayers)
        }
    }

    private fun createPlayer(
        id: String,
        name: String,
        config: GameSetupConfig,
    ): Player {
        val randomEnabled = config.randomRouletteEnabled &&
            config.randomRouletteTriggerType == RandomRouletteTriggerType.RANDOM

        return Player(
            id = id,
            name = name,
            sushiCount = 0,
            lastRandomRouletteTrigger = 0,
            nextRandomRouletteTarget = if (randomEnabled) {
                randomRouletteLogic.generateInitialTarget()
            } else {
                null
            },
        )
    }

    private fun resetRandomRoulettePlayer(
        player: Player,
        state: GameState,
    ): Player {
        val randomEnabled = state.randomRouletteEnabled &&
            state.randomRouletteTriggerType == RandomRouletteTriggerType.RANDOM

        return player.copy(
            lastRandomRouletteTrigger = 0,
            nextRandomRouletteTarget = if (randomEnabled) {
                randomRouletteLogic.generateInitialTarget()
            } else {
                null
            },
        )
    }

    private fun DecodedGameState.toSafeGameState(): GameState {
        val decodedState = gameState
        return if (isDecodeValid && GameStateValidator.isValid(decodedState)) {
            decodedState
        } else {
            GameState(hasActiveGame = false)
        }
    }
}
