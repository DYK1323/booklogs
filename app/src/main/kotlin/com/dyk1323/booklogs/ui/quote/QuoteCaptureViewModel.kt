package com.dyk1323.booklogs.ui.quote

import android.graphics.Bitmap
import android.graphics.Rect
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

data class QuoteCaptureUiState(
    val bookId: Long? = null,
    val capturedPages: List<CapturedQuotePage> = emptyList(),
    val currentPageText: String = "",
    val quoteText: String = "",
    val editingPageIndex: Int? = null,
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

    fun recognize(bitmap: Bitmap, cropRect: Rect) {
        if (cropRect.width() < 12 || cropRect.height() < 12) {
            _uiState.update { it.copy(message = "인용할 영역을 조금 더 크게 표시해주세요.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isRecognizing = true, message = null, isSaved = false) }
            runCatching { ocrProcessor.recognize(bitmap, cropRect) }
                .onSuccess { text ->
                    _uiState.update { state ->
                        if (text.isBlank()) {
                            state.copy(
                                isRecognizing = false,
                                message = "텍스트를 찾지 못했어요. 영역을 다시 표시해주세요.",
                            )
                        } else {
                            val editIndex = state.editingPageIndex
                            val pages = if (editIndex != null && editIndex in state.capturedPages.indices) {
                                state.capturedPages.toMutableList().also { list ->
                                    list[editIndex] = list[editIndex].copy(
                                        text = text,
                                        pageText = state.currentPageText,
                                    )
                                }
                            } else {
                                state.capturedPages + CapturedQuotePage(
                                    order = state.capturedPages.size + 1,
                                    text = text,
                                    pageText = state.currentPageText,
                                )
                            }
                            state.copy(
                                capturedPages = pages,
                                quoteText = pages.joinToString("\n\n") { it.text },
                                editingPageIndex = pages.lastIndex,
                                isRecognizing = false,
                                message = null,
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
                quoteText = pages.joinToString("\n\n") { it.text },
                currentPageText = "",
                editingPageIndex = null,
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
