package com.mancebolabs.sushiclash.game

import com.mancebolabs.sushiclash.data.datastore.AppPreferencesDataStore
import com.mancebolabs.sushiclash.data.datastore.DecodedGameState
import com.mancebolabs.sushiclash.data.repository.GameRepositoryImpl
import com.mancebolabs.sushiclash.data.repository.HistoryRepositoryImpl
import com.mancebolabs.sushiclash.domain.model.FinishedGameSnapshot
import com.mancebolabs.sushiclash.domain.model.GameMode
import com.mancebolabs.sushiclash.domain.model.GameSetupConfig
import com.mancebolabs.sushiclash.domain.model.GameState
import com.mancebolabs.sushiclash.domain.model.Player
import com.mancebolabs.sushiclash.domain.model.RandomRouletteLogic
import com.mancebolabs.sushiclash.domain.model.RandomRouletteTriggerType
import com.mancebolabs.sushiclash.testutil.FakeRandomProvider
import com.mancebolabs.sushiclash.testutil.TestGameStates
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import io.mockk.slot
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
        repository = GameRepositoryImpl(dataStore, RandomRouletteLogic(FakeRandomProvider().apply { enqueue(7) }))
    }

    @Test
    fun givenValidPersistedSoloGame_whenRestoring_thenPreservesActiveStateAndCount() = runTest {
        val persistedState = TestGameStates.soloActive(count = 12)
        gameStateFlow.value = persistedState

        val restoredState = repository.restoreGameState()

        assertEquals(persistedState, restoredState)
        assertTrue(restoredState.hasActiveGame)
        assertEquals(12, restoredState.soloCount)
        coVerify(exactly = 0) { dataStore.clearActiveGame() }
    }

    @Test
    fun givenMalformedPersistedPlayersSignal_whenRestoring_thenClearsAndReturnsInactiveState() = runTest {
        every { dataStore.decodedGameState } returns flowOf(
            DecodedGameState(
                gameState = TestGameStates.soloActive(count = 12),
                isDecodeValid = false,
            ),
        )
        repository = GameRepositoryImpl(dataStore)
        coEvery { dataStore.clearActiveGame() } just Runs

        val restoredState = repository.restoreGameState()

        assertFalse(restoredState.hasActiveGame)
        coVerify(exactly = 1) { dataStore.clearActiveGame() }
    }

    @Test
    fun givenMissingSemanticData_whenRestoring_thenClearsDespiteValidDecodeSignal() = runTest {
        every { dataStore.decodedGameState } returns flowOf(
            DecodedGameState(
                gameState = GameState(
                    hasActiveGame = true,
                    gameMode = null,
                    players = listOf(Player(AppPreferencesDataStore.SOLO_PLAYER_ID, "")),
                ),
                isDecodeValid = true,
            ),
        )
        repository = GameRepositoryImpl(dataStore)
        coEvery { dataStore.clearActiveGame() } just Runs

        val restoredState = repository.restoreGameState()

        assertFalse(restoredState.hasActiveGame)
        coVerify(exactly = 1) { dataStore.clearActiveGame() }
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
    fun givenRestoreWaitingOnInvalidState_whenCompletingSetup_thenRestoreCannotClearNewGame() = runTest {
        val restoreReadStarted = CompletableDeferred<Unit>()
        val allowRestoreRead = CompletableDeferred<Unit>()
        every { dataStore.decodedGameState } returns flow {
            restoreReadStarted.complete(Unit)
            allowRestoreRead.await()
            emit(
                DecodedGameState(
                    gameState = GameState(hasActiveGame = true),
                    isDecodeValid = false,
                ),
            )
        }
        coEvery { dataStore.clearActiveGame() } just Runs
        coEvery { dataStore.saveGameState(any(), any(), any(), any(), any()) } just Runs
        coEvery { dataStore.setParticipants(any()) } just Runs
        repository = GameRepositoryImpl(dataStore)

        val restoreJob = launch { repository.restoreGameState() }
        restoreReadStarted.await()
        val setupJob = launch {
            repository.completeSetup(GameSetupConfig(gameMode = GameMode.SOLO))
        }
        yield()
        allowRestoreRead.complete(Unit)
        restoreJob.join()
        setupJob.join()

        coVerifyOrder {
            dataStore.clearActiveGame()
            dataStore.saveGameState(any(), any(), any(), any(), any())
        }
        coVerify(exactly = 1) { dataStore.clearActiveGame() }
    }

    @Test
    fun givenSoloSetup_whenCompletingSetup_thenCreatesSoloPlayer() = runTest {
        coEvery { dataStore.saveGameState(any(), any(), any(), any(), any()) } just Runs
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
        coEvery { dataStore.saveGameState(any(), any(), any(), any(), any()) } just Runs
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
    fun givenActiveGame_whenCreatingSnapshot_thenReturnsSnapshotWithoutClearing() = runTest {
        gameStateFlow.value = TestGameStates.soloActive(count = 12)

        val snapshot = repository.createFinishedGameSnapshot()

        assertNotNull(snapshot)
        assertEquals(GameMode.SOLO, snapshot?.gameMode)
        assertEquals(12, snapshot?.soloCount)
        coVerify(exactly = 0) { dataStore.clearActiveGame() }
    }

    @Test
    fun givenNoActiveGame_whenCreatingSnapshot_thenReturnsNull() = runTest {
        gameStateFlow.value = GameState(hasActiveGame = false)

        val snapshot = repository.createFinishedGameSnapshot()

        assertNull(snapshot)
        coVerify(exactly = 0) { dataStore.clearActiveGame() }
    }

    @Test
    fun givenActiveGame_whenClearingActiveGame_thenPersistenceIsCleared() = runTest {
        coEvery { dataStore.clearActiveGame() } just Runs

        repository.clearActiveGame()

        coVerify(exactly = 1) { dataStore.clearActiveGame() }
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

class HistoryRepositoryImplTest {

    private val dataStore = mockk<AppPreferencesDataStore>()
    private lateinit var repository: HistoryRepositoryImpl

    @Before
    fun setUpHistoryRepository() {
        every { dataStore.soloHistory } returns flowOf(emptyList())
        every { dataStore.groupHistory } returns flowOf(emptyList())
        repository = HistoryRepositoryImpl(dataStore)
    }

    @Test
    fun givenSoloSnapshot_whenSaving_thenAppendsSoloHistoryEntry() = runTest {
        coEvery { dataStore.appendSoloHistoryEntry(any()) } just Runs

        repository.saveFinishedGame(
            FinishedGameSnapshot(
                gameMode = GameMode.SOLO,
                soloCount = 25,
                playerScores = emptyList(),
                randomRouletteEnabled = true,
                randomRouletteTriggerType = RandomRouletteTriggerType.FIXED,
                randomRouletteFixedThreshold = 5,
                finishedAt = 1_700_000_000_000L,
            ),
        )

        coVerify {
            dataStore.appendSoloHistoryEntry(
                match { entry ->
                    entry.totalSushi == 25 &&
                        entry.randomRouletteEnabled &&
                        entry.randomRouletteMode == RandomRouletteTriggerType.FIXED.name
                },
            )
        }
    }

    @Test
    fun givenGroupSnapshot_whenSaving_thenAppendsGroupHistoryEntry() = runTest {
        coEvery { dataStore.appendGroupHistoryEntry(any()) } just Runs

        repository.saveFinishedGame(
            FinishedGameSnapshot(
                gameMode = GameMode.GROUP,
                soloCount = null,
                playerScores = listOf(
                    com.mancebolabs.sushiclash.domain.model.PlayerScore("Ana", 10),
                    com.mancebolabs.sushiclash.domain.model.PlayerScore("Luis", 7),
                ),
                randomRouletteEnabled = false,
                randomRouletteTriggerType = RandomRouletteTriggerType.FIXED,
                randomRouletteFixedThreshold = 5,
                finishedAt = 1_700_000_000_000L,
            ),
        )

        coVerify {
            dataStore.appendGroupHistoryEntry(
                match { entry ->
                    entry.players.size == 2 && entry.players.first().playerName == "Ana"
                },
            )
        }
    }
}
