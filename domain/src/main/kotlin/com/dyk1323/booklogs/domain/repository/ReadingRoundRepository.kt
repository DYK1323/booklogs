package com.dyk1323.booklogs.domain.repository

import com.dyk1323.booklogs.domain.model.ReadingRound

interface ReadingRoundRepository {
    suspend fun getById(roundId: Long): ReadingRound?

    /** The currently open round for a book (finishedAt == null), or null if the book has none yet (e.g. PLANNED). */
    suspend fun getOpenRound(bookId: Long): ReadingRound?
    suspend fun getRoundsForBook(bookId: Long): List<ReadingRound>
    suspend fun insert(round: ReadingRound): Long
    suspend fun update(round: ReadingRound)
}
