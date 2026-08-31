package com.mancebolabs.sushiclash

import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mancebolabs.sushiclash.R
import com.mancebolabs.sushiclash.feature.onboarding.defaultOnboardingSteps
import com.mancebolabs.sushiclash.support.assertBottomNavHidden
import com.mancebolabs.sushiclash.support.assertBottomNavVisible
import com.mancebolabs.sushiclash.support.assertOnboardingProgressDotSelected
import com.mancebolabs.sushiclash.support.completeOnboarding
import com.mancebolabs.sushiclash.support.navigateToLastOnboardingStep
import com.mancebolabs.sushiclash.support.onText
import com.mancebolabs.sushiclash.support.openOnboardingFromSettings
import com.mancebolabs.sushiclash.support.recreateActivity
import com.mancebolabs.sushiclash.support.skipOnboardingIfShown
import com.mancebolabs.sushiclash.support.swipeOnboardingPagerLeft
import com.mancebolabs.sushiclash.support.swipeOnboardingPagerRight
import com.mancebolabs.sushiclash.support.waitForText
import com.mancebolabs.sushiclash.testing.SushiClashTestTags
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnboardingUiTest : SushiClashInstrumentedTestCase() {

    @Test
    fun givenFreshInstall_whenAppLaunches_thenShowsOnboardingWithChefContent() {
        composeTestRule.waitForText(R.string.onboarding_step_welcome_dialogue)
        composeTestRule.waitForText(R.string.onboarding_skip)
        composeTestRule.onNodeWithTag(SushiClashTestTags.ONBOARDING_CHEF_GREETING, useUnmergedTree = true)
            .assertIsDisplayed()
        composeTestRule.assertBottomNavHidden()
    }

    @Test
    fun givenOnboarding_whenNextClicked_thenShowsNextStepAndUpdatesIndicator() {
        composeTestRule.waitForText(R.string.onboarding_step_welcome_dialogue)
        composeTestRule.assertOnboardingProgressDotSelected(0)
        composeTestRule.onText(R.string.onboarding_next).assertIsDisplayed().performClick()
        composeTestRule.waitForText(R.string.onboarding_step_solo_title)
        composeTestRule.assertOnboardingProgressDotSelected(1)
        composeTestRule.onAllNodesWithTag(SushiClashTestTags.ONBOARDING_CHEF_TUTORIAL, useUnmergedTree = true)[0]
            .assertIsDisplayed()
    }

    @Test
    fun givenSecondStep_whenPreviousClicked_thenReturnsToWelcomeStep() {
        composeTestRule.waitForText(R.string.onboarding_step_welcome_dialogue)
        composeTestRule.onText(R.string.onboarding_next).performClick()
        composeTestRule.waitForText(R.string.onboarding_step_solo_title)
        composeTestRule.onText(R.string.onboarding_previous).performClick()
        composeTestRule.waitForText(R.string.onboarding_step_welcome_dialogue)
        composeTestRule.assertOnboardingProgressDotSelected(0)
    }

    @Test
    fun givenOnboarding_whenSwipedLeft_thenShowsNextStepAndIndicator() {
        composeTestRule.waitForText(R.string.onboarding_step_welcome_dialogue)
        composeTestRule.swipeOnboardingPagerLeft()
        composeTestRule.waitForText(R.string.onboarding_step_solo_title)
        composeTestRule.assertOnboardingProgressDotSelected(1)
    }

    @Test
    fun givenSecondOnboardingStep_whenSwipedRight_thenShowsPreviousStepAndIndicator() {
        composeTestRule.waitForText(R.string.onboarding_step_welcome_dialogue)
        composeTestRule.onText(R.string.onboarding_next).performClick()
        composeTestRule.waitForText(R.string.onboarding_step_solo_title)
        composeTestRule.swipeOnboardingPagerRight()
        composeTestRule.waitForText(R.string.onboarding_step_welcome_dialogue)
        composeTestRule.assertOnboardingProgressDotSelected(0)
    }

    @Test
    fun givenOnboarding_whenSkipped_thenReachesCounterWithoutActiveGame() {
        composeTestRule.skipOnboardingIfShown()
        composeTestRule.waitForText(R.string.counter_no_active_game_title)
        composeTestRule.assertBottomNavVisible()
    }

    @Test
    fun givenOnboarding_whenCompleted_thenReachesCounterWithoutActiveGame() {
        composeTestRule.completeOnboarding()
        composeTestRule.waitForText(R.string.counter_no_active_game_title)
        composeTestRule.assertBottomNavVisible()
    }

    @Test
    fun givenCompletedOnboarding_whenRelaunchingApp_thenOnboardingDoesNotAppearAutomatically() {
        composeTestRule.completeOnboarding()
        composeTestRule.recreateActivity()
        composeTestRule.waitForText(R.string.counter_start_game)
        composeTestRule.onAllNodesWithText(
            composeTestRule.activity.getString(R.string.onboarding_skip),
            useUnmergedTree = true,
        ).fetchSemanticsNodes(atLeastOneRootRequired = false)
            .also { nodes -> check(nodes.isEmpty()) { "Onboarding should not reappear automatically" } }
    }

    @Test
    fun givenCompletedOnboarding_whenOpeningFromSettings_thenOnboardingIsShown() {
        composeTestRule.completeOnboarding()
        composeTestRule.openOnboardingFromSettings()
        composeTestRule.waitForText(R.string.onboarding_step_welcome_dialogue)
        composeTestRule.assertBottomNavHidden()
    }

    @Test
    fun givenLastOnboardingStep_whenDisplayed_thenResponsibleUseBodyIsScrollableAndFinishIsVisible() {
        composeTestRule.navigateToLastOnboardingStep()
        composeTestRule.onText(R.string.onboarding_step_responsible_use_description)
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onText(R.string.onboarding_finish).assertIsDisplayed()
        composeTestRule.assertOnboardingProgressDotSelected(defaultOnboardingSteps().lastIndex)
    }

    @Test
    fun givenLastOnboardingStep_whenSwipedLeft_thenRemainsOnLastStep() {
        composeTestRule.navigateToLastOnboardingStep()
        composeTestRule.swipeOnboardingPagerLeft()
        composeTestRule.waitForText(R.string.onboarding_step_responsible_use_title)
        composeTestRule.onText(R.string.onboarding_finish).assertIsDisplayed()
    }
}
