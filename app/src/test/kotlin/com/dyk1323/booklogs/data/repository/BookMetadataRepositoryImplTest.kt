package com.dyk1323.booklogs.data.repository

import com.dyk1323.booklogs.domain.model.BookMetadata
import org.junit.Assert.assertEquals
import org.junit.Test

class BookMetadataRepositoryImplTest {

    @Test
    fun selectBestGoogleMetadata_prefersMatchingIsbnWithPages() {
        val result = selectBestGoogleMetadata(
            candidates = listOf(
                googleBook(isbn = "9780000000000", totalPages = 300),
                googleBook(isbn = "9781234567890", totalPages = null),
                googleBook(isbn = "9781234567890", totalPages = 412),
            ),
            preferredIsbn = "9781234567890",
        )

        assertEquals("9781234567890", result?.isbn)
        assertEquals(412, result?.totalPages)
    }

    @Test
    fun selectBestGoogleMetadata_usesAnyCandidateWithPagesWhenIsbnDoesNotMatch() {
        val result = selectBestGoogleMetadata(
            candidates = listOf(
                googleBook(isbn = "9781111111111", totalPages = null),
                googleBook(isbn = "9782222222222", totalPages = 256),
            ),
            preferredIsbn = "9789999999999",
        )

        assertEquals("9782222222222", result?.isbn)
        assertEquals(256, result?.totalPages)
    }

    private fun googleBook(isbn: String, totalPages: Int?) = BookMetadata(
        isbn = isbn,
        title = "Title",
        author = "Author",
        publisher = null,
        coverImageUrl = null,
        totalPages = totalPages,
        genre = null,
    )
}
