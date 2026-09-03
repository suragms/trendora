package com.example.presentation.ai

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.TrendItem
import com.example.presentation.components.*
import com.example.ui.theme.*

@Composable
fun AITrendsScreen(
    viewModel: AITrendsViewModel,
    onNavigateToTrend: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var customPromptInput by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("ai_trends_screen_content"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // HEADER
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "AI TREND INTELLIGENCE",
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold),
                                color = NeonPurple
                            )
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI",
                                tint = ElectricCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "Predictive Analytics",
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            // LOADING SKELETON
            if (uiState.isLoading) {
                item {
                    ShimmerBox(modifier = Modifier.fillMaxWidth().height(120.dp))
                }
                items(3) {
                    TrendListItemShimmer()
                }
            }

            // DAILY AI SUMMARY HERO BANNER
            item {
                AIDailySummaryBanner(summary = uiState.dailySummary)
            }

            // AI INSIGHTS DASHBOARD
            item {
                SectionHeader(
                    title = "📊 AI INSIGHTS DASHBOARD",
                    subtitle = "Live sentiment, growth & confidence overview"
                )
            }
            item {
                AIInsightsDashboard(
                    overallSentiment = uiState.overallSentiment,
                    risingCount = uiState.risingTrendCount,
                    highViralCount = uiState.highViralCount,
                    avgConfidence = uiState.avgConfidence
                )
            }

            // NOT CONFIGURED BANNER
            if (!uiState.isConfigured) {
                item {
                    GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = AmberGold.copy(alpha = 0.4f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = AmberGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "AI analysis is not configured yet. Add your Gemini API key in local.properties to enable live AI insights.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // AI PREDICTION CARDS (EXPECTED GROWTH & VIRAL PROBABILITY)
            item {
                SectionHeader(
                    title = "🔮 TOP AI ANALYZED TRENDS",
                    subtitle = "Machine learning forecasts on tomorrow's top trends"
                )
            }

            if (uiState.aiTrends.isEmpty()) {
                item {
                    EmptyState(
                        emoji = "🤖",
                        title = "No AI-analyzed trends yet",
                        subtitle = "AI predictions appear here once live trend data is available. If you're offline, cached or sample trends will be analyzed instead.",
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
                    )
                }
            } else {
                items(uiState.aiTrends, key = { it.id }) { trend ->
                    AIPredictionCard(
                        trend = trend,
                        onClick = { onNavigateToTrend(trend.id) }
                    )
                }
            }

            // ANALYZE WITH AI SECTION
            item {
                SectionHeader(
                    title = "✨ ANALYZE WITH AI",
                    subtitle = "Generate a deep, structured analysis of any top trend"
                )
            }

            item {
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = NeonPurple.copy(alpha = 0.35f)
                ) {
                    val selected = uiState.selectedTrendForAnalysis
                    if (selected != null) {
                        Text(
                            text = selected.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${selected.category.iconEmoji} ${selected.category.displayName} • Score ${selected.score.totalScore}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricCyan
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        AnalyzeWithAIButton(
                            isAnalyzing = uiState.isAnalyzing,
                            enabled = !uiState.isAnalyzing,
                            onClick = { viewModel.analyzeSelectedTrend() }
                        )
                    } else {
                        Text(
                            text = "No trend selected.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ANALYSIS PROGRESS + RESULT
            if (uiState.isAnalyzing) {
                item {
                    AnalysisProgressSteps(
                        currentStepLabel = uiState.analysisStep.label,
                        isComplete = uiState.analysisStep == AnalysisStep.COMPLETE
                    )
                }
            }

            if (uiState.analysisError != null) {
                item {
                    ErrorBanner(
                        message = uiState.analysisError ?: "Analysis failed.",
                        onRetry = { viewModel.analyzeSelectedTrend(forceRefresh = true) }
                    )
                }
            }

            uiState.selectedAnalysis?.let { analysis ->
                item {
                    AITrendAnalysisCard(
                        analysis = analysis,
                        onTopicClick = { topic -> viewModel.askAIAssistant("Tell me more about $topic", analysis.trendSummary) },
                        onShare = {
                            val shareText = buildString {
                                appendLine("🤖 ${analysis.trendSummary.ifBlank { "Trendora AI Analysis" }}")
                                appendLine("Sentiment: ${analysis.sentiment} (${analysis.sentimentScore}/100)")
                                appendLine("Growth: ${analysis.growthPrediction} ${analysis.predictedGrowth}%")
                                appendLine("Viral Probability: ${analysis.viralProbability}%")
                                appendLine("AI Confidence: ${analysis.aiConfidence}%")
                                append("#Trendora #AIInsights")
                            }
                            val sendIntent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            context.startActivity(android.content.Intent.createChooser(sendIntent, null))
                        }
                    )
                }
            }

            // ASK AI TREND ASSISTANT SECTION
            item {
                SectionHeader(
                    title = "🤖 ASK TRENDORA AI",
                    subtitle = "Get real-time insights & custom growth analysis"
                )
            }

            item {
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = ElectricCyan.copy(alpha = 0.4f)
                ) {
                    Text(
                        text = "Real-time AI Trend Consultant",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Ask anything about current viral velocity, topic forecasts, or sentiment dynamics.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Suggested Prompts Row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val samplePrompts = listOf(
                            "Why is AI trending today?",
                            "Will Fusion Energy stay viral?",
                            "Forecast next week's tech trends",
                            "Explain the GTA 6 viral spike"
                        )
                        items(samplePrompts, key = { it }) { prompt ->
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, NeonPurple.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .clickable {
                                        customPromptInput = prompt
                                        viewModel.askAIAssistant(prompt)
                                    },
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Text(
                                    text = "✨ $prompt",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Input field + Send button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextField(
                            value = customPromptInput,
                            onValueChange = { customPromptInput = it },
                            placeholder = { Text("Ask Trendora AI a question...") },
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp)),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            singleLine = true
                        )

                        IconButton(
                            onClick = {
                                if (customPromptInput.isNotBlank()) {
                                    viewModel.askAIAssistant(customPromptInput)
                                }
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Brush.linearGradient(listOf(NeonPurple, ElectricCyan)))
                                .testTag("ask_ai_button")
                        ) {
                            if (uiState.isAskingAI) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    // Answer Display
                    AnimatedVisibility(visible = uiState.aiAssistantAnswer != null) {
                        uiState.aiAssistantAnswer?.let { answer ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 14.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(1.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = "AI Answer",
                                                tint = NeonPurple,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "Trendora AI Insight",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = ElectricCyan
                                            )
                                        }

                                        IconButton(
                                            onClick = { viewModel.clearAIAnswer() },
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Close",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = answer,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AIDailySummaryBanner(
    summary: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(
                1.dp,
                Color(0xFFA855F7).copy(alpha = 0.25f),
                RoundedCornerShape(24.dp)
            ),
        color = Color(0xFF1E1B4B).copy(alpha = 0.35f)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            .background(Brush.linearGradient(listOf(Color(0xFF9333EA), Color(0xFF22D3EE)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "AI DISCOVERY BRIEF",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                }

                Surface(
                    modifier = Modifier.clip(RoundedCornerShape(10.dp)),
                    color = ElectricCyan.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "TODAY",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = ElectricCyan,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Text(
                text = summary,
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 22.sp),
                color = Color(0xFFE2E2E2)
            )
        }
    }
}

@Composable
fun AIPredictionCard(
    trend: TrendItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassmorphicCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("ai_prediction_card_${trend.id}"),
        borderColor = NeonPurple.copy(alpha = 0.3f)
    ) {
        // Title + Category
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${trend.category.iconEmoji} ${trend.category.displayName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricCyan
                )
                Text(
                    text = trend.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            TrendScoreBadge(score = trend.score.totalScore, tier = trend.score.tier)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Gauge + Predictions Matrix
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularConfidenceGauge(
                percentage = trend.aiAnalysis.aiConfidencePercent,
                size = 76.dp
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Expected Growth Bar
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Expected Growth", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("+${trend.aiAnalysis.expectedGrowthPercent}%", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = EmeraldGreen)
                    }
                    LinearProgressIndicator(
                        progress = { trend.aiAnalysis.expectedGrowthPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = EmeraldGreen,
                        trackColor = MaterialTheme.colorScheme.surface
                    )
                }

                // Viral Probability Bar
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Viral Probability", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${trend.aiAnalysis.viralProbabilityPercent}%", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = NeonPurple)
                    }
                    LinearProgressIndicator(
                        progress = { trend.aiAnalysis.viralProbabilityPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = NeonPurple,
                        trackColor = MaterialTheme.colorScheme.surface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Sentiment breakdown mini bar
        SentimentMiniBar(sentiment = trend.aiAnalysis.sentiment)
    }
}

@Composable
fun SentimentMiniBar(
    sentiment: com.example.domain.model.SentimentBreakdown,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Community Sentiment Breakdown",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "👍 ${sentiment.positivePercent}%  😐 ${sentiment.neutralPercent}%  👎 ${sentiment.negativePercent}%",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .weight(sentiment.positivePercent.toFloat().coerceAtLeast(1f))
                    .fillMaxHeight()
                    .background(EmeraldGreen)
            )
            Box(
                modifier = Modifier
                    .weight(sentiment.neutralPercent.toFloat().coerceAtLeast(1f))
                    .fillMaxHeight()
                    .background(AmberGold)
            )
            Box(
                modifier = Modifier
                    .weight(sentiment.negativePercent.toFloat().coerceAtLeast(1f))
                    .fillMaxHeight()
                    .background(CrimsonRed)
            )
        }
    }
}
