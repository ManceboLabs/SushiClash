package com.mancebolabs.sushiclash.domain.frequentplayer

import com.mancebolabs.sushiclash.domain.model.FrequentPlayer
import java.util.UUID

object FrequentPlayersMerger {

    fun mergeFromGroupGame(
        existing: List<FrequentPlayer>,
        playerNames: List<String>,
        idGenerator: () -> String = { UUID.randomUUID().toString() },
    ): List<FrequentPlayer> {
        if (playerNames.isEmpty()) return existing

        val merged = existing.toMutableList()
        val knownNames = existing
            .map { it.displayName.trim().lowercase() }
            .toMutableSet()

        playerNames.forEach { rawName ->
            val trimmedName = rawName.trim()
            if (trimmedName.isEmpty()) return@forEach

            val normalizedName = trimmedName.lowercase()
            if (normalizedName in knownNames) return@forEach

            knownNames += normalizedName
            merged += FrequentPlayer(
                id = idGenerator(),
                displayName = trimmedName,
            )
        }

        return merged
    }
}
