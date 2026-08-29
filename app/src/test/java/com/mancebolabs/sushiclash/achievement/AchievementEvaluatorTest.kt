package com.mancebolabs.sushiclash.achievement

import com.mancebolabs.sushiclash.domain.achievement.AchievementEvaluator
import com.mancebolabs.sushiclash.domain.model.GameMode
import com.mancebolabs.sushiclash.domain.model.achievement.AchievementCatalog
import com.mancebolabs.sushiclash.domain.model.achievement.AchievementId
import com.mancebolabs.sushiclash.domain.model.achievement.AchievementPersistenceState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementEvaluatorTest {

    @Test
    fun givenSushiCountUpdated_whenThresholdReached_thenUnlocksOnce() {
        val first = AchievementEvaluator.onSushiCountUpdated(
            state = AchievementPersistenceState(),
            sushiCountInGame = 10,
            unlockedAtEpochMillis = 100L,
        )
        assertEquals(listOf(AchievementId.SUSHI_10), first.newlyUnlocked.map { it.achievementId })
        assertTrue(first.state.isUnlocked(AchievementId.SUSHI_10))

        val second = AchievementEvaluator.onSushiCountUpdated(
            state = first.state,
            sushiCountInGame = 11,
            unlockedAtEpochMillis = 200L,
        )
        assertTrue(second.newlyUnlocked.isEmpty())
        assertEquals(100L, second.state.unlockedAt(AchievementId.SUSHI_10))
    }

    @Test
    fun givenGameCompleted_whenCountIncreases_thenUnlocksCumulativeGamesAchievement() {
        var state = AchievementPersistenceState()

        repeat(9) { index ->
            val result = AchievementEvaluator.onGameCompleted(
                state = state,
                gameMode = GameMode.SOLO,
                maxSushiInGame = 3,
                totalSushiInGame = 3,
                unlockedAtEpochMillis = index.toLong(),
            )
            state = result.state
            if (index == 0) {
                assertEquals(listOf(AchievementId.GAMES_1), result.newlyUnlocked.map { it.achievementId })
            } else {
                assertTrue(result.newlyUnlocked.isEmpty())
            }
        }

        val tenthGame = AchievementEvaluator.onGameCompleted(
            state = state,
            gameMode = GameMode.SOLO,
            maxSushiInGame = 8,
            totalSushiInGame = 8,
            unlockedAtEpochMillis = 10L,
        )

        assertEquals(
            listOf(AchievementId.GAMES_10),
            tenthGame.newlyUnlocked.map { it.achievementId },
        )
        assertEquals(10, tenthGame.state.totalGamesCompleted)
        assertEquals(8, tenthGame.state.peakSushiInSingleGame)
    }

    @Test
    fun givenSoloGameCompleted_whenSushiAccumulates_thenUnlocksLifetimeSoloThresholds() {
        val result = AchievementEvaluator.onGameCompleted(
            state = AchievementPersistenceState(),
            gameMode = GameMode.SOLO,
            maxSushiInGame = 120,
            totalSushiInGame = 120,
            unlockedAtEpochMillis = 100L,
        )

        assertEquals(
            listOf(
                AchievementId.SUSHI_10,
                AchievementId.SUSHI_20,
                AchievementId.SUSHI_30,
                AchievementId.SUSHI_40,
                AchievementId.SUSHI_50,
                AchievementId.SUSHI_100,
                AchievementId.SOLO_TOTAL_50,
                AchievementId.SOLO_TOTAL_100,
                AchievementId.GAMES_1,
            ),
            result.newlyUnlocked.map { it.achievementId },
        )
        assertEquals(120, result.state.lifetimeSoloSushiTotal)
        assertEquals(0, result.state.lifetimeGroupSushiTotal)
    }

    @Test
    fun givenGroupGameCompleted_whenSushiAccumulates_thenUnlocksLifetimeGroupThresholdsOnly() {
        val result = AchievementEvaluator.onGameCompleted(
            state = AchievementPersistenceState(),
            gameMode = GameMode.GROUP,
            maxSushiInGame = 40,
            totalSushiInGame = 75,
            unlockedAtEpochMillis = 200L,
        )

        assertEquals(
            listOf(
                AchievementId.SUSHI_10,
                AchievementId.SUSHI_20,
                AchievementId.SUSHI_30,
                AchievementId.SUSHI_40,
                AchievementId.GROUP_TOTAL_50,
                AchievementId.GAMES_1,
            ),
            result.newlyUnlocked.map { it.achievementId },
        )
        assertEquals(0, result.state.lifetimeSoloSushiTotal)
        assertEquals(75, result.state.lifetimeGroupSushiTotal)
    }

    @Test
    fun givenExistingSoloTotal_whenGroupGameCompleted_thenSoloTotalIsUntouched() {
        val initial = AchievementPersistenceState(lifetimeSoloSushiTotal = 742)

        val result = AchievementEvaluator.onGameCompleted(
            state = initial,
            gameMode = GameMode.GROUP,
            maxSushiInGame = 30,
            totalSushiInGame = 90,
            unlockedAtEpochMillis = 300L,
        )

        assertEquals(742, result.state.lifetimeSoloSushiTotal)
        assertEquals(90, result.state.lifetimeGroupSushiTotal)
        assertTrue(result.newlyUnlocked.any { it.achievementId == AchievementId.GROUP_TOTAL_50 })
        assertFalse(result.newlyUnlocked.any { it.achievementId == AchievementId.SOLO_TOTAL_50 })
    }

    @Test
    fun givenExistingSoloTotalWithoutUnlocks_whenSoloGameCompleted_thenUnlocksPendingSoloThresholds() {
        val initial = AchievementPersistenceState(lifetimeSoloSushiTotal = 742)

        val result = AchievementEvaluator.onGameCompleted(
            state = initial,
            gameMode = GameMode.SOLO,
            maxSushiInGame = 5,
            totalSushiInGame = 5,
            unlockedAtEpochMillis = 400L,
        )

        assertEquals(
            listOf(
                AchievementId.SOLO_TOTAL_50,
                AchievementId.SOLO_TOTAL_100,
                AchievementId.SOLO_TOTAL_250,
                AchievementId.SOLO_TOTAL_500,
                AchievementId.GAMES_1,
            ),
            result.newlyUnlocked.map { it.achievementId },
        )
        assertEquals(747, result.state.lifetimeSoloSushiTotal)
    }

    @Test
    fun givenMultipleSoloGames_whenThresholdCrossedAcrossGames_thenUnlocksOncePerThreshold() {
        var state = AchievementPersistenceState()

        val first = AchievementEvaluator.onGameCompleted(
            state = state,
            gameMode = GameMode.SOLO,
            maxSushiInGame = 55,
            totalSushiInGame = 55,
            unlockedAtEpochMillis = 1L,
        )
        state = first.state
        assertEquals(
            listOf(
                AchievementId.SUSHI_10,
                AchievementId.SUSHI_20,
                AchievementId.SUSHI_30,
                AchievementId.SUSHI_40,
                AchievementId.SUSHI_50,
                AchievementId.SOLO_TOTAL_50,
                AchievementId.GAMES_1,
            ),
            first.newlyUnlocked.map { it.achievementId },
        )

        val second = AchievementEvaluator.onGameCompleted(
            state = state,
            gameMode = GameMode.SOLO,
            maxSushiInGame = 60,
            totalSushiInGame = 60,
            unlockedAtEpochMillis = 2L,
        )

        assertEquals(
            listOf(AchievementId.SOLO_TOTAL_100),
            second.newlyUnlocked.map { it.achievementId },
        )
        assertEquals(115, second.state.lifetimeSoloSushiTotal)
    }

    @Test
    fun givenLargeSingleSoloGame_whenCompleted_thenUnlocksMultipleLifetimeThresholdsAtOnce() {
        val result = AchievementEvaluator.onGameCompleted(
            state = AchievementPersistenceState(),
            gameMode = GameMode.SOLO,
            maxSushiInGame = 600,
            totalSushiInGame = 600,
            unlockedAtEpochMillis = 400L,
        )

        assertEquals(
            listOf(
                AchievementId.SUSHI_10,
                AchievementId.SUSHI_20,
                AchievementId.SUSHI_30,
                AchievementId.SUSHI_40,
                AchievementId.SUSHI_50,
                AchievementId.SUSHI_100,
                AchievementId.SOLO_TOTAL_50,
                AchievementId.SOLO_TOTAL_100,
                AchievementId.SOLO_TOTAL_250,
                AchievementId.SOLO_TOTAL_500,
                AchievementId.GAMES_1,
            ),
            result.newlyUnlocked.map { it.achievementId },
        )
    }

    @Test
    fun givenProgressForDisplay_whenPartialLifetimeProgress_thenCapsAtTarget() {
        val state = AchievementPersistenceState(lifetimeSoloSushiTotal = 742)
        val definition = AchievementCatalog.byId.getValue(AchievementId.SOLO_TOTAL_1000)

        assertEquals(742, AchievementEvaluator.progressForDisplay(state, definition))
        assertFalse(state.isUnlocked(AchievementId.SOLO_TOTAL_1000))
    }

    @Test
    fun givenRouletteSpun_whenAlreadyUnlocked_thenDoesNotUnlockAgain() {
        val first = AchievementEvaluator.onRouletteSpun(
            state = AchievementPersistenceState(),
            unlockedAtEpochMillis = 50L,
        )
        assertEquals(listOf(AchievementId.ROULETTE_FIRST_SPIN), first.newlyUnlocked.map { it.achievementId })

        val second = AchievementEvaluator.onRouletteSpun(
            state = first.state,
            unlockedAtEpochMillis = 60L,
        )
        assertTrue(second.newlyUnlocked.isEmpty())
        assertEquals(2, second.state.totalRouletteSpins)
    }

    @Test
    fun givenAutomaticRouletteTriggered_whenCalledTwice_thenUnlocksOnlyOnce() {
        val first = AchievementEvaluator.onAutomaticRouletteTriggered(
            state = AchievementPersistenceState(),
            unlockedAtEpochMillis = 300L,
        )
        assertEquals(listOf(AchievementId.ROULETTE_AUTO_FIRST), first.newlyUnlocked.map { it.achievementId })

        val second = AchievementEvaluator.onAutomaticRouletteTriggered(
            state = first.state,
            unlockedAtEpochMillis = 400L,
        )
        assertTrue(second.newlyUnlocked.isEmpty())
        assertTrue(second.state.hasTriggeredAutomaticRoulette)
    }

    @Test
    fun givenProgressForDisplay_whenPartialProgress_thenCapsAtTarget() {
        val state = AchievementPersistenceState(peakSushiInSingleGame = 17)
        val definition = AchievementCatalog.byId.getValue(AchievementId.SUSHI_20)

        assertEquals(17, AchievementEvaluator.progressForDisplay(state, definition))
        assertFalse(state.isUnlocked(AchievementId.SUSHI_20))
    }
}
