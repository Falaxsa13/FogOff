package com.example.foggoff.map

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Degrees moved per step. ~5 m at mid-latitudes. */
private const val STEP = 0.00005

/**
 * A directional pad that calls [onMove] on each tap/hold tick.
 * Holding a button fires the first move immediately, then repeats after
 * a short initial delay with a fast repeat interval.
 */
@Composable
fun MovementPad(
    modifier: Modifier = Modifier,
    onMove: (dLat: Double, dLng: Double) -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        PadButton(label = "↑") { onMove(STEP, 0.0) }
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PadButton(label = "←") { onMove(0.0, -STEP) }
            Box(modifier = Modifier.size(36.dp)) // center gap
            PadButton(label = "→") { onMove(0.0, STEP) }
        }
        PadButton(label = "↓") { onMove(-STEP, 0.0) }
    }
}

// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PadButton(
    modifier: Modifier = Modifier,
    label: String,
    onMove: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    Surface(
        modifier = modifier
            .size(36.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        val job = scope.launch {
                            onMove()           // fire immediately on press
                            delay(300L)        // initial hold pause
                            while (true) {
                                onMove()
                                delay(80L)     // rapid-fire interval
                            }
                        }
                        tryAwaitRelease()
                        job.cancel()
                    }
                )
            },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
        shadowElevation = 2.dp,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
