package com.dyk1323.booklogs.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dyk1323.booklogs.data.local.entity.QuoteCommentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuoteCommentDao {
    @Query("SELECT * FROM quote_comments WHERE quote_id = :quoteId ORDER BY created_at ASC")
    fun observeForQuote(quoteId: Long): Flow<List<QuoteCommentEntity>>

    @Query(
        """
        SELECT qc.quote_id AS quoteId, COUNT(*) AS count
        FROM quote_comments qc
        INNER JOIN quotes q ON q.id = qc.quote_id
        WHERE q.book_id = :bookId
        GROUP BY qc.quote_id
        """,
    )
    fun observeCountsForBook(bookId: Long): Flow<List<QuoteCommentCount>>

    @Query("SELECT * FROM quote_comments")
    suspend fun getAll(): List<QuoteCommentEntity>

    @Insert
    suspend fun insert(comment: QuoteCommentEntity): Long

    @Query("DELETE FROM quote_comments WHERE id = :commentId")
    suspend fun deleteById(commentId: Long)

    @Query("DELETE FROM quote_comments")
    suspend fun deleteAll()
}

data class QuoteCommentCount(
    val quoteId: Long,
    val count: Int,
)
