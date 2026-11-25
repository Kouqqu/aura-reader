package com.aura.reader.ui.components

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Material 3 Expressive wavy progress indicator backed by the official Compose
 * Material 3 `LinearWavyProgressIndicator` component.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WavyProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    strokeWidth: Dp = 4.dp,
    amplitude: Dp = 2.4.dp,
    waveLength: Dp = 32.dp
) {
    val density = LocalDensity.current
    val stroke = remember(density, strokeWidth) {
        with(density) {
            Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        }
    }
    val clamped = progress.coerceIn(0f, 1f)
    val ampFactor = if (amplitude > 0.dp && clamped > 0f && clamped < 1f) 1f else 0f

    LinearWavyProgressIndicator(
        progress = { clamped },
        modifier = modifier,
        color = color,
        trackColor = trackColor,
        stroke = stroke,
        trackStroke = stroke,
        amplitude = { ampFactor },
        wavelength = waveLength
    )
}
