package com.dyk1323.booklogs.domain.usecase

import com.dyk1323.booklogs.domain.model.RoundEndReason
import com.dyk1323.booklogs.domain.repository.ReadingRoundRepository

/**
 * Corrects a round's dates/종료 사유/시작 페이지 after the fact (docs/PLAN.md "라운드 이력 편집") — e.g.
 * the reader only remembers to log a finish a few days late and wants the date on record to be accurate,
 * or a round got split by an accidental 완독 → 다시 읽기 and its starting page needs to be moved from the
 * default 0 up to wherever the reader actually left off (see [computeLogDeltas]). This deliberately
 * cannot open or close a round: whether a round is open (`finishedAt == null`) must stay exactly as it
 * was, since that's the book's status state machine's job ([ChangeBookStatusUseCase]) and flipping it
 * here would desync `Book.status` from the round data without going through it.
 */
class EditRoundUseCase(
    private val roundRepository: ReadingRoundRepository,
) {
    suspend operator fun invoke(
        roundId: Long,
        startedAt: Long,
        finishedAt: Long?,
        endReason: RoundEndReason?,
        startingPage: Int,
    ): Result<Unit> {
        val round = roundRepository.getById(roundId)
            ?: return Result.failure(NoSuchElementException("ReadingRound $roundId not found"))
        if ((round.finishedAt == null) != (finishedAt == null)) {
            return Result.failure(IllegalArgumentException("Cannot change whether a round is open or closed here"))
        }
        if ((finishedAt == null) != (endReason == null)) {
            return Result.failure(IllegalArgumentException("finishedAt and endReason must be set or cleared together"))
        }
        if (finishedAt != null && startedAt > finishedAt) {
            return Result.failure(IllegalArgumentException("startedAt must not be after finishedAt"))
        }
        if (startingPage < 0) {
            return Result.failure(IllegalArgumentException("startingPage must be >= 0"))
        }
        roundRepository.update(
            round.copy(startedAt = startedAt, finishedAt = finishedAt, endReason = endReason, startingPage = startingPage),
        )
        return Result.success(Unit)
    }
}
