package com.aura.reader.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.aura.reader.data.model.HolidaySeason
import com.aura.reader.ui.theme.LocalEInkMode
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class AmbientSnowflake(
    val xFraction: Float,
    val yOffset: Float,
    val radiusDp: Float,
    val speed: Float,
    val swayAmplitudeDp: Float,
    val swayPhase: Float,
    val alpha: Float,
    val isStar: Boolean
)

private data class AmbientStar(
    val xFraction: Float,
    val yFraction: Float,
    val radiusDp: Float,
    val phase: Float,
    val speed: Float,
    val isGold: Boolean,
    val isCross: Boolean
)

private data class SnowGlobeParticle(
    val angle: Float,
    val initialSpeed: Float,
    val swirlDir: Float,
    val driftX: Float,
    val gravity: Float,
    val radiusDp: Float,
    val color: Color,
    val isFlakeStar: Boolean,
    val phase: Float
)

internal fun HolidaySeason.isChristmasSeason(): Boolean =
    this == HolidaySeason.CHRISTMAS_WESTERN || this == HolidaySeason.CHRISTMAS_ORTHODOX

internal fun HolidaySeason.hasWinterEffects(): Boolean = this.isWinterSeason

private data class MiniFireworkSpec(
    val xFraction: Float,
    val yFraction: Float,
    val phaseOffset: Float,
    val radiusDp: Float,
    val color: Color,
    val secondaryColor: Color,
    val rayCount: Int
)

private data class BurstFireworkRocket(
    val centerXFrac: Float,
    val centerYFrac: Float,
    val startDelay: Float,
    val primaryColor: Color,
    val secondaryColor: Color,
    val sparks: List<FireworkSpark>
)

private data class FireworkSpark(
    val angle: Float,
    val speed: Float,
    val radiusDp: Float,
    val useSecondary: Boolean,
    val twinklePhase: Float
)

/**
 * Ambient winter holiday visual layer for the Library Top Bar:
 * - CHRISTMAS_WESTERN: warm sagging fairy lights garland + soft drifting snow.
 * - CHRISTMAS_ORTHODOX: quiet starry night sky + 8-pointed Star of Bethlehem + subtle snow.
 * - NEW_YEAR: gentle mini firework bursts in the upper sky + sparkling icy snow.
 * - OLD_NEW_YEAR: soft silver crescent moon glow + tranquil crystal snow.
 */
