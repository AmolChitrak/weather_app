package com.jenil.weather.data.repository



import com.jenil.weather.data.remote.RadarMapApi
import com.jenil.weather.domain.model.RadarFrame
import com.jenil.weather.domain.model.RadarMetaData
import com.jenil.weather.domain.repository.RadarRepository
import javax.inject.Inject



class RadarRepositoryImpl @Inject constructor(
    private val api: RadarMapApi
) : RadarRepository {

    override suspend fun getRadarMetaData(): Result<RadarMetaData> {
        return try {
            val response = api.getRadarMaps()
            val host = response.host ?: "https://tilecache.rainviewer.com"
            val frames = response.radar?.past?.map {
                RadarFrame(time = it.time, path = it.path)
            } ?: emptyList()

            Result.success(RadarMetaData(host = host, pastFrames = frames))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}