package com.example.presentation.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.BreakingNewsItem
import com.example.domain.model.TrendItem
import com.example.domain.repository.NewsRepository
import com.example.domain.repository.TrendRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class SavedTab(val title: String) {
    TRENDS("Saved Trends"),
    ARTICLES("Saved Articles"),
    RECENT("Recently Viewed")
}

data class SavedUiState(
    val selectedTab: SavedTab = SavedTab.TRENDS,
    val savedTrends: List<TrendItem> = emptyList(),
    val savedArticles: List<BreakingNewsItem> = emptyList(),
    val recentlyViewed: List<TrendItem> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false
)

class SavedViewModel(
    private val trendRepository: TrendRepository,
    private val newsRepository: NewsRepository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(SavedTab.TRENDS)
    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<SavedUiState> = combine(
        _selectedTab,
        _searchQuery,
        trendRepository.getSavedTrends(),
        newsRepository.getSavedArticles(),
        trendRepository.getRecentlyViewedTrends()
    ) { tab, query, trends, articles, recentlyViewed ->
        val filteredTrends = if (query.isBlank()) trends else trends.filter { it.title.contains(query, ignoreCase = true) }
        val filteredArticles = if (query.isBlank()) articles else articles.filter { it.headline.contains(query, ignoreCase = true) }
        val filteredRecent = if (query.isBlank()) recentlyViewed else recentlyViewed.filter { it.title.contains(query, ignoreCase = true) }

        SavedUiState(
            selectedTab = tab,
            searchQuery = query,
            savedTrends = filteredTrends,
            savedArticles = filteredArticles,
            recentlyViewed = filteredRecent,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SavedUiState(isLoading = true)
    )

    fun onTabSelected(tab: SavedTab) {
        _selectedTab.value = tab
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun removeSavedTrend(trend: TrendItem) {
        viewModelScope.launch {
            trendRepository.toggleSaveTrend(trend)
        }
    }

    fun removeSavedArticle(article: BreakingNewsItem) {
        viewModelScope.launch {
            newsRepository.toggleSaveArticle(article)
        }
    }
}
