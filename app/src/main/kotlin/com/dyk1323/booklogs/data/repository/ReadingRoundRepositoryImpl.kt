package com.dyk1323.booklogs.data.repository

import com.dyk1323.booklogs.data.local.dao.ReadingRoundDao
import com.dyk1323.booklogs.data.local.entity.ReadingRoundEntity
import com.dyk1323.booklogs.domain.model.ReadingRound
import com.dyk1323.booklogs.domain.model.RoundEndReason
import com.dyk1323.booklogs.domain.repository.ReadingRoundRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReadingRoundRepositoryImpl(
    private val readingRoundDao: ReadingRoundDao,
) : ReadingRoundRepository {
    override fun observeAll(): Flow<List<ReadingRound>> =
        readingRoundDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeForBook(bookId: Long): Flow<List<ReadingRound>> =
        readingRoundDao.observeForBook(bookId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getById(roundId: Long): ReadingRound? = readingRoundDao.getById(roundId)?.toDomain()

    override suspend fun getOpenRound(bookId: Long): ReadingRound? = readingRoundDao.getOpenRound(bookId)?.toDomain()

    override suspend fun getRoundsForBook(bookId: Long): List<ReadingRound> =
        readingRoundDao.getRoundsForBook(bookId).map { it.toDomain() }

    override suspend fun insert(round: ReadingRound): Long = readingRoundDao.insert(round.toEntity())

    override suspend fun update(round: ReadingRound) = readingRoundDao.update(round.toEntity())

    override suspend fun deleteById(roundId: Long) = readingRoundDao.deleteById(roundId)
}

internal fun ReadingRoundEntity.toDomain(): ReadingRound = ReadingRound(
    id = id,
    bookId = bookId,
    roundNumber = roundNumber,
    startedAt = startedAt,
    finishedAt = finishedAt,
    endReason = endReason?.let { RoundEndReason.valueOf(it) },
    startingPage = startingPage,
)

internal fun ReadingRound.toEntity(): ReadingRoundEntity = ReadingRoundEntity(
    id = id,
    bookId = bookId,
    roundNumber = roundNumber,
    startedAt = startedAt,
    finishedAt = finishedAt,
    endReason = endReason?.name,
    startingPage = startingPage,
)
