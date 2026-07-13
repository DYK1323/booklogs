package com.dyk1323.booklogs.ui.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dyk1323.booklogs.domain.model.ApiLookupResult
import com.dyk1323.booklogs.domain.model.Book
import com.dyk1323.booklogs.domain.model.BookFormat
import com.dyk1323.booklogs.domain.model.BookMetadata
import com.dyk1323.booklogs.domain.model.BookStatus
import com.dyk1323.booklogs.domain.model.MetadataLookupResult
import com.dyk1323.booklogs.domain.repository.BookMetadataRepository
import com.dyk1323.booklogs.domain.repository.BookRepository
import com.dyk1323.booklogs.domain.usecase.RegisterBookUseCase
import com.dyk1323.booklogs.domain.usecase.validateBookForm
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class BookFormState(
    val isbn: String? = null,
    val title: String = "",
    val author: String = "",
    val publisher: String = "",
    val coverImageUrl: String? = null,
    val totalPagesText: String = "",
    val genre: String = "",
    val country: String = "",
    val format: BookFormat = BookFormat.PHYSICAL,
    val startReadingImmediately: Boolean = true,
    val duplicateOfTitle: String? = null,
    val duplicateOfBookId: Long? = null,
)

sealed interface LookupUiState {
    data object Idle : LookupUiState
    data object Loading : LookupUiState
    /** The form is populated (found, partially found, or blank for manual entry) and ready to show. */
    data object Ready : LookupUiState
    data object NotFound : LookupUiState
    data object NetworkError : LookupUiState
}

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data object Loading : SearchUiState
    data class Results(val items: List<BookMetadata>) : SearchUiState
    data object Empty : SearchUiState
    data object NetworkError : SearchUiState
}

sealed interface SaveUiState {
    data object Idle : SaveUiState
    data object Saving : SaveUiState
    data class Saved(val bookId: Long) : SaveUiState
    data class Error(val message: String) : SaveUiState
}

/**
 * Backs the whole registration flow (docs/PLAN.md 화면 흐름 #2: entry choice -> scan/search/manual ->
 * confirm form -> save). One instance is shared across all four screens (created once in
 * MainActivity, like DashboardViewModel) — [reset] is called every time the entry screen is opened so
 * a previous registration's state doesn't leak into the next one.
 */
