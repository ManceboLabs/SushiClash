package com.mancebolabs.sushiclash.feature.feedback

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper
import android.view.HapticFeedbackConstants
import android.view.View

class AndroidGameFeedbackController(
    private val view: View,
) : GameFeedbackController {

    override fun playSushiIncrement(
        soundEnabled: Boolean,
        vibrationEnabled: Boolean,
    ) {
        if (vibrationEnabled) {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        }
        if (soundEnabled) {
            playTone(
                toneType = ToneGenerator.TONE_PROP_BEEP,
                durationMs = SUSHI_TONE_DURATION_MS,
                volume = SUSHI_TONE_VOLUME,
            )
        }
    }

    override fun playRouletteTriggered(
        soundEnabled: Boolean,
        vibrationEnabled: Boolean,
    ) {
        if (vibrationEnabled) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
        if (soundEnabled) {
            playTone(
                toneType = ToneGenerator.TONE_PROP_ACK,
                durationMs = ROULETTE_TONE_DURATION_MS,
                volume = ROULETTE_TONE_VOLUME,
            )
        }
    }

    private fun playTone(
        toneType: Int,
        durationMs: Int,
        volume: Int,
    ) {
        runCatching {
            val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, volume)
            val started = toneGenerator.startTone(toneType, durationMs)
            if (!started) {
                toneGenerator.release()
                return@runCatching
            }
            releaseToneAfter(toneGenerator, durationMs)
        }
    }

    private fun releaseToneAfter(toneGenerator: ToneGenerator, durationMs: Int) {
        val releaseDelayMs = durationMs.toLong() + TONE_RELEASE_BUFFER_MS
        val handler = view.handler ?: Handler(Looper.getMainLooper())
        handler.postDelayed({ toneGenerator.release() }, releaseDelayMs)
    }

    private companion object {
        const val SUSHI_TONE_VOLUME = 35
        const val ROULETTE_TONE_VOLUME = 55
        const val SUSHI_TONE_DURATION_MS = 45
        const val ROULETTE_TONE_DURATION_MS = 120
        const val TONE_RELEASE_BUFFER_MS = 25L
    }
}
