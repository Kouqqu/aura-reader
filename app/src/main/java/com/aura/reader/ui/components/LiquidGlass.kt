package com.aura.reader.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawOutline
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
import kotlin.math.abs

/**
 * Authentic Apple iOS-inspired Liquid Glass Surface.
 * Balanced translucent frosted glass with delicate specular rim highlights.
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
    content: @Composable () -> Unit
) {
    // Translucent glass fill: delicate frosted glass background
    val baseFill = tintColor ?: if (isDark) {
        Color(0x2A141824) // ~16% opacity frosted obsidian
    } else {
        Color(0x65FFFFFF) // ~40% opacity frosted light glass
    }

    // Top-to-bottom optical refraction sheen
    val surfaceSheen = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = if (isDark) 0.05f * highlightIntensity else 0.10f * highlightIntensity),
            Color.Transparent,
            Color.Black.copy(alpha = if (isDark) 0.04f else 0.01f)
        )
    )

    // Delicate specular highlight rim border (lighter, softer Apple glass lighting)
    val specularRim = Brush.verticalGradient(
        0.00f to Color.White.copy(alpha = if (isDark) 0.32f * highlightIntensity else 0.45f * highlightIntensity),
        0.15f to Color.White.copy(alpha = if (isDark) 0.12f * highlightIntensity else 0.18f * highlightIntensity),
        0.50f to Color.Transparent,
        0.85f to Color.White.copy(alpha = if (isDark) 0.04f * highlightIntensity else 0.06f * highlightIntensity),
        1.00f to Color.White.copy(alpha = if (isDark) 0.18f * highlightIntensity else 0.25f * highlightIntensity)
    )

    val shadowAlpha = if (isDark) 0.30f else 0.10f

    Box(
        modifier = modifier
            .drawBehind {
                val outline = shape.createOutline(size, layoutDirection, this)
                // Soft diffused ambient drop shadow
                drawIntoCanvas { canvas ->
                    val paint = Paint().apply {
                        asFrameworkPaint().apply {
                            color = android.graphics.Color.TRANSPARENT
                            setShadowLayer(
                                shadowElevation.toPx(),
                                0f,
                                (shadowElevation / 2.5f).toPx(),
                                android.graphics.Color.argb(
                                    (shadowAlpha * 255).toInt(),
                                    0,
                                    0,
                                    0
                                )
                            )
                        }
                    }
                    canvas.drawOutline(outline, paint)
                }
            }
            .clip(shape)
            .background(baseFill)
            .background(surfaceSheen)
            .drawBehind {
                val outline = shape.createOutline(size, layoutDirection, this)
                // Delicate specular rim
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
 * iOS-inspired floating dock with animated sliding active selection lens and optical refraction.
 * In [AppDesignStyle.MATERIAL]:
 * Material 3 Expressive pill container.
 */
