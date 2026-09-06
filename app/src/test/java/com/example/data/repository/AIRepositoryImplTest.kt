package com.example.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.core.network.GeminiApiClient
import com.example.core.network.NetworkMonitor
import com.example.data.local.AiAnalysisCacheEntity
import com.example.data.local.AppDatabase
import com.example.domain.model.TrendAnalysisResult
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AIRepositoryImplTest {

    private lateinit var db: AppDatabase
    private lateinit var context: Context

    private class FakeNetworkMonitor(private val connected: Boolean) :
        NetworkMonitor(ApplicationProvider.getApplicationContext()) {
        override fun isCurrentlyConnected(): Boolean = connected
    }

    private class FakeGeminiClient(
        private var response: String? = null,
        override val isConfigured: Boolean = true
    ) : GeminiApiClient() {
        var callCount = 0
        override suspend fun generateContent(prompt: String): String? {
            callCount++
            return response
        }
    }

    private fun repo(
        gemini: GeminiApiClient,
        connected: Boolean
    ) = AIRepositoryImpl(gemini, db, FakeNetworkMonitor(connected))

    private val sampleJson = """
        {
          "trendSummary": "AI market expanding rapidly.",
          "whyTrending": "New product launches.",
          "sentiment": "Positive",
          "sentimentScore": 78,
          "growthPrediction": "Rising",
          "predictedGrowth": 55,
          "viralProbability": 82,
          "aiConfidence": 88,
          "relatedTopics": ["GenAI", "Robotics"],
          "keyInsights": ["Strong growth", "More investment"],
          "riskFactors": ["Regulation"]
        }
    """.trimIndent()

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `missing key returns not-configured result`() = runBlocking {
        val gemini = FakeGeminiClient(isConfigured = false)
        val result = repo(gemini, connected = true)
            .analyzeTrendStructured("t1", "AI", "Technology")

        assertEquals("AI analysis requires a Gemini API key.", result.trendSummary)
    }

    @Test
    fun `offline with no cache returns offline message`() = runBlocking {
        val gemini = FakeGeminiClient(response = sampleJson, isConfigured = true)
        val result = repo(gemini, connected = false)
            .analyzeTrendStructured("t1", "AI", "Technology")

        assertEquals("No cached AI analysis available.", result.trendSummary)
        assertTrue(result.whyTrending.contains("Connect to the internet"))
    }

    @Test
    fun `offline returns cached analysis when available`() = runBlocking {
        // Online fetch + cache
        val gemini = FakeGeminiClient(response = sampleJson, isConfigured = true)
        val repository = repo(gemini, connected = true)
        val fresh = repository.analyzeTrendStructured("t1", "AI", "Technology")
        assertEquals("AI market expanding rapidly.", fresh.trendSummary)

        // Now offline — should return cached
        val offlineRepo = repo(FakeGeminiClient(isConfigured = true), connected = false)
        val cached = offlineRepo.analyzeTrendStructured("t1", "AI", "Technology")
        assertEquals("AI market expanding rapidly.", cached.trendSummary)
    }

    @Test
    fun `fresh cache avoids duplicate api calls`() = runBlocking {
        val gemini = FakeGeminiClient(response = sampleJson, isConfigured = true)
        val repository = repo(gemini, connected = true)

        repository.analyzeTrendStructured("t1", "AI", "Technology")
        val firstCalls = gemini.callCount
        assertTrue(firstCalls >= 1)

        // Second call should hit cache, not the API
        repository.analyzeTrendStructured("t1", "AI", "Technology")
        assertEquals(firstCalls, gemini.callCount)
    }

    @Test
    fun `force refresh bypasses cache and calls api again`() = runBlocking {
        val gemini = FakeGeminiClient(response = sampleJson, isConfigured = true)
        val repository = repo(gemini, connected = true)

        repository.analyzeTrendStructured("t1", "AI", "Technology")
        val firstCalls = gemini.callCount

        repository.analyzeTrendStructured("t1", "AI", "Technology", forceRefresh = true)
        assertTrue(gemini.callCount > firstCalls)
    }

    @Test
    fun `getCachedAnalysis returns null when none cached`() = runBlocking {
        val gemini = FakeGeminiClient(response = sampleJson, isConfigured = true)
        val result = repo(gemini, connected = true).getCachedAnalysis("missing")
        assertNull(result)
    }

    @Test
    fun `getCachedAnalysis returns stored analysis`() = runBlocking {
        val gemini = FakeGeminiClient(response = sampleJson, isConfigured = true)
        val repository = repo(gemini, connected = true)
        repository.analyzeTrendStructured("t1", "AI", "Technology")

        val cached = repository.getCachedAnalysis("t1")
        assertNotNull(cached)
        assertEquals("AI market expanding rapidly.", cached?.trendSummary)
    }

    @Test
    fun `stale cache older than 30 min is invalidated and triggers a fresh call`() = runBlocking {
        // Seed a stale cache entry written 31 minutes ago.
        db.trendDao().insertCachedAnalysis(
            AiAnalysisCacheEntity(
                trendId = "t1",
                analysisJson = """{"trendSummary":"Stale cached summary.","analyzedAt":0}""",
                trendTitle = "AI",
                category = "Technology",
                analyzedAt = System.currentTimeMillis() - 31 * 60 * 1000
            )
        )

        val gemini = FakeGeminiClient(response = sampleJson, isConfigured = true)
        val repository = repo(gemini, connected = true)

        val result = repository.analyzeTrendStructured("t1", "AI", "Technology")

        // Stale cache was ignored — a live API call was made and the fresh result returned.
        assertEquals("AI market expanding rapidly.", result.trendSummary)
        assertTrue(gemini.callCount >= 1)
    }

    @Test
    fun `fresh cache within 30 min is returned without a new api call`() = runBlocking {
        db.trendDao().insertCachedAnalysis(
            AiAnalysisCacheEntity(
                trendId = "t1",
                // analyzedAt inside the JSON must be recent, because isRecent() reads it from the JSON.
                analysisJson = """{"trendSummary":"Fresh cached summary.","analyzedAt":${System.currentTimeMillis() - 10 * 60 * 1000}}""",
                trendTitle = "AI",
                category = "Technology",
                analyzedAt = System.currentTimeMillis() - 10 * 60 * 1000
            )
        )

        val gemini = FakeGeminiClient(response = sampleJson, isConfigured = true)
        val repository = repo(gemini, connected = true)

        val result = repository.analyzeTrendStructured("t1", "AI", "Technology")

        // Fresh cache hit — no duplicate API call, cached summary returned.
        assertEquals("Fresh cached summary.", result.trendSummary)
        assertEquals(0, gemini.callCount)
    }
}
