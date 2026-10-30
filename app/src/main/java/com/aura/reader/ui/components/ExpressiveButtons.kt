package com.aura.reader.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Material 3 Expressive Split Button.
 *
 * Adheres directly to the official Material 3 specification (https://m3.material.io/components/split-button/overview):
 * - Two distinct interactive areas: leading action button + trailing disclosure button.
 * - Expressive asymmetrical corner radii: outer corners are large (20.dp), inner touching corners are small (4.dp).
 * - Calibrated 2.dp gap between segments.
 * - Standard 40.dp height.
 * - 40.dp square touch target for trailing chevron with smooth 180-degree rotation animation when expanded.
 * - Independent ripple effects and press animations for both segments.
 */
@Composable
fun ExpressiveSplitButton(
    leadingContent: @Composable RowScope.() -> Unit,
    onLeadingClick: () -> Unit,
    onTrailingClick: () -> Unit,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false,
    leadingContentDescription: String? = null,
    trailingContentDescription: String? = "Дополнительные действия",
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    border: BorderStroke? = null,
    height: Dp = 40.dp,
    gap: Dp = 2.dp,
    enabled: Boolean = true
) {
    val leadingInteractionSource = remember { MutableInteractionSource() }
    val trailingInteractionSource = remember { MutableInteractionSource() }

    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 240),
        label = "splitButtonChevronRotation"
    )

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
            shape = leadingShape,
            color = containerColor,
            contentColor = contentColor,
            border = border,
            modifier = Modifier
                .height(height)
                .expressiveBounce(leadingInteractionSource, enabled = enabled)
                .clickable(
                    interactionSource = leadingInteractionSource,
                    indication = ripple(color = contentColor),
                    enabled = enabled,
                    onClickLabel = leadingContentDescription,
                    role = Role.Button,
                    onClick = onLeadingClick
                )
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

        // 2. Trailing Button (Chevron / Menu Anchor)
        Surface(
            shape = trailingShape,
            color = containerColor,
            contentColor = contentColor,
            border = border,
            modifier = Modifier
                .size(height)
                .expressiveBounce(trailingInteractionSource, enabled = enabled)
                .clickable(
                    interactionSource = trailingInteractionSource,
                    indication = ripple(color = contentColor),
                    enabled = enabled,
                    onClickLabel = trailingContentDescription,
                    role = Role.Button,
                    onClick = onTrailingClick
                )
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(height)
            ) {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = trailingContentDescription,
                    tint = contentColor,
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer { rotationZ = chevronRotation }
                )
            }
        }
    }
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
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        border = border,
        modifier = modifier
            .size(size)
            .expressiveBounce(interactionSource, enabled = enabled)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = contentColor),
                enabled = enabled,
                onClickLabel = contentDescription,
                role = Role.Button,
                onClick = onClick
            )
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
