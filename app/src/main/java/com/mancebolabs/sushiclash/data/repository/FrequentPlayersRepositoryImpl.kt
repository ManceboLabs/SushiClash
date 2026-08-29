package com.mancebolabs.sushiclash.data.repository

import com.mancebolabs.sushiclash.data.datastore.AppPreferencesDataStore
import com.mancebolabs.sushiclash.domain.model.FrequentPlayer
import com.mancebolabs.sushiclash.domain.repository.FrequentPlayersRepository
import kotlinx.coroutines.flow.Flow

class FrequentPlayersRepositoryImpl(
    private val dataStore: AppPreferencesDataStore,
) : FrequentPlayersRepository {

    override val frequentPlayers: Flow<List<FrequentPlayer>> = dataStore.frequentPlayers
}