@Composable
fun ChristmasTopBarOverlay(
    holidaySeason: HolidaySeason,
    scrollAlpha: Float,
    isThemeDark: Boolean,
    modifier: Modifier = Modifier
) {
    val isEInk = LocalEInkMode.current
    if (isEInk || !holidaySeason.hasWinterEffects() || scrollAlpha <= 0.005f) return

    val infiniteTransition = rememberInfiniteTransition(label = "christmasTopBar")
    val snowProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "snowFall"
    )
    val twinklePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "twinklePhase"
    )
    val fireworkCycle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "miniFireworkCycle"
    )

    val snowflakes = remember(holidaySeason) {
        val seed = when (holidaySeason) {
            HolidaySeason.CHRISTMAS_WESTERN -> 1225
            HolidaySeason.CHRISTMAS_ORTHODOX -> 107
            HolidaySeason.NEW_YEAR -> 2026
            else -> 114
        }
        val rng = Random(seed)
        val count = when (holidaySeason) {
            HolidaySeason.CHRISTMAS_WESTERN -> 18
            HolidaySeason.NEW_YEAR -> 16
            HolidaySeason.OLD_NEW_YEAR -> 14
            else -> 10
        }
        List(count) {
            AmbientSnowflake(
                xFraction = rng.nextFloat(),
                yOffset = rng.nextFloat(),
                radiusDp = 1.2f + rng.nextFloat() * 1.9f,
                speed = if (rng.nextBoolean()) 1f else 2f,
                swayAmplitudeDp = 5f + rng.nextFloat() * 9f,
                swayPhase = rng.nextFloat() * (2f * PI.toFloat()),
                alpha = 0.25f + rng.nextFloat() * 0.45f,
                isStar = rng.nextFloat() < if (holidaySeason == HolidaySeason.NEW_YEAR) 0.45f else 0.28f
            )
        }
    }

    val miniFireworks = remember {
        listOf(
            MiniFireworkSpec(0.22f, 0.28f, 0.00f, 22f, Color(0xFFFFD700), Color(0xFFFFFFFF), 10),
            MiniFireworkSpec(0.76f, 0.24f, 0.33f, 26f, Color(0xFF40C4FF), Color(0xFFFFD700), 12),
            MiniFireworkSpec(0.52f, 0.20f, 0.66f, 20f, Color(0xFFE040FB), Color(0xFF80D8FF), 10)
        )
    }

    val orthodoxStars = remember {
        val rng = Random(701)
        List(16) {
            AmbientStar(
                xFraction = 0.06f + rng.nextFloat() * 0.88f,
                yFraction = 0.14f + rng.nextFloat() * 0.68f,
                radiusDp = 0.9f + rng.nextFloat() * 1.4f,
                phase = rng.nextFloat() * (2f * PI.toFloat()),
                speed = 1f + (it % 3) * 0.5f,
                isGold = rng.nextFloat() < 0.62f,
                isCross = rng.nextFloat() < 0.35f
            )
        }
    }

    val garlandColors = remember {
        listOf(
            Color(0xFFFF453A), // Warm Crimson
            Color(0xFFFFD60A), // Warm Gold
            Color(0xFF34C759), // Pine Green
            Color(0xFFFFF3D6), // Candle White
            Color(0xFFFF453A),
            Color(0xFFFFD60A),
            Color(0xFF34C759),
            Color(0xFFFFF3D6)
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        when (holidaySeason) {
            HolidaySeason.CHRISTMAS_WESTERN -> {
                // 1. Soft sagging fairy-lights wire along the upper top bar
                val wireStartY = 34.dp.toPx().coerceAtMost(h * 0.26f)
                val sagDepth = 13.dp.toPx()
                val midX = w * 0.5f

                val wireColor = if (isThemeDark) {
                    Color(0xFF2E4A36).copy(alpha = 0.55f * scrollAlpha)
                } else {
                    Color(0xFF2E5939).copy(alpha = 0.32f * scrollAlpha)
                }

                val wirePath = Path().apply {
                    moveTo(-4.dp.toPx(), wireStartY)
                    quadraticTo(w * 0.25f, wireStartY + sagDepth, midX, wireStartY + 2.dp.toPx())
                    quadraticTo(w * 0.75f, wireStartY + sagDepth, w + 4.dp.toPx(), wireStartY)
                }
                drawPath(
                    path = wirePath,
                    color = wireColor,
                    style = Stroke(width = 1.15.dp.toPx(), cap = StrokeCap.Round)
                )

                // 8 warm breathing bulbs along the two catenary arcs
                val bulbCount = garlandColors.size
                for (i in 0 until bulbCount) {
                    val u = (i + 0.5f) / bulbCount
                    val x = w * u
                    val localT = if (u < 0.5f) u * 2f else (u - 0.5f) * 2f
                    val y = wireStartY + (4f * localT * (1f - localT)) * (sagDepth * 0.52f) +
                        (if (u < 0.5f) localT * 2.dp.toPx() else (1f - localT) * 2.dp.toPx())

                    val pulse = 0.62f + 0.38f * sin(twinklePhase + i * 0.95f)
                    val bulbColor = garlandColors[i]
                    val glowRadius = (13.dp.toPx() + 4.dp.toPx() * pulse)

                    // Soft warm glow halo
                    drawCircle(
                        brush = Brush.radialGradient(
                            0.0f to bulbColor.copy(alpha = (if (isThemeDark) 0.48f else 0.34f) * pulse * scrollAlpha),
                            0.45f to bulbColor.copy(alpha = (if (isThemeDark) 0.16f else 0.10f) * pulse * scrollAlpha),
                            1.0f to Color.Transparent,
                            center = Offset(x, y + 3.dp.toPx()),
                            radius = glowRadius
                        ),
                        radius = glowRadius,
                        center = Offset(x, y + 3.dp.toPx())
                    )

                    // Tiny socket
                    drawLine(
                        color = wireColor,
                        start = Offset(x, y),
                        end = Offset(x, y + 2.2.dp.toPx()),
                        strokeWidth = 1.4.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // Bulb core
                    drawCircle(
                        color = bulbColor.copy(alpha = (0.78f + 0.22f * pulse) * scrollAlpha),
                        radius = 2.6.dp.toPx(),
                        center = Offset(x, y + 3.4.dp.toPx())
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.65f * pulse * scrollAlpha),
                        radius = 1.0.dp.toPx(),
                        center = Offset(x - 0.5.dp.toPx(), y + 2.9.dp.toPx())
                    )
                }
            }
            HolidaySeason.CHRISTMAS_ORTHODOX -> {
                // ORTHODOX CHRISTMAS: Quiet starry night + 8-pointed Star of Bethlehem
                for (star in orthodoxStars) {
                    val twinkle = 0.35f + 0.65f * ((sin(twinklePhase * star.speed + star.phase) + 1f) * 0.5f)
                    val sx = w * star.xFraction
                    val sy = h * star.yFraction
                    val baseColor = if (star.isGold) {
                        if (isThemeDark) Color(0xFFFFE082) else Color(0xFFFFB300)
                    } else {
                        if (isThemeDark) Color(0xFFB3E5FC) else Color(0xFF29B6F6)
                    }
                    val a = (if (isThemeDark) 0.72f else 0.48f) * twinkle * scrollAlpha
                    val r = star.radiusDp.dp.toPx()

                    if (star.isCross) {
                        drawFourPointStar(Offset(sx, sy), r * 2.4f, baseColor.copy(alpha = a))
                    } else {
                        drawCircle(
                            color = baseColor.copy(alpha = a),
                            radius = r,
                            center = Offset(sx, sy)
                        )
                    }
                }

                // Star of Bethlehem softly glowing in the upper-right sky
                val bethlehemCenter = Offset(w * 0.78f, (48.dp.toPx()).coerceAtMost(h * 0.34f))
                val bethlehemPulse = 0.78f + 0.22f * sin(twinklePhase * 0.7f)
                val starGold = if (isThemeDark) Color(0xFFFFD54F) else Color(0xFFFFA000)
                val haloRadius = 34.dp.toPx() * bethlehemPulse

                drawCircle(
                    brush = Brush.radialGradient(
                        0.0f to starGold.copy(alpha = (if (isThemeDark) 0.38f else 0.24f) * scrollAlpha),
                        0.45f to starGold.copy(alpha = (if (isThemeDark) 0.12f else 0.08f) * scrollAlpha),
                        1.0f to Color.Transparent,
                        center = bethlehemCenter,
                        radius = haloRadius
                    ),
                    radius = haloRadius,
                    center = bethlehemCenter
                )
                drawEightPointStar(
                    center = bethlehemCenter,
                    majorRadius = 11.dp.toPx() * (0.94f + 0.08f * bethlehemPulse),
                    minorRadius = 5.5.dp.toPx(),
                    color = starGold.copy(alpha = (if (isThemeDark) 0.90f else 0.78f) * scrollAlpha),
                    coreColor = Color.White.copy(alpha = 0.85f * scrollAlpha)
                )
            }
            HolidaySeason.NEW_YEAR -> {
                // NEW YEAR: Subtle cyclic mini fireworks blooming softly in the upper sky
                for (fw in miniFireworks) {
                    val localT = (fireworkCycle + fw.phaseOffset) % 1f
                    // Active burst lasts ~55% of cycle, rest is calm pause
                    if (localT < 0.58f) {
                        val p = localT / 0.58f
                        val easeOut = 1f - (1f - p) * (1f - p)
                        val alpha = (if (p < 0.2f) p / 0.2f else ((1f - p) / 0.8f).coerceIn(0f, 1f)) *
                            scrollAlpha * (if (isThemeDark) 0.68f else 0.45f)
                        val center = Offset(w * fw.xFraction, (42.dp.toPx() + h * fw.yFraction * 0.35f).coerceAtMost(h * 0.48f))
                        val maxR = fw.radiusDp.dp.toPx()

                        // Soft center flash
                        if (p < 0.45f) {
                            val flashR = maxR * 0.9f * easeOut
                            drawCircle(
                                brush = Brush.radialGradient(
                                    0.0f to fw.color.copy(alpha = alpha * 0.55f),
                                    1.0f to Color.Transparent,
                                    center = center,
                                    radius = flashR.coerceAtLeast(1f)
                                ),
                                radius = flashR.coerceAtLeast(1f),
                                center = center
                            )
                        }

                        val innerR = maxR * (0.18f + 0.42f * easeOut)
                        val outerR = maxR * (0.35f + 0.65f * easeOut)
                        val gravityDrop = 5.dp.toPx() * p * p
                        for (rIdx in 0 until fw.rayCount) {
                            val angle = (2.0 * PI * rIdx / fw.rayCount).toFloat()
                            val dx = cos(angle)
                            val dy = sin(angle)
                            val rayColor = if (rIdx % 2 == 0) fw.color else fw.secondaryColor
                            val start = Offset(center.x + dx * innerR, center.y + dy * innerR + gravityDrop * 0.4f)
                            val end = Offset(center.x + dx * outerR, center.y + dy * outerR + gravityDrop)
                            drawLine(
                                color = rayColor.copy(alpha = alpha),
                                start = start,
                                end = end,
                                strokeWidth = 1.25.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                            drawCircle(
                                color = Color.White.copy(alpha = alpha * 0.9f),
                                radius = 1.1.dp.toPx(),
                                center = end
                            )
                        }
                    }
                }
            }
            HolidaySeason.OLD_NEW_YEAR -> {
                // OLD NEW YEAR: Calm frost silver-blue moon glow + subtle twinkling frost stars
                val moonCenter = Offset(w * 0.80f, 48.dp.toPx().coerceAtMost(h * 0.34f))
                val pulse = 0.85f + 0.15f * sin(twinklePhase * 0.6f)
                val moonColor = if (isThemeDark) Color(0xFFB3E5FC) else Color(0xFF29B6F6)
                drawCircle(
                    brush = Brush.radialGradient(
                        0.0f to moonColor.copy(alpha = (if (isThemeDark) 0.30f else 0.20f) * pulse * scrollAlpha),
                        0.55f to moonColor.copy(alpha = (if (isThemeDark) 0.10f else 0.06f) * scrollAlpha),
                        1.0f to Color.Transparent,
                        center = moonCenter,
                        radius = 32.dp.toPx() * pulse
                    ),
                    radius = 32.dp.toPx() * pulse,
                    center = moonCenter
                )
                drawSixPointSnowflake(
                    center = moonCenter,
                    radius = 10.dp.toPx(),
                    color = moonColor.copy(alpha = (if (isThemeDark) 0.72f else 0.58f) * scrollAlpha),
                    coreColor = Color.White.copy(alpha = 0.8f * scrollAlpha)
                )
            }
            else -> {}
        }

        // 2. Soft drifting snow confined to the top bar, smoothly fading out toward the bottom
        val snowColor = when {
            isThemeDark && holidaySeason == HolidaySeason.NEW_YEAR -> Color(0xFFE1F5FE)
            isThemeDark -> Color.White
            holidaySeason == HolidaySeason.OLD_NEW_YEAR -> Color(0xFF78909C)
            else -> Color(0xFF90CAF9)
        }
        for (flake in snowflakes) {
            val yFrac = (flake.yOffset + snowProgress * flake.speed) % 1f
            val y = yFrac * h
            val edgeFade = (1f - ((yFrac - 0.62f) / 0.38f).coerceIn(0f, 1f)) *
                (yFrac / 0.12f).coerceIn(0f, 1f)
            val swayPx = sin(snowProgress * 2f * PI.toFloat() * flake.speed + flake.swayPhase) *
                flake.swayAmplitudeDp.dp.toPx()
            val x = (flake.xFraction * w + swayPx).coerceIn(0f, w)
            val a = flake.alpha * edgeFade * scrollAlpha * (if (isThemeDark) 0.95f else 0.72f)
            val r = flake.radiusDp.dp.toPx()

            if (a > 0.01f) {
                if (flake.isStar) {
                    val starTint = if (holidaySeason == HolidaySeason.NEW_YEAR && flake.radiusDp > 2.2f) {
                        Color(0xFFFFE082)
                    } else snowColor
                    drawFourPointStar(Offset(x, y), r * 1.6f, starTint.copy(alpha = a))
                } else {
                    drawCircle(
                        color = snowColor.copy(alpha = a),
                        radius = r,
                        center = Offset(x, y)
                    )
                }
            }
        }
    }
}

/**
 * Tiny handcrafted vector ornament on the top-right corner of the Aura logo box:
 * - WESTERN: sleek vector Santa hat tilted neatly on the corner.
 * - ORTHODOX: warm glowing 8-pointed golden star.
 */
@Composable
fun ChristmasLogoDecoration(
    holidaySeason: HolidaySeason,
    isSmallTitle: Boolean,
    modifier: Modifier = Modifier
) {
    if (!holidaySeason.hasWinterEffects()) return

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        when (holidaySeason) {
            HolidaySeason.CHRISTMAS_WESTERN -> {
                // Sleek miniature Santa hat tilted jauntily on the top-right corner
                rotate(degrees = 26f, pivot = Offset(w * 0.50f, h * 0.58f)) {
                    val hatPath = Path().apply {
                        moveTo(w * 0.16f, h * 0.72f)
                        quadraticTo(w * 0.34f, h * 0.16f, w * 0.80f, h * 0.28f)
                        quadraticTo(w * 0.68f, h * 0.48f, w * 0.82f, h * 0.74f)
                        close()
                    }
                    drawPath(hatPath, color = Color(0xFFD92B2B))

                    // Subtle highlight fold on the cap
                    val foldPath = Path().apply {
                        moveTo(w * 0.25f, h * 0.64f)
                        quadraticTo(w * 0.40f, h * 0.28f, w * 0.70f, h * 0.32f)
                    }
                    drawPath(
                        path = foldPath,
                        color = Color(0xFFFF6B6B).copy(alpha = 0.55f),
                        style = Stroke(width = (if (isSmallTitle) 0.9f else 1.4f).dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Fluffy white fur trim along the base
                    drawLine(
                        color = Color(0xFFFAFAFA),
                        start = Offset(w * 0.14f, h * 0.74f),
                        end = Offset(w * 0.82f, h * 0.76f),
                        strokeWidth = (if (isSmallTitle) 2.2f else 3.4f).dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // Pom-pom at the tilted tip
                    drawCircle(
                        color = Color(0xFFFFFFFF),
                        radius = (if (isSmallTitle) 1.9f else 2.8f).dp.toPx(),
                        center = Offset(w * 0.83f, h * 0.33f)
                    )
                }
            }
            HolidaySeason.CHRISTMAS_ORTHODOX -> {
                // Orthodox: glowing golden Star of Bethlehem safely inside canvas bounds
                val center = Offset(w * 0.50f, h * 0.50f)
                val gold = Color(0xFFFFD54F)
                drawCircle(
                    brush = Brush.radialGradient(
                        0.0f to gold.copy(alpha = 0.52f),
                        1.0f to Color.Transparent,
                        center = center,
                        radius = w * 0.48f
                    ),
                    radius = w * 0.48f,
                    center = center
                )
                drawEightPointStar(
                    center = center,
                    majorRadius = w * 0.36f,
                    minorRadius = w * 0.18f,
                    color = gold,
                    coreColor = Color.White
                )
            }
            HolidaySeason.NEW_YEAR -> {
                // New Year: carved 6-pointed ice snowflake with golden spark center
                val center = Offset(w * 0.50f, h * 0.50f)
                val ice = Color(0xFF40C4FF)
                drawCircle(
                    brush = Brush.radialGradient(
                        0.0f to ice.copy(alpha = 0.48f),
                        1.0f to Color.Transparent,
                        center = center,
                        radius = w * 0.48f
                    ),
                    radius = w * 0.48f,
                    center = center
                )
                drawSixPointSnowflake(
                    center = center,
                    radius = w * 0.37f,
                    color = Color(0xFFB3E5FC),
                    coreColor = Color(0xFFFFD700)
                )
            }
            HolidaySeason.OLD_NEW_YEAR -> {
                // Old New Year: tranquil silver-ice 6-pointed snowflake
                val center = Offset(w * 0.50f, h * 0.50f)
                val frost = Color(0xFF81D4FA)
                drawCircle(
                    brush = Brush.radialGradient(
                        0.0f to frost.copy(alpha = 0.38f),
                        1.0f to Color.Transparent,
                        center = center,
                        radius = w * 0.46f
                    ),
                    radius = w * 0.46f,
                    center = center
                )
                drawSixPointSnowflake(
                    center = center,
                    radius = w * 0.35f,
                    color = Color(0xFFE1F5FE),
                    coreColor = Color.White
                )
            }
            else -> {}
        }
    }
}

/**
 * Subtle frost / snow rim along the top edge of the "Currently Reading" hero card
 * plus a slow, understated golden shimmer sweeping around the border every ~7 seconds.
 */
@Composable
fun ChristmasHeroCardDecoration(
    holidaySeason: HolidaySeason,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val isEInk = LocalEInkMode.current
    if (isEInk || !holidaySeason.hasWinterEffects()) return

    val infiniteTransition = rememberInfiniteTransition(label = "heroChristmasShimmer")
    val shimmerProgress by infiniteTransition.animateFloat(
        initialValue = -0.35f,
        targetValue = 1.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerProgress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        // 1. Delicate snow / frost cap along the top rounded rim
        val frostColor = when (holidaySeason) {
            HolidaySeason.NEW_YEAR -> if (isDark) Color(0xFFB3E5FC) else Color(0xFF81D4FA)
            HolidaySeason.OLD_NEW_YEAR -> if (isDark) Color(0xFFCFD8DC) else Color(0xFFB0BEC5)
            else -> if (isDark) Color(0xFFE3F2FD) else Color(0xFFB3E5FC)
        }
        val snowRimHeight = 9.dp.toPx()
        val snowPath = Path().apply {
            moveTo(0f, 0f)
            lineTo(w, 0f)
            lineTo(w, snowRimHeight * 0.52f)
            cubicTo(
                w * 0.82f, snowRimHeight * 1.15f,
                w * 0.68f, snowRimHeight * 0.28f,
                w * 0.50f, snowRimHeight * 0.82f
            )
            cubicTo(
                w * 0.34f, snowRimHeight * 1.25f,
                w * 0.16f, snowRimHeight * 0.35f,
                0f, snowRimHeight * 0.65f
            )
            close()
        }
        drawPath(
            path = snowPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    frostColor.copy(alpha = if (isDark) 0.26f else 0.38f),
                    frostColor.copy(alpha = if (isDark) 0.08f else 0.14f),
                    Color.Transparent
                ),
                startY = 0f,
                endY = snowRimHeight * 1.3f
            )
        )

        // Tiny frost sparkles or carved corner snowflakes
        val sparkleAlpha = if (isDark) 0.52f else 0.42f
        if (holidaySeason == HolidaySeason.NEW_YEAR || holidaySeason == HolidaySeason.OLD_NEW_YEAR) {
            val flakeTint = if (holidaySeason == HolidaySeason.NEW_YEAR) Color(0xFF80D8FF) else Color(0xFFCFD8DC)
            val coreTint = if (holidaySeason == HolidaySeason.NEW_YEAR) Color(0xFFFFD700) else Color.White
            drawSixPointSnowflake(Offset(w * 0.12f, 10.dp.toPx()), 4.5.dp.toPx(), flakeTint.copy(alpha = sparkleAlpha), coreTint.copy(alpha = sparkleAlpha))
            drawSixPointSnowflake(Offset(w * 0.88f, 10.dp.toPx()), 4.5.dp.toPx(), flakeTint.copy(alpha = sparkleAlpha), coreTint.copy(alpha = sparkleAlpha))
            drawFourPointStar(Offset(w * 0.52f, 5.dp.toPx()), 2.6.dp.toPx(), coreTint.copy(alpha = sparkleAlpha * 0.85f))
        } else {
            val sparkleColor = if (holidaySeason == HolidaySeason.CHRISTMAS_ORTHODOX) {
                Color(0xFFFFE082)
            } else {
                Color.White
            }
            drawFourPointStar(Offset(w * 0.14f, 6.dp.toPx()), 3.2.dp.toPx(), sparkleColor.copy(alpha = sparkleAlpha))
            drawFourPointStar(Offset(w * 0.86f, 7.dp.toPx()), 3.6.dp.toPx(), sparkleColor.copy(alpha = sparkleAlpha * 0.9f))
            drawFourPointStar(Offset(w * 0.53f, 5.dp.toPx()), 2.4.dp.toPx(), sparkleColor.copy(alpha = sparkleAlpha * 0.7f))
        }

        // 2. Slow, refined light sweep along the card border
        if (shimmerProgress in -0.25f..1.25f) {
            val sweepX = w * shimmerProgress
            val bandHalf = w * 0.22f
            val shimmerColors = when (holidaySeason) {
                HolidaySeason.NEW_YEAR -> listOf(
                    Color.Transparent,
                    Color(0xFF40C4FF).copy(alpha = if (isDark) 0.58f else 0.45f),
                    Color(0xFFFFD700).copy(alpha = if (isDark) 0.62f else 0.50f),
                    Color.White.copy(alpha = if (isDark) 0.48f else 0.35f),
                    Color.Transparent
                )
                HolidaySeason.OLD_NEW_YEAR -> listOf(
                    Color.Transparent,
                    Color(0xFF81D4FA).copy(alpha = if (isDark) 0.48f else 0.38f),
                    Color.White.copy(alpha = if (isDark) 0.52f else 0.42f),
                    Color(0xFFB0BEC5).copy(alpha = if (isDark) 0.45f else 0.35f),
                    Color.Transparent
                )
                else -> {
                    val goldColor = if (holidaySeason == HolidaySeason.CHRISTMAS_ORTHODOX) {
                        Color(0xFFFFD54F)
                    } else {
                        Color(0xFFFFE082)
                    }
                    listOf(
                        Color.Transparent,
                        goldColor.copy(alpha = if (isDark) 0.55f else 0.45f),
                        Color.White.copy(alpha = if (isDark) 0.45f else 0.35f),
                        goldColor.copy(alpha = if (isDark) 0.55f else 0.45f),
                        Color.Transparent
                    )
                }
            }
            val shimmerBrush = Brush.linearGradient(
                colors = shimmerColors,
                start = Offset(sweepX - bandHalf, 0f),
                end = Offset(sweepX + bandHalf, h * 0.65f)
            )
            val cornerPx = 24.dp.toPx()
            val strokePx = 1.5.dp.toPx()
            drawRoundRect(
                brush = shimmerBrush,
                topLeft = Offset(strokePx / 2f, strokePx / 2f),
                size = Size(w - strokePx, h - strokePx),
                cornerRadius = CornerRadius(cornerPx, cornerPx),
                style = Stroke(width = strokePx)
            )
        }
    }
}

/**
 * Tasteful winter backdrop for the chapter transition spread in the reader
 * (never touches actual book pages).
 */
@Composable
fun ChristmasChapterTransitionOverlay(
    holidaySeason: HolidaySeason,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val isEInk = LocalEInkMode.current
    if (isEInk || !holidaySeason.hasWinterEffects()) return

    val infiniteTransition = rememberInfiniteTransition(label = "chapterChristmas")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "chapterSnow"
    )
    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "chapterStarPulse"
    )

    val isWestern = holidaySeason == HolidaySeason.CHRISTMAS_WESTERN
    val isOrthodox = holidaySeason == HolidaySeason.CHRISTMAS_ORTHODOX
    val isNewYear = holidaySeason == HolidaySeason.NEW_YEAR
    val flakes = remember(holidaySeason) {
        val rng = Random(holidaySeason.ordinal * 997 + 2026)
        List(16) {
            AmbientSnowflake(
                xFraction = 0.05f + rng.nextFloat() * 0.90f,
                yOffset = rng.nextFloat(),
                radiusDp = 1.3f + rng.nextFloat() * 2.1f,
                speed = if (it % 2 == 0) 1f else 2f,
                swayAmplitudeDp = 8f + rng.nextFloat() * 12f,
                swayPhase = rng.nextFloat() * (2f * PI.toFloat()),
                alpha = 0.16f + rng.nextFloat() * 0.28f,
                isStar = isOrthodox || isNewYear || rng.nextFloat() < 0.35f
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        val center = Offset(w / 2f, h * 0.42f)
        val glowColor = when (holidaySeason) {
            HolidaySeason.CHRISTMAS_WESTERN -> Color(0xFFFFD54F)
            HolidaySeason.CHRISTMAS_ORTHODOX -> Color(0xFFFFB300)
            HolidaySeason.NEW_YEAR -> Color(0xFF40C4FF)
            else -> Color(0xFF81D4FA)
        }
        val pulse = 0.85f + 0.15f * sin(pulsePhase)
        val glowRadius = 110.dp.toPx() * pulse

        drawCircle(
            brush = Brush.radialGradient(
                0.0f to glowColor.copy(alpha = if (isDark) 0.13f else 0.09f),
                0.55f to glowColor.copy(alpha = if (isDark) 0.04f else 0.03f),
                1.0f to Color.Transparent,
                center = center,
                radius = glowRadius
            ),
            radius = glowRadius,
            center = center
        )

        if (isOrthodox) {
            drawEightPointStar(
                center = Offset(w / 2f, h * 0.30f),
                majorRadius = 14.dp.toPx() * pulse,
                minorRadius = 6.dp.toPx(),
                color = glowColor.copy(alpha = if (isDark) 0.45f else 0.35f),
                coreColor = Color.White.copy(alpha = if (isDark) 0.55f else 0.40f)
            )
        } else if (isNewYear || holidaySeason == HolidaySeason.OLD_NEW_YEAR) {
            drawSixPointSnowflake(
                center = Offset(w / 2f, h * 0.30f),
                radius = 12.dp.toPx() * pulse,
                color = glowColor.copy(alpha = if (isDark) 0.42f else 0.32f),
                coreColor = if (isNewYear) Color(0xFFFFD700).copy(alpha = 0.55f) else Color.White.copy(alpha = 0.5f)
            )
        }

        val particleColor = when {
            isDark && isWestern -> Color.White
            isDark && isOrthodox -> Color(0xFFFFE082)
            isDark && isNewYear -> Color(0xFF80D8FF)
            isDark -> Color(0xFFCFD8DC)
            isOrthodox -> Color(0xFFFFA000)
            else -> Color(0xFF64B5F6)
        }

        for (flake in flakes) {
            val yFrac = (flake.yOffset + progress * flake.speed) % 1f
            val y = yFrac * h
            val edgeFade = (yFrac / 0.15f).coerceIn(0f, 1f) * ((1f - yFrac) / 0.15f).coerceIn(0f, 1f)
            val swayPx = sin(progress * 2f * PI.toFloat() * flake.speed + flake.swayPhase) * flake.swayAmplitudeDp.dp.toPx()
            val x = (flake.xFraction * w + swayPx).coerceIn(0f, w)
            val a = flake.alpha * edgeFade
            val r = flake.radiusDp.dp.toPx()
            if (a > 0.01f) {
                val tint = if (isNewYear && flake.radiusDp > 2.4f) Color(0xFFFFD700) else particleColor
                if (flake.isStar) {
                    drawFourPointStar(Offset(x, y), r * 1.7f, tint.copy(alpha = a))
                } else {
                    drawCircle(color = tint.copy(alpha = a), radius = r, center = Offset(x, y))
                }
            }
        }
    }
}

/**
 * Interactive "Snow Globe" burst when tapping the Christmas badge in the top bar:
 * soft snowflakes, crystalline stars, and warm gold sparks swirl out from [origin]
 * and gently drift downward before fading away.
 */
@Composable
fun ChristmasSnowGlobeBurst(
    triggerKey: Long,
    origin: Offset,
    holidaySeason: HolidaySeason,
    durationMillis: Int = 3000,
    modifier: Modifier = Modifier
) {
    val isEInk = LocalEInkMode.current
    if (isEInk || triggerKey == 0L) return

    val progress = remember(triggerKey) { Animatable(0f) }
    val particles = remember(triggerKey, holidaySeason) {
        val rng = Random(triggerKey)
        val palette = when (holidaySeason) {
            HolidaySeason.CHRISTMAS_ORTHODOX -> listOf(
                Color.White,
                Color(0xFFFFE082),
                Color(0xFFFFB300),
                Color(0xFFB3E5FC),
                Color(0xFFFFF9C4)
            )
            HolidaySeason.OLD_NEW_YEAR -> listOf(
                Color.White,
                Color(0xFFE1F5FE),
                Color(0xFF81D4FA),
                Color(0xFFB0BEC5),
                Color(0xFFFFE082)
            )
            else -> listOf(
                Color.White,
                Color(0xFFE3F2FD),
                Color(0xFFFFD54F),
                Color(0xFFFF8A80),
                Color(0xFFA5D6A7)
            )
        }
        List(64) {
            SnowGlobeParticle(
                angle = rng.nextFloat() * (2f * PI.toFloat()),
                initialSpeed = 160f + rng.nextFloat() * 520f,
                swirlDir = if (rng.nextBoolean()) 1f else -1f,
                driftX = (rng.nextFloat() - 0.5f) * 140f,
                gravity = 190f + rng.nextFloat() * 280f,
                radiusDp = 1.8f + rng.nextFloat() * 3.4f,
                color = palette[rng.nextInt(palette.size)],
                isFlakeStar = rng.nextFloat() < 0.48f,
                phase = rng.nextFloat() * (2f * PI.toFloat())
            )
        }
    }

    LaunchedEffect(triggerKey) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing)
        )
    }

    val t = progress.value
    if (t <= 0f || t >= 1f) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val burstT = (t * 2.2f).coerceAtMost(1f)
        val settleT = t
        val fadeAlpha = if (t < 0.68f) 1f else ((1f - t) / 0.32f).coerceIn(0f, 1f)

        for (p in particles) {
            val swirlAngle = p.angle + p.swirlDir * settleT * 1.15f
            val radialDist = p.initialSpeed * burstT * (1f - 0.25f * burstT)
            val sway = sin(settleT * 9f + p.phase) * 24f * settleT
            val x = origin.x + cos(swirlAngle) * radialDist + p.driftX * settleT + sway
            val y = origin.y + sin(swirlAngle) * radialDist * 0.85f + p.gravity * settleT * settleT

            val r = p.radiusDp.dp.toPx() * (0.75f + 0.25f * (1f - settleT))
            val alpha = fadeAlpha * (0.68f + 0.32f * sin(settleT * 12f + p.phase)).coerceIn(0f, 1f)

            if (p.isFlakeStar) {
                drawEightPointStar(
                    center = Offset(x, y),
                    majorRadius = r * 1.85f,
                    minorRadius = r * 0.85f,
                    color = p.color.copy(alpha = alpha),
                    coreColor = Color.White.copy(alpha = alpha * 0.9f)
                )
            } else {
                drawCircle(
                    color = p.color.copy(alpha = alpha * 0.88f),
                    radius = r,
                    center = Offset(x, y)
                )
            }
        }
    }
}

