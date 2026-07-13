package com.dyk1323.booklogs.ui.registration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dyk1323.booklogs.ui.common.components.BooklogsFilledButton
import com.dyk1323.booklogs.ui.common.theme.BooklogsScreenBackground
import com.dyk1323.booklogs.ui.common.theme.BooklogsTextPrimary
import com.dyk1323.booklogs.ui.common.components.BooklogsTopBar

private val RegistrationTitleTextStyle = TextStyle(
    fontSize = 27.sp,
    lineHeight = 27.sp,
    fontWeight = FontWeight.SemiBold,
    letterSpacing = 0.sp,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookRegistrationScreen(
    onScanBarcode: () -> Unit,
    onSearchByTitle: () -> Unit,
    onManualEntry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        containerColor = BooklogsScreenBackground,
        topBar = {
            BooklogsTopBar(title = "책 등록", onBack = onBack)
        },
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "어떻게 등록할까요?",
                style = RegistrationTitleTextStyle,
                color = BooklogsTextPrimary,
                modifier = Modifier.padding(bottom = 20.dp),
            )
            BooklogsFilledButton(
                text = "바코드로 스캔",
                onClick = onScanBarcode,
                modifier = Modifier.fillMaxWidth(),
                primary = true,
            )
            BooklogsFilledButton(
                text = "제목으로 검색",
                onClick = onSearchByTitle,
                modifier = Modifier.fillMaxWidth(),
            )
            BooklogsFilledButton(
                text = "직접 입력",
                onClick = onManualEntry,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
