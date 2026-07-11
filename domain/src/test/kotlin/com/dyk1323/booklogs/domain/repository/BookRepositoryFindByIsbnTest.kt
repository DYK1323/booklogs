package com.dyk1323.booklogs.domain.repository

import com.dyk1323.booklogs.domain.fake.FakeBookRepository
import com.dyk1323.booklogs.domain.model.Book
import com.dyk1323.booklogs.domain.model.BookFormat
import com.dyk1323.booklogs.domain.model.BookStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Covers the duplicate-registration check (docs/PLAN.md "중복 등록 감지"), driven directly by the registration ViewModel. */
class BookRepositoryFindByIsbnTest {

    private fun book(isbn: String?) = Book(
        id = 0, isbn = isbn, title = "Some Book", author = null, publisher = null,
        coverImageUrl = null, totalPages = null, status = BookStatus.PLANNED, format = BookFormat.PHYSICAL,
        genre = null, country = null, createdAt = 0,
    )

    @Test
    fun `finds an already-registered book by isbn`() = runTest {
        val repository = FakeBookRepository()
        repository.insert(book(isbn = "9788937460777"))

        val found = repository.findByIsbn("9788937460777")

        assertEquals("9788937460777", found?.isbn)
    }

    @Test
    fun `returns null when no book has that isbn`() = runTest {
        val repository = FakeBookRepository()
        repository.insert(book(isbn = "9788937460777"))

        assertNull(repository.findByIsbn("0000000000000"))
    }
}
