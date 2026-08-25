package com.mancebolabs.sushiclash.testutil

import com.mancebolabs.sushiclash.domain.model.PersistenceReadState
import com.mancebolabs.sushiclash.domain.repository.ParticipantsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeParticipantsRepository(
    initialParticipants: List<String> = emptyList(),
) : ParticipantsRepository {
    private val _participants = MutableStateFlow<PersistenceReadState<List<String>>>(
        PersistenceReadState.Data(initialParticipants),
    )
    override val participants: Flow<PersistenceReadState<List<String>>> = _participants.asStateFlow()

    var ensureGroupParticipantsSeededCallCount = 0
    var ensureGroupParticipantsSeededResult: Boolean? = null
    var ensureGroupParticipantsSeededThrowable: Throwable? = null
    var addParticipantCallCount = 0
    var addParticipantThrowable: Throwable? = null
    var removeParticipantThrowable: Throwable? = null

    override suspend fun ensureGroupParticipantsSeeded(): Boolean {
        ensureGroupParticipantsSeededCallCount++
        ensureGroupParticipantsSeededThrowable?.let { throw it }
        ensureGroupParticipantsSeededResult?.let { return it }
        return when (_participants.value) {
            is PersistenceReadState.Data,
            PersistenceReadState.Missing -> true
            PersistenceReadState.Corrupted,
            PersistenceReadState.Unavailable -> false
        }
    }

    override suspend fun addParticipant(name: String): Boolean {
        addParticipantCallCount++
        addParticipantThrowable?.let { throw it }
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return false
        val current = currentNames() ?: return false
        if (current.any { it.equals(trimmed, ignoreCase = true) }) return false
        _participants.value = PersistenceReadState.Data(current + trimmed)
        return true
    }

    override suspend fun removeParticipant(name: String): Boolean {
        removeParticipantThrowable?.let { throw it }
        val current = currentNames() ?: return false
        _participants.value = PersistenceReadState.Data(current.filterNot { it == name })
        return true
    }

    override suspend fun clearParticipants(): Boolean {
        _participants.value = PersistenceReadState.Data(emptyList())
        return true
    }

    fun setParticipants(participants: List<String>) {
        _participants.value = PersistenceReadState.Data(participants)
    }

    fun setParticipantsCorrupted() {
        _participants.value = PersistenceReadState.Corrupted
    }

    fun setParticipantsUnavailable() {
        _participants.value = PersistenceReadState.Unavailable
    }

    private fun currentNames(): List<String>? {
        return when (val state = _participants.value) {
            is PersistenceReadState.Data -> state.value
            PersistenceReadState.Missing -> emptyList()
            PersistenceReadState.Corrupted,
            PersistenceReadState.Unavailable -> null
        }
    }
}
