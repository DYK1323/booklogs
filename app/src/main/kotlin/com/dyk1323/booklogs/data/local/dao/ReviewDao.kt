package com.dyk1323.booklogs.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dyk1323.booklogs.data.local.entity.ReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE book_id = :bookId ORDER BY created_at DESC")
    fun observeForBook(bookId: Long): Flow<List<ReviewEntity>>

    @Query("SELECT * FROM reviews WHERE reading_round_id = :roundId ORDER BY created_at DESC")
    fun observeForRound(roundId: Long): Flow<List<ReviewEntity>>

    @Insert
    suspend fun insert(review: ReviewEntity): Long
}
