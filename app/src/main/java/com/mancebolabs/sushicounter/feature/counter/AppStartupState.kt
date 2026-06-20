package com.mancebolabs.sushicounter.feature.counter

sealed interface AppStartupState {
    data object Loading : AppStartupState

    data object SetupRequired : AppStartupState

    data object Ready : AppStartupState
}
