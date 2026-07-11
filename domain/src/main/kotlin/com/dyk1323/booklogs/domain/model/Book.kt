package com.dyk1323.booklogs.domain.model

data class Book(
    val id: Long,
    val isbn: String?,
    val title: String,
    val author: String?,
    val publisher: String?,
    val coverImageUrl: String?,
    val totalPages: Int?,
    val status: BookStatus,
    val format: BookFormat,
    val genre: String?,
    val country: String?,
    val createdAt: Long,
)
