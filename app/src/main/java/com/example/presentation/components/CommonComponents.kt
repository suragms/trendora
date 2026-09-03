package com.example.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.TrendCategory
import com.example.domain.model.TrendHistoryPoint
import com.example.domain.model.TrendItem
import com.example.domain.model.TrendTier
import com.example.ui.theme.*

@Composable
fun SectionHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    isLive: Boolean = false
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            if (isLive) {
                Surface(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                    color = CrimsonRed.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        PulsingLiveDot(color = CrimsonRed)
                        Text(
                            text = "LIVE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = CrimsonRed
                        )
                    }
                }
            }
        }
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun TrendingListItem(
    trend: TrendItem,
    onClick: () -> Unit,
    onToggleSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .testTag("trend_list_item_${trend.id}"),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank badge / emoji
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF27272A)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = trend.category.iconEmoji,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = trend.category.displayName,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = NeonPurple
                    )
                    Text("•", color = Color(0xFF71717A))
                    Text(
                        text = trend.timeAgo,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF71717A)
                    )
                }

                Text(
                    text = trend.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${trend.discussionsCount} • ${trend.searchVolume}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFA1A1AA)
                )
            }

            // Mini sparkline & score
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TrendScoreBadge(score = trend.score.totalScore, tier = trend.score.tier)
                Text(
                    text = "+${trend.growthPercentage}%",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = EmeraldGreen
                )
            }
        }
    }
}

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    borderColor: Color = Color.White.copy(alpha = 0.06f),
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = modifier
        .clip(RoundedCornerShape(24.dp))
        .background(backgroundColor)
        .border(1.dp, borderColor, RoundedCornerShape(24.dp))
        .then(
            if (onClick != null) {
                Modifier.clickable(onClick = onClick)
            } else Modifier
        )

    Column(
        modifier = cardModifier.padding(16.dp),
        content = content
    )
}

@Composable
fun TrendScoreBadge(
    score: Int,
    tier: TrendTier,
    modifier: Modifier = Modifier
) {
    val (badgeBg, badgeText, badgeBorder) = when (tier) {
        TrendTier.VIRAL -> Triple(
            Color(0x33FF416C),
            Color(0xFFFF5252),
            Color(0x66FF416C)
        )
        TrendTier.RISING -> Triple(
            Color(0x33A855F7),
            NeonPurple,
            Color(0x66A855F7)
        )
        TrendTier.POPULAR -> Triple(
            Color(0x3300F0FF),
            ElectricCyan,
            Color(0x6600F0FF)
        )
        TrendTier.LOW_ACTIVITY -> Triple(
            Color(0x3364748B),
            TextSecondaryDark,
            Color(0x6664748B)
        )
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, badgeBorder, RoundedCornerShape(12.dp)),
        color = badgeBg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = tier.emoji,
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = "${tier.title} $score%",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = badgeText
            )
        }
    }
}

@Composable
fun PulsingLiveDot(
    modifier: Modifier = Modifier,
    color: Color = CrimsonRed
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier.size(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(10.dp * scale)
                .clip(CircleShape)
                .background(color.copy(alpha = alpha * 0.4f))
        )
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
    }
}

