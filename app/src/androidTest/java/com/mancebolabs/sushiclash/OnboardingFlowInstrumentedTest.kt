package com.mancebolabs.sushiclash

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mancebolabs.sushiclash.R
import com.mancebolabs.sushiclash.support.completeOnboarding
import com.mancebolabs.sushiclash.support.skipOnboardingIfShown
import com.mancebolabs.sushiclash.support.waitForText
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnboardingFlowInstrumentedTest : SushiClashInstrumentedTestCase() {

    @Test
    fun givenFreshInstall_whenAppLaunches_thenShowsOnboarding() {
        composeTestRule.waitForText(R.string.onboarding_step_welcome_title)
        composeTestRule.waitForText(R.string.onboarding_skip)
    }

    @Test
    fun givenOnboarding_whenSkipped_thenReachesCounterWithoutActiveGame() {
        composeTestRule.skipOnboardingIfShown()
        composeTestRule.waitForText(R.string.counter_no_active_game_title)
        composeTestRule.waitForText(R.string.counter_start_game)
    }

    @Test
    fun givenOnboarding_whenCompleted_thenReachesCounterWithoutActiveGame() {
        composeTestRule.completeOnboarding()
        composeTestRule.waitForText(R.string.counter_no_active_game_title)
        composeTestRule.waitForText(R.string.counter_start_game)
    }
}
