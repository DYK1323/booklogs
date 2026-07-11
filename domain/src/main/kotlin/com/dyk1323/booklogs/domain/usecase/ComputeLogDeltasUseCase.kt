package com.dyk1323.booklogs.domain.usecase

import com.dyk1323.booklogs.domain.model.ReadingLog

data class LogDelta(
    val log: ReadingLog,
    val pagesRead: Int,
)

/**
 * Derives per-log page deltas for a single reading round. Logs are sorted by [ReadingLog.loggedAt];
 * each delta is the increase over the previous log's currentPage (the first log's delta is measured
 * from page 0), clamped to zero so a correction that lowers the page number never produces a negative
 * bar in the aggregate chart.
 *
 * Deltas are intentionally not persisted anywhere (see ReadingLogEntity in docs/PLAN.md) so editing or
 * deleting any single log never requires touching its neighbors — this function is what re-derives the
 * delta on demand.
 */
fun computeLogDeltas(logsForRound: List<ReadingLog>): List<LogDelta> {
    val sorted = logsForRound.sortedBy { it.loggedAt }
    var previousPage = 0
    return sorted.map { log ->
        val delta = (log.currentPage - previousPage).coerceAtLeast(0)
        previousPage = log.currentPage
        LogDelta(log, delta)
    }
}
