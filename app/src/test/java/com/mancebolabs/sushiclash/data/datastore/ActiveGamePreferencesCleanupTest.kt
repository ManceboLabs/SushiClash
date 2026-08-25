package com.mancebolabs.sushiclash.data.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.mancebolabs.sushiclash.domain.model.GameMode
import com.mancebolabs.sushiclash.domain.model.GameState
import com.mancebolabs.sushiclash.domain.model.Player
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ActiveGamePreferencesCleanupTest {

    @Test
    fun givenCurrentValidNewGame_whenRestoringStaleSnapshot_thenPreservesNewGame() {
        val dataStore = createDataStore()
        val hasActiveGameKey = booleanPreferencesKey("has_active_game")
        val sessionIdKey = stringPreferencesKey("game_session_id")
        val gameModeKey = stringPreferencesKey("game_mode")
        val playersKey = stringPreferencesKey("players")
        val themeKey = stringPreferencesKey("theme_mode")
        val preferences = mutablePreferencesOf(
            hasActiveGameKey to true,
            sessionIdKey to "new-session",
            gameModeKey to "SOLO",
            playersKey to validSoloPlayers,
            themeKey to "DARK",
        )

        val restoredState = dataStore.restoreGameState(
            preferences = preferences,
            decodedState = DecodedGameState(
                gameState = validSoloGameState(sessionId = "new-session"),
            ),
            migratedSessionId = "stale-migration",
        )

        assertTrue(restoredState.hasActiveGame)
        assertEquals("new-session", restoredState.sessionId)
        assertEquals("new-session", preferences[sessionIdKey])
        assertEquals("DARK", preferences[themeKey])
    }

    @Test
    fun givenCurrentValidLegacyGame_whenRestoring_thenMigratesSessionId() {
        val dataStore = createDataStore()
        val hasActiveGameKey = booleanPreferencesKey("has_active_game")
        val sessionIdKey = stringPreferencesKey("game_session_id")
        val gameModeKey = stringPreferencesKey("game_mode")
        val playersKey = stringPreferencesKey("players")
        val preferences = mutablePreferencesOf(
            hasActiveGameKey to true,
            gameModeKey to "SOLO",
            playersKey to validSoloPlayers,
        )

        val restoredState = dataStore.restoreGameState(
            preferences = preferences,
            decodedState = DecodedGameState(
                gameState = validSoloGameState(sessionId = null),
            ),
            migratedSessionId = "migrated-session",
        )

        assertTrue(restoredState.hasActiveGame)
        assertEquals("migrated-session", restoredState.sessionId)
        assertEquals("migrated-session", preferences[sessionIdKey])
    }

    @Test
    fun givenCurrentInvalidActiveGame_whenRestoring_thenClearsOnlyActiveGameKeys() {
        val dataStore = createDataStore()
        val hasActiveGameKey = booleanPreferencesKey("has_active_game")
        val sessionIdKey = stringPreferencesKey("game_session_id")
        val gameModeKey = stringPreferencesKey("game_mode")
        val playersKey = stringPreferencesKey("players")
        val themeKey = stringPreferencesKey("theme_mode")
        val historyKey = stringPreferencesKey("solo_history")
        val preferences = mutablePreferencesOf(
            hasActiveGameKey to true,
            sessionIdKey to "invalid-session",
            gameModeKey to "SOLO",
            playersKey to "malformed",
            themeKey to "DARK",
            historyKey to """[{"id":"history"}]""",
        )

        val restoredState = dataStore.restoreGameState(
            preferences = preferences,
            decodedState = DecodedGameState(
                gameState = GameState(hasActiveGame = true),
                isDecodeValid = false,
            ),
            migratedSessionId = "unused-migration",
        )

        assertFalse(restoredState.hasActiveGame)
        assertFalse(preferences[hasActiveGameKey]!!)
        assertNull(preferences[sessionIdKey])
        assertEquals("DARK", preferences[themeKey])
        assertEquals("""[{"id":"history"}]""", preferences[historyKey])
    }

    @Test
    fun givenMixedPreferences_whenClearingActiveGameKeys_thenPreservesUnrelatedAndHistoryValues() {
        val dataStore = createDataStore()
        val hasActiveGameKey = booleanPreferencesKey("has_active_game")
        val hasCompletedSetupKey = booleanPreferencesKey("has_completed_setup")
        val sessionIdKey = stringPreferencesKey("game_session_id")
        val gameModeKey = stringPreferencesKey("game_mode")
        val playersKey = stringPreferencesKey("players")
        val participantsKey = stringPreferencesKey("participants")
        val rouletteEnabledKey = booleanPreferencesKey("random_roulette_enabled")
        val rouletteTriggerTypeKey = stringPreferencesKey("random_roulette_trigger_type")
        val rouletteThresholdKey = intPreferencesKey("random_roulette_fixed_threshold")
        val themeKey = stringPreferencesKey("theme_mode")
        val onboardingKey = booleanPreferencesKey("has_completed_onboarding")
        val soloHistoryKey = stringPreferencesKey("solo_history")
        val groupHistoryKey = stringPreferencesKey("group_history")
        val arbitraryKey = stringPreferencesKey("arbitrary_key")
        val preferences = mutablePreferencesOf(
            hasActiveGameKey to true,
            hasCompletedSetupKey to true,
            sessionIdKey to "session",
            gameModeKey to "GROUP",
            playersKey to """[{"id":"p1"}]""",
            participantsKey to """["Ana"]""",
            rouletteEnabledKey to true,
            rouletteTriggerTypeKey to "RANDOM",
            rouletteThresholdKey to 8,
            themeKey to "DARK",
            onboardingKey to true,
            soloHistoryKey to """[{"id":"solo"}]""",
            groupHistoryKey to """[{"id":"group"}]""",
            arbitraryKey to "keep",
        )

        dataStore.clearActiveGameKeys(preferences)

        assertFalse(preferences[hasActiveGameKey]!!)
        assertFalse(preferences[hasCompletedSetupKey]!!)
        assertNull(preferences[sessionIdKey])
        assertNull(preferences[gameModeKey])
        assertEquals("[]", preferences[playersKey])
        assertEquals("[]", preferences[participantsKey])
        assertFalse(preferences[rouletteEnabledKey]!!)
        assertNull(preferences[rouletteTriggerTypeKey])
        assertNull(preferences[rouletteThresholdKey])
        assertEquals("DARK", preferences[themeKey])
        assertTrue(preferences[onboardingKey]!!)
        assertEquals("""[{"id":"solo"}]""", preferences[soloHistoryKey])
        assertEquals("""[{"id":"group"}]""", preferences[groupHistoryKey])
        assertEquals("keep", preferences[arbitraryKey])
    }

    private fun createDataStore(): AppPreferencesDataStore {
        return AppPreferencesDataStore(
            dataStore = mockk(relaxed = true),
            logger = NoOpPersistenceLogger,
        )
    }

    private fun validSoloGameState(sessionId: String?): GameState {
        return GameState(
            hasActiveGame = true,
            sessionId = sessionId,
            gameMode = GameMode.SOLO,
            players = listOf(
                Player(
                    id = AppPreferencesDataStore.SOLO_PLAYER_ID,
                    name = "",
                    sushiCount = 12,
                ),
            ),
        )
    }

    private companion object {
        const val validSoloPlayers = "valid-current-players"
    }
}
