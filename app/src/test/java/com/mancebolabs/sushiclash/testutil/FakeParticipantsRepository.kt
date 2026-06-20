package com.mancebolabs.sushiclash.testutil

import com.mancebolabs.sushiclash.domain.repository.ParticipantsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeParticipantsRepository(
    initialParticipants: List<String> = emptyList(),
) : ParticipantsRepository {
    private val _participants = MutableStateFlow(initialParticipants)
    override val participants: Flow<List<String>> = _participants.asStateFlow()

    var ensureGroupParticipantsSeededCallCount = 0

    override suspend fun ensureGroupParticipantsSeeded() {
        ensureGroupParticipantsSeededCallCount++
    }

    override suspend fun addParticipant(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        if (_participants.value.any { it.equals(trimmed, ignoreCase = true) }) return
        _participants.value = _participants.value + trimmed
    }

    override suspend fun removeParticipant(name: String) {
        _participants.value = _participants.value.filterNot { it == name }
    }

    override suspend fun clearParticipants() {
        _participants.value = emptyList()
    }

    fun setParticipants(participants: List<String>) {
        _participants.value = participants
    }
}
