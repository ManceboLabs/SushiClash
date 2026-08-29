package com.mancebolabs.sushiclash.feature.achievements

import com.mancebolabs.sushiclash.domain.model.achievement.AchievementId
import com.mancebolabs.sushiclash.domain.model.achievement.AchievementPersistenceState
import com.mancebolabs.sushiclash.domain.achievement.AchievementEvaluator
import com.mancebolabs.sushiclash.domain.model.achievement.AchievementCatalog
import com.mancebolabs.sushiclash.domain.model.achievement.AchievementCategory
import com.mancebolabs.sushiclash.domain.model.achievement.AchievementDefinition

data class AchievementItemUiState(
    val id: AchievementId,
    val isUnlocked: Boolean,
    val progress: Int,
    val target: Int,
    val unlockedAtEpochMillis: Long?,
)

data class AchievementCategoryUiState(
    val category: AchievementCategory,
    val achievements: List<AchievementItemUiState>,
)

data class AchievementsUiState(
    val categories: List<AchievementCategoryUiState> = emptyList(),
    val unlockedCount: Int = 0,
    val totalCount: Int = AchievementCatalog.definitions.size,
)

fun buildAchievementsUiState(state: AchievementPersistenceState): AchievementsUiState {
    val items = AchievementCatalog.definitions.map { definition ->
        AchievementItemUiState(
            id = definition.id,
            isUnlocked = AchievementEvaluator.isUnlocked(state, definition.id),
            progress = AchievementEvaluator.progressForDisplay(state, definition),
            target = definition.target,
            unlockedAtEpochMillis = state.unlockedAt(definition.id),
        )
    }

    val categories = AchievementCategory.entries.map { category ->
        AchievementCategoryUiState(
            category = category,
            achievements = items.filter { item ->
                AchievementCatalog.byId.getValue(item.id).category == category
            },
        )
    }

    return AchievementsUiState(
        categories = categories,
        unlockedCount = items.count { it.isUnlocked },
        totalCount = items.size,
    )
}
