package com.example.foggoff.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foggoff.h3.buildFogGeoJson
import com.mapbox.bindgen.Value
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.circleLayer
import com.mapbox.maps.extension.style.layers.generated.fillLayer
import com.mapbox.maps.extension.style.layers.properties.generated.ProjectionName
import com.mapbox.maps.extension.style.projection.generated.Projection
import com.mapbox.maps.extension.style.projection.generated.setProjection
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource

private const val FOG_SOURCE_ID = "fog-source"
private const val FOG_LAYER_ID = "fog-layer"
private const val PLAYER_SOURCE_ID = "player-source"
private const val PLAYER_LAYER_ID = "player-layer"
private const val DEFAULT_LNG = -98.0
private const val DEFAULT_LAT = 39.5
private const val STREET_ZOOM = 15.0
private const val OVERVIEW_ZOOM = 4.0

// ── Root screen ──────────────────────────────────────────────────────────────

@OptIn(MapboxExperimental::class)
@Composable
fun FogMapScreen(
    modifier: Modifier = Modifier,
    viewModel: FogMapViewModel = viewModel(),
) {
    val context = LocalContext.current
    val unlockedH3Ids by viewModel.unlockedH3Ids.collectAsStateWithLifecycle()
    val isTracking by viewModel.isTracking.collectAsStateWithLifecycle()
    val currentPosition by viewModel.currentPosition.collectAsStateWithLifecycle()

    var hasPermission by remember { mutableStateOf(context.hasLocationPermission()) }
    var hasFirstFix by remember { mutableStateOf(false) }
    var zoomLevel by remember { mutableStateOf(OVERVIEW_ZOOM) }
    var mapCenter by remember {
        mutableStateOf(Point.fromLngLat(DEFAULT_LNG, DEFAULT_LAT))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    LaunchedEffect(hasPermission) {
        if (hasPermission) viewModel.startTracking()
    }

    // Camera follows the player; zoom to street level on first position fix.
    LaunchedEffect(currentPosition) {
        val pos = currentPosition ?: return@LaunchedEffect
        mapCenter = pos
        if (!hasFirstFix) {
            zoomLevel = STREET_ZOOM
            hasFirstFix = true
        }
    }

    val viewportState = rememberMapViewportState {
        setCameraOptions(cameraOptions {
            center(mapCenter)
            zoom(zoomLevel)
            pitch(0.0)
            bearing(0.0)
        })
    }

    LaunchedEffect(zoomLevel, mapCenter) {
        viewportState.setCameraOptions(cameraOptions {
            center(mapCenter)
            zoom(zoomLevel)
            pitch(0.0)
            bearing(0.0)
        })
    }

    val bottomControlPadding = if (hasPermission) 32.dp else 140.dp

    Box(modifier = modifier.fillMaxSize()) {
        FogMap(
            modifier = Modifier.fillMaxSize(),
            viewportState = viewportState,
            unlockedH3Ids = unlockedH3Ids,
            currentPosition = currentPosition,
        )

        StatsBar(
            hexCount = unlockedH3Ids.size,
            isTracking = isTracking,
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp),
        )

        MovementPad(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = bottomControlPadding),
            onMove = { dLat, dLng -> viewModel.movePosition(dLat, dLng) },
        )

        ZoomControls(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = bottomControlPadding),
            onZoomIn = { zoomLevel = (zoomLevel + 1).coerceIn(0.0, 20.0) },
            onZoomOut = { zoomLevel = (zoomLevel - 1).coerceIn(0.0, 20.0) },
        )

        if (!hasPermission) {
            LocationPermissionBanner(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                onGrantClick = {
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                },
            )
        }
    }
}

// ── Map + fog layer ───────────────────────────────────────────────────────────

@OptIn(MapboxExperimental::class)
@Composable
private fun FogMap(
    modifier: Modifier,
    viewportState: MapViewportState,
    unlockedH3Ids: Set<String>,
    currentPosition: Point?,
) {
    MapboxMap(
        modifier = modifier,
        mapViewportState = viewportState,
    ) {
        // Fog layer — created once, then source data updated in place to keep layer order stable.
        MapEffect(key1 = unlockedH3Ids.toSortedSet().joinToString()) { mapView ->
            val ids = unlockedH3Ids
            mapView.mapboxMap.getStyle { style ->
                style.setProjection(Projection(ProjectionName.MERCATOR))
                style.setStyleImportConfigProperty("basemap", "lightPreset", Value("night"))

                val fogGeoJson = buildFogGeoJson(ids)
                if (style.styleSourceExists(FOG_SOURCE_ID)) {
                    style.setStyleSourceProperty(FOG_SOURCE_ID, "data", Value(fogGeoJson))
                } else {
                    style.addSource(geoJsonSource(id = FOG_SOURCE_ID) { data(fogGeoJson) })
                    style.addLayer(
                        fillLayer(layerId = FOG_LAYER_ID, sourceId = FOG_SOURCE_ID) {
                            fillColor(android.graphics.Color.parseColor("#0a0a0a"))
                            fillOpacity(0.92)
                        }
                    )
                }
            }
        }

        // Player dot — anchored to map coordinates, always above the fog layer.
        MapEffect(key1 = currentPosition?.toString()) { mapView ->
            val pos = currentPosition ?: return@MapEffect
            val geoJson = """{"type":"Feature","geometry":{"type":"Point","coordinates":[${pos.longitude()},${pos.latitude()}]},"properties":{}}"""
            mapView.mapboxMap.getStyle { style ->
                if (style.styleSourceExists(PLAYER_SOURCE_ID)) {
                    style.setStyleSourceProperty(PLAYER_SOURCE_ID, "data", Value(geoJson))
                } else {
                    style.addSource(geoJsonSource(id = PLAYER_SOURCE_ID) { data(geoJson) })
                    style.addLayer(
                        circleLayer(layerId = PLAYER_LAYER_ID, sourceId = PLAYER_SOURCE_ID) {
                            circleRadius(8.0)
                            circleColor(android.graphics.Color.parseColor("#2196F3"))
                            circleStrokeWidth(2.0)
                            circleStrokeColor(android.graphics.Color.parseColor("#FFFFFF"))
                        }
                    )
                }
            }
        }
    }
}

// ── HUD overlays ──────────────────────────────────────────────────────────────

@Composable
private fun StatsBar(
    hexCount: Int,
    isTracking: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HudChip {
            Text(
                text = if (hexCount == 0) "No hexes yet" else "$hexCount hex${if (hexCount == 1) "" else "es"} unlocked",
                style = MaterialTheme.typography.labelMedium,
            )
        }
        HudChip {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isTracking) Color(0xFF4CAF50) else Color(0xFF9E9E9E))
                )
                Text(
                    text = if (isTracking) "Tracking" else "Location off",
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
private fun HudChip(content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
        shadowElevation = 2.dp,
    ) {
        Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
            content()
        }
    }
}

@Composable
private fun ZoomControls(
    modifier: Modifier = Modifier,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ZoomButton(label = "+", onClick = onZoomIn)
        ZoomButton(label = "−", onClick = onZoomOut)
    }
}

@Composable
private fun ZoomButton(label: String, onClick: () -> Unit) {
    SmallFloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Text(text = label, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun LocationPermissionBanner(
    modifier: Modifier = Modifier,
    onGrantClick: () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Unlock the world as you explore",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Fog Off reveals the map around you as you walk. Grant location access to start uncovering the fog.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = onGrantClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
            ) {
                Text("Enable Location")
            }
        }
    }
}

// ── Extensions ────────────────────────────────────────────────────────────────

private fun Context.hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
