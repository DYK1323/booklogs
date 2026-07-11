package com.dyk1323.booklogs.data.repository

import com.dyk1323.booklogs.data.local.dao.ReadingLogDao
import com.dyk1323.booklogs.data.local.entity.ReadingLogEntity
import com.dyk1323.booklogs.domain.model.ReadingLog
import com.dyk1323.booklogs.domain.repository.ReadingLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReadingLogRepositoryImpl(
    private val readingLogDao: ReadingLogDao,
) : ReadingLogRepository {
    override fun observeAll(): Flow<List<ReadingLog>> =
        readingLogDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeLatestForRound(roundId: Long): Flow<ReadingLog?> =
        readingLogDao.observeLatestForRound(roundId).map { it?.toDomain() }

    override suspend fun getAllForRound(roundId: Long): List<ReadingLog> =
        readingLogDao.getAllForRound(roundId).map { it.toDomain() }

    override suspend fun getById(logId: Long): ReadingLog? = readingLogDao.getById(logId)?.toDomain()

    override suspend fun insert(log: ReadingLog): Long = readingLogDao.insert(log.toEntity())

    override suspend fun update(log: ReadingLog) = readingLogDao.update(log.toEntity())

    override suspend fun deleteById(logId: Long) = readingLogDao.deleteById(logId)
}

internal fun ReadingLogEntity.toDomain(): ReadingLog = ReadingLog(
    id = id,
    bookId = bookId,
    readingRoundId = readingRoundId,
    currentPage = currentPage,
    logDateEpochDay = logDateEpochDay,
    loggedAt = loggedAt,
)

internal fun ReadingLog.toEntity(): ReadingLogEntity = ReadingLogEntity(
    id = id,
    bookId = bookId,
    readingRoundId = readingRoundId,
    currentPage = currentPage,
    logDateEpochDay = logDateEpochDay,
    loggedAt = loggedAt,
)
