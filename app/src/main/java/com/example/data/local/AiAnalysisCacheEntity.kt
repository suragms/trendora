package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity that caches AI trend analyses. Each row stores a complete
 * [TrendAnalysisResult] as JSON text, keyed by trend ID.
 *
 * The cache prevents duplicate Gemini API calls and enables offline access.
 * Analyses are considered "fresh" for 30 minutes by default.
 */
@Entity(tableName = "ai_analysis_cache")
data class AiAnalysisCacheEntity(
    @PrimaryKey val trendId: String,
    /** Full TrendAnalysisResult serialised as JSON. */
    val analysisJson: String,
    val trendTitle: String,
    val category: String,
    val analyzedAt: Long = System.currentTimeMillis()
)
