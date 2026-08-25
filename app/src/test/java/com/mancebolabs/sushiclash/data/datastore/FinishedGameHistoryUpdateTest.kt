package com.mancebolabs.sushiclash.data.datastore

import com.mancebolabs.sushiclash.domain.model.GameState
import com.mancebolabs.sushiclash.domain.model.GroupGameHistoryEntry
import com.mancebolabs.sushiclash.domain.model.Player
import com.mancebolabs.sushiclash.domain.model.PlayerScore
import com.mancebolabs.sushiclash.domain.model.RandomRouletteTriggerType
import com.mancebolabs.sushiclash.domain.model.SoloGameHistoryEntry
import com.mancebolabs.sushiclash.testutil.TestGameStates
import org.junit.Assert.assertEquals
import org.junit.Test

class FinishedGameHistoryUpdateTest {

    @Test
    fun givenSoloGame_whenBuildingUpdate_thenOnlyDecodesSoloAndPreservesItsExistingHistory() {
        val existingEntry = soloEntry(id = "previous")
        var groupDecodeCount = 0

        val result = buildFinishedGameHistoryUpdate(
            gameState = TestGameStates.soloActive(
                sessionId = "session-solo",
                count = 7,
                randomRouletteEnabled = true,
                triggerType = RandomRouletteTriggerType.RANDOM,
                nextTarget = 8,
            ),
            legacySessionId = "unused",
            finishedAt = 100L,
            decodeSoloHistory = {
                DecodedHistory(entries = listOf(existingEntry), isValid = true)
            },
            decodeGroupHistory = {
                groupDecodeCount++
                DecodedHistory(entries = emptyList(), isValid = true)
            },
        )

        assertEquals(0, groupDecodeCount)
        val update = result as FinishedGameHistoryUpdate.Solo
        assertEquals(
            SoloGameHistoryEntry(
                id = "session-solo",
                date = 100L,
                totalSushi = 7,
                randomRouletteEnabled = true,
                randomRouletteMode = RandomRouletteTriggerType.RANDOM.name,
            ),
            update.history.first(),
        )
        assertEquals(existingEntry, update.history.last())
    }

    @Test
    fun givenGroupGame_whenBuildingUpdate_thenOnlyDecodesGroupAndMapsExactScores() {
        val existingEntry = groupEntry(id = "previous")
        var soloDecodeCount = 0

        val result = buildFinishedGameHistoryUpdate(
            gameState = TestGameStates.groupActive(
                sessionId = "session-group",
                players = listOf(
                    Player(id = "p1", name = "Ana", sushiCount = 11),
                    Player(id = "p2", name = "Luis", sushiCount = 8),
                ),
            ),
            legacySessionId = "unused",
            finishedAt = 200L,
            decodeSoloHistory = {
                soloDecodeCount++
                DecodedHistory(entries = emptyList(), isValid = true)
            },
            decodeGroupHistory = {
                DecodedHistory(entries = listOf(existingEntry), isValid = true)
            },
        )

        assertEquals(0, soloDecodeCount)
        val update = result as FinishedGameHistoryUpdate.Group
        assertEquals(
            GroupGameHistoryEntry(
                id = "session-group",
                date = 200L,
                players = listOf(
                    PlayerScore(playerName = "Ana", sushiCount = 11),
                    PlayerScore(playerName = "Luis", sushiCount = 8),
                ),
                randomRouletteEnabled = false,
                randomRouletteMode = null,
            ),
            update.history.first(),
        )
        assertEquals(existingEntry, update.history.last())
    }

    @Test
    fun givenCorruptTargetHistory_whenBuildingUpdate_thenReturnsCorruptWithoutDecodingOppositeBranch() {
        var oppositeDecodeCount = 0

        val result = buildFinishedGameHistoryUpdate(
            gameState = TestGameStates.soloActive(sessionId = "session-solo"),
            legacySessionId = "unused",
            finishedAt = 300L,
            decodeSoloHistory = {
                DecodedHistory(entries = emptyList(), isValid = false)
            },
            decodeGroupHistory = {
                oppositeDecodeCount++
                DecodedHistory(entries = emptyList(), isValid = true)
            },
        )

        assertEquals(FinishedGameHistoryUpdate.CorruptHistory, result)
        assertEquals(0, oppositeDecodeCount)
    }

