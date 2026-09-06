package com.example.data.remote

import com.example.domain.model.AIAnalysis
import com.example.domain.model.BreakingNewsItem
import com.example.domain.model.Country
import com.example.domain.model.SentimentBreakdown
import com.example.domain.model.TrendHistoryPoint
import com.example.domain.model.TrendItem
import com.example.domain.model.TrendScore
import java.util.Locale

/**
 * Computes a Trendora [TrendScore] (0-100) and related fields from real news data.
 *
 * IMPORTANT: GNews does NOT provide a trend/virality score, discussion counts,
 * search volumes, or measured growth percentages. Trendora never presents such
 * fabricated absolute metrics. Instead:
 *   - The score is a Trendora-computed estimate derived from real signals we DO have:
 *     - Recency (how fresh the article is)
 *     - Source popularity (known, high-traffic outlets rank higher)
 *     - Article frequency (how many articles share this category/topic)
 *     - Category activity (share of the fetched set)
 *   - "Social interest" / "Search interest" are qualitative bands
 *     ("Very high" .. "Low") derived from those score components — never counts.
 *   - Growth % and sentiment are deterministic estimates, explicitly labeled as such.
 *
 * All of this is clearly separate from the official API data and is never
 * presented as something GNews reported.
 */
object TrendScoreCalculator {

    // A small, curated popularity weight for well-known global outlets.
    private val POPULAR_SOURCES = setOf(
        "BBC", "CNN", "Reuters", "The Guardian", "AP", "Associated Press",
        "The New York Times", "NYT", "The Washington Post", "Forbes",
        "Bloomberg", "CNBC", "The Verge", "TechCrunch", "Wired", "Engadget",
        "The Wall Street Journal", "WSJ", "Financial Times", "Al Jazeera",
        "NPR", "Business Insider", "Axios", "The Economist", "Sky News",
        "The Times of India", "NDTV", "India Today", "The Hindu"
    )

    /**
     * Builds a [TrendItem] from a single real [BreakingNewsItem], given the full
     * fetched set [allArticles] (used to compute frequency/activity signals).
     */
    fun toTrendItem(
        article: BreakingNewsItem,
        allArticles: List<BreakingNewsItem>,
        country: Country
    ): TrendItem {
        val score = calculateScore(article, allArticles)
        val growth = computeGrowth(article, score)

        return TrendItem(
            id = article.id,
            title = article.headline,
            category = article.category,
            score = score,
            growthPercentage = growth,
            discussionsCount = "${socialInterestBand(score)} social interest",
            searchVolume = "${searchInterestBand(score)} search interest",
            timeAgo = article.timeAgo,
            country = country,
            chartData = generateChartData(score, growth),
            historyToday = listOf(
                TrendHistoryPoint("Now", score.totalScore.toFloat())
            ),
            history7Days = emptyList(),
            history30Days = emptyList(),
            sourceIcons = listOf(article.source.take(8)),
            isTrendingUp = score.totalScore >= 50,
            isBreaking = score.totalScore >= 80,
            aiAnalysis = AIAnalysis(
                summary = article.summary.ifBlank { article.headline },
                whyTrending = article.summary.ifBlank { article.headline },
                sentiment = estimateSentiment(score),
                expectedGrowthPercent = (score.totalScore * 0.55 + 10).toInt().coerceIn(10, 90),
                viralProbabilityPercent = score.totalScore,
                aiConfidencePercent = 25,
                growthTrajectory = when {
                    score.totalScore >= 80 -> "Rapid Exponential"
                    score.totalScore >= 50 -> "Steady Climb"
                    else -> "Sustained Peak"
                },
                relatedTopics = extractTopics(article.headline),
                timeline = emptyList()
            ),
            relatedNews = allArticles.filter { it.id != article.id }.take(3)
        )
    }

    /** Full 0-100 [TrendScore] with labeled sub-scores (see class doc for provenance). */
    fun calculateScore(article: BreakingNewsItem, allArticles: List<BreakingNewsItem>): TrendScore {
        val recency = recencyScore(article)             // 0..100
        val source = sourceScore(article)               // 0..100
        val frequency = frequencyScore(article, allArticles) // 0..100
        val activity = categoryActivityScore(article, allArticles) // 0..100

        // Blend into the five weighted TrendScore components.
        val searchGrowth = ((recency * 0.4 + activity * 0.6)).toInt()
        val socialMentions = ((source * 0.4 + frequency * 0.6)).toInt()
        val engagement = ((source * 0.3 + recency * 0.7)).toInt()
        val newsCoverage = ((activity * 0.5 + source * 0.5)).toInt()
        val growthVelocity = ((recency * 0.6 + frequency * 0.4)).toInt()

        return TrendScore.calculate(
            searchGrowth = searchGrowth.coerceIn(0, 100),
            socialMentions = socialMentions.coerceIn(0, 100),
            engagement = engagement.coerceIn(0, 100),
            newsCoverage = newsCoverage.coerceIn(0, 100),
            growthVelocity = growthVelocity.coerceIn(0, 100)
        )
    }

    /** Higher score the more recently the article was published. */
    private fun recencyScore(article: BreakingNewsItem): Int {
        val minutes = articleMinutesOld(article)
        return when {
            minutes < 60 -> 95
            minutes < 180 -> 85
            minutes < 360 -> 75
            minutes < 720 -> 60
            minutes < 1440 -> 45
            else -> 30
        }
    }

