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
}
