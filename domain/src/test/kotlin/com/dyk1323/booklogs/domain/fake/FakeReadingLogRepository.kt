package com.dyk1323.booklogs.domain.fake

import com.dyk1323.booklogs.domain.model.ReadingLog
import com.dyk1323.booklogs.domain.repository.ReadingLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeReadingLogRepository(initial: List<ReadingLog> = emptyList()) : ReadingLogRepository {
    private var nextId = (initial.maxOfOrNull { it.id } ?: 0L) + 1
    private val state = MutableStateFlow(initial.associateBy { it.id })

    override fun observeAll(): Flow<List<ReadingLog>> = state.map { it.values.toList() }

    override fun observeLatestForRound(roundId: Long): Flow<ReadingLog?> =
        state.map { logs -> logs.values.filter { it.readingRoundId == roundId }.maxByOrNull { it.loggedAt } }

    override suspend fun getAllForRound(roundId: Long): List<ReadingLog> =
        state.value.values.filter { it.readingRoundId == roundId }

    override suspend fun getById(logId: Long): ReadingLog? = state.value[logId]

    override suspend fun insert(log: ReadingLog): Long {
        val id = if (log.id != 0L) log.id else nextId++
        state.value = state.value + (id to log.copy(id = id))
        return id
    }

    override suspend fun update(log: ReadingLog) {
        require(state.value.containsKey(log.id)) { "ReadingLog ${log.id} does not exist" }
        state.value = state.value + (log.id to log)
    }

    override suspend fun deleteById(logId: Long) {
        state.value = state.value - logId
    }

    fun all(): List<ReadingLog> = state.value.values.toList()
}
