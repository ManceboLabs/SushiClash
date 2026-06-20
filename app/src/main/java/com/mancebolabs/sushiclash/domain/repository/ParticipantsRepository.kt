package com.mancebolabs.sushiclash.domain.repository

import kotlinx.coroutines.flow.Flow

interface ParticipantsRepository {
    val participants: Flow<List<String>>

    suspend fun ensureGroupParticipantsSeeded()

    suspend fun addParticipant(name: String)

    suspend fun removeParticipant(name: String)

    suspend fun clearParticipants()
}
