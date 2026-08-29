package com.mancebolabs.sushiclash.domain.model.achievement

object AchievementCatalog {
    private val lifetimeSushiThresholds = listOf(50, 100, 250, 500, 1_000, 2_500, 5_000, 10_000)

    private val soloLifetimeDefinitions = lifetimeSushiThresholds.map { threshold ->
        AchievementDefinition(
            id = soloLifetimeIdForThreshold(threshold),
            category = AchievementCategory.SOLO_LIFETIME_SUSHI,
            type = AchievementType.LIFETIME_SOLO_SUSHI,
            target = threshold,
        )
    }

    private val groupLifetimeDefinitions = lifetimeSushiThresholds.map { threshold ->
        AchievementDefinition(
            id = groupLifetimeIdForThreshold(threshold),
            category = AchievementCategory.GROUP_LIFETIME_SUSHI,
            type = AchievementType.LIFETIME_GROUP_SUSHI,
            target = threshold,
        )
    }

    val definitions: List<AchievementDefinition> = listOf(
        AchievementDefinition(AchievementId.SUSHI_10, AchievementCategory.SUSHI, AchievementType.PEAK_SUSHI_IN_GAME, 10),
        AchievementDefinition(AchievementId.SUSHI_20, AchievementCategory.SUSHI, AchievementType.PEAK_SUSHI_IN_GAME, 20),
        AchievementDefinition(AchievementId.SUSHI_30, AchievementCategory.SUSHI, AchievementType.PEAK_SUSHI_IN_GAME, 30),
        AchievementDefinition(AchievementId.SUSHI_40, AchievementCategory.SUSHI, AchievementType.PEAK_SUSHI_IN_GAME, 40),
        AchievementDefinition(AchievementId.SUSHI_50, AchievementCategory.SUSHI, AchievementType.PEAK_SUSHI_IN_GAME, 50),
        AchievementDefinition(AchievementId.SUSHI_100, AchievementCategory.SUSHI, AchievementType.PEAK_SUSHI_IN_GAME, 100),
    ) + soloLifetimeDefinitions + groupLifetimeDefinitions + listOf(
        AchievementDefinition(AchievementId.GAMES_1, AchievementCategory.GAMES, AchievementType.GAMES_COMPLETED, 1),
        AchievementDefinition(AchievementId.GAMES_10, AchievementCategory.GAMES, AchievementType.GAMES_COMPLETED, 10),
        AchievementDefinition(AchievementId.GAMES_20, AchievementCategory.GAMES, AchievementType.GAMES_COMPLETED, 20),
        AchievementDefinition(AchievementId.GAMES_30, AchievementCategory.GAMES, AchievementType.GAMES_COMPLETED, 30),
        AchievementDefinition(AchievementId.GAMES_50, AchievementCategory.GAMES, AchievementType.GAMES_COMPLETED, 50),
        AchievementDefinition(AchievementId.GAMES_100, AchievementCategory.GAMES, AchievementType.GAMES_COMPLETED, 100),
        AchievementDefinition(AchievementId.ROULETTE_FIRST_SPIN, AchievementCategory.ROULETTE, AchievementType.ROULETTE_SPINS, 1),
        AchievementDefinition(AchievementId.ROULETTE_SPINS_10, AchievementCategory.ROULETTE, AchievementType.ROULETTE_SPINS, 10),
        AchievementDefinition(AchievementId.ROULETTE_SPINS_20, AchievementCategory.ROULETTE, AchievementType.ROULETTE_SPINS, 20),
        AchievementDefinition(AchievementId.ROULETTE_SPINS_30, AchievementCategory.ROULETTE, AchievementType.ROULETTE_SPINS, 30),
        AchievementDefinition(AchievementId.ROULETTE_SPINS_40, AchievementCategory.ROULETTE, AchievementType.ROULETTE_SPINS, 40),
        AchievementDefinition(AchievementId.ROULETTE_SPINS_50, AchievementCategory.ROULETTE, AchievementType.ROULETTE_SPINS, 50),
        AchievementDefinition(AchievementId.ROULETTE_AUTO_FIRST, AchievementCategory.ROULETTE, AchievementType.AUTOMATIC_ROULETTE_TRIGGERED, 1),
    )

    val byId: Map<AchievementId, AchievementDefinition> = definitions.associateBy { it.id }

    private fun soloLifetimeIdForThreshold(threshold: Int): AchievementId {
        return when (threshold) {
            50 -> AchievementId.SOLO_TOTAL_50
            100 -> AchievementId.SOLO_TOTAL_100
            250 -> AchievementId.SOLO_TOTAL_250
            500 -> AchievementId.SOLO_TOTAL_500
            1_000 -> AchievementId.SOLO_TOTAL_1000
            2_500 -> AchievementId.SOLO_TOTAL_2500
            5_000 -> AchievementId.SOLO_TOTAL_5000
            10_000 -> AchievementId.SOLO_TOTAL_10000
            else -> error("Unsupported solo lifetime sushi threshold: $threshold")
        }
    }

    private fun groupLifetimeIdForThreshold(threshold: Int): AchievementId {
        return when (threshold) {
            50 -> AchievementId.GROUP_TOTAL_50
            100 -> AchievementId.GROUP_TOTAL_100
            250 -> AchievementId.GROUP_TOTAL_250
            500 -> AchievementId.GROUP_TOTAL_500
            1_000 -> AchievementId.GROUP_TOTAL_1000
            2_500 -> AchievementId.GROUP_TOTAL_2500
            5_000 -> AchievementId.GROUP_TOTAL_5000
            10_000 -> AchievementId.GROUP_TOTAL_10000
            else -> error("Unsupported group lifetime sushi threshold: $threshold")
        }
    }
}
