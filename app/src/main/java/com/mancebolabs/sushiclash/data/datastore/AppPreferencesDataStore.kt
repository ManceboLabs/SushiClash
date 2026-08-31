package com.mancebolabs.sushiclash.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mancebolabs.sushiclash.domain.model.AppThemeMode
import com.mancebolabs.sushiclash.domain.model.FeedbackSettingsDefaults
import com.mancebolabs.sushiclash.domain.frequentplayer.FrequentPlayersMerger
import com.mancebolabs.sushiclash.domain.model.FrequentPlayer
import com.mancebolabs.sushiclash.domain.model.achievement.AchievementId
import com.mancebolabs.sushiclash.domain.model.achievement.AchievementPersistenceState
import com.mancebolabs.sushiclash.domain.model.GameMode
import com.mancebolabs.sushiclash.domain.model.GameSetupRules
import com.mancebolabs.sushiclash.domain.model.GameState
import com.mancebolabs.sushiclash.domain.model.GameStateValidator
import com.mancebolabs.sushiclash.domain.model.GroupGameHistoryEntry
import com.mancebolabs.sushiclash.domain.model.IncrementResult
import com.mancebolabs.sushiclash.domain.model.PersistenceReadState
import com.mancebolabs.sushiclash.domain.model.Player
import com.mancebolabs.sushiclash.domain.model.PlayerScore
import com.mancebolabs.sushiclash.domain.model.RandomRouletteTriggerType
import com.mancebolabs.sushiclash.domain.model.SoloGameHistoryEntry
import java.io.File
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.sushiClashPreferences: DataStore<Preferences> by preferencesDataStore(
    name = "sushi_counter_preferences",
)

data class DecodedGameState(
    val gameState: GameState,
    val isDecodeValid: Boolean = true,
)

enum class FinishGamePersistenceResult {
    Saved,
    NoActiveGame,
    InvalidActiveGame,
    CorruptHistory,
}

sealed interface RestoreGamePersistenceResult {
    data class Restored(val gameState: GameState) : RestoreGamePersistenceResult
    data object Unavailable : RestoreGamePersistenceResult
}

internal data class DecodedHistory<T>(
    val entries: List<T>,
    val isValid: Boolean,
)

internal fun <T> PersistenceReadState<List<T>>.toDecodedHistory(): DecodedHistory<T> {
    return when (this) {
        PersistenceReadState.Missing -> DecodedHistory(entries = emptyList(), isValid = true)
        is PersistenceReadState.Data -> DecodedHistory(entries = value, isValid = true)
        PersistenceReadState.Corrupted,
        PersistenceReadState.Unavailable -> DecodedHistory(entries = emptyList(), isValid = false)
    }
}

internal fun mapInvalidDecodedGameResult(
    decodedState: DecodedGameState,
): FinishGamePersistenceResult? {
    if (decodedState.isDecodeValid) return null
    return if (decodedState.gameState.hasActiveGame) {
        FinishGamePersistenceResult.InvalidActiveGame
    } else {
        FinishGamePersistenceResult.NoActiveGame
    }
}

internal sealed interface FinishedGameHistoryUpdate {
    data class Solo(
        val history: List<SoloGameHistoryEntry>,
    ) : FinishedGameHistoryUpdate

    data class Group(
        val history: List<GroupGameHistoryEntry>,
    ) : FinishedGameHistoryUpdate

    data object NoActiveGame : FinishedGameHistoryUpdate

    data object InvalidActiveGame : FinishedGameHistoryUpdate

    data object CorruptHistory : FinishedGameHistoryUpdate
}

