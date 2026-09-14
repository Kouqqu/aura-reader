package com.aura.reader.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

fun triggerThemeHaptic(context: Context) {
    try {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && vibrator?.hasVibrator() == true) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
        }
    } catch (e: Exception) {
        // Fallback or ignore
    }
}

private data class PixelParticle(
    val angle: Double,
    val distance: Float,
    val radius: Float,
    val color: Color
)

@Composable
fun PixelButtonBurst(
    triggerKey: Long,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    if (triggerKey == 0L || colors.isEmpty()) return

    val progress = remember(triggerKey) { Animatable(0f) }
    val particles = remember(triggerKey) {
        val rand = Random(triggerKey)
        List(56) {
            val angle = rand.nextDouble(0.0, Math.PI * 2)
            val distance = rand.nextFloat() * 160f + 40f
            val radius = rand.nextFloat() * 3.5f + 1.8f
            val color = colors[rand.nextInt(colors.size)]
            PixelParticle(
                angle = angle,
                distance = distance,
                radius = radius,
                color = color
            )
        }
    }

    LaunchedEffect(triggerKey) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )
    }

    val currentProgress = progress.value
    if (currentProgress < 1f) {
        Canvas(
            modifier = modifier.graphicsLayer(clip = false)
        ) {
            val centerOffset = Offset(size.width / 2f, size.height / 2f)
            val ease = currentProgress
            val alpha = (1f - (ease - 0.25f) / 0.75f).coerceIn(0f, 1f)

            // Shockwave ring
            val ringRadius = ease * (size.width.coerceAtLeast(size.height) * 0.9f)
            val ringAlpha = (1f - ease).coerceIn(0f, 0.7f)
            if (ringRadius > 0f && ringAlpha > 0f) {
                drawCircle(
                    color = colors.first().copy(alpha = ringAlpha),
                    radius = ringRadius,
                    center = centerOffset,
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // Flying particles
            for (p in particles) {
                val dist = p.distance * ease
                val x = centerOffset.x + (dist * cos(p.angle)).toFloat()
                val y = centerOffset.y + (dist * sin(p.angle)).toFloat()
                val particleRadius = (p.radius * (1f - ease * 0.45f)).coerceAtLeast(0.5f)

                drawCircle(
                    color = p.color.copy(alpha = alpha),
                    radius = particleRadius,
                    center = Offset(x, y)
                )
            }
        }
    }
}
