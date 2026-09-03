package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Caches real news articles from the remote API so the app can show content
 * while offline. The cache is a single recent snapshot: each successful remote
 * fetch clears and re-populates it, and offline reads filter it in-memory.
 */
@Entity(tableName = "news_cache")
data class NewsCacheEntity(
    @PrimaryKey val id: String,
    val headline: String,
    val source: String,
    val timeAgo: String,
    val imageUrl: String,
    val category: String,
    val trendingScore: Int,
    val readTimeMinutes: Int,
    val summary: String,
    val country: String,
    val fetchedAt: Long = System.currentTimeMillis()
)
