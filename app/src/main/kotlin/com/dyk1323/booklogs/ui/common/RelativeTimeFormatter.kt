package com.dyk1323.booklogs.ui.common

import java.util.concurrent.TimeUnit

/** Formats a past timestamp as a short relative caption for the quick-log sheet's "최근 기록" line. */
fun formatRelativeTime(loggedAt: Long, now: Long = System.currentTimeMillis()): String {
    val elapsedMillis = (now - loggedAt).coerceAtLeast(0)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis)
    val hours = TimeUnit.MILLISECONDS.toHours(elapsedMillis)
    val days = TimeUnit.MILLISECONDS.toDays(elapsedMillis)
    return when {
        minutes < 1 -> "방금 전"
        hours < 1 -> "${minutes}분 전"
        days < 1 -> "${hours}시간 전"
        else -> "${days}일 전"
    }
}
