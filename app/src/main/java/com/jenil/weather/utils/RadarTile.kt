//package com.jenil.weather.utils
//
//import com.google.android.gms.maps.model.LatLngBounds
//import kotlin.math.PI
//import kotlin.math.cos
//import kotlin.math.ln
//import kotlin.math.tan
//import androidx.collection.LruCache
//
//data class TileRect(
//    val minX: Int,
//    val maxX: Int,
//    val minY: Int,
//    val maxY: Int
//)
//
//object TileMath {
//
//    fun latLngToTile(lat: Double, lng: Double, zoom: Int): Pair<Int, Int> {
//        val maxTile = (1 shl zoom) - 1
//        val x = (((lng + 180.0) / 360.0) * (1 shl zoom)).toInt().coerceIn(0, maxTile)
//        val latRad = Math.toRadians(lat)
//        val y = (((1.0 - ln(tan(latRad) + 1 / cos(latRad)) / PI) / 2.0) * (1 shl zoom)).toInt().coerceIn(0, maxTile)
//        return x to y
//    }
//
//    /**
//     * Calculates the minimum bounding box of tiles covering the current camera viewport.
//     */
//    fun getTileBounds(bounds: LatLngBounds, zoom: Int): TileRect {
//        val (x1, y1) = latLngToTile(bounds.northeast.latitude, bounds.southwest.longitude, zoom)
//        val (x2, y2) = latLngToTile(bounds.southwest.latitude, bounds.northeast.longitude, zoom)
//
//        val minX = minOf(x1, x2)
//        val maxX = maxOf(x1, x2)
//        val minY = minOf(y1, y2)
//        val maxY = maxOf(y1, y2)
//
//        return TileRect(minX, maxX, minY, maxY)
//    }
//
//    /**
//     * Expands the bounding box by [extraScreens] in all directions for viewport prediction.
//     */
//    fun expandBounds(rect: TileRect, zoom: Int, extraScreens: Int = 1): TileRect {
//        val width = rect.maxX - rect.minX + 1
//        val height = rect.maxY - rect.minY + 1
//        val maxTile = (1 shl zoom) - 1
//
//        val newMinX = (rect.minX - width * extraScreens).coerceIn(0, maxTile)
//        val newMaxX = (rect.maxX + width * extraScreens).coerceIn(0, maxTile)
//        val newMinY = (rect.minY - height * extraScreens).coerceIn(0, maxTile)
//        val newMaxY = (rect.maxY + height * extraScreens).coerceIn(0, maxTile)
//
//        return TileRect(newMinX, newMaxX, newMinY, newMaxY)
//    }
//}
//
//
//
//object RadarTileCache {
//    // Increased capacity to hold multiple frames across dynamic viewports (~25MB memory budget)
//    private const val MAX_CACHE_SIZE = 2500
//    private val cache = LruCache<String, ByteArray>(MAX_CACHE_SIZE)
//
//    fun get(url: String): ByteArray? = cache.get(url)
//
//    fun put(url: String, bytes: ByteArray) {
//        cache.put(url, bytes)
//    }
//
//    fun contains(url: String): Boolean = cache.get(url) != null
//
//    /**
//     * Checks if every required tile in [rect] for [framePath] is present in memory.
//     */
//    fun isViewportCached(
//        host: String,
//        framePath: String,
//        rect: TileRect,
//        zoom: Int,
//        tileSize: Int = 256,
//        colorScheme: Int = 2
//    ): Boolean {
//        for (x in rect.minX..rect.maxX) {
//            for (y in rect.minY..rect.maxY) {
//                val url = "$host$framePath/$tileSize/$zoom/$x/$y/$colorScheme/1_1.png"
//                if (!contains(url)) return false
//            }
//        }
//        return true
//    }
//}