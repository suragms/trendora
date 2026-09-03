package com.example.domain.model

enum class TrendCategory(val displayName: String, val iconEmoji: String) {
    ALL("All", "🔥"),
    AI("AI", "🤖"),
    TECH("Technology", "💻"),
    ENTERTAINMENT("Entertainment", "🎬"),
    SPORTS("Sports", "⚽"),
    BUSINESS("Business", "💰"),
    WORLD("World", "🌍"),
    SCIENCE("Science", "🔬"),
    HEALTH("Health", "❤️"),
    GAMING("Gaming", "🎮"),
    MUSIC("Music", "🎵")
}

enum class Country(val code: String, val displayName: String, val flagEmoji: String) {
    GLOBAL("GLOBAL", "Global", "🌍"),
    INDIA("IN", "India", "🇮🇳"),
    USA("US", "USA", "🇺🇸"),
    UK("UK", "UK", "🇬🇧"),
    JAPAN("JP", "Japan", "🇯🇵"),
    GERMANY("DE", "Germany", "🇩🇪")
}

enum class TimeFilter(val displayName: String) {
    LAST_HOUR("Last Hour"),
    TODAY("Today"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month")
}

enum class TrendTier(val title: String, val emoji: String) {
    VIRAL("Viral", "🔥"),
    RISING("Rising", "📈"),
    POPULAR("Popular", "👀"),
    DECLINING("Declining", "📉")
}

data class TrendScore(
    val totalScore: Int, // 0-100
    val searchGrowth: Int, // 0-100 (30% weight)
    val socialMentions: Int, // 0-100 (25% weight)
    val engagement: Int, // 0-100 (20% weight)
    val newsCoverage: Int, // 0-100 (15% weight)
    val growthVelocity: Int // 0-100 (10% weight)
) {
    val tier: TrendTier
        get() = when {
            totalScore >= 90 -> TrendTier.VIRAL
            totalScore >= 70 -> TrendTier.RISING
            totalScore >= 40 -> TrendTier.POPULAR
            else -> TrendTier.DECLINING
        }

    companion object {
        fun calculate(
            searchGrowth: Int,
            socialMentions: Int,
            engagement: Int,
            newsCoverage: Int,
            growthVelocity: Int
        ): TrendScore {
            val weighted = (searchGrowth * 0.30) +
                    (socialMentions * 0.25) +
                    (engagement * 0.20) +
                    (newsCoverage * 0.15) +
                    (growthVelocity * 0.10)
            val score = weighted.toInt().coerceIn(0, 100)
            return TrendScore(
                totalScore = score,
                searchGrowth = searchGrowth,
                socialMentions = socialMentions,
                engagement = engagement,
                newsCoverage = newsCoverage,
                growthVelocity = growthVelocity
            )
        }
    }
}
