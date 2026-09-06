package com.example.presentation.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.domain.model.*
import com.example.presentation.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToTrend: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isLoading) {
                // Skeleton shimmer while the first load is in flight.
                HomeScreenShimmer(
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = { viewModel.refreshData() },
                    modifier = Modifier.fillMaxSize()
                ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("home_screen_content"),
                    contentPadding = PaddingValues(bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // OFFLINE BANNER
                    if (uiState.isOffline) {
                        item {
                            OfflineBanner(
                                message = if (uiState.isShowingMock) {
                                    "You're offline — showing sample trends."
                                } else {
                                    "You're offline — showing cached trends."
                                },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }

                    // ERROR BANNER WITH RETRY
                    if (uiState.errorMessage != null) {
                        item {
                            ErrorBanner(
                                message = uiState.errorMessage ?: "",
                                onRetry = {
                                    viewModel.clearError()
                                    viewModel.refreshData()
                                },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }

                    // TOP HEADER
                    item {
                        HomeTopHeader(
                    greeting = uiState.userGreeting,
                    unreadNotifs = uiState.unreadNotificationsCount,
                    onNotifClick = { viewModel.setNotificationsDialogVisible(true) },
                    onSettingsClick = onNavigateToSettings,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // SEARCH BAR
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SearchBarHeader(
                        query = uiState.searchQuery,
                        onQueryChange = { viewModel.onSearchQueryChanged(it) },
                        onSearch = { viewModel.onSearchSubmitted(it) },
                        onVoiceClick = { viewModel.setVoiceDialogVisible(true) }
                    )

                    // Trending suggestions / recent searches
                    if (uiState.searchQuery.isEmpty() && uiState.recentSearches.isNotEmpty()) {
                        RecentSearchesRow(
                            searches = uiState.recentSearches,
                            onSearchClick = { viewModel.onSearchQueryChanged(it) }
                        )
                    } else if (uiState.searchQuery.isEmpty()) {
                        TrendingSuggestionsRow(
                            suggestions = uiState.trendingSuggestions.take(4),
                            onSuggestionClick = { viewModel.onSearchQueryChanged(it) }
                        )
                    }
                }
            }

            // TRENDING CATEGORIES
            item {
                CategoryChipRow(
                    categories = TrendCategory.values().toList(),
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = { viewModel.onCategorySelected(it) }
                )
            }

            // TRENDING NOW HEADER
            item {
                SectionHeader(
                    title = "🔥 TRENDING NOW",
                    subtitle = "Real-time velocity & discussion heatmaps",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // TRENDING NOW HORIZONTAL CARDS
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(uiState.trends, key = { it.id }) { trend ->
                        TrendingNowCard(
                            trend = trend,
                            onClick = { onNavigateToTrend(trend.id) },
                            onToggleSave = { viewModel.toggleSaveTrend(trend) }
                        )
                    }
                }
            }

            // BREAKING NOW SECTION
            if (uiState.breakingNews.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "⚡ BREAKING NOW",
                        subtitle = "Fastest-rising news across the globe",
                        isLive = true,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(uiState.breakingNews, key = { it.id }) { news ->
                            BreakingNewsCard(
                                news = news,
                                onSaveClick = { viewModel.toggleSaveArticle(news) }
                            )
                        }
                    }
                }
            }

            // TREND ANALYTICS
            uiState.analytics?.let { analytics ->
                item {
                    SectionHeader(
                        title = "📊 TREND ANALYTICS",
                        subtitle = "Multi-platform telemetry & momentum breakdown",
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                item {
                    TrendAnalyticsSection(
                        analytics = analytics,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // TOP TRENDING LIST (VERTICAL)
            item {
                SectionHeader(
                    title = "📈 ALL TRENDING TOPICS",
                    subtitle = "Sorted by weighted AI trend score",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            if (uiState.trends.isEmpty()) {
                item {
                    EmptyState(
                        emoji = "🌐",
                        title = "No trends to show right now",
                        subtitle = if (uiState.isOffline) {
                            "You're offline and no cached trends are available yet. Pull to refresh when you're back online."
                        } else {
                            "We couldn't find any trending topics. Pull down to try again."
                        },
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp)
                    )
                }
            } else {
                items(uiState.trends, key = { it.id }) { trend ->
                    TrendingListItem(
                        trend = trend,
                        onClick = { onNavigateToTrend(trend.id) },
                        onToggleSave = { viewModel.toggleSaveTrend(trend) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
                }
            }
        }
    }

    // VOICE SEARCH DIALOG
    if (uiState.showVoiceDialog) {
        VoiceSearchDialog(
            onDismiss = { viewModel.setVoiceDialogVisible(false) },
            onResult = { result ->
                viewModel.onSearchQueryChanged(result)
                viewModel.onSearchSubmitted(result)
                viewModel.setVoiceDialogVisible(false)
            }
        )
    }

    // NOTIFICATIONS BOTTOM SHEET
    if (uiState.showNotificationsDialog) {
        NotificationsBottomSheet(
            onDismiss = { viewModel.setNotificationsDialogVisible(false) },
            onTriggerTestAlert = { viewModel.triggerSimulatedTrendAlert() },
            onNavigateToTrend = { trendId ->
                viewModel.setNotificationsDialogVisible(false)
                onNavigateToTrend(trendId)
            }
        )
    }
}

@Composable
fun HomeTopHeader(
    greeting: String,
    unreadNotifs: Int,
    onNotifClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Logo + Greeting
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF9333EA), Color(0xFF22D3EE)))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "T",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = Color.White
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "TRENDORA LIVE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.2.sp,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    PulsingLiveDot()
                }
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Actions: Notifications & Settings
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Notification Bell with unread badge
            IconButton(
                onClick = onNotifClick,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                    .testTag("notification_button")
            ) {
                Box(contentAlignment = Alignment.TopEnd) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                    if (unreadNotifs > 0) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(CrimsonRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$unreadNotifs",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Settings Gear
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                    .testTag("header_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun TrendingNowCard(
    trend: TrendItem,
    onClick: () -> Unit,
    onToggleSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .width(280.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(24.dp)
            )
            .clickable(onClick = onClick)
            .testTag("trend_card_${trend.id}"),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Category + Save
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                    color = MaterialTheme.colorScheme.outline
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(trend.category.iconEmoji, style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = trend.category.displayName,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = NeonPurple
                        )
                    }
                }

                IconButton(
                    onClick = onToggleSave,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (trend.isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Save trend",
                        tint = if (trend.isSaved) ElectricCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Title
            Text(
                text = trend.title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Sparkline visualization
            MiniSparkline(
                points = trend.chartData,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                lineColor = if (trend.score.totalScore >= 90) Color(0xFFFF5252) else ElectricCyan
            )

            // Metrics row: Score Badge + Growth + Discussions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TrendScoreBadge(
                    score = trend.score.totalScore,
                    tier = trend.score.tier
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = "Growth",
                        tint = EmeraldGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "+${trend.growthPercentage}%",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldGreen
                    )
                }
            }

            // Footer: discussions & source icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = trend.discussionsCount,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    trend.sourceIcons.take(3).forEach { icon ->
                        Surface(
                            modifier = Modifier.clip(RoundedCornerShape(6.dp)),
                            color = MaterialTheme.colorScheme.outline
                        ) {
                            Text(
                                text = icon,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BreakingNewsCard(
    news: BreakingNewsItem,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .width(300.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                AsyncImage(
                    model = news.imageUrl,
                    contentDescription = news.headline,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0xDD09090B))
                            )
                        )
                )

                // Category tag on top left
                Surface(
                    modifier = Modifier
                        .padding(10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .align(Alignment.TopStart),
                    color = Color.Black.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = "${news.category.iconEmoji} ${news.category.displayName}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Trend score on top right
                Surface(
                    modifier = Modifier
                        .padding(10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .align(Alignment.TopEnd),
                    color = CrimsonRed.copy(alpha = 0.85f)
                ) {
                    Text(
                        text = "🔥 ${news.trendingScore}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = news.headline,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = news.source,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = ElectricCyan
                        )
                        Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = news.timeAgo,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onSaveClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.BookmarkBorder,
                            contentDescription = "Save news",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TrendAnalyticsSection(
    analytics: TrendAnalytics,
    modifier: Modifier = Modifier
) {
    GlassmorphicCard(
        modifier = modifier.fillMaxWidth(),
        borderColor = NeonPurple.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Global Momentum Telemetry",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "24H Window",
                style = MaterialTheme.typography.labelSmall,
                color = ElectricCyan
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3 Cards: Upward, Downward, Stable
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AnalyticsMiniCard(
                title = "Surging Up",
                count = "${analytics.upwardTrendingCount}",
                trendEmoji = "📈",
                color = EmeraldGreen,
                modifier = Modifier.weight(1f)
            )
            AnalyticsMiniCard(
                title = "Stable Peak",
                count = "${analytics.stableTrendingCount}",
                trendEmoji = "👀",
                color = ElectricCyan,
                modifier = Modifier.weight(1f)
            )
            AnalyticsMiniCard(
                title = "Cooling Down",
                count = "${analytics.downwardTrendingCount}",
                trendEmoji = "📉",
                color = CrimsonRed,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Stats rows
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Total 24h Mentions",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = analytics.totalMentions24h,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = NeonPurple
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Top Velocity Category",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = analytics.topTrendingCategory,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = ElectricCyan
                )
            }
        }
    }
}

@Composable
fun AnalyticsMiniCard(
    title: String,
    count: String,
    trendEmoji: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(trendEmoji, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = count,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun RecentSearchesRow(
    searches: List<String>,
    onSearchClick: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Recent:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
        items(searches, key = { it }) { query ->
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                    .clickable { onSearchClick(query) },
                color = MaterialTheme.colorScheme.surface
            ) {
                Text(
                    text = query,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun TrendingSuggestionsRow(
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Popular:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
        items(suggestions, key = { it }) { item ->
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, NeonPurple.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .clickable { onSuggestionClick(item) },
                color = MaterialTheme.colorScheme.surface
            ) {
                Text(
                    text = "🔍 $item",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun VoiceSearchDialog(
    onDismiss: () -> Unit,
    onResult: (String) -> Unit
) {
    val simulatedVoiceQueries = listOf(
        "Artificial Intelligence breakthroughs",
        "Kerala Technology startup hub",
        "Clean fusion energy progress",
        "GTA 6 gameplay preview",
        "iPhone 18 titanium leaks"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Search",
                    tint = NeonPurple
                )
                Text("Voice Trend Search")
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Audio Waveform Animation
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(40.dp)
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
                    repeat(7) { i ->
                        val height by infiniteTransition.animateFloat(
                            initialValue = 8f,
                            targetValue = 36f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(350 + i * 80, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "bar_$i"
                        )
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(height.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (i % 2 == 0) ElectricCyan else NeonPurple)
                        )
                    }
                }

                Text(
                    text = "Listening... or select a spoken prompt:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    simulatedVoiceQueries.forEach { query ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                                .clickable {
                                    onResult(query)
                                },
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = "🗣️ \"$query\"",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsBottomSheet(
    onDismiss: () -> Unit,
    onTriggerTestAlert: () -> Unit,
    onNavigateToTrend: (String) -> Unit
) {
    val sampleNotifications = remember {
        listOf(
            NotificationItem("1", "🔥 AI is rapidly trending right now!", "Artificial Intelligence spiked +142% with +185K discussions.", NotificationType.AI_ALERT, "10m ago", trendId = "trend_ai_gen"),
            NotificationItem("2", "📈 Topic Growth Alert", "Your followed topic 'Technology & Startups' increased by 45% today.", NotificationType.TOPIC_GROWTH, "1h ago", trendId = "trend_kerala_tech"),
            NotificationItem("3", "⚡ Breaking Trend Detected", "Clean Fusion Energy achieved sustained net gain milestones.", NotificationType.BREAKING, "3h ago", trendId = "trend_fusion_energy"),
            NotificationItem("4", "📋 Daily Intelligence Brief", "Your morning digest is ready: 8 top viral trends across Tech & AI.", NotificationType.DAILY_BRIEF, "6h ago")
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Intelligent Trend Alerts",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Button(
                    onClick = onTriggerTestAlert,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                ) {
                    Text("Trigger Alert", style = MaterialTheme.typography.labelSmall)
                }
            }

            sampleNotifications.forEach { notif ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                        .clickable {
                            notif.trendId?.let { onNavigateToTrend(it) }
                        },
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = notif.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = notif.timeAgo,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = notif.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
