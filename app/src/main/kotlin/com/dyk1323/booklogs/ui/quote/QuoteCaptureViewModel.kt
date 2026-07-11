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

data class QuoteCaptureUiState(
    val bookId: Long? = null,
    val quoteText: String = "",
    val pageText: String = "",
    val isRecognizing: Boolean = false,
    val isSaving: Boolean = false,
    val message: String? = null,
)

class QuoteCaptureViewModel(
    private val quoteRepository: QuoteRepository,
) : ViewModel() {

    private val ocrProcessor = QuoteOcrProcessor()
    private val _uiState = MutableStateFlow(QuoteCaptureUiState())
    val uiState: StateFlow<QuoteCaptureUiState> = _uiState

    fun start(bookId: Long) {
        _uiState.value = QuoteCaptureUiState(bookId = bookId)
    }

    fun updateQuoteText(value: String) {
        _uiState.update { it.copy(quoteText = value, message = null) }
    }

    fun updatePageText(value: String) {
        _uiState.update { it.copy(pageText = value.filter(Char::isDigit).take(4), message = null) }
    }

    fun recognize(bitmap: Bitmap, cropRect: Rect) {
        if (cropRect.width() < 12 || cropRect.height() < 12) {
            _uiState.update { it.copy(message = "인용할 영역을 조금 더 크게 표시해주세요.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isRecognizing = true, message = null) }
            runCatching { ocrProcessor.recognize(bitmap, cropRect) }
                .onSuccess { text ->
                    _uiState.update {
                        it.copy(
                            quoteText = text,
                            isRecognizing = false,
                            message = if (text.isBlank()) "텍스트를 찾지 못했어요. 영역을 다시 표시해주세요." else null,
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(isRecognizing = false, message = "텍스트 인식에 실패했어요. 다시 시도해주세요.")
                    }
                }
        }
    }

    fun resetRecognizedText() {
        _uiState.update { it.copy(quoteText = "", message = null) }
    }

    fun save(onSaved: () -> Unit) {
        val state = _uiState.value
        val bookId = state.bookId ?: return
        val text = state.quoteText.trim()
        if (text.isEmpty()) {
            _uiState.update { it.copy(message = "저장할 인용구를 입력해주세요.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, message = null) }
            quoteRepository.insert(
                Quote(
                    id = 0,
                    bookId = bookId,
                    text = text,
                    pageNumber = state.pageText.toIntOrNull(),
                    pageNumberEnd = null,
                    createdAt = System.currentTimeMillis(),
                ),
            )
            _uiState.value = QuoteCaptureUiState(bookId = bookId)
            onSaved()
        }
    }

    override fun onCleared() {
        ocrProcessor.close()
        super.onCleared()
    }
}
