package com.dyk1323.booklogs.data.repository

import com.dyk1323.booklogs.data.local.dao.QuoteDao
import com.dyk1323.booklogs.data.local.entity.QuoteEntity
import com.dyk1323.booklogs.domain.model.Quote
import com.dyk1323.booklogs.domain.repository.QuoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class QuoteRepositoryImpl(
    private val quoteDao: QuoteDao,
) : QuoteRepository {
    override fun observeForBook(bookId: Long): Flow<List<Quote>> =
        quoteDao.observeForBook(bookId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun insert(quote: Quote): Long = quoteDao.insert(quote.toEntity())

    override suspend fun deleteById(quoteId: Long) = quoteDao.deleteById(quoteId)
}

internal fun QuoteEntity.toDomain(): Quote = Quote(
    id = id,
    bookId = bookId,
    text = text,
    pageNumber = pageNumber,
    pageNumberEnd = pageNumberEnd,
    createdAt = createdAt,
)

internal fun Quote.toEntity(): QuoteEntity = QuoteEntity(
    id = id,
    bookId = bookId,
    text = text,
    pageNumber = pageNumber,
    pageNumberEnd = pageNumberEnd,
    createdAt = createdAt,
)
