package com.dyk1323.booklogs.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.dyk1323.booklogs.data.settings.ThemeMode
import com.dyk1323.booklogs.ui.common.components.BooklogsFilledButton
import com.dyk1323.booklogs.ui.common.components.BooklogsScreenBackground
import com.dyk1323.booklogs.ui.common.components.BooklogsSegmentButton
import com.dyk1323.booklogs.ui.common.components.BooklogsSegmentRow
import com.dyk1323.booklogs.ui.common.components.BooklogsTopBar
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showTimePicker by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri -> uri?.let(viewModel::exportBackup) }
    val importPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri -> uri?.let { pendingImportUri = it } }

    Scaffold(
        containerColor = BooklogsScreenBackground,
        topBar = { BooklogsTopBar(title = "설정", onBack = onBack) },
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 36.dp),
            verticalArrangement = Arrangement.spacedBy(36.dp),
        ) {
            SettingsSection(title = "리마인더") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "매일 진행률 알림",
                            style = SettingsBodyTextStyle,
                        )
                        ReminderToggle(
                            checked = uiState.reminderEnabled,
                            onToggle = { enabled ->
                                if (
                                    enabled &&
                                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                                    PackageManager.PERMISSION_GRANTED
                                ) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                                viewModel.setReminderEnabled(enabled)
                            },
                        )
                    }
                    if (uiState.reminderEnabled) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(text = "알림 시각", style = SettingsBodyTextStyle)
                            Text(
                                text = formatReminderTime(uiState.reminderHour, uiState.reminderMinute),
                                modifier = Modifier.clickable { showTimePicker = true },
                                style = SettingsActionTextStyle.copy(fontWeight = FontWeight.Medium),
                                color = Color(0xFF0C7EFF),
                            )
                        }
                    }
                }
            }

            SettingsSection(title = "일일 목표") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        GoalInput(
                            value = uiState.dailyGoalPagesText,
                            onValueChange = viewModel::updateDailyGoalPagesText,
                            modifier = Modifier.weight(1f),
                        )
                        BooklogsFilledButton(
                            text = "저장",
                            onClick = viewModel::saveDailyGoalPages,
                            primary = true,
                            modifier = Modifier
                                .width(64.dp),
                            compact = true,
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp),
                    ) {
                        Text(
                            text = "비워두면 목표 없이 기록만 표시돼요.",
                            style = SettingsCaptionTextStyle,
                            color = Color(0xFF757575),
                        )
                    }
                }
            }

            SettingsSection(title = "화면 테마") {
                BooklogsSegmentRow(
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    BooklogsSegmentButton(
                        text = "시스템 설정",
                        selected = uiState.themeMode == ThemeMode.SYSTEM,
                        onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) },
                    )
                    BooklogsSegmentButton(
                        text = "라이트",
                        selected = uiState.themeMode == ThemeMode.LIGHT,
                        onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) },
                    )
                    BooklogsSegmentButton(
                        text = "다크",
                        selected = uiState.themeMode == ThemeMode.DARK,
                        onClick = { viewModel.setThemeMode(ThemeMode.DARK) },
                    )
                }
            }

            SettingsSection(title = "데이터 백업") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BooklogsFilledButton(
                        text = if (uiState.isExportingBackup) "데이터 내보내는 중" else "데이터 내보내기",
                        onClick = { exportLauncher.launch(defaultBackupFileName()) },
                        modifier = Modifier
                            .weight(1f),
                        compact = true,
                    )
                    BooklogsFilledButton(
                        text = if (uiState.isImportingBackup) "데이터 가져오는 중" else "데이터 가져오기",
                        onClick = { importPickerLauncher.launch(arrayOf("*/*")) },
                        primary = true,
                        modifier = Modifier
                            .weight(1f),
                        compact = true,
                    )
                }
            }
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = uiState.reminderHour,
            initialMinute = uiState.reminderMinute,
            is24Hour = false,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.setReminderTime(timePickerState.hour, timePickerState.minute)
                        showTimePicker = false
                    },
                ) { Text(text = "확인") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text(text = "취소") }
            },
            text = { TimePicker(state = timePickerState) },
        )
    }

    pendingImportUri?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingImportUri = null },
            title = { Text(text = "가져오기를 진행할까요?") },
            text = { Text(text = "가져오기를 하면 현재 앱의 모든 데이터가 가져온 파일 내용으로 대체됩니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.importBackup(uri)
                        pendingImportUri = null
                    },
                ) {
                    Text(text = "가져오기", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingImportUri = null }) { Text(text = "취소") }
            },
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = title,
            style = SettingsSectionTitleTextStyle,
            color = Color.Black,
        )
        content()
    }
}

@Composable
private fun ReminderToggle(
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .width(48.dp)
            .height(24.dp)
            .background(
                color = if (checked) Color(0xFF0969DA) else Color(0xFFF5F5F5),
                shape = RoundedCornerShape(6.dp),
            )
            .padding(2.dp)
            .clickable { onToggle(!checked) },
        horizontalArrangement = if (checked) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (checked) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                ToggleCheckGlyph()
            }
        }
        Box(
            modifier = Modifier
                .size(width = 21.dp, height = 20.dp)
                .background(Color.White, RoundedCornerShape(4.dp)),
        )
        if (!checked) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ToggleCheckGlyph() {
    val path = remember {
        PathParser().parsePathString(
            "M0.75 6.75V1.5",
        ).toPath()
    }
    androidx.compose.foundation.Canvas(modifier = Modifier.size(width = 1.5.dp, height = 13.5.dp)) {
        drawPath(
            path = path,
            color = Color.White,
            style = Stroke(width = 1.5f),
        )
    }
}

@Composable
private fun GoalInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.height(42.dp),
        singleLine = true,
        textStyle = SettingsInputTextStyle.copy(color = Color.Black),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White, RoundedCornerShape(5.dp))
                    .border(0.5.dp, Color(0xFF757575), RoundedCornerShape(5.dp))
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = "하루에 읽을 페이지 수 목표",
                            style = SettingsInputTextStyle,
                            color = Color(0xFFB3B3B3),
                        )
                    }
                    innerTextField()
                }
                Text(
                    text = "p",
                    style = SettingsInputTextStyle,
                    color = Color(0xFFB3B3B3),
                )
            }
        },
    )
}

private val SettingsSectionTitleTextStyle = TextStyle(
    fontSize = 20.sp,
    lineHeight = 20.sp,
    fontWeight = FontWeight.SemiBold,
    letterSpacing = 0.sp,
)

private val SettingsBodyTextStyle = TextStyle(
    fontSize = 14.sp,
    lineHeight = 14.sp,
    fontWeight = FontWeight.Normal,
    letterSpacing = 0.sp,
)

private val SettingsActionTextStyle = TextStyle(
    fontSize = 14.sp,
    lineHeight = 14.sp,
    letterSpacing = 0.sp,
)

private val SettingsInputTextStyle = TextStyle(
    fontSize = 12.sp,
    lineHeight = 12.sp,
    fontWeight = FontWeight.Normal,
    letterSpacing = 0.sp,
)

private val SettingsCaptionTextStyle = TextStyle(
    fontSize = 12.sp,
    lineHeight = 12.sp,
    fontWeight = FontWeight.Normal,
    letterSpacing = 0.sp,
)

private fun defaultBackupFileName(): String =
    "booklogs_backup_${LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)}.json"

private fun formatReminderTime(hour: Int, minute: Int): String {
    val period = if (hour < 12) "오전" else "오후"
    val hour12 = when (val h = hour % 12) {
        0 -> 12
        else -> h
    }
    return "%s %d:%02d".format(period, hour12, minute)
}
