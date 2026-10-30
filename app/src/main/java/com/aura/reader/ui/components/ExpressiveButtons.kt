package com.aura.reader.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Material 3 Expressive Split Button / Connected Action Pair.
 *
 * Adheres directly to the official Material 3 specification (https://m3.material.io/components/split-button/overview):
 * - Two distinct interactive areas: leading action button + trailing secondary/toggle button.
 * - Expressive asymmetrical corner radii: outer corners are 20.dp, inner touching corners are 4.dp.
 * - Calibrated 2.dp gap between segments.
 * - Standard 40.dp height.
 * - Strict corner clipping so ripple/pressed state never bleeds out as a rectangle.
 * - Independent ripple effects and touch physics (expressiveBounce) for both segments.
 */
@Composable
fun ExpressiveSplitButton(
    leadingContent: @Composable RowScope.() -> Unit,
    trailingContent: @Composable BoxScope.() -> Unit,
    onLeadingClick: () -> Unit,
    onTrailingClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    border: BorderStroke? = null,
    height: Dp = 40.dp,
    gap: Dp = 2.dp,
    enabled: Boolean = true
) {
    val leadingInteractionSource = remember { MutableInteractionSource() }
    val trailingInteractionSource = remember { MutableInteractionSource() }

    val leadingShape: Shape = RoundedCornerShape(
        topStart = 20.dp,
        bottomStart = 20.dp,
        topEnd = 4.dp,
        bottomEnd = 4.dp
    )

    val trailingShape: Shape = RoundedCornerShape(
        topStart = 4.dp,
        bottomStart = 4.dp,
        topEnd = 20.dp,
        bottomEnd = 20.dp
    )

    Row(
        modifier = modifier.height(height),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(gap)
    ) {
        // 1. Leading Button (Action / Selected Option)
        Surface(
            onClick = onLeadingClick,
            shape = leadingShape,
            color = containerColor,
            contentColor = contentColor,
            border = border,
            enabled = enabled,
            interactionSource = leadingInteractionSource,
            modifier = Modifier
                .height(height)
                .clip(leadingShape)
                .expressiveBounce(leadingInteractionSource, enabled = enabled)
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 14.dp)
                    .defaultMinSize(minHeight = height),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CompositionLocalProvider(LocalContentColor provides contentColor) {
                    leadingContent()
                }
            }
        }

        // 2. Trailing Button (Toggle / Action / Menu Anchor)
        Surface(
            onClick = onTrailingClick,
            shape = trailingShape,
            color = containerColor,
            contentColor = contentColor,
            border = border,
            enabled = enabled,
            interactionSource = trailingInteractionSource,
            modifier = Modifier
                .size(height)
                .clip(trailingShape)
                .expressiveBounce(trailingInteractionSource, enabled = enabled)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(height)
            ) {
                CompositionLocalProvider(LocalContentColor provides contentColor) {
                    trailingContent()
                }
            }
        }
    }
}

/**
 * Standard M3 Expressive Split Button with Chevron Disclosure.
 */
@Composable
fun ExpressiveSplitButton(
    leadingContent: @Composable RowScope.() -> Unit,
    onLeadingClick: () -> Unit,
    onTrailingClick: () -> Unit,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    border: BorderStroke? = null,
    height: Dp = 40.dp,
    gap: Dp = 2.dp,
    enabled: Boolean = true
) {
    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 240),
        label = "splitButtonChevronRotation"
    )

    ExpressiveSplitButton(
        leadingContent = leadingContent,
        trailingContent = {
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer { rotationZ = chevronRotation }
            )
        },
        onLeadingClick = onLeadingClick,
        onTrailingClick = onTrailingClick,
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        border = border,
        height = height,
        gap = gap,
        enabled = enabled
    )
}

/**
 * Material 3 Expressive standalone Icon Button with squircle geometry.
 * Sits alongside Split Buttons with matching 40.dp height.
 */
