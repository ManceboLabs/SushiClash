package com.mancebolabs.sushiclash.feature.achievements

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.mancebolabs.sushiclash.R
import com.mancebolabs.sushiclash.domain.model.achievement.AchievementId
import com.mancebolabs.sushiclash.domain.model.achievement.AchievementUnlock
import com.mancebolabs.sushiclash.feature.feedback.AndroidGameFeedbackController
import com.mancebolabs.sushiclash.feature.feedback.GameFeedbackController
import com.mancebolabs.sushiclash.ui.components.ItamaeCard
import com.mancebolabs.sushiclash.ui.theme.ItamaePreviewTheme
import com.mancebolabs.sushiclash.ui.theme.ItamaeSpacing

private val achievementNotificationEnterTransition =
    fadeIn(animationSpec = tween(durationMillis = ACHIEVEMENT_NOTIFICATION_EXIT_ANIMATION_MS.toInt())) +
        slideInVertically(
            animationSpec = tween(durationMillis = ACHIEVEMENT_NOTIFICATION_EXIT_ANIMATION_MS.toInt()),
        ) { fullHeight -> -fullHeight }

private val achievementNotificationExitTransition =
    fadeOut(animationSpec = tween(durationMillis = ACHIEVEMENT_NOTIFICATION_EXIT_ANIMATION_MS.toInt())) +
        slideOutVertically(
            animationSpec = tween(durationMillis = ACHIEVEMENT_NOTIFICATION_EXIT_ANIMATION_MS.toInt()),
        ) { fullHeight -> -fullHeight }

@Composable
fun AchievementNotificationHost(
    vibrationEnabled: Boolean,
    onNavigateToAchievements: () -> Unit,
    modifier: Modifier = Modifier,
    feedbackController: GameFeedbackController = AndroidGameFeedbackController(LocalView.current),
) {
    var displayedUnlock by remember { mutableStateOf<AchievementUnlock?>(null) }
    var isBannerVisible by remember { mutableStateOf(false) }
    val processor = remember { AchievementNotificationSequenceProcessor() }

    val currentVibrationEnabled = rememberUpdatedState(vibrationEnabled)
    val currentFeedbackController = rememberUpdatedState(feedbackController)
    val currentNavigateToAchievements = rememberUpdatedState(onNavigateToAchievements)

    LaunchedEffect(processor) {
        processor.process(
            events = AchievementNotificationDispatcher.events,
            onFeedback = { unlock ->
                currentFeedbackController.value.playAchievementUnlocked(
                    vibrationEnabled = currentVibrationEnabled.value,
                )
            },
            onStateChange = { state ->
                displayedUnlock = state.unlock
                isBannerVisible = state.visible
            },
        )
    }

    Box(modifier = modifier.fillMaxWidth()) {
        AnimatedVisibility(
            visible = isBannerVisible,
            enter = achievementNotificationEnterTransition,
            exit = achievementNotificationExitTransition,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = ItamaeSpacing.marginMobile)
                .padding(top = ItamaeSpacing.md),
        ) {
            displayedUnlock?.let { unlock ->
                AchievementNotificationBanner(
                    achievementId = unlock.achievementId,
                    onClick = {
                        processor.requestDismiss()
                        currentNavigateToAchievements.value()
                    },
                )
            }
        }
    }
}

@Composable
internal fun AchievementNotificationBanner(
    achievementId: AchievementId,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ItamaeCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ItamaeSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ItamaeSpacing.md),
        ) {
            Icon(
                imageVector = Icons.Filled.EmojiEvents,
                contentDescription = null,
                modifier = Modifier.size(ItamaeSpacing.lg),
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(ItamaeSpacing.xs),
            ) {
                Text(
                    text = stringResource(R.string.achievement_unlocked_banner),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = achievementTitle(achievementId),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Preview(name = "Achievement unlock banner", showBackground = true)
@Composable
private fun AchievementUnlockBannerPreview() {
    ItamaePreviewTheme {
        AchievementNotificationBanner(
            achievementId = AchievementId.SUSHI_10,
            onClick = {},
        )
    }
}
