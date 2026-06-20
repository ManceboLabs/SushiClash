package com.mancebolabs.sushicounter.domain.model

object GroupHistoryRanking {
    /**
     * Combines scores from every saved group game, grouped by player name.
     */
    fun aggregate(entries: List<GroupGameHistoryEntry>): List<GroupPlayerRanking> {
        val scoresByPlayer = linkedMapOf<String, MutableList<Int>>()
        entries.forEach { entry ->
            entry.players.forEach { playerScore ->
                scoresByPlayer
                    .getOrPut(playerScore.playerName) { mutableListOf() }
                    .add(playerScore.sushiCount)
            }
        }

        return scoresByPlayer.map { (playerName, scores) ->
            GroupPlayerRanking(
                playerName = playerName,
                bestScore = scores.max(),
                totalSushi = scores.sum(),
                gamesPlayed = scores.size,
            )
        }.sortedWith(
            compareByDescending<GroupPlayerRanking> { it.bestScore }
                .thenByDescending { it.totalSushi },
        )
    }
}
