package com.mancebolabs.sushiclash

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mancebolabs.sushiclash.R
import com.mancebolabs.sushiclash.support.finishActiveGameWithoutSaving
import com.mancebolabs.sushiclash.support.startSoloGame
import com.mancebolabs.sushiclash.support.tapSushi
import com.mancebolabs.sushiclash.support.waitForText
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CounterFinishFlowInstrumentedTest : SushiClashInstrumentedTestCase() {

    @Test
    fun givenSoloGame_whenIncrementingAndFinishingWithoutSaving_thenReturnsToNoActiveGameState() {
        composeTestRule.startSoloGame()
        composeTestRule.tapSushi(times = 2)
        composeTestRule.waitForText(R.string.counter_finish_game)

        composeTestRule.finishActiveGameWithoutSaving()

        composeTestRule.waitForText(R.string.counter_no_active_game_title)
        composeTestRule.waitForText(R.string.counter_start_game)
    }
}
