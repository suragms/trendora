package com.example.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.TrendAnalysisResult
import com.example.ui.theme.*

/**
 * Shared AI Intelligence UI components used by both the AI Trends screen and
 * the Trend Details screen.
 */

// ---------------------------------------------------------------------------
// AI Insights Dashboard
// ---------------------------------------------------------------------------

@Composable
fun AIInsightsDashboard(
    overallSentiment: String,
    risingCount: Int,
    highViralCount: Int,
    avgConfidence: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            InsightStatCard(
                title = "Overall Sentiment",
                value = overallSentiment,
                icon = Icons.Default.SentimentSatisfied,
                iconColor = EmeraldGreen,
                weight = 1f
            )
            InsightStatCard(
                title = "Rising Trends",
                value = "$risingCount",
                icon = Icons.Default.TrendingUp,
                iconColor = ElectricCyan,
                weight = 1f
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            InsightStatCard(
                title = "High Viral Potential",
                value = "$highViralCount",
                icon = Icons.Default.LocalFireDepartment,
                iconColor = FlameOrange,
                weight = 1f
            )
            InsightStatCard(
                title = "AI Confidence",
                value = "$avgConfidence%",
                icon = Icons.Default.AutoAwesome,
                iconColor = NeonPurple,
                weight = 1f
            )
        }
    }
}

