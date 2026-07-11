package com.dyk1323.booklogs.ui.dashboard

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dyk1323.booklogs.data.settings.AppSettingsDataStore
import com.dyk1323.booklogs.domain.model.Book
import com.dyk1323.booklogs.domain.model.BookFormat
import com.dyk1323.booklogs.domain.model.BookStatus
import com.dyk1323.booklogs.domain.repository.BookRepository
import com.dyk1323.booklogs.domain.repository.ReadingLogRepository
import com.dyk1323.booklogs.domain.repository.ReadingRoundRepository
import com.dyk1323.booklogs.domain.usecase.ConvertPagePercentUseCase
import com.dyk1323.booklogs.domain.usecase.DayPageTotal
import com.dyk1323.booklogs.domain.usecase.LogProgressUseCase
import com.dyk1323.booklogs.domain.usecase.aggregateDailyPages
import com.dyk1323.booklogs.domain.usecase.computeBookProgress
import com.dyk1323.booklogs.ui.quote.QuoteOcrProcessor
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val readingBooks: List<BookShelfItemUi> = emptyList(),
    val weekTotals: List<DayPageTotal> = emptyList(),
    val todayPages: Int = 0,
    val dailyGoalPages: Int? = null,
    val isLoading: Boolean = true,
)

data class BookShelfItemUi(
    val book: Book,
    val currentPage: Int?,
    val progress: Float?,
)

data class QuickLogSheetUiState(
    val book: Book,
    val currentPage: Int?,
    val progress: Float?,
    val inputText: String,
    val isSaving: Boolean,
    val errorMessage: String?,
) {
    val inputLabel: String =
        if (book.format == BookFormat.EBOOK) "현재 진행률" else "현재 페이지"
    val inputSuffix: String =
        if (book.format == BookFormat.EBOOK) "%" else "p"

    // EBOOK progress is a %, not a page number visible on a printed page — camera OCR doesn't apply.
    val showPageCameraButton: Boolean =
        book.format != BookFormat.EBOOK
}

