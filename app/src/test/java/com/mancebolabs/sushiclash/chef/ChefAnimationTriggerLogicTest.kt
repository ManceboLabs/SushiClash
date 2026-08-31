package com.mancebolabs.sushiclash.chef

import com.mancebolabs.sushiclash.domain.model.ChefAnimationTriggerLogic
import com.mancebolabs.sushiclash.domain.model.Player
import com.mancebolabs.sushiclash.testutil.FakeRandomProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChefAnimationTriggerLogicTest {

    @Test
    fun givenFakeRandom_whenGeneratingInitialTarget_thenUsesProviderValue() {
        val random = FakeRandomProvider().apply { enqueue(4) }
        val target = ChefAnimationTriggerLogic(random).generateInitialTarget()
        assertEquals(4, target)
    }

    @Test
    fun givenInitialTargetGeneration_thenValueIsWithinConfiguredBounds() {
        val random = FakeRandomProvider().apply {
            enqueue(ChefAnimationTriggerLogic.MIN_INTERVAL)
            enqueue(ChefAnimationTriggerLogic.MAX_INTERVAL)
        }
        val logic = ChefAnimationTriggerLogic(random)
        assertEquals(3, logic.generateInitialTarget())
        assertEquals(5, logic.generateInitialTarget())
    }

    @Test
    fun givenCurrentCount4_whenGeneratingNextTarget_thenAddsIntervalFromProvider() {
        val random = FakeRandomProvider().apply { enqueue(3) }
        val nextTarget = ChefAnimationTriggerLogic(random).generateNextTargetAfterTrigger(currentCount = 4)
        assertEquals(7, nextTarget)
    }

    @Test
    fun givenCurrentCount7_whenNextTargetGenerated_thenValueIsBetween10And12() {
        val random = FakeRandomProvider().apply {
            enqueue(3)
            enqueue(5)
        }
        val logic = ChefAnimationTriggerLogic(random)
        assertEquals(10, logic.generateNextTargetAfterTrigger(currentCount = 7))
        assertEquals(12, logic.generateNextTargetAfterTrigger(currentCount = 7))
    }

    @Test
    fun givenTarget_whenCountMatchesExactly_thenTriggersOnce() {
        val logic = ChefAnimationTriggerLogic()
        assertTrue(logic.shouldTrigger(count = 4, target = 4))
        assertFalse(logic.shouldTrigger(count = 5, target = 4))
    }

    @Test
    fun givenLegacyPlayerWithMissingTargetAndZeroCount_whenResolvingTarget_thenUsesInitialTarget() {
        val random = FakeRandomProvider().apply { enqueue(5) }
        val logic = ChefAnimationTriggerLogic(random)
        val player = Player(id = "solo", name = "")

        assertEquals(5, logic.resolveStoredTarget(player))
    }

    @Test
    fun givenLegacyPlayerWithMissingTargetAndExistingCount_whenResolvingTarget_thenUsesProgressiveTarget() {
        val random = FakeRandomProvider().apply { enqueue(4) }
        val logic = ChefAnimationTriggerLogic(random)
        val player = Player(id = "solo", name = "", sushiCount = 10)

        assertEquals(14, logic.resolveStoredTarget(player))
    }
}
