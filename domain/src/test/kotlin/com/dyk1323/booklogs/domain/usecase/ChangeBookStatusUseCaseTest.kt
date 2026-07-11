package com.dyk1323.booklogs.domain.usecase

import com.dyk1323.booklogs.domain.fake.FakeBookRepository
import com.dyk1323.booklogs.domain.fake.FakeReadingRoundRepository
import com.dyk1323.booklogs.domain.model.Book
import com.dyk1323.booklogs.domain.model.BookFormat
import com.dyk1323.booklogs.domain.model.BookStatus
import com.dyk1323.booklogs.domain.model.ReadingRound
import com.dyk1323.booklogs.domain.model.RoundEndReason
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChangeBookStatusUseCaseTest {

    private fun book(id: Long = 1, status: BookStatus) = Book(
        id = id, isbn = null, title = "Test Book", author = null, publisher = null,
        coverImageUrl = null, totalPages = 300, status = status, format = BookFormat.PHYSICAL,
        genre = null, country = null, createdAt = 0,
    )

    @Test
    fun `READING to PAUSED leaves the round untouched, only the status changes`() = runTest {
        val books = FakeBookRepository(listOf(book(status = BookStatus.READING)))
        val rounds = FakeReadingRoundRepository(
            listOf(ReadingRound(id = 1, bookId = 1, roundNumber = 1, startedAt = 0, finishedAt = null, endReason = null)),
        )
        val useCase = ChangeBookStatusUseCase(books, rounds)

        val result = useCase(bookId = 1, newStatus = BookStatus.PAUSED, now = 1000)

        assertTrue(result.isSuccess)
        assertEquals(BookStatus.PAUSED, books.all().single().status)
        assertEquals(1, rounds.all().size)
        assertNull(rounds.all().single().finishedAt)
    }

    @Test
    fun `PAUSED to READING resumes the same open round`() = runTest {
        val books = FakeBookRepository(listOf(book(status = BookStatus.PAUSED)))
        val rounds = FakeReadingRoundRepository(
            listOf(ReadingRound(id = 1, bookId = 1, roundNumber = 1, startedAt = 0, finishedAt = null, endReason = null)),
        )
        val useCase = ChangeBookStatusUseCase(books, rounds)

        useCase(bookId = 1, newStatus = BookStatus.READING, now = 1000)

        assertEquals(BookStatus.READING, books.all().single().status)
        assertEquals(1, rounds.all().size)
    }

    @Test
    fun `READING to FINISHED closes the open round with endReason COMPLETED`() = runTest {
        val books = FakeBookRepository(listOf(book(status = BookStatus.READING)))
        val rounds = FakeReadingRoundRepository(
            listOf(ReadingRound(id = 1, bookId = 1, roundNumber = 1, startedAt = 0, finishedAt = null, endReason = null)),
        )
        val useCase = ChangeBookStatusUseCase(books, rounds)

        useCase(bookId = 1, newStatus = BookStatus.FINISHED, now = 5000)

        val round = rounds.all().single()
        assertEquals(5000L, round.finishedAt)
        assertEquals(RoundEndReason.COMPLETED, round.endReason)
        assertEquals(BookStatus.FINISHED, books.all().single().status)
    }

    @Test
    fun `READING to DROPPED closes the open round with endReason DROPPED`() = runTest {
        val books = FakeBookRepository(listOf(book(status = BookStatus.READING)))
        val rounds = FakeReadingRoundRepository(
            listOf(ReadingRound(id = 1, bookId = 1, roundNumber = 1, startedAt = 0, finishedAt = null, endReason = null)),
        )
        val useCase = ChangeBookStatusUseCase(books, rounds)

        useCase(bookId = 1, newStatus = BookStatus.DROPPED, now = 5000)

        val round = rounds.all().single()
        assertEquals(RoundEndReason.DROPPED, round.endReason)
        assertEquals(BookStatus.DROPPED, books.all().single().status)
    }

    @Test
    fun `FINISHED to READING starts a new round with roundNumber incremented`() = runTest {
        val books = FakeBookRepository(listOf(book(status = BookStatus.FINISHED)))
        val rounds = FakeReadingRoundRepository(
            listOf(ReadingRound(id = 1, bookId = 1, roundNumber = 1, startedAt = 0, finishedAt = 100, endReason = RoundEndReason.COMPLETED)),
        )
        val useCase = ChangeBookStatusUseCase(books, rounds)

        useCase(bookId = 1, newStatus = BookStatus.READING, now = 9000)

        assertEquals(2, rounds.all().size)
        val newRound = rounds.all().first { it.finishedAt == null }
        assertEquals(2, newRound.roundNumber)
        assertEquals(9000L, newRound.startedAt)
        assertEquals(BookStatus.READING, books.all().single().status)
    }

    @Test
    fun `DROPPED to READING also starts a new round (re-attempt)`() = runTest {
        val books = FakeBookRepository(listOf(book(status = BookStatus.DROPPED)))
        val rounds = FakeReadingRoundRepository(
            listOf(ReadingRound(id = 1, bookId = 1, roundNumber = 1, startedAt = 0, finishedAt = 100, endReason = RoundEndReason.DROPPED)),
        )
        val useCase = ChangeBookStatusUseCase(books, rounds)

        useCase(bookId = 1, newStatus = BookStatus.READING, now = 9000)

        assertEquals(2, rounds.all().size)
        assertEquals(2, rounds.all().first { it.finishedAt == null }.roundNumber)
    }

    @Test
    fun `PLANNED to READING creates the book's first round`() = runTest {
        val books = FakeBookRepository(listOf(book(status = BookStatus.PLANNED)))
        val rounds = FakeReadingRoundRepository() // no rounds yet
        val useCase = ChangeBookStatusUseCase(books, rounds)

        val result = useCase(bookId = 1, newStatus = BookStatus.READING, now = 9000)

        assertTrue(result.isSuccess)
        val round = rounds.all().single()
        assertEquals(1, round.roundNumber)
        assertNull(round.finishedAt)
        assertEquals(BookStatus.READING, books.all().single().status)
    }

    @Test
    fun `an unsupported transition fails without mutating state`() = runTest {
        val books = FakeBookRepository(listOf(book(status = BookStatus.PLANNED)))
        val rounds = FakeReadingRoundRepository()
        val useCase = ChangeBookStatusUseCase(books, rounds)

        val result = useCase(bookId = 1, newStatus = BookStatus.DROPPED, now = 9000)

        assertTrue(result.isFailure)
        assertEquals(BookStatus.PLANNED, books.all().single().status)
        assertTrue(rounds.all().isEmpty())
    }
}
