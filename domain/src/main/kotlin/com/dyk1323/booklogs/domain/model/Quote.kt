package com.dyk1323.booklogs.domain.model

data class Quote(
    val id: Long,
    val bookId: Long,
    val text: String,
    val pageNumber: Int?,
    val pageNumberEnd: Int?,
    val createdAt: Long,
)
