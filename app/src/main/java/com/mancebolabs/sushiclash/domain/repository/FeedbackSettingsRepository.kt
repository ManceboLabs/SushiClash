package com.mancebolabs.sushiclash.domain.repository

import kotlinx.coroutines.flow.Flow

interface FeedbackSettingsRepository {
    val soundEnabled: Flow<Boolean>

    val vibrationEnabled: Flow<Boolean>

    suspend fun setSoundEnabled(enabled: Boolean)

    suspend fun setVibrationEnabled(enabled: Boolean)
}
