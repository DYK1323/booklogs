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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.dyk1323.booklogs.ui.common.theme.BooklogsScreenBackground
import com.dyk1323.booklogs.ui.common.components.BooklogsScreenHorizontalPadding
import com.dyk1323.booklogs.ui.common.components.BooklogsScreenVerticalPadding
import com.dyk1323.booklogs.ui.common.components.BooklogsSearchField
import com.dyk1323.booklogs.ui.common.theme.BooklogsTextActionTextStyle
import com.dyk1323.booklogs.ui.common.theme.BooklogsTextPrimary
import com.dyk1323.booklogs.ui.common.components.BooklogsTopBar
import com.dyk1323.booklogs.ui.common.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewListScreen(
    viewModel: BookDetailViewModel,
    onBack: () -> Unit,
    onWriteReviewClick: () -> Unit,
    onEditReviewClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    var query by remember { mutableStateOf("") }
    var pendingDeleteReviewId by remember { mutableStateOf<Long?>(null) }

    val filteredReviews = remember(uiState.reviews, query) {
        uiState.reviews.filter { review ->
            query.isBlank() || review.content.contains(query, ignoreCase = true)
        }
    }

    Scaffold(
        containerColor = BooklogsScreenBackground,
        topBar = {
            BooklogsTopBar(
                title = "독후감 전체보기",
                onBack = onBack,
                actions = {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clickable(onClick = onWriteReviewClick),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "독후감 작성",
                            modifier = Modifier.size(22.dp),
                            tint = BooklogsTextPrimary,
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
                placeholder = "내용 검색",
            )

            if (filteredReviews.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        message = if (uiState.reviews.isEmpty()) "등록된 독후감이 없어요." else "검색 결과가 없어요.",
                        icon = null,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    itemsIndexed(filteredReviews, key = { _, review -> review.id }) { index, review ->
                        ReviewCard(
                            review = review,
                            highlighted = index == 0,
                            onEdit = { onEditReviewClick(review.id) },
                            onDelete = { pendingDeleteReviewId = review.id },
                        )
                    }
                }
            }
        }
    }

    pendingDeleteReviewId?.let { reviewId ->
        AlertDialog(
            onDismissRequest = { pendingDeleteReviewId = null },
            title = { Text(text = "독후감을 삭제할까요?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDeleteReviewId = null
                        viewModel.deleteReview(reviewId)
                    },
                ) {
                    Text(text = "삭제", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteReviewId = null }) {
                    Text(text = "취소", style = BooklogsTextActionTextStyle, color = BooklogsTextPrimary)
                }
            },
        )
    }
}