internal fun buildFinishedGameHistoryUpdate(
    gameState: GameState,
    legacySessionId: String,
    finishedAt: Long,
    decodeSoloHistory: () -> DecodedHistory<SoloGameHistoryEntry>,
    decodeGroupHistory: () -> DecodedHistory<GroupGameHistoryEntry>,
): FinishedGameHistoryUpdate {
    if (!gameState.hasActiveGame) {
        return FinishedGameHistoryUpdate.NoActiveGame
    }
    if (!GameStateValidator.isValid(gameState)) {
        return FinishedGameHistoryUpdate.InvalidActiveGame
    }
    val sessionId = gameState.sessionId?.takeIf(String::isNotBlank)
        ?: legacySessionId.takeIf(String::isNotBlank)
        ?: return FinishedGameHistoryUpdate.InvalidActiveGame

    val rouletteMode = gameState.randomRouletteTriggerType.name
        .takeIf { gameState.randomRouletteEnabled }
    return when (gameState.gameMode) {
        GameMode.SOLO -> {
            val decodedHistory = decodeSoloHistory()
            if (!decodedHistory.isValid) return FinishedGameHistoryUpdate.CorruptHistory
            if (decodedHistory.entries.any { it.id == sessionId }) {
                return FinishedGameHistoryUpdate.Solo(decodedHistory.entries)
            }
            FinishedGameHistoryUpdate.Solo(
                history = listOf(
                    SoloGameHistoryEntry(
                        id = sessionId,
                        date = finishedAt,
                        totalSushi = gameState.soloCount,
                        randomRouletteEnabled = gameState.randomRouletteEnabled,
                        randomRouletteMode = rouletteMode,
                    ),
                ) + decodedHistory.entries,
            )
        }
        GameMode.GROUP -> {
            val decodedHistory = decodeGroupHistory()
            if (!decodedHistory.isValid) return FinishedGameHistoryUpdate.CorruptHistory
            if (decodedHistory.entries.any { it.id == sessionId }) {
                return FinishedGameHistoryUpdate.Group(decodedHistory.entries)
            }
            FinishedGameHistoryUpdate.Group(
                history = listOf(
                    GroupGameHistoryEntry(
                        id = sessionId,
                        date = finishedAt,
                        players = gameState.players.map { player ->
                            PlayerScore(
                                playerName = player.name,
                                sushiCount = player.sushiCount,
                            )
                        },
                        randomRouletteEnabled = gameState.randomRouletteEnabled,
                        randomRouletteMode = rouletteMode,
                    ),
                ) + decodedHistory.entries,
            )
        }
        null -> FinishedGameHistoryUpdate.InvalidActiveGame
    }
}

