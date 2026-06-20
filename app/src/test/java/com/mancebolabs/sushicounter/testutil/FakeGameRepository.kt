package com.mancebolabs.sushicounter.testutil

import com.mancebolabs.sushicounter.data.datastore.AppPreferencesDataStore
import com.mancebolabs.sushicounter.domain.model.FinishedGameSnapshot
import com.mancebolabs.sushicounter.domain.model.GameMode
import com.mancebolabs.sushicounter.domain.model.GameSetupConfig
import com.mancebolabs.sushicounter.domain.model.GameState
import com.mancebolabs.sushicounter.domain.model.IncrementResult
import com.mancebolabs.sushicounter.domain.model.Player
import com.mancebolabs.sushicounter.domain.model.RandomRouletteTriggerType
import com.mancebolabs.sushicounter.domain.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object TestGameStates {
    fun soloActive(
        count: Int = 0,
        randomRouletteEnabled: Boolean = false,
        triggerType: RandomRouletteTriggerType = RandomRouletteTriggerType.FIXED,
        fixedThreshold: Int = 5,
        nextTarget: Int? = null,
    ): GameState {
        return GameState(
            hasActiveGame = true,
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
        randomRouletteEnabled: Boolean = false,
        triggerType: RandomRouletteTriggerType = RandomRouletteTriggerType.FIXED,
        fixedThreshold: Int = 5,
    ): GameState {
        return GameState(
            hasActiveGame = true,
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

    var finishActiveGameCallCount = 0
    var lastFinishedSnapshot: FinishedGameSnapshot? = null
    var lastSetupConfig: GameSetupConfig? = null

    override suspend fun finishActiveGame(): FinishedGameSnapshot? {
        finishActiveGameCallCount++
        val current = _gameState.value
        if (!current.hasActiveGame || current.gameMode == null) {
            return null
        }
        val snapshot = FinishedGameSnapshot(
            gameMode = current.gameMode,
            soloCount = if (current.gameMode == GameMode.SOLO) current.soloCount else null,
            playerScores = current.players.map { player ->
                com.mancebolabs.sushicounter.domain.model.PlayerScore(
                    playerName = player.name,
                    sushiCount = player.sushiCount,
                )
            },
            randomRouletteEnabled = current.randomRouletteEnabled,
            randomRouletteTriggerType = current.randomRouletteTriggerType,
            randomRouletteFixedThreshold = current.randomRouletteFixedThreshold,
            finishedAt = 1_700_000_000_000L,
        )
        lastFinishedSnapshot = snapshot
        _gameState.value = GameState(hasActiveGame = false)
        return snapshot
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
