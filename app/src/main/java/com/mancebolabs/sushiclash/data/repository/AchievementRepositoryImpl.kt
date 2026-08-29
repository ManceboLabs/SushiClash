package com.mancebolabs.sushiclash.data.repository

import com.mancebolabs.sushiclash.data.datastore.AppPreferencesDataStore
import com.mancebolabs.sushiclash.domain.model.GameMode
import com.mancebolabs.sushiclash.domain.achievement.AchievementEvaluator
import com.mancebolabs.sushiclash.domain.model.achievement.AchievementPersistenceState
import com.mancebolabs.sushiclash.domain.model.achievement.AchievementUnlock
import com.mancebolabs.sushiclash.domain.repository.AchievementRepository
import com.mancebolabs.sushiclash.feature.achievements.AchievementNotificationDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class AchievementRepositoryImpl(
    private val dataStore: AppPreferencesDataStore,
    private val clock: () -> Long = System::currentTimeMillis,
) : AchievementRepository {

    override val achievementState: Flow<AchievementPersistenceState> = dataStore.achievementState

    override suspend fun onGameCompleted(
        gameMode: GameMode,
        maxSushiInGame: Int,
        totalSushiInGame: Int,
    ): List<AchievementUnlock> {
        return applyEvent { current ->
            AchievementEvaluator.onGameCompleted(
                state = current,
                gameMode = gameMode,
                maxSushiInGame = maxSushiInGame,
                totalSushiInGame = totalSushiInGame,
                unlockedAtEpochMillis = clock(),
            )
        }
    }

    override suspend fun onSushiCountUpdated(sushiCountInGame: Int): List<AchievementUnlock> {
        return applyEvent { current ->
            AchievementEvaluator.onSushiCountUpdated(
                state = current,
                sushiCountInGame = sushiCountInGame,
                unlockedAtEpochMillis = clock(),
            )
        }
    }

    override suspend fun onRouletteSpun(): List<AchievementUnlock> {
        return applyEvent { current ->
            AchievementEvaluator.onRouletteSpun(
                state = current,
                unlockedAtEpochMillis = clock(),
            )
        }
    }

    override suspend fun onAutomaticRouletteTriggered(): List<AchievementUnlock> {
        return applyEvent { current ->
            AchievementEvaluator.onAutomaticRouletteTriggered(
                state = current,
                unlockedAtEpochMillis = clock(),
            )
        }
    }

    override suspend fun clearAchievements() {
        dataStore.setAchievementState(AchievementPersistenceState())
    }

    private suspend fun applyEvent(
        evaluate: (AchievementPersistenceState) -> com.mancebolabs.sushiclash.domain.achievement.AchievementEvaluationResult,
    ): List<AchievementUnlock> {
        val current = dataStore.achievementState.first()
        val result = evaluate(current)
        if (result.state != current) {
            dataStore.setAchievementState(result.state)
        }
        if (result.newlyUnlocked.isNotEmpty()) {
            AchievementNotificationDispatcher.notifyAll(result.newlyUnlocked)
        }
        return result.newlyUnlocked
    }
}
