package com.mancebolabs.sushiclash.domain.repository

import com.mancebolabs.sushiclash.domain.model.GameMode
import com.mancebolabs.sushiclash.domain.model.achievement.AchievementId
import com.mancebolabs.sushiclash.domain.model.achievement.AchievementPersistenceState
import com.mancebolabs.sushiclash.domain.model.achievement.AchievementUnlock
import kotlinx.coroutines.flow.Flow

interface AchievementRepository {
    val achievementState: Flow<AchievementPersistenceState>

    suspend fun onGameCompleted(
        gameMode: GameMode,
        maxSushiInGame: Int,
        totalSushiInGame: Int,
    ): List<AchievementUnlock>

    suspend fun onSushiCountUpdated(sushiCountInGame: Int): List<AchievementUnlock>

    suspend fun onRouletteSpun(): List<AchievementUnlock>

    suspend fun onAutomaticRouletteTriggered(): List<AchievementUnlock>

    suspend fun clearAchievements()
}