class BookRegistrationViewModel(
    private val bookMetadataRepository: BookMetadataRepository,
    private val bookRepository: BookRepository,
    private val registerBookUseCase: RegisterBookUseCase,
) : ViewModel() {

    private val _lookupState = MutableStateFlow<LookupUiState>(LookupUiState.Idle)
    val lookupState: StateFlow<LookupUiState> = _lookupState.asStateFlow()

    private val _searchState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()

    private val _formState = MutableStateFlow(BookFormState())
    val formState: StateFlow<BookFormState> = _formState.asStateFlow()

    private val _saveState = MutableStateFlow<SaveUiState>(SaveUiState.Idle)
    val saveState: StateFlow<SaveUiState> = _saveState.asStateFlow()

    private var searchJob: Job? = null
    private var latestSearchQuery: String = ""

    fun reset() {
        searchJob?.cancel()
        latestSearchQuery = ""
        _lookupState.value = LookupUiState.Idle
        _searchState.value = SearchUiState.Idle
        _formState.value = BookFormState()
        _saveState.value = SaveUiState.Idle
    }

    fun startManualEntry() {
        _formState.value = BookFormState()
        _lookupState.value = LookupUiState.Ready
    }

    fun lookupByIsbn(isbn: String) {
        _lookupState.value = LookupUiState.Loading
        viewModelScope.launch {
            applyLookupResult(isbn, bookMetadataRepository.lookupByIsbn(isbn))
        }
    }

    fun searchByTitle(query: String) {
        val normalizedQuery = query.trim()
        searchJob?.cancel()
        latestSearchQuery = normalizedQuery

        if (normalizedQuery.isBlank()) {
            _searchState.value = SearchUiState.Idle
            return
        }
        _searchState.value = SearchUiState.Loading
        searchJob = viewModelScope.launch {
            val nextState = when (val result = bookMetadataRepository.searchByTitle(normalizedQuery)) {
                is ApiLookupResult.Success -> SearchUiState.Results(result.data)
                is ApiLookupResult.NotFound -> SearchUiState.Empty
                is ApiLookupResult.NetworkError -> SearchUiState.NetworkError
            }
            if (latestSearchQuery == normalizedQuery) {
                _searchState.value = nextState
            }
        }
    }

    fun selectSearchResult(candidate: BookMetadata) {
        _lookupState.value = LookupUiState.Loading
        viewModelScope.launch {
            applyLookupResult(candidate.isbn, bookMetadataRepository.resolveSelectedCandidate(candidate))
        }
    }

    private suspend fun applyLookupResult(isbn: String?, result: MetadataLookupResult) {
        when (result) {
            is MetadataLookupResult.Found -> {
                _formState.value = formFrom(result.metadata)
                _lookupState.value = LookupUiState.Ready
            }
            MetadataLookupResult.NotFound -> {
                _formState.value = BookFormState(isbn = isbn)
                _lookupState.value = LookupUiState.NotFound
            }
            MetadataLookupResult.NetworkError -> {
                _formState.value = BookFormState(isbn = isbn)
                _lookupState.value = LookupUiState.NetworkError
            }
        }
        checkDuplicate(isbn)
    }

    private suspend fun formFrom(metadata: BookMetadata) = BookFormState(
        isbn = metadata.isbn,
        title = metadata.title,
        author = metadata.author.orEmpty(),
        publisher = metadata.publisher.orEmpty(),
        coverImageUrl = metadata.coverImageUrl,
        totalPagesText = metadata.totalPages?.toString().orEmpty(),
        genre = metadata.genre.orEmpty(),
    )

    private suspend fun checkDuplicate(isbn: String?) {
        if (isbn == null) return
        val duplicate = bookRepository.findByIsbn(isbn) ?: return
        _formState.update { it.copy(duplicateOfTitle = duplicate.title, duplicateOfBookId = duplicate.id) }
    }

    fun updateTitle(value: String) = _formState.update { it.copy(title = value) }
    fun updateAuthor(value: String) = _formState.update { it.copy(author = value) }
    fun updatePublisher(value: String) = _formState.update { it.copy(publisher = value) }
    fun updateTotalPagesText(value: String) = _formState.update { it.copy(totalPagesText = value.filter(Char::isDigit)) }
    fun updateGenre(value: String) = _formState.update { it.copy(genre = value) }
    fun updateCountry(value: String) = _formState.update { it.copy(country = value) }
    fun updateFormat(value: BookFormat) = _formState.update { it.copy(format = value) }
    fun updateStartReadingImmediately(value: Boolean) = _formState.update { it.copy(startReadingImmediately = value) }

    fun save() {
        val form = _formState.value
        if (form.title.isBlank()) {
            _saveState.value = SaveUiState.Error("제목을 입력해주세요.")
            return
        }
        val totalPages = form.totalPagesText.toIntOrNull()
        validateBookForm(form.format, totalPages)?.let { message ->
            _saveState.value = SaveUiState.Error(message)
            return
        }
        _saveState.value = SaveUiState.Saving
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val book = Book(
                id = 0,
                isbn = form.isbn,
                title = form.title.trim(),
                author = form.author.trim().ifBlank { null },
                publisher = form.publisher.trim().ifBlank { null },
                coverImageUrl = form.coverImageUrl,
                totalPages = totalPages,
                status = BookStatus.PLANNED,
                format = form.format,
                genre = form.genre.trim().ifBlank { null },
                country = form.country.trim().ifBlank { null },
                createdAt = now,
            )
            val result = registerBookUseCase(book, form.startReadingImmediately, now)
            _saveState.value = result.fold(
                onSuccess = { bookId -> SaveUiState.Saved(bookId) },
                onFailure = { error -> SaveUiState.Error(error.message ?: "저장하지 못했어요.") },
            )
        }
    }
}
