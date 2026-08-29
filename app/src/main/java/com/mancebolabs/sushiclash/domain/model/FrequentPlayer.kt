package com.mancebolabs.sushiclash.domain.model

/**
 * Lightweight record for players that appear often in Group games.
 *
 * [id] is stable across sessions so future profile/statistics features can reference the same player.
 */
data class FrequentPlayer(
    val id: String,
    val displayName: String,
)
