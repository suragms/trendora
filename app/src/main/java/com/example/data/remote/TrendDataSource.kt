package com.example.data.remote

import com.example.domain.model.BreakingNewsItem
import com.example.domain.model.Country
import com.example.domain.model.TrendCategory

/**
 * A generic news/trend data source. Implementations supply news either from the
 * remote GNews API, a local Room cache, or the bundled mock data.
 *
 * The app composes these through [FallbackTrendDataSource] with the priority:
 *   1. Remote (when online and configured)
 *   2. Cached (when offline / remote failed)
 *   3. Mock  (first launch / nothing available)
 */
interface TrendDataSource {

    /** Fetches news for the given filter. Never throws — always returns a result. */
    suspend fun fetchNews(
        category: TrendCategory,
        country: Country,
        query: String = "",
        max: Int = 20
    ): DataSourceResult<List<BreakingNewsItem>>
}

/**
 * Sealed result that models success / failure / empty so callers can build
 * user-friendly UI states without exposing raw exceptions.
 */
sealed class DataSourceResult<out T> {
    data class Success<T>(val data: T, val fromCache: Boolean = false) : DataSourceResult<T>()
    data class Error(val userMessage: String, val code: Int? = null) : DataSourceResult<Nothing>()
    object Empty : DataSourceResult<Nothing>()
}
