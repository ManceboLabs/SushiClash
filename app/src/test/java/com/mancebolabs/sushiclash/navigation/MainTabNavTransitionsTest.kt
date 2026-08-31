package com.mancebolabs.sushiclash.navigation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MainTabNavTransitionsTest {

    @Test
    fun givenTwoDifferentMainTabs_whenCheckingSwitch_thenReturnsTrue() {
        assertTrue(isMainTabSwitch(SushiDestination.Counter.route, SushiDestination.Wheel.route))
    }

    @Test
    fun givenSameMainTab_whenCheckingSwitch_thenReturnsFalse() {
        assertFalse(isMainTabSwitch(SushiDestination.History.route, SushiDestination.History.route))
    }

    @Test
    fun givenOnboardingToMainTab_whenCheckingSwitch_thenReturnsFalse() {
        assertFalse(
            isMainTabSwitch(
                SushiDestination.Onboarding.route(OnboardingSource.FIRST_LAUNCH),
                SushiDestination.Counter.route,
            ),
        )
    }

    @Test
    fun givenMainTabToAchievements_whenCheckingSwitch_thenReturnsFalse() {
        assertFalse(
            isMainTabSwitch(
                SushiDestination.Settings.route,
                SushiDestination.Achievements.route,
            ),
        )
    }
}
