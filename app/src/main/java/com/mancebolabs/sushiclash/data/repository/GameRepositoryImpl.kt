package com.mancebolabs.sushiclash.data.repository

import com.mancebolabs.sushiclash.data.datastore.AppPreferencesDataStore
import com.mancebolabs.sushiclash.domain.model.FinishedGameSnapshot
import com.mancebolabs.sushiclash.domain.model.GameMode
import com.mancebolabs.sushiclash.domain.model.GameSetupConfig
import com.mancebolabs.sushiclash.domain.model.GameState
import com.mancebolabs.sushiclash.domain.model.IncrementResult
import com.mancebolabs.sushiclash.domain.model.Player
import com.mancebolabs.sushiclash.domain.model.PlayerScore
import com.mancebolabs.sushiclash.domain.model.RandomRouletteLogic
import com.mancebolabs.sushiclash.domain.model.RandomRouletteTriggerType
import com.mancebolabs.sushiclash.domain.repository.GameRepository
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class GameRepositoryImpl(
    private val dataStore: AppPreferencesDataStore,
    private val randomRouletteLogic: RandomRouletteLogic = RandomRouletteLogic.Default,
) : GameRepository {

    override val gameState: Flow<GameState> = dataStore.gameState

    override suspend fun finishActiveGame(): FinishedGameSnapshot? {
        val currentState = dataStore.gameState.first()
        if (!currentState.hasActiveGame || currentState.gameMode == null) {
            return null
        }

        // Snapshot is taken before clearing persistence so the user can still choose to save it.
        val snapshot = FinishedGameSnapshot(
            gameMode = currentState.gameMode,
            soloCount = if (currentState.gameMode == GameMode.SOLO) {
                currentState.soloCount
            } else {
                null
            },
            playerScores = currentState.players.map { player ->
                PlayerScore(
                    playerName = player.name,
                    sushiCount = player.sushiCount,
                )
            },
            randomRouletteEnabled = currentState.randomRouletteEnabled,
            randomRouletteTriggerType = currentState.randomRouletteTriggerType,
            randomRouletteFixedThreshold = currentState.randomRouletteFixedThreshold,
            finishedAt = System.currentTimeMillis(),
        )

        dataStore.clearActiveGame()
        return snapshot
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

        dataStore.saveGameState(
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

    override suspend fun incrementPlayerCount(playerId: String): IncrementResult {
        val currentState = dataStore.gameState.first()
        if (!currentState.hasActiveGame) {
            return IncrementResult(newCount = 0, shouldTriggerRoulette = false)
        }
        var newCount = 0
        var shouldTrigger = false

        val updatedPlayers = currentState.players.map { player ->
            if (player.id != playerId) {
                return@map player
            }

            newCount = player.sushiCount + 1
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

            updatedPlayer
        }

        dataStore.setPlayers(updatedPlayers)
        return IncrementResult(
            newCount = newCount,
            shouldTriggerRoulette = shouldTrigger,
        )
    }

    override suspend fun resetSoloCount() {
        val currentState = dataStore.gameState.first()
        if (!currentState.hasActiveGame || currentState.gameMode != GameMode.SOLO) return

        val updatedPlayers = currentState.players.map { player ->
            resetRandomRoulettePlayer(
                player = player.copy(sushiCount = 0),
                state = currentState,
            )
        }
        dataStore.setPlayers(updatedPlayers)
    }

    override suspend fun resetPlayerCount(playerId: String) {
        val currentState = dataStore.gameState.first()
        if (!currentState.hasActiveGame || currentState.gameMode != GameMode.GROUP) return

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
}
