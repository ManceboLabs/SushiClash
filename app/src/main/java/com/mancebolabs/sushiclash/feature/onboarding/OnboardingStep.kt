package com.mancebolabs.sushiclash.feature.onboarding

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

@Immutable
data class OnboardingStep(
    val titleRes: Int,
    val descriptionRes: Int,
    val illustration: OnboardingIllustration,
)

@Immutable
sealed interface OnboardingIllustration {
    data class DrawableResource(
        @param:DrawableRes val resId: Int,
    ) : OnboardingIllustration

    data class VectorIcon(
        val imageVector: ImageVector,
    ) : OnboardingIllustration
}
