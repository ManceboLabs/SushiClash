package com.mancebolabs.sushiclash.domain.achievement

import com.mancebolabs.sushiclash.domain.model.GameState

fun GameState.maxSushiInGame(): Int {
    return players.maxOfOrNull { it.sushiCount } ?: 0
}

fun GameState.totalSushiInGame(): Int {
    return players.sumOf { it.sushiCount }
}
