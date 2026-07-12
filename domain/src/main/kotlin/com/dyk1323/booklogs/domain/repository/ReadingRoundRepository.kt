package com.dyk1323.booklogs.domain.repository

import com.dyk1323.booklogs.domain.model.ReadingRound
import kotlinx.coroutines.flow.Flow

interface ReadingRoundRepository {
    /** Every round across every book — the dashboard's cross-book daily-pages aggregate needs every
     * round's [ReadingRound.startingPage] to compute deltas correctly, mirroring [com.dyk1323.booklogs.domain.repository.ReadingLogRepository.observeAll]. */
    fun observeAll(): Flow<List<ReadingRound>>
    fun observeForBook(bookId: Long): Flow<List<ReadingRound>>

    suspend fun getById(roundId: Long): ReadingRound?

    /** The currently open round for a book (finishedAt == null), or null if the book has none yet (e.g. PLANNED). */
    suspend fun getOpenRound(bookId: Long): ReadingRound?
    suspend fun getRoundsForBook(bookId: Long): List<ReadingRound>
    suspend fun insert(round: ReadingRound): Long
    suspend fun update(round: ReadingRound)
    suspend fun deleteById(roundId: Long)
}
