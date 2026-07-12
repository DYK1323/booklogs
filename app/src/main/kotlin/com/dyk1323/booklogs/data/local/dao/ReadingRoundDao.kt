package com.dyk1323.booklogs.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.dyk1323.booklogs.data.local.entity.ReadingRoundEntity

@Dao
interface ReadingRoundDao {
    @Query("SELECT * FROM reading_rounds")
    suspend fun getAll(): List<ReadingRoundEntity>

    @Query("SELECT * FROM reading_rounds WHERE id = :roundId")
    suspend fun getById(roundId: Long): ReadingRoundEntity?

    @Query("SELECT * FROM reading_rounds WHERE book_id = :bookId AND finished_at IS NULL LIMIT 1")
    suspend fun getOpenRound(bookId: Long): ReadingRoundEntity?

    @Query("SELECT * FROM reading_rounds WHERE book_id = :bookId ORDER BY round_number ASC")
    suspend fun getRoundsForBook(bookId: Long): List<ReadingRoundEntity>

    @Insert
    suspend fun insert(round: ReadingRoundEntity): Long

    @Update
    suspend fun update(round: ReadingRoundEntity)

    @Query("DELETE FROM reading_rounds WHERE id = :roundId")
    suspend fun deleteById(roundId: Long)

    @Query("DELETE FROM reading_rounds")
    suspend fun deleteAll()
}
