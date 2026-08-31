package com.mancebolabs.sushiclash.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mancebolabs.sushiclash.R
import com.mancebolabs.sushiclash.domain.model.AppLanguage
import com.mancebolabs.sushiclash.domain.model.AppThemeMode
import com.mancebolabs.sushiclash.ui.components.ConfirmationDialog
import androidx.compose.ui.platform.testTag
import com.mancebolabs.sushiclash.testing.SushiClashTestTags
import com.mancebolabs.sushiclash.ui.components.ItamaeCard
import com.mancebolabs.sushiclash.ui.components.ItamaeScreenTitle
import com.mancebolabs.sushiclash.ui.components.PersistenceErrorMessage
import com.mancebolabs.sushiclash.ui.theme.ItamaePreviewTheme
import com.mancebolabs.sushiclash.ui.theme.ItamaeShapes
import com.mancebolabs.sushiclash.ui.theme.ItamaeSpacing
import com.mancebolabs.sushiclash.ui.theme.itamaeScreenTopInsets
import com.mancebolabs.sushiclash.ui.theme.rememberItamaeBottomContentPadding

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onThemeModeSelected: (AppThemeMode) -> Unit,
    onLanguagePickerRequested: () -> Unit,
    onLanguagePickerDismissed: () -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    onAppLanguageRefreshRequested: () -> Unit,
    onSoundEnabledChanged: (Boolean) -> Unit,
    onVibrationEnabledChanged: (Boolean) -> Unit,
    onClearHistoryRequested: () -> Unit,
    onClearHistoryConfirmed: () -> Unit,
    onClearHistoryDismissed: () -> Unit,
    onClearAchievementsRequested: () -> Unit,
    onClearAchievementsConfirmed: () -> Unit,
    onClearAchievementsDismissed: () -> Unit,
    onViewTutorialRequested: () -> Unit,
    onViewAchievementsRequested: () -> Unit,
    onPersistenceRetry: () -> Unit,
    appVersion: String = "",
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val bottomContentPadding = rememberItamaeBottomContentPadding(scrollable = true)
    val configuration = LocalConfiguration.current

    LaunchedEffect(configuration) {
        onAppLanguageRefreshRequested()
    }

    if (uiState.showLanguagePickerDialog) {
        LanguagePickerDialog(
            selectedLanguage = uiState.activeAppLanguage,
            onLanguageSelected = onLanguageSelected,
            onDismiss = onLanguagePickerDismissed,
        )
    }

    if (uiState.showClearHistoryDialog) {
        ConfirmationDialog(
            title = stringResource(R.string.settings_clear_history_title),
            message = stringResource(R.string.settings_clear_history_message),
            confirmLabel = stringResource(R.string.settings_clear_history_confirm),
            dismissLabel = stringResource(R.string.counter_cancel),
            onConfirm = onClearHistoryConfirmed,
            onDismiss = onClearHistoryDismissed,
        )
    }

    if (uiState.showClearAchievementsDialog) {
        ConfirmationDialog(
            title = stringResource(R.string.settings_clear_achievements_title),
            message = stringResource(R.string.settings_clear_achievements_message),
            confirmLabel = stringResource(R.string.settings_clear_achievements_confirm),
            dismissLabel = stringResource(R.string.counter_cancel),
            onConfirm = onClearAchievementsConfirmed,
            onDismiss = onClearAchievementsDismissed,
        )
    }

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
        ItamaeScreenTitle(title = stringResource(R.string.settings_screen_title))

        if (uiState.persistenceError) {
            PersistenceErrorMessage(
                isRetrying = uiState.isPersistenceRetrying,
                onRetry = onPersistenceRetry,
            )
        }

        SettingsSectionCard(
            title = stringResource(R.string.settings_language_section),
            icon = Icons.Outlined.Language,
        ) {
            val displayLanguage = AppLanguage.resolveEffectiveDisplayLanguage(uiState.activeAppLanguage)
            val activeLanguageLabel = stringResource(displayLanguage.labelRes())
            val languageRowDescription = stringResource(
                R.string.settings_language_open_picker_content_description,
                activeLanguageLabel,
            )
            SettingsActionRow(
                icon = Icons.Outlined.Language,
                title = stringResource(R.string.settings_language_section),
                description = activeLanguageLabel,
                trailing = SettingsTrailing.Chevron,
                onClick = onLanguagePickerRequested,
                contentDescription = languageRowDescription,
            )
        }

        SettingsSectionCard(
            title = stringResource(R.string.settings_appearance_section),
            icon = Icons.Outlined.Palette,
        ) {
            ThemeOptionCard(
                title = stringResource(R.string.settings_theme_light),
                selected = uiState.themeMode == AppThemeMode.LIGHT,
                icon = Icons.Outlined.LightMode,
                onClick = { onThemeModeSelected(AppThemeMode.LIGHT) },
            )

            Spacer(modifier = Modifier.height(ItamaeSpacing.sm))

            ThemeOptionCard(
                title = stringResource(R.string.settings_theme_dark),
                selected = uiState.themeMode == AppThemeMode.DARK,
                icon = Icons.Outlined.DarkMode,
                onClick = { onThemeModeSelected(AppThemeMode.DARK) },
            )
        }

        SettingsSectionCard(
            title = stringResource(R.string.settings_feedback_section),
            icon = Icons.AutoMirrored.Outlined.VolumeUp,
        ) {
            SettingsToggleRow(
                icon = Icons.AutoMirrored.Outlined.VolumeUp,
                title = stringResource(R.string.settings_sound_title),
                description = stringResource(R.string.settings_sound_description),
                checked = uiState.soundEnabled,
                onCheckedChange = onSoundEnabledChanged,
            )

            Spacer(modifier = Modifier.height(ItamaeSpacing.sm))

            SettingsToggleRow(
                icon = Icons.Outlined.Vibration,
                title = stringResource(R.string.settings_vibration_title),
                description = stringResource(R.string.settings_vibration_description),
                checked = uiState.vibrationEnabled,
                onCheckedChange = onVibrationEnabledChanged,
            )
        }

        SettingsSectionCard(
            title = stringResource(R.string.settings_help_section),
            icon = Icons.AutoMirrored.Outlined.HelpOutline,
        ) {
            SettingsActionRow(
                icon = Icons.AutoMirrored.Outlined.HelpOutline,
                title = stringResource(R.string.settings_view_tutorial),
                description = stringResource(R.string.settings_view_tutorial_description),
                trailing = SettingsTrailing.Chevron,
                onClick = onViewTutorialRequested,
                modifier = Modifier.testTag(SushiClashTestTags.SETTINGS_VIEW_TUTORIAL_ROW),
            )

            Spacer(modifier = Modifier.height(ItamaeSpacing.sm))

            SettingsActionRow(
                icon = Icons.Filled.EmojiEvents,
                title = stringResource(R.string.settings_view_achievements),
                description = stringResource(R.string.settings_view_achievements_description),
                trailing = SettingsTrailing.Chevron,
                onClick = onViewAchievementsRequested,
            )
        }

        SettingsSectionCard(
            title = stringResource(R.string.settings_history_section),
            icon = Icons.Outlined.DeleteOutline,
        ) {
            SettingsActionRow(
                icon = Icons.Outlined.DeleteOutline,
                title = stringResource(R.string.settings_clear_history),
                description = stringResource(R.string.settings_clear_history_description),
                trailing = SettingsTrailing.DestructiveButton(
                    label = stringResource(R.string.settings_clear_history),
                ),
                destructive = true,
                onClick = onClearHistoryRequested,
            )

            Spacer(modifier = Modifier.height(ItamaeSpacing.sm))

            SettingsActionRow(
                icon = Icons.Filled.EmojiEvents,
                title = stringResource(R.string.settings_clear_achievements),
                description = stringResource(R.string.settings_clear_achievements_description),
                trailing = SettingsTrailing.DestructiveButton(
                    label = stringResource(R.string.settings_clear_achievements),
                ),
                destructive = true,
                onClick = onClearAchievementsRequested,
            )
        }

        SettingsSectionCard(
            title = stringResource(R.string.settings_about_section),
            icon = Icons.Outlined.Info,
        ) {
            SettingsAboutContent(appVersion = appVersion)
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(ItamaeSpacing.sm),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        ItamaeCard {
            content()
        }
    }
}

