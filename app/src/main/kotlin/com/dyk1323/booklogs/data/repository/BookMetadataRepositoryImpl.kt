package com.dyk1323.booklogs.data.repository

import com.dyk1323.booklogs.data.remote.GoogleBooksApi
import com.dyk1323.booklogs.data.remote.KakaoBooksApi
import com.dyk1323.booklogs.data.remote.toBookMetadata
import com.dyk1323.booklogs.domain.model.ApiLookupResult
import com.dyk1323.booklogs.domain.model.BookMetadata
import com.dyk1323.booklogs.domain.model.MetadataLookupResult
import com.dyk1323.booklogs.domain.repository.BookMetadataRepository
import com.dyk1323.booklogs.domain.usecase.mergeBookMetadata
import com.dyk1323.booklogs.domain.usecase.resolveBookMetadata
import java.io.IOException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeout

private const val LOOKUP_TIMEOUT_MS = 5_000L

class BookMetadataRepositoryImpl(
    private val kakaoApi: KakaoBooksApi,
    private val googleApi: GoogleBooksApi,
) : BookMetadataRepository {

    override suspend fun lookupByIsbn(isbn: String): MetadataLookupResult = coroutineScope {
        val kakaoDeferred = async { queryKakaoByIsbn(isbn) }
        val googleDeferred = async { queryGoogleByIsbn(isbn) }
        val kakao = kakaoDeferred.await()
        val google = googleDeferred.await()
        val resolved = resolveBookMetadata(kakao, google)

        if (resolved is MetadataLookupResult.Found &&
            resolved.metadata.totalPages == null &&
            kakao is ApiLookupResult.Success
        ) {
            return@coroutineScope supplementPagesFromGoogleTitle(resolved.metadata, isbn)
        }

        resolved
    }

    override suspend fun searchByTitle(query: String): ApiLookupResult<List<BookMetadata>> {
        val kakaoResult = queryKakaoByTitle(query)
        if (kakaoResult is ApiLookupResult.Success && kakaoResult.data.isNotEmpty()) return kakaoResult
        if (kakaoResult is ApiLookupResult.NetworkError) return kakaoResult

        // Kakao returned zero results (or an empty success list) — retry with Google Books.
        return when (val google = queryGoogleByTitle(query)) {
            is ApiLookupResult.Success -> google
            is ApiLookupResult.NotFound -> ApiLookupResult.NotFound
            is ApiLookupResult.NetworkError -> if (kakaoResult is ApiLookupResult.Success) kakaoResult else ApiLookupResult.NetworkError
        }
    }

    override suspend fun resolveSelectedCandidate(candidate: BookMetadata): MetadataLookupResult {
        val isbn = candidate.isbn
        if (isbn != null) return lookupByIsbn(isbn)

        return when (val google = queryGoogleByTitle("${candidate.title} ${candidate.author.orEmpty()}".trim())) {
            is ApiLookupResult.Success -> MetadataLookupResult.Found(mergeBookMetadata(candidate, selectBestGoogleMetadata(google.data)))
            else -> MetadataLookupResult.Found(candidate)
        }
    }

    private suspend fun supplementPagesFromGoogleTitle(metadata: BookMetadata, preferredIsbn: String): MetadataLookupResult {
        val query = "${metadata.title} ${metadata.author.orEmpty()}".trim()
        if (query.isBlank()) return MetadataLookupResult.Found(metadata)

        return when (val google = queryGoogleByTitle(query)) {
            is ApiLookupResult.Success -> MetadataLookupResult.Found(
                mergeBookMetadata(metadata, selectBestGoogleMetadata(google.data, preferredIsbn)),
            )
            else -> MetadataLookupResult.Found(metadata)
        }
    }

    private suspend fun queryKakaoByIsbn(isbn: String): ApiLookupResult<BookMetadata> = safeCall {
        kakaoApi.searchBooks(query = isbn, target = "isbn", size = 1).documents.firstOrNull()?.toBookMetadata()
    }

    private suspend fun queryKakaoByTitle(query: String): ApiLookupResult<List<BookMetadata>> = safeCall {
        kakaoApi.searchBooks(query = query, target = "title", size = 10).documents.map { it.toBookMetadata() }
            .takeIf { it.isNotEmpty() }
    }

    private suspend fun queryGoogleByIsbn(isbn: String): ApiLookupResult<BookMetadata> = safeCall {
        googleApi.searchVolumes(query = "isbn:$isbn", maxResults = 10)
            .items.map { it.volumeInfo.toBookMetadata() }
            .let { selectBestGoogleMetadata(it, isbn) }
    }

    private suspend fun queryGoogleByTitle(query: String): ApiLookupResult<List<BookMetadata>> = safeCall {
        googleApi.searchVolumes(query = query, maxResults = 10)
            .items.map { it.volumeInfo.toBookMetadata() }.takeIf { it.isNotEmpty() }
    }

    /**
     * Runs [block] with a shared timeout, mapping a null result -> NotFound (a normal "zero results"
     * response) and timeouts/IO/HTTP failures -> NetworkError. These two must stay distinct: a timeout
     * is not the same as "the API legitimately found nothing" (see docs/PLAN.md's 5-branch matrix).
     */
    private suspend fun <T> safeCall(block: suspend () -> T?): ApiLookupResult<T> = try {
        withTimeout(LOOKUP_TIMEOUT_MS) {
            when (val result = block()) {
                null -> ApiLookupResult.NotFound
                else -> ApiLookupResult.Success(result)
            }
        }
    } catch (e: TimeoutCancellationException) {
        ApiLookupResult.NetworkError
    } catch (e: IOException) {
        ApiLookupResult.NetworkError
    }
}

internal fun selectBestGoogleMetadata(
    candidates: List<BookMetadata>,
    preferredIsbn: String? = null,
): BookMetadata? {
    if (candidates.isEmpty()) return null
    val isbnMatched = preferredIsbn?.let { isbn -> candidates.filter { it.isbn == isbn } }.orEmpty()
    val pool = isbnMatched.ifEmpty { candidates }

    return pool.firstOrNull { it.totalPages != null }
        ?: candidates.firstOrNull { it.totalPages != null }
        ?: pool.firstOrNull()
}
