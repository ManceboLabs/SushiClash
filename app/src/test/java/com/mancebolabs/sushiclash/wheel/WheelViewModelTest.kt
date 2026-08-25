package com.mancebolabs.sushiclash.wheel

import com.mancebolabs.sushiclash.feature.wheel.WheelViewModel
import com.mancebolabs.sushiclash.testutil.FakeParticipantsRepository
import com.mancebolabs.sushiclash.testutil.FakeRandomProvider
import com.mancebolabs.sushiclash.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class WheelViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    @Test
    fun givenAtLeastTwoParticipants_whenSpinning_thenSelectsWinner() = runTest(testDispatcher) {
        val random = FakeRandomProvider().apply {
            enqueue(1)
            enqueue(4)
        }
        val viewModel = WheelViewModel(
            participantsRepository = FakeParticipantsRepository(listOf("Ana", "Luis", "Bea")),
            randomProvider = random,
        )
        subscribeToUiState(this, viewModel)

        viewModel.onSpin()
        advanceTimeBy(WheelViewModel.SPIN_DURATION_MS + 1)

        assertEquals("Luis", viewModel.uiState.value.selectedWinner)
        assertFalse(viewModel.uiState.value.isSpinning)
    }

    @Test
    fun givenAutoSpinWithEnoughParticipants_whenRequested_thenSpinsAutomatically() = runTest(testDispatcher) {
        val random = FakeRandomProvider().apply {
            enqueue(0)
            enqueue(4)
        }
        val participantsRepository = FakeParticipantsRepository(listOf("Ana", "Luis"))
        val viewModel = WheelViewModel(participantsRepository, random)
        subscribeToUiState(this, viewModel)

        viewModel.onAutoSpinRequested()
        advanceTimeBy(WheelViewModel.SPIN_DURATION_MS + 1)
        advanceUntilIdle()

        assertEquals("Ana", viewModel.uiState.value.selectedWinner)
        assertTrue(participantsRepository.ensureGroupParticipantsSeededCallCount >= 1)
    }

    @Test
    fun givenAutoSpinWithInsufficientParticipants_whenRequested_thenShowsWarning() = runTest(testDispatcher) {
        val viewModel = WheelViewModel(
            participantsRepository = FakeParticipantsRepository(listOf("Ana")),
        )
        subscribeToUiState(this, viewModel)

        viewModel.onAutoSpinRequested()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showInsufficientParticipantsWarning)
        assertEquals(null, viewModel.uiState.value.selectedWinner)
    }

    @Test
    fun givenParticipantAdded_whenNameIsValid_thenUpdatesParticipantList() = runTest(testDispatcher) {
        val viewModel = WheelViewModel(FakeParticipantsRepository())
        subscribeToUiState(this, viewModel)

        viewModel.onInputChanged("Marta")
        viewModel.onAddParticipant()
        advanceUntilIdle()

        assertEquals(listOf("Marta"), viewModel.uiState.value.participants)
        assertEquals("", viewModel.uiState.value.inputName)
    }

    @Test
    fun givenCorruptedParticipants_whenObserving_thenShowsPersistenceErrorAndEmptyList() = runTest(testDispatcher) {
        val participantsRepository = FakeParticipantsRepository(listOf("Ana", "Luis")).apply {
            setParticipantsCorrupted()
        }
        val viewModel = WheelViewModel(participantsRepository)
        subscribeToUiState(this, viewModel)

        val state = viewModel.uiState.value
        assertTrue(state.persistenceError)
        assertTrue(state.participants.isEmpty())
    }

    @Test
    fun givenUnavailableParticipants_whenObserving_thenShowsPersistenceErrorAndEmptyList() = runTest(testDispatcher) {
        val participantsRepository = FakeParticipantsRepository(listOf("Ana", "Luis")).apply {
            setParticipantsUnavailable()
        }
        val viewModel = WheelViewModel(participantsRepository)
        subscribeToUiState(this, viewModel)

        val state = viewModel.uiState.value
        assertTrue(state.persistenceError)
        assertTrue(state.participants.isEmpty())
    }

    @Test
    fun givenCorruptedParticipants_whenAdding_thenKeepsErrorAndDoesNotClearInput() = runTest(testDispatcher) {
        val participantsRepository = FakeParticipantsRepository().apply {
            setParticipantsCorrupted()
        }
        val viewModel = WheelViewModel(participantsRepository)
        subscribeToUiState(this, viewModel)

        viewModel.onInputChanged("Marta")
        viewModel.onAddParticipant()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.persistenceError)
        assertTrue(state.participants.isEmpty())
        assertEquals("Marta", state.inputName)
    }

    @Test
    fun givenAddThrows_whenAddingParticipant_thenShowsPersistenceErrorAndKeepsInput() = runTest(testDispatcher) {
        val participantsRepository = FakeParticipantsRepository(listOf("Ana")).apply {
            addParticipantThrowable = IOException("disk")
        }
        val viewModel = WheelViewModel(participantsRepository)
        subscribeToUiState(this, viewModel)

        viewModel.onInputChanged("Marta")
        viewModel.onAddParticipant()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.persistenceError)
        assertEquals(listOf("Ana"), state.participants)
        assertEquals("Marta", state.inputName)
    }

    @Test
    fun givenAddThrows_whenRetrySucceeds_thenAddsParticipantAndClearsError() = runTest(testDispatcher) {
        val participantsRepository = FakeParticipantsRepository(listOf("Ana")).apply {
            addParticipantThrowable = IOException("disk")
        }
        val viewModel = WheelViewModel(participantsRepository)
        subscribeToUiState(this, viewModel)

        viewModel.onInputChanged("Marta")
        viewModel.onAddParticipant()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.persistenceError)
        assertEquals(1, participantsRepository.addParticipantCallCount)

        viewModel.onPersistenceRetry()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.persistenceError)
        assertEquals(listOf("Ana"), viewModel.uiState.value.participants)
        assertEquals("Marta", viewModel.uiState.value.inputName)

        participantsRepository.addParticipantThrowable = null
        viewModel.onPersistenceRetry()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.persistenceError)
        assertEquals(listOf("Ana", "Marta"), state.participants)
        assertEquals("", state.inputName)
        assertEquals(3, participantsRepository.addParticipantCallCount)
    }

    @Test
    fun givenSeedThrows_whenInitialized_thenShowsPersistenceError() = runTest(testDispatcher) {
        val participantsRepository = FakeParticipantsRepository(listOf("Ana")).apply {
            ensureGroupParticipantsSeededThrowable = IOException("disk")
        }
        val viewModel = WheelViewModel(participantsRepository)
        subscribeToUiState(this, viewModel)

        assertTrue(viewModel.uiState.value.persistenceError)
        assertEquals(listOf("Ana"), viewModel.uiState.value.participants)
    }

    @Test
    fun givenCorruptedParticipants_whenRetryWithoutPendingWrite_thenDoesNotClaimSuccess() = runTest(testDispatcher) {
        val participantsRepository = FakeParticipantsRepository(listOf("Ana", "Luis")).apply {
            setParticipantsCorrupted()
        }
        val viewModel = WheelViewModel(participantsRepository)
        subscribeToUiState(this, viewModel)
        val seedCountAfterInit = participantsRepository.ensureGroupParticipantsSeededCallCount
        assertTrue(viewModel.uiState.value.persistenceError)

        viewModel.onPersistenceRetry()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.persistenceError)
        assertTrue(state.participants.isEmpty())
        assertEquals(seedCountAfterInit + 1, participantsRepository.ensureGroupParticipantsSeededCallCount)
    }

    private fun subscribeToUiState(scope: TestScope, viewModel: WheelViewModel) {
        scope.backgroundScope.launch {
            viewModel.uiState.collect { }
        }
        scope.advanceUntilIdle()
    }
}
