package com.mancebolabs.sushiclash.feature.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mancebolabs.sushiclash.domain.repository.AchievementRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class AchievementsViewModel(
    achievementRepository: AchievementRepository,
) : ViewModel() {

    val uiState: StateFlow<AchievementsUiState> = achievementRepository.achievementState
        .map(::buildAchievementsUiState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = buildAchievementsUiState(
                com.mancebolabs.sushiclash.domain.model.achievement.AchievementPersistenceState(),
            ),
        )

    companion object {
        fun factory(
            achievementRepository: AchievementRepository,
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AchievementsViewModel(achievementRepository) as T
                }
            }
        }
    }
}
