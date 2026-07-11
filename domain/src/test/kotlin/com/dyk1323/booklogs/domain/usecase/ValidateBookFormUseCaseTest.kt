package com.dyk1323.booklogs.domain.usecase

import com.dyk1323.booklogs.domain.model.BookFormat
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidateBookFormUseCaseTest {

    @Test
    fun `PHYSICAL with no totalPages is valid`() {
        assertNull(validateBookForm(BookFormat.PHYSICAL, totalPages = null))
    }

    @Test
    fun `EBOOK with a positive totalPages is valid`() {
        assertNull(validateBookForm(BookFormat.EBOOK, totalPages = 320))
    }

    @Test
    fun `EBOOK with no totalPages is invalid`() {
        assertNotNull(validateBookForm(BookFormat.EBOOK, totalPages = null))
    }

    @Test
    fun `EBOOK with nonpositive totalPages is invalid`() {
        assertNotNull(validateBookForm(BookFormat.EBOOK, totalPages = 0))
    }
}
