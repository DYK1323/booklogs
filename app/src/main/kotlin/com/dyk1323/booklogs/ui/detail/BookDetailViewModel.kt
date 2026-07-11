package com.dyk1323.booklogs.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dyk1323.booklogs.domain.model.Book
import com.dyk1323.booklogs.domain.model.BookStatus
import com.dyk1323.booklogs.domain.model.Quote
import com.dyk1323.booklogs.domain.model.ReadingLog
import com.dyk1323.booklogs.domain.model.Review
import com.dyk1323.booklogs.domain.repository.BookRepository
import com.dyk1323.booklogs.domain.repository.QuoteRepository
import com.dyk1323.booklogs.domain.repository.ReadingLogRepository
import com.dyk1323.booklogs.domain.repository.ReviewRepository
import com.dyk1323.booklogs.domain.usecase.ChangeBookStatusUseCase
import com.dyk1323.booklogs.domain.usecase.DeleteBookUseCase
import com.dyk1323.booklogs.domain.usecase.DeleteLogUseCase
import com.dyk1323.booklogs.domain.usecase.LogDelta
import com.dyk1323.booklogs.domain.usecase.computeBookProgress
import com.dyk1323.booklogs.domain.usecase.computeLogDeltas
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BookDetailUiState(
    val book: Book? = null,
    val currentPage: Int? = null,
    val progress: Float? = null,
    val logDeltas: List<LogDelta> = emptyList(),
    val quotes: List<Quote> = emptyList(),
    val reviews: List<Review> = emptyList(),
    val quoteText: String = "",
    val quotePageText: String = "",
    val editingQuoteId: Long? = null,
    val message: String? = null,
)

private data class BookDetailBaseState(
    val book: Book?,
    val currentPage: Int?,
    val progress: Float?,
    val logDeltas: List<LogDelta>,
    val quotes: List<Quote>,
    val reviews: List<Review>,
)

@OptIn(ExperimentalCoroutinesApi::class)
class BookDetailViewModel(
    private val bookRepository: BookRepository,
    private val readingLogRepository: ReadingLogRepository,
    private val quoteRepository: QuoteRepository,
    private val reviewRepository: ReviewRepository,
    private val changeBookStatusUseCase: ChangeBookStatusUseCase,
    private val deleteBookUseCase: DeleteBookUseCase,
    private val deleteLogUseCase: DeleteLogUseCase,
) : ViewModel() {

    private val selectedBookId = MutableStateFlow<Long?>(null)
    private val quoteText = MutableStateFlow("")
    private val quotePageText = MutableStateFlow("")
    private val editingQuote = MutableStateFlow<Quote?>(null)
    private val message = MutableStateFlow<String?>(null)

    private val quotes = selectedBookId.flatMapLatest { bookId ->
        if (bookId == null) flowOf(emptyList()) else quoteRepository.observeForBook(bookId)
    }

    private val reviews = selectedBookId.flatMapLatest { bookId ->
        if (bookId == null) flowOf(emptyList()) else reviewRepository.observeForBook(bookId)
    }

    private val baseState = combine(
        selectedBookId,
        bookRepository.observeAll(),
        readingLogRepository.observeAll(),
        quotes,
        reviews,
    ) { bookId, books, logs, quoteList, reviewList ->
        val book = books.firstOrNull { it.id == bookId }
        val bookLogs = logs.filter { it.bookId == bookId }
        val latestPage = bookLogs.maxByOrNull { it.loggedAt }?.currentPage
        BookDetailBaseState(
            book = book,
            currentPage = latestPage,
            progress = computeBookProgress(latestPage, book?.totalPages),
            logDeltas = bookLogs.groupBy { it.readingRoundId }.values.flatMap(::computeLogDeltas)
                .sortedByDescending { it.log.loggedAt },
            quotes = quoteList,
            reviews = reviewList,
        )
    }

    val uiState: StateFlow<BookDetailUiState> = combine(
        baseState,
        quoteText,
        quotePageText,
        editingQuote,
        message,
    ) { base, quoteText, quotePageText, editingQuote, message ->
        BookDetailUiState(
            book = base.book,
            currentPage = base.currentPage,
            progress = base.progress,
            logDeltas = base.logDeltas,
            quotes = base.quotes,
            reviews = base.reviews,
            quoteText = quoteText,
            quotePageText = quotePageText,
            editingQuoteId = editingQuote?.id,
            message = message,
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

    fun saveQuote() {
        val bookId = selectedBookId.value ?: return
        val text = quoteText.value.trim()
        if (text.isEmpty()) {
            message.value = "저장할 인용구를 입력해주세요."
            return
        }
        val editing = editingQuote.value
        viewModelScope.launch {
            if (editing != null) {
                // pageNumberEnd/createdAt are preserved from the original quote — this inline form
                // only edits text/single page number, not the multi-page-capture range.
                quoteRepository.update(
                    editing.copy(text = text, pageNumber = quotePageText.value.toIntOrNull()),
                )
                message.value = "인용구를 수정했어요."
            } else {
                quoteRepository.insert(
                    Quote(
                        id = 0,
                        bookId = bookId,
                        text = text,
                        pageNumber = quotePageText.value.toIntOrNull(),
                        pageNumberEnd = null,
                        createdAt = System.currentTimeMillis(),
                    ),
                )
                message.value = "인용구를 저장했어요."
            }
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

    fun deleteLog(logId: Long) {
        viewModelScope.launch {
            deleteLogUseCase(logId)
            message.value = "진행 기록을 삭제했어요."
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
            message.value = "인용구를 삭제했어요."
        }
    }
}

private fun statusLabel(status: BookStatus): String = when (status) {
    BookStatus.READING -> "읽는 중"
    BookStatus.PLANNED -> "읽을 예정"
    BookStatus.PAUSED -> "멈춤"
    BookStatus.FINISHED -> "완독"
    BookStatus.DROPPED -> "중단"
}
