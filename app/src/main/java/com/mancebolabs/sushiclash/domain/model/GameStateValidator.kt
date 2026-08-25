package com.mancebolabs.sushiclash.domain.model

object GameStateValidator {
    const val SOLO_PLAYER_ID = "solo_player"

    fun isValid(gameState: GameState): Boolean {
        if (!gameState.hasActiveGame) return true
        if (gameState.randomRouletteFixedThreshold !in
            GameState.MIN_RANDOM_ROULETTE_THRESHOLD..GameState.MAX_RANDOM_ROULETTE_THRESHOLD
        ) {
            return false
        }
        if (!gameState.players.all(::hasValidProgress)) return false

        val hasValidPlayers = when (gameState.gameMode) {
            GameMode.SOLO -> {
                gameState.players.size == 1 &&
                    gameState.players.single().id == SOLO_PLAYER_ID
            }
            GameMode.GROUP -> {
                gameState.players.size in
                    GameSetupRules.MIN_GROUP_PLAYERS..GameSetupRules.MAX_GROUP_PLAYERS &&
                    gameState.players.all { player ->
                        player.id.isNotEmpty() && player.name.isNotBlank()
                    } &&
                    gameState.players.map(Player::id).distinct().size == gameState.players.size
            }
            null -> false
        }
        if (!hasValidPlayers) return false

        return !gameState.randomRouletteEnabled ||
            gameState.randomRouletteTriggerType != RandomRouletteTriggerType.RANDOM ||
            gameState.players.all(::hasValidRandomTarget)
    }

    private fun hasValidProgress(player: Player): Boolean {
        return player.sushiCount >= 0 &&
            player.lastRandomRouletteTrigger >= 0 &&
            player.lastRandomRouletteTrigger <= player.sushiCount
    }

    private fun hasValidRandomTarget(player: Player): Boolean {
        val target = player.nextRandomRouletteTarget ?: return false
        if (target <= player.sushiCount) return false
        if (player.lastRandomRouletteTrigger == 0) {
            return target <= GameState.MAX_RANDOM_ROULETTE_THRESHOLD
        }

        val targetAsLong = target.toLong()
        val lastTrigger = player.lastRandomRouletteTrigger.toLong()
        return targetAsLong in
            (lastTrigger + 1)..(lastTrigger + MAX_PROGRESSIVE_TARGET_OFFSET)
    }

    private const val MAX_PROGRESSIVE_TARGET_OFFSET = 11L
}
