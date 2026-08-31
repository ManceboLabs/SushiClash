package com.mancebolabs.sushiclash.counter

import com.mancebolabs.sushiclash.data.datastore.AppPreferencesDataStore
import com.mancebolabs.sushiclash.data.datastore.DecodedGameState
import com.mancebolabs.sushiclash.data.datastore.RestoreGamePersistenceResult
import com.mancebolabs.sushiclash.data.repository.GameRepositoryImpl
import com.mancebolabs.sushiclash.domain.model.ChefAnimationTriggerLogic
import com.mancebolabs.sushiclash.domain.model.ChefEventAnimation
import com.mancebolabs.sushiclash.domain.model.ChefEventAnimationSelector
import com.mancebolabs.sushiclash.domain.model.GameMode
import com.mancebolabs.sushiclash.domain.model.GameSetupConfig
import com.mancebolabs.sushiclash.domain.model.GameState
import com.mancebolabs.sushiclash.domain.model.IncrementResult
import com.mancebolabs.sushiclash.domain.model.PersistenceReadState
import com.mancebolabs.sushiclash.domain.model.Player
import com.mancebolabs.sushiclash.feature.counter.AppStartupState
import com.mancebolabs.sushiclash.feature.counter.CounterViewModel
import com.mancebolabs.sushiclash.testutil.FakeAchievementRepository
import com.mancebolabs.sushiclash.testutil.FakeFeedbackSettingsRepository
import com.mancebolabs.sushiclash.testutil.FakeFrequentPlayersRepository
import com.mancebolabs.sushiclash.testutil.FakeOnboardingRepository
import com.mancebolabs.sushiclash.testutil.FakeRandomProvider
import com.mancebolabs.sushiclash.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CounterViewModelChefRandomIntegrationTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun givenNewSoloGame_whenIncrementingUpToInitialTarget_thenEmitsChefEventExactlyOnce() = runTest {
        val chefRandom = FakeRandomProvider().apply {
            enqueue(4)
            enqueue(3)
        }
        val selectorRandom = FakeRandomProvider().apply { enqueue(0) }
        val harness = ChefRandomIntegrationHarness(
            chefRandom = chefRandom,
            selectorRandom = selectorRandom,
        )
        val viewModel = harness.createViewModel()
        advanceUntilIdle()

        viewModel.onSetupConfirmed(GameSetupConfig(gameMode = GameMode.SOLO))
        advanceUntilIdle()
        viewModel.onChefCelebrationDismissed()
        advanceUntilIdle()

        val initialTarget = harness.gameRepository.gameState.first().players.first().nextChefAnimationTarget
        assertTrue(initialTarget in ChefAnimationTriggerLogic.MIN_INTERVAL..ChefAnimationTriggerLogic.MAX_INTERVAL)

        repeat(initialTarget!! - 1) {
            viewModel.onSoloSushiTapped()
            advanceUntilIdle()
            assertNull(viewModel.uiState.value.chefRandomEvent)
        }

        viewModel.onSoloSushiTapped()
        advanceUntilIdle()

        assertEquals(ChefEventAnimation.DEVOURING, viewModel.uiState.value.chefRandomEvent)
        assertEquals(initialTarget, viewModel.uiState.value.soloCount)
        assertEquals(
            initialTarget + 3,
            harness.gameRepository.gameState.first().players.first().nextChefAnimationTarget,
        )
    }

    @Test
    fun givenNewGroupGame_whenOnePlayerReachesTarget_thenOnlyThatPlayerTriggers() = runTest {
        val chefRandom = FakeRandomProvider().apply {
            enqueue(3)
            enqueue(5)
            enqueue(4)
        }
        val selectorRandom = FakeRandomProvider().apply {
            enqueue(1)
            enqueue(2)
        }
        val harness = ChefRandomIntegrationHarness(
            chefRandom = chefRandom,
            selectorRandom = selectorRandom,
        )
        val viewModel = harness.createViewModel()
        advanceUntilIdle()

        viewModel.onSetupConfirmed(
            GameSetupConfig(
                gameMode = GameMode.GROUP,
                playerNames = listOf("Carlos", "Pablo"),
            ),
        )
        advanceUntilIdle()
        viewModel.onChefCelebrationDismissed()
        advanceUntilIdle()

        val players = harness.gameRepository.gameState.first().players
        val carlos = players.first { it.name == "Carlos" }
        val pablo = players.first { it.name == "Pablo" }
        assertEquals(3, carlos.nextChefAnimationTarget)
        assertEquals(5, pablo.nextChefAnimationTarget)

        repeat(2) {
            viewModel.onPlayerSushiTapped(carlos.id)
            advanceUntilIdle()
            assertNull(viewModel.uiState.value.chefRandomEvent)
        }

        viewModel.onPlayerSushiTapped(carlos.id)
        advanceUntilIdle()

        assertEquals(ChefEventAnimation.GIANT_SUSHI, viewModel.uiState.value.chefRandomEvent)
        assertEquals(3, harness.gameRepository.gameState.first().players.first { it.id == carlos.id }.sushiCount)
        assertEquals(0, harness.gameRepository.gameState.first().players.first { it.id == pablo.id }.sushiCount)
        assertEquals(5, harness.gameRepository.gameState.first().players.first { it.id == pablo.id }.nextChefAnimationTarget)
    }

    private class ChefRandomIntegrationHarness(
        chefRandom: FakeRandomProvider,
        selectorRandom: FakeRandomProvider,
    ) {
        private val dataStore = mockk<AppPreferencesDataStore>(relaxed = true)
        private val gameStateFlow = MutableStateFlow(GameState())
        val gameRepository: GameRepositoryImpl

        init {
            every { dataStore.decodedGameState } returns gameStateFlow.map { gameState ->
                PersistenceReadState.Data(DecodedGameState(gameState))
            }
            coEvery { dataStore.clearActiveGameAfterBackupRestoreIfNeeded() } just Runs
            coEvery { dataStore.restoreGameState(any()) } returns RestoreGamePersistenceResult.Restored(
                GameState(hasActiveGame = false),
            )
            coEvery { dataStore.saveGameState(any(), any(), any(), any(), any(), any()) } coAnswers {
                gameStateFlow.value = GameState(
                    hasActiveGame = true,
                    sessionId = firstArg(),
                    gameMode = secondArg(),
                    players = thirdArg(),
                    randomRouletteEnabled = arg(3),
                    randomRouletteTriggerType = arg(4),
                    randomRouletteFixedThreshold = arg(5),
                )
            }
            coEvery { dataStore.setParticipants(any()) } just Runs
            coEvery { dataStore.incrementPlayerCount(any(), any()) } coAnswers {
                yield()
                val playerId = firstArg<String>()
                val applyIncrement = secondArg<(Player, GameState) -> Pair<Player, IncrementResult>>()
                val currentState = gameStateFlow.value
                if (!currentState.hasActiveGame) {
                    return@coAnswers IncrementResult(newCount = 0, shouldTriggerRoulette = false)
                }
                var result = IncrementResult(newCount = 0, shouldTriggerRoulette = false)
                val updatedPlayers = currentState.players.map { player ->
                    if (player.id != playerId) {
                        player
                    } else {
                        val (updatedPlayer, incrementResult) = applyIncrement(player, currentState)
                        result = incrementResult
                        updatedPlayer
                    }
                }
                gameStateFlow.value = currentState.copy(players = updatedPlayers)
                result
            }
            gameRepository = GameRepositoryImpl(
                dataStore = dataStore,
                chefAnimationTriggerLogic = ChefAnimationTriggerLogic(chefRandom),
                chefEventAnimationSelector = ChefEventAnimationSelector(selectorRandom),
                sessionIdProvider = { "integration-session" },
            )
        }

        fun createViewModel(): CounterViewModel {
            return CounterViewModel(
                gameRepository,
                FakeOnboardingRepository(completed = true),
                FakeFeedbackSettingsRepository(),
                FakeAchievementRepository(),
                FakeFrequentPlayersRepository(),
            )
        }
    }
}
