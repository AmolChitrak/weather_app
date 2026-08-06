package com.jenil.weather.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jenil.weather.ui.weather.WeatherViewModel
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

private val SkyTop = Color(0xFF4FB3F7)
private val SkyBottom = Color(0xFF1665C4)
private val SunCore = Color(0xFFFFC94A)
private val SunGlow = Color(0xFFFFE18A)
private val CloudWhite = Color(0xFFFFFFFF)
private val CloudShadow = Color(0xFFDCEBFA)
private val RainBlue = Color(0xFFBFE0FF)

@Composable
fun SplashScreen(
    viewModel: WeatherViewModel,
    onReady: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var minDurationElapsed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchWeatherForCurrentLocation()

        delay(1200.milliseconds)
        minDurationElapsed = true
    }

    val dataResolved = uiState.data != null || uiState.error != null
    LaunchedEffect(dataResolved, minDurationElapsed) {
        if (dataResolved && minDurationElapsed) onReady()
    }

    LaunchedEffect(Unit) {
        delay(6000.milliseconds)
        onReady()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "splash")

    val sunGlowPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "sun_glow"
    )

    val sunRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(14000, easing = LinearEasing)),
        label = "sun_rotation"
    )

    val rainProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing)),
        label = "rain_progress"
    )

    val cloudHover by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(tween(2500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "cloud_hover"
    )

    val entrance = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        entrance.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    }

    val titleReveal = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(300.milliseconds)
        titleReveal.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
    }

    val progressSweep by infiniteTransition.animateFloat(
        initialValue = -0.1f, // Tightened from -0.4f
        targetValue = 1.1f,   // Tightened from 1.4f
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing)),
        label = "progress_sweep"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(SkyTop, SkyBottom),
                    center = Offset.Unspecified.let { Offset(0.5f, 0.32f) },
                    radius = 1400f
                )
            )
            .background(Brush.verticalGradient(listOf(Color.Transparent, SkyBottom.copy(alpha = 0.35f)))),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .graphicsLayer(scaleX = entrance.value, scaleY = entrance.value, alpha = entrance.value)
        ) {
            SunCloudRainGlyph(
                glowPulse = sunGlowPulse,
                sunRotation = sunRotation,
                rainProgress = rainProgress,
                cloudHoverY = cloudHover
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .offset(y = 130.dp)
                .graphicsAlpha(titleReveal.value)
        ) {
            Text(
                text = "Weatherly",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.3.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Skies, simplified",
                color = Color.White.copy(alpha = 0.68f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.4.sp
            )

            Spacer(Modifier.height(32.dp))
            ProgressLine(sweep = progressSweep)
        }
    }
}

@Composable
private fun SunCloudRainGlyph(
    glowPulse: Float,
    sunRotation: Float,
    rainProgress: Float,
    cloudHoverY: Float
) {
    Canvas(modifier = Modifier.fillMaxSize()) {

        val sunCenter = Offset(size.width * 0.68f, size.height * 0.35f)
        val sunRadius = size.minDimension / 5.2f

        rotate(degrees = sunRotation, pivot = sunCenter) {
            repeat(8) { i ->
                rotate(degrees = i * 45f, pivot = sunCenter) {
                    val innerR = sunRadius + 12.dp.toPx()
                    val outerR = innerR + sunRadius * 0.35f

                    drawLine(
                        color = SunGlow.copy(alpha = 0.8f),
                        start = sunCenter.copy(y = sunCenter.y - innerR),
                        end = sunCenter.copy(y = sunCenter.y - outerR),
                        strokeWidth = 5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(SunGlow.copy(alpha = 0.4f * glowPulse), Color.Transparent),
                center = sunCenter,
                radius = sunRadius * 2.8f
            ),
            radius = sunRadius * 2.8f,
            center = sunCenter
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(SunGlow, SunCore),
                center = sunCenter,
                radius = sunRadius * 1.3f
            ),
            radius = sunRadius,
            center = sunCenter
        )

        val baseScale = size.minDimension / 240f
        val cloudScale = baseScale * 0.82f
        val cloudCenter = Offset(size.width * 0.46f, (size.height * 0.54f) + cloudHoverY.dp.toPx())

        drawRain(cloudCenter, cloudScale, rainProgress)
        drawCloud(cloudCenter, cloudScale)
    }
}

private fun DrawScope.drawCloud(center: Offset, scale: Float) {
    val r = 28.dp.toPx() * scale

    val leftCircleCenter = center + Offset(-r * 1.1f, r * 0.15f)
    val rightCircleCenter = center + Offset(r * 1.15f, r * 0.3f)
    val topCircleCenter = center + Offset(0f, -r * 0.3f)

    val rectTopLeft = Offset(leftCircleCenter.x, center.y - r * 0.1f)
    val rectSize = Size(rightCircleCenter.x - leftCircleCenter.x + r * 0.2f, r * 1.1f)

    val cloudPath = Path().apply {
        addOval(Rect(center = leftCircleCenter, radius = r * 0.8f))
        addOval(Rect(center = rightCircleCenter, radius = r * 0.7f))
        addOval(Rect(center = topCircleCenter, radius = r * 1.15f))
        addRoundRect(
            RoundRect(
                left = rectTopLeft.x,
                top = rectTopLeft.y,
                right = rectTopLeft.x + rectSize.width,
                bottom = rectTopLeft.y + rectSize.height,
                cornerRadius = CornerRadius(r * 0.6f)
            )
        )
    }

    translate(left = 2.dp.toPx(), top = 6.dp.toPx()) {
        drawPath(path = cloudPath, color = CloudShadow)
    }

    drawPath(path = cloudPath, color = CloudWhite)
}

private fun DrawScope.drawRain(cloudCenter: Offset, scale: Float, progress: Float) {
    val dropsX = listOf(-0.4f, 0f, 0.4f)
    val baseY = cloudCenter.y + 20.dp.toPx() * scale
    val fallDistance = 28.dp.toPx() * scale

    dropsX.forEachIndexed { index, xOffset ->
        val phase = (progress + index * 0.33f) % 1f
        val alpha = if (phase < 0.2f) phase * 5f else if (phase > 0.8f) (1f - phase) * 5f else 1f
        val yStart = baseY + (fallDistance * phase)
        val yEnd = yStart + (10.dp.toPx() * scale)
        val x = cloudCenter.x + xOffset * 40.dp.toPx() * scale

        drawLine(
            color = RainBlue.copy(alpha = alpha * 0.9f),
            start = Offset(x, yStart),
            end = Offset(x, yEnd),
            strokeWidth = 4.dp.toPx() * scale,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun ProgressLine(sweep: Float, modifier: Modifier = Modifier) {
    val trackWidth = 140.dp
    val indicatorWidth = 36.dp

    Box(
        modifier = modifier
            .width(trackWidth)
            .height(3.dp)
            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(50))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(indicatorWidth)
                .graphicsLayer {
                    // Correct total travel math for a flawless loop
                    val totalTravel = trackWidth.toPx() + indicatorWidth.toPx()
                    translationX = (sweep * totalTravel) - indicatorWidth.toPx()
                }
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, Color.White.copy(alpha = 0.9f), Color.Transparent)
                    ),
                    RoundedCornerShape(50)
                )
        )
    }
}

private fun Modifier.graphicsAlpha(value: Float) = this.then(
    Modifier.graphicsLayer(alpha = value, translationY = (1f - value) * 16f)
)