class DashboardViewModel(
    bookRepository: BookRepository,
    readingLogRepository: ReadingLogRepository,
    private val readingRoundRepository: ReadingRoundRepository,
    private val logProgressUseCase: LogProgressUseCase,
    appSettingsDataStore: AppSettingsDataStore,
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) : ViewModel() {

    private val ocrProcessor = QuoteOcrProcessor()

    private val selectedQuickLogBookId = MutableStateFlow<Long?>(null)
    private val quickLogInputText = MutableStateFlow("")
    private val quickLogSaving = MutableStateFlow(false)
    private val quickLogErrorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<DashboardUiState> = combine(
        bookRepository.observeAll(),
        readingLogRepository.observeAll(),
        appSettingsDataStore.settings,
    ) { books, logs, settings ->
        val today = LocalDate.now(zoneId).toEpochDay()
        val weekTotals = aggregateDailyPages(
            allLogs = logs,
            startEpochDay = today - 6,
            endEpochDay = today,
            dailyGoalPages = settings.dailyGoalPages,
        )
        val latestPageByBook = logs
            .groupBy { it.bookId }
            .mapValues { (_, bookLogs) -> bookLogs.maxByOrNull { it.loggedAt }?.currentPage }

        DashboardUiState(
            readingBooks = books
                .filter { it.status == BookStatus.READING }
                .map { book ->
                    val currentPage = latestPageByBook[book.id]
                    BookShelfItemUi(
                        book = book,
                        currentPage = currentPage,
                        progress = computeBookProgress(currentPage, book.totalPages),
                    )
                },
            weekTotals = weekTotals,
            todayPages = weekTotals.lastOrNull()?.totalPages ?: 0,
            dailyGoalPages = settings.dailyGoalPages,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState(),
    )

    val quickLogSheetState: StateFlow<QuickLogSheetUiState?> = combine(
        uiState,
        selectedQuickLogBookId,
        quickLogInputText,
        quickLogSaving,
        quickLogErrorMessage,
    ) { state, selectedBookId, inputText, isSaving, errorMessage ->
        val item = state.readingBooks.firstOrNull { it.book.id == selectedBookId }
        item?.let {
            QuickLogSheetUiState(
                book = it.book,
                currentPage = it.currentPage,
                progress = it.progress,
                inputText = inputText,
                isSaving = isSaving,
                errorMessage = errorMessage,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    fun openQuickLog(bookId: Long) {
        val item = uiState.value.readingBooks.firstOrNull { it.book.id == bookId } ?: return
        selectedQuickLogBookId.value = bookId
        quickLogErrorMessage.value = null
        val totalPages = item.book.totalPages
        quickLogInputText.value = when {
            item.book.format == BookFormat.EBOOK && totalPages != null ->
                ConvertPagePercentUseCase.pageToPercent(item.currentPage ?: 0, totalPages).toString()
            item.currentPage != null -> item.currentPage.toString()
            else -> ""
        }
    }

    fun closeQuickLog() {
        selectedQuickLogBookId.value = null
        quickLogInputText.value = ""
        quickLogSaving.value = false
        quickLogErrorMessage.value = null
    }

    fun updateQuickLogInput(value: String) {
        quickLogInputText.value = value.filter(Char::isDigit).take(4)
        quickLogErrorMessage.value = null
    }

    /** docs/PLAN.md "빠른 기록 UX" — PHYSICAL 책의 카메라 아이콘: 찍은 페이지 사진에서 코너의 숫자를 OCR로 프리필한다. */
    fun prefillQuickLogFromCapture(bitmap: Bitmap) {
        if (selectedQuickLogBookId.value == null) return
        viewModelScope.launch {
            val page = runCatching { ocrProcessor.detectPageNumber(bitmap) }.getOrNull()
            if (page != null) {
                quickLogInputText.value = page.toString().take(4)
                quickLogErrorMessage.value = null
            }
        }
    }

    fun saveQuickLog() {
        val sheet = quickLogSheetState.value ?: return
        val inputValue = sheet.inputText.toIntOrNull()
        if (inputValue == null) {
            quickLogErrorMessage.value = "숫자로 입력해주세요."
            return
        }

        val currentPage = resolveCurrentPage(sheet.book, inputValue) ?: return
        viewModelScope.launch {
            quickLogSaving.value = true
            val openRound = readingRoundRepository.getOpenRound(sheet.book.id)
            if (openRound == null) {
                quickLogSaving.value = false
                quickLogErrorMessage.value = "읽기 시작한 책만 기록할 수 있어요."
                return@launch
            }

            val now = System.currentTimeMillis()
            logProgressUseCase(
                bookId = sheet.book.id,
                readingRoundId = openRound.id,
                currentPage = currentPage,
                loggedAt = now,
                logDateEpochDay = LocalDate.now(zoneId).toEpochDay(),
            )
            closeQuickLog()
        }
    }

    private fun resolveCurrentPage(book: Book, inputValue: Int): Int? {
        val totalPages = book.totalPages
        return if (book.format == BookFormat.EBOOK) {
            if (totalPages == null || totalPages <= 0) {
                quickLogErrorMessage.value = "전자책은 전체 페이지 수가 필요해요."
                null
            } else if (inputValue !in 0..100) {
                quickLogErrorMessage.value = "진행률은 0부터 100까지 입력해주세요."
                null
            } else {
                ConvertPagePercentUseCase.percentToPage(inputValue, totalPages)
            }
        } else {
            if (totalPages != null && inputValue > totalPages) {
                quickLogErrorMessage.value = "전체 페이지보다 큰 값이에요."
                null
            } else {
                inputValue
            }
        }
    }

    override fun onCleared() {
        ocrProcessor.close()
        super.onCleared()
    }
}
