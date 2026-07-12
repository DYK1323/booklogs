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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
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
import com.dyk1323.booklogs.ui.common.components.BooklogsListBlock
import com.dyk1323.booklogs.ui.common.components.BooklogsSegmentRow
import com.dyk1323.booklogs.ui.common.components.BooklogsSheetBottomPadding
import com.dyk1323.booklogs.ui.common.components.BooklogsSheetHorizontalPadding
import com.dyk1323.booklogs.ui.common.components.BooklogsScreenBackground
import com.dyk1323.booklogs.ui.common.components.BooklogsTopBar
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val DetailHeaderTitleTextStyle = TextStyle(
    fontSize = 20.sp,
    lineHeight = 20.sp,
    fontWeight = FontWeight.SemiBold,
    letterSpacing = 0.sp,
)

private val DetailHeaderMetaTextStyle = TextStyle(
    fontSize = 12.sp,
    lineHeight = 12.sp,
    fontWeight = FontWeight.Light,
    letterSpacing = 0.sp,
)

private val DetailSectionTitleTextStyle = TextStyle(
    fontSize = 14.sp,
    lineHeight = 14.sp,
    fontWeight = FontWeight.Light,
    letterSpacing = 0.sp,
)

private val DetailProgressTextStyle = TextStyle(
    fontSize = 14.sp,
    lineHeight = 14.sp,
    fontWeight = FontWeight.Medium,
    letterSpacing = 0.sp,
)

private val DetailListPrimaryTextStyle = TextStyle(
    fontSize = 16.sp,
    lineHeight = 16.sp,
    fontWeight = FontWeight.Medium,
    letterSpacing = 0.sp,
)

private val DetailListSecondaryTextStyle = TextStyle(
    fontSize = 12.sp,
    lineHeight = 12.sp,
    fontWeight = FontWeight.Light,
    letterSpacing = 0.sp,
)

private val DetailSectionActionTextStyle = TextStyle(
    fontSize = 14.sp,
    lineHeight = 14.sp,
    fontWeight = FontWeight.Light,
    letterSpacing = 0.sp,
)

private val DetailQuoteTextStyle = TextStyle(
    fontSize = 14.sp,
    lineHeight = 21.sp,
    fontWeight = FontWeight.Light,
    letterSpacing = 0.sp,
)

