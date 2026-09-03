package com.example.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Country
import com.example.domain.model.UserStats
import com.example.domain.repository.TrendRepository
import com.example.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProfileUiState(
    val userStats: UserStats = UserStats(),
    val isDarkMode: Boolean = true,
    val selectedCountry: Country = Country.GLOBAL,
    val notificationSettings: Map<String, Boolean> = emptyMap(),
    val showAboutDialog: Boolean = false,
    val cacheClearedMessage: String? = null
)

private data class ProfilePreferencesState(
    val isDarkMode: Boolean,
    val selectedCountry: Country,
    val notificationSettings: Map<String, Boolean>
)

private data class ProfileDialogState(
    val showAboutDialog: Boolean,
    val cacheClearedMessage: String?
)

class ProfileViewModel(
    private val preferencesRepository: UserPreferencesRepository,
    private val trendRepository: TrendRepository
) : ViewModel() {

    private val _showAboutDialog = MutableStateFlow(false)
    private val _cacheClearedMessage = MutableStateFlow<String?>(null)

    private val preferencesFlow = combine(
        preferencesRepository.isDarkMode,
        preferencesRepository.selectedCountry,
        preferencesRepository.notificationSettings
    ) { darkMode, country, notifSettings ->
        ProfilePreferencesState(darkMode, country, notifSettings)
    }

    private val dialogFlow = combine(
        _showAboutDialog,
        _cacheClearedMessage
    ) { aboutDialog, cacheMsg ->
        ProfileDialogState(aboutDialog, cacheMsg)
    }

    val uiState: StateFlow<ProfileUiState> = combine(
        preferencesRepository.userStats,
        preferencesFlow,
        dialogFlow
    ) { stats, prefs, dialogs ->
        ProfileUiState(
            userStats = stats,
            isDarkMode = prefs.isDarkMode,
            selectedCountry = prefs.selectedCountry,
            notificationSettings = prefs.notificationSettings,
            showAboutDialog = dialogs.showAboutDialog,
            cacheClearedMessage = dialogs.cacheClearedMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileUiState()
    )

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setDarkMode(enabled)
        }
    }

    fun setSelectedCountry(country: Country) {
        viewModelScope.launch {
            preferencesRepository.setSelectedCountry(country)
        }
    }

    fun toggleNotificationSetting(key: String, enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateNotificationSetting(key, enabled)
        }
    }

    fun clearCacheAndHistory() {
        viewModelScope.launch {
            trendRepository.clearRecentSearches()
            _cacheClearedMessage.value = "Cache and recent searches cleared successfully!"
            kotlinx.coroutines.delay(3000)
            _cacheClearedMessage.value = null
        }
    }

    fun setAboutDialogVisible(visible: Boolean) {
        _showAboutDialog.value = visible
    }
}
