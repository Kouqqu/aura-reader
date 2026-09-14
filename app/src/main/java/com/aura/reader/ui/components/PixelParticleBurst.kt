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
import kotlin.math.pow
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
fun PixelFullScreenBurst(
    triggerKey: Long,
    origin: Offset,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    if (triggerKey == 0L || colors.isEmpty()) return

    val progress = remember(triggerKey) { Animatable(0f) }
    val particles = remember(triggerKey) {
        val rand = Random(triggerKey)
        List(90) {
            val angle = rand.nextDouble(0.0, Math.PI * 2)
            // Long trajectory flying across the entire screen
            val distance = rand.nextFloat() * 1300f + 250f
            val radius = rand.nextFloat() * 4.2f + 1.8f
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
            animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing)
        )
    }

    val currentProgress = progress.value
    if (currentProgress < 1f) {
        Canvas(
            modifier = modifier.graphicsLayer(clip = false)
        ) {
            val ease = 1f - (1f - currentProgress).pow(2.5f)
            val alpha = (1f - (ease - 0.15f) / 0.85f).coerceIn(0f, 1f)

            // Primary shockwave ring
            val primaryRingRadius = ease * 700f
            val primaryRingAlpha = (1f - ease * 1.1f).coerceIn(0f, 0.8f)
            if (primaryRingRadius > 0f && primaryRingAlpha > 0f) {
                drawCircle(
                    color = colors.first().copy(alpha = primaryRingAlpha),
                    radius = primaryRingRadius,
                    center = origin,
                    style = Stroke(width = 2.5.dp.toPx())
                )
            }

            // Secondary wider ambient shockwave ring
            val secondaryRingRadius = ease * 1200f
            val secondaryRingAlpha = (1f - ease * 1.3f).coerceIn(0f, 0.45f)
            if (secondaryRingRadius > 0f && secondaryRingAlpha > 0f) {
                drawCircle(
                    color = colors.last().copy(alpha = secondaryRingAlpha),
                    radius = secondaryRingRadius,
                    center = origin,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // Flying particles radiating across the entire screen
            for (p in particles) {
                val dist = p.distance * ease
                val x = origin.x + (dist * cos(p.angle)).toFloat()
                val y = origin.y + (dist * sin(p.angle)).toFloat()
                val particleRadius = (p.radius * (1f - ease * 0.4f)).coerceAtLeast(0.5f)

                drawCircle(
                    color = p.color.copy(alpha = alpha),
                    radius = particleRadius,
                    center = Offset(x, y)
                )
            }
        }
    }
}
