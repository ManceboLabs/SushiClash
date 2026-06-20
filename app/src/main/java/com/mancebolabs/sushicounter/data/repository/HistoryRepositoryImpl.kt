package com.mancebolabs.sushicounter.data.repository

import com.mancebolabs.sushicounter.data.datastore.AppPreferencesDataStore
import com.mancebolabs.sushicounter.domain.model.FinishedGameSnapshot
import com.mancebolabs.sushicounter.domain.model.GameMode
import com.mancebolabs.sushicounter.domain.model.GroupGameHistoryEntry
import com.mancebolabs.sushicounter.domain.model.SoloGameHistoryEntry
import com.mancebolabs.sushicounter.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class HistoryRepositoryImpl(
    private val dataStore: AppPreferencesDataStore,
) : HistoryRepository {

    override val soloHistory: Flow<List<SoloGameHistoryEntry>> = dataStore.soloHistory

    override val groupHistory: Flow<List<GroupGameHistoryEntry>> = dataStore.groupHistory

    override suspend fun saveFinishedGame(snapshot: FinishedGameSnapshot) {
        when (snapshot.gameMode) {
            GameMode.SOLO -> {
                dataStore.appendSoloHistoryEntry(
                    SoloGameHistoryEntry(
                        id = UUID.randomUUID().toString(),
                        date = snapshot.finishedAt,
                        totalSushi = snapshot.soloCount ?: 0,
                        randomRouletteEnabled = snapshot.randomRouletteEnabled,
                        randomRouletteMode = rouletteModeLabel(snapshot),
                    ),
                )
            }
            GameMode.GROUP -> {
                dataStore.appendGroupHistoryEntry(
                    GroupGameHistoryEntry(
                        id = UUID.randomUUID().toString(),
                        date = snapshot.finishedAt,
                        players = snapshot.playerScores,
                        randomRouletteEnabled = snapshot.randomRouletteEnabled,
                        randomRouletteMode = rouletteModeLabel(snapshot),
                    ),
                )
            }
        }
    }

    override suspend fun clearHistory() {
        dataStore.clearHistory()
    }

    private fun rouletteModeLabel(snapshot: FinishedGameSnapshot): String? {
        if (!snapshot.randomRouletteEnabled) return null
        return snapshot.randomRouletteTriggerType.name
    }
}
