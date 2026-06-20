package com.mancebolabs.sushiclash.startup

import com.mancebolabs.sushiclash.feature.counter.AppStartupState
import org.junit.Assert.assertNotEquals
import org.junit.Test

class AppStartupStateTest {

    @Test
    fun givenStartupStates_whenCompared_thenLoadingIsDistinctFromGameplayStates() {
        assertNotEquals(AppStartupState.Loading, AppStartupState.NoActiveGame)
        assertNotEquals(AppStartupState.Loading, AppStartupState.ActiveGame)
        assertNotEquals(AppStartupState.NoActiveGame, AppStartupState.ActiveGame)
    }
}
