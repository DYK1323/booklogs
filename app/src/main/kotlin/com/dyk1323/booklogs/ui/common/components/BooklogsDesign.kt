package com.dyk1323.booklogs.ui.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val BooklogsButtonShape = RoundedCornerShape(5.dp)
val BooklogsBlockShape = RoundedCornerShape(5.dp)
val BooklogsScreenBackground = Color.White
val BooklogsScreenHorizontalPadding = 16.dp
val BooklogsScreenVerticalPadding = 20.dp
val BooklogsSheetHorizontalPadding = 16.dp
val BooklogsSheetBottomPadding = 20.dp

private val BooklogsTopBarTitleTextStyle = TextStyle(
    fontSize = 20.sp,
    lineHeight = 20.sp,
    fontWeight = FontWeight.Normal,
    letterSpacing = 0.sp,
)

private val BooklogsButtonTextStyle = TextStyle(
    fontSize = 16.sp,
    lineHeight = 16.sp,
    fontWeight = FontWeight.SemiBold,
    letterSpacing = 0.sp,
)

private val BooklogsSearchTextStyle = TextStyle(
    fontSize = 14.sp,
    lineHeight = 14.sp,
    fontWeight = FontWeight.Normal,
    letterSpacing = 0.sp,
)

@Composable
fun BooklogsTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(48.dp)
            .background(Color.White)
            .drawBehind {
                val stroke = 0.8.dp.toPx()
                drawLine(
                    color = Color(0xFFB3B3B3),
                    start = Offset(0f, size.height - stroke / 2f),
                    end = Offset(size.width, size.height - stroke / 2f),
                    strokeWidth = stroke,
                )
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "뒤로",
                modifier = Modifier.size(24.dp),
                tint = Color.Black,
            )
        }
        Text(
            text = title,
            modifier = Modifier
                .padding(start = 15.dp)
                .weight(1f),
            style = BooklogsTopBarTitleTextStyle,
            color = Color.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        actions()
    }
}

@Composable
fun BooklogsFilledButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(42.dp),
        enabled = enabled,
        shape = BooklogsButtonShape,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (primary) Color(0xFF0C7EFF) else Color(0xFFF5F5F5),
            contentColor = if (primary) Color.White else Color(0xFF757575),
            disabledContainerColor = Color(0xFFF5F5F5),
            disabledContentColor = Color(0xFFB3B3B3),
        ),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
    ) {
        Text(text = text, style = BooklogsButtonTextStyle)
    }
}

@Composable
fun BooklogsTextAction(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(onClick = onClick, modifier = modifier.height(36.dp)) {
        Text(text = text, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
    }
}

@Composable
fun BooklogsSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(Color.White, BooklogsBlockShape)
            .border(0.5.dp, Color(0xFF757575), BooklogsBlockShape),
        singleLine = true,
        textStyle = BooklogsSearchTextStyle.copy(color = Color.Black),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = BooklogsSearchTextStyle,
                            color = Color(0xFFB3B3B3),
                        )
                    }
                    innerTextField()
                }
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color(0xFF8C8C8C),
                )
            }
        },
    )
}

@Composable
fun BooklogsListBlock(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor, BooklogsBlockShape)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        content()
    }
}

@Composable
fun BooklogsSegmentRow(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(43.dp)
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        content()
    }
}
