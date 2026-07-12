package com.dyk1323.booklogs.domain.usecase

import com.dyk1323.booklogs.domain.fake.FakeReadingRoundRepository
import com.dyk1323.booklogs.domain.model.ReadingRound
import com.dyk1323.booklogs.domain.model.RoundEndReason
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteRoundUseCaseTest {

    @Test
    fun `deletes a closed round`() = runTest {
        val rounds = FakeReadingRoundRepository(
            listOf(
                ReadingRound(id = 1, bookId = 1, roundNumber = 1, startedAt = 0, finishedAt = 500, endReason = RoundEndReason.COMPLETED),
                ReadingRound(id = 2, bookId = 1, roundNumber = 2, startedAt = 600, finishedAt = null, endReason = null),
            ),
        )
        val useCase = DeleteRoundUseCase(rounds)

        val result = useCase(roundId = 1)

        assertTrue(result.isSuccess)
        assertEquals(listOf(2L), rounds.all().map { it.id })
    }

    @Test
    fun `fails to delete the currently open round`() = runTest {
        val rounds = FakeReadingRoundRepository(
            listOf(ReadingRound(id = 1, bookId = 1, roundNumber = 1, startedAt = 0, finishedAt = null, endReason = null)),
        )
        val useCase = DeleteRoundUseCase(rounds)

        val result = useCase(roundId = 1)

        assertTrue(result.isFailure)
        assertEquals(1, rounds.all().size)
    }

    @Test
    fun `fails when the round does not exist`() = runTest {
        val rounds = FakeReadingRoundRepository()
        val useCase = DeleteRoundUseCase(rounds)

        val result = useCase(roundId = 99)

        assertTrue(result.isFailure)
    }
}
