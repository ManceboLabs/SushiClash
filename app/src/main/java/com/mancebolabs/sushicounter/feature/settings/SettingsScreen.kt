package com.mancebolabs.sushicounter.feature.settings

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.mancebolabs.sushicounter.R
import com.mancebolabs.sushicounter.domain.model.AppThemeMode
import com.mancebolabs.sushicounter.ui.components.ConfirmationDialog
import com.mancebolabs.sushicounter.ui.components.ItamaeCard
import com.mancebolabs.sushicounter.ui.components.ItamaeGhostButton
import com.mancebolabs.sushicounter.ui.components.ItamaeScreenTitle
import androidx.compose.ui.unit.dp
import com.mancebolabs.sushicounter.ui.theme.ItamaeShapes
import com.mancebolabs.sushicounter.ui.theme.ItamaeSpacing
import com.mancebolabs.sushicounter.ui.theme.itamaeScreenTopInsets
import com.mancebolabs.sushicounter.ui.theme.rememberItamaeBottomContentPadding

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onThemeModeSelected: (AppThemeMode) -> Unit,
    onClearHistoryRequested: () -> Unit,
    onClearHistoryConfirmed: () -> Unit,
    onClearHistoryDismissed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val bottomContentPadding = rememberItamaeBottomContentPadding(scrollable = true)

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .itamaeScreenTopInsets()
            .verticalScroll(scrollState)
            .padding(horizontal = ItamaeSpacing.marginMobile)
            .padding(bottom = bottomContentPadding),
        verticalArrangement = Arrangement.spacedBy(ItamaeSpacing.md),
    ) {
        ItamaeScreenTitle(title = stringResource(R.string.settings_screen_title))

        ItamaeCard {
            Text(
                text = stringResource(R.string.settings_appearance_section),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(ItamaeSpacing.md))

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

        ItamaeCard {
            Text(
                text = stringResource(R.string.settings_history_section),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(ItamaeSpacing.md))

            ItamaeGhostButton(
                text = stringResource(R.string.settings_clear_history),
                onClick = onClearHistoryRequested,
                modifier = Modifier.fillMaxWidth(),
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
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
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
