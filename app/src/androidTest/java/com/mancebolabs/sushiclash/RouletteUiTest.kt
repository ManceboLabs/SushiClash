package com.mancebolabs.sushiclash

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mancebolabs.sushiclash.R
import com.mancebolabs.sushiclash.support.addWheelParticipant
import com.mancebolabs.sushiclash.support.clickNav
import com.mancebolabs.sushiclash.support.clickText
import com.mancebolabs.sushiclash.support.configureWheelDeterministicWinner
import com.mancebolabs.sushiclash.support.dismissChefCelebrationIfShown
import com.mancebolabs.sushiclash.support.enableFixedRouletteInSetup
import com.mancebolabs.sushiclash.support.openWheel
import com.mancebolabs.sushiclash.support.skipOnboardingIfShown
import com.mancebolabs.sushiclash.support.spinWheelAndWaitForWinner
import com.mancebolabs.sushiclash.support.startSoloGame
import com.mancebolabs.sushiclash.support.tapSushi
import com.mancebolabs.sushiclash.support.waitForText
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RouletteUiTest : SushiClashInstrumentedTestCase() {

    @Test
    fun givenParticipants_whenSpinningWithDeterministicRandom_thenWinnerCelebrationShowsSelectedParticipant() {
        composeTestRule.configureWheelDeterministicWinner(winnerIndex = 0)
        composeTestRule.skipOnboardingIfShown()
        composeTestRule.openWheel()

        composeTestRule.addWheelParticipant("Ana")
        composeTestRule.addWheelParticipant("Luis")
        composeTestRule.spinWheelAndWaitForWinner("Ana")

        composeTestRule.clickText(R.string.wheel_ok)
        composeTestRule.dismissChefCelebrationIfShown()
        composeTestRule.waitForText(R.string.wheel_spin)
    }

    @Test
    fun givenActiveSoloGameWithFixedRoulette_whenThresholdReached_thenTriggerDialogAppearsWithoutCorruptingCount() {
        composeTestRule.skipOnboardingIfShown()
        composeTestRule.clickText(R.string.counter_start_game)
        composeTestRule.waitForText(R.string.setup_solo_title)
        composeTestRule.clickText(R.string.setup_solo_title)
        composeTestRule.enableFixedRouletteInSetup()
        composeTestRule.clickText(R.string.setup_start)
        composeTestRule.waitForText(R.string.counter_finish_game)
        composeTestRule.dismissChefCelebrationIfShown()

        composeTestRule.tapSushi(times = 5)
        composeTestRule.waitForText(R.string.roulette_trigger_title)
        composeTestRule.clickText(R.string.counter_cancel)

        composeTestRule.openWheel()
        composeTestRule.waitForText(R.string.wheel_screen_title)
        composeTestRule.clickNav(R.string.nav_counter)
        composeTestRule.waitForText(R.string.counter_finish_game)
    }
}
