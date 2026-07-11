package com.dyk1323.booklogs.domain.usecase

import com.dyk1323.booklogs.domain.fake.FakeReadingLogRepository
import com.dyk1323.booklogs.domain.model.ReadingLog
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteLogUseCaseTest {

    @Test
    fun `deletes an arbitrary log without touching neighboring logs`() = runTest {
        val repository = FakeReadingLogRepository(
            listOf(
                ReadingLog(id = 1, bookId = 1, readingRoundId = 1, currentPage = 10, logDateEpochDay = 1, loggedAt = 100),
                ReadingLog(id = 2, bookId = 1, readingRoundId = 1, currentPage = 30, logDateEpochDay = 2, loggedAt = 200),
                ReadingLog(id = 3, bookId = 1, readingRoundId = 1, currentPage = 50, logDateEpochDay = 3, loggedAt = 300),
            ),
        )
        val useCase = DeleteLogUseCase(repository)

        useCase(logId = 2) // deleting the middle log, not the latest one

        val remaining = repository.all().map { it.id }.sorted()
        assertEquals(listOf(1L, 3L), remaining)
    }

    @Test
    fun `deleting a nonexistent id is a no-op, not an error`() = runTest {
        val repository = FakeReadingLogRepository()
        val useCase = DeleteLogUseCase(repository)

        useCase(logId = 999)

        assertTrue(repository.all().isEmpty())
    }
}
