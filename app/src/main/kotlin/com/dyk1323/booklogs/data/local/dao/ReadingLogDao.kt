package com.dyk1323.booklogs.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.dyk1323.booklogs.data.local.entity.ReadingLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingLogDao {
    /** All logs across every book/round — feeds the dashboard's cross-book daily aggregate. */
    @Query("SELECT * FROM reading_logs")
    fun observeAll(): Flow<List<ReadingLogEntity>>

    @Query("SELECT * FROM reading_logs WHERE reading_round_id = :roundId ORDER BY logged_at DESC LIMIT 1")
    fun observeLatestForRound(roundId: Long): Flow<ReadingLogEntity?>

    @Query("SELECT * FROM reading_logs WHERE reading_round_id = :roundId ORDER BY logged_at ASC")
    suspend fun getAllForRound(roundId: Long): List<ReadingLogEntity>

    @Query("SELECT * FROM reading_logs WHERE id = :logId")
    suspend fun getById(logId: Long): ReadingLogEntity?

    @Insert
    suspend fun insert(log: ReadingLogEntity): Long

    @Update
    suspend fun update(log: ReadingLogEntity)

    @Query("DELETE FROM reading_logs WHERE id = :logId")
    suspend fun deleteById(logId: Long)
}
