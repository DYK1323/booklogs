package com.dyk1323.booklogs.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dyk1323.booklogs.domain.model.Book
import com.dyk1323.booklogs.domain.model.BookStatus
import com.dyk1323.booklogs.domain.model.ReadingLog
import com.dyk1323.booklogs.domain.repository.BookRepository
import com.dyk1323.booklogs.domain.repository.ReadingLogRepository
import com.dyk1323.booklogs.domain.usecase.computeBookProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class LibraryStatusFilter(val label: String, val status: BookStatus?) {
    ALL("전체", null),
    READING("읽는 중", BookStatus.READING),
    PLANNED("읽을 예정", BookStatus.PLANNED),
    PAUSED("멈춤", BookStatus.PAUSED),
    FINISHED("완독", BookStatus.FINISHED),
    DROPPED("중단", BookStatus.DROPPED),
}

data class LibraryBookItemUi(
    val book: Book,
    val currentPage: Int?,
    val progress: Float?,
)

data class LibraryUiState(
    val query: String = "",
    val selectedFilter: LibraryStatusFilter = LibraryStatusFilter.ALL,
    val filters: List<LibraryStatusFilter> = LibraryStatusFilter.entries,
    val countsByFilter: Map<LibraryStatusFilter, Int> = emptyMap(),
    val books: List<LibraryBookItemUi> = emptyList(),
    val isLoading: Boolean = true,
)

class LibraryViewModel(
    bookRepository: BookRepository,
    readingLogRepository: ReadingLogRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val selectedFilter = MutableStateFlow(LibraryStatusFilter.ALL)

    val uiState: StateFlow<LibraryUiState> = combine(
        bookRepository.observeAll(),
        readingLogRepository.observeAll(),
        query,
        selectedFilter,
    ) { books, logs, query, filter ->
        val normalizedQuery = query.trim()
        val latestPageByBook = logs.latestPageByBook()
        val items = books
            .filter { book -> filter.status == null || book.status == filter.status }
            .filter { book ->
                normalizedQuery.isEmpty() ||
                    book.title.contains(normalizedQuery, ignoreCase = true) ||
                    book.author?.contains(normalizedQuery, ignoreCase = true) == true
            }
            .map { book ->
                val currentPage = latestPageByBook[book.id]
                LibraryBookItemUi(
                    book = book,
                    currentPage = currentPage,
                    progress = computeBookProgress(currentPage, book.totalPages),
                )
            }

        LibraryUiState(
            query = query,
            selectedFilter = filter,
            countsByFilter = LibraryStatusFilter.entries.associateWith { option ->
                if (option.status == null) books.size else books.count { it.status == option.status }
            },
            books = items,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LibraryUiState(),
    )

    fun updateQuery(value: String) {
        query.value = value
    }

    fun selectFilter(filter: LibraryStatusFilter) {
        selectedFilter.value = filter
    }
}

private fun List<ReadingLog>.latestPageByBook(): Map<Long, Int?> =
    groupBy { it.bookId }.mapValues { (_, logs) -> logs.maxByOrNull { it.loggedAt }?.currentPage }
