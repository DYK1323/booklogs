package com.dyk1323.booklogs.ui.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dyk1323.booklogs.ui.common.theme.BooklogsAccent
import com.dyk1323.booklogs.ui.common.theme.BooklogsBodyTextStyle
import com.dyk1323.booklogs.ui.common.theme.BooklogsButtonTextStyle
import com.dyk1323.booklogs.ui.common.theme.BooklogsCaptionTextStyle
import com.dyk1323.booklogs.ui.common.theme.BooklogsCompactButtonTextStyle
import com.dyk1323.booklogs.ui.common.theme.BooklogsHairline
import com.dyk1323.booklogs.ui.common.theme.BooklogsInputTextStyle
import com.dyk1323.booklogs.ui.common.theme.BooklogsOnAccent
import com.dyk1323.booklogs.ui.common.theme.BooklogsScreenBackground
import com.dyk1323.booklogs.ui.common.theme.BooklogsSegmentTextStyle
import com.dyk1323.booklogs.ui.common.theme.BooklogsSurfaceMuted
import com.dyk1323.booklogs.ui.common.theme.BooklogsTextActionTextStyle
import com.dyk1323.booklogs.ui.common.theme.BooklogsTextPlaceholder
import com.dyk1323.booklogs.ui.common.theme.BooklogsTextPrimary
import com.dyk1323.booklogs.ui.common.theme.BooklogsTextSecondary
import com.dyk1323.booklogs.ui.common.theme.BooklogsTextTertiary
import com.dyk1323.booklogs.ui.common.theme.BooklogsTopBarTitleTextStyle

val BooklogsButtonShape = RoundedCornerShape(5.dp)
val BooklogsBlockShape = RoundedCornerShape(5.dp)
val BooklogsScreenHorizontalPadding = 16.dp
val BooklogsScreenVerticalPadding = 20.dp
val BooklogsSheetHorizontalPadding = 16.dp
val BooklogsSheetBottomPadding = 20.dp
private val BooklogsSegmentOuterPadding = 4.dp
private val BooklogsSegmentChipHorizontalPadding = 8.dp
private val BooklogsSegmentChipVerticalPadding = 6.dp

@Composable
fun booklogsScaledDp(value: Dp): Dp {
    val fontScale = LocalDensity.current.fontScale
    return value * fontScale
}

@Composable
fun booklogsScreenBottomPadding(): Dp = with(LocalDensity.current) { 64.toDp() }

