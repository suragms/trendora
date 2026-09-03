package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_trends")
data class SavedTrendEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val totalScore: Int,
    val growthPercentage: Int,
    val discussionsCount: String,
    val searchVolume: String,
    val country: String,
    val timeAgo: String,
    val whyTrending: String,
    val expectedGrowth: Int,
    val viralProbability: Int,
    val aiConfidence: Int,
    val savedAtTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_articles")
data class SavedArticleEntity(
    @PrimaryKey val id: String,
    val headline: String,
    val source: String,
    val timeAgo: String,
    val imageUrl: String,
    val category: String,
    val trendingScore: Int,
    val summary: String,
    val savedAtTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey val query: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "recently_viewed")
data class RecentlyViewedEntity(
    @PrimaryKey val trendId: String,
    val title: String,
    val category: String,
    val score: Int,
    val growthPercentage: Int,
    val viewedAtTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val message: String,
    val type: String,
    val timeAgo: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val trendId: String? = null
)
