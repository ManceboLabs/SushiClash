package com.mancebolabs.sushiclash.domain.model

data class PlayerScore(
    val playerName: String,
    val sushiCount: Int,
)

data class GroupGameHistoryEntry(
    val id: String,
    val date: Long,
    val players: List<PlayerScore>,
    val randomRouletteEnabled: Boolean,
    val randomRouletteMode: String?,
)

data class GroupPlayerRanking(
    val playerName: String,
    val bestScore: Int,
    val totalSushi: Int,
    val gamesPlayed: Int,
)
