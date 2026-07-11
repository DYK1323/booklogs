package com.dyk1323.booklogs.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dyk1323.booklogs.domain.model.Book
import com.dyk1323.booklogs.domain.model.Quote
import com.dyk1323.booklogs.domain.model.ReadingLog
import com.dyk1323.booklogs.domain.model.Review
import com.dyk1323.booklogs.domain.repository.BookRepository
import com.dyk1323.booklogs.domain.repository.QuoteRepository
import com.dyk1323.booklogs.domain.repository.ReadingLogRepository
import com.dyk1323.booklogs.domain.repository.ReadingRoundRepository
import com.dyk1323.booklogs.domain.repository.ReviewRepository
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
    val reviewText: String = "",
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
    private val readingRoundRepository: ReadingRoundRepository,
    private val quoteRepository: QuoteRepository,
    private val reviewRepository: ReviewRepository,
) : ViewModel() {

    private val selectedBookId = MutableStateFlow<Long?>(null)
    private val quoteText = MutableStateFlow("")
    private val quotePageText = MutableStateFlow("")
    private val reviewText = MutableStateFlow("")
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
        reviewText,
        message,
    ) { base, quoteText, quotePageText, reviewText, message ->
        BookDetailUiState(
            book = base.book,
            currentPage = base.currentPage,
            progress = base.progress,
            logDeltas = base.logDeltas,
            quotes = base.quotes,
            reviews = base.reviews,
            quoteText = quoteText,
            quotePageText = quotePageText,
            reviewText = reviewText,
            message = message,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BookDetailUiState(),
    )

    fun selectBook(bookId: Long) {
        selectedBookId.value = bookId
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

    fun updateReviewText(value: String) {
        reviewText.value = value
        message.value = null
    }

    fun saveQuote() {
        val bookId = selectedBookId.value ?: return
        val text = quoteText.value.trim()
        if (text.isEmpty()) {
            message.value = "저장할 인용구를 입력해주세요."
            return
        }
        viewModelScope.launch {
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
            quoteText.value = ""
            quotePageText.value = ""
            message.value = "인용구를 저장했어요."
        }
    }

    fun saveReview() {
        val bookId = selectedBookId.value ?: return
        val text = reviewText.value.trim()
        if (text.isEmpty()) {
            message.value = "저장할 독후감을 입력해주세요."
            return
        }
        viewModelScope.launch {
            val round = readingRoundRepository.getOpenRound(bookId)
                ?: readingRoundRepository.getRoundsForBook(bookId).maxByOrNull { it.roundNumber }
            if (round == null) {
                message.value = "읽기 기록이 있는 책에 독후감을 저장할 수 있어요."
                return@launch
            }
            reviewRepository.insert(
                Review(
                    id = 0,
                    bookId = bookId,
                    readingRoundId = round.id,
                    content = text,
                    rating = null,
                    createdAt = System.currentTimeMillis(),
                ),
            )
            reviewText.value = ""
            message.value = "독후감을 저장했어요."
        }
    }
}
