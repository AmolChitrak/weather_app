package com.jenil.weather.ui.core

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jenil.weather.domain.model.LifeStyleIndex
import com.jenil.weather.ui.theme.WeatherTheme
import com.jenil.weather.ui.theme.glassCard
import com.jenil.weather.utils.getBadgeColor
import com.jenil.weather.utils.getIcon
import dev.chrisbanes.haze.HazeState
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun LifestyleSection(
    modifier: Modifier = Modifier,
    indices: List<LifeStyleIndex>,
    isInitialLoading: Boolean,
    isRefreshing: Boolean = false,
    hazeState: HazeState,
) {
    if (!isInitialLoading && indices.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Text(
                text = "LIFESTYLE TIPS",
                style = WeatherTheme.typography.labelSmall,
                color = WeatherTheme.colors.onSurfaceMuted,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )

            AnimatedVisibility(visible = isRefreshing) {
                RefreshPulseDot(modifier = Modifier.padding(start = 6.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        AnimatedContent(
            targetState = isInitialLoading,
            transitionSpec = {
                fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
            },
            label = "ai_cards_transition"
        ) { loading ->
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (loading) {
                    items(2) { LifestyleCardSkeleton(hazeState = hazeState) }
                } else {
                    items(indices, key = { it.category.name }) { index ->
                        LifeStyleIndexCard(
                            index = index,
                            hazeState = hazeState,
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun RefreshPulseDot(modifier: Modifier = Modifier) {
    val pulse = rememberInfiniteTransition(label = "refresh_pulse")
    val alpha by pulse.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "refresh_pulse_alpha"
    )

    Box(
        modifier = modifier
            .size(4.dp)
            .clip(CircleShape)
            .background(WeatherTheme.colors.onSurfaceMuted.copy(alpha = alpha))
    )
}

@Composable
private fun LifestyleCardSkeleton(
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    val shimmer = rememberInfiniteTransition(label = "shimmer")
    val alpha by shimmer.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )
    val shimmerColor = WeatherTheme.colors.onSurfaceMuted.copy(alpha = alpha * 0.25f)

    Box(
        modifier = modifier
            .width(260.dp)
            .glassCard(hazeState = hazeState, shape = RoundedCornerShape(24.dp))
            .background(WeatherTheme.colors.surfaceCard)
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(shimmerColor)
                )
                Box(
                    modifier = Modifier
                        .size(width = 56.dp, height = 20.dp)
                        .clip(RoundedCornerShape(50))
                        .background(shimmerColor)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerColor)
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerColor)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerColor)
                )
            }
        }
    }
}

@Composable
fun LifeStyleIndexCard(
    index: LifeStyleIndex,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(260.dp)
            .glassCard(
                hazeState = hazeState,
                shape = RoundedCornerShape(24.dp)
            )
            .background(color = WeatherTheme.colors.surfaceCard)
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Row: Icon + Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(index.level.getBadgeColor().copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = index.category.getIcon(),
                        contentDescription = null,
                        tint = index.level.getBadgeColor(),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(index.level.getBadgeColor().copy(alpha = 0.18f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = index.level.name,
                        style = WeatherTheme.typography.labelSmall,
                        color = index.level.getBadgeColor(),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Title
            Text(
                text = index.title,
                style = WeatherTheme.typography.titleSmall,
                color = WeatherTheme.colors.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Recommendation
            Text(
                text = index.recommendation,
                style = WeatherTheme.typography.bodySmall,
                color = WeatherTheme.colors.onSurfaceMuted,
                lineHeight = 18.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}