package com.example.core.network

import com.example.data.remote.dto.GNewsResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit definition for the GNews REST API.
 *
 * Base URL: https://gnews.io/api/v4/
 *
 * The API key is passed as a query parameter (`apikey`), which is how the
 * GNews API expects it. The key value is injected at build time from
 * `local.properties` via `BuildConfig.GNEWS_API_KEY` — never hardcoded here.
 */
interface GNewsApiService {

    /** Top headlines, optionally filtered by category and country. */
    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("category") category: String?,
        @Query("country") country: String?,
        @Query("lang") lang: String = "en",
        @Query("max") max: Int = 20,
        @Query("apikey") apiKey: String
    ): GNewsResponseDto

    /** Keyword search, optionally filtered by country. */
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("country") country: String?,
        @Query("lang") lang: String = "en",
        @Query("max") max: Int = 20,
        @Query("apikey") apiKey: String
    ): GNewsResponseDto
}
