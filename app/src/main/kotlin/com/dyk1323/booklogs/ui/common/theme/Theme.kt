package com.dyk1323.booklogs.ui.common.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = AppleBlue,
    onPrimary = OnPrimary,
    background = CanvasLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = CanvasLight,
    outline = HairlineLight,
    error = StatusCriticalLight,
)

private val DarkColors = darkColorScheme(
    primary = AppleBlue,
    onPrimary = OnPrimary,
    background = CanvasDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceCardDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceDark,
    outline = HairlineDark,
    error = StatusCriticalDark,
)

/** No Material 3 dynamic color — see docs/PLAN.md "비주얼 디자인 원칙" for why the palette is fixed. */
@Composable
fun BooklogsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = BooklogsTypography,
        content = content,
    )
}
