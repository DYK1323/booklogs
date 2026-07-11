package com.dyk1323.booklogs.domain.usecase

import com.dyk1323.booklogs.domain.model.BookStatus
import com.dyk1323.booklogs.domain.model.ReadingRound
import com.dyk1323.booklogs.domain.model.RoundEndReason
import com.dyk1323.booklogs.domain.repository.BookRepository
import com.dyk1323.booklogs.domain.repository.ReadingRoundRepository

/**
 * Handles every book status transition and its round side effect:
 * - READING <-> PAUSED: the round is left open, only the book's status changes.
 * - READING -> FINISHED/DROPPED: the open round is closed with the matching [RoundEndReason].
 * - FINISHED/DROPPED -> READING: a new round is created (roundNumber = previous max + 1) — a re-read.
 * - PLANNED -> READING: the book's first round is created (roundNumber = 1) — same mechanism as
 *   FINISHED/DROPPED -> READING, just starting from zero rounds instead of restarting.
 *
 * The two repository calls per transition (round + book) must be wrapped in a single transaction by
 * the concrete repository implementations in the :app module — this use case only sequences the calls.
 */
class ChangeBookStatusUseCase(
    private val bookRepository: BookRepository,
    private val roundRepository: ReadingRoundRepository,
) {
    suspend operator fun invoke(bookId: Long, newStatus: BookStatus, now: Long): Result<Unit> {
        val book = bookRepository.getById(bookId)
            ?: return Result.failure(NoSuchElementException("Book $bookId not found"))
        val from = book.status

        when {
            from == BookStatus.READING && newStatus == BookStatus.PAUSED -> {
                bookRepository.update(book.copy(status = newStatus))
            }

            from == BookStatus.READING && (newStatus == BookStatus.FINISHED || newStatus == BookStatus.DROPPED) -> {
                val openRound = roundRepository.getOpenRound(bookId)
                    ?: return Result.failure(IllegalStateException("Book $bookId is READING but has no open round"))
                val endReason = if (newStatus == BookStatus.FINISHED) RoundEndReason.COMPLETED else RoundEndReason.DROPPED
                roundRepository.update(openRound.copy(finishedAt = now, endReason = endReason))
                bookRepository.update(book.copy(status = newStatus))
            }

            from == BookStatus.PAUSED && newStatus == BookStatus.READING -> {
                bookRepository.update(book.copy(status = newStatus))
            }

            (from == BookStatus.FINISHED || from == BookStatus.DROPPED) && newStatus == BookStatus.READING -> {
                startNewRound(bookId, now)
                bookRepository.update(book.copy(status = newStatus))
            }

            from == BookStatus.PLANNED && newStatus == BookStatus.READING -> {
                startNewRound(bookId, now)
                bookRepository.update(book.copy(status = newStatus))
            }

            else -> return Result.failure(
                IllegalArgumentException("Unsupported status transition from $from to $newStatus"),
            )
        }
        return Result.success(Unit)
    }

    private suspend fun startNewRound(bookId: Long, now: Long) {
        val rounds = roundRepository.getRoundsForBook(bookId)
        val nextRoundNumber = (rounds.maxOfOrNull { it.roundNumber } ?: 0) + 1
        roundRepository.insert(
            ReadingRound(
                id = 0,
                bookId = bookId,
                roundNumber = nextRoundNumber,
                startedAt = now,
                finishedAt = null,
                endReason = null,
            ),
        )
    }
}
