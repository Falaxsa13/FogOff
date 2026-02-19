package com.example.foggoff

import android.app.Application
import com.mapbox.common.MapboxOptions

class FogOffApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Set Mapbox token before any MapView is created (token comes from local.properties via BuildConfig)
        BuildConfig.MAPBOX_ACCESS_TOKEN.takeIf { it.isNotBlank() }?.let { token ->
            MapboxOptions.accessToken = token
        }
    }
}
