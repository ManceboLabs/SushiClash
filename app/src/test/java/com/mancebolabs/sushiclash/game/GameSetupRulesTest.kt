package com.mancebolabs.sushiclash.game

import com.mancebolabs.sushiclash.domain.model.GameMode
import com.mancebolabs.sushiclash.domain.model.GameSetupRules
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameSetupRulesTest {

    @Test
    fun givenSoloMode_whenConfirmingSetup_thenAllowed() {
        assertTrue(GameSetupRules.canConfirmSetup(GameMode.SOLO, groupPlayerCount = 0))
    }

    @Test
    fun givenGroupModeWithOnePlayer_whenConfirmingSetup_thenNotAllowed() {
        assertFalse(GameSetupRules.canConfirmSetup(GameMode.GROUP, groupPlayerCount = 1))
    }

    @Test
    fun givenGroupModeWithTwoPlayers_whenConfirmingSetup_thenAllowed() {
        assertTrue(GameSetupRules.canConfirmSetup(GameMode.GROUP, groupPlayerCount = 2))
    }

    @Test
    fun givenGroupModeWithSixPlayers_whenAddingPlayer_thenNotAllowed() {
        assertFalse(GameSetupRules.canAddGroupPlayer(currentCount = 6))
    }

    @Test
    fun givenGroupModeWithFivePlayers_whenAddingPlayer_thenAllowed() {
        assertTrue(GameSetupRules.canAddGroupPlayer(currentCount = 5))
    }

    @Test
    fun givenDuplicateNameIgnoringCase_whenAddingGroupPlayerName_thenNotAllowed() {
        assertFalse(
            GameSetupRules.canAddGroupPlayerName(
                currentPlayers = listOf("Ana"),
                name = " ana ",
            ),
        )
    }

    @Test
    fun givenBlankName_whenAddingGroupPlayerName_thenNotAllowed() {
        assertFalse(
            GameSetupRules.canAddGroupPlayerName(
                currentPlayers = emptyList(),
                name = "   ",
            ),
        )
    }

    @Test
    fun givenUniqueValidName_whenAddingGroupPlayerName_thenAllowed() {
        assertTrue(
            GameSetupRules.canAddGroupPlayerName(
                currentPlayers = listOf("Ana"),
                name = " Luis ",
            ),
        )
    }

    @Test
    fun givenNoModeSelected_whenConfirmingSetup_thenNotAllowed() {
        assertFalse(GameSetupRules.canConfirmSetup(gameMode = null, groupPlayerCount = 4))
    }
}
