package com.aura.reader.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

/**
 * Material 3 Expressive animated wavy progress bar (similar to Android 15/16 media scrubber).
 * Features a smoothly undulating sine wave that flows as progress increases.
 */
@Composable
fun WavyProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    strokeWidth: Dp = 3.5.dp,
    amplitude: Dp = 2.8.dp,
    waveLength: Dp = 22.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wavyProgress")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    val density = LocalDensity.current
    val strokeWidthPx = with(density) { strokeWidth.toPx() }
    val amplitudePx = with(density) { amplitude.toPx() }
    val waveLengthPx = with(density) { waveLength.toPx() }

    Canvas(modifier = modifier.height(14.dp)) {
        val width = size.width
        val centerY = size.height / 2f
        val clampedProgress = progress.coerceIn(0f, 1f)
        val activeWidth = width * clampedProgress

        // 1. Draw inactive track (flat background line)
        if (activeWidth < width) {
            drawLine(
                color = trackColor.copy(alpha = 0.35f),
                start = Offset(activeWidth, centerY),
                end = Offset(width, centerY),
                strokeWidth = strokeWidthPx,
                cap = StrokeCap.Round
            )
        }

        // 2. Draw active wavy progress path
        if (activeWidth > 2f) {
            val wavePath = Path()
            var first = true
            val stepPx = 3f

            var x = 0f
            while (x <= activeWidth) {
                // Smooth damping at both ends for a clean seamless visual join
                val dampStart = (x / 10f).coerceIn(0f, 1f)
                val dampEnd = ((activeWidth - x) / 10f).coerceIn(0f, 1f)
                val edgeDamping = dampStart * dampEnd

                val waveAngle = 2.0 * PI * ((x / waveLengthPx) - phase)
                val y = centerY + (amplitudePx * edgeDamping * sin(waveAngle)).toFloat()

                if (first) {
                    wavePath.moveTo(x, y)
                    first = false
                } else {
                    wavePath.lineTo(x, y)
                }
                x += stepPx
            }

            drawPath(
                path = wavePath,
                color = color,
                style = Stroke(
                    width = strokeWidthPx,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}