@Composable
fun FloatingNavigationIsland(
    designStyle: AppDesignStyle,
    selectedTab: Int, // 0 = Books, 1 = Add, 2 = Settings
    onMyBooksClick: () -> Unit,
    onAddBookClick: () -> Unit,
    onSettingsClick: () -> Unit,
    clarity: Float = 0.85f,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val isDark = isSystemInDarkTheme()
    val isGlass = designStyle == AppDesignStyle.LIQUID_GLASS

    if (isGlass) {
        // LIQUID GLASS (Apple iOS) STYLE
        val density = LocalDensity.current
        val haptic = LocalHapticFeedback.current

        LiquidGlassSurface(
            shape = RoundedCornerShape(32.dp),
            isDark = isDark,
            shadowElevation = 18.dp,
            tintColor = if (isDark) Color(0xD012151D) else Color(0xEEFFFFFF),
            modifier = modifier
                .height(64.dp)
                .width(330.dp)
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 6.dp)
            ) {
                val tabWidth = maxWidth / 3
                val tabWidthPx = with(density) { tabWidth.toPx() }
                val maxDragXPx = tabWidthPx * 2f

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

                // Lens dynamic swelling scale when pressed / dragged (liquid bubble physics)
                val lensScaleX by animateFloatAsState(
                    targetValue = if (isDragging) 1.15f else 1.0f,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label = "lensScaleX"
                )
                val lensScaleY by animateFloatAsState(
                    targetValue = if (isDragging) 1.10f else 1.0f,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label = "lensScaleY"
                )

                // Drag gesture detection across the entire floating dock
                Box(
                    modifier = Modifier
                        .fillMaxSize()
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
                                        1 -> onAddBookClick()
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
                    // The Sliding Active Pill Lens with subtle specular outline & translucent refractive body
                    Box(
                        modifier = Modifier
                            .offset(x = with(density) { animatedLensOffsetPx.toDp() })
                            .width(tabWidth)
                            .height(52.dp)
                            .padding(horizontal = 4.dp)
                            .graphicsLayer {
                                scaleX = lensScaleX
                                scaleY = lensScaleY
                            }
                            .clip(RoundedCornerShape(26.dp))
                            .background(
                                if (isDark) Color(0x28FFFFFF) else Color(0x35000000)
                            )
                            .drawBehind {
                                val outline = RoundedCornerShape(26.dp).createOutline(size, layoutDirection, this)
                                drawOutline(
                                    outline = outline,
                                    brush = Brush.verticalGradient(
                                        listOf(
                                            Color.White.copy(alpha = if (isDark) 0.40f else 0.55f),
                                            Color.White.copy(alpha = 0.08f)
                                        )
                                    ),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(0.9.dp.toPx())
                                )
                            }
                    )

                    // Continuous optical refraction calculation over tabs (cosine bell curve magnification + deflection)
                    val currentLensCenterPx = animatedLensOffsetPx + tabWidthPx / 2f
                    fun opticalRefractionForSlot(slotIndex: Int): Triple<Float, Float, Float> {
                        val slotCenterPx = (slotIndex + 0.5f) * tabWidthPx
                        val dist = abs(currentLensCenterPx - slotCenterPx)
                        val norm = (dist / tabWidthPx).coerceIn(0f, 1f)
                        // Smooth cosine bell curve
                        val prox = (0.5f * (1f + kotlin.math.cos(norm * Math.PI.toFloat()))).toFloat()
                        // Snell's law optical deflection towards center of lens
                        val shiftX = ((slotCenterPx - currentLensCenterPx) * 0.08f * prox)
                        // Dynamic convex magnification
                        val scale = 1.0f + 0.16f * prox
                        return Triple(prox, scale, shiftX)
                    }

                    val activeColor = MaterialTheme.colorScheme.primary
                    val inactiveColor = if (isDark) Color.White.copy(alpha = 0.80f) else Color.Black.copy(alpha = 0.70f)

                    // The 3 tabs evenly distributed across the dock
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Tab 0: My Books
                        val (prox0, scale0, shiftX0) = opticalRefractionForSlot(0)
                        val color0 = lerp(inactiveColor, activeColor, prox0)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(26.dp))
                                .expressiveClickable(scaleDown = 0.92f, onClick = onMyBooksClick),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.graphicsLayer {
                                    scaleX = scale0
                                    scaleY = scale0
                                    translationX = shiftX0
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = strings.navMyBooks,
                                    tint = color0,
                                    modifier = Modifier.size(23.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = strings.navMyBooks,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    fontWeight = if (prox0 > 0.5f) FontWeight.Bold else FontWeight.Medium,
                                    color = color0,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Tab 1: Add Book
                        val (prox1, scale1, shiftX1) = opticalRefractionForSlot(1)
                        val color1 = lerp(inactiveColor, activeColor, prox1)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(26.dp))
                                .expressiveClickable(scaleDown = 0.90f, onClick = onAddBookClick),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.graphicsLayer {
                                    scaleX = scale1
                                    scaleY = scale1
                                    translationX = shiftX1
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = strings.navAddBook,
                                    tint = color1,
                                    modifier = Modifier.size(25.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = strings.navAddBook,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    fontWeight = if (prox1 > 0.5f) FontWeight.Bold else FontWeight.Medium,
                                    color = color1,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Tab 2: Settings
                        val (prox2, scale2, shiftX2) = opticalRefractionForSlot(2)
                        val color2 = lerp(inactiveColor, activeColor, prox2)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(26.dp))
                                .expressiveClickable(scaleDown = 0.92f, onClick = onSettingsClick),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.graphicsLayer {
                                    scaleX = scale2
                                    scaleY = scale2
                                    translationX = shiftX2
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = strings.settingsTitle,
                                    tint = color2,
                                    modifier = Modifier.size(23.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = strings.settingsTitle,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    fontWeight = if (prox2 > 0.5f) FontWeight.Bold else FontWeight.Medium,
                                    color = color2,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        // MATERIAL 3 EXPRESSIVE STYLE
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

                // Tab 1: Add (+)
                Surface(
                    onClick = onAddBookClick,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .size(46.dp)
                        .expressiveClickable(scaleDown = 0.90f, onClick = onAddBookClick)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = strings.navAddBook,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
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
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    val isDark = isSystemInDarkTheme()

    if (isGlass) {
        val bg = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
        } else {
            Color.White.copy(alpha = if (isDark) 0.08f else 0.45f)
        }

        val primaryColor = MaterialTheme.colorScheme.primary
        val border = if (selected) {
            BorderStroke(1.2.dp, primaryColor)
        } else {
            BorderStroke(
                0.8.dp,
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = if (isDark) 0.28f else 0.50f),
                        Color.White.copy(alpha = 0.06f)
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
    clarity: Float = 0.85f,
    shape: Shape = RoundedCornerShape(24.dp),
    content: @Composable () -> Unit
) {
    if (isGlass) {
        LiquidGlassSurface(
            modifier = modifier,
            shape = shape,
            clarity = clarity,
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
