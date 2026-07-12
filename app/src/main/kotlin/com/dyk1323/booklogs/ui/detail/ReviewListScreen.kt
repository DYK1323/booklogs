package com.dyk1323.booklogs.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
 * 책 상세의 "독후감" 섹션에서 "전체보기"를 눌렀을 때 진입(docs/PLAN.md 화면 흐름 #4/#6). 독후감은 이제
 * 라운드에 묶이지 않고 책 단위로 여러 개 쌓일 수 있으므로, 내용 검색과 평점 필터를 이 화면에서 제공한다.
 * [BookDetailViewModel]을 그대로 공유해 별도 데이터 로딩 없이 이미 구독 중인 목록을 재사용한다.
 */
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
    var selectedRating by remember { mutableStateOf<Int?>(null) }
    var pendingDeleteReviewId by remember { mutableStateOf<Long?>(null) }

    val filteredReviews = remember(uiState.reviews, query, selectedRating) {
        uiState.reviews.filter { review ->
            (query.isBlank() || review.content.contains(query, ignoreCase = true)) &&
                (selectedRating == null || review.rating == selectedRating)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "독후감 전체보기") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    IconButton(onClick = onWriteReviewClick) {
                        Icon(Icons.Outlined.Add, contentDescription = "독후감 작성")
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
                label = { Text(text = "내용 검색") },
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ratingFilterOptions.forEach { rating ->
                    val selected = selectedRating == rating
                    AssistChip(
                        onClick = { selectedRating = if (selected) null else rating },
                        label = { Text(text = ratingFilterLabel(rating)) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            labelColor = if (selected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        ),
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))

            if (filteredReviews.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        message = if (uiState.reviews.isEmpty()) "저장된 독후감이 없어요." else "검색 결과가 없어요.",
                        icon = null,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(filteredReviews, key = { it.id }) { review ->
                        ReviewCard(
                            review = review,
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
                    Text(text = "취소")
                }
            },
        )
    }
}

private val ratingFilterOptions: List<Int?> = listOf(null, 5, 4, 3, 2, 1)

private fun ratingFilterLabel(rating: Int?): String = if (rating == null) "전체" else "★$rating"
