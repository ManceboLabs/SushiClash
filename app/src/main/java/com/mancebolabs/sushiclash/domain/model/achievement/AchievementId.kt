package com.mancebolabs.sushiclash.domain.model.achievement

enum class AchievementId(val key: String) {
    SUSHI_10("sushi_10"),
    SUSHI_20("sushi_20"),
    SUSHI_30("sushi_30"),
    SUSHI_40("sushi_40"),
    SUSHI_50("sushi_50"),
    SUSHI_100("sushi_100"),
    SOLO_TOTAL_50("solo_total_50"),
    SOLO_TOTAL_100("solo_total_100"),
    SOLO_TOTAL_250("solo_total_250"),
    SOLO_TOTAL_500("solo_total_500"),
    SOLO_TOTAL_1000("solo_total_1000"),
    SOLO_TOTAL_2500("solo_total_2500"),
    SOLO_TOTAL_5000("solo_total_5000"),
    SOLO_TOTAL_10000("solo_total_10000"),
    GROUP_TOTAL_50("group_total_50"),
    GROUP_TOTAL_100("group_total_100"),
    GROUP_TOTAL_250("group_total_250"),
    GROUP_TOTAL_500("group_total_500"),
    GROUP_TOTAL_1000("group_total_1000"),
    GROUP_TOTAL_2500("group_total_2500"),
    GROUP_TOTAL_5000("group_total_5000"),
    GROUP_TOTAL_10000("group_total_10000"),
    GAMES_1("games_1"),
    GAMES_10("games_10"),
    GAMES_20("games_20"),
    GAMES_30("games_30"),
    GAMES_50("games_50"),
    GAMES_100("games_100"),
    ROULETTE_FIRST_SPIN("roulette_first_spin"),
    ROULETTE_SPINS_10("roulette_spins_10"),
    ROULETTE_SPINS_20("roulette_spins_20"),
    ROULETTE_SPINS_30("roulette_spins_30"),
    ROULETTE_SPINS_40("roulette_spins_40"),
    ROULETTE_SPINS_50("roulette_spins_50"),
    ROULETTE_AUTO_FIRST("roulette_auto_first"),
    ;

    companion object {
        fun fromKey(key: String): AchievementId? = entries.firstOrNull { it.key == key }
    }
}
