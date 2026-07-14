package com.dyk1323.booklogs.domain.repository

import com.dyk1323.booklogs.domain.model.QuoteComment
import kotlinx.coroutines.flow.Flow

interface QuoteCommentRepository {
    fun observeForQuote(quoteId: Long): Flow<List<QuoteComment>>
    fun observeCountsForBook(bookId: Long): Flow<Map<Long, Int>>
    suspend fun insert(comment: QuoteComment): Long
    suspend fun deleteById(commentId: Long)
}
