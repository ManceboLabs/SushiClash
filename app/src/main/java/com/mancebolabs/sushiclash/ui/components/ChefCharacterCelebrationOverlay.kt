package com.mancebolabs.sushiclash.ui.components

import androidx.annotation.RawRes
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

internal val CelebrationChefSize = 220.dp
private val SpeechBubbleMaxWidth = 300.dp
private val SpeechBubbleTailHeight = 14.dp
private val SpeechBubbleTailWidth = 28.dp
private val SpeechBubbleBorderWidth = 3.dp

@Composable
fun ChefCharacterCelebrationOverlay(
    speechMessage: String,
    @RawRes rawResId: Int,
    contentDescription: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    okButtonText: String = stringResource(R.string.wheel_ok),
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
            modifier = modifier
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
                ComicSpeechBubble(message = speechMessage)

                Spacer(modifier = Modifier.height(ItamaeSpacing.sm))

                AnimatedCharacterGif(
                    rawResId = rawResId,
                    modifier = Modifier.size(CelebrationChefSize),
                    contentDescription = contentDescription,
                )

                Spacer(modifier = Modifier.height(ItamaeSpacing.xl))

                ItamaePrimaryButton(
                    text = okButtonText,
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(0.7f),
                )
            }
        }
    }
}

@Composable
internal fun ComicSpeechBubble(
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

@Preview(name = "Game start celebration – Light", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun GameStartCelebrationLightPreview() {
    ItamaePreviewTheme(darkTheme = false) {
        ChefCharacterCelebrationOverlay(
            speechMessage = "Let the feast begin!",
            rawResId = SushiClashCharacterAnimations.GameStart,
            contentDescription = "Chef welcoming a new game",
            onDismiss = {},
        )
    }
}

@Preview(name = "Game finish celebration – Dark", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun GameFinishCelebrationDarkPreview() {
    ItamaePreviewTheme(darkTheme = true) {
        ChefCharacterCelebrationOverlay(
            speechMessage = "I can't take any more...!",
            rawResId = SushiClashCharacterAnimations.GameFinish,
            contentDescription = "Chef celebrating the end of a game",
            onDismiss = {},
        )
    }
}
