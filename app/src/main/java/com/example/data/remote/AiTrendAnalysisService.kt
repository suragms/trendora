package com.example.data.remote

import com.example.core.network.GeminiApiClient
import com.example.domain.model.TrendAnalysisResult
import com.example.domain.model.TrendCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Service that calls the Gemini API to generate structured AI trend analyses.
 *
 * Returns [TrendAnalysisResult] with safe defaults when the response is
 * incomplete or unparseable. Never throws — all errors produce a valid
 * fallback result.
 */
class AiTrendAnalysisService(
    private val geminiClient: GeminiApiClient
) {
    val isConfigured: Boolean get() = geminiClient.isConfigured

    /**
     * Generate a full structured analysis for a single trend.
     *
     * @param trendTitle   The trend name / headline.
     * @param category     Category display name (e.g. "Technology").
     * @param headlines    Related news headlines for context.
     * @param descriptions News article descriptions for context.
     * @param trendScore   Current trend score (0-100).
     * @param growthInfo   Growth percentage string (e.g. "+45%").
     */
    suspend fun analyzeTrend(
        trendTitle: String,
        category: String,
        headlines: List<String> = emptyList(),
        descriptions: List<String> = emptyList(),
        trendScore: Int = 0,
        growthInfo: String = ""
    ): TrendAnalysisResult = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext TrendAnalysisResult.NOT_CONFIGURED
        }

        val prompt = buildAnalysisPrompt(
            trendTitle, category, headlines, descriptions, trendScore, growthInfo
        )

        val rawResponse = geminiClient.generateContent(prompt)
            ?: return@withContext TrendAnalysisResult(
                trendSummary = "AI analysis could not be completed.",
                whyTrending = "The AI service returned an empty response.",
                keyInsights = listOf("Try again later"),
                riskFactors = listOf("Empty API response")
            )

        parseAnalysisResponse(rawResponse, trendTitle)
    }

    /**
     * Generate a brief daily summary of the top trending topics.
     */
    suspend fun generateDailySummary(): String {
        if (!isConfigured) {
            return "Configure your Gemini API key to see live AI-powered trend summaries."
        }

        val prompt = """
            You are Trendora AI, a trend intelligence system.
            Provide a 2-3 sentence summary of the most important tech and global trends happening right now.
            Be specific, use emojis, and mention concrete topics.
            Do NOT use markdown. Write in plain text only.
        """.trimIndent()

        return geminiClient.generateContent(prompt)
            ?: "📡 Real-time trend analysis unavailable. Check your connection and try again."
    }

    private fun buildAnalysisPrompt(
        trendTitle: String,
        category: String,
        headlines: List<String>,
        descriptions: List<String>,
        trendScore: Int,
        growthInfo: String
    ): String {
        val newsContext = buildString {
            if (headlines.isNotEmpty()) {
                appendLine("Related news headlines:")
                headlines.take(5).forEach { appendLine("  - $it") }
            }
            if (descriptions.isNotEmpty()) {
                appendLine("Article descriptions:")
                descriptions.take(3).forEach { appendLine("  - $it") }
            }
        }

        return """
            You are Trendora AI, an advanced trend intelligence system powered by Gemini.
            Analyze the following trend and return ONLY a valid JSON object (no markdown, no backticks, no extra text).

            Trend: "$trendTitle"
            Category: $category
            Trend Score: $trendScore/100
            Growth: $growthInfo
            $newsContext

            Return this exact JSON structure:
            {
              "trendSummary": "2-3 sentence summary of what this trend is about",
              "whyTrending": "1-2 sentences explaining why this is trending now",
              "sentiment": "Positive" or "Negative" or "Neutral" or "Mixed",
              "sentimentScore": 75,
              "growthPrediction": "Rising" or "Stable" or "Declining",
              "predictedGrowth": 45,
              "viralProbability": 80,
              "aiConfidence": 85,
              "relatedTopics": ["Topic1", "Topic2", "Topic3", "Topic4"],
              "keyInsights": ["Insight 1", "Insight 2", "Insight 3"],
              "riskFactors": ["Risk 1", "Risk 2"]
            }

            Rules:
            - sentimentScore: 0-100 (0=very negative, 100=very positive)
            - predictedGrowth: 0-100 (growth percentage estimate)
            - viralProbability: 0-100 (probability this becomes viral)
            - aiConfidence: 0-100 (your confidence in this analysis)
            - relatedTopics: 3-5 related trending topics
            - keyInsights: 2-4 key takeaways
            - riskFactors: 1-3 potential risks or concerns
            - Return ONLY the JSON object. No markdown fences, no explanation.
        """.trimIndent()
    }

    /**
     * Safely parse the Gemini response into a [TrendAnalysisResult].
     * Attempts JSON parsing first. Falls back to tag-based extraction.
     * Returns a valid result with safe defaults in every case.
     */
    private fun parseAnalysisResponse(rawResponse: String, trendTitle: String): TrendAnalysisResult {
        // Try JSON parsing first
        val jsonResult = tryParseJson(rawResponse)
        if (jsonResult != null) {
            return jsonResult
        }

        // Try tag-based extraction as fallback
        val tagResult = tryParseTags(rawResponse)
        if (tagResult != null) {
            return tagResult
        }

        // Complete fallback — use the raw response as summary
        return TrendAnalysisResult(
            trendSummary = rawResponse.take(300).ifBlank { "$trendTitle: AI analysis generated." },
            whyTrending = "AI analysis generated for $trendTitle",
            keyInsights = listOf(rawResponse.take(200).ifBlank { "No detailed insights from raw response." })
        )
    }

    private fun tryParseJson(rawResponse: String): TrendAnalysisResult? {
        return try {
            // Strip markdown code fences if present
            val cleaned = rawResponse
                .replace(Regex("```json\\s*"), "")
                .replace(Regex("```\\s*"), "")
                .trim()

            // Find the JSON object in the response
            val jsonStart = cleaned.indexOf('{')
            val jsonEnd = cleaned.lastIndexOf('}')
            if (jsonStart == -1 || jsonEnd == -1 || jsonEnd <= jsonStart) return null

            val json = JSONObject(cleaned.substring(jsonStart, jsonEnd + 1))

            TrendAnalysisResult(
                trendSummary = json.optString("trendSummary", "").ifBlank { "Analysis generated." },
                whyTrending = json.optString("whyTrending", "").ifBlank { "Trending due to increased media coverage." },
                sentiment = json.optString("sentiment", "Neutral").let { s ->
                    when {
                        s.contains("Positive", ignoreCase = true) -> "Positive"
                        s.contains("Negative", ignoreCase = true) -> "Negative"
                        s.contains("Mixed", ignoreCase = true) -> "Mixed"
                        else -> "Neutral"
                    }
                },
                sentimentScore = json.optInt("sentimentScore", 50).coerceIn(0, 100),
                growthPrediction = json.optString("growthPrediction", "Stable").let { s ->
                    when {
                        s.contains("Rising", ignoreCase = true) -> "Rising"
                        s.contains("Declining", ignoreCase = true) -> "Declining"
                        else -> "Stable"
                    }
                },
                predictedGrowth = json.optInt("predictedGrowth", 0).coerceIn(0, 100),
                viralProbability = json.optInt("viralProbability", 0).coerceIn(0, 100),
                aiConfidence = json.optInt("aiConfidence", 0).coerceIn(0, 100),
                relatedTopics = parseJsonStringList(json, "relatedTopics"),
                keyInsights = parseJsonStringList(json, "keyInsights"),
                riskFactors = parseJsonStringList(json, "riskFactors"),
                analyzedAt = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parseJsonStringList(json: JSONObject, key: String): List<String> {
        return try {
            val array = json.optJSONArray(key) ?: return emptyList()
            (0 until array.length()).mapNotNull { array.optString(it) }.filter { it.isNotBlank() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun tryParseTags(text: String): TrendAnalysisResult? {
        val summary = extractTag(text, "SUMMARY:", "WHY_TRENDING:")
            ?: extractTag(text, "trendSummary:", "whyTrending:")
            ?: return null

        return TrendAnalysisResult(
            trendSummary = summary,
            whyTrending = extractTag(text, "WHY_TRENDING:", "SENTIMENT") ?: "Trending topic.",
            sentiment = extractTag(text, "SENTIMENT:", "sentimentScore")?.let { s ->
                when {
                    s.contains("Positive", ignoreCase = true) -> "Positive"
                    s.contains("Negative", ignoreCase = true) -> "Negative"
                    s.contains("Mixed", ignoreCase = true) -> "Mixed"
                    else -> "Neutral"
                }
            } ?: "Neutral",
            sentimentScore = extractTag(text, "sentimentScore:", "growthPrediction")
                ?.replace(Regex("[^0-9]"), "")?.toIntOrNull()?.coerceIn(0, 100) ?: 50,
            growthPrediction = extractTag(text, "growthPrediction:", "predictedGrowth")?.let { s ->
                when {
                    s.contains("Rising", ignoreCase = true) -> "Rising"
                    s.contains("Declining", ignoreCase = true) -> "Declining"
                    else -> "Stable"
                }
            } ?: "Stable",
            predictedGrowth = extractTag(text, "predictedGrowth:", "viralProbability")
                ?.replace(Regex("[^0-9]"), "")?.toIntOrNull()?.coerceIn(0, 100) ?: 0,
            viralProbability = extractTag(text, "viralProbability:", "aiConfidence")
                ?.replace(Regex("[^0-9]"), "")?.toIntOrNull()?.coerceIn(0, 100) ?: 0,
            aiConfidence = extractTag(text, "aiConfidence:", "relatedTopics")
                ?.replace(Regex("[^0-9]"), "")?.toIntOrNull()?.coerceIn(0, 100) ?: 0,
            relatedTopics = extractList(text, "relatedTopics:", "keyInsights"),
            keyInsights = extractList(text, "keyInsights:", "riskFactors"),
            riskFactors = extractList(text, "riskFactors:", null),
            analyzedAt = System.currentTimeMillis()
        )
    }

    private fun extractTag(text: String, startTag: String, endTag: String?): String? {
        val startIndex = text.indexOf(startTag)
        if (startIndex == -1) return null
        val contentStart = startIndex + startTag.length
        val endIndex = if (endTag != null) {
            val e = text.indexOf(endTag, contentStart)
            if (e == -1) text.length else e
        } else {
            text.indexOf("\n", contentStart).let { if (it == -1) text.length else it }
        }
        return text.substring(contentStart, endIndex).trim().removeSurrounding("\"")
    }

    private fun extractList(text: String, startTag: String, endTag: String?): List<String> {
        val content = extractTag(text, startTag, endTag) ?: return emptyList()
        // Try JSON array format: ["a", "b", "c"]
        return try {
            val cleaned = content.trim()
            if (cleaned.startsWith("[")) {
                val array = JSONArray(cleaned)
                (0 until array.length()).map { array.getString(it) }.filter { it.isNotBlank() }
            } else {
                // Comma-separated fallback
                content.split(",").map { it.trim().removeSurrounding("\"") }.filter { it.isNotBlank() }
            }
        } catch (e: Exception) {
            content.split(",").map { it.trim().removeSurrounding("\"") }.filter { it.isNotBlank() }
        }
    }
}
