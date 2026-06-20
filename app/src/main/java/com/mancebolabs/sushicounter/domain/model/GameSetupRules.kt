package com.mancebolabs.sushicounter.domain.model

/**
 * Shared setup validation used by the setup dialog and tests.
 */
object GameSetupRules {
    const val MIN_GROUP_PLAYERS = 2
    const val MAX_GROUP_PLAYERS = 6

    fun canConfirmSetup(gameMode: GameMode?, groupPlayerCount: Int): Boolean {
        return when (gameMode) {
            GameMode.SOLO -> true
            GameMode.GROUP -> groupPlayerCount >= MIN_GROUP_PLAYERS
            null -> false
        }
    }

    fun canAddGroupPlayer(currentCount: Int): Boolean {
        return currentCount < MAX_GROUP_PLAYERS
    }
}
