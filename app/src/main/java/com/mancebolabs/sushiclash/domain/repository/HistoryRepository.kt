package com.mancebolabs.sushiclash.domain.repository

import com.mancebolabs.sushiclash.domain.model.GroupGameHistoryEntry
import com.mancebolabs.sushiclash.domain.model.PersistenceReadState
import com.mancebolabs.sushiclash.domain.model.SoloGameHistoryEntry
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    val soloHistory: Flow<PersistenceReadState<List<SoloGameHistoryEntry>>>

    val groupHistory: Flow<PersistenceReadState<List<GroupGameHistoryEntry>>>

    suspend fun reloadHistory()

    suspend fun clearHistory()
}
