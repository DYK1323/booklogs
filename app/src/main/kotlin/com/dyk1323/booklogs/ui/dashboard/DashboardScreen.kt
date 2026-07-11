package com.dyk1323.booklogs.ui.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dyk1323.booklogs.domain.model.Book

/**
 * Minimal first slice of the dashboard (today's pages + the READING shelf as a plain list). The full
 * bookshelf grid with dim/donut overlays, the 7-day bar chart, and the goal line (docs/PLAN.md 화면
 * 흐름 #1) are follow-up work — this proves the data flow end to end.
 */
@Composable
fun DashboardScreen(viewModel: DashboardViewModel, modifier: Modifier = Modifier) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold { innerPadding ->
        Column(modifier = modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
            Text(
                text = "오늘 ${uiState.todayPages}p",
                style = MaterialTheme.typography.displayLarge,
            )
            LazyColumn {
                items(uiState.readingBooks, key = Book::id) { book ->
                    Text(text = book.title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}
