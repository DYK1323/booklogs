package com.dyk1323.booklogs.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** `GET /books/v1/volumes` response shape — https://developers.google.com/books/docs/v1/using#WorkingVolumes */
@Serializable
data class GoogleBooksResponse(
    @SerialName("items") val items: List<GoogleBookItemDto> = emptyList(),
)

@Serializable
data class GoogleBookItemDto(
    @SerialName("volumeInfo") val volumeInfo: GoogleVolumeInfoDto,
)

@Serializable
data class GoogleVolumeInfoDto(
    @SerialName("title") val title: String? = null,
    @SerialName("authors") val authors: List<String> = emptyList(),
    @SerialName("publisher") val publisher: String? = null,
    @SerialName("industryIdentifiers") val industryIdentifiers: List<GoogleIndustryIdentifierDto> = emptyList(),
    @SerialName("pageCount") val pageCount: Int? = null,
    @SerialName("categories") val categories: List<String> = emptyList(),
    @SerialName("imageLinks") val imageLinks: GoogleImageLinksDto? = null,
)

@Serializable
data class GoogleIndustryIdentifierDto(
    @SerialName("type") val type: String? = null,
    @SerialName("identifier") val identifier: String? = null,
)

@Serializable
data class GoogleImageLinksDto(
    @SerialName("thumbnail") val thumbnail: String? = null,
)