/**
 * Interactive New Year fireworks burst (4 staggered volleys with glowing tracer trails
 * and sparkling gravity arcs) when tapping the New Year badge in the top bar.
 */
@Composable
fun NewYearFireworksBurst(
    triggerKey: Long,
    origin: Offset,
    durationMillis: Int = 3000,
    modifier: Modifier = Modifier
) {
    val isEInk = LocalEInkMode.current
    if (isEInk || triggerKey == 0L) return

    val progress = remember(triggerKey) { Animatable(0f) }
    val rockets = remember(triggerKey) {
        val rng = Random(triggerKey)
        val configs = listOf(
            Triple(0f, Color(0xFF00B0FF), Color(0xFFFFD700)),
            Triple(0.11f, Color(0xFFFFD700), Color(0xFFFFFFFF)),
            Triple(0.22f, Color(0xFFE040FB), Color(0xFF40C4FF)),
            Triple(0.32f, Color(0xFF40C4FF), Color(0xFFFFD54F))
        )
        configs.mapIndexed { idx, (delay, c1, c2) ->
            val sparks = List(24) { sIdx ->
                val baseAngle = (2.0 * PI * sIdx / 24.0).toFloat()
                FireworkSpark(
                    angle = baseAngle + (rng.nextFloat() - 0.5f) * 0.14f,
                    speed = 180f + rng.nextFloat() * 340f,
                    radiusDp = 1.6f + rng.nextFloat() * 1.8f,
                    useSecondary = sIdx % 2 == 1,
                    twinklePhase = rng.nextFloat() * (2f * PI.toFloat())
                )
            }
            BurstFireworkRocket(
                centerXFrac = when (idx) {
                    0 -> -1f // use origin
                    1 -> 0.24f + rng.nextFloat() * 0.12f
                    2 -> 0.68f + rng.nextFloat() * 0.14f
                    else -> 0.44f + rng.nextFloat() * 0.16f
                },
                centerYFrac = when (idx) {
                    0 -> -1f
                    1 -> 0.22f + rng.nextFloat() * 0.08f
                    2 -> 0.18f + rng.nextFloat() * 0.09f
                    else -> 0.32f + rng.nextFloat() * 0.08f
                },
                startDelay = delay,
                primaryColor = c1,
                secondaryColor = c2,
                sparks = sparks
            )
        }
    }

    LaunchedEffect(triggerKey) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = durationMillis, easing = LinearEasing)
        )
    }

    val globalT = progress.value
    if (globalT <= 0f || globalT >= 1f) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        for (rocket in rockets) {
            val localT = ((globalT - rocket.startDelay) / (1f - rocket.startDelay)).coerceIn(0f, 1f)
            if (localT <= 0f || localT >= 1f) continue

            val center = if (rocket.centerXFrac < 0f) {
                origin
            } else {
                Offset(w * rocket.centerXFrac, h * rocket.centerYFrac)
            }

            val ease = 1f - (1f - localT) * (1f - localT) * (1f - localT)
            val fade = if (localT < 0.6f) 1f else ((1f - localT) / 0.4f).coerceIn(0f, 1f)

            // Initial bright core flash
            if (localT < 0.25f) {
                val flashAlpha = (1f - localT / 0.25f) * 0.65f
                val flashR = 52.dp.toPx() * (0.3f + localT * 2.8f)
                drawCircle(
                    brush = Brush.radialGradient(
                        0.0f to rocket.primaryColor.copy(alpha = flashAlpha),
                        1.0f to Color.Transparent,
                        center = center,
                        radius = flashR
                    ),
                    radius = flashR,
                    center = center
                )
            }

            for (spark in rocket.sparks) {
                val dist = spark.speed * ease
                val prevEase = (1f - (1f - (localT - 0.06f).coerceAtLeast(0f)).let { it * it * it })
                val prevDist = spark.speed * prevEase
                val grav = 220f * localT * localT
                val prevGrav = 220f * ((localT - 0.06f).coerceAtLeast(0f)).let { it * it }

                val dx = cos(spark.angle)
                val dy = sin(spark.angle)
                val head = Offset(center.x + dx * dist, center.y + dy * dist + grav)
                val tail = Offset(center.x + dx * prevDist, center.y + dy * prevDist + prevGrav)

                val twinkle = 0.65f + 0.35f * sin(localT * 22f + spark.twinklePhase)
                val alpha = (fade * twinkle).coerceIn(0f, 1f)
                val color = if (spark.useSecondary) rocket.secondaryColor else rocket.primaryColor
                val rPx = spark.radiusDp.dp.toPx() * (1f - 0.35f * localT)

                drawLine(
                    color = color.copy(alpha = alpha * 0.72f),
                    start = tail,
                    end = head,
                    strokeWidth = (rPx * 0.9f).coerceAtLeast(1.2f),
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = Color.White.copy(alpha = alpha * 0.95f),
                    radius = (rPx * 0.85f).coerceAtLeast(1.1f),
                    center = head
                )
            }
        }
    }
}

