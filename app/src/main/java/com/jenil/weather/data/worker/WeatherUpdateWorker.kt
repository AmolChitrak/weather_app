package com.jenil.weather.data.worker

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jenil.weather.data.local.PreferenceKeys
import com.jenil.weather.domain.location.LocationTracker
import com.jenil.weather.domain.usecase.GetWidgetWeatherUseCase
import com.jenil.weather.widget.WeatherAppWidget
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class WeatherUpdateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val getWeatherUseCase: GetWidgetWeatherUseCase,
    private val locationTracker: LocationTracker,
    private val dataStore: DataStore<Preferences>
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val liveLocation = locationTracker.getCurrentLocation()

            val lat: Double
            val lon: Double
            val cityName: String

            if (liveLocation != null) {
                lat = liveLocation.latitude
                lon = liveLocation.longitude
                cityName = liveLocation.cityName ?: "Current Location"
            } else {
                val prefs = dataStore.data.first()
                lat = prefs[PreferenceKeys.OFFLINE_LAT] ?: 23.0225
                lon = prefs[PreferenceKeys.OFFLINE_LON] ?: 72.5714
                cityName = prefs[PreferenceKeys.OFFLINE_NAME] ?: "Ahmedabad"
            }

            val result = getWeatherUseCase(lat, lon, cityName)
            if (result.isSuccess) {
                WeatherAppWidget().updateAll(applicationContext)
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}