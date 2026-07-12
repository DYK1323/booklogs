package com.dyk1323.booklogs.domain.usecase

import com.dyk1323.booklogs.domain.repository.ReadingRoundRepository

/**
 * Deletes a round from "라운드 이력"(docs/PLAN.md) — e.g. an accidentally-created empty round, or a
 * historical round the reader just wants gone rather than corrected. Only ever deletes a *closed* round:
 * the currently open round is what keeps `Book.status == READING/PAUSED` meaningful (there must always be
 * an open round to log progress against), and closing/reopening a round is [ChangeBookStatusUseCase]'s
 * job, not this screen's — deleting the open round here would silently desync the two. The round's own
 * [com.dyk1323.booklogs.domain.model.ReadingLog] rows cascade-delete with it (Room `onDelete = CASCADE`).
 */
class DeleteRoundUseCase(
    private val roundRepository: ReadingRoundRepository,
) {
    suspend operator fun invoke(roundId: Long): Result<Unit> {
        val round = roundRepository.getById(roundId)
            ?: return Result.failure(NoSuchElementException("ReadingRound $roundId not found"))
        if (round.finishedAt == null) {
            return Result.failure(IllegalArgumentException("Cannot delete the currently open round"))
        }
        roundRepository.deleteById(roundId)
        return Result.success(Unit)
    }
}
