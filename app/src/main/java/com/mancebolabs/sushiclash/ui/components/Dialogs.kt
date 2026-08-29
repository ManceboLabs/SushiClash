package com.mancebolabs.sushiclash.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import com.mancebolabs.sushiclash.R
import com.mancebolabs.sushiclash.ui.theme.ItamaePreviewTheme
import com.mancebolabs.sushiclash.ui.theme.ItamaeShapes
import com.mancebolabs.sushiclash.ui.theme.ItamaeSpacing

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
            }
        },
        confirmButton = {
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
        },
        dismissButton = {
            Row {
                TextButton(
                    onClick = onDismiss,
                    enabled = !isSaving,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.counter_cancel),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                TextButton(
                    onClick = onSkipSave,
                    enabled = !isSaving,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.finish_game_skip_save),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        },
        properties = DialogProperties(),
    )
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

@Composable
fun WinnerDialog(
    winnerName: String,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = ItamaeShapes.large,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        title = {
            Text(
                text = stringResource(R.string.wheel_selected_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Text(
                text = winnerName,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.wheel_ok),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
    )
}

@Preview(name = "Finish game dialog", showBackground = true)
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

@Preview(name = "Winner dialog", showBackground = true)
@Composable
private fun WinnerDialogPreview() {
    ItamaePreviewTheme {
        WinnerDialog(
            winnerName = "Marta",
            onDismiss = {},
        )
    }
}
