package com.mancebolabs.sushiclash.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mancebolabs.sushiclash.R
import com.mancebolabs.sushiclash.ui.components.character.AnimatedCharacterGif
import com.mancebolabs.sushiclash.ui.components.character.SushiClashCharacterAnimations
import com.mancebolabs.sushiclash.ui.theme.ItamaePreviewTheme
import com.mancebolabs.sushiclash.ui.theme.ItamaeSpacing

private val CelebrationChefSize = 220.dp
private val SpeechBubbleMaxWidth = 300.dp
private val SpeechBubbleTailHeight = 14.dp
private val SpeechBubbleTailWidth = 28.dp
private val SpeechBubbleBorderWidth = 3.dp

@Composable
fun RouletteWinnerCelebrationOverlay(
    winnerName: String,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.58f)),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ItamaeSpacing.marginMobile),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ComicWinnerSpeechBubble(
                    message = stringResource(R.string.wheel_winner_speech, winnerName),
                )

                Spacer(modifier = Modifier.height(ItamaeSpacing.sm))

                AnimatedCharacterGif(
                    rawResId = SushiClashCharacterAnimations.Celebration,
                    modifier = Modifier.size(CelebrationChefSize),
                    contentDescription = stringResource(
                        R.string.wheel_winner_celebration_content_description,
                        winnerName,
                    ),
                )

                Spacer(modifier = Modifier.height(ItamaeSpacing.xl))

                ItamaePrimaryButton(
                    text = stringResource(R.string.wheel_ok),
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(0.7f),
                )
            }
        }
    }
}

@Composable
private fun ComicWinnerSpeechBubble(
    message: String,
    modifier: Modifier = Modifier,
) {
    val bubbleColor = MaterialTheme.colorScheme.surfaceContainerLowest
    val borderColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = SpeechBubbleMaxWidth)
                .shadow(6.dp, RoundedCornerShape(22.dp)),
            shape = RoundedCornerShape(22.dp),
            color = bubbleColor,
            border = androidx.compose.foundation.BorderStroke(
                width = SpeechBubbleBorderWidth,
                color = borderColor,
            ),
        ) {
            Text(
                text = message,
                modifier = Modifier.padding(
                    horizontal = ItamaeSpacing.lg,
                    vertical = ItamaeSpacing.md,
                ),
                style = MaterialTheme.typography.titleLarge,
                color = textColor,
                textAlign = TextAlign.Center,
            )
        }

        Canvas(
            modifier = Modifier.size(
                width = SpeechBubbleTailWidth,
                height = SpeechBubbleTailHeight,
            ),
        ) {
            val tailPath = Path().apply {
                moveTo(size.width / 2f, size.height)
                lineTo(0f, 0f)
                lineTo(size.width, 0f)
                close()
            }
            drawPath(tailPath, color = bubbleColor)
            drawPath(
                path = tailPath,
                color = borderColor,
                style = Stroke(width = SpeechBubbleBorderWidth.toPx()),
            )
        }
    }
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
