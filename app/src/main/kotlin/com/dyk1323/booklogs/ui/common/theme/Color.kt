package com.dyk1323.booklogs.ui.common.theme

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
