package com.mancebolabs.sushiclash

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mancebolabs.sushiclash.R
import com.mancebolabs.sushiclash.support.clickFirstText
import com.mancebolabs.sushiclash.support.finishActiveGameWithSaving
import com.mancebolabs.sushiclash.support.skipOnboardingIfShown
import com.mancebolabs.sushiclash.support.startSoloGame
import com.mancebolabs.sushiclash.support.tapSushi
import com.mancebolabs.sushiclash.support.waitForAchievementBanner
import com.mancebolabs.sushiclash.support.waitForText
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AchievementsUiTest : SushiClashInstrumentedTestCase() {

    @Test
    fun givenNoCompletedGames_whenFinishingFirstSoloGame_thenAchievementUnlocksOnce() {
        composeTestRule.skipOnboardingIfShown()
        composeTestRule.startSoloGame()
        composeTestRule.tapSushi(times = 1)
        composeTestRule.finishActiveGameWithSaving(dismissFinishCelebration = true)

        composeTestRule.waitForAchievementBanner()
        composeTestRule.clickFirstText(R.string.achievement_games_1_title)
        composeTestRule.waitForText(R.string.achievements_screen_title)
        composeTestRule.waitForText(R.string.achievement_games_1_title)
    }
}
