package com.mancebolabs.sushiclash.feature.counter

/**
 * High-level counter screen lifecycle.
 *
 * [Loading] avoids flashing setup or empty UI before persisted state is read.
 * Startup waits here while first-launch onboarding is shown at app level; the setup popup
 * is never opened automatically.
 * [NoActiveGame] is shown when there is no active match; setup opens only after user action.
 * [ActiveGame] shows counters and gameplay controls.
 */
sealed interface AppStartupState {
    data object Loading : AppStartupState

    data object NoActiveGame : AppStartupState

    data object ActiveGame : AppStartupState
}
