package com.dyk1323.booklogs.ui.quote

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dyk1323.booklogs.domain.model.Quote
import com.dyk1323.booklogs.domain.repository.QuoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CapturedQuotePage(
    val order: Int,
    val text: String,
    val pageText: String,
)

/** Pure join of captured pages into the final quote text, in capture order. No Android deps — unit testable. */
fun joinQuotePages(pages: List<CapturedQuotePage>): String = pages.joinToString("\n\n") { it.text }

data class QuoteCaptureUiState(
    val bookId: Long? = null,
    val capturedPages: List<CapturedQuotePage> = emptyList(),
    val currentPageText: String = "",
    val quoteText: String = "",
    val editingPageIndex: Int? = null,
    /** Skipped the camera entirely via "직접 입력" — [quoteText] is freely typed, not OCR-derived. */
    val isManualEntry: Boolean = false,
    val recognizedWords: List<RecognizedWord> = emptyList(),
    val selectionStartIndex: Int? = null,
    val selectionEndIndex: Int? = null,
    val mergedLineBreakGaps: Set<Int> = emptySet(),
    val isRecognizing: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val message: String? = null,
)

class QuoteCaptureViewModel(
    private val quoteRepository: QuoteRepository,
) : ViewModel() {

    private val ocrProcessor = QuoteOcrProcessor()
    private val _uiState = MutableStateFlow(QuoteCaptureUiState())
    val uiState: StateFlow<QuoteCaptureUiState> = _uiState

    fun start(bookId: Long) {
        if (_uiState.value.bookId != bookId) {
            _uiState.value = QuoteCaptureUiState(bookId = bookId)
        }
    }

    fun beginNewQuote() {
        val bookId = _uiState.value.bookId
        _uiState.value = QuoteCaptureUiState(bookId = bookId)
    }

    fun startNextPage() {
        _uiState.update {
            it.copy(
                currentPageText = "",
                editingPageIndex = null,
                isManualEntry = false,
                recognizedWords = emptyList(),
                selectionStartIndex = null,
                selectionEndIndex = null,
                mergedLineBreakGaps = emptySet(),
                message = null,
                isSaved = false,
            )
        }
    }

    /** "직접 입력" — skips camera/OCR entirely, dropping straight into the free-text final screen. */
    fun beginManualEntry() {
        _uiState.update {
            it.copy(currentPageText = "", quoteText = "", isManualEntry = true, message = null, isSaved = false)
        }
    }

    fun returnToReviewFromCamera() {
        _uiState.update { state ->
            val lastPage = state.capturedPages.lastOrNull() ?: return@update state
            state.copy(
                currentPageText = lastPage.pageText,
                quoteText = joinQuotePages(state.capturedPages),
                editingPageIndex = state.capturedPages.lastIndex,
                isManualEntry = false,
                recognizedWords = emptyList(),
                selectionStartIndex = null,
                selectionEndIndex = null,
                mergedLineBreakGaps = emptySet(),
                message = null,
                isSaved = false,
            )
        }
    }

    fun updateQuoteText(value: String) {
        _uiState.update { it.copy(quoteText = value, message = null, isSaved = false) }
    }

    fun updatePageText(value: String) {
        val filtered = value.filter(Char::isDigit).take(4)
        _uiState.update { state ->
            val index = state.editingPageIndex
            val pages = if (index != null && index in state.capturedPages.indices) {
                state.capturedPages.toMutableList().also { list ->
                    list[index] = list[index].copy(pageText = filtered)
                }
            } else {
                state.capturedPages
            }
            state.copy(currentPageText = filtered, capturedPages = pages, message = null, isSaved = false)
        }
    }

    fun prefillPageNumber(bitmap: Bitmap) {
        val state = _uiState.value
        if (state.currentPageText.isNotBlank() || state.editingPageIndex != null) return

        viewModelScope.launch {
            val page = runCatching { ocrProcessor.detectPageNumber(bitmap) }.getOrNull()
            if (page != null) {
                _uiState.update {
                    if (it.currentPageText.isBlank() && it.editingPageIndex == null) {
                        it.copy(currentPageText = page.toString())
                    } else {
                        it
                    }
                }
            }
        }
    }

    /** Runs full-page OCR right after capture so words can be tapped for range selection. */
    fun recognizeFullPage(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRecognizing = true, message = null, isSaved = false) }
            runCatching { ocrProcessor.recognizeWords(bitmap) }
                .onSuccess { words ->
                    _uiState.update { state ->
                        if (words.isEmpty()) {
                            state.copy(isRecognizing = false, message = "텍스트를 찾지 못했어요. 다시 촬영해주세요.")
                        } else {
                            state.copy(
                                recognizedWords = words,
                                selectionStartIndex = null,
                                selectionEndIndex = null,
                                mergedLineBreakGaps = emptySet(),
                                isRecognizing = false,
                            )
                        }
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(isRecognizing = false, message = "텍스트 인식에 실패했어요. 다시 시도해주세요.")
                    }
                }
        }
    }

    /**
     * Tap a word on the photo: first tap sets the start, second sets the end (auto-ordered); a third
     * restarts. Once both are set, the screen opens the gap-adjustment bottom sheet — nothing is
     * committed to [QuoteCaptureUiState.capturedPages] yet, that only happens on [confirmSelection].
     */
    fun selectWord(index: Int) {
        _uiState.update { state ->
            val start = state.selectionStartIndex
            val end = state.selectionEndIndex
            val (newStart, newEnd) = if (start == null || end != null) index to null else start to index
            state.copy(
                selectionStartIndex = newStart,
                selectionEndIndex = newEnd,
                mergedLineBreakGaps = emptySet(),
            )
        }
    }

    /** Tap a line-break gap in the bottom sheet's preview to toggle "이어붙이기" (drop the space). */
    fun toggleLineBreakGap(gapIndex: Int) {
        _uiState.update { state ->
            val updated = state.mergedLineBreakGaps.toMutableSet().apply {
                if (!add(gapIndex)) remove(gapIndex)
            }
            state.copy(mergedLineBreakGaps = updated)
        }
    }

    /** "단어 다시 선택하기" — closes the sheet without saving anything, back to tapping words on the photo. */
    fun cancelSelection() {
        _uiState.update {
            it.copy(selectionStartIndex = null, selectionEndIndex = null, mergedLineBreakGaps = emptySet())
        }
    }

    /**
     * "사용하기" — commits the current word selection (with whatever gaps were merged) into
     * [QuoteCaptureUiState.capturedPages]. A fresh page is appended the first time a range is confirmed
     * for this photo; confirming again for the same photo (after "단어 다시 선택하기") edits that same
     * page entry in place rather than adding a new one.
     */
    fun confirmSelection() {
        _uiState.update { state ->
            val start = state.selectionStartIndex
            val end = state.selectionEndIndex
            if (start == null || end == null) return@update state

            val tokens = state.recognizedWords.map { WordToken(it.text, it.lineId) }
            val text = joinWords(tokens, start, end, state.mergedLineBreakGaps)

            val editIndex = state.editingPageIndex
            val pages = if (editIndex != null && editIndex in state.capturedPages.indices) {
                state.capturedPages.toMutableList().also { list ->
                    list[editIndex] = list[editIndex].copy(text = text, pageText = state.currentPageText)
                }
            } else {
                state.capturedPages +
                    CapturedQuotePage(order = state.capturedPages.size + 1, text = text, pageText = state.currentPageText)
            }
            state.copy(
                capturedPages = pages,
                quoteText = joinQuotePages(pages),
                editingPageIndex = pages.lastIndex,
                selectionStartIndex = null,
                selectionEndIndex = null,
                mergedLineBreakGaps = emptySet(),
                message = null,
            )
        }
    }

    fun discardCurrentCaptureText() {
        _uiState.update { state ->
            val index = state.editingPageIndex
            val pages = if (index != null && index in state.capturedPages.indices) {
                state.capturedPages.dropLast(1)
            } else {
                state.capturedPages
            }
            state.copy(
                capturedPages = pages,
                quoteText = joinQuotePages(pages),
                currentPageText = "",
                editingPageIndex = null,
                recognizedWords = emptyList(),
                selectionStartIndex = null,
                selectionEndIndex = null,
                mergedLineBreakGaps = emptySet(),
                message = null,
                isSaved = false,
            )
        }
    }

    fun save() {
        val state = _uiState.value
        val bookId = state.bookId ?: return
        val text = state.quoteText.trim()
        if (text.isEmpty()) {
            _uiState.update { it.copy(message = "저장할 인용구를 입력해주세요.") }
            return
        }

        val startPage = state.capturedPages.firstOrNull()?.pageText?.toIntOrNull()
            ?: state.currentPageText.toIntOrNull()
        val endPage = state.capturedPages
            .takeIf { it.size > 1 }
            ?.lastOrNull()
            ?.pageText
            ?.toIntOrNull()
            ?.takeIf { it != startPage }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, message = null) }
            quoteRepository.insert(
                Quote(
                    id = 0,
                    bookId = bookId,
                    text = text,
                    pageNumber = startPage,
                    pageNumberEnd = endPage,
                    createdAt = System.currentTimeMillis(),
                ),
            )
            _uiState.update {
                it.copy(
                    isSaving = false,
                    isSaved = true,
                    message = "인용구를 저장했어요.",
                )
            }
        }
    }

    override fun onCleared() {
        ocrProcessor.close()
        super.onCleared()
    }
}
