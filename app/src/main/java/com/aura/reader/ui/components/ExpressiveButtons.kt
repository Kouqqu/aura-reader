package com.aura.reader.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
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
