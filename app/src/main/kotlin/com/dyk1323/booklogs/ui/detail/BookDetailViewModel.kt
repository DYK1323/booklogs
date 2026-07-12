package com.dyk1323.booklogs.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dyk1323.booklogs.domain.model.Book
import com.dyk1323.booklogs.domain.model.BookFormat
import com.dyk1323.booklogs.domain.model.BookStatus
import com.dyk1323.booklogs.domain.model.Quote
import com.dyk1323.booklogs.domain.model.QuoteComment
import com.dyk1323.booklogs.domain.model.ReadingLog
import com.dyk1323.booklogs.domain.model.ReadingRound
import com.dyk1323.booklogs.domain.model.Review
import com.dyk1323.booklogs.domain.model.RoundEndReason
import com.dyk1323.booklogs.domain.repository.BookRepository
import com.dyk1323.booklogs.domain.repository.QuoteCommentRepository
import com.dyk1323.booklogs.domain.repository.QuoteRepository
import com.dyk1323.booklogs.domain.repository.ReadingLogRepository
import com.dyk1323.booklogs.domain.repository.ReadingRoundRepository
import com.dyk1323.booklogs.domain.repository.ReviewRepository
import com.dyk1323.booklogs.domain.usecase.ChangeBookStatusUseCase
import com.dyk1323.booklogs.domain.usecase.ConvertPagePercentUseCase
import com.dyk1323.booklogs.domain.usecase.DeleteBookUseCase
import com.dyk1323.booklogs.domain.usecase.DeleteLogUseCase
import com.dyk1323.booklogs.domain.usecase.DeleteRoundUseCase
import com.dyk1323.booklogs.domain.usecase.EditLogUseCase
import com.dyk1323.booklogs.domain.usecase.EditRoundUseCase
import com.dyk1323.booklogs.domain.usecase.LogDelta
import com.dyk1323.booklogs.domain.usecase.ResolveLoggedPageResult
import com.dyk1323.booklogs.domain.usecase.computeBookProgress
import com.dyk1323.booklogs.domain.usecase.computeLogDeltas
import com.dyk1323.booklogs.domain.usecase.resolveLoggedPage
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BookDetailUiState(
    val book: Book? = null,
    val currentPage: Int? = null,
    val progress: Float? = null,
    val logDeltas: List<LogDelta> = emptyList(),
    val quotes: List<Quote> = emptyList(),
    val reviews: List<Review> = emptyList(),
    val rounds: List<ReadingRound> = emptyList(),
    val quoteText: String = "",
    val quotePageText: String = "",
    val editingQuoteId: Long? = null,
    val expandedLogId: Long? = null,
    val logEditInputText: String = "",
    val logEditErrorMessage: String? = null,
    val expandedCommentsQuoteId: Long? = null,
    val comments: List<QuoteComment> = emptyList(),
    val commentInputText: String = "",
    val expandedRoundId: Long? = null,
    val roundEditStartedAtText: String = "",
    val roundEditFinishedAtText: String = "",
    val roundEditEndReason: RoundEndReason? = null,
    val roundEditStartingPageText: String = "",
    val message: String? = null,
)

private data class QuoteFormState(
    val quoteText: String,
    val quotePageText: String,
    val editingQuoteId: Long?,
    val message: String?,
)

private data class LogEditState(
    val expandedLogId: Long?,
    val logEditInputText: String,
    val logEditErrorMessage: String?,
)

private data class CommentState(
    val expandedCommentsQuoteId: Long?,
    val comments: List<QuoteComment>,
    val commentInputText: String,
)

private data class RoundEditState(
    val expandedRoundId: Long?,
    val startedAtText: String,
    val finishedAtText: String,
    val endReason: RoundEndReason?,
    val startingPageText: String,
)

private data class QuoteReviewRoundState(
    val quotes: List<Quote>,
    val reviews: List<Review>,
    val rounds: List<ReadingRound>,
)

private data class BookDetailBaseState(
    val book: Book?,
    val currentPage: Int?,
    val progress: Float?,
    val logDeltas: List<LogDelta>,
    val quotes: List<Quote>,
    val reviews: List<Review>,
    val rounds: List<ReadingRound>,
)

private val ROUND_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

