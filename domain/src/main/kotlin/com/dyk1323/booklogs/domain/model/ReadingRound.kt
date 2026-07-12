package com.dyk1323.booklogs.domain.model

data class ReadingRound(
    val id: Long,
    val bookId: Long,
    val roundNumber: Int,
    val startedAt: Long,
    val finishedAt: Long?,
    val endReason: RoundEndReason?,
    /** Delta baseline for this round's first log — see [com.dyk1323.booklogs.domain.usecase.computeLogDeltas]. */
    val startingPage: Int = 0,
)
