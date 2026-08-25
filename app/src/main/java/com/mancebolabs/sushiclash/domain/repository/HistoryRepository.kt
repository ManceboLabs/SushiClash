package com.mancebolabs.sushiclash.domain.repository

import com.mancebolabs.sushiclash.domain.model.GroupGameHistoryEntry
import com.mancebolabs.sushiclash.domain.model.SoloGameHistoryEntry
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    val soloHistory: Flow<List<SoloGameHistoryEntry>>

    val groupHistory: Flow<List<GroupGameHistoryEntry>>

    suspend fun clearHistory()
}
