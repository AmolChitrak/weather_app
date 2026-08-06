package com.jenil.weather.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RainViewerResponseDto(
    @SerialName("version") val version: String? = null,
    @SerialName("generated") val generated: Long? = null,
    @SerialName("host") val host: String? = null,
    @SerialName("radar") val radar: RadarDto? = null
)

@Serializable
data class RadarDto(
    @SerialName("past") val past: List<RadarFrameDto>? = null
)

@Serializable
data class RadarFrameDto(
    @SerialName("time") val time: Long,
    @SerialName("path") val path: String
)