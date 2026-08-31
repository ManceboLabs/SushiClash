package com.mancebolabs.sushiclash.chef

import com.mancebolabs.sushiclash.domain.model.ChefEventAnimation
import com.mancebolabs.sushiclash.domain.model.ChefEventAnimationSelector
import com.mancebolabs.sushiclash.testutil.FakeRandomProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ChefEventAnimationSelectorTest {

    @Test
    fun givenNoPreviousAnimation_whenSelecting_thenReturnsCandidateFromProvider() {
        val random = FakeRandomProvider().apply { enqueue(2) }
        val selected = ChefEventAnimationSelector(random).select(lastAnimation = null)
        assertEquals(ChefEventAnimation.COMA, selected)
    }

    @Test
    fun givenPreviousAnimation_whenSelecting_thenDoesNotRepeatImmediately() {
        val random = FakeRandomProvider().apply {
            repeat(20) { enqueue(0) }
        }
        val selector = ChefEventAnimationSelector(random)
        val last = ChefEventAnimation.NINJA

        repeat(20) {
            assertNotEquals(last, selector.select(last))
        }
    }
}
