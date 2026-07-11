package com.dyk1323.booklogs.data.remote

import com.dyk1323.booklogs.data.remote.dto.GoogleVolumeInfoDto
import com.dyk1323.booklogs.data.remote.dto.KakaoBookDto
import com.dyk1323.booklogs.domain.model.BookMetadata

/**
 * Thin DTO -> domain field mapping only (no merge/failure-branch decisions — that's
 * [com.dyk1323.booklogs.domain.usecase.resolveBookMetadata] in :domain, which is unit-tested there).
 */

fun KakaoBookDto.toBookMetadata(): BookMetadata = BookMetadata(
    isbn = extractIsbn13(isbn),
    title = title,
    author = authors.takeIf { it.isNotEmpty() }?.joinToString(", "),
    publisher = publisher,
    coverImageUrl = thumbnail?.takeIf { it.isNotBlank() },
    totalPages = null,
    genre = null,
)

fun GoogleVolumeInfoDto.toBookMetadata(): BookMetadata = BookMetadata(
    isbn = null,
    title = title.orEmpty(),
    author = authors.takeIf { it.isNotEmpty() }?.joinToString(", "),
    publisher = publisher,
    coverImageUrl = imageLinks?.thumbnail?.takeIf { it.isNotBlank() },
    totalPages = pageCount,
    genre = categories.firstOrNull(),
)

/** Kakao's `isbn` field holds a space-separated "{ISBN-10} {ISBN-13}" (or just one of the two). */
internal fun extractIsbn13(rawIsbn: String?): String? =
    rawIsbn?.split(" ")?.map { it.trim() }?.firstOrNull { it.length == 13 }
