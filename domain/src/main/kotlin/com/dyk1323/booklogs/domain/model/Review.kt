package com.dyk1323.booklogs.domain.model

data class Review(
    val id: Long,
    val bookId: Long,
    val readingRoundId: Long,
    val content: String,
    val rating: Int?,
    val createdAt: Long,
)
