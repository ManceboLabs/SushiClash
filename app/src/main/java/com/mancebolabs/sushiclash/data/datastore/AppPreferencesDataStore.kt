package com.mancebolabs.sushiclash.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mancebolabs.sushiclash.domain.model.AppThemeMode
import com.mancebolabs.sushiclash.domain.model.GameMode
import com.mancebolabs.sushiclash.domain.model.GameSetupRules
import com.mancebolabs.sushiclash.domain.model.GameState
import com.mancebolabs.sushiclash.domain.model.GroupGameHistoryEntry
import com.mancebolabs.sushiclash.domain.model.Player
import com.mancebolabs.sushiclash.domain.model.PlayerScore
import com.mancebolabs.sushiclash.domain.model.RandomRouletteTriggerType
import com.mancebolabs.sushiclash.domain.model.SoloGameHistoryEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "sushi_counter_preferences",
)

class AppPreferencesDataStore(
    private val context: Context,
) {
    val gameState: Flow<GameState> = context.dataStore.data.map { preferences ->
        // Prefer has_active_game; fall back to legacy has_completed_setup for migration.
        val hasActiveGame = preferences[HAS_ACTIVE_GAME_KEY]
            ?: preferences[HAS_COMPLETED_SETUP_KEY]
            ?: false

        if (!hasActiveGame) {
            return@map GameState(hasActiveGame = false)
        }

        val gameMode = preferences[GAME_MODE_KEY]?.let { storedMode ->
            runCatching { GameMode.valueOf(storedMode) }.getOrNull()
        }
        val players = decodePlayers(preferences[PLAYERS_KEY].orEmpty())

        val triggerType = preferences[RANDOM_ROULETTE_TRIGGER_TYPE_KEY]?.let { storedType ->
            runCatching { RandomRouletteTriggerType.valueOf(storedType) }.getOrNull()
        } ?: RandomRouletteTriggerType.FIXED
        val fixedThreshold = (preferences[RANDOM_ROULETTE_FIXED_THRESHOLD_KEY]
            ?: preferences[RANDOM_ROULETTE_THRESHOLD_KEY]
            ?: GameState.DEFAULT_RANDOM_ROULETTE_THRESHOLD)
            .coerceIn(
                GameState.MIN_RANDOM_ROULETTE_THRESHOLD,
                GameState.MAX_RANDOM_ROULETTE_THRESHOLD,
            )

        GameState(
            hasActiveGame = true,
            gameMode = gameMode,
            players = players,
            randomRouletteEnabled = preferences[RANDOM_ROULETTE_ENABLED_KEY] ?: false,
            randomRouletteTriggerType = triggerType,
            randomRouletteFixedThreshold = fixedThreshold,
        )
    }

    val participants: Flow<List<String>> = context.dataStore.data.map { preferences ->
        decodeParticipants(preferences[PARTICIPANTS_KEY].orEmpty())
    }

    val themeMode: Flow<AppThemeMode> = context.dataStore.data.map { preferences ->
        preferences[THEME_MODE_KEY]?.let { storedTheme ->
            runCatching { AppThemeMode.valueOf(storedTheme) }.getOrNull()
        } ?: AppThemeMode.LIGHT
    }

    val soloHistory: Flow<List<SoloGameHistoryEntry>> = context.dataStore.data.map { preferences ->
        decodeSoloHistory(preferences[SOLO_HISTORY_KEY])
    }

    val groupHistory: Flow<List<GroupGameHistoryEntry>> = context.dataStore.data.map { preferences ->
        decodeGroupHistory(preferences[GROUP_HISTORY_KEY])
    }

    suspend fun setThemeMode(themeMode: AppThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = themeMode.name
        }
    }

    suspend fun saveGameState(
        gameMode: GameMode,
        players: List<Player>,
        randomRouletteEnabled: Boolean,
        randomRouletteTriggerType: RandomRouletteTriggerType,
        randomRouletteFixedThreshold: Int,
    ) {
        context.dataStore.edit { preferences ->
            preferences[HAS_ACTIVE_GAME_KEY] = true
            preferences[HAS_COMPLETED_SETUP_KEY] = true
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

    suspend fun clearActiveGame() {
        context.dataStore.edit { preferences ->
            preferences[HAS_ACTIVE_GAME_KEY] = false
            preferences[HAS_COMPLETED_SETUP_KEY] = false
            preferences.remove(GAME_MODE_KEY)
            preferences[PLAYERS_KEY] = encodePlayers(emptyList())
            preferences[PARTICIPANTS_KEY] = encodeParticipants(emptyList())
            preferences[RANDOM_ROULETTE_ENABLED_KEY] = false
            preferences.remove(RANDOM_ROULETTE_TRIGGER_TYPE_KEY)
            preferences.remove(RANDOM_ROULETTE_FIXED_THRESHOLD_KEY)
        }
    }

    suspend fun setPlayers(players: List<Player>) {
        context.dataStore.edit { preferences ->
            preferences[PLAYERS_KEY] = encodePlayers(players)
        }
    }

    suspend fun setParticipants(names: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[PARTICIPANTS_KEY] = encodeParticipants(names)
        }
    }

    suspend fun appendSoloHistoryEntry(entry: SoloGameHistoryEntry) {
        context.dataStore.edit { preferences ->
            val current = decodeSoloHistory(preferences[SOLO_HISTORY_KEY]).toMutableList()
            current.add(0, entry)
            preferences[SOLO_HISTORY_KEY] = encodeSoloHistory(current)
        }
    }

    suspend fun appendGroupHistoryEntry(entry: GroupGameHistoryEntry) {
        context.dataStore.edit { preferences ->
            val current = decodeGroupHistory(preferences[GROUP_HISTORY_KEY]).toMutableList()
            current.add(0, entry)
            preferences[GROUP_HISTORY_KEY] = encodeGroupHistory(current)
        }
    }

    suspend fun clearHistory() {
        context.dataStore.edit { preferences ->
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
                },
            )
        }
        return jsonArray.toString()
    }

    private fun decodePlayers(raw: String): List<Player> {
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val jsonArray = JSONArray(raw)
            buildList {
                for (index in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(index)
                    add(
                        Player(
                            id = item.getString("id"),
                            name = item.getString("name"),
                            sushiCount = item.optInt("sushiCount", 0),
                            nextRandomRouletteTarget = item.optInt("nextRandomRouletteTarget", -1)
                                .takeIf { it >= GameState.MIN_RANDOM_ROULETTE_THRESHOLD },
                            lastRandomRouletteTrigger = item.optInt("lastRandomRouletteTrigger", 0),
                        ),
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun encodeParticipants(names: List<String>): String {
        val jsonArray = JSONArray()
        names.forEach { name -> jsonArray.put(name) }
        return jsonArray.toString()
    }

    private fun decodeParticipants(raw: String): List<String> {
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val jsonArray = JSONArray(raw)
            buildList {
                for (index in 0 until jsonArray.length()) {
                    add(jsonArray.getString(index))
                }
            }
        }.getOrDefault(emptyList())
    }

    companion object {
        private val HAS_ACTIVE_GAME_KEY = booleanPreferencesKey("has_active_game")
        private val HAS_COMPLETED_SETUP_KEY = booleanPreferencesKey("has_completed_setup")
        private val GAME_MODE_KEY = stringPreferencesKey("game_mode")
        private val PLAYERS_KEY = stringPreferencesKey("players")
        private val PARTICIPANTS_KEY = stringPreferencesKey("participants")
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val RANDOM_ROULETTE_ENABLED_KEY = booleanPreferencesKey("random_roulette_enabled")
        private val RANDOM_ROULETTE_THRESHOLD_KEY = intPreferencesKey("random_roulette_threshold")
        private val RANDOM_ROULETTE_TRIGGER_TYPE_KEY = stringPreferencesKey("random_roulette_trigger_type")
        private val RANDOM_ROULETTE_FIXED_THRESHOLD_KEY = intPreferencesKey("random_roulette_fixed_threshold")
        private val SOLO_HISTORY_KEY = stringPreferencesKey("solo_history")
        private val GROUP_HISTORY_KEY = stringPreferencesKey("group_history")
        const val SOLO_PLAYER_ID = "solo_player"
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

    private fun decodeSoloHistory(raw: String?): List<SoloGameHistoryEntry> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val jsonArray = JSONArray(raw)
            buildList {
                for (index in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(index)
                    add(
                        SoloGameHistoryEntry(
                            id = item.getString("id"),
                            date = item.getLong("date"),
                            totalSushi = item.getInt("totalSushi"),
                            randomRouletteEnabled = item.getBoolean("randomRouletteEnabled"),
                            randomRouletteMode = item.optString("randomRouletteMode").takeIf { it.isNotBlank() },
                        ),
                    )
                }
            }
        }.getOrDefault(emptyList())
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

    private fun decodeGroupHistory(raw: String?): List<GroupGameHistoryEntry> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val jsonArray = JSONArray(raw)
            buildList {
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
                            randomRouletteMode = item.optString("randomRouletteMode").takeIf { it.isNotBlank() },
                        ),
                    )
                }
            }
        }.getOrDefault(emptyList())
    }
}
