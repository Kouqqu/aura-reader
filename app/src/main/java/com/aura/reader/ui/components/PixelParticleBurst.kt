package com.aura.reader.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
        List(110) {
            val angle = rand.nextDouble(0.0, Math.PI * 2)
            // Radiating smoothly across the entire display screen
            val distance = rand.nextFloat() * 1600f + 250f
            val radius = rand.nextFloat() * 4.2f + 2.0f
            val color = colors[rand.nextInt(colors.size)]
            PixelParticle(
                angle = angle,
                distance = distance,
                radius = radius,
                color = color
            )
        }
    }

    // 1350ms duration with smooth physical deceleration easing (starts lively, glides smoothly)
    LaunchedEffect(triggerKey) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1350,
                easing = CubicBezierEasing(0.08f, 0.8f, 0.15f, 1.0f)
            )
        )
    }

    val currentProgress = progress.value
    if (currentProgress < 1f) {
        Canvas(
            modifier = modifier.graphicsLayer(clip = false)
        ) {
            val ease = currentProgress
            // Alpha stays bright for prominent visibility, fading softly near the finish
            val alpha = (1f - (ease - 0.35f) / 0.65f).coerceIn(0f, 1f)

            // NO shockwave circles/rings - purely sparkling, floating particle dots
            for (p in particles) {
                val dist = p.distance * ease
                val x = origin.x + (dist * cos(p.angle)).toFloat()
                val y = origin.y + (dist * sin(p.angle)).toFloat()
                val particleRadius = (p.radius * (1f - ease * 0.35f)).coerceAtLeast(0.8f)

                drawCircle(
                    color = p.color.copy(alpha = alpha),
                    radius = particleRadius,
                    center = Offset(x, y)
                )
            }
        }
    }
}
