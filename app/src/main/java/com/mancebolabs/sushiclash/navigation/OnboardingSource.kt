package com.mancebolabs.sushiclash.navigation

/**
 * Identifies why onboarding was opened so dismiss behavior can differ.
 *
 * [FIRST_LAUNCH] persists completion and lands on Counter.
 * [SETTINGS] pops back to Settings without touching persisted app state.
 */
enum class OnboardingSource {
    FIRST_LAUNCH,
    SETTINGS,
}
