package com.example.data.remote

import com.example.domain.model.BreakingNewsItem
import com.example.domain.model.Country
import com.example.domain.model.TrendCategory
import com.example.domain.model.TrendScore
import com.example.domain.model.TrendTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrendScoreCalculatorTest {

    private fun article(id: String, source: String, category: TrendCategory, timeAgo: String) =
        BreakingNewsItem(
            id = id,
            headline = "Headline $id",
            source = source,
            timeAgo = timeAgo,
            imageUrl = "",
            category = category,
            trendingScore = 0,
            summary = "Some summary text."
        )

    @Test
    fun `score is always within 0-100`() {
        val all = listOf(
            article("1", "BBC", TrendCategory.TECH, "5m ago"),
            article("2", "Unknown", TrendCategory.TECH, "1h ago"),
            article("3", "CNN", TrendCategory.SPORTS, "2d ago")
        )
        val score = TrendScoreCalculator.calculateScore(all.first(), all)
        assertTrue(score.totalScore in 0..100)
        assertTrue(score.searchGrowth in 0..100)
        assertTrue(score.newsCoverage in 0..100)
    }

    @Test
    fun `fresh popular-source article scores higher than stale obscure one`() {
        val all = listOf(
            article("1", "Reuters", TrendCategory.TECH, "5m ago"),
            article("2", "Some Blog", TrendCategory.TECH, "5d ago")
        )
        val fresh = TrendScoreCalculator.calculateScore(all[0], all)
        val stale = TrendScoreCalculator.calculateScore(all[1], all)
        assertTrue(fresh.totalScore > stale.totalScore)
    }

    @Test
    fun `tier labels map to the documented ranges`() {
        val viral = TrendScore(95, 90, 90, 90, 90, 90).tier
        val rising = TrendScore(75, 90, 90, 90, 90, 90).tier
        val popular = TrendScore(50, 90, 90, 90, 90, 90).tier
        val low = TrendScore(20, 90, 90, 90, 90, 90).tier

        assertEquals(TrendTier.VIRAL, viral)
        assertEquals(TrendTier.RISING, rising)
        assertEquals(TrendTier.POPULAR, popular)
        assertEquals(TrendTier.LOW_ACTIVITY, low)
    }

    @Test
    fun `builds a trend item from real article data`() {
        val all = listOf(
            article("1", "BBC", TrendCategory.TECH, "10m ago"),
            article("2", "CNN", TrendCategory.TECH, "20m ago"),
            article("3", "Reuters", TrendCategory.SPORTS, "1h ago")
        )
        val trend = TrendScoreCalculator.toTrendItem(all[0], all, Country.GLOBAL)

        assertEquals(all[0].headline, trend.title)
        assertEquals(TrendCategory.TECH, trend.category)
        assertTrue(trend.growthPercentage > 0)
        assertTrue(trend.chartData.isNotEmpty())
        assertEquals(Country.GLOBAL, trend.country)
        assertEquals(2, trend.relatedNews.size)
    }

    @Test
    fun `trend labels are qualitative bands, never fabricated counts`() {
        val all = listOf(
            article("1", "BBC", TrendCategory.TECH, "5m ago"),
            article("2", "Unknown", TrendCategory.TECH, "1h ago"),
            article("3", "CNN", TrendCategory.SPORTS, "2d ago")
        )
        val trend = TrendScoreCalculator.toTrendItem(all[0], all, Country.GLOBAL)

        // No fake absolute metrics ("+X discussions", "Y searches").
        assertFalse(trend.discussionsCount.contains("K "))
        assertFalse(trend.discussionsCount.contains("discussion"))
        assertFalse(trend.searchVolume.contains("searches"))
        assertTrue(trend.discussionsCount.endsWith("social interest"))
        assertTrue(trend.searchVolume.endsWith("search interest"))

        // Growth is bounded and stays a Trendora estimate, never hash-random.
        assertTrue(trend.growthPercentage in 5..180)
        trend.chartData.forEach { assertTrue(it in 0.1f..1f) }
        // Without a live Gemini call, confidence must stay low and honest.
        assertEquals(25, trend.aiAnalysis.aiConfidencePercent)
    }

    @Test
    fun `growth and chart data are deterministic for a given article`() {
        val all = listOf(
            article("1", "BBC", TrendCategory.TECH, "10m ago"),
            article("2", "CNN", TrendCategory.TECH, "20m ago"),
            article("3", "Reuters", TrendCategory.SPORTS, "1h ago")
        )
        val a = TrendScoreCalculator.toTrendItem(all[0], all, Country.GLOBAL)
        val b = TrendScoreCalculator.toTrendItem(all[0], all, Country.GLOBAL)

        assertEquals(a.growthPercentage, b.growthPercentage)
        assertEquals(a.chartData, b.chartData)
        assertEquals(a.discussionsCount, b.discussionsCount)
        assertEquals(a.searchVolume, b.searchVolume)
    }

    @Test
    fun `social and search interest bands map to known tiers`() {
        val veryHigh = TrendScore(95, 90, 95, 90, 95, 90) // searchGrowth=90, socialMentions=95
        val low = TrendScore(20, 40, 40, 40, 40, 40)     // searchGrowth=40, socialMentions=40

        assertEquals("Very high", TrendScoreCalculator.socialInterestBand(veryHigh))
        assertEquals("Very high", TrendScoreCalculator.searchInterestBand(veryHigh))
        assertEquals("Low", TrendScoreCalculator.socialInterestBand(low))
        assertEquals("Low", TrendScoreCalculator.searchInterestBand(low))
    }
}
