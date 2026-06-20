package com.mancebolabs.sushicounter.data.repository

import com.mancebolabs.sushicounter.data.datastore.AppPreferencesDataStore
import com.mancebolabs.sushicounter.domain.model.AppThemeMode
import com.mancebolabs.sushicounter.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow

class ThemeRepositoryImpl(
    private val dataStore: AppPreferencesDataStore,
) : ThemeRepository {

    override val themeMode: Flow<AppThemeMode> = dataStore.themeMode

    override suspend fun setThemeMode(themeMode: AppThemeMode) {
        dataStore.setThemeMode(themeMode)
    }
}
