package com.example.foggoff.h3

import com.uber.h3core.H3Core
import com.uber.h3core.util.LatLng

/** H3 resolution 11 (~0.7 m hex edge); fine-grained fog reveal. */
const val H3_RESOLUTION = 11

/**
 * H3Core loaded from app's jniLibs (arm64-v8a/armeabi-v7a). On Android, newInstance() fails
 * because it tries to unpack from the JAR; we use newSystemInstance() after loading the lib.
 */
private val h3Core: H3Core? by lazy {
    try {
        System.loadLibrary("h3-java")
        H3Core.newSystemInstance()
    } catch (e: Throwable) {
        null
    }
}

/**
 * Converts lat/lng (degrees) to H3 cell index at resolution 9.
 * @return H3 index string, or null if H3 failed to load
 */
fun latLngToH3Index(lat: Double, lng: Double): String? {
    val core = h3Core ?: return null
    return core.latLngToCellAddress(lat, lng, H3_RESOLUTION)
}

/**
 * Returns the boundary of the H3 cell as a closed ring of [lng, lat] pairs for GeoJSON.
 * Interior rings (holes) should be clockwise; H3 returns counter-clockwise, so we reverse.
 */
fun h3IndexToBoundaryRing(h3Index: String): List<Pair<Double, Double>>? {
    val core = h3Core ?: return null
    val boundary: List<LatLng> = core.cellToBoundary(h3Index)
    if (boundary.isEmpty()) return null
    // GeoJSON is [lng, lat]. Interior rings (holes) clockwise; reverse H3 order.
    val ring = boundary.map { (it.lng to it.lat) }.reversed()
    // Close the ring (first point = last point)
    return if (ring.first() == ring.last()) ring else ring + ring.first()
}

/**
 * Builds a GeoJSON Feature string for the fog: one Polygon with exterior = world bbox,
 * interior rings = one closed ring per unlocked H3 hex (holes).
 */
fun buildFogGeoJson(unlockedH3Ids: Set<String>): String {
    val exteriorRing = worldExteriorRing()
    val holeRings = unlockedH3Ids.mapNotNull { h3IndexToBoundaryRing(it) }
    val coordinates = mutableListOf<List<List<Double>>>()
    coordinates.add(exteriorRing)
    for (ring in holeRings) {
        coordinates.add(ring.map { listOf(it.first, it.second) })
    }
    val coordsJson = coordinates.joinToString(",") { ring ->
        "[" + ring.joinToString(",") { "[${it[0]},${it[1]}]" } + "]"
    }
    return """{"type":"Feature","geometry":{"type":"Polygon","coordinates":[$coordsJson]},"properties":{}}"""
}

/** World bbox as closed exterior ring [lng, lat], counterclockwise. */
private fun worldExteriorRing(): List<List<Double>> {
    return listOf(
        listOf(-180.0, -85.0),
        listOf(180.0, -85.0),
        listOf(180.0, 85.0),
        listOf(-180.0, 85.0),
        listOf(-180.0, -85.0)
    )
}
