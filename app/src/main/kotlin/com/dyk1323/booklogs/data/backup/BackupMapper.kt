package com.dyk1323.booklogs.data.backup

import com.dyk1323.booklogs.data.local.entity.BookEntity
import com.dyk1323.booklogs.data.local.entity.QuoteCommentEntity
import com.dyk1323.booklogs.data.local.entity.QuoteEntity
import com.dyk1323.booklogs.data.local.entity.ReadingLogEntity
import com.dyk1323.booklogs.data.local.entity.ReadingRoundEntity
import com.dyk1323.booklogs.data.local.entity.ReviewEntity

internal fun BookEntity.toBackupDto() = BookBackupDto(
    id = id,
    isbn = isbn,
    title = title,
    author = author,
    publisher = publisher,
    coverImageUrl = coverImageUrl,
    totalPages = totalPages,
    status = status,
    format = format,
    genre = genre,
    country = country,
    createdAt = createdAt,
)

internal fun BookBackupDto.toEntity() = BookEntity(
    id = id,
    isbn = isbn,
    title = title,
    author = author,
    publisher = publisher,
    coverImageUrl = coverImageUrl,
    totalPages = totalPages,
    status = status,
    format = format,
    genre = genre,
    country = country,
    createdAt = createdAt,
)

internal fun ReadingRoundEntity.toBackupDto() = ReadingRoundBackupDto(
    id = id,
    bookId = bookId,
    roundNumber = roundNumber,
    startedAt = startedAt,
    finishedAt = finishedAt,
    endReason = endReason,
)

internal fun ReadingRoundBackupDto.toEntity() = ReadingRoundEntity(
    id = id,
    bookId = bookId,
    roundNumber = roundNumber,
    startedAt = startedAt,
    finishedAt = finishedAt,
    endReason = endReason,
)

internal fun ReadingLogEntity.toBackupDto() = ReadingLogBackupDto(
    id = id,
    bookId = bookId,
    readingRoundId = readingRoundId,
    currentPage = currentPage,
    logDateEpochDay = logDateEpochDay,
    loggedAt = loggedAt,
)

internal fun ReadingLogBackupDto.toEntity() = ReadingLogEntity(
    id = id,
    bookId = bookId,
    readingRoundId = readingRoundId,
    currentPage = currentPage,
    logDateEpochDay = logDateEpochDay,
    loggedAt = loggedAt,
)

internal fun QuoteEntity.toBackupDto() = QuoteBackupDto(
    id = id,
    bookId = bookId,
    text = text,
    pageNumber = pageNumber,
    pageNumberEnd = pageNumberEnd,
    createdAt = createdAt,
)

internal fun QuoteBackupDto.toEntity() = QuoteEntity(
    id = id,
    bookId = bookId,
    text = text,
    pageNumber = pageNumber,
    pageNumberEnd = pageNumberEnd,
    createdAt = createdAt,
)

internal fun ReviewEntity.toBackupDto() = ReviewBackupDto(
    id = id,
    bookId = bookId,
    readingRoundId = readingRoundId,
    content = content,
    rating = rating,
    createdAt = createdAt,
)

internal fun ReviewBackupDto.toEntity() = ReviewEntity(
    id = id,
    bookId = bookId,
    readingRoundId = readingRoundId,
    content = content,
    rating = rating,
    createdAt = createdAt,
)

internal fun QuoteCommentEntity.toBackupDto() = QuoteCommentBackupDto(
    id = id,
    quoteId = quoteId,
    content = content,
    createdAt = createdAt,
)

internal fun QuoteCommentBackupDto.toEntity() = QuoteCommentEntity(
    id = id,
    quoteId = quoteId,
    content = content,
    createdAt = createdAt,
)