@Composable
fun MiniSparkline(
    points: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = ElectricCyan
) {
    if (points.isEmpty()) return

    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(points) {
        animatedProgress.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val minVal = points.minOrNull() ?: 0f
        val maxVal = (points.maxOrNull() ?: 100f).coerceAtLeast(minVal + 1f)

        val path = Path()
        val fillPath = Path()

        val stepX = width / (points.size - 1).coerceAtLeast(1)

        points.forEachIndexed { index, value ->
            val normalizedY = 1f - ((value - minVal) / (maxVal - minVal))
            val x = index * stepX * animatedProgress.value
            val y = normalizedY * (height - 8.dp.toPx()) + 4.dp.toPx()

            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        fillPath.lineTo(width * animatedProgress.value, height)
        fillPath.close()

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                listOf(lineColor.copy(alpha = 0.35f), Color.Transparent)
            )
        )

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun InteractiveTrendGraph(
    historyPoints: List<TrendHistoryPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = NeonPurple
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(historyPoints) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
    }

    if (historyPoints.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No historical data available", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    Column(modifier = modifier) {
        // Active point indicator label
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val activePoint = selectedIndex?.let { historyPoints.getOrNull(it) } ?: historyPoints.last()
            Text(
                text = "Timeframe: ${activePoint.label}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Score: ${activePoint.value.toInt()}%",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = ElectricCyan
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val values = historyPoints.map { it.value }
                val minVal = (values.minOrNull() ?: 0f).coerceAtLeast(0f)
                val maxVal = (values.maxOrNull() ?: 100f).coerceAtLeast(minVal + 10f)

                val stepX = width / (historyPoints.size - 1).coerceAtLeast(1)

                val path = Path()
                val fillPath = Path()

                historyPoints.forEachIndexed { index, point ->
                    val normalizedY = 1f - ((point.value - minVal) / (maxVal - minVal))
                    val x = index * stepX * animatedProgress.value
                    val y = normalizedY * (height - 24.dp.toPx()) + 12.dp.toPx()

                    if (index == 0) {
                        path.moveTo(x, y)
                        fillPath.moveTo(x, height)
                        fillPath.lineTo(x, y)
                    } else {
                        path.lineTo(x, y)
                        fillPath.lineTo(x, y)
                    }

                    // Draw Node
                    drawCircle(
                        color = if (selectedIndex == index) ElectricCyan else lineColor,
                        radius = if (selectedIndex == index) 6.dp.toPx() else 3.5.dp.toPx(),
                        center = Offset(x, y)
                    )
                }

                fillPath.lineTo(width * animatedProgress.value, height)
                fillPath.close()

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        listOf(lineColor.copy(alpha = 0.4f), lineColor.copy(alpha = 0.05f), Color.Transparent)
                    )
                )

                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        // Timeline labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            historyPoints.forEachIndexed { index, point ->
                Text(
                    text = point.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selectedIndex == index) ElectricCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable { selectedIndex = index }
                )
            }
        }
    }
}

@Composable
fun CategoryChipRow(
    categories: List<TrendCategory>,
    selectedCategory: TrendCategory,
    onCategorySelected: (TrendCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory
            val chipModifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .then(
                    if (isSelected) {
                        Modifier.background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF9333EA), Color(0xFF2563EB))
                            )
                        )
                    } else {
                        Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(24.dp))
                    }
                )
                .clickable { onCategorySelected(category) }
                .testTag("category_chip_${category.name.lowercase()}")

            Box(
                modifier = chipModifier
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = category.iconEmoji,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = category.displayName,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) Color.White else Color(0xFFA1A1AA)
                    )
                }
            }
        }
    }
}

@Composable
fun SearchBarHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onVoiceClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search trends, topics, news..."
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(24.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color(0xFF71717A),
                modifier = Modifier.size(20.dp)
            )

            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF71717A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("search_input_field")
            )

            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Clear search",
                        tint = Color(0xFF71717A),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            IconButton(
                onClick = onVoiceClick,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF27272A))
                    .testTag("voice_search_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Search",
                    tint = ElectricCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun CircularConfidenceGauge(
    percentage: Int,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    strokeWidth: Dp = 8.dp,
    activeColor: Color = ElectricCyan
) {
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(percentage) {
        animatedProgress.animateTo(percentage / 100f, tween(1000, easing = FastOutSlowInEasing))
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val diameter = this.size.minDimension - strokePx
            val topLeft = Offset(strokePx / 2, strokePx / 2)

            // Background track
            drawArc(
                color = activeColor.copy(alpha = 0.15f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = androidx.compose.ui.geometry.Size(diameter, diameter),
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Active Arc
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(NeonPurple, ElectricCyan, NeonPurple)
                ),
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress.value,
                useCenter = false,
                topLeft = topLeft,
                size = androidx.compose.ui.geometry.Size(diameter, diameter),
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Confidence",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
