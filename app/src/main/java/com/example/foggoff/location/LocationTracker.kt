package com.example.foggoff.location

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Wraps FusedLocationProviderClient to emit (lat, lng) updates.
 * Permission must be granted before calling [start].
 */
class LocationTracker(context: Context) {

    private val client = LocationServices.getFusedLocationProviderClient(context.applicationContext)

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private var callback: LocationCallback? = null

    @SuppressLint("MissingPermission")
    fun start(onLocation: (lat: Double, lng: Double) -> Unit) {
        if (_isTracking.value) return

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10_000L)
            .setMinUpdateDistanceMeters(5f)
            .setMinUpdateIntervalMillis(5_000L)
            .build()

        callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { onLocation(it.latitude, it.longitude) }
            }
        }

        client.requestLocationUpdates(request, callback!!, Looper.getMainLooper())
        _isTracking.value = true
    }

    fun stop() {
        callback?.let { client.removeLocationUpdates(it) }
        callback = null
        _isTracking.value = false
    }
}
