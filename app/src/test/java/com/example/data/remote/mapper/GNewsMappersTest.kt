package com.example.data.remote.mapper

import com.example.data.remote.dto.GNewsArticleDto
import com.example.data.remote.dto.GNewsSourceDto
import com.example.domain.model.Country
import com.example.domain.model.TrendCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GNewsMappersTest {

    @Test
    fun `maps gnews category tokens to trendora categories`() {
        assertEquals(TrendCategory.TECH, GNewsMappers.toTrendCategory("technology"))
        assertEquals(TrendCategory.SPORTS, GNewsMappers.toTrendCategory("sports"))
        assertEquals(TrendCategory.BUSINESS, GNewsMappers.toTrendCategory("business"))
        assertEquals(TrendCategory.ENTERTAINMENT, GNewsMappers.toTrendCategory("entertainment"))
        assertEquals(TrendCategory.WORLD, GNewsMappers.toTrendCategory("world"))
        assertEquals(TrendCategory.SCIENCE, GNewsMappers.toTrendCategory("science"))
        assertEquals(TrendCategory.HEALTH, GNewsMappers.toTrendCategory("health"))
        assertEquals(TrendCategory.ALL, GNewsMappers.toTrendCategory(null))
        assertEquals(TrendCategory.ALL, GNewsMappers.toTrendCategory("unknown"))
    }

    @Test
    fun `maps trendora categories to gnews category tokens`() {
        assertEquals("technology", GNewsMappers.toGNewsCategory(TrendCategory.TECH))
        assertEquals("technology", GNewsMappers.toGNewsCategory(TrendCategory.AI))
        assertEquals("science", GNewsMappers.toGNewsCategory(TrendCategory.SCIENCE))
        assertEquals("health", GNewsMappers.toGNewsCategory(TrendCategory.HEALTH))
        assertEquals(null, GNewsMappers.toGNewsCategory(TrendCategory.ALL))
    }

    @Test
    fun `maps trendora countries to gnews iso codes`() {
        assertEquals("in", GNewsMappers.toGNewsCountry(Country.INDIA))
        assertEquals("us", GNewsMappers.toGNewsCountry(Country.USA))
        assertEquals("gb", GNewsMappers.toGNewsCountry(Country.UK))
        assertEquals(null, GNewsMappers.toGNewsCountry(Country.GLOBAL))
    }

    @Test
    fun `parses iso8601 timestamps`() {
        val parsed = GNewsMappers.parseDate("2024-05-01T12:00:00Z")
        assertTrue(parsed != null)
        assertTrue(parsed!! > 0)
    }

    @Test
    fun `maps a dto to breaking news without leaking dto fields`() {
        val dto = GNewsArticleDto(
            title = "Tech breakthrough",
            description = "A new discovery.",
            url = "https://example.com/a",
            image = "https://example.com/a.png",
            publishedAt = "2024-05-01T12:00:00Z",
            source = GNewsSourceDto(name = "TechCrunch", url = "https://techcrunch.com")
        )
        val item = GNewsMappers.toBreakingNews(dto, TrendCategory.TECH)

        assertEquals("Tech breakthrough", item.headline)
        assertEquals("TechCrunch", item.source)
        assertEquals(TrendCategory.TECH, item.category)
        assertEquals("https://example.com/a.png", item.imageUrl)
        assertEquals("A new discovery.", item.summary)
        assertTrue(item.timeAgo.isNotBlank())
    }

    @Test
    fun `derives a stable id from url`() {
        val a = GNewsArticleDto(url = "https://example.com/1")
        val b = GNewsArticleDto(url = "https://example.com/1")
        assertEquals(
            GNewsMappers.toBreakingNews(a, TrendCategory.ALL).id,
            GNewsMappers.toBreakingNews(b, TrendCategory.ALL).id
        )
    }
}
