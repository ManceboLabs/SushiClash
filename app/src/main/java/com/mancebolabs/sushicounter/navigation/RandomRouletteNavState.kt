package com.mancebolabs.sushicounter.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class RandomRouletteNavState {
    var pendingAutoSpin by mutableStateOf(false)

    fun requestAutoSpin() {
        pendingAutoSpin = true
    }

    fun consumeAutoSpin() {
        pendingAutoSpin = false
    }
}
