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
    val color: Color,
    val speedMult: Float
)

@Composable
fun PixelFullScreenBurst(
    triggerKey: Long,
    origin: Offset,
    colors: List<Color>,
    durationMillis: Int = 2400,
    modifier: Modifier = Modifier
) {
    if (triggerKey == 0L || colors.isEmpty()) return

    val progress = remember(triggerKey) { Animatable(0f) }

    val particles = remember(triggerKey) {
        val rand = Random(triggerKey)
        List(180) {
            val angle = rand.nextDouble(0.0, Math.PI * 2)
            // Radiating smoothly across the screen from the button center
            val distance = rand.nextFloat() * 1400f + 180f
            val radius = rand.nextFloat() * 4.2f + 2.2f
            val color = colors[rand.nextInt(colors.size)]
            val speedMult = rand.nextFloat() * 0.5f + 0.75f // range 0.75 to 1.25 for organic cloud
            PixelParticle(
                angle = angle,
                distance = distance,
                radius = radius,
                color = color,
                speedMult = speedMult
            )
        }
    }

    // 2400ms duration with smooth physical deceleration easing (starts lively, glides like glowing stardust)
    LaunchedEffect(triggerKey) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = CubicBezierEasing(0.02f, 0.75f, 0.15f, 1.0f)
            )
        )
    }

    // Pure Canvas without Popup - NEVER intercepts touches or blocks clicks!
    Canvas(
        modifier = modifier.graphicsLayer(clip = false)
    ) {
        // Read progress ONLY inside Canvas draw phase (skips Composition and Layout phases!)
        val pVal = progress.value
        if (pVal >= 1f) return@Canvas

        // Stays bright and vibrant for the first 60%, then softly dissolves
        val alpha = if (pVal < 0.6f) 1f else ((1f - pVal) / 0.4f).coerceIn(0f, 1f)

        // Luminous glowing stardust dots with outer glow and solid core
        for (p in particles) {
            val particleProgress = (pVal * p.speedMult).coerceIn(0f, 1f)
            val dist = p.distance * particleProgress
            val x = origin.x + (dist * cos(p.angle)).toFloat()
            val y = origin.y + (dist * sin(p.angle)).toFloat()
            val particleRadius = (p.radius * (1f - pVal * 0.3f)).coerceAtLeast(1.0f)

            // Outer soft glow: provides strong contrast on both dark and light backgrounds
            drawCircle(
                color = p.color.copy(alpha = alpha * 0.35f),
                radius = particleRadius * 1.85f,
                center = Offset(x, y)
            )

            // Bright sparkling core
            drawCircle(
                color = p.color.copy(alpha = alpha),
                radius = particleRadius,
                center = Offset(x, y)
            )
        }
    }
}
