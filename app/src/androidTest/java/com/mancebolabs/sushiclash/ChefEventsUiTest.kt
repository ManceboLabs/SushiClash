package com.mancebolabs.sushiclash

import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mancebolabs.sushiclash.R
import com.mancebolabs.sushiclash.domain.model.ChefAnimationTriggerLogic
import com.mancebolabs.sushiclash.support.assertChefRandomEventOverlayHidden
import com.mancebolabs.sushiclash.support.assertSoloCount
import com.mancebolabs.sushiclash.support.clickNav
import com.mancebolabs.sushiclash.support.configureAppContainerAndRelaunch
import com.mancebolabs.sushiclash.support.configureChefRandomTriggerAt
import com.mancebolabs.sushiclash.support.configureGroupChefRandomTriggers
import com.mancebolabs.sushiclash.support.FakeRandomProvider
import com.mancebolabs.sushiclash.support.skipOnboardingIfShown
import com.mancebolabs.sushiclash.support.startGroupGame
import com.mancebolabs.sushiclash.support.startSoloGame
import com.mancebolabs.sushiclash.support.tapPlayerSushi
import com.mancebolabs.sushiclash.support.tapSushi
import com.mancebolabs.sushiclash.support.waitForText
import com.mancebolabs.sushiclash.testing.SushiClashTestTags
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChefEventsUiTest : SushiClashInstrumentedTestCase() {

    @Test
    fun givenSoloWithTriggerAtThree_whenIncrementing_thenEventFiresAtThirdCountAndCountingResumes() {
        composeTestRule.configureChefRandomTriggerAt(3)
        composeTestRule.skipOnboardingIfShown()
        composeTestRule.startSoloGame()

        composeTestRule.tapSushi(times = 1)
        composeTestRule.assertSoloCount(1)
        composeTestRule.assertChefRandomEventOverlayHidden()

        composeTestRule.tapSushi(times = 1)
        composeTestRule.assertSoloCount(2)
        composeTestRule.assertChefRandomEventOverlayHidden()

        composeTestRule.tapSushi(times = 1)
        composeTestRule.assertSoloCount(3)
        composeTestRule.waitUntil(15_000L) {
            composeTestRule.onAllNodesWithTag(SushiClashTestTags.CHEF_RANDOM_EVENT_OVERLAY, useUnmergedTree = true)
                .fetchSemanticsNodes(atLeastOneRootRequired = false)
                .isEmpty()
        }

        composeTestRule.tapSushi(times = 1)
        composeTestRule.assertSoloCount(4)
    }

    @Test
    fun givenGroupWithIndependentTriggers_whenOnePlayerTriggers_thenOtherScheduleIsUnchanged() {
        composeTestRule.configureGroupChefRandomTriggers(3, 4)
        composeTestRule.skipOnboardingIfShown()
        composeTestRule.startGroupGame(listOf("Ana", "Luis"))

        composeTestRule.tapPlayerSushi("Ana", times = 3)
        composeTestRule.waitUntil(15_000L) {
            composeTestRule.onAllNodesWithTag(SushiClashTestTags.CHEF_RANDOM_EVENT_OVERLAY, useUnmergedTree = true)
                .fetchSemanticsNodes(atLeastOneRootRequired = false)
                .isEmpty()
        }

        composeTestRule.tapPlayerSushi("Luis", times = 3)
        composeTestRule.assertChefRandomEventOverlayHidden()

        composeTestRule.tapPlayerSushi("Luis", times = 4)
        composeTestRule.waitUntil(15_000L) {
            composeTestRule.onAllNodesWithTag(SushiClashTestTags.CHEF_RANDOM_EVENT_OVERLAY, useUnmergedTree = true)
                .fetchSemanticsNodes(atLeastOneRootRequired = false)
                .isEmpty()
        }
    }

    @Test
    fun givenActiveChefRandomEvent_whenBottomNavIsTapped_thenNavigationIsBlocked() {
        composeTestRule.configureAppContainerAndRelaunch {
            chefRandomProvider = FakeRandomProvider().apply {
                enqueue(3)
                enqueue(0)
                enqueue(ChefAnimationTriggerLogic.MIN_INTERVAL)
            }
            completeGifCyclesImmediately = false
        }
        composeTestRule.skipOnboardingIfShown()
        composeTestRule.startSoloGame()

        composeTestRule.tapSushi(times = 3)
        composeTestRule.assertSoloCount(3)

        composeTestRule.clickNav(R.string.nav_history)
        composeTestRule.onAllNodesWithText(
            composeTestRule.activity.getString(R.string.history_section_solo),
            useUnmergedTree = true,
        ).fetchSemanticsNodes(atLeastOneRootRequired = false).also { nodes ->
            check(nodes.isEmpty()) { "History screen should not open while chef event is active" }
        }

        composeTestRule.waitUntil(15_000L) {
            composeTestRule.onAllNodesWithTag(SushiClashTestTags.CHEF_RANDOM_EVENT_OVERLAY, useUnmergedTree = true)
                .fetchSemanticsNodes(atLeastOneRootRequired = false)
                .isEmpty()
        }

        composeTestRule.waitForText(R.string.counter_finish_game)
        composeTestRule.onAllNodesWithText(
            composeTestRule.activity.getString(R.string.history_section_solo),
            useUnmergedTree = true,
        ).fetchSemanticsNodes(atLeastOneRootRequired = false).also { nodes ->
            check(nodes.isEmpty()) { "History screen should remain closed after chef event completes" }
        }
    }
}
