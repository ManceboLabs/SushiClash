package com.mancebolabs.sushiclash.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mancebolabs.sushiclash.domain.model.AppThemeMode
import com.mancebolabs.sushiclash.domain.repository.HistoryRepository
import com.mancebolabs.sushiclash.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val themeMode: AppThemeMode = AppThemeMode.LIGHT,
    val showClearHistoryDialog: Boolean = false,
)

class SettingsViewModel(
    private val themeRepository: ThemeRepository,
    private val historyRepository: HistoryRepository,
) : ViewModel() {

    private val showClearHistoryDialog = MutableStateFlow(false)

    val uiState: StateFlow<SettingsUiState> = combine(
        themeRepository.themeMode.map { themeMode -> themeMode },
        showClearHistoryDialog,
    ) { themeMode, clearDialog ->
        SettingsUiState(
            themeMode = themeMode,
            showClearHistoryDialog = clearDialog,
        )
    }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState(),
        )

    fun onThemeModeSelected(themeMode: AppThemeMode) {
        viewModelScope.launch {
            themeRepository.setThemeMode(themeMode)
        }
    }

    fun onClearHistoryRequested() {
        showClearHistoryDialog.value = true
    }

    fun onClearHistoryDismissed() {
        showClearHistoryDialog.value = false
    }

    fun onClearHistoryConfirmed() {
        viewModelScope.launch {
            historyRepository.clearHistory()
            showClearHistoryDialog.value = false
        }
    }

    companion object {
        fun factory(
            themeRepository: ThemeRepository,
            historyRepository: HistoryRepository,
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(themeRepository, historyRepository) as T
                }
            }
        }
    }
}
