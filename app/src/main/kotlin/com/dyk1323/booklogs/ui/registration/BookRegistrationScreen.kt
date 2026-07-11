package com.dyk1323.booklogs.ui.registration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

/** Entry point of docs/PLAN.md 화면 흐름 #2 — a low-frequency action, so three explicit choices is fine. */
@Composable
fun BookRegistrationScreen(
    onScanBarcode: () -> Unit,
    onSearchByTitle: () -> Unit,
    onManualEntry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("책 등록") },
                navigationIcon = { TextButton(onClick = onBack) { Text("닫기") } },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "어떻게 등록할까요?",
                style = MaterialTheme.typography.headlineMedium,
            )
            Button(
                onClick = onScanBarcode,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(percent = 50),
            ) {
                Text("바코드로 스캔", style = MaterialTheme.typography.labelLarge)
            }
            OutlinedButton(
                onClick = onSearchByTitle,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(percent = 50),
            ) {
                Text("제목으로 검색", style = MaterialTheme.typography.labelLarge)
            }
            TextButton(onClick = onManualEntry, modifier = Modifier.fillMaxWidth()) {
                Text("직접 입력", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