    /** Known, popular sources score higher. */
    private fun sourceScore(article: BreakingNewsItem): Int {
        val name = article.source.lowercase(Locale.ROOT)
        return if (POPULAR_SOURCES.any { name.contains(it.lowercase(Locale.ROOT)) }) 90 else 55
    }

    /** Articles that belong to a busier category score higher. */
    private fun categoryActivityScore(article: BreakingNewsItem, allArticles: List<BreakingNewsItem>): Int {
        val total = allArticles.size.coerceAtLeast(1)
        val count = allArticles.count { it.category == article.category }
        val ratio = count.toFloat() / total
        return (ratio * 100).toInt().coerceIn(20, 100)
    }

    /** More articles in the same category => higher "buzz" score. */
    private fun frequencyScore(article: BreakingNewsItem, allArticles: List<BreakingNewsItem>): Int {
        val count = allArticles.count { it.category == article.category }
        return when {
            count >= 8 -> 90
            count >= 5 -> 75
            count >= 3 -> 60
            else -> 40
        }
    }

    /**
     * Approximate minutes since publication, parsed from the human-readable
     * `timeAgo` string (e.g. "25m ago", "3h ago", "2d ago"). Defaults to a
     * mid-range value if it cannot be parsed.
     */
    private fun articleMinutesOld(article: BreakingNewsItem): Long {
        val t = article.timeAgo.lowercase(Locale.ROOT)
        return when {
            t.contains("m ago") -> t.replace(Regex("[^0-9]"), "").toLongOrNull() ?: 300
            t.contains("h ago") -> (t.replace(Regex("[^0-9]"), "").toLongOrNull() ?: 1) * 60
            t.contains("d ago") -> (t.replace(Regex("[^0-9]"), "").toLongOrNull() ?: 1) * 24 * 60
            t.contains("just now") -> 1
            else -> 300
        }
    }

    /**
     * A deterministic Trendora estimate of growth % (0..180). GNews offers no
     * measured growth, so this is derived from the real signals we do have:
     * article freshness (recency) blended with the computed total score.
     * Deterministic for a given article — never random, never presented as measured.
     */
    private fun computeGrowth(article: BreakingNewsItem, score: TrendScore): Int {
        val freshness = recencyScore(article)
        return (freshness * 0.5 + score.totalScore * 0.5).toInt().coerceIn(5, 180)
    }

    /**
     * Qualitative reach level derived from the socialMentions component
     * (itself a Trendora estimate from real source/frequency signals).
     * Public so screens reuse the same label in the score-grid pill.
     */
    fun socialInterestBand(score: TrendScore): String = when {
        score.socialMentions >= 85 -> "Very high"
        score.socialMentions >= 65 -> "High"
        score.socialMentions >= 45 -> "Moderate"
        else -> "Low"
    }

    /** Qualitative search-interest level, derived from the searchGrowth component. */
    fun searchInterestBand(score: TrendScore): String = when {
        score.searchGrowth >= 85 -> "Very high"
        score.searchGrowth >= 65 -> "High"
        score.searchGrowth >= 45 -> "Moderate"
        else -> "Low"
    }

    /** Estimated sentiment breakdown — bounded, deterministic, and explicitly labeled an estimate. */
    private fun estimateSentiment(score: TrendScore): SentimentBreakdown {
        val pos = (score.socialMentions * 0.35 + 30).toInt().coerceIn(35, 70)
        val neg = (score.totalScore / 6).toInt().coerceAtMost(18)
        val neu = (100 - pos - neg).coerceIn(0, 100)
        return SentimentBreakdown(
            positivePercent = pos,
            neutralPercent = neu,
            negativePercent = neg,
            summary = "Trendora estimate from coverage & activity signals — not measured social sentiment."
        )
    }

    /** Deterministic, monotone sparkline shape derived from the real score + growth estimate. */
    private fun generateChartData(score: TrendScore, growth: Int): List<Float> {
        val points = 8
        val start = 0.25f + (score.totalScore / 100f) * 0.20f
        val end = (0.55f + (score.totalScore / 100f) * 0.25f + growth / 400f).coerceAtMost(0.98f)
        return List(points) { i ->
            val t = i.toFloat() / (points - 1)
            (start + (end - start) * t).coerceIn(0.1f, 1f)
        }
    }

    /** A couple of rough topic keywords from the headline for the "related topics" row. */
    private fun extractTopics(headline: String): List<String> {
        val words = headline
            .replace(Regex("[^A-Za-z0-9 ]"), " ")
            .split(Regex("\\s+"))
            .filter { it.length > 3 && !STOP_WORDS.contains(it.lowercase(Locale.ROOT)) }
            .take(3)
        return if (words.isEmpty()) listOf("Trending") else words
    }

    private val STOP_WORDS = setOf(
        "that", "this", "with", "from", "have", "will", "they", "their", "there",
        "what", "when", "where", "which", "while", "after", "before", "about",
        "into", "over", "than", "then", "these", "those", "your", "them", "being",
        "been", "were", "was", "are", "the", "and", "for", "not", "you", "all"
    )
}
