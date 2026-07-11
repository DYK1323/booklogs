package com.dyk1323.booklogs.ui.common.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Pill-shaped PHYSICAL/EBOOK toggle, shared by the registration confirm form and the book edit screen. */
@Composable
fun FormatChoiceButton(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
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
