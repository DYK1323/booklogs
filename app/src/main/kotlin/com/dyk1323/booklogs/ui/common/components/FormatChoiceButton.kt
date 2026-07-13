package com.dyk1323.booklogs.ui.common.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dyk1323.booklogs.ui.common.theme.BooklogsAccent
import com.dyk1323.booklogs.ui.common.theme.BooklogsCompactButtonTextStyle
import com.dyk1323.booklogs.ui.common.theme.BooklogsOnAccent
import com.dyk1323.booklogs.ui.common.theme.BooklogsTextSecondary

/** Pill-shaped PHYSICAL/EBOOK toggle, shared by the registration confirm form and the book edit screen. */
@Composable
fun FormatChoiceButton(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    if (selected) {
        Button(
            onClick = onClick,
            modifier = modifier.heightIn(min = 42.dp),
            shape = RoundedCornerShape(percent = 50),
            colors = ButtonDefaults.buttonColors(
                containerColor = BooklogsAccent,
                contentColor = BooklogsOnAccent,
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(label, style = BooklogsCompactButtonTextStyle)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.heightIn(min = 42.dp),
            shape = RoundedCornerShape(percent = 50),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = BooklogsTextSecondary,
            ),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(label, style = BooklogsCompactButtonTextStyle)
        }
    }
}
