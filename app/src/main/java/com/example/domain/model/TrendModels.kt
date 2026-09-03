package com.example.domain.model

data class SentimentBreakdown(
    val positivePercent: Int,
    val neutralPercent: Int,
    val negativePercent: Int,
    val summary: String
)

data class DiscussionItem(
    val id: String,
    val author: String,
    val handle: String,
    val platform: String, // "X / Twitter", "Reddit", "Threads", "YouTube", "TechNews"
    val content: String,
    val timeAgo: String,
    val likesCount: String,
    val commentsCount: String,
    val sentiment: String = "Positive"
)

data class BreakingNewsItem(
    val id: String,
    val headline: String,
    val source: String,
    val timeAgo: String,
    val imageUrl: String,
    val category: TrendCategory,
    val trendingScore: Int,
    val readTimeMinutes: Int = 3,
    val summary: String = ""
)

data class TrendHistoryPoint(
    val label: String,
    val value: Float
)

data class AIAnalysis(
    val summary: String,
    val whyTrending: String,
    val sentiment: SentimentBreakdown,
    val expectedGrowthPercent: Int,
    val viralProbabilityPercent: Int,
    val aiConfidencePercent: Int,
    val growthTrajectory: String, // "Rapid Exponential", "Sustained Peak", "Steady Climb"
    val relatedTopics: List<String>,
    val timeline: List<String>
)

data class TrendItem(
    val id: String,
    val title: String,
    val category: TrendCategory,
    val score: TrendScore,
    val growthPercentage: Int, // e.g. +145%
    val discussionsCount: String, // e.g. "+125K discussions"
    val searchVolume: String, // e.g. "850K searches"
    val timeAgo: String,
    val country: Country,
    val chartData: List<Float>, // 7-10 normalized points for sparkline/graph
    val historyToday: List<TrendHistoryPoint>,
    val history7Days: List<TrendHistoryPoint>,
    val history30Days: List<TrendHistoryPoint>,
    val sourceIcons: List<String>, // e.g. ["X", "Reddit", "Google", "YouTube"]
    val isTrendingUp: Boolean = true,
    val isSaved: Boolean = false,
    val isBreaking: Boolean = false,
    val aiAnalysis: AIAnalysis,
    val discussions: List<DiscussionItem> = emptyList(),
    val relatedNews: List<BreakingNewsItem> = emptyList()
)

data class TrendAnalytics(
    val totalActiveTrends: Int,
    val upwardTrendingCount: Int,
    val downwardTrendingCount: Int,
    val stableTrendingCount: Int,
    val averageGrowthRate: String,
    val totalMentions24h: String,
    val topTrendingCategory: String
)

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val timeAgo: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val trendId: String? = null
)

enum class NotificationType {
    BREAKING,
    AI_ALERT,
    TOPIC_GROWTH,
    DAILY_BRIEF,
    GENERAL
}

data class UserStats(
    val trendsViewed: Int = 142,
    val savedItems: Int = 18,
    val topicsFollowed: Int = 9
)
