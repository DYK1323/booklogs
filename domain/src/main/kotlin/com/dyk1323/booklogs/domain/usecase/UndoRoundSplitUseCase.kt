package com.dyk1323.booklogs.domain.usecase

import com.dyk1323.booklogs.domain.model.BookStatus
import com.dyk1323.booklogs.domain.repository.BookRepository
import com.dyk1323.booklogs.domain.repository.ReadingLogRepository
import com.dyk1323.booklogs.domain.repository.ReadingRoundRepository
import com.dyk1323.booklogs.domain.repository.ReviewRepository
import com.dyk1323.booklogs.domain.repository.TransactionRunner

/**
 * Recovery for an accidental 완독/중단 → 다시 읽기 round split (docs/PLAN.md "완독/중단에 확인
 * 다이얼로그 추가"): merges the just-created current round back into the round immediately before it
 * — any logs/reviews already recorded under the current round are reassigned rather than deleted, so
 * this is safe even if the user already logged something before deciding to undo. The previous round is
 * reopened (finishedAt/endReason cleared) and the current round is removed.
 */
class UndoRoundSplitUseCase(
    private val bookRepository: BookRepository,
    private val roundRepository: ReadingRoundRepository,
    private val readingLogRepository: ReadingLogRepository,
    private val reviewRepository: ReviewRepository,
    private val transactionRunner: TransactionRunner,
) {
    suspend operator fun invoke(bookId: Long): Result<Unit> = transactionRunner.run {
        val book = bookRepository.getById(bookId)
            ?: return@run Result.failure(NoSuchElementException("Book $bookId not found"))
        val currentRound = roundRepository.getOpenRound(bookId)
            ?: return@run Result.failure(IllegalStateException("Book $bookId has no open round to undo"))
        val previousRound = roundRepository.getRoundsForBook(bookId)
            .filter { it.roundNumber < currentRound.roundNumber }
            .maxByOrNull { it.roundNumber }
            ?: return@run Result.failure(IllegalStateException("No previous round to merge into"))
        if (previousRound.finishedAt == null) {
            return@run Result.failure(IllegalStateException("Previous round is still open — nothing to undo"))
        }

        readingLogRepository.getAllForRound(currentRound.id).forEach { log ->
            readingLogRepository.update(log.copy(readingRoundId = previousRound.id))
        }
        reviewRepository.getAllForRound(currentRound.id).forEach { review ->
            reviewRepository.update(review.copy(readingRoundId = previousRound.id))
        }

        roundRepository.deleteById(currentRound.id)
        roundRepository.update(previousRound.copy(finishedAt = null, endReason = null))
        bookRepository.update(book.copy(status = BookStatus.READING))
        Result.success(Unit)
    }
}
