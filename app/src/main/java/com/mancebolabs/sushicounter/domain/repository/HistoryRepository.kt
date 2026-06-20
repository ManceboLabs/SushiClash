package com.mancebolabs.sushicounter.domain.repository

import com.mancebolabs.sushicounter.domain.model.FinishedGameSnapshot
import com.mancebolabs.sushicounter.domain.model.GroupGameHistoryEntry
import com.mancebolabs.sushicounter.domain.model.SoloGameHistoryEntry
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    val soloHistory: Flow<List<SoloGameHistoryEntry>>

    val groupHistory: Flow<List<GroupGameHistoryEntry>>

    suspend fun saveFinishedGame(snapshot: FinishedGameSnapshot)

    suspend fun clearHistory()
}
