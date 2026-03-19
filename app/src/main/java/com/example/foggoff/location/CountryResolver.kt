package com.example.foggoff.location

import android.content.Context
import android.location.Geocoder
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

/**
 * MVP reverse-geocoder: converts (lat,lng) into a country code/name.
 * Uses Android's [Geocoder] and caches results to avoid repeated lookups.
 */
class CountryResolver(context: Context) {

    private val geocoder = Geocoder(context.applicationContext, Locale.getDefault())

    // Cache by coarse lat/lng grid to reduce calls while the user is moving.
    // Example key: "12.3:-45.6"
    private val cache = HashMap<String, String>()

    suspend fun resolveCountry(lat: Double, lng: Double): String? {
        val latKey = (lat * 10).roundToInt() / 10.0
        val lngKey = (lng * 10).roundToInt() / 10.0
        val cacheKey = "${latKey}:${lngKey}"

        cache[cacheKey]?.let { return it }

        return withContext(Dispatchers.IO) {
            try {
                @Suppress("DEPRECATION")
                val results = geocoder.getFromLocation(lat, lng, 1)
                val address = results?.firstOrNull() ?: return@withContext null
                val code = address.countryCode?.takeIf { it.isNotBlank() }?.uppercase(Locale.ROOT)
                val name = address.countryName?.takeIf { it.isNotBlank() }
                val resolved = code ?: name?.let { inferIso2FromCountryName(it) } ?: return@withContext null
                cache[cacheKey] = resolved
                resolved
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun inferIso2FromCountryName(countryNameRaw: String): String? {
        val trimmed = countryNameRaw.trim()
        if (trimmed.isBlank()) return null

        val normalizedTarget = trimmed.replace(" ", "").uppercase(Locale.ROOT)

        for (code in Locale.getISOCountries()) {
            val localeName = Locale("", code).displayCountry ?: continue
            val normalizedCandidate = localeName.replace(" ", "").uppercase(Locale.ROOT)
            if (normalizedCandidate == normalizedTarget) return code
            if (localeName.equals(trimmed, ignoreCase = true)) return code
        }
        return null
    }
}

