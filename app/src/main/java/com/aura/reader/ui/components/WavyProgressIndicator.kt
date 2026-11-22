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
 * Material 3 Expressive wavy progress indicator.
 *
 * Both the active segment and the track undulate with the same phase, a small
 * flat gap separates them at the progress head, and a stop indicator closes the
 * line. That is the shape the current Material spec uses; the previous version
 * drew a sine wave only for the active part and left a flat track, which read as
 * a custom bar rather than a Material indicator.
 */
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
    val infiniteTransition = rememberInfiniteTransition(label = "wavyProgress")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    val density = LocalDensity.current
    val strokeWidthPx = with(density) { strokeWidth.toPx() }
    val amplitudePx = with(density) { amplitude.toPx() }
    val waveLengthPx = with(density) { waveLength.toPx() }
    val gapPx = with(density) { 4.dp.toPx() }
    val stopRadiusPx = with(density) { 2.dp.toPx() }
    val dampPx = with(density) { 10.dp.toPx() }

    Canvas(modifier = modifier.height(16.dp)) {
        val width = size.width
        val centerY = size.height / 2f
        val clampedProgress = progress.coerceIn(0f, 1f)
        val lineEnd = (width - stopRadiusPx * 2f - gapPx).coerceAtLeast(0f)
        val activeEnd = lineEnd * clampedProgress
        val trackStart = (activeEnd + gapPx).coerceAtMost(lineEnd)

        fun wavePath(start: Float, end: Float): Path? {
            if (end - start < 1f) return null
            val path = Path()
            var first = true
            var x = start
            val stepPx = 3f
            while (x <= end) {
                val startDamp = ((x - start) / dampPx).coerceIn(0f, 1f)
                val endDamp = ((end - x) / dampPx).coerceIn(0f, 1f)
                val edge = startDamp * endDamp
                val y = centerY +
                    (amplitudePx * edge * sin(2.0 * PI * ((x / waveLengthPx) - phase))).toFloat()
                if (first) {
                    path.moveTo(x, y)
                    first = false
                } else {
                    path.lineTo(x, y)
                }
                x += stepPx
            }
            // The damping envelope always flattens the segment's ends onto the centre line.
            path.lineTo(end, centerY)
            return path
        }

        wavePath(trackStart, lineEnd)?.let { path ->
            drawPath(
                path = path,
                color = trackColor.copy(alpha = 0.35f),
                style = Stroke(
                    width = strokeWidthPx,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }

        wavePath(0f, activeEnd)?.let { path ->
            drawPath(
                path = path,
                color = color,
                style = Stroke(
                    width = strokeWidthPx,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }

        // Stop indicator at the far end, exactly like the flat linear indicator.
        drawCircle(
            color = trackColor.copy(alpha = 0.9f),
            radius = stopRadiusPx,
            center = Offset(width - stopRadiusPx, centerY)
        )
    }
}
