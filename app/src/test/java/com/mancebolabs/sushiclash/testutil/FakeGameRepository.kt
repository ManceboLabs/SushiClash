package com.mancebolabs.sushiclash.testutil

import com.mancebolabs.sushiclash.data.datastore.AppPreferencesDataStore
import com.mancebolabs.sushiclash.domain.model.FinishGameResult
import com.mancebolabs.sushiclash.domain.model.GameMode
import com.mancebolabs.sushiclash.domain.model.GameSetupConfig
import com.mancebolabs.sushiclash.domain.model.GameState
import com.mancebolabs.sushiclash.domain.model.GameStateValidator
import com.mancebolabs.sushiclash.domain.model.IncrementResult
import com.mancebolabs.sushiclash.domain.model.Player
import com.mancebolabs.sushiclash.domain.model.RandomRouletteTriggerType
import com.mancebolabs.sushiclash.domain.repository.GameRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object TestGameStates {
    fun soloActive(
        sessionId: String? = "test-session",
        count: Int = 0,
        randomRouletteEnabled: Boolean = false,
        triggerType: RandomRouletteTriggerType = RandomRouletteTriggerType.FIXED,
        fixedThreshold: Int = 5,
        nextTarget: Int? = null,
    ): GameState {
        return GameState(
            hasActiveGame = true,
            sessionId = sessionId,
            gameMode = GameMode.SOLO,
            players = listOf(
                Player(
                    id = AppPreferencesDataStore.SOLO_PLAYER_ID,
                    name = "",
                    sushiCount = count,
                    nextRandomRouletteTarget = nextTarget,
                ),
            ),
            randomRouletteEnabled = randomRouletteEnabled,
            randomRouletteTriggerType = triggerType,
            randomRouletteFixedThreshold = fixedThreshold,
        )
    }

    fun groupActive(
        players: List<Player>,
        sessionId: String? = "test-session",
        randomRouletteEnabled: Boolean = false,
        triggerType: RandomRouletteTriggerType = RandomRouletteTriggerType.FIXED,
        fixedThreshold: Int = 5,
    ): GameState {
        return GameState(
            hasActiveGame = true,
            sessionId = sessionId,
            gameMode = GameMode.GROUP,
            players = players,
            randomRouletteEnabled = randomRouletteEnabled,
            randomRouletteTriggerType = triggerType,
            randomRouletteFixedThreshold = fixedThreshold,
        )
    }
}

class FakeGameRepository(
    initialState: GameState = GameState(),
) : GameRepository {
    private val _gameState = MutableStateFlow(initialState)
    override val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    var clearActiveGameCallCount = 0
    var restoreGameStateCallCount = 0
    var lastSetupConfig: GameSetupConfig? = null
    var finishGameWithSavingCallCount = 0
    var finishGameWithoutSavingCallCount = 0
    val finishGameWithSavingResults = mutableListOf<FinishGameResult>()
    val finishGameWithoutSavingResults = mutableListOf<FinishGameResult>()
    var finishGameWithSavingGate: CompletableDeferred<Unit>? = null
    var finishGameWithSavingThrowable: Throwable? = null

    override suspend fun restoreGameState(): GameState {
        restoreGameStateCallCount++
        val current = _gameState.value
        if (GameStateValidator.isValid(current)) return current
        clearActiveGame()
        return _gameState.value
    }

    private fun clearActiveGame() {
        clearActiveGameCallCount++
        _gameState.value = GameState(hasActiveGame = false)
    }

    override suspend fun finishGameWithSaving(): FinishGameResult {
        finishGameWithSavingCallCount++
        finishGameWithSavingGate?.await()
        finishGameWithSavingThrowable?.let { throw it }
        val result = finishGameWithSavingResults.removeFirstOrNull()
            ?: if (_gameState.value.hasActiveGame) {
                FinishGameResult.Success
            } else {
                FinishGameResult.NoActiveGame
            }
        if (result is FinishGameResult.Success) clearActiveGame()
        return result
    }

    override suspend fun finishGameWithoutSaving(): FinishGameResult {
        finishGameWithoutSavingCallCount++
        val result = finishGameWithoutSavingResults.removeFirstOrNull()
            ?: if (_gameState.value.hasActiveGame) {
                FinishGameResult.Success
            } else {
                FinishGameResult.NoActiveGame
            }
        if (result is FinishGameResult.Success) clearActiveGame()
        return result
    }

    override suspend fun completeSetup(config: GameSetupConfig) {
        lastSetupConfig = config
        _gameState.value = when (config.gameMode) {
            GameMode.SOLO -> TestGameStates.soloActive(
                randomRouletteEnabled = config.randomRouletteEnabled,
                triggerType = config.randomRouletteTriggerType,
                fixedThreshold = config.randomRouletteFixedThreshold,
            )
            GameMode.GROUP -> TestGameStates.groupActive(
                players = config.playerNames.mapIndexed { index, name ->
                    Player(id = "player-$index", name = name)
                },
                randomRouletteEnabled = config.randomRouletteEnabled,
                triggerType = config.randomRouletteTriggerType,
                fixedThreshold = config.randomRouletteFixedThreshold,
            )
        }
    }

    override suspend fun incrementPlayerCount(playerId: String): IncrementResult {
        val current = _gameState.value
        if (!current.hasActiveGame) {
            return IncrementResult(newCount = 0, shouldTriggerRoulette = false)
        }
        var newCount = 0
        var shouldTrigger = false
        val updatedPlayers = current.players.map { player ->
            if (player.id != playerId) return@map player
            newCount = player.sushiCount + 1
            if (current.randomRouletteEnabled &&
                current.randomRouletteTriggerType == RandomRouletteTriggerType.FIXED &&
                newCount % current.randomRouletteFixedThreshold == 0
            ) {
                shouldTrigger = true
            }
            player.copy(sushiCount = newCount)
        }
        _gameState.value = current.copy(players = updatedPlayers)
        return IncrementResult(newCount = newCount, shouldTriggerRoulette = shouldTrigger)
    }

    override suspend fun resetSoloCount() {
        val current = _gameState.value
        if (!current.hasActiveGame || current.gameMode != GameMode.SOLO) return
        _gameState.value = current.copy(
            players = current.players.map { it.copy(sushiCount = 0) },
        )
    }

    override suspend fun resetPlayerCount(playerId: String) {
        val current = _gameState.value
        if (!current.hasActiveGame || current.gameMode != GameMode.GROUP) return
        _gameState.value = current.copy(
            players = current.players.map { player ->
                if (player.id == playerId) player.copy(sushiCount = 0) else player
            },
        )
    }

    fun setGameState(state: GameState) {
        _gameState.value = state
    }
}
