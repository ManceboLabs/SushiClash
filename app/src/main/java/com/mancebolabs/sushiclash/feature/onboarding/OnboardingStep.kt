package com.mancebolabs.sushiclash.feature.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

@Immutable
data class OnboardingStep(
    @param:StringRes val dialogueRes: Int,
    val chefRole: OnboardingChefRole,
    @param:StringRes val titleRes: Int? = null,
    @param:StringRes val descriptionRes: Int? = null,
    val illustration: OnboardingIllustration? = null,
    val chefPointToward: OnboardingChefPointToward = OnboardingChefPointToward.FeatureIllustration,
)

enum class OnboardingChefRole {
    Greeting,
    Tutorial,
}

enum class OnboardingChefPointToward {
    /** Chef centered beneath the speech bubble. */
    Center,

    /** Chef on the left, pointing toward feature content on the right. */
    FeatureIllustration,

    /** Mirrored chef on the right, pointing toward feature content on the left. */
    FeatureIllustrationStart,
}

@Immutable
sealed interface OnboardingIllustration {
    data class DrawableResource(
        @param:DrawableRes val resId: Int,
    ) : OnboardingIllustration

    data class VectorIcon(
        val imageVector: ImageVector,
    ) : OnboardingIllustration
}
