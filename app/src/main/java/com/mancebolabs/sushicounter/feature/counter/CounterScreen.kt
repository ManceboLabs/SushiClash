package com.mancebolabs.sushicounter.feature.counter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.mancebolabs.sushicounter.R
import com.mancebolabs.sushicounter.domain.model.GameSetupConfig
import com.mancebolabs.sushicounter.domain.model.GameMode
import com.mancebolabs.sushicounter.ui.components.ConfirmationDialog
import com.mancebolabs.sushicounter.ui.components.ItamaeGhostButton
import com.mancebolabs.sushicounter.ui.components.ItamaeScreenTitle
import com.mancebolabs.sushicounter.ui.components.SushiClickerButton
import com.mancebolabs.sushicounter.ui.theme.ItamaeSpacing
import com.mancebolabs.sushicounter.ui.theme.itamaeScreenBottomInsets
import com.mancebolabs.sushicounter.ui.theme.itamaeScreenTopInsets

@Composable
fun CounterScreen(
    uiState: CounterUiState,
    onSoloSushiTapped: () -> Unit,
    onPlayerSushiTapped: (String) -> Unit,
    onPlayerResetRequested: (String) -> Unit,
    onPlayerResetConfirmed: () -> Unit,
    onPlayerResetDismissed: () -> Unit,
    onResetSoloCountConfirmed: () -> Unit,
    onRestartRequested: () -> Unit,
    onSetupConfirmed: (GameSetupConfig) -> Unit,
    onRouletteTriggerAccepted: () -> Unit,
    onRouletteTriggerDismissed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState.startupState) {
        AppStartupState.Loading -> {
            StartupLoadingScreen(modifier = modifier)
        }
        AppStartupState.SetupRequired -> {
            CounterMainContent(
                uiState = uiState,
                onSoloSushiTapped = onSoloSushiTapped,
                onPlayerSushiTapped = onPlayerSushiTapped,
                onPlayerResetRequested = onPlayerResetRequested,
                onPlayerResetConfirmed = onPlayerResetConfirmed,
                onPlayerResetDismissed = onPlayerResetDismissed,
                onResetSoloCountConfirmed = onResetSoloCountConfirmed,
                onRestartRequested = onRestartRequested,
                onRouletteTriggerAccepted = onRouletteTriggerAccepted,
                onRouletteTriggerDismissed = onRouletteTriggerDismissed,
                modifier = modifier,
            )
            GameSetupDialog(onConfirm = onSetupConfirmed)
        }
        AppStartupState.Ready -> {
            CounterMainContent(
                uiState = uiState,
                onSoloSushiTapped = onSoloSushiTapped,
                onPlayerSushiTapped = onPlayerSushiTapped,
                onPlayerResetRequested = onPlayerResetRequested,
                onPlayerResetConfirmed = onPlayerResetConfirmed,
                onPlayerResetDismissed = onPlayerResetDismissed,
                onResetSoloCountConfirmed = onResetSoloCountConfirmed,
                onRestartRequested = onRestartRequested,
                onRouletteTriggerAccepted = onRouletteTriggerAccepted,
                onRouletteTriggerDismissed = onRouletteTriggerDismissed,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun StartupLoadingScreen(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    )
}

@Composable
private fun CounterMainContent(
    uiState: CounterUiState,
    onSoloSushiTapped: () -> Unit,
    onPlayerSushiTapped: (String) -> Unit,
    onPlayerResetRequested: (String) -> Unit,
    onPlayerResetConfirmed: () -> Unit,
    onPlayerResetDismissed: () -> Unit,
    onResetSoloCountConfirmed: () -> Unit,
    onRestartRequested: () -> Unit,
    onRouletteTriggerAccepted: () -> Unit,
    onRouletteTriggerDismissed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showResetDialog by remember { mutableStateOf(false) }

    uiState.rouletteTriggerEvent?.let { event ->
        val message = when (event) {
            is RouletteTriggerEvent.Solo -> {
                stringResource(R.string.roulette_trigger_solo_message, event.count)
            }
            is RouletteTriggerEvent.Group -> {
                stringResource(
                    R.string.roulette_trigger_group_message,
                    event.playerName,
                    event.count,
                )
            }
        }
        ConfirmationDialog(
            title = stringResource(R.string.roulette_trigger_title),
            message = message,
            confirmLabel = stringResource(R.string.wheel_ok),
            dismissLabel = stringResource(R.string.counter_cancel),
            onConfirm = onRouletteTriggerAccepted,
            onDismiss = onRouletteTriggerDismissed,
        )
    }

    if (showResetDialog) {
        ConfirmationDialog(
            title = stringResource(R.string.counter_reset_title),
            confirmLabel = stringResource(R.string.counter_reset_confirm),
            dismissLabel = stringResource(R.string.counter_cancel),
            onConfirm = {
                showResetDialog = false
                onResetSoloCountConfirmed()
            },
            onDismiss = { showResetDialog = false },
        )
    }

    uiState.playerResetRequest?.let { request ->
        ConfirmationDialog(
            title = stringResource(R.string.counter_reset_title),
            message = stringResource(R.string.counter_player_reset_message, request.playerName),
            confirmLabel = stringResource(R.string.counter_reset_confirm),
            dismissLabel = stringResource(R.string.counter_cancel),
            onConfirm = onPlayerResetConfirmed,
            onDismiss = onPlayerResetDismissed,
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .itamaeScreenTopInsets()
            .itamaeScreenBottomInsets(scrollable = false)
            .padding(horizontal = ItamaeSpacing.marginMobile),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ItamaeScreenTitle(title = stringResource(R.string.counter_screen_title))

        Spacer(modifier = Modifier.height(ItamaeSpacing.md))

        when (uiState.gameMode) {
            GameMode.GROUP -> {
                GroupCounterLayout(
                    players = uiState.players,
                    onPlayerTapped = onPlayerSushiTapped,
                    onPlayerResetRequested = onPlayerResetRequested,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
            }
            GameMode.SOLO, null -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    SushiClickerButton(onClick = onSoloSushiTapped)

                    Spacer(modifier = Modifier.height(ItamaeSpacing.lg))

                    Text(
                        text = uiState.soloCount.toString(),
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(ItamaeSpacing.sm))

                    Text(
                        text = stringResource(R.string.counter_sushi_eaten),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(ItamaeSpacing.sm))

        if (uiState.gameMode == GameMode.SOLO) {
            ItamaeGhostButton(
                text = stringResource(R.string.counter_reset),
                onClick = { showResetDialog = true },
                enabled = uiState.soloCount > 0,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(ItamaeSpacing.sm))
        }

        ItamaeGhostButton(
            text = stringResource(R.string.counter_restart),
            onClick = onRestartRequested,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
