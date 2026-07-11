package com.dyk1323.booklogs.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dyk1323.booklogs.domain.model.Review
import com.dyk1323.booklogs.domain.repository.ReadingRoundRepository
import com.dyk1323.booklogs.domain.repository.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReviewEditorUiState(
    val bookId: Long? = null,
    val reviewText: String = "",
    val rating: Int? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val message: String? = null,
)

/** docs/PLAN.md 화면 흐름 #6 — 특정 라운드에 연결된 독후감 작성/저장 전담 화면의 ViewModel. */
class ReviewEditorViewModel(
    private val readingRoundRepository: ReadingRoundRepository,
    private val reviewRepository: ReviewRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewEditorUiState())
    val uiState: StateFlow<ReviewEditorUiState> = _uiState

    fun start(bookId: Long) {
        if (_uiState.value.bookId != bookId) {
            _uiState.value = ReviewEditorUiState(bookId = bookId)
        }
    }

    fun updateReviewText(value: String) {
        _uiState.update { it.copy(reviewText = value, message = null) }
    }

    fun updateRating(value: Int?) {
        _uiState.update { it.copy(rating = value) }
    }

    fun save() {
        val state = _uiState.value
        val bookId = state.bookId ?: return
        val text = state.reviewText.trim()
        if (text.isEmpty()) {
            _uiState.update { it.copy(message = "저장할 독후감을 입력해주세요.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, message = null) }
            val round = readingRoundRepository.getOpenRound(bookId)
                ?: readingRoundRepository.getRoundsForBook(bookId).maxByOrNull { it.roundNumber }
            if (round == null) {
                _uiState.update {
                    it.copy(isSaving = false, message = "읽기 기록이 있는 책에 독후감을 저장할 수 있어요.")
                }
                return@launch
            }
            reviewRepository.insert(
                Review(
                    id = 0,
                    bookId = bookId,
                    readingRoundId = round.id,
                    content = text,
                    rating = state.rating,
                    createdAt = System.currentTimeMillis(),
                ),
            )
            _uiState.update {
                it.copy(isSaving = false, isSaved = true, message = "독후감을 저장했어요.")
            }
        }
    }
}
