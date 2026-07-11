package com.dyk1323.booklogs.data.remote

import com.dyk1323.booklogs.data.remote.dto.GoogleBooksResponse
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * `GET /books/v1/volumes` — https://developers.google.com/books/docs/v1/using#WorkingVolumes.
 *
 * Despite older docs saying a key is optional for read-only search, Google now buckets every
 * keyless request into a shared default-quota project that's permanently rate-limited (confirmed via
 * a real 429 with `quota_limit_value: "0"` from both a dev sandbox and a real device on separate
 * networks — not a proxy/IP artifact). A key is required. Plain OkHttp + manual decoding, see
 * [KakaoBooksApi]'s doc.
 */
class GoogleBooksApi(
    private val httpClient: OkHttpClient,
    private val apiKey: String,
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    suspend fun searchVolumes(query: String, maxResults: Int = 5): GoogleBooksResponse =
        withContext(Dispatchers.IO) {
            val url = "https://www.googleapis.com/books/v1/volumes".toHttpUrl().newBuilder()
                .addQueryParameter("q", query)
                .addQueryParameter("maxResults", maxResults.toString())
                .apply { if (apiKey.isNotBlank()) addQueryParameter("key", apiKey) }
                .build()
            val request = Request.Builder().url(url).build()
            httpClient.newCall(request).execute().use { response ->
                val body = response.body?.string()
                if (!response.isSuccessful || body == null) {
                    throw IOException("Google Books search failed: HTTP ${response.code}")
                }
                json.decodeFromString(body)
            }
        }
}
