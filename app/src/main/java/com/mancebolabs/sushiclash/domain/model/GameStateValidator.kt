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
            player.lastRandomRouletteTrigger <= player.sushiCount &&
            player.lastChefAnimationTrigger >= 0 &&
            player.lastChefAnimationTrigger <= player.sushiCount &&
            hasValidChefAnimationTarget(player)
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

    private fun hasValidChefAnimationTarget(player: Player): Boolean {
        val target = player.nextChefAnimationTarget ?: return true
        if (target <= player.sushiCount) return false
        if (player.lastChefAnimationTrigger > player.sushiCount) return false

        if (player.lastChefAnimationTrigger == 0) {
            if (player.sushiCount == 0) {
                return target in ChefAnimationTriggerLogic.MIN_INTERVAL..ChefAnimationTriggerLogic.MAX_INTERVAL
            }
            return target > player.sushiCount
        }

        val lastTrigger = player.lastChefAnimationTrigger.toLong()
        return target.toLong() in
            (lastTrigger + ChefAnimationTriggerLogic.MIN_INTERVAL)..(
                lastTrigger + ChefAnimationTriggerLogic.MAX_INTERVAL
                )
    }

    private const val MAX_PROGRESSIVE_TARGET_OFFSET = 11L
}
