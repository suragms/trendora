package com.example.data.remote

import com.example.core.network.NetworkMonitor
import com.example.domain.model.BreakingNewsItem
import com.example.domain.model.Country
import com.example.domain.model.TimeFilter
import com.example.domain.model.TrendCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Which data source served the most recent successful fetch. */
enum class ServedFrom { REMOTE, CACHE, MOCK }

/**
 * Orchestrates the data-source priority:
 *
 *   1. Remote (GNews) when online & configured
 *   2. Cached (Room) when offline or the remote call fails/returns empty
 *   3. Mock (bundled) as a last resort so the UI is NEVER broken/empty
 *
 * The mock fallback is guaranteed even when the remote call fails with no cache,
 * so the user always sees content (never a blank screen) unless the mock set
 * itself is empty (which it is not).
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

    private val _lastServedFrom = MutableStateFlow(ServedFrom.MOCK)

    /** Which source (remote/cache/mock) served the last successful fetch. */
    val lastServedFrom: StateFlow<ServedFrom> = _lastServedFrom.asStateFlow()

    override suspend fun fetchNews(
        category: TrendCategory,
        country: Country,
        query: String,
        max: Int,
        timeFilter: TimeFilter
    ): DataSourceResult<List<BreakingNewsItem>> {

        if (networkMonitor.isCurrentlyConnected()) {
            when (val remoteResult = remote.fetchNews(category, country, query, max, timeFilter)) {
                is DataSourceResult.Success -> {
                    // Refresh the offline cache with the fresh batch.
                    cached.save(remoteResult.data, country)
                    _lastError.value = null
                    _lastServedFrom.value = ServedFrom.REMOTE
                    return remoteResult
                }
                is DataSourceResult.Empty, is DataSourceResult.Error -> {
                    if (remoteResult is DataSourceResult.Error) _lastError.value = remoteResult
                    // Remote failed or returned nothing -> fall back to cache.
                    val cachedResult = cached.fetchNews(category, country, query, max, timeFilter)
                    if (cachedResult is DataSourceResult.Success) {
                        _lastServedFrom.value = ServedFrom.CACHE
                        return cachedResult
                    }
                    // No usable cache -> guaranteed mock fallback (never empty screen).
                    _lastServedFrom.value = ServedFrom.MOCK
                    return mock.fetchNews(category, country, query, max, timeFilter)
                }
            }
        }

        // Offline -> cached first, then mock.
        val cachedResult = cached.fetchNews(category, country, query, max, timeFilter)
        if (cachedResult is DataSourceResult.Success) {
            _lastServedFrom.value = ServedFrom.CACHE
            return cachedResult
        }
        _lastServedFrom.value = ServedFrom.MOCK
        return mock.fetchNews(category, country, query, max, timeFilter)
    }
}
