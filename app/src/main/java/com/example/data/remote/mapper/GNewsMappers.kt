package com.example.data.remote.mapper

import com.example.data.remote.dto.GNewsArticleDto
import com.example.domain.model.BreakingNewsItem
import com.example.domain.model.Country
import com.example.domain.model.TrendCategory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Converts GNews DTOs into domain models. Keeps all API DTOs out of the UI layer.
 */
object GNewsMappers {

    /** Stable-ish id derived from the article URL (unique per article). */
    private fun stableId(url: String?): String =
        "gnews_" + (url?.hashCode()?.let { if (it == Int.MIN_VALUE) 0 else kotlin.math.abs(it) } ?: 0)

    /** Map a GNews category token to a Trendora [TrendCategory]. */
    fun toTrendCategory(gnewsCategory: String?): TrendCategory = when (gnewsCategory?.lowercase(Locale.ROOT)) {
        "technology" -> TrendCategory.TECH
        "entertainment" -> TrendCategory.ENTERTAINMENT
        "sports" -> TrendCategory.SPORTS
        "business" -> TrendCategory.BUSINESS
        "world" -> TrendCategory.WORLD
        "science" -> TrendCategory.SCIENCE
        "health" -> TrendCategory.HEALTH
        else -> TrendCategory.ALL
    }

    /** Map a Trendora category to a GNews category token (or null for "general"). */
    fun toGNewsCategory(category: TrendCategory): String? = when (category) {
        TrendCategory.TECH, TrendCategory.AI -> "technology"
        TrendCategory.ENTERTAINMENT, TrendCategory.GAMING, TrendCategory.MUSIC -> "entertainment"
        TrendCategory.SPORTS -> "sports"
        TrendCategory.BUSINESS -> "business"
        TrendCategory.WORLD -> "world"
        TrendCategory.SCIENCE -> "science"
        TrendCategory.HEALTH -> "health"
        TrendCategory.ALL -> null // "general"
    }

    /** Map a Trendora country to a GNews ISO country code (or null for global). */
    fun toGNewsCountry(country: Country): String? = when (country) {
        Country.INDIA -> "in"
        Country.USA -> "us"
        Country.UK -> "gb"
        Country.JAPAN -> "jp"
        Country.GERMANY -> "de"
        Country.GLOBAL -> null
    }

    /** Human-readable relative time, e.g. "25m ago" / "3h ago" / "2d ago". */
    fun toTimeAgo(publishedAt: String?): String {
        val parsed = parseDate(publishedAt) ?: return "Just now"
        val now = System.currentTimeMillis()
        val diff = now - parsed
        val minutes = diff / 60_000L
        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            minutes < 24 * 60 -> "${minutes / 60}h ago"
            else -> "${minutes / (24 * 60)}d ago"
        }
    }

    /** Parses GNews ISO-8601 timestamps (e.g. 2024-01-01T12:00:00Z). */
    fun parseDate(publishedAt: String?): Long? {
        if (publishedAt.isNullOrBlank()) return null
        return try {
            val formats = listOf(
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd'T'HH:mm:ssXXX"
            )
            for (format in formats) {
                val sdf = SimpleDateFormat(format, Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                    isLenient = false
                }
                val d = sdf.parse(publishedAt)
                if (d != null) return d.time
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    /** Maps a single article DTO to a domain [BreakingNewsItem]. */
    fun toBreakingNews(dto: GNewsArticleDto, category: TrendCategory): BreakingNewsItem {
        val timeAgo = toTimeAgo(dto.publishedAt)
        return BreakingNewsItem(
            id = stableId(dto.url),
            headline = dto.title ?: "Untitled",
            source = dto.source?.name ?: "Unknown source",
            timeAgo = timeAgo,
            imageUrl = dto.image ?: "",
            category = category,
            trendingScore = basicScore(timeAgo, dto.source?.name),
            readTimeMinutes = estimateReadTime(dto.description, dto.content),
            summary = dto.description ?: dto.content ?: ""
        )
    }

    /**
     * A lightweight 50-95 "heat" score for the breaking-news card, based on
     * freshness and source. It is a Trendora estimate, not an official API metric.
     */
    private fun basicScore(timeAgo: String, sourceName: String?): Int {
        val recency = when {
            timeAgo.contains("m ago") -> 0
            timeAgo.contains("h ago") -> 10
            timeAgo.contains("d ago") -> 25
            else -> 5
        }
        val sourceBonus = if (sourceName?.lowercase(Locale.ROOT) in KNOWN_SOURCES) 10 else 0
        return (90 - recency + sourceBonus).coerceIn(50, 95)
    }

    private val KNOWN_SOURCES = setOf(
        "bbc", "cnn", "reuters", "the guardian", "ap", "associated press",
        "the new york times", "forbes", "bloomberg", "cnbc", "the verge",
        "techcrunch", "wired", "al jazeera", "npr"
    )

    private fun estimateReadTime(description: String?, content: String?): Int {
        val text = "${description ?: ""} ${content ?: ""}"
        val words = text.trim().split(Regex("\\s+")).size
        return (words / 200).coerceIn(1, 8)
    }
}
