package com.mancebolabs.sushiclash.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.mancebolabs.sushiclash.R
import com.mancebolabs.sushiclash.ui.theme.ItamaePreviewTheme
import com.mancebolabs.sushiclash.ui.theme.ItamaeShapes
import com.mancebolabs.sushiclash.ui.theme.ItamaeSpacing

private val FinishGameDialogStackedActionsMaxWidth = 320.dp

@Composable
fun FinishGameDialog(
    onDismiss: () -> Unit,
    onSkipSave: () -> Unit,
    onSave: () -> Unit,
    isSaving: Boolean,
    hasSaveError: Boolean,
) {
    AlertDialog(
        onDismissRequest = {
            if (!isSaving) onDismiss()
        },
        shape = ItamaeShapes.large,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        title = {
            Text(
                text = stringResource(R.string.finish_game_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.finish_game_message),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (hasSaveError) {
                    Spacer(modifier = Modifier.height(ItamaeSpacing.sm))
                    Text(
                        text = stringResource(R.string.finish_game_error),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                Spacer(modifier = Modifier.height(ItamaeSpacing.md))
                FinishGameDialogActions(
                    onDismiss = onDismiss,
                    onSkipSave = onSkipSave,
                    onSave = onSave,
                    isSaving = isSaving,
                )
            }
        },
        confirmButton = {},
        properties = DialogProperties(),
    )
}

@Composable
private fun FinishGameDialogActions(
    onDismiss: () -> Unit,
    onSkipSave: () -> Unit,
    onSave: () -> Unit,
    isSaving: Boolean,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val stackActions = maxWidth < FinishGameDialogStackedActionsMaxWidth

        if (stackActions) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(ItamaeSpacing.xs),
                horizontalAlignment = Alignment.End,
            ) {
                FinishGameSaveAction(onSave = onSave, isSaving = isSaving)
                FinishGameSecondaryAction(
                    label = stringResource(R.string.finish_game_skip_save),
                    onClick = onSkipSave,
                    isSaving = isSaving,
                )
                FinishGameSecondaryAction(
                    label = stringResource(R.string.counter_cancel),
                    onClick = onDismiss,
                    isSaving = isSaving,
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row {
                    FinishGameSecondaryAction(
                        label = stringResource(R.string.counter_cancel),
                        onClick = onDismiss,
                        isSaving = isSaving,
                    )
                    FinishGameSecondaryAction(
                        label = stringResource(R.string.finish_game_skip_save),
                        onClick = onSkipSave,
                        isSaving = isSaving,
                    )
                }
                FinishGameSaveAction(onSave = onSave, isSaving = isSaving)
            }
        }
    }
}

@Composable
private fun FinishGameSaveAction(
    onSave: () -> Unit,
    isSaving: Boolean,
) {
    TextButton(
        onClick = onSave,
        enabled = !isSaving,
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
        ),
    ) {
        Text(
            text = stringResource(R.string.finish_game_save),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun FinishGameSecondaryAction(
    label: String,
    onClick: () -> Unit,
    isSaving: Boolean,
) {
    TextButton(
        onClick = onClick,
        enabled = !isSaving,
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
fun ConfirmationDialog(
    title: String,
    confirmLabel: String,
    dismissLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    message: String? = null,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = ItamaeShapes.large,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = message?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = dismissLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        properties = DialogProperties(),
    )
}

@Preview(name = "Finish game dialog", showBackground = true, widthDp = 360)
@Composable
private fun FinishGameDialogPreview() {
    ItamaePreviewTheme {
        FinishGameDialog(
            onDismiss = {},
            onSkipSave = {},
            onSave = {},
            isSaving = false,
            hasSaveError = false,
        )
    }
}

@Preview(name = "Finish game dialog narrow", showBackground = true, widthDp = 280)
@Composable
private fun FinishGameDialogNarrowPreview() {
    ItamaePreviewTheme {
        FinishGameDialog(
            onDismiss = {},
            onSkipSave = {},
            onSave = {},
            isSaving = false,
            hasSaveError = false,
        )
    }
}

@Preview(name = "Finish game dialog error", showBackground = true)
@Composable
private fun FinishGameDialogErrorPreview() {
    ItamaePreviewTheme {
        FinishGameDialog(
            onDismiss = {},
            onSkipSave = {},
            onSave = {},
            isSaving = false,
            hasSaveError = true,
        )
    }
}

@Preview(name = "Finish game dialog loading", showBackground = true)
@Composable
private fun FinishGameDialogLoadingPreview() {
    ItamaePreviewTheme {
        FinishGameDialog(
            onDismiss = {},
            onSkipSave = {},
            onSave = {},
            isSaving = true,
            hasSaveError = false,
        )
    }
}

@Preview(name = "Confirmation dialog", showBackground = true)
@Composable
private fun ConfirmationDialogPreview() {
    ItamaePreviewTheme {
        ConfirmationDialog(
            title = stringResource(R.string.roulette_trigger_title),
            message = stringResource(R.string.roulette_trigger_solo_message, 5),
            confirmLabel = stringResource(R.string.wheel_ok),
            dismissLabel = stringResource(R.string.counter_cancel),
            onConfirm = {},
            onDismiss = {},
        )
    }
}
