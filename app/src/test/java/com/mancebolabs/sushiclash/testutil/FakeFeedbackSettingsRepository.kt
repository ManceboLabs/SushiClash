package com.mancebolabs.sushiclash.testutil

import com.mancebolabs.sushiclash.domain.repository.FeedbackSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeFeedbackSettingsRepository(
    soundEnabled: Boolean = true,
    vibrationEnabled: Boolean = true,
    randomChefAnimationsEnabled: Boolean = true,
) : FeedbackSettingsRepository {
    private val _soundEnabled = MutableStateFlow(soundEnabled)
    private val _vibrationEnabled = MutableStateFlow(vibrationEnabled)
    private val _randomChefAnimationsEnabled = MutableStateFlow(randomChefAnimationsEnabled)

    override val soundEnabled: Flow<Boolean> = _soundEnabled.asStateFlow()
    override val vibrationEnabled: Flow<Boolean> = _vibrationEnabled.asStateFlow()
    override val randomChefAnimationsEnabled: Flow<Boolean> = _randomChefAnimationsEnabled.asStateFlow()

    var setSoundEnabledThrow: Throwable? = null
    var setVibrationEnabledThrow: Throwable? = null
    var setRandomChefAnimationsEnabledThrow: Throwable? = null

    override suspend fun setSoundEnabled(enabled: Boolean) {
        setSoundEnabledThrow?.let { throw it }
        _soundEnabled.value = enabled
    }

    override suspend fun setVibrationEnabled(enabled: Boolean) {
        setVibrationEnabledThrow?.let { throw it }
        _vibrationEnabled.value = enabled
    }

    override suspend fun setRandomChefAnimationsEnabled(enabled: Boolean) {
        setRandomChefAnimationsEnabledThrow?.let { throw it }
        _randomChefAnimationsEnabled.value = enabled
    }
}