@Composable
fun BooklogsTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable () -> Unit = {},
) {
    val backgroundColor = BooklogsScreenBackground
    val hairlineColor = BooklogsHairline
    val verticalPadding = booklogsScaledDp(12.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .heightIn(min = 48.dp)
            .background(backgroundColor)
            .drawBehind {
                val stroke = 0.8.dp.toPx()
                drawLine(
                    color = hairlineColor,
                    start = Offset(0f, size.height - stroke / 2f),
                    end = Offset(size.width, size.height - stroke / 2f),
                    strokeWidth = stroke,
                )
            }
            .padding(horizontal = 16.dp, vertical = verticalPadding),
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
    height: Dp? = null,
    compact: Boolean = false,
) {
    val horizontalPadding = booklogsScaledDp(12.dp)
    val verticalPadding = booklogsScaledDp(10.dp)
    Button(
        onClick = onClick,
        modifier = if (height != null) modifier.height(height) else modifier,
        enabled = enabled,
        shape = BooklogsButtonShape,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (primary) BooklogsAccent else BooklogsSurfaceMuted,
            contentColor = if (primary) BooklogsOnAccent else BooklogsTextSecondary,
            disabledContainerColor = BooklogsSurfaceMuted,
            disabledContentColor = BooklogsTextPlaceholder,
        ),
        contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = verticalPadding),
    ) {
        Text(
            text = text,
            style = if (compact) BooklogsCompactButtonTextStyle else BooklogsButtonTextStyle,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun BooklogsTextAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp? = null,
) {
    TextButton(
        onClick = onClick,
        modifier = if (height != null) modifier.height(height) else modifier,
        contentPadding = PaddingValues(
            horizontal = booklogsScaledDp(8.dp),
            vertical = booklogsScaledDp(8.dp),
        ),
    ) {
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
            .padding(horizontal = booklogsScaledDp(6.dp), vertical = booklogsScaledDp(4.dp)),
        style = BooklogsBodyTextStyle,
        color = BooklogsTextSecondary,
    )
}

@Composable
fun BooklogsSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    val minHeight = booklogsScaledDp(48.dp)
    var isFocused by remember { mutableStateOf(false) }
    val borderColor = if (isFocused) BooklogsAccent else BooklogsHairline
    val borderWidth = if (isFocused) 1.dp else 0.5.dp
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .background(Color.Transparent, BooklogsBlockShape)
            .border(borderWidth, borderColor, BooklogsBlockShape)
            .onFocusChanged { isFocused = it.isFocused },
        singleLine = true,
        textStyle = BooklogsInputTextStyle.copy(color = BooklogsTextPrimary),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minHeight)
                    .padding(horizontal = booklogsScaledDp(20.dp)),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = BooklogsInputTextStyle,
                            color = BooklogsTextPlaceholder,
                        )
                    }
                    innerTextField()
                }
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    modifier = Modifier.size(booklogsScaledDp(20.dp)),
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
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    supportingText: String? = null,
    suffix: String? = null,
    enabled: Boolean = true,
    minHeight: Dp = if (singleLine) 48.dp else 140.dp,
    fieldWeight: Float? = null,
    containerColor: Color = Color.Transparent,
) {
    val labelSpacing = booklogsScaledDp(4.dp)
    val scaledMinHeight = booklogsScaledDp(minHeight)
    val horizontalPadding = booklogsScaledDp(12.dp)
    val multilineVerticalPadding = booklogsScaledDp(12.dp)
    var isFocused by remember { mutableStateOf(false) }
    val borderColor = if (isFocused) BooklogsAccent else BooklogsHairline
    val borderWidth = if (isFocused) 1.dp else 0.5.dp
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(labelSpacing),
    ) {
        Text(
            text = label,
            style = BooklogsCaptionTextStyle,
            color = BooklogsTextSecondary,
        )
        val fieldModifier = Modifier
            .fillMaxWidth()
            .then(if (fieldWeight != null) Modifier.weight(fieldWeight) else Modifier.heightIn(min = scaledMinHeight))
            .background(containerColor, BooklogsBlockShape)
            .border(borderWidth, borderColor, BooklogsBlockShape)
            .onFocusChanged { isFocused = it.isFocused }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = fieldModifier,
            enabled = enabled,
            singleLine = singleLine,
            textStyle = BooklogsInputTextStyle.copy(
                color = if (enabled) BooklogsTextPrimary else BooklogsTextSecondary,
            ),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            decorationBox = { innerTextField ->
                if (singleLine) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = scaledMinHeight)
                            .padding(horizontal = horizontalPadding),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            innerTextField()
                        }
                        suffix?.let {
                            Text(
                                text = it,
                                style = BooklogsInputTextStyle,
                                color = BooklogsTextPlaceholder,
                            )
                        }
                    }
                } else {
                    Box(modifier = Modifier.padding(horizontal = horizontalPadding, vertical = multilineVerticalPadding)) {
                        innerTextField()
                    }
                }
            },
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
fun BooklogsReadOnlyTextField(
    value: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    minHeight: Dp = 48.dp,
    containerColor: Color = Color.Transparent,
) {
    val labelSpacing = booklogsScaledDp(4.dp)
    val scaledMinHeight = booklogsScaledDp(minHeight)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(labelSpacing),
    ) {
        Text(
            text = label,
            style = BooklogsCaptionTextStyle,
            color = BooklogsTextSecondary,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = scaledMinHeight)
                .background(containerColor, BooklogsBlockShape)
                .border(0.5.dp, BooklogsHairline, BooklogsBlockShape)
                .clickable(onClick = onClick)
                .padding(horizontal = booklogsScaledDp(12.dp)),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = value,
                style = BooklogsInputTextStyle,
                color = BooklogsTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
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
    suffix: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    enabled: Boolean = true,
    containerColor: Color = Color.Transparent,
) {
    BooklogsLabeledTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        supportingText = supportingText,
        suffix = suffix,
        enabled = enabled,
        containerColor = containerColor,
    )
}

@Composable
fun BooklogsListBlock(
    modifier: Modifier = Modifier,
    backgroundColor: Color = BooklogsScreenBackground,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor, BooklogsBlockShape)
            .padding(horizontal = booklogsScaledDp(16.dp), vertical = booklogsScaledDp(12.dp)),
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
    val horizontalPadding = booklogsScaledDp(16.dp)
    val verticalPadding = booklogsScaledDp(12.dp)
    val cardModifier = modifier
        .fillMaxWidth()
        .background(if (highlighted) BooklogsSurfaceMuted else BooklogsScreenBackground, BooklogsBlockShape)
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .then(if (minHeight != null) Modifier.heightIn(min = minHeight) else Modifier)
        .padding(horizontal = horizontalPadding, vertical = verticalPadding)

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
fun BooklogsCardActions(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(booklogsScaledDp(12.dp)),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
fun BooklogsSegmentRow(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(BooklogsSurfaceMuted, RoundedCornerShape(8.dp))
            .padding(BooklogsSegmentOuterPadding),
        horizontalArrangement = Arrangement.spacedBy(BooklogsSegmentOuterPadding),
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
    val verticalPadding = booklogsScaledDp(BooklogsSegmentChipVerticalPadding)
    Box(
        modifier = modifier
            .weight(1f, fill = true)
            .fillMaxHeight()
            .background(if (selected) BooklogsScreenBackground else Color.Transparent, RoundedCornerShape(5.dp))
            .clickable(onClick = onClick)
            .padding(
                horizontal = BooklogsSegmentChipHorizontalPadding,
                vertical = verticalPadding,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = BooklogsSegmentTextStyle,
            color = if (selected) BooklogsTextPrimary else BooklogsTextSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}
