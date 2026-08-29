package com.mancebolabs.sushiclash.domain.repository

import com.mancebolabs.sushiclash.domain.model.FrequentPlayer
import kotlinx.coroutines.flow.Flow

interface FrequentPlayersRepository {
    val frequentPlayers: Flow<List<FrequentPlayer>>
}
