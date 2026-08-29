package com.mancebolabs.sushiclash.domain.model.achievement

/**
 * Local achievement progress keyed for a single player profile.
 * The outer map key is reserved for future multi-profile support; use [DEFAULT_PROFILE_KEY] today.
 */
data class AchievementPersistenceState(
    val totalGamesCompleted: Int = 0,
    val totalRouletteSpins: Int = 0,
    val peakSushiInSingleGame: Int = 0,
    val lifetimeSoloSushiTotal: Int = 0,
    val lifetimeGroupSushiTotal: Int = 0,
    val hasTriggeredAutomaticRoulette: Boolean = false,
    val unlockedAtById: Map<String, Long> = emptyMap(),
) {
    fun isUnlocked(id: AchievementId): Boolean = id.key in unlockedAtById

    fun unlockedAt(id: AchievementId): Long? = unlockedAtById[id.key]

    fun progressFor(definition: AchievementDefinition): Int {
        return when (definition.type) {
            AchievementType.PEAK_SUSHI_IN_GAME -> peakSushiInSingleGame
            AchievementType.LIFETIME_SOLO_SUSHI -> lifetimeSoloSushiTotal
            AchievementType.LIFETIME_GROUP_SUSHI -> lifetimeGroupSushiTotal
            AchievementType.GAMES_COMPLETED -> totalGamesCompleted
            AchievementType.ROULETTE_SPINS -> totalRouletteSpins
            AchievementType.AUTOMATIC_ROULETTE_TRIGGERED -> if (hasTriggeredAutomaticRoulette) 1 else 0
        }
    }

    companion object {
        const val DEFAULT_PROFILE_KEY = "default"
    }
}
