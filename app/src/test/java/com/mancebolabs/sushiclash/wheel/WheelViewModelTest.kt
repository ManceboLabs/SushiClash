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

    private fun subscribeToUiState(scope: TestScope, viewModel: WheelViewModel) {
        scope.backgroundScope.launch {
            viewModel.uiState.collect { }
        }
        scope.advanceUntilIdle()
    }
}
