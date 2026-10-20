package com.aura.reader.ui.components

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.reader.data.model.AppDesignStyle
import com.aura.reader.ui.theme.LocalAppStrings

/**
 * Authentic Apple / Mornye-inspired Liquid Glass Surface.
 * Features:
 * - Multi-layered diffused ambient drop shadow
 * - Translucent frosted base with adjustable glass clarity
 * - Top-to-bottom optical refraction sheen
 * - Dual-layer specular edge rim ([_MornyeGlassRim] matching mornyeapp/Mornye):
 *   hairline dark contact edge + 5-stop illumination gradient reflection.
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
    val clampedClarity = clarity.coerceIn(0.2f, 1.0f)

    // Base glass translucent fill (translucent obsidian in dark, frosted ice in light)
    val baseFill = tintColor ?: if (isDark) {
        Color(0xFF14151B).copy(alpha = (0.50f + (1f - clampedClarity) * 0.35f))
    } else {
        Color(0xFFF6F7FA).copy(alpha = (0.65f + (1f - clampedClarity) * 0.25f))
    }

    // Top-to-bottom surface sheen (optical refraction)
    val surfaceSheen = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = if (isDark) 0.08f * highlightIntensity else 0.16f * highlightIntensity),
            Color.Transparent,
            Color.Black.copy(alpha = if (isDark) 0.08f else 0.03f)
        )
    )

    // Dual-pass specular rim border (Mornye 5-stop optical rim gradient)
    val specularRim = Brush.verticalGradient(
        0.00f to Color.White.copy(alpha = if (isDark) 0.65f * highlightIntensity else 0.90f * highlightIntensity),
        0.14f to Color.White.copy(alpha = if (isDark) 0.22f * highlightIntensity else 0.38f * highlightIntensity),
        0.50f to Color.Transparent,
        0.86f to Color.White.copy(alpha = if (isDark) 0.12f * highlightIntensity else 0.22f * highlightIntensity),
        1.00f to Color.White.copy(alpha = if (isDark) 0.38f * highlightIntensity else 0.55f * highlightIntensity)
    )

    val shadowAlpha = if (isDark) 0.35f else 0.12f

    Box(
        modifier = modifier
            .drawBehind {
                val outline = shape.createOutline(size, layoutDirection, this)
                // Draw soft ambient diffused drop shadow
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
                // Outer subtle contact stroke
                val strokeWidthPx = 0.6.dp.toPx()
                val outline = shape.createOutline(size, layoutDirection, this)
                drawOutline(
                    outline = outline,
                    color = Color.Black.copy(alpha = if (isDark) 0.40f else 0.12f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthPx)
                )
                // Inner specular highlight illumination stroke
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
 * Supports:
 * - [AppDesignStyle.LIQUID_GLASS]: Apple iOS & Mornye frosted glass capsule with
 *   specular lighting rim, smooth active selection pill, and responsive spring physics.
 * - [AppDesignStyle.MATERIAL]: Google Material 3 Expressive pill container with dynamic colors.
 */
