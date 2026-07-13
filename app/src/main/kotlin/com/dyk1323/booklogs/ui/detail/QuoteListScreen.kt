package com.dyk1323.booklogs.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dyk1323.booklogs.ui.common.components.BooklogsScreenBackground
import com.dyk1323.booklogs.ui.common.components.BooklogsScreenHorizontalPadding
import com.dyk1323.booklogs.ui.common.components.BooklogsScreenVerticalPadding
import com.dyk1323.booklogs.ui.common.components.BooklogsSearchField
import com.dyk1323.booklogs.ui.common.components.BooklogsTextActionTextStyle
import com.dyk1323.booklogs.ui.common.components.BooklogsTextPrimary
import com.dyk1323.booklogs.ui.common.components.BooklogsTopBar
import com.dyk1323.booklogs.ui.common.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteListScreen(
    viewModel: BookDetailViewModel,
    onBack: () -> Unit,
    onCaptureQuoteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    var query by remember { mutableStateOf("") }
    var pendingDeleteQuoteId by remember { mutableStateOf<Long?>(null) }

    val filteredQuotes = remember(uiState.quotes, query) {
        if (query.isBlank()) {
            uiState.quotes
        } else {
            uiState.quotes.filter { quote ->
                quote.text.contains(query, ignoreCase = true) ||
                    quote.pageNumber?.toString()?.contains(query) == true
            }
        }
    }

    Scaffold(
        containerColor = BooklogsScreenBackground,
        topBar = {
            BooklogsTopBar(
                title = "인용구 전체보기",
                onBack = onBack,
                actions = {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clickable(onClick = onCaptureQuoteClick),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "인용구 추가",
                            modifier = Modifier.size(22.dp),
                            tint = Color.Black,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(BooklogsScreenBackground)
                .padding(innerPadding)
                .padding(horizontal = BooklogsScreenHorizontalPadding, vertical = BooklogsScreenVerticalPadding),
            verticalArrangement = Arrangement.spacedBy(36.dp),
        ) {
            BooklogsSearchField(
                value = query,
                onValueChange = { query = it },
                placeholder = "내용 또는 페이지 검색",
            )

            if (filteredQuotes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        message = if (uiState.quotes.isEmpty()) "등록된 인용구가 없어요." else "검색 결과가 없어요.",
                        icon = null,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(filteredQuotes, key = { it.id }) { quote ->
                        QuoteCard(
                            quote = quote,
                            onComments = { viewModel.openComments(quote.id) },
                            onOpen = { viewModel.startEditQuote(quote) },
                            onEdit = { viewModel.startEditQuote(quote) },
                            onDelete = { pendingDeleteQuoteId = quote.id },
                        )
                    }
                }
            }
        }
    }

    pendingDeleteQuoteId?.let { quoteId ->
        AlertDialog(
            onDismissRequest = { pendingDeleteQuoteId = null },
            title = { Text(text = "인용구를 삭제할까요?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDeleteQuoteId = null
                        viewModel.deleteQuote(quoteId)
                    },
                ) {
                    Text(text = "삭제", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteQuoteId = null }) {
                    Text(text = "취소", style = BooklogsTextActionTextStyle, color = BooklogsTextPrimary)
                }
            },
        )
    }

    if (uiState.editingQuoteId != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.cancelEditQuote() },
            containerColor = BooklogsScreenBackground,
            tonalElevation = 0.dp,
        ) {
            QuoteEditSheetContent(
                quoteText = uiState.quoteText,
                quotePageText = uiState.quotePageText,
                message = uiState.message,
                onQuoteTextChanged = viewModel::updateQuoteText,
                onQuotePageTextChanged = viewModel::updateQuotePageText,
                onCancel = viewModel::cancelEditQuote,
                onSave = viewModel::saveQuote,
            )
        }
    }

    if (uiState.expandedCommentsQuoteId != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeComments() },
            containerColor = BooklogsScreenBackground,
            tonalElevation = 0.dp,
        ) {
            QuoteCommentsSheetContent(
                comments = uiState.comments,
                inputText = uiState.commentInputText,
                onInputChanged = viewModel::updateCommentInput,
                onAdd = viewModel::addComment,
                onDelete = viewModel::deleteComment,
            )
        }
    }
}
