package com.mancebolabs.sushiclash.data.repository

import com.mancebolabs.sushiclash.data.datastore.AppPreferencesDataStore
import com.mancebolabs.sushiclash.domain.model.GroupGameHistoryEntry
import com.mancebolabs.sushiclash.domain.model.PersistenceReadState
import com.mancebolabs.sushiclash.domain.model.SoloGameHistoryEntry
import com.mancebolabs.sushiclash.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow

class HistoryRepositoryImpl(
    private val dataStore: AppPreferencesDataStore,
) : HistoryRepository {

    override val soloHistory: Flow<PersistenceReadState<List<SoloGameHistoryEntry>>> =
        dataStore.soloHistory

    override val groupHistory: Flow<PersistenceReadState<List<GroupGameHistoryEntry>>> =
        dataStore.groupHistory

    override suspend fun reloadHistory() {
        dataStore.refreshPreferencesRead()
    }

    override suspend fun clearHistory() {
        dataStore.clearHistory()
    }
}
