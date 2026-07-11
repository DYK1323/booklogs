package com.dyk1323.booklogs.ui.quote

import org.junit.Assert.assertEquals
import org.junit.Test

class QuoteCaptureViewModelTest {

    @Test
    fun joinQuotePages_joinsInCaptureOrderSeparatedByBlankLine() {
        val pages = listOf(
            CapturedQuotePage(order = 1, text = "첫 페이지 문장", pageText = "12"),
            CapturedQuotePage(order = 2, text = "두 번째 페이지 문장", pageText = "13"),
        )

        val result = joinQuotePages(pages)

        assertEquals("첫 페이지 문장\n\n두 번째 페이지 문장", result)
    }

    @Test
    fun joinQuotePages_emptyListReturnsEmptyString() {
        assertEquals("", joinQuotePages(emptyList()))
    }

    @Test
    fun joinQuotePages_singlePageReturnsItsTextUnchanged() {
        val pages = listOf(CapturedQuotePage(order = 1, text = "한 페이지짜리 인용구", pageText = "5"))

        assertEquals("한 페이지짜리 인용구", joinQuotePages(pages))
    }
}
