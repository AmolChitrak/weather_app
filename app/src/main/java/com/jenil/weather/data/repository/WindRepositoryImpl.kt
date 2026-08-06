package com.jenil.weather.data.repository

import com.jenil.weather.data.remote.WeatherApi
import com.jenil.weather.domain.model.MapLayerType
import com.jenil.weather.domain.repository.WindRepository
import com.jenil.weather.utils.WindStreamlines
import com.jenil.weather.utils.WindVector
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Position
import javax.inject.Inject
import javax.inject.Named

class WindRepositoryImpl @Inject constructor(
    @Named("open_weather_api_key") private val apiKey: String,
    private val weatherApi: WeatherApi
) : WindRepository {

    override fun getTileUrl(layerType: MapLayerType): String? {
        return when (layerType) {
            MapLayerType.WIND -> {
                if (apiKey.isNotEmpty()) {
                    "https://tile.openweathermap.org/map/wind_new/{z}/{x}/{y}.png?appid=$apiKey"
                } else null
            }
            MapLayerType.RADAR -> null
        }
    }

    override suspend fun fetchWindGridGeoJson(latitude: Double, longitude: Double, zoom: Double): String {
        // Dynamically scale grid coverage based on zoom level[cite: 2]
        val (span, step) = when {
            zoom < 5.0 -> Pair(10, 1.2)   // Country/Continent view (Wide coverage)
            zoom < 8.0 -> Pair(7, 0.6)    // Regional view
            else -> Pair(5, 0.3)          // Local/City view (Dense detail)
        }

        val lats = mutableListOf<Double>()
        val lons = mutableListOf<Double>()

        for (i in -span..span) {
            for (j in -span..span) {
                lats.add(latitude + (i * step))
                lons.add(longitude + (j * step))
            }
        }

        val response = weatherApi.getWindGridData(
            latitudes = lats.joinToString(","),
            longitudes = lons.joinToString(",")
        )

        val windVectors = response.map { point ->
            WindVector(
                latitude = point.latitude,
                longitude = point.longitude,
                speedKmh = point.current.windSpeed,
                directionFromDeg = point.current.windDirection.toDouble()
            )
        }

        // Updated to use maxStepsTotal for the new bidirectional tracer algorithm
        val streamlines = WindStreamlines.trace(windVectors, seedCount = 60, maxStepsTotal = 36)

        val features = streamlines.map { streamline ->
            val positions = streamline.points.map { (lon, lat) ->
                Position(longitude = lon, latitude = lat)
            }
            val lineString = LineString(coordinates = positions)
            val properties = buildJsonObject {
                put("averageSpeedKmh", streamline.averageSpeedKmh)
            }

            Feature(geometry = lineString, properties = properties)
        }

        val featureCollection = FeatureCollection(features = features)
        return Json.encodeToString(featureCollection)
    }
}