package com.dyk1323.booklogs.domain.usecase

import com.dyk1323.booklogs.domain.model.ApiLookupResult
import com.dyk1323.booklogs.domain.model.BookMetadata
import com.dyk1323.booklogs.domain.model.MetadataLookupResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Covers docs/PLAN.md "메타데이터 연동 — 실패 처리 상세" 5-branch matrix. */
class ResolveBookMetadataUseCaseTest {

    private val kakaoMeta = BookMetadata(
        isbn = "9788937460777", title = "Kakao Title", author = "Kakao Author",
        publisher = "Kakao Publisher", coverImageUrl = "https://kakao/cover.jpg",
        totalPages = null, genre = null,
    )
    private val googleMeta = BookMetadata(
        isbn = "9788937460777", title = "Google Title", author = "Google Author",
        publisher = "Google Publisher", coverImageUrl = "https://google/cover.jpg",
        totalPages = 320, genre = "소설",
    )

    @Test
    fun `both succeed - merges kakao base fields with google totalPages and genre`() {
        val result = resolveBookMetadata(
            kakao = ApiLookupResult.Success(kakaoMeta),
            google = ApiLookupResult.Success(googleMeta),
        )

        assertTrue(result is MetadataLookupResult.Found)
        val merged = (result as MetadataLookupResult.Found).metadata
        assertEquals("Kakao Title", merged.title)
        assertEquals("Kakao Author", merged.author)
        assertEquals("Kakao Publisher", merged.publisher)
        assertEquals("https://kakao/cover.jpg", merged.coverImageUrl)
        assertEquals(320, merged.totalPages)
        assertEquals("소설", merged.genre)
    }

    @Test
    fun `kakao succeeds, google not found - kakao fields only, totalPages and genre stay null`() {
        val result = resolveBookMetadata(
            kakao = ApiLookupResult.Success(kakaoMeta),
            google = ApiLookupResult.NotFound,
        )

        assertTrue(result is MetadataLookupResult.Found)
        val found = (result as MetadataLookupResult.Found).metadata
        assertEquals("Kakao Title", found.title)
        assertEquals(null, found.totalPages)
        assertEquals(null, found.genre)
    }

    @Test
    fun `kakao succeeds, google network error - kakao fields only`() {
        val result = resolveBookMetadata(
            kakao = ApiLookupResult.Success(kakaoMeta),
            google = ApiLookupResult.NetworkError,
        )

        assertTrue(result is MetadataLookupResult.Found)
        assertEquals("Kakao Title", (result as MetadataLookupResult.Found).metadata.title)
    }

    @Test
    fun `kakao not found, google succeeds - falls back to google entirely`() {
        val result = resolveBookMetadata(
            kakao = ApiLookupResult.NotFound,
            google = ApiLookupResult.Success(googleMeta),
        )

        assertTrue(result is MetadataLookupResult.Found)
        assertEquals("Google Title", (result as MetadataLookupResult.Found).metadata.title)
    }

    @Test
    fun `kakao network error, google succeeds - falls back to google entirely`() {
        val result = resolveBookMetadata(
            kakao = ApiLookupResult.NetworkError,
            google = ApiLookupResult.Success(googleMeta),
        )

        assertTrue(result is MetadataLookupResult.Found)
        assertEquals("Google Title", (result as MetadataLookupResult.Found).metadata.title)
    }

    @Test
    fun `both not found - classified as NotFound`() {
        val result = resolveBookMetadata(ApiLookupResult.NotFound, ApiLookupResult.NotFound)
        assertEquals(MetadataLookupResult.NotFound, result)
    }

    @Test
    fun `one not found, one network error - classified as NotFound (at least one real answer)`() {
        val result = resolveBookMetadata(ApiLookupResult.NotFound, ApiLookupResult.NetworkError)
        assertEquals(MetadataLookupResult.NotFound, result)

        val flipped = resolveBookMetadata(ApiLookupResult.NetworkError, ApiLookupResult.NotFound)
        assertEquals(MetadataLookupResult.NotFound, flipped)
    }

    @Test
    fun `both network error - classified as NetworkError`() {
        val result = resolveBookMetadata(ApiLookupResult.NetworkError, ApiLookupResult.NetworkError)
        assertEquals(MetadataLookupResult.NetworkError, result)
    }
}
