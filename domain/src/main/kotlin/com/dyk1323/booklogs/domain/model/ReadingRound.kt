package com.dyk1323.booklogs.domain.model

data class ReadingRound(
    val id: Long,
    val bookId: Long,
    val roundNumber: Int,
    val startedAt: Long,
    val finishedAt: Long?,
    val endReason: RoundEndReason?,
)
