package com.dyk1323.booklogs.domain.fake

import com.dyk1323.booklogs.domain.model.Book
import com.dyk1323.booklogs.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeBookRepository(initial: List<Book> = emptyList()) : BookRepository {
    private var nextId = (initial.maxOfOrNull { it.id } ?: 0L) + 1
    private val state = MutableStateFlow(initial.associateBy { it.id })

    override fun observeAll(): Flow<List<Book>> = state.map { it.values.toList() }

    override suspend fun getById(bookId: Long): Book? = state.value[bookId]

    override suspend fun findByIsbn(isbn: String): Book? = state.value.values.firstOrNull { it.isbn == isbn }

    override suspend fun insert(book: Book): Long {
        val id = if (book.id != 0L) book.id else nextId++
        state.value = state.value + (id to book.copy(id = id))
        return id
    }

    override suspend fun update(book: Book) {
        require(state.value.containsKey(book.id)) { "Book ${book.id} does not exist" }
        state.value = state.value + (book.id to book)
    }

    override suspend fun deleteById(bookId: Long) {
        state.value = state.value - bookId
    }

    fun all(): List<Book> = state.value.values.toList()
}
