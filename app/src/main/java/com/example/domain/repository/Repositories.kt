package com.example.domain.repository

import com.example.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * UI-facing load status for the real data layer, so screens can show
 * user-friendly states (offline, soft live-unavailable, cached/mock)
 * without raw exceptions.
 */
data class NewsLoadState(
    val isOffline: Boolean = false,
    val errorMessage: String? = null,
    val fromCache: Boolean = false,
    val fromMock: Boolean = false,
    /** True when the status is informational (cached/mock after remote failure), not a hard failure. */
    val isSoftStatus: Boolean = false,
    /** Whether a live Retry is currently allowed (false during rate-limit cooldown). */
    val canRetry: Boolean = true,
    /** Seconds remaining before a live retry is allowed (rate-limit cooldown). */
    val retryAfterSeconds: Long = 0L
)

interface TrendRepository {
    fun getTrendingNow(category: TrendCategory = TrendCategory.ALL): Flow<List<TrendItem>>
    fun getExploreTrends(
        query: String = "",
        category: TrendCategory = TrendCategory.ALL,
        country: Country = Country.GLOBAL,
        timeFilter: TimeFilter = TimeFilter.TODAY
    ): Flow<List<TrendItem>>
    suspend fun getTrendById(id: String): TrendItem?
    fun getTrendAnalytics(): Flow<TrendAnalytics>
    fun getSavedTrends(): Flow<List<TrendItem>>
    suspend fun toggleSaveTrend(trend: TrendItem): Boolean
    fun getRecentlyViewedTrends(): Flow<List<TrendItem>>
    suspend fun recordTrendView(trend: TrendItem)
    suspend fun refreshTrends(): Result<Unit>
    fun getRecentSearches(): Flow<List<String>>
    suspend fun addRecentSearch(query: String)
    suspend fun clearRecentSearches()
    fun getTrendingSearchSuggestions(): List<String>
    fun getLoadState(): Flow<NewsLoadState>
}

interface NewsRepository {
    fun getBreakingNews(category: TrendCategory = TrendCategory.ALL): Flow<List<BreakingNewsItem>>
    fun getSavedArticles(): Flow<List<BreakingNewsItem>>
    suspend fun toggleSaveArticle(article: BreakingNewsItem): Boolean
    fun getLoadState(): Flow<NewsLoadState>
}

interface AIRepository {
    /** Whether the Gemini API key is configured and the service is ready. */
    val isConfigured: Boolean

    fun getAITrendsOverview(): Flow<List<TrendItem>>
    suspend fun generateDeepAnalysis(trendTitle: String, category: String): AIAnalysis
    suspend fun askAIAssistant(prompt: String, trendContext: String? = null): String
    fun getDailyAISummary(): Flow<String>

    // --- Structured AI Analysis (Phase 3-4) ---

    /**
     * Generate a structured AI analysis for a trend, using cache when available
     * and fresh. Returns [TrendAnalysisResult.NOT_CONFIGURED] if no API key
     * is present, and the cached version if offline.
     */
    suspend fun analyzeTrendStructured(
        trendId: String,
        trendTitle: String,
        category: String,
        headlines: List<String> = emptyList(),
        descriptions: List<String> = emptyList(),
        trendScore: Int = 0,
        growthInfo: String = "",
        forceRefresh: Boolean = false
    ): TrendAnalysisResult

    /** Retrieve a previously cached analysis, or null. */
    suspend fun getCachedAnalysis(trendId: String): TrendAnalysisResult?

    /** Regenerate the daily AI summary. */
    suspend fun refreshDailySummary(): String
}

interface UserPreferencesRepository {
    val isDarkMode: Flow<Boolean>
    val selectedCountry: Flow<Country>
    val notificationSettings: Flow<Map<String, Boolean>>
    suspend fun setDarkMode(enabled: Boolean)
    suspend fun setSelectedCountry(country: Country)
    suspend fun updateNotificationSetting(key: String, enabled: Boolean)
    fun getNotifications(): Flow<List<NotificationItem>>
    suspend fun addNotification(notification: NotificationItem)
    suspend fun markNotificationAsRead(id: String)
    suspend fun clearAllNotifications()
}
