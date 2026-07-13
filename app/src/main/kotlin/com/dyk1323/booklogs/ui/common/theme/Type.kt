package com.dyk1323.booklogs.ui.common.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.dyk1323.booklogs.R

/**
 * Pretendard v1.3.9 static TTFs (Regular/Medium/SemiBold/Bold), bundled at `app/src/main/res/font/`.
 * License (SIL OFL 1.1) copied to `licenses/PRETENDARD_LICENSE.txt`.
 */
val PretendardFontFamily = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold),
    Font(R.font.pretendard_bold, FontWeight.Bold),
)

// Sizes/weights mapped from Apple's marketing scale down to in-app slots — see docs/PLAN.md
// "타이포그래피 (Pretendard)" for the full mapping table and rationale per slot.
val BooklogsTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 62.sp,
        lineHeight = 68.sp, // ~1.07x
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 35.sp,
        lineHeight = 45.sp,
        letterSpacing = 0.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 33.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 21.sp,
        lineHeight = 25.sp,
        letterSpacing = 0.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 19.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp,
        lineHeight = 27.sp,
        letterSpacing = 0.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 19.sp,
        letterSpacing = 0.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 23.sp,
        letterSpacing = 0.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.sp,
    ),
)

private fun booklogsTextStyle(
    fontSize: Int,
    lineHeight: Int,
    fontWeight: FontWeight,
) = TextStyle(
    fontSize = fontSize.sp,
    lineHeight = lineHeight.sp,
    fontWeight = fontWeight,
    letterSpacing = 0.sp,
)

val BooklogsTopBarTitleTextStyle = TextStyle(
    fontSize = 22.sp,
    lineHeight = TextUnit.Unspecified,
    fontWeight = FontWeight.Normal,
    letterSpacing = 0.sp,
)

val BooklogsButtonTextStyle = BooklogsTypography.labelLarge.copy(
    lineHeight = 21.sp,
    fontWeight = FontWeight.Medium,
)

val BooklogsCompactButtonTextStyle = booklogsTextStyle(
    fontSize = 17,
    lineHeight = 19,
    fontWeight = FontWeight.Medium,
)

val BooklogsSegmentTextStyle = booklogsTextStyle(
    fontSize = 15,
    lineHeight = 17,
    fontWeight = FontWeight.Medium,
)

val BooklogsTextActionTextStyle = booklogsTextStyle(
    fontSize = 17,
    lineHeight = 19,
    fontWeight = FontWeight.Medium,
)

val BooklogsSearchTextStyle = booklogsTextStyle(
    fontSize = 17,
    lineHeight = 24,
    fontWeight = FontWeight.Normal,
)

val BooklogsSectionTitleTextStyle = booklogsTextStyle(
    fontSize = 17,
    lineHeight = 19,
    fontWeight = FontWeight.Light,
)

val BooklogsTitleTextStyle = booklogsTextStyle(
    fontSize = 25,
    lineHeight = 29,
    fontWeight = FontWeight.SemiBold,
)

val BooklogsBodyEmphasisTextStyle = booklogsTextStyle(
    fontSize = 19,
    lineHeight = 27,
    fontWeight = FontWeight.Medium,
)

val BooklogsBodyTextStyle = booklogsTextStyle(
    fontSize = 17,
    lineHeight = 24,
    fontWeight = FontWeight.Light,
)

val BooklogsCaptionTextStyle = BooklogsTypography.labelSmall

val BooklogsCaptionEmphasisTextStyle = BooklogsTypography.labelSmall.copy(
    fontWeight = FontWeight.Medium,
)
