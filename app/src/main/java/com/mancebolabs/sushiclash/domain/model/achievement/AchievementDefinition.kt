package com.mancebolabs.sushiclash.domain.model.achievement

data class AchievementDefinition(
    val id: AchievementId,
    val category: AchievementCategory,
    val type: AchievementType,
    val target: Int,
)
