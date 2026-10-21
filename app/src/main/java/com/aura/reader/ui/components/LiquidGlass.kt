package com.aura.reader.ui.components

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.reader.data.model.AppDesignStyle
import com.aura.reader.ui.theme.LocalAppStrings
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.HazeMaterials
import kotlin.math.abs

/**
 * CompositionLocal for ambient Haze backdrop blur across Liquid Glass surfaces.
 */
val LocalHazeState = compositionLocalOf<HazeState?> { null }

// AGSL Refraction and Convex Optical Bevel Shader for Android 13+ (API 33+)
private const val LIQUID_REFRACTION_AGSL = """
    uniform shader composable;
    uniform float2 uCenter;
    uniform float2 uHalfSize;
    uniform float uRadius;
    uniform float uRefractionDepth;
    uniform float uRefractionWidth;
    uniform float uDispersion;
    uniform float uIntensity;

    half4 main(float2 fragCoord) {
        if (uIntensity <= 0.005) {
            return composable.eval(fragCoord);
        }

        float2 p = fragCoord - uCenter;
        
        // Medial horizontal spine of the capsule lens
        float capLimit = max(0.0, uHalfSize.x - uRadius);
        float2 spinePt = float2(clamp(p.x, -capLimit, capLimit), 0.0);
        float2 delta = p - spinePt;
        float dist = length(delta);
        
        // Signed distance to capsule boundary (negative inside)
        float d = dist - uRadius;
        if (d > 0.0) {
            return composable.eval(fragCoord);
        }
        
        // Curvature and refraction profile across bevel
        float distInside = -d;
        float bevelWidth = max(1.0, min(uRefractionWidth, uRadius));
        float t = clamp(distInside / bevelWidth, 0.0, 1.0);
        
        // Smooth C1-continuous falloff (zero discontinuity, zero inner oval ring!)
        float falloff = (1.0 - t) * (1.0 - t);
        float slope = falloff * uRefractionDepth * uIntensity;
        float2 normal = (dist > 0.001) ? (delta / dist) : float2(0.0, 0.0);
        float2 offset = -normal * (slope * bevelWidth);
        
        // Pure, crystal sharp evaluation without chromatic blurring
        return composable.eval(fragCoord + offset);
    }
"""

fun Modifier.liquidRefraction(
    centerPx: Offset,
    sizePx: Size,
    radiusPx: Float,
    refractionDepth: Float = 0.22f,
    refractionWidthPx: Float = 24f,
    dispersion: Float = 1.25f,
    intensity: Float = 1.0f
): Modifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && intensity > 0.01f) {
    this.graphicsLayer {
        val halfW = (sizePx.width * 0.5f).coerceAtLeast(radiusPx)
        val halfH = (sizePx.height * 0.5f).coerceAtLeast(radiusPx)
        try {
            val shader = RuntimeShader(LIQUID_REFRACTION_AGSL).apply {
                setFloatUniform("uCenter", centerPx.x, centerPx.y)
                setFloatUniform("uHalfSize", halfW, halfH)
                setFloatUniform("uRadius", radiusPx)
                setFloatUniform("uRefractionDepth", refractionDepth)
                setFloatUniform("uRefractionWidth", refractionWidthPx)
                setFloatUniform("uDispersion", dispersion)
                setFloatUniform("uIntensity", intensity)
            }
            renderEffect = RenderEffect.createRuntimeShaderEffect(shader, "composable").asComposeRenderEffect()
        } catch (e: Exception) {
            android.util.Log.e("LiquidGlass", "AGSL compilation failed", e)
        }
    }
} else {
    this
}

