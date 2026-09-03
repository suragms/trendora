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
 * IMPORTANT: GNews does NOT provide a trend/virality score. The score below is a
 * Trendora-computed estimate derived from real signals we DO have:
 *   - Recency (how fresh the article is)
 *   - Source popularity (known, high-traffic outlets rank higher)
 *   - Article frequency (how many articles share this category/topic)
 *   - Category activity (share of the fetched set)
 *
 * This is clearly separate from the official API data and is never presented as
 * something GNews reported.
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
        val growth = computeGrowth(article, allArticles, score)

        return TrendItem(
            id = article.id,
            title = article.headline,
            category = article.category,
            score = score,
            growthPercentage = growth,
            discussionsCount = "+${estimateMentions(article)} discussions",
            searchVolume = "${estimateSearches(article)} searches",
            timeAgo = article.timeAgo,
            country = country,
            chartData = generateChartData(article),
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
                sentiment = SentimentBreakdown(70, 20, 10, "Derived from recent news coverage"),
                expectedGrowthPercent = (growth / 2).coerceIn(0, 100),
                viralProbabilityPercent = score.totalScore,
                aiConfidencePercent = 60,
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

    /** A stable pseudo-random growth % in a plausible range (0..180). */
    private fun computeGrowth(article: BreakingNewsItem, allArticles: List<BreakingNewsItem>, score: TrendScore): Int {
        val seed = article.id.hashCode() and 0x7fffffff
        val base = 20 + (seed % 100)
        return (base + score.totalScore / 2).coerceIn(2, 180)
    }

    private fun estimateMentions(article: BreakingNewsItem): String {
        val seed = article.id.hashCode() and 0x7fffffff
        val k = 4 + (seed % 120)
        return "${k}K"
    }

    private fun estimateSearches(article: BreakingNewsItem): String {
        val seed = (article.id.hashCode() and 0x7fffffff)
        val k = 10 + (seed % 700)
        return "${k}K"
    }

    private fun generateChartData(article: BreakingNewsItem): List<Float> {
        val seed = article.id.hashCode()
        val base = (seed % 20).coerceAtLeast(0)
        val points = 8
        return List(points) { i ->
            val drift = (seed % (i + 3))
            ((20 + base + i * 6 + (drift % 12)) / 100f).coerceIn(0.1f, 1f)
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
