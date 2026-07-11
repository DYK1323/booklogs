package com.dyk1323.booklogs.data.repository

import com.dyk1323.booklogs.data.local.dao.ReviewDao
import com.dyk1323.booklogs.data.local.entity.ReviewEntity
import com.dyk1323.booklogs.domain.model.Review
import com.dyk1323.booklogs.domain.repository.ReviewRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReviewRepositoryImpl(
    private val reviewDao: ReviewDao,
) : ReviewRepository {
    override fun observeForBook(bookId: Long): Flow<List<Review>> =
        reviewDao.observeForBook(bookId).map { entities -> entities.map { it.toDomain() } }

    override fun observeForRound(roundId: Long): Flow<List<Review>> =
        reviewDao.observeForRound(roundId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun insert(review: Review): Long = reviewDao.insert(review.toEntity())
}

internal fun ReviewEntity.toDomain(): Review = Review(
    id = id,
    bookId = bookId,
    readingRoundId = readingRoundId,
    content = content,
    rating = rating,
    createdAt = createdAt,
)

internal fun Review.toEntity(): ReviewEntity = ReviewEntity(
    id = id,
    bookId = bookId,
    readingRoundId = readingRoundId,
    content = content,
    rating = rating,
    createdAt = createdAt,
)
