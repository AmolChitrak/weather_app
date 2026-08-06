package com.jenil.weather.ui.core

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jenil.weather.ui.theme.WeatherExtraShapes
import com.jenil.weather.ui.theme.WeatherTheme
import com.jenil.weather.ui.theme.glassCard
import dev.chrisbanes.haze.HazeState

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Weather : Screen("weather_tab", "Weather", Icons.Outlined.Cloud)
    object Locations : Screen("locations_tab", "Locations", Icons.Outlined.Place)
    object Map : Screen("map_tab", "Map", Icons.Outlined.Map)
    object Settings : Screen("settings_route", "Settings", Icons.Outlined.Settings)
}

@Composable
fun WeatherBottomBar(
    currentRoute: String,
    onTabSelected: (Screen) -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    val items = listOf(Screen.Weather, Screen.Locations, Screen.Map, Screen.Settings)
    val selectedIndex = items.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)

    val haptic = LocalHapticFeedback.current

    var userInteracted by remember { mutableStateOf(false) }

    val barHeight = 64.dp
    val circleSize = 48.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 32.dp, end = 32.dp, top = 16.dp, bottom = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .shadow(elevation = 12.dp, shape = WeatherExtraShapes.navBar)
                .clip(WeatherExtraShapes.navBar)
                .glassCard(hazeState, shape = WeatherExtraShapes.navBar)
                .background(color = WeatherTheme.colors.surfaceCard)
                .height(barHeight)
        ) {
            val segmentWidth = maxWidth / items.size

            val offsetAnimationSpec: AnimationSpec<Dp> = if (userInteracted) {
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            } else {
                snap()
            }

            val animatedOffset by animateDpAsState(
                targetValue = segmentWidth * selectedIndex,
                animationSpec = offsetAnimationSpec,
                label = "navIndicatorOffset"
            )

            Box(
                modifier = Modifier
                    .offset(x = animatedOffset)
                    .width(segmentWidth)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(circleSize)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.inverseSurface)
                )
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                items.forEach { screen ->
                    val selected = screen.route == currentRoute

                    Box(
                        modifier = Modifier
                            .width(segmentWidth)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (!selected) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    userInteracted = true
                                    onTabSelected(screen)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = screen.title,
                            tint = if (selected) {
                                MaterialTheme.colorScheme.inverseOnSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}