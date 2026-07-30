package com.jenil.weather.data.repository

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.jenil.weather.domain.location.Location
import com.jenil.weather.domain.location.LocationTracker
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume

class DefaultLocationTracker @Inject constructor(
    private val locationClient: FusedLocationProviderClient,
    private val application: Application
) : LocationTracker {

    override suspend fun getCurrentLocation(): Location? {
        // Verify system permissions
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            application,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
            application,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        // Verify hardware GPS/Network location provider status
        val locationManager =
            application.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!hasFineLocationPermission || !hasCoarseLocationPermission || !isGpsEnabled) {
            return null
        }

        return suspendCancellableCoroutine { continuation ->
            locationClient.lastLocation.apply {
                if (isComplete) {
                    if (isSuccessful && result != null) {
                        continuation.resume(createLocationModel(result.latitude, result.longitude))
                    } else {
                        continuation.resume(null)
                    }
                    return@suspendCancellableCoroutine
                }
                // Set up standard listeners for the async operation
                addOnSuccessListener { location ->
                    if (location != null) {
                        continuation.resume(
                            createLocationModel(
                                location.latitude,
                                location.longitude
                            )
                        )
                    } else {
                        continuation.resume(null)
                    }
                }
                addOnFailureListener {
                    continuation.resume(null)
                }
                addOnCanceledListener {
                    continuation.cancel()
                }
            }
        }
    }

    private fun createLocationModel(lat: Double, lon: Double): Location {
        var cityName: String? = null
        try {
            val geocoder = Geocoder(application, Locale.getDefault())

            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(lat, lon, 1)

            if (!addresses.isNullOrEmpty()) {

                cityName = addresses[0].locality ?: addresses[0].adminArea
            }
        } catch (e: Exception) {
            e.printStackTrace()

        }

        return Location(
            latitude = lat,
            longitude = lon,
            cityName = cityName
        )
    }
}
