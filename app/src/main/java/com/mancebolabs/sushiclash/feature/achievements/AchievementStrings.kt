package com.mancebolabs.sushiclash.feature.achievements

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mancebolabs.sushiclash.R
import com.mancebolabs.sushiclash.domain.model.achievement.AchievementCategory
import com.mancebolabs.sushiclash.domain.model.achievement.AchievementId

@Composable
fun achievementTitle(id: AchievementId): String {
    return stringResource(
        when (id) {
            AchievementId.SUSHI_10 -> R.string.achievement_sushi_10_title
            AchievementId.SUSHI_20 -> R.string.achievement_sushi_20_title
            AchievementId.SUSHI_30 -> R.string.achievement_sushi_30_title
            AchievementId.SUSHI_40 -> R.string.achievement_sushi_40_title
            AchievementId.SUSHI_50 -> R.string.achievement_sushi_50_title
            AchievementId.SUSHI_100 -> R.string.achievement_sushi_100_title
            AchievementId.SOLO_TOTAL_50 -> R.string.achievement_solo_total_50_title
            AchievementId.SOLO_TOTAL_100 -> R.string.achievement_solo_total_100_title
            AchievementId.SOLO_TOTAL_250 -> R.string.achievement_solo_total_250_title
            AchievementId.SOLO_TOTAL_500 -> R.string.achievement_solo_total_500_title
            AchievementId.SOLO_TOTAL_1000 -> R.string.achievement_solo_total_1000_title
            AchievementId.SOLO_TOTAL_2500 -> R.string.achievement_solo_total_2500_title
            AchievementId.SOLO_TOTAL_5000 -> R.string.achievement_solo_total_5000_title
            AchievementId.SOLO_TOTAL_10000 -> R.string.achievement_solo_total_10000_title
            AchievementId.GROUP_TOTAL_50 -> R.string.achievement_group_total_50_title
            AchievementId.GROUP_TOTAL_100 -> R.string.achievement_group_total_100_title
            AchievementId.GROUP_TOTAL_250 -> R.string.achievement_group_total_250_title
            AchievementId.GROUP_TOTAL_500 -> R.string.achievement_group_total_500_title
            AchievementId.GROUP_TOTAL_1000 -> R.string.achievement_group_total_1000_title
            AchievementId.GROUP_TOTAL_2500 -> R.string.achievement_group_total_2500_title
            AchievementId.GROUP_TOTAL_5000 -> R.string.achievement_group_total_5000_title
            AchievementId.GROUP_TOTAL_10000 -> R.string.achievement_group_total_10000_title
            AchievementId.GAMES_1 -> R.string.achievement_games_1_title
            AchievementId.GAMES_10 -> R.string.achievement_games_10_title
            AchievementId.GAMES_20 -> R.string.achievement_games_20_title
            AchievementId.GAMES_30 -> R.string.achievement_games_30_title
            AchievementId.GAMES_50 -> R.string.achievement_games_50_title
            AchievementId.GAMES_100 -> R.string.achievement_games_100_title
            AchievementId.ROULETTE_FIRST_SPIN -> R.string.achievement_roulette_first_spin_title
            AchievementId.ROULETTE_SPINS_10 -> R.string.achievement_roulette_spins_10_title
            AchievementId.ROULETTE_SPINS_20 -> R.string.achievement_roulette_spins_20_title
            AchievementId.ROULETTE_SPINS_30 -> R.string.achievement_roulette_spins_30_title
            AchievementId.ROULETTE_SPINS_40 -> R.string.achievement_roulette_spins_40_title
            AchievementId.ROULETTE_SPINS_50 -> R.string.achievement_roulette_spins_50_title
            AchievementId.ROULETTE_AUTO_FIRST -> R.string.achievement_roulette_auto_first_title
        },
    )
}

