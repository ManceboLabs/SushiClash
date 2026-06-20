package com.mancebolabs.sushicounter.roulette

import com.mancebolabs.sushicounter.navigation.RandomRouletteNavState
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
