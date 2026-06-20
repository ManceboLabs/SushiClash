package com.mancebolabs.sushiclash.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * One-shot navigation flag for random-roulette auto-spin.
 * Consumed after handling so the wheel does not spin again on recomposition.
 */
class RandomRouletteNavState {
    var pendingAutoSpin by mutableStateOf(false)

    fun requestAutoSpin() {
        pendingAutoSpin = true
    }

    fun consumeAutoSpin() {
        pendingAutoSpin = false
    }
}
