package com.mancebolabs.sushiclash.achievement

import com.mancebolabs.sushiclash.domain.achievement.totalSushiInGame
import com.mancebolabs.sushiclash.domain.model.GameMode
import com.mancebolabs.sushiclash.domain.model.GameState
import com.mancebolabs.sushiclash.domain.model.Player
import org.junit.Assert.assertEquals
import org.junit.Test

class AchievementGameStatsTest {

    @Test
    fun givenSoloGameState_whenTotalSushiInGame_thenReturnsPlayerCount() {
        val state = GameState(
            hasActiveGame = true,
            gameMode = GameMode.SOLO,
            players = listOf(Player(id = "solo", name = "Yo", sushiCount = 42)),
        )

        assertEquals(42, state.totalSushiInGame())
    }

    @Test
    fun givenGroupGameState_whenTotalSushiInGame_thenSumsAllPlayers() {
        val state = GameState(
            hasActiveGame = true,
            gameMode = GameMode.GROUP,
            players = listOf(
                Player(id = "p1", name = "Ana", sushiCount = 20),
                Player(id = "p2", name = "Luis", sushiCount = 30),
                Player(id = "p3", name = "Marta", sushiCount = 25),
            ),
        )

        assertEquals(75, state.totalSushiInGame())
    }
}
