package com.dyk1323.booklogs.ui.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val BooklogsButtonShape = RoundedCornerShape(5.dp)
val BooklogsBlockShape = RoundedCornerShape(5.dp)
val BooklogsScreenBackground = Color.White
val BooklogsTextPrimary = Color(0xFF111111)
val BooklogsTextSecondary = Color(0xFF757575)
val BooklogsTextTertiary = Color(0xFF8C8C8C)
val BooklogsTextPlaceholder = Color(0xFFB3B3B3)
val BooklogsAccent = Color(0xFF0C7EFF)
val BooklogsSurfaceMuted = Color(0xFFF5F5F5)
val BooklogsHairline = Color(0xFFB3B3B3)
val BooklogsScreenHorizontalPadding = 16.dp
val BooklogsScreenVerticalPadding = 20.dp
val BooklogsSheetHorizontalPadding = 16.dp
val BooklogsSheetBottomPadding = 20.dp

val BooklogsTopBarTitleTextStyle = TextStyle(
    fontSize = 20.sp,
    lineHeight = 20.sp,
    fontWeight = FontWeight.Normal,
    letterSpacing = 0.sp,
)

val BooklogsButtonTextStyle = TextStyle(
    fontSize = 16.sp,
    lineHeight = 16.sp,
    fontWeight = FontWeight.SemiBold,
    letterSpacing = 0.sp,
)

val BooklogsCompactButtonTextStyle = TextStyle(
    fontSize = 14.sp,
    lineHeight = 14.sp,
    fontWeight = FontWeight.SemiBold,
    letterSpacing = 0.sp,
)

val BooklogsTextActionTextStyle = TextStyle(
    fontSize = 14.sp,
    lineHeight = 14.sp,
    fontWeight = FontWeight.Light,
    letterSpacing = 0.sp,
)

val BooklogsSearchTextStyle = TextStyle(
    fontSize = 14.sp,
    lineHeight = 18.sp,
    fontWeight = FontWeight.Normal,
    letterSpacing = 0.sp,
)

val BooklogsSectionTitleTextStyle = TextStyle(
    fontSize = 14.sp,
    lineHeight = 14.sp,
    fontWeight = FontWeight.Light,
    letterSpacing = 0.sp,
)

val BooklogsTitleTextStyle = TextStyle(
    fontSize = 20.sp,
    lineHeight = 22.sp,
    fontWeight = FontWeight.SemiBold,
    letterSpacing = 0.sp,
)

val BooklogsBodyEmphasisTextStyle = TextStyle(
    fontSize = 16.sp,
    lineHeight = 20.sp,
    fontWeight = FontWeight.Medium,
    letterSpacing = 0.sp,
)

val BooklogsBodyTextStyle = TextStyle(
    fontSize = 14.sp,
    lineHeight = 21.sp,
    fontWeight = FontWeight.Light,
    letterSpacing = 0.sp,
)

val BooklogsCaptionTextStyle = TextStyle(
    fontSize = 12.sp,
    lineHeight = 16.sp,
    fontWeight = FontWeight.Normal,
    letterSpacing = 0.sp,
)

val BooklogsCaptionEmphasisTextStyle = TextStyle(
    fontSize = 12.sp,
    lineHeight = 16.sp,
    fontWeight = FontWeight.Medium,
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
                    color = BooklogsHairline,
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
                tint = BooklogsTextPrimary,
            )
        }
        Text(
            text = title,
            modifier = Modifier
                .padding(start = 15.dp)
                .weight(1f),
            style = BooklogsTopBarTitleTextStyle,
            color = BooklogsTextPrimary,
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
    height: Dp = 42.dp,
    compact: Boolean = false,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(height),
        enabled = enabled,
        shape = BooklogsButtonShape,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (primary) BooklogsAccent else BooklogsSurfaceMuted,
            contentColor = if (primary) Color.White else BooklogsTextSecondary,
            disabledContainerColor = BooklogsSurfaceMuted,
            disabledContentColor = BooklogsTextPlaceholder,
        ),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
    ) {
        Text(text = text, style = if (compact) BooklogsCompactButtonTextStyle else BooklogsButtonTextStyle)
    }
}

@Composable
fun BooklogsTextAction(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(onClick = onClick, modifier = modifier.height(36.dp)) {
        Text(
            text = text,
            style = BooklogsTextActionTextStyle,
            color = BooklogsAccent,
        )
    }
}

@Composable
fun BooklogsSectionEmptyText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.56f),
    )
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
            .background(BooklogsScreenBackground, BooklogsBlockShape)
            .border(0.5.dp, BooklogsTextSecondary, BooklogsBlockShape),
        singleLine = true,
        textStyle = BooklogsSearchTextStyle.copy(color = BooklogsTextPrimary),
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
                            color = BooklogsTextPlaceholder,
                        )
                    }
                    innerTextField()
                }
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = BooklogsTextTertiary,
                )
            }
        },
    )
}

@Composable
fun BooklogsLabeledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    supportingText: String? = null,
    enabled: Boolean = true,
    minHeight: Dp = if (singleLine) 44.dp else 140.dp,
    fieldWeight: Float? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = BooklogsCaptionTextStyle,
            color = BooklogsTextSecondary,
        )
        val fieldModifier = Modifier
            .fillMaxWidth()
            .then(if (fieldWeight != null) Modifier.weight(fieldWeight) else Modifier.height(minHeight))
            .background(BooklogsScreenBackground, BooklogsBlockShape)
            .border(0.5.dp, BooklogsTextSecondary, BooklogsBlockShape)
            .padding(horizontal = 12.dp, vertical = 12.dp)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = fieldModifier,
            enabled = enabled,
            singleLine = singleLine,
            textStyle = BooklogsSearchTextStyle.copy(
                color = if (enabled) BooklogsTextPrimary else BooklogsTextSecondary,
            ),
            keyboardOptions = keyboardOptions,
        )
        supportingText?.let {
            Text(
                text = it,
                style = BooklogsCaptionTextStyle,
                color = BooklogsTextSecondary,
            )
        }
    }
}

@Composable
fun BooklogsNumberTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    enabled: Boolean = true,
) {
    BooklogsLabeledTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        supportingText = supportingText,
        enabled = enabled,
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
fun BooklogsContentCard(
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    minHeight: Dp? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val cardModifier = modifier
        .fillMaxWidth()
        .background(if (highlighted) BooklogsSurfaceMuted else BooklogsScreenBackground, BooklogsBlockShape)
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .then(if (minHeight != null) Modifier.heightIn(min = minHeight) else Modifier)
        .padding(horizontal = 16.dp, vertical = 12.dp)

    Box(modifier = cardModifier) {
        content()
    }
}

@Composable
fun BooklogsIconAction(
    imageVector: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = 20.dp,
) {
    Box(
        modifier = modifier
            .size(28.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            modifier = Modifier.size(iconSize),
            tint = BooklogsTextSecondary,
        )
    }
}

@Composable
fun BooklogsSegmentRow(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(43.dp)
            .background(BooklogsSurfaceMuted, RoundedCornerShape(8.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        content()
    }
}

@Composable
fun RowScope.BooklogsSegmentButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .weight(1f)
            .fillMaxWidth()
            .height(35.dp)
            .background(if (selected) BooklogsScreenBackground else Color.Transparent, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = BooklogsCompactButtonTextStyle.copy(fontWeight = FontWeight.Normal),
            color = if (selected) BooklogsTextPrimary else BooklogsTextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
