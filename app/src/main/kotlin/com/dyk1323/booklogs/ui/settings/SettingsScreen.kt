package com.dyk1323.booklogs.ui.settings

import android.Manifest
import android.content.Intent
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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
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
import com.dyk1323.booklogs.BuildConfig
import com.dyk1323.booklogs.data.settings.ThemeMode
import com.dyk1323.booklogs.ui.common.theme.BooklogsAccent
import com.dyk1323.booklogs.ui.common.components.BooklogsFilledButton
import com.dyk1323.booklogs.ui.common.theme.BooklogsHairline
import com.dyk1323.booklogs.ui.common.theme.BooklogsInputTextStyle
import com.dyk1323.booklogs.ui.common.theme.BooklogsScreenBackground
import com.dyk1323.booklogs.ui.common.theme.BooklogsSectionTitleTextStyle
import com.dyk1323.booklogs.ui.common.components.BooklogsSegmentButton
import com.dyk1323.booklogs.ui.common.components.BooklogsSegmentRow
import com.dyk1323.booklogs.ui.common.theme.BooklogsSurfaceMuted
import com.dyk1323.booklogs.ui.common.theme.BooklogsTextPlaceholder
import com.dyk1323.booklogs.ui.common.theme.BooklogsTextPrimary
import com.dyk1323.booklogs.ui.common.theme.BooklogsTextSecondary
import com.dyk1323.booklogs.ui.common.components.BooklogsTopBar
import com.dyk1323.booklogs.ui.common.components.booklogsScreenBottomPadding
import com.dyk1323.booklogs.ui.common.components.booklogsScaledDp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
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
    val coroutineScope = rememberCoroutineScope()
    var showTimePicker by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var isCheckingUpdate by remember { mutableStateOf(false) }
    var updateDialog by remember { mutableStateOf<UpdateDialogState?>(null) }

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
                .background(BooklogsScreenBackground)
                .padding(innerPadding)
                .padding(start = 24.dp, top = 36.dp, end = 24.dp, bottom = booklogsScreenBottomPadding()),
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
                                color = BooklogsAccent,
                            )
                        }
                    }
                }
            }

            SettingsSection(title = "일일 목표") {
                Column(verticalArrangement = Arrangement.spacedBy(booklogsScaledDp(8.dp))) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(booklogsScaledDp(8.dp)),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        GoalInput(
                            value = uiState.dailyGoalPagesText,
                            onValueChange = viewModel::updateDailyGoalPagesText,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        )
                        BooklogsFilledButton(
                            text = "저장",
                            onClick = viewModel::saveDailyGoalPages,
                            primary = true,
                            modifier = Modifier
                                .fillMaxHeight(),
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
                            color = BooklogsTextSecondary,
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
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(booklogsScaledDp(8.dp)),
                ) {
                    BooklogsFilledButton(
                        text = if (uiState.isExportingBackup) "데이터 내보내는 중" else "데이터 내보내기",
                        onClick = { exportLauncher.launch(defaultBackupFileName()) },
                        modifier = Modifier
                            .fillMaxWidth(),
                        compact = true,
                    )
                    BooklogsFilledButton(
                        text = if (uiState.isImportingBackup) "데이터 가져오는 중" else "데이터 가져오기",
                        onClick = { importPickerLauncher.launch(arrayOf("*/*")) },
                        primary = true,
                        modifier = Modifier
                            .fillMaxWidth(),
                        compact = true,
                    )
                }
            }

            SettingsSection(title = "앱 업데이트") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(booklogsScaledDp(8.dp)),
                ) {
                    BooklogsFilledButton(
                        text = if (isCheckingUpdate) "업데이트 확인 중" else "업데이트 확인",
                        onClick = {
                            coroutineScope.launch {
                                isCheckingUpdate = true
                                updateDialog = checkGitHubReleaseUpdate()
                                isCheckingUpdate = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        compact = true,
                        enabled = !isCheckingUpdate,
                    )
                    Text(
                        text = "현재 버전 ${BuildConfig.VERSION_NAME}",
                        style = SettingsCaptionTextStyle,
                        color = BooklogsTextSecondary,
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

    updateDialog?.let { dialogState ->
        AlertDialog(
            onDismissRequest = { updateDialog = null },
            title = { Text(text = dialogState.title) },
            text = { Text(text = dialogState.message) },
            confirmButton = {
                TextButton(
                    onClick = {
                        dialogState.releaseUrl?.let { url ->
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }
                        updateDialog = null
                    },
                ) {
                    Text(text = if (dialogState.releaseUrl == null) "확인" else "릴리즈 열기")
                }
            },
            dismissButton = if (dialogState.releaseUrl == null) {
                null
            } else {
                {
                    TextButton(onClick = { updateDialog = null }) { Text(text = "닫기") }
                }
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
            style = BooklogsSectionTitleTextStyle,
            color = BooklogsTextSecondary,
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
                color = if (checked) BooklogsAccent else BooklogsSurfaceMuted,
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
                .background(BooklogsScreenBackground, RoundedCornerShape(4.dp)),
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
    val checkColor = MaterialTheme.colorScheme.onPrimary
    androidx.compose.foundation.Canvas(modifier = Modifier.size(width = 1.5.dp, height = 13.5.dp)) {
        drawPath(
            path = path,
            color = checkColor,
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
    val minHeight = booklogsScaledDp(48.dp)
    var isFocused by remember { mutableStateOf(false) }
    val borderColor = if (isFocused) BooklogsAccent else BooklogsHairline
    val borderWidth = if (isFocused) 1.dp else 0.5.dp
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxHeight()
            .heightIn(min = minHeight)
            .onFocusChanged { isFocused = it.isFocused },
        singleLine = true,
        textStyle = BooklogsInputTextStyle.copy(color = BooklogsTextPrimary),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent, RoundedCornerShape(5.dp))
                    .border(borderWidth, borderColor, RoundedCornerShape(5.dp))
                    .padding(horizontal = booklogsScaledDp(12.dp)),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = "하루에 읽을 페이지 수 목표",
                            style = BooklogsInputTextStyle,
                            color = BooklogsTextPlaceholder,
                        )
                    }
                    innerTextField()
                }
                Text(
                    text = "p",
                    style = BooklogsInputTextStyle,
                    color = BooklogsTextPlaceholder,
                )
            }
        },
    )
}

private val SettingsBodyTextStyle = TextStyle(
    fontSize = 19.sp,
    lineHeight = 19.sp,
    fontWeight = FontWeight.Normal,
    letterSpacing = 0.sp,
)

private val SettingsActionTextStyle = TextStyle(
    fontSize = 19.sp,
    lineHeight = 19.sp,
    letterSpacing = 0.sp,
)

private val SettingsCaptionTextStyle = TextStyle(
    fontSize = 16.sp,
    lineHeight = 16.sp,
    fontWeight = FontWeight.Normal,
    letterSpacing = 0.sp,
)

private data class GitHubRelease(
    val tagName: String,
    val htmlUrl: String,
)

private data class UpdateDialogState(
    val title: String,
    val message: String,
    val releaseUrl: String? = null,
)

private const val GitHubLatestReleaseUrl = "https://api.github.com/repos/DYK1323/booklogs/releases/latest"

private val GitHubReleaseHttpClient = OkHttpClient()

private suspend fun checkGitHubReleaseUpdate(): UpdateDialogState {
    val latestRelease = runCatching { fetchLatestGitHubRelease() }.getOrElse {
        return UpdateDialogState(
            title = "업데이트 확인 실패",
            message = "업데이트 정보를 가져오지 못했어요. 인터넷 연결을 확인한 뒤 다시 시도해주세요.",
        )
    }

    if (latestRelease == null) {
        return UpdateDialogState(
            title = "릴리즈 없음",
            message = "아직 GitHub Release에 등록된 APK가 없어요.",
        )
    }

    val currentVersion = BuildConfig.VERSION_NAME.normalizedReleaseVersion()
    val latestVersion = latestRelease.tagName.normalizedReleaseVersion()

    return if (latestVersion == currentVersion) {
        UpdateDialogState(
            title = "최신 버전",
            message = "현재 최신 버전을 사용 중이에요.\n${BuildConfig.VERSION_NAME}",
        )
    } else {
        UpdateDialogState(
            title = "업데이트 가능",
            message = "새 릴리즈가 있어요.\n현재: ${BuildConfig.VERSION_NAME}\n최신: ${latestRelease.tagName}",
            releaseUrl = latestRelease.htmlUrl,
        )
    }
}

private suspend fun fetchLatestGitHubRelease(): GitHubRelease? = withContext(Dispatchers.IO) {
    val request = Request.Builder()
        .url(GitHubLatestReleaseUrl)
        .header("Accept", "application/vnd.github+json")
        .build()

    GitHubReleaseHttpClient.newCall(request).execute().use { response ->
        if (response.code == 404) return@withContext null
        if (!response.isSuccessful) error("GitHub release request failed: ${response.code}")

        val body = response.body?.string().orEmpty()
        val json = JSONObject(body)
        GitHubRelease(
            tagName = json.optString("tag_name"),
            htmlUrl = json.optString("html_url"),
        )
    }
}

private fun String.normalizedReleaseVersion(): String =
    trim()
        .removePrefix("v")
        .substringBefore("+")
        .substringBefore("-")

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
