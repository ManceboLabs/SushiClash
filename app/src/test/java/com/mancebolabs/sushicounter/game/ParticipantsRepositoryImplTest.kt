package com.mancebolabs.sushicounter.game

import com.mancebolabs.sushicounter.data.repository.ParticipantsRepositoryImpl
import com.mancebolabs.sushicounter.data.repository.GameRepositoryImpl
import com.mancebolabs.sushicounter.data.datastore.AppPreferencesDataStore
import com.mancebolabs.sushicounter.domain.model.GameMode
import com.mancebolabs.sushicounter.testutil.TestGameStates
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ParticipantsRepositoryImplTest {

    private val dataStore = mockk<AppPreferencesDataStore>()
    private val gameRepository = mockk<GameRepositoryImpl>()
    private val participantsFlow = MutableStateFlow<List<String>>(emptyList())
    private lateinit var repository: ParticipantsRepositoryImpl

    @Before
    fun setUp() {
        every { dataStore.participants } returns participantsFlow
        every { gameRepository.gameState } returns flowOf(
            TestGameStates.groupActive(
                players = listOf(
                    com.mancebolabs.sushicounter.domain.model.Player(id = "1", name = "Ana", sushiCount = 0),
                    com.mancebolabs.sushicounter.domain.model.Player(id = "2", name = "Luis", sushiCount = 0),
                ),
            ),
        )
        repository = ParticipantsRepositoryImpl(dataStore, gameRepository)
    }

    @Test
    fun givenEmptyParticipants_whenSeedingGroupGame_thenCopiesPlayerNames() = runTest {
        coEvery { dataStore.setParticipants(any()) } just Runs

        repository.ensureGroupParticipantsSeeded()

        coVerify { dataStore.setParticipants(listOf("Ana", "Luis")) }
    }

    @Test
    fun givenDuplicateName_whenAddingParticipant_thenDoesNotDuplicate() = runTest {
        participantsFlow.value = listOf("Ana")
        coEvery { dataStore.setParticipants(any()) } just Runs

        repository.addParticipant("Ana")

        coVerify(exactly = 0) { dataStore.setParticipants(any()) }
    }

    @Test
    fun givenNoActiveGame_whenSeedingParticipants_thenDoesNothing() = runTest {
        every { gameRepository.gameState } returns flowOf(
            com.mancebolabs.sushicounter.domain.model.GameState(hasActiveGame = false),
        )

        repository.ensureGroupParticipantsSeeded()

        coVerify(exactly = 0) { dataStore.setParticipants(any()) }
    }
}
