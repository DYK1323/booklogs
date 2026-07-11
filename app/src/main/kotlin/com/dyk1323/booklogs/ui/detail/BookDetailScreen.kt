package com.dyk1323.booklogs.ui.detail

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.dyk1323.booklogs.domain.model.BookStatus
import com.dyk1323.booklogs.domain.model.Quote
import com.dyk1323.booklogs.domain.usecase.LogDelta
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    bookId: Long,
    viewModel: BookDetailViewModel,
    onBack: () -> Unit,
    onDeleted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(bookId) {
        viewModel.selectBook(bookId)
    }
    val uiState by viewModel.uiState.collectAsState()
    val book = uiState.book
    var showDeleteBookDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = book?.title ?: "책 상세",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    if (book != null) {
                        IconButton(onClick = { showDeleteBookDialog = true }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "책 삭제")
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        if (book == null) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "책 정보를 찾지 못했어요.", style = MaterialTheme.typography.bodyLarge)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            item {
                BookHeader(state = uiState)
            }
            item {
                DetailSection(title = "상태") {
                    StatusActions(
                        status = book.status,
                        onStatusClick = viewModel::changeStatus,
                    )
                }
            }
            item {
                DetailSection(title = "진행 이력") {
                    if (uiState.logDeltas.isEmpty()) {
                        Text(
                            text = "아직 진행 기록이 없어요.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.56f),
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            uiState.logDeltas.take(12).forEach { delta ->
                                LogDeltaRow(delta = delta, onDelete = { viewModel.deleteLog(delta.log.id) })
                            }
                        }
                    }
                }
            }
            item {
                DetailSection(title = "인용구") {
                    OutlinedTextField(
                        value = uiState.quoteText,
                        onValueChange = viewModel::updateQuoteText,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("인용구") },
                        minLines = 2,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = uiState.quotePageText,
                            onValueChange = viewModel::updateQuotePageText,
                            modifier = Modifier.weight(1f),
                            label = { Text("페이지") },
                            singleLine = true,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = viewModel::saveQuote, shape = RoundedCornerShape(8.dp)) {
                            Text(text = "저장")
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    if (uiState.quotes.isEmpty()) {
                        Text(
                            text = "저장된 인용구가 없어요.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.56f),
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            uiState.quotes.forEach { quote ->
                                QuoteRow(quote = quote, onDelete = { viewModel.deleteQuote(quote.id) })
                            }
                        }
                    }
                }
            }
            item {
                DetailSection(title = "독후감") {
                    OutlinedTextField(
                        value = uiState.reviewText,
                        onValueChange = viewModel::updateReviewText,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("독후감") },
                        minLines = 4,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Button(onClick = viewModel::saveReview, shape = RoundedCornerShape(8.dp)) {
                            Text(text = "저장")
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    if (uiState.reviews.isEmpty()) {
                        Text(
                            text = "저장된 독후감이 없어요.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.56f),
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            uiState.reviews.forEach { review ->
                                Text(text = review.content, style = MaterialTheme.typography.bodyLarge)
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
            item {
                uiState.message?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 24.dp),
                    )
                } ?: Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showDeleteBookDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteBookDialog = false },
            title = { Text(text = "책을 삭제할까요?") },
            text = { Text(text = "진행 기록, 인용구, 독후감이 함께 삭제돼요.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteBookDialog = false
                        viewModel.deleteBook(onDeleted)
                    },
                ) {
                    Text(text = "삭제", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteBookDialog = false }) {
                    Text(text = "취소")
                }
            },
        )
    }
}

@Composable
private fun BookHeader(state: BookDetailUiState) {
    val book = state.book ?: return
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .width(116.dp)
                .aspectRatio(0.68f)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            if (book.coverImageUrl != null) {
                AsyncImage(
                    model = book.coverImageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Book,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.32f),
                )
            }
        }
        Spacer(modifier = Modifier.width(18.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = book.title, style = MaterialTheme.typography.titleLarge)
            book.author?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = progressText(state), style = MaterialTheme.typography.bodyMedium)
            book.genre?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.56f),
                )
            }
        }
    }
}

@Composable
private fun DetailSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun StatusActions(status: BookStatus, onStatusClick: (BookStatus) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        availableStatusActions(status).forEach { target ->
            TextButton(
                onClick = { onStatusClick(target) },
                modifier = Modifier.weight(1f, fill = false),
            ) {
                Text(text = statusActionLabel(status, target))
            }
        }
    }
}

@Composable
private fun LogDeltaRow(delta: LogDelta, onDelete: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column {
            Text(text = "p. ${delta.log.currentPage}", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = formatDate(delta.log.loggedAt),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.56f),
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "+${delta.pagesRead}p", style = MaterialTheme.typography.bodyMedium)
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = "진행 기록 삭제")
            }
        }
    }
}

@Composable
private fun QuoteRow(quote: Quote, onDelete: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = quote.text, style = MaterialTheme.typography.bodyLarge)
            quote.pageNumber?.let {
                Text(
                    text = "p. $it",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.56f),
                )
            }
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Outlined.Delete, contentDescription = "인용구 삭제")
        }
    }
    HorizontalDivider()
}

private fun progressText(state: BookDetailUiState): String {
    val total = state.book?.totalPages ?: return "전체 페이지 수가 필요해요."
    val current = state.currentPage ?: 0
    val percent = state.progress?.let { (it * 100).toInt() } ?: 0
    return "$current / ${total}p · $percent%"
}

private fun formatDate(timestampMillis: Long): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    return Instant.ofEpochMilli(timestampMillis).atZone(ZoneId.systemDefault()).format(formatter)
}

private fun availableStatusActions(status: BookStatus): List<BookStatus> = when (status) {
    BookStatus.PLANNED -> listOf(BookStatus.READING)
    BookStatus.READING -> listOf(BookStatus.PAUSED, BookStatus.FINISHED, BookStatus.DROPPED)
    BookStatus.PAUSED -> listOf(BookStatus.READING)
    BookStatus.FINISHED -> listOf(BookStatus.READING)
    BookStatus.DROPPED -> listOf(BookStatus.READING)
}

private fun statusActionLabel(from: BookStatus, target: BookStatus): String = when {
    from == BookStatus.FINISHED && target == BookStatus.READING -> "다시 읽기"
    from == BookStatus.DROPPED && target == BookStatus.READING -> "다시 읽기"
    target == BookStatus.READING -> "읽기 시작"
    target == BookStatus.PAUSED -> "멈추기"
    target == BookStatus.FINISHED -> "완독"
    target == BookStatus.DROPPED -> "중단"
    target == BookStatus.PLANNED -> "읽을 예정"
    else -> "변경"
}
