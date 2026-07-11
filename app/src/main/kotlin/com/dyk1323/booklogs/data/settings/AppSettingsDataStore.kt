package com.dyk1323.booklogs.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

data class AppSettings(
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 21,
    val reminderMinute: Int = 0,
    val dailyGoalPages: Int? = null,
)

/** docs/PLAN.md data/settings/AppSettingsDataStore — Room이 아닌 DataStore Preferences로 리마인더/일일 목표를 보관. */
class AppSettingsDataStore(context: Context) {

    private val appContext = context.applicationContext

    private object Keys {
        val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
        val DAILY_GOAL_PAGES = intPreferencesKey("daily_goal_pages")
    }

    val settings: Flow<AppSettings> = appContext.settingsDataStore.data.map { prefs ->
        AppSettings(
            reminderEnabled = prefs[Keys.REMINDER_ENABLED] ?: false,
            reminderHour = prefs[Keys.REMINDER_HOUR] ?: 21,
            reminderMinute = prefs[Keys.REMINDER_MINUTE] ?: 0,
            dailyGoalPages = prefs[Keys.DAILY_GOAL_PAGES],
        )
    }

    suspend fun setReminder(enabled: Boolean, hour: Int, minute: Int) {
        appContext.settingsDataStore.edit { prefs ->
            prefs[Keys.REMINDER_ENABLED] = enabled
            prefs[Keys.REMINDER_HOUR] = hour
            prefs[Keys.REMINDER_MINUTE] = minute
        }
    }

    suspend fun setDailyGoalPages(pages: Int?) {
        appContext.settingsDataStore.edit { prefs ->
            if (pages == null) {
                prefs.remove(Keys.DAILY_GOAL_PAGES)
            } else {
                prefs[Keys.DAILY_GOAL_PAGES] = pages
            }
        }
    }
}
