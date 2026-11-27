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

/**
 * Ambient Christmas visual layer for the Library Top Bar:
 * - WESTERN: gentle sagging fairy lights garland along the top edge + soft drifting snow.
 * - ORTHODOX: quiet starry night sky + pulsing 8-pointed Star of Bethlehem + subtle snow.
 */
@Composable
fun ChristmasTopBarOverlay(
    holidaySeason: HolidaySeason,
    scrollAlpha: Float,
    isThemeDark: Boolean,
    modifier: Modifier = Modifier
) {
    val isEInk = LocalEInkMode.current
    if (isEInk || !holidaySeason.isChristmasSeason() || scrollAlpha <= 0.005f) return

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

    val isWestern = holidaySeason == HolidaySeason.CHRISTMAS_WESTERN
    val snowflakes = remember(holidaySeason) {
        val rng = Random(if (isWestern) 1225 else 107)
        val count = if (isWestern) 18 else 10
        List(count) {
            AmbientSnowflake(
                xFraction = rng.nextFloat(),
                yOffset = rng.nextFloat(),
                radiusDp = 1.2f + rng.nextFloat() * 1.9f,
                speed = if (rng.nextBoolean()) 1f else 2f,
                swayAmplitudeDp = 5f + rng.nextFloat() * 9f,
                swayPhase = rng.nextFloat() * (2f * PI.toFloat()),
                alpha = 0.25f + rng.nextFloat() * 0.45f,
                isStar = rng.nextFloat() < 0.28f
            )
        }
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

        if (isWestern) {
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
        } else {
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

        // 2. Soft drifting snow confined to the top bar, smoothly fading out toward the bottom
        val snowColor = if (isThemeDark) Color.White else Color(0xFF90CAF9)
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
                    drawFourPointStar(Offset(x, y), r * 1.6f, snowColor.copy(alpha = a))
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
    if (!holidaySeason.isChristmasSeason()) return

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        if (holidaySeason == HolidaySeason.CHRISTMAS_WESTERN) {
            // Sleek miniature Santa hat perched on top-right corner
            val hatPath = Path().apply {
                moveTo(w * 0.14f, h * 0.76f)
                quadraticTo(w * 0.34f, h * 0.12f, w * 0.84f, h * 0.24f)
                quadraticTo(w * 0.70f, h * 0.48f, w * 0.86f, h * 0.80f)
                close()
            }
            drawPath(hatPath, color = Color(0xFFD92B2B))

            // Subtle highlight fold on the cap
            val foldPath = Path().apply {
                moveTo(w * 0.24f, h * 0.68f)
                quadraticTo(w * 0.42f, h * 0.26f, w * 0.74f, h * 0.28f)
            }
            drawPath(
                path = foldPath,
                color = Color(0xFFFF6B6B).copy(alpha = 0.55f),
                style = Stroke(width = (if (isSmallTitle) 0.9f else 1.4f).dp.toPx(), cap = StrokeCap.Round)
            )

            // Fluffy white fur trim along the base
            drawLine(
                color = Color(0xFFFAFAFA),
                start = Offset(w * 0.12f, h * 0.78f),
                end = Offset(w * 0.86f, h * 0.82f),
                strokeWidth = (if (isSmallTitle) 2.4f else 3.6f).dp.toPx(),
                cap = StrokeCap.Round
            )

            // Pom-pom at the tilted tip
            drawCircle(
                color = Color(0xFFFFFFFF),
                radius = (if (isSmallTitle) 2.0f else 2.9f).dp.toPx(),
                center = Offset(w * 0.88f, h * 0.30f)
            )
        } else {
            // Orthodox: glowing golden Star of Bethlehem on the corner
            val center = Offset(w * 0.54f, h * 0.48f)
            val gold = Color(0xFFFFD54F)
            drawCircle(
                brush = Brush.radialGradient(
                    0.0f to gold.copy(alpha = 0.55f),
                    1.0f to Color.Transparent,
                    center = center,
                    radius = w * 0.55f
                ),
                radius = w * 0.55f,
                center = center
            )
            drawEightPointStar(
                center = center,
                majorRadius = w * 0.44f,
                minorRadius = w * 0.21f,
                color = gold,
                coreColor = Color.White
            )
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
    if (isEInk || !holidaySeason.isChristmasSeason()) return

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
        val frostColor = if (isDark) Color(0xFFE3F2FD) else Color(0xFFB3E5FC)
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

        // Tiny frost sparkles nestled near the top corners
        val sparkleColor = if (holidaySeason == HolidaySeason.CHRISTMAS_ORTHODOX) {
            Color(0xFFFFE082)
        } else {
            Color.White
        }
        val sparkleAlpha = if (isDark) 0.52f else 0.42f
        drawFourPointStar(Offset(w * 0.14f, 6.dp.toPx()), 3.2.dp.toPx(), sparkleColor.copy(alpha = sparkleAlpha))
        drawFourPointStar(Offset(w * 0.86f, 7.dp.toPx()), 3.6.dp.toPx(), sparkleColor.copy(alpha = sparkleAlpha * 0.9f))
        drawFourPointStar(Offset(w * 0.53f, 5.dp.toPx()), 2.4.dp.toPx(), sparkleColor.copy(alpha = sparkleAlpha * 0.7f))

        // 2. Slow, refined golden light sweep along the card border
        if (shimmerProgress in -0.25f..1.25f) {
            val sweepX = w * shimmerProgress
            val bandHalf = w * 0.22f
            val goldColor = if (holidaySeason == HolidaySeason.CHRISTMAS_ORTHODOX) {
                Color(0xFFFFD54F)
            } else {
                Color(0xFFFFE082)
            }
            val shimmerBrush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    goldColor.copy(alpha = if (isDark) 0.55f else 0.45f),
                    Color.White.copy(alpha = if (isDark) 0.45f else 0.35f),
                    goldColor.copy(alpha = if (isDark) 0.55f else 0.45f),
                    Color.Transparent
                ),
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
    if (isEInk || !holidaySeason.isChristmasSeason()) return

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
    val flakes = remember(holidaySeason) {
        val rng = Random(if (isWestern) 2412 else 7010)
        List(16) {
            AmbientSnowflake(
                xFraction = 0.05f + rng.nextFloat() * 0.90f,
                yOffset = rng.nextFloat(),
                radiusDp = 1.3f + rng.nextFloat() * 2.1f,
                speed = if (it % 2 == 0) 1f else 2f,
                swayAmplitudeDp = 8f + rng.nextFloat() * 12f,
                swayPhase = rng.nextFloat() * (2f * PI.toFloat()),
                alpha = 0.16f + rng.nextFloat() * 0.28f,
                isStar = !isWestern || rng.nextFloat() < 0.35f
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        val center = Offset(w / 2f, h * 0.42f)
        val glowColor = if (isWestern) Color(0xFFFFD54F) else Color(0xFFFFB300)
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

        if (!isWestern) {
            drawEightPointStar(
                center = Offset(w / 2f, h * 0.30f),
                majorRadius = 14.dp.toPx() * pulse,
                minorRadius = 6.dp.toPx(),
                color = glowColor.copy(alpha = if (isDark) 0.45f else 0.35f),
                coreColor = Color.White.copy(alpha = if (isDark) 0.55f else 0.40f)
            )
        }

        val particleColor = if (isDark) {
            if (isWestern) Color.White else Color(0xFFFFE082)
        } else {
            if (isWestern) Color(0xFF64B5F6) else Color(0xFFFFA000)
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
                if (flake.isStar) {
                    drawFourPointStar(Offset(x, y), r * 1.7f, particleColor.copy(alpha = a))
                } else {
                    drawCircle(color = particleColor.copy(alpha = a), radius = r, center = Offset(x, y))
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
        val palette = if (holidaySeason == HolidaySeason.CHRISTMAS_ORTHODOX) {
            listOf(
                Color.White,
                Color(0xFFFFE082),
                Color(0xFFFFB300),
                Color(0xFFB3E5FC),
                Color(0xFFFFF9C4)
            )
        } else {
            listOf(
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
