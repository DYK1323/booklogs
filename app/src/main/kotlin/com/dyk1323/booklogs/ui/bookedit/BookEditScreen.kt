package com.dyk1323.booklogs.ui.bookedit

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dyk1323.booklogs.domain.model.BookFormat
import com.dyk1323.booklogs.ui.common.components.FormatChoiceButton

/** Dedicated book metadata edit screen (docs/PLAN.md gap fix — see BookEditViewModel doc comment). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookEditScreen(
    bookId: Long,
    viewModel: BookEditViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(bookId) {
        viewModel.load(bookId)
    }
    val formState by viewModel.formState.collectAsState()
    val saveState by viewModel.saveState.collectAsState()

    LaunchedEffect(saveState) {
        if (saveState is BookEditSaveState.Saved) onSaved()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("책 정보 수정") },
                navigationIcon = { TextButton(onClick = onBack) { Text("취소") } },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = formState.title,
                onValueChange = viewModel::updateTitle,
                label = { Text("제목") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = formState.author,
                onValueChange = viewModel::updateAuthor,
                label = { Text("저자") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = formState.publisher,
                onValueChange = viewModel::updatePublisher,
                label = { Text("출판사") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = formState.totalPagesText,
                onValueChange = viewModel::updateTotalPagesText,
                label = { Text("총 페이지") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            OutlinedTextField(
                value = formState.genre,
                onValueChange = viewModel::updateGenre,
                label = { Text("장르") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = formState.country,
                onValueChange = viewModel::updateCountry,
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
                        onClick = { viewModel.updateFormat(BookFormat.PHYSICAL) },
                    )
                    FormatChoiceButton(
                        label = "전자책",
                        selected = formState.format == BookFormat.EBOOK,
                        onClick = { viewModel.updateFormat(BookFormat.EBOOK) },
                    )
                }
            }

            if (saveState is BookEditSaveState.Error) {
                Text(
                    text = (saveState as BookEditSaveState.Error).message,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Button(
                onClick = viewModel::save,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(percent = 50),
                enabled = saveState !is BookEditSaveState.Saving,
            ) {
                if (saveState is BookEditSaveState.Saving) {
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
    }
}
