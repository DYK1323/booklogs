package com.dyk1323.booklogs.domain.usecase

import com.dyk1323.booklogs.domain.model.Book
import com.dyk1323.booklogs.domain.model.BookFormat
import com.dyk1323.booklogs.domain.model.BookStatus
import kotlin.random.Random
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PickReminderBookUseCaseTest {

    private fun book(id: Long) = Book(
        id = id, isbn = null, title = "Book $id", author = null, publisher = null,
        coverImageUrl = null, totalPages = 300, status = BookStatus.READING, format = BookFormat.PHYSICAL,
        genre = null, country = null, createdAt = 0,
    )

    @Test
    fun `an empty list returns null instead of throwing`() {
        val useCase = PickReminderBookUseCase()

        assertNull(useCase(emptyList()))
    }

    @Test
    fun `a single-book list always returns that book`() {
        val useCase = PickReminderBookUseCase()
        val onlyBook = book(1)

        assertTrue(useCase(listOf(onlyBook)) === onlyBook)
    }

    @Test
    fun `the picked book is always a member of the input list, across many random seeds`() {
        val books = (1..5L).map(::book)

        repeat(200) { seed ->
            val useCase = PickReminderBookUseCase(random = Random(seed))
            val picked = useCase(books)
            assertTrue(picked != null && picked in books)
        }
    }
}
