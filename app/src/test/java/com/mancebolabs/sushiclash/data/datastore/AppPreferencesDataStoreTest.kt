package com.mancebolabs.sushiclash.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.preferencesOf
import app.cash.turbine.test
import com.mancebolabs.sushiclash.domain.model.AppThemeMode
import com.mancebolabs.sushiclash.domain.model.FeedbackSettingsDefaults
import com.mancebolabs.sushiclash.domain.model.GameMode
import com.mancebolabs.sushiclash.domain.model.GameState
import com.mancebolabs.sushiclash.domain.model.GameStateValidator
import com.mancebolabs.sushiclash.domain.model.GroupGameHistoryEntry
import com.mancebolabs.sushiclash.domain.model.PersistenceReadState
import com.mancebolabs.sushiclash.domain.model.Player
import com.mancebolabs.sushiclash.domain.model.PlayerScore
import com.mancebolabs.sushiclash.domain.model.RandomRouletteTriggerType
import com.mancebolabs.sushiclash.domain.model.SoloGameHistoryEntry
import java.io.File
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class AppPreferencesDataStoreTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun givenInjectedDataStore_whenSettingThemeMode_thenThemeModeEmitsPersistedValue() = runTest {
        val (dataStore, dataStoreJob) = createTemporaryPreferencesDataStore()
        val store = AppPreferencesDataStore(
            dataStore = dataStore,
            logger = NoOpPersistenceLogger,
        )

        try {
            store.setThemeMode(AppThemeMode.DARK)
            assertEquals(AppThemeMode.DARK, store.themeMode.first())
        } finally {
            dataStoreJob.cancel()
        }
    }

    @Test
    fun givenMissingFeedbackPreferences_whenObserving_thenDefaultsToEnabled() = runTest {
        val (dataStore, dataStoreJob) = createTemporaryPreferencesDataStore()
        val store = AppPreferencesDataStore(
            dataStore = dataStore,
            logger = NoOpPersistenceLogger,
        )

        try {
            assertTrue(store.soundEnabled.first())
            assertTrue(store.vibrationEnabled.first())
            assertEquals(
                PersistenceReadState.Missing,
                store.soundEnabledState.first(),
            )
            assertEquals(
                PersistenceReadState.Missing,
                store.vibrationEnabledState.first(),
            )
        } finally {
            dataStoreJob.cancel()
        }
    }

    @Test
    fun givenFeedbackDisabled_whenPersisted_thenEmitsStoredValues() = runTest {
        val (dataStore, dataStoreJob) = createTemporaryPreferencesDataStore()
        val store = AppPreferencesDataStore(
            dataStore = dataStore,
            logger = NoOpPersistenceLogger,
        )

        try {
            store.setSoundEnabled(false)
            store.setVibrationEnabled(false)

            assertFalse(store.soundEnabled.first())
            assertFalse(store.vibrationEnabled.first())
            assertEquals(
                PersistenceReadState.Data(false),
                store.soundEnabledState.first(),
            )
            assertEquals(
                PersistenceReadState.Data(false),
                store.vibrationEnabledState.first(),
            )
        } finally {
            dataStoreJob.cancel()
        }
    }

    @Test
    fun givenMissingFeedbackPreferences_whenMappedForUi_thenUsesBackwardCompatibleDefaults() = runTest {
        val (dataStore, dataStoreJob) = createTemporaryPreferencesDataStore()
        val store = AppPreferencesDataStore(
            dataStore = dataStore,
            logger = NoOpPersistenceLogger,
        )

        try {
            assertEquals(FeedbackSettingsDefaults.SOUND_ENABLED, store.soundEnabled.first())
            assertEquals(FeedbackSettingsDefaults.VIBRATION_ENABLED, store.vibrationEnabled.first())
        } finally {
            dataStoreJob.cancel()
        }
    }

    @Test
    fun givenMissingAchievementState_whenObserving_thenDefaultsToEmptyProgress() = runTest {
        val (dataStore, dataStoreJob) = createTemporaryPreferencesDataStore()
        val store = AppPreferencesDataStore(
            dataStore = dataStore,
            logger = NoOpPersistenceLogger,
        )

        try {
            val state = store.achievementState.first()
            assertEquals(0, state.totalGamesCompleted)
            assertEquals(0, state.totalRouletteSpins)
            assertTrue(state.unlockedAtById.isEmpty())
            assertEquals(
                PersistenceReadState.Missing,
                store.achievementStateFlow.first(),
            )
        } finally {
            dataStoreJob.cancel()
        }
    }

    @Test
    fun givenAchievementUnlocked_whenPersisted_thenRoundTripsUnlockTimestamp() = runTest {
        val (dataStore, dataStoreJob) = createTemporaryPreferencesDataStore()
        val store = AppPreferencesDataStore(
            dataStore = dataStore,
            logger = NoOpPersistenceLogger,
        )

        try {
            store.setAchievementState(
                com.mancebolabs.sushiclash.domain.model.achievement.AchievementPersistenceState(
                    totalGamesCompleted = 1,
                    peakSushiInSingleGame = 12,
                    unlockedAtById = mapOf("games_1" to 1_700_000_000_000L),
                ),
            )

            val state = store.achievementState.first()
            assertEquals(1, state.totalGamesCompleted)
            assertEquals(12, state.peakSushiInSingleGame)
            assertEquals(1_700_000_000_000L, state.unlockedAtById["games_1"])
        } finally {
            dataStoreJob.cancel()
        }
    }

    @Test
    fun givenAchievementProgress_whenResetToEmpty_thenRoundTripsDefaults() = runTest {
        val (dataStore, dataStoreJob) = createTemporaryPreferencesDataStore()
        val store = AppPreferencesDataStore(
            dataStore = dataStore,
            logger = NoOpPersistenceLogger,
        )

        try {
            store.setAchievementState(
                com.mancebolabs.sushiclash.domain.model.achievement.AchievementPersistenceState(
                    totalGamesCompleted = 10,
                    totalRouletteSpins = 5,
                    peakSushiInSingleGame = 40,
                    hasTriggeredAutomaticRoulette = true,
                    unlockedAtById = mapOf("games_10" to 1_700_000_000_000L),
                ),
            )
            store.setAchievementState(
                com.mancebolabs.sushiclash.domain.model.achievement.AchievementPersistenceState(),
            )

            val state = store.achievementState.first()
            assertEquals(com.mancebolabs.sushiclash.domain.model.achievement.AchievementPersistenceState(), state)
        } finally {
            dataStoreJob.cancel()
        }
    }

    @Test
    fun givenLifetimeSushiTotals_whenPersisted_thenRoundTripsValues() = runTest {
        val (dataStore, dataStoreJob) = createTemporaryPreferencesDataStore()
        val store = AppPreferencesDataStore(
            dataStore = dataStore,
            logger = NoOpPersistenceLogger,
        )

        try {
            store.setAchievementState(
                com.mancebolabs.sushiclash.domain.model.achievement.AchievementPersistenceState(
                    lifetimeSoloSushiTotal = 742,
                    lifetimeGroupSushiTotal = 1_250,
                    unlockedAtById = mapOf("solo_total_50" to 1_700_000_000_000L),
                ),
            )

            val state = store.achievementState.first()
            assertEquals(742, state.lifetimeSoloSushiTotal)
            assertEquals(1_250, state.lifetimeGroupSushiTotal)
            assertEquals(1_700_000_000_000L, state.unlockedAtById["solo_total_50"])
        } finally {
            dataStoreJob.cancel()
        }
    }

    @Test
    fun givenLegacyAchievementStateWithoutLifetimeTotals_whenDecoded_thenDefaultsToZero() = runTest {
        val (dataStore, dataStoreJob) = createTemporaryPreferencesDataStore()
        val store = AppPreferencesDataStore(
            dataStore = dataStore,
            logger = NoOpPersistenceLogger,
        )

        try {
            dataStore.updateData { preferences ->
                preferences.toMutablePreferences().apply {
                    this[AppPreferencesDataStore.ACHIEVEMENT_STATE_KEY] =
                        """{"profiles":{"default":{"totalGamesCompleted":2,"peakSushiInSingleGame":15,"totalRouletteSpins":0,"hasTriggeredAutomaticRoulette":false,"unlockedAtById":{}}}}"""
                }
            }

            val decoded = store.achievementState.first()
            assertEquals(0, decoded.lifetimeSoloSushiTotal)
            assertEquals(0, decoded.lifetimeGroupSushiTotal)
            assertEquals(2, decoded.totalGamesCompleted)
        } finally {
            dataStoreJob.cancel()
        }
    }

    @Test
    fun givenDataStoreAlwaysThrowsIOException_whenCollectingThemeModeState_thenEmitsUnavailableWithoutFloodingUntilDelay() =
        runTest {
            val dataStore = AlwaysFailingDataStore()
            val store = AppPreferencesDataStore(
                dataStore = dataStore,
                logger = NoOpPersistenceLogger,
            )

            store.themeModeState.test {
                assertEquals(PersistenceReadState.Unavailable, awaitItem())
                expectNoEvents()
                testScheduler.advanceTimeBy(1_000L)
                assertEquals(PersistenceReadState.Unavailable, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun givenDataStoreThrowsIOException_whenCollectingThemeModeState_thenEmitsUnavailableThenRecovers() =
        runTest {
            val dataStore = FirstReadFailingDataStore()
            val logger = RecordingPersistenceLogger()
            val store = AppPreferencesDataStore(
                dataStore = dataStore,
                logger = logger,
            )

            store.themeModeState.test {
                assertEquals(PersistenceReadState.Unavailable, awaitItem())
                testScheduler.advanceTimeBy(1_000L)
                assertEquals(PersistenceReadState.Missing, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }

            assertEquals(
                listOf(
                    RecordingPersistenceLogger.Failure(
                        operation = "readThemeMode",
                        errorClassName = "IOException",
                    ),
                ),
                logger.failures,
            )
        }

    @Test
    fun givenInvalidThemeValue_whenCollecting_thenThemeModeIsLightAndStateIsCorrupted() = runTest {
        val (dataStore, dataStoreJob) = createTemporaryPreferencesDataStore()
        val store = AppPreferencesDataStore(
            dataStore = dataStore,
            logger = NoOpPersistenceLogger,
        )

        try {
            dataStore.edit { preferences ->
                preferences[AppPreferencesDataStore.THEME_MODE_KEY] = "NOT_A_THEME"
            }

            assertEquals(AppThemeMode.LIGHT, store.themeMode.first())
            assertEquals(PersistenceReadState.Corrupted, store.themeModeState.first())
        } finally {
            dataStoreJob.cancel()
        }
    }

    @Test
    fun givenDataStoreThrowsIllegalStateException_whenCollectingThemeModeState_thenExceptionSurfacesToCollector() =
        runTest {
            val dataStore = IllegalStateThrowingDataStore()
            val store = AppPreferencesDataStore(
                dataStore = dataStore,
                logger = NoOpPersistenceLogger,
            )

            store.themeModeState.test {
                val error = awaitError()
                assertEquals(IllegalStateException::class.java, error::class.java)
            }
        }

    @Test
    fun givenValidSoloGame_whenSavingAndRestoring_thenCountersSessionAndModeArePreserved() = runTest {
        val (dataStore, dataStoreJob) = createTemporaryPreferencesDataStore()
        val store = AppPreferencesDataStore(
            dataStore = dataStore,
            logger = NoOpPersistenceLogger,
        )

        try {
            val players = listOf(
                Player(
                    id = AppPreferencesDataStore.SOLO_PLAYER_ID,
                    name = "",
                    sushiCount = 7,
                    nextRandomRouletteTarget = 9,
                    lastRandomRouletteTrigger = 0,
                ),
            )
            store.saveGameState(
                sessionId = "solo-session",
                gameMode = GameMode.SOLO,
                players = players,
                randomRouletteEnabled = true,
                randomRouletteTriggerType = RandomRouletteTriggerType.RANDOM,
                randomRouletteFixedThreshold = 6,
            )

            val result = store.restoreGameState(migratedSessionId = "unused-migration")

            val restored = (result as RestoreGamePersistenceResult.Restored).gameState
            assertTrue(restored.hasActiveGame)
            assertEquals("solo-session", restored.sessionId)
            assertEquals(GameMode.SOLO, restored.gameMode)
            assertEquals(players, restored.players)
            assertEquals(7, restored.soloCount)
            assertEquals(true, restored.randomRouletteEnabled)
            assertEquals(RandomRouletteTriggerType.RANDOM, restored.randomRouletteTriggerType)
            assertEquals(6, restored.randomRouletteFixedThreshold)
        } finally {
            dataStoreJob.cancel()
        }
    }

    @Test
    fun givenValidGroupGame_whenSavingAndRestoring_thenPlayersAndScoresArePreserved() = runTest {
        val (dataStore, dataStoreJob) = createTemporaryPreferencesDataStore()
        val store = AppPreferencesDataStore(
            dataStore = dataStore,
            logger = NoOpPersistenceLogger,
        )

        try {
            val players = listOf(
                Player(id = "p1", name = "Ana", sushiCount = 2),
                Player(id = "p2", name = "Luis", sushiCount = 5),
                Player(id = "p3", name = "Marta", sushiCount = 9),
                Player(id = "p4", name = "Paz", sushiCount = 1),
                Player(id = "p5", name = "Noa", sushiCount = 0),
                Player(id = "p6", name = "Iker", sushiCount = 4),
            )
            store.saveGameState(
                sessionId = "group-session",
                gameMode = GameMode.GROUP,
                players = players,
                randomRouletteEnabled = false,
                randomRouletteTriggerType = RandomRouletteTriggerType.FIXED,
                randomRouletteFixedThreshold = 5,
            )

            val result = store.restoreGameState(migratedSessionId = "unused-migration")

            val restored = (result as RestoreGamePersistenceResult.Restored).gameState
            assertTrue(restored.hasActiveGame)
            assertEquals("group-session", restored.sessionId)
            assertEquals(GameMode.GROUP, restored.gameMode)
            assertEquals(players, restored.players)
            assertEquals(listOf(2, 5, 9, 1, 0, 4), restored.players.map(Player::sushiCount))
        } finally {
            dataStoreJob.cancel()
        }
    }

    @Test
    fun givenMalformedPlayersJson_whenRestoring_thenClearsActiveGameAndPreservesUnrelatedPrefs() =
        runTest {
            val (dataStore, dataStoreJob) = createTemporaryPreferencesDataStore()
            val store = AppPreferencesDataStore(
                dataStore = dataStore,
                logger = NoOpPersistenceLogger,
            )

            try {
                dataStore.writeUnrelatedPreferences()
                dataStore.edit { preferences ->
                    preferences[AppPreferencesDataStore.HAS_ACTIVE_GAME_KEY] = true
                    preferences[AppPreferencesDataStore.GAME_MODE_KEY] = GameMode.SOLO.name
                    preferences[AppPreferencesDataStore.PLAYERS_KEY] = "{not-json"
                }

                val result = store.restoreGameState(migratedSessionId = "unused-migration")

                assertInactiveRestored(result)
                assertFalse(dataStore.data.first()[AppPreferencesDataStore.HAS_ACTIVE_GAME_KEY]!!)
                dataStore.assertUnrelatedPreferencesUnchanged()
            } finally {
                dataStoreJob.cancel()
            }
        }

    @Test
    fun givenInvalidGameModeEnum_whenRestoring_thenClearsActiveGameAndPreservesUnrelatedPrefs() =
        runTest {
            val (dataStore, dataStoreJob) = createTemporaryPreferencesDataStore()
            val store = AppPreferencesDataStore(
                dataStore = dataStore,
                logger = NoOpPersistenceLogger,
            )

            try {
                dataStore.writeUnrelatedPreferences()
                dataStore.edit { preferences ->
                    preferences[AppPreferencesDataStore.HAS_ACTIVE_GAME_KEY] = true
                    preferences[AppPreferencesDataStore.GAME_MODE_KEY] = "NOT_A_MODE"
                    preferences[AppPreferencesDataStore.PLAYERS_KEY] = VALID_SOLO_PLAYERS_JSON
                }

                val result = store.restoreGameState(migratedSessionId = "unused-migration")

                assertInactiveRestored(result)
                assertFalse(dataStore.data.first()[AppPreferencesDataStore.HAS_ACTIVE_GAME_KEY]!!)
                dataStore.assertUnrelatedPreferencesUnchanged()
            } finally {
                dataStoreJob.cancel()
            }
        }

    @Test
    fun givenInvalidRouletteTriggerEnum_whenRestoring_thenClearsActiveGameAndPreservesUnrelatedPrefs() =
        runTest {
            val (dataStore, dataStoreJob) = createTemporaryPreferencesDataStore()
            val store = AppPreferencesDataStore(
                dataStore = dataStore,
                logger = NoOpPersistenceLogger,
            )

            try {
                dataStore.writeUnrelatedPreferences()
                dataStore.edit { preferences ->
                    preferences[AppPreferencesDataStore.HAS_ACTIVE_GAME_KEY] = true
                    preferences[AppPreferencesDataStore.GAME_MODE_KEY] = GameMode.SOLO.name
                    preferences[AppPreferencesDataStore.PLAYERS_KEY] = VALID_SOLO_PLAYERS_JSON
                    preferences[AppPreferencesDataStore.RANDOM_ROULETTE_ENABLED_KEY] = true
                    preferences[AppPreferencesDataStore.RANDOM_ROULETTE_TRIGGER_TYPE_KEY] =
                        "NOT_A_TRIGGER"
                }

                val result = store.restoreGameState(migratedSessionId = "unused-migration")

                assertInactiveRestored(result)
                assertFalse(dataStore.data.first()[AppPreferencesDataStore.HAS_ACTIVE_GAME_KEY]!!)
                dataStore.assertUnrelatedPreferencesUnchanged()
            } finally {
                dataStoreJob.cancel()
            }
        }

    @Test
    fun givenLegacyActiveGameMissingSessionId_whenRestoring_thenWritesMigratedSessionIdAndStaysActive() =
        runTest {
            val (dataStore, dataStoreJob) = createTemporaryPreferencesDataStore()
            val store = AppPreferencesDataStore(
                dataStore = dataStore,
                logger = NoOpPersistenceLogger,
            )

            try {
                store.saveGameState(
                    sessionId = "will-be-removed",
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
                dataStore.edit { preferences ->
                    preferences.remove(AppPreferencesDataStore.SESSION_ID_KEY)
                }

                val result = store.restoreGameState(migratedSessionId = "migrated-session")

                val restored = (result as RestoreGamePersistenceResult.Restored).gameState
                assertTrue(restored.hasActiveGame)
                assertEquals("migrated-session", restored.sessionId)
                assertEquals(
                    "migrated-session",
                    dataStore.data.first()[AppPreferencesDataStore.SESSION_ID_KEY],
                )
            } finally {
                dataStoreJob.cancel()
            }
        }

    @Test
    fun givenEmptyStore_whenRestoring_thenReturnsInactiveGameWithoutInventingUnrelatedKeys() =
        runTest {
            val (dataStore, dataStoreJob) = createTemporaryPreferencesDataStore()
            val store = AppPreferencesDataStore(
                dataStore = dataStore,
                logger = NoOpPersistenceLogger,
            )

            try {
                val result = store.restoreGameState(migratedSessionId = "unused-migration")

                assertInactiveRestored(result)
                val preferences = dataStore.data.first()
                assertNull(preferences[AppPreferencesDataStore.SOLO_HISTORY_KEY])
                assertNull(preferences[AppPreferencesDataStore.GROUP_HISTORY_KEY])
                assertNull(preferences[AppPreferencesDataStore.THEME_MODE_KEY])
                assertNull(preferences[AppPreferencesDataStore.HAS_COMPLETED_ONBOARDING_KEY])
            } finally {
                dataStoreJob.cancel()
            }
        }

    @Test
    fun givenDataStoreThrowsIOException_whenRestoringGameState_thenReturnsUnavailableWithoutWritingAndFlowRecovers() =
        runTest {
            val failingDataStore = IoFailingPreferencesDataStore(
                initialPreferences = preferencesOf(
                    AppPreferencesDataStore.HAS_ACTIVE_GAME_KEY to true,
                    AppPreferencesDataStore.GAME_MODE_KEY to GameMode.SOLO.name,
                    AppPreferencesDataStore.PLAYERS_KEY to "{not-json",
                    AppPreferencesDataStore.THEME_MODE_KEY to AppThemeMode.DARK.name,
                    AppPreferencesDataStore.SOLO_HISTORY_KEY to UNRELATED_SOLO_HISTORY,
                    AppPreferencesDataStore.GROUP_HISTORY_KEY to UNRELATED_GROUP_HISTORY,
                    AppPreferencesDataStore.HAS_COMPLETED_ONBOARDING_KEY to true,
                ),
            )
            val logger = RecordingPersistenceLogger()
            val store = AppPreferencesDataStore(
                dataStore = failingDataStore,
                logger = logger,
            )

            val result = store.restoreGameState(migratedSessionId = "unused-migration")

            assertEquals(RestoreGamePersistenceResult.Unavailable, result)
            assertNotEquals(
                RestoreGamePersistenceResult.Restored(gameState = GameState()),
                result,
            )
            assertEquals(true, failingDataStore.storedPreferences[AppPreferencesDataStore.HAS_ACTIVE_GAME_KEY])
            assertEquals("{not-json", failingDataStore.storedPreferences[AppPreferencesDataStore.PLAYERS_KEY])
            assertEquals(AppThemeMode.DARK.name, failingDataStore.storedPreferences[AppPreferencesDataStore.THEME_MODE_KEY])
            assertEquals(
                UNRELATED_SOLO_HISTORY,
                failingDataStore.storedPreferences[AppPreferencesDataStore.SOLO_HISTORY_KEY],
            )
            assertEquals(
                UNRELATED_GROUP_HISTORY,
                failingDataStore.storedPreferences[AppPreferencesDataStore.GROUP_HISTORY_KEY],
            )
            assertEquals(
                true,
                failingDataStore.storedPreferences[AppPreferencesDataStore.HAS_COMPLETED_ONBOARDING_KEY],
            )
            assertTrue(
                logger.failures.any { failure ->
                    failure.operation == "restoreGameState" && failure.errorClassName == "IOException"
                },
            )

            store.decodedGameState.test {
                assertEquals(PersistenceReadState.Unavailable, awaitItem())
                failingDataStore.failReads = false
                testScheduler.advanceTimeBy(1_000L)
                val recovered = awaitItem()
                assertTrue(recovered is PersistenceReadState.Data<*>)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun givenInvalidActiveJson_whenRestoreRacesWithSaveGameState_thenNewGameIsNotDestroyedByStaleRestore() =
        runTest {
            val (dataStore, dataStoreJob) = createTemporaryPreferencesDataStore()
            val store = AppPreferencesDataStore(
                dataStore = dataStore,
                logger = NoOpPersistenceLogger,
            )

            try {
                dataStore.edit { preferences ->
                    preferences[AppPreferencesDataStore.HAS_ACTIVE_GAME_KEY] = true
                    preferences[AppPreferencesDataStore.SESSION_ID_KEY] = "stale-invalid"
                    preferences[AppPreferencesDataStore.GAME_MODE_KEY] = GameMode.SOLO.name
                    preferences[AppPreferencesDataStore.PLAYERS_KEY] = "{not-json"
                }

                val newPlayers = listOf(
                    Player(
                        id = AppPreferencesDataStore.SOLO_PLAYER_ID,
                        name = "",
                        sushiCount = 0,
                    ),
                )
                coroutineScope {
                    listOf(
                        async {
                            yield()
                            store.restoreGameState(migratedSessionId = "unused-migration")
                        },
                        async {
                            yield()
                            store.saveGameState(
                                sessionId = "new-valid-session",
                                gameMode = GameMode.SOLO,
                                players = newPlayers,
                                randomRouletteEnabled = false,
                                randomRouletteTriggerType = RandomRouletteTriggerType.FIXED,
                                randomRouletteFixedThreshold = GameState.DEFAULT_RANDOM_ROULETTE_THRESHOLD,
                            )
                        },
                    ).awaitAll()
                }

                val preferences = dataStore.data.first()
                val sessionId = preferences[AppPreferencesDataStore.SESSION_ID_KEY]
                val playersRaw = preferences[AppPreferencesDataStore.PLAYERS_KEY]
                val hasActiveGame = preferences[AppPreferencesDataStore.HAS_ACTIVE_GAME_KEY] == true
                val decodedState = store.decodedGameState.first()
                val decodedGame = (decodedState as PersistenceReadState.Data).value

                val isNewValidGame = hasActiveGame &&
                    sessionId == "new-valid-session" &&
                    playersRaw != "{not-json" &&
                    decodedGame.isDecodeValid &&
                    GameStateValidator.isValid(decodedGame.gameState) &&
                    decodedGame.gameState.sessionId == "new-valid-session"

                assertTrue(
                    "stale restore must not wipe a new game that already committed, " +
                        "was active=$hasActiveGame session=$sessionId players=$playersRaw",
                    isNewValidGame,
                )
            } finally {
                dataStoreJob.cancel()
            }
        }

    @Test
    fun givenAbsentHistoryAndParticipantsKeys_whenCollecting_thenEmitsMissingNotDataOrCorrupted() =
        runTest {
            val (dataStore, dataStoreJob) = createTemporaryPreferencesDataStore()
            val store = AppPreferencesDataStore(
                dataStore = dataStore,
                logger = NoOpPersistenceLogger,
            )

            try {
                assertEquals(PersistenceReadState.Missing, store.soloHistory.first())
                assertEquals(PersistenceReadState.Missing, store.groupHistory.first())
                assertEquals(PersistenceReadState.Missing, store.participants.first())
                assertNotEquals(
                    PersistenceReadState.Data(emptyList<SoloGameHistoryEntry>()),
                    store.soloHistory.first(),
                )
                assertNotEquals(
                    PersistenceReadState.Data(emptyList<GroupGameHistoryEntry>()),
                    store.groupHistory.first(),
                )
                assertNotEquals(
                    PersistenceReadState.Data(emptyList<String>()),
                    store.participants.first(),
                )
                assertNotEquals(PersistenceReadState.Corrupted, store.soloHistory.first())
                assertNotEquals(PersistenceReadState.Corrupted, store.groupHistory.first())
                assertNotEquals(PersistenceReadState.Corrupted, store.participants.first())
            } finally {
                dataStoreJob.cancel()
            }
        }

    @Test
    fun givenValidEmptyJsonArrays_whenCollectingHistoryAndParticipants_thenEmitsDataEmptyList() =
        runTest {
            val (dataStore, dataStoreJob) = createTemporaryPreferencesDataStore()
            val store = AppPreferencesDataStore(
                dataStore = dataStore,
                logger = NoOpPersistenceLogger,
            )

            try {
                dataStore.edit { preferences ->
                    preferences[AppPreferencesDataStore.SOLO_HISTORY_KEY] = "[]"
                    preferences[AppPreferencesDataStore.GROUP_HISTORY_KEY] = "[]"
                    preferences[AppPreferencesDataStore.PARTICIPANTS_KEY] = "[]"
                }

                assertEquals(
                    PersistenceReadState.Data(emptyList<SoloGameHistoryEntry>()),
                    store.soloHistory.first(),
                )
                assertEquals(
                    PersistenceReadState.Data(emptyList<GroupGameHistoryEntry>()),
                    store.groupHistory.first(),
                )
                assertEquals(
                    PersistenceReadState.Data(emptyList<String>()),
                    store.participants.first(),
                )
            } finally {
                dataStoreJob.cancel()
            }
        }

    @Test
    fun givenValidHistoryAndParticipantsJson_whenCollecting_thenEmitsDataEntries() = runTest {
        val (dataStore, dataStoreJob) = createTemporaryPreferencesDataStore()
        val store = AppPreferencesDataStore(
            dataStore = dataStore,
            logger = NoOpPersistenceLogger,
        )

        try {
            dataStore.edit { preferences ->
                preferences[AppPreferencesDataStore.SOLO_HISTORY_KEY] = VALID_SOLO_HISTORY_JSON
                preferences[AppPreferencesDataStore.GROUP_HISTORY_KEY] = VALID_GROUP_HISTORY_JSON
                preferences[AppPreferencesDataStore.PARTICIPANTS_KEY] = VALID_PARTICIPANTS_JSON
            }

            assertEquals(
                PersistenceReadState.Data(listOf(VALID_SOLO_HISTORY_ENTRY)),
                store.soloHistory.first(),
            )
            assertEquals(
                PersistenceReadState.Data(listOf(VALID_GROUP_HISTORY_ENTRY)),
                store.groupHistory.first(),
            )
            assertEquals(
                PersistenceReadState.Data(listOf("Ana", "Luis")),
                store.participants.first(),
            )
        } finally {
            dataStoreJob.cancel()
        }
    }

    @Test
    fun givenMalformedHistoryAndParticipantsJson_whenCollecting_thenEmitsCorruptedNotDataEmptyList() =
        runTest {
            val (dataStore, dataStoreJob) = createTemporaryPreferencesDataStore()
            val logger = RecordingPersistenceLogger()
            val store = AppPreferencesDataStore(
                dataStore = dataStore,
                logger = logger,
            )

            try {
                dataStore.edit { preferences ->
                    preferences[AppPreferencesDataStore.SOLO_HISTORY_KEY] = "{"
                    preferences[AppPreferencesDataStore.GROUP_HISTORY_KEY] = "not-json"
                    preferences[AppPreferencesDataStore.PARTICIPANTS_KEY] = "{"
                }

                assertEquals(PersistenceReadState.Corrupted, store.soloHistory.first())
                assertEquals(PersistenceReadState.Corrupted, store.groupHistory.first())
                assertEquals(PersistenceReadState.Corrupted, store.participants.first())
                assertNotEquals(
                    PersistenceReadState.Data(emptyList<SoloGameHistoryEntry>()),
                    store.soloHistory.first(),
                )
                assertNotEquals(
                    PersistenceReadState.Data(emptyList<GroupGameHistoryEntry>()),
                    store.groupHistory.first(),
                )
                assertNotEquals(
                    PersistenceReadState.Data(emptyList<String>()),
                    store.participants.first(),
                )
                assertTrue(
                    logger.failures.any { failure ->
                        failure.operation == "decodeSoloHistory" &&
                            failure.errorClassName == "JSONException"
                    },
                )
                assertTrue(
                    logger.failures.any { failure ->
                        failure.operation == "decodeGroupHistory" &&
                            failure.errorClassName == "JSONException"
                    },
                )
                assertTrue(
                    logger.failures.any { failure ->
                        failure.operation == "decodeParticipants" &&
                            failure.errorClassName == "JSONException"
                    },
                )
            } finally {
                dataStoreJob.cancel()
            }
        }

    @Test
    fun givenEmptyStore_whenReadingOnboardingState_thenEmitsMissing() = runTest {
        val (dataStore, dataStoreJob) = createTemporaryPreferencesDataStore()
        val store = AppPreferencesDataStore(
            dataStore = dataStore,
            logger = NoOpPersistenceLogger,
        )

        try {
            assertEquals(PersistenceReadState.Missing, store.hasCompletedOnboardingState.first())
        } finally {
            dataStoreJob.cancel()
        }
    }

    @Test
    fun givenOnboardingCompleted_whenPersisted_thenEmitsDataTrue() = runTest {
        val (dataStore, dataStoreJob) = createTemporaryPreferencesDataStore()
        val store = AppPreferencesDataStore(
            dataStore = dataStore,
            logger = NoOpPersistenceLogger,
        )

        try {
            store.setOnboardingCompleted()
            assertEquals(PersistenceReadState.Data(true), store.hasCompletedOnboardingState.first())
        } finally {
            dataStoreJob.cancel()
        }
    }

    @Test
    fun givenRestoredPreferencesWithoutInstallMarker_whenPreparingAfterBackup_thenClearsActiveGameAndCreatesMarker() =
        runTest {
            val root = temporaryFolder.newFolder("backup_restore")
            val preferencesFile = File(
                root,
                "${AppPreferencesDataStore.PREFERENCES_DATASTORE_DIR}/${AppPreferencesDataStore.PREFERENCES_FILE_NAME}",
            )
            preferencesFile.parentFile?.mkdirs()

            val markerFile = File(root, AppPreferencesDataStore.BACKUP_INSTALL_MARKER_RELATIVE_PATH)
            val (dataStore, dataStoreJob) = createTemporaryPreferencesDataStoreAt(preferencesFile)
            val store = AppPreferencesDataStore(
                dataStore = dataStore,
                logger = NoOpPersistenceLogger,
                installMarkerFile = markerFile,
                preferencesFile = preferencesFile,
            )

            try {
                store.saveGameState(
                    sessionId = "restored-session",
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
                    randomRouletteFixedThreshold = GameState.DEFAULT_RANDOM_ROULETTE_THRESHOLD,
                )
                store.setThemeMode(AppThemeMode.DARK)
                assertTrue(preferencesFile.exists())
                assertTrue(preferencesFile.length() > 0L)

                store.clearActiveGameAfterBackupRestoreIfNeeded()

                val restored = store.decodedGameState.first()
                assertTrue(restored is PersistenceReadState.Data<*>)
                val gameState = (restored as PersistenceReadState.Data<DecodedGameState>).value.gameState
                assertFalse(gameState.hasActiveGame)
                assertEquals(AppThemeMode.DARK, store.themeMode.first())
                assertTrue(markerFile.exists())
            } finally {
                dataStoreJob.cancel()
            }
        }

    @Test
    fun givenExistingInstallMarker_whenPreparingAfterBackup_thenLeavesActiveGameUntouched() = runTest {
        val root = temporaryFolder.newFolder("install_marker")
        val preferencesFile = File(
            root,
            "${AppPreferencesDataStore.PREFERENCES_DATASTORE_DIR}/${AppPreferencesDataStore.PREFERENCES_FILE_NAME}",
        )
        preferencesFile.parentFile?.mkdirs()

        val markerFile = File(root, AppPreferencesDataStore.BACKUP_INSTALL_MARKER_RELATIVE_PATH)
        markerFile.parentFile?.mkdirs()
        markerFile.createNewFile()

        val (dataStore, dataStoreJob) = createTemporaryPreferencesDataStoreAt(preferencesFile)
        val store = AppPreferencesDataStore(
            dataStore = dataStore,
            logger = NoOpPersistenceLogger,
            installMarkerFile = markerFile,
            preferencesFile = preferencesFile,
        )

        try {
            store.saveGameState(
                sessionId = "current-session",
                gameMode = GameMode.SOLO,
                players = listOf(
                    Player(
                        id = AppPreferencesDataStore.SOLO_PLAYER_ID,
                        name = "",
                        sushiCount = 9,
                    ),
                ),
                randomRouletteEnabled = false,
                randomRouletteTriggerType = RandomRouletteTriggerType.FIXED,
                randomRouletteFixedThreshold = GameState.DEFAULT_RANDOM_ROULETTE_THRESHOLD,
            )

            store.clearActiveGameAfterBackupRestoreIfNeeded()

            val restored = store.decodedGameState.first()
            assertTrue(restored is PersistenceReadState.Data<*>)
            val gameState = (restored as PersistenceReadState.Data<DecodedGameState>).value.gameState
            assertTrue(gameState.hasActiveGame)
            assertEquals(9, gameState.soloCount)
        } finally {
            dataStoreJob.cancel()
        }
    }

    @Test
    fun givenFreshInstallWithoutPreferencesFile_whenPreparingAfterBackup_thenCreatesMarkerOnly() = runTest {
        val root = temporaryFolder.newFolder("fresh_install")
        val preferencesFile = File(
            root,
            "${AppPreferencesDataStore.PREFERENCES_DATASTORE_DIR}/${AppPreferencesDataStore.PREFERENCES_FILE_NAME}",
        )
        val markerFile = File(root, AppPreferencesDataStore.BACKUP_INSTALL_MARKER_RELATIVE_PATH)

        val (dataStore, dataStoreJob) = createTemporaryPreferencesDataStoreAt(preferencesFile)
        val store = AppPreferencesDataStore(
            dataStore = dataStore,
            logger = NoOpPersistenceLogger,
            installMarkerFile = markerFile,
            preferencesFile = preferencesFile,
        )

        try {
            store.clearActiveGameAfterBackupRestoreIfNeeded()

            assertTrue(markerFile.exists())
            val decodedState = store.decodedGameState.first()
            assertTrue(decodedState is PersistenceReadState.Data<*>)
            val gameState = (decodedState as PersistenceReadState.Data<DecodedGameState>).value.gameState
            assertFalse(gameState.hasActiveGame)
        } finally {
            dataStoreJob.cancel()
        }
    }

    @Test
    fun givenReadIOException_whenCollectingHistoryAndParticipants_thenEmitsUnavailableThenRecovers() =
        runTest {
            assertUnavailableThenMissing(createHistoryParticipantsStore()) { it.soloHistory }
            assertUnavailableThenMissing(createHistoryParticipantsStore()) { it.groupHistory }
            assertUnavailableThenMissing(createHistoryParticipantsStore()) { it.participants }
            assertUnavailableThenMissing(createHistoryParticipantsStore()) { it.hasCompletedOnboardingState }
        }

    private fun TestScope.createHistoryParticipantsStore(): AppPreferencesDataStore {
        return AppPreferencesDataStore(
            dataStore = FirstReadFailingDataStore(),
            logger = NoOpPersistenceLogger,
        )
    }

    private suspend fun TestScope.assertUnavailableThenMissing(
        store: AppPreferencesDataStore,
        flow: (AppPreferencesDataStore) -> Flow<PersistenceReadState<*>>,
    ) {
        flow(store).test {
            assertEquals(PersistenceReadState.Unavailable, awaitItem())
            testScheduler.advanceTimeBy(1_000L)
            assertEquals(PersistenceReadState.Missing, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun TestScope.createTemporaryPreferencesDataStore(): Pair<DataStore<Preferences>, Job> {
        val preferencesFile = File(temporaryFolder.root, "sushi_counter_preferences.preferences_pb")
        return createTemporaryPreferencesDataStoreAt(preferencesFile)
    }

    private fun TestScope.createTemporaryPreferencesDataStoreAt(
        preferencesFile: File,
    ): Pair<DataStore<Preferences>, Job> {
        val dataStoreJob = Job(coroutineContext[Job])
        val dataStore = PreferenceDataStoreFactory.create(
            scope = CoroutineScope(coroutineContext + dataStoreJob),
            produceFile = { preferencesFile },
        )
        return dataStore to dataStoreJob
    }

    private fun assertInactiveRestored(result: RestoreGamePersistenceResult) {
        assertTrue(result is RestoreGamePersistenceResult.Restored)
        val restored = (result as RestoreGamePersistenceResult.Restored).gameState
        assertFalse(restored.hasActiveGame)
    }

    private suspend fun DataStore<Preferences>.writeUnrelatedPreferences() {
        edit { preferences ->
            preferences[AppPreferencesDataStore.SOLO_HISTORY_KEY] = UNRELATED_SOLO_HISTORY
            preferences[AppPreferencesDataStore.GROUP_HISTORY_KEY] = UNRELATED_GROUP_HISTORY
            preferences[AppPreferencesDataStore.THEME_MODE_KEY] = AppThemeMode.DARK.name
            preferences[AppPreferencesDataStore.HAS_COMPLETED_ONBOARDING_KEY] = true
        }
    }

    private suspend fun DataStore<Preferences>.assertUnrelatedPreferencesUnchanged() {
        val preferences = data.first()
        assertEquals(UNRELATED_SOLO_HISTORY, preferences[AppPreferencesDataStore.SOLO_HISTORY_KEY])
        assertEquals(UNRELATED_GROUP_HISTORY, preferences[AppPreferencesDataStore.GROUP_HISTORY_KEY])
        assertEquals(AppThemeMode.DARK.name, preferences[AppPreferencesDataStore.THEME_MODE_KEY])
        assertEquals(true, preferences[AppPreferencesDataStore.HAS_COMPLETED_ONBOARDING_KEY])
    }

    private class RecordingPersistenceLogger : PersistenceLogger {
        data class Failure(
            val operation: String,
            val errorClassName: String,
        )

        val failures = mutableListOf<Failure>()

        override fun logFailure(operation: String, errorClassName: String) {
            failures += Failure(operation, errorClassName)
        }
    }

    private class AlwaysFailingDataStore : DataStore<Preferences> {
        override val data: Flow<Preferences> = flow {
            throw IOException("disk unavailable")
        }

        override suspend fun updateData(
            transform: suspend (t: Preferences) -> Preferences,
        ): Preferences = transform(emptyPreferences())
    }

    private class FirstReadFailingDataStore : DataStore<Preferences> {
        private var remainingFailures = 1

        override val data: Flow<Preferences> = flow {
            if (remainingFailures > 0) {
                remainingFailures--
                throw IOException("disk")
            }
            emit(emptyPreferences())
        }

        override suspend fun updateData(
            transform: suspend (t: Preferences) -> Preferences,
        ): Preferences = transform(emptyPreferences())
    }

    private class IllegalStateThrowingDataStore : DataStore<Preferences> {
        override val data: Flow<Preferences> = flow {
            throw IllegalStateException("unexpected")
        }

        override suspend fun updateData(
            transform: suspend (t: Preferences) -> Preferences,
        ): Preferences = transform(emptyPreferences())
    }

    private class IoFailingPreferencesDataStore(
        initialPreferences: Preferences = emptyPreferences(),
    ) : DataStore<Preferences> {
        var failReads: Boolean = true
        var failUpdates: Boolean = true
        private val stored = MutableStateFlow(initialPreferences)

        val storedPreferences: Preferences
            get() = stored.value

        override val data: Flow<Preferences> = flow {
            if (failReads) {
                throw IOException("disk unavailable")
            }
            emitAll(stored)
        }

        override suspend fun updateData(
            transform: suspend (t: Preferences) -> Preferences,
        ): Preferences {
            if (failUpdates) {
                throw IOException("disk unavailable")
            }
            val updated = transform(stored.value)
            stored.value = updated
            return updated
        }
    }

    private companion object {
        const val UNRELATED_SOLO_HISTORY = """[{"id":"solo-keep"}]"""
        const val UNRELATED_GROUP_HISTORY = """[{"id":"group-keep"}]"""
        const val VALID_SOLO_PLAYERS_JSON =
            """[{"id":"solo_player","name":"","sushiCount":4,"lastRandomRouletteTrigger":0}]"""
        const val VALID_SOLO_HISTORY_JSON =
            """[{"id":"solo-1","date":100,"totalSushi":5,"randomRouletteEnabled":true,"randomRouletteMode":"RANDOM"}]"""
        const val VALID_GROUP_HISTORY_JSON =
            """[{"id":"group-1","date":200,"players":[{"playerName":"Ana","sushiCount":3}],"randomRouletteEnabled":false,"randomRouletteMode":null}]"""
        const val VALID_PARTICIPANTS_JSON = """["Ana","Luis"]"""
        val VALID_SOLO_HISTORY_ENTRY = SoloGameHistoryEntry(
            id = "solo-1",
            date = 100L,
            totalSushi = 5,
            randomRouletteEnabled = true,
            randomRouletteMode = "RANDOM",
        )
        val VALID_GROUP_HISTORY_ENTRY = GroupGameHistoryEntry(
            id = "group-1",
            date = 200L,
            players = listOf(PlayerScore(playerName = "Ana", sushiCount = 3)),
            randomRouletteEnabled = false,
            randomRouletteMode = null,
        )
    }
}
