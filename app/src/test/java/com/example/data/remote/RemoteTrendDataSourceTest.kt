package com.example.data.remote

import androidx.test.core.app.ApplicationProvider
import com.example.core.network.GNewsApiService
import com.example.core.network.GNewsErrorMapper
import com.example.core.network.NetworkMonitor
import com.example.data.remote.dto.GNewsArticleDto
import com.example.data.remote.dto.GNewsResponseDto
import com.example.domain.model.Country
import com.example.domain.model.TimeFilter
import com.example.domain.model.TrendCategory
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.HttpException
import retrofit2.Response

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RemoteTrendDataSourceTest {

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
                description = "desc $i",
                url = "https://example.com/$i",
                source = com.example.data.remote.dto.GNewsSourceDto(name = "Source $i", url = null)
            )
        }
    )

    private fun isoUtc(epochMillis: Long): String =
        java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }.format(java.util.Date(epochMillis))

    @Test
    fun `time filter drops stale articles when timestamp is available`() = runBlocking {
        val now = System.currentTimeMillis()
        val service = FakeGNewsApiService(
            response = GNewsResponseDto(
                totalArticles = 2,
                articles = listOf(
                    GNewsArticleDto(
                        title = "Fresh", url = "https://example.com/fresh",
                        publishedAt = isoUtc(now - 10 * 60 * 1000L) // 10 min ago
                    ),
                    GNewsArticleDto(
                        title = "Stale", url = "https://example.com/stale",
                        publishedAt = isoUtc(now - 5 * 24 * 60 * 60 * 1000L) // 5 days ago
                    )
                )
            )
        )
        val source = RemoteTrendDataSource(service, FakeNetworkMonitor(true), isConfigured = true)

        val result = source.fetchNews(TrendCategory.TECH, Country.USA, timeFilter = TimeFilter.LAST_HOUR)
        assertTrue(result is DataSourceResult.Success)
        assertEquals(listOf("Fresh"), (result as DataSourceResult.Success).data.map { it.headline })
    }

    @Test
    fun `time filter keeps articles with unknown timestamps`() = runBlocking {
        val service = FakeGNewsApiService(response = responseWith("Alpha", "Beta"))
        val source = RemoteTrendDataSource(service, FakeNetworkMonitor(true), isConfigured = true)

        val result = source.fetchNews(TrendCategory.TECH, Country.USA, timeFilter = TimeFilter.LAST_HOUR)
        assertTrue(result is DataSourceResult.Success)
        assertEquals(2, (result as DataSourceResult.Success).data.size)
    }

    @Test
    fun `returns success with mapped articles`() = runBlocking {
        val service = FakeGNewsApiService(response = responseWith("Alpha", "Beta"))
        val source = RemoteTrendDataSource(
            apiService = service,
            networkMonitor = FakeNetworkMonitor(true),
            isConfigured = true
        )

        val result = source.fetchNews(TrendCategory.TECH, Country.USA)

        assertTrue(result is DataSourceResult.Success)
        val data = (result as DataSourceResult.Success).data
        assertEquals(2, data.size)
        assertEquals("Alpha", data[0].headline)
    }

    @Test
    fun `returns empty when api returns no articles`() = runBlocking {
        val service = FakeGNewsApiService(response = GNewsResponseDto(totalArticles = 0, articles = emptyList()))
        val source = RemoteTrendDataSource(
            apiService = service,
            networkMonitor = FakeNetworkMonitor(true),
            isConfigured = true
        )
        assertEquals(DataSourceResult.Empty, source.fetchNews(TrendCategory.ALL, Country.GLOBAL))
    }

    @Test
    fun `maps rate limit to friendly message`() = runBlocking {
        val body = "".toResponseBody("application/json".toMediaType())
        val service = FakeGNewsApiService(error = HttpException(Response.error<Any>(429, body)))
        val source = RemoteTrendDataSource(
            apiService = service,
            networkMonitor = FakeNetworkMonitor(true),
            isConfigured = true
        )
        val result = source.fetchNews(TrendCategory.ALL, Country.GLOBAL) as DataSourceResult.Error
        assertEquals("API limit reached. Please try again later.", result.userMessage)
    }

    @Test
    fun `returns missing key message when not configured`() = runBlocking {
        val source = RemoteTrendDataSource(
            apiService = FakeGNewsApiService(),
            networkMonitor = FakeNetworkMonitor(true),
            isConfigured = false
        )
        val result = source.fetchNews(TrendCategory.ALL, Country.GLOBAL) as DataSourceResult.Error
        assertEquals(GNewsErrorMapper.MISSING_KEY_MESSAGE, result.userMessage)
    }

    @Test
    fun `returns no internet message when offline`() = runBlocking {
        val source = RemoteTrendDataSource(
            apiService = FakeGNewsApiService(),
            networkMonitor = FakeNetworkMonitor(false),
            isConfigured = true
        )
        val result = source.fetchNews(TrendCategory.ALL, Country.GLOBAL) as DataSourceResult.Error
        assertEquals(GNewsErrorMapper.NO_INTERNET_MESSAGE, result.userMessage)
    }
}
