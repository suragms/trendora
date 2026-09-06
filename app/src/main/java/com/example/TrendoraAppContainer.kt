package com.example

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.core.network.GeminiApiClient
import com.example.core.network.NetworkMonitor
import com.example.data.local.AppDatabase
import com.example.data.remote.CachedTrendDataSource
import com.example.data.remote.FallbackTrendDataSource
import com.example.data.remote.MockTrendDataSource
import com.example.data.remote.RemoteTrendDataSource
import com.example.data.repository.AIRepositoryImpl
import com.example.data.repository.NewsRepositoryImpl
import com.example.data.repository.TrendRepositoryImpl
import com.example.data.repository.UserPreferencesRepositoryImpl
import com.example.domain.repository.AIRepository
import com.example.domain.repository.NewsRepository
import com.example.domain.repository.TrendRepository
import com.example.domain.repository.UserPreferencesRepository
import com.example.presentation.ai.AITrendsViewModel
import com.example.presentation.detail.TrendDetailViewModel
import com.example.presentation.explore.ExploreViewModel
import com.example.presentation.home.HomeViewModel
import com.example.presentation.saved.SavedViewModel
import com.example.presentation.settings.SettingsViewModel

class TrendoraAppContainer(context: Context) {
    val database: AppDatabase = AppDatabase.getDatabase(context)
    val geminiApiClient: GeminiApiClient = GeminiApiClient()
    val networkMonitor: NetworkMonitor = NetworkMonitor(context)

    // ---- Real news data-source chain (Remote -> Cached -> Mock) ----
    private val remoteTrendDataSource = RemoteTrendDataSource(networkMonitor = networkMonitor)
    private val cachedTrendDataSource = CachedTrendDataSource(database)
    private val mockTrendDataSource = MockTrendDataSource()
    private val fallbackTrendDataSource = FallbackTrendDataSource(
        remote = remoteTrendDataSource,
        cached = cachedTrendDataSource,
        mock = mockTrendDataSource,
        networkMonitor = networkMonitor
    )

    val trendRepository: TrendRepository =
        TrendRepositoryImpl(fallbackTrendDataSource, networkMonitor, database)
    val newsRepository: NewsRepository =
        NewsRepositoryImpl(fallbackTrendDataSource, networkMonitor, database)
    val aiRepository: AIRepository = AIRepositoryImpl(geminiApiClient, database, networkMonitor)
    val userPreferencesRepository: UserPreferencesRepository = UserPreferencesRepositoryImpl(context, database)

    val viewModelFactory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return when {
                modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                    HomeViewModel(trendRepository, newsRepository, userPreferencesRepository) as T
                }
                modelClass.isAssignableFrom(ExploreViewModel::class.java) -> {
                    ExploreViewModel(trendRepository) as T
                }
                modelClass.isAssignableFrom(AITrendsViewModel::class.java) -> {
                    AITrendsViewModel(aiRepository) as T
                }
                modelClass.isAssignableFrom(SavedViewModel::class.java) -> {
                    SavedViewModel(trendRepository, newsRepository) as T
                }
                modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                    SettingsViewModel(userPreferencesRepository, trendRepository) as T
                }
                modelClass.isAssignableFrom(TrendDetailViewModel::class.java) -> {
                    TrendDetailViewModel(trendRepository, aiRepository) as T
                }
                else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}
