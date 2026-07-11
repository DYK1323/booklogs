package com.dyk1323.booklogs.domain.model

data class ReadingLog(
    val id: Long,
    val bookId: Long,
    val readingRoundId: Long,
    val currentPage: Int,
    val logDateEpochDay: Long,
    val loggedAt: Long,
)
