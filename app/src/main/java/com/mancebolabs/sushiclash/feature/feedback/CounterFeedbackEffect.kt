package com.mancebolabs.sushiclash.feature.feedback

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalView
import com.mancebolabs.sushiclash.feature.counter.CounterUiState

@Composable
fun CounterFeedbackEffect(
    uiState: CounterUiState,
    onFeedbackConsumed: () -> Unit,
    feedbackController: GameFeedbackController = AndroidGameFeedbackController(LocalView.current),
) {
    LaunchedEffect(uiState.feedbackEvent) {
        when (val event = uiState.feedbackEvent) {
            CounterFeedbackEvent.SushiIncrement -> {
                feedbackController.playSushiIncrement(
                    soundEnabled = uiState.soundEnabled,
                    vibrationEnabled = uiState.vibrationEnabled,
                )
            }
            CounterFeedbackEvent.RouletteTriggered -> {
                feedbackController.playRouletteTriggered(
                    soundEnabled = uiState.soundEnabled,
                    vibrationEnabled = uiState.vibrationEnabled,
                )
            }
            null -> return@LaunchedEffect
        }
        onFeedbackConsumed()
    }
}
