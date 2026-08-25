package com.mancebolabs.sushiclash.game

import com.mancebolabs.sushiclash.data.datastore.AppPreferencesDataStore
import com.mancebolabs.sushiclash.data.repository.GameRepositoryImpl
import com.mancebolabs.sushiclash.data.repository.ParticipantsRepositoryImpl
import com.mancebolabs.sushiclash.domain.model.PersistenceReadState
import com.mancebolabs.sushiclash.testutil.TestGameStates
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class ParticipantsRepositoryImplTest {

    private val dataStore = mockk<AppPreferencesDataStore>()
    private val gameRepository = mockk<GameRepositoryImpl>()
    private val participantsFlow =
        MutableStateFlow<PersistenceReadState<List<String>>>(PersistenceReadState.Data(emptyList()))
    private lateinit var repository: ParticipantsRepositoryImpl

    @Before
    fun setUp() {
        every { dataStore.participants } returns participantsFlow
        every { gameRepository.gameState } returns flowOf(
            TestGameStates.groupActive(
                players = listOf(
                    com.mancebolabs.sushiclash.domain.model.Player(id = "1", name = "Ana", sushiCount = 0),
                    com.mancebolabs.sushiclash.domain.model.Player(id = "2", name = "Luis", sushiCount = 0),
                ),
            ),
        )
        repository = ParticipantsRepositoryImpl(dataStore, gameRepository)
    }

    @Test
    fun givenEmptyParticipants_whenSeedingGroupGame_thenCopiesPlayerNames() = runTest {
        coEvery { dataStore.setParticipants(any()) } just Runs

        val persisted = repository.ensureGroupParticipantsSeeded()

        assertTrue(persisted)
        coVerify { dataStore.setParticipants(listOf("Ana", "Luis")) }
    }

    @Test
    fun givenMissingParticipants_whenSeedingGroupGame_thenCopiesPlayerNames() = runTest {
        participantsFlow.value = PersistenceReadState.Missing
        coEvery { dataStore.setParticipants(any()) } just Runs

        val persisted = repository.ensureGroupParticipantsSeeded()

        assertTrue(persisted)
        coVerify { dataStore.setParticipants(listOf("Ana", "Luis")) }
    }

    @Test
    fun givenDuplicateName_whenAddingParticipant_thenDoesNotDuplicate() = runTest {
        participantsFlow.value = PersistenceReadState.Data(listOf("Ana"))
        coEvery { dataStore.setParticipants(any()) } just Runs

        val persisted = repository.addParticipant("Ana")

        assertFalse(persisted)
        coVerify(exactly = 0) { dataStore.setParticipants(any()) }
    }

    @Test
    fun givenNoActiveGame_whenSeedingParticipants_thenDoesNothing() = runTest {
        every { gameRepository.gameState } returns flowOf(
            com.mancebolabs.sushiclash.domain.model.GameState(hasActiveGame = false),
        )

        val persisted = repository.ensureGroupParticipantsSeeded()

        assertFalse(persisted)
        coVerify(exactly = 0) { dataStore.setParticipants(any()) }
    }

    @Test
    fun givenCorruptedParticipants_whenSeedingGroupGame_thenDoesNotOverwrite() = runTest {
        participantsFlow.value = PersistenceReadState.Corrupted
        coEvery { dataStore.setParticipants(any()) } just Runs

        val persisted = repository.ensureGroupParticipantsSeeded()

        assertFalse(persisted)
        coVerify(exactly = 0) { dataStore.setParticipants(any()) }
    }

    @Test
    fun givenUnavailableParticipants_whenSeedingGroupGame_thenDoesNotOverwrite() = runTest {
        participantsFlow.value = PersistenceReadState.Unavailable
        coEvery { dataStore.setParticipants(any()) } just Runs

        val persisted = repository.ensureGroupParticipantsSeeded()

        assertFalse(persisted)
        coVerify(exactly = 0) { dataStore.setParticipants(any()) }
    }

    @Test
    fun givenCorruptedParticipants_whenAddingOrRemoving_thenDoesNotOverwrite() = runTest {
        participantsFlow.value = PersistenceReadState.Corrupted
        coEvery { dataStore.setParticipants(any()) } just Runs

        assertFalse(repository.addParticipant("Bea"))
        assertFalse(repository.removeParticipant("Ana"))
        coVerify(exactly = 0) { dataStore.setParticipants(any()) }
    }

    @Test
    fun givenUnavailableParticipants_whenAddingOrRemoving_thenDoesNotOverwrite() = runTest {
        participantsFlow.value = PersistenceReadState.Unavailable
        coEvery { dataStore.setParticipants(any()) } just Runs

        assertFalse(repository.addParticipant("Bea"))
        assertFalse(repository.removeParticipant("Ana"))
        coVerify(exactly = 0) { dataStore.setParticipants(any()) }
    }

    @Test
    fun givenSetParticipantsThrows_whenMutatingParticipants_thenDoesNotReportSuccess() = runTest {
        coEvery { dataStore.setParticipants(any()) } throws IOException("disk unavailable")

        try {
            repository.ensureGroupParticipantsSeeded()
            fail("expected IOException")
        } catch (_: IOException) {
        }

        try {
            repository.addParticipant("Bea")
            fail("expected IOException")
        } catch (_: IOException) {
        }

        participantsFlow.value = PersistenceReadState.Data(listOf("Ana"))
        try {
            repository.removeParticipant("Ana")
            fail("expected IOException")
        } catch (_: IOException) {
        }

        try {
            repository.clearParticipants()
            fail("expected IOException")
        } catch (_: IOException) {
        }
    }
}
