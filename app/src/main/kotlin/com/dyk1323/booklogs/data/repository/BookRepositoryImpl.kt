package com.dyk1323.booklogs.data.repository

import com.dyk1323.booklogs.data.local.dao.BookDao
import com.dyk1323.booklogs.data.local.entity.BookEntity
import com.dyk1323.booklogs.domain.model.Book
import com.dyk1323.booklogs.domain.model.BookFormat
import com.dyk1323.booklogs.domain.model.BookStatus
import com.dyk1323.booklogs.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BookRepositoryImpl(
    private val bookDao: BookDao,
) : BookRepository {
    override fun observeAll(): Flow<List<Book>> =
        bookDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getById(bookId: Long): Book? = bookDao.getById(bookId)?.toDomain()

    override suspend fun findByIsbn(isbn: String): Book? = bookDao.findByIsbn(isbn)?.toDomain()

    override suspend fun insert(book: Book): Long = bookDao.insert(book.toEntity())

    override suspend fun update(book: Book) = bookDao.update(book.toEntity())

    override suspend fun deleteById(bookId: Long) = bookDao.deleteById(bookId)
}

internal fun BookEntity.toDomain(): Book = Book(
    id = id,
    isbn = isbn,
    title = title,
    author = author,
    publisher = publisher,
    coverImageUrl = coverImageUrl,
    totalPages = totalPages,
    status = BookStatus.valueOf(status),
    format = BookFormat.valueOf(format),
    genre = genre,
    country = country,
    createdAt = createdAt,
)

internal fun Book.toEntity(): BookEntity = BookEntity(
    id = id,
    isbn = isbn,
    title = title,
    author = author,
    publisher = publisher,
    coverImageUrl = coverImageUrl,
    totalPages = totalPages,
    status = status.name,
    format = format.name,
    genre = genre,
    country = country,
    createdAt = createdAt,
)
