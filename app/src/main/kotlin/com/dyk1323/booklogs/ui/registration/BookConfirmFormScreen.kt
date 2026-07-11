package com.dyk1323.booklogs.ui.registration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dyk1323.booklogs.domain.model.BookFormat
import com.dyk1323.booklogs.ui.common.components.LoadingOverlay

/** docs/PLAN.md 화면 흐름 #2 확인/수정 폼 — 모든 진입 경로(스캔/검색/수동)가 마지막에 이 화면으로 모인다. */
@Composable
fun BookConfirmFormScreen(
    formState: BookFormState,
    lookupState: LookupUiState,
    saveState: SaveUiState,
    onRetryLookup: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onAuthorChanged: (String) -> Unit,
    onPublisherChanged: (String) -> Unit,
    onTotalPagesChanged: (String) -> Unit,
    onGenreChanged: (String) -> Unit,
    onCountryChanged: (String) -> Unit,
    onFormatChanged: (BookFormat) -> Unit,
    onStartReadingImmediatelyChanged: (Boolean) -> Unit,
    onSave: () -> Unit,
    onSaved: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(saveState) {
        if (saveState is SaveUiState.Saved) onSaved()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("책 정보 확인") },
                navigationIcon = { TextButton(onClick = onBack) { Text("취소") } },
            )
        },
    ) { innerPadding ->
        Box(modifier = modifier.fillMaxSize().padding(innerPadding)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when (lookupState) {
                LookupUiState.NotFound -> Text(
                    text = "책 정보를 찾지 못했어요, 직접 입력해주세요.",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                LookupUiState.NetworkError -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "인터넷 연결을 확인해주세요.",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                    TextButton(onClick = onRetryLookup) { Text("정보 다시 불러오기") }
                }
                else -> Unit
            }

            if (formState.duplicateOfTitle != null) {
                Text(
                    text = "이미 등록된 책이에요 · 『${formState.duplicateOfTitle}』. 그래도 새로 등록할 수 있어요.",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            OutlinedTextField(
                value = formState.title,
                onValueChange = onTitleChanged,
                label = { Text("제목") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = formState.author,
                onValueChange = onAuthorChanged,
                label = { Text("저자") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = formState.publisher,
                onValueChange = onPublisherChanged,
                label = { Text("출판사") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = formState.totalPagesText,
                onValueChange = onTotalPagesChanged,
                label = { Text("총 페이지") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            OutlinedTextField(
                value = formState.genre,
                onValueChange = onGenreChanged,
                label = { Text("장르") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = formState.country,
                onValueChange = onCountryChanged,
                label = { Text("국가") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("형식", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FormatChoiceButton(
                        label = "종이책",
                        selected = formState.format == BookFormat.PHYSICAL,
                        onClick = { onFormatChanged(BookFormat.PHYSICAL) },
                    )
                    FormatChoiceButton(
                        label = "전자책",
                        selected = formState.format == BookFormat.EBOOK,
                        onClick = { onFormatChanged(BookFormat.EBOOK) },
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("바로 읽기 시작", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = formState.startReadingImmediately,
                    onCheckedChange = onStartReadingImmediatelyChanged,
                )
            }

            if (saveState is SaveUiState.Error) {
                Text(
                    text = saveState.message,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(percent = 50),
                enabled = saveState !is SaveUiState.Saving,
            ) {
                if (saveState is SaveUiState.Saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("저장", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        if (lookupState == LookupUiState.Loading) {
            LoadingOverlay(message = "책 정보를 찾고 있어요")
        }
        }
    }
}

@Composable
private fun FormatChoiceButton(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    if (selected) {
        Button(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(percent = 50)) {
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    } else {
        OutlinedButton(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(percent = 50)) {
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}
