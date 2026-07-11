package com.dyk1323.booklogs.domain.repository

import com.dyk1323.booklogs.domain.model.ReadingLog
import kotlinx.coroutines.flow.Flow

interface ReadingLogRepository {
    /** All logs across every book/round — the source for the dashboard's cross-book daily aggregate. */
    fun observeAll(): Flow<List<ReadingLog>>
    fun observeLatestForRound(roundId: Long): Flow<ReadingLog?>
    suspend fun getAllForRound(roundId: Long): List<ReadingLog>
    suspend fun getById(logId: Long): ReadingLog?
    suspend fun insert(log: ReadingLog): Long
    suspend fun update(log: ReadingLog)
    suspend fun deleteById(logId: Long)
}
