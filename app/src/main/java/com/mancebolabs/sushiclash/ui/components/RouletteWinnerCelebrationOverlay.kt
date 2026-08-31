package com.mancebolabs.sushiclash.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.mancebolabs.sushiclash.R
import com.mancebolabs.sushiclash.ui.components.character.SushiClashCharacterAnimations
import com.mancebolabs.sushiclash.ui.theme.ItamaePreviewTheme

@Composable
fun RouletteWinnerCelebrationOverlay(
    winnerName: String,
    onDismiss: () -> Unit,
) {
    ChefCharacterCelebrationOverlay(
        speechMessage = stringResource(R.string.wheel_winner_speech, winnerName),
        rawResId = SushiClashCharacterAnimations.Celebration,
        contentDescription = stringResource(
            R.string.wheel_winner_celebration_content_description,
            winnerName,
        ),
        onDismiss = onDismiss,
    )
}

@Preview(name = "Roulette winner celebration – Light", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun RouletteWinnerCelebrationLightPreview() {
    ItamaePreviewTheme(darkTheme = false) {
        RouletteWinnerCelebrationOverlay(
            winnerName = "Carlos",
            onDismiss = {},
        )
    }
}

@Preview(name = "Roulette winner celebration – Dark", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun RouletteWinnerCelebrationDarkPreview() {
    ItamaePreviewTheme(darkTheme = true) {
        RouletteWinnerCelebrationOverlay(
            winnerName = "Marta",
            onDismiss = {},
        )
    }
}

@Preview(name = "Roulette winner celebration – Long name", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun RouletteWinnerCelebrationLongNamePreview() {
    ItamaePreviewTheme {
        RouletteWinnerCelebrationOverlay(
            winnerName = "Alejandro Fernando",
            onDismiss = {},
        )
    }
}
