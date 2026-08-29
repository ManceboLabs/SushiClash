package com.mancebolabs.sushiclash.feature.achievements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mancebolabs.sushiclash.R
import com.mancebolabs.sushiclash.domain.model.achievement.AchievementCatalog
import com.mancebolabs.sushiclash.domain.model.achievement.AchievementCategory
import com.mancebolabs.sushiclash.domain.model.achievement.AchievementId
import com.mancebolabs.sushiclash.ui.components.ItamaeCard
import com.mancebolabs.sushiclash.ui.components.ItamaeGhostButton
import com.mancebolabs.sushiclash.ui.components.ItamaeScreenTitle
import com.mancebolabs.sushiclash.ui.components.SushiIcon
import com.mancebolabs.sushiclash.ui.theme.ItamaePreviewTheme
import com.mancebolabs.sushiclash.ui.theme.ItamaeSpacing
import com.mancebolabs.sushiclash.ui.theme.itamaeScreenTopInsets
import com.mancebolabs.sushiclash.ui.theme.rememberItamaeBottomContentPadding
import java.text.DateFormat
import java.util.Date

@Composable
fun AchievementsScreen(
    uiState: AchievementsUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val bottomContentPadding = rememberItamaeBottomContentPadding(scrollable = true)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .itamaeScreenTopInsets()
            .verticalScroll(scrollState)
            .padding(horizontal = ItamaeSpacing.marginMobile)
            .padding(bottom = bottomContentPadding),
        verticalArrangement = Arrangement.spacedBy(ItamaeSpacing.lg),
    ) {
        ItamaeGhostButton(
            text = stringResource(R.string.achievements_back),
            onClick = onBack,
        )

        ItamaeScreenTitle(title = stringResource(R.string.achievements_screen_title))

        Text(
            text = stringResource(
                R.string.achievements_progress_summary,
                uiState.unlockedCount,
                uiState.totalCount,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        uiState.categories.forEach { category ->
            AchievementCategorySection(category = category)
        }
    }
}

@Composable
private fun AchievementCategorySection(
    category: AchievementCategoryUiState,
) {
    Column(verticalArrangement = Arrangement.spacedBy(ItamaeSpacing.sm)) {
        Text(
            text = achievementCategoryTitle(category.category),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        ItamaeCard {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(ItamaeSpacing.md),
            ) {
                category.achievements.forEach { achievement ->
                    AchievementRow(achievement = achievement, category = category.category)
                }
            }
        }
    }
}

@Composable
private fun AchievementRow(
    achievement: AchievementItemUiState,
    category: AchievementCategory,
) {
    val dateFormat = remember { DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(ItamaeSpacing.md),
    ) {
        AchievementIcon(
            isUnlocked = achievement.isUnlocked,
            category = category,
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(ItamaeSpacing.xs),
        ) {
            Text(
                text = achievementTitle(achievement.id),
                style = MaterialTheme.typography.bodyLarge,
                color = if (achievement.isUnlocked) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
            Text(
                text = achievementDescription(achievement.id),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (!achievement.isUnlocked && achievement.target > 1) {
                LinearProgressIndicator(
                    progress = {
                        achievement.progress.toFloat() / achievement.target.toFloat()
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = stringResource(
                        R.string.achievements_progress_value,
                        achievement.progress,
                        achievement.target,
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            achievement.unlockedAtEpochMillis?.let { unlockedAt ->
                Text(
                    text = stringResource(
                        R.string.achievements_unlocked_at,
                        dateFormat.format(Date(unlockedAt)),
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun AchievementIcon(
    isUnlocked: Boolean,
    category: AchievementCategory,
) {
    val containerColor = if (isUnlocked) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest
    }
    val iconTint = if (isUnlocked) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .size(ItamaeSpacing.xl)
            .clip(CircleShape)
            .background(containerColor),
        contentAlignment = Alignment.Center,
    ) {
        if (isUnlocked) {
            when (category) {
                AchievementCategory.SUSHI,
                AchievementCategory.SOLO_LIFETIME_SUSHI,
                AchievementCategory.GROUP_LIFETIME_SUSHI,
                -> {
                    SushiIcon(
                        contentDescription = null,
                        modifier = Modifier.size(ItamaeSpacing.lg),
                    )
                }
                AchievementCategory.GAMES -> {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        modifier = Modifier.size(ItamaeSpacing.lg),
                        tint = iconTint,
                    )
                }
                AchievementCategory.ROULETTE -> {
                    Icon(
                        imageVector = Icons.Filled.Casino,
                        contentDescription = null,
                        modifier = Modifier.size(ItamaeSpacing.lg),
                        tint = iconTint,
                    )
                }
            }
        } else {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
                modifier = Modifier.size(ItamaeSpacing.lg),
                tint = iconTint,
            )
        }
    }
}

@Preview(name = "Achievements – Light", showBackground = true)
@Composable
private fun AchievementsLightPreview() {
    ItamaePreviewTheme(darkTheme = false) {
        AchievementsScreen(
            uiState = AchievementsUiState(
                categories = listOf(
                    AchievementCategoryUiState(
                        category = AchievementCategory.SUSHI,
                        achievements = listOf(
                            AchievementItemUiState(
                                id = AchievementId.SUSHI_10,
                                isUnlocked = true,
                                progress = 10,
                                target = 10,
                                unlockedAtEpochMillis = 1_700_000_000_000L,
                            ),
                            AchievementItemUiState(
                                id = AchievementId.SUSHI_20,
                                isUnlocked = false,
                                progress = 7,
                                target = 20,
                                unlockedAtEpochMillis = null,
                            ),
                        ),
                    ),
                ),
                unlockedCount = 1,
                totalCount = AchievementCatalog.definitions.size,
            ),
            onBack = {},
        )
    }
}

@Preview(name = "Achievements – Dark", showBackground = true)
@Composable
private fun AchievementsDarkPreview() {
    ItamaePreviewTheme(darkTheme = true) {
        AchievementsScreen(
            uiState = AchievementsUiState(
                categories = listOf(
                    AchievementCategoryUiState(
                        category = AchievementCategory.ROULETTE,
                        achievements = listOf(
                            AchievementItemUiState(
                                id = AchievementId.ROULETTE_FIRST_SPIN,
                                isUnlocked = false,
                                progress = 0,
                                target = 1,
                                unlockedAtEpochMillis = null,
                            ),
                        ),
                    ),
                ),
                unlockedCount = 0,
                totalCount = AchievementCatalog.definitions.size,
            ),
            onBack = {},
        )
    }
}

