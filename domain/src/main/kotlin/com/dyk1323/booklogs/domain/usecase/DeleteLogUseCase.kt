package com.dyk1323.booklogs.domain.usecase

import com.dyk1323.booklogs.domain.repository.ReadingLogRepository

/** Deletes any single log — past or present. Neighboring logs are unaffected since deltas are derived on demand. */
class DeleteLogUseCase(
    private val readingLogRepository: ReadingLogRepository,
) {
    suspend operator fun invoke(logId: Long) {
        readingLogRepository.deleteById(logId)
    }
}
