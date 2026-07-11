package com.dyk1323.booklogs.domain.repository

import com.dyk1323.booklogs.domain.model.Book
import kotlinx.coroutines.flow.Flow

interface BookRepository {
    fun observeAll(): Flow<List<Book>>
    suspend fun getById(bookId: Long): Book?
    suspend fun findByIsbn(isbn: String): Book?
    suspend fun insert(book: Book): Long
    suspend fun update(book: Book)
    suspend fun deleteById(bookId: Long)
}
