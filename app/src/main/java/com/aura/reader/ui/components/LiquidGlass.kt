package com.aura.reader.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.reader.data.model.AppDesignStyle
import com.aura.reader.ui.theme.LocalAppStrings
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.HazeMaterials
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * CompositionLocal for ambient Haze backdrop blur across Liquid Glass surfaces.
 */
val LocalHazeState = compositionLocalOf<HazeState?> { null }

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

    val isAmoled = isDark && MaterialTheme.colorScheme.background == Color.Black
    val baseFill = tintColor ?: if (isDark) {
        if (isAmoled) Color(0xF2121316) else Color(0xEE181A20)
    } else {
        Color(0xF5F6F7FA)
    }

    val borderColor = if (isDark) {
        Color.White.copy(alpha = 0.06f * highlightIntensity)
    } else {
        Color.Black.copy(alpha = 0.05f * highlightIntensity)
    }

    val effectiveElevation = if (isDark) 0.dp else shadowElevation.coerceAtMost(8.dp)

    Box(
        modifier = modifier
            .then(
                if (effectiveElevation > 0.dp) {
                    Modifier.shadow(
                        elevation = effectiveElevation,
                        shape = shape,
                        clip = false,
                        ambientColor = Color.Black.copy(alpha = 0.06f),
                        spotColor = Color.Black.copy(alpha = 0.08f)
                    )
                } else Modifier
            )
            .clip(shape)
            .then(hazeModifier)
            .background(baseFill)
            .border(
                width = 0.5.dp,
                color = borderColor,
                shape = shape
            )
    ) {
        val defaultContentColor = if (isDark) Color(0xFFF6F7F9) else Color(0xFF191B1F)
        CompositionLocalProvider(LocalContentColor provides defaultContentColor) {
            content()
        }
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
                .width(294.dp)
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
                    .padding(horizontal = 4.dp, vertical = 3.dp)
            ) {
                val tabWidth = maxWidth / 3
                val tabWidthPx = with(density) { tabWidth.toPx() }
                val maxDragXPx = tabWidthPx * 2f

                var isPressed by remember { mutableStateOf(false) }
                var isInteracting by remember { mutableStateOf(false) }
                var isDragging by remember { mutableStateOf(false) }
                var dragOffsetPx by remember { mutableFloatStateOf(0f) }

                // The resting target offset according to selectedTab
                val activeSlot = selectedTab.coerceIn(0, 2)
                val restingOffsetPx = tabWidthPx * activeSlot

                val animatedLensOffsetPx by animateFloatAsState(
                    targetValue = if (isInteracting) dragOffsetPx else restingOffsetPx,
                    animationSpec = if (isInteracting) {
                        spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = 500f
                        )
                    } else {
                        spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = 380f
                        )
                    },
                    label = "liquidLensSlide"
                )

                // Tactile pill expansion:
                // - Full hold (1.0f) while dragging
                // - Snappy, critically damped response on tap: smooth swell and clean return without edge bounce
                val isHolding = isDragging || isPressed
                val targetExpansion = if (isDragging) 1.0f else if (isPressed) 0.85f else 0.0f
                val expansionProgress by animateFloatAsState(
                    targetValue = targetExpansion,
                    animationSpec = if (isHolding) {
                        spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = 900f
                        )
                    } else {
                        spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = 700f
                        )
                    },
                    label = "pillExpansion"
                )

                // Refraction optics:
                // - Active strictly while dragging (1.0f) where it scans smoothly across icons
                // - Completely off during tap (0.0f) so symbols NEVER jump or warp on click!
                val targetInteraction = if (isDragging) 1.0f else 0.0f
                val interactionStrength by animateFloatAsState(
                    targetValue = targetInteraction,
                    animationSpec = tween(durationMillis = 150),
                    label = "interactionStrength"
                )

                // Physical Pill dimensions:
                // Expanded generously for comfortable symbol area (no "symbols on the edge")
                val basePillWidthPx = tabWidthPx - with(density) { 2.dp.toPx() }
                val currentPillWidthPx = basePillWidthPx * (1.0f + 0.10f * expansionProgress)

                // Vertically: at rest 46dp, on press expands to fill the entire 54dp container height (zero gap!)
                val containerHeightPx = with(density) { 54.dp.toPx() }
                val restingPillHeightPx = with(density) { 46.dp.toPx() }
                val currentPillHeightPx = restingPillHeightPx + (containerHeightPx - restingPillHeightPx) * expansionProgress

                val pillCenterXPx = animatedLensOffsetPx + tabWidthPx / 2f
                val pillCenterYPx = containerHeightPx / 2f

                val pillLeftPx = pillCenterXPx - currentPillWidthPx / 2f
                val pillRightPx = pillCenterXPx + currentPillWidthPx / 2f
                val pillTopPx = pillCenterYPx - currentPillHeightPx / 2f
                val pillBottomPx = pillCenterYPx + currentPillHeightPx / 2f
                val pillRadiusPx = currentPillHeightPx / 2f
                val pillRadiusDp = with(density) { pillRadiusPx.toDp() }

                val activeColor = MaterialTheme.colorScheme.primary
                val inactiveColor = if (isDark) Color.White.copy(alpha = 0.45f) else Color(0x8A1C1D24)

                // Seamless Fluid Kinematics:
                // - Touch/Hold anywhere: pill glides immediately and smoothly under the finger without teleporting.
                // - Dragging: follows finger smoothly across the dock with haptic feedback when crossing slots.
                // - Release (UP): snaps cleanly to the nearest button slot and switches page ONLY on release!
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(tabWidthPx, maxDragXPx) {
                            val touchSlop = viewConfiguration.touchSlop
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                val startX = down.position.x
                                var currentX = startX
                                var hasDragged = false
                                var lastHapticSlot = (startX / tabWidthPx).toInt().coerceIn(0, 2)

                                isPressed = true
                                isInteracting = true
                                dragOffsetPx = (startX - tabWidthPx / 2f).coerceIn(0f, maxDragXPx)

                                val pointerId = down.id
                                var isSuccessRelease = false

                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                                    if (change.isConsumed) break
                                    if (!change.pressed) {
                                        change.consume()
                                        isSuccessRelease = true
                                        break
                                    }
                                    currentX = change.position.x
                                    val dx = kotlin.math.abs(currentX - startX)
                                    val holdDuration = change.uptimeMillis - down.uptimeMillis
                                    if (!hasDragged && (dx > touchSlop || holdDuration > 150)) {
                                        hasDragged = true
                                        isDragging = true
                                    }
                                    dragOffsetPx = (currentX - tabWidthPx / 2f).coerceIn(0f, maxDragXPx)

                                    val hoverSlot = (currentX / tabWidthPx).toInt().coerceIn(0, 2)
                                    if (hasDragged && hoverSlot != lastHapticSlot) {
                                        lastHapticSlot = hoverSlot
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                    change.consume()
                                }

                                isPressed = false
                                isInteracting = false
                                isDragging = false

                                if (isSuccessRelease) {
                                    val nearestSlot = ((dragOffsetPx + tabWidthPx / 2f) / tabWidthPx).toInt().coerceIn(0, 2)
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    when (nearestSlot) {
                                        0 -> onMyBooksClick()
                                        1 -> onSearchClick()
                                        2 -> onSettingsClick()
                                    }
                                }
                            }
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
                    // Deeply transparent, neutral, calm glass without milky glow:
                    val pillBgColor = if (isDark) {
                        if (isAmoled) Color(0x10FFFFFF) else Color(0x0EFFFFFF)
                    } else {
                        Color(0x08000000)
                    }

                    // Single-tone uniform border (no gradient, calm glass outline):
                    val borderAlpha = if (isDark) {
                        0.05f + 0.22f * expansionProgress
                    } else {
                        0.04f + 0.16f * expansionProgress
                    }
                    val pillBorderColor = if (isDark) {
                        Color.White.copy(alpha = borderAlpha)
                    } else {
                        Color.Black.copy(alpha = borderAlpha)
                    }
                    val borderWidthDp = (0.5f + 0.4f * expansionProgress).dp

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
                                width = borderWidthDp,
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
                                refractiveIndex = 1.018f,
                                refractionWidthPx = with(density) { 18.dp.toPx() },
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
/**
 * Circular Liquid Glass action button (+ icon) positioned next to the navigation island.
 * Styled with identical glass material DNA as the navigation dock and capsule pill.
 * A centered, bounded press response keeps the plus inside its glass circle.
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

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val containerScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 650f),
        label = "addButtonScale"
    )
    val expansionProgress by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 650f),
        label = "addButtonPress"
    )

    // Exact same dock glass styling as FloatingNavigationIsland
    val dockBgColor = if (isDark) {
        if (isAmoled) Color(0xF2121316) else Color(0xEE181A20)
    } else {
        Color(0xF4F5F6FA)
    }
    val dockBorderColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.05f)

    // Exact same inner pill styling as the sliding tablet in the dock
    val pillBgColor = if (isDark) {
        if (isAmoled) Color(0x10FFFFFF) else Color(0x0EFFFFFF)
    } else {
        Color(0x08000000)
    }
    val borderAlpha = if (isDark) {
        0.05f + 0.22f * expansionProgress
    } else {
        0.04f + 0.16f * expansionProgress
    }
    val pillBorderColor = if (isDark) {
        Color.White.copy(alpha = borderAlpha)
    } else {
        Color.Black.copy(alpha = borderAlpha)
    }
    val borderWidthDp = (0.5f + 0.4f * expansionProgress).dp

    // Outer anchored glass vessel (matches 60dp height of FloatingNavigationIsland)
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
            .size(60.dp)
            .graphicsLayer {
                scaleX = containerScale
                scaleY = containerScale
            }
            .clip(shape)
            .then(hazeModifier)
            .background(dockBgColor)
            .border(
                width = 0.6.dp,
                color = dockBorderColor,
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        // A centered press response always fits inside the 60dp glass circle.
        val pillSize = (46 + 6 * expansionProgress).dp
        Box(
            modifier = Modifier
                .size(pillSize)
                .clip(CircleShape)
                .background(pillBgColor)
                .border(
                    width = borderWidthDp,
                    color = pillBorderColor,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = LocalAppStrings.current.addBooks,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(26.dp)
            )
        }
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
        val primaryColor = MaterialTheme.colorScheme.primary
        val bg = if (selected) {
            primaryColor.copy(alpha = if (isDark) 0.22f else 0.16f)
        } else {
            if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f)
        }
        val borderColor = if (selected) {
            primaryColor.copy(alpha = 0.65f)
        } else {
            if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.05f)
        }

        Box(
            modifier = modifier
                .height(34.dp)
                .clip(CircleShape)
                .background(bg)
                .border(
                    width = 0.5.dp,
                    color = borderColor,
                    shape = CircleShape
                )
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
