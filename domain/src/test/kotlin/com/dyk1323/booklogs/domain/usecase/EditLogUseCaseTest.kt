package com.dyk1323.booklogs.domain.usecase

import com.dyk1323.booklogs.domain.fake.FakeReadingLogRepository
import com.dyk1323.booklogs.domain.model.ReadingLog
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EditLogUseCaseTest {

    @Test
    fun `updates an arbitrary log's page without touching neighboring logs`() = runTest {
        val repository = FakeReadingLogRepository(
            listOf(
                ReadingLog(id = 1, bookId = 1, readingRoundId = 1, currentPage = 10, logDateEpochDay = 1, loggedAt = 100),
                ReadingLog(id = 2, bookId = 1, readingRoundId = 1, currentPage = 30, logDateEpochDay = 2, loggedAt = 200),
                ReadingLog(id = 3, bookId = 1, readingRoundId = 1, currentPage = 50, logDateEpochDay = 3, loggedAt = 300),
            ),
        )
        val useCase = EditLogUseCase(repository)

        val result = useCase(logId = 1, newCurrentPage = 15) // editing the oldest (non-latest) log

        assertTrue(result.isSuccess)
        assertEquals(15, repository.all().first { it.id == 1L }.currentPage)
        assertEquals(30, repository.all().first { it.id == 2L }.currentPage)
        assertEquals(50, repository.all().first { it.id == 3L }.currentPage)
    }

    @Test
    fun `fails for a nonexistent log id`() = runTest {
        val useCase = EditLogUseCase(FakeReadingLogRepository())

        val result = useCase(logId = 999, newCurrentPage = 10)

        assertTrue(result.isFailure)
    }
}
