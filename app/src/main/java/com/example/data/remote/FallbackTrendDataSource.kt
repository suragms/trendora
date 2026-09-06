package com.example.data.remote

import com.example.core.network.GNewsErrorMapper
import com.example.core.network.NetworkMonitor
import com.example.domain.model.BreakingNewsItem
import com.example.domain.model.Country
import com.example.domain.model.TimeFilter
import com.example.domain.model.TrendCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Which data source served the most recent successful fetch. */
enum class ServedFrom { REMOTE, CACHE, MOCK }

/**
 * Orchestrates the data-source priority:
 *
 *   1. Remote (GNews) when online & configured & not in rate-limit cooldown
 *   2. Cached (Room) when offline or the remote call fails/returns empty
 *   3. Mock (bundled) as a last resort so the UI is NEVER broken/empty
 *
 * Also:
 * - Respects HTTP 429 / Retry-After with a client-side cooldown (no automatic retry loop)
 * - Coalesces near-identical in-flight / recent successful fetches so Home cannot
 *   fire duplicate GNews calls for trends + breaking news
 */
class FallbackTrendDataSource(
    private val remote: RemoteTrendDataSource,
    private val cached: CachedTrendDataSource,
    private val mock: MockTrendDataSource,
    private val networkMonitor: NetworkMonitor,
    private val clock: () -> Long = { System.currentTimeMillis() }
) : TrendDataSource {

    private val _lastError = MutableStateFlow<DataSourceResult.Error?>(null)

    /** The most recent remote error (rate limit, invalid key, ...). Cleared on success. */
    val lastError: StateFlow<DataSourceResult.Error?> = _lastError.asStateFlow()

    private val _lastServedFrom = MutableStateFlow(ServedFrom.MOCK)

    /** Which source (remote/cache/mock) served the last successful fetch. */
    val lastServedFrom: StateFlow<ServedFrom> = _lastServedFrom.asStateFlow()

    private val _rateLimitedUntilMs = MutableStateFlow(0L)

    /** Epoch millis until which live GNews requests are suppressed. */
    val rateLimitedUntilMs: StateFlow<Long> = _rateLimitedUntilMs.asStateFlow()

    private val remoteMutex = Mutex()

    /** Short-lived memory of the last successful remote batch (any max) for request coalescing. */
    private data class MemoryEntry(
        val category: TrendCategory,
        val country: Country,
        val query: String,
        val timeFilter: TimeFilter,
        val items: List<BreakingNewsItem>,
        val storedAtMs: Long
    )

    @Volatile
    private var memoryEntry: MemoryEntry? = null

    override suspend fun fetchNews(
        category: TrendCategory,
        country: Country,
        query: String,
        max: Int,
        timeFilter: TimeFilter
    ): DataSourceResult<List<BreakingNewsItem>> {

        // Serve a very recent successful remote result without another network hop.
        // Home loads trends (max=20) and breaking news (max=12) nearly simultaneously.
        memoryEntry?.let { mem ->
            if (
                mem.category == category &&
                mem.country == country &&
                mem.query == query &&
                mem.timeFilter == timeFilter &&
                clock() - mem.storedAtMs <= MEMORY_TTL_MS &&
                mem.items.isNotEmpty()
            ) {
                _lastError.value = null
                _lastServedFrom.value = ServedFrom.REMOTE
                return DataSourceResult.Success(mem.items.take(max))
            }
        }

        if (networkMonitor.isCurrentlyConnected()) {
            val remoteResult = fetchRemoteGuarded(category, country, query, max, timeFilter)
            when (remoteResult) {
                is DataSourceResult.Success -> {
                    cached.save(remoteResult.data, country)
                    memoryEntry = MemoryEntry(
                        category = category,
                        country = country,
                        query = query,
                        timeFilter = timeFilter,
                        items = remoteResult.data,
                        storedAtMs = clock()
                    )
                    _lastError.value = null
                    _lastServedFrom.value = ServedFrom.REMOTE
                    return remoteResult
                }
                is DataSourceResult.Empty -> {
                    return resolveFallback(
                        category, country, query, max, timeFilter,
                        remoteError = DataSourceResult.Error(
                            userMessage = GNewsErrorMapper.LIVE_UNAVAILABLE_CACHED_MESSAGE,
                            kind = ApiErrorKind.EMPTY_RESPONSE
                        )
                    )
                }
                is DataSourceResult.Error -> {
                    return resolveFallback(
                        category, country, query, max, timeFilter,
                        remoteError = remoteResult
                    )
                }
            }
        }

        // Offline -> cached first, then mock. Clear stale remote error for offline UX.
        _lastError.value = null
        return resolveOffline(category, country, query, max, timeFilter)
    }

    /** True when live GNews calls are currently suppressed due to rate limiting. */
    fun isRateLimitedNow(): Boolean = clock() < _rateLimitedUntilMs.value

    /** Seconds remaining in the rate-limit cooldown (0 if none). */
    fun rateLimitRemainingSeconds(): Long {
        val remainingMs = _rateLimitedUntilMs.value - clock()
        return if (remainingMs > 0) (remainingMs + 999) / 1000 else 0L
    }

    private suspend fun fetchRemoteGuarded(
        category: TrendCategory,
        country: Country,
        query: String,
        max: Int,
        timeFilter: TimeFilter
    ): DataSourceResult<List<BreakingNewsItem>> = remoteMutex.withLock {
        val now = clock()
        if (now < _rateLimitedUntilMs.value) {
            val remaining = ((_rateLimitedUntilMs.value - now + 999) / 1000).coerceAtLeast(1)
            return@withLock DataSourceResult.Error(
                userMessage = GNewsErrorMapper.RATE_LIMIT_FALLBACK_MESSAGE,
                code = 429,
                kind = ApiErrorKind.RATE_LIMITED,
                retryAfterSeconds = remaining
            )
        }

        // Re-check memory inside the lock in case a twin request just finished.
        memoryEntry?.let { mem ->
            if (
                mem.category == category &&
                mem.country == country &&
                mem.query == query &&
                mem.timeFilter == timeFilter &&
                now - mem.storedAtMs <= MEMORY_TTL_MS &&
                mem.items.isNotEmpty()
            ) {
                return@withLock DataSourceResult.Success(mem.items.take(max))
            }
        }

        val result = remote.fetchNews(category, country, query, max, timeFilter)
        if (result is DataSourceResult.Error && result.kind == ApiErrorKind.RATE_LIMITED) {
            val cooldownSec = result.retryAfterSeconds ?: DEFAULT_RATE_LIMIT_COOLDOWN_SEC
            _rateLimitedUntilMs.value = now + cooldownSec * 1000L
        }
        result
    }

    private suspend fun resolveFallback(
        category: TrendCategory,
        country: Country,
        query: String,
        max: Int,
        timeFilter: TimeFilter,
        remoteError: DataSourceResult.Error
    ): DataSourceResult<List<BreakingNewsItem>> {
        val cachedResult = cached.fetchNews(category, country, query, max, timeFilter)
        if (cachedResult is DataSourceResult.Success) {
            _lastError.value = remoteError.copy(
                userMessage = softMessageFor(remoteError, hasCache = true)
            )
            _lastServedFrom.value = ServedFrom.CACHE
            return cachedResult
        }

        _lastError.value = remoteError.copy(
            userMessage = softMessageFor(remoteError, hasCache = false)
        )
        _lastServedFrom.value = ServedFrom.MOCK
        return mock.fetchNews(category, country, query, max, timeFilter)
    }

    private suspend fun resolveOffline(
        category: TrendCategory,
        country: Country,
        query: String,
        max: Int,
        timeFilter: TimeFilter
    ): DataSourceResult<List<BreakingNewsItem>> {
        val cachedResult = cached.fetchNews(category, country, query, max, timeFilter)
        if (cachedResult is DataSourceResult.Success) {
            _lastServedFrom.value = ServedFrom.CACHE
            return cachedResult
        }
        _lastServedFrom.value = ServedFrom.MOCK
        return mock.fetchNews(category, country, query, max, timeFilter)
    }

    private fun softMessageFor(error: DataSourceResult.Error, hasCache: Boolean): String {
        return when (error.kind) {
            ApiErrorKind.RATE_LIMITED -> if (hasCache) {
                GNewsErrorMapper.RATE_LIMIT_CACHED_MESSAGE
            } else {
                GNewsErrorMapper.RATE_LIMIT_FALLBACK_MESSAGE
            }
            ApiErrorKind.EMPTY_RESPONSE,
            ApiErrorKind.NETWORK,
            ApiErrorKind.SERVER_ERROR,
            ApiErrorKind.UNKNOWN -> if (hasCache) {
                GNewsErrorMapper.LIVE_UNAVAILABLE_CACHED_MESSAGE
            } else {
                error.userMessage
            }
            else -> error.userMessage
        }
    }

    companion object {
        /** Default cooldown when Retry-After is absent (GNews free-tier friendly). */
        const val DEFAULT_RATE_LIMIT_COOLDOWN_SEC = 15 * 60L

        /** Coalesce Home's dual fetch (trends + breaking) within this window. */
        const val MEMORY_TTL_MS = 45_000L
    }
}
