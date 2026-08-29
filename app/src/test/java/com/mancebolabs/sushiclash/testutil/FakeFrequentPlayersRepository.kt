package com.mancebolabs.sushiclash.testutil

import com.mancebolabs.sushiclash.domain.model.FrequentPlayer
import com.mancebolabs.sushiclash.domain.repository.FrequentPlayersRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeFrequentPlayersRepository(
    initialPlayers: List<FrequentPlayer> = emptyList(),
) : FrequentPlayersRepository {

    private val _frequentPlayers = MutableStateFlow(initialPlayers)

    override val frequentPlayers: Flow<List<FrequentPlayer>> = _frequentPlayers.asStateFlow()

    fun setPlayers(players: List<FrequentPlayer>) {
        _frequentPlayers.value = players
    }
}
