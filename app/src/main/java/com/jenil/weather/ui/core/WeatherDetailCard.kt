package com.jenil.weather.ui.core

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WeatherDetailCard(
    icon: ImageVector,
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    accentColor: Color? = null,
    progress: Float? = null,
    progressTrackColors: List<Color>? = null
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(24.dp),
                clip = false,
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)
                    )
                )
            )
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.03f)) // subtle tonal tint
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(16.dp)
    ) {
        Column {
            // Card Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Value Display
            AnimatedContent(
                targetState = value,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(150)) },
                label = "detail_value_crossfade"
            ) { animatedValue ->
                Text(
                    text = animatedValue,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            }

            if (progress != null && accentColor != null && progressTrackColors != null) {
                Spacer(modifier = Modifier.height(8.dp))

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp), // Taller height to fit the overlapping dot
                    contentAlignment = Alignment.CenterStart
                ) {
                    // 1. The Full-Width Gradient Track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp) // Thinner track
                            .clip(RoundedCornerShape(2.dp))
                            .background(Brush.horizontalGradient(progressTrackColors))
                    )

                    // 2. The Marker Dot
                    val safeProgress = progress.coerceIn(0f, 1f)
                    val dotSize = 10.dp

                    // Subtract the dot's own width from the max width so it stops exactly at the end
                    val offsetX = (maxWidth - dotSize) * safeProgress

                    Box(
                        modifier = Modifier
                            .padding(start = offsetX)
                            .size(dotSize)
                            .clip(CircleShape)
                            .background(Color.White) // Crisp white center
                            .border(2.dp, accentColor, CircleShape) // Ring colored by current risk level
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle Display
            AnimatedContent(
                targetState = subtitle,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(150)) },
                label = "detail_subtitle_crossfade"
            ) { animatedSubtitle ->
                Text(
                    text = animatedSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun WindCompassCard(
    windDirectionDegree: Int,
    windDirectionString: String,
    modifier: Modifier = Modifier
) {

    var previousDegree by remember { mutableFloatStateOf(windDirectionDegree.toFloat()) }
    var accumulatedRotation by remember { mutableFloatStateOf(windDirectionDegree.toFloat()) }

    LaunchedEffect(windDirectionDegree) {
        val target = windDirectionDegree.toFloat()
        var delta = (target - previousDegree) % 360f
        if (delta > 180f) delta -= 360f
        if (delta < -180f) delta += 360f
        accumulatedRotation += delta
        previousDegree = target
    }

    val rotation by animateFloatAsState(
        targetValue = accumulatedRotation,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "compass_rotation"
    )

    Box(
        modifier = modifier
            .semantics(mergeDescendants = true) {
                contentDescription = "Wind direction: $windDirectionString, $windDirectionDegree degrees"
            }
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(24.dp),
                clip = false,
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)
                    )
                )
            )
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.03f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(16.dp)
    ) {
        Column {
            // Header — matches WeatherDetailCard's header exactly
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Outlined.Explore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "WIND DIRECTION",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    AnimatedContent(
                        targetState = windDirectionString,
                        transitionSpec = {
                            (fadeIn(tween(300)) + slideInVertically { it / 3 }) togetherWith
                                    (fadeOut(tween(150)))
                        },
                        label = "wind_dir_crossfade"
                    ) { animatedDir ->
                        Text(
                            text = animatedDir,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    AnimatedContent(
                        targetState = windDirectionDegree,
                        transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(150)) },
                        label = "wind_degree_crossfade"
                    ) { animatedDegree ->
                        Text(
                            text = "$animatedDegree°",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                }

                CompassRose(
                    rotationDegrees = rotation,
                    modifier = Modifier.size(56.dp)
                )
            }
        }
    }
}

@Composable
private fun CompassRose(
    rotationDegrees: Float,
    modifier: Modifier = Modifier
) {
    val tickColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
    val ringColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    val northColor = MaterialTheme.colorScheme.error
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val arrowColor = MaterialTheme.colorScheme.primary
    val textMeasurer = rememberTextMeasurer()

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2
            val center = Offset(size.width / 2, size.height / 2)

            // Outer ring
            drawCircle(color = ringColor, radius = radius, style = Stroke(width = 1.5.dp.toPx()))

            // Tick marks every 30°, longer ticks on cardinal points
            for (angle in 0 until 360 step 30) {
                val isCardinal = angle % 90 == 0
                val tickLength = if (isCardinal) 6.dp.toPx() else 3.dp.toPx()
                val rad = Math.toRadians(angle.toDouble() - 90)
                val outer = Offset(
                    x = center.x + (radius - 2.dp.toPx()) * cos(rad).toFloat(),
                    y = center.y + (radius - 2.dp.toPx()) * sin(rad).toFloat()
                )
                val inner = Offset(
                    x = center.x + (radius - 2.dp.toPx() - tickLength) * cos(rad).toFloat(),
                    y = center.y + (radius - 2.dp.toPx() - tickLength) * sin(rad).toFloat()
                )
                drawLine(
                    color = if (angle == 0) northColor else tickColor,
                    start = inner,
                    end = outer,
                    strokeWidth = if (isCardinal) 2.dp.toPx() else 1.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // N label at top
            val nLayout = textMeasurer.measure(
                text = AnnotatedString("N"),
                style = TextStyle(fontSize = 8.sp, fontWeight = FontWeight.Bold, color = northColor)
            )
            drawText(
                textLayoutResult = nLayout,
                topLeft = Offset(
                    center.x - nLayout.size.width / 2,
                    center.y - radius + 8.dp.toPx()
                )
            )
        }

        // Rotating needle
        Icon(
            imageVector = Icons.Filled.Navigation,
            contentDescription = null,
            tint = arrowColor,
            modifier = Modifier
                .size(26.dp)
                .graphicsLayer { rotationZ = rotationDegrees }
        )
    }
}