package com.example.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrendAnalysisResultTest {

    @Test
    fun `default result is safe`() {
        val result = TrendAnalysisResult()
        assertEquals("", result.trendSummary)
        assertEquals("Neutral", result.sentiment)
        assertEquals("Stable", result.growthPrediction)
        assertTrue(result.relatedTopics.isEmpty())
        assertTrue(result.keyInsights.isEmpty())
        assertTrue(result.riskFactors.isEmpty())
        assertTrue(result.sentimentScore in 0..100)
    }

    @Test
    fun `not configured fallback has helpful message`() {
        val result = TrendAnalysisResult.NOT_CONFIGURED
        assertEquals("AI analysis requires a Gemini API key.", result.trendSummary)
        assertTrue(result.keyInsights.contains("Add GEMINI_API_KEY to local.properties"))
    }

    @Test
    fun `offline fallback has helpful message`() {
        val result = TrendAnalysisResult.OFFLINE
        assertEquals("No cached AI analysis available.", result.trendSummary)
        assertTrue(result.whyTrending.contains("Connect to the internet"))
    }

    @Test
    fun `isRecent returns true within ttl`() {
        val result = TrendAnalysisResult(analyzedAt = System.currentTimeMillis())
        assertTrue(result.isRecent())
    }

    @Test
    fun `isRecent returns false for old analysis`() {
        val result = TrendAnalysisResult(analyzedAt = System.currentTimeMillis() - 10_000_000L)
        assertFalse(result.isRecent())
    }
}
