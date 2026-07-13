package com.dyk1323.booklogs.ui.bookedit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dyk1323.booklogs.domain.model.BookFormat
import com.dyk1323.booklogs.ui.common.components.BooklogsFilledButton
import com.dyk1323.booklogs.ui.common.components.BooklogsLabeledTextField
import com.dyk1323.booklogs.ui.common.components.BooklogsNumberTextField
import com.dyk1323.booklogs.ui.common.components.BooklogsScreenBackground
import com.dyk1323.booklogs.ui.common.components.BooklogsTopBar
import com.dyk1323.booklogs.ui.common.components.FormatChoiceButton

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
        containerColor = BooklogsScreenBackground,
        topBar = {
            BooklogsTopBar(title = "책 정보 수정", onBack = onBack)
        },
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(BooklogsScreenBackground)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BooklogsLabeledTextField(
                value = formState.title,
                onValueChange = viewModel::updateTitle,
                label = "제목",
                modifier = Modifier.fillMaxWidth(),
            )
            BooklogsLabeledTextField(
                value = formState.author,
                onValueChange = viewModel::updateAuthor,
                label = "저자",
                modifier = Modifier.fillMaxWidth(),
            )
            BooklogsLabeledTextField(
                value = formState.publisher,
                onValueChange = viewModel::updatePublisher,
                label = "출판사",
                modifier = Modifier.fillMaxWidth(),
            )
            BooklogsNumberTextField(
                value = formState.totalPagesText,
                onValueChange = viewModel::updateTotalPagesText,
                label = "총 페이지",
                modifier = Modifier.fillMaxWidth(),
            )
            BooklogsLabeledTextField(
                value = formState.genre,
                onValueChange = viewModel::updateGenre,
                label = "장르",
                modifier = Modifier.fillMaxWidth(),
            )
            BooklogsLabeledTextField(
                value = formState.country,
                onValueChange = viewModel::updateCountry,
                label = "국가",
                modifier = Modifier.fillMaxWidth(),
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

            BooklogsFilledButton(
                text = if (saveState is BookEditSaveState.Saving) "" else "저장",
                onClick = viewModel::save,
                modifier = Modifier.fillMaxWidth(),
                primary = true,
                enabled = saveState !is BookEditSaveState.Saving,
            )

            if (saveState is BookEditSaveState.Saving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp,
                )
            }
        }
    }
}
