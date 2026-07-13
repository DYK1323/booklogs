package com.dyk1323.booklogs.ui.dashboard

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dyk1323.booklogs.data.settings.AppSettingsDataStore
import com.dyk1323.booklogs.domain.model.Book
import com.dyk1323.booklogs.domain.model.BookFormat
import com.dyk1323.booklogs.domain.model.BookStatus
import com.dyk1323.booklogs.domain.model.ReadingLog
import com.dyk1323.booklogs.domain.repository.BookRepository
import com.dyk1323.booklogs.domain.repository.ReadingLogRepository
import com.dyk1323.booklogs.domain.repository.ReadingRoundRepository
import com.dyk1323.booklogs.domain.usecase.ConvertPagePercentUseCase
import com.dyk1323.booklogs.domain.usecase.DayPageTotal
import com.dyk1323.booklogs.domain.usecase.DeleteLogUseCase
import com.dyk1323.booklogs.domain.usecase.EditLogUseCase
import com.dyk1323.booklogs.domain.usecase.LogProgressUseCase
import com.dyk1323.booklogs.domain.usecase.ResolveLoggedPageResult
import com.dyk1323.booklogs.domain.usecase.aggregateDailyPages
import com.dyk1323.booklogs.domain.usecase.computeBookProgress
import com.dyk1323.booklogs.domain.usecase.resolveLoggedPage
import com.dyk1323.booklogs.ui.quote.QuoteOcrProcessor
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
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
    val latestLog: ReadingLog? = null,
)

data class QuickLogSheetUiState(
    val book: Book,
    val currentPage: Int?,
    val progress: Float?,
    val latestLog: ReadingLog?,
    val inputText: String,
    val isSaving: Boolean,
    val errorMessage: String?,
    val editingLogId: Long?,
    val prefillNonce: Int,
) {
    val inputLabel: String =
        if (book.format == BookFormat.EBOOK) "현재 진행률" else "현재 페이지"
    val inputSuffix: String =
        if (book.format == BookFormat.EBOOK) "%" else "p"

    // EBOOK progress is a %, not a page number visible on a printed page — camera OCR doesn't apply.
    val showPageCameraButton: Boolean =
        book.format != BookFormat.EBOOK

    val isEditingLog: Boolean = editingLogId != null

    val saveButtonLabel: String = when {
        isSaving && isEditingLog -> "수정하는 중"
        isSaving -> "저장 중"
        isEditingLog -> "수정 저장"
        else -> "저장"
    }
}

private data class QuickLogInputState(
    val inputText: String,
    val isSaving: Boolean,
    val errorMessage: String?,
    val editingLogId: Long?,
    val prefillNonce: Int,
)

