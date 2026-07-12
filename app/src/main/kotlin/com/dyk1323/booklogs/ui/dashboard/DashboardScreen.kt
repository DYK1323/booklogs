package com.dyk1323.booklogs.ui.dashboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings as AndroidSettings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.dyk1323.booklogs.domain.usecase.DayPageTotal
import com.dyk1323.booklogs.ui.common.components.BookCoverImage
import com.dyk1323.booklogs.ui.common.components.BooklogsScreenBackground
import com.dyk1323.booklogs.ui.common.components.BooklogsSheetBottomPadding
import com.dyk1323.booklogs.ui.common.components.BooklogsSheetHorizontalPadding
import com.dyk1323.booklogs.ui.common.components.CameraCapturePreview
import com.dyk1323.booklogs.ui.common.components.EmptyState
import com.dyk1323.booklogs.ui.common.formatRelativeTime
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onRegisterBookClick: () -> Unit,
    onBookDetailClick: (Long) -> Unit,
    onCaptureQuoteClick: (Long) -> Unit,
    onLibraryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val quickLogSheetState by viewModel.quickLogSheetState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.undoLogEvents.collect { log ->
            val result = snackbarHostState.showSnackbar(
                message = "기록 삭제됨",
                actionLabel = "실행취소",
                duration = SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) viewModel.undoDeleteLog(log)
        }
    }

    var isCapturingPage by remember { mutableStateOf(false) }
    var showSuccessCheck by remember { mutableStateOf(false) }
    LaunchedEffect(quickLogSheetState == null) {
        if (quickLogSheetState == null) isCapturingPage = false
    }
    LaunchedEffect(Unit) {
        viewModel.quickLogSaveSucceeded.collect {
            showSuccessCheck = true
            delay(300)
            showSuccessCheck = false
            viewModel.closeQuickLog()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = BooklogsScreenBackground,
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { innerPadding ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 20.dp),
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                TodayPagesHero(
                    todayPages = uiState.todayPages,
                    dailyGoalPages = uiState.dailyGoalPages,
                    totals = uiState.weekTotals,
                )
                Spacer(modifier = Modifier.height(36.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "내 책장",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 16.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = "${uiState.readingBooks.size}권",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 10.sp,
                                lineHeight = 10.sp,
                                fontWeight = FontWeight.Normal,
                            ),
                            color = Color(0xFF757575),
                        )
                        Text(
                            text = "라이브러리",
                            modifier = Modifier.clickable(onClick = onLibraryClick),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 12.sp,
                                lineHeight = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = Color(0xFF0C7EFF),
                        )
                        Box(modifier = Modifier.clickable(onClick = onSettingsClick)) {
                            DashboardSettingsIcon()
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (uiState.readingBooks.isEmpty() && !uiState.isLoading) {
                    EmptyState(
                        message = "아직 읽는 중인 책이 없어요.",
                        actionLabel = "책 등록",
                        onActionClick = onRegisterBookClick,
                    )
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            items(uiState.readingBooks, key = { it.book.id }) { item ->
                                BookShelfTile(item = item, onClick = { viewModel.openQuickLog(item.book.id) })
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(
                            onClick = onRegisterBookClick,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                        ) {
                            Text(
                                text = "새 책 추가",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontSize = 12.sp,
                                    lineHeight = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = Color(0xFF0C7EFF),
                            )
                        }
                    }
                }
            }
        }

        // Rendered as a plain full-screen overlay, NOT nested inside the ModalBottomSheet below —
        // CameraX's PreviewView (a SurfaceView) hosted inside a ModalBottomSheet's own Popup/Dialog
        // window rendered blank with no visible capture button on real devices, while the identical
        // preview works fine as a plain top-level composable elsewhere (QuoteCaptureScreen). Closing the
        // sheet first and showing the camera as a normal screen sidesteps that SurfaceView-in-Dialog
        // interop issue.
        if (isCapturingPage) {
            PageCameraCapture(
                onCaptured = { bitmap ->
                    viewModel.prefillQuickLogFromCapture(bitmap)
                    isCapturingPage = false
                },
                onCancel = { isCapturingPage = false },
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            quickLogSheetState?.let { sheetState ->
                ModalBottomSheet(
                    onDismissRequest = { viewModel.closeQuickLog() },
                    containerColor = BooklogsScreenBackground,
                    tonalElevation = 0.dp,
                    dragHandle = null,
                ) {
                    QuickLogSheet(
                        state = sheetState,
                        showSuccessCheck = showSuccessCheck,
                        onInputChanged = viewModel::updateQuickLogInput,
                        onSave = viewModel::saveQuickLog,
                        onDismiss = viewModel::closeQuickLog,
                        onOpenDetail = {
                            val bookId = sheetState.book.id
                            viewModel.closeQuickLog()
                            onBookDetailClick(bookId)
                        },
                        onCaptureQuote = {
                            val bookId = sheetState.book.id
                            viewModel.closeQuickLog()
                            onCaptureQuoteClick(bookId)
                        },
                        onCapturePage = { isCapturingPage = true },
                        onEditLatestLog = viewModel::startEditLatestLog,
                        onCancelEditLatestLog = viewModel::cancelEditLatestLog,
                        onDeleteLatestLog = viewModel::deleteLatestLog,
                    )
                }
            }
        }
    }
}

