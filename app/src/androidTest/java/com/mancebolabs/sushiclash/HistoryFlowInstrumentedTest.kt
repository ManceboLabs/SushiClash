package com.mancebolabs.sushiclash

import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mancebolabs.sushiclash.R
import com.mancebolabs.sushiclash.support.clickNav
import com.mancebolabs.sushiclash.support.finishActiveGameWithSaving
import com.mancebolabs.sushiclash.support.startSoloGame
import com.mancebolabs.sushiclash.support.tapSushi
import com.mancebolabs.sushiclash.support.waitForText
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Exercises History rendering on a real device/emulator (minSdk 24+) so java.time formatting
 * stays covered alongside the desugaring-backed HistoryDateFormatter unit tests.
 */
@RunWith(AndroidJUnit4::class)
class HistoryFlowInstrumentedTest : SushiClashInstrumentedTestCase() {

    @Test
    fun givenSavedSoloGame_whenOpeningHistory_thenScreenRendersSavedEntry() {
        composeTestRule.startSoloGame()
        composeTestRule.tapSushi(times = 1)
        composeTestRule.finishActiveGameWithSaving()

        composeTestRule.clickNav(R.string.nav_history)
        composeTestRule.waitForText(R.string.history_screen_title)
        composeTestRule.waitForText(R.string.history_section_solo)

        val savedCountLabel = composeTestRule.activity.getString(
            R.string.history_solo_sushi_count,
            1,
        )
        composeTestRule.waitUntil(15_000L) {
            composeTestRule.onAllNodesWithText(savedCountLabel, useUnmergedTree = true)
                .fetchSemanticsNodes(atLeastOneRootRequired = false)
                .isNotEmpty()
        }
    }
}