class DashboardViewModel(
    bookRepository: BookRepository,
    private val readingLogRepository: ReadingLogRepository,
    private val readingRoundRepository: ReadingRoundRepository,
    private val logProgressUseCase: LogProgressUseCase,
    private val editLogUseCase: EditLogUseCase,
    private val deleteLogUseCase: DeleteLogUseCase,
    appSettingsDataStore: AppSettingsDataStore,
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) : ViewModel() {

    private val ocrProcessor = QuoteOcrProcessor()

    private val selectedQuickLogBookId = MutableStateFlow<Long?>(null)
    private val quickLogInputText = MutableStateFlow("")
    private val quickLogSaving = MutableStateFlow(false)
    private val quickLogErrorMessage = MutableStateFlow<String?>(null)
    private val editingLogId = MutableStateFlow<Long?>(null)
    private val quickLogPrefillNonce = MutableStateFlow(0)

    private val _undoLogEvents = Channel<ReadingLog>(Channel.BUFFERED)
    val undoLogEvents: Flow<ReadingLog> = _undoLogEvents.receiveAsFlow()

    private val _quickLogSaveSucceeded = Channel<Unit>(Channel.BUFFERED)
    val quickLogSaveSucceeded: Flow<Unit> = _quickLogSaveSucceeded.receiveAsFlow()

    private val _quickLogSaveThenCaptureQuoteSucceeded = Channel<Long>(Channel.BUFFERED)
    val quickLogSaveThenCaptureQuoteSucceeded: Flow<Long> = _quickLogSaveThenCaptureQuoteSucceeded.receiveAsFlow()

    val uiState: StateFlow<DashboardUiState> = combine(
        bookRepository.observeAll(),
        readingLogRepository.observeAll(),
        readingRoundRepository.observeAll(),
        appSettingsDataStore.settings,
    ) { books, logs, rounds, settings ->
        val today = LocalDate.now(zoneId).toEpochDay()
        val weekTotals = aggregateDailyPages(
            allLogs = logs,
            allRounds = rounds,
            startEpochDay = today - 6,
            endEpochDay = today,
            dailyGoalPages = settings.dailyGoalPages,
        )
        val latestLogByBook = logs
            .groupBy { it.bookId }
            .mapValues { (_, bookLogs) -> bookLogs.maxByOrNull { it.loggedAt } }

        DashboardUiState(
            readingBooks = books
                .filter { it.status == BookStatus.READING }
                .map { book ->
                    val latestLog = latestLogByBook[book.id]
                    BookShelfItemUi(
                        book = book,
                        currentPage = latestLog?.currentPage,
                        progress = computeBookProgress(latestLog?.currentPage, book.totalPages),
                        latestLog = latestLog,
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

    private val quickLogInputState: Flow<QuickLogInputState> = combine(
        quickLogInputText,
        quickLogSaving,
        quickLogErrorMessage,
        editingLogId,
        quickLogPrefillNonce,
    ) { inputText, isSaving, errorMessage, editingId, prefillNonce ->
        QuickLogInputState(inputText, isSaving, errorMessage, editingId, prefillNonce)
    }

    val quickLogSheetState: StateFlow<QuickLogSheetUiState?> = combine(
        uiState,
        selectedQuickLogBookId,
        quickLogInputState,
    ) { state, selectedBookId, input ->
        val item = state.readingBooks.firstOrNull { it.book.id == selectedBookId }
        item?.let {
            QuickLogSheetUiState(
                book = it.book,
                currentPage = it.currentPage,
                progress = it.progress,
                latestLog = it.latestLog,
                inputText = input.inputText,
                isSaving = input.isSaving,
                errorMessage = input.errorMessage,
                editingLogId = input.editingLogId,
                prefillNonce = input.prefillNonce,
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
        editingLogId.value = null
        quickLogInputText.value = inputTextFor(item.book, item.currentPage)
        quickLogPrefillNonce.value++
    }

    fun closeQuickLog() {
        selectedQuickLogBookId.value = null
        quickLogInputText.value = ""
        quickLogSaving.value = false
        quickLogErrorMessage.value = null
        editingLogId.value = null
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
                quickLogPrefillNonce.value++
            }
        }
    }

    /** Loads the most recent log into the input so a mistaken entry can be corrected in place. */
    fun startEditLatestLog() {
        val sheet = quickLogSheetState.value ?: return
        val log = sheet.latestLog ?: return
        editingLogId.value = log.id
        quickLogInputText.value = inputTextFor(sheet.book, log.currentPage)
        quickLogErrorMessage.value = null
        quickLogPrefillNonce.value++
    }

    fun cancelEditLatestLog() {
        val sheet = quickLogSheetState.value ?: return
        editingLogId.value = null
        quickLogInputText.value = inputTextFor(sheet.book, sheet.currentPage)
        quickLogErrorMessage.value = null
        quickLogPrefillNonce.value++
    }

    fun deleteLatestLog() {
        val sheet = quickLogSheetState.value ?: return
        val log = sheet.latestLog ?: return
        viewModelScope.launch {
            deleteLogUseCase(log.id)
            if (editingLogId.value == log.id) {
                editingLogId.value = null
                quickLogInputText.value = ""
            }
            _undoLogEvents.send(log)
        }
    }

    fun undoDeleteLog(log: ReadingLog) {
        viewModelScope.launch {
            readingLogRepository.insert(log.copy(id = 0))
        }
    }

    fun saveQuickLog() {
        saveQuickLog(onSaved = { _quickLogSaveSucceeded.send(Unit) })
    }

    fun saveQuickLogThenCaptureQuote() {
        val sheet = quickLogSheetState.value ?: return
        val unchangedInput =
            sheet.editingLogId == null && sheet.inputText == inputTextFor(sheet.book, sheet.currentPage)
        if (sheet.inputText.isBlank() || unchangedInput) {
            viewModelScope.launch {
                _quickLogSaveThenCaptureQuoteSucceeded.send(sheet.book.id)
            }
            return
        }
        saveQuickLog(onSaved = { bookId -> _quickLogSaveThenCaptureQuoteSucceeded.send(bookId) })
    }

    private fun saveQuickLog(onSaved: suspend (Long) -> Unit) {
        val sheet = quickLogSheetState.value ?: return
        val inputValue = sheet.inputText.toIntOrNull()
        if (inputValue == null) {
            quickLogErrorMessage.value = "숫자로 입력해주세요."
            return
        }

        val resolved = resolveLoggedPage(sheet.book.format, sheet.book.totalPages, inputValue)
        val currentPage = when (resolved) {
            is ResolveLoggedPageResult.Error -> {
                quickLogErrorMessage.value = resolved.message
                return
            }
            is ResolveLoggedPageResult.Success -> resolved.currentPage
        }

        val editingId = sheet.editingLogId
        viewModelScope.launch {
            quickLogSaving.value = true
            if (editingId != null) {
                editLogUseCase(editingId, currentPage)
                quickLogSaving.value = false
                editingLogId.value = null
                onSaved(sheet.book.id)
                return@launch
            }

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
            quickLogSaving.value = false
            onSaved(sheet.book.id)
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

    override fun onCleared() {
        ocrProcessor.close()
        super.onCleared()
    }
}
