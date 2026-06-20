package com.mancebolabs.sushicounter.feature.counter

sealed interface AppStartupState {
    data object Loading : AppStartupState

    data object NoActiveGame : AppStartupState

    data object ActiveGame : AppStartupState
}