/** Camera permission gate for the quick-log page-photo flow, then delegates to the shared preview. */
@Composable
private fun PageCameraCapture(
    onCaptured: (android.graphics.Bitmap) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission = granted
    }
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Box(modifier = modifier.background(BooklogsScreenBackground)) {
        if (hasCameraPermission) {
            CameraCapturePreview(
                captionText = "페이지를 맞춘 뒤 사진을 찍어주세요.",
                onCaptured = onCaptured,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "페이지 사진을 찍으려면 카메라 권한이 필요해요.",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        val intent = Intent(AndroidSettings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    },
                ) {
                    Text(text = "설정에서 권한 허용")
                }
            }
        }
        IconButton(
            onClick = onCancel,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(12.dp)
                .background(Color.Black.copy(alpha = 0.4f), CircleShape),
        ) {
            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "취소", tint = Color.White)
        }
    }
}

@Composable
private fun TodayPagesHero(todayPages: Int, dailyGoalPages: Int?, totals: List<DayPageTotal>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(149.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0C7EFF)),
    ) {
        DashboardHeroBackground(
            modifier = Modifier
                .offset(x = (-31).dp, y = 39.dp)
                .width(462.dp)
                .height(149.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "오늘 읽은 페이지",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 16.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = Color.White,
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = todayPages.toString(),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 40.sp,
                            lineHeight = 40.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = Color.White,
                    )
                    Text(
                        text = " / ${dailyGoalPages ?: 0}p",
                        modifier = Modifier.padding(bottom = 3.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 12.sp,
                            lineHeight = 12.sp,
                            fontWeight = FontWeight.Normal,
                        ),
                        color = Color.White.copy(alpha = 0.86f),
                    )
                }
            }
            DailyPagesBarChart(
                totals = totals,
                dailyGoalPages = dailyGoalPages,
                modifier = Modifier
                    .width(208.dp)
                    .height(109.dp),
            )
        }
    }
}

@Composable
private fun DailyPagesBarChart(totals: List<DayPageTotal>, dailyGoalPages: Int?, modifier: Modifier = Modifier) {
    val plotHeight = 77.dp
    val maxValue = (totals.maxOfOrNull { it.totalPages }?.coerceAtLeast(dailyGoalPages ?: 0) ?: 0).coerceAtLeast(1)
    Column(
        modifier = modifier.semantics {
            contentDescription = "최근 7일 동안 읽은 페이지 막대그래프"
        },
        verticalArrangement = Arrangement.Top,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(plotHeight),
            contentAlignment = Alignment.BottomStart,
        ) {
            if (dailyGoalPages != null && dailyGoalPages > 0) {
                val ratio = dailyGoalPages.toFloat() / maxValue.toFloat()
                val lineY = plotHeight - (plotHeight * ratio.coerceIn(0f, 1f))
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .offset(y = lineY),
                ) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.47f),
                        start = Offset(0.5f, size.height / 2f),
                        end = Offset(size.width - 0.5f, size.height / 2f),
                        strokeWidth = 1.dp.toPx(),
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                totals.forEach { total ->
                    DashboardBarColumn(total = total, maxValue = maxValue, plotHeight = plotHeight)
                }
            }
        }
    }
}

@Composable
private fun BookShelfTile(item: BookShelfItemUi, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics {
                val percent = item.progress?.let { "${(it * 100).toInt()}%" } ?: "진행률 알 수 없음"
                contentDescription = "${item.book.title}, $percent"
            },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(116f / 153.115f),
            contentAlignment = Alignment.Center,
        ) {
            BookCoverImage(
                coverImageUrl = item.book.coverImageUrl,
                modifier = Modifier.matchParentSize(),
                shape = RoundedCornerShape(4.dp),
                placeholderIconSize = 42.dp,
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(4.dp)),
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = item.book.title,
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 12.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 2.dp),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = progressCaption(item),
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 10.sp,
                lineHeight = 10.sp,
                fontWeight = FontWeight.Normal,
            ),
            color = Color(0xFF757575),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 2.dp),
        )
    }
}

