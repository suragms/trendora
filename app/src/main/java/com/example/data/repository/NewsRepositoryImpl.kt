package com.example.data.repository

import com.example.core.network.NetworkMonitor
import com.example.data.local.AppDatabase
import com.example.data.local.SavedArticleEntity
import com.example.data.remote.DataSourceResult
import com.example.data.remote.FallbackTrendDataSource
import com.example.domain.model.BreakingNewsItem
import com.example.domain.model.Country
import com.example.domain.model.TrendCategory
import com.example.domain.repository.NewsLoadState
import com.example.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class NewsRepositoryImpl(
    private val dataSource: FallbackTrendDataSource,
    private val networkMonitor: NetworkMonitor,
    private val database: AppDatabase
) : NewsRepository {

    override fun getBreakingNews(category: TrendCategory): Flow<List<BreakingNewsItem>> {
        return flow {
            val result = dataSource.fetchNews(category, Country.GLOBAL, max = 12)
            // The fallback data source already resolves to cache or mock on
            // failure, so by the time we get here there is always content to
            // show. A genuine empty result (nothing at all) is still emitted as
            // an empty list so the UI can render its empty state deliberately.
            emit(
                when (result) {
                    is DataSourceResult.Success -> result.data
                    else -> emptyList()
                }
            )
        }
    }

    override fun getLoadState(): Flow<NewsLoadState> {
        return combine(
            dataSource.lastError,
            networkMonitor.isConnected,
            dataSource.lastServedFrom
        ) { error, connected, servedFrom ->
            NewsLoadState(
                isOffline = !connected,
                errorMessage = error?.userMessage,
                fromCache = servedFrom == com.example.data.remote.ServedFrom.CACHE,
                fromMock = servedFrom == com.example.data.remote.ServedFrom.MOCK
            )
        }
    }

    override fun getSavedArticles(): Flow<List<BreakingNewsItem>> {
        return database.trendDao().getAllSavedArticles().map { list ->
            list.map { entity ->
                BreakingNewsItem(
                    id = entity.id,
                    headline = entity.headline,
                    source = entity.source,
                    timeAgo = entity.timeAgo,
                    imageUrl = entity.imageUrl,
                    category = try { TrendCategory.valueOf(entity.category) } catch (e: Exception) { TrendCategory.TECH },
                    trendingScore = entity.trendingScore,
                    summary = entity.summary
                )
            }
        }
    }

    override suspend fun toggleSaveArticle(article: BreakingNewsItem): Boolean {
        val isSaved = database.trendDao().isArticleSaved(article.id)
        if (isSaved) {
            database.trendDao().deleteSavedArticle(article.id)
            return false
        } else {
            database.trendDao().insertSavedArticle(
                SavedArticleEntity(
                    id = article.id,
                    headline = article.headline,
                    source = article.source,
                    timeAgo = article.timeAgo,
                    imageUrl = article.imageUrl,
                    category = article.category.name,
                    trendingScore = article.trendingScore,
                    summary = article.summary
                )
            )
            return true
        }
    }
}
