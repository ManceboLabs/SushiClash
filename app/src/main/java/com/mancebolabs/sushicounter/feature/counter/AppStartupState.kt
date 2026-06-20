package com.mancebolabs.sushicounter.feature.counter

/**
 * High-level counter screen lifecycle.
 *
 * [Loading] avoids flashing setup or empty UI before persisted state is read.
 * [NoActiveGame] is shown when there is no active match; setup opens only after user action.
 * [ActiveGame] shows counters and gameplay controls.
 */
sealed interface AppStartupState {
    data object Loading : AppStartupState

    data object NoActiveGame : AppStartupState

    data object ActiveGame : AppStartupState
}