    @Test
    fun givenExistingSessionInTargetHistory_whenBuildingTwice_thenDoesNotDuplicate() {
        val existingEntry = soloEntry(id = "same-session")

        val result = buildFinishedGameHistoryUpdate(
            gameState = TestGameStates.soloActive(sessionId = "same-session", count = 99),
            legacySessionId = "unused",
            finishedAt = 400L,
            decodeSoloHistory = {
                DecodedHistory(entries = listOf(existingEntry), isValid = true)
            },
            decodeGroupHistory = {
                error("Group history must not be decoded for a solo game")
            },
        )

        assertEquals(listOf(existingEntry), (result as FinishedGameHistoryUpdate.Solo).history)
    }

    @Test
    fun givenLegacyGame_whenBuildingUpdate_thenUsesStableFallbackId() {
        val result = buildFinishedGameHistoryUpdate(
            gameState = TestGameStates.soloActive(sessionId = null, count = 3),
            legacySessionId = "legacy-session",
            finishedAt = 500L,
            decodeSoloHistory = {
                DecodedHistory(entries = emptyList(), isValid = true)
            },
            decodeGroupHistory = {
                error("Group history must not be decoded for a solo game")
            },
        )

        assertEquals(
            "legacy-session",
            (result as FinishedGameHistoryUpdate.Solo).history.single().id,
        )
    }

    @Test
    fun givenInactiveGame_whenBuildingUpdate_thenReturnsInvalidWithoutDecodingHistory() {
        var decodeCount = 0

        val result = buildFinishedGameHistoryUpdate(
            gameState = GameState(),
            legacySessionId = "fallback",
            finishedAt = 600L,
            decodeSoloHistory = {
                decodeCount++
                DecodedHistory(entries = emptyList(), isValid = true)
            },
            decodeGroupHistory = {
                decodeCount++
                DecodedHistory(entries = emptyList(), isValid = true)
            },
        )

        assertEquals(FinishedGameHistoryUpdate.NoActiveGame, result)
        assertEquals(0, decodeCount)
    }

    @Test
    fun givenActiveInvalidGame_whenBuildingUpdate_thenReturnsInvalidActiveWithoutDecodingHistory() {
        var decodeCount = 0

        val result = buildFinishedGameHistoryUpdate(
            gameState = GameState(hasActiveGame = true),
            legacySessionId = "fallback",
            finishedAt = 700L,
            decodeSoloHistory = {
                decodeCount++
                DecodedHistory(entries = emptyList(), isValid = true)
            },
            decodeGroupHistory = {
                decodeCount++
                DecodedHistory(entries = emptyList(), isValid = true)
            },
        )

        assertEquals(FinishedGameHistoryUpdate.InvalidActiveGame, result)
        assertEquals(0, decodeCount)
    }

    @Test
    fun givenActiveGameDecodeFailure_whenMappingPersistenceResult_thenReturnsInvalidActiveGame() {
        val result = mapInvalidDecodedGameResult(
            DecodedGameState(
                gameState = GameState(hasActiveGame = true),
                isDecodeValid = false,
            ),
        )

        assertEquals(FinishGamePersistenceResult.InvalidActiveGame, result)
    }

    @Test
    fun givenInactiveGameDecodeFailure_whenMappingPersistenceResult_thenReturnsNoActiveGame() {
        val result = mapInvalidDecodedGameResult(
            DecodedGameState(
                gameState = GameState(hasActiveGame = false),
                isDecodeValid = false,
            ),
        )

        assertEquals(FinishGamePersistenceResult.NoActiveGame, result)
    }

    private fun soloEntry(id: String): SoloGameHistoryEntry {
        return SoloGameHistoryEntry(
            id = id,
            date = 10L,
            totalSushi = 4,
            randomRouletteEnabled = false,
            randomRouletteMode = null,
        )
    }

    private fun groupEntry(id: String): GroupGameHistoryEntry {
        return GroupGameHistoryEntry(
            id = id,
            date = 20L,
            players = listOf(
                PlayerScore(playerName = "Previous", sushiCount = 5),
            ),
            randomRouletteEnabled = false,
            randomRouletteMode = null,
        )
    }
}
