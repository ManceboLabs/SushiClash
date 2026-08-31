package com.mancebolabs.sushiclash.ui.components.character

import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun AnimatedCharacterGif(
    @RawRes rawResId: Int,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    AndroidView(
        modifier = modifier.semantics {
            if (contentDescription != null) {
                this.contentDescription = contentDescription
            }
        },
        factory = { context ->
            GifMovieView(context)
        },
        update = { view ->
            view.contentDescription = contentDescription
            view.setGifResource(rawResId)
        },
        onRelease = { view ->
            view.stop()
        },
    )
}
