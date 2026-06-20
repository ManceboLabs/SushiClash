package com.mancebolabs.sushiclash.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingNavigationTest {

    @Test
    fun givenMainTabRoutes_whenChecked_thenOnboardingIsExcluded() {
        val mainTabRoutes = setOf(
            SushiDestination.Counter.route,
            SushiDestination.Wheel.route,
            SushiDestination.History.route,
            SushiDestination.Settings.route,
        )

        assertFalse(SushiDestination.Onboarding.route(OnboardingSource.FIRST_LAUNCH) in mainTabRoutes)
        assertFalse(SushiDestination.Onboarding.route(OnboardingSource.SETTINGS) in mainTabRoutes)
        assertTrue(SushiDestination.Settings.route in mainTabRoutes)
    }

    @Test
    fun givenOnboardingSources_whenRouted_thenPathsAreDistinct() {
        assertEquals(
            "onboarding/FIRST_LAUNCH",
            SushiDestination.Onboarding.route(OnboardingSource.FIRST_LAUNCH),
        )
        assertEquals(
            "onboarding/SETTINGS",
            SushiDestination.Onboarding.route(OnboardingSource.SETTINGS),
        )
    }
}
