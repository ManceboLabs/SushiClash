package com.mancebolabs.sushiclash.feature.wheel

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mancebolabs.sushiclash.R
import com.mancebolabs.sushiclash.ui.components.ConfirmationDialog
import com.mancebolabs.sushiclash.ui.components.ItamaeCard
import com.mancebolabs.sushiclash.ui.components.ItamaePrimaryButton
import com.mancebolabs.sushiclash.ui.components.ItamaeScreenTitle
import com.mancebolabs.sushiclash.ui.components.ItamaeTextField
import com.mancebolabs.sushiclash.ui.components.PersistenceErrorMessage
import com.mancebolabs.sushiclash.ui.components.RouletteWheel
import com.mancebolabs.sushiclash.ui.components.RouletteWinnerCelebrationOverlay
import com.mancebolabs.sushiclash.ui.theme.ItamaePreviewTheme
import com.mancebolabs.sushiclash.ui.theme.ItamaeShapes
import com.mancebolabs.sushiclash.ui.theme.ItamaeSpacing
import com.mancebolabs.sushiclash.ui.theme.itamaeScreenTopInsets
import com.mancebolabs.sushiclash.ui.theme.rememberItamaeBottomContentPadding

private val WheelMinHeight = 280.dp

@Composable
fun WheelScreen(
    uiState: WheelUiState,
    onInputChanged: (String) -> Unit,
    onAddParticipant: () -> Unit,
    onRemoveParticipant: (String) -> Unit,
    onSpin: () -> Unit,
    onWinnerDialogDismissed: () -> Unit,
    onInsufficientParticipantsDismissed: () -> Unit,
    onPersistenceRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    uiState.selectedWinner?.let { winner ->
        RouletteWinnerCelebrationOverlay(
            winnerName = winner,
            onDismiss = onWinnerDialogDismissed,
        )
    }

    if (uiState.showInsufficientParticipantsWarning) {
        ConfirmationDialog(
            title = stringResource(R.string.roulette_insufficient_participants_title),
            message = stringResource(R.string.roulette_insufficient_participants_message),
            confirmLabel = stringResource(R.string.wheel_ok),
            dismissLabel = stringResource(R.string.counter_cancel),
            onConfirm = onInsufficientParticipantsDismissed,
            onDismiss = onInsufficientParticipantsDismissed,
        )
    }

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
        verticalArrangement = Arrangement.spacedBy(ItamaeSpacing.md),
    ) {
        ItamaeScreenTitle(title = stringResource(R.string.wheel_screen_title))

        if (uiState.persistenceError) {
            PersistenceErrorMessage(
                isRetrying = uiState.isPersistenceRetrying,
                onRetry = onPersistenceRetry,
            )
        }

        ItamaeCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ItamaeSpacing.sm),
                verticalAlignment = Alignment.Bottom,
            ) {
                ItamaeTextField(
                    value = uiState.inputName,
                    onValueChange = onInputChanged,
                    label = stringResource(R.string.wheel_name_hint),
                    modifier = Modifier.weight(1f),
                )
                ItamaePrimaryButton(
                    text = stringResource(R.string.wheel_add),
                    onClick = onAddParticipant,
                    enabled = uiState.inputName.isNotBlank(),
                )
            }
        }

        ItamaeCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(WheelMinHeight),
        ) {
            if (uiState.participants.size < 2) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = stringResource(R.string.wheel_empty_wheel_hint),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                RouletteWheel(
                    participants = uiState.participants,
                    targetRotation = uiState.wheelRotation,
                    isSpinning = uiState.isSpinning,
                    modifier = Modifier.fillMaxSize(),
                    onClick = onSpin
                )
            }
        }

        ItamaeCard {
            Text(
                text = stringResource(R.string.wheel_participants_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(ItamaeSpacing.sm))

            if (uiState.participants.isEmpty()) {
                Text(
                    text = stringResource(R.string.wheel_min_participants_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(ItamaeSpacing.sm),
                ) {
                    uiState.participants.forEach { participant ->
                        ParticipantRow(
                            name = participant,
                            onDelete = { onRemoveParticipant(participant) },
                        )
                    }
                }
            }
        }

        if (uiState.participants.size < 2) {
            Text(
                text = stringResource(R.string.wheel_min_participants_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        ItamaePrimaryButton(
            text = stringResource(R.string.wheel_spin),
            onClick = onSpin,
            enabled = uiState.canSpin,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ParticipantRow(
    name: String,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = ItamaeShapes.small,
            )
            .padding(horizontal = ItamaeSpacing.md, vertical = ItamaeSpacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = stringResource(
                    R.string.wheel_delete_content_description,
                    name,
                ),
                modifier = Modifier.size(ItamaeSpacing.md),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private val previewWheelParticipants = listOf("Ana", "Luis", "Marta", "Javier")

@Preview(name = "Wheel empty – Light", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun WheelEmptyLightPreview() {
    ItamaePreviewTheme(darkTheme = false) {
        WheelScreen(
            uiState = WheelUiState(),
            onInputChanged = {},
            onAddParticipant = {},
            onRemoveParticipant = {},
            onSpin = {},
            onWinnerDialogDismissed = {},
            onInsufficientParticipantsDismissed = {},
            onPersistenceRetry = {},
        )
    }
}

@Preview(name = "Wheel with participants – Light", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun WheelWithParticipantsLightPreview() {
    ItamaePreviewTheme(darkTheme = false) {
        WheelScreen(
            uiState = WheelUiState(
                participants = previewWheelParticipants,
                inputName = "Bea",
            ),
            onInputChanged = {},
            onAddParticipant = {},
            onRemoveParticipant = {},
            onSpin = {},
            onWinnerDialogDismissed = {},
            onInsufficientParticipantsDismissed = {},
            onPersistenceRetry = {},
        )
    }
}

@Preview(name = "Wheel with participants – Dark", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun WheelWithParticipantsDarkPreview() {
    ItamaePreviewTheme(darkTheme = true) {
        WheelScreen(
            uiState = WheelUiState(
                participants = previewWheelParticipants,
            ),
            onInputChanged = {},
            onAddParticipant = {},
            onRemoveParticipant = {},
            onSpin = {},
            onWinnerDialogDismissed = {},
            onInsufficientParticipantsDismissed = {},
            onPersistenceRetry = {},
        )
    }
}

@Preview(name = "Wheel winner celebration", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun WheelWinnerDialogPreview() {
    ItamaePreviewTheme {
        WheelScreen(
            uiState = WheelUiState(
                participants = previewWheelParticipants,
                selectedWinner = "Marta",
            ),
            onInputChanged = {},
            onAddParticipant = {},
            onRemoveParticipant = {},
            onSpin = {},
            onWinnerDialogDismissed = {},
            onInsufficientParticipantsDismissed = {},
            onPersistenceRetry = {},
        )
    }
}

@Preview(name = "Wheel persistence error – Light", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun WheelPersistenceErrorLightPreview() {
    ItamaePreviewTheme(darkTheme = false) {
        WheelScreen(
            uiState = WheelUiState(persistenceError = true),
            onInputChanged = {},
            onAddParticipant = {},
            onRemoveParticipant = {},
            onSpin = {},
            onWinnerDialogDismissed = {},
            onInsufficientParticipantsDismissed = {},
            onPersistenceRetry = {},
        )
    }
}

@Preview(name = "Wheel persistence error – Dark", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun WheelPersistenceErrorDarkPreview() {
    ItamaePreviewTheme(darkTheme = true) {
        WheelScreen(
            uiState = WheelUiState(persistenceError = true),
            onInputChanged = {},
            onAddParticipant = {},
            onRemoveParticipant = {},
            onSpin = {},
            onWinnerDialogDismissed = {},
            onInsufficientParticipantsDismissed = {},
            onPersistenceRetry = {},
        )
    }
}
