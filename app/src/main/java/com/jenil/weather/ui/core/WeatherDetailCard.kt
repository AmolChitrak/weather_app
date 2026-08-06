package com.jenil.weather.ui.core

import androidx.annotation.DrawableRes
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jenil.weather.ui.theme.WeatherTheme
import com.jenil.weather.ui.theme.glassCard
import dev.chrisbanes.haze.HazeState
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WeatherDetailCard(
    icon: ImageVector,
    title: String,
    value: String,
    subtitle: String,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    accentColor: Color? = null,
    progress: Float? = null,
    progressTrackColors: List<Color>? = null
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .glassCard(
                hazeState = hazeState,
                shape = RoundedCornerShape(24.dp)
            )
            .background(
                color = WeatherTheme.colors.surfaceCard,
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor ?: WeatherTheme.colors.onSurface.copy(alpha = 0.8f),
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = title,
                        style = WeatherTheme.typography.labelSmall,
                        color = WeatherTheme.colors.onSurfaceMuted,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                AnimatedContent(
                    targetState = value,
                    transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(150)) },
                    label = "detail_value_crossfade"
                ) { animatedValue ->
                    Text(
                        text = animatedValue,
                        style = WeatherTheme.typography.headlineSmall,
                        color = WeatherTheme.colors.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column {
                if (progress != null && accentColor != null && progressTrackColors != null) {
                    Spacer(modifier = Modifier.height(8.dp))

                    InsightGaugeTrack(
                        progress = progress,
                        accentColor = accentColor,
                        trackColors = progressTrackColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                AnimatedContent(
                    targetState = subtitle,
                    transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(150)) },
                    label = "detail_subtitle_crossfade"
                ) { animatedSubtitle ->
                    Text(
                        text = animatedSubtitle,
                        style = WeatherTheme.typography.bodySmall,
                        color = WeatherTheme.colors.onSurfaceMuted,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun InsightGaugeTrack(
    progress: Float,
    accentColor: Color,
    trackColors: List<Color>,
    modifier: Modifier = Modifier
) {
    val safeProgress = progress.coerceIn(0f, 1f)

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val trackHeight = 4.dp.toPx()
        val centerY = height / 2f

        drawRoundRect(
            brush = Brush.horizontalGradient(trackColors),
            topLeft = Offset(x = 0f, y = centerY - trackHeight / 2f),
            size = Size(width, trackHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeight / 2f)
        )

        val thumbRadius = 5.dp.toPx()
        val thumbX = (width * safeProgress).coerceIn(thumbRadius, width - thumbRadius)

        drawCircle(
            color = accentColor.copy(alpha = 0.3f),
            radius = thumbRadius + 2.dp.toPx(),
            center = Offset(x = thumbX, y = centerY)
        )

        drawCircle(
            color = Color.White,
            radius = thumbRadius,
            center = Offset(x = thumbX, y = centerY)
        )

        drawCircle(
            color = accentColor,
            radius = thumbRadius - 1.5.dp.toPx(),
            center = Offset(x = thumbX, y = centerY)
        )
    }
}

@Composable
fun WindCompassCard(
    windDirectionDegree: Int,
    windDirectionString: String,
    hazeState: HazeState,
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
            .fillMaxHeight()
            .semantics(mergeDescendants = true) {
                contentDescription =
                    "Wind direction: $windDirectionString, $windDirectionDegree degrees"
            }
            .glassCard(
                hazeState = hazeState,
                shape = RoundedCornerShape(24.dp)
            )
            .background(
                color = WeatherTheme.colors.surfaceCard,
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Explore,
                        contentDescription = null,
                        tint = WeatherTheme.colors.onSurface.copy(alpha = 0.8f),
                        modifier = Modifier.size(14.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "WIND DIRECTION",
                    style = WeatherTheme.typography.labelSmall,
                    color = WeatherTheme.colors.onSurfaceMuted,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

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
                            style = WeatherTheme.typography.headlineSmall,
                            color = WeatherTheme.colors.onSurface,
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
                            style = WeatherTheme.typography.bodySmall,
                            color = WeatherTheme.colors.onSurfaceMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                CompassRose(
                    rotationDegrees = rotation,
                    modifier = Modifier.size(54.dp)
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
    val tickColor = WeatherTheme.colors.onSurface.copy(alpha = 0.25f)
    val ringColor = WeatherTheme.colors.onSurface.copy(alpha = 0.12f)

    // Reverted to MaterialTheme.colorScheme for error & primary
    val northColor = MaterialTheme.colorScheme.error
    val arrowColor = MaterialTheme.colorScheme.primary
    val textMeasurer = rememberTextMeasurer()

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            drawCircle(color = ringColor, radius = radius, style = Stroke(width = 1.5.dp.toPx()))

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

            val nLayout = textMeasurer.measure(
                text = AnnotatedString("N"),
                style = TextStyle(fontSize = 8.sp, fontWeight = FontWeight.Bold, color = northColor)
            )
            drawText(
                textLayoutResult = nLayout,
                topLeft = Offset(
                    center.x - nLayout.size.width / 2f,
                    center.y - radius + 7.dp.toPx()
                )
            )
        }

        Icon(
            imageVector = Icons.Filled.Navigation,
            contentDescription = null,
            tint = arrowColor,
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer { rotationZ = rotationDegrees }
        )
    }
}

@Composable
fun SunTimeCard(
    title: String,
    time: String,
    @DrawableRes iconRes: Int,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .glassCard(
                hazeState = hazeState,
                shape = RoundedCornerShape(24.dp)
            )
            .background(
                color = WeatherTheme.colors.surfaceCard,
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title.uppercase(),
                    style = WeatherTheme.typography.labelSmall,
                    color = WeatherTheme.colors.onSurfaceMuted,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                AnimatedContent(
                    targetState = time,
                    transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(150)) },
                    label = "${title}_time_crossfade"
                ) { animatedTime ->
                    Text(
                        text = animatedTime,
                        style = WeatherTheme.typography.headlineSmall,
                        color = WeatherTheme.colors.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = "$title Icon",
                    modifier = Modifier.size(72.dp)
                )
            }
        }
    }
}


@Composable
fun WeatherCardContainer(
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .glassCard(
                hazeState = hazeState,
                shape = shape
            )
            .background(
                color = WeatherTheme.colors.surfaceCard
            )
            .padding(16.dp),
        content = content
    )
}


@Composable
fun GenericWeatherCard(
    title: String,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    value: String? = null,
    subtitle: String? = null,
    headerIcon: ImageVector? = null,
    headerIconAccent: Color? = null,
    graphicContent: (@Composable BoxScope.() -> Unit)? = null
) {
    WeatherCardContainer(
        hazeState = hazeState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (headerIcon != null) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = headerIcon,
                                contentDescription = null,
                                tint = headerIconAccent ?: WeatherTheme.colors.onSurface.copy(alpha = 0.8f),
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Text(
                        text = title.uppercase(),
                        style = WeatherTheme.typography.labelSmall,
                        color = WeatherTheme.colors.onSurfaceMuted,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }

                if (value != null) {
                    Spacer(modifier = Modifier.height(8.dp))

                    AnimatedContent(
                        targetState = value,
                        transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(150)) },
                        label = "${title}_value_crossfade"
                    ) { animatedValue ->
                        Text(
                            text = animatedValue,
                            style = WeatherTheme.typography.headlineSmall,
                            color = WeatherTheme.colors.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(4.dp))

                    AnimatedContent(
                        targetState = subtitle,
                        transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(150)) },
                        label = "${title}_subtitle_crossfade"
                    ) { animatedSubtitle ->
                        Text(
                            text = animatedSubtitle,
                            style = WeatherTheme.typography.bodySmall,
                            color = WeatherTheme.colors.onSurfaceMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (graphicContent != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                    content = graphicContent
                )
            }
        }
    }
}