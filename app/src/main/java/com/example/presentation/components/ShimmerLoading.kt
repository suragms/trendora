package com.example.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * A shimmer loading placeholder effect for skeleton loading states.
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    baseColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    shimmerColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
) {
    val shimmerColors = listOf(
        baseColor,
        shimmerColor,
        baseColor
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 200f, translateAnim - 200f),
        end = Offset(translateAnim, translateAnim)
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(brush)
    )
}

/**
 * Shimmer loading skeleton for a single trending card.
 */
@Composable
fun TrendCardShimmer(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .width(280.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Category tag + bookmark
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ShimmerBox(modifier = Modifier.size(80.dp, 24.dp))
                ShimmerBox(modifier = Modifier.size(32.dp))
            }

            // Title
            ShimmerBox(modifier = Modifier.fillMaxWidth().height(24.dp))

            // Sparkline
            ShimmerBox(modifier = Modifier.fillMaxWidth().height(48.dp))

            // Score + Growth
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ShimmerBox(modifier = Modifier.size(120.dp, 28.dp))
                ShimmerBox(modifier = Modifier.size(60.dp, 20.dp))
            }

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ShimmerBox(modifier = Modifier.size(100.dp, 16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ShimmerBox(modifier = Modifier.size(32.dp, 16.dp))
                    ShimmerBox(modifier = Modifier.size(32.dp, 16.dp))
                    ShimmerBox(modifier = Modifier.size(32.dp, 16.dp))
                }
            }
        }
    }
}

/**
 * Shimmer loading skeleton for a vertical trending list item.
 */
@Composable
fun TrendListItemShimmer(modifier: Modifier = Modifier) {
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
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Emoji box
            ShimmerBox(modifier = Modifier.size(44.dp))

            // Text content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ShimmerBox(modifier = Modifier.size(140.dp, 12.dp))
                ShimmerBox(modifier = Modifier.fillMaxWidth(0.7f).height(16.dp))
                ShimmerBox(modifier = Modifier.size(160.dp, 12.dp))
            }

            // Score + Growth
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.End
            ) {
                ShimmerBox(modifier = Modifier.size(80.dp, 24.dp))
                ShimmerBox(modifier = Modifier.size(50.dp, 14.dp))
            }
        }
    }
}

/**
 * A full shimmer loading screen for the Home screen.
 */
@Composable
fun HomeScreenShimmer(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header shimmer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ShimmerBox(modifier = Modifier.size(44.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ShimmerBox(modifier = Modifier.size(100.dp, 10.dp))
                    ShimmerBox(modifier = Modifier.size(180.dp, 22.dp))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ShimmerBox(modifier = Modifier.size(44.dp))
                ShimmerBox(modifier = Modifier.size(44.dp))
            }
        }

        // Search bar shimmer
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(52.dp))

        // Category chips shimmer
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(4) {
                ShimmerBox(modifier = Modifier.size(80.dp, 36.dp))
            }
        }

        // Trending cards shimmer
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            repeat(3) {
                TrendCardShimmer()
            }
        }

        // List items shimmer
        repeat(3) {
            TrendListItemShimmer()
        }
    }
}
