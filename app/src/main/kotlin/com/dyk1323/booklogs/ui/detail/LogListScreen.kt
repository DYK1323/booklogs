package com.dyk1323.booklogs.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dyk1323.booklogs.ui.common.components.BooklogsScreenBackground
import com.dyk1323.booklogs.ui.common.components.BooklogsTopBar
import com.dyk1323.booklogs.ui.common.components.EmptyState

@Composable
fun LogListScreen(
    viewModel: BookDetailViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val book = uiState.book

    Scaffold(
        containerColor = BooklogsScreenBackground,
        topBar = {
            BooklogsTopBar(
                title = "\uC9C4\uD589 \uC774\uB825 \uC804\uCCB4\uBCF4\uAE30",
                onBack = onBack,
            )
        },
    ) { innerPadding ->
        if (book == null || uiState.logDeltas.isEmpty()) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(BooklogsScreenBackground)
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                EmptyState(message = "\uC544\uC9C1 \uC9C4\uD589 \uAE30\uB85D\uC774 \uC5C6\uC5B4\uC694", icon = null)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(BooklogsScreenBackground)
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            itemsIndexed(uiState.logDeltas, key = { _, delta -> delta.log.id }) { index, delta ->
                LogDeltaRow(
                    book = book,
                    delta = delta,
                    highlighted = index == 0,
                    isExpanded = uiState.expandedLogId == delta.log.id,
                    editInputText = uiState.logEditInputText,
                    editErrorMessage = uiState.logEditErrorMessage,
                    onToggleExpand = { viewModel.toggleLogExpanded(delta.log.id) },
                    onEditInputChanged = viewModel::updateLogEditInput,
                    onSaveEdit = viewModel::saveLogEdit,
                    onCancelEdit = viewModel::cancelLogEdit,
                    onDelete = { viewModel.deleteLog(delta.log.id) },
                )
            }
        }
    }
}
