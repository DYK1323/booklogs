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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dyk1323.booklogs.ui.common.theme.BooklogsAccent
import com.dyk1323.booklogs.ui.common.theme.BooklogsBodyTextStyle
import com.dyk1323.booklogs.ui.common.theme.BooklogsButtonTextStyle
import com.dyk1323.booklogs.ui.common.theme.BooklogsCaptionTextStyle
import com.dyk1323.booklogs.ui.common.theme.BooklogsCompactButtonTextStyle
import com.dyk1323.booklogs.ui.common.theme.BooklogsHairline
import com.dyk1323.booklogs.ui.common.theme.BooklogsOnAccent
import com.dyk1323.booklogs.ui.common.theme.BooklogsScreenBackground
import com.dyk1323.booklogs.ui.common.theme.BooklogsSearchTextStyle
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
fun BooklogsTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable () -> Unit = {},
) {
    val backgroundColor = BooklogsScreenBackground
    val hairlineColor = BooklogsHairline
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
    height: Dp? = null,
    compact: Boolean = false,
) {
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
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(
            text = text,
            style = if (compact) BooklogsCompactButtonTextStyle else BooklogsButtonTextStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
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
    TextButton(onClick = onClick, modifier = if (height != null) modifier.height(height) else modifier) {
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
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .background(BooklogsScreenBackground, BooklogsBlockShape)
            .border(0.5.dp, BooklogsTextSecondary, BooklogsBlockShape),
        singleLine = true,
        textStyle = BooklogsSearchTextStyle.copy(color = BooklogsTextPrimary),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart,
                ) {
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
            .then(if (fieldWeight != null) Modifier.weight(fieldWeight) else Modifier.heightIn(min = minHeight))
            .background(BooklogsScreenBackground, BooklogsBlockShape)
            .border(0.5.dp, BooklogsTextSecondary, BooklogsBlockShape)
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
            decorationBox = { innerTextField ->
                if (singleLine) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = minHeight)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            innerTextField()
                        }
                    }
                } else {
                    Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
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
    backgroundColor: Color = BooklogsScreenBackground,
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
fun BooklogsCardActions(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
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
    Box(
        modifier = modifier
            .weight(1f, fill = true)
            .fillMaxHeight()
            .background(if (selected) BooklogsScreenBackground else Color.Transparent, RoundedCornerShape(5.dp))
            .clickable(onClick = onClick)
            .padding(
                horizontal = BooklogsSegmentChipHorizontalPadding,
                vertical = BooklogsSegmentChipVerticalPadding,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = BooklogsSegmentTextStyle,
            color = if (selected) BooklogsTextPrimary else BooklogsTextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
