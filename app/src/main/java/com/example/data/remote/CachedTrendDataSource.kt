package com.example.data.remote

import com.example.data.local.AppDatabase
import com.example.data.local.NewsCacheEntity
import com.example.domain.model.BreakingNewsItem
import com.example.domain.model.Country
import com.example.domain.model.TrendCategory

/**
 * Reads news from the local Room cache. Used when offline or when the remote
 * call fails, so the app can still show recently-fetched content.
 */
class CachedTrendDataSource(
    private val database: AppDatabase
) : TrendDataSource {

    override suspend fun fetchNews(
        category: TrendCategory,
        country: Country,
        query: String,
        max: Int
    ): DataSourceResult<List<BreakingNewsItem>> {
        val cached = database.trendDao().getAllCachedNews()
        if (cached.isEmpty()) return DataSourceResult.Empty

        val filtered = cached
            .filter { category == TrendCategory.ALL || it.category == category.name }
            .filter { country == Country.GLOBAL || it.country == country.name }
            .filter { query.isBlank() || it.headline.contains(query, ignoreCase = true) }
            .take(max)

        if (filtered.isEmpty()) return DataSourceResult.Empty

        return DataSourceResult.Success(filtered.map { it.toBreakingNews() }, fromCache = true)
    }

    /** Persists a fresh remote batch, replacing the previous snapshot. */
    suspend fun save(items: List<BreakingNewsItem>, country: Country) {
        val dao = database.trendDao()
        dao.clearCachedNews()
        dao.insertCachedNews(items.map { it.toCacheEntity(country) })
    }

    private fun BreakingNewsItem.toCacheEntity(country: Country) = NewsCacheEntity(
        id = id,
        headline = headline,
        source = source,
        timeAgo = timeAgo,
        imageUrl = imageUrl,
        category = category.name,
        trendingScore = trendingScore,
        readTimeMinutes = readTimeMinutes,
        summary = summary,
        country = country.name
    )

    private fun NewsCacheEntity.toBreakingNews() = BreakingNewsItem(
        id = id,
        headline = headline,
        source = source,
        timeAgo = timeAgo,
        imageUrl = imageUrl,
        category = try {
            TrendCategory.valueOf(category)
        } catch (e: Exception) {
            TrendCategory.ALL
        },
        trendingScore = trendingScore,
        readTimeMinutes = readTimeMinutes,
        summary = summary
    )
}
