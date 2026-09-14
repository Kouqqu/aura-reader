package com.aura.reader.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class Particle(
    val angle: Double,
    val speed: Float,
    val radius: Float,
    val color: Color,
    val distance: Float
)

class ParticleBurstState {
    var triggerKey by mutableStateOf(0L)
        private set
    var origin by mutableStateOf(Offset.Zero)
        private set
    var colors by mutableStateOf<List<Color>>(emptyList())
        private set

    fun burst(origin: Offset, colors: List<Color>) {
        this.origin = origin
        this.colors = colors
        this.triggerKey = System.currentTimeMillis()
    }
}

@Composable
fun rememberParticleBurstState(): ParticleBurstState {
    return remember { ParticleBurstState() }
}

@Composable
fun PixelParticleBurstOverlay(
    state: ParticleBurstState,
    modifier: Modifier = Modifier
) {
    if (state.triggerKey == 0L || state.colors.isEmpty()) return

    val progress = remember(state.triggerKey) { Animatable(0f) }
    val particles = remember(state.triggerKey) {
        val rand = Random(state.triggerKey)
        List(48) {
            val angle = rand.nextDouble(0.0, Math.PI * 2)
            val distance = rand.nextFloat() * 220f + 60f
            val radius = rand.nextFloat() * 4.5f + 2f
            val color = state.colors[rand.nextInt(state.colors.size)]
            Particle(
                angle = angle,
                speed = rand.nextFloat() * 0.8f + 0.4f,
                radius = radius,
                color = color,
                distance = distance
            )
        }
    }

    LaunchedEffect(state.triggerKey) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
        )
    }

    val currentProgress = progress.value
    if (currentProgress < 1f) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val ease = currentProgress
            val alpha = (1f - (currentProgress - 0.35f) / 0.65f).coerceIn(0f, 1f)

            for (p in particles) {
                val dist = p.distance * ease
                val x = state.origin.x + (dist * cos(p.angle)).toFloat()
                val y = state.origin.y + (dist * sin(p.angle)).toFloat()
                val particleRadius = p.radius * (1f - ease * 0.4f)

                drawCircle(
                    color = p.color.copy(alpha = alpha),
                    radius = particleRadius,
                    center = Offset(x, y)
                )
            }
        }
    }
}
