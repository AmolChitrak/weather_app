package com.jenil.weather.widget

import android.content.Context
import androidx.compose.runtime.Composable
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
import com.jenil.weather.MainActivity
import com.jenil.weather.data.remote.dto.toDisplayName
import com.jenil.weather.di.AppModule
import com.jenil.weather.domain.model.HourlyForecast
import com.jenil.weather.domain.model.WeatherData
import com.jenil.weather.ui.core.widgetWeatherIcon
import com.jenil.weather.ui.theme.DarkColors
import com.jenil.weather.ui.theme.LightColors
import com.jenil.weather.utils.cardSurfaceColor
import com.jenil.weather.utils.mutedTextColor
import com.jenil.weather.utils.primaryTextColor
import com.jenil.weather.utils.thermalColor
import dagger.hilt.android.EntryPointAccessors

private val WIDGET_SMALL = DpSize(80.dp, 80.dp)
private val WIDGET_MEDIUM = DpSize(160.dp, 120.dp)
private val WIDGET_LARGE = DpSize(240.dp, 160.dp)

class ForecastAppWidget : GlanceAppWidget() {

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
                ForecastWidgetContent(cachedWeather)
            }
        }
    }
}

@Composable
fun ForecastWidgetContent(weather: WeatherData?) {
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
                size.width < WIDGET_MEDIUM.width -> SmallForecast(weather)
                size.width < WIDGET_LARGE.width -> MediumForecast(weather)
                else -> LargeForecast(weather)
            }
        } else {
            Text(
                text = "Open app to load",
                style = TextStyle(
                    color = mutedTextColor(),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}


@Composable
private fun SmallForecast(weather: WeatherData) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "${weather.temperature}°",
            style = TextStyle(
                color = thermalColor(weather.temperature),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = weather.condition.toDisplayName(),
            style = TextStyle(
                color = primaryTextColor(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        )
        Spacer(modifier = GlanceModifier.defaultWeight())
        Text(
            text = "↓ ${weather.lowTemp}°",
            style = TextStyle(color = mutedTextColor(), fontSize = 11.sp)
        )
        Text(
            text = "↑ ${weather.highTemp}°",
            style = TextStyle(color = mutedTextColor(), fontSize = 11.sp)
        )
    }
}

@Composable
private fun MediumForecast(weather: WeatherData) {
    val hourlyList = weather.hourlyForecast?.take(4) ?: emptyList()

    Column(modifier = GlanceModifier.fillMaxSize()) {
        Text(
            text = weather.cityName,
            style = TextStyle(
                color = primaryTextColor(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = GlanceModifier.height(4.dp))

        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${weather.temperature}°",
                style = TextStyle(
                    color = thermalColor(weather.temperature),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            Image(
                provider = ImageProvider(widgetWeatherIcon(weather.condition)),
                contentDescription = null,
                modifier = GlanceModifier.size(20.dp)
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            Text(
                text = "Now",
                style = TextStyle(color = mutedTextColor(), fontSize = 12.sp)
            )
        }

        Spacer(modifier = GlanceModifier.height(6.dp))

        Column(modifier = GlanceModifier.fillMaxWidth()) {
            hourlyList.forEach { item ->
                HourlyListRow(item)
            }
        }
    }
}

@Composable
private fun HourlyListRow(item: HourlyForecast) {
    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.time, // e.g. "3 PM"
            style = TextStyle(color = mutedTextColor(), fontSize = 11.sp),
            modifier = GlanceModifier.defaultWeight()
        )
        Image(
            provider = ImageProvider(widgetWeatherIcon(item.condition)),
            contentDescription = null,
            modifier = GlanceModifier.size(16.dp)
        )
        Spacer(modifier = GlanceModifier.width(6.dp))
        Text(
            text = "${item.temperature}°",
            style = TextStyle(color = primaryTextColor(), fontSize = 11.sp, fontWeight = FontWeight.Medium)
        )
    }
}


@Composable
private fun LargeForecast(weather: WeatherData) {
    val hourlyList = weather.hourlyForecast?.take(6) ?: emptyList()

    Column(modifier = GlanceModifier.fillMaxSize()) {
        Text(
            text = "Today",
            style = TextStyle(
                color = primaryTextColor(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = GlanceModifier.height(4.dp))

        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${weather.temperature}°",
                style = TextStyle(
                    color = thermalColor(weather.temperature),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            Image(
                provider = ImageProvider(widgetWeatherIcon(weather.condition)),
                contentDescription = null,
                modifier = GlanceModifier.size(22.dp)
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            Text(
                text = "Now",
                style = TextStyle(color = mutedTextColor(), fontSize = 12.sp)
            )
        }

        Spacer(modifier = GlanceModifier.defaultWeight())

        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            hourlyList.forEach { item ->
                HourlyColumnItem(
                    item = item,
                    modifier = GlanceModifier.defaultWeight()
                )
            }
        }

        Spacer(modifier = GlanceModifier.defaultWeight())

        Text(
            text = "Tomorrow",
            style = TextStyle(
                color = mutedTextColor(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun HourlyColumnItem(
    item: HourlyForecast,
    modifier: GlanceModifier = GlanceModifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = item.time,
            style = TextStyle(color = mutedTextColor(), fontSize = 10.sp)
        )
        Spacer(modifier = GlanceModifier.height(2.dp))
        Image(
            provider = ImageProvider(widgetWeatherIcon(item.condition)),
            contentDescription = null,
            modifier = GlanceModifier.size(18.dp)
        )
        Spacer(modifier = GlanceModifier.height(2.dp))
        Text(
            text = "${item.temperature}°",
            style = TextStyle(color = primaryTextColor(), fontSize = 11.sp, fontWeight = FontWeight.Medium)
        )
    }
}