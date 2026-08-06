package com.jenil.weather.utils

import androidx.collection.LruCache
import com.google.android.gms.maps.model.Tile
import com.google.android.gms.maps.model.TileProvider
import java.net.URL

object RadarTileCache {
    private val cache = LruCache<String, ByteArray>(maxSize = 200) // ~200 tiles in memory
    fun get(url: String): ByteArray? = cache.get(url)
    fun put(url: String, bytes: ByteArray) = cache.put(url, bytes)
}

class RainViewerTileProvider(
    private val host: String,
    private val framePath: String,
    private val tileSize: Int = 256,
    private val colorScheme: Int = 2,
) : TileProvider {
    override fun getTile(x: Int, y: Int, zoom: Int): Tile {
        val url = "$host$framePath/$tileSize/$zoom/$x/$y/$colorScheme/1_1.png"
        RadarTileCache.get(url)?.let { return Tile(tileSize, tileSize, it) }
        return try {
            val bytes = URL(url).openStream().use { it.readBytes() }
            RadarTileCache.put(url, bytes)
            Tile(tileSize, tileSize, bytes)
        } catch (e: Exception) {
            e.printStackTrace()
            TileProvider.NO_TILE
        }
    }
}