package com.jenil.weather.ui.theme

import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------
// DARK MODE — matches the original mockups (near-black bg, floating gray cards)
// ---------------------------------------------------------------------------
val DarkBackground = Color(0xFF101215)
val DarkSurfaceCard = Color(0xFF1B1E22)      // hero card, forecast row bg
val DarkSurfaceElevated = Color(0xFF24272C)  // nested tiles (stat tiles, chips)
val DarkSurfaceElevatedHigh = Color(0xFF2E323A) // pressed/selected nav pill, popovers

val DarkOnBackground = Color(0xFFF5F6F7)
val DarkOnSurface = Color(0xFFF5F6F7)
val DarkOnSurfaceMuted = Color(0xFF8A8F98)   // labels like "WIND", "HUMIDITY"
val DarkOnSurfaceFaint = Color(0xFF5A5F66)   // graph axis labels, disabled

val DarkDivider = Color(0xFF2A2D32)
val DarkNavSelectedBg = Color(0xFFFFFFFF)
val DarkNavSelectedIcon = Color(0xFF101215)
val DarkNavUnselectedIcon = Color(0xFF8A8F98)

// ---------------------------------------------------------------------------
// LIGHT MODE — same structure, depth via shadow/border instead of brightness
// ---------------------------------------------------------------------------
val LightBackground = Color(0xFFF3F4F6)
val LightSurfaceCard = Color(0xFFFFFFFF)
val LightSurfaceElevated = Color(0xFFF7F8FA)   // nested tiles sit one step off white
val LightSurfaceElevatedHigh = Color(0xFFEDEEF1)

val LightOnBackground = Color(0xFF14161A)
val LightOnSurface = Color(0xFF14161A)
val LightOnSurfaceMuted = Color(0xFF6B7076)
val LightOnSurfaceFaint = Color(0xFFA6ABB2)

val LightDivider = Color(0xFFE7E8EB)
val LightNavSelectedBg = Color(0xFF14161A)
val LightNavSelectedIcon = Color(0xFFFFFFFF)
val LightNavUnselectedIcon = Color(0xFF9498A0)

// ---------------------------------------------------------------------------
// BRAND / DATA-VIZ ACCENTS — identical in both modes (they carry meaning,
// e.g. the pressure gradient, so they shouldn't shift with theme)
// ---------------------------------------------------------------------------
val AccentSun = Color(0xFFFFC24B)
val AccentSky = Color(0xFF4FC3F7)
val AccentRain = Color(0xFF5B8DEF)
val AccentBadgeRed = Color(0xFFE6395A)     // "Health Risk" badge
val AccentBadgeGreen = Color(0xFF3ECF8E)   // low-risk / good AQI state


val GradientPressureTop = Color(0xFF8B5CF6)    // purple (high)
val GradientPressureMid = Color(0xFF4FC3F7)    // blue
val GradientPressureBottom = Color(0xFF3ECF8E) // green (low)

val GraphLineColor = Color(0xFFF5F6F7)
val GraphLineColorLight = Color(0xFF14161A)
val GraphHighlightDot = Color(0xFF4FC3F7)

val DarkCardBorder = Color(0xFFFFFFFF).copy(alpha = 0.08f)
val LightCardBorder = Color(0xFF14161A).copy(alpha = 0.05f)

val DarkNavContainerBg = Color(0xFF1B1E22).copy(alpha = 0.85f)
val LightNavContainerBg = Color(0xFFFFFFFF).copy(alpha = 0.85f)