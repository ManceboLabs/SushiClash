package com.mancebolabs.sushiclash.data.repository

import com.mancebolabs.sushiclash.data.datastore.AppPreferencesDataStore
import com.mancebolabs.sushiclash.domain.model.GameMode
import com.mancebolabs.sushiclash.domain.model.PersistenceReadState
import com.mancebolabs.sushiclash.domain.repository.GameRepository
import com.mancebolabs.sushiclash.domain.repository.ParticipantsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ParticipantsRepositoryImpl(
    private val dataStore: AppPreferencesDataStore,
    private val gameRepository: GameRepository,
) : ParticipantsRepository {

    override val participants: Flow<PersistenceReadState<List<String>>> = dataStore.participants

    override suspend fun ensureGroupParticipantsSeeded(): Boolean {
        val gameState = gameRepository.gameState.first()
        if (!gameState.hasActiveGame || gameState.gameMode != GameMode.GROUP) return false

        val playerNames = gameState.players.map { it.name }
        if (playerNames.size < AppPreferencesDataStore.MIN_GROUP_PLAYERS) return false

        return when (val currentParticipants = dataStore.participants.first()) {
            // Corrupted or unread participants must not be replaced by automatic seeding.
            PersistenceReadState.Corrupted,
            PersistenceReadState.Unavailable -> false
            PersistenceReadState.Missing -> persistParticipants(playerNames)
            is PersistenceReadState.Data -> {
                if (currentParticipants.value.isEmpty()) {
                    persistParticipants(playerNames)
                } else {
                    false
                }
            }
        }
    }

    override suspend fun addParticipant(name: String): Boolean {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return false

        val currentParticipants = readableParticipantNames() ?: return false
        if (currentParticipants.any { it.equals(trimmedName, ignoreCase = true) }) return false

        return persistParticipants(currentParticipants + trimmedName)
    }

    override suspend fun removeParticipant(name: String): Boolean {
        val currentParticipants = readableParticipantNames() ?: return false
        return persistParticipants(currentParticipants.filterNot { it == name })
    }

    override suspend fun clearParticipants(): Boolean {
        return persistParticipants(emptyList())
    }

    private suspend fun readableParticipantNames(): List<String>? {
        return when (val current = dataStore.participants.first()) {
            is PersistenceReadState.Data -> current.value
            PersistenceReadState.Missing -> emptyList()
            // Keep corrupted bytes until a later explicit recovery action; do not persist an empty list.
            PersistenceReadState.Corrupted,
            PersistenceReadState.Unavailable -> null
        }
    }

    private suspend fun persistParticipants(names: List<String>): Boolean {
        dataStore.setParticipants(names)
        return true
    }
}
