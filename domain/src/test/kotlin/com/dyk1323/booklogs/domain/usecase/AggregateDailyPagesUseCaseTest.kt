package com.dyk1323.booklogs.domain.usecase

import com.dyk1323.booklogs.domain.model.ReadingLog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AggregateDailyPagesUseCaseTest {

    private fun log(id: Long, bookRoundId: Long, page: Int, day: Long) =
        ReadingLog(id = id, bookId = bookRoundId, readingRoundId = bookRoundId, currentPage = page, logDateEpochDay = day, loggedAt = day * 100_000)

    @Test
    fun `sums pages across multiple books on the same day`() {
        val logs = listOf(
            log(id = 1, bookRoundId = 1, page = 20, day = 10), // book 1, round 1: +20
            log(id = 2, bookRoundId = 2, page = 15, day = 10), // book 2, round 2: +15
        )

        val result = aggregateDailyPages(logs, startEpochDay = 10, endEpochDay = 10)

        assertEquals(1, result.size)
        assertEquals(35, result.single().totalPages)
    }

    @Test
    fun `zero-fills days with no logs instead of compressing gaps`() {
        val logs = listOf(log(id = 1, bookRoundId = 1, page = 20, day = 10))

        val result = aggregateDailyPages(logs, startEpochDay = 8, endEpochDay = 12)

        assertEquals(listOf(8L, 9L, 10L, 11L, 12L), result.map { it.epochDay })
        assertEquals(listOf(0, 0, 20, 0, 0), result.map { it.totalPages })
    }

    @Test
    fun `a log outside the display window still contributes to the delta at the window boundary`() {
        // Round 1's previous log is on day 5 (page 30), outside the requested [10, 12] window.
        val logs = listOf(
            log(id = 1, bookRoundId = 1, page = 30, day = 5),
            log(id = 2, bookRoundId = 1, page = 50, day = 10), // delta should be 20, not 50
        )

        val result = aggregateDailyPages(logs, startEpochDay = 10, endEpochDay = 12)

        assertEquals(20, result.first { it.epochDay == 10L }.totalPages)
    }

    @Test
    fun `goalMet is true only when the day's total reaches the goal`() {
        val logs = listOf(
            log(id = 1, bookRoundId = 1, page = 50, day = 1),
            log(id = 2, bookRoundId = 1, page = 60, day = 2), // +10, below goal
        )

        val result = aggregateDailyPages(logs, startEpochDay = 1, endEpochDay = 2, dailyGoalPages = 50)

        assertTrue(result.first { it.epochDay == 1L }.goalMet)
        assertFalse(result.first { it.epochDay == 2L }.goalMet)
    }

    @Test
    fun `no goal set means goalMet is always false`() {
        val logs = listOf(log(id = 1, bookRoundId = 1, page = 200, day = 1))

        val result = aggregateDailyPages(logs, startEpochDay = 1, endEpochDay = 1, dailyGoalPages = null)

        assertFalse(result.single().goalMet)
    }
}
