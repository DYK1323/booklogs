package com.dyk1323.booklogs.ui.review

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewEditorScreen(
    bookId: Long,
    reviewId: Long?,
    viewModel: ReviewEditorViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(bookId, reviewId) {
        viewModel.start(bookId, reviewId)
    }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = if (uiState.isEditing) "독후감 수정" else "독후감 작성") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "뒤로")
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
            RatingSelector(rating = uiState.rating, onRatingChanged = viewModel::updateRating)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = uiState.reviewText,
                onValueChange = viewModel::updateReviewText,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                label = { Text(text = "독후감") },
                enabled = !uiState.isSaving,
            )
            uiState.message?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = viewModel::save,
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    text = when {
                        uiState.isSaving -> "저장 중"
                        uiState.isEditing -> "수정 저장"
                        else -> "저장"
                    },
                )
            }
        }
    }
}

/** 평점은 선택 사항(docs/PLAN.md 화면 흐름 #6). 아이콘 일관성 원칙에 따라 outlined 별 하나만 쓰고 색으로만 선택 여부를 표시. */
@Composable
private fun RatingSelector(rating: Int?, onRatingChanged: (Int?) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        (1..5).forEach { star ->
            val selected = rating != null && star <= rating
            IconButton(onClick = { onRatingChanged(if (rating == star) null else star) }) {
                Icon(
                    imageVector = Icons.Outlined.Star,
                    contentDescription = "$star 점",
                    tint = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.32f)
                    },
                )
            }
        }
    }
}
