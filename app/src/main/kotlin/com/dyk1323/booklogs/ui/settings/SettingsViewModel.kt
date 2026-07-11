package com.dyk1323.booklogs.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dyk1323.booklogs.data.backup.BackupExporter
import com.dyk1323.booklogs.data.backup.BackupImporter
import com.dyk1323.booklogs.data.settings.AppSettingsDataStore
import com.dyk1323.booklogs.data.settings.ThemeMode
import com.dyk1323.booklogs.notification.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 21,
    val reminderMinute: Int = 0,
    val dailyGoalPagesText: String = "",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isExportingBackup: Boolean = false,
    val isImportingBackup: Boolean = false,
    val backupMessage: String? = null,
    val isLoading: Boolean = true,
)

private data class BackupUiState(
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val message: String? = null,
)

/** docs/PLAN.md 화면 흐름 #9 설정 — 리마인더 on/off+시각, 일일 목표 페이지 수, 화면 테마, 백업/복원. */
class SettingsViewModel(
    private val appSettingsDataStore: AppSettingsDataStore,
    private val reminderScheduler: ReminderScheduler,
    private val backupExporter: BackupExporter,
    private val backupImporter: BackupImporter,
) : ViewModel() {

    private val dailyGoalDraft = MutableStateFlow<String?>(null)
    private val backupUiState = MutableStateFlow(BackupUiState())

    val uiState: StateFlow<SettingsUiState> = combine(
        appSettingsDataStore.settings,
        dailyGoalDraft,
        backupUiState,
    ) { settings, draft, backup ->
        SettingsUiState(
            reminderEnabled = settings.reminderEnabled,
            reminderHour = settings.reminderHour,
            reminderMinute = settings.reminderMinute,
            dailyGoalPagesText = draft ?: (settings.dailyGoalPages?.toString() ?: ""),
            themeMode = settings.themeMode,
            isExportingBackup = backup.isExporting,
            isImportingBackup = backup.isImporting,
            backupMessage = backup.message,
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

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            appSettingsDataStore.setThemeMode(mode)
        }
    }

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            backupUiState.update { it.copy(isExporting = true, message = null) }
            runCatching { backupExporter.exportTo(uri) }
                .onSuccess {
                    backupUiState.update { it.copy(isExporting = false, message = "백업 파일을 저장했어요.") }
                }
                .onFailure { error ->
                    backupUiState.update {
                        it.copy(isExporting = false, message = "백업 저장에 실패했어요. ${error.message ?: ""}".trim())
                    }
                }
        }
    }

    /** UI must show the "이 작업은 현재 데이터를 전부 대체합니다" blocking confirm before calling this. */
    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            backupUiState.update { it.copy(isImporting = true, message = null) }
            runCatching { backupImporter.importFrom(uri) }
                .onSuccess {
                    backupUiState.update { it.copy(isImporting = false, message = "백업을 가져왔어요.") }
                }
                .onFailure { error ->
                    backupUiState.update {
                        it.copy(isImporting = false, message = "가져오기에 실패했어요. ${error.message ?: ""}".trim())
                    }
                }
        }
    }
}
