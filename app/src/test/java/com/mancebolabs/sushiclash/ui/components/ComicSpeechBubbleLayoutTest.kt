package com.mancebolabs.sushiclash.ui.components

import com.mancebolabs.sushiclash.feature.onboarding.OnboardingChefPointToward
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import androidx.compose.ui.unit.dp

class ComicSpeechBubbleLayoutTest {

    @Test
    fun givenCenterFraction_whenResolvingTailOffset_thenCentersTail() {
        val offset = resolveComicSpeechBubbleTailOffsetX(
            containerWidth = 300.dp,
            tailCenterFraction = 0.5f,
        )

        assertEquals(136.dp, offset)
    }

    @Test
    fun givenLeftChefLayout_whenResolvingTailCenterFraction_thenAlignsOverChefCenter() {
        val fraction = resolveOnboardingSpeechBubbleTailCenterFraction(
            chefPointToward = OnboardingChefPointToward.FeatureIllustration,
            contentWidth = 320.dp,
            bubbleWidth = 300.dp,
            chefSize = 140.dp,
            rowHorizontalPadding = 8.dp,
        )

        assertEquals(68f / 300f, fraction, 0.001f)
        assertEquals(54.dp, resolveComicSpeechBubbleTailOffsetX(300.dp, fraction))
    }

    @Test
    fun givenRightChefLayout_whenResolvingTailCenterFraction_thenAlignsOverChefCenter() {
        val fraction = resolveOnboardingSpeechBubbleTailCenterFraction(
            chefPointToward = OnboardingChefPointToward.FeatureIllustrationStart,
            contentWidth = 320.dp,
            bubbleWidth = 300.dp,
            chefSize = 140.dp,
            rowHorizontalPadding = 8.dp,
        )

        assertEquals(232f / 300f, fraction, 0.001f)
        assertEquals(218.dp, resolveComicSpeechBubbleTailOffsetX(300.dp, fraction))
    }

    @Test
    fun givenCenteredChef_whenResolvingTailCenterFraction_thenUsesCenter() {
        val fraction = resolveOnboardingSpeechBubbleTailCenterFraction(
            chefPointToward = OnboardingChefPointToward.Center,
            contentWidth = 320.dp,
            bubbleWidth = 300.dp,
            chefSize = 140.dp,
        )

        assertEquals(0.5f, fraction)
        assertTrue(fraction.isComicSpeechBubbleTailCentered())
    }

    @Test
    fun givenExtremeLeftFraction_whenClampingTailCenter_thenRespectsCornerInset() {
        val fraction = clampTailCenterFraction(
            rawFraction = 0.05f,
            bubbleWidth = 300.dp,
        )

        assertEquals((26f / 300f), fraction, 0.001f)
    }

    @Test
    fun givenNarrowBubble_whenResolvingLeftChefTail_thenStaysWithinSafeBounds() {
        val fraction = resolveOnboardingSpeechBubbleTailCenterFraction(
            chefPointToward = OnboardingChefPointToward.FeatureIllustration,
            contentWidth = 120.dp,
            bubbleWidth = 120.dp,
            chefSize = 100.dp,
            rowHorizontalPadding = 8.dp,
        )
        val offset = resolveComicSpeechBubbleTailOffsetX(
            containerWidth = 120.dp,
            tailCenterFraction = fraction,
        )

        assertTrue(offset >= 0.dp)
        assertTrue(offset + ComicSpeechBubbleTailWidth <= 120.dp)
    }
}
