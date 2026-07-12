package com.dyk1323.booklogs.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dyk1323.booklogs.domain.model.Review
import com.dyk1323.booklogs.domain.repository.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReviewEditorUiState(
    val bookId: Long? = null,
    val reviewText: String = "",
    val rating: Int? = null,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val message: String? = null,
)

/**
 * docs/PLAN.md 화면 흐름 #6 — 독후감 작성/수정 화면의 ViewModel. 독후감은 책 단위로 여러 개 쌓일 수
 * 있으므로(재독마다 새로 작성 가능), [reviewId]가 주어지면 그 특정 독후감을 불러와 수정 모드로 시작하고,
 * null이면 항상 새 독후감 작성 모드로 시작한다 — "어떤 독후감을 편집 중인지"는 항상 호출부(독후감
 * 목록/책 상세)가 명시적으로 알려줘야 하며, 이 화면이 알아서 "가장 최근 것"을 추측하지 않는다.
 */
class ReviewEditorViewModel(
    private val reviewRepository: ReviewRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewEditorUiState())
    val uiState: StateFlow<ReviewEditorUiState> = _uiState

    private var editingReview: Review? = null

    fun start(bookId: Long, reviewId: Long?) {
        // Always re-initialize (no "same bookId, skip" guard) — the screen bounces back via
        // LaunchedEffect(uiState.isSaved) once isSaved is true, so a stale isSaved=true left over
        // from a previous visit to this same book would otherwise instantly close the screen again
        // before the freshly-loaded existing review is even shown.
        editingReview = null
        _uiState.value = ReviewEditorUiState(bookId = bookId)
        if (reviewId == null) return
        viewModelScope.launch {
            val existing = reviewRepository.observeForBook(bookId).first().firstOrNull { it.id == reviewId }
            if (existing != null) {
                editingReview = existing
                _uiState.update {
                    it.copy(reviewText = existing.content, rating = existing.rating, isEditing = true)
                }
            }
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
            val editing = editingReview
            if (editing != null) {
                reviewRepository.update(editing.copy(content = text, rating = state.rating))
            } else {
                reviewRepository.insert(
                    Review(
                        id = 0,
                        bookId = bookId,
                        content = text,
                        rating = state.rating,
                        createdAt = System.currentTimeMillis(),
                    ),
                )
            }
            _uiState.update {
                it.copy(
                    isSaving = false,
                    isSaved = true,
                    message = if (editing != null) "독후감을 수정했어요." else "독후감을 저장했어요.",
                )
            }
        }
    }
}
