package com.dyk1323.booklogs.ui.common.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.dyk1323.booklogs.ui.common.theme.BooklogsBodyTextStyle
import com.dyk1323.booklogs.ui.common.theme.BooklogsTextPlaceholder
import com.dyk1323.booklogs.ui.common.theme.BooklogsTextSecondary

/** 마스코트 일러스트 없이 텍스트 + 아이콘 하나만 쓰는 공용 빈 상태 — see docs/PLAN.md "UI 전반 원칙". */
@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = Icons.Outlined.Book,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 42.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BooklogsTextPlaceholder,
                modifier = Modifier.size(42.dp),
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        Text(
            text = message,
            style = BooklogsBodyTextStyle,
            color = BooklogsTextSecondary,
        )
        if (actionLabel != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(12.dp))
            BooklogsTextAction(text = actionLabel, onClick = onActionClick)
        }
    }
}
