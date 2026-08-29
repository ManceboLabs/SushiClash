package com.mancebolabs.sushiclash.frequentplayer

import com.mancebolabs.sushiclash.domain.frequentplayer.FrequentPlayersMerger
import com.mancebolabs.sushiclash.domain.model.FrequentPlayer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FrequentPlayersMergerTest {

    @Test
    fun givenNewNames_whenMerging_thenAddsTrimmedUniquePlayers() {
        val existing = listOf(FrequentPlayer(id = "1", displayName = "Ana"))

        val merged = FrequentPlayersMerger.mergeFromGroupGame(
            existing = existing,
            playerNames = listOf(" Luis ", "Marta", "  ", "luis"),
            idGenerator = { "generated-id" },
        )

        assertEquals(3, merged.size)
        assertEquals("Ana", merged[0].displayName)
        assertEquals("Luis", merged[1].displayName)
        assertEquals("Marta", merged[2].displayName)
    }

    @Test
    fun givenBlankNames_whenMerging_thenSkipsInvalidEntries() {
        val merged = FrequentPlayersMerger.mergeFromGroupGame(
            existing = emptyList(),
            playerNames = listOf("   ", "", "\t"),
            idGenerator = { "generated-id" },
        )

        assertTrue(merged.isEmpty())
    }

    @Test
    fun givenExistingPlayerWithDifferentCasing_whenMerging_thenDoesNotDuplicate() {
        val existing = listOf(FrequentPlayer(id = "1", displayName = "Ana"))

        val merged = FrequentPlayersMerger.mergeFromGroupGame(
            existing = existing,
            playerNames = listOf("ANA", " ana "),
            idGenerator = { "generated-id" },
        )

        assertEquals(1, merged.size)
        assertEquals("Ana", merged.single().displayName)
    }
}
