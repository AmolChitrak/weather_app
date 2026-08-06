package com.jenil.weather.data.remote.dto

import com.jenil.weather.data.local.entity.FavoriteLocationEntity
import com.jenil.weather.domain.model.DailyForecast
import com.jenil.weather.domain.model.HourlyForecast
import com.jenil.weather.domain.model.LocationSearchResult
import com.jenil.weather.domain.model.MoonPhase
import com.jenil.weather.domain.model.WeatherCondition
import com.jenil.weather.domain.model.WeatherData
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun FavoriteLocationEntity.toLocationSearchResult(): LocationSearchResult {
    return LocationSearchResult(
        id = id,
        name = name,
        latitude = latitude,
        longitude = longitude,
        country = country,
        state = state
    )
}

fun LocationSearchResult.toFavoriteEntity(): FavoriteLocationEntity {
    return FavoriteLocationEntity(
        id = id,
        name = name,
        latitude = latitude,
        longitude = longitude,
        country = country,
        state = state
    )
}

fun Int.toWeatherCondition(): WeatherCondition {
    return when (this) {
        0,1 -> WeatherCondition.CLEAR
        2 -> WeatherCondition.PARTLY_CLOUDY
        3 ,45, 48-> WeatherCondition.OVERCAST
        51, 53, 55, 56, 57, -> WeatherCondition.DRIZZLE
        61, 63, 65, -> WeatherCondition.RAINY
        66, 67, 80, 81, 82 -> WeatherCondition.HEAVY_RAIN
        71, 73, 75, 77, 85, 86 -> WeatherCondition.SNOW
        95 -> WeatherCondition.THUNDERSTORM
        96 -> WeatherCondition.THUNDERSTORM_RAIN
        99 -> WeatherCondition.THUNDERSTORM_RAIN_HEAVY
        else -> WeatherCondition.CLEAR
    }
}

fun WeatherCondition.toDisplayName(): String {
    return when (this) {
        WeatherCondition.CLEAR -> "Clear"
        WeatherCondition.PARTLY_CLOUDY -> "Partly Cloudy"
        WeatherCondition.CLOUDY -> "Cloudy"
        WeatherCondition.OVERCAST -> "Overcast"
        WeatherCondition.DRIZZLE -> "Drizzle"
        WeatherCondition.RAINY -> "Rainy"
        WeatherCondition.HEAVY_RAIN -> "Heavy Rain"
        WeatherCondition.THUNDERSTORM -> "Thunderstorm"
        WeatherCondition.THUNDERSTORM_RAIN -> "Thunderstorm with Rain"
        WeatherCondition.THUNDERSTORM_RAIN_HEAVY -> "Severe Thunderstorm"
        WeatherCondition.SNOW -> "Snow"
    }
}

fun LocationDto.toLocationSearchResult(): LocationSearchResult {
    return LocationSearchResult(
        id = id,
        name = name,
        latitude = latitude,
        longitude = longitude,
        country = country ?: "",
        state = admin1 ?: ""
    )
}

