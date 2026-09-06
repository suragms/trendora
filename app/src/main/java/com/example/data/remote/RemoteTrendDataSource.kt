package com.example.data.remote

import android.util.Log
import com.example.core.network.GNewsApiService
import com.example.core.network.GNewsClient
import com.example.core.network.GNewsErrorMapper
import com.example.core.network.NetworkMonitor
import com.example.data.remote.dto.GNewsArticleDto
import com.example.data.remote.mapper.GNewsMappers
import com.example.domain.model.BreakingNewsItem
import com.example.domain.model.Country
import com.example.domain.model.TimeFilter
import com.example.domain.model.TrendCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

/**
 * Remote data source backed by the real GNews API.
 *
 * Priority source when the device is online and an API key is configured.
 */
class RemoteTrendDataSource(
    private val apiService: GNewsApiService = GNewsClient.apiService,
    private val networkMonitor: NetworkMonitor,
    private val isConfigured: Boolean = GNewsClient.isConfigured
) : TrendDataSource {

    override suspend fun fetchNews(
        category: TrendCategory,
        country: Country,
        query: String,
        max: Int,
        timeFilter: TimeFilter
    ): DataSourceResult<List<BreakingNewsItem>> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            Log.w(TAG, "GNews API key is not configured")
            return@withContext DataSourceResult.Error(
                userMessage = GNewsErrorMapper.MISSING_KEY_MESSAGE,
                kind = ApiErrorKind.NOT_CONFIGURED
            )
        }
        if (!networkMonitor.isCurrentlyConnected()) {
            return@withContext DataSourceResult.Error(
                userMessage = GNewsErrorMapper.NO_INTERNET_MESSAGE,
                kind = ApiErrorKind.NETWORK
            )
        }

        try {
            val response = if (query.isBlank()) {
                apiService.getTopHeadlines(
                    category = GNewsMappers.toGNewsCategory(category),
                    country = GNewsMappers.toGNewsCountry(country),
                    max = max,
                    apiKey = GNewsClient.apiKey
                )
            } else {
                apiService.search(
                    query = query,
                    country = GNewsMappers.toGNewsCountry(country),
                    max = max,
                    apiKey = GNewsClient.apiKey
                )
            }

            // Apply the recency filter client-side on real publication timestamps.
            val cutoffMillis = timeFilter.cutoffMillis()
            val articles = response.articles.orEmpty()
                .filter { it.publishedAtMillis() >= cutoffMillis }

            if (articles.isEmpty()) {
                return@withContext DataSourceResult.Empty
            }

            val mapped = articles.mapNotNull { dto ->
                dto.url ?: return@mapNotNull null // skip entries with no url/id
                dto.title?.let {
                    GNewsMappers.toBreakingNews(dto, category)
                }
            }

            if (mapped.isEmpty()) {
                DataSourceResult.Empty
            } else {
                DataSourceResult.Success(mapped)
            }
        } catch (e: HttpException) {
            val (message, kind) = GNewsErrorMapper.fromHttpCode(e.code(), isConfigured)
            val retryAfter = parseRetryAfterSeconds(e)
            if (e.code() == 429) {
                Log.w(TAG, "GNews rate limited (HTTP 429); retryAfterSeconds=$retryAfter")
            } else {
                Log.w(TAG, "GNews HTTP error code=${e.code()}")
            }
            DataSourceResult.Error(
                userMessage = message,
                code = e.code(),
                kind = kind,
                retryAfterSeconds = retryAfter
            )
        } catch (e: Exception) {
            val (message, kind) = GNewsErrorMapper.fromThrowable(e)
            DataSourceResult.Error(userMessage = message, kind = kind)
        }
    }

    /**
     * Publication time in epoch millis. When the timestamp is absent/unparsable
     * we return [Long.MAX_VALUE] so the article is KEPT — we cannot determine
     * its age, and recency filtering should only apply where a real timestamp
     * is available.
     */
    private fun GNewsArticleDto.publishedAtMillis(): Long =
        GNewsMappers.parseDate(publishedAt) ?: Long.MAX_VALUE

    companion object {
        private const val TAG = "RemoteTrendDataSource"

        /** Parses Retry-After as seconds (numeric) — never logs header values beyond the number. */
        fun parseRetryAfterSeconds(e: HttpException): Long? {
            val raw = e.response()?.headers()?.get("Retry-After") ?: return null
            raw.trim().toLongOrNull()?.let { return it.coerceIn(1L, 86_400L) }
            return null
        }
    }
}

/** Number of milliseconds an article must be newer than to match a [TimeFilter]. */
private fun TimeFilter.cutoffMillis(): Long {
    val now = System.currentTimeMillis()
    return when (this) {
        TimeFilter.LAST_HOUR -> now - 60 * 60 * 1000L
        TimeFilter.TODAY -> now - 24 * 60 * 60 * 1000L
        TimeFilter.THIS_WEEK -> now - 7 * 24 * 60 * 60 * 1000L
        TimeFilter.THIS_MONTH -> now - 30 * 24 * 60 * 60 * 1000L
    }
}
