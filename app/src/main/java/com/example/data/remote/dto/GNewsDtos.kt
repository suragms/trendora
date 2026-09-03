package com.example.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTOs for the GNews API (https://gnews.io/docs).
 *
 * These are transport models only and must never leak into the UI layer.
 * Convert them to domain models with [com.example.data.remote.mapper.GNewsMappers].
 */

@JsonClass(generateAdapter = true)
data class GNewsResponseDto(
    @Json(name = "totalArticles") val totalArticles: Int? = null,
    @Json(name = "articles") val articles: List<GNewsArticleDto>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class GNewsArticleDto(
    @Json(name = "title") val title: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "content") val content: String? = null,
    @Json(name = "url") val url: String? = null,
    @Json(name = "image") val image: String? = null,
    @Json(name = "publishedAt") val publishedAt: String? = null,
    @Json(name = "source") val source: GNewsSourceDto? = null
)

@JsonClass(generateAdapter = true)
data class GNewsSourceDto(
    @Json(name = "name") val name: String? = null,
    @Json(name = "url") val url: String? = null
)
