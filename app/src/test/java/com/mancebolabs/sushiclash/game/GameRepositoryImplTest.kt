package com.mancebolabs.sushiclash.game

import com.mancebolabs.sushiclash.data.datastore.AppPreferencesDataStore
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
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import io.mockk.slot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
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
        every { dataStore.gameState } returns gameStateFlow
        repository = GameRepositoryImpl(dataStore, RandomRouletteLogic(FakeRandomProvider().apply { enqueue(7) }))
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
    fun givenActiveGame_whenFinishing_thenClearsActiveGameAndReturnsSnapshot() = runTest {
        gameStateFlow.value = TestGameStates.soloActive(count = 12)
        coEvery { dataStore.clearActiveGame() } just Runs

        val snapshot = repository.finishActiveGame()

        assertNotNull(snapshot)
        assertEquals(GameMode.SOLO, snapshot?.gameMode)
        assertEquals(12, snapshot?.soloCount)
        coVerify { dataStore.clearActiveGame() }
    }

    @Test
    fun givenNoActiveGame_whenFinishing_thenReturnsNull() = runTest {
        gameStateFlow.value = GameState(hasActiveGame = false)

        val snapshot = repository.finishActiveGame()

        assertNull(snapshot)
        coVerify(exactly = 0) { dataStore.clearActiveGame() }
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
