package com.mancebolabs.sushiclash.onboarding

import com.mancebolabs.sushiclash.feature.onboarding.OnboardingChefRole
import com.mancebolabs.sushiclash.feature.onboarding.resolveOnboardingChefSize
import com.mancebolabs.sushiclash.feature.onboarding.resolveOnboardingIllustrationSize
import org.junit.Assert.assertEquals
import org.junit.Test
import androidx.compose.ui.unit.dp

class OnboardingLayoutTest {

    @Test
    fun givenTallContentArea_whenResolvingIllustrationSize_thenUsesDesignMaximum() {
        assertEquals(160.dp, resolveOnboardingIllustrationSize(maxHeight = 900.dp))
    }

    @Test
    fun givenShortContentArea_whenResolvingIllustrationSize_thenScalesDownWithinBounds() {
        val maxHeight = 560.dp
        val expected = minOf(160.dp, maxHeight * 0.22f).coerceIn(96.dp, 160.dp)
        assertEquals(expected, resolveOnboardingIllustrationSize(maxHeight = maxHeight))
    }

    @Test
    fun givenVeryShortContentArea_whenResolvingIllustrationSize_thenNeverDropsBelowMinimum() {
        assertEquals(96.dp, resolveOnboardingIllustrationSize(maxHeight = 300.dp))
    }

    @Test
    fun givenTallContentArea_whenResolvingChefSize_thenUsesDesignMaximum() {
        assertEquals(200.dp, resolveOnboardingChefSize(maxHeight = 900.dp))
    }

    @Test
    fun givenShortContentArea_whenResolvingChefSize_thenScalesDownWithinBounds() {
        val maxHeight = 560.dp
        val expected = minOf(200.dp, maxHeight * 0.26f).coerceIn(100.dp, 200.dp)
        assertEquals(expected, resolveOnboardingChefSize(maxHeight = maxHeight))
    }

    @Test
    fun givenVeryShortContentArea_whenResolvingChefSize_thenNeverDropsBelowMinimum() {
        assertEquals(100.dp, resolveOnboardingChefSize(maxHeight = 300.dp))
    }
}
