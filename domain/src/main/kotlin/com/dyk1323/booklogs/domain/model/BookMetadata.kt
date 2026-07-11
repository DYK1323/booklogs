package com.dyk1323.booklogs.domain.model

/**
 * A single provider's view of a book, already mapped to domain fields (Kakao/Google Books DTO
 * mapping happens in :app — this type is what [ResolveBookMetadataUseCase] merges/classifies).
 */
data class BookMetadata(
    val isbn: String?,
    val title: String,
    val author: String?,
    val publisher: String?,
    val coverImageUrl: String?,
    val totalPages: Int?,
    val genre: String?,
)
