package com.mancebolabs.sushicounter.domain.repository

import com.mancebolabs.sushicounter.domain.model.AppThemeMode
import kotlinx.coroutines.flow.Flow

interface ThemeRepository {
    val themeMode: Flow<AppThemeMode>

    suspend fun setThemeMode(themeMode: AppThemeMode)
}
