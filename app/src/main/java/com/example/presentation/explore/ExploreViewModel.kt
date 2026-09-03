package com.example.presentation.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.*
import com.example.domain.repository.TrendRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class SortOption(val displayName: String) {
    TREND_SCORE("Highest Score"),
    GROWTH_VELOCITY("Growth Velocity"),
    DISCUSSIONS("Most Discussed"),
    RECENT("Most Recent")
}

data class ExploreUiState(
    val trends: List<TrendItem> = emptyList(),
    val selectedCategory: TrendCategory = TrendCategory.ALL,
    val selectedCountry: Country = Country.GLOBAL,
    val selectedTimeFilter: TimeFilter = TimeFilter.TODAY,
    val selectedSort: SortOption = SortOption.TREND_SCORE,
    val searchQuery: String = "",
    val isGridView: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isOffline: Boolean = false,
    val isShowingMock: Boolean = false
)

private data class FilterConfig(
    val category: TrendCategory,
    val country: Country,
    val timeFilter: TimeFilter
)

private data class ViewConfig(
    val sort: SortOption,
    val query: String,
    val isGridView: Boolean
)

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class ExploreViewModel(
    private val trendRepository: TrendRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow(TrendCategory.ALL)
    private val _selectedCountry = MutableStateFlow(Country.GLOBAL)
    private val _selectedTimeFilter = MutableStateFlow(TimeFilter.TODAY)
    private val _selectedSort = MutableStateFlow(SortOption.TREND_SCORE)

    // Debounced search: only queries the API after the user pauses typing.
    private val _searchQuery = MutableStateFlow("")
    private val debouncedQuery = _searchQuery
        .debounce(400)
        .distinctUntilChanged()

    private val _isGridView = MutableStateFlow(false)

    private val _isLoading = MutableStateFlow(true)
    private val _lastError = MutableStateFlow<String?>(null)
    private val _isOffline = MutableStateFlow(false)
    private val _isShowingMock = MutableStateFlow(false)

    private data class StatusState(
        val isLoading: Boolean,
        val errorMessage: String?,
        val isOffline: Boolean,
        val isShowingMock: Boolean
    )

    private val statusFlow = combine(_isLoading, _lastError, _isOffline, _isShowingMock) { loading, error, offline, mock ->
        StatusState(loading, error, offline, mock)
    }

    private val filterConfigFlow = combine(
        _selectedCategory,
        _selectedCountry,
        _selectedTimeFilter
    ) { cat, country, time ->
        FilterConfig(cat, country, time)
    }

    private val viewConfigFlow = combine(
        _selectedSort,
        debouncedQuery,
        _isGridView
    ) { sort, query, grid ->
        ViewConfig(sort, query, grid)
    }

    private val resultsFlow = combine(
        filterConfigFlow,
        viewConfigFlow
    ) { filter, view ->
        Pair(filter, view)
    }.flatMapLatest { (filter, view) ->
        trendRepository.getExploreTrends(
            query = view.query,
            category = filter.category,
            country = filter.country,
            timeFilter = filter.timeFilter
        )
            .onStart { _isLoading.value = true }
            .onEach { _isLoading.value = false }
            .map { list ->
                when (view.sort) {
                    SortOption.TREND_SCORE -> list.sortedByDescending { it.score.totalScore }
                    SortOption.GROWTH_VELOCITY -> list.sortedByDescending { it.growthPercentage }
                    SortOption.DISCUSSIONS -> list.sortedByDescending { it.discussionsCount }
                    SortOption.RECENT -> list
                }
            }
            .catch {
                _isLoading.value = false
                emit(emptyList())
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ExploreUiState> = combine(
        resultsFlow,
        filterConfigFlow,
        viewConfigFlow,
        statusFlow
    ) { results, filter, view, status ->
        ExploreUiState(
            trends = results,
            selectedCategory = filter.category,
            selectedCountry = filter.country,
            selectedTimeFilter = filter.timeFilter,
            selectedSort = view.sort,
            searchQuery = _searchQuery.value,
            isGridView = view.isGridView,
            isLoading = status.isLoading,
            errorMessage = status.errorMessage,
            isOffline = status.isOffline,
            isShowingMock = status.isShowingMock
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExploreUiState(isLoading = true)
    )

    init {
        // Track real offline/error state from the data layer.
        viewModelScope.launch {
            trendRepository.getLoadState().collect { state ->
                _isOffline.value = state.isOffline
                _isShowingMock.value = state.fromMock
                if (state.errorMessage != null) _lastError.value = state.errorMessage
                else if (!state.isOffline) _lastError.value = null
            }
        }
    }

    fun onCategorySelected(category: TrendCategory) {
        _selectedCategory.value = category
    }

    fun onCountrySelected(country: Country) {
        _selectedCountry.value = country
    }

    fun onTimeFilterSelected(timeFilter: TimeFilter) {
        _selectedTimeFilter.value = timeFilter
    }

    fun onSortOptionSelected(sort: SortOption) {
        _selectedSort.value = sort
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun clearError() {
        _lastError.value = null
    }

    fun toggleGridView() {
        _isGridView.value = !_isGridView.value
    }

    fun toggleSaveTrend(trend: TrendItem) {
        viewModelScope.launch {
            trendRepository.toggleSaveTrend(trend)
        }
    }
}
