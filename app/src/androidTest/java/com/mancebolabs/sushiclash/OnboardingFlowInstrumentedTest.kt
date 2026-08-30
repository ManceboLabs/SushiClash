package com.mancebolabs.sushiclash

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mancebolabs.sushiclash.R
import com.mancebolabs.sushiclash.support.completeOnboarding
import com.mancebolabs.sushiclash.support.navigateToLastOnboardingStep
import com.mancebolabs.sushiclash.support.onText
import com.mancebolabs.sushiclash.support.skipOnboardingIfShown
import com.mancebolabs.sushiclash.support.waitForText
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import com.mancebolabs.sushiclash.feature.onboarding.ONBOARDING_PAGER_TEST_TAG
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

    @Test
    fun givenLastOnboardingStep_whenDisplayed_thenResponsibleUseBodyIsScrollableAndFinishIsVisible() {
        composeTestRule.navigateToLastOnboardingStep()
        composeTestRule.onText(R.string.onboarding_step_responsible_use_description)
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onText(R.string.onboarding_finish).assertIsDisplayed()
    }

    @Test
    fun givenOnboarding_whenSwipedLeft_thenShowsNextStep() {
        composeTestRule.waitForText(R.string.onboarding_step_welcome_title)
        composeTestRule.onNodeWithTag(ONBOARDING_PAGER_TEST_TAG)
            .performTouchInput {
                val y = center.y
                down(Offset(right - 1f, y))
                moveTo(Offset(left + 1f, y), 200)
                up()
            }
        composeTestRule.waitForText(R.string.onboarding_step_solo_title)
    }

    @Test
    fun givenSecondOnboardingStep_whenSwipedRight_thenShowsPreviousStep() {
        composeTestRule.waitForText(R.string.onboarding_step_welcome_title)
        composeTestRule.onText(R.string.onboarding_next).performClick()
        composeTestRule.waitForText(R.string.onboarding_step_solo_title)
        composeTestRule.onNodeWithTag(ONBOARDING_PAGER_TEST_TAG)
            .performTouchInput {
                val y = center.y
                down(Offset(left + 1f, y))
                moveTo(Offset(right - 1f, y), 200)
                up()
            }
        composeTestRule.waitForText(R.string.onboarding_step_welcome_title)
    }

    @Test
    fun givenLastOnboardingStep_whenSwipedLeft_thenRemainsOnLastStep() {
        composeTestRule.navigateToLastOnboardingStep()
        composeTestRule.onNodeWithTag(ONBOARDING_PAGER_TEST_TAG)
            .performTouchInput {
                val y = center.y
                down(Offset(right - 1f, y))
                moveTo(Offset(left + 1f, y), 200)
                up()
            }
        composeTestRule.waitForText(R.string.onboarding_step_responsible_use_title)
        composeTestRule.onText(R.string.onboarding_finish).assertIsDisplayed()
    }
}
