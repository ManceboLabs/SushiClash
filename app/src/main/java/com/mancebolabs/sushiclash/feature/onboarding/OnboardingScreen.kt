package com.mancebolabs.sushiclash.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mancebolabs.sushiclash.R
import com.mancebolabs.sushiclash.ui.components.ItamaeGhostButton
import com.mancebolabs.sushiclash.ui.components.ItamaePrimaryButton
import com.mancebolabs.sushiclash.ui.components.SushiIcon
import com.mancebolabs.sushiclash.ui.theme.ItamaePreviewTheme
import com.mancebolabs.sushiclash.ui.theme.ItamaeShapes
import com.mancebolabs.sushiclash.ui.theme.ItamaeSpacing
import com.mancebolabs.sushiclash.ui.theme.itamaeScreenTopInsets
import kotlinx.coroutines.launch

internal const val ONBOARDING_PAGER_TEST_TAG = "onboarding_pager"

private val OnboardingIllustrationMaxSize = 220.dp
private val OnboardingIllustrationMinSize = 120.dp
private const val OnboardingIllustrationMaxHeightFraction = 0.30f

internal fun resolveOnboardingIllustrationSize(maxHeight: Dp): Dp {
    return minOf(
        OnboardingIllustrationMaxSize,
        maxHeight * OnboardingIllustrationMaxHeightFraction,
    ).coerceIn(OnboardingIllustrationMinSize, OnboardingIllustrationMaxSize)
}

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
    val safeInitialIndex = initialStepIndex.coerceIn(0, (steps.size - 1).coerceAtLeast(0))
    val pagerState = rememberPagerState(
        initialPage = safeInitialIndex,
        pageCount = { steps.size },
    )
    val scope = rememberCoroutineScope()
    val currentStepIndex = pagerState.currentPage
    val isFirstStep = currentStepIndex == 0
    val isLastStep = currentStepIndex == steps.lastIndex

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .itamaeScreenTopInsets()
            .navigationBarsPadding()
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

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag(ONBOARDING_PAGER_TEST_TAG),
            beyondViewportPageCount = 0,
        ) { page ->
            OnboardingStepContent(
                step = steps[page],
                modifier = Modifier.fillMaxSize(),
            )
        }

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
            onPrevious = {
                scope.launch {
                    pagerState.animateScrollToPage(currentStepIndex - 1)
                }
            },
            onNext = {
                scope.launch {
                    pagerState.animateScrollToPage(currentStepIndex + 1)
                }
            },
            onFinish = onFinish,
        )
    }
}

@Composable
fun OnboardingStepContent(
    step: OnboardingStep,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
    ) {
        val illustrationSize = resolveOnboardingIllustrationSize(maxHeight)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OnboardingIllustration(
                illustration = step.illustration,
                containerSize = illustrationSize,
            )

            Spacer(modifier = Modifier.height(ItamaeSpacing.lg))

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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ItamaeSpacing.sm),
            )

            Spacer(modifier = Modifier.height(ItamaeSpacing.md))
        }
    }
}

@Composable
private fun OnboardingIllustration(
    illustration: OnboardingIllustration,
    containerSize: Dp,
    modifier: Modifier = Modifier,
) {
    val iconSize = containerSize * 0.64f

    Box(
        modifier = modifier
            .size(containerSize)
            .clip(ItamaeShapes.large)
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
        contentAlignment = Alignment.Center,
    ) {
        when (illustration) {
            is OnboardingIllustration.DrawableResource -> {
                SushiIcon(
                    contentDescription = null,
                    modifier = Modifier.size(iconSize),
                )
            }
            is OnboardingIllustration.VectorIcon -> {
                Icon(
                    imageVector = illustration.imageVector,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize * 0.86f),
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

@Preview(name = "Onboarding – Last step small phone", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun OnboardingLastStepSmallPhonePreview() {
    ItamaePreviewTheme {
        OnboardingScreen(
            steps = previewSteps,
            initialStepIndex = previewSteps.lastIndex,
            onSkip = {},
            onFinish = {},
        )
    }
}

@Preview(
    name = "Onboarding – Last step large font",
    showBackground = true,
    widthDp = 360,
    heightDp = 640,
    fontScale = 1.3f,
)
@Composable
private fun OnboardingLastStepLargeFontPreview() {
    ItamaePreviewTheme {
        OnboardingScreen(
            steps = previewSteps,
            initialStepIndex = previewSteps.lastIndex,
            onSkip = {},
            onFinish = {},
        )
    }
}

@Preview(name = "Onboarding – Achievements step small phone", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun OnboardingAchievementsStepSmallPhonePreview() {
    ItamaePreviewTheme {
        OnboardingScreen(
            steps = previewSteps,
            initialStepIndex = 6,
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
