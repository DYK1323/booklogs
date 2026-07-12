package com.dyk1323.booklogs.domain.usecase

import com.dyk1323.booklogs.domain.model.ReadingLog

data class LogDelta(
    val log: ReadingLog,
    val pagesRead: Int,
)

/**
 * Derives per-log page deltas for a single reading round. Logs are sorted by [ReadingLog.loggedAt];
 * each delta is the increase over the previous log's currentPage (the first log's delta is measured
 * from [startingPage] — [ReadingRound.startingPage], defaulting to 0 for "started from the beginning"),
 * clamped to zero so a correction that lowers the page number never produces a negative bar in the
 * aggregate chart.
 *
 * Deltas are intentionally not persisted anywhere (see ReadingLogEntity in docs/PLAN.md) so editing or
 * deleting any single log never requires touching its neighbors — this function is what re-derives the
 * delta on demand. [startingPage] being editable (docs/PLAN.md "라운드 이력 편집") is also the fix for a
 * round that was split by mistake (e.g. an accidental 완독 → 다시 읽기): correcting the new round's
 * starting page to where the reader actually left off makes its first log's delta come out right,
 * without needing to merge or delete the round at all.
 */
fun computeLogDeltas(logsForRound: List<ReadingLog>, startingPage: Int = 0): List<LogDelta> {
    val sorted = logsForRound.sortedBy { it.loggedAt }
    var previousPage = startingPage
    return sorted.map { log ->
        val delta = (log.currentPage - previousPage).coerceAtLeast(0)
        previousPage = log.currentPage
        LogDelta(log, delta)
    }
}
