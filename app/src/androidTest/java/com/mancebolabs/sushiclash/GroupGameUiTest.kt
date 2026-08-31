package com.mancebolabs.sushiclash

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mancebolabs.sushiclash.R
import com.mancebolabs.sushiclash.support.addGroupPlayer
import com.mancebolabs.sushiclash.support.assertAddGroupPlayerDisabled
import com.mancebolabs.sushiclash.support.assertGroupPlayerCount
import com.mancebolabs.sushiclash.support.clickNav
import com.mancebolabs.sushiclash.support.clickText
import com.mancebolabs.sushiclash.support.finishGroupGameAndOpenHistory
import com.mancebolabs.sushiclash.support.longPressResetPlayer
import com.mancebolabs.sushiclash.support.openSettings
import com.mancebolabs.sushiclash.support.skipOnboardingIfShown
import com.mancebolabs.sushiclash.support.startGroupGame
import com.mancebolabs.sushiclash.support.tapPlayerSushi
import com.mancebolabs.sushiclash.support.typeIntoPlayerNameField
import com.mancebolabs.sushiclash.support.waitForGroupHistoryEntry
import com.mancebolabs.sushiclash.support.waitForText
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GroupGameUiTest : SushiClashInstrumentedTestCase() {

    @Test
    fun givenGroupGame_whenPlayingThroughMainFlow_thenCountsPersistAndHistoryShowsResult() {
        composeTestRule.startGroupGame(listOf("Ana", "Luis"))
        composeTestRule.tapPlayerSushi("Ana", times = 2)
        composeTestRule.tapPlayerSushi("Luis", times = 3)
        composeTestRule.assertGroupPlayerCount("Ana", 2)
        composeTestRule.assertGroupPlayerCount("Luis", 3)

        composeTestRule.openSettings()
        composeTestRule.clickNav(R.string.nav_counter)
        composeTestRule.waitForText(R.string.counter_finish_game)
        composeTestRule.assertGroupPlayerCount("Ana", 2)
        composeTestRule.assertGroupPlayerCount("Luis", 3)

        composeTestRule.finishGroupGameAndOpenHistory()
        composeTestRule.waitForGroupHistoryEntry("Luis", bestScore = 3)
    }

    @Test
    fun givenTwoPlayers_whenOneIncrements_thenOtherCountIsUnchanged() {
        composeTestRule.startGroupGame(listOf("Ana", "Luis"))
        composeTestRule.tapPlayerSushi("Ana", times = 2)
        composeTestRule.assertGroupPlayerCount("Ana", 2)
        composeTestRule.assertGroupPlayerCount("Luis", 0)
    }

    @Test
    fun givenGroupPlayerWithCount_whenResetViaLongPress_thenOnlyThatPlayerResets() {
        composeTestRule.startGroupGame(listOf("Ana", "Luis"))
        composeTestRule.tapPlayerSushi("Ana", times = 3)
        composeTestRule.tapPlayerSushi("Luis", times = 1)
        composeTestRule.longPressResetPlayer("Ana")
        composeTestRule.assertGroupPlayerCount("Ana", 0)
        composeTestRule.assertGroupPlayerCount("Luis", 1)
    }

    @Test
    fun givenGroupSetup_whenDuplicateNameEntered_thenAddIsDisabled() {
        composeTestRule.skipOnboardingIfShown()
        composeTestRule.clickText(R.string.counter_start_game)
        composeTestRule.waitForText(R.string.setup_group_title)
        composeTestRule.clickText(R.string.setup_group_title)
        composeTestRule.addGroupPlayer("Ana")
        composeTestRule.typeIntoPlayerNameField("Ana")
        composeTestRule.assertAddGroupPlayerDisabled()
    }
}
