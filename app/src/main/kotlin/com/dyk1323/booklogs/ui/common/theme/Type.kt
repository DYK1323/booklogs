package com.dyk1323.booklogs.ui.common.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.dyk1323.booklogs.R

/**
 * REQUIRES four Pretendard static TTF files to be added at `app/src/main/res/font/` before this
 * compiles — see docs/PLAN.md "Gradle 설정": download from https://github.com/orioncactus/pretendard
 * (static TTFs under packages/pretendard/dist/public/static/) and place as:
 *   res/font/pretendard_regular.ttf   (Regular, 400)
 *   res/font/pretendard_medium.ttf    (Medium, 500)
 *   res/font/pretendard_semibold.ttf  (SemiBold, 600)
 *   res/font/pretendard_bold.ttf      (Bold, 700)
 * This could not be done from the build sandbox (no reachable path to the release assets found), so
 * it's the one manual step left before `:app` will compile.
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
        fontSize = 48.sp,
        lineHeight = 51.sp, // ~1.07x
        letterSpacing = (-0.28).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.3).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 21.sp,
        lineHeight = 25.sp,
        letterSpacing = 0.2.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp,
        lineHeight = 19.sp,
        letterSpacing = 0.2.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.2).sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.2).sp,
    ),
    labelLarge = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        letterSpacing = 0.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 17.sp,
        letterSpacing = (-0.1).sp,
    ),
    labelSmall = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 15.sp,
    ),
)
