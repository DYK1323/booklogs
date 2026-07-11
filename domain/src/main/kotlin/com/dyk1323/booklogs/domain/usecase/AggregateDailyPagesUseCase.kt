package com.dyk1323.booklogs.domain.usecase

import com.dyk1323.booklogs.domain.model.ReadingLog

data class DayPageTotal(
    val epochDay: Long,
    val totalPages: Int,
    val goalMet: Boolean,
)

/**
 * Sums pages read per calendar day across every book, for the dashboard's "today's pages" hero number
 * and 7-day bar chart.
 *
 * [allLogs] should include logs outside [startEpochDay]..[endEpochDay] too when available — a delta is
 * computed against the *previous* log in its round regardless of which day that previous log falls on,
 * so trimming the input to the display window before calling this would corrupt the first delta at the
 * window's edge. Filtering to the display range happens only in the returned list.
 *
 * Days with no logged pages are zero-filled so gaps in recording show as gaps in the chart rather than
 * being silently compressed out.
 */
fun aggregateDailyPages(
    allLogs: List<ReadingLog>,
    startEpochDay: Long,
    endEpochDay: Long,
    dailyGoalPages: Int? = null,
): List<DayPageTotal> {
    require(startEpochDay <= endEpochDay) { "startEpochDay must be <= endEpochDay" }

    val totalsByDay: Map<Long, Int> = allLogs
        .groupBy { it.readingRoundId }
        .flatMap { (_, roundLogs) -> computeLogDeltas(roundLogs) }
        .groupBy { it.log.logDateEpochDay }
        .mapValues { (_, deltas) -> deltas.sumOf { it.pagesRead } }

    return (startEpochDay..endEpochDay).map { day ->
        val total = totalsByDay[day] ?: 0
        DayPageTotal(
            epochDay = day,
            totalPages = total,
            goalMet = dailyGoalPages != null && dailyGoalPages > 0 && total >= dailyGoalPages,
        )
    }
}
