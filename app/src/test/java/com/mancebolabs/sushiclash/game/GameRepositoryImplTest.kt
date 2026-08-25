package com.mancebolabs.sushiclash.game

import com.mancebolabs.sushiclash.data.datastore.AppPreferencesDataStore
import com.mancebolabs.sushiclash.data.datastore.DecodedGameState
import com.mancebolabs.sushiclash.data.datastore.FinishGamePersistenceResult
import com.mancebolabs.sushiclash.data.repository.GameRepositoryImpl
import com.mancebolabs.sushiclash.domain.model.CorruptGameHistoryException
import com.mancebolabs.sushiclash.domain.model.FinishGameResult
import com.mancebolabs.sushiclash.domain.model.GameMode
import com.mancebolabs.sushiclash.domain.model.GameSetupConfig
import com.mancebolabs.sushiclash.domain.model.GameState
import com.mancebolabs.sushiclash.domain.model.InvalidActiveGameException
import com.mancebolabs.sushiclash.domain.model.Player
import com.mancebolabs.sushiclash.domain.model.RandomRouletteLogic
import com.mancebolabs.sushiclash.domain.model.RandomRouletteTriggerType
import com.mancebolabs.sushiclash.testutil.FakeRandomProvider
import com.mancebolabs.sushiclash.testutil.TestGameStates
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import io.mockk.slot
import java.io.IOException
import java.util.concurrent.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GameRepositoryImplTest {

    private val dataStore = mockk<AppPreferencesDataStore>()
    private val gameStateFlow = MutableStateFlow(GameState())
    private lateinit var repository: GameRepositoryImpl

    @Before
    fun setUp() {
        every { dataStore.decodedGameState } returns gameStateFlow.map { gameState ->
            DecodedGameState(gameState)
        }
        repository = GameRepositoryImpl(
            dataStore = dataStore,
            randomRouletteLogic = RandomRouletteLogic(FakeRandomProvider().apply { enqueue(7) }),
            sessionIdProvider = { "session-created" },
            clock = { 1_700_000_000_000L },
        )
    }

    @Test
    fun givenValidPersistedSoloGame_whenRestoring_thenUsesSingleAtomicOperationAndReturnsState() = runTest {
        val persistedState = TestGameStates.soloActive(count = 12)
        coEvery { dataStore.restoreGameState("session-created") } returns persistedState

        val restoredState = repository.restoreGameState()

        assertEquals(persistedState, restoredState)
        assertTrue(restoredState.hasActiveGame)
        assertEquals(12, restoredState.soloCount)
        coVerify(exactly = 1) { dataStore.restoreGameState("session-created") }
        coVerify(exactly = 0) { dataStore.clearActiveGame() }
    }

    @Test
    fun givenValidLegacyGame_whenRestoring_thenPersistsAndReturnsGeneratedSessionId() = runTest {
        val migratedState = TestGameStates.soloActive(sessionId = "session-created", count = 12)
        coEvery { dataStore.restoreGameState("session-created") } returns migratedState

        val restoredState = repository.restoreGameState()

        assertEquals("session-created", restoredState.sessionId)
        coVerify(exactly = 1) { dataStore.restoreGameState("session-created") }
        coVerify(exactly = 0) { dataStore.clearActiveGame() }
    }

    @Test
    fun givenMalformedPersistedPlayersSignal_whenRestoring_thenClearsAndReturnsInactiveState() = runTest {
        coEvery { dataStore.restoreGameState("session-created") } returns GameState()

        val restoredState = repository.restoreGameState()

        assertFalse(restoredState.hasActiveGame)
        coVerify(exactly = 1) { dataStore.restoreGameState("session-created") }
        coVerify(exactly = 0) { dataStore.clearActiveGame() }
    }

    @Test
    fun givenMissingSemanticData_whenRestoring_thenClearsDespiteValidDecodeSignal() = runTest {
        coEvery { dataStore.restoreGameState("session-created") } returns GameState()

        val restoredState = repository.restoreGameState()

        assertFalse(restoredState.hasActiveGame)
        coVerify(exactly = 1) { dataStore.restoreGameState("session-created") }
        coVerify(exactly = 0) { dataStore.clearActiveGame() }
    }

    @Test
    fun givenSemanticallyInvalidPersistedGame_whenCollectingFlow_thenNeverEmitsActiveGame() = runTest {
        gameStateFlow.value =
            GameState(
                hasActiveGame = true,
                gameMode = GameMode.GROUP,
                players = emptyList(),
            )
        coEvery { dataStore.clearActiveGame() } just Runs

        val exposedState = repository.gameState.first()

        assertFalse(exposedState.hasActiveGame)
        coVerify(exactly = 0) { dataStore.clearActiveGame() }
    }

    @Test
    fun givenSoloSetup_whenCompletingSetup_thenCreatesSoloPlayer() = runTest {
        coEvery { dataStore.saveGameState(any(), any(), any(), any(), any(), any()) } just Runs
        coEvery { dataStore.setParticipants(any()) } just Runs

        repository.completeSetup(
            GameSetupConfig(
                gameMode = GameMode.SOLO,
                randomRouletteEnabled = true,
                randomRouletteTriggerType = RandomRouletteTriggerType.RANDOM,
            ),
        )

        val playersSlot = slot<List<Player>>()
        coVerify {
            dataStore.saveGameState(
                sessionId = "session-created",
                gameMode = GameMode.SOLO,
                players = capture(playersSlot),
                randomRouletteEnabled = true,
                randomRouletteTriggerType = RandomRouletteTriggerType.RANDOM,
                randomRouletteFixedThreshold = GameState.DEFAULT_RANDOM_ROULETTE_THRESHOLD,
            )
        }
        assertEquals(1, playersSlot.captured.size)
        assertEquals(AppPreferencesDataStore.SOLO_PLAYER_ID, playersSlot.captured.first().id)
        assertEquals(7, playersSlot.captured.first().nextRandomRouletteTarget)
    }

    @Test
    fun givenGroupSetup_whenCompletingSetup_thenCreatesPlayersAndSeedsParticipants() = runTest {
        coEvery { dataStore.saveGameState(any(), any(), any(), any(), any(), any()) } just Runs
        coEvery { dataStore.setParticipants(any()) } just Runs

        repository.completeSetup(
            GameSetupConfig(
                gameMode = GameMode.GROUP,
                playerNames = listOf("Ana", "Luis"),
            ),
        )

        val playersSlot = slot<List<Player>>()
        coVerify {
            dataStore.saveGameState(
                sessionId = "session-created",
                gameMode = GameMode.GROUP,
                players = capture(playersSlot),
                randomRouletteEnabled = false,
                randomRouletteTriggerType = RandomRouletteTriggerType.FIXED,
                randomRouletteFixedThreshold = GameState.DEFAULT_RANDOM_ROULETTE_THRESHOLD,
            )
        }
        assertEquals(listOf("Ana", "Luis"), playersSlot.captured.map { it.name })
        coVerify { dataStore.setParticipants(listOf("Ana", "Luis")) }
    }

    @Test
    fun givenActiveSoloGame_whenIncrementing_thenCounterIncreases() = runTest {
        gameStateFlow.value = TestGameStates.soloActive(count = 3)
        val playersSlot = slot<List<Player>>()
        coEvery { dataStore.setPlayers(capture(playersSlot)) } just Runs

        val result = repository.incrementPlayerCount(AppPreferencesDataStore.SOLO_PLAYER_ID)

        assertEquals(4, result.newCount)
        assertFalse(result.shouldTriggerRoulette)
        assertEquals(4, playersSlot.captured.first().sushiCount)
    }

    @Test
    fun givenFixedRoulette_whenThresholdReached_thenTriggersRoulette() = runTest {
        gameStateFlow.value = TestGameStates.soloActive(
            count = 4,
            randomRouletteEnabled = true,
            fixedThreshold = 5,
        )
        coEvery { dataStore.setPlayers(any()) } just Runs

        val result = repository.incrementPlayerCount(AppPreferencesDataStore.SOLO_PLAYER_ID)

        assertTrue(result.shouldTriggerRoulette)
        assertEquals(5, result.newCount)
    }

    @Test
    fun givenRandomRoulette_whenTargetReached_thenGeneratesNextTarget() = runTest {
        val random = FakeRandomProvider().apply {
            enqueue(12)
        }
        repository = GameRepositoryImpl(dataStore, RandomRouletteLogic(random))
        gameStateFlow.value = TestGameStates.soloActive(
            count = 4,
            randomRouletteEnabled = true,
            triggerType = RandomRouletteTriggerType.RANDOM,
            nextTarget = 5,
        )
        val playersSlot = slot<List<Player>>()
        coEvery { dataStore.setPlayers(capture(playersSlot)) } just Runs

        val result = repository.incrementPlayerCount(AppPreferencesDataStore.SOLO_PLAYER_ID)

        assertTrue(result.shouldTriggerRoulette)
        assertEquals(12, playersSlot.captured.first().nextRandomRouletteTarget)
    }

    @Test
    fun givenActiveSoloGame_whenIncrementingConcurrently_thenNoIncrementsAreLost() = runTest {
        gameStateFlow.value = TestGameStates.soloActive()
        persistPlayerUpdatesWithCooperativeYield()

        coroutineScope {
            List(100) {
                async {
                    repository.incrementPlayerCount(AppPreferencesDataStore.SOLO_PLAYER_ID)
                }
            }.awaitAll()
        }

        assertEquals(100, gameStateFlow.value.soloCount)
    }

    @Test
    fun givenGroupGame_whenIncrementingDifferentPlayersConcurrently_thenEachCountIsPreserved() = runTest {
        gameStateFlow.value = TestGameStates.groupActive(
            players = listOf(
                Player(id = "p1", name = "Ana"),
                Player(id = "p2", name = "Luis"),
            ),
        )
        persistPlayerUpdatesWithCooperativeYield()

        coroutineScope {
            val playerAUpdates = List(50) {
                async { repository.incrementPlayerCount("p1") }
            }
            val playerBUpdates = List(75) {
                async { repository.incrementPlayerCount("p2") }
            }
            (playerAUpdates + playerBUpdates).awaitAll()
        }

        assertEquals(50, gameStateFlow.value.players.first { it.id == "p1" }.sushiCount)
        assertEquals(75, gameStateFlow.value.players.first { it.id == "p2" }.sushiCount)
    }

    @Test
    fun givenGroupGame_whenIncrementingSamePlayerConcurrently_thenNoIncrementsAreLost() = runTest {
        gameStateFlow.value = TestGameStates.groupActive(
            players = listOf(
                Player(id = "p1", name = "Ana"),
                Player(id = "p2", name = "Luis"),
            ),
        )
        persistPlayerUpdatesWithCooperativeYield()

        coroutineScope {
            List(100) {
                async { repository.incrementPlayerCount("p1") }
            }.awaitAll()
        }

        assertEquals(100, gameStateFlow.value.players.first { it.id == "p1" }.sushiCount)
    }

    @Test
    fun givenFixedRoulette_whenConcurrentIncrementsReachThreshold_thenRouletteTriggersAtThreshold() = runTest {
        gameStateFlow.value = TestGameStates.soloActive(
            randomRouletteEnabled = true,
            fixedThreshold = 5,
        )
        persistPlayerUpdatesWithCooperativeYield()

        val results = coroutineScope {
            List(5) {
                async {
                    repository.incrementPlayerCount(AppPreferencesDataStore.SOLO_PLAYER_ID)
                }
            }.awaitAll()
        }

        assertEquals(5, gameStateFlow.value.soloCount)
        assertEquals(
            listOf(5),
            results.filter { it.shouldTriggerRoulette }.map { it.newCount },
        )
    }

    @Test
    fun givenGroupGame_whenResettingOnePlayer_thenOnlyThatPlayerIsReset() = runTest {
        val players = listOf(
            Player(id = "p1", name = "Ana", sushiCount = 8),
            Player(id = "p2", name = "Luis", sushiCount = 3),
        )
        gameStateFlow.value = TestGameStates.groupActive(players)
        val playersSlot = slot<List<Player>>()
        coEvery { dataStore.setPlayers(capture(playersSlot)) } just Runs

        repository.resetPlayerCount("p1")

        assertEquals(0, playersSlot.captured.first { it.id == "p1" }.sushiCount)
        assertEquals(3, playersSlot.captured.first { it.id == "p2" }.sushiCount)
    }

    @Test
    fun givenActiveGame_whenFinishingWithSaving_thenUsesSingleAtomicStoreOperation() = runTest {
        coEvery {
            dataStore.finishGameWithSaving(
                legacySessionId = "session-created",
                finishedAt = 1_700_000_000_000L,
            )
        } returns FinishGamePersistenceResult.Saved

        val result = repository.finishGameWithSaving()

        assertEquals(FinishGameResult.Success, result)
        coVerify(exactly = 1) {
            dataStore.finishGameWithSaving(
                legacySessionId = "session-created",
                finishedAt = 1_700_000_000_000L,
            )
        }
        coVerify(exactly = 0) { dataStore.clearActiveGame() }
    }

    @Test
    fun givenNoActiveGame_whenFinishingWithSaving_thenReturnsNoActiveGame() = runTest {
        coEvery {
            dataStore.finishGameWithSaving(any(), any())
        } returns FinishGamePersistenceResult.NoActiveGame

        val result = repository.finishGameWithSaving()

        assertEquals(FinishGameResult.NoActiveGame, result)
    }

    @Test
    fun givenInvalidActiveGame_whenFinishingWithSaving_thenReturnsRecoverableFailureWithoutClearing() = runTest {
        coEvery {
            dataStore.finishGameWithSaving(any(), any())
        } returns FinishGamePersistenceResult.InvalidActiveGame

        val result = repository.finishGameWithSaving()

        assertTrue(result is FinishGameResult.Failure)
        assertTrue((result as FinishGameResult.Failure).cause is InvalidActiveGameException)
        coVerify(exactly = 0) { dataStore.clearActiveGame() }
    }

    @Test
    fun givenPersistenceFailure_whenFinishingWithSaving_thenReturnsFailureWithoutClearing() = runTest {
        val failure = IOException("disk failure")
        coEvery { dataStore.finishGameWithSaving(any(), any()) } throws failure

        val result = repository.finishGameWithSaving()

        assertEquals(FinishGameResult.Failure(failure), result)
        coVerify(exactly = 0) { dataStore.clearActiveGame() }
    }

    @Test
    fun givenCorruptTargetHistory_whenFinishingWithSaving_thenReturnsFailureWithoutClearing() = runTest {
        coEvery {
            dataStore.finishGameWithSaving(any(), any())
        } returns FinishGamePersistenceResult.CorruptHistory

        val result = repository.finishGameWithSaving()

        assertTrue(result is FinishGameResult.Failure)
        assertTrue((result as FinishGameResult.Failure).cause is CorruptGameHistoryException)
        coVerify(exactly = 0) { dataStore.clearActiveGame() }
    }

    @Test(expected = CancellationException::class)
    fun givenCancellation_whenFinishingWithSaving_thenPropagatesCancellation() = runTest {
        coEvery {
            dataStore.finishGameWithSaving(any(), any())
        } throws CancellationException("cancelled")

        repository.finishGameWithSaving()
    }

    @Test
    fun givenConcurrentFinishCalls_whenSaving_thenMutexSerializesAtomicOperations() = runTest {
        var invocationCount = 0
        var activeCalls = 0
        var maximumActiveCalls = 0
        coEvery { dataStore.finishGameWithSaving(any(), any()) } coAnswers {
            activeCalls++
            maximumActiveCalls = maxOf(maximumActiveCalls, activeCalls)
            yield()
            invocationCount++
            activeCalls--
            if (invocationCount == 1) {
                FinishGamePersistenceResult.Saved
            } else {
                FinishGamePersistenceResult.NoActiveGame
            }
        }

        val results = coroutineScope {
            listOf(
                async { repository.finishGameWithSaving() },
                async { repository.finishGameWithSaving() },
            ).awaitAll()
        }

        assertEquals(1, maximumActiveCalls)
        assertEquals(1, results.count { it == FinishGameResult.Success })
        assertEquals(1, results.count { it == FinishGameResult.NoActiveGame })
    }

    @Test
    fun givenTwoRepositoryInstances_whenFinishingConcurrently_thenAtomicStoreKeepsOperationIdempotent() = runTest {
        val atomicStoreMutex = Mutex()
        var hasActiveGame = true
        var savedHistoryCount = 0
        coEvery { dataStore.finishGameWithSaving(any(), any()) } coAnswers {
            atomicStoreMutex.withLock {
                yield()
                if (!hasActiveGame) {
                    FinishGamePersistenceResult.NoActiveGame
                } else {
                    savedHistoryCount++
                    hasActiveGame = false
                    FinishGamePersistenceResult.Saved
                }
            }
        }
        val firstRepository = GameRepositoryImpl(
            dataStore = dataStore,
            sessionIdProvider = { "fallback-one" },
        )
        val secondRepository = GameRepositoryImpl(
            dataStore = dataStore,
            sessionIdProvider = { "fallback-two" },
        )

        val results = coroutineScope {
            listOf(
                async { firstRepository.finishGameWithSaving() },
                async { secondRepository.finishGameWithSaving() },
            ).awaitAll()
        }

        assertEquals(1, savedHistoryCount)
        assertEquals(1, results.count { it == FinishGameResult.Success })
        assertEquals(1, results.count { it == FinishGameResult.NoActiveGame })
        coVerify(exactly = 2) { dataStore.finishGameWithSaving(any(), any()) }
    }

    @Test
    fun givenActiveGame_whenFinishingWithoutSaving_thenOnlyClearsActiveGame() = runTest {
        gameStateFlow.value = TestGameStates.soloActive()
        coEvery { dataStore.clearActiveGame() } just Runs

        val result = repository.finishGameWithoutSaving()

        assertEquals(FinishGameResult.Success, result)
        coVerify(exactly = 1) { dataStore.clearActiveGame() }
        coVerify(exactly = 0) { dataStore.finishGameWithSaving(any(), any()) }
    }

    @Test
    fun givenClearFailure_whenFinishingWithoutSaving_thenReturnsFailure() = runTest {
        gameStateFlow.value = TestGameStates.soloActive()
        val failure = IOException("disk failure")
        coEvery { dataStore.clearActiveGame() } throws failure

        val result = repository.finishGameWithoutSaving()

        assertEquals(FinishGameResult.Failure(failure), result)
    }

    private fun persistPlayerUpdatesWithCooperativeYield() {
        coEvery { dataStore.setPlayers(any()) } coAnswers {
            yield()
            gameStateFlow.value = gameStateFlow.value.copy(
                players = arg<List<Player>>(0),
            )
        }
    }
}
