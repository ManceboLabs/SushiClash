package com.mancebolabs.sushiclash.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mancebolabs.sushiclash.R
import com.mancebolabs.sushiclash.ui.components.ItamaeGhostButton
import com.mancebolabs.sushiclash.ui.components.ItamaePrimaryButton
import com.mancebolabs.sushiclash.ui.components.SushiIcon
import com.mancebolabs.sushiclash.ui.theme.ItamaePreviewTheme
import com.mancebolabs.sushiclash.ui.theme.ItamaeShapes
import com.mancebolabs.sushiclash.ui.theme.ItamaeSpacing
import com.mancebolabs.sushiclash.ui.theme.itamaeScreenTopInsets

fun defaultOnboardingSteps(): List<OnboardingStep> = listOf(
    OnboardingStep(
        titleRes = R.string.onboarding_step_welcome_title,
        descriptionRes = R.string.onboarding_step_welcome_description,
        illustration = OnboardingIllustration.DrawableResource(R.drawable.ic_sushi),
    ),
    OnboardingStep(
        titleRes = R.string.onboarding_step_solo_title,
        descriptionRes = R.string.onboarding_step_solo_description,
        illustration = OnboardingIllustration.DrawableResource(R.drawable.ic_sushi),
    ),
    OnboardingStep(
        titleRes = R.string.onboarding_step_group_title,
        descriptionRes = R.string.onboarding_step_group_description,
        illustration = OnboardingIllustration.VectorIcon(Icons.Outlined.Groups),
    ),
    OnboardingStep(
        titleRes = R.string.onboarding_step_roulette_title,
        descriptionRes = R.string.onboarding_step_roulette_description,
        illustration = OnboardingIllustration.VectorIcon(Icons.Outlined.Casino),
    ),
    OnboardingStep(
        titleRes = R.string.onboarding_step_random_roulette_title,
        descriptionRes = R.string.onboarding_step_random_roulette_description,
        illustration = OnboardingIllustration.VectorIcon(Icons.Outlined.Autorenew),
    ),
    OnboardingStep(
        titleRes = R.string.onboarding_step_history_title,
        descriptionRes = R.string.onboarding_step_history_description,
        illustration = OnboardingIllustration.VectorIcon(Icons.Outlined.History),
    ),
    OnboardingStep(
        titleRes = R.string.onboarding_step_achievements_title,
        descriptionRes = R.string.onboarding_step_achievements_description,
        illustration = OnboardingIllustration.VectorIcon(Icons.Filled.EmojiEvents),
    ),
    OnboardingStep(
        titleRes = R.string.onboarding_step_responsible_use_title,
        descriptionRes = R.string.onboarding_step_responsible_use_description,
        illustration = OnboardingIllustration.DrawableResource(R.drawable.ic_sushi),
    ),
)

@Composable
fun OnboardingScreen(
    steps: List<OnboardingStep>,
    onSkip: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
    initialStepIndex: Int = 0,
) {
    var currentStepIndex by remember(steps, initialStepIndex) {
        mutableIntStateOf(initialStepIndex.coerceIn(0, (steps.size - 1).coerceAtLeast(0)))
    }
    val currentStep = steps[currentStepIndex]
    val isFirstStep = currentStepIndex == 0
    val isLastStep = currentStepIndex == steps.lastIndex

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .itamaeScreenTopInsets()
            .padding(horizontal = ItamaeSpacing.marginMobile)
            .padding(bottom = ItamaeSpacing.xl),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            ItamaeGhostButton(
                text = stringResource(R.string.onboarding_skip),
                onClick = onSkip,
            )
        }

        Spacer(modifier = Modifier.height(ItamaeSpacing.md))

        OnboardingStepContent(
            step = currentStep,
            modifier = Modifier.weight(1f),
        )

        Spacer(modifier = Modifier.height(ItamaeSpacing.lg))

        OnboardingProgressDots(
            stepCount = steps.size,
            currentStepIndex = currentStepIndex,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Spacer(modifier = Modifier.height(ItamaeSpacing.lg))

        OnboardingNavigationButtons(
            showPrevious = !isFirstStep,
            showNext = !isLastStep,
            finishLabel = stringResource(R.string.onboarding_finish),
            onPrevious = { currentStepIndex -= 1 },
            onNext = { currentStepIndex += 1 },
            onFinish = onFinish,
        )
    }
}

@Composable
fun OnboardingStepContent(
    step: OnboardingStep,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        OnboardingIllustration(
            illustration = step.illustration,
            modifier = Modifier.size(220.dp),
        )

        Spacer(modifier = Modifier.height(ItamaeSpacing.xl))

        Text(
            text = stringResource(step.titleRes),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(ItamaeSpacing.md))

        Text(
            text = stringResource(step.descriptionRes),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = ItamaeSpacing.sm),
        )
    }
}

