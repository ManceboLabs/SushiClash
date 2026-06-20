package com.mancebolabs.sushicounter.testutil

import com.mancebolabs.sushicounter.domain.model.FinishedGameSnapshot
import com.mancebolabs.sushicounter.domain.model.GroupGameHistoryEntry
import com.mancebolabs.sushicounter.domain.model.SoloGameHistoryEntry
import com.mancebolabs.sushicounter.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeHistoryRepository : HistoryRepository {
    private val _soloHistory = MutableStateFlow<List<SoloGameHistoryEntry>>(emptyList())
    private val _groupHistory = MutableStateFlow<List<GroupGameHistoryEntry>>(emptyList())

    override val soloHistory: Flow<List<SoloGameHistoryEntry>> = _soloHistory.asStateFlow()
    override val groupHistory: Flow<List<GroupGameHistoryEntry>> = _groupHistory.asStateFlow()

    val savedSnapshots = mutableListOf<FinishedGameSnapshot>()
    var clearHistoryCallCount = 0

    override suspend fun saveFinishedGame(snapshot: FinishedGameSnapshot) {
        savedSnapshots.add(snapshot)
    }

    override suspend fun clearHistory() {
        clearHistoryCallCount++
        _soloHistory.value = emptyList()
        _groupHistory.value = emptyList()
    }

    fun setSoloHistory(entries: List<SoloGameHistoryEntry>) {
        _soloHistory.value = entries
    }

    fun setGroupHistory(entries: List<GroupGameHistoryEntry>) {
        _groupHistory.value = entries
    }
}
