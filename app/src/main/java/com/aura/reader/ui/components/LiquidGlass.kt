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
 * Authentic Apple / Mornye-inspired Liquid Glass Surface.
 * Default transparency is high (~25-30% opacity) for a genuine frosted glass look.
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
    // Translucent glass fill: high transparency so content behind is clearly visible
    val baseFill = tintColor ?: if (isDark) {
        Color(0x3D101116) // ~24% opacity frosted dark obsidian
    } else {
        Color(0x55FFFFFF) // ~33% opacity frosted light glass
    }

    // Top-to-bottom optical refraction sheen
    val surfaceSheen = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = if (isDark) 0.08f * highlightIntensity else 0.14f * highlightIntensity),
            Color.Transparent,
            Color.Black.copy(alpha = if (isDark) 0.06f else 0.02f)
        )
    )

    // Specular highlight rim border (Mornye 5-stop lighting gradient)
    val specularRim = Brush.verticalGradient(
        0.00f to Color.White.copy(alpha = if (isDark) 0.65f * highlightIntensity else 0.88f * highlightIntensity),
        0.14f to Color.White.copy(alpha = if (isDark) 0.22f * highlightIntensity else 0.35f * highlightIntensity),
        0.50f to Color.Transparent,
        0.86f to Color.White.copy(alpha = if (isDark) 0.12f * highlightIntensity else 0.20f * highlightIntensity),
        1.00f to Color.White.copy(alpha = if (isDark) 0.38f * highlightIntensity else 0.50f * highlightIntensity)
    )

    val shadowAlpha = if (isDark) 0.35f else 0.12f

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
                // Contact edge
                drawOutline(
                    outline = outline,
                    color = Color.Black.copy(alpha = if (isDark) 0.40f else 0.10f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 0.6.dp.toPx())
                )
                // Specular rim
                drawOutline(
                    outline = outline,
                    brush = specularRim,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.0.dp.toPx())
                )
            }
    ) {
        content()
    }
}

/**
 * Floating Navigation Island dock.
 * In [AppDesignStyle.LIQUID_GLASS]:
 * Matches Mornye's floating dock with animated sliding active selection lens ([MornyeSelectionPill]).
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
        // LIQUID GLASS (Apple / Mornye) STYLE
        val density = LocalDensity.current
        val haptic = LocalHapticFeedback.current

        LiquidGlassSurface(
            shape = RoundedCornerShape(32.dp),
            isDark = isDark,
            shadowElevation = 18.dp,
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
                    label = "mornyeLensSlide"
                )

                // Lens dynamic swelling scale when pressed / dragged (liquid physics)
                val lensScaleX by animateFloatAsState(
                    targetValue = if (isDragging) 1.08f else 1.0f,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label = "lensScaleX"
                )
                val lensScaleY by animateFloatAsState(
                    targetValue = if (isDragging) 1.06f else 1.0f,
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
                    // The Mornye Sliding Active Pill Lens with crisp white specular outline & glow
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
                                if (isDark) Color(0x38FFFFFF) else Color(0x45000000)
                            )
                            .drawBehind {
                                val outline = RoundedCornerShape(26.dp).createOutline(size, layoutDirection, this)
                                drawOutline(
                                    outline = outline,
                                    brush = Brush.verticalGradient(
                                        listOf(
                                            Color.White.copy(alpha = if (isDark) 0.88f else 0.95f),
                                            Color.White.copy(alpha = 0.25f)
                                        )
                                    ),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(1.2.dp.toPx())
                                )
                            }
                    )

                    // Proximity calculation for continuous optical refraction over tabs
                    val currentLensCenterPx = animatedLensOffsetPx + tabWidthPx / 2f
                    fun proximityForSlot(slotIndex: Int): Float {
                        val slotCenterPx = (slotIndex + 0.5f) * tabWidthPx
                        val dist = abs(currentLensCenterPx - slotCenterPx)
                        return (1f - (dist / tabWidthPx)).coerceIn(0f, 1f)
                    }

                    val activeColor = MaterialTheme.colorScheme.primary
                    val inactiveColor = if (isDark) Color.White.copy(alpha = 0.80f) else Color.Black.copy(alpha = 0.70f)

                    // The 3 tabs evenly distributed across the dock
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Tab 0: My Books
                        val prox0 = proximityForSlot(0)
                        val scale0 = 1.0f + 0.12f * prox0
                        val color0 = lerp(inactiveColor, activeColor, prox0)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .expressiveClickable(scaleDown = 0.94f, onClick = onMyBooksClick),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.graphicsLayer {
                                    scaleX = scale0
                                    scaleY = scale0
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
                        val prox1 = proximityForSlot(1)
                        val scale1 = 1.0f + 0.12f * prox1
                        val color1 = lerp(inactiveColor, activeColor, prox1)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .expressiveClickable(scaleDown = 0.90f, onClick = onAddBookClick),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.graphicsLayer {
                                    scaleX = scale1
                                    scaleY = scale1
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
                        val prox2 = proximityForSlot(2)
                        val scale2 = 1.0f + 0.12f * prox2
                        val color2 = lerp(inactiveColor, activeColor, prox2)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .expressiveClickable(scaleDown = 0.94f, onClick = onSettingsClick),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.graphicsLayer {
                                    scaleX = scale2
                                    scaleY = scale2
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
 * Frosted Glass Filter Chip matching Mornye's [MornyeFilterChip].
 */
@Composable
fun MornyeFilterChip(
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
                        Color.White.copy(alpha = if (isDark) 0.35f else 0.70f),
                        Color.White.copy(alpha = 0.08f)
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
                .expressiveClickable(onClick = onClick)
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
 * Card surface that adapts between Material 3 container and Mornye Liquid Glass.
 */
@Composable
fun MornyeGlassCard(
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
