package com.mancebolabs.sushiclash.onboarding

import com.mancebolabs.sushiclash.feature.onboarding.resolveOnboardingIllustrationSize
import org.junit.Assert.assertEquals
import org.junit.Test
import androidx.compose.ui.unit.dp

class OnboardingLayoutTest {

    @Test
    fun givenTallContentArea_whenResolvingIllustrationSize_thenUsesDesignMaximum() {
        assertEquals(220.dp, resolveOnboardingIllustrationSize(maxHeight = 900.dp))
    }

    @Test
    fun givenShortContentArea_whenResolvingIllustrationSize_thenScalesDownWithinBounds() {
        assertEquals(168.dp, resolveOnboardingIllustrationSize(maxHeight = 560.dp))
    }

    @Test
    fun givenVeryShortContentArea_whenResolvingIllustrationSize_thenNeverDropsBelowMinimum() {
        assertEquals(120.dp, resolveOnboardingIllustrationSize(maxHeight = 300.dp))
    }
}
