package com.dyk1323.booklogs.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** `GET /v3/search/book` response shape — https://developers.kakao.com/docs/latest/ko/daum-search/dev-guide#search-book */
@Serializable
data class KakaoBookSearchResponse(
    @SerialName("documents") val documents: List<KakaoBookDto> = emptyList(),
)

@Serializable
data class KakaoBookDto(
    @SerialName("title") val title: String,
    @SerialName("authors") val authors: List<String> = emptyList(),
    @SerialName("publisher") val publisher: String? = null,
    @SerialName("thumbnail") val thumbnail: String? = null,
    // space-separated ISBN10 and ISBN13, e.g. "8937460777 9788937460777"
    @SerialName("isbn") val isbn: String? = null,
)
