package com.dyk1323.booklogs.domain.usecase

import com.dyk1323.booklogs.domain.fake.FakeBookRepository
import com.dyk1323.booklogs.domain.fake.FakeReadingRoundRepository
import com.dyk1323.booklogs.domain.fake.NoopTransactionRunner
import com.dyk1323.booklogs.domain.model.Book
import com.dyk1323.booklogs.domain.model.BookFormat
import com.dyk1323.booklogs.domain.model.BookStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RegisterBookUseCaseTest {

    private fun newBook() = Book(
        id = 0, isbn = "9788937460777", title = "New Book", author = null, publisher = null,
        coverImageUrl = null, totalPages = 300, status = BookStatus.PLANNED, format = BookFormat.PHYSICAL,
        genre = null, country = null, createdAt = 0,
    )

    @Test
    fun `registering as planned only inserts the book, no round`() = runTest {
        val books = FakeBookRepository()
        val rounds = FakeReadingRoundRepository()
        val useCase = RegisterBookUseCase(books, ChangeBookStatusUseCase(books, rounds, NoopTransactionRunner()))

        val result = useCase(newBook(), startReadingImmediately = false, now = 1000)

        assertTrue(result.isSuccess)
        val saved = books.all().single()
        assertEquals(BookStatus.PLANNED, saved.status)
        assertTrue(rounds.all().isEmpty())
    }

    @Test
    fun `registering with start-reading-immediately creates the first round`() = runTest {
        val books = FakeBookRepository()
        val rounds = FakeReadingRoundRepository()
        val useCase = RegisterBookUseCase(books, ChangeBookStatusUseCase(books, rounds, NoopTransactionRunner()))

        val result = useCase(newBook(), startReadingImmediately = true, now = 1000)

        assertTrue(result.isSuccess)
        val saved = books.all().single()
        assertEquals(BookStatus.READING, saved.status)
        val round = rounds.all().single()
        assertEquals(1, round.roundNumber)
        assertNull(round.finishedAt)
    }

    @Test
    fun `the returned id can be used to look the book back up`() = runTest {
        val books = FakeBookRepository()
        val rounds = FakeReadingRoundRepository()
        val useCase = RegisterBookUseCase(books, ChangeBookStatusUseCase(books, rounds, NoopTransactionRunner()))

        val result = useCase(newBook(), startReadingImmediately = true, now = 1000)

        val bookId = result.getOrThrow()
        assertEquals("New Book", books.getById(bookId)?.title)
    }
}