@Composable
private fun OnboardingIllustration(
    illustration: OnboardingIllustration,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(ItamaeShapes.large)
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
        contentAlignment = Alignment.Center,
    ) {
        when (illustration) {
            is OnboardingIllustration.DrawableResource -> {
                SushiIcon(
                    contentDescription = null,
                    modifier = Modifier.size(140.dp),
                )
            }
            is OnboardingIllustration.VectorIcon -> {
                Icon(
                    imageVector = illustration.imageVector,
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
fun OnboardingProgressDots(
    stepCount: Int,
    currentStepIndex: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(ItamaeSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(stepCount) { index ->
            val isSelected = index == currentStepIndex
            Box(
                modifier = Modifier
                    .size(if (isSelected) 12.dp else 8.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        },
                    ),
            )
        }
    }
}

@Composable
fun OnboardingNavigationButtons(
    showPrevious: Boolean,
    showNext: Boolean,
    finishLabel: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ItamaeSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showPrevious) {
            ItamaeGhostButton(
                text = stringResource(R.string.onboarding_previous),
                onClick = onPrevious,
                modifier = Modifier.weight(1f),
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        when {
            showNext -> {
                ItamaePrimaryButton(
                    text = stringResource(R.string.onboarding_next),
                    onClick = onNext,
                    modifier = Modifier.weight(1f),
                )
            }
            else -> {
                ItamaePrimaryButton(
                    text = finishLabel,
                    onClick = onFinish,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

private val previewSteps = defaultOnboardingSteps()

@Preview(name = "Onboarding – Light", showBackground = true, heightDp = 780)
@Composable
private fun OnboardingLightPreview() {
    ItamaePreviewTheme(darkTheme = false) {
        OnboardingScreen(
            steps = previewSteps,
            onSkip = {},
            onFinish = {},
        )
    }
}

@Preview(name = "Onboarding – Dark", showBackground = true, heightDp = 780)
@Composable
private fun OnboardingDarkPreview() {
    ItamaePreviewTheme(darkTheme = true) {
        OnboardingScreen(
            steps = previewSteps,
            onSkip = {},
            onFinish = {},
        )
    }
}

@Preview(name = "Onboarding – First step", showBackground = true, heightDp = 780)
@Composable
private fun OnboardingFirstStepPreview() {
    ItamaePreviewTheme {
        OnboardingScreen(
            steps = previewSteps,
            initialStepIndex = 0,
            onSkip = {},
            onFinish = {},
        )
    }
}

@Preview(name = "Onboarding – Middle step", showBackground = true, heightDp = 780)
@Composable
private fun OnboardingMiddleStepPreview() {
    ItamaePreviewTheme {
        OnboardingScreen(
            steps = previewSteps,
            initialStepIndex = 2,
            onSkip = {},
            onFinish = {},
        )
    }
}

@Preview(name = "Onboarding – Last step", showBackground = true, heightDp = 780)
@Composable
private fun OnboardingLastStepPreview() {
    ItamaePreviewTheme {
        OnboardingScreen(
            steps = previewSteps,
            initialStepIndex = previewSteps.lastIndex,
            onSkip = {},
            onFinish = {},
        )
    }
}

@Preview(name = "Onboarding step content – Achievements", showBackground = true)
@Composable
private fun OnboardingAchievementsStepContentPreview() {
    ItamaePreviewTheme {
        OnboardingStepContent(
            step = defaultOnboardingSteps()[6],
        )
    }
}

@Preview(name = "Onboarding step content – Responsible use", showBackground = true)
@Composable
private fun OnboardingResponsibleUseStepContentPreview() {
    ItamaePreviewTheme {
        OnboardingStepContent(
            step = defaultOnboardingSteps().last(),
        )
    }
}

@Preview(name = "Onboarding step content", showBackground = true)
@Composable
private fun OnboardingStepContentPreview() {
    ItamaePreviewTheme {
        OnboardingStepContent(
            step = OnboardingStep(
                titleRes = R.string.onboarding_step_solo_title,
                descriptionRes = R.string.onboarding_step_solo_description,
                illustration = OnboardingIllustration.VectorIcon(Icons.Outlined.TouchApp),
            ),
        )
    }
}

@Preview(name = "Onboarding progress dots", showBackground = true)
@Composable
private fun OnboardingProgressDotsPreview() {
    ItamaePreviewTheme {
        OnboardingProgressDots(stepCount = defaultOnboardingSteps().size, currentStepIndex = 2)
    }
}
