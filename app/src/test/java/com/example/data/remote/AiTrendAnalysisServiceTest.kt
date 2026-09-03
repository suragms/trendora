package com.example.data.remote

import com.example.core.network.GeminiApiClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for structured AI output parsing. Uses a fake Gemini client so no
 * network or Android dependency is required.
 */
class AiTrendAnalysisServiceTest {

    private class FakeGeminiClient(
        var response: String? = null,
        override val isConfigured: Boolean = true
    ) : GeminiApiClient() {
        override suspend fun generateContent(prompt: String): String? = response
    }

    private fun service(client: FakeGeminiClient) = AiTrendAnalysisService(client)

    @Test
    fun `parses valid json response`() = runTest {
        val json = """
            {
              "trendSummary": "AI is transforming industries.",
              "whyTrending": "Major announcements drove interest.",
              "sentiment": "Positive",
              "sentimentScore": 80,
              "growthPrediction": "Rising",
              "predictedGrowth": 60,
              "viralProbability": 85,
              "aiConfidence": 90,
              "relatedTopics": ["Generative AI", "OpenAI", "Machine Learning"],
              "keyInsights": ["Adoption growing", "Investment increasing"],
              "riskFactors": ["Regulation", "Bias"]
            }
        """.trimIndent()
        val result = service(FakeGeminiClient(response = json)).analyzeTrend("AI", "Technology")

        assertEquals("AI is transforming industries.", result.trendSummary)
        assertEquals("Positive", result.sentiment)
        assertEquals(80, result.sentimentScore)
        assertEquals("Rising", result.growthPrediction)
        assertEquals(60, result.predictedGrowth)
        assertEquals(85, result.viralProbability)
        assertEquals(90, result.aiConfidence)
        assertEquals(listOf("Generative AI", "OpenAI", "Machine Learning"), result.relatedTopics)
        assertEquals(2, result.keyInsights.size)
        assertEquals(2, result.riskFactors.size)
    }

    @Test
    fun `clamps out-of-range numbers to 0-100`() = runTest {
        val json = """
            {
              "sentiment": "Positive",
              "sentimentScore": 500,
              "viralProbability": -10,
              "aiConfidence": 999,
              "predictedGrowth": 150
            }
        """.trimIndent()
        val result = service(FakeGeminiClient(response = json)).analyzeTrend("X", "Tech")

        assertEquals(100, result.sentimentScore)
        assertEquals(0, result.viralProbability)
        assertEquals(100, result.aiConfidence)
        assertEquals(100, result.predictedGrowth)
    }

    @Test
    fun `handles json wrapped in markdown code fences`() = runTest {
        val json = """
            ```json
            {
              "trendSummary": "Fusion energy breakthroughs.",
              "sentiment": "Neutral",
              "sentimentScore": 50,
              "growthPrediction": "Stable",
              "predictedGrowth": 10,
              "viralProbability": 40,
              "aiConfidence": 70
            }
            ```
        """.trimIndent()
        val result = service(FakeGeminiClient(response = json)).analyzeTrend("Fusion", "Science")

        assertEquals("Fusion energy breakthroughs.", result.trendSummary)
        assertEquals("Neutral", result.sentiment)
        assertEquals(40, result.viralProbability)
    }

    @Test
    fun `does not crash on invalid json and returns fallback`() = runTest {
        val invalid = "This is not JSON at all. No valid structure here."
        val result = service(FakeGeminiClient(response = invalid)).analyzeTrend("X", "Tech")

        assertNotNull(result)
        // Should fall back to using the raw text as summary
        assertTrue(result.trendSummary.contains("not JSON"))
    }

    @Test
    fun `empty response produces safe fallback`() = runTest {
        val result = service(FakeGeminiClient(response = "")).analyzeTrend("X", "Tech")

        assertNotNull(result)
        assertEquals("Neutral", result.sentiment)
        assertTrue(result.sentimentScore in 0..100)
    }

    @Test
    fun `not configured returns not-configured result`() = runTest {
        val result = service(FakeGeminiClient(response = "{}", isConfigured = false))
            .analyzeTrend("X", "Tech")

        assertEquals("AI analysis requires a Gemini API key.", result.trendSummary)
    }

    @Test
    fun `normalizes sentiment values`() = runTest {
        val json = """
            { "sentiment": "POSITIVE", "sentimentScore": 70,
              "growthPrediction": "rising", "predictedGrowth": 20,
              "viralProbability": 50, "aiConfidence": 60 }
        """.trimIndent()
        val result = service(FakeGeminiClient(response = json)).analyzeTrend("X", "Tech")

        assertEquals("Positive", result.sentiment)
        assertEquals("Rising", result.growthPrediction)
    }

    private fun runTest(block: suspend () -> Unit) {
        kotlinx.coroutines.runBlocking { block() }
    }
}
