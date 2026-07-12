package com.dyk1323.booklogs.ui.common.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.dyk1323.booklogs.data.settings.ThemeMode

// Every role is specified explicitly (not just the ones a screen references by name) — Material 3
// components like ModalBottomSheet/Snackbar/Chip read roles such as surfaceContainerLow/inverseSurface
// internally, and any role left unspecified silently falls back to `lightColorScheme()`/
// `darkColorScheme()`'s baseline purple-seeded token. See the comment above `ScrimColor` in Color.kt.
private val LightColors = lightColorScheme(
    primary = AppleBlue,
    onPrimary = OnPrimary,
    primaryContainer = SurfaceVariantLight,
    onPrimaryContainer = OnSurfaceLight,
    inversePrimary = AppleBlue,
    secondary = ContainerAccentLight,
    onSecondary = OnSurfaceLight,
    secondaryContainer = SurfaceVariantLight,
    onSecondaryContainer = OnSurfaceLight,
    tertiary = ContainerAccentLight,
    onTertiary = OnSurfaceLight,
    tertiaryContainer = ContainerAccentLight,
    onTertiaryContainer = OnSurfaceLight,
    background = CanvasLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = MutedLight,
    inverseSurface = OnSurfaceLight,
    inverseOnSurface = CanvasLight,
    error = StatusCriticalLight,
    outline = HairlineLight,
    outlineVariant = SurfaceVariantLight,
    scrim = ScrimColor,
    surfaceBright = SurfaceLight,
    surfaceContainer = SurfaceVariantLight,
    surfaceContainerHigh = ContainerAccentLight,
    surfaceContainerHighest = SurfaceContainerHighestLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainerLowest = SurfaceLight,
    surfaceDim = ContainerAccentLight,
)

private val DarkColors = darkColorScheme(
    primary = AppleBlue,
    onPrimary = OnPrimary,
    primaryContainer = SurfaceVariantDark,
    onPrimaryContainer = OnSurfaceDark,
    inversePrimary = AppleBlue,
    secondary = ContainerAccentDark,
    onSecondary = OnSurfaceDark,
    secondaryContainer = SurfaceVariantDark,
    onSecondaryContainer = OnSurfaceDark,
    tertiary = ContainerAccentDark,
    onTertiary = OnSurfaceDark,
    tertiaryContainer = ContainerAccentDark,
    onTertiaryContainer = OnSurfaceDark,
    background = CanvasDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceCardDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = MutedDark,
    inverseSurface = CanvasLight,
    inverseOnSurface = OnSurfaceLight,
    error = StatusCriticalDark,
    outline = HairlineDark,
    outlineVariant = SurfaceVariantDark,
    scrim = ScrimColor,
    surfaceBright = SurfaceCardDark,
    surfaceContainer = SurfaceVariantDark,
    surfaceContainerHigh = ContainerAccentDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
    surfaceContainerLow = SurfaceCardDark,
    surfaceContainerLowest = CanvasDark,
    surfaceDim = CanvasDark,
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

    // `Theme.Booklogs` (themes.xml) is a plain always-light window theme, so the OS otherwise decides
    // status/nav bar icon color from the *system* dark-mode setting — which desyncs from `darkTheme`
    // whenever 설정 > 화면 테마 overrides SYSTEM (e.g. system dark + app forced light leaves light
    // icons sitting on this screen's light background, invisible either direction).
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = BooklogsTypography,
        content = content,
    )
}
