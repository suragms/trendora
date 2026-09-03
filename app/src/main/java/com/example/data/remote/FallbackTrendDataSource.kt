package com.example.data.remote

import com.example.core.network.NetworkMonitor
import com.example.domain.model.BreakingNewsItem
import com.example.domain.model.Country
import com.example.domain.model.TrendCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Orchestrates the data-source priority:
 *
 *   1. Remote (GNews) when online & configured
 *   2. Cached (Room) when offline or the remote call fails
 *   3. Mock (bundled) as a last resort so the UI is never broken/empty
 *
 * On a successful remote fetch, the Room cache is refreshed for offline use.
 */
class FallbackTrendDataSource(
    private val remote: RemoteTrendDataSource,
    private val cached: CachedTrendDataSource,
    private val mock: MockTrendDataSource,
    private val networkMonitor: NetworkMonitor
) : TrendDataSource {

    private val _lastError = MutableStateFlow<DataSourceResult.Error?>(null)

    /** The most recent remote error (rate limit, invalid key, ...). Cleared on success. */
    val lastError: StateFlow<DataSourceResult.Error?> = _lastError.asStateFlow()

    override suspend fun fetchNews(
        category: TrendCategory,
        country: Country,
        query: String,
        max: Int
    ): DataSourceResult<List<BreakingNewsItem>> {

        if (networkMonitor.isCurrentlyConnected()) {
            when (val remoteResult = remote.fetchNews(category, country, query, max)) {
                is DataSourceResult.Success -> {
                    // Refresh the offline cache with the fresh batch.
                    cached.save(remoteResult.data, country)
                    _lastError.value = null
                    return remoteResult
                }
                is DataSourceResult.Empty -> return DataSourceResult.Empty
                is DataSourceResult.Error -> {
                    _lastError.value = remoteResult
                    // Remote failed -> fall back to cache.
                    val cachedResult = cached.fetchNews(category, country, query, max)
                    if (cachedResult is DataSourceResult.Success) return cachedResult
                    // No cache -> surface the remote error (never silently drop it).
                    return remoteResult
                }
            }
        }

        // Offline -> cached first, then mock.
        val cachedResult = cached.fetchNews(category, country, query, max)
        if (cachedResult is DataSourceResult.Success) return cachedResult
        return mock.fetchNews(category, country, query, max)
    }
}
