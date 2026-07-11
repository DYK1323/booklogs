package com.dyk1323.booklogs.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dyk1323.booklogs.domain.model.Book
import com.dyk1323.booklogs.domain.model.BookStatus
import com.dyk1323.booklogs.domain.repository.BookRepository
import com.dyk1323.booklogs.domain.repository.ReadingLogRepository
import com.dyk1323.booklogs.domain.usecase.aggregateDailyPages
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DashboardUiState(
    val readingBooks: List<Book> = emptyList(),
    val todayPages: Int = 0,
    val isLoading: Boolean = true,
)

/**
 * Minimal first vertical slice: today's total pages + the READING bookshelf list, wired through the
 * real Room-backed repositories. The full dashboard (7-day bar chart, donut rings, goal line — see
 * docs/PLAN.md "화면 흐름" #1) is a follow-up; this proves the Compose -> ViewModel -> Repository ->
 * Room -> :domain wiring end to end.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    bookRepository: BookRepository,
    readingLogRepository: ReadingLogRepository,
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        bookRepository.observeAll(),
        readingLogRepository.observeAll(),
    ) { books, logs ->
        val today = LocalDate.now(zoneId).toEpochDay()
        val todayTotal = aggregateDailyPages(logs, startEpochDay = today, endEpochDay = today)
            .firstOrNull()?.totalPages ?: 0
        DashboardUiState(
            readingBooks = books.filter { it.status == BookStatus.READING },
            todayPages = todayTotal,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState(),
    )
}
