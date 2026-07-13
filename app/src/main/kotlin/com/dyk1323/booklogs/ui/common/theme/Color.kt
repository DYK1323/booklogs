package com.dyk1323.booklogs.ui.common.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// Values from docs/PLAN.md "비주얼 디자인 원칙" — Apple HIG-derived palette (Apple Blue as the sole
// chromatic accent; success/critical are iOS system colors added since the source doc has no app
// states). Not from Material 3 dynamic color.

val AppleBlue = Color(0xFF0071E3)
val OnPrimary = Color(0xFFFFFFFF)

val CanvasLight = Color(0xFFF5F5F7)
val CanvasDark = Color(0xFF000000)

val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceDark = Color(0xFF000000)
val SurfaceCardDark = Color(0xFF1D1D1F) // dark-mode cards lift via this tone, not shadow

// iOS systemGray5 — a visibly distinct fill tone for placeholders/chips/photo-box backgrounds. Was
// previously mapped to the same value as `background` in Theme.kt, making anything using it (book
// cover placeholders, library filter chips, the quote-capture photo box) invisible against the screen.
val SurfaceVariantLight = Color(0xFFE5E5EA)
val SurfaceVariantDark = Color(0xFF3A3A3C)

// iOS systemGray4 — one step darker/lighter than SurfaceVariant, for the "active/selected" state of a
// gray fill pair (e.g. a toggled chip) so it reads as pressed against the neutral default fill. Used in
// place of Material 3's default primary/secondary/tertiary "Container" roles, which are a light purple
// baked into `lightColorScheme()`/`darkColorScheme()` when left unspecified — clashing with the app's
// single blue accent (see docs/PLAN.md "비주얼 디자인 원칙").
val ContainerAccentLight = Color(0xFFD1D1D6)
val ContainerAccentDark = Color(0xFF48484A)

val OnSurfaceLight = Color(0xFF1D1D1F)
val OnSurfaceDark = Color(0xFFFFFFFF)

val SecondaryTextLight = Color(0xCC000000) // rgba(0,0,0,0.8)
val SecondaryTextDark = Color(0xCCFFFFFF)

val MutedLight = Color(0x7A000000) // rgba(0,0,0,0.48)
val MutedDark = Color(0x7AFFFFFF)

val LinkLight = Color(0xFF0066CC)
val LinkDark = Color(0xFF2997FF)

val HairlineLight = Color(0x1A000000) // rgba(0,0,0,0.10)
val HairlineDark = Color(0x1AFFFFFF)

val StatusGoodLight = Color(0xFF34C759)
val StatusGoodDark = Color(0xFF30D158)

val StatusCriticalLight = Color(0xFFFF3B30)
val StatusCriticalDark = Color(0xFFFF453A)

// Fills the remaining Material 3 ColorScheme roles that `Theme.kt` didn't already override (surface
// containers, secondary/tertiary, outlineVariant, inverse*, scrim) — all of these fall back to
// `lightColorScheme()`/`darkColorScheme()`'s baseline purple-seeded tokens when left unspecified, which
// is exactly how `surfaceContainerHigh` ended up as the light-purple tint on QuoteCard/ReviewCard even
// though the app is supposed to have a single blue accent (see "ContainerAccent" comment above — same
// root cause, different roles this time; ModalBottomSheet/Snackbar pull these roles internally even
// though no screen references them by name directly).
val SurfaceContainerLowLight = Color(0xFFF2F2F7) // iOS systemGray6
val SurfaceContainerHighestLight = Color(0xFFC7C7CC) // iOS systemGray3
val SurfaceContainerHighestDark = Color(0xFF636366) // iOS systemGray2 (dark)
val ScrimColor = Color(0xFF000000)

val BooklogsScreenBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surface

val BooklogsTextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSurface

val BooklogsTextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSurfaceVariant

val BooklogsTextTertiary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)

val BooklogsTextPlaceholder: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.56f)

val BooklogsAccent: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.primary

val BooklogsOnAccent: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onPrimary

val BooklogsSurfaceMuted: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surfaceVariant

val BooklogsHairline: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.outline

val BooklogsOverlayScrim = ScrimColor.copy(alpha = 0.56f)
val BooklogsOnOverlay = OnPrimary
