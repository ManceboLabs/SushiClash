package com.mancebolabs.sushiclash.ui.components

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import com.mancebolabs.sushiclash.feature.onboarding.OnboardingChefPointToward
import com.mancebolabs.sushiclash.ui.theme.ItamaeSpacing
import kotlin.math.abs

internal val ComicSpeechBubbleMaxWidth = 300.dp
internal val ComicSpeechBubbleTailWidth = 28.dp
private val ComicSpeechBubbleCornerRadius = 22.dp
private val ComicSpeechBubbleTailCornerPadding = 4.dp
private const val CenterTailFraction = 0.5f
private const val CenterTailFractionTolerance = 0.001f

internal fun Float.isComicSpeechBubbleTailCentered(): Boolean {
    return abs(this - CenterTailFraction) < CenterTailFractionTolerance
}

internal fun resolveOnboardingSpeechBubbleWidth(contentWidth: Dp): Dp {
    return minOf(contentWidth, ComicSpeechBubbleMaxWidth)
}

internal fun resolveOnboardingSpeechBubbleTailCenterFraction(
    chefPointToward: OnboardingChefPointToward,
    contentWidth: Dp,
    bubbleWidth: Dp,
    chefSize: Dp,
    rowHorizontalPadding: Dp = ItamaeSpacing.sm,
): Float {
    if (chefPointToward == OnboardingChefPointToward.Center || contentWidth <= 0.dp || bubbleWidth <= 0.dp) {
        return CenterTailFraction
    }

    val bubbleLeadingInset = ((contentWidth - bubbleWidth) / 2).coerceAtLeast(0.dp)
    val chefCenterInContent = when (chefPointToward) {
        OnboardingChefPointToward.FeatureIllustration -> rowHorizontalPadding + chefSize / 2
        OnboardingChefPointToward.FeatureIllustrationStart -> contentWidth - rowHorizontalPadding - chefSize / 2
        OnboardingChefPointToward.Center -> contentWidth / 2
    }
    val chefCenterInBubble = chefCenterInContent - bubbleLeadingInset
    val rawFraction = chefCenterInBubble.value / bubbleWidth.value

    return clampTailCenterFraction(rawFraction, bubbleWidth)
}

internal fun clampTailCenterFraction(rawFraction: Float, bubbleWidth: Dp): Float {
    if (bubbleWidth <= 0.dp) return CenterTailFraction

    val cornerSafeInset = ComicSpeechBubbleCornerRadius + ComicSpeechBubbleTailCornerPadding
    val minFraction = cornerSafeInset.value / bubbleWidth.value
    val maxFraction = (bubbleWidth - cornerSafeInset).value / bubbleWidth.value
    return rawFraction.coerceIn(minFraction, maxFraction)
}

internal fun resolveComicSpeechBubbleTailOffsetX(
    containerWidth: Dp,
    tailCenterFraction: Float = CenterTailFraction,
    tailWidth: Dp = ComicSpeechBubbleTailWidth,
): Dp {
    if (containerWidth <= 0.dp) return 0.dp

    val clampedFraction = clampTailCenterFraction(tailCenterFraction, containerWidth)
    val tailHalfWidth = tailWidth / 2
    val tailCenterX = containerWidth * clampedFraction

    return (tailCenterX - tailHalfWidth)
        .coerceIn(0.dp, (containerWidth - tailWidth).coerceAtLeast(0.dp))
}
