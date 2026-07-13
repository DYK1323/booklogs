package com.dyk1323.booklogs.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dyk1323.booklogs.domain.model.BookStatus
import com.dyk1323.booklogs.ui.common.components.BookCoverImage
import com.dyk1323.booklogs.ui.common.theme.BooklogsAccent
import com.dyk1323.booklogs.ui.common.theme.BooklogsSurfaceMuted
import com.dyk1323.booklogs.ui.common.theme.BooklogsScreenBackground
import com.dyk1323.booklogs.ui.common.components.BooklogsScreenHorizontalPadding
import com.dyk1323.booklogs.ui.common.components.BooklogsScreenVerticalPadding
import com.dyk1323.booklogs.ui.common.components.BooklogsSearchField
import com.dyk1323.booklogs.ui.common.theme.BooklogsTextSecondary
import com.dyk1323.booklogs.ui.common.components.BooklogsTopBar
import com.dyk1323.booklogs.ui.common.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onBookClick: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = BooklogsScreenBackground,
        topBar = {
            BooklogsTopBar(title = "라이브러리", onBack = onBack)
        },
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(
                    horizontal = BooklogsScreenHorizontalPadding,
                    vertical = BooklogsScreenVerticalPadding,
                ),
        ) {
            BooklogsSearchField(
                value = uiState.query,
                onValueChange = viewModel::updateQuery,
                placeholder = "제목 또는 저자 검색",
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                uiState.filters.forEach { filter ->
                    val selected = uiState.selectedFilter == filter
                    AssistChip(
                        onClick = { viewModel.selectFilter(filter) },
                        label = { Text("${filter.label} ${uiState.countsByFilter[filter] ?: 0}") },
                        shape = RoundedCornerShape(999.dp),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (selected) BooklogsAccent else BooklogsSurfaceMuted,
                            labelColor = if (selected) Color.White else BooklogsTextSecondary,
                        ),
                        border = null,
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
            if (uiState.books.isEmpty() && !uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(message = "조건에 맞는 책이 없어요", icon = null)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(uiState.books, key = { it.book.id }) { item ->
                        LibraryBookRow(item = item, onClick = { onBookClick(item.book.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryBookRow(item: LibraryBookItemUi, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .height(IntrinsicSize.Min)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top,
    ) {
        BookCoverImage(
            coverImageUrl = item.book.coverImageUrl,
            modifier = Modifier
                .width(58.dp)
                .aspectRatio(0.68f),
            shape = RoundedCornerShape(6.dp),
            placeholderIconSize = 28.dp,
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = item.book.title,
                        style = MaterialTheme.typography.titleMedium.copy(lineHeight = 21.sp),
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = statusLabel(item.book.status),
                        style = MaterialTheme.typography.labelMedium,
                        color = BooklogsAccent,
                    )
                }
                item.book.author?.let {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelMedium.copy(lineHeight = 15.sp),
                        color = BooklogsTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = progressCaption(item),
                style = MaterialTheme.typography.labelMedium,
                color = BooklogsTextSecondary,
            )
        }
    }
}

private fun progressCaption(item: LibraryBookItemUi): String {
    val totalPages = item.book.totalPages ?: return "전체 페이지 수가 필요해요."
    val currentPage = item.currentPage ?: 0
    val percent = item.progress?.let { (it * 100).toInt() } ?: 0
    return "$currentPage / ${totalPages}p · $percent%"
}

fun statusLabel(status: BookStatus): String = when (status) {
    BookStatus.READING -> "읽는 중"
    BookStatus.PLANNED -> "읽을 예정"
    BookStatus.PAUSED -> "멈춤"
    BookStatus.FINISHED -> "완독"
    BookStatus.DROPPED -> "중단"
}
