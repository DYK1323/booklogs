package com.dyk1323.booklogs.data.remote

import com.dyk1323.booklogs.data.remote.dto.KakaoBookSearchResponse
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * `GET /v3/search/book` — https://developers.kakao.com/docs/latest/ko/daum-search/dev-guide#search-book
 *
 * Plain OkHttp + manual kotlinx.serialization decoding rather than a Retrofit interface, so the whole
 * :app metadata layer only depends on okhttp/kotlinx-serialization-json (both already exercised
 * elsewhere in the project) instead of the smaller third-party Retrofit<->kotlinx.serialization bridge.
 */
class KakaoBooksApi(
    private val httpClient: OkHttpClient,
    private val apiKey: String,
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    suspend fun searchBooks(query: String, target: String? = null, size: Int = 10): KakaoBookSearchResponse =
        withContext(Dispatchers.IO) {
            val url = "https://dapi.kakao.com/v3/search/book".toHttpUrl().newBuilder()
                .addQueryParameter("query", query)
                .addQueryParameter("size", size.toString())
                .apply { target?.let { addQueryParameter("target", it) } }
                .build()
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "KakaoAK $apiKey")
                .build()
            httpClient.newCall(request).execute().use { response ->
                val body = response.body?.string()
                if (!response.isSuccessful || body == null) {
                    throw IOException("Kakao book search failed: HTTP ${response.code}")
                }
                json.decodeFromString(body)
            }
        }
}
