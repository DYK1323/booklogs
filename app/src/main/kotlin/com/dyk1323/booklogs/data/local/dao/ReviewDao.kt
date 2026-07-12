package com.dyk1323.booklogs.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.dyk1323.booklogs.data.local.entity.ReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE book_id = :bookId ORDER BY created_at DESC")
    fun observeForBook(bookId: Long): Flow<List<ReviewEntity>>

    @Query("SELECT * FROM reviews WHERE reading_round_id = :roundId ORDER BY created_at DESC")
    fun observeForRound(roundId: Long): Flow<List<ReviewEntity>>

    @Query("SELECT * FROM reviews WHERE reading_round_id = :roundId")
    suspend fun getAllForRound(roundId: Long): List<ReviewEntity>

    @Query("SELECT * FROM reviews")
    suspend fun getAll(): List<ReviewEntity>

    @Insert
    suspend fun insert(review: ReviewEntity): Long

    @Update
    suspend fun update(review: ReviewEntity)

    @Query("DELETE FROM reviews WHERE id = :reviewId")
    suspend fun deleteById(reviewId: Long)

    @Query("DELETE FROM reviews")
    suspend fun deleteAll()
}
