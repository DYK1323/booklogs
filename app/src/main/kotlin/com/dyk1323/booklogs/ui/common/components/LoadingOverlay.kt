package com.dyk1323.booklogs.ui.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dyk1323.booklogs.ui.common.theme.BooklogsBodyEmphasisTextStyle
import com.dyk1323.booklogs.ui.common.theme.BooklogsOnOverlay
import com.dyk1323.booklogs.ui.common.theme.BooklogsOverlayScrim

/** Blocking full-screen wait indicator (network lookups, OCR) — see docs/PLAN.md "로딩/대기 상태 UX". */
@Composable
fun LoadingOverlay(message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().background(BooklogsOverlayScrim),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(color = BooklogsOnOverlay)
        Text(
            text = message,
            style = BooklogsBodyEmphasisTextStyle,
            color = BooklogsOnOverlay,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}
