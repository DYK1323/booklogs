package com.dyk1323.booklogs.domain.fake

import com.dyk1323.booklogs.domain.model.ReadingRound
import com.dyk1323.booklogs.domain.repository.ReadingRoundRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeReadingRoundRepository(initial: List<ReadingRound> = emptyList()) : ReadingRoundRepository {
    private var nextId = (initial.maxOfOrNull { it.id } ?: 0L) + 1
    private val state = MutableStateFlow(initial.associateBy { it.id })

    override fun observeAll(): Flow<List<ReadingRound>> = state.map { it.values.toList() }

    override fun observeForBook(bookId: Long): Flow<List<ReadingRound>> =
        state.map { rounds -> rounds.values.filter { it.bookId == bookId } }

    override suspend fun getById(roundId: Long): ReadingRound? = state.value[roundId]

    override suspend fun getOpenRound(bookId: Long): ReadingRound? =
        state.value.values.firstOrNull { it.bookId == bookId && it.finishedAt == null }

    override suspend fun getRoundsForBook(bookId: Long): List<ReadingRound> =
        state.value.values.filter { it.bookId == bookId }

    override suspend fun insert(round: ReadingRound): Long {
        val id = if (round.id != 0L) round.id else nextId++
        state.value = state.value + (id to round.copy(id = id))
        return id
    }

    override suspend fun update(round: ReadingRound) {
        require(state.value.containsKey(round.id)) { "ReadingRound ${round.id} does not exist" }
        state.value = state.value + (round.id to round)
    }

    override suspend fun deleteById(roundId: Long) {
        state.value = state.value - roundId
    }

    fun all(): List<ReadingRound> = state.value.values.toList()
}
