package com.mancebolabs.sushiclash

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mancebolabs.sushiclash.R
import com.mancebolabs.sushiclash.support.assertChefCelebrationSpeechNotShown
import com.mancebolabs.sushiclash.support.assertChefRandomEventOverlayHidden
import com.mancebolabs.sushiclash.support.assertSoloCount
import com.mancebolabs.sushiclash.support.configureChefRandomTriggerAt
import com.mancebolabs.sushiclash.support.recreateActivity
import com.mancebolabs.sushiclash.support.skipOnboardingIfShown
import com.mancebolabs.sushiclash.support.startSoloGame
import com.mancebolabs.sushiclash.support.tapSushi
import com.mancebolabs.sushiclash.support.waitForText
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameLifecycleUiTest : SushiClashInstrumentedTestCase() {

    @Test
    fun givenActiveSoloGame_whenActivityRecreated_thenCountPersistsWithoutReplayedChefEvents() {
        composeTestRule.configureChefRandomTriggerAt(3)
        composeTestRule.skipOnboardingIfShown()
        composeTestRule.startSoloGame()
        composeTestRule.tapSushi(times = 2)
        composeTestRule.assertSoloCount(2)

        composeTestRule.recreateActivity()
        composeTestRule.waitForText(R.string.counter_finish_game)
        composeTestRule.assertSoloCount(2)
        composeTestRule.assertChefCelebrationSpeechNotShown(R.string.game_start_speech)
        composeTestRule.assertChefRandomEventOverlayHidden()
    }
}
