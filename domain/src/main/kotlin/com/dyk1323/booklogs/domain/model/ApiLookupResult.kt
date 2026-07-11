package com.dyk1323.booklogs.domain.model

/** Outcome of a single metadata provider call (Kakao or Google Books), see docs/PLAN.md "메타데이터 연동". */
sealed class ApiLookupResult<out T> {
    data class Success<T>(val data: T) : ApiLookupResult<T>()
    data object NotFound : ApiLookupResult<Nothing>()
    data object NetworkError : ApiLookupResult<Nothing>()
}
