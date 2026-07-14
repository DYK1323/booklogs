package com.dyk1323.booklogs.data.repository

import com.dyk1323.booklogs.data.local.dao.QuoteCommentDao
import com.dyk1323.booklogs.data.local.entity.QuoteCommentEntity
import com.dyk1323.booklogs.domain.model.QuoteComment
import com.dyk1323.booklogs.domain.repository.QuoteCommentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class QuoteCommentRepositoryImpl(
    private val quoteCommentDao: QuoteCommentDao,
) : QuoteCommentRepository {
    override fun observeForQuote(quoteId: Long): Flow<List<QuoteComment>> =
        quoteCommentDao.observeForQuote(quoteId).map { entities -> entities.map { it.toDomain() } }

    override fun observeCountsForBook(bookId: Long): Flow<Map<Long, Int>> =
        quoteCommentDao.observeCountsForBook(bookId).map { counts ->
            counts.associate { it.quoteId to it.count }
        }

    override suspend fun insert(comment: QuoteComment): Long = quoteCommentDao.insert(comment.toEntity())

    override suspend fun deleteById(commentId: Long) = quoteCommentDao.deleteById(commentId)
}

internal fun QuoteCommentEntity.toDomain(): QuoteComment = QuoteComment(
    id = id,
    quoteId = quoteId,
    content = content,
    createdAt = createdAt,
)

internal fun QuoteComment.toEntity(): QuoteCommentEntity = QuoteCommentEntity(
    id = id,
    quoteId = quoteId,
    content = content,
    createdAt = createdAt,
)
