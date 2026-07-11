package com.dyk1323.booklogs.domain.usecase

import com.dyk1323.booklogs.domain.model.BookFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResolveLoggedPageUseCaseTest {

    @Test
    fun `PHYSICAL input within totalPages succeeds as-is`() {
        val result = resolveLoggedPage(BookFormat.PHYSICAL, totalPages = 320, inputValue = 150)
        assertEquals(ResolveLoggedPageResult.Success(150), result)
    }

    @Test
    fun `PHYSICAL input over totalPages errors`() {
        val result = resolveLoggedPage(BookFormat.PHYSICAL, totalPages = 320, inputValue = 400)
        assertTrue(result is ResolveLoggedPageResult.Error)
    }

    @Test
    fun `PHYSICAL input with unknown totalPages succeeds as-is`() {
        val result = resolveLoggedPage(BookFormat.PHYSICAL, totalPages = null, inputValue = 400)
        assertEquals(ResolveLoggedPageResult.Success(400), result)
    }

    @Test
    fun `EBOOK input converts percent to page using totalPages`() {
        val result = resolveLoggedPage(BookFormat.EBOOK, totalPages = 320, inputValue = 62)
        assertEquals(ResolveLoggedPageResult.Success(198), result)
    }

    @Test
    fun `EBOOK input outside 0 to 100 errors`() {
        val result = resolveLoggedPage(BookFormat.EBOOK, totalPages = 320, inputValue = 150)
        assertTrue(result is ResolveLoggedPageResult.Error)
    }

    @Test
    fun `EBOOK with missing totalPages errors`() {
        val result = resolveLoggedPage(BookFormat.EBOOK, totalPages = null, inputValue = 50)
        assertTrue(result is ResolveLoggedPageResult.Error)
    }

    @Test
    fun `EBOOK with nonpositive totalPages errors`() {
        val result = resolveLoggedPage(BookFormat.EBOOK, totalPages = 0, inputValue = 50)
        assertTrue(result is ResolveLoggedPageResult.Error)
    }
}
