package com.jenil.weather.domain.model

data class RadarFrame(
    val time: Long,
    val path: String
)

data class RadarMetaData(
    val host: String,
    val pastFrames: List<RadarFrame>
)