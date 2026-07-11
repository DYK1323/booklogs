package com.dyk1323.booklogs.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dyk1323.booklogs.domain.model.Review
import com.dyk1323.booklogs.domain.model.ReadingRound
import com.dyk1323.booklogs.domain.repository.ReadingRoundRepository
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
 * docs/PLAN.md 화면 흐름 #6 — 특정 라운드에 연결된 독후감 작성/수정 화면의 ViewModel. 진입 시 해당 책의
 * 현재 라운드(열린 라운드, 없으면 가장 최근 라운드)에 이미 독후감이 있으면 불러와 수정 모드로 시작한다 —
 * 그렇지 않으면 매번 "작성"을 누를 때마다 같은 라운드에 독후감이 중복으로 쌓이게 된다.
 */
class ReviewEditorViewModel(
    private val readingRoundRepository: ReadingRoundRepository,
    private val reviewRepository: ReviewRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewEditorUiState())
    val uiState: StateFlow<ReviewEditorUiState> = _uiState

    private var editingReview: Review? = null

    fun start(bookId: Long) {
        // Always re-initialize (no "same bookId, skip" guard) — the screen bounces back via
        // LaunchedEffect(uiState.isSaved) once isSaved is true, so a stale isSaved=true left over
        // from a previous visit to this same book would otherwise instantly close the screen again
        // before the freshly-loaded existing review is even shown.
        editingReview = null
        _uiState.value = ReviewEditorUiState(bookId = bookId)
        viewModelScope.launch {
            val round = currentRound(bookId) ?: return@launch
            val existing = reviewRepository.observeForRound(round.id).first().firstOrNull()
            if (existing != null) {
                editingReview = existing
                _uiState.update {
                    it.copy(reviewText = existing.content, rating = existing.rating, isEditing = true)
                }
            }
        }
    }

    private suspend fun currentRound(bookId: Long): ReadingRound? =
        readingRoundRepository.getOpenRound(bookId)
            ?: readingRoundRepository.getRoundsForBook(bookId).maxByOrNull { it.roundNumber }

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
                val round = currentRound(bookId)
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
