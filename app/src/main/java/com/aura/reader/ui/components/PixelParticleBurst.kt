package com.aura.reader.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlin.math.cos
import kotlin.math.hypot
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
    durationMillis: Int = 2000,
    onFinished: () -> Unit = {}
) {
    if (triggerKey == 0L || colors.isEmpty()) return

    val progress = remember(triggerKey) { Animatable(0f) }
    var isRunning by remember(triggerKey) { mutableStateOf(true) }

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    val particles = remember(triggerKey) {
        val rand = Random(triggerKey)
        val maxDist = hypot(screenWidthPx, screenHeightPx) + 300f
        List(130) {
            val angle = rand.nextDouble(0.0, Math.PI * 2)
            // Varied distance and speed for organic, immersive depth
            val distance = rand.nextFloat() * maxDist * 0.9f + 120f
            val radius = rand.nextFloat() * 4.4f + 1.8f
            val color = colors[rand.nextInt(colors.size)]
            val speedMult = rand.nextFloat() * 0.45f + 0.8f // range 0.8 to 1.25
            PixelParticle(
                angle = angle,
                distance = distance,
                radius = radius,
                color = color,
                speedMult = speedMult
            )
        }
    }

    // 2000ms duration with smooth physical deceleration easing (organic, slow & immersive)
    LaunchedEffect(triggerKey) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = CubicBezierEasing(0.04f, 0.65f, 0.12f, 1.0f)
            )
        )
        isRunning = false
        onFinished()
    }

    if (isRunning) {
        Popup(
            properties = PopupProperties(
                focusable = false,
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                clippingEnabled = false
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Read progress ONLY inside Canvas draw phase (skips Composition and Layout phases!)
                    val pVal = progress.value
                    if (pVal >= 1f) return@Canvas

                    // Smooth fade: remains visible across the screen, then gracefully fades out
                    val alpha = (1f - (pVal - 0.45f) / 0.55f).coerceIn(0f, 1f)

                    // NO shockwave circles/rings - purely sparkling, floating stardust particle dots
                    for (p in particles) {
                        val particleProgress = (pVal * p.speedMult).coerceIn(0f, 1f)
                        val dist = p.distance * particleProgress
                        val x = origin.x + (dist * cos(p.angle)).toFloat()
                        val y = origin.y + (dist * sin(p.angle)).toFloat()
                        val particleRadius = (p.radius * (1f - pVal * 0.35f)).coerceAtLeast(0.8f)

                        drawCircle(
                            color = p.color.copy(alpha = alpha),
                            radius = particleRadius,
                            center = Offset(x, y)
                        )
                    }
                }
            }
        }
    }
}
