package com.dyk1323.booklogs.domain.usecase

import com.dyk1323.booklogs.domain.fake.FakeReadingRoundRepository
import com.dyk1323.booklogs.domain.model.ReadingRound
import com.dyk1323.booklogs.domain.model.RoundEndReason
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EditRoundUseCaseTest {

    @Test
    fun `edits an open round's startedAt`() = runTest {
        val rounds = FakeReadingRoundRepository(
            listOf(ReadingRound(id = 1, bookId = 1, roundNumber = 1, startedAt = 100, finishedAt = null, endReason = null)),
        )
        val useCase = EditRoundUseCase(rounds)

        val result = useCase(roundId = 1, startedAt = 50, finishedAt = null, endReason = null, startingPage = 0)

        assertTrue(result.isSuccess)
        assertEquals(50L, rounds.all().single().startedAt)
    }

    @Test
    fun `corrects an accidentally-split round's starting page`() = runTest {
        // e.g. a 완독 tap followed by 다시 읽기 defaulted this round's baseline to 0; the reader actually
        // left off at page 165, so setting startingPage=165 is what makes the next log's delta come out right.
        val rounds = FakeReadingRoundRepository(
            listOf(ReadingRound(id = 2, bookId = 1, roundNumber = 2, startedAt = 100, finishedAt = null, endReason = null)),
        )
        val useCase = EditRoundUseCase(rounds)

        val result = useCase(roundId = 2, startedAt = 100, finishedAt = null, endReason = null, startingPage = 165)

        assertTrue(result.isSuccess)
        assertEquals(165, rounds.all().single().startingPage)
    }

    @Test
    fun `edits a closed round's dates and end reason`() = runTest {
        val rounds = FakeReadingRoundRepository(
            listOf(
                ReadingRound(id = 1, bookId = 1, roundNumber = 1, startedAt = 0, finishedAt = 500, endReason = RoundEndReason.COMPLETED),
            ),
        )
        val useCase = EditRoundUseCase(rounds)

        val result = useCase(roundId = 1, startedAt = 10, finishedAt = 400, endReason = RoundEndReason.DROPPED, startingPage = 0)

        assertTrue(result.isSuccess)
        val round = rounds.all().single()
        assertEquals(10L, round.startedAt)
        assertEquals(400L, round.finishedAt)
        assertEquals(RoundEndReason.DROPPED, round.endReason)
    }

    @Test
    fun `fails to close an open round through this editor`() = runTest {
        val rounds = FakeReadingRoundRepository(
            listOf(ReadingRound(id = 1, bookId = 1, roundNumber = 1, startedAt = 0, finishedAt = null, endReason = null)),
        )
        val useCase = EditRoundUseCase(rounds)

        val result = useCase(roundId = 1, startedAt = 0, finishedAt = 500, endReason = RoundEndReason.COMPLETED, startingPage = 0)

        assertTrue(result.isFailure)
        assertEquals(null, rounds.all().single().finishedAt)
    }

    @Test
    fun `fails to reopen a closed round through this editor`() = runTest {
        val rounds = FakeReadingRoundRepository(
            listOf(
                ReadingRound(id = 1, bookId = 1, roundNumber = 1, startedAt = 0, finishedAt = 500, endReason = RoundEndReason.COMPLETED),
            ),
        )
        val useCase = EditRoundUseCase(rounds)

        val result = useCase(roundId = 1, startedAt = 0, finishedAt = null, endReason = null, startingPage = 0)

        assertTrue(result.isFailure)
        assertEquals(500L, rounds.all().single().finishedAt)
    }

    @Test
    fun `fails when startedAt is after finishedAt`() = runTest {
        val rounds = FakeReadingRoundRepository(
            listOf(
                ReadingRound(id = 1, bookId = 1, roundNumber = 1, startedAt = 0, finishedAt = 500, endReason = RoundEndReason.COMPLETED),
            ),
        )
        val useCase = EditRoundUseCase(rounds)

        val result = useCase(roundId = 1, startedAt = 600, finishedAt = 500, endReason = RoundEndReason.COMPLETED, startingPage = 0)

        assertTrue(result.isFailure)
    }

    @Test
    fun `fails when startingPage is negative`() = runTest {
        val rounds = FakeReadingRoundRepository(
            listOf(ReadingRound(id = 1, bookId = 1, roundNumber = 1, startedAt = 0, finishedAt = null, endReason = null)),
        )
        val useCase = EditRoundUseCase(rounds)

        val result = useCase(roundId = 1, startedAt = 0, finishedAt = null, endReason = null, startingPage = -1)

        assertTrue(result.isFailure)
        assertEquals(0, rounds.all().single().startingPage)
    }

    @Test
    fun `fails when the round does not exist`() = runTest {
        val rounds = FakeReadingRoundRepository()
        val useCase = EditRoundUseCase(rounds)

        val result = useCase(roundId = 99, startedAt = 0, finishedAt = null, endReason = null, startingPage = 0)

        assertTrue(result.isFailure)
    }
}
