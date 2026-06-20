package com.mancebolabs.sushiclash.domain.model

data class SoloGameHistoryEntry(
    val id: String,
    val date: Long,
    val totalSushi: Int,
    val randomRouletteEnabled: Boolean,
    val randomRouletteMode: String?,
)
