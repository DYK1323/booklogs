package com.dyk1323.booklogs.ui.detail

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dyk1323.booklogs.domain.model.Book
import com.dyk1323.booklogs.domain.model.BookFormat
import com.dyk1323.booklogs.domain.model.BookStatus
import com.dyk1323.booklogs.domain.model.Quote
import com.dyk1323.booklogs.domain.model.QuoteComment
import com.dyk1323.booklogs.domain.model.Review
import com.dyk1323.booklogs.domain.usecase.ConvertPagePercentUseCase
import com.dyk1323.booklogs.domain.usecase.LogDelta
import com.dyk1323.booklogs.ui.common.components.BookCoverImage
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
    onCaptureQuoteClick: () -> Unit,
    onWriteReviewClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(bookId) {
        viewModel.selectBook(bookId)
    }
    val uiState by viewModel.uiState.collectAsState()
    val book = uiState.book
    var showDeleteBookDialog by remember { mutableStateOf(false) }
    var pendingDeleteQuoteId by remember { mutableStateOf<Long?>(null) }
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                        IconButton(onClick = onEditClick) {
                            Icon(Icons.Outlined.Edit, contentDescription = "책 정보 수정")
                        }
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
                .padding(horizontal = 20.dp, vertical = 18.dp),
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
                                LogDeltaRow(
                                    book = book,
                                    delta = delta,
                                    isExpanded = uiState.expandedLogId == delta.log.id,
                                    editInputText = uiState.logEditInputText,
                                    editErrorMessage = uiState.logEditErrorMessage,
                                    onToggleExpand = { viewModel.toggleLogExpanded(delta.log.id) },
                                    onEditInputChanged = viewModel::updateLogEditInput,
                                    onSaveEdit = viewModel::saveLogEdit,
                                    onCancelEdit = viewModel::cancelLogEdit,
                                    onDelete = { viewModel.deleteLog(delta.log.id) },
                                )
                            }
                        }
                    }
                }
            }
            item {
                DetailSection(title = "인용구") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onCaptureQuoteClick) {
                            Text(text = "촬영으로 추가")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (uiState.editingQuoteId != null) {
                        Text(
                            text = "인용구 수정 중",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
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
                        if (uiState.editingQuoteId != null) {
                            TextButton(onClick = viewModel::cancelEditQuote) {
                                Text(text = "취소")
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Button(onClick = viewModel::saveQuote, shape = RoundedCornerShape(8.dp)) {
                            Text(text = if (uiState.editingQuoteId != null) "수정 저장" else "저장")
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
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 320.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            items(uiState.quotes, key = { it.id }) { quote ->
                                QuoteCard(
                                    quote = quote,
                                    onComments = { viewModel.openComments(quote.id) },
                                    onEdit = { viewModel.startEditQuote(quote) },
                                    onDelete = { pendingDeleteQuoteId = quote.id },
                                )
                            }
                        }
                    }
                }
            }
            item {
                DetailSection(title = "독후감") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onWriteReviewClick) {
                            Text(text = "독후감 작성")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (uiState.reviews.isEmpty()) {
                        Text(
                            text = "저장된 독후감이 없어요.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.56f),
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            uiState.reviews.forEach { review ->
                                ReviewCard(review = review)
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

    pendingDeleteQuoteId?.let { quoteId ->
        AlertDialog(
            onDismissRequest = { pendingDeleteQuoteId = null },
            title = { Text(text = "인용구를 삭제할까요?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDeleteQuoteId = null
                        viewModel.deleteQuote(quoteId)
                    },
                ) {
                    Text(text = "삭제", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteQuoteId = null }) {
                    Text(text = "취소")
                }
            },
        )
    }

    if (uiState.expandedCommentsQuoteId != null) {
        ModalBottomSheet(onDismissRequest = { viewModel.closeComments() }) {
            QuoteCommentsSheetContent(
                comments = uiState.comments,
                inputText = uiState.commentInputText,
                onInputChanged = viewModel::updateCommentInput,
                onAdd = viewModel::addComment,
                onDelete = viewModel::deleteComment,
            )
        }
    }
}

@Composable
private fun BookHeader(state: BookDetailUiState) {
    val book = state.book ?: return
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        BookCoverImage(
            coverImageUrl = book.coverImageUrl,
            modifier = Modifier
                .width(116.dp)
                .aspectRatio(0.68f),
        )
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
            Button(
                onClick = { onStatusClick(target) },
                modifier = Modifier.weight(1f, fill = false),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                Text(text = statusActionLabel(status, target))
            }
        }
    }
}

@Composable
private fun LogDeltaRow(
    book: Book,
    delta: LogDelta,
    isExpanded: Boolean,
    editInputText: String,
    editErrorMessage: String?,
    onToggleExpand: () -> Unit,
    onEditInputChanged: (String) -> Unit,
    onSaveEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleExpand),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(text = "p. ${delta.log.currentPage}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = formatDate(delta.log.loggedAt),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.56f),
                )
            }
            Text(text = deltaLabel(book, delta), style = MaterialTheme.typography.bodyMedium)
        }
        if (isExpanded) {
            Spacer(modifier = Modifier.height(8.dp))
            val inputLabel = if (book.format == BookFormat.EBOOK) "진행률" else "페이지"
            val inputSuffix = if (book.format == BookFormat.EBOOK) "%" else "p"
            OutlinedTextField(
                value = editInputText,
                onValueChange = onEditInputChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(inputLabel) },
                suffix = { Text(inputSuffix) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { onSaveEdit() }),
                isError = editErrorMessage != null,
                supportingText = editErrorMessage?.let { { Text(it) } },
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onDelete) {
                    Text(text = "삭제", color = MaterialTheme.colorScheme.error)
                }
                Row {
                    TextButton(onClick = onCancelEdit) {
                        Text(text = "취소")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Button(onClick = onSaveEdit, shape = RoundedCornerShape(8.dp)) {
                        Text(text = "수정 저장")
                    }
                }
            }
        }
    }
}

