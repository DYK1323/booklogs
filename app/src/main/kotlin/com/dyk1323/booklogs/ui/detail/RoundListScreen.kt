package com.dyk1323.booklogs.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.unit.dp
import com.dyk1323.booklogs.ui.common.components.BooklogsScreenBackground
import com.dyk1323.booklogs.ui.common.components.BooklogsTopBar
import com.dyk1323.booklogs.ui.common.components.EmptyState

@Composable
fun RoundListScreen(
    viewModel: BookDetailViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    var pendingDeleteRoundId by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        containerColor = BooklogsScreenBackground,
        topBar = {
            BooklogsTopBar(
                title = "\uB77C\uC6B4\uB4DC \uC774\uB825 \uC804\uCCB4\uBCF4\uAE30",
                onBack = onBack,
            )
        },
    ) { innerPadding ->
        if (uiState.rounds.isEmpty()) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(BooklogsScreenBackground)
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                EmptyState(message = "\uC544\uC9C1 \uB77C\uC6B4\uB4DC\uAC00 \uC5C6\uC5B4\uC694", icon = null)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(BooklogsScreenBackground)
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            itemsIndexed(uiState.rounds, key = { _, round -> round.id }) { index, round ->
                RoundRow(
                    round = round,
                    highlighted = index == 0,
                    isExpanded = uiState.expandedRoundId == round.id,
                    startedAtText = uiState.roundEditStartedAtText,
                    finishedAtText = uiState.roundEditFinishedAtText,
                    endReason = uiState.roundEditEndReason,
                    startingPageText = uiState.roundEditStartingPageText,
                    onToggleExpand = { viewModel.toggleRoundExpanded(round.id) },
                    onStartedAtChanged = viewModel::updateRoundEditStartedAt,
                    onFinishedAtChanged = viewModel::updateRoundEditFinishedAt,
                    onEndReasonChanged = viewModel::updateRoundEditEndReason,
                    onStartingPageChanged = viewModel::updateRoundEditStartingPage,
                    onSave = viewModel::saveRoundEdit,
                    onCancel = viewModel::cancelRoundEdit,
                    onDelete = { pendingDeleteRoundId = round.id },
                )
            }
        }
    }

    pendingDeleteRoundId?.let { roundId ->
        AlertDialog(
            onDismissRequest = { pendingDeleteRoundId = null },
            title = { Text(text = "\uC774 \uB77C\uC6B4\uB4DC\uB97C \uC0AD\uC81C\uD560\uAE4C\uC694?") },
            text = { Text(text = "\uC0AD\uC81C\uD558\uBA74 \uC774 \uB77C\uC6B4\uB4DC\uC5D0 \uC18D\uD55C \uBAA8\uB4E0 \uC9C4\uD589 \uAE30\uB85D\uB3C4 \uD568\uAED8 \uC0AD\uC81C\uB3FC\uC694.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDeleteRoundId = null
                        viewModel.deleteRound(roundId)
                    },
                ) {
                    Text(text = "\uC0AD\uC81C", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteRoundId = null }) {
                    Text(text = "\uCDE8\uC18C")
                }
            },
        )
    }
}
