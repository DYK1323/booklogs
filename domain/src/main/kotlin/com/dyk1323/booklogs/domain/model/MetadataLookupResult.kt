package com.dyk1323.booklogs.domain.model

/** Final classification after combining Kakao + Google Books results, see docs/PLAN.md "메타데이터 연동". */
sealed class MetadataLookupResult {
    data class Found(val metadata: BookMetadata) : MetadataLookupResult()
    data object NotFound : MetadataLookupResult()
    data object NetworkError : MetadataLookupResult()
}
