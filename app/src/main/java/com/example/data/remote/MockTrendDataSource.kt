package com.example.data.remote

import com.example.data.remote.MockTrendsDataSource
import com.example.domain.model.BreakingNewsItem
import com.example.domain.model.Country
import com.example.domain.model.TrendCategory

/**
 * Returns bundled mock news. This is the guaranteed fallback so the app is
 * never left on a broken empty screen (first launch, no key, or API down).
 *
 * The mock data is NEVER removed — it is preserved and used as the last resort.
 */
class MockTrendDataSource : TrendDataSource {

    private val allNews: List<BreakingNewsItem> = MockTrendsDataSource.getBreakingNews()

    override suspend fun fetchNews(
        category: TrendCategory,
        country: Country,
        query: String,
        max: Int
    ): DataSourceResult<List<BreakingNewsItem>> {
        // Mock breaking-news items are global (no country tag), so country is
        // only used to decide category relevance, not to filter exact regions.
        val filtered = allNews
            .filter { category == TrendCategory.ALL || it.category == category }
            .filter { query.isBlank() || it.headline.contains(query, ignoreCase = true) }
            .take(max)

        if (filtered.isEmpty()) {
            // Never hand back an empty screen: fall back to the full mock set.
            return DataSourceResult.Success(allNews.take(max))
        }
        return DataSourceResult.Success(filtered)
    }
}
