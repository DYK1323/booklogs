package com.dyk1323.booklogs.domain.usecase

import com.dyk1323.booklogs.domain.fake.FakeBookRepository
import com.dyk1323.booklogs.domain.fake.FakeReadingLogRepository
import com.dyk1323.booklogs.domain.fake.FakeReadingRoundRepository
import com.dyk1323.booklogs.domain.fake.FakeReviewRepository
import com.dyk1323.booklogs.domain.fake.NoopTransactionRunner
import com.dyk1323.booklogs.domain.model.Book
import com.dyk1323.booklogs.domain.model.BookFormat
import com.dyk1323.booklogs.domain.model.BookStatus
import com.dyk1323.booklogs.domain.model.ReadingLog
import com.dyk1323.booklogs.domain.model.ReadingRound
import com.dyk1323.booklogs.domain.model.Review
import com.dyk1323.booklogs.domain.model.RoundEndReason
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UndoRoundSplitUseCaseTest {

    private fun book(status: BookStatus) = Book(
        id = 1, isbn = null, title = "Test Book", author = null, publisher = null,
        coverImageUrl = null, totalPages = 300, status = status, format = BookFormat.PHYSICAL,
        genre = null, country = null, createdAt = 0,
    )

    @Test
    fun `merges the empty current round back into the closed previous round`() = runTest {
        val books = FakeBookRepository(listOf(book(status = BookStatus.READING)))
        val rounds = FakeReadingRoundRepository(
            listOf(
                ReadingRound(id = 1, bookId = 1, roundNumber = 1, startedAt = 0, finishedAt = 100, endReason = RoundEndReason.COMPLETED),
                ReadingRound(id = 2, bookId = 1, roundNumber = 2, startedAt = 100, finishedAt = null, endReason = null),
            ),
        )
        val logs = FakeReadingLogRepository()
        val reviews = FakeReviewRepository()
        val useCase = UndoRoundSplitUseCase(books, rounds, logs, reviews, NoopTransactionRunner())

        val result = useCase(bookId = 1)

        assertTrue(result.isSuccess)
        val remaining = rounds.all()
        assertEquals(listOf(1L), remaining.map { it.id })
        assertNull(remaining.single().finishedAt)
        assertNull(remaining.single().endReason)
        assertEquals(BookStatus.READING, books.all().single().status)
    }

    @Test
    fun `logs and reviews already recorded under the accidental round are reassigned, not lost`() = runTest {
        val books = FakeBookRepository(listOf(book(status = BookStatus.READING)))
        val rounds = FakeReadingRoundRepository(
            listOf(
                ReadingRound(id = 1, bookId = 1, roundNumber = 1, startedAt = 0, finishedAt = 100, endReason = RoundEndReason.COMPLETED),
                ReadingRound(id = 2, bookId = 1, roundNumber = 2, startedAt = 100, finishedAt = null, endReason = null),
            ),
        )
        val logs = FakeReadingLogRepository(
            listOf(ReadingLog(id = 1, bookId = 1, readingRoundId = 2, currentPage = 40, logDateEpochDay = 1, loggedAt = 150)),
        )
        val reviews = FakeReviewRepository(
            listOf(Review(id = 1, bookId = 1, readingRoundId = 2, content = "다시 보니 좋다", rating = null, createdAt = 150)),
        )
        val useCase = UndoRoundSplitUseCase(books, rounds, logs, reviews, NoopTransactionRunner())

        val result = useCase(bookId = 1)

        assertTrue(result.isSuccess)
        assertEquals(1L, logs.all().single().readingRoundId)
        assertEquals(1L, reviews.all().single().readingRoundId)
        assertEquals(1, rounds.all().size)
    }

    @Test
    fun `fails when the book has no open round`() = runTest {
        val books = FakeBookRepository(listOf(book(status = BookStatus.FINISHED)))
        val rounds = FakeReadingRoundRepository(
            listOf(ReadingRound(id = 1, bookId = 1, roundNumber = 1, startedAt = 0, finishedAt = 100, endReason = RoundEndReason.COMPLETED)),
        )
        val useCase = UndoRoundSplitUseCase(
            books, rounds, FakeReadingLogRepository(), FakeReviewRepository(), NoopTransactionRunner(),
        )

        val result = useCase(bookId = 1)

        assertTrue(result.isFailure)
        assertEquals(1, rounds.all().size)
    }

    @Test
    fun `fails when there is no previous round to merge into`() = runTest {
        val books = FakeBookRepository(listOf(book(status = BookStatus.READING)))
        val rounds = FakeReadingRoundRepository(
            listOf(ReadingRound(id = 1, bookId = 1, roundNumber = 1, startedAt = 0, finishedAt = null, endReason = null)),
        )
        val useCase = UndoRoundSplitUseCase(
            books, rounds, FakeReadingLogRepository(), FakeReviewRepository(), NoopTransactionRunner(),
        )

        val result = useCase(bookId = 1)

        assertTrue(result.isFailure)
        assertEquals(1, rounds.all().size)
    }
}
