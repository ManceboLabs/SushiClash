package com.mancebolabs.sushiclash.domain.repository

import com.mancebolabs.sushiclash.domain.model.AppThemeMode
import kotlinx.coroutines.flow.Flow

interface ThemeRepository {
    val themeMode: Flow<AppThemeMode>

    suspend fun setThemeMode(themeMode: AppThemeMode)
}
