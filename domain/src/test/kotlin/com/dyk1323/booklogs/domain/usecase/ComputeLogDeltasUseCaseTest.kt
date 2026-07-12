package com.dyk1323.booklogs.domain.usecase

import com.dyk1323.booklogs.domain.model.ReadingLog
import org.junit.Assert.assertEquals
import org.junit.Test

class ComputeLogDeltasUseCaseTest {

    private fun log(id: Long, page: Int, loggedAt: Long, roundId: Long = 1L, day: Long = loggedAt) =
        ReadingLog(id = id, bookId = 1L, readingRoundId = roundId, currentPage = page, logDateEpochDay = day, loggedAt = loggedAt)

    @Test
    fun `first log's delta is measured from page 0`() {
        val logs = listOf(log(id = 1, page = 20, loggedAt = 100))

        val deltas = computeLogDeltas(logs)

        assertEquals(listOf(20), deltas.map { it.pagesRead })
    }

    @Test
    fun `delta is the increase over the previous log regardless of insertion order`() {
        val logs = listOf(
            log(id = 2, page = 50, loggedAt = 200),
            log(id = 1, page = 20, loggedAt = 100),
            log(id = 3, page = 80, loggedAt = 300),
        )

        val deltas = computeLogDeltas(logs)

        assertEquals(listOf(1L, 2L, 3L), deltas.map { it.log.id })
        assertEquals(listOf(20, 30, 30), deltas.map { it.pagesRead })
    }

    @Test
    fun `a page number that goes backward clamps to zero instead of going negative`() {
        val logs = listOf(
            log(id = 1, page = 100, loggedAt = 100),
            log(id = 2, page = 40, loggedAt = 200), // e.g. a typo correction
        )

        val deltas = computeLogDeltas(logs)

        assertEquals(listOf(100, 0), deltas.map { it.pagesRead })
    }

    @Test
    fun `editing a log in the middle changes both its own delta and the next log's delta on recompute`() {
        val original = listOf(
            log(id = 1, page = 10, loggedAt = 100),
            log(id = 2, page = 30, loggedAt = 200),
            log(id = 3, page = 50, loggedAt = 300),
        )
        assertEquals(listOf(10, 20, 20), computeLogDeltas(original).map { it.pagesRead })

        // Simulate EditLogUseCase changing log 2's page in place (no stored delta to fix up elsewhere).
        val edited = original.map { if (it.id == 2L) it.copy(currentPage = 25) else it }

        val deltas = computeLogDeltas(edited)

        assertEquals(listOf(10, 15, 25), deltas.map { it.pagesRead })
    }

    @Test
    fun `deleting a log in the middle recomputes its neighbors' delta against each other`() {
        val original = listOf(
            log(id = 1, page = 10, loggedAt = 100),
            log(id = 2, page = 30, loggedAt = 200),
            log(id = 3, page = 50, loggedAt = 300),
        )

        val afterDeletingMiddle = original.filterNot { it.id == 2L }

        val deltas = computeLogDeltas(afterDeletingMiddle)

        assertEquals(listOf(1L, 3L), deltas.map { it.log.id })
        assertEquals(listOf(10, 40), deltas.map { it.pagesRead })
    }

    @Test
    fun `empty list produces no deltas`() {
        assertEquals(emptyList<LogDelta>(), computeLogDeltas(emptyList()))
    }

    @Test
    fun `first log's delta is measured from a non-zero startingPage when given`() {
        val logs = listOf(log(id = 1, page = 170, loggedAt = 100))

        val deltas = computeLogDeltas(logs, startingPage = 165)

        assertEquals(listOf(5), deltas.map { it.pagesRead })
    }

    @Test
    fun `startingPage still clamps to zero if the first log's page is lower`() {
        val logs = listOf(log(id = 1, page = 50, loggedAt = 100))

        val deltas = computeLogDeltas(logs, startingPage = 165)

        assertEquals(listOf(0), deltas.map { it.pagesRead })
    }
}
