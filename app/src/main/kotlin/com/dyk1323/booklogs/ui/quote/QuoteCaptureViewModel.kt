package com.dyk1323.booklogs.ui.quote

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
    val lines: List<RecognizedQuoteLine> = emptyList(),
    val selectedIndexes: Set<Int> = emptySet(),
    val pageText: String = "",
    val isSaving: Boolean = false,
    val message: String? = null,
) {
    val hasRecognizedText: Boolean = lines.isNotEmpty()
    val selectedText: String = selectedIndexes.sorted().mapNotNull { lines.getOrNull(it)?.text }.joinToString("\n")
}

class QuoteCaptureViewModel(
    private val quoteRepository: QuoteRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuoteCaptureUiState())
    val uiState: StateFlow<QuoteCaptureUiState> = _uiState

    fun start(bookId: Long) {
        _uiState.value = QuoteCaptureUiState(bookId = bookId)
    }

    fun onLinesRecognized(lines: List<RecognizedQuoteLine>, pageNumber: Int?) {
        _uiState.update {
            it.copy(
                lines = lines,
                selectedIndexes = lines.indices.toSet(),
                pageText = pageNumber?.toString().orEmpty(),
                message = null,
            )
        }
    }

    fun toggleLine(index: Int) {
        _uiState.update { state ->
            val next = if (index in state.selectedIndexes) {
                state.selectedIndexes - index
            } else {
                state.selectedIndexes + index
            }
            state.copy(selectedIndexes = next, message = null)
        }
    }

    fun updatePageText(value: String) {
        _uiState.update { it.copy(pageText = value.filter(Char::isDigit).take(4), message = null) }
    }

    fun retake() {
        val bookId = _uiState.value.bookId
        _uiState.value = QuoteCaptureUiState(bookId = bookId)
    }

    fun save(onSaved: () -> Unit) {
        val state = _uiState.value
        val bookId = state.bookId ?: return
        val text = state.selectedText.trim()
        if (text.isEmpty()) {
            _uiState.update { it.copy(message = "저장할 문장을 선택해주세요.") }
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
}
