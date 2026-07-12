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
import androidx.compose.material.icons.outlined.Add
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
import com.dyk1323.booklogs.domain.model.ReadingRound
import com.dyk1323.booklogs.domain.model.Review
import com.dyk1323.booklogs.domain.model.RoundEndReason
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
    onViewAllQuotesClick: () -> Unit,
    onWriteReviewClick: () -> Unit,
    onEditReviewClick: (Long) -> Unit,
    onViewAllReviewsClick: () -> Unit,
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
    var pendingDeleteReviewId by remember { mutableStateOf<Long?>(null) }
    var pendingStatusChange by remember { mutableStateOf<BookStatus?>(null) }
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
                        onStatusClick = { target ->
                            // 완독/중단은 현재 라운드를 끝내는 동작이라, 실수로 눌렀다가 "다시 읽기"로
                            // 되돌려도 진행 페이지가 0부터 다시 계산되는 새 라운드가 시작돼버림 —
                            // 되돌릴 수 없는 결과라 확정 전에 한 번 더 확인.
                            if (target == BookStatus.FINISHED || target == BookStatus.DROPPED) {
                                pendingStatusChange = target
                            } else {
                                viewModel.changeStatus(target)
                            }
                        },
                    )
                }
            }
            item {
                DetailSection(title = "라운드 이력") {
                    if (uiState.rounds.isEmpty()) {
                        Text(
                            text = "아직 라운드가 없어요.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.56f),
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            uiState.rounds.forEach { round ->
                                RoundRow(
                                    round = round,
                                    isExpanded = uiState.expandedRoundId == round.id,
                                    startedAtText = uiState.roundEditStartedAtText,
                                    finishedAtText = uiState.roundEditFinishedAtText,
                                    endReason = uiState.roundEditEndReason,
                                    startingPageText = uiState.roundEditStartingPageText,
                                    onToggleExpand = { viewModel.toggleRoundExpanded(round.id) },
                                    onStartedAtChanged = viewModel::updateRoundEditStartedAt,
                                    onFinishedAtChanged = viewModel::updateRoundEditFinishedAt,
                                    onEndReasonChanged = viewModel::updateRoundEditEndReason,
                                    onStartingPageChanged = viewModel::updateRoundEditStartingPage,
                                    onSave = viewModel::saveRoundEdit,
                                    onCancel = viewModel::cancelRoundEdit,
                                )
                            }
                        }
                    }
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
                DetailSection(
                    title = "인용구",
                    actions = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (uiState.quotes.isNotEmpty()) {
                                TextButton(onClick = onViewAllQuotesClick) {
                                    Text(text = "전체보기")
                                }
                            }
                            IconButton(onClick = onCaptureQuoteClick) {
                                Icon(Icons.Outlined.Add, contentDescription = "인용구 추가")
                            }
                        }
                    },
                ) {
                    if (uiState.editingQuoteId != null) {
                        QuoteEditForm(
                            quoteText = uiState.quoteText,
                            quotePageText = uiState.quotePageText,
                            onQuoteTextChanged = viewModel::updateQuoteText,
                            onQuotePageTextChanged = viewModel::updateQuotePageText,
                            onCancel = viewModel::cancelEditQuote,
                            onSave = viewModel::saveQuote,
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                    if (uiState.quotes.isEmpty()) {
                        Text(
                            text = "저장된 인용구가 없어요.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.56f),
                        )
                    } else {
                        // 최근 3개만 미리보기 — 전체 목록은 "전체보기"에서 검색/필터와 함께 확인(QuoteListScreen).
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            uiState.quotes.take(3).forEach { quote ->
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
                DetailSection(
                    title = "독후감",
                    actions = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (uiState.reviews.isNotEmpty()) {
                                TextButton(onClick = onViewAllReviewsClick) {
                                    Text(text = "전체보기")
                                }
                            }
                            TextButton(onClick = onWriteReviewClick) {
                                Text(text = "작성")
                            }
                        }
                    },
                ) {
                    if (uiState.reviews.isEmpty()) {
                        Text(
                            text = "저장된 독후감이 없어요.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.56f),
                        )
                    } else {
                        // 최근 3개만 미리보기 — 전체 목록은 "전체보기"에서 검색/평점 필터와 함께 확인(ReviewListScreen).
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            uiState.reviews.take(3).forEach { review ->
                                ReviewCard(
                                    review = review,
                                    onEdit = { onEditReviewClick(review.id) },
                                    onDelete = { pendingDeleteReviewId = review.id },
                                )
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

    pendingDeleteReviewId?.let { reviewId ->
        AlertDialog(
            onDismissRequest = { pendingDeleteReviewId = null },
            title = { Text(text = "독후감을 삭제할까요?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDeleteReviewId = null
                        viewModel.deleteReview(reviewId)
                    },
                ) {
                    Text(text = "삭제", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteReviewId = null }) {
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

    pendingStatusChange?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingStatusChange = null },
            title = { Text(text = if (target == BookStatus.FINISHED) "완독으로 표시할까요?" else "읽기를 중단할까요?") },
            text = { Text(text = "지금 라운드가 종료돼요. 나중에 \"다시 읽기\"를 시작하면 새 라운드가 만들어져요 — 날짜나 시작 페이지는 라운드 이력에서 언제든 고칠 수 있어요.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingStatusChange = null
                        viewModel.changeStatus(target)
                    },
                ) {
                    Text(text = if (target == BookStatus.FINISHED) "완독" else "중단", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingStatusChange = null }) {
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

/** Shared by [BookDetailScreen]'s inline "인용구" 섹션 and [QuoteListScreen] — inline edit form for a quote. */
@Composable
internal fun QuoteEditForm(
    quoteText: String,
    quotePageText: String,
    onQuoteTextChanged: (String) -> Unit,
    onQuotePageTextChanged: (String) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit,
) {
    Column {
        Text(
            text = "인용구 수정 중",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = quoteText,
            onValueChange = onQuoteTextChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("인용구") },
            minLines = 2,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = quotePageText,
                onValueChange = onQuotePageTextChanged,
                modifier = Modifier.weight(1f),
                label = { Text("페이지") },
                singleLine = true,
            )
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(onClick = onCancel) {
                Text(text = "취소")
            }
            Spacer(modifier = Modifier.width(4.dp))
            Button(onClick = onSave, shape = RoundedCornerShape(8.dp)) {
                Text(text = "수정 저장")
            }
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    actions: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            actions?.invoke()
        }
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

/**
 * Tap to expand and correct a round's 시작일/종료일/종료 사유/시작 페이지 — this never opens or closes a
 * round (see [EditRoundUseCase]), only fixes its recorded metadata. [startingPage] is the delta baseline
 * for this round's first log (docs/PLAN.md "라운드 이력 편집") — the fix for a round that got split by an
 * accidental 완독/중단 → 다시 읽기 is to correct it here to wherever the reader actually left off.
 */
@Composable
private fun RoundRow(
    round: ReadingRound,
    isExpanded: Boolean,
    startedAtText: String,
    finishedAtText: String,
    endReason: RoundEndReason?,
    startingPageText: String,
    onToggleExpand: () -> Unit,
    onStartedAtChanged: (String) -> Unit,
    onFinishedAtChanged: (String) -> Unit,
    onEndReasonChanged: (RoundEndReason) -> Unit,
    onStartingPageChanged: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    val isOpen = round.finishedAt == null
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleExpand),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(text = "${round.roundNumber}번째 라운드", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = roundPeriodText(round),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.56f),
                )
            }
            Text(
                text = if (isOpen) "읽는 중" else roundEndReasonLabel(round.endReason),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        if (isExpanded) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = startedAtText,
                onValueChange = onStartedAtChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("시작일 (yyyy.MM.dd)") },
                singleLine = true,
            )
            if (!isOpen) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = finishedAtText,
                    onValueChange = onFinishedAtChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("종료일 (yyyy.MM.dd)") },
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(RoundEndReason.COMPLETED to "완독", RoundEndReason.DROPPED to "중단").forEach { (reason, label) ->
                        val selected = endReason == reason
                        Button(
                            onClick = { onEndReasonChanged(reason) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                                contentColor = if (selected) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                            ),
                        ) {
                            Text(text = label)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = startingPageText,
                onValueChange = onStartingPageChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("시작 페이지") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onCancel) {
                    Text(text = "취소")
                }
                Spacer(modifier = Modifier.width(4.dp))
                Button(onClick = onSave, shape = RoundedCornerShape(8.dp)) {
                    Text(text = "저장")
                }
            }
        }
    }
}

private fun roundPeriodText(round: ReadingRound): String {
    val started = formatDate(round.startedAt)
    val finished = round.finishedAt?.let { formatDate(it) } ?: "진행 중"
    return "$started ~ $finished · ${round.startingPage}p부터"
}

private fun roundEndReasonLabel(reason: RoundEndReason?): String = when (reason) {
    RoundEndReason.COMPLETED -> "완독"
    RoundEndReason.DROPPED -> "중단"
    null -> "-"
}

/** Tapping the card toggles between a 4-line preview and the full quote text. Shared with [QuoteListScreen]. */
@Composable
internal fun QuoteCard(quote: Quote, onComments: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
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
internal fun QuoteCommentsSheetContent(
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

/** Collapsed to title + date; tapping the card reveals the full review text. Shared with [ReviewListScreen]. */
@Composable
internal fun ReviewCard(review: Review, onEdit: () -> Unit, onDelete: () -> Unit) {
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
            review.rating?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "★".repeat(it),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = formatDate(review.createdAt),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.56f),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
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

/** Reviews have no separate title field — the first non-blank line stands in for one. */
internal fun reviewTitle(review: Review): String =
    review.content.lineSequence().firstOrNull { it.isNotBlank() }?.trim() ?: "(내용 없음)"

internal fun quotePageLabel(quote: Quote): String? {
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

internal fun formatDate(timestampMillis: Long): String {
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
