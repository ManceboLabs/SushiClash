package com.mancebolabs.sushiclash.testutil

import com.mancebolabs.sushiclash.domain.model.GameMode
import com.mancebolabs.sushiclash.domain.achievement.AchievementEvaluator
import com.mancebolabs.sushiclash.domain.model.achievement.AchievementPersistenceState
import com.mancebolabs.sushiclash.domain.model.achievement.AchievementUnlock
import com.mancebolabs.sushiclash.domain.repository.AchievementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeAchievementRepository(
    initialState: AchievementPersistenceState = AchievementPersistenceState(),
    private val clock: () -> Long = { 1_000L },
) : AchievementRepository {

    private val _achievementState = MutableStateFlow(initialState)

    override val achievementState: Flow<AchievementPersistenceState> = _achievementState.asStateFlow()

    var onGameCompletedCallCount = 0
    var onSushiCountUpdatedCallCount = 0
    var onRouletteSpunCallCount = 0
    var onAutomaticRouletteTriggeredCallCount = 0

    var lastGameMode: GameMode? = null
    var lastMaxSushiInGame: Int? = null
    var lastTotalSushiInGame: Int? = null
    var lastSushiCountInGame: Int? = null

    override suspend fun onGameCompleted(
        gameMode: GameMode,
        maxSushiInGame: Int,
        totalSushiInGame: Int,
    ): List<AchievementUnlock> {
        onGameCompletedCallCount++
        lastGameMode = gameMode
        lastMaxSushiInGame = maxSushiInGame
        lastTotalSushiInGame = totalSushiInGame
        return apply { current ->
            AchievementEvaluator.onGameCompleted(
                current,
                gameMode,
                maxSushiInGame,
                totalSushiInGame,
                clock(),
            )
        }
    }

    override suspend fun onSushiCountUpdated(sushiCountInGame: Int): List<AchievementUnlock> {
        onSushiCountUpdatedCallCount++
        lastSushiCountInGame = sushiCountInGame
        return apply { current ->
            AchievementEvaluator.onSushiCountUpdated(current, sushiCountInGame, clock())
        }
    }

    override suspend fun onRouletteSpun(): List<AchievementUnlock> {
        onRouletteSpunCallCount++
        return apply { current ->
            AchievementEvaluator.onRouletteSpun(current, clock())
        }
    }

    override suspend fun onAutomaticRouletteTriggered(): List<AchievementUnlock> {
        onAutomaticRouletteTriggeredCallCount++
        return apply { current ->
            AchievementEvaluator.onAutomaticRouletteTriggered(current, clock())
        }
    }

    var clearAchievementsCallCount = 0
    var clearAchievementsThrowable: Throwable? = null

    override suspend fun clearAchievements() {
        clearAchievementsCallCount++
        clearAchievementsThrowable?.let { throw it }
        _achievementState.value = AchievementPersistenceState()
    }

    fun setAchievementState(state: AchievementPersistenceState) {
        _achievementState.value = state
    }

    private inline fun apply(
        evaluate: (AchievementPersistenceState) -> com.mancebolabs.sushiclash.domain.achievement.AchievementEvaluationResult,
    ): List<AchievementUnlock> {
        val result = evaluate(_achievementState.value)
        _achievementState.value = result.state
        return result.newlyUnlocked
    }
}
