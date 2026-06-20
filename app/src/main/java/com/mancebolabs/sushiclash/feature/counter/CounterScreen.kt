package com.mancebolabs.sushiclash.feature.counter

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
import androidx.compose.ui.tooling.preview.Preview
import com.mancebolabs.sushiclash.R
import com.mancebolabs.sushiclash.data.datastore.AppPreferencesDataStore
import com.mancebolabs.sushiclash.domain.model.GameMode
import com.mancebolabs.sushiclash.domain.model.GameSetupConfig
import com.mancebolabs.sushiclash.domain.model.GameState
import com.mancebolabs.sushiclash.domain.model.Player
import com.mancebolabs.sushiclash.ui.components.ConfirmationDialog
import com.mancebolabs.sushiclash.ui.components.FinishGameDialog
import com.mancebolabs.sushiclash.ui.components.ItamaeCard
import com.mancebolabs.sushiclash.ui.components.ItamaeGhostButton
import com.mancebolabs.sushiclash.ui.components.ItamaePrimaryButton
import com.mancebolabs.sushiclash.ui.components.ItamaeScreenTitle
import com.mancebolabs.sushiclash.ui.components.SushiClickerButton
import com.mancebolabs.sushiclash.ui.theme.ItamaePreviewTheme
import com.mancebolabs.sushiclash.ui.theme.ItamaeSpacing
import com.mancebolabs.sushiclash.ui.theme.itamaeScreenBottomInsets
import com.mancebolabs.sushiclash.ui.theme.itamaeScreenTopInsets

