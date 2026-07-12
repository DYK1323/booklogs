package com.dyk1323.booklogs.domain.fake

import com.dyk1323.booklogs.domain.model.ReadingRound
import com.dyk1323.booklogs.domain.repository.ReadingRoundRepository

class FakeReadingRoundRepository(initial: List<ReadingRound> = emptyList()) : ReadingRoundRepository {
    private var nextId = (initial.maxOfOrNull { it.id } ?: 0L) + 1
    private val rounds = initial.associateBy { it.id }.toMutableMap()

    override suspend fun getById(roundId: Long): ReadingRound? = rounds[roundId]

    override suspend fun getOpenRound(bookId: Long): ReadingRound? =
        rounds.values.firstOrNull { it.bookId == bookId && it.finishedAt == null }

    override suspend fun getRoundsForBook(bookId: Long): List<ReadingRound> =
        rounds.values.filter { it.bookId == bookId }

    override suspend fun insert(round: ReadingRound): Long {
        val id = if (round.id != 0L) round.id else nextId++
        rounds[id] = round.copy(id = id)
        return id
    }

    override suspend fun update(round: ReadingRound) {
        require(rounds.containsKey(round.id)) { "ReadingRound ${round.id} does not exist" }
        rounds[round.id] = round
    }

    override suspend fun deleteById(roundId: Long) {
        rounds.remove(roundId)
    }

    fun all(): List<ReadingRound> = rounds.values.toList()
}