fun WeatherDto.toWeatherData(cityName: String, airQualityDto: AirQualityDto?): WeatherData {
    val currentHour = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0)

    data class RawHour(val parsedTime: LocalDateTime, val index: Int)

    val futureHours = hourly.time.indices
        .map { index -> RawHour(LocalDateTime.parse(hourly.time[index], DateTimeFormatter.ISO_DATE_TIME), index) }
        .filter { !it.parsedTime.isBefore(currentHour) }
        .take(24)

    val sunriseTimes = daily.sunrise?.mapNotNull {
        runCatching { LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME) }.getOrNull()
    } ?: emptyList()
    val sunsetTimes = daily.sunset?.mapNotNull {
        runCatching { LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME) }.getOrNull()
    } ?: emptyList()


    fun sunEventBetween(hourStart: LocalDateTime, nextHourStart: LocalDateTime?): Pair<String, Boolean>? {
        if (nextHourStart == null) return null
        sunsetTimes.firstOrNull { it >= hourStart && it < nextHourStart }?.let {
            return it.format(DateTimeFormatter.ofPattern("h:mm a")) to true
        }
        sunriseTimes.firstOrNull { it >= hourStart && it < nextHourStart }?.let {
            return it.format(DateTimeFormatter.ofPattern("h:mm a")) to false
        }
        return null
    }

    val hourlyForecasts = futureHours.mapIndexed { i, rawHour ->
        val index = rawHour.index
        val parsedTime = rawHour.parsedTime
        val nextParsedTime = futureHours.getOrNull(i + 1)?.parsedTime
        val formattedTime = parsedTime.format(DateTimeFormatter.ofPattern("h a"))
        val sunEvent = sunEventBetween(parsedTime, nextParsedTime)

        HourlyForecast(
            time = if (parsedTime == currentHour) "Now" else formattedTime,
            temperature = hourly.temperatures[index].toInt(),
            dewPoint = hourly.dewPoint2m?.getOrNull(index)?.toInt() ?: 0,
            condition = hourly.weatherCodes[index].toWeatherCondition(),
            precipitationProbability = hourly.precipitationProbabilities.getOrElse(index) { 0 },
            uvIndex = hourly.uvIndices.getOrElse(index) { 0.0 },
            isDay = (hourly.isDay?.getOrNull(index) ?: 1) == 1,
            sunEventTime = sunEvent?.first,
            isSunsetEvent = sunEvent?.second ?: false
        )
    }

    val rawSunrise = daily.sunrise?.firstOrNull() ?: ""
    val rawSunset = daily.sunset?.firstOrNull() ?: ""

    val sunriseParsed = if (rawSunrise.isNotEmpty())
        LocalDateTime.parse(rawSunrise, DateTimeFormatter.ISO_DATE_TIME) else null
    val sunsetParsed = if (rawSunset.isNotEmpty())
        LocalDateTime.parse(rawSunset, DateTimeFormatter.ISO_DATE_TIME) else null

    val sunriseFormatted = sunriseParsed?.format(DateTimeFormatter.ofPattern("h:mm a")) ?: "N/A"
    val sunsetFormatted = sunsetParsed?.format(DateTimeFormatter.ofPattern("h:mm a")) ?: "N/A"

    val dailyForecasts = daily.time.indices.map { index ->
        val parsedDate = LocalDateTime.parse(daily.time[index] + "T00:00", DateTimeFormatter.ISO_DATE_TIME)
        val dayName = if (index == 0) "Today" else parsedDate.dayOfWeek.name.lowercase()
            .replaceFirstChar { it.uppercase() }.take(3)

        DailyForecast(
            day = dayName,
            highTemp = daily.maxTemperatures[index].toInt(),
            lowTemp = daily.minTemperatures[index].toInt(),
            condition = daily.weatherCodes[index].toWeatherCondition(),
            uvIndexMax = daily.maxUvIndices.getOrElse(index) { 0.0 },
            precipitationSum = daily.precipitationSums.getOrElse(index) { 0.0 },
            moonPhase = MoonPhase.fromValue(daily.moonPhase?.getOrNull(index) ?: 0.0)
        )
    }

    return WeatherData(
        cityName = cityName,
        date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEE, d MMM")),
        temperature = current.temperature.toInt(),
        apparentTemperature = current.apparentTemperature.toInt(),
        condition = current.weatherCode.toWeatherCondition(),
        highTemp = daily.maxTemperatures[0].toInt(),
        lowTemp = daily.minTemperatures[0].toInt(),
        humidity = current.humidity,
        dewPoint = current.dewPoint2m?.toInt() ?: 0,
        windSpeed = current.windSpeed,
        windDirection = current.windDirection,
        pressure = current.surfacePressure,
        visibility = current.visibility / 1000.0,
        aqi = airQualityDto?.current?.usAqi,
        hourlyForecast = hourlyForecasts,
        sunrise = sunriseFormatted,
        sunset = sunsetFormatted,
        sunriseSecondsOfDay = sunriseParsed?.toLocalTime()?.toSecondOfDay() ?: 0,
        sunsetSecondsOfDay = sunsetParsed?.toLocalTime()?.toSecondOfDay() ?: 0,
        moonPhase = MoonPhase.fromValue(daily.moonPhase?.firstOrNull() ?: 0.0),
        dailyForecast = dailyForecasts
    )
}