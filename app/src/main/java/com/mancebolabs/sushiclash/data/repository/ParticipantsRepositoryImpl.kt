package com.mancebolabs.sushiclash.data.repository

import com.mancebolabs.sushiclash.data.datastore.AppPreferencesDataStore
import com.mancebolabs.sushiclash.domain.model.GameMode
import com.mancebolabs.sushiclash.domain.repository.GameRepository
import com.mancebolabs.sushiclash.domain.repository.ParticipantsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ParticipantsRepositoryImpl(
    private val dataStore: AppPreferencesDataStore,
    private val gameRepository: GameRepository,
) : ParticipantsRepository {

    override val participants: Flow<List<String>> = dataStore.participants

    override suspend fun ensureGroupParticipantsSeeded() {
        val gameState = gameRepository.gameState.first()
        if (!gameState.hasActiveGame || gameState.gameMode != GameMode.GROUP) return

        val playerNames = gameState.players.map { it.name }
        if (playerNames.size < AppPreferencesDataStore.MIN_GROUP_PLAYERS) return

        val currentParticipants = dataStore.participants.first()
        if (currentParticipants.isEmpty()) {
            dataStore.setParticipants(playerNames)
        }
    }

    override suspend fun addParticipant(name: String) {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return

        val currentParticipants = dataStore.participants.first()
        if (currentParticipants.any { it.equals(trimmedName, ignoreCase = true) }) return

        dataStore.setParticipants(currentParticipants + trimmedName)
    }

    override suspend fun removeParticipant(name: String) {
        val currentParticipants = dataStore.participants.first()
        dataStore.setParticipants(currentParticipants.filterNot { it == name })
    }

    override suspend fun clearParticipants() {
        dataStore.setParticipants(emptyList())
    }
}
