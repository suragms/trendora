package com.example.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.TrendoraAppContainer
import com.example.presentation.ai.AITrendsScreen
import com.example.presentation.ai.AITrendsViewModel
import com.example.presentation.detail.TrendDetailScreen
import com.example.presentation.detail.TrendDetailViewModel
import com.example.presentation.explore.ExploreScreen
import com.example.presentation.explore.ExploreViewModel
import com.example.presentation.home.HomeScreen
import com.example.presentation.home.HomeViewModel
import com.example.presentation.navigation.FuturisticBottomBar
import com.example.presentation.navigation.Screen
import com.example.presentation.profile.ProfileScreen
import com.example.presentation.profile.ProfileViewModel
import com.example.presentation.saved.SavedScreen
import com.example.presentation.saved.SavedViewModel
import com.example.ui.theme.TrendoraTheme

@Composable
fun TrendoraApp(
    container: TrendoraAppContainer
) {
    val isDarkMode by container.userPreferencesRepository.isDarkMode.collectAsStateWithLifecycle(initialValue = true)
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    TrendoraTheme(darkTheme = isDarkMode) {
        val showBottomBar = currentRoute in listOf(
            Screen.Home.route,
            Screen.Explore.route,
            Screen.AITrends.route,
            Screen.Saved.route,
            Screen.Profile.route
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (showBottomBar) {
                    FuturisticBottomBar(
                        currentRoute = currentRoute,
                        onNavigateToRoute = { route ->
                            navController.navigate(route) {
                                popUpTo(Screen.Home.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                enterTransition = { fadeIn(animationSpec = tween(250)) },
                exitTransition = { fadeOut(animationSpec = tween(200)) }
            ) {
                composable(Screen.Home.route) {
                    val homeViewModel: HomeViewModel = viewModel(factory = container.viewModelFactory)
                    val isConnected by container.networkMonitor.isConnected
                        .collectAsStateWithLifecycle(initialValue = true)
                    LaunchedEffect(isConnected) {
                        homeViewModel.setOffline(!isConnected)
                    }
                    HomeScreen(
                        viewModel = homeViewModel,
                        onNavigateToTrend = { trendId ->
                            navController.navigate(Screen.TrendDetails.createRoute(trendId))
                        },
                        onNavigateToProfile = {
                            navController.navigate(Screen.Profile.route)
                        }
                    )
                }

                composable(Screen.Explore.route) {
                    val exploreViewModel: ExploreViewModel = viewModel(factory = container.viewModelFactory)
                    ExploreScreen(
                        viewModel = exploreViewModel,
                        onNavigateToTrend = { trendId ->
                            navController.navigate(Screen.TrendDetails.createRoute(trendId))
                        }
                    )
                }

                composable(Screen.AITrends.route) {
                    val aiTrendsViewModel: AITrendsViewModel = viewModel(factory = container.viewModelFactory)
                    AITrendsScreen(
                        viewModel = aiTrendsViewModel,
                        onNavigateToTrend = { trendId ->
                            navController.navigate(Screen.TrendDetails.createRoute(trendId))
                        }
                    )
                }

                composable(Screen.Saved.route) {
                    val savedViewModel: SavedViewModel = viewModel(factory = container.viewModelFactory)
                    SavedScreen(
                        viewModel = savedViewModel,
                        onNavigateToTrend = { trendId ->
                            navController.navigate(Screen.TrendDetails.createRoute(trendId))
                        },
                        onNavigateToExplore = {
                            navController.navigate(Screen.Explore.route)
                        }
                    )
                }

                composable(Screen.Profile.route) {
                    val profileViewModel: ProfileViewModel = viewModel(factory = container.viewModelFactory)
                    ProfileScreen(
                        viewModel = profileViewModel
                    )
                }

                composable(
                    route = Screen.TrendDetails.route,
                    arguments = listOf(
                        navArgument("trendId") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val trendId = backStackEntry.arguments?.getString("trendId") ?: ""
                    val trendDetailViewModel: TrendDetailViewModel = viewModel(factory = container.viewModelFactory)
                    TrendDetailScreen(
                        trendId = trendId,
                        viewModel = trendDetailViewModel,
                        onBackClick = { navController.popBackStack() },
                        onTopicClick = { topic ->
                            navController.navigate(Screen.Explore.route)
                        }
                    )
                }
            }
        }
    }
}
