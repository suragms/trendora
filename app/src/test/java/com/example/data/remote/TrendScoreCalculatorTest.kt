package com.example.data.remote

import com.example.domain.model.BreakingNewsItem
import com.example.domain.model.Country
import com.example.domain.model.TrendCategory
import com.example.domain.model.TrendScore
import com.example.domain.model.TrendTier
import org.junit.Assert.assertEquals
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
}
