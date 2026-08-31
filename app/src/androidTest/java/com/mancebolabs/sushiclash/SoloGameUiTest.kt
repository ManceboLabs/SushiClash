package com.mancebolabs.sushiclash

import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mancebolabs.sushiclash.R
import com.mancebolabs.sushiclash.support.assertSoloCount
import com.mancebolabs.sushiclash.support.clickNav
import com.mancebolabs.sushiclash.support.finishActiveGameCancel
import com.mancebolabs.sushiclash.support.finishActiveGameWithoutSaving
import com.mancebolabs.sushiclash.support.finishActiveGameWithSaving
import com.mancebolabs.sushiclash.support.openHistory
import com.mancebolabs.sushiclash.support.openSettings
import com.mancebolabs.sushiclash.support.startSoloGame
import com.mancebolabs.sushiclash.support.tapSushi
import com.mancebolabs.sushiclash.support.waitForText
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SoloGameUiTest : SushiClashInstrumentedTestCase() {

    @Test
    fun givenSoloGame_whenPlayingThroughMainFlow_thenCountPersistsAndHistoryShowsSavedResult() {
        composeTestRule.startSoloGame()
        composeTestRule.tapSushi(times = 4)
        composeTestRule.assertSoloCount(4)

        composeTestRule.openSettings()
        composeTestRule.clickNav(R.string.nav_counter)
        composeTestRule.waitForText(R.string.counter_finish_game)
        composeTestRule.assertSoloCount(4)

        composeTestRule.finishActiveGameWithSaving()
        composeTestRule.waitForText(R.string.counter_no_active_game_title)

        composeTestRule.openHistory()
        composeTestRule.waitForText(R.string.history_section_solo)
        val savedCountLabel = composeTestRule.activity.getString(
            R.string.history_solo_sushi_count,
            4,
        )
        composeTestRule.waitUntil(15_000L) {
            composeTestRule.onAllNodesWithText(savedCountLabel, useUnmergedTree = true)
                .fetchSemanticsNodes(atLeastOneRootRequired = false)
                .isNotEmpty()
        }
    }

    @Test
    fun givenActiveSoloGame_whenFinishCancelled_thenGameAndCountRemain() {
        composeTestRule.startSoloGame()
        composeTestRule.tapSushi(times = 2)
        composeTestRule.assertSoloCount(2)

        composeTestRule.finishActiveGameCancel()
        composeTestRule.waitForText(R.string.counter_finish_game)
        composeTestRule.assertSoloCount(2)
    }

    @Test
    fun givenActiveSoloGame_whenFinishWithoutSaving_thenActiveGameIsCleared() {
        composeTestRule.startSoloGame()
        composeTestRule.tapSushi(times = 2)
        composeTestRule.finishActiveGameWithoutSaving()
        composeTestRule.waitForText(R.string.counter_no_active_game_title)
        composeTestRule.waitForText(R.string.counter_start_game)
    }

    @Test
    fun givenActiveSoloGame_whenFinishWithSaving_thenActiveGameIsClearedAndHistoryHasOneEntry() {
        composeTestRule.startSoloGame()
        composeTestRule.tapSushi(times = 1)
        composeTestRule.finishActiveGameWithSaving()

        composeTestRule.openHistory()
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
