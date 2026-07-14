package com.dyk1323.booklogs.ui.detail

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.dyk1323.booklogs.ui.common.theme.BooklogsBodyEmphasisTextStyle
import com.dyk1323.booklogs.ui.common.theme.BooklogsBodyTextStyle
import com.dyk1323.booklogs.ui.common.components.BooklogsCardActions
import com.dyk1323.booklogs.ui.common.theme.BooklogsCaptionEmphasisTextStyle
import com.dyk1323.booklogs.ui.common.theme.BooklogsCaptionTextStyle
import com.dyk1323.booklogs.ui.common.components.BooklogsContentCard
import com.dyk1323.booklogs.ui.common.components.BooklogsFilledButton
import com.dyk1323.booklogs.ui.common.components.BooklogsIconAction
import com.dyk1323.booklogs.ui.common.components.BooklogsInlineTextField
import com.dyk1323.booklogs.ui.common.components.BooklogsLabeledTextField
import com.dyk1323.booklogs.ui.common.components.BooklogsListBlock
import com.dyk1323.booklogs.ui.common.components.BooklogsNumberTextField
import com.dyk1323.booklogs.ui.common.components.BooklogsReadOnlyTextField
import com.dyk1323.booklogs.ui.common.components.BooklogsSegmentButton
import com.dyk1323.booklogs.ui.common.components.BooklogsSegmentRow
import com.dyk1323.booklogs.ui.common.components.BooklogsSheetBottomPadding
import com.dyk1323.booklogs.ui.common.components.BooklogsSheetHorizontalPadding
import com.dyk1323.booklogs.ui.common.theme.BooklogsScreenBackground
import com.dyk1323.booklogs.ui.common.components.BooklogsScreenHorizontalPadding
import com.dyk1323.booklogs.ui.common.components.BooklogsScreenVerticalPadding
import com.dyk1323.booklogs.ui.common.components.BooklogsSectionEmptyText
import com.dyk1323.booklogs.ui.common.theme.BooklogsSectionTitleTextStyle
import com.dyk1323.booklogs.ui.common.theme.BooklogsSurfaceMuted
import com.dyk1323.booklogs.ui.common.components.BooklogsTextAction
import com.dyk1323.booklogs.ui.common.theme.BooklogsTextPrimary
import com.dyk1323.booklogs.ui.common.theme.BooklogsTextSecondary
import com.dyk1323.booklogs.ui.common.theme.BooklogsTitleTextStyle
import com.dyk1323.booklogs.ui.common.components.BooklogsTopBar
import com.dyk1323.booklogs.ui.common.components.booklogsScreenBottomPadding
import com.dyk1323.booklogs.ui.common.components.booklogsScaledDp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    bookId: Long,
    viewModel: BookDetailViewModel,
    onBack: () -> Unit,
    onDeleted: () -> Unit,
    onCaptureQuoteClick: () -> Unit,
    onEditQuoteClick: (Long) -> Unit,
    onViewAllRoundsClick: () -> Unit,
    onViewAllLogsClick: () -> Unit,
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
    var pendingDeleteRoundId by remember { mutableStateOf<Long?>(null) }
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
        containerColor = BooklogsScreenBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            BooklogsTopBar(
                title = "책 상세보기",
                onBack = onBack,
                actions = {
                    if (book != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clickable(onClick = onEditClick),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Outlined.Edit,
                                    contentDescription = "책 정보 수정",
                                    modifier = Modifier.size(24.dp),
                                    tint = BooklogsTextPrimary,
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clickable(onClick = { showDeleteBookDialog = true }),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = "책 삭제",
                                    modifier = Modifier.size(24.dp),
                                    tint = BooklogsTextPrimary,
                                )
                            }
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
                .background(BooklogsScreenBackground)
                .padding(innerPadding)
                .padding(
                    start = BooklogsScreenHorizontalPadding,
                    top = BooklogsScreenVerticalPadding,
                    end = BooklogsScreenHorizontalPadding,
                    bottom = booklogsScreenBottomPadding(),
                ),
            contentPadding = PaddingValues(bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(36.dp),
        ) {
            item { BookHeader(state = uiState) }
            item {
                DetailSection(title = "상태") {
                    StatusActions(
                        status = book.status,
                        onStatusClick = { target ->
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
                DetailSection(
                    title = "라운드 이력",
                    actions = {
                        if (uiState.rounds.size > 3) {
                            BooklogsTextAction(text = "전체 보기", onClick = onViewAllRoundsClick)
                        }
                    },
                ) {
                    if (uiState.rounds.isEmpty()) {
                        BooklogsSectionEmptyText(text = "아직 라운드가 없어요")
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            uiState.rounds.take(3).forEachIndexed { index, round ->
                                RoundRow(
                                    round = round,
                                    highlighted = index == 0,
                                    isExpanded = uiState.expandedRoundId == round.id,
                                    startedAtText = uiState.roundEditStartedAtText,
                                    finishedAtText = uiState.roundEditFinishedAtText,
                                    endReason = uiState.roundEditEndReason,
                                    startingPageText = uiState.roundEditStartingPageText,
                                    message = uiState.message,
                                    onToggleExpand = { viewModel.toggleRoundExpanded(round.id) },
                                    onStartedAtChanged = viewModel::updateRoundEditStartedAt,
                                    onFinishedAtChanged = viewModel::updateRoundEditFinishedAt,
                                    onEndReasonChanged = viewModel::updateRoundEditEndReason,
                                    onStartingPageChanged = viewModel::updateRoundEditStartingPage,
                                    onSave = viewModel::saveRoundEdit,
                                    onCancel = viewModel::cancelRoundEdit,
                                    onDelete = { pendingDeleteRoundId = round.id },
                                )
                            }
                        }
                    }
                }
            }
            item {
                DetailSection(
                    title = "진행 이력",
                    actions = {
                        if (uiState.logDeltas.size > 3) {
                            BooklogsTextAction(text = "전체 보기", onClick = onViewAllLogsClick)
                        }
                    },
                ) {
                    if (uiState.logDeltas.isEmpty()) {
                        BooklogsSectionEmptyText(text = "아직 진행 기록이 없어요")
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            uiState.logDeltas.take(3).forEachIndexed { index, delta ->
                                LogDeltaRow(
                                    book = book,
                                    delta = delta,
                                    highlighted = index == 0,
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
                                BooklogsTextAction(text = "전체 보기", onClick = onViewAllQuotesClick)
                            }
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clickable(onClick = onCaptureQuoteClick),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Outlined.Add,
                                    contentDescription = "인용구 추가",
                                    modifier = Modifier.size(22.dp),
                                    tint = BooklogsTextPrimary,
                                )
                            }
                        }
                    },
                ) {
                    if (uiState.quotes.isEmpty()) {
                        BooklogsSectionEmptyText(text = "저장된 인용구가 없어요")
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            uiState.quotes.take(3).forEach { quote ->
                                QuoteCard(
                                    quote = quote,
                                    commentCount = uiState.quoteCommentCounts[quote.id] ?: 0,
                                    onComments = { viewModel.openComments(quote.id) },
                                    onEdit = { onEditQuoteClick(quote.id) },
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
                                BooklogsTextAction(text = "전체 보기", onClick = onViewAllReviewsClick)
                            }
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clickable(onClick = onWriteReviewClick),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Outlined.Add,
                                    contentDescription = "독후감 작성",
                                    modifier = Modifier.size(22.dp),
                                    tint = BooklogsTextPrimary,
                                )
                            }
                        }
                    },
                ) {
                    if (uiState.reviews.isEmpty()) {
                        BooklogsSectionEmptyText(text = "저장된 독후감이 없어요")
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            uiState.reviews.take(3).forEachIndexed { index, review ->
                                ReviewCard(
                                    review = review,
                                    highlighted = index == 0,
                                    onEdit = { onEditReviewClick(review.id) },
                                    onDelete = { pendingDeleteReviewId = review.id },
                                )
                            }
                        }
                    }
                }
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

    pendingDeleteRoundId?.let { roundId ->
        AlertDialog(
            onDismissRequest = { pendingDeleteRoundId = null },
            title = { Text(text = "라운드를 삭제할까요?") },
            text = { Text(text = "이 라운드에 기록된 진행 이력도 함께 삭제돼요.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDeleteRoundId = null
                        viewModel.deleteRound(roundId)
                    },
                ) {
                    Text(text = "삭제", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteRoundId = null }) {
                    Text(text = "취소")
                }
            },
        )
    }

    if (uiState.expandedCommentsQuoteId != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeComments() },
            containerColor = BooklogsScreenBackground,
            tonalElevation = 0.dp,
        ) {
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
            text = { Text(text = "지금 라운드가 종료돼요. 나중에 \"다시 읽기\"를 시작하면 새 라운드가 만들어져서 날짜와 시작 페이지는 라운드 이력에서 따로 고쳐야 해요.") },
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
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(booklogsScaledDp(8.dp)),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(booklogsScaledDp(4.dp))) {
                Text(
                    text = book.title,
                    style = BooklogsTitleTextStyle,
                    color = BooklogsTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val authorPublisher = listOfNotNull(book.author, book.publisher).joinToString(" · ")
                if (authorPublisher.isNotBlank()) {
                    Text(
                        text = authorPublisher,
                        style = BooklogsCaptionTextStyle,
                        color = BooklogsTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(booklogsScaledDp(4.dp))) {
                Text(
                    text = progressText(state),
                    style = BooklogsBodyTextStyle.copy(fontWeight = FontWeight.Medium),
                    color = BooklogsTextPrimary,
                )
                book.genre?.let {
                    Text(
                        text = it,
                        style = BooklogsCaptionTextStyle,
                        color = BooklogsTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
fun QuoteEditScreen(
    bookId: Long,
    quoteId: Long,
    viewModel: BookDetailViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val quote = uiState.quotes.firstOrNull { it.id == quoteId }
    var editLoaded by remember(quoteId) { mutableStateOf(false) }
    var saveRequested by remember(quoteId) { mutableStateOf(false) }

    DisposableEffect(quoteId) {
        onDispose { viewModel.cancelEditQuote() }
    }
    LaunchedEffect(bookId) {
        viewModel.selectBook(bookId)
    }
    LaunchedEffect(quote?.id) {
        if (!editLoaded && quote != null) {
            viewModel.startEditQuote(quote)
            editLoaded = true
        }
    }
    LaunchedEffect(saveRequested, uiState.editingQuoteId, uiState.message) {
        if (saveRequested && uiState.editingQuoteId == null && uiState.message == "인용구를 수정했어요.") {
            onBack()
        }
    }

    Scaffold(
        containerColor = BooklogsScreenBackground,
        topBar = {
            BooklogsTopBar(
                title = "인용구 수정",
                onBack = {
                    viewModel.cancelEditQuote()
                    onBack()
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(BooklogsScreenBackground)
                .padding(innerPadding)
                .padding(
                    start = BooklogsScreenHorizontalPadding,
                    top = BooklogsScreenVerticalPadding,
                    end = BooklogsScreenHorizontalPadding,
                    bottom = booklogsScreenBottomPadding(),
                ),
        ) {
            if (quote == null && !editLoaded) {
                Text(
                    text = "인용구를 찾지 못했어요.",
                    style = BooklogsBodyTextStyle,
                    color = BooklogsTextSecondary,
                )
                return@Column
            }
            BooklogsNumberTextField(
                value = uiState.quotePageText,
                onValueChange = viewModel::updateQuotePageText,
                label = "현재 페이지",
            )
            Spacer(modifier = Modifier.height(8.dp))
            BooklogsLabeledTextField(
                value = uiState.quoteText,
                onValueChange = viewModel::updateQuoteText,
                label = "최종 인용구",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                singleLine = false,
                fieldWeight = 1f,
                textStyle = BooklogsBodyTextStyle,
            )
            uiState.message?.takeUnless { it == "인용구를 수정했어요." }?.let {
                Spacer(modifier = Modifier.height(booklogsScaledDp(6.dp)))
                Text(
                    text = it,
                    style = BooklogsCaptionTextStyle,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(booklogsScaledDp(8.dp), Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BooklogsFilledButton(
                    text = "취소",
                    onClick = {
                        viewModel.cancelEditQuote()
                        onBack()
                    },
                    compact = true,
                )
                BooklogsFilledButton(
                    text = "수정 저장",
                    onClick = {
                        saveRequested = true
                        viewModel.saveQuote()
                    },
                    primary = true,
                    compact = true,
                )
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = BooklogsSectionTitleTextStyle,
                color = BooklogsTextSecondary,
            )
            actions?.invoke()
        }
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun StatusActions(status: BookStatus, onStatusClick: (BookStatus) -> Unit) {
    BooklogsSegmentRow {
        listOf(BookStatus.READING, BookStatus.FINISHED, BookStatus.PAUSED, BookStatus.DROPPED).forEach { target ->
            BooklogsSegmentButton(
                text = statusTabLabel(target),
                selected = target == status,
                onClick = { if (target != status) onStatusClick(target) },
            )
        }
    }
}

@Composable
internal fun LogDeltaRow(
    book: Book,
    delta: LogDelta,
    highlighted: Boolean,
    isExpanded: Boolean,
    editInputText: String,
    editErrorMessage: String?,
    onToggleExpand: () -> Unit,
    onEditInputChanged: (String) -> Unit,
    onSaveEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val cardShape = RoundedCornerShape(5.dp)
    val cardBackground = when {
        isExpanded -> MaterialTheme.colorScheme.surface
        highlighted -> BooklogsSurfaceMuted
        else -> BooklogsScreenBackground
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = booklogsScaledDp(57.dp))
            .then(if (isExpanded) Modifier.shadow(elevation = 10.dp, shape = cardShape, clip = false) else Modifier)
            .background(cardBackground, cardShape)
            .clickable(onClick = onToggleExpand)
            .animateContentSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        contentAlignment = if (isExpanded) Alignment.TopStart else Alignment.CenterStart,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "p. ${delta.log.currentPage}", style = BooklogsBodyEmphasisTextStyle, color = BooklogsTextPrimary)
                Text(
                    text = formatDate(delta.log.loggedAt),
                    style = BooklogsCaptionTextStyle,
                    color = BooklogsTextSecondary,
                )
            }
            if (isExpanded) {
                Spacer(modifier = Modifier.height(booklogsScaledDp(8.dp)))
                Text(
                    text = deltaLabel(book, delta),
                    style = BooklogsCaptionTextStyle,
                    color = BooklogsTextSecondary,
                )
                Spacer(modifier = Modifier.height(booklogsScaledDp(14.dp)))
                val inputLabel = if (book.format == BookFormat.EBOOK) "진행률" else "페이지"
                val inputSuffix = if (book.format == BookFormat.EBOOK) "%" else "p"
                BooklogsNumberTextField(
                    value = editInputText,
                    onValueChange = onEditInputChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = inputLabel,
                    suffix = inputSuffix,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { onSaveEdit() }),
                    supportingText = editErrorMessage,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = onDelete) {
                        Text(text = "삭제", color = MaterialTheme.colorScheme.error)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(booklogsScaledDp(8.dp))) {
                        BooklogsFilledButton(text = "취소", onClick = onCancelEdit, compact = true)
                        BooklogsFilledButton(text = "수정 저장", onClick = onSaveEdit, primary = true, compact = true)
                    }
                }
            }
        }
    }
}

private fun deltaLabel(book: Book, delta: LogDelta): String {
    val totalPages = book.totalPages
    return if (book.format == BookFormat.EBOOK && totalPages != null) {
        val previousPage = (delta.log.currentPage - delta.pagesRead).coerceAtLeast(0)
        val deltaPercent = ConvertPagePercentUseCase.pageToPercent(delta.log.currentPage, totalPages) -
            ConvertPagePercentUseCase.pageToPercent(previousPage, totalPages)
        "+${deltaPercent}%"
    } else {
        "+${delta.pagesRead}p"
    }
}

@Composable
internal fun RoundRow(
    round: ReadingRound,
    highlighted: Boolean,
    isExpanded: Boolean,
    startedAtText: String,
    finishedAtText: String,
    endReason: RoundEndReason?,
    startingPageText: String,
    message: String?,
    onToggleExpand: () -> Unit,
    onStartedAtChanged: (String) -> Unit,
    onFinishedAtChanged: (String) -> Unit,
    onEndReasonChanged: (RoundEndReason) -> Unit,
    onStartingPageChanged: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
) {
    val isOpen = round.finishedAt == null
    val cardShape = RoundedCornerShape(5.dp)
    val cardBackground = when {
        isExpanded -> MaterialTheme.colorScheme.surface
        highlighted -> BooklogsSurfaceMuted
        else -> BooklogsScreenBackground
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isExpanded) Modifier.shadow(elevation = 10.dp, shape = cardShape, clip = false) else Modifier)
            .background(cardBackground, cardShape)
            .clickable(onClick = onToggleExpand)
            .animateContentSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = "${round.roundNumber}번째 라운드",
                        style = BooklogsBodyEmphasisTextStyle.copy(lineHeight = 19.sp),
                        color = BooklogsTextPrimary,
                    )
                    Spacer(modifier = Modifier.height(booklogsScaledDp(6.dp)))
                    Text(
                        text = roundPeriodText(round),
                        style = BooklogsCaptionTextStyle.copy(lineHeight = 14.sp),
                        color = BooklogsTextSecondary,
                    )
                }
                Text(
                    text = if (isOpen) "읽는 중" else roundEndReasonLabel(round.endReason),
                    modifier = Modifier.padding(start = 8.dp),
                    style = BooklogsCaptionEmphasisTextStyle,
                    color = BooklogsTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (isExpanded) {
                Spacer(modifier = Modifier.height(booklogsScaledDp(14.dp)))
                RoundDatePickerField(
                    value = startedAtText,
                    onValueChange = onStartedAtChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = "시작일",
                )
                if (!isOpen) {
                    Spacer(modifier = Modifier.height(booklogsScaledDp(8.dp)))
                    RoundDatePickerField(
                        value = finishedAtText,
                        onValueChange = onFinishedAtChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = "종료일",
                    )
                    Spacer(modifier = Modifier.height(booklogsScaledDp(8.dp)))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(RoundEndReason.COMPLETED to "완독", RoundEndReason.DROPPED to "중단").forEach { (reason, label) ->
                            val selected = endReason == reason
                            Button(
                                onClick = { onEndReasonChanged(reason) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                ),
                            ) {
                                Text(text = label)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(booklogsScaledDp(8.dp)))
                BooklogsNumberTextField(
                    value = startingPageText,
                    onValueChange = onStartingPageChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = "시작 페이지",
                    suffix = "p",
                )
                message?.let {
                    Spacer(modifier = Modifier.height(booklogsScaledDp(6.dp)))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    if (!isOpen) {
                        TextButton(onClick = onDelete) {
                            Text(text = "삭제", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Row(horizontalArrangement = Arrangement.spacedBy(booklogsScaledDp(8.dp))) {
                        BooklogsFilledButton(text = "취소", onClick = onCancel, compact = true)
                        BooklogsFilledButton(text = "저장", onClick = onSave, primary = true, compact = true)
                    }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoundDatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    var showPicker by remember { mutableStateOf(false) }

    BooklogsReadOnlyTextField(
        value = value,
        label = label,
        onClick = { showPicker = true },
        modifier = modifier,
    )

    if (showPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = value.toDatePickerMillis(),
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedMillis ->
                            onValueChange(selectedMillis.toRoundDateText())
                        }
                        showPicker = false
                    },
                ) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("취소")
                }
            },
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        }
    }
}

@Composable
internal fun QuoteCard(
    quote: Quote,
    commentCount: Int,
    onComments: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var expanded by remember(quote.id) { mutableStateOf(false) }
    BooklogsContentCard(
        highlighted = true,
        onClick = { expanded = !expanded },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = quoteDisplayText(quote),
                style = BooklogsBodyTextStyle,
                color = BooklogsTextPrimary,
                maxLines = if (expanded) Int.MAX_VALUE else 4,
                overflow = TextOverflow.Ellipsis,
            )
            quotePageLabel(quote)?.let {
                Spacer(modifier = Modifier.height(booklogsScaledDp(6.dp)))
                Text(
                    text = it,
                    style = BooklogsCaptionTextStyle,
                    color = BooklogsTextSecondary,
                )
            }
            Spacer(modifier = Modifier.height(booklogsScaledDp(12.dp)))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.clickable(onClick = onComments),
                    horizontalArrangement = Arrangement.spacedBy(booklogsScaledDp(4.dp)),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "댓글",
                        modifier = Modifier.size(20.dp),
                        tint = BooklogsTextSecondary,
                    )
                    Text(
                        text = commentCount.toString(),
                        style = BooklogsCaptionEmphasisTextStyle,
                        color = BooklogsTextSecondary,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(booklogsScaledDp(12.dp))) {
                    BooklogsIconAction(Icons.Outlined.Edit, "인용구 수정", onEdit, iconSize = 24.dp)
                    BooklogsIconAction(Icons.Outlined.Delete, "인용구 삭제", onDelete, iconSize = 24.dp)
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
            .background(BooklogsScreenBackground)
            .padding(horizontal = BooklogsSheetHorizontalPadding)
            .padding(bottom = BooklogsSheetBottomPadding + 48.dp),
    ) {
        Text(text = "댓글", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(booklogsScaledDp(12.dp)))
        if (comments.isEmpty()) {
            Text(
                text = "아직 댓글이 없어요",
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
        Row(
            modifier = Modifier.navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BooklogsInlineTextField(
                value = inputText,
                onValueChange = onInputChanged,
                placeholder = "댓글 추가",
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                label = { Text("댓글 추가") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onAdd() }),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onAdd,
                modifier = Modifier.height(56.dp),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(text = "추가")
            }
        }
    }
}

@Composable
internal fun ReviewCard(
    review: Review,
    highlighted: Boolean = false,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var expanded by remember(review.id) { mutableStateOf(false) }
    BooklogsContentCard(
        highlighted = highlighted,
        onClick = { expanded = !expanded },
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = reviewDisplayText(review),
                style = BooklogsBodyTextStyle,
                color = BooklogsTextPrimary,
                maxLines = if (expanded) Int.MAX_VALUE else 4,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(booklogsScaledDp(6.dp)))
            Text(
                text = formatDate(review.createdAt),
                style = BooklogsCaptionTextStyle,
                color = BooklogsTextSecondary,
            )
            Spacer(modifier = Modifier.height(booklogsScaledDp(12.dp)))
            BooklogsCardActions {
                BooklogsIconAction(Icons.Outlined.Edit, "독후감 수정", onEdit, iconSize = 24.dp)
                BooklogsIconAction(Icons.Outlined.Delete, "독후감 삭제", onDelete, iconSize = 24.dp)
            }
        }
    }
}

internal fun quoteDisplayText(quote: Quote): String =
    quote.text.takeIf { it.isNotBlank() } ?: "(내용 없음)"

internal fun reviewDisplayText(review: Review): String =
    review.content.takeIf { it.isNotBlank() } ?: "(내용 없음)"

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

private val roundEditDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

private fun String.toDatePickerMillis(): Long? =
    try {
        LocalDate.parse(this, roundEditDateFormatter)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
    } catch (_: DateTimeParseException) {
        null
    }

private fun Long.toRoundDateText(): String =
    Instant.ofEpochMilli(this)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
        .format(roundEditDateFormatter)

private fun statusTabLabel(target: BookStatus): String = when (target) {
    BookStatus.READING -> "읽는중"
    BookStatus.FINISHED -> "완독"
    BookStatus.PAUSED -> "일시중지"
    BookStatus.DROPPED -> "중단"
    BookStatus.PLANNED -> "읽을 예정"
}
