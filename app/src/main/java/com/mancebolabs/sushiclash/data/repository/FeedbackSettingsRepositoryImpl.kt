package com.mancebolabs.sushiclash.data.repository

import com.mancebolabs.sushiclash.data.datastore.AppPreferencesDataStore
import com.mancebolabs.sushiclash.domain.repository.FeedbackSettingsRepository
import kotlinx.coroutines.flow.Flow

class FeedbackSettingsRepositoryImpl(
    private val dataStore: AppPreferencesDataStore,
) : FeedbackSettingsRepository {

    override val soundEnabled: Flow<Boolean> = dataStore.soundEnabled

    override val vibrationEnabled: Flow<Boolean> = dataStore.vibrationEnabled

    override suspend fun setSoundEnabled(enabled: Boolean) {
        dataStore.setSoundEnabled(enabled)
    }

    override suspend fun setVibrationEnabled(enabled: Boolean) {
        dataStore.setVibrationEnabled(enabled)
    }
}
