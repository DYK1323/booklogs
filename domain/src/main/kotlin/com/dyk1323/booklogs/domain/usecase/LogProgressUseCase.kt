package com.dyk1323.booklogs.domain.usecase

import com.dyk1323.booklogs.domain.model.ReadingLog
import com.dyk1323.booklogs.domain.repository.ReadingLogRepository

/** Records a new progress snapshot. A plain insert — no delta is computed or stored (see [computeLogDeltas]). */
class LogProgressUseCase(
    private val readingLogRepository: ReadingLogRepository,
) {
    suspend operator fun invoke(
        bookId: Long,
        readingRoundId: Long,
        currentPage: Int,
        loggedAt: Long,
        logDateEpochDay: Long,
    ): Long {
        require(currentPage >= 0) { "currentPage must be >= 0" }
        return readingLogRepository.insert(
            ReadingLog(
                id = 0,
                bookId = bookId,
                readingRoundId = readingRoundId,
                currentPage = currentPage,
                logDateEpochDay = logDateEpochDay,
                loggedAt = loggedAt,
            ),
        )
    }
}
