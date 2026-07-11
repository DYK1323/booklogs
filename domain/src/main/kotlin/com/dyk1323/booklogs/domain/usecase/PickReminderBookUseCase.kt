package com.dyk1323.booklogs.domain.usecase

import com.dyk1323.booklogs.domain.model.Book
import kotlin.random.Random

/** Picks one random book from the given (already-filtered-to-READING) list, or null if it's empty. */
class PickReminderBookUseCase(
    private val random: Random = Random.Default,
) {
    operator fun invoke(readingBooks: List<Book>): Book? {
        if (readingBooks.isEmpty()) return null
        return readingBooks[random.nextInt(readingBooks.size)]
    }
}