@Composable
fun RowScope.InsightStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    weight: Float = 1f
) {
    GlassmorphicCard(
        modifier = modifier
            .weight(weight)
            .semantics { contentDescription = "$title: $value" },
        borderColor = iconColor.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
}

// ---------------------------------------------------------------------------
// Sentiment badge (color + text, never color alone)
// ---------------------------------------------------------------------------

@Composable
fun SentimentLabel(
    sentiment: String,
    modifier: Modifier = Modifier
) {
    val (color, icon) = when (sentiment) {
        "Positive" -> EmeraldGreen to Icons.Default.ThumbUp
        "Negative" -> CrimsonRed to Icons.Default.ThumbDown
        "Mixed" -> AmberGold to Icons.Default.Balance
        else -> TextSecondaryDark to Icons.Default.HelpOutline
    }
    Surface(
        modifier = modifier.clip(RoundedCornerShape(10.dp)),
        color = color.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 5.dp)
                .semantics { contentDescription = "Sentiment: $sentiment" },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            Text(
                text = sentiment,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Growth prediction badge
// ---------------------------------------------------------------------------

@Composable
fun GrowthPredictionLabel(
    prediction: String,
    predictedGrowth: Int,
    modifier: Modifier = Modifier
) {
    val color = when (prediction) {
        "Rising" -> EmeraldGreen
        "Declining" -> CrimsonRed
        else -> AmberGold
    }
    Surface(
        modifier = modifier.clip(RoundedCornerShape(10.dp)),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = "$prediction ${predictedGrowth}%",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = color,
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 5.dp)
                .semantics { contentDescription = "Growth prediction: $prediction $predictedGrowth percent" }
        )
    }
}

// ---------------------------------------------------------------------------
// Expandable analysis section (used in Trend Details)
// ---------------------------------------------------------------------------

@Composable
fun ExpandableAISection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    initialExpanded: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    var expanded by remember { mutableStateOf(initialExpanded) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, iconColor.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .semantics { contentDescription = title },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 14.dp),
                content = content
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Bullet list (for key insights & risk factors)
// ---------------------------------------------------------------------------

@Composable
fun BulletList(
    items: List<String>,
    iconColor: Color,
    modifier: Modifier = Modifier,
    bullet: String = "•"
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = bullet,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = iconColor
                )
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Related topics chips (clickable)
// ---------------------------------------------------------------------------

@Composable
fun RelatedTopicsRow(
    topics: List<String>,
    onTopicClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (topics.isEmpty()) return
    androidx.compose.foundation.lazy.LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(topics.take(8)) { topic ->
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, NeonPurple.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .clickable { onTopicClick(topic) }
                    .semantics { contentDescription = "Related topic: $topic" },
                color = NeonPurple.copy(alpha = 0.08f)
            ) {
                Text(
                    text = "# $topic",
                    style = MaterialTheme.typography.labelMedium,
                    color = NeonPurple,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Analyze with AI button
// ---------------------------------------------------------------------------

@Composable
fun AnalyzeWithAIButton(
    isAnalyzing: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .semantics { contentDescription = "Analyze with AI" },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.linearGradient(listOf(NeonPurple, ElectricCyan))),
            contentAlignment = Alignment.Center
        ) {
            if (isAnalyzing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "✨ Analyze with AI",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Analysis progress steps animation
// ---------------------------------------------------------------------------

@Composable
fun AnalysisProgressSteps(
    currentStepLabel: String,
    modifier: Modifier = Modifier,
    isComplete: Boolean = false
) {
    val steps = listOf(
        "Collecting information",
        "Understanding discussions",
        "Detecting sentiment",
        "Predicting growth"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(14.dp)
            .semantics { contentDescription = "AI analysis in progress" },
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = ElectricCyan,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "🧠 Analyzing trend...",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        val activeIndex = steps.indexOfFirst { it in currentStepLabel }
        steps.forEachIndexed { index, stepLabel ->
            val isDone = isComplete || (activeIndex != -1 && index < activeIndex)
            val isActive = !isComplete && activeIndex != -1 && stepLabel in currentStepLabel

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isDone) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completed: $stepLabel",
                        tint = EmeraldGreen,
                        modifier = Modifier.size(16.dp)
                    )
                } else if (isActive) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = ElectricCyan,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Pending: $stepLabel",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = "✓ $stepLabel",
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        isDone -> EmeraldGreen
                        isActive -> MaterialTheme.colorScheme.onSurface
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    }
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// AI Analysis result card (full breakdown)
// ---------------------------------------------------------------------------

@Composable
fun AITrendAnalysisCard(
    analysis: TrendAnalysisResult,
    onTopicClick: (String) -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassmorphicCard(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "AI trend analysis for ${analysis.trendSummary.take(40)}" },
        borderColor = NeonPurple.copy(alpha = 0.35f)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = NeonPurple,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "AI ANALYSIS",
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                    color = ElectricCyan
                )
            }
            IconButton(
                onClick = onShare,
                modifier = Modifier
                    .size(40.dp)
                    .semantics { contentDescription = "Share AI analysis" }
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Sentiment & Growth row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SentimentLabel(sentiment = analysis.sentiment)
            GrowthPredictionLabel(
                prediction = analysis.growthPrediction,
                predictedGrowth = analysis.predictedGrowth
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Metric bars
        MetricBar(
            label = "Viral Probability",
            value = analysis.viralProbability,
            color = FlameOrange
        )
        Spacer(modifier = Modifier.height(6.dp))
        MetricBar(
            label = "AI Confidence",
            value = analysis.aiConfidence,
            color = NeonPurple
        )

        if (analysis.trendSummary.isNotBlank()) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = analysis.trendSummary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { contentDescription = "AI summary: ${analysis.trendSummary}" }
            )
        }

        if (analysis.whyTrending.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            ExpandableAISection(
                title = "🔥 Why is this trending?",
                icon = Icons.Default.Whatshot,
                iconColor = FlameOrange,
                initialExpanded = true
            ) {
                Text(
                    text = analysis.whyTrending,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (analysis.keyInsights.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            ExpandableAISection(
                title = "💡 Key Insights",
                icon = Icons.Default.Lightbulb,
                iconColor = AmberGold,
                initialExpanded = true
            ) {
                BulletList(
                    items = analysis.keyInsights,
                    iconColor = AmberGold,
                    bullet = "✦"
                )
            }
        }

        if (analysis.riskFactors.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            ExpandableAISection(
                title = "⚠️ Risk Factors",
                icon = Icons.Default.Warning,
                iconColor = CrimsonRed,
                initialExpanded = false
            ) {
                BulletList(
                    items = analysis.riskFactors,
                    iconColor = CrimsonRed,
                    bullet = "⚠"
                )
            }
        }

        if (analysis.relatedTopics.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "🔗 Related Topics",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            RelatedTopicsRow(topics = analysis.relatedTopics, onTopicClick = onTopicClick)
        }
    }
}

@Composable
fun MetricBar(
    label: String,
    value: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("$value%", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = color)
        }
        LinearProgressIndicator(
            progress = { value / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surface
        )
    }
}
