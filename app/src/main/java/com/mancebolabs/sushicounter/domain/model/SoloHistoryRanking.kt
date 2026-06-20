package com.mancebolabs.sushicounter.domain.model

/**
 * Sorts solo history by highest score first, then most recent date on ties.
 */
object SoloHistoryRanking {
    fun sort(entries: List<SoloGameHistoryEntry>): List<SoloGameHistoryEntry> {
        return entries.sortedWith(
            compareByDescending<SoloGameHistoryEntry> { it.totalSushi }
                .thenByDescending { it.date },
        )
    }
}