@Composable
fun ExpressiveIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    border: BorderStroke? = null,
    shape: Shape = RoundedCornerShape(14.dp),
    size: Dp = 40.dp,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        onClick = onClick,
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        border = border,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = modifier
            .size(size)
            .clip(shape)
            .expressiveBounce(interactionSource, enabled = enabled)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(size)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Material 3 Expressive Connected Button Group / Segmented Button Item.
 *
 * Adheres to Material 3 Expressive Connected Button Group guidelines:
 * (https://m3.material.io/components/all-buttons and https://m3.material.io/components/segmented-buttons/overview)
 * - Segments form a continuous connected ribbon with 2.dp calibrated gaps.
 * - Asymmetrical corner radii: outer ends are large (16.dp), inner touching ends are small (4.dp).
 * - Standard 36.dp height.
 * - Dynamic containerColor (secondaryContainer when selected, surfaceContainerLow with outlineVariant border when unselected).
 * - Animated selection checkmark when selected (if no explicit leading icon is present).
 * - Strict corner clipping so ripple/press states never bleed out as a rectangle.
 * - Independent press spring physics.
 */
@Composable
fun ExpressiveConnectedButtonItem(
    selected: Boolean,
    onClick: () -> Unit,
    index: Int,
    totalCount: Int,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    isGlass: Boolean = false,
    isDark: Boolean = isSystemInDarkTheme(),
    outerRadius: Dp = 16.dp,
    innerRadius: Dp = 4.dp,
    height: Dp = 36.dp,
    enabled: Boolean = true
) {
    val shape: Shape = when {
        totalCount <= 1 -> RoundedCornerShape(outerRadius)
        index == 0 -> RoundedCornerShape(
            topStart = outerRadius,
            bottomStart = outerRadius,
            topEnd = innerRadius,
            bottomEnd = innerRadius
        )
        index == totalCount - 1 -> RoundedCornerShape(
            topStart = innerRadius,
            bottomStart = innerRadius,
            topEnd = outerRadius,
            bottomEnd = outerRadius
        )
        else -> RoundedCornerShape(innerRadius)
    }

    val interactionSource = remember { MutableInteractionSource() }

    if (isGlass) {
        val primaryColor = MaterialTheme.colorScheme.primary
        val bg = if (selected) {
            primaryColor.copy(alpha = if (isDark) 0.28f else 0.18f)
        } else {
            if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f)
        }
        val borderColor = if (selected) {
            primaryColor.copy(alpha = 0.75f)
        } else {
            if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
        }
        val contentColor = if (selected) primaryColor else MaterialTheme.colorScheme.onSurface

        Box(
            modifier = modifier
                .height(height)
                .clip(shape)
                .background(bg)
                .border(width = 0.5.dp, color = borderColor, shape = shape)
                .clip(shape)
                .expressiveClickable(scaleDown = 0.95f, onClick = onClick)
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (leadingIcon != null) {
                    leadingIcon()
                } else {
                    AnimatedVisibility(
                        visible = selected,
                        enter = expandHorizontally() + fadeIn(),
                        exit = shrinkHorizontally() + fadeOut()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                CompositionLocalProvider(
                    LocalContentColor provides contentColor,
                    LocalTextStyle provides MaterialTheme.typography.labelLarge
                ) {
                    label()
                }
                if (trailingIcon != null) {
                    trailingIcon()
                }
            }
        }
    } else {
        val containerColor = if (selected) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        }
        val contentColor = if (selected) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
        val border = if (!selected) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        } else null

        Surface(
            onClick = onClick,
            shape = shape,
            color = containerColor,
            contentColor = contentColor,
            border = border,
            enabled = enabled,
            interactionSource = interactionSource,
            modifier = modifier
                .height(height)
                .clip(shape)
                .expressiveBounce(interactionSource, enabled = enabled)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (leadingIcon != null) {
                    leadingIcon()
                } else {
                    AnimatedVisibility(
                        visible = selected,
                        enter = expandHorizontally() + fadeIn(),
                        exit = shrinkHorizontally() + fadeOut()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                CompositionLocalProvider(
                    LocalContentColor provides contentColor,
                    LocalTextStyle provides MaterialTheme.typography.labelLarge
                ) {
                    label()
                }
                if (trailingIcon != null) {
                    trailingIcon()
                }
            }
        }
    }
}
