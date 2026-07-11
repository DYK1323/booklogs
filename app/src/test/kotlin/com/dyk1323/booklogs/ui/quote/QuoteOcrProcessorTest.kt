package com.dyk1323.booklogs.ui.quote

import org.junit.Assert.assertEquals
import org.junit.Test

class QuoteOcrProcessorTest {

    @Test
    fun pickBetterQuoteText_prefersLongerNonBlankResult() {
        assertEquals("longer korean result", pickBetterQuoteText("longer korean result", "short"))
        assertEquals("longer latin result", pickBetterQuoteText("short", "longer latin result"))
    }

    @Test
    fun pickBetterQuoteText_fallsBackToWhicheverIsNonBlank() {
        assertEquals("korean only", pickBetterQuoteText("korean only", ""))
        assertEquals("latin only", pickBetterQuoteText("", "latin only"))
    }

    @Test
    fun pickBetterQuoteText_tieFavorsKorean() {
        assertEquals("가나다", pickBetterQuoteText("가나다", "abc"))
    }

    @Test
    fun pickBetterQuoteText_bothBlankReturnsBlank() {
        assertEquals("", pickBetterQuoteText("", ""))
    }

    @Test
    fun joinWords_sameLineWordsAlwaysGetASpace() {
        val words = listOf(WordToken("hello", lineId = 0), WordToken("world", lineId = 0))
        assertEquals("hello world", joinWords(words, 0, 1))
    }

    @Test
    fun joinWords_lineBreakGapDefaultsToSpace() {
        val words = listOf(WordToken("hello", lineId = 0), WordToken("world", lineId = 1))
        assertEquals("hello world", joinWords(words, 0, 1))
    }

    @Test
    fun joinWords_mergedLineBreakGapDropsTheSpace() {
        val words = listOf(WordToken("exam", lineId = 0), WordToken("ple", lineId = 1))
        assertEquals("example", joinWords(words, 0, 1, mergedLineBreakGaps = setOf(0)))
    }

    @Test
    fun joinWords_onlyTheTargetedGapIsMerged() {
        val words = listOf(
            WordToken("exam", lineId = 0),
            WordToken("ple", lineId = 1),
            WordToken("text", lineId = 2),
        )
        assertEquals("example text", joinWords(words, 0, 2, mergedLineBreakGaps = setOf(0)))
    }

    @Test
    fun joinWords_reversedIndicesAreNormalized() {
        val words = listOf(WordToken("hello", lineId = 0), WordToken("world", lineId = 0))
        assertEquals("hello world", joinWords(words, 1, 0))
    }

    @Test
    fun joinWords_singleWordSelection() {
        val words = listOf(WordToken("hello", lineId = 0), WordToken("world", lineId = 0))
        assertEquals("hello", joinWords(words, 0, 0))
    }

    @Test
    fun joinWords_outOfRangeIndicesAreClamped() {
        val words = listOf(WordToken("hello", lineId = 0), WordToken("world", lineId = 0))
        assertEquals("hello world", joinWords(words, -5, 99))
    }

    @Test
    fun joinWords_emptyWordListReturnsEmptyString() {
        assertEquals("", joinWords(emptyList(), 0, 0))
    }
}
