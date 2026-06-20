package com.mancebolabs.sushicounter.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mancebolabs.sushicounter.domain.model.AppThemeMode
import com.mancebolabs.sushicounter.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val themeMode: AppThemeMode = AppThemeMode.LIGHT,
)

class SettingsViewModel(
    private val themeRepository: ThemeRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = themeRepository.themeMode
        .map { themeMode -> SettingsUiState(themeMode = themeMode) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState(),
        )

    fun onThemeModeSelected(themeMode: AppThemeMode) {
        viewModelScope.launch {
            themeRepository.setThemeMode(themeMode)
        }
    }

    companion object {
        fun factory(themeRepository: ThemeRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(themeRepository) as T
                }
            }
        }
    }
}
