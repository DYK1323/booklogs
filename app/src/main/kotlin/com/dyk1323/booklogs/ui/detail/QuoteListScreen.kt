package com.dyk1323.booklogs.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dyk1323.booklogs.ui.common.components.EmptyState

/**
 * 책 상세의 "인용구" 섹션에서 "전체보기"를 눌렀을 때 진입 — 미리보기 3개로는 부족한 검색/전체 조회를
 * 이 화면이 담당한다(docs/PLAN.md 화면 흐름 #4). [BookDetailViewModel]을 그대로 공유하므로 이미
 * `selectBook`이 호출된 상태를 그대로 이어받고, 편집/댓글/삭제 로직도 책 상세와 동일하게 재사용한다.
 */
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
        topBar = {
            TopAppBar(
                title = { Text(text = "인용구 전체보기") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    IconButton(onClick = onCaptureQuoteClick) {
                        Icon(Icons.Outlined.Add, contentDescription = "인용구 추가")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "내용 또는 페이지 검색") },
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(14.dp))

            if (uiState.editingQuoteId != null) {
                QuoteEditForm(
                    quoteText = uiState.quoteText,
                    quotePageText = uiState.quotePageText,
                    onQuoteTextChanged = viewModel::updateQuoteText,
                    onQuotePageTextChanged = viewModel::updateQuotePageText,
                    onCancel = viewModel::cancelEditQuote,
                    onSave = viewModel::saveQuote,
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            if (filteredQuotes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        message = if (uiState.quotes.isEmpty()) "저장된 인용구가 없어요." else "검색 결과가 없어요.",
                        icon = null,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(filteredQuotes, key = { it.id }) { quote ->
                        QuoteCard(
                            quote = quote,
                            onComments = { viewModel.openComments(quote.id) },
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
                    Text(text = "취소")
                }
            },
        )
    }

    if (uiState.expandedCommentsQuoteId != null) {
        ModalBottomSheet(onDismissRequest = { viewModel.closeComments() }) {
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
