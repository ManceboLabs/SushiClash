package com.mancebolabs.sushiclash.roulette

import com.mancebolabs.sushiclash.navigation.RandomRouletteNavState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RandomRouletteNavStateTest {

    @Test
    fun givenAutoSpinRequested_whenConsumed_thenFlagIsCleared() {
        val navState = RandomRouletteNavState()

        navState.requestAutoSpin()
        assertTrue(navState.pendingAutoSpin)

        navState.consumeAutoSpin()
        assertFalse(navState.pendingAutoSpin)
    }

    @Test
    fun givenFreshState_whenNotRequested_thenPendingAutoSpinIsFalse() {
        val navState = RandomRouletteNavState()
        assertFalse(navState.pendingAutoSpin)
    }
}