/**
 * Authentic Apple iOS-inspired Liquid Glass Surface.
 * Balanced translucent frosted glass with delicate specular rim highlights and optional Haze blur.
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    clarity: Float = 0.85f,
    isDark: Boolean = isSystemInDarkTheme(),
    highlightIntensity: Float = 1.0f,
    tintColor: Color? = null,
    shadowElevation: Dp = 16.dp,
    hazeState: HazeState? = null,
    content: @Composable () -> Unit
) {
    val hazeModifier = if (hazeState != null) {
        Modifier.hazeChild(
            state = hazeState,
            shape = shape,
            style = if (isDark) HazeMaterials.ultraThin() else HazeMaterials.thin()
        )
    } else Modifier

    val baseFill = tintColor ?: if (isDark) {
        Color(0x22FFFFFF)
    } else {
        Color(0x70FFFFFF)
    }

    val surfaceSheen = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = if (isDark) 0.08f * highlightIntensity else 0.16f * highlightIntensity),
            Color.Transparent,
            Color.Black.copy(alpha = if (isDark) 0.02f else 0.01f)
        )
    )

    val specularRim = Brush.verticalGradient(
        0.00f to Color.White.copy(alpha = if (isDark) 0.45f * highlightIntensity else 0.65f * highlightIntensity),
        0.18f to Color.White.copy(alpha = if (isDark) 0.15f * highlightIntensity else 0.22f * highlightIntensity),
        0.50f to Color.Transparent,
        0.85f to Color.White.copy(alpha = if (isDark) 0.06f * highlightIntensity else 0.08f * highlightIntensity),
        1.00f to Color.White.copy(alpha = if (isDark) 0.22f * highlightIntensity else 0.35f * highlightIntensity)
    )

    val effectiveElevation = if (isDark) 0.dp else shadowElevation.coerceAtMost(8.dp)

    Box(
        modifier = modifier
            .then(
                if (effectiveElevation > 0.dp) {
                    Modifier.shadow(
                        elevation = effectiveElevation,
                        shape = shape,
                        clip = false,
                        ambientColor = Color.Black.copy(alpha = 0.08f),
                        spotColor = Color.Black.copy(alpha = 0.12f)
                    )
                } else Modifier
            )
            .clip(shape)
            .then(hazeModifier)
            .background(baseFill)
            .background(surfaceSheen)
            .drawBehind {
                val outline = shape.createOutline(size, layoutDirection, this)
                drawOutline(
                    outline = outline,
                    brush = specularRim,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 0.85.dp.toPx())
                )
            }
    ) {
        content()
    }
}

/**
 * Floating Navigation Island dock.
 * In [AppDesignStyle.LIQUID_GLASS]:
 * Authentic visionOS / Mornye optical liquid dock with:
 *  - Real-time spatial color masking (dual-layer slice).
 *  - Dynamic liquid droplet swelling on drag / slide.
 *  - Snell's convex bevel refraction shader.
 * In [AppDesignStyle.MATERIAL]:
 * Material 3 Expressive pill container.
 */
