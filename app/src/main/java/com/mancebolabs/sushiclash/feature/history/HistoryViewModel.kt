package com.mancebolabs.sushiclash.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mancebolabs.sushiclash.domain.model.GroupGameHistoryEntry
import com.mancebolabs.sushiclash.domain.model.GroupHistoryRanking
import com.mancebolabs.sushiclash.domain.model.GroupPlayerRanking
import com.mancebolabs.sushiclash.domain.model.PersistenceReadState
import com.mancebolabs.sushiclash.domain.model.SoloGameHistoryEntry
import com.mancebolabs.sushiclash.domain.model.SoloHistoryRanking
import com.mancebolabs.sushiclash.domain.model.isUnreadable
import com.mancebolabs.sushiclash.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class HistorySection {
    SOLO,
    GROUP,
}

data class SoloHistoryItem(
    val position: Int,
    val entry: SoloGameHistoryEntry,
)

data class GroupHistoryItem(
    val position: Int,
    val ranking: GroupPlayerRanking,
)

data class HistoryUiState(
    val selectedSection: HistorySection = HistorySection.SOLO,
    val soloItems: List<SoloHistoryItem> = emptyList(),
    val groupItems: List<GroupHistoryItem> = emptyList(),
    val persistenceError: Boolean = false,
    val isPersistenceRetrying: Boolean = false,
)

class HistoryViewModel(
    historyRepository: HistoryRepository,
) : ViewModel() {

    private val selectedSection = kotlinx.coroutines.flow.MutableStateFlow(HistorySection.SOLO)

    val uiState: StateFlow<HistoryUiState> = combine(
        historyRepository.soloHistory,
        historyRepository.groupHistory,
        selectedSection,
    ) { soloHistory, groupHistory, section ->
        HistoryUiState(
            selectedSection = section,
            soloItems = soloItemsFrom(soloHistory),
            groupItems = groupItemsFrom(groupHistory),
            persistenceError = soloHistory.isUnreadable() || groupHistory.isUnreadable(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HistoryUiState(),
    )

    fun onSectionSelected(section: HistorySection) {
        selectedSection.value = section
    }

    fun onPersistenceRetry() = Unit

    companion object {
        fun factory(historyRepository: HistoryRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HistoryViewModel(historyRepository) as T
                }
            }
        }

        private fun soloItemsFrom(
            state: PersistenceReadState<List<SoloGameHistoryEntry>>,
        ): List<SoloHistoryItem> {
            val entries = when (state) {
                is PersistenceReadState.Data -> state.value
                PersistenceReadState.Missing,
                PersistenceReadState.Corrupted,
                PersistenceReadState.Unavailable -> emptyList()
            }
            return SoloHistoryRanking.sort(entries).mapIndexed { index, entry ->
                SoloHistoryItem(position = index + 1, entry = entry)
            }
        }

        private fun groupItemsFrom(
            state: PersistenceReadState<List<GroupGameHistoryEntry>>,
        ): List<GroupHistoryItem> {
            val entries = when (state) {
                is PersistenceReadState.Data -> state.value
                PersistenceReadState.Missing,
                PersistenceReadState.Corrupted,
                PersistenceReadState.Unavailable -> emptyList()
            }
            return GroupHistoryRanking.aggregate(entries).mapIndexed { index, ranking ->
                GroupHistoryItem(position = index + 1, ranking = ranking)
            }
        }
    }
}
