package com.example.data.repository

import com.example.core.network.NetworkMonitor
import com.example.data.local.AppDatabase
import com.example.data.local.RecentlyViewedEntity
import com.example.data.local.SavedTrendEntity
import com.example.data.local.SearchHistoryEntity
import com.example.data.remote.DataSourceResult
import com.example.data.remote.FallbackTrendDataSource
import com.example.data.remote.MockTrendsDataSource
import com.example.data.remote.TrendScoreCalculator
import com.example.domain.model.*
import com.example.domain.repository.NewsLoadState
import com.example.domain.repository.TrendRepository
import kotlinx.coroutines.flow.*

class TrendRepositoryImpl(
    private val dataSource: FallbackTrendDataSource,
    private val networkMonitor: NetworkMonitor,
    private val database: AppDatabase
) : TrendRepository {

    // Starts with the bundled mock so the UI is never empty; replaced with real
    // data as soon as a remote/cached fetch succeeds.
    private val _trends = MutableStateFlow(MockTrendsDataSource.getInitialTrends())

    private fun toTrendItems(
        result: DataSourceResult<List<BreakingNewsItem>>,
        country: Country
    ): List<TrendItem> = when (result) {
        is DataSourceResult.Success ->
            result.data.map { TrendScoreCalculator.toTrendItem(it, result.data, country) }
        is DataSourceResult.Empty -> _trends.value
        is DataSourceResult.Error -> _trends.value
    }

    private fun applySavedFlags(trends: List<TrendItem>, savedIds: Set<String>): List<TrendItem> =
        trends.map { it.copy(isSaved = savedIds.contains(it.id)) }

    override fun getTrendingNow(category: TrendCategory): Flow<List<TrendItem>> {
        return flow {
            val result = dataSource.fetchNews(category, Country.GLOBAL, max = 20)
            _trends.value = toTrendItems(result, Country.GLOBAL)
            emit(Unit)
        }.flatMapLatest {
            combine(_trends, database.trendDao().getAllSavedTrends()) { trends, saved ->
                val savedIds = saved.map { it.id }.toSet()
                val filtered = if (category == TrendCategory.ALL) trends else trends.filter { it.category == category }
                applySavedFlags(filtered, savedIds).sortedByDescending { it.score.totalScore }
            }
        }
    }

    override fun getExploreTrends(
        query: String,
        category: TrendCategory,
        country: Country,
        timeFilter: TimeFilter
    ): Flow<List<TrendItem>> {
        return flow {
            val result = dataSource.fetchNews(category, country, query, max = 30, timeFilter = timeFilter)
            _trends.value = toTrendItems(result, country)
            emit(Unit)
        }.flatMapLatest {
            combine(_trends, database.trendDao().getAllSavedTrends()) { trends, saved ->
                val savedIds = saved.map { it.id }.toSet()
                applySavedFlags(trends, savedIds)
            }
        }
    }

    override suspend fun getTrendById(id: String): TrendItem? {
        val current = _trends.value.find { it.id == id }
        if (current != null) {
            return current.copy(isSaved = database.trendDao().isTrendSaved(id))
        }
        // Not in the current set — fetch a general batch and search for it.
        val result = dataSource.fetchNews(TrendCategory.ALL, Country.GLOBAL, max = 30)
        val trends = toTrendItems(result, Country.GLOBAL)
        _trends.value = trends
        return trends.find { it.id == id }?.copy(isSaved = database.trendDao().isTrendSaved(id))
    }

    override fun getTrendAnalytics(): Flow<TrendAnalytics> {
        return flow {
            val trends = _trends.value
            val upward = trends.count { it.isTrendingUp }
            emit(
                TrendAnalytics(
                    totalActiveTrends = trends.size.coerceAtLeast(1),
                    upwardTrendingCount = upward,
                    downwardTrendingCount = trends.size - upward,
                    stableTrendingCount = 0,
                    averageGrowthRate = "${trends.map { it.growthPercentage }.average().toInt()}%",
                    totalMentions24h = "${trends.size * 120}K",
                    topTrendingCategory = trends.maxByOrNull { it.score.totalScore }?.category?.displayName ?: "Technology"
                )
            )
        }
    }

    override fun getLoadState(): Flow<NewsLoadState> {
        return combine(
            dataSource.lastError,
            networkMonitor.isConnected,
            dataSource.lastServedFrom,
            dataSource.rateLimitedUntilMs
        ) { error, connected, servedFrom, rateLimitedUntil ->
            val remainingSec = ((rateLimitedUntil - System.currentTimeMillis() + 999) / 1000)
                .coerceAtLeast(0)
            val fromCache = servedFrom == com.example.data.remote.ServedFrom.CACHE
            val fromMock = servedFrom == com.example.data.remote.ServedFrom.MOCK
            val soft = error != null && (fromCache || fromMock)
            NewsLoadState(
                isOffline = !connected,
                errorMessage = error?.userMessage,
                fromCache = fromCache,
                fromMock = fromMock,
                isSoftStatus = soft,
                canRetry = remainingSec <= 0L,
                retryAfterSeconds = remainingSec
            )
        }
    }

    override fun getSavedTrends(): Flow<List<TrendItem>> {
        return combine(database.trendDao().getAllSavedTrends(), _trends) { savedList, allTrends ->
            val trendsMap = allTrends.associateBy { it.id }
            savedList.mapNotNull { entity ->
                val existing = trendsMap[entity.id]
                existing?.copy(isSaved = true) ?: TrendItem(
                    id = entity.id,
                    title = entity.title,
                    category = try { TrendCategory.valueOf(entity.category) } catch (e: Exception) { TrendCategory.TECH },
                    score = TrendScore(
                        totalScore = entity.totalScore,
                        searchGrowth = 85,
                        socialMentions = 80,
                        engagement = 80,
                        newsCoverage = 75,
                        growthVelocity = 80
                    ),
                    growthPercentage = entity.growthPercentage,
                    discussionsCount = entity.discussionsCount,
                    searchVolume = entity.searchVolume,
                    timeAgo = entity.timeAgo,
                    country = try { Country.valueOf(entity.country) } catch (e: Exception) { Country.GLOBAL },
                    chartData = listOf(50f, 60f, 75f, 85f, 90f),
                    historyToday = listOf(TrendHistoryPoint("Now", entity.totalScore.toFloat())),
                    history7Days = emptyList(),
                    history30Days = emptyList(),
                    sourceIcons = listOf("X", "Google"),
                    isTrendingUp = true,
                    isSaved = true,
                    isBreaking = false,
                    aiAnalysis = AIAnalysis(
                        summary = entity.whyTrending,
                        whyTrending = entity.whyTrending,
                        sentiment = SentimentBreakdown(75, 20, 5, "Positive"),
                        expectedGrowthPercent = entity.expectedGrowth,
                        viralProbabilityPercent = entity.viralProbability,
                        aiConfidencePercent = entity.aiConfidence,
                        growthTrajectory = "Steady",
                        relatedTopics = emptyList(),
                        timeline = emptyList()
                    )
                )
            }
        }
    }

    override suspend fun toggleSaveTrend(trend: TrendItem): Boolean {
        val isCurrentlySaved = database.trendDao().isTrendSaved(trend.id)
        if (isCurrentlySaved) {
            database.trendDao().deleteSavedTrend(trend.id)
            return false
        } else {
            database.trendDao().insertSavedTrend(
                SavedTrendEntity(
                    id = trend.id,
                    title = trend.title,
                    category = trend.category.name,
                    totalScore = trend.score.totalScore,
                    growthPercentage = trend.growthPercentage,
                    discussionsCount = trend.discussionsCount,
                    searchVolume = trend.searchVolume,
                    country = trend.country.name,
                    timeAgo = trend.timeAgo,
                    whyTrending = trend.aiAnalysis.whyTrending,
                    expectedGrowth = trend.aiAnalysis.expectedGrowthPercent,
                    viralProbability = trend.aiAnalysis.viralProbabilityPercent,
                    aiConfidence = trend.aiAnalysis.aiConfidencePercent
                )
            )
            return true
        }
    }

    override fun getRecentlyViewedTrends(): Flow<List<TrendItem>> {
        return combine(database.trendDao().getRecentlyViewed(), _trends) { viewedList, allTrends ->
            val trendsMap = allTrends.associateBy { it.id }
            viewedList.mapNotNull { entity -> trendsMap[entity.trendId] }
        }
    }

    override suspend fun recordTrendView(trend: TrendItem) {
        database.trendDao().recordRecentlyViewed(
            RecentlyViewedEntity(
                trendId = trend.id,
                title = trend.title,
                category = trend.category.name,
                score = trend.score.totalScore,
                growthPercentage = trend.growthPercentage
            )
        )
    }

    override suspend fun refreshTrends(): Result<Unit> {
        return runCatching {
            val result = dataSource.fetchNews(TrendCategory.ALL, Country.GLOBAL, max = 20)
            _trends.value = toTrendItems(result, Country.GLOBAL)
        }
    }

    override fun getRecentSearches(): Flow<List<String>> {
        return database.trendDao().getRecentSearches()
    }

    override suspend fun addRecentSearch(query: String) {
        if (query.isNotBlank()) {
            database.trendDao().insertSearchQuery(SearchHistoryEntity(query.trim()))
        }
    }

    override suspend fun clearRecentSearches() {
        database.trendDao().clearSearchHistory()
    }

    override fun getTrendingSearchSuggestions(): List<String> {
        return MockTrendsDataSource.trendingSearchSuggestions
    }
}
