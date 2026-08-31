package com.mancebolabs.sushiclash

import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mancebolabs.sushiclash.R
import com.mancebolabs.sushiclash.support.clearHistoryFromSettings
import com.mancebolabs.sushiclash.support.finishActiveGameWithoutSaving
import com.mancebolabs.sushiclash.support.finishActiveGameWithSaving
import com.mancebolabs.sushiclash.support.openHistory
import com.mancebolabs.sushiclash.support.selectHistorySection
import com.mancebolabs.sushiclash.support.skipOnboardingIfShown
import com.mancebolabs.sushiclash.support.startGroupGame
import com.mancebolabs.sushiclash.support.startSoloGame
import com.mancebolabs.sushiclash.support.tapPlayerSushi
import com.mancebolabs.sushiclash.support.tapSushi
import com.mancebolabs.sushiclash.support.finishGroupGameAndOpenHistory
import com.mancebolabs.sushiclash.support.waitForGroupHistoryEntry
import com.mancebolabs.sushiclash.support.waitForText
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HistoryUiTest : SushiClashInstrumentedTestCase() {

    @Test
    fun givenSavedSoloGame_whenOpeningHistory_thenScreenRendersSavedEntry() {
        composeTestRule.startSoloGame()
        composeTestRule.tapSushi(times = 1)
        composeTestRule.finishActiveGameWithSaving()

        composeTestRule.openHistory()
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

    @Test
    fun givenSavedGroupGame_whenOpeningGroupHistory_thenPlayerRankingIsVisible() {
        composeTestRule.startGroupGame(listOf("Ana", "Luis"))
        composeTestRule.tapPlayerSushi("Ana", times = 2)
        composeTestRule.finishGroupGameAndOpenHistory()
        composeTestRule.waitForGroupHistoryEntry("Ana", bestScore = 2)
    }

    @Test
    fun givenFinishedGameWithoutSaving_whenOpeningHistory_thenNoNewEntryIsAdded() {
        composeTestRule.startSoloGame()
        composeTestRule.tapSushi(times = 2)
        composeTestRule.finishActiveGameWithoutSaving()

        composeTestRule.openHistory()
        composeTestRule.waitForText(R.string.history_solo_empty)
    }

    @Test
    fun givenSavedHistory_whenClearingFromSettings_thenHistoryBecomesEmpty() {
        composeTestRule.startSoloGame()
        composeTestRule.tapSushi(times = 1)
        composeTestRule.finishActiveGameWithSaving()

        composeTestRule.openHistory()
        composeTestRule.waitForText(R.string.history_section_solo)
        composeTestRule.clearHistoryFromSettings()
        composeTestRule.openHistory()
        composeTestRule.waitForText(R.string.history_solo_empty)
    }
}