@OptIn(ExperimentalCoroutinesApi::class)
class BookDetailViewModel(
    private val bookRepository: BookRepository,
    private val readingLogRepository: ReadingLogRepository,
    private val readingRoundRepository: ReadingRoundRepository,
    private val quoteRepository: QuoteRepository,
    private val reviewRepository: ReviewRepository,
    private val quoteCommentRepository: QuoteCommentRepository,
    private val changeBookStatusUseCase: ChangeBookStatusUseCase,
    private val deleteBookUseCase: DeleteBookUseCase,
    private val deleteLogUseCase: DeleteLogUseCase,
    private val editLogUseCase: EditLogUseCase,
    private val editRoundUseCase: EditRoundUseCase,
    private val deleteRoundUseCase: DeleteRoundUseCase,
) : ViewModel() {

    private val selectedBookId = MutableStateFlow<Long?>(null)
    private val quoteText = MutableStateFlow("")
    private val quotePageText = MutableStateFlow("")
    private val editingQuote = MutableStateFlow<Quote?>(null)
    private val message = MutableStateFlow<String?>(null)
    private val expandedLogId = MutableStateFlow<Long?>(null)
    private val logEditInputText = MutableStateFlow("")
    private val logEditErrorMessage = MutableStateFlow<String?>(null)
    private val expandedCommentsQuoteId = MutableStateFlow<Long?>(null)
    private val commentInputText = MutableStateFlow("")
    private val expandedRoundId = MutableStateFlow<Long?>(null)
    private val roundEditStartedAtText = MutableStateFlow("")
    private val roundEditFinishedAtText = MutableStateFlow("")
    private val roundEditEndReason = MutableStateFlow<RoundEndReason?>(null)
    private val roundEditStartingPageText = MutableStateFlow("")

    private val _undoLogEvents = Channel<ReadingLog>(Channel.BUFFERED)
    val undoLogEvents: Flow<ReadingLog> = _undoLogEvents.receiveAsFlow()

    private val quotes = selectedBookId.flatMapLatest { bookId ->
        if (bookId == null) flowOf(emptyList()) else quoteRepository.observeForBook(bookId)
    }

    private val reviews = selectedBookId.flatMapLatest { bookId ->
        if (bookId == null) flowOf(emptyList()) else reviewRepository.observeForBook(bookId)
    }

    private val rounds = selectedBookId.flatMapLatest { bookId ->
        if (bookId == null) flowOf(emptyList()) else readingRoundRepository.observeForBook(bookId)
    }

    private val comments = expandedCommentsQuoteId.flatMapLatest { quoteId ->
        if (quoteId == null) flowOf(emptyList()) else quoteCommentRepository.observeForQuote(quoteId)
    }

    // combine() only has typed overloads up to 5 flows — quotes/reviews/rounds are bundled here first
    // so the outer baseState combine below stays within that limit.
    private val quotesReviewsRounds: Flow<QuoteReviewRoundState> = combine(
        quotes,
        reviews,
        rounds,
    ) { quoteList, reviewList, roundList ->
        QuoteReviewRoundState(quoteList, reviewList, roundList)
    }

    private val baseState = combine(
        selectedBookId,
        bookRepository.observeAll(),
        readingLogRepository.observeAll(),
        quotesReviewsRounds,
    ) { bookId, books, logs, qrr ->
        val book = books.firstOrNull { it.id == bookId }
        val bookLogs = logs.filter { it.bookId == bookId }
        val latestPage = bookLogs.maxByOrNull { it.loggedAt }?.currentPage
        val startingPageByRound = qrr.rounds.associate { it.id to it.startingPage }
        BookDetailBaseState(
            book = book,
            currentPage = latestPage,
            progress = computeBookProgress(latestPage, book?.totalPages),
            logDeltas = bookLogs.groupBy { it.readingRoundId }
                .flatMap { (roundId, roundLogs) -> computeLogDeltas(roundLogs, startingPageByRound[roundId] ?: 0) }
                .sortedByDescending { it.log.loggedAt },
            quotes = qrr.quotes,
            reviews = qrr.reviews,
            rounds = qrr.rounds.sortedByDescending { it.roundNumber },
        )
    }

    private val quoteFormState: Flow<QuoteFormState> = combine(
        quoteText,
        quotePageText,
        editingQuote,
        message,
    ) { quoteText, quotePageText, editingQuote, message ->
        QuoteFormState(quoteText, quotePageText, editingQuote?.id, message)
    }

    private val logEditState: Flow<LogEditState> = combine(
        expandedLogId,
        logEditInputText,
        logEditErrorMessage,
    ) { expandedLogId, logEditInputText, logEditErrorMessage ->
        LogEditState(expandedLogId, logEditInputText, logEditErrorMessage)
    }

    private val commentState: Flow<CommentState> = combine(
        expandedCommentsQuoteId,
        comments,
        commentInputText,
    ) { expandedCommentsQuoteId, comments, commentInputText ->
        CommentState(expandedCommentsQuoteId, comments, commentInputText)
    }

    private val roundEditState: Flow<RoundEditState> = combine(
        expandedRoundId,
        roundEditStartedAtText,
        roundEditFinishedAtText,
        roundEditEndReason,
        roundEditStartingPageText,
    ) { expandedRoundId, startedAtText, finishedAtText, endReason, startingPageText ->
        RoundEditState(expandedRoundId, startedAtText, finishedAtText, endReason, startingPageText)
    }

    val uiState: StateFlow<BookDetailUiState> = combine(
        baseState,
        quoteFormState,
        logEditState,
        commentState,
        roundEditState,
    ) { base, quoteForm, logEdit, commentForm, roundEdit ->
        BookDetailUiState(
            book = base.book,
            currentPage = base.currentPage,
            progress = base.progress,
            logDeltas = base.logDeltas,
            quotes = base.quotes,
            reviews = base.reviews,
            rounds = base.rounds,
            quoteText = quoteForm.quoteText,
            quotePageText = quoteForm.quotePageText,
            editingQuoteId = quoteForm.editingQuoteId,
            expandedLogId = logEdit.expandedLogId,
            logEditInputText = logEdit.logEditInputText,
            logEditErrorMessage = logEdit.logEditErrorMessage,
            expandedCommentsQuoteId = commentForm.expandedCommentsQuoteId,
            comments = commentForm.comments,
            commentInputText = commentForm.commentInputText,
            expandedRoundId = roundEdit.expandedRoundId,
            roundEditStartedAtText = roundEdit.startedAtText,
            roundEditFinishedAtText = roundEdit.finishedAtText,
            roundEditEndReason = roundEdit.endReason,
            roundEditStartingPageText = roundEdit.startingPageText,
            message = quoteForm.message,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BookDetailUiState(),
    )

    fun selectBook(bookId: Long) {
        selectedBookId.value = bookId
        editingQuote.value = null
        quoteText.value = ""
        quotePageText.value = ""
        expandedCommentsQuoteId.value = null
        commentInputText.value = ""
        collapseRoundEdit()
        message.value = null
    }

    fun updateQuoteText(value: String) {
        quoteText.value = value
        message.value = null
    }

    fun updateQuotePageText(value: String) {
        quotePageText.value = value.filter(Char::isDigit).take(4)
        message.value = null
    }

    /** Loads an existing quote's text/page into the inline add form, switching [saveQuote] to update mode. */
    fun startEditQuote(quote: Quote) {
        editingQuote.value = quote
        quoteText.value = quote.text
        quotePageText.value = quote.pageNumber?.toString().orEmpty()
        message.value = null
    }

    fun cancelEditQuote() {
        editingQuote.value = null
        quoteText.value = ""
        quotePageText.value = ""
        message.value = null
    }

    /** Inline editing only — adding a new quote now always goes through the camera/album/직접 입력 flow. */
    fun saveQuote() {
        val editing = editingQuote.value ?: return
        val text = quoteText.value.trim()
        if (text.isEmpty()) {
            message.value = "저장할 인용구를 입력해주세요."
            return
        }
        viewModelScope.launch {
            // pageNumberEnd/createdAt are preserved from the original quote — this inline form
            // only edits text/single page number, not the multi-page-capture range.
            quoteRepository.update(
                editing.copy(text = text, pageNumber = quotePageText.value.toIntOrNull()),
            )
            message.value = "인용구를 수정했어요."
            editingQuote.value = null
            quoteText.value = ""
            quotePageText.value = ""
        }
    }

    fun changeStatus(newStatus: BookStatus) {
        val bookId = selectedBookId.value ?: return
        viewModelScope.launch {
            val result = changeBookStatusUseCase(
                bookId = bookId,
                newStatus = newStatus,
                now = System.currentTimeMillis(),
            )
            message.value = result.fold(
                onSuccess = { "상태를 ${statusLabel(newStatus)}으로 변경했어요." },
                onFailure = { error ->
                    error.message?.let { "상태를 변경하지 못했어요. $it" } ?: "상태를 변경하지 못했어요."
                },
            )
        }
    }

    fun deleteBook(onDeleted: () -> Unit) {
        val bookId = selectedBookId.value ?: return
        viewModelScope.launch {
            deleteBookUseCase(bookId)
            selectedBookId.value = null
            onDeleted()
        }
    }

    fun toggleLogExpanded(logId: Long) {
        if (expandedLogId.value == logId) {
            expandedLogId.value = null
            logEditInputText.value = ""
            logEditErrorMessage.value = null
            return
        }
        val book = uiState.value.book ?: return
        val log = uiState.value.logDeltas.firstOrNull { it.log.id == logId }?.log ?: return
        expandedLogId.value = logId
        logEditInputText.value = inputTextFor(book, log.currentPage)
        logEditErrorMessage.value = null
    }

    fun updateLogEditInput(value: String) {
        logEditInputText.value = value.filter(Char::isDigit).take(4)
        logEditErrorMessage.value = null
    }

    fun cancelLogEdit() {
        expandedLogId.value = null
        logEditInputText.value = ""
        logEditErrorMessage.value = null
    }

    fun saveLogEdit() {
        val logId = expandedLogId.value ?: return
        val book = uiState.value.book ?: return
        val inputValue = logEditInputText.value.toIntOrNull()
        if (inputValue == null) {
            logEditErrorMessage.value = "숫자로 입력해주세요."
            return
        }
        val resolved = resolveLoggedPage(book.format, book.totalPages, inputValue)
        val currentPage = when (resolved) {
            is ResolveLoggedPageResult.Error -> {
                logEditErrorMessage.value = resolved.message
                return
            }
            is ResolveLoggedPageResult.Success -> resolved.currentPage
        }
        viewModelScope.launch {
            editLogUseCase(logId, currentPage)
            expandedLogId.value = null
            logEditInputText.value = ""
            logEditErrorMessage.value = null
            message.value = "진행 기록을 수정했어요."
        }
    }

    fun deleteLog(logId: Long) {
        val log = uiState.value.logDeltas.firstOrNull { it.log.id == logId }?.log ?: return
        viewModelScope.launch {
            deleteLogUseCase(logId)
            if (expandedLogId.value == logId) {
                expandedLogId.value = null
                logEditInputText.value = ""
                logEditErrorMessage.value = null
            }
            message.value = "진행 기록을 삭제했어요."
            _undoLogEvents.send(log)
        }
    }

    fun undoDeleteLog(log: ReadingLog) {
        viewModelScope.launch {
            readingLogRepository.insert(log.copy(id = 0))
        }
    }

    private fun inputTextFor(book: Book, currentPage: Int?): String {
        val totalPages = book.totalPages
        return when {
            book.format == BookFormat.EBOOK && totalPages != null ->
                ConvertPagePercentUseCase.pageToPercent(currentPage ?: 0, totalPages).toString()
            currentPage != null -> currentPage.toString()
            else -> ""
        }
    }

    fun deleteReview(reviewId: Long) {
        viewModelScope.launch {
            reviewRepository.deleteById(reviewId)
            message.value = "독후감을 삭제했어요."
        }
    }

    fun deleteQuote(quoteId: Long) {
        viewModelScope.launch {
            quoteRepository.deleteById(quoteId)
            if (editingQuote.value?.id == quoteId) {
                editingQuote.value = null
                quoteText.value = ""
                quotePageText.value = ""
            }
            if (expandedCommentsQuoteId.value == quoteId) {
                expandedCommentsQuoteId.value = null
                commentInputText.value = ""
            }
            message.value = "인용구를 삭제했어요."
        }
    }

    fun openComments(quoteId: Long) {
        expandedCommentsQuoteId.value = quoteId
        commentInputText.value = ""
    }

    fun closeComments() {
        expandedCommentsQuoteId.value = null
        commentInputText.value = ""
    }

    fun updateCommentInput(value: String) {
        commentInputText.value = value
    }

    fun addComment() {
        val quoteId = expandedCommentsQuoteId.value ?: return
        val text = commentInputText.value.trim()
        if (text.isEmpty()) return
        viewModelScope.launch {
            quoteCommentRepository.insert(
                QuoteComment(id = 0, quoteId = quoteId, content = text, createdAt = System.currentTimeMillis()),
            )
            commentInputText.value = ""
        }
    }

    fun deleteComment(commentId: Long) {
        viewModelScope.launch {
            quoteCommentRepository.deleteById(commentId)
        }
    }

    /** Expands a round row for editing, seeding the fields from its current values. */
    fun toggleRoundExpanded(roundId: Long) {
        if (expandedRoundId.value == roundId) {
            collapseRoundEdit()
            return
        }
        val round = uiState.value.rounds.firstOrNull { it.id == roundId } ?: return
        expandedRoundId.value = roundId
        roundEditStartedAtText.value = millisToDateText(round.startedAt)
        roundEditFinishedAtText.value = round.finishedAt?.let(::millisToDateText).orEmpty()
        roundEditEndReason.value = round.endReason
        roundEditStartingPageText.value = round.startingPage.toString()
        message.value = null
    }

    fun updateRoundEditStartedAt(value: String) {
        roundEditStartedAtText.value = value
        message.value = null
    }

    fun updateRoundEditFinishedAt(value: String) {
        roundEditFinishedAtText.value = value
        message.value = null
    }

    fun updateRoundEditEndReason(reason: RoundEndReason) {
        roundEditEndReason.value = reason
    }

    fun updateRoundEditStartingPage(value: String) {
        roundEditStartingPageText.value = value.filter(Char::isDigit).take(6)
        message.value = null
    }

    fun cancelRoundEdit() {
        collapseRoundEdit()
        message.value = null
    }

    /**
     * Only ever corrects a round's own fields — see [EditRoundUseCase] for why this can never flip a
     * round between open/closed (that stays the status state machine's job).
     */
    fun saveRoundEdit() {
        val roundId = expandedRoundId.value ?: return
        val round = uiState.value.rounds.firstOrNull { it.id == roundId } ?: return
        val startedAt = dateTextToMillis(roundEditStartedAtText.value)
        if (startedAt == null) {
            message.value = "시작일을 yyyy.MM.dd 형식으로 입력해주세요."
            return
        }
        val isOpen = round.finishedAt == null
        val finishedAt = if (isOpen) null else dateTextToMillis(roundEditFinishedAtText.value)
        if (!isOpen && finishedAt == null) {
            message.value = "종료일을 yyyy.MM.dd 형식으로 입력해주세요."
            return
        }
        val endReason = if (isOpen) null else (roundEditEndReason.value ?: round.endReason)
        val startingPage = roundEditStartingPageText.value.toIntOrNull()
        if (startingPage == null || startingPage < 0) {
            message.value = "시작 페이지를 숫자로 입력해주세요."
            return
        }
        viewModelScope.launch {
            val result = editRoundUseCase(
                roundId = roundId,
                startedAt = startedAt,
                finishedAt = finishedAt,
                endReason = endReason,
                startingPage = startingPage,
            )
            message.value = result.fold(
                onSuccess = { "라운드 정보를 수정했어요." },
                onFailure = { "라운드 정보를 수정하지 못했어요." },
            )
            if (result.isSuccess) collapseRoundEdit()
        }
    }

    /** Only closed rounds can be deleted here — see [DeleteRoundUseCase] for why the open round is protected. */
    fun deleteRound(roundId: Long) {
        viewModelScope.launch {
            val result = deleteRoundUseCase(roundId)
            message.value = result.fold(
                onSuccess = { "라운드를 삭제했어요." },
                onFailure = { "라운드를 삭제하지 못했어요." },
            )
            if (result.isSuccess && expandedRoundId.value == roundId) collapseRoundEdit()
        }
    }

    private fun collapseRoundEdit() {
        expandedRoundId.value = null
        roundEditStartedAtText.value = ""
        roundEditFinishedAtText.value = ""
        roundEditEndReason.value = null
        roundEditStartingPageText.value = ""
    }

    private fun millisToDateText(millis: Long): String =
        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate().format(ROUND_DATE_FORMATTER)

    private fun dateTextToMillis(text: String): Long? = runCatching {
        LocalDate.parse(text.trim(), ROUND_DATE_FORMATTER).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }.getOrNull()
}

private fun statusLabel(status: BookStatus): String = when (status) {
    BookStatus.READING -> "읽는 중"
    BookStatus.PLANNED -> "읽을 예정"
    BookStatus.PAUSED -> "멈춤"
    BookStatus.FINISHED -> "완독"
    BookStatus.DROPPED -> "중단"
}
