package com.jenil.weather.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Material3 default shape scale — used for generic components (buttons, dialogs)
val WeatherShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

// Extra shapes for the specific components in the mockups, not covered by
// Material3's default scale (chips, hero card, pill nav bar, stat tiles)
object WeatherExtraShapes {
    val heroCard = RoundedCornerShape(28.dp)       // big weather card
    val statTile = RoundedCornerShape(20.dp)       // wind/humidity/etc tiles
    val forecastChip = RoundedCornerShape(18.dp)   // Sat/Sun/Mon day chips

    val locationRow = RoundedCornerShape(20.dp)    // nearby-locations card
    val navBar = RoundedCornerShape(28.dp)         // outer pill nav bar
    val navPill = RoundedCornerShape(20.dp)        // selected icon's pill highlight
    val badge = RoundedCornerShape(16.dp)          // air-quality health-risk badge
}