package com.example.presentation.detail

import android.content.Intent
import androidx.compose.animation.*
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.DiscussionItem
import com.example.domain.model.TrendItem
import com.example.presentation.ai.SentimentMiniBar
import com.example.presentation.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendDetailScreen(
    trendId: String,
    viewModel: TrendDetailViewModel,
    onBackClick: () -> Unit,
    onTopicClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(trendId) {
        viewModel.loadTrend(trendId)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Trend Intelligence",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Share button
                    IconButton(
                        onClick = {
                            uiState.trend?.let { trend ->
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "Check out this trending topic on Trendora: ${trend.title} (${trend.score.totalScore}% Viral Score)\n#${trend.category.name}")
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, null)
                                context.startActivity(shareIntent)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share trend",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Save Bookmark button
                    IconButton(
                        onClick = { viewModel.toggleSave() },
                        modifier = Modifier.testTag("detail_bookmark_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Save trend",
                            tint = if (uiState.isSaved) ElectricCyan else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        val trend = uiState.trend

        if (trend == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NeonPurple)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("trend_detail_content"),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // HERO SECTION
                item {
                    DetailHeroSection(trend = trend)
                }

                // INTERACTIVE GRAPH SECTION
                item {
                    GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = NeonPurple.copy(alpha = 0.35f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📈 Trend Velocity Graph",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Timeframe switch buttons (Today / 7D / 30D)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                DetailTimeframe.values().forEach { tf ->
                                    val isSelected = tf == uiState.selectedTimeframe
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { viewModel.setTimeframe(tf) },
                                        color = if (isSelected) ElectricCyan else Color(0xFF27272A)
                                    ) {
                                        Text(
                                            text = tf.title,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            color = if (isSelected) Color.Black else Color(0xFFA1A1AA),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        InteractiveTrendGraph(
                            historyPoints = uiState.activeHistoryPoints,
                            lineColor = ElectricCyan,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // AI DEEP DIVE SECTION
                item {
                    AIDeepDiveCard(
                        trend = trend,
                        isAnalyzing = uiState.isAnalyzingWithAI,
                        onRefreshAI = { viewModel.analyzeWithAI(forceRefresh = true) }
                    )
                }

                // STRUCTURED AI ANALYSIS SECTION
                item {
                    AITrendIntelligenceSection(
                        analysis = uiState.aiAnalysis,
                        isAnalyzing = uiState.isAnalyzingWithAI,
                        errorMessage = uiState.aiAnalysisError,
                        analysisStepLabel = uiState.analysisStep.label,
                        analysisComplete = uiState.analysisStep == DetailAnalysisStep.COMPLETE,
                        isConfigured = uiState.isAIConfigured,
                        onAnalyze = { viewModel.analyzeWithAI(forceRefresh = true) },
                        onTopicClick = onTopicClick,
                        onShare = {
                            uiState.aiAnalysis?.let { analysis ->
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, buildShareText(trend, analysis))
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, null))
                            }
                        }
                    )
                }

                // RELATED TOPICS CHIPS
                if (trend.aiAnalysis.relatedTopics.isNotEmpty()) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "🏷️ Related Topics & Entities",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(trend.aiAnalysis.relatedTopics) { topic ->
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                                            .clickable { onTopicClick(topic) },
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = "#$topic",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = ElectricCyan,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // MULTI-PLATFORM DISCUSSIONS
                if (trend.discussions.isNotEmpty()) {
                    item {
                        Text(
                            text = "💬 Multi-Platform Discussions",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    items(trend.discussions) { discussion ->
                        DiscussionCard(discussion = discussion)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailHeroSection(
    trend: TrendItem,
    modifier: Modifier = Modifier
) {
    GlassmorphicCard(
        modifier = modifier.fillMaxWidth(),
        borderColor = NeonPurple.copy(alpha = 0.5f)
    ) {
        // Category + Region
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                color = MaterialTheme.colorScheme.surface
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
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Text(
                text = "${trend.country.flagEmoji} ${trend.country.displayName} • ${trend.timeAgo}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Title
        Text(
            text = trend.title,
            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Badges Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TrendScoreBadge(score = trend.score.totalScore, tier = trend.score.tier)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = "Growth",
                    tint = EmeraldGreen,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "+${trend.growthPercentage}% Growth Velocity",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = EmeraldGreen
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Multi-channel Metrics Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MetricPill(label = "Search Growth", value = "+${trend.score.searchGrowth}%", color = ElectricCyan)
            MetricPill(label = "Social Volume", value = trend.discussionsCount, color = NeonPurple)
            MetricPill(label = "News Coverage", value = "${trend.score.newsCoverage}%", color = AmberGold)
        }
    }
}

@Composable
fun MetricPill(
    label: String,
    value: String,
    color: Color
) {
    Surface(
        modifier = Modifier.clip(RoundedCornerShape(10.dp)),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AIDeepDiveCard(
    trend: TrendItem,
    isAnalyzing: Boolean,
    onRefreshAI: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassmorphicCard(
        modifier = modifier.fillMaxWidth(),
        borderColor = ElectricCyan.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI",
                    tint = NeonPurple,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "AI Deep Dive & Why Trending",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            IconButton(
                onClick = onRefreshAI,
                modifier = Modifier.size(32.dp)
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = ElectricCyan, strokeWidth = 2.dp)
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh AI",
                        tint = ElectricCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Why Trending explanation
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "🤖 WHY IS THIS TRENDING?",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = ElectricCyan
                )
                Text(
                    text = trend.aiAnalysis.whyTrending,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // AI Summary
        Text(
            text = trend.aiAnalysis.summary,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Sentiment Breakdown
        SentimentMiniBar(sentiment = trend.aiAnalysis.sentiment)
    }
}

/**
 * Full structured AI intelligence section for the trend detail screen.
 * Includes: Analyze button, progress steps, and the complete analysis breakdown.
 */
@Composable
fun AITrendIntelligenceSection(
    analysis: com.example.domain.model.TrendAnalysisResult?,
    isAnalyzing: Boolean,
    errorMessage: String?,
    analysisStepLabel: String,
    analysisComplete: Boolean,
    isConfigured: Boolean,
    onAnalyze: () -> Unit,
    onTopicClick: (String) -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SectionHeader(
            title = "🤖 AI ANALYSIS",
            subtitle = "Understand what is trending and why"
        )

        // Analyze button
        AnalyzeWithAIButton(
            isAnalyzing = isAnalyzing,
            enabled = !isAnalyzing,
            onClick = onAnalyze
        )

        // Progress steps
        if (isAnalyzing) {
            AnalysisProgressSteps(
                currentStepLabel = analysisStepLabel,
                isComplete = analysisComplete
            )
        }

        // Error
        if (errorMessage != null) {
            ErrorBanner(
                message = errorMessage,
                onRetry = onAnalyze
            )
        }

        // Not configured message
        if (!isConfigured && analysis == null) {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = AmberGold.copy(alpha = 0.4f)
            ) {
                Text(
                    text = "AI analysis is not configured yet. Add your Gemini API key in local.properties to enable live AI insights.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Analysis result
        analysis?.let { result ->
            AITrendAnalysisCard(
                analysis = result,
                onTopicClick = onTopicClick,
                onShare = onShare
            )
        }
    }
}

private fun buildShareText(
    trend: TrendItem,
    analysis: com.example.domain.model.TrendAnalysisResult
): String {
    return buildString {
        appendLine("🤖 ${trend.title}")
        appendLine("${trend.category.displayName} • Score ${trend.score.totalScore}")
        appendLine("")
        if (analysis.trendSummary.isNotBlank()) {
            appendLine(analysis.trendSummary)
            appendLine("")
        }
        appendLine("Sentiment: ${analysis.sentiment} (${analysis.sentimentScore}/100)")
        appendLine("Growth: ${analysis.growthPrediction} ${analysis.predictedGrowth}%")
        appendLine("Viral Probability: ${analysis.viralProbability}%")
        appendLine("AI Confidence: ${analysis.aiConfidence}%")
        append("#Trendora #AIInsights")
    }
}

@Composable
fun DiscussionCard(
    discussion: DiscussionItem,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(20.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = discussion.author.take(1),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Column {
                        Text(
                            text = discussion.author,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${discussion.handle} • ${discussion.platform}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricCyan
                        )
                    }
                }

                Text(
                    text = discussion.timeAgo,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = discussion.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FavoriteBorder,
                        contentDescription = "Likes",
                        tint = CrimsonRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = discussion.likesCount,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ModeComment,
                        contentDescription = "Comments",
                        tint = ElectricCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = discussion.commentsCount,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
