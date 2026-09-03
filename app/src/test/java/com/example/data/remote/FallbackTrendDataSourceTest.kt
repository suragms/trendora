package com.example.data.remote

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.core.network.GNewsApiService
import com.example.core.network.NetworkMonitor
import com.example.data.local.AppDatabase
import com.example.data.remote.dto.GNewsArticleDto
import com.example.data.remote.dto.GNewsResponseDto
import com.example.domain.model.BreakingNewsItem
import com.example.domain.model.Country
import com.example.domain.model.TrendCategory
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.HttpException
import retrofit2.Response

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FallbackTrendDataSourceTest {

    private lateinit var db: AppDatabase
    private lateinit var context: Context

    private class FakeNetworkMonitor(private val connected: Boolean) :
        NetworkMonitor(ApplicationProvider.getApplicationContext()) {
        override fun isCurrentlyConnected(): Boolean = connected
    }

    private class FakeGNewsApiService(
        var response: GNewsResponseDto = GNewsResponseDto(),
        var error: Exception? = null
    ) : GNewsApiService {
        override suspend fun getTopHeadlines(
            category: String?, country: String?, lang: String, max: Int, apiKey: String
        ): GNewsResponseDto {
            error?.let { throw it }
            return response
        }

        override suspend fun search(
            query: String, country: String?, lang: String, max: Int, apiKey: String
        ): GNewsResponseDto {
            error?.let { throw it }
            return response
        }
    }

    private fun responseWith(vararg titles: String) = GNewsResponseDto(
        totalArticles = titles.size,
        articles = titles.mapIndexed { i, t ->
            GNewsArticleDto(
                title = t,
                url = "https://example.com/$i",
                source = com.example.data.remote.dto.GNewsSourceDto(name = "Src $i", url = null)
            )
        }
    )

    private fun build(
        connected: Boolean,
        service: GNewsApiService
    ): Triple<FallbackTrendDataSource, CachedTrendDataSource, RemoteTrendDataSource> {
        val remote = RemoteTrendDataSource(service, FakeNetworkMonitor(connected), isConfigured = true)
        val cached = CachedTrendDataSource(db)
        val mock = MockTrendDataSource()
        val fallback = FallbackTrendDataSource(remote, cached, mock, FakeNetworkMonitor(connected))
        return Triple(fallback, cached, remote)
    }

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
    fun `remote success is returned and cache is populated`() = runBlocking {
        val service = FakeGNewsApiService(response = responseWith("Alpha", "Beta"))
        val (fallback, cached, _) = build(connected = true, service = service)

        val result = fallback.fetchNews(TrendCategory.TECH, Country.USA)

        assertTrue(result is DataSourceResult.Success)
        val data = (result as DataSourceResult.Success).data
        assertEquals(listOf("Alpha", "Beta"), data.map { it.headline })

        // cache should now hold the batch for offline use
        val cachedResult = cached.fetchNews(TrendCategory.TECH, Country.USA)
        assertTrue(cachedResult is DataSourceResult.Success)
        assertEquals(2, (cachedResult as DataSourceResult.Success).data.size)
    }

    @Test
    fun `offline falls back to cached data`() = runBlocking {
        val service = FakeGNewsApiService(response = responseWith("Alpha", "Beta"))
        val (_, cached, _) = build(connected = true, service = service)
        // seed cache
        cached.save(
            listOf(
                BreakingNewsItem("1", "Cached Headline", "BBC", "1h ago", "", TrendCategory.TECH, 70, 3, "summary")
            ),
            Country.USA
        )

        val (fallback2, _, _) = build(connected = false, service = FakeGNewsApiService())
        val result = fallback2.fetchNews(TrendCategory.TECH, Country.USA)

        assertTrue(result is DataSourceResult.Success)
        val data = (result as DataSourceResult.Success).data
        assertEquals("Cached Headline", data[0].headline)
        assertTrue(result.fromCache)
    }

    @Test
    fun `offline with no cache falls back to mock - never broken empty screen`() = runBlocking {
        val (fallback, _, _) = build(connected = false, service = FakeGNewsApiService())
        val result = fallback.fetchNews(TrendCategory.ALL, Country.GLOBAL)
        assertTrue(result is DataSourceResult.Success)
        assertTrue((result as DataSourceResult.Success).data.isNotEmpty())
    }

    @Test
    fun `remote failure with no cache falls back to mock - never empty`() = runBlocking {
        val body = "".toResponseBody("application/json".toMediaType())
        val service = FakeGNewsApiService(error = HttpException(Response.error<Any>(429, body)))
        val (fallback, _, _) = build(connected = true, service = service)

        val result = fallback.fetchNews(TrendCategory.ALL, Country.GLOBAL)
        assertTrue(result is DataSourceResult.Success)
        assertTrue((result as DataSourceResult.Success).data.isNotEmpty())
        assertTrue(result.fromMock)
    }

    @Test
    fun `remote empty response with no cache falls back to mock`() = runBlocking {
        val service = FakeGNewsApiService(response = GNewsResponseDto(totalArticles = 0, articles = emptyList()))
        val (fallback, _, _) = build(connected = true, service = service)

        val result = fallback.fetchNews(TrendCategory.ALL, Country.GLOBAL)
        assertTrue(result is DataSourceResult.Success)
        assertTrue((result as DataSourceResult.Success).data.isNotEmpty())
        assertTrue(result.fromMock)
    }

    @Test
    fun `remote failure falls back to cache`() = runBlocking {
        val (_, cached, _) = build(connected = true, service = FakeGNewsApiService())
        cached.save(
            listOf(
                BreakingNewsItem("1", "Cached", "BBC", "1h ago", "", TrendCategory.SPORTS, 70, 3, "summary")
            ),
            Country.GLOBAL
        )

        val body = "".toResponseBody("application/json".toMediaType())
        val service = FakeGNewsApiService(error = HttpException(Response.error<Any>(429, body)))
        val (fallback, _, _) = build(connected = true, service = service)

        val result = fallback.fetchNews(TrendCategory.SPORTS, Country.GLOBAL)
        assertTrue(result is DataSourceResult.Success)
        assertEquals("Cached", (result as DataSourceResult.Success).data[0].headline)
    }
}