/** For EBOOK books, a raw page delta ("+5p") doesn't map onto the % the reader actually tracks. */
private fun deltaLabel(book: Book, delta: LogDelta): String {
    val totalPages = book.totalPages
    return if (book.format == BookFormat.EBOOK && totalPages != null) {
        val previousPage = (delta.log.currentPage - delta.pagesRead).coerceAtLeast(0)
        val deltaPercent = ConvertPagePercentUseCase.pageToPercent(delta.log.currentPage, totalPages) -
            ConvertPagePercentUseCase.pageToPercent(previousPage, totalPages)
        "+$deltaPercent%"
    } else {
        "+${delta.pagesRead}p"
    }
}

/** Tapping the card toggles between a 4-line preview and the full quote text. */
@Composable
private fun QuoteCard(quote: Quote, onComments: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    var expanded by remember(quote.id) { mutableStateOf(false) }
    Card(
        onClick = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Text(
                text = quote.text,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = if (expanded) Int.MAX_VALUE else 4,
                overflow = TextOverflow.Ellipsis,
            )
            quotePageLabel(quote)?.let {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.56f),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onComments) {
                    Icon(
                        Icons.Outlined.ChatBubbleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "댓글")
                }
                TextButton(onClick = onEdit) {
                    Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "수정")
                }
                TextButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "삭제")
                }
            }
        }
    }
}

@Composable
private fun QuoteCommentsSheetContent(
    comments: List<QuoteComment>,
    inputText: String,
    onInputChanged: (String) -> Unit,
    onAdd: () -> Unit,
    onDelete: (Long) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
    ) {
        Text(text = "댓글", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(12.dp))
        if (comments.isEmpty()) {
            Text(
                text = "아직 댓글이 없어요.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.56f),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(comments, key = { it.id }) { comment ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = comment.content, style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formatDate(comment.createdAt),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.56f),
                            )
                        }
                        IconButton(onClick = { onDelete(comment.id) }, modifier = Modifier.size(32.dp)) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = "댓글 삭제",
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChanged,
                modifier = Modifier.weight(1f),
                label = { Text("댓글 추가") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onAdd() }),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onAdd, shape = RoundedCornerShape(8.dp)) {
                Text(text = "추가")
            }
        }
    }
}

/** Collapsed to title + date; tapping the card reveals the full review text. */
@Composable
private fun ReviewCard(review: Review) {
    var expanded by remember(review.id) { mutableStateOf(false) }
    Card(
        onClick = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            if (expanded) {
                Text(text = review.content, style = MaterialTheme.typography.bodyLarge)
            } else {
                Text(
                    text = reviewTitle(review),
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = formatDate(review.createdAt),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.56f),
            )
        }
    }
}

/** Reviews have no separate title field — the first non-blank line stands in for one. */
private fun reviewTitle(review: Review): String =
    review.content.lineSequence().firstOrNull { it.isNotBlank() }?.trim() ?: "(내용 없음)"

private fun quotePageLabel(quote: Quote): String? {
    val start = quote.pageNumber ?: return null
    val end = quote.pageNumberEnd
    return if (end != null && end != start) "p. $start-$end" else "p. $start"
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
