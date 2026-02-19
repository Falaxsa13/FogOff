package com.example.foggoff.map

import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapbox.bindgen.Value
import com.mapbox.geojson.Point
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.fillLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource

/**
 * Full-world polygon used as the "fog" overlay (GeoJSON Feature).
 * Covers the map so we can punch holes for unlocked H3 hexes later.
 */
private const val WORLD_FOG_GEOJSON = """
{
  "type": "Feature",
  "geometry": {
    "type": "Polygon",
    "coordinates": [[[-180, -85], [180, -85], [180, 85], [-180, 85], [-180, -85]]]
  },
  "properties": {}
}
"""

private const val FOG_SOURCE_ID = "fog-source"
private const val FOG_LAYER_ID = "fog-layer"

/**
 * Main map screen: dark base map with a full-coverage fog layer on top.
 * Mapbox token is set from local.properties (see local.properties.example).
 */
@OptIn(MapboxExperimental::class)
@Composable
fun FogMapScreen(
    modifier: Modifier = Modifier,
) {
    val viewportState = rememberMapViewportState {
        setCameraOptions(
            cameraOptions {
                center(Point.fromLngLat(-98.0, 39.5))
                zoom(2.0)
                pitch(0.0)
                bearing(0.0)
            }
        )
    }

    MapboxMap(
        modifier = modifier.fillMaxSize(),
        mapViewportState = viewportState,
    ) {
        MapEffect(key1 = Unit) { mapView ->
            mapView.mapboxMap.getStyle { style ->
                // Dark base map (Mapbox Standard night preset)
                style.setStyleImportConfigProperty(
                    "basemap",
                    "lightPreset",
                    Value("night")
                )
                // Fog: one polygon covering the world, drawn on top of the map
                if (!style.styleSourceExists(FOG_SOURCE_ID)) {
                    style.addSource(
                        geoJsonSource(id = FOG_SOURCE_ID) {
                            data(WORLD_FOG_GEOJSON)
                        }
                    )
                }
                if (!style.styleLayerExists(FOG_LAYER_ID)) {
                    style.addLayer(
                        fillLayer(layerId = FOG_LAYER_ID, sourceId = FOG_SOURCE_ID) {
                            fillColor(Color.parseColor("#0a0a0a"))
                            fillOpacity(0.92)
                        }
                    )
                }
            }
        }
    }
}
