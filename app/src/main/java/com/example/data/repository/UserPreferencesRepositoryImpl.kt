package com.example.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.local.AppDatabase
import com.example.data.local.NotificationEntity
import com.example.domain.model.Country
import com.example.domain.model.NotificationItem
import com.example.domain.model.NotificationType
import com.example.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "trendora_preferences")

class UserPreferencesRepositoryImpl(
    private val context: Context,
    private val database: AppDatabase
) : UserPreferencesRepository {

    private val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
    private val KEY_COUNTRY = stringPreferencesKey("selected_country")

    private val KEY_NOTIF_BREAKING = booleanPreferencesKey("notif_breaking")
    private val KEY_NOTIF_AI = booleanPreferencesKey("notif_ai")
    private val KEY_NOTIF_TECH = booleanPreferencesKey("notif_tech")
    private val KEY_NOTIF_ENTERTAINMENT = booleanPreferencesKey("notif_entertainment")
    private val KEY_NOTIF_DAILY = booleanPreferencesKey("notif_daily")

    override val isDarkMode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_DARK_MODE] ?: true // Default dark mode
    }

    override val selectedCountry: Flow<Country> = context.dataStore.data.map { prefs ->
        val code = prefs[KEY_COUNTRY] ?: Country.GLOBAL.name
        try { Country.valueOf(code) } catch (e: Exception) { Country.GLOBAL }
    }

    override suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DARK_MODE] = enabled
        }
    }

    override suspend fun setSelectedCountry(country: Country) {
        context.dataStore.edit { prefs ->
            prefs[KEY_COUNTRY] = country.name
        }
    }

    override suspend fun updateNotificationSetting(key: String, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            when (key) {
                "Breaking Trends" -> prefs[KEY_NOTIF_BREAKING] = enabled
                "AI Trends & Predictions" -> prefs[KEY_NOTIF_AI] = enabled
                "Technology & Startups" -> prefs[KEY_NOTIF_TECH] = enabled
                "Entertainment & Gaming" -> prefs[KEY_NOTIF_ENTERTAINMENT] = enabled
                "Daily Intelligence Brief" -> prefs[KEY_NOTIF_DAILY] = enabled
            }
        }
    }

    override val notificationSettings: Flow<Map<String, Boolean>> = context.dataStore.data.map { prefs ->
        mapOf(
            "Breaking Trends" to (prefs[KEY_NOTIF_BREAKING] ?: true),
            "AI Trends & Predictions" to (prefs[KEY_NOTIF_AI] ?: true),
            "Technology & Startups" to (prefs[KEY_NOTIF_TECH] ?: true),
            "Entertainment & Gaming" to (prefs[KEY_NOTIF_ENTERTAINMENT] ?: true),
            "Daily Intelligence Brief" to (prefs[KEY_NOTIF_DAILY] ?: true)
        )
    }

    override fun getNotifications(): Flow<List<NotificationItem>> {
        return database.trendDao().getNotifications().map { entities ->
            if (entities.isEmpty()) {
                getSeedNotifications()
            } else {
                entities.map { entity ->
                    NotificationItem(
                        id = entity.id,
                        title = entity.title,
                        message = entity.message,
                        type = try { NotificationType.valueOf(entity.type) } catch (e: Exception) { NotificationType.GENERAL },
                        timeAgo = entity.timeAgo,
                        timestamp = entity.timestamp,
                        isRead = entity.isRead,
                        trendId = entity.trendId
                    )
                }
            }
        }
    }

    override suspend fun addNotification(notification: NotificationItem) {
        database.trendDao().insertNotification(
            NotificationEntity(
                id = notification.id,
                title = notification.title,
                message = notification.message,
                type = notification.type.name,
                timeAgo = notification.timeAgo,
                timestamp = notification.timestamp,
                isRead = notification.isRead,
                trendId = notification.trendId
            )
        )
    }

    override suspend fun markNotificationAsRead(id: String) {
        database.trendDao().markNotificationAsRead(id)
    }

    override suspend fun clearAllNotifications() {
        database.trendDao().clearAllNotifications()
    }

    private fun getSeedNotifications(): List<NotificationItem> {
        return listOf(
            NotificationItem(
                id = "n_seed_1",
                title = "🔥 AI is rapidly trending right now!",
                message = "Artificial Intelligence spiked +142% with +185K discussions in the last 4 hours.",
                type = NotificationType.AI_ALERT,
                timeAgo = "10m ago",
                isRead = false,
                trendId = "trend_ai_gen"
            ),
            NotificationItem(
                id = "n_seed_2",
                title = "📈 Topic Growth Alert",
                message = "Your followed topic 'Technology & Startups' increased by 45% today.",
                type = NotificationType.TOPIC_GROWTH,
                timeAgo = "1h ago",
                isRead = false,
                trendId = "trend_kerala_tech"
            ),
            NotificationItem(
                id = "n_seed_3",
                title = "⚡ Breaking Trend Detected",
                message = "Clean Fusion Energy achieved sustained net gain milestones with global viral coverage.",
                type = NotificationType.BREAKING,
                timeAgo = "3h ago",
                isRead = true,
                trendId = "trend_fusion_energy"
            ),
            NotificationItem(
                id = "n_seed_4",
                title = "📋 Daily Intelligence Brief",
                message = "Your morning digest is ready: 8 top viral trends across Tech, AI, and Gaming.",
                type = NotificationType.DAILY_BRIEF,
                timeAgo = "6h ago",
                isRead = true
            )
        )
    }
}
