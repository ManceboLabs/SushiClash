package com.mancebolabs.sushiclash.domain.repository

import com.mancebolabs.sushiclash.domain.model.PersistenceReadState
import kotlinx.coroutines.flow.Flow

interface ParticipantsRepository {
    val participants: Flow<PersistenceReadState<List<String>>>

    suspend fun ensureGroupParticipantsSeeded(): Boolean

    suspend fun addParticipant(name: String): Boolean

    suspend fun removeParticipant(name: String): Boolean

    suspend fun clearParticipants(): Boolean
}
