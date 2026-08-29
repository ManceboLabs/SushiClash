package com.mancebolabs.sushiclash.data.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.mancebolabs.sushiclash.domain.model.GameMode
import com.mancebolabs.sushiclash.domain.model.PersistenceReadState
import com.mancebolabs.sushiclash.domain.model.Player
import com.mancebolabs.sushiclash.domain.model.RandomRouletteTriggerType
import io.mockk.mockk
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class FrequentPlayersPersistenceTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun givenMissingFrequentPlayers_whenObserving_thenReturnsEmptyList() = runTest {
        val (dataStore, dataStoreJob) = createTemporaryPreferencesDataStore()
        val store = AppPreferencesDataStore(dataStore, NoOpPersistenceLogger)

        try {
            assertEquals(emptyList<Any>(), store.frequentPlayers.first())
            assertEquals(PersistenceReadState.Missing, store.frequentPlayersFlow.first())
        } finally {
            dataStoreJob.cancel()
        }
    }

    @Test
    fun givenGroupGameFinishedWithSaving_whenPersisting_thenStoresUniqueTrimmedNames() = runTest {
        val (dataStore, dataStoreJob) = createTemporaryPreferencesDataStore()
        val store = AppPreferencesDataStore(dataStore, NoOpPersistenceLogger)

        try {
            store.saveGameState(
                sessionId = "group-session",
                gameMode = GameMode.GROUP,
                players = listOf(
                    Player(id = "p1", name = " Ana ", sushiCount = 2),
                    Player(id = "p2", name = "Luis", sushiCount = 1),
                ),
                randomRouletteEnabled = false,
                randomRouletteTriggerType = RandomRouletteTriggerType.FIXED,
                randomRouletteFixedThreshold = 5,
            )

            val result = store.finishGameWithSaving(
                legacySessionId = "group-session",
                finishedAt = 1_700_000_000_000L,
            )

            assertEquals(FinishGamePersistenceResult.Saved, result)
            val frequentPlayers = store.frequentPlayers.first()
            assertEquals(2, frequentPlayers.size)
            assertEquals(listOf("Ana", "Luis"), frequentPlayers.map { it.displayName })
            assertTrue(frequentPlayers.all { it.id.isNotBlank() })
            val decoded = store.decodedGameState.first()
            assertTrue(decoded is PersistenceReadState.Data)
            assertFalse((decoded as PersistenceReadState.Data).value.gameState.hasActiveGame)
        } finally {
            dataStoreJob.cancel()
        }
    }

    @Test
    fun givenExistingFrequentPlayers_whenFinishingAnotherGroupGame_thenMergesWithoutDuplicates() = runTest {
        val (dataStore, dataStoreJob) = createTemporaryPreferencesDataStore()
        val store = AppPreferencesDataStore(dataStore, NoOpPersistenceLogger)

        try {
            dataStore.edit { preferences ->
                preferences[AppPreferencesDataStore.FREQUENT_PLAYERS_KEY] =
                    """[{"id":"existing-1","displayName":"Ana"}]"""
            }

            store.saveGameState(
                sessionId = "group-session-2",
                gameMode = GameMode.GROUP,
                players = listOf(
                    Player(id = "p1", name = "ANA", sushiCount = 2),
                    Player(id = "p2", name = "Marta", sushiCount = 1),
                ),
                randomRouletteEnabled = false,
                randomRouletteTriggerType = RandomRouletteTriggerType.FIXED,
                randomRouletteFixedThreshold = 5,
            )

            store.finishGameWithSaving(
                legacySessionId = "group-session-2",
                finishedAt = 1_700_000_000_001L,
            )

            val frequentPlayers = store.frequentPlayers.first()
            assertEquals(2, frequentPlayers.size)
            assertEquals("Ana", frequentPlayers[0].displayName)
            assertEquals("existing-1", frequentPlayers[0].id)
            assertEquals("Marta", frequentPlayers[1].displayName)
        } finally {
            dataStoreJob.cancel()
        }
    }

    @Test
    fun givenSoloGameFinishedWithSaving_whenPersisting_thenDoesNotStoreFrequentPlayers() = runTest {
        val (dataStore, dataStoreJob) = createTemporaryPreferencesDataStore()
        val store = AppPreferencesDataStore(dataStore, NoOpPersistenceLogger)

        try {
            store.saveGameState(
                sessionId = "solo-session",
                gameMode = GameMode.SOLO,
                players = listOf(
                    Player(
                        id = AppPreferencesDataStore.SOLO_PLAYER_ID,
                        name = "",
                        sushiCount = 4,
                    ),
                ),
                randomRouletteEnabled = false,
                randomRouletteTriggerType = RandomRouletteTriggerType.FIXED,
                randomRouletteFixedThreshold = 5,
            )

            store.finishGameWithSaving(
                legacySessionId = "solo-session",
                finishedAt = 1_700_000_000_000L,
            )

            assertEquals(emptyList<Any>(), store.frequentPlayers.first())
        } finally {
            dataStoreJob.cancel()
        }
    }

    @Test
    fun givenGroupGameFinishedWithoutSaving_whenClearingActiveGame_thenDoesNotStoreFrequentPlayers() = runTest {
        val (dataStore, dataStoreJob) = createTemporaryPreferencesDataStore()
        val store = AppPreferencesDataStore(dataStore, NoOpPersistenceLogger)

        try {
            store.saveGameState(
                sessionId = "group-session",
                gameMode = GameMode.GROUP,
                players = listOf(
                    Player(id = "p1", name = "Ana", sushiCount = 2),
                    Player(id = "p2", name = "Luis", sushiCount = 1),
                ),
                randomRouletteEnabled = false,
                randomRouletteTriggerType = RandomRouletteTriggerType.FIXED,
                randomRouletteFixedThreshold = 5,
            )

            store.clearActiveGame()

            assertEquals(emptyList<Any>(), store.frequentPlayers.first())
        } finally {
            dataStoreJob.cancel()
        }
    }

    @Test
    fun givenStoredFrequentPlayers_whenClearingActiveGameKeys_thenPreservesFrequentPlayers() = runTest {
        val store = AppPreferencesDataStore(mockk(relaxed = true), NoOpPersistenceLogger)
        val frequentPlayersKey = stringPreferencesKey("frequent_players")
        val hasActiveGameKey = booleanPreferencesKey("has_active_game")
        val preferences = androidx.datastore.preferences.core.mutablePreferencesOf(
            hasActiveGameKey to true,
            frequentPlayersKey to """[{"id":"1","displayName":"Ana"}]""",
        )

        store.clearActiveGameKeys(preferences)

        assertEquals("""[{"id":"1","displayName":"Ana"}]""", preferences[frequentPlayersKey])
        assertFalse(preferences[hasActiveGameKey]!!)
    }

    private fun TestScope.createTemporaryPreferencesDataStore(): Pair<androidx.datastore.core.DataStore<Preferences>, Job> {
        val preferencesFile = File(temporaryFolder.root, "frequent_players_preferences.preferences_pb")
        val dataStoreJob = Job(coroutineContext[Job])
        val dataStore = PreferenceDataStoreFactory.create(
            scope = CoroutineScope(coroutineContext + dataStoreJob),
            produceFile = { preferencesFile },
        )
        return dataStore to dataStoreJob
    }

    private object NoOpPersistenceLogger : PersistenceLogger {
        override fun logFailure(operation: String, errorClassName: String) = Unit
    }
}
