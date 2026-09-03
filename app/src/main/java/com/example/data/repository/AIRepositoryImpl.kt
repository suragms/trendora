package com.example.data.repository

import com.example.core.network.GeminiApiClient
import com.example.core.network.NetworkMonitor
import com.example.data.local.AppDatabase
import com.example.data.local.AiAnalysisCacheEntity
import com.example.data.remote.AiTrendAnalysisService
import com.example.data.remote.MockTrendsDataSource
import com.example.domain.model.*
import com.example.domain.repository.AIRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Full implementation of [AIRepository] that:
 *
 * 1. Generates structured AI analyses via [AiTrendAnalysisService] (Gemini).
 * 2. Caches results in Room so the app works offline and avoids duplicate API calls.
 * 3. Falls back to rule-based analysis when the API key is missing or the call fails.
 * 4. Provides the AI Trends overview from existing mock data (preserved as-is).
 */
class AIRepositoryImpl(
    private val geminiClient: GeminiApiClient,
    private val database: AppDatabase,
    private val networkMonitor: NetworkMonitor
) : AIRepository {

    private val analysisService = AiTrendAnalysisService(geminiClient)
    private val dao = database.trendDao()

    override val isConfigured: Boolean
        get() = geminiClient.isConfigured

    /** In-memory daily summary to avoid recomposition re-fetches. */
    private val _dailySummary = MutableStateFlow(
        "📡 Real-time trend analysis loading... Configure your Gemini API key for live insights."
    )

    // ---- Existing methods (preserved) ----

    override fun getAITrendsOverview(): Flow<List<TrendItem>> {
        val trends = MockTrendsDataSource.getInitialTrends()
        val aiTrends = trends.filter { it.category == TrendCategory.AI || it.score.totalScore >= 88 }
        return flowOf(aiTrends)
    }

    override suspend fun generateDeepAnalysis(trendTitle: String, category: String): AIAnalysis {
        if (geminiClient.isConfigured) {
            val prompt = """
                You are Trendora AI, an advanced trend intelligence system.
                Provide a concise analysis for the trending topic: "$trendTitle" in category "$category".
                Format your response strictly as:
                SUMMARY: <2-3 sentence summary of the trend>
                WHY_TRENDING: <1-2 sentences explaining why it is viral now>
                SENTIMENT_POS: <number 0-100>
                SENTIMENT_NEU: <number 0-100>
                SENTIMENT_NEG: <number 0-100>
                SENTIMENT_SUMMARY: <1 sentence sentiment breakdown>
                EXPECTED_GROWTH: <number 0-100>
                VIRAL_PROBABILITY: <number 0-100>
                AI_CONFIDENCE: <number 0-100>
                TRAJECTORY: <Rapid Exponential | Sustained Peak | Steady Climb>
                RELATED_TOPICS: <comma-separated list of 4-5 topics>
            """.trimIndent()

            val response = geminiClient.generateContent(prompt)
            if (response != null && response.contains("SUMMARY:")) {
                try {
                    val summary = extractTag(response, "SUMMARY:")
                    val whyTrending = extractTag(response, "WHY_TRENDING:")
                    val pos = extractTag(response, "SENTIMENT_POS:").toIntOrNull() ?: 75
                    val neu = extractTag(response, "SENTIMENT_NEU:").toIntOrNull() ?: 18
                    val neg = extractTag(response, "SENTIMENT_NEG:").toIntOrNull() ?: 7
                    val sentimentSummary = extractTag(response, "SENTIMENT_SUMMARY:")
                    val growth = extractTag(response, "EXPECTED_GROWTH:").toIntOrNull() ?: 88
                    val viral = extractTag(response, "VIRAL_PROBABILITY:").toIntOrNull() ?: 84
                    val confidence = extractTag(response, "AI_CONFIDENCE:").toIntOrNull() ?: 92
                    val trajectory = extractTag(response, "TRAJECTORY:")
                    val topics = extractTag(response, "RELATED_TOPICS:").split(",").map { it.trim() }.filter { it.isNotBlank() }

                    return AIAnalysis(
                        summary = summary.ifBlank { "High-velocity momentum detected across global search and social discussions." },
                        whyTrending = whyTrending.ifBlank { "Unprecedented spike in developer and news coverage over the last 24 hours." },
                        sentiment = SentimentBreakdown(pos, neu, neg, sentimentSummary.ifBlank { "Largely positive outlook." }),
                        expectedGrowthPercent = growth,
                        viralProbabilityPercent = viral,
                        aiConfidencePercent = confidence,
                        growthTrajectory = trajectory.ifBlank { "Rapid Exponential" },
                        relatedTopics = if (topics.isNotEmpty()) topics else listOf("Autonomous Systems", "Machine Intelligence", "Next-Gen Compute"),
                        timeline = listOf(
                            "04:00 UTC: Exponential spike in organic search queries",
                            "08:30 UTC: Viral discourse across technical forums and news outlets",
                            "Now: Sustained peak attention across global tech hubs"
                        )
                    )
                } catch (_: Exception) {
                    // Fall through to rule-based
                }
            }
        }

        // Rule-based fallback — deliberately neutral. It must NOT invent
        // specific metrics (discussion counts, exact growth %, timestamps) that
        // were never observed, to avoid presenting fabricated data as real.
        return AIAnalysis(
            summary = "$trendTitle is receiving notable attention in the $category space, based on available coverage. Live metrics require the Gemini API key to be configured.",
            whyTrending = "Increased recent media coverage and public interest in $trendTitle. Configure the Gemini API key for a detailed, data-driven explanation.",
            sentiment = SentimentBreakdown(70, 20, 10, "Estimated from available coverage; not based on live sentiment data."),
            expectedGrowthPercent = 50,
            viralProbabilityPercent = 50,
            aiConfidencePercent = 35,
            growthTrajectory = "Steady Climb",
            relatedTopics = listOf("$category", "Trending Now"),
            timeline = listOf(
                "Coverage for this trend is currently being aggregated.",
                "Connect to the internet with a configured API key for live analysis."
            )
        )
    }

    override suspend fun askAIAssistant(prompt: String, trendContext: String?): String {
        if (geminiClient.isConfigured) {
            val systemPrompt = "You are Trendora AI, an intelligent trend discovery expert. Answer questions about current trends, viral metrics, technology, entertainment, and growth projections concisely in 2-3 sentences with emojis."
            val fullPrompt = if (trendContext != null) {
                "$systemPrompt\nContext: Trend $trendContext\nQuestion: $prompt"
            } else {
                "$systemPrompt\nQuestion: $prompt"
            }
            val response = geminiClient.generateContent(fullPrompt)
            if (!response.isNullOrBlank()) return response
        }

        return "🤖 Trendora AI Insight: I need a configured Gemini API key and an internet connection to give you live analysis. Add your key in local.properties and try again."
    }

    override fun getDailyAISummary(): Flow<String> = _dailySummary

    // ---- Structured AI Analysis (new) ----

    override suspend fun analyzeTrendStructured(
        trendId: String,
        trendTitle: String,
        category: String,
        headlines: List<String>,
        descriptions: List<String>,
        trendScore: Int,
        growthInfo: String,
        forceRefresh: Boolean
    ): TrendAnalysisResult = withContext(Dispatchers.IO) {
        // 1. Check cache first (unless force refresh)
        if (!forceRefresh) {
            val cached = dao.getCachedAnalysis(trendId)
            if (cached != null) {
                val result = jsonToAnalysisResult(cached.analysisJson, trendId)
                // If cache is recent (30 min), use it directly
                if (result.isRecent()) {
                    return@withContext result
                }
                // Offline: use any cached analysis even if stale
                if (!networkMonitor.isCurrentlyConnected()) {
                    return@withContext result
                }
            }
        }

        // 2. Offline with no cache -> helpful offline message
        if (!networkMonitor.isCurrentlyConnected()) {
            return@withContext TrendAnalysisResult.OFFLINE.copy(trendId = trendId)
        }

        // 3. Check API configuration
        if (!analysisService.isConfigured) {
            return@withContext TrendAnalysisResult.NOT_CONFIGURED.copy(trendId = trendId)
        }

        // 4. Try Gemini API
        val result = try {
            analysisService.analyzeTrend(
                trendTitle = trendTitle,
                category = category,
                headlines = headlines,
                descriptions = descriptions,
                trendScore = trendScore,
                growthInfo = growthInfo
            ).copy(trendId = trendId)
        } catch (e: Exception) {
            // API call failed — check if we have an older cache to use
            val olderCache = dao.getCachedAnalysis(trendId)
            if (olderCache != null) {
                jsonToAnalysisResult(olderCache.analysisJson, trendId)
            } else {
                // Complete fallback: rule-based analysis
                ruleBasedAnalysis(trendId, trendTitle, category)
            }
        }

        // 5. Cache the result
        try {
            dao.insertCachedAnalysis(
                AiAnalysisCacheEntity(
                    trendId = trendId,
                    analysisJson = analysisResultToJson(result),
                    trendTitle = trendTitle,
                    category = category,
                    analyzedAt = System.currentTimeMillis()
                )
            )
        } catch (_: Exception) {
            // Cache write failed — not critical
        }

        result
    }

    override suspend fun getCachedAnalysis(trendId: String): TrendAnalysisResult? {
        return try {
            val entity = dao.getCachedAnalysis(trendId) ?: return null
            jsonToAnalysisResult(entity.analysisJson, trendId)
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun refreshDailySummary(): String {
        val summary = analysisService.generateDailySummary()
        _dailySummary.value = summary
        return summary
    }

    // ---- Private helpers ----

    private fun ruleBasedAnalysis(trendId: String, trendTitle: String, category: String): TrendAnalysisResult {
        return TrendAnalysisResult(
            trendId = trendId,
            trendSummary = "$trendTitle is generating significant interest in the $category space with growing media attention and public discussion.",
            whyTrending = "A combination of recent developments, industry announcements, and social media engagement has propelled $trendTitle into the spotlight.",
            sentiment = "Mixed",
            sentimentScore = 65,
            growthPrediction = "Rising",
            predictedGrowth = 45,
            viralProbability = 60,
            aiConfidence = 40,
            relatedTopics = listOf(trendTitle, category, "Trending Now", "Tech News"),
            keyInsights = listOf(
                "Growing public interest detected across multiple channels",
                "Media coverage increasing steadily",
                "Social engagement above average for $category topics"
            ),
            riskFactors = listOf(
                "Analysis based on limited data (no API key configured)",
                "Actual metrics may differ from estimates"
            ),
            analyzedAt = System.currentTimeMillis()
        )
    }

    private fun analysisResultToJson(result: TrendAnalysisResult): String {
        return JSONObject().apply {
            put("trendId", result.trendId)
            put("trendSummary", result.trendSummary)
            put("whyTrending", result.whyTrending)
            put("sentiment", result.sentiment)
            put("sentimentScore", result.sentimentScore)
            put("growthPrediction", result.growthPrediction)
            put("predictedGrowth", result.predictedGrowth)
            put("viralProbability", result.viralProbability)
            put("aiConfidence", result.aiConfidence)
            put("relatedTopics", org.json.JSONArray(result.relatedTopics))
            put("keyInsights", org.json.JSONArray(result.keyInsights))
            put("riskFactors", org.json.JSONArray(result.riskFactors))
            put("analyzedAt", result.analyzedAt)
        }.toString()
    }

    private fun jsonToAnalysisResult(json: String, trendId: String): TrendAnalysisResult {
        return try {
            val obj = JSONObject(json)
            TrendAnalysisResult(
                trendId = trendId,
                trendSummary = obj.optString("trendSummary", ""),
                whyTrending = obj.optString("whyTrending", ""),
                sentiment = obj.optString("sentiment", "Neutral"),
                sentimentScore = obj.optInt("sentimentScore", 50).coerceIn(0, 100),
                growthPrediction = obj.optString("growthPrediction", "Stable"),
                predictedGrowth = obj.optInt("predictedGrowth", 0).coerceIn(0, 100),
                viralProbability = obj.optInt("viralProbability", 0).coerceIn(0, 100),
                aiConfidence = obj.optInt("aiConfidence", 0).coerceIn(0, 100),
                relatedTopics = parseJsonStringList(obj, "relatedTopics"),
                keyInsights = parseJsonStringList(obj, "keyInsights"),
                riskFactors = parseJsonStringList(obj, "riskFactors"),
                analyzedAt = obj.optLong("analyzedAt", System.currentTimeMillis())
            )
        } catch (e: Exception) {
            TrendAnalysisResult(trendId = trendId, trendSummary = "Cached analysis could not be parsed.")
        }
    }

    private fun parseJsonStringList(obj: JSONObject, key: String): List<String> {
        return try {
            val array = obj.optJSONArray(key) ?: return emptyList()
            (0 until array.length()).mapNotNull { array.optString(it) }.filter { it.isNotBlank() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun extractTag(text: String, tag: String): String {
        val startIndex = text.indexOf(tag)
        if (startIndex == -1) return ""
        val contentStart = startIndex + tag.length
        val endIndex = text.indexOf("\n", contentStart).let { if (it == -1) text.length else it }
        return text.substring(contentStart, endIndex).trim()
    }
}
