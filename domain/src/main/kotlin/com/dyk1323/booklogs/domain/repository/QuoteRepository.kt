package com.dyk1323.booklogs.domain.repository

import com.dyk1323.booklogs.domain.model.Quote
import kotlinx.coroutines.flow.Flow

interface QuoteRepository {
    fun observeForBook(bookId: Long): Flow<List<Quote>>
    suspend fun insert(quote: Quote): Long
    suspend fun update(quote: Quote)
    suspend fun deleteById(quoteId: Long)
}
