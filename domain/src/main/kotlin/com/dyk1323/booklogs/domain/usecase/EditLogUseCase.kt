package com.dyk1323.booklogs.domain.usecase

import com.dyk1323.booklogs.domain.repository.ReadingLogRepository

/**
 * Updates any single log's currentPage — past or present, not just the latest — since deltas are
 * derived on demand and never stored, so neighboring logs never need to be touched.
 */
class EditLogUseCase(
    private val readingLogRepository: ReadingLogRepository,
) {
    suspend operator fun invoke(logId: Long, newCurrentPage: Int): Result<Unit> {
        require(newCurrentPage >= 0) { "newCurrentPage must be >= 0" }
        val existing = readingLogRepository.getById(logId)
            ?: return Result.failure(NoSuchElementException("ReadingLog $logId not found"))
        readingLogRepository.update(existing.copy(currentPage = newCurrentPage))
        return Result.success(Unit)
    }
}
