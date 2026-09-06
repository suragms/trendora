package com.example.presentation.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonPurple

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Explore : Screen("explore")
    object AITrends : Screen("ai_trends")
    object Saved : Screen("saved")
    object Settings : Screen("settings")
    object TrendDetails : Screen("trend_details/{trendId}") {
        fun createRoute(trendId: String) = "trend_details/$trendId"
    }
}

enum class NavigationItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("home", "Home", Icons.Filled.Whatshot, Icons.Outlined.Whatshot),
    EXPLORE("explore", "Explore", Icons.Filled.Explore, Icons.Outlined.Explore),
    AI("ai_trends", "AI Trends", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    SAVED("saved", "Saved", Icons.Filled.Bookmark, Icons.Outlined.BookmarkBorder)
}

@Composable
fun FuturisticBottomBar(
    currentRoute: String?,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.98f),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavigationItem.values().forEach { item ->
                val isSelected = currentRoute == item.route
                val isCenterAI = item == NavigationItem.AI

                if (isCenterAI) {
                    // Center Floating AI Button
                    Box(
                        modifier = Modifier
                            .offset(y = (-6).dp)
                            .size(54.dp)
                            .shadow(12.dp, shape = CircleShape, ambientColor = NeonPurple, spotColor = ElectricCyan)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color(0xFF9333EA), Color(0xFF22D3EE))))
                            .clickable {
                                if (currentRoute != item.route) {
                                    onNavigateToRoute(item.route)
                                }
                            }
                            .testTag("nav_tab_${item.route}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = item.title,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                } else {
                    val iconColor by animateColorAsState(
                        targetValue = if (isSelected) ElectricCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "iconColor"
                    )

                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                if (currentRoute != item.route) {
                                    onNavigateToRoute(item.route)
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("nav_tab_${item.route}"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.title,
                            tint = iconColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = iconColor,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
