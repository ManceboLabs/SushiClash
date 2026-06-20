package com.mancebolabs.sushicounter.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mancebolabs.sushicounter.domain.model.AppThemeMode
import com.mancebolabs.sushicounter.domain.model.GameMode
import com.mancebolabs.sushicounter.domain.model.GameState
import com.mancebolabs.sushicounter.domain.model.Player
import com.mancebolabs.sushicounter.domain.model.RandomRouletteTriggerType
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
        val hasCompletedSetup = preferences[HAS_COMPLETED_SETUP_KEY] ?: false
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
            hasCompletedSetup = hasCompletedSetup,
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

    suspend fun setThemeMode(themeMode: AppThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = themeMode.name
        }
    }

    suspend fun saveGameState(
        hasCompletedSetup: Boolean,
        gameMode: GameMode,
        players: List<Player>,
        randomRouletteEnabled: Boolean,
        randomRouletteTriggerType: RandomRouletteTriggerType,
        randomRouletteFixedThreshold: Int,
    ) {
        context.dataStore.edit { preferences ->
            preferences[HAS_COMPLETED_SETUP_KEY] = hasCompletedSetup
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
        private val HAS_COMPLETED_SETUP_KEY = booleanPreferencesKey("has_completed_setup")
        private val GAME_MODE_KEY = stringPreferencesKey("game_mode")
        private val PLAYERS_KEY = stringPreferencesKey("players")
        private val PARTICIPANTS_KEY = stringPreferencesKey("participants")
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val RANDOM_ROULETTE_ENABLED_KEY = booleanPreferencesKey("random_roulette_enabled")
        private val RANDOM_ROULETTE_THRESHOLD_KEY = intPreferencesKey("random_roulette_threshold")
        private val RANDOM_ROULETTE_TRIGGER_TYPE_KEY = stringPreferencesKey("random_roulette_trigger_type")
        private val RANDOM_ROULETTE_FIXED_THRESHOLD_KEY = intPreferencesKey("random_roulette_fixed_threshold")
        const val SOLO_PLAYER_ID = "solo_player"
        const val MAX_GROUP_PLAYERS = 6
        const val MIN_GROUP_PLAYERS = 2
    }
}
