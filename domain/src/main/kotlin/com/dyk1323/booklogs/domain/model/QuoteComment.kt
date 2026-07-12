package com.dyk1323.booklogs.domain.model

data class QuoteComment(
    val id: Long,
    val quoteId: Long,
    val content: String,
    val createdAt: Long,
)
