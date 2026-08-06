package com.jenil.weather.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
private fun SampleStatTile(label: String, value: String) {
    Surface(
        modifier = Modifier
            .size(150.dp, 100.dp),
        shape = WeatherExtraShapes.statTile,
        color = WeatherTheme.colors.surfaceElevated
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.Center) {
            Text(text = label.uppercase(), style = WeatherTypography.labelSmall, color = WeatherTheme.colors.onSurfaceMuted)
            Text(text = value, style = WeatherTypography.titleMedium)
        }
    }
}

@Composable
private fun SamplePreviewContent() {
    Surface(color = androidx.compose.material3.MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Weather Insight", style = WeatherTypography.headlineLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SampleStatTile("Humidity", "74%")
                SampleStatTile("Wind", "7.5km/h")
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = WeatherExtraShapes.badge,
                color = WeatherTheme.colors.badgeRed.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "3 · Low Health Risk",
                    modifier = Modifier.padding(12.dp),
                    color = WeatherTheme.colors.badgeRed
                )
            }
        }
    }
}

@Preview(name = "Dark", showBackground = true, backgroundColor = 0xFF101215)
@Composable
private fun PreviewDark() {
    WeatherAppTheme(darkTheme = true) { SamplePreviewContent() }
}

@Preview(name = "Light", showBackground = true, backgroundColor = 0xFFF3F4F6)
@Composable
private fun PreviewLight() {
    WeatherAppTheme(darkTheme = false) { SamplePreviewContent() }
}