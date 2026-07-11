package com.dyk1323.booklogs.data.remote

import com.dyk1323.booklogs.data.remote.dto.KakaoBookSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

/** Base URL: https://dapi.kakao.com/ — Authorization header ("KakaoAK {key}") added by an OkHttp interceptor. */
interface KakaoBooksApi {
    @GET("v3/search/book")
    suspend fun searchBooks(
        @Query("query") query: String,
        @Query("target") target: String? = null,
        @Query("size") size: Int = 10,
    ): KakaoBookSearchResponse
}
