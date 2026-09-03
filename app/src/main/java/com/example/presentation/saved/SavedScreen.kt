package com.example.presentation.saved

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.BreakingNewsItem
import com.example.domain.model.TrendItem
import com.example.presentation.components.*
import com.example.presentation.home.BreakingNewsCard
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SavedScreen(
    viewModel: SavedViewModel,
    onNavigateToTrend: (String) -> Unit,
    onNavigateToExplore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("saved_screen_content")
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Saved & Library",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                // Search in saved
                SearchBarHeader(
                    query = uiState.searchQuery,
                    onQueryChange = { viewModel.onSearchQueryChanged(it) },
                    onSearch = { viewModel.onSearchQueryChanged(it) },
                    onVoiceClick = {},
                    placeholder = "Search saved trends or articles..."
                )

                // Segmented Tabs
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp)),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SavedTab.values().forEach { tab ->
                            val isSelected = tab == uiState.selectedTab
                            val tabModifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .then(
                                    if (isSelected) {
                                        Modifier.background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFF9333EA), Color(0xFF2563EB))
                                            )
                                        )
                                    } else {
                                        Modifier.background(Color.Transparent)
                                    }
                                )
                                .clickable { viewModel.onTabSelected(tab) }
                                .testTag("saved_tab_${tab.name.lowercase()}")

                            Box(
                                modifier = tabModifier,
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tab.title,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Tab Content
            when (uiState.selectedTab) {
                SavedTab.TRENDS -> {
                    if (uiState.savedTrends.isEmpty()) {
                        EmptySavedView(
                            icon = "🔖",
                            title = "No Saved Trends Yet",
                            description = "Tap the bookmark icon on any trending card to save it for offline reading and future monitoring.",
                            buttonText = "Explore Trends",
                            onButtonClick = onNavigateToExplore
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.savedTrends, key = { it.id }) { trend ->
                                TrendingListItem(
                                    trend = trend,
                                    onClick = { onNavigateToTrend(trend.id) },
                                    onToggleSave = {
                                        viewModel.removeSavedTrend(trend)
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Removed \"${trend.title}\" from saved")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                SavedTab.ARTICLES -> {
                    if (uiState.savedArticles.isEmpty()) {
                        EmptySavedView(
                            icon = "📰",
                            title = "No Saved Articles Yet",
                            description = "Bookmark breaking news and viral reports from the Home screen to build your offline reading archive.",
                            buttonText = "Discover News",
                            onButtonClick = onNavigateToExplore
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(uiState.savedArticles, key = { it.id }) { article ->
                                SavedArticleRowItem(
                                    article = article,
                                    onRemove = {
                                        viewModel.removeSavedArticle(article)
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Article removed from saved")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                SavedTab.RECENT -> {
                    if (uiState.recentlyViewed.isEmpty()) {
                        EmptySavedView(
                            icon = "🕒",
                            title = "No History Yet",
                            description = "Trends and intelligence reports you open will automatically appear in this timeline.",
                            buttonText = "Start Exploring",
                            onButtonClick = onNavigateToExplore
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.recentlyViewed, key = { it.id }) { trend ->
                                TrendingListItem(
                                    trend = trend,
                                    onClick = { onNavigateToTrend(trend.id) },
                                    onToggleSave = {
                                        viewModel.removeSavedTrend(trend)
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Removed \"${trend.title}\" from saved")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SavedArticleRowItem(
    article: BreakingNewsItem,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = article.source,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = ElectricCyan
                    )
                    Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = article.timeAgo,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = article.headline,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "${article.category.iconEmoji} ${article.category.displayName} • 🔥 ${article.trendingScore}% Viral",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Bookmark,
                    contentDescription = "Remove from saved",
                    tint = ElectricCyan
                )
            }
        }
    }
}

@Composable
fun EmptySavedView(
    icon: String,
    title: String,
    description: String,
    buttonText: String,
    onButtonClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(icon, style = MaterialTheme.typography.displayLarge)
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Button(
                onClick = onButtonClick,
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(buttonText)
            }
        }
    }
}
