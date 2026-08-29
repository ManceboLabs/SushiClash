package com.mancebolabs.sushiclash.feedback

import com.mancebolabs.sushiclash.domain.model.IncrementResult
import com.mancebolabs.sushiclash.feature.counter.wasCounterIncrementSuccessful
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CounterFeedbackLogicTest {

    @Test
    fun givenSuccessfulIncrement_whenCheckingResult_thenReturnsTrue() {
        assertTrue(
            wasCounterIncrementSuccessful(
                previousCount = 4,
                result = IncrementResult(newCount = 5, shouldTriggerRoulette = false),
            ),
        )
    }

    @Test
    fun givenFailedIncrement_whenCountUnchanged_thenReturnsFalse() {
        assertFalse(
            wasCounterIncrementSuccessful(
                previousCount = 4,
                result = IncrementResult(newCount = 4, shouldTriggerRoulette = false),
            ),
        )
    }

    @Test
    fun givenInvalidIncrement_whenCountIsZero_thenReturnsFalse() {
        assertFalse(
            wasCounterIncrementSuccessful(
                previousCount = 3,
                result = IncrementResult(newCount = 0, shouldTriggerRoulette = false),
            ),
        )
    }

    @Test
    fun givenMissingPreviousCount_whenCheckingResult_thenReturnsFalse() {
        assertFalse(
            wasCounterIncrementSuccessful(
                previousCount = null,
                result = IncrementResult(newCount = 1, shouldTriggerRoulette = false),
            ),
        )
    }
}