@Composable
fun FloatingNavigationIsland(
    designStyle: AppDesignStyle,
    selectedTab: Int, // 0 = Books, 1 = Search, 2 = Settings
    onMyBooksClick: () -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    isDark: Boolean = false,
    clarity: Float = 0.85f,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val isGlass = designStyle == AppDesignStyle.LIQUID_GLASS

    if (isGlass) {
        // LIQUID GLASS (Apple iOS / Mornye) STYLE
        val density = LocalDensity.current
        val haptic = LocalHapticFeedback.current
        val isAmoled = isDark && MaterialTheme.colorScheme.background == Color.Black

        val dockShape = RoundedCornerShape(30.dp)
        val hazeState = LocalHazeState.current
        val hazeModifier = if (hazeState != null) {
            Modifier.hazeChild(
                state = hazeState,
                shape = dockShape,
                style = if (isDark) HazeMaterials.ultraThin() else HazeMaterials.thin()
            )
        } else Modifier

        val dockBgColor = if (isDark) {
            if (isAmoled) Color(0xF2121316) else Color(0xEE181A20)
        } else {
            Color(0xF4F5F6FA)
        }
        val dockBorderColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.05f)

        Box(
            modifier = modifier
                .then(
                    if (!isDark) {
                        Modifier.shadow(
                            elevation = 8.dp,
                            shape = dockShape,
                            clip = false,
                            ambientColor = Color.Black.copy(alpha = 0.06f),
                            spotColor = Color.Black.copy(alpha = 0.08f)
                        )
                    } else Modifier
                )
                .height(60.dp)
                .width(276.dp)
                .clip(dockShape)
                .then(hazeModifier)
                .background(dockBgColor)
                .border(
                    width = 0.6.dp,
                    color = dockBorderColor,
                    shape = dockShape
                )
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 6.dp)
            ) {
                val tabWidth = maxWidth / 3
                val tabWidthPx = with(density) { tabWidth.toPx() }
                val maxDragXPx = tabWidthPx * 2f

                var isPressed by remember { mutableStateOf(false) }
                var isDragging by remember { mutableStateOf(false) }
                var dragOffsetPx by remember { mutableFloatStateOf(0f) }

                // The resting target offset according to selectedTab
                val activeSlot = selectedTab.coerceIn(0, 2)
                val restingOffsetPx = tabWidthPx * activeSlot

                val animatedLensOffsetPx by animateFloatAsState(
                    targetValue = if (isDragging) dragOffsetPx else restingOffsetPx,
                    animationSpec = if (isDragging) {
                        spring(stiffness = Spring.StiffnessHigh)
                    } else {
                        spring(
                            dampingRatio = 0.78f,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    },
                    label = "liquidLensSlide"
                )

                // Smooth tactile pill expansion on press & slide
                val isHolding = isPressed || isDragging
                val offsetDelta = abs(animatedLensOffsetPx - restingOffsetPx)
                val isSliding = offsetDelta > 8f

                val targetExpansion = if (isHolding) 1.0f else if (isSliding) 0.5f else 0.0f
                val expansionProgress by animateFloatAsState(
                    targetValue = targetExpansion,
                    animationSpec = spring(
                        dampingRatio = 0.72f,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "pillExpansion"
                )

                val lensScaleX = 1.0f + 0.16f * expansionProgress
                val lensScaleY = 1.0f + 0.10f * expansionProgress

                val targetInteraction = if (isHolding || offsetDelta > 4f) 1.0f else 0.0f
                val interactionStrength by animateFloatAsState(
                    targetValue = targetInteraction,
                    animationSpec = tween(durationMillis = 180),
                    label = "interactionStrength"
                )

                // Physical Pill dimensions and positions
                val basePillWidthPx = tabWidthPx - with(density) { 6.dp.toPx() }
                val currentPillWidthPx = basePillWidthPx * lensScaleX
                val basePillHeightPx = with(density) { 44.dp.toPx() }
                val currentPillHeightPx = basePillHeightPx * lensScaleY

                val pillCenterXPx = animatedLensOffsetPx + tabWidthPx / 2f
                val containerHeightPx = with(density) { 48.dp.toPx() }
                val pillCenterYPx = containerHeightPx / 2f

                val pillLeftPx = pillCenterXPx - currentPillWidthPx / 2f
                val pillRightPx = pillCenterXPx + currentPillWidthPx / 2f
                val pillTopPx = pillCenterYPx - currentPillHeightPx / 2f
                val pillBottomPx = pillCenterYPx + currentPillHeightPx / 2f
                val pillRadiusPx = with(density) { 22.dp.toPx() } * lensScaleY
                val pillRadiusDp = with(density) { pillRadiusPx.toDp() }

                val activeColor = MaterialTheme.colorScheme.primary
                val inactiveColor = if (isDark) Color.White.copy(alpha = 0.45f) else Color(0x8A1C1D24)

                // Drag gesture and Tap detection across the dock
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(tabWidthPx) {
                            detectTapGestures(
                                onPress = {
                                    isPressed = true
                                    try {
                                        awaitRelease()
                                    } finally {
                                        isPressed = false
                                    }
                                },
                                onTap = { offset ->
                                    val slot = (offset.x / tabWidthPx).toInt().coerceIn(0, 2)
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    when (slot) {
                                        0 -> onMyBooksClick()
                                        1 -> onSearchClick()
                                        2 -> onSettingsClick()
                                    }
                                }
                            )
                        }
                        .pointerInput(tabWidthPx) {
                            detectHorizontalDragGestures(
                                onDragStart = { offset ->
                                    isDragging = true
                                    dragOffsetPx = (offset.x - tabWidthPx / 2f).coerceIn(0f, maxDragXPx)
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                },
                                onDragEnd = {
                                    isDragging = false
                                    val nearestSlot = ((dragOffsetPx + tabWidthPx / 2f) / tabWidthPx).toInt().coerceIn(0, 2)
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    when (nearestSlot) {
                                        0 -> onMyBooksClick()
                                        1 -> onSearchClick()
                                        2 -> onSettingsClick()
                                    }
                                },
                                onDragCancel = {
                                    isDragging = false
                                },
                                onHorizontalDrag = { change, dragAmount ->
                                    change.consume()
                                    dragOffsetPx = (dragOffsetPx + dragAmount).coerceIn(0f, maxDragXPx)
                                }
                            )
                        }
                ) {
                    // --- LAYER 1: Base Inactive Tab Row (rendered ONLY outside the pill, zero double-layer bleed) ---
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawWithContent {
                                val fullPath = Path().apply {
                                    addRect(Rect(0f, 0f, size.width, size.height))
                                }
                                val pillPath = Path().apply {
                                    addRoundRect(
                                        RoundRect(
                                            rect = Rect(pillLeftPx, pillTopPx, pillRightPx, pillBottomPx),
                                            cornerRadius = CornerRadius(pillRadiusPx, pillRadiusPx)
                                        )
                                    )
                                }
                                val outsidePillPath = Path.combine(
                                    operation = PathOperation.Difference,
                                    path1 = fullPath,
                                    path2 = pillPath
                                )
                                clipPath(outsidePillPath) {
                                    this@drawWithContent.drawContent()
                                }
                            }
                    ) {
                        TabDockRow(
                            strings = strings,
                            textColor = inactiveColor,
                            iconColor = inactiveColor,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // --- LAYER 2: Mornye Translucent Grey Capsule Pill ---
                    val pillBgColor = if (isDark) {
                        if (isAmoled) Color(0x28FFFFFF) else Color(0x1EFFFFFF)
                    } else {
                        Color(0x12000000)
                    }
                    val pillBorderColor = if (isDark) {
                        Color.White.copy(alpha = 0.06f)
                    } else {
                        Color.Black.copy(alpha = 0.04f)
                    }

                    Box(
                        modifier = Modifier
                            .offset(
                                x = with(density) { pillLeftPx.toDp() },
                                y = with(density) { pillTopPx.toDp() }
                            )
                            .size(
                                width = with(density) { currentPillWidthPx.toDp() },
                                height = with(density) { currentPillHeightPx.toDp() }
                            )
                            .clip(RoundedCornerShape(pillRadiusDp))
                            .background(pillBgColor)
                            .border(
                                width = 0.5.dp,
                                color = pillBorderColor,
                                shape = RoundedCornerShape(pillRadiusDp)
                            )
                    )

                    // --- LAYER 3: Active Accent Row with SPATIAL COLOR MASK & OPTICS ---
                    // Rendered strictly INSIDE the pill capsule: zero underlying gray text bleed-through!
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            // 1. SPATIAL COLOR MASK: Slice & color strictly inside moving capsule
                            .drawWithContent {
                                val pillPath = Path().apply {
                                    addRoundRect(
                                        RoundRect(
                                            rect = Rect(pillLeftPx, pillTopPx, pillRightPx, pillBottomPx),
                                            cornerRadius = CornerRadius(pillRadiusPx, pillRadiusPx)
                                        )
                                    )
                                }
                                clipPath(pillPath) {
                                    this@drawWithContent.drawContent()
                                }
                            }
                            // 2. OPTICAL CONVEX REFRACTION SHADER (Snell's bevel rim deflection)
                            .liquidRefraction(
                                centerPx = Offset(pillCenterXPx, pillCenterYPx),
                                sizePx = Size(currentPillWidthPx, currentPillHeightPx),
                                radiusPx = pillRadiusPx,
                                refractionDepth = 0.14f,
                                refractionWidthPx = with(density) { 14.dp.toPx() },
                                dispersion = 0.0f,
                                intensity = interactionStrength
                            )
                    ) {
                        TabDockRow(
                            strings = strings,
                            textColor = activeColor,
                            iconColor = activeColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    } else {
        // MATERIAL 3 EXPRESSIVE STYLE FALLBACK
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.96f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
            shadowElevation = 8.dp,
            modifier = modifier
                .height(60.dp)
                .width(320.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Tab 0: Books
                val isBooksActive = selectedTab == 0
                Surface(
                    onClick = onMyBooksClick,
                    shape = CircleShape,
                    color = if (isBooksActive) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else Color.Transparent,
                    modifier = Modifier
                        .height(46.dp)
                        .weight(1f)
                        .expressiveClickable(scaleDown = 0.94f, onClick = onMyBooksClick)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = strings.navMyBooks,
                            tint = if (isBooksActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = strings.navMyBooks,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isBooksActive) FontWeight.Bold else FontWeight.Medium,
                            color = if (isBooksActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Tab 1: Search
                val isSearchActive = selectedTab == 1
                Surface(
                    onClick = onSearchClick,
                    shape = CircleShape,
                    color = if (isSearchActive) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else Color.Transparent,
                    modifier = Modifier
                        .height(46.dp)
                        .weight(1f)
                        .expressiveClickable(scaleDown = 0.90f, onClick = onSearchClick)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = strings.search,
                            tint = if (isSearchActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = strings.search,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSearchActive) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSearchActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Tab 2: Settings
                val isSettingsActive = selectedTab == 2
                Surface(
                    onClick = onSettingsClick,
                    shape = CircleShape,
                    color = if (isSettingsActive) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else Color.Transparent,
                    modifier = Modifier
                        .height(46.dp)
                        .weight(1f)
                        .expressiveClickable(scaleDown = 0.94f, onClick = onSettingsClick)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = strings.settingsTitle,
                            tint = if (isSettingsActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = strings.settingsTitle,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSettingsActive) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSettingsActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TabDockRow(
    strings: com.aura.reader.ui.theme.Strings,
    textColor: Color,
    iconColor: Color,
    fontWeight: FontWeight
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (slotIndex in 0..2) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val icon = when (slotIndex) {
                        0 -> Icons.Default.MenuBook
                        1 -> Icons.Default.Search
                        else -> Icons.Default.Settings
                    }
                    val label = when (slotIndex) {
                        0 -> strings.navMyBooks
                        1 -> strings.search
                        else -> strings.settingsTitle
                    }

                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                        fontWeight = fontWeight,
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Circular Liquid Glass action button (+ icon) positioned next to the navigation island.
 */
@Composable
fun FloatingAddGlassButton(
    onClick: () -> Unit,
    isDark: Boolean = false,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isAmoled = isDark && MaterialTheme.colorScheme.background == Color.Black
    val shape = CircleShape
    val hazeState = LocalHazeState.current
    val hazeModifier = if (hazeState != null) {
        Modifier.hazeChild(
            state = hazeState,
            shape = shape,
            style = if (isDark) HazeMaterials.ultraThin() else HazeMaterials.thin()
        )
    } else Modifier

    val btnBgColor = if (isDark) {
        if (isAmoled) Color(0xF2121316) else Color(0xEE181A20)
    } else {
        Color(0xF4F5F6FA)
    }
    val btnBorderColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.05f)

    Box(
        modifier = modifier
            .then(
                if (!isDark) {
                    Modifier.shadow(
                        elevation = 8.dp,
                        shape = shape,
                        clip = false,
                        ambientColor = Color.Black.copy(alpha = 0.06f),
                        spotColor = Color.Black.copy(alpha = 0.08f)
                    )
                } else Modifier
            )
            .size(54.dp)
            .clip(shape)
            .then(hazeModifier)
            .background(btnBgColor)
            .border(
                width = 0.6.dp,
                color = btnBorderColor,
                shape = shape
            )
            .expressiveClickable(scaleDown = 0.90f) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = LocalAppStrings.current.addBooks,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
    }
}

/**
 * Frosted Glass Filter Chip matching iOS Liquid Glass.
 */
@Composable
fun LiquidGlassFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    isGlass: Boolean = true,
    isDark: Boolean = isSystemInDarkTheme(),
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    if (isGlass) {
        val bg = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.25f else 0.18f)
        } else {
            if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.04f)
        }

        val primaryColor = MaterialTheme.colorScheme.primary
        val border = if (selected) {
            BorderStroke(1.2.dp, primaryColor)
        } else {
            BorderStroke(
                0.8.dp,
                Brush.verticalGradient(
                    listOf(
                        if (isDark) Color.White.copy(alpha = 0.28f) else Color.Black.copy(alpha = 0.08f),
                        if (isDark) Color.White.copy(alpha = 0.06f) else Color.Transparent
                    )
                )
            )
        }

        Box(
            modifier = modifier
                .height(36.dp)
                .clip(CircleShape)
                .background(bg)
                .drawBehind {
                    drawOutline(
                        outline = CircleShape.createOutline(size, layoutDirection, this),
                        brush = if (selected) SolidColor(primaryColor) else border.brush,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(border.width.toPx())
                    )
                }
                .clip(CircleShape)
                .expressiveClickable(scaleDown = 0.94f, onClick = onClick)
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (leadingIcon != null) {
                    leadingIcon()
                }
                label()
                if (trailingIcon != null) {
                    trailingIcon()
                }
            }
        }
    } else {
        androidx.compose.material3.FilterChip(
            selected = selected,
            onClick = onClick,
            label = label,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            shape = RoundedCornerShape(12.dp),
            modifier = modifier
        )
    }
}

/**
 * Card surface that adapts between Material 3 container and iOS Liquid Glass.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    isGlass: Boolean = false,
    isDark: Boolean = isSystemInDarkTheme(),
    clarity: Float = 0.85f,
    shape: Shape = RoundedCornerShape(24.dp),
    content: @Composable () -> Unit
) {
    if (isGlass) {
        LiquidGlassSurface(
            modifier = modifier,
            shape = shape,
            clarity = clarity,
            isDark = isDark,
            content = content
        )
    } else {
        Card(
            modifier = modifier,
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            content()
        }
    }
}
