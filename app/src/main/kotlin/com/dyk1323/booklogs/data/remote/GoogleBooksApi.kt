package com.dyk1323.booklogs.data.remote

import com.dyk1323.booklogs.data.remote.dto.GoogleBooksResponse
import retrofit2.http.GET
import retrofit2.http.Query

/** Base URL: https://www.googleapis.com/books/v1/ — no API key required for basic volume search. */
interface GoogleBooksApi {
    @GET("volumes")
    suspend fun searchVolumes(
        @Query("q") query: String,
        @Query("maxResults") maxResults: Int = 5,
    ): GoogleBooksResponse
}
