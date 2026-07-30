package com.jenil.weather.ui.core

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefreshIndicator(
    isRefreshing: Boolean,
    state: PullToRefreshState,
    modifier: Modifier = Modifier,
) {

    val pullDistance = state.distanceFraction.coerceIn(0f, 1f)

    // Spring scale
    val scale by animateFloatAsState(
        targetValue = if (isRefreshing) 1f else pullDistance.coerceAtLeast(0.01f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "scale"
    )

    // Fade
    val alpha by animateFloatAsState(
        targetValue = if (pullDistance > 0f || isRefreshing) 1f else 0f,
        label = "alpha"
    )

    // Elevation
    val elevation by animateDpAsState(
        targetValue = if (pullDistance > 0f || isRefreshing) 8.dp else 0.dp,
        label = "elevation"
    )

    // Background color
    val backgroundColor by animateColorAsState(
        targetValue = if (isRefreshing) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
        },
        label = "background"
    )

    // Icon color
    val iconTint by animateColorAsState(
        targetValue = lerp(
            MaterialTheme.colorScheme.onSurfaceVariant,
            MaterialTheme.colorScheme.primary,
            pullDistance
        ),
        label = "iconTint"
    )

    // Rotation
    val infiniteTransition = rememberInfiniteTransition(label = "spin")

    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = LinearEasing
            )
        ),
        label = "spin"
    )

    val rotation = if (isRefreshing) {
        spinAngle
    } else {
        pullDistance * 180f
    }

    val haptic = LocalHapticFeedback.current
    var hasTriggeredHaptic by remember { mutableStateOf(false) }

    LaunchedEffect(pullDistance) {
        if (pullDistance >= 1f && !hasTriggeredHaptic) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            hasTriggeredHaptic = true
        }

        if (pullDistance < 1f) {
            hasTriggeredHaptic = false
        }
    }

    Box(
        modifier = modifier
            .size(48.dp)
            .alpha(alpha)
            .scale(scale)
            .shadow(
                elevation = elevation,
                shape = CircleShape,
                clip = false
            )
            .clip(CircleShape)
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Public,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier
                .size(22.dp)
                .rotate(rotation)
        )
    }
}