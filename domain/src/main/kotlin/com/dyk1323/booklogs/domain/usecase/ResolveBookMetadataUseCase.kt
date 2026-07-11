package com.dyk1323.booklogs.domain.usecase

import com.dyk1323.booklogs.domain.model.ApiLookupResult
import com.dyk1323.booklogs.domain.model.BookMetadata
import com.dyk1323.booklogs.domain.model.MetadataLookupResult

/**
 * Kakao (primary: title/author/publisher/coverImageUrl/isbn) + Google Books (supplement:
 * totalPages/genre only) always get called in parallel by the :app repository; this pure function
 * decides how to combine their two [ApiLookupResult]s into one [MetadataLookupResult]. The 5 branches
 * mirror docs/PLAN.md "메타데이터 연동(카카오 메인 + Google Books 병렬 보조) — 실패 처리 상세" exactly.
 */
fun resolveBookMetadata(
    kakao: ApiLookupResult<BookMetadata>,
    google: ApiLookupResult<BookMetadata>,
): MetadataLookupResult = when {
    kakao is ApiLookupResult.Success && google is ApiLookupResult.Success ->
        MetadataLookupResult.Found(mergeBookMetadata(kakao.data, google.data))

    kakao is ApiLookupResult.Success -> MetadataLookupResult.Found(kakao.data)

    google is ApiLookupResult.Success -> MetadataLookupResult.Found(google.data)

    kakao is ApiLookupResult.NetworkError && google is ApiLookupResult.NetworkError ->
        MetadataLookupResult.NetworkError

    else -> MetadataLookupResult.NotFound
}

/** Kakao's fields win; Google Books only ever supplies totalPages/genre on top. */
fun mergeBookMetadata(kakao: BookMetadata, google: BookMetadata?): BookMetadata = kakao.copy(
    totalPages = google?.totalPages ?: kakao.totalPages,
    genre = google?.genre ?: kakao.genre,
)
