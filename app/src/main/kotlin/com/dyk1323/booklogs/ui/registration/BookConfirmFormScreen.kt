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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dyk1323.booklogs.domain.model.BookFormat
import com.dyk1323.booklogs.ui.common.components.BooklogsFilledButton
import com.dyk1323.booklogs.ui.common.components.BooklogsLabeledTextField
import com.dyk1323.booklogs.ui.common.components.BooklogsNumberTextField
import com.dyk1323.booklogs.ui.common.theme.BooklogsScreenBackground
import com.dyk1323.booklogs.ui.common.components.BooklogsTopBar
import com.dyk1323.booklogs.ui.common.components.FormatChoiceButton
import com.dyk1323.booklogs.ui.common.components.LoadingOverlay
import com.dyk1323.booklogs.ui.common.components.booklogsScreenBottomPadding

@OptIn(ExperimentalMaterial3Api::class)
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
    onGoToDuplicateBook: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(saveState) {
        if (saveState is SaveUiState.Saved) onSaved()
    }

    Scaffold(
        containerColor = BooklogsScreenBackground,
        topBar = {
            BooklogsTopBar(
                title = "책 정보 확인",
                onBack = onBack,
            )
        },
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, top = 20.dp, end = 16.dp, bottom = booklogsScreenBottomPadding()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                when (lookupState) {
                    LookupUiState.NotFound -> {
                        Text(
                            text = "책 정보를 찾지 못했어요. 직접 입력해 주세요.",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    LookupUiState.NetworkError -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "인터넷 연결을 확인해 주세요.",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.error,
                            )
                            TextButton(onClick = onRetryLookup) {
                                Text("정보 다시 불러오기")
                            }
                        }
                    }

                    else -> Unit
                }

                if (formState.duplicateOfTitle != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "이미 등록된 책이에요. \"${formState.duplicateOfTitle}\" 이(가) 내 책장에 있어요.",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        formState.duplicateOfBookId?.let { bookId ->
                            TextButton(onClick = { onGoToDuplicateBook(bookId) }) {
                                Text("그 책으로 이동")
                            }
                        }
                    }
                }

                BooklogsLabeledTextField(
                    value = formState.title,
                    onValueChange = onTitleChanged,
                    label = "제목",
                    modifier = Modifier.fillMaxWidth(),
                )
                BooklogsLabeledTextField(
                    value = formState.author,
                    onValueChange = onAuthorChanged,
                    label = "저자",
                    modifier = Modifier.fillMaxWidth(),
                )
                BooklogsLabeledTextField(
                    value = formState.publisher,
                    onValueChange = onPublisherChanged,
                    label = "출판사",
                    modifier = Modifier.fillMaxWidth(),
                )
                BooklogsNumberTextField(
                    value = formState.totalPagesText,
                    onValueChange = onTotalPagesChanged,
                    label = "총 페이지",
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = if (lookupState != LookupUiState.Loading && formState.totalPagesText.isBlank()) {
                        "책 정보 API에서 페이지 수를 찾지 못했어요. 직접 입력해 주세요."
                    } else {
                        null
                    },
                )
                BooklogsLabeledTextField(
                    value = formState.genre,
                    onValueChange = onGenreChanged,
                    label = "장르",
                    modifier = Modifier.fillMaxWidth(),
                )
                BooklogsLabeledTextField(
                    value = formState.country,
                    onValueChange = onCountryChanged,
                    label = "국가",
                    modifier = Modifier.fillMaxWidth(),
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

                BooklogsFilledButton(
                    text = "저장",
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth(),
                    primary = true,
                    enabled = saveState !is SaveUiState.Saving,
                )

                if (saveState is SaveUiState.Saving) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp,
                        )
                    }
                }
            }

            if (lookupState == LookupUiState.Loading) {
                LoadingOverlay(message = "책 정보를 찾고 있어요.")
            }
        }
    }
}
