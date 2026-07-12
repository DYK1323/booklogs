package com.dyk1323.booklogs.domain.repository

import com.dyk1323.booklogs.domain.model.Review
import kotlinx.coroutines.flow.Flow

interface ReviewRepository {
    fun observeForBook(bookId: Long): Flow<List<Review>>
    suspend fun insert(review: Review): Long
    suspend fun update(review: Review)
    suspend fun deleteById(reviewId: Long)
}
