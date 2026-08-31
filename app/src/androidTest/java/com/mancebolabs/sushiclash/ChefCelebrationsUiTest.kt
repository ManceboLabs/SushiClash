package com.mancebolabs.sushiclash

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mancebolabs.sushiclash.R
import com.mancebolabs.sushiclash.support.assertChefCelebrationSpeechNotShown
import com.mancebolabs.sushiclash.support.clickNav
import com.mancebolabs.sushiclash.support.clickText
import com.mancebolabs.sushiclash.support.dismissChefCelebrationIfShown
import com.mancebolabs.sushiclash.support.finishActiveGameCancel
import com.mancebolabs.sushiclash.support.openSettings
import com.mancebolabs.sushiclash.support.skipOnboardingIfShown
import com.mancebolabs.sushiclash.support.startSoloGame
import com.mancebolabs.sushiclash.support.tapSushi
import com.mancebolabs.sushiclash.support.waitForChefCelebrationSpeech
import com.mancebolabs.sushiclash.support.waitForText
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChefCelebrationsUiTest : SushiClashInstrumentedTestCase() {

    @Test
    fun givenNewSoloGame_whenStarted_thenStartCelebrationIsShownOnce() {
        composeTestRule.skipOnboardingIfShown()
        composeTestRule.startSoloGame(dismissStartCelebration = false)
        composeTestRule.waitForChefCelebrationSpeech(R.string.game_start_speech)
        composeTestRule.dismissChefCelebrationIfShown()
    }

    @Test
    fun givenActiveSoloGame_whenNavigatingAwayAndBack_thenStartCelebrationIsNotReplayed() {
        composeTestRule.startSoloGame()
        composeTestRule.openSettings()
        composeTestRule.clickNav(R.string.nav_counter)
        composeTestRule.waitForText(R.string.counter_finish_game)
        composeTestRule.assertChefCelebrationSpeechNotShown(R.string.game_start_speech)
    }

    @Test
    fun givenActiveSoloGame_whenFinishDialogOpenedOrCancelled_thenFinishCelebrationIsNotShown() {
        composeTestRule.startSoloGame()
        composeTestRule.tapSushi(times = 1)
        composeTestRule.finishActiveGameCancel()
        composeTestRule.assertChefCelebrationSpeechNotShown(R.string.game_finish_speech)

        composeTestRule.clickText(R.string.counter_finish_game)
        composeTestRule.waitForText(R.string.finish_game_title)
        composeTestRule.assertChefCelebrationSpeechNotShown(R.string.game_finish_speech)
        composeTestRule.clickText(R.string.counter_cancel)
        composeTestRule.waitForText(R.string.counter_finish_game)
    }

    @Test
    fun givenActiveSoloGame_whenFinishConfirmedWithSave_thenFinishCelebrationIsShownOnce() {
        composeTestRule.startSoloGame()
        composeTestRule.tapSushi(times = 1)
        composeTestRule.clickText(R.string.counter_finish_game)
        composeTestRule.waitForText(R.string.finish_game_title)
        composeTestRule.clickText(R.string.finish_game_save)
        composeTestRule.waitForChefCelebrationSpeech(R.string.game_finish_speech)
        composeTestRule.dismissChefCelebrationIfShown()
        composeTestRule.waitForText(R.string.counter_no_active_game_title)
        composeTestRule.assertChefCelebrationSpeechNotShown(R.string.game_finish_speech)
    }
}
