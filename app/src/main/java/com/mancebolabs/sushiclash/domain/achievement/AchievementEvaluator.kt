package com.mancebolabs.sushiclash.domain.achievement

import com.mancebolabs.sushiclash.domain.model.GameMode
import com.mancebolabs.sushiclash.domain.model.achievement.AchievementCatalog
import com.mancebolabs.sushiclash.domain.model.achievement.AchievementDefinition
import com.mancebolabs.sushiclash.domain.model.achievement.AchievementId
import com.mancebolabs.sushiclash.domain.model.achievement.AchievementPersistenceState
import com.mancebolabs.sushiclash.domain.model.achievement.AchievementType
import com.mancebolabs.sushiclash.domain.model.achievement.AchievementUnlock

data class AchievementEvaluationResult(
    val state: AchievementPersistenceState,
    val newlyUnlocked: List<AchievementUnlock>,
)

object AchievementEvaluator {

    fun onGameCompleted(
        state: AchievementPersistenceState,
        gameMode: GameMode,
        maxSushiInGame: Int,
        totalSushiInGame: Int,
        unlockedAtEpochMillis: Long,
    ): AchievementEvaluationResult {
        val updatedState = state.copy(
            totalGamesCompleted = state.totalGamesCompleted + 1,
            peakSushiInSingleGame = maxOf(state.peakSushiInSingleGame, maxSushiInGame),
            lifetimeSoloSushiTotal = state.lifetimeSoloSushiTotal +
                if (gameMode == GameMode.SOLO) totalSushiInGame else 0,
            lifetimeGroupSushiTotal = state.lifetimeGroupSushiTotal +
                if (gameMode == GameMode.GROUP) totalSushiInGame else 0,
        )
        return evaluateUnlocks(updatedState, unlockedAtEpochMillis) { definition ->
            when (definition.type) {
                AchievementType.GAMES_COMPLETED,
                AchievementType.PEAK_SUSHI_IN_GAME -> true
                AchievementType.LIFETIME_SOLO_SUSHI -> gameMode == GameMode.SOLO
                AchievementType.LIFETIME_GROUP_SUSHI -> gameMode == GameMode.GROUP
                AchievementType.ROULETTE_SPINS,
                AchievementType.AUTOMATIC_ROULETTE_TRIGGERED -> false
            }
        }
    }

    fun onSushiCountUpdated(
        state: AchievementPersistenceState,
        sushiCountInGame: Int,
        unlockedAtEpochMillis: Long,
    ): AchievementEvaluationResult {
        val updatedState = state.copy(
            peakSushiInSingleGame = maxOf(state.peakSushiInSingleGame, sushiCountInGame),
        )
        return evaluateUnlocks(updatedState, unlockedAtEpochMillis) { definition ->
            definition.type == AchievementType.PEAK_SUSHI_IN_GAME
        }
    }

    fun onRouletteSpun(
        state: AchievementPersistenceState,
        unlockedAtEpochMillis: Long,
    ): AchievementEvaluationResult {
        val updatedState = state.copy(
            totalRouletteSpins = state.totalRouletteSpins + 1,
        )
        return evaluateUnlocks(updatedState, unlockedAtEpochMillis) { definition ->
            definition.type == AchievementType.ROULETTE_SPINS
        }
    }

    fun onAutomaticRouletteTriggered(
        state: AchievementPersistenceState,
        unlockedAtEpochMillis: Long,
    ): AchievementEvaluationResult {
        val updatedState = state.copy(hasTriggeredAutomaticRoulette = true)
        return evaluateUnlocks(updatedState, unlockedAtEpochMillis) { definition ->
            definition.type == AchievementType.AUTOMATIC_ROULETTE_TRIGGERED
        }
    }

    private fun evaluateUnlocks(
        state: AchievementPersistenceState,
        unlockedAtEpochMillis: Long,
        filter: (AchievementDefinition) -> Boolean,
    ): AchievementEvaluationResult {
        var currentState = state
        val unlocks = mutableListOf<AchievementUnlock>()

        AchievementCatalog.definitions
            .filter(filter)
            .forEach { definition ->
                if (currentState.isUnlocked(definition.id)) return@forEach
                if (currentState.progressFor(definition) < definition.target) return@forEach

                currentState = currentState.copy(
                    unlockedAtById = currentState.unlockedAtById + (definition.id.key to unlockedAtEpochMillis),
                )
                unlocks += AchievementUnlock(
                    achievementId = definition.id,
                    unlockedAtEpochMillis = unlockedAtEpochMillis,
                )
            }

        return AchievementEvaluationResult(
            state = currentState,
            newlyUnlocked = unlocks,
        )
    }

    fun progressForDisplay(
        state: AchievementPersistenceState,
        definition: AchievementDefinition,
    ): Int {
        return minOf(state.progressFor(definition), definition.target)
    }

    fun isUnlocked(state: AchievementPersistenceState, id: AchievementId): Boolean {
        return state.isUnlocked(id)
    }
}