private val DetailRowStatusTextStyle = TextStyle(
    fontSize = 12.sp,
    lineHeight = 12.sp,
    fontWeight = FontWeight.Medium,
    letterSpacing = 0.sp,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    bookId: Long,
    viewModel: BookDetailViewModel,
    onBack: () -> Unit,
    onDeleted: () -> Unit,
    onCaptureQuoteClick: () -> Unit,
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
                                    tint = Color.Black,
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
                                    tint = Color.Black,
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
                .background(Color.White)
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 20.dp),
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
                            TextButton(onClick = onViewAllRoundsClick) {
                                Text(text = "전체 보기", style = DetailSectionActionTextStyle, color = Color(0xFF0C7EFF))
                            }
                        }
                    },
                ) {
                    if (uiState.rounds.isEmpty()) {
                        Text(
                            text = "아직 라운드가 없어요",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.56f),
                        )
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
                            TextButton(onClick = onViewAllLogsClick) {
                                Text(text = "전체 보기", style = DetailSectionActionTextStyle, color = Color(0xFF0C7EFF))
                            }
                        }
                    },
                ) {
                    if (uiState.logDeltas.isEmpty()) {
                        Text(
                            text = "아직 진행 기록이 없어요",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.56f),
                        )
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
                                TextButton(onClick = onViewAllQuotesClick) {
                                    Text(text = "전체 보기", style = DetailSectionActionTextStyle, color = Color(0xFF0C7EFF))
                                }
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
                                    tint = Color.Black,
                                )
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
                            text = "저장된 인용구가 없어요",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.56f),
                        )
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
                        if (uiState.reviews.isNotEmpty()) {
                            TextButton(onClick = onViewAllReviewsClick) {
                                Text(text = "전체 보기", style = DetailSectionActionTextStyle, color = Color(0xFF0C7EFF))
                            }
                        }
                    },
                ) {
                    if (uiState.reviews.isEmpty()) {
                        Text(
                            text = "저장된 독후감이 없어요",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.56f),
                        )
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
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = book.title,
                    style = DetailHeaderTitleTextStyle,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val authorPublisher = listOfNotNull(book.author, book.publisher).joinToString(" · ")
                if (authorPublisher.isNotBlank()) {
                    Text(
                        text = authorPublisher,
                        style = DetailHeaderMetaTextStyle,
                        color = Color(0xFF757575),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = progressText(state),
                    style = DetailProgressTextStyle,
                    color = Color.Black,
                )
                book.genre?.let {
                    Text(
                        text = it,
                        style = DetailHeaderMetaTextStyle,
                        color = Color(0xFF757575),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

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
            TextButton(onClick = onCancel) { Text(text = "취소") }
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = DetailSectionTitleTextStyle,
                color = Color(0xFF757575),
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
            Button(
                onClick = { if (target != status) onStatusClick(target) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(5.dp),
                enabled = target != status,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (target == status) Color.White else Color.Transparent,
                    contentColor = if (target == status) Color(0xFF111111) else Color(0xFF757575),
                    disabledContainerColor = Color.White,
                    disabledContentColor = Color(0xFF111111),
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                Text(
                    text = statusTabLabel(target),
                    style = TextStyle(
                        fontSize = 16.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 0.sp,
                    ),
                )
            }
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (highlighted) Color(0xFFF5F5F5) else Color.White, RoundedCornerShape(5.dp))
            .clickable(onClick = onToggleExpand)
            .animateContentSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "p. ${delta.log.currentPage}", style = DetailListPrimaryTextStyle, color = Color.Black)
                Text(
                    text = formatDate(delta.log.loggedAt),
                    style = DetailListSecondaryTextStyle,
                    color = Color(0xFF757575),
                )
            }
            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = deltaLabel(book, delta),
                    style = DetailListSecondaryTextStyle,
                    color = Color(0xFF757575),
                )
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
                        TextButton(onClick = onCancelEdit) { Text(text = "취소") }
                        Spacer(modifier = Modifier.width(4.dp))
                        Button(onClick = onSaveEdit, shape = RoundedCornerShape(8.dp)) {
                            Text(text = "수정 저장")
                        }
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (highlighted) Color(0xFFF5F5F5) else Color.White, RoundedCornerShape(5.dp))
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
                Column {
                    Text(text = "${round.roundNumber}번째 라운드", style = DetailListPrimaryTextStyle, color = Color.Black)
                    Text(
                        text = roundPeriodText(round),
                        style = DetailListSecondaryTextStyle,
                        color = Color(0xFF757575),
                    )
                }
                Text(
                    text = if (isOpen) "읽는 중" else roundEndReasonLabel(round.endReason),
                    style = DetailRowStatusTextStyle,
                    color = Color.Black,
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
                                    containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
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
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    if (!isOpen) {
                        TextButton(onClick = onDelete) {
                            Text(text = "삭제", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = onCancel) { Text(text = "취소") }
                    Spacer(modifier = Modifier.width(4.dp))
                    Button(onClick = onSave, shape = RoundedCornerShape(8.dp)) {
                        Text(text = "저장")
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

@Composable
internal fun QuoteCard(quote: Quote, onComments: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    var expanded by remember(quote.id) { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5), RoundedCornerShape(5.dp))
            .clickable { expanded = !expanded }
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 116.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f, fill = false),
            ) {
                Text(
                    text = quote.text,
                    style = DetailQuoteTextStyle,
                    color = Color.Black,
                    maxLines = if (expanded) Int.MAX_VALUE else 4,
                    overflow = TextOverflow.Ellipsis,
                )
                quotePageLabel(quote)?.let {
                    Text(
                        text = it,
                        style = DetailListSecondaryTextStyle,
                        color = Color(0xFF757575),
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(onClick = onComments),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Outlined.ChatBubbleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFF757575),
                    )
                }
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(onClick = onEdit),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Outlined.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Color(0xFF757575),
                    )
                }
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(onClick = onDelete),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Color(0xFF757575),
                    )
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
            .padding(bottom = BooklogsSheetBottomPadding),
    ) {
        Text(text = "댓글", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(12.dp))
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
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChanged,
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (highlighted) Color(0xFFF5F5F5) else Color.White, RoundedCornerShape(5.dp))
            .clickable { expanded = !expanded }
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (expanded) {
                Text(text = review.content, style = DetailQuoteTextStyle, color = Color.Black)
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = reviewTitle(review),
                        modifier = Modifier.weight(1f),
                        style = DetailListPrimaryTextStyle,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = formatDate(review.createdAt),
                        style = DetailListSecondaryTextStyle,
                        color = Color(0xFF757575),
                    )
                }
            }
            if (expanded) {
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
                    style = DetailListSecondaryTextStyle,
                    color = Color(0xFF757575),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onEdit, contentPadding = PaddingValues(0.dp)) {
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color(0xFF757575),
                        )
                    }
                    TextButton(onClick = onDelete, contentPadding = PaddingValues(0.dp)) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color(0xFF757575),
                        )
                    }
                }
            }
        }
    }
}

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

private fun statusTabLabel(target: BookStatus): String = when (target) {
    BookStatus.READING -> "읽는중"
    BookStatus.FINISHED -> "완독"
    BookStatus.PAUSED -> "일시중지"
    BookStatus.DROPPED -> "중단"
    BookStatus.PLANNED -> "읽을 예정"
}
