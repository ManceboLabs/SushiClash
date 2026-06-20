package com.mancebolabs.sushicounter.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mancebolabs.sushicounter.domain.model.GroupHistoryRanking
import com.mancebolabs.sushicounter.domain.model.GroupPlayerRanking
import com.mancebolabs.sushicounter.domain.model.SoloGameHistoryEntry
import com.mancebolabs.sushicounter.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
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
)

class HistoryViewModel(
    historyRepository: HistoryRepository,
) : ViewModel() {

    private val selectedSection = kotlinx.coroutines.flow.MutableStateFlow(HistorySection.SOLO)

    val uiState: StateFlow<HistoryUiState> = combine(
        historyRepository.soloHistory.map { entries -> sortSoloEntries(entries) },
        historyRepository.groupHistory.map { entries -> GroupHistoryRanking.aggregate(entries) },
        selectedSection,
    ) { soloItems, groupRankings, section ->
        HistoryUiState(
            selectedSection = section,
            soloItems = soloItems,
            groupItems = groupRankings.mapIndexed { index, ranking ->
                GroupHistoryItem(position = index + 1, ranking = ranking)
            },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HistoryUiState(),
    )

    fun onSectionSelected(section: HistorySection) {
        selectedSection.value = section
    }

    private fun sortSoloEntries(entries: List<SoloGameHistoryEntry>): List<SoloHistoryItem> {
        return entries
            .sortedWith(
                compareByDescending<SoloGameHistoryEntry> { it.totalSushi }
                    .thenByDescending { it.date },
            )
            .mapIndexed { index, entry ->
                SoloHistoryItem(position = index + 1, entry = entry)
            }
    }

    companion object {
        fun factory(historyRepository: HistoryRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HistoryViewModel(historyRepository) as T
                }
            }
        }
    }
}