class AppPreferencesDataStore(
    private val dataStore: DataStore<Preferences>,
    private val logger: PersistenceLogger,
    private val installMarkerFile: File? = null,
    private val preferencesFile: File? = null,
) {
    constructor(context: Context) : this(
        dataStore = context.applicationContext.sushiClashPreferences,
        logger = AndroidPersistenceLogger(),
        installMarkerFile = File(
            context.applicationContext.filesDir,
            BACKUP_INSTALL_MARKER_RELATIVE_PATH,
        ),
        preferencesFile = File(
            context.applicationContext.filesDir,
            "$PREFERENCES_DATASTORE_DIR/$PREFERENCES_FILE_NAME",
        ),
    )

    val decodedGameState: Flow<PersistenceReadState<DecodedGameState>> = dataStore.data
        .mapWithPersistenceReadState(
            logger = logger,
            operation = "readGameState",
        ) { preferences ->
            PersistenceReadState.Data(decodeGameState(preferences))
        }

    private fun decodeGameState(preferences: Preferences): DecodedGameState {
        // Prefer has_active_game; fall back to legacy has_completed_setup for migration.
        val hasActiveGame = preferences[HAS_ACTIVE_GAME_KEY]
            ?: preferences[HAS_COMPLETED_SETUP_KEY]
            ?: false

        if (!hasActiveGame) {
            return DecodedGameState(GameState(hasActiveGame = false))
        }

        val storedMode = preferences[GAME_MODE_KEY]
        val gameMode = storedMode?.let {
            runCatching { GameMode.valueOf(it) }.getOrNull()
        }
        val decodedPlayers = decodePlayers(preferences[PLAYERS_KEY])

        val storedTriggerType = preferences[RANDOM_ROULETTE_TRIGGER_TYPE_KEY]
        val decodedTriggerType = storedTriggerType?.let {
            runCatching { RandomRouletteTriggerType.valueOf(it) }.getOrNull()
        } ?: RandomRouletteTriggerType.FIXED
        val fixedThreshold = (preferences[RANDOM_ROULETTE_FIXED_THRESHOLD_KEY]
            ?: preferences[RANDOM_ROULETTE_THRESHOLD_KEY]
            ?: GameState.DEFAULT_RANDOM_ROULETTE_THRESHOLD)

        return DecodedGameState(
            gameState = GameState(
                hasActiveGame = true,
                sessionId = preferences[SESSION_ID_KEY],
                gameMode = gameMode,
                players = decodedPlayers.players,
                randomRouletteEnabled = preferences[RANDOM_ROULETTE_ENABLED_KEY] ?: false,
                randomRouletteTriggerType = decodedTriggerType,
                randomRouletteFixedThreshold = fixedThreshold,
            ),
            isDecodeValid = decodedPlayers.isValid &&
                (storedMode == null || gameMode != null) &&
                (storedTriggerType == null ||
                    runCatching { RandomRouletteTriggerType.valueOf(storedTriggerType) }.isSuccess),
        )
    }

    val participants: Flow<PersistenceReadState<List<String>>> = dataStore.data
        .mapWithPersistenceReadState(
            logger = logger,
            operation = "readParticipants",
        ) { preferences ->
            decodeParticipants(preferences[PARTICIPANTS_KEY])
        }

    internal val frequentPlayersFlow: Flow<PersistenceReadState<List<FrequentPlayer>>> = dataStore.data
        .mapWithPersistenceReadState(
            logger = logger,
            operation = "readFrequentPlayers",
        ) { preferences ->
            decodeFrequentPlayers(preferences[FREQUENT_PLAYERS_KEY])
        }

    val frequentPlayers: Flow<List<FrequentPlayer>> = frequentPlayersFlow.map { state ->
        when (state) {
            is PersistenceReadState.Data -> state.value
            PersistenceReadState.Missing,
            PersistenceReadState.Corrupted,
            PersistenceReadState.Unavailable -> emptyList()
        }
    }

    internal val themeModeState: Flow<PersistenceReadState<AppThemeMode>> = dataStore.data
        .mapWithPersistenceReadState(
            logger = logger,
            operation = "readThemeMode",
        ) { preferences ->
            val storedTheme = preferences[THEME_MODE_KEY]
                ?: return@mapWithPersistenceReadState PersistenceReadState.Missing
            val themeMode = runCatching { AppThemeMode.valueOf(storedTheme) }.getOrNull()
                ?: return@mapWithPersistenceReadState PersistenceReadState.Corrupted
            PersistenceReadState.Data(themeMode)
        }

    // Keep Flow<AppThemeMode> for ThemeRepository; typed themeModeState preserves Missing/Corrupted/Unavailable.
    val themeMode: Flow<AppThemeMode> = themeModeState.map { state ->
        when (state) {
            is PersistenceReadState.Data -> state.value
            PersistenceReadState.Missing,
            PersistenceReadState.Corrupted,
            PersistenceReadState.Unavailable -> AppThemeMode.LIGHT
        }
    }

    internal val soundEnabledState: Flow<PersistenceReadState<Boolean>> = dataStore.data
        .mapWithPersistenceReadState(
            logger = logger,
            operation = "readSoundEnabled",
        ) { preferences ->
            val stored = preferences[SOUND_ENABLED_KEY]
                ?: return@mapWithPersistenceReadState PersistenceReadState.Missing
            PersistenceReadState.Data(stored)
        }

    val soundEnabled: Flow<Boolean> = soundEnabledState.map { state ->
        when (state) {
            is PersistenceReadState.Data -> state.value
            PersistenceReadState.Missing,
            PersistenceReadState.Corrupted,
            PersistenceReadState.Unavailable -> FeedbackSettingsDefaults.SOUND_ENABLED
        }
    }

    internal val vibrationEnabledState: Flow<PersistenceReadState<Boolean>> = dataStore.data
        .mapWithPersistenceReadState(
            logger = logger,
            operation = "readVibrationEnabled",
        ) { preferences ->
            val stored = preferences[VIBRATION_ENABLED_KEY]
                ?: return@mapWithPersistenceReadState PersistenceReadState.Missing
            PersistenceReadState.Data(stored)
        }

    val vibrationEnabled: Flow<Boolean> = vibrationEnabledState.map { state ->
        when (state) {
            is PersistenceReadState.Data -> state.value
            PersistenceReadState.Missing,
            PersistenceReadState.Corrupted,
            PersistenceReadState.Unavailable -> FeedbackSettingsDefaults.VIBRATION_ENABLED
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SOUND_ENABLED_KEY] = enabled
        }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[VIBRATION_ENABLED_KEY] = enabled
        }
    }

    internal val achievementStateFlow: Flow<PersistenceReadState<AchievementPersistenceState>> = dataStore.data
        .mapWithPersistenceReadState(
            logger = logger,
            operation = "readAchievementState",
        ) { preferences ->
            decodeAchievementState(preferences[ACHIEVEMENT_STATE_KEY])
        }

    val achievementState: Flow<AchievementPersistenceState> = achievementStateFlow.map { state ->
        when (state) {
            is PersistenceReadState.Data -> state.value
            PersistenceReadState.Missing,
            PersistenceReadState.Corrupted,
            PersistenceReadState.Unavailable -> AchievementPersistenceState()
        }
    }

    suspend fun setAchievementState(state: AchievementPersistenceState) {
        dataStore.edit { preferences ->
            preferences[ACHIEVEMENT_STATE_KEY] = encodeAchievementState(state)
        }
    }

    val soloHistory: Flow<PersistenceReadState<List<SoloGameHistoryEntry>>> = dataStore.data
        .mapWithPersistenceReadState(
            logger = logger,
            operation = "readSoloHistory",
        ) { preferences ->
            decodeSoloHistory(preferences[SOLO_HISTORY_KEY])
        }

    val groupHistory: Flow<PersistenceReadState<List<GroupGameHistoryEntry>>> = dataStore.data
        .mapWithPersistenceReadState(
            logger = logger,
            operation = "readGroupHistory",
        ) { preferences ->
            decodeGroupHistory(preferences[GROUP_HISTORY_KEY])
        }

    val hasCompletedOnboardingState: Flow<PersistenceReadState<Boolean>> = dataStore.data
        .mapWithPersistenceReadState(
            logger = logger,
            operation = "readOnboarding",
        ) { preferences ->
            val stored = preferences[HAS_COMPLETED_ONBOARDING_KEY]
                ?: return@mapWithPersistenceReadState PersistenceReadState.Missing
            PersistenceReadState.Data(stored)
        }

    val hasCompletedOnboarding: Flow<Boolean> = hasCompletedOnboardingState.map { state ->
        when (state) {
            is PersistenceReadState.Data -> state.value
            PersistenceReadState.Missing,
            PersistenceReadState.Corrupted,
            PersistenceReadState.Unavailable -> false
        }
    }

    suspend fun setOnboardingCompleted() {
        dataStore.edit { preferences ->
            preferences[HAS_COMPLETED_ONBOARDING_KEY] = true
        }
    }

    suspend fun setThemeMode(themeMode: AppThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = themeMode.name
        }
    }

    suspend fun saveGameState(
        sessionId: String,
        gameMode: GameMode,
        players: List<Player>,
        randomRouletteEnabled: Boolean,
        randomRouletteTriggerType: RandomRouletteTriggerType,
        randomRouletteFixedThreshold: Int,
    ) {
        dataStore.edit { preferences ->
            preferences[HAS_ACTIVE_GAME_KEY] = true
            preferences[HAS_COMPLETED_SETUP_KEY] = true
            preferences[SESSION_ID_KEY] = sessionId
            preferences[GAME_MODE_KEY] = gameMode.name
            preferences[PLAYERS_KEY] = encodePlayers(players)
            preferences[RANDOM_ROULETTE_ENABLED_KEY] = randomRouletteEnabled
            preferences[RANDOM_ROULETTE_TRIGGER_TYPE_KEY] = randomRouletteTriggerType.name
            preferences[RANDOM_ROULETTE_FIXED_THRESHOLD_KEY] = randomRouletteFixedThreshold.coerceIn(
                GameState.MIN_RANDOM_ROULETTE_THRESHOLD,
                GameState.MAX_RANDOM_ROULETTE_THRESHOLD,
            )
        }
    }

    /**
     * After backup/restore the preferences file may contain a stale in-progress game.
     * A device-local marker (excluded from backup) detects the first launch on this install.
     */
    suspend fun clearActiveGameAfterBackupRestoreIfNeeded() {
        val markerFile = installMarkerFile ?: return
        if (markerFile.exists()) return

        val restoredPreferences = preferencesFile
        if (restoredPreferences != null && restoredPreferences.exists() && restoredPreferences.length() > 0L) {
            clearActiveGame()
        }

        markerFile.parentFile?.mkdirs()
        markerFile.createNewFile()
    }

    suspend fun restoreGameState(migratedSessionId: String): RestoreGamePersistenceResult {
        var restoredState = GameState()
        try {
            dataStore.edit { preferences ->
                restoredState = restoreGameState(
                    preferences = preferences,
                    decodedState = decodeGameState(preferences),
                    migratedSessionId = migratedSessionId,
                )
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: IOException) {
            // Disk I/O is not proof of corruption; leave unknown bytes untouched.
            logger.logFailure("restoreGameState", error::class.java.simpleName)
            return RestoreGamePersistenceResult.Unavailable
        }
        return RestoreGamePersistenceResult.Restored(restoredState)
    }

    internal fun restoreGameState(
        preferences: MutablePreferences,
        decodedState: DecodedGameState,
        migratedSessionId: String,
    ): GameState {
        val safeGameState = if (
            decodedState.isDecodeValid &&
            GameStateValidator.isValid(decodedState.gameState)
        ) {
            decodedState.gameState
        } else {
            GameState()
        }

        if (decodedState.gameState.hasActiveGame && !safeGameState.hasActiveGame) {
            logger.logFailure("restoreGameState", "InvalidActiveGame")
            clearActiveGameKeys(preferences)
            return safeGameState
        }
        if (safeGameState.hasActiveGame && safeGameState.sessionId.isNullOrBlank()) {
            preferences[SESSION_ID_KEY] = migratedSessionId
            return safeGameState.copy(sessionId = migratedSessionId)
        }
        return safeGameState
    }

    suspend fun finishGameWithSaving(
        legacySessionId: String,
        finishedAt: Long,
    ): FinishGamePersistenceResult {
        var result = FinishGamePersistenceResult.NoActiveGame
        try {
            dataStore.edit { preferences ->
                val decodedState = decodeGameState(preferences)
                mapInvalidDecodedGameResult(decodedState)?.let { invalidDecodeResult ->
                    logger.logFailure("finishGameWithSaving", invalidDecodeResult.name)
                    result = invalidDecodeResult
                    return@edit
                }
                val update = buildFinishedGameHistoryUpdate(
                    gameState = decodedState.gameState,
                    legacySessionId = legacySessionId,
                    finishedAt = finishedAt,
                    decodeSoloHistory = {
                        decodeSoloHistory(preferences[SOLO_HISTORY_KEY]).toDecodedHistory()
                    },
                    decodeGroupHistory = {
                        decodeGroupHistory(preferences[GROUP_HISTORY_KEY]).toDecodedHistory()
                    },
                )

                when (update) {
                    FinishedGameHistoryUpdate.NoActiveGame -> Unit
                    FinishedGameHistoryUpdate.InvalidActiveGame -> {
                        logger.logFailure("finishGameWithSaving", "InvalidActiveGame")
                        result = FinishGamePersistenceResult.InvalidActiveGame
                    }
                    FinishedGameHistoryUpdate.CorruptHistory -> {
                        logger.logFailure("finishGameWithSaving", "CorruptHistory")
                        result = FinishGamePersistenceResult.CorruptHistory
                    }
                    is FinishedGameHistoryUpdate.Solo -> {
                        preferences[SOLO_HISTORY_KEY] = encodeSoloHistory(update.history)
                        clearActiveGameKeys(preferences)
                        result = FinishGamePersistenceResult.Saved
                    }
                    is FinishedGameHistoryUpdate.Group -> {
                        preferences[GROUP_HISTORY_KEY] = encodeGroupHistory(update.history)
                        mergeFrequentPlayersFromGroupGame(
                            preferences = preferences,
                            playerNames = decodedState.gameState.players.map { it.name },
                        )
                        clearActiveGameKeys(preferences)
                        result = FinishGamePersistenceResult.Saved
                    }
                }
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: IOException) {
            logger.logFailure("finishGameWithSaving", error::class.java.simpleName)
            throw error
        }
        return result
    }

    suspend fun clearActiveGame() {
        dataStore.edit { preferences ->
            clearActiveGameKeys(preferences)
        }
    }

    internal fun clearActiveGameKeys(preferences: MutablePreferences) {
        preferences[HAS_ACTIVE_GAME_KEY] = false
        preferences[HAS_COMPLETED_SETUP_KEY] = false
        preferences.remove(SESSION_ID_KEY)
        preferences.remove(GAME_MODE_KEY)
        preferences[PLAYERS_KEY] = "[]"
        preferences[PARTICIPANTS_KEY] = "[]"
        preferences[RANDOM_ROULETTE_ENABLED_KEY] = false
        preferences.remove(RANDOM_ROULETTE_TRIGGER_TYPE_KEY)
        preferences.remove(RANDOM_ROULETTE_FIXED_THRESHOLD_KEY)
    }

    suspend fun setPlayers(players: List<Player>) {
        dataStore.edit { preferences ->
            preferences[PLAYERS_KEY] = encodePlayers(players)
        }
    }

    suspend fun incrementPlayerCount(
        playerId: String,
        applyIncrement: (player: Player, gameState: GameState) -> Pair<Player, IncrementResult>,
    ): IncrementResult {
        var result = IncrementResult(newCount = 0, shouldTriggerRoulette = false)
        dataStore.edit { preferences ->
            val decodedState = decodeGameState(preferences)
            val gameState = decodedState.gameState
            if (
                !decodedState.isDecodeValid ||
                !gameState.hasActiveGame ||
                !GameStateValidator.isValid(gameState)
            ) {
                return@edit
            }
            val updatedPlayers = gameState.players.map { player ->
                if (player.id != playerId) {
                    player
                } else {
                    val (updatedPlayer, incrementResult) = applyIncrement(player, gameState)
                    result = incrementResult
                    updatedPlayer
                }
            }
            preferences[PLAYERS_KEY] = encodePlayers(updatedPlayers)
        }
        return result
    }

    suspend fun setParticipants(names: List<String>) {
        dataStore.edit { preferences ->
            preferences[PARTICIPANTS_KEY] = encodeParticipants(names)
        }
    }

    suspend fun clearHistory() {
        dataStore.edit { preferences ->
            preferences.remove(SOLO_HISTORY_KEY)
            preferences.remove(GROUP_HISTORY_KEY)
        }
    }

    private fun encodePlayers(players: List<Player>): String {
        val jsonArray = JSONArray()
        players.forEach { player ->
            jsonArray.put(
                JSONObject().apply {
                    put("id", player.id)
                    put("name", player.name)
                    put("sushiCount", player.sushiCount)
                    player.nextRandomRouletteTarget?.let { target ->
                        put("nextRandomRouletteTarget", target)
                    }
                    put("lastRandomRouletteTrigger", player.lastRandomRouletteTrigger)
                    player.nextChefAnimationTarget?.let { target ->
                        put("nextChefAnimationTarget", target)
                    }
                    put("lastChefAnimationTrigger", player.lastChefAnimationTrigger)
                    player.lastChefEventAnimation?.let { animation ->
                        put("lastChefEventAnimation", animation.name)
                    }
                },
            )
        }
        return jsonArray.toString()
    }

    private fun decodePlayers(raw: String?): DecodedPlayers {
        if (raw == null || raw.isBlank()) {
            return DecodedPlayers(players = emptyList(), isValid = false)
        }
        return runCatching {
            val jsonArray = JSONArray(raw)
            val players = buildList {
                for (index in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(index)
                    add(
                        Player(
                            id = item.getString("id"),
                            name = item.getString("name"),
                            sushiCount = if (item.has("sushiCount")) item.getInt("sushiCount") else 0,
                            nextRandomRouletteTarget = if (
                                item.has("nextRandomRouletteTarget") &&
                                !item.isNull("nextRandomRouletteTarget")
                            ) {
                                item.getInt("nextRandomRouletteTarget")
                            } else {
                                null
                            },
                            lastRandomRouletteTrigger = if (item.has("lastRandomRouletteTrigger")) {
                                item.getInt("lastRandomRouletteTrigger")
                            } else {
                                0
                            },
                            nextChefAnimationTarget = if (
                                item.has("nextChefAnimationTarget") &&
                                !item.isNull("nextChefAnimationTarget")
                            ) {
                                item.getInt("nextChefAnimationTarget")
                            } else {
                                null
                            },
                            lastChefAnimationTrigger = if (item.has("lastChefAnimationTrigger")) {
                                item.getInt("lastChefAnimationTrigger")
                            } else {
                                0
                            },
                            lastChefEventAnimation = if (
                                item.has("lastChefEventAnimation") &&
                                !item.isNull("lastChefEventAnimation")
                            ) {
                                runCatching {
                                    com.mancebolabs.sushiclash.domain.model.ChefEventAnimation.valueOf(
                                        item.getString("lastChefEventAnimation"),
                                    )
                                }.getOrNull()
                            } else {
                                null
                            },
                        ),
                    )
                }
            }
            DecodedPlayers(players = players, isValid = true)
        }.getOrElse {
            DecodedPlayers(players = emptyList(), isValid = false)
        }
    }

    private data class DecodedPlayers(
        val players: List<Player>,
        val isValid: Boolean,
    )

    private fun encodeParticipants(names: List<String>): String {
        val jsonArray = JSONArray()
        names.forEach { name -> jsonArray.put(name) }
        return jsonArray.toString()
    }

    private fun decodeParticipants(raw: String?): PersistenceReadState<List<String>> {
        if (raw == null) return PersistenceReadState.Missing
        return runCatching {
            val jsonArray = JSONArray(raw)
            val names = buildList {
                for (index in 0 until jsonArray.length()) {
                    add(jsonArray.getString(index))
                }
            }
            PersistenceReadState.Data(names)
        }.getOrElse { error ->
            logger.logFailure("decodeParticipants", error::class.java.simpleName)
            PersistenceReadState.Corrupted
        }
    }

    private fun encodeFrequentPlayers(players: List<FrequentPlayer>): String {
        val jsonArray = JSONArray()
        players.forEach { player ->
            jsonArray.put(
                JSONObject().apply {
                    put("id", player.id)
                    put("displayName", player.displayName)
                },
            )
        }
        return jsonArray.toString()
    }

    private fun decodeFrequentPlayers(raw: String?): PersistenceReadState<List<FrequentPlayer>> {
        if (raw == null) return PersistenceReadState.Missing
        return runCatching {
            val jsonArray = JSONArray(raw)
            val players = buildList {
                for (index in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(index)
                    val id = item.optString("id", "").trim()
                    val displayName = item.optString("displayName", "").trim()
                    if (id.isNotEmpty() && displayName.isNotEmpty()) {
                        add(FrequentPlayer(id = id, displayName = displayName))
                    }
                }
            }
            PersistenceReadState.Data(players)
        }.getOrElse { error ->
            logger.logFailure("decodeFrequentPlayers", error::class.java.simpleName)
            PersistenceReadState.Corrupted
        }
    }

    private fun mergeFrequentPlayersFromGroupGame(
        preferences: MutablePreferences,
        playerNames: List<String>,
    ) {
        val existing = when (val decoded = decodeFrequentPlayers(preferences[FREQUENT_PLAYERS_KEY])) {
            is PersistenceReadState.Data -> decoded.value
            PersistenceReadState.Missing -> emptyList()
            PersistenceReadState.Corrupted,
            PersistenceReadState.Unavailable -> return
        }
        val merged = FrequentPlayersMerger.mergeFromGroupGame(
            existing = existing,
            playerNames = playerNames,
        )
        if (merged != existing) {
            preferences[FREQUENT_PLAYERS_KEY] = encodeFrequentPlayers(merged)
        }
    }

    companion object {
        const val PREFERENCES_FILE_NAME = "sushi_counter_preferences.preferences_pb"
        const val PREFERENCES_DATASTORE_DIR = "datastore"
        const val BACKUP_INSTALL_MARKER_RELATIVE_PATH = "no_backup/install_marker"

        internal val HAS_ACTIVE_GAME_KEY = booleanPreferencesKey("has_active_game")
        internal val HAS_COMPLETED_SETUP_KEY = booleanPreferencesKey("has_completed_setup")
        internal val SESSION_ID_KEY = stringPreferencesKey("game_session_id")
        internal val GAME_MODE_KEY = stringPreferencesKey("game_mode")
        internal val PLAYERS_KEY = stringPreferencesKey("players")
        internal val PARTICIPANTS_KEY = stringPreferencesKey("participants")
        internal val FREQUENT_PLAYERS_KEY = stringPreferencesKey("frequent_players")
        internal val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        internal val RANDOM_ROULETTE_ENABLED_KEY = booleanPreferencesKey("random_roulette_enabled")
        internal val RANDOM_ROULETTE_THRESHOLD_KEY = intPreferencesKey("random_roulette_threshold")
        internal val RANDOM_ROULETTE_TRIGGER_TYPE_KEY = stringPreferencesKey("random_roulette_trigger_type")
        internal val RANDOM_ROULETTE_FIXED_THRESHOLD_KEY = intPreferencesKey("random_roulette_fixed_threshold")
        internal val SOLO_HISTORY_KEY = stringPreferencesKey("solo_history")
        internal val GROUP_HISTORY_KEY = stringPreferencesKey("group_history")
        internal val HAS_COMPLETED_ONBOARDING_KEY = booleanPreferencesKey("has_completed_onboarding")
        internal val SOUND_ENABLED_KEY = booleanPreferencesKey("sound_enabled")
        internal val VIBRATION_ENABLED_KEY = booleanPreferencesKey("vibration_enabled")
        internal val ACHIEVEMENT_STATE_KEY = stringPreferencesKey("achievement_state")
        const val SOLO_PLAYER_ID = GameStateValidator.SOLO_PLAYER_ID
        const val MAX_GROUP_PLAYERS = GameSetupRules.MAX_GROUP_PLAYERS
        const val MIN_GROUP_PLAYERS = GameSetupRules.MIN_GROUP_PLAYERS
    }

    private fun encodeSoloHistory(entries: List<SoloGameHistoryEntry>): String {
        val jsonArray = JSONArray()
        entries.forEach { entry ->
            jsonArray.put(
                JSONObject().apply {
                    put("id", entry.id)
                    put("date", entry.date)
                    put("totalSushi", entry.totalSushi)
                    put("randomRouletteEnabled", entry.randomRouletteEnabled)
                    put("randomRouletteMode", entry.randomRouletteMode ?: JSONObject.NULL)
                },
            )
        }
        return jsonArray.toString()
    }

    private fun decodeSoloHistory(raw: String?): PersistenceReadState<List<SoloGameHistoryEntry>> {
        if (raw == null) return PersistenceReadState.Missing
        return runCatching {
            val jsonArray = JSONArray(raw)
            val entries = buildList {
                for (index in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(index)
                    add(
                        SoloGameHistoryEntry(
                            id = item.getString("id"),
                            date = item.getLong("date"),
                            totalSushi = item.getInt("totalSushi"),
                            randomRouletteEnabled = item.getBoolean("randomRouletteEnabled"),
                            randomRouletteMode = if (item.isNull("randomRouletteMode")) {
                                null
                            } else {
                                item.getString("randomRouletteMode").takeIf(String::isNotBlank)
                            },
                        ),
                    )
                }
            }
            PersistenceReadState.Data(entries)
        }.getOrElse { error ->
            // Corrupted history must not be treated as emptyList that later gets persisted.
            logger.logFailure("decodeSoloHistory", error::class.java.simpleName)
            PersistenceReadState.Corrupted
        }
    }

    private fun encodeGroupHistory(entries: List<GroupGameHistoryEntry>): String {
        val jsonArray = JSONArray()
        entries.forEach { entry ->
            val playersArray = JSONArray()
            entry.players.forEach { playerScore ->
                playersArray.put(
                    JSONObject().apply {
                        put("playerName", playerScore.playerName)
                        put("sushiCount", playerScore.sushiCount)
                    },
                )
            }
            jsonArray.put(
                JSONObject().apply {
                    put("id", entry.id)
                    put("date", entry.date)
                    put("players", playersArray)
                    put("randomRouletteEnabled", entry.randomRouletteEnabled)
                    put("randomRouletteMode", entry.randomRouletteMode ?: JSONObject.NULL)
                },
            )
        }
        return jsonArray.toString()
    }

    private fun decodeGroupHistory(raw: String?): PersistenceReadState<List<GroupGameHistoryEntry>> {
        if (raw == null) return PersistenceReadState.Missing
        return runCatching {
            val jsonArray = JSONArray(raw)
            val entries = buildList {
                for (index in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(index)
                    val playersArray = item.getJSONArray("players")
                    val players = buildList {
                        for (playerIndex in 0 until playersArray.length()) {
                            val playerItem = playersArray.getJSONObject(playerIndex)
                            add(
                                PlayerScore(
                                    playerName = playerItem.getString("playerName"),
                                    sushiCount = playerItem.getInt("sushiCount"),
                                ),
                            )
                        }
                    }
                    add(
                        GroupGameHistoryEntry(
                            id = item.getString("id"),
                            date = item.getLong("date"),
                            players = players,
                            randomRouletteEnabled = item.optBoolean("randomRouletteEnabled", false),
                            randomRouletteMode = if (item.isNull("randomRouletteMode")) {
                                null
                            } else {
                                item.getString("randomRouletteMode").takeIf(String::isNotBlank)
                            },
                        ),
                    )
                }
            }
            PersistenceReadState.Data(entries)
        }.getOrElse { error ->
            // Corrupted history must not be treated as emptyList that later gets persisted.
            logger.logFailure("decodeGroupHistory", error::class.java.simpleName)
            PersistenceReadState.Corrupted
        }
    }

    private fun encodeAchievementState(state: AchievementPersistenceState): String {
        val profileJson = JSONObject().apply {
            put("totalGamesCompleted", state.totalGamesCompleted)
            put("totalRouletteSpins", state.totalRouletteSpins)
            put("peakSushiInSingleGame", state.peakSushiInSingleGame)
            put("lifetimeSoloSushiTotal", state.lifetimeSoloSushiTotal)
            put("lifetimeGroupSushiTotal", state.lifetimeGroupSushiTotal)
            put("hasTriggeredAutomaticRoulette", state.hasTriggeredAutomaticRoulette)
            put(
                "unlockedAtById",
                JSONObject().apply {
                    state.unlockedAtById.forEach { (achievementId, unlockedAt) ->
                        put(achievementId, unlockedAt)
                    }
                },
            )
        }
        return JSONObject().apply {
            put(
                "profiles",
                JSONObject().apply {
                    put(AchievementPersistenceState.DEFAULT_PROFILE_KEY, profileJson)
                },
            )
        }.toString()
    }

    private fun decodeAchievementState(raw: String?): PersistenceReadState<AchievementPersistenceState> {
        if (raw == null) return PersistenceReadState.Missing
        return runCatching {
            val root = JSONObject(raw)
            val profiles = root.optJSONObject("profiles")
                ?: return@runCatching PersistenceReadState.Data(AchievementPersistenceState())
            val profile = profiles.optJSONObject(AchievementPersistenceState.DEFAULT_PROFILE_KEY)
                ?: return@runCatching PersistenceReadState.Data(AchievementPersistenceState())

            val unlocksJson = profile.optJSONObject("unlockedAtById") ?: JSONObject()
            val unlockedAtById = buildMap {
                unlocksJson.keys().forEach { key ->
                    if (AchievementId.fromKey(key) != null) {
                        put(key, unlocksJson.getLong(key))
                    }
                }
            }

            PersistenceReadState.Data(
                AchievementPersistenceState(
                    totalGamesCompleted = profile.optInt("totalGamesCompleted", 0),
                    totalRouletteSpins = profile.optInt("totalRouletteSpins", 0),
                    peakSushiInSingleGame = profile.optInt("peakSushiInSingleGame", 0),
                    lifetimeSoloSushiTotal = profile.optInt("lifetimeSoloSushiTotal", 0),
                    lifetimeGroupSushiTotal = profile.optInt("lifetimeGroupSushiTotal", 0),
                    hasTriggeredAutomaticRoulette = profile.optBoolean("hasTriggeredAutomaticRoulette", false),
                    unlockedAtById = unlockedAtById,
                ),
            )
        }.getOrElse { error ->
            logger.logFailure("decodeAchievementState", error::class.java.simpleName)
            PersistenceReadState.Corrupted
        }
    }
}
