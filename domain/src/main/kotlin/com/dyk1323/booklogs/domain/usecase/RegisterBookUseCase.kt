package com.dyk1323.booklogs.domain.usecase

import com.dyk1323.booklogs.domain.model.Book
import com.dyk1323.booklogs.domain.model.BookStatus
import com.dyk1323.booklogs.domain.repository.BookRepository

/**
 * Saves a newly-registered book (docs/PLAN.md 화면 흐름 #2). The book is always inserted as PLANNED
 * first, then — if the user chose "바로 읽기 시작" — immediately transitioned to READING via
 * [ChangeBookStatusUseCase], which is the exact same "create the first round" mechanism already used
 * for PLANNED -> READING from the book detail screen. No separate first-round logic needed here.
 */
class RegisterBookUseCase(
    private val bookRepository: BookRepository,
    private val changeBookStatusUseCase: ChangeBookStatusUseCase,
) {
    suspend operator fun invoke(book: Book, startReadingImmediately: Boolean, now: Long): Result<Long> {
        val bookId = bookRepository.insert(book.copy(id = 0, status = BookStatus.PLANNED, createdAt = now))
        if (!startReadingImmediately) return Result.success(bookId)

        return changeBookStatusUseCase(bookId, BookStatus.READING, now)
            .map { bookId }
    }
}