private sealed interface SettingsTrailing {
    data object Chevron : SettingsTrailing

    data class DestructiveButton(val label: String) : SettingsTrailing
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    description: String,
    trailing: SettingsTrailing,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    destructive: Boolean = false,
    contentDescription: String? = null,
) {
    val iconTint = if (destructive) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }
    val titleColor = if (destructive) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val rowClickable = trailing is SettingsTrailing.Chevron

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (contentDescription != null) {
                    Modifier.semantics { this.contentDescription = contentDescription }
                } else {
                    Modifier
                },
            )
            .then(
                if (rowClickable) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            )
            .padding(vertical = ItamaeSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ItamaeSpacing.md),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(ItamaeSpacing.lg),
            tint = iconTint,
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(ItamaeSpacing.xs),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = titleColor,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        when (trailing) {
            SettingsTrailing.Chevron -> {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(ItamaeSpacing.lg),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            is SettingsTrailing.DestructiveButton -> {
                TextButton(onClick = onClick) {
                    Text(
                        text = trailing.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = ItamaeSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ItamaeSpacing.md),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(ItamaeSpacing.lg),
            tint = MaterialTheme.colorScheme.primary,
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(ItamaeSpacing.xs),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun SettingsAboutContent(appVersion: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = ItamaeSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ItamaeSpacing.md),
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            modifier = Modifier.size(ItamaeSpacing.lg),
            tint = MaterialTheme.colorScheme.primary,
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(ItamaeSpacing.xs),
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (appVersion.isNotBlank()) {
                Text(
                    text = stringResource(R.string.settings_about_version, appVersion),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = stringResource(R.string.settings_about_branding),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ThemeOptionCard(
    title: String,
    selected: Boolean,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ItamaeShapes.small)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = ItamaeShapes.small,
            )
            .background(containerColor)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            )
            .padding(horizontal = ItamaeSpacing.md, vertical = ItamaeSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ItamaeSpacing.sm),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(ItamaeSpacing.lg),
            tint = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = if (selected) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
        RadioButton(
            selected = selected,
            onClick = null,
        )
    }
}

@Preview(name = "Settings language picker open – Light", showBackground = true)
@Composable
private fun SettingsLanguagePickerOpenPreview() {
    ItamaePreviewTheme(darkTheme = false) {
        SettingsScreen(
            uiState = SettingsUiState(
                themeMode = AppThemeMode.LIGHT,
                activeAppLanguage = AppLanguage.ENGLISH,
                showLanguagePickerDialog = true,
            ),
            onThemeModeSelected = {},
            onLanguagePickerRequested = {},
            onLanguagePickerDismissed = {},
            onLanguageSelected = {},
            onAppLanguageRefreshRequested = {},
            onSoundEnabledChanged = {},
            onVibrationEnabledChanged = {},
            onClearHistoryRequested = {},
            onClearHistoryConfirmed = {},
            onClearHistoryDismissed = {},
            onClearAchievementsRequested = {},
            onClearAchievementsConfirmed = {},
            onClearAchievementsDismissed = {},
            onViewTutorialRequested = {},
            onViewAchievementsRequested = {},
            onPersistenceRetry = {},
            appVersion = "1.0",
        )
    }
}

@Preview(name = "Language picker dialog", showBackground = true)
@Composable
private fun LanguagePickerDialogPreview() {
    ItamaePreviewTheme {
        LanguagePickerDialog(
            selectedLanguage = AppLanguage.SPANISH,
            onLanguageSelected = {},
            onDismiss = {},
        )
    }
}

@Preview(name = "Settings – Light", showBackground = true)
@Composable
private fun SettingsLightPreview() {
    ItamaePreviewTheme(darkTheme = false) {
        SettingsScreen(
            uiState = SettingsUiState(themeMode = AppThemeMode.LIGHT),
            onThemeModeSelected = {},
            onLanguagePickerRequested = {},
            onLanguagePickerDismissed = {},
            onLanguageSelected = {},
            onAppLanguageRefreshRequested = {},
            onSoundEnabledChanged = {},
            onVibrationEnabledChanged = {},
            onClearHistoryRequested = {},
            onClearHistoryConfirmed = {},
            onClearHistoryDismissed = {},
            onClearAchievementsRequested = {},
            onClearAchievementsConfirmed = {},
            onClearAchievementsDismissed = {},
            onViewTutorialRequested = {},
            onViewAchievementsRequested = {},
            onPersistenceRetry = {},
            appVersion = "1.0",
        )
    }
}

@Preview(name = "Settings – Dark", showBackground = true)
@Composable
private fun SettingsDarkPreview() {
    ItamaePreviewTheme(darkTheme = true) {
        SettingsScreen(
            uiState = SettingsUiState(themeMode = AppThemeMode.DARK),
            onThemeModeSelected = {},
            onLanguagePickerRequested = {},
            onLanguagePickerDismissed = {},
            onLanguageSelected = {},
            onAppLanguageRefreshRequested = {},
            onSoundEnabledChanged = {},
            onVibrationEnabledChanged = {},
            onClearHistoryRequested = {},
            onClearHistoryConfirmed = {},
            onClearHistoryDismissed = {},
            onClearAchievementsRequested = {},
            onClearAchievementsConfirmed = {},
            onClearAchievementsDismissed = {},
            onViewTutorialRequested = {},
            onViewAchievementsRequested = {},
            onPersistenceRetry = {},
            appVersion = "1.0",
        )
    }
}

@Preview(name = "Clear history dialog", showBackground = true)
@Composable
private fun SettingsClearHistoryDialogPreview() {
    ItamaePreviewTheme {
        SettingsScreen(
            uiState = SettingsUiState(
                themeMode = AppThemeMode.LIGHT,
                showClearHistoryDialog = true,
            ),
            onThemeModeSelected = {},
            onLanguagePickerRequested = {},
            onLanguagePickerDismissed = {},
            onLanguageSelected = {},
            onAppLanguageRefreshRequested = {},
            onSoundEnabledChanged = {},
            onVibrationEnabledChanged = {},
            onClearHistoryRequested = {},
            onClearHistoryConfirmed = {},
            onClearHistoryDismissed = {},
            onClearAchievementsRequested = {},
            onClearAchievementsConfirmed = {},
            onClearAchievementsDismissed = {},
            onViewTutorialRequested = {},
            onViewAchievementsRequested = {},
            onPersistenceRetry = {},
            appVersion = "1.0",
        )
    }
}

@Preview(name = "Settings persistence error – Light", showBackground = true)
@Composable
private fun SettingsPersistenceErrorLightPreview() {
    ItamaePreviewTheme(darkTheme = false) {
        SettingsScreen(
            uiState = SettingsUiState(
                themeMode = AppThemeMode.LIGHT,
                persistenceError = true,
            ),
            onThemeModeSelected = {},
            onLanguagePickerRequested = {},
            onLanguagePickerDismissed = {},
            onLanguageSelected = {},
            onAppLanguageRefreshRequested = {},
            onSoundEnabledChanged = {},
            onVibrationEnabledChanged = {},
            onClearHistoryRequested = {},
            onClearHistoryConfirmed = {},
            onClearHistoryDismissed = {},
            onClearAchievementsRequested = {},
            onClearAchievementsConfirmed = {},
            onClearAchievementsDismissed = {},
            onViewTutorialRequested = {},
            onViewAchievementsRequested = {},
            onPersistenceRetry = {},
            appVersion = "1.0",
        )
    }
}

@Preview(name = "Settings persistence error – Dark", showBackground = true)
@Composable
private fun SettingsPersistenceErrorDarkPreview() {
    ItamaePreviewTheme(darkTheme = true) {
        SettingsScreen(
            uiState = SettingsUiState(
                themeMode = AppThemeMode.DARK,
                persistenceError = true,
            ),
            onThemeModeSelected = {},
            onLanguagePickerRequested = {},
            onLanguagePickerDismissed = {},
            onLanguageSelected = {},
            onAppLanguageRefreshRequested = {},
            onSoundEnabledChanged = {},
            onVibrationEnabledChanged = {},
            onClearHistoryRequested = {},
            onClearHistoryConfirmed = {},
            onClearHistoryDismissed = {},
            onClearAchievementsRequested = {},
            onClearAchievementsConfirmed = {},
            onClearAchievementsDismissed = {},
            onViewTutorialRequested = {},
            onViewAchievementsRequested = {},
            onPersistenceRetry = {},
            appVersion = "1.0",
        )
    }
}
