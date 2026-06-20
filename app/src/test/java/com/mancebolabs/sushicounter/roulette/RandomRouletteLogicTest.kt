package com.mancebolabs.sushicounter.roulette

import com.mancebolabs.sushicounter.domain.model.GameState
import com.mancebolabs.sushicounter.domain.model.RandomRouletteLogic
import com.mancebolabs.sushicounter.testutil.FakeRandomProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RandomRouletteLogicTest {

    private val logic = RandomRouletteLogic()

    @Test
    fun givenFixedThreshold_whenCountIsMultiple_thenTriggers() {
        assertTrue(logic.shouldTriggerFixed(count = 5, fixedThreshold = 5))
        assertTrue(logic.shouldTriggerFixed(count = 10, fixedThreshold = 5))
    }

    @Test
    fun givenFixedThreshold_whenCountIsNotMultiple_thenDoesNotTrigger() {
        assertFalse(logic.shouldTriggerFixed(count = 4, fixedThreshold = 5))
        assertFalse(logic.shouldTriggerFixed(count = 0, fixedThreshold = 5))
    }

    @Test
    fun givenRandomTarget_whenCountMatchesTarget_thenTriggers() {
        assertTrue(logic.shouldTriggerRandom(count = 7, target = 7))
    }

    @Test
    fun givenRandomTarget_whenCountDoesNotMatch_thenDoesNotTrigger() {
        assertFalse(logic.shouldTriggerRandom(count = 6, target = 7))
    }

    @Test
    fun givenFakeRandom_whenGeneratingInitialTarget_thenUsesProviderValue() {
        val random = FakeRandomProvider().apply { enqueue(8) }
        val target = RandomRouletteLogic(random).generateInitialTarget()
        assertEquals(8, target)
    }

    @Test
    fun givenPreviousTriggerWas4_whenGeneratingNextTarget_thenUsesProgressiveRange() {
        val random = FakeRandomProvider().apply { enqueue(12) }
        val nextTarget = RandomRouletteLogic(random).generateNextTargetAfterTrigger(lastTrigger = 4)
        assertEquals(12, nextTarget)
    }

    @Test
    fun givenPreviousTriggerWas4_whenNextTargetGenerated_thenValueIsBetween5And15() {
        val random = FakeRandomProvider().apply {
            enqueue(5)
            enqueue(15)
        }
        val logicWithRandom = RandomRouletteLogic(random)
        assertEquals(5, logicWithRandom.generateNextTargetAfterTrigger(4))
        assertEquals(15, logicWithRandom.generateNextTargetAfterTrigger(4))
    }

    @Test
    fun givenInitialTargetGeneration_thenValueIsWithinConfiguredBounds() {
        val random = FakeRandomProvider().apply {
            enqueue(GameState.MIN_RANDOM_ROULETTE_THRESHOLD)
            enqueue(GameState.MAX_RANDOM_ROULETTE_THRESHOLD)
        }
        val logicWithRandom = RandomRouletteLogic(random)
        assertEquals(1, logicWithRandom.generateInitialTarget())
        assertEquals(10, logicWithRandom.generateInitialTarget())
    }
}
