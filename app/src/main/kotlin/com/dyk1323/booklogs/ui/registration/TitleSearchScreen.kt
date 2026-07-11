package com.dyk1323.booklogs.ui.registration

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dyk1323.booklogs.domain.model.BookMetadata
import com.dyk1323.booklogs.ui.common.components.SkeletonBox

/** docs/PLAN.md 화면 흐름 #2 제목 검색 경로 — 카카오 우선, 0건일 때만 Google Books 폴백(리포지토리가 처리). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TitleSearchScreen(
    searchState: SearchUiState,
    onQueryChanged: (String) -> Unit,
    onResultSelected: (BookMetadata) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("제목으로 검색") },
                navigationIcon = { TextButton(onClick = onBack) { Text("취소") } },
            )
        },
    ) { innerPadding ->
        Column(modifier = modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("책 제목") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onQueryChanged(query) }),
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (searchState) {
                SearchUiState.Idle -> Unit
                SearchUiState.Loading -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        repeat(5) { SkeletonBox(modifier = Modifier.fillMaxWidth().height(64.dp)) }
                    }
                }
                is SearchUiState.Results -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(searchState.items) { item ->
                            SearchResultRow(item, onClick = { onResultSelected(item) })
                        }
                    }
                }
                SearchUiState.Empty -> Text(
                    text = "검색 결과가 없어요. 직접 입력으로 등록할 수 있어요.",
                    style = MaterialTheme.typography.bodyLarge,
                )
                SearchUiState.NetworkError -> Text(
                    text = "인터넷 연결을 확인해주세요.",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Composable
private fun SearchResultRow(item: BookMetadata, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(text = item.title, style = MaterialTheme.typography.bodyMedium)
            val subtitle = listOfNotNull(item.author, item.publisher).joinToString(" · ")
            if (subtitle.isNotBlank()) {
                Text(text = subtitle, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
