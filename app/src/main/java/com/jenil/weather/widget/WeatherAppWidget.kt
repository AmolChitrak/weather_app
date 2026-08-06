package com.jenil.weather.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.material3.ColorProviders
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.jenil.weather.MainActivity
import com.jenil.weather.data.remote.dto.toDisplayName
import com.jenil.weather.di.AppModule
import com.jenil.weather.domain.model.WeatherData
import com.jenil.weather.ui.core.widgetWeatherIcon
import com.jenil.weather.ui.theme.DarkColors
import com.jenil.weather.ui.theme.LightColors
import com.jenil.weather.utils.cardSurfaceColor
import com.jenil.weather.utils.mutedTextColor
import com.jenil.weather.utils.primaryTextColor
import com.jenil.weather.utils.semanticConditionColor
import com.jenil.weather.utils.thermalColor
import dagger.hilt.android.EntryPointAccessors

private val WIDGET_SMALL = DpSize(80.dp, 80.dp)
private val WIDGET_MEDIUM = DpSize(160.dp, 80.dp)
private val WIDGET_LARGE = DpSize(220.dp, 140.dp)



class WeatherAppWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(WIDGET_SMALL, WIDGET_MEDIUM, WIDGET_LARGE)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            AppModule.WeatherWidgetEntryPoint::class.java
        )
        val repository = entryPoint.weatherRepository()
        val cachedWeather = repository.getCachedWeather()

        provideContent {
            GlanceTheme(colors = ColorProviders(light = LightColors, dark = DarkColors)) {
                WeatherWidgetContent(cachedWeather)
            }
        }
    }
}

@Composable
fun WeatherWidgetContent(weather: WeatherData?) {
    val size = LocalSize.current

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(cardSurfaceColor())
            .appWidgetBackground()
            .cornerRadius(24.dp)
            .padding(12.dp)
            .clickable(actionStartActivity<MainActivity>()),
        contentAlignment = Alignment.Center
    ) {
        if (weather != null) {
            when {
                size.width < WIDGET_MEDIUM.width -> CompactContent(weather)
                size.width < WIDGET_LARGE.width -> MediumContent(weather)
                else -> LargeContent(weather)
            }
        } else {
            Text(
                text = "Open app to load",
                style = TextStyle(
                    color = mutedTextColor(),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                ),
            )
        }
    }
}

@Composable
private fun CompactContent(weather: WeatherData) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            provider = ImageProvider(widgetWeatherIcon(weather.condition)),
            contentDescription = weather.condition.toDisplayName(),
            modifier = GlanceModifier.size(32.dp),
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = "${weather.temperature}°",
            style = TextStyle(
                color = thermalColor(weather.temperature),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}

@Composable
private fun MediumContent(weather: WeatherData) {
    Row(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = weather.cityName,
                style = TextStyle(
                    color = primaryTextColor(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(modifier = GlanceModifier.height(2.dp))
            Text(
                text = "${weather.temperature}°",
                style = TextStyle(
                    color = thermalColor(weather.temperature),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                )
            )
            Spacer(modifier = GlanceModifier.height(2.dp))
            Text(
                text = weather.condition.toDisplayName(),
                style = TextStyle(
                    color = semanticConditionColor(weather.condition.toDisplayName()),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }

        Image(
            provider = ImageProvider(widgetWeatherIcon(weather.condition)),
            contentDescription = null,
            modifier = GlanceModifier.size(48.dp),
        )
    }
}

@Composable
private fun LargeContent(weather: WeatherData) {
    val conditionName = weather.condition.toDisplayName()

    Column(
        modifier = GlanceModifier.fillMaxSize()
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = weather.cityName,
                    style = TextStyle(
                        color = primaryTextColor(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(modifier = GlanceModifier.height(2.dp))
                Text(
                    text = "H ${weather.highTemp}° • L ${weather.lowTemp}°",
                    style = TextStyle(
                        color = mutedTextColor(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Image(
                provider = ImageProvider(widgetWeatherIcon(weather.condition)),
                contentDescription = null,
                modifier = GlanceModifier.size(52.dp)
            )
        }

        Spacer(modifier = GlanceModifier.defaultWeight())

        Text(
            text = "${weather.temperature}°",
            style = TextStyle(
                color = thermalColor(weather.temperature),
                fontSize = 46.sp,
                fontWeight = FontWeight.Bold,
            )
        )
        Text(
            text = conditionName,
            style = TextStyle(
                color = semanticConditionColor(conditionName),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        )

        Spacer(modifier = GlanceModifier.height(6.dp))

        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "💧 ${weather.humidity ?: "--"}%",
                style = TextStyle(
                    color = mutedTextColor(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(modifier = GlanceModifier.width(16.dp))
            Text(
                text = "💨 ${weather.windSpeed ?: "--"} km/h",
                style = TextStyle(
                    color = mutedTextColor(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}