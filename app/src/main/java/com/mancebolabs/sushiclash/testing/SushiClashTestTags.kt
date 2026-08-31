package com.mancebolabs.sushiclash.testing

object SushiClashTestTags {
    const val BOTTOM_NAV_BAR = "bottom_nav_bar"
    const val SOLO_SUSHI_COUNT = "solo_sushi_count"
    const val CHEF_RANDOM_EVENT_OVERLAY = "chef_random_event_overlay"
    const val ONBOARDING_CHEF_GREETING = "onboarding_chef_greeting"
    const val ONBOARDING_CHEF_TUTORIAL = "onboarding_chef_tutorial"
    const val ONBOARDING_PROGRESS_DOT = "onboarding_progress_dot"
    const val SETUP_RANDOM_ROULETTE_SWITCH = "setup_random_roulette_switch"
    const val PLAYER_NAME_INPUT = "player_name_input"
    const val WHEEL_PARTICIPANT_NAME_INPUT = "wheel_participant_name_input"
    const val SETUP_ADD_PLAYER_BUTTON = "setup_add_player_button"
    const val SETTINGS_VIEW_TUTORIAL_ROW = "settings_view_tutorial_row"
    const val HISTORY_SECTION_SOLO = "history_section_solo"
    const val HISTORY_SECTION_GROUP = "history_section_group"
    const val HISTORY_UNABLE_TO_LOAD = "history_unable_to_load"

    fun onboardingProgressDot(index: Int): String = "${ONBOARDING_PROGRESS_DOT}_$index"

    fun groupHistoryPlayerName(playerName: String): String = "group_history_player_$playerName"

    fun groupPlayerCount(playerName: String): String = "group_player_count_$playerName"

    fun groupPlayerButton(playerName: String): String = "group_player_button_$playerName"
}
