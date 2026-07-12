package com.dyk1323.booklogs.domain.fake

import com.dyk1323.booklogs.domain.model.Review
import com.dyk1323.booklogs.domain.repository.ReviewRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeReviewRepository(initial: List<Review> = emptyList()) : ReviewRepository {
    private var nextId = (initial.maxOfOrNull { it.id } ?: 0L) + 1
    private val state = MutableStateFlow(initial.associateBy { it.id })

    override fun observeForBook(bookId: Long): Flow<List<Review>> =
        state.map { reviews -> reviews.values.filter { it.bookId == bookId } }

    override fun observeForRound(roundId: Long): Flow<List<Review>> =
        state.map { reviews -> reviews.values.filter { it.readingRoundId == roundId } }

    override suspend fun getAllForRound(roundId: Long): List<Review> =
        state.value.values.filter { it.readingRoundId == roundId }

    override suspend fun insert(review: Review): Long {
        val id = if (review.id != 0L) review.id else nextId++
        state.value = state.value + (id to review.copy(id = id))
        return id
    }

    override suspend fun update(review: Review) {
        require(state.value.containsKey(review.id)) { "Review ${review.id} does not exist" }
        state.value = state.value + (review.id to review)
    }

    override suspend fun deleteById(reviewId: Long) {
        state.value = state.value - reviewId
    }

    fun all(): List<Review> = state.value.values.toList()
}
