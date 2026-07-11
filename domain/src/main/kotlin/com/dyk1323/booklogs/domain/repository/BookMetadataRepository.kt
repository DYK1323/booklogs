package com.dyk1323.booklogs.domain.repository

import com.dyk1323.booklogs.domain.model.ApiLookupResult
import com.dyk1323.booklogs.domain.model.BookMetadata
import com.dyk1323.booklogs.domain.model.MetadataLookupResult

/**
 * Orchestrates the parallel Kakao (primary) + Google Books (totalPages/genre supplement) lookups —
 * see docs/PLAN.md "메타데이터 연동". The :app implementation does the actual network calls and DTO
 * mapping; this interface only deals in domain types so it stays swappable/fake-able like the other
 * repositories.
 */
interface BookMetadataRepository {
    /** Barcode-scan entry point: both APIs queried by ISBN in parallel, merged via [resolveBookMetadata]. */
    suspend fun lookupByIsbn(isbn: String): MetadataLookupResult

    /** Title-search entry point: Kakao only, Google Books retried only if Kakao returns zero results. */
    suspend fun searchByTitle(query: String): ApiLookupResult<List<BookMetadata>>

    /** Called once the user picks one item from [searchByTitle]'s results, to fill in totalPages/genre. */
    suspend fun resolveSelectedCandidate(candidate: BookMetadata): MetadataLookupResult
}
