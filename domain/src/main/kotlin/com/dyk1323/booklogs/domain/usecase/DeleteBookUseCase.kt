package com.dyk1323.booklogs.domain.usecase

import com.dyk1323.booklogs.domain.repository.BookRepository

/**
 * Deletes a book (cascade-deletes its rounds/logs/quotes/reviews at the Room FK level in :app).
 * Intentionally has no confirmation logic — that's the calling UI's responsibility (a blocking confirm
 * dialog, per docs/PLAN.md), so this stays a plain, trivially correct delete.
 */
class DeleteBookUseCase(
    private val bookRepository: BookRepository,
) {
    suspend operator fun invoke(bookId: Long) {
        bookRepository.deleteById(bookId)
    }
}