private fun DrawScope.drawSixPointSnowflake(
    center: Offset,
    radius: Float,
    color: Color,
    coreColor: Color
) {
    val stroke = (radius * 0.16f).coerceAtLeast(1.0f)
    val branchStroke = (radius * 0.12f).coerceAtLeast(0.8f)
    for (i in 0 until 6) {
        val angle = (i * PI / 3.0).toFloat()
        val dx = cos(angle)
        val dy = sin(angle)
        val tip = Offset(center.x + dx * radius, center.y + dy * radius)
        drawLine(
            color = color,
            start = center,
            end = tip,
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        // Tiny V-branches near the tip
        val mid = Offset(center.x + dx * radius * 0.62f, center.y + dy * radius * 0.62f)
        val branchLen = radius * 0.28f
        val leftAngle = angle + 0.65f
        val rightAngle = angle - 0.65f
        drawLine(
            color = color.copy(alpha = color.alpha * 0.88f),
            start = mid,
            end = Offset(mid.x + cos(leftAngle) * branchLen, mid.y + sin(leftAngle) * branchLen),
            strokeWidth = branchStroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color.copy(alpha = color.alpha * 0.88f),
            start = mid,
            end = Offset(mid.x + cos(rightAngle) * branchLen, mid.y + sin(rightAngle) * branchLen),
            strokeWidth = branchStroke,
            cap = StrokeCap.Round
        )
    }
    drawCircle(
        color = coreColor,
        radius = (radius * 0.18f).coerceAtLeast(1.2f),
        center = center
    )
}

private fun DrawScope.drawFourPointStar(
    center: Offset,
    radius: Float,
    color: Color
) {
    val stroke = (radius * 0.24f).coerceAtLeast(1f)
    drawLine(
        color = color,
        start = Offset(center.x - radius, center.y),
        end = Offset(center.x + radius, center.y),
        strokeWidth = stroke,
        cap = StrokeCap.Round
    )
    drawLine(
        color = color,
        start = Offset(center.x, center.y - radius),
        end = Offset(center.x, center.y + radius),
        strokeWidth = stroke,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawEightPointStar(
    center: Offset,
    majorRadius: Float,
    minorRadius: Float,
    color: Color,
    coreColor: Color
) {
    val majorStroke = (majorRadius * 0.16f).coerceAtLeast(1.1f)
    val minorStroke = (minorRadius * 0.18f).coerceAtLeast(0.9f)
    // Vertical & horizontal major rays
    drawLine(
        color = color,
        start = Offset(center.x - majorRadius, center.y),
        end = Offset(center.x + majorRadius, center.y),
        strokeWidth = majorStroke,
        cap = StrokeCap.Round
    )
    drawLine(
        color = color,
        start = Offset(center.x, center.y - majorRadius),
        end = Offset(center.x, center.y + majorRadius),
        strokeWidth = majorStroke,
        cap = StrokeCap.Round
    )
    // Diagonal minor rays
    val diag = minorRadius * 0.7071f
    drawLine(
        color = color.copy(alpha = color.alpha * 0.85f),
        start = Offset(center.x - diag, center.y - diag),
        end = Offset(center.x + diag, center.y + diag),
        strokeWidth = minorStroke,
        cap = StrokeCap.Round
    )
    drawLine(
        color = color.copy(alpha = color.alpha * 0.85f),
        start = Offset(center.x - diag, center.y + diag),
        end = Offset(center.x + diag, center.y - diag),
        strokeWidth = minorStroke,
        cap = StrokeCap.Round
    )
    // Glowing core
    drawCircle(
        color = coreColor,
        radius = (majorRadius * 0.18f).coerceAtLeast(1.2f),
        center = center
    )
}
