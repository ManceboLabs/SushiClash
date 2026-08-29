package com.mancebolabs.sushiclash.domain.model.achievement

data class AchievementUnlock(
    val achievementId: AchievementId,
    val unlockedAtEpochMillis: Long,
)
