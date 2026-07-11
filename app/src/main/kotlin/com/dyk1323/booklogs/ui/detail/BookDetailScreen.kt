package com.dyk1323.booklogs.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.dyk1323.booklogs.domain.usecase.LogDelta
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    bookId: Long,
    viewModel: BookDetailViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(bookId) {
        viewModel.selectBook(bookId)
    }
    val uiState by viewModel.uiState.collectAsState()
    val book = uiState.book

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = book?.title ?: "책 상세",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "뒤로")
                    }
                },
            )
        },
    ) { innerPadding ->
        if (book == null) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "책 정보를 찾지 못했어요.", style = MaterialTheme.typography.bodyLarge)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            item {
                BookHeader(state = uiState)
            }
            item {
                DetailSection(title = "진행 이력") {
                    if (uiState.logDeltas.isEmpty()) {
                        Text(
                            text = "아직 진행 기록이 없어요.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.56f),
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            uiState.logDeltas.take(12).forEach { delta ->
                                LogDeltaRow(delta = delta)
                            }
                        }
                    }
                }
            }
            item {
                DetailSection(title = "인용구") {
                    OutlinedTextField(
                        value = uiState.quoteText,
                        onValueChange = viewModel::updateQuoteText,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("인용구") },
                        minLines = 2,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = uiState.quotePageText,
                            onValueChange = viewModel::updateQuotePageText,
                            modifier = Modifier.weight(1f),
                            label = { Text("페이지") },
                            singleLine = true,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = viewModel::saveQuote, shape = RoundedCornerShape(8.dp)) {
                            Text(text = "저장")
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    if (uiState.quotes.isEmpty()) {
                        Text(
                            text = "저장된 인용구가 없어요.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.56f),
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            uiState.quotes.forEach { quote ->
                                Text(text = quote.text, style = MaterialTheme.typography.bodyLarge)
                                quote.pageNumber?.let {
                                    Text(
                                        text = "p. $it",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.56f),
                                    )
                                }
                                Divider()
                            }
                        }
                    }
                }
            }
            item {
                DetailSection(title = "독후감") {
                    OutlinedTextField(
                        value = uiState.reviewText,
                        onValueChange = viewModel::updateReviewText,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("독후감") },
                        minLines = 4,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Button(onClick = viewModel::saveReview, shape = RoundedCornerShape(8.dp)) {
                            Text(text = "저장")
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    if (uiState.reviews.isEmpty()) {
                        Text(
                            text = "저장된 독후감이 없어요.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.56f),
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            uiState.reviews.forEach { review ->
                                Text(text = review.content, style = MaterialTheme.typography.bodyLarge)
                                Divider()
                            }
                        }
                    }
                }
            }
            item {
                uiState.message?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 24.dp),
                    )
                } ?: Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun BookHeader(state: BookDetailUiState) {
    val book = state.book ?: return
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .width(116.dp)
                .aspectRatio(0.68f)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            if (book.coverImageUrl != null) {
                AsyncImage(
                    model = book.coverImageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Book,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.32f),
                )
            }
        }
        Spacer(modifier = Modifier.width(18.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = book.title, style = MaterialTheme.typography.titleLarge)
            book.author?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = progressText(state), style = MaterialTheme.typography.bodyMedium)
            book.genre?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.56f),
                )
            }
        }
    }
}

@Composable
private fun DetailSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun LogDeltaRow(delta: LogDelta) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column {
            Text(text = "p. ${delta.log.currentPage}", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = formatDate(delta.log.loggedAt),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.56f),
            )
        }
        Text(text = "+${delta.pagesRead}p", style = MaterialTheme.typography.bodyMedium)
    }
}

private fun progressText(state: BookDetailUiState): String {
    val total = state.book?.totalPages ?: return "전체 페이지 수가 필요해요."
    val current = state.currentPage ?: 0
    val percent = state.progress?.let { (it * 100).toInt() } ?: 0
    return "$current / ${total}p · $percent%"
}

private fun formatDate(timestampMillis: Long): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    return Instant.ofEpochMilli(timestampMillis).atZone(ZoneId.systemDefault()).format(formatter)
}
