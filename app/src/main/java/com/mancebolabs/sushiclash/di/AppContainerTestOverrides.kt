package com.mancebolabs.sushiclash.di

import com.mancebolabs.sushiclash.domain.model.DefaultRandomProvider
import com.mancebolabs.sushiclash.domain.model.RandomProvider
import com.mancebolabs.sushiclash.feature.wheel.WheelViewModel

/**
 * Optional overrides used by instrumented tests to inject deterministic behavior.
 * Production code reads these when building repositories and ViewModels; defaults preserve normal behavior.
 */
object AppContainerTestOverrides {
    var chefRandomProvider: RandomProvider? = null
    var rouletteRandomProvider: RandomProvider? = null
    var wheelRandomProvider: RandomProvider? = null
    var completeGifCyclesImmediately: Boolean = false
    var wheelSpinDurationMs: Long = WheelViewModel.SPIN_DURATION_MS

    fun chefRandomProviderOrDefault(): RandomProvider = chefRandomProvider ?: DefaultRandomProvider()

    fun rouletteRandomProviderOrDefault(): RandomProvider = rouletteRandomProvider ?: DefaultRandomProvider()

    fun wheelRandomProviderOrDefault(): RandomProvider = wheelRandomProvider ?: DefaultRandomProvider()

    fun reset() {
        chefRandomProvider = null
        rouletteRandomProvider = null
        wheelRandomProvider = null
        completeGifCyclesImmediately = false
        wheelSpinDurationMs = WheelViewModel.SPIN_DURATION_MS
    }
}
