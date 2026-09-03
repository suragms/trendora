package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrendDao {
    // Saved Trends
    @Query("SELECT * FROM saved_trends ORDER BY savedAtTimestamp DESC")
    fun getAllSavedTrends(): Flow<List<SavedTrendEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_trends WHERE id = :id)")
    suspend fun isTrendSaved(id: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedTrend(trend: SavedTrendEntity)

    @Query("DELETE FROM saved_trends WHERE id = :id")
    suspend fun deleteSavedTrend(id: String)

    // Saved Articles
    @Query("SELECT * FROM saved_articles ORDER BY savedAtTimestamp DESC")
    fun getAllSavedArticles(): Flow<List<SavedArticleEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_articles WHERE id = :id)")
    suspend fun isArticleSaved(id: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedArticle(article: SavedArticleEntity)

    @Query("DELETE FROM saved_articles WHERE id = :id")
    suspend fun deleteSavedArticle(id: String)

    // Search History
    @Query("SELECT query FROM search_history ORDER BY timestamp DESC LIMIT 10")
    fun getRecentSearches(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchQuery(search: SearchHistoryEntity)

    @Query("DELETE FROM search_history")
    suspend fun clearSearchHistory()

    // Recently Viewed
    @Query("SELECT * FROM recently_viewed ORDER BY viewedAtTimestamp DESC LIMIT 20")
    fun getRecentlyViewed(): Flow<List<RecentlyViewedEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordRecentlyViewed(viewed: RecentlyViewedEntity)

    // Notifications
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: String)

    @Query("DELETE FROM notifications")
    suspend fun clearAllNotifications()

    // News Cache (offline support)
    @Query("SELECT * FROM news_cache ORDER BY fetchedAt DESC")
    suspend fun getAllCachedNews(): List<NewsCacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedNews(items: List<NewsCacheEntity>)

    @Query("DELETE FROM news_cache")
    suspend fun clearCachedNews()

    // AI Analysis Cache
    @Query("SELECT * FROM ai_analysis_cache WHERE trendId = :trendId")
    suspend fun getCachedAnalysis(trendId: String): AiAnalysisCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedAnalysis(analysis: AiAnalysisCacheEntity)

    @Query("DELETE FROM ai_analysis_cache WHERE trendId = :trendId")
    suspend fun deleteCachedAnalysis(trendId: String)

    @Query("DELETE FROM ai_analysis_cache")
    suspend fun clearAllCachedAnalyses()

    @Query("SELECT * FROM ai_analysis_cache ORDER BY analyzedAt DESC")
    suspend fun getAllCachedAnalyses(): List<AiAnalysisCacheEntity>
}
