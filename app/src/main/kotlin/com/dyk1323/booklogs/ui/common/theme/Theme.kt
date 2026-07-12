package com.dyk1323.booklogs.ui.common.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.dyk1323.booklogs.data.settings.ThemeMode

private val LightColors = lightColorScheme(
    primary = AppleBlue,
    onPrimary = OnPrimary,
    background = CanvasLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    primaryContainer = SurfaceVariantLight,
    onPrimaryContainer = OnSurfaceLight,
    secondaryContainer = SurfaceVariantLight,
    onSecondaryContainer = OnSurfaceLight,
    tertiaryContainer = ContainerAccentLight,
    onTertiaryContainer = OnSurfaceLight,
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
    surfaceVariant = SurfaceVariantDark,
    primaryContainer = SurfaceVariantDark,
    onPrimaryContainer = OnSurfaceDark,
    secondaryContainer = SurfaceVariantDark,
    onSecondaryContainer = OnSurfaceDark,
    tertiaryContainer = ContainerAccentDark,
    onTertiaryContainer = OnSurfaceDark,
    outline = HairlineDark,
    error = StatusCriticalDark,
)

/**
 * No Material 3 dynamic color — see docs/PLAN.md "비주얼 디자인 원칙" for why the palette is fixed.
 * [themeMode] is the user's 설정 > 화면 테마 choice (docs/PLAN.md 화면 흐름 #9); SYSTEM defers to the OS.
 */
@Composable
fun BooklogsTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = BooklogsTypography,
        content = content,
    )
}
