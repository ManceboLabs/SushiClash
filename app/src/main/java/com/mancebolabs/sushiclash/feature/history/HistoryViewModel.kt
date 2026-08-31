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
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
    private val historyRepository: HistoryRepository,
) : ViewModel() {

    private val selectedSection = MutableStateFlow(HistorySection.SOLO)
    private val isPersistenceRetrying = MutableStateFlow(false)

    val uiState: StateFlow<HistoryUiState> = combine(
        historyRepository.soloHistory,
        historyRepository.groupHistory,
        selectedSection,
        isPersistenceRetrying,
    ) { soloHistory, groupHistory, section, isRetrying ->
        HistoryUiState(
            selectedSection = section,
            soloItems = soloItemsFrom(soloHistory),
            groupItems = groupItemsFrom(groupHistory),
            persistenceError = soloHistory.isUnreadable() || groupHistory.isUnreadable(),
            isPersistenceRetrying = isRetrying,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HistoryUiState(),
    )

    fun onSectionSelected(section: HistorySection) {
        selectedSection.value = section
    }

    fun onPersistenceRetry() {
        if (!isPersistenceRetrying.compareAndSet(expect = false, update = true)) return
        viewModelScope.launch {
            try {
                historyRepository.reloadHistory()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: IOException) {
                // The repository flows keep exposing the unreadable state until data recovers.
            } finally {
                isPersistenceRetrying.value = false
            }
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
