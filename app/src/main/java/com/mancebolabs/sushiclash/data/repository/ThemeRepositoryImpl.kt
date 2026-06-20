package com.mancebolabs.sushiclash.data.repository

import com.mancebolabs.sushiclash.data.datastore.AppPreferencesDataStore
import com.mancebolabs.sushiclash.domain.model.AppThemeMode
import com.mancebolabs.sushiclash.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow

class ThemeRepositoryImpl(
    private val dataStore: AppPreferencesDataStore,
) : ThemeRepository {

    override val themeMode: Flow<AppThemeMode> = dataStore.themeMode

    override suspend fun setThemeMode(themeMode: AppThemeMode) {
        dataStore.setThemeMode(themeMode)
    }
}
