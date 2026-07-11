package com.dyk1323.booklogs.ui.bookedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dyk1323.booklogs.domain.model.Book
import com.dyk1323.booklogs.domain.model.BookFormat
import com.dyk1323.booklogs.domain.repository.BookRepository
import com.dyk1323.booklogs.domain.usecase.validateBookForm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BookEditFormState(
    val bookId: Long = 0,
    val title: String = "",
    val author: String = "",
    val publisher: String = "",
    val totalPagesText: String = "",
    val genre: String = "",
    val country: String = "",
    val format: BookFormat = BookFormat.PHYSICAL,
)

sealed interface BookEditSaveState {
    data object Idle : BookEditSaveState
    data object Saving : BookEditSaveState
    data object Saved : BookEditSaveState
    data class Error(val message: String) : BookEditSaveState
}

/**
 * Backs the dedicated book metadata edit screen (docs/PLAN.md gap fix — editing was previously only
 * possible through [com.dyk1323.booklogs.domain.usecase.ChangeBookStatusUseCase], never for title/
 * author/etc). isbn/coverImageUrl/status/createdAt are preserved from the originally loaded [Book] since
 * this screen never edits them.
 */
class BookEditViewModel(
    private val bookRepository: BookRepository,
) : ViewModel() {

    private var original: Book? = null

    private val _formState = MutableStateFlow(BookEditFormState())
    val formState: StateFlow<BookEditFormState> = _formState.asStateFlow()

    private val _saveState = MutableStateFlow<BookEditSaveState>(BookEditSaveState.Idle)
    val saveState: StateFlow<BookEditSaveState> = _saveState.asStateFlow()

    fun load(bookId: Long) {
        _saveState.value = BookEditSaveState.Idle
        viewModelScope.launch {
            val book = bookRepository.getById(bookId) ?: return@launch
            original = book
            _formState.value = BookEditFormState(
                bookId = book.id,
                title = book.title,
                author = book.author.orEmpty(),
                publisher = book.publisher.orEmpty(),
                totalPagesText = book.totalPages?.toString().orEmpty(),
                genre = book.genre.orEmpty(),
                country = book.country.orEmpty(),
                format = book.format,
            )
        }
    }

    fun updateTitle(value: String) = _formState.update { it.copy(title = value) }
    fun updateAuthor(value: String) = _formState.update { it.copy(author = value) }
    fun updatePublisher(value: String) = _formState.update { it.copy(publisher = value) }
    fun updateTotalPagesText(value: String) = _formState.update { it.copy(totalPagesText = value.filter(Char::isDigit)) }
    fun updateGenre(value: String) = _formState.update { it.copy(genre = value) }
    fun updateCountry(value: String) = _formState.update { it.copy(country = value) }
    fun updateFormat(value: BookFormat) = _formState.update { it.copy(format = value) }

    fun save() {
        val original = original ?: return
        val form = _formState.value
        if (form.title.isBlank()) {
            _saveState.value = BookEditSaveState.Error("제목을 입력해주세요.")
            return
        }
        val totalPages = form.totalPagesText.toIntOrNull()
        validateBookForm(form.format, totalPages)?.let { message ->
            _saveState.value = BookEditSaveState.Error(message)
            return
        }
        _saveState.value = BookEditSaveState.Saving
        viewModelScope.launch {
            bookRepository.update(
                original.copy(
                    title = form.title.trim(),
                    author = form.author.trim().ifBlank { null },
                    publisher = form.publisher.trim().ifBlank { null },
                    totalPages = totalPages,
                    genre = form.genre.trim().ifBlank { null },
                    country = form.country.trim().ifBlank { null },
                    format = form.format,
                ),
            )
            _saveState.value = BookEditSaveState.Saved
        }
    }
}
