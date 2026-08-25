package com.mancebolabs.sushiclash.game

import com.mancebolabs.sushiclash.domain.model.GameMode
import com.mancebolabs.sushiclash.domain.model.GameState
import com.mancebolabs.sushiclash.domain.model.GameStateValidator
import com.mancebolabs.sushiclash.domain.model.Player
import com.mancebolabs.sushiclash.domain.model.RandomRouletteTriggerType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameStateValidatorTest {

    @Test
    fun givenInactiveState_whenValidating_thenReturnsValid() {
        assertTrue(GameStateValidator.isValid(GameState()))
    }

    @Test
    fun givenValidSoloState_whenValidating_thenReturnsValid() {
        assertTrue(GameStateValidator.isValid(soloState(count = 12)))
    }

    @Test
    fun givenActiveStateWithoutMode_whenValidating_thenReturnsInvalid() {
        assertFalse(
            GameStateValidator.isValid(
                GameState(
                    hasActiveGame = true,
                    players = listOf(Player(GameStateValidator.SOLO_PLAYER_ID, "", 2)),
                ),
            ),
        )
    }

    @Test
    fun givenInvalidSoloPlayer_whenValidating_thenReturnsInvalid() {
        assertFalse(GameStateValidator.isValid(soloState(players = emptyList())))
        assertFalse(
            GameStateValidator.isValid(
                soloState(players = listOf(Player("wrong-id", "", 0))),
            ),
        )
        assertFalse(GameStateValidator.isValid(soloState(count = -1)))
    }

    @Test
    fun givenGroupWithTwoOrSixValidPlayers_whenValidating_thenReturnsValid() {
        assertTrue(GameStateValidator.isValid(groupState(playerCount = 2)))
        assertTrue(GameStateValidator.isValid(groupState(playerCount = 6)))
    }

    @Test
    fun givenGroupOutsidePlayerLimits_whenValidating_thenReturnsInvalid() {
        assertFalse(GameStateValidator.isValid(groupState(playerCount = 0)))
        assertFalse(GameStateValidator.isValid(groupState(playerCount = 1)))
        assertFalse(GameStateValidator.isValid(groupState(playerCount = 7)))
    }

    @Test
    fun givenInvalidGroupPlayerFields_whenValidating_thenReturnsInvalid() {
        assertFalse(
            GameStateValidator.isValid(
                groupState(players = listOf(Player("", "Ana"), Player("p2", "Luis"))),
            ),
        )
        assertFalse(
            GameStateValidator.isValid(
                groupState(players = listOf(Player("p1", " "), Player("p2", "Luis"))),
            ),
        )
        assertFalse(
            GameStateValidator.isValid(
                groupState(players = listOf(Player("p1", "Ana", -1), Player("p2", "Luis"))),
            ),
        )
        assertFalse(
            GameStateValidator.isValid(
                groupState(players = listOf(Player("p1", "Ana"), Player("p1", "Luis"))),
            ),
        )
    }

    @Test
    fun givenInvalidRouletteProgress_whenValidating_thenReturnsInvalid() {
        assertFalse(
            GameStateValidator.isValid(
                soloState(
                    count = 4,
                    players = listOf(
                        Player(
                            id = GameStateValidator.SOLO_PLAYER_ID,
                            name = "",
                            sushiCount = 4,
                            lastRandomRouletteTrigger = 5,
                        ),
                    ),
                ),
            ),
        )
        assertFalse(GameStateValidator.isValid(soloState(fixedThreshold = 0)))
        assertFalse(GameStateValidator.isValid(soloState(fixedThreshold = 11)))
    }

    @Test
    fun givenEnabledRandomRouletteWithoutFutureTarget_whenValidating_thenReturnsInvalid() {
        assertFalse(
            GameStateValidator.isValid(
                soloState(
                    count = 5,
                    randomRouletteEnabled = true,
                    triggerType = RandomRouletteTriggerType.RANDOM,
                    nextTarget = 5,
                ),
            ),
        )
        assertFalse(
            GameStateValidator.isValid(
                soloState(
                    count = 5,
                    randomRouletteEnabled = true,
                    triggerType = RandomRouletteTriggerType.RANDOM,
                    nextTarget = null,
                ),
            ),
        )
    }

    @Test
    fun givenInitialRandomTargetBeyondRuntimeRange_whenValidating_thenReturnsInvalid() {
        assertFalse(
            GameStateValidator.isValid(
                soloState(
                    randomRouletteEnabled = true,
                    triggerType = RandomRouletteTriggerType.RANDOM,
                    nextTarget = 11,
                ),
            ),
        )
    }

    @Test
    fun givenProgressiveRandomTargetBeyondRuntimeRange_whenValidating_thenReturnsInvalid() {
        assertFalse(
            GameStateValidator.isValid(
                soloState(
                    count = 5,
                    players = listOf(
                        Player(
                            id = GameStateValidator.SOLO_PLAYER_ID,
                            name = "",
                            sushiCount = 5,
                            lastRandomRouletteTrigger = 5,
                        ),
                    ),
                    randomRouletteEnabled = true,
                    triggerType = RandomRouletteTriggerType.RANDOM,
                    nextTarget = 17,
                ),
            ),
        )
    }

    private fun soloState(
        count: Int = 0,
        players: List<Player> = listOf(
            Player(
                id = GameStateValidator.SOLO_PLAYER_ID,
                name = "",
                sushiCount = count,
            ),
        ),
        randomRouletteEnabled: Boolean = false,
        triggerType: RandomRouletteTriggerType = RandomRouletteTriggerType.FIXED,
        nextTarget: Int? = null,
        fixedThreshold: Int = GameState.DEFAULT_RANDOM_ROULETTE_THRESHOLD,
    ): GameState {
        val playersWithTarget = players.map { player ->
            player.copy(nextRandomRouletteTarget = nextTarget)
        }
        return GameState(
            hasActiveGame = true,
            gameMode = GameMode.SOLO,
            players = playersWithTarget,
            randomRouletteEnabled = randomRouletteEnabled,
            randomRouletteTriggerType = triggerType,
            randomRouletteFixedThreshold = fixedThreshold,
        )
    }

    private fun groupState(
        playerCount: Int = 2,
        players: List<Player> = List(playerCount) { index ->
            Player(id = "p$index", name = "Player $index")
        },
    ): GameState {
        return GameState(
            hasActiveGame = true,
            gameMode = GameMode.GROUP,
            players = players,
        )
    }
}
