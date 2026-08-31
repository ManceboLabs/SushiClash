package com.mancebolabs.sushiclash.testutil

import com.mancebolabs.sushiclash.domain.model.GroupGameHistoryEntry
import com.mancebolabs.sushiclash.domain.model.PersistenceReadState
import com.mancebolabs.sushiclash.domain.model.SoloGameHistoryEntry
import com.mancebolabs.sushiclash.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeHistoryRepository : HistoryRepository {
    private val _soloHistory =
        MutableStateFlow<PersistenceReadState<List<SoloGameHistoryEntry>>>(PersistenceReadState.Missing)
    private val _groupHistory =
        MutableStateFlow<PersistenceReadState<List<GroupGameHistoryEntry>>>(PersistenceReadState.Missing)

    override val soloHistory: Flow<PersistenceReadState<List<SoloGameHistoryEntry>>> =
        _soloHistory.asStateFlow()
    override val groupHistory: Flow<PersistenceReadState<List<GroupGameHistoryEntry>>> =
        _groupHistory.asStateFlow()

    var clearHistoryCallCount = 0
    var clearHistoryThrowable: Throwable? = null

    override suspend fun clearHistory() {
        clearHistoryCallCount++
        clearHistoryThrowable?.let { throw it }
        _soloHistory.value = PersistenceReadState.Missing
        _groupHistory.value = PersistenceReadState.Missing
    }

    override suspend fun reloadHistory() {
        reloadHistoryCallCount++
        reloadHistoryThrowable?.let { throw it }
    }

    var reloadHistoryCallCount = 0
    var reloadHistoryThrowable: Throwable? = null

    fun setSoloHistory(entries: List<SoloGameHistoryEntry>) {
        _soloHistory.value = PersistenceReadState.Data(entries)
    }

    fun setGroupHistory(entries: List<GroupGameHistoryEntry>) {
        _groupHistory.value = PersistenceReadState.Data(entries)
    }

    fun setSoloHistoryCorrupted() {
        _soloHistory.value = PersistenceReadState.Corrupted
    }

    fun setGroupHistoryCorrupted() {
        _groupHistory.value = PersistenceReadState.Corrupted
    }

    fun setSoloHistoryUnavailable() {
        _soloHistory.value = PersistenceReadState.Unavailable
    }

    fun setGroupHistoryUnavailable() {
        _groupHistory.value = PersistenceReadState.Unavailable
    }
}