@Composable
fun CounterScreen(
    uiState: CounterUiState,
    onStartGameRequested: () -> Unit,
    onSoloSushiTapped: () -> Unit,
    onPlayerSushiTapped: (String) -> Unit,
    onPlayerResetRequested: (String) -> Unit,
    onPlayerResetConfirmed: () -> Unit,
    onPlayerResetDismissed: () -> Unit,
    onResetSoloCountConfirmed: () -> Unit,
    onFinishGameRequested: () -> Unit,
    onFinishGameCancelled: () -> Unit,
    onFinishGameWithoutSaving: () -> Unit,
    onFinishGameWithSaving: () -> Unit,
    onSetupConfirmed: (GameSetupConfig) -> Unit,
    onRouletteTriggerAccepted: () -> Unit,
    onRouletteTriggerDismissed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FinishGameDialogs(
        showFinishGameDialog = uiState.showFinishGameDialog,
        onFinishGameCancelled = onFinishGameCancelled,
        onFinishGameWithoutSaving = onFinishGameWithoutSaving,
        onFinishGameWithSaving = onFinishGameWithSaving,
    )

    if (uiState.showSetupDialog) {
        GameSetupDialog(onConfirm = onSetupConfirmed)
    }

    when (uiState.startupState) {
        AppStartupState.Loading -> {
            StartupLoadingScreen(modifier = modifier)
        }
        AppStartupState.NoActiveGame -> {
            // Setup is intentionally user-driven; first launch shows "Empezar partida" instead.
            NoActiveGameContent(
                onStartGameRequested = onStartGameRequested,
                modifier = modifier,
            )
        }
        AppStartupState.ActiveGame -> {
            ActiveGameContent(
                uiState = uiState,
                onSoloSushiTapped = onSoloSushiTapped,
                onPlayerSushiTapped = onPlayerSushiTapped,
                onPlayerResetRequested = onPlayerResetRequested,
                onPlayerResetConfirmed = onPlayerResetConfirmed,
                onPlayerResetDismissed = onPlayerResetDismissed,
                onResetSoloCountConfirmed = onResetSoloCountConfirmed,
                onFinishGameRequested = onFinishGameRequested,
                onRouletteTriggerAccepted = onRouletteTriggerAccepted,
                onRouletteTriggerDismissed = onRouletteTriggerDismissed,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun FinishGameDialogs(
    showFinishGameDialog: Boolean,
    onFinishGameCancelled: () -> Unit,
    onFinishGameWithoutSaving: () -> Unit,
    onFinishGameWithSaving: () -> Unit,
) {
    if (showFinishGameDialog) {
        FinishGameDialog(
            onDismiss = onFinishGameCancelled,
            onSkipSave = onFinishGameWithoutSaving,
            onSave = onFinishGameWithSaving,
        )
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
private fun NoActiveGameContent(
    onStartGameRequested: () -> Unit,
    modifier: Modifier = Modifier,
) {
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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            ItamaeCard {
                Text(
                    text = stringResource(R.string.counter_no_active_game_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(ItamaeSpacing.sm))

                Text(
                    text = stringResource(R.string.counter_no_active_game_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        ItamaePrimaryButton(
            text = stringResource(R.string.counter_start_game),
            onClick = onStartGameRequested,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ActiveGameContent(
    uiState: CounterUiState,
    onSoloSushiTapped: () -> Unit,
    onPlayerSushiTapped: (String) -> Unit,
    onPlayerResetRequested: (String) -> Unit,
    onPlayerResetConfirmed: () -> Unit,
    onPlayerResetDismissed: () -> Unit,
    onResetSoloCountConfirmed: () -> Unit,
    onFinishGameRequested: () -> Unit,
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
            text = stringResource(R.string.counter_finish_game),
            onClick = onFinishGameRequested,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// region Previews

@Composable
private fun PreviewCounterScreen(uiState: CounterUiState) {
    CounterScreen(
        uiState = uiState,
        onStartGameRequested = {},
        onSoloSushiTapped = {},
        onPlayerSushiTapped = {},
        onPlayerResetRequested = {},
        onPlayerResetConfirmed = {},
        onPlayerResetDismissed = {},
        onResetSoloCountConfirmed = {},
        onFinishGameRequested = {},
        onFinishGameCancelled = {},
        onFinishGameWithoutSaving = {},
        onFinishGameWithSaving = {},
        onSetupConfirmed = {},
        onRouletteTriggerAccepted = {},
        onRouletteTriggerDismissed = {},
    )
}

private fun previewSoloGameState(count: Int): GameState {
    return GameState(
        hasActiveGame = true,
        gameMode = GameMode.SOLO,
        players = listOf(
            Player(
                id = AppPreferencesDataStore.SOLO_PLAYER_ID,
                name = "",
                sushiCount = count,
            ),
        ),
    )
}

private fun previewGroupGameState(playerCount: Int): GameState {
    val names = listOf("Ana", "Luis", "Marta", "Javier", "Bea", "Carlos")
    return GameState(
        hasActiveGame = true,
        gameMode = GameMode.GROUP,
        players = names.take(playerCount).mapIndexed { index, name ->
            Player(
                id = "player-$index",
                name = name,
                sushiCount = (index + 1) * 3,
            )
        },
    )
}

@Preview(name = "No active game – Light", showBackground = true)
@Composable
private fun NoActiveGameLightPreview() {
    ItamaePreviewTheme(darkTheme = false) {
        PreviewCounterScreen(
            uiState = CounterUiState(startupState = AppStartupState.NoActiveGame),
        )
    }
}

@Preview(name = "No active game – Dark", showBackground = true)
@Composable
private fun NoActiveGameDarkPreview() {
    ItamaePreviewTheme(darkTheme = true) {
        PreviewCounterScreen(
            uiState = CounterUiState(startupState = AppStartupState.NoActiveGame),
        )
    }
}

@Preview(name = "Solo counter – Light", showBackground = true)
@Composable
private fun SoloCounterLightPreview() {
    ItamaePreviewTheme(darkTheme = false) {
        PreviewCounterScreen(
            uiState = CounterUiState(
                startupState = AppStartupState.ActiveGame,
                gameState = previewSoloGameState(count = 18),
            ),
        )
    }
}

@Preview(name = "Solo counter – Dark", showBackground = true)
@Composable
private fun SoloCounterDarkPreview() {
    ItamaePreviewTheme(darkTheme = true) {
        PreviewCounterScreen(
            uiState = CounterUiState(
                startupState = AppStartupState.ActiveGame,
                gameState = previewSoloGameState(count = 18),
            ),
        )
    }
}

@Preview(name = "Group counter – Light", showBackground = true, heightDp = 780)
@Composable
private fun GroupCounterLightPreview() {
    ItamaePreviewTheme(darkTheme = false) {
        PreviewCounterScreen(
            uiState = CounterUiState(
                startupState = AppStartupState.ActiveGame,
                gameState = previewGroupGameState(playerCount = 4),
            ),
        )
    }
}

@Preview(name = "Group counter – Dark", showBackground = true, heightDp = 780)
@Composable
private fun GroupCounterDarkPreview() {
    ItamaePreviewTheme(darkTheme = true) {
        PreviewCounterScreen(
            uiState = CounterUiState(
                startupState = AppStartupState.ActiveGame,
                gameState = previewGroupGameState(playerCount = 4),
            ),
        )
    }
}

@Preview(name = "Setup popup", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun SetupPopupPreview() {
    ItamaePreviewTheme {
        PreviewCounterScreen(
            uiState = CounterUiState(
                startupState = AppStartupState.NoActiveGame,
                showSetupDialog = true,
            ),
        )
    }
}

@Preview(name = "Finish game dialog", showBackground = true)
@Composable
private fun FinishGameDialogPreview() {
    ItamaePreviewTheme {
        PreviewCounterScreen(
            uiState = CounterUiState(
                startupState = AppStartupState.NoActiveGame,
                showFinishGameDialog = true,
            ),
        )
    }
}

// endregion
