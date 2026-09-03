package com.example.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.*
import com.example.domain.repository.NewsRepository
import com.example.domain.repository.TrendRepository
import com.example.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val trends: List<TrendItem> = emptyList(),
    val breakingNews: List<BreakingNewsItem> = emptyList(),
    val analytics: TrendAnalytics? = null,
    val selectedCategory: TrendCategory = TrendCategory.ALL,
    val searchQuery: String = "",
    val recentSearches: List<String> = emptyList(),
    val trendingSuggestions: List<String> = emptyList(),
    val unreadNotificationsCount: Int = 2,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isVoiceListening: Boolean = false,
    val showVoiceDialog: Boolean = false,
    val showNotificationsDialog: Boolean = false,
    val isOffline: Boolean = false,
    val errorMessage: String? = null,
    val isShowingCached: Boolean = false,
    val isShowingMock: Boolean = false,
    val userGreeting: String = "Good Morning, Surag 👋"
)

private data class HomeContentData(
    val trends: List<TrendItem>,
    val breakingNews: List<BreakingNewsItem>,
    val analytics: TrendAnalytics,
    val recentSearches: List<String>,
    val unreadNotifsCount: Int
)

private data class HomeFilterState(
    val selectedCategory: TrendCategory,
    val searchQuery: String,
    val isRefreshing: Boolean,
    val showVoiceDialog: Boolean,
    val showNotificationsDialog: Boolean
)

private data class HomeFlags(
    val isOffline: Boolean,
    val errorMessage: String?,
    val isShowingCached: Boolean,
    val isShowingMock: Boolean
)

class HomeViewModel(
    private val trendRepository: TrendRepository,
    private val newsRepository: NewsRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow(TrendCategory.ALL)
    private val _searchQuery = MutableStateFlow("")
    private val _isRefreshing = MutableStateFlow(false)
    private val _showVoiceDialog = MutableStateFlow(false)
    private val _showNotificationsDialog = MutableStateFlow(false)
    private val _isLoading = MutableStateFlow(true)
    private val _isOffline = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _isShowingCached = MutableStateFlow(false)
    private val _isShowingMock = MutableStateFlow(false)

    private val contentDataFlow: Flow<HomeContentData> = _selectedCategory.flatMapLatest { cat ->
        combine(
            trendRepository.getTrendingNow(cat),
            newsRepository.getBreakingNews(cat),
            trendRepository.getTrendAnalytics(),
            trendRepository.getRecentSearches(),
            preferencesRepository.getNotifications()
        ) { trends, news, analytics, recent, notifs ->
            HomeContentData(
                trends = trends,
                breakingNews = news,
                analytics = analytics,
                recentSearches = recent,
                unreadNotifsCount = notifs.count { !it.isRead }
            )
        }
    }

    private val filterStateFlow: Flow<HomeFilterState> = combine(
        _selectedCategory,
        _searchQuery,
        _isRefreshing,
        _showVoiceDialog,
        _showNotificationsDialog
    ) { cat, query, refreshing, voiceDialog, notifDialog ->
        HomeFilterState(cat, query, refreshing, voiceDialog, notifDialog)
    }

    // Fold the two flag flows into one so the final combine stays at 5 named params.
    private val flagsFlow: Flow<HomeFlags> = combine(
        _isOffline, _errorMessage, _isShowingCached, _isShowingMock
    ) { isOffline, errorMessage, showingCached, showingMock ->
        HomeFlags(isOffline, errorMessage, showingCached, showingMock)
    }

    val uiState: StateFlow<HomeUiState> = combine(
        contentDataFlow,
        filterStateFlow,
        _isLoading,
        flagsFlow
    ) { content, filter, isLoading, flags ->
        val filteredTrends = if (filter.searchQuery.isBlank()) {
            content.trends
        } else {
            content.trends.filter {
                it.title.contains(filter.searchQuery, ignoreCase = true) ||
                        it.aiAnalysis.relatedTopics.any { t -> t.contains(filter.searchQuery, ignoreCase = true) }
            }
        }

        HomeUiState(
            trends = filteredTrends,
            breakingNews = content.breakingNews,
            analytics = content.analytics,
            selectedCategory = filter.selectedCategory,
            searchQuery = filter.searchQuery,
            recentSearches = content.recentSearches,
            trendingSuggestions = trendRepository.getTrendingSearchSuggestions(),
            unreadNotificationsCount = content.unreadNotifsCount,
            isLoading = isLoading,
            isRefreshing = filter.isRefreshing,
            isOffline = flags.isOffline,
            errorMessage = flags.errorMessage,
            isShowingCached = flags.isShowingCached,
            isShowingMock = flags.isShowingMock,
            showVoiceDialog = filter.showVoiceDialog,
            showNotificationsDialog = filter.showNotificationsDialog
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    init {
        // Simulate an initial load so the shimmer skeleton is visible briefly.
        viewModelScope.launch {
            delay(900)
            _isLoading.value = false
        }
        // Surface real offline/error state from the data layer (rate limits,
        // missing key, server errors) with user-friendly messages. Also track
        // whether we are showing cached or mock data so the banner is accurate.
        viewModelScope.launch {
            newsRepository.getLoadState().collect { state ->
                _isOffline.value = state.isOffline
                _isShowingCached.value = state.fromCache
                _isShowingMock.value = state.fromMock
                if (state.errorMessage != null) _errorMessage.value = state.errorMessage
            }
        }
    }

    fun onCategorySelected(category: TrendCategory) {
        _selectedCategory.value = category
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onSearchSubmitted(query: String) {
        if (query.isNotBlank()) {
            viewModelScope.launch {
                trendRepository.addRecentSearch(query)
            }
        }
    }

    fun toggleSaveTrend(trend: TrendItem) {
        viewModelScope.launch {
            trendRepository.toggleSaveTrend(trend)
        }
    }

    fun toggleSaveArticle(article: BreakingNewsItem) {
        viewModelScope.launch {
            newsRepository.toggleSaveArticle(article)
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            trendRepository.refreshTrends()
            kotlinx.coroutines.delay(600)
            _isRefreshing.value = false
        }
    }

    /** Updates the offline state surfaced to the UI (e.g. from NetworkMonitor). */
    fun setOffline(offline: Boolean) {
        _isOffline.value = offline
    }

    /** Clears a transient error message shown in the UI. */
    fun clearError() {
        _errorMessage.value = null
    }

    /** Simulates a load failure so the retry/error state can be exercised (for testing). */
    fun triggerSimulatedLoadError() {
        _errorMessage.value = "Something went wrong while loading your trends."
    }

    fun setVoiceDialogVisible(visible: Boolean) {
        _showVoiceDialog.value = visible
    }

    fun setNotificationsDialogVisible(visible: Boolean) {
        _showNotificationsDialog.value = visible
    }

    fun triggerSimulatedTrendAlert() {
        viewModelScope.launch {
            preferencesRepository.addNotification(
                NotificationItem(
                    id = "alert_${System.currentTimeMillis()}",
                    title = "⚡ Live Viral Spike!",
                    message = "Autonomous AI Multi-Agents surged +180% in the last 15 minutes.",
                    type = NotificationType.BREAKING,
                    timeAgo = "Just now",
                    timestamp = System.currentTimeMillis(),
                    trendId = "trend_ai_gen"
                )
            )
        }
    }
}
