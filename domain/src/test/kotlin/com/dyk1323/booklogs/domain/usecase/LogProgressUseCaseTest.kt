package com.dyk1323.booklogs.domain.usecase

import com.dyk1323.booklogs.domain.fake.FakeReadingLogRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class LogProgressUseCaseTest {

    @Test
    fun `inserts a plain snapshot log with no delta computation`() = runTest {
        val repository = FakeReadingLogRepository()
        val useCase = LogProgressUseCase(repository)

        val id = useCase(bookId = 1, readingRoundId = 1, currentPage = 42, loggedAt = 1_000, logDateEpochDay = 5)

        val stored = repository.all().single()
        assertEquals(id, stored.id)
        assertEquals(42, stored.currentPage)
        assertEquals(1, stored.bookId)
        assertEquals(1, stored.readingRoundId)
        assertEquals(5, stored.logDateEpochDay)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects a negative page number`() = runTest {
        val useCase = LogProgressUseCase(FakeReadingLogRepository())

        useCase(bookId = 1, readingRoundId = 1, currentPage = -1, loggedAt = 1_000, logDateEpochDay = 5)
    }
}
