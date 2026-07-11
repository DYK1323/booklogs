package com.dyk1323.booklogs.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dyk1323.booklogs.domain.usecase.DayPageTotal
import com.dyk1323.booklogs.ui.common.components.BookCoverImage
import com.dyk1323.booklogs.ui.common.components.EmptyState
import com.dyk1323.booklogs.ui.common.theme.StatusGoodLight
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onRegisterBookClick: () -> Unit,
    onBookDetailClick: (Long) -> Unit,
    onCaptureQuoteClick: (Long) -> Unit,
    onLibraryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val quickLogSheetState by viewModel.quickLogSheetState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onRegisterBookClick) {
                Icon(Icons.Outlined.Add, contentDescription = "책 등록")
            }
        },
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            TodayPagesHero(todayPages = uiState.todayPages)
            Spacer(modifier = Modifier.height(20.dp))
            DailyPagesBarChart(
                totals = uiState.weekTotals,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(116.dp),
            )
            Spacer(modifier = Modifier.height(28.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "읽는 중", style = MaterialTheme.typography.headlineMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${uiState.readingBooks.size}권",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.56f),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onLibraryClick) {
                        Text(text = "라이브러리")
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            if (uiState.readingBooks.isEmpty() && !uiState.isLoading) {
                EmptyState(
                    message = "아직 읽는 중인 책이 없어요.",
                    actionLabel = "책 등록",
                    onActionClick = onRegisterBookClick,
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 132.dp),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    items(uiState.readingBooks, key = { it.book.id }) { item ->
                        BookShelfTile(item = item, onClick = { viewModel.openQuickLog(item.book.id) })
                    }
                }
            }
        }
    }

    quickLogSheetState?.let { sheetState ->
        ModalBottomSheet(onDismissRequest = viewModel::closeQuickLog) {
            QuickLogSheet(
                state = sheetState,
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
            )
        }
    }
}

@Composable
private fun TodayPagesHero(todayPages: Int) {
    Column {
        Text(
            text = "오늘",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.56f),
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = todayPages.toString(),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "페이지",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 9.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
            )
        }
    }
}

@Composable
private fun DailyPagesBarChart(totals: List<DayPageTotal>, modifier: Modifier = Modifier) {
    val maxValue = (totals.maxOfOrNull { it.totalPages } ?: 0).coerceAtLeast(1)
    val primary = MaterialTheme.colorScheme.primary
    val muted = MaterialTheme.colorScheme.primary.copy(alpha = 0.26f)
    val today = LocalDate.now().toEpochDay()

    Canvas(
        modifier = modifier.semantics {
            contentDescription = "최근 7일 동안 읽은 페이지 막대그래프"
        },
    ) {
        val barWidth = size.width / (totals.size * 2.1f).coerceAtLeast(1f)
        val gap = (size.width - barWidth * totals.size) / (totals.size + 1)
        val chartHeight = size.height - 24.dp.toPx()
        totals.forEachIndexed { index, total ->
            val left = gap + index * (barWidth + gap)
            val barHeight = chartHeight * (total.totalPages.toFloat() / maxValue)
            val color = when {
                total.goalMet -> StatusGoodLight
                total.epochDay == today -> primary
                else -> muted
            }
            drawRoundRect(
                color = color,
                topLeft = Offset(left, chartHeight - barHeight),
                size = Size(barWidth, barHeight.coerceAtLeast(4.dp.toPx())),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx()),
            )
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
                .aspectRatio(0.68f),
            contentAlignment = Alignment.Center,
        ) {
            BookCoverImage(
                coverImageUrl = item.book.coverImageUrl,
                modifier = Modifier.matchParentSize(),
                placeholderIconSize = 42.dp,
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.28f)),
            )
            ProgressDonut(progress = item.progress, modifier = Modifier.size(72.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = item.book.title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = progressCaption(item),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.56f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ProgressDonut(progress: Float?, modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    if (progress == null) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(percent = 50))
                .background(Color.Black.copy(alpha = 0.36f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "?", style = MaterialTheme.typography.titleLarge, color = Color.White)
        }
        return
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
            val inset = 5.dp.toPx()
            val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
            drawArc(
                color = Color.White.copy(alpha = 0.30f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = stroke,
            )
            drawArc(
                color = primary,
                startAngle = -90f,
                sweepAngle = 360f * progress.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = stroke,
            )
        }
        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
        )
    }
}

@Composable
private fun QuickLogSheet(
    state: QuickLogSheetUiState,
    onInputChanged: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    onOpenDetail: () -> Unit,
    onCaptureQuote: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    LaunchedEffect(state.book.id) {
        focusManager.clearFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp),
    ) {
        Text(text = state.book.title, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = progressCaption(
                BookShelfItemUi(
                    book = state.book,
                    currentPage = state.currentPage,
                    progress = state.progress,
                ),
            ),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.56f),
        )
        Spacer(modifier = Modifier.height(18.dp))
        OutlinedTextField(
            value = state.inputText,
            onValueChange = onInputChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(state.inputLabel) },
            suffix = { Text(state.inputSuffix) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { onSave() }),
            isError = state.errorMessage != null,
            supportingText = {
                Text(state.errorMessage ?: "지금 도달한 위치를 입력해주세요.")
            },
        )
        Spacer(modifier = Modifier.height(18.dp))
        TextButton(onClick = onCaptureQuote, modifier = Modifier.fillMaxWidth()) {
            Text(text = "인용구 촬영")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onOpenDetail) {
                Text(text = "상세 보기")
            }
            Row {
                TextButton(onClick = onDismiss) {
                    Text(text = "취소")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onSave,
                    enabled = !state.isSaving,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                ) {
                    Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = if (state.isSaving) "저장 중" else "저장")
                }
            }
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
