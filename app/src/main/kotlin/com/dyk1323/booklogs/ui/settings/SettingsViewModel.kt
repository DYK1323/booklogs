package com.dyk1323.booklogs.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dyk1323.booklogs.data.settings.AppSettingsDataStore
import com.dyk1323.booklogs.notification.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 21,
    val reminderMinute: Int = 0,
    val dailyGoalPagesText: String = "",
    val isLoading: Boolean = true,
)

/** docs/PLAN.md 화면 흐름 #9 설정 — 리마인더 on/off+시각, 일일 목표 페이지 수. */
class SettingsViewModel(
    private val appSettingsDataStore: AppSettingsDataStore,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {

    private val dailyGoalDraft = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        appSettingsDataStore.settings,
        dailyGoalDraft,
    ) { settings, draft ->
        SettingsUiState(
            reminderEnabled = settings.reminderEnabled,
            reminderHour = settings.reminderHour,
            reminderMinute = settings.reminderMinute,
            dailyGoalPagesText = draft ?: (settings.dailyGoalPages?.toString() ?: ""),
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(),
    )

    fun setReminderEnabled(enabled: Boolean) {
        val state = uiState.value
        viewModelScope.launch {
            appSettingsDataStore.setReminder(enabled, state.reminderHour, state.reminderMinute)
            if (enabled) {
                reminderScheduler.schedule(state.reminderHour, state.reminderMinute)
            } else {
                reminderScheduler.cancel()
            }
        }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        val state = uiState.value
        viewModelScope.launch {
            appSettingsDataStore.setReminder(state.reminderEnabled, hour, minute)
            if (state.reminderEnabled) {
                reminderScheduler.schedule(hour, minute)
            }
        }
    }

    fun updateDailyGoalPagesText(value: String) {
        dailyGoalDraft.value = value.filter(Char::isDigit).take(4)
    }

    fun saveDailyGoalPages() {
        val pages = uiState.value.dailyGoalPagesText.toIntOrNull()
        viewModelScope.launch {
            appSettingsDataStore.setDailyGoalPages(pages)
            dailyGoalDraft.value = null
        }
    }
}
