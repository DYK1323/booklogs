package com.dyk1323.booklogs.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.dyk1323.booklogs.data.local.entity.QuoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuoteDao {
    @Query("SELECT * FROM quotes WHERE book_id = :bookId ORDER BY created_at DESC")
    fun observeForBook(bookId: Long): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes")
    suspend fun getAll(): List<QuoteEntity>

    @Insert
    suspend fun insert(quote: QuoteEntity): Long

    @Update
    suspend fun update(quote: QuoteEntity)

    @Query("DELETE FROM quotes WHERE id = :quoteId")
    suspend fun deleteById(quoteId: Long)

    @Query("DELETE FROM quotes")
    suspend fun deleteAll()
}