@Composable
fun achievementDescription(id: AchievementId): String {
    return stringResource(
        when (id) {
            AchievementId.SUSHI_10 -> R.string.achievement_sushi_10_description
            AchievementId.SUSHI_20 -> R.string.achievement_sushi_20_description
            AchievementId.SUSHI_30 -> R.string.achievement_sushi_30_description
            AchievementId.SUSHI_40 -> R.string.achievement_sushi_40_description
            AchievementId.SUSHI_50 -> R.string.achievement_sushi_50_description
            AchievementId.SUSHI_100 -> R.string.achievement_sushi_100_description
            AchievementId.SOLO_TOTAL_50 -> R.string.achievement_solo_total_50_description
            AchievementId.SOLO_TOTAL_100 -> R.string.achievement_solo_total_100_description
            AchievementId.SOLO_TOTAL_250 -> R.string.achievement_solo_total_250_description
            AchievementId.SOLO_TOTAL_500 -> R.string.achievement_solo_total_500_description
            AchievementId.SOLO_TOTAL_1000 -> R.string.achievement_solo_total_1000_description
            AchievementId.SOLO_TOTAL_2500 -> R.string.achievement_solo_total_2500_description
            AchievementId.SOLO_TOTAL_5000 -> R.string.achievement_solo_total_5000_description
            AchievementId.SOLO_TOTAL_10000 -> R.string.achievement_solo_total_10000_description
            AchievementId.GROUP_TOTAL_50 -> R.string.achievement_group_total_50_description
            AchievementId.GROUP_TOTAL_100 -> R.string.achievement_group_total_100_description
            AchievementId.GROUP_TOTAL_250 -> R.string.achievement_group_total_250_description
            AchievementId.GROUP_TOTAL_500 -> R.string.achievement_group_total_500_description
            AchievementId.GROUP_TOTAL_1000 -> R.string.achievement_group_total_1000_description
            AchievementId.GROUP_TOTAL_2500 -> R.string.achievement_group_total_2500_description
            AchievementId.GROUP_TOTAL_5000 -> R.string.achievement_group_total_5000_description
            AchievementId.GROUP_TOTAL_10000 -> R.string.achievement_group_total_10000_description
            AchievementId.GAMES_1 -> R.string.achievement_games_1_description
            AchievementId.GAMES_10 -> R.string.achievement_games_10_description
            AchievementId.GAMES_20 -> R.string.achievement_games_20_description
            AchievementId.GAMES_30 -> R.string.achievement_games_30_description
            AchievementId.GAMES_50 -> R.string.achievement_games_50_description
            AchievementId.GAMES_100 -> R.string.achievement_games_100_description
            AchievementId.ROULETTE_FIRST_SPIN -> R.string.achievement_roulette_first_spin_description
            AchievementId.ROULETTE_SPINS_10 -> R.string.achievement_roulette_spins_10_description
            AchievementId.ROULETTE_SPINS_20 -> R.string.achievement_roulette_spins_20_description
            AchievementId.ROULETTE_SPINS_30 -> R.string.achievement_roulette_spins_30_description
            AchievementId.ROULETTE_SPINS_40 -> R.string.achievement_roulette_spins_40_description
            AchievementId.ROULETTE_SPINS_50 -> R.string.achievement_roulette_spins_50_description
            AchievementId.ROULETTE_AUTO_FIRST -> R.string.achievement_roulette_auto_first_description
        },
    )
}

@Composable
fun achievementCategoryTitle(category: AchievementCategory): String {
    return stringResource(
        when (category) {
            AchievementCategory.SUSHI -> R.string.achievements_category_sushi
            AchievementCategory.SOLO_LIFETIME_SUSHI -> R.string.achievements_category_solo_lifetime_sushi
            AchievementCategory.GROUP_LIFETIME_SUSHI -> R.string.achievements_category_group_lifetime_sushi
            AchievementCategory.GAMES -> R.string.achievements_category_games
            AchievementCategory.ROULETTE -> R.string.achievements_category_roulette
        },
    )
}
