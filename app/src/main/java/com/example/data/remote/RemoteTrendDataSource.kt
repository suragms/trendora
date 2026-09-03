package com.example.data.remote

import com.example.core.network.GNewsApiService
import com.example.core.network.GNewsClient
import com.example.core.network.GNewsErrorMapper
import com.example.core.network.NetworkMonitor
import com.example.data.remote.mapper.GNewsMappers
import com.example.domain.model.BreakingNewsItem
import com.example.domain.model.Country
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
        max: Int
    ): DataSourceResult<List<BreakingNewsItem>> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext DataSourceResult.Error(GNewsErrorMapper.MISSING_KEY_MESSAGE)
        }
        if (!networkMonitor.isCurrentlyConnected()) {
            return@withContext DataSourceResult.Error(GNewsErrorMapper.NO_INTERNET_MESSAGE)
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

            val articles = response.articles.orEmpty()
            if (articles.isEmpty()) {
                return@withContext DataSourceResult.Empty
            }

            val mapped = articles.mapNotNull { dto ->
                dto.url ?: return@mapNotNull null // skip entries with no url/id
                dto.title?.let {
                    GNewsMappers.toBreakingNews(dto, category)
                }
            }

            if (mapped.isEmpty()) DataSourceResult.Empty else DataSourceResult.Success(mapped)
        } catch (e: HttpException) {
            DataSourceResult.Error(
                userMessage = GNewsErrorMapper.fromHttpCode(e.code(), isConfigured),
                code = e.code()
            )
        } catch (e: Exception) {
            DataSourceResult.Error(GNewsErrorMapper.fromThrowable(e))
        }
    }
}