@Composable
fun FloatingNavigationIsland(
    designStyle: AppDesignStyle,
    clarity: Float = 0.85f,
    selectedTab: Int = 0,
    onMyBooksClick: () -> Unit,
    onAddBookClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val isDark = isSystemInDarkTheme()
    val isGlass = designStyle == AppDesignStyle.LIQUID_GLASS

    if (isGlass) {
        // LIQUID GLASS (Apple / Mornye) STYLE
        LiquidGlassSurface(
            shape = RoundedCornerShape(32.dp),
            clarity = clarity,
            isDark = isDark,
            shadowElevation = 18.dp,
            modifier = modifier
                .height(64.dp)
                .width(330.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Tab 1: Books (Library)
                val isMyBooksSelected = selectedTab == 0
                val myBooksBg = if (isMyBooksSelected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                } else Color.Transparent

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(myBooksBg)
                        .then(
                            if (isMyBooksSelected) {
                                Modifier.drawBehind {
                                    drawOutline(
                                        outline = RoundedCornerShape(22.dp).createOutline(size, layoutDirection, this),
                                        brush = Brush.verticalGradient(
                                            listOf(
                                                Color.White.copy(alpha = if (isDark) 0.35f else 0.65f),
                                                Color.White.copy(alpha = 0.08f)
                                            )
                                        ),
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(0.8.dp.toPx())
                                    )
                                }
                            } else Modifier
                        )
                        .expressiveClickable(onClick = onMyBooksClick),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = strings.navMyBooks,
                            tint = if (isMyBooksSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                            },
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = strings.navMyBooks,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            fontWeight = if (isMyBooksSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isMyBooksSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.70f)
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Tab 2: Add Book (Central Hero Button)
                Box(
                    modifier = Modifier
                        .width(56.dp)
                        .height(50.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.88f)
                                )
                            )
                        )
                        .drawBehind {
                            // Specular top highlight for the hero button
                            drawOutline(
                                outline = RoundedCornerShape(24.dp).createOutline(size, layoutDirection, this),
                                brush = Brush.verticalGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.55f),
                                        Color.Transparent
                                    )
                                ),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(1.0.dp.toPx())
                            )
                        }
                        .expressiveClickable(scaleDown = 0.92f, onClick = onAddBookClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = strings.addBooks,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Tab 3: Settings
                val isSettingsSelected = selectedTab == 2
                val settingsBg = if (isSettingsSelected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                } else Color.Transparent

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(settingsBg)
                        .then(
                            if (isSettingsSelected) {
                                Modifier.drawBehind {
                                    drawOutline(
                                        outline = RoundedCornerShape(22.dp).createOutline(size, layoutDirection, this),
                                        brush = Brush.verticalGradient(
                                            listOf(
                                                Color.White.copy(alpha = if (isDark) 0.35f else 0.65f),
                                                Color.White.copy(alpha = 0.08f)
                                            )
                                        ),
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(0.8.dp.toPx())
                                    )
                                }
                            } else Modifier
                        )
                        .expressiveClickable(onClick = onSettingsClick),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = strings.settingsTitle,
                            tint = if (isSettingsSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                            },
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = strings.settingsTitle,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            fontWeight = if (isSettingsSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSettingsSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.70f)
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
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
                .height(58.dp)
                .width(310.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Tab 1: Books (Library)
                Surface(
                    onClick = onMyBooksClick,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.90f),
                    modifier = Modifier
                        .height(44.dp)
                        .weight(1f)
                        .expressiveBounce(remember { MutableInteractionSource() })
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = strings.navMyBooks,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = strings.navMyBooks,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Tab 2: Add Book (+)
                Surface(
                    onClick = onAddBookClick,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .size(44.dp)
                        .expressiveBounce(remember { MutableInteractionSource() })
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = strings.addBooks,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Tab 3: Settings
                Surface(
                    onClick = onSettingsClick,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    modifier = Modifier
                        .height(44.dp)
                        .weight(1f)
                        .expressiveBounce(remember { MutableInteractionSource() })
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = strings.settingsTitle,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = strings.settingsTitle,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
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
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val isDark = isSystemInDarkTheme()

    if (isGlass) {
        val bg = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
        } else {
            Color.White.copy(alpha = if (isDark) 0.08f else 0.45f)
        }

        val border = if (selected) {
            BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary)
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
                        brush = if (selected) SolidColor(MaterialTheme.colorScheme.primary) else border.brush,
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
            }
        }
    } else {
        androidx.compose.material3.FilterChip(
            selected = selected,
            onClick = onClick,
            label = label,
            leadingIcon = leadingIcon,
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

/**
 * Live Interactive Preview Card for the Navigation Island shown in Settings.
 * Gives immediate visual delight and allows the user to see the frosted reflections.
 */
@Composable
fun NavigationIslandPreviewCard(
    designStyle: AppDesignStyle,
    clarity: Float = 0.85f,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val isDark = isSystemInDarkTheme()
    var previewTab by remember { mutableIntStateOf(0) }

    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF0F1014) else Color(0xFFECEEF4)
        ),
        border = BorderStroke(
            1.dp,
            if (isDark) Color(0x33FFFFFF) else Color(0x22000000)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = strings.previewNavigationIsland,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.height(24.dp)
                ) {
                    Text(
                        text = if (designStyle == AppDesignStyle.LIQUID_GLASS) "Liquid Glass" else "Material 3",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Ambient background simulating book content scrolling behind the island
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                if (isDark) Color(0xFF1E2230) else Color(0xFFD6DBE8),
                                if (isDark) Color(0xFF2A1F36) else Color(0xFFE8D6DE),
                                if (isDark) Color(0xFF182828) else Color(0xFFD8EBE6)
                            )
                        )
                    )
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Mock book cards in background
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .width(70.dp)
                                .height(95.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isDark) Color(0x55FFFFFF) else Color(0x88FFFFFF)
                                )
                        )
                    }
                }

                // Floating island floating over the mock content
                FloatingNavigationIsland(
                    designStyle = designStyle,
                    clarity = clarity,
                    selectedTab = previewTab,
                    onMyBooksClick = { previewTab = 0 },
                    onAddBookClick = { previewTab = 1 },
                    onSettingsClick = { previewTab = 2 },
                    modifier = Modifier.graphicsLayer { scaleX = 0.88f; scaleY = 0.88f }
                )
            }
        }
    }
}
