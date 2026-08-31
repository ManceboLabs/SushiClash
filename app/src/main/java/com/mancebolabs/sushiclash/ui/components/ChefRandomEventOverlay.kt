package com.mancebolabs.sushiclash.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.mancebolabs.sushiclash.domain.model.ChefEventAnimation
import com.mancebolabs.sushiclash.testing.SushiClashTestTags
import com.mancebolabs.sushiclash.ui.components.character.AnimatedCharacterGif
import com.mancebolabs.sushiclash.ui.components.character.SushiClashCharacterAnimations
import com.mancebolabs.sushiclash.ui.theme.ItamaePreviewTheme

@Composable
fun ChefRandomEventOverlay(
    animation: ChefEventAnimation,
    onPlaybackComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag(SushiClashTestTags.CHEF_RANDOM_EVENT_OVERLAY)
            .consumeAllPointerInput()
            .background(Color.Black.copy(alpha = 0.42f)),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedCharacterGif(
            rawResId = SushiClashCharacterAnimations.rawResIdFor(animation),
            modifier = Modifier.size(CelebrationChefSize),
            contentDescription = stringResource(
                SushiClashCharacterAnimations.contentDescriptionResFor(animation),
            ),
            onSingleCycleComplete = onPlaybackComplete,
        )
    }
}

private fun Modifier.consumeAllPointerInput(): Modifier = pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent()
            event.changes.forEach { it.consume() }
        }
    }
}

@Preview(name = "Chef random event – Light", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun ChefRandomEventOverlayPreview() {
    ItamaePreviewTheme(darkTheme = false) {
        ChefRandomEventOverlay(
            animation = ChefEventAnimation.NINJA,
            onPlaybackComplete = {},
        )
    }
}
