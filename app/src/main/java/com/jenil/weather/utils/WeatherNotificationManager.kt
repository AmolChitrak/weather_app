package com.jenil.weather.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import com.jenil.weather.MainActivity
import com.jenil.weather.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val MORNING_BRIEF_ID = 101
        const val EVENING_BRIEF_ID = 102
        const val RAIN_ALERT_ID = 103
        const val AQI_ALERT_ID = 104
        const val RECOMMENDATION_ID = 105
        const val WIND_ALERT_ID = 106
        const val TEMPERATURE_ALERT_ID = 107

        const val CHANNEL_DAILY = "daily_forecasts_v1"
        const val CHANNEL_RAIN = "rain_alerts_v1"
        const val CHANNEL_WEATHER_ALERTS = "weather_alerts_v1"
        const val CHANNEL_AQI = "aqi_alerts_v1"
    }

    private val appIconBitmap = BitmapFactory.decodeResource(context.resources, R.mipmap.ic_weather_icon_round)

    private fun contentIntent(requestCode: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun showMorningBrief(title: String, message: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_DAILY)
            .setSmallIcon(R.drawable.ic_weather_sun)
            .setLargeIcon(appIconBitmap)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentIntent(MORNING_BRIEF_ID))
            .build()
        notificationManager.notify(MORNING_BRIEF_ID, notification)
    }

    fun showEveningBrief(title: String, message: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_DAILY)
            .setSmallIcon(R.drawable.ic_weather_night)
            .setLargeIcon(appIconBitmap)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentIntent(EVENING_BRIEF_ID))
            .build()
        notificationManager.notify(EVENING_BRIEF_ID, notification)
    }

    fun showRainAlert(message: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_RAIN)
            .setSmallIcon(R.drawable.ic_rain)
            .setLargeIcon(appIconBitmap)
            .setContentTitle("Rain Approaching")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(contentIntent(RAIN_ALERT_ID))
            .build()
        notificationManager.notify(RAIN_ALERT_ID, notification)
    }

    fun showAqiAlert(message: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_AQI)
            .setSmallIcon(R.drawable.ic_aqi)
            .setLargeIcon(appIconBitmap)
            .setContentTitle("Air Quality Warning")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ERROR)
            .setAutoCancel(true)
            .setContentIntent(contentIntent(AQI_ALERT_ID))
            .build()
        notificationManager.notify(AQI_ALERT_ID, notification)
    }

    fun showWindAlert(message: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_WEATHER_ALERTS)
            .setSmallIcon(R.drawable.ic_air)
            .setLargeIcon(appIconBitmap)
            .setContentTitle("High Wind Advisory")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ERROR)
            .setAutoCancel(true)
            .setContentIntent(contentIntent(WIND_ALERT_ID))
            .build()
        notificationManager.notify(WIND_ALERT_ID, notification)
    }

    fun showTemperatureAlert(title: String, message: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_WEATHER_ALERTS)
            .setSmallIcon(R.drawable.ic_temp)
            .setLargeIcon(appIconBitmap)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ERROR)
            .setAutoCancel(true)
            .setContentIntent(contentIntent(TEMPERATURE_ALERT_ID))
            .build()
        notificationManager.notify(TEMPERATURE_ALERT_ID, notification)
    }

    fun showSmartRecommendation(message: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_DAILY)
            .setSmallIcon(R.drawable.ic_tips)
            .setLargeIcon(appIconBitmap)
            .setContentTitle("Weather Tip")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentIntent(RECOMMENDATION_ID))
            .build()
        notificationManager.notify(RECOMMENDATION_ID, notification)
    }
}