@Composable
private fun DashboardHeroBackground(modifier: Modifier = Modifier) {
    val shapePath = remember {
        androidx.compose.ui.graphics.vector.PathParser()
            .parsePathString(
                "M128 0C83 0 52 7 0 37.5L12.5 139.5L89.5 149L462 113.5C454.333 91 440 45.8 444 45C449 44 349 45 286.5 37.5C224 30 173 0 128 0Z",
            )
            .toPath()
    }
    Canvas(modifier = modifier) {
        scale(scaleX = size.width / 462f, scaleY = size.height / 149f, pivot = Offset.Zero) {
            drawPath(
                path = shapePath,
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF0065D8), Color(0x000065D8)),
                    startX = 0f,
                    endX = 462f,
                ),
            )
        }
    }
}

@Composable
private fun DashboardBarColumn(total: DayPageTotal, maxValue: Int, plotHeight: androidx.compose.ui.unit.Dp) {
    val date = LocalDate.ofEpochDay(total.epochDay)
    val barHeight = ((total.totalPages.toFloat() / maxValue.toFloat()) * 77f).coerceAtLeast(4f)
    Column(
        modifier = Modifier.width(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.height(plotHeight),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Box(
                modifier = Modifier
                    .width(13.dp)
                    .height(barHeight.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.White, Color(0xFF8CC2FF)),
                        ),
                        shape = RoundedCornerShape(5.dp),
                    ),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 11.sp,
                    lineHeight = 11.sp,
                    fontWeight = FontWeight.Normal,
                ),
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Text(
                text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).uppercase(Locale.ENGLISH),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 8.sp,
                    lineHeight = 8.sp,
                    fontWeight = FontWeight.Normal,
                ),
                color = Color.White,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun DashboardSettingsIcon(modifier: Modifier = Modifier) {
    val outerPath = remember {
        androidx.compose.ui.graphics.vector.PathParser()
            .parsePathString(
                "M16.0387 6.04191L15.7337 5.87191L15.6396 5.81858C15.4124 5.68223 15.221 5.49369 15.0812 5.26858C15.0662 5.24608 15.0529 5.22191 15.0262 5.17525C14.8466 4.88685 14.7593 4.55044 14.7762 4.21108L14.7812 3.85691C14.7912 3.29025 14.7962 3.00525 14.7162 2.75025C14.6455 2.52336 14.527 2.31424 14.3687 2.13691C14.1904 1.93691 13.9429 1.79358 13.4471 1.50858L13.0354 1.27108C12.5421 0.986913 12.2946 0.844413 12.0321 0.790246C11.8001 0.742256 11.5607 0.744244 11.3296 0.79608C11.0687 0.854413 10.8246 1.00108 10.3371 1.29275L10.3346 1.29441L10.0396 1.47025C9.9929 1.49858 9.96874 1.51191 9.9454 1.52525C9.71374 1.65441 9.4554 1.72525 9.18957 1.73358C9.1629 1.73525 9.1354 1.73525 9.08124 1.73525L8.9729 1.73441C8.70699 1.72594 8.44696 1.65394 8.21457 1.52441C8.19124 1.51191 8.16874 1.49775 8.12207 1.46941L7.82457 1.29108C7.33374 0.996079 7.0879 0.849413 6.8254 0.790246C6.59345 0.738326 6.35307 0.736621 6.1204 0.785246C5.85707 0.840246 5.6104 0.98358 5.11624 1.27025L5.11374 1.27108L4.70707 1.50691L4.7029 1.51025C4.2129 1.79358 3.96707 1.93691 3.7904 2.13608C3.63297 2.31308 3.51507 2.5216 3.44457 2.74775C3.3654 3.00358 3.36957 3.28858 3.37957 3.85858L3.3854 4.21191C3.3854 4.26608 3.3879 4.29275 3.38707 4.31858C3.38275 4.62135 3.29604 4.91722 3.13624 5.17441C3.10874 5.22108 3.09624 5.24441 3.08207 5.26608C2.94156 5.49289 2.74866 5.68265 2.51957 5.81941L2.42624 5.87191L2.1254 6.03858C1.62374 6.31608 1.3729 6.45525 1.19124 6.65358C1.02972 6.82844 0.907534 7.03587 0.832903 7.26191C0.749569 7.51775 0.749569 7.80358 0.750403 8.37691L0.752069 8.84608C0.752903 9.41525 0.754569 9.69941 0.838736 9.95358C0.913057 10.1782 1.03436 10.3845 1.19457 10.5586C1.37624 10.7552 1.62457 10.8936 2.12207 11.1702L2.4204 11.3361C2.47124 11.3644 2.49707 11.3777 2.52124 11.3927C2.78238 11.5493 2.99565 11.7744 3.1379 12.0436L3.19374 12.1436C3.3345 12.4093 3.40072 12.7082 3.3854 13.0086L3.37957 13.3477C3.36957 13.9194 3.3654 14.2061 3.4454 14.4619C3.51624 14.6886 3.63457 14.8977 3.7929 15.0752C3.97124 15.2752 4.21957 15.4177 4.71457 15.7036L5.12624 15.9411C5.6204 16.2252 5.86707 16.3677 6.12957 16.4219C6.36149 16.4699 6.60098 16.4679 6.83207 16.4161C7.09374 16.3577 7.3379 16.2111 7.82707 15.9177L8.12207 15.7411L8.21624 15.6861C8.4479 15.5577 8.70624 15.4861 8.97207 15.4777L9.0804 15.4769H9.18874C9.45374 15.4852 9.71374 15.5577 9.94707 15.6869L10.0237 15.7327L10.3371 15.9211C10.8287 16.2161 11.0737 16.3627 11.3362 16.4211C11.5681 16.4735 11.8085 16.4758 12.0412 16.4277C12.3037 16.3727 12.5521 16.2286 13.0462 15.9419L13.4587 15.7027C13.9487 15.4177 14.1946 15.2752 14.3712 15.0761C14.5296 14.8986 14.6462 14.6902 14.7171 14.4644C14.7962 14.2102 14.7921 13.9277 14.7821 13.3661L14.7754 12.9994V12.8936C14.7793 12.5906 14.8658 12.2944 15.0254 12.0369L15.0796 11.9452C15.2201 11.7184 15.413 11.5287 15.6421 11.3919L15.7337 11.3411L15.7354 11.3402L16.0362 11.1736C16.5379 10.8952 16.7887 10.7569 16.9712 10.5586C17.1329 10.3836 17.2546 10.1752 17.3287 9.95025C17.4121 9.69608 17.4121 9.41108 17.4104 8.84441L17.4087 8.36608C17.4079 7.79691 17.4071 7.51191 17.3229 7.25775C17.2482 7.03333 17.1266 6.82738 16.9662 6.65358C16.7854 6.45691 16.5371 6.31858 16.0404 6.04275L16.0387 6.04191Z",
            )
            .toPath()
    }
    val innerPath = remember {
        androidx.compose.ui.graphics.vector.PathParser()
            .parsePathString(
                "M6.72338 10.9631C6.09826 10.338 5.74707 9.49013 5.74707 8.60608C5.74707 7.72202 6.09826 6.87418 6.72338 6.24906C7.3485 5.62394 8.19635 5.27275 9.0804 5.27275C9.96446 5.27275 10.8123 5.62394 11.4374 6.24906C12.0625 6.87418 12.4137 7.72202 12.4137 8.60608C12.4137 9.49013 12.0625 10.338 11.4374 10.9631C10.8123 11.5882 9.96446 11.9394 9.0804 11.9394C8.19635 11.9394 7.3485 11.5882 6.72338 10.9631Z",
            )
            .toPath()
    }
    Canvas(modifier = modifier.size(20.dp)) {
        scale(scaleX = size.width / 18.1611f, scaleY = size.height / 17.2122f, pivot = Offset.Zero) {
            drawPath(path = outerPath, color = Color(0xFF757575), style = Stroke(width = 1.5f))
            drawPath(path = innerPath, color = Color(0xFF757575), style = Stroke(width = 1.5f))
        }
    }
}

@Composable
private fun QuickLogSheet(
    state: QuickLogSheetUiState,
    showSuccessCheck: Boolean,
    onInputChanged: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    onOpenDetail: () -> Unit,
    onCaptureQuote: () -> Unit,
    onCapturePage: () -> Unit,
    onEditLatestLog: () -> Unit,
    onCancelEditLatestLog: () -> Unit,
    onDeleteLatestLog: () -> Unit,
) {
    // Never auto-focus — book-cover tap is the highest-frequency action in the app, and requesting
    // focus programmatically (even only on some events) proved flaky under rapid open/close (keyboard
    // would sometimes show, sometimes not, depending on recomposition timing). The field is prefilled
    // and select-all'd so a manual tap + type still overwrites it outright; focus only ever comes from
    // the user tapping the field themselves.
    var fieldValue by remember { mutableStateOf(TextFieldValue(state.inputText)) }
    LaunchedEffect(state.book.id, state.prefillNonce) {
        fieldValue = TextFieldValue(text = state.inputText, selection = TextRange(0, state.inputText.length))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BooklogsScreenBackground)
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 64.dp),
        verticalArrangement = Arrangement.spacedBy(36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(4.dp)
                .background(Color(0xFF757575), RoundedCornerShape(999.dp)),
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = state.book.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 20.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.sp,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = progressCaption(
                    BookShelfItemUi(
                        book = state.book,
                        currentPage = state.currentPage,
                        progress = state.progress,
                    ),
                ),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 14.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.sp,
                ),
                color = Color(0xFF757575),
            )
        }
        QuickLogPageInput(
            value = fieldValue,
            inputLabel = state.inputLabel,
            inputSuffix = state.inputSuffix,
            errorMessage = state.errorMessage,
            showPageCameraButton = state.showPageCameraButton,
            onValueChange = {
                fieldValue = it
                onInputChanged(it.text)
            },
            onDone = onSave,
            onCapturePage = onCapturePage,
        )
        Button(
            onClick = onCaptureQuote,
            modifier = Modifier
                .fillMaxWidth()
                .height(43.dp),
            shape = RoundedCornerShape(5.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF5F5F5),
                contentColor = Color(0xFF757575),
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp),
        ) {
            Text(
                text = "인용구 추가",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 14.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.sp,
                ),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(43.dp)
                .padding(start = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "상세 보기",
                modifier = Modifier.clickable(onClick = onOpenDetail),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 14.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.sp,
                ),
                color = Color(0xFF0C7EFF),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .width(64.dp)
                        .height(43.dp),
                    shape = RoundedCornerShape(5.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF5F5F5),
                        contentColor = Color(0xFF757575),
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp),
                ) {
                    Text(
                        text = "취소",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 14.sp,
                            lineHeight = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.sp,
                        ),
                    )
                }
                Button(
                    onClick = onSave,
                    enabled = !state.isSaving,
                    modifier = Modifier
                        .width(64.dp)
                        .height(43.dp),
                    shape = RoundedCornerShape(5.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0C7EFF),
                        contentColor = Color.White,
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp),
                ) {
                    Text(
                        text = if (showSuccessCheck) "저장" else state.saveButtonLabel,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 14.sp,
                            lineHeight = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.sp,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickLogPageInput(
    value: TextFieldValue,
    inputLabel: String,
    inputSuffix: String,
    errorMessage: String?,
    showPageCameraButton: Boolean,
    onValueChange: (TextFieldValue) -> Unit,
    onDone: () -> Unit,
    onCapturePage: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = inputLabel,
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 12.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.sp,
            ),
            color = Color(0xFF757575),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { onDone() }),
                visualTransformation = VisualTransformation.None,
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White, RoundedCornerShape(4.dp))
                            .border(
                                width = 0.5.dp,
                                color = if (errorMessage != null) MaterialTheme.colorScheme.error else Color(0xFF757575),
                                shape = RoundedCornerShape(5.dp),
                            )
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            if (value.text.isEmpty()) {
                                Text(
                                    text = "현재까지 읽은 페이지",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontSize = 12.sp,
                                        lineHeight = 12.sp,
                                        fontWeight = FontWeight.Normal,
                                        letterSpacing = 0.sp,
                                    ),
                                    color = Color(0xFFB3B3B3),
                                )
                            }
                            innerTextField()
                        }
                        Text(
                            text = inputSuffix,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 12.sp,
                                lineHeight = 12.sp,
                                fontWeight = FontWeight.Normal,
                                letterSpacing = 0.sp,
                            ),
                            color = Color(0xFFB3B3B3),
                        )
                    }
                },
            )
            if (showPageCameraButton) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(5.dp))
                        .clickable(onClick = onCapturePage),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Outlined.PhotoCamera,
                        contentDescription = "사진으로 페이지 인식",
                        modifier = Modifier.size(28.dp),
                        tint = Color(0xFF757575),
                    )
                }
            }
        }
        errorMessage?.let {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 12.sp,
                    lineHeight = 12.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.sp,
                ),
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

private fun progressCaption(item: BookShelfItemUi): String {
    val totalPages = item.book.totalPages
    return when {
        totalPages == null -> "전체 페이지 수가 필요해요."
        item.currentPage == null -> "아직 기록이 없어요."
        else -> "${item.currentPage} / ${totalPages}p"
    }
}
