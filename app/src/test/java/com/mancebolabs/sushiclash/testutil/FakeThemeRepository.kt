package com.mancebolabs.sushiclash.testutil

import com.mancebolabs.sushiclash.domain.model.AppThemeMode
import com.mancebolabs.sushiclash.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeThemeRepository(
    initialMode: AppThemeMode = AppThemeMode.LIGHT,
) : ThemeRepository {
    private val _themeMode = MutableStateFlow(initialMode)
    override val themeMode: Flow<AppThemeMode> = _themeMode.asStateFlow()

    override suspend fun setThemeMode(themeMode: AppThemeMode) {
        _themeMode.value = themeMode
    }
}
