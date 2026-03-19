package com.example.foggoff.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.foggoff.data.UnlockedHexRepository
import com.example.foggoff.location.CountryResolver
import com.example.foggoff.h3.latLngToH3Index
import com.example.foggoff.location.LocationTracker
import com.mapbox.geojson.Point
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Default start used when the pad is pressed before any GPS fix. */
private val DEFAULT_START = Point.fromLngLat(-98.0, 39.5)

class FogMapViewModel(application: Application) : AndroidViewModel(application) {

    private val locationTracker = LocationTracker(application)
    private val hexRepository = UnlockedHexRepository()
    private val countryResolver = CountryResolver(application)

    private var lastUnlockPoint: Point? = null

    private val _unlockedH3Ids = MutableStateFlow(setOf<String>())
    val unlockedH3Ids: StateFlow<Set<String>> = _unlockedH3Ids.asStateFlow()

    val isTracking: StateFlow<Boolean> = locationTracker.isTracking

    /** The player's active position — sourced from GPS or the virtual movement pad. */
    private val _currentPosition = MutableStateFlow<Point?>(null)
    val currentPosition: StateFlow<Point?> = _currentPosition.asStateFlow()

    init {
        // Load persisted hexes from Firestore (merge with any already in memory).
        viewModelScope.launch {
            val loaded = hexRepository.loadUnlockedH3Ids()
            _unlockedH3Ids.update { it + loaded }
        }
        // Unlock the H3 cell at whatever position we're at, whenever it changes; persist to Firestore.
        viewModelScope.launch {
            _currentPosition.filterNotNull().collect { point ->
                val index = latLngToH3Index(point.latitude(), point.longitude()) ?: return@collect
                var added = false
                _unlockedH3Ids.update { current ->
                    if (index in current) current else {
                        added = true
                        current + index
                    }
                }
                if (added) {
                    val lat = point.latitude()
                    val lng = point.longitude()
                    val h3 = index
                    viewModelScope.launch {
                        val country = countryResolver.resolveCountry(lat, lng)
                        val distanceKm = lastUnlockPoint?.let { last ->
                            haversineKm(
                                lat1 = last.latitude(),
                                lon1 = last.longitude(),
                                lat2 = lat,
                                lon2 = lng,
                            )
                        } ?: 0.0
                        hexRepository.addUnlockedH3Ids(
                            ids = setOf(h3),
                            unlockedCountryCodes = if (country != null) setOf(country) else emptySet(),
                            unlockedCountryKmAdds = if (country != null && distanceKm > 0.0) {
                                mapOf(country to distanceKm)
                            } else emptyMap(),
                        )
                    }
                    lastUnlockPoint = point
                }
            }
        }
    }

    private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val sinLat = Math.sin(dLat / 2)
        val sinLon = Math.sin(dLon / 2)
        val a = (sinLat * sinLat) +
            (Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * (sinLon * sinLon))
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }

    /** Start tracking the device's real GPS location. */
    fun startTracking() {
        locationTracker.start { lat, lng -> setPosition(lat, lng) }
    }

    /**
     * Move the simulated player position by [dLat]/[dLng] degrees.
     * Falls back to [DEFAULT_START] if no position has been set yet.
     */
    fun movePosition(dLat: Double, dLng: Double) {
        val base = _currentPosition.value ?: DEFAULT_START
        setPosition(base.latitude() + dLat, base.longitude() + dLng)
    }

    fun stopTracking() = locationTracker.stop()

    private fun setPosition(lat: Double, lng: Double) {
        _currentPosition.value = Point.fromLngLat(lng, lat)
    }

    override fun onCleared() {
        super.onCleared()
        locationTracker.stop()
    }
}
