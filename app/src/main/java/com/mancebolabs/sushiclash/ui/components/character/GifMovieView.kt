package com.mancebolabs.sushiclash.ui.components.character

import android.content.Context
import android.graphics.Canvas
import android.graphics.Movie
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RawRes
import kotlin.math.min

/**
 * Lightweight GIF renderer backed by [Movie] so animated characters work from API 24
 * without extra image-loading dependencies.
 */
@Suppress("DEPRECATION")
internal class GifMovieView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private var movie: Movie? = null
    private var movieStartMillis: Long = 0L
    private var isPlaying: Boolean = false
    private var hasReportedCycleComplete: Boolean = false
    var onSingleCycleComplete: (() -> Unit)? = null

    fun setGifResource(@RawRes rawResId: Int) {
        stop()
        context.resources.openRawResource(rawResId).use { input ->
            movie = Movie.decodeStream(input)
        }
        movieStartMillis = System.currentTimeMillis()
        hasReportedCycleComplete = false
        isPlaying = movie != null
        requestLayout()
        invalidate()
    }

    fun stop() {
        isPlaying = false
        movie = null
        movieStartMillis = 0L
        hasReportedCycleComplete = false
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        val currentMovie = movie
        if (!isPlaying || currentMovie == null) return

        val duration = currentMovie.duration().takeIf { it > 0 } ?: 1_000
        val elapsed = ((System.currentTimeMillis() - movieStartMillis) % duration).toInt()
        currentMovie.setTime(elapsed)

        if (!hasReportedCycleComplete) {
            val playedMillis = System.currentTimeMillis() - movieStartMillis
            if (playedMillis >= duration) {
                hasReportedCycleComplete = true
                onSingleCycleComplete?.invoke()
            }
        }

        val movieWidth = currentMovie.width().toFloat().coerceAtLeast(1f)
        val movieHeight = currentMovie.height().toFloat().coerceAtLeast(1f)
        val scale = min(width / movieWidth, height / movieHeight)

        canvas.save()
        canvas.scale(scale, scale)
        val offsetX = (width / scale - movieWidth) / 2f
        val offsetY = (height / scale - movieHeight) / 2f
        currentMovie.draw(canvas, offsetX, offsetY)
        canvas.restore()

        postInvalidateOnAnimation()
    }

    override fun onDetachedFromWindow() {
        stop()
        super.onDetachedFromWindow()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean = false
}
