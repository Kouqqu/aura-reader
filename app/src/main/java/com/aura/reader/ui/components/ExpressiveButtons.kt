package com.aura.reader.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
 * The five M3 Expressive button sizes and the measurements the design tokens assign to
 * each of them.
 *
 * Sources (design token tables on m3.material.io):
 * - Buttons: `md.comp.button.{size}.*`
 * - Icon buttons: `md.comp.icon-button.{size}.*`
 * - Split buttons: `md.comp.split-button.{size}.*`
 * - Connected button groups: `md.comp.button-group.connected.{size}.*`
 *
 * Aura Reader only ever uses [Small] (40 dp), but the table keeps the whole ladder so a
 * taller variant can be dropped in without rediscovering the numbers.
 */
enum class ExpressiveButtonSize(
    /** Container height, shared by buttons, icon buttons, split buttons and groups. */
    val height: Dp,
    /** Leading / trailing padding of a label button (also the group item padding). */
    val containerSpace: Dp,
    /** Icon size inside a *label* button. */
    val iconSize: Dp,
    /** Icon size inside an icon-only button. */
    val iconButtonIconSize: Dp,
    /** Padding around the icon inside an icon-only button (`icon + 2 * space = height`). */
    val iconButtonSpace: Dp,
    /** Gap between a button's icon and its label. */
    val iconLabelSpace: Dp,
    /** Corner radius of a square button / icon button at rest. */
    val squareCorner: Dp,
    /** Corner radius of a square button / icon button while pressed. */
    val pressedCorner: Dp,
    /** Split button: leading button's start padding. */
    val splitLeadingStart: Dp,
    /** Split button: leading button's end padding. */
    val splitLeadingEnd: Dp,
    /** Split button: trailing button width (never narrower than the 48 dp target). */
    val splitTrailingWidth: Dp,
    /** Split button: trailing menu icon size. */
    val splitTrailingIcon: Dp,
    /** Split button: optical offset of the unselected menu icon. */
    val splitIconOffset: Dp,
    /** Split button: inner corner at rest. */
    val splitInnerCorner: Dp,
    /** Split button: inner corner while hovered, focused or pressed. */
    val splitInnerCornerActive: Dp,
    /** Connected group: inner corner at rest. */
    val groupInnerCorner: Dp,
    /** Connected group: inner corner while pressed. */
    val groupInnerCornerPressed: Dp,
) {
    XSmall(
        height = 32.dp,
        containerSpace = 12.dp,
        iconSize = 20.dp,
        iconButtonIconSize = 20.dp,
        iconButtonSpace = 6.dp,
        iconLabelSpace = 4.dp,
        squareCorner = 12.dp,
        pressedCorner = 8.dp,
        splitLeadingStart = 12.dp,
        splitLeadingEnd = 10.dp,
        splitTrailingWidth = 48.dp,
        splitTrailingIcon = 22.dp,
        splitIconOffset = (-1).dp,
        splitInnerCorner = 4.dp,
        splitInnerCornerActive = 8.dp,
        groupInnerCorner = 8.dp,
        groupInnerCornerPressed = 4.dp,
    ),
    Small(
        height = 40.dp,
        containerSpace = 16.dp,
        iconSize = 20.dp,
        iconButtonIconSize = 24.dp,
        iconButtonSpace = 8.dp,
        iconLabelSpace = 8.dp,
        squareCorner = 12.dp,
        pressedCorner = 8.dp,
        splitLeadingStart = 16.dp,
        splitLeadingEnd = 12.dp,
        splitTrailingWidth = 48.dp,
        splitTrailingIcon = 22.dp,
        splitIconOffset = (-1).dp,
        splitInnerCorner = 4.dp,
        splitInnerCornerActive = 12.dp,
        groupInnerCorner = 8.dp,
        groupInnerCornerPressed = 4.dp,
    ),
    Medium(
        height = 56.dp,
        containerSpace = 24.dp,
        iconSize = 24.dp,
        iconButtonIconSize = 24.dp,
        iconButtonSpace = 16.dp,
        iconLabelSpace = 8.dp,
        squareCorner = 16.dp,
        pressedCorner = 12.dp,
        splitLeadingStart = 24.dp,
        splitLeadingEnd = 24.dp,
        splitTrailingWidth = 56.dp,
        splitTrailingIcon = 26.dp,
        splitIconOffset = (-2).dp,
        splitInnerCorner = 4.dp,
        splitInnerCornerActive = 12.dp,
        groupInnerCorner = 8.dp,
        groupInnerCornerPressed = 4.dp,
    ),
    Large(
        height = 96.dp,
        containerSpace = 48.dp,
        iconSize = 32.dp,
        iconButtonIconSize = 32.dp,
        iconButtonSpace = 32.dp,
        iconLabelSpace = 12.dp,
        squareCorner = 28.dp,
        pressedCorner = 16.dp,
        splitLeadingStart = 48.dp,
        splitLeadingEnd = 48.dp,
        splitTrailingWidth = 96.dp,
        splitTrailingIcon = 38.dp,
        splitIconOffset = (-3).dp,
        splitInnerCorner = 8.dp,
        splitInnerCornerActive = 20.dp,
        groupInnerCorner = 16.dp,
        groupInnerCornerPressed = 12.dp,
    ),
    XLarge(
        height = 136.dp,
        containerSpace = 64.dp,
        iconSize = 40.dp,
        iconButtonIconSize = 40.dp,
        iconButtonSpace = 48.dp,
        iconLabelSpace = 16.dp,
        squareCorner = 28.dp,
        pressedCorner = 16.dp,
        splitLeadingStart = 64.dp,
        splitLeadingEnd = 64.dp,
        splitTrailingWidth = 136.dp,
        splitTrailingIcon = 50.dp,
        splitIconOffset = (-6).dp,
        splitInnerCorner = 12.dp,
        splitInnerCornerActive = 20.dp,
        groupInnerCorner = 20.dp,
        groupInnerCornerPressed = 16.dp,
    ),
}

/** Half of the container height, i.e. the "full" corner of a pill. */
private val ExpressiveButtonSize.fullCorner: Dp get() = height / 2

/**
 * M3 Expressive motion for corner morphs: the fast spatial spring
 * (`md.sys.motion.spring.fast.spatial`, damping 0.9 / stiffness 1400).
 */
private fun cornerSpring() = spring<Dp>(dampingRatio = 0.9f, stiffness = 1400f)

/**
 * Animated inner corner of a split button / group item. It grows on hover, focus or press
 * and becomes a full round corner when the segment is selected, which is exactly what the
 * `selected.inner-corner.corner-size = 50%` token describes.
 */
@Composable
private fun segmentInnerCorner(
    interactionSource: MutableInteractionSource,
    rest: Dp,
    active: Dp,
    selected: Boolean,
    height: Dp,
): Dp {
    val pressed by interactionSource.collectIsPressedAsState()
    val hovered by interactionSource.collectIsHoveredAsState()
    val focused by interactionSource.collectIsFocusedAsState()
    val target = when {
        selected -> height / 2
        pressed || hovered || focused -> active
        else -> rest
    }
    val corner by animateDpAsState(
        targetValue = target,
        animationSpec = cornerSpring(),
        label = "expressiveSegmentInnerCorner"
    )
    return corner
}

/**
 * Material 3 Expressive Split Button.
 *
 * Geometry follows `md.comp.split-button.small.*` (the default 40 dp size):
 * - outer corners are fully round, inner corners are 4 dp at rest and 12 dp while
 *   hovered / focused / pressed;
 * - the 2 dp seam between the two segments never changes;
 * - the leading button is padded 16 dp / 12 dp (not symmetrically);
 * - the trailing menu button is 48 x 40 dp with a 22 dp icon, optically nudged 1 dp
 *   toward the leading button while unselected and centred once the menu is open;
 * - when the trailing button is selected its inner corner becomes fully round.
 *
 * The old implementation used 14 dp of symmetrical padding, a 40 x 40 dp trailing
 * button, a 20 dp menu icon and no corner morph at all, which is why it read as a
 * generic "rounded chip pair" rather than an M3 Expressive split button.
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
    size: ExpressiveButtonSize = ExpressiveButtonSize.Small,
    gap: Dp = 2.dp,
    expanded: Boolean = false,
    enabled: Boolean = true
) {
    val leadingInteractionSource = remember { MutableInteractionSource() }
    val trailingInteractionSource = remember { MutableInteractionSource() }

    val leadingInner = segmentInnerCorner(
        interactionSource = leadingInteractionSource,
        rest = size.splitInnerCorner,
        active = size.splitInnerCornerActive,
        selected = false,
        height = size.height
    )
    val trailingInner = segmentInnerCorner(
        interactionSource = trailingInteractionSource,
        rest = size.splitInnerCorner,
        active = size.splitInnerCornerActive,
        selected = expanded,
        height = size.height
    )

    val leadingShape: Shape = RoundedCornerShape(
        topStart = CornerSize(50),
        bottomStart = CornerSize(50),
        topEnd = CornerSize(leadingInner),
        bottomEnd = CornerSize(leadingInner)
    )
    val trailingShape: Shape = RoundedCornerShape(
        topStart = CornerSize(trailingInner),
        bottomStart = CornerSize(trailingInner),
        topEnd = CornerSize(50),
        bottomEnd = CornerSize(50)
    )

    // The menu icon sits 1 dp off centre while unselected and locks to the centre once
    // the menu is open (the spec calls this optical centring).
    val trailingIconOffset by animateDpAsState(
        targetValue = if (expanded) 0.dp else size.splitIconOffset,
        animationSpec = cornerSpring(),
        label = "splitButtonMenuIconOffset"
    )

    Row(
        modifier = modifier.height(size.height),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(gap)
    ) {
        // 1. Leading button (the primary action).
        Surface(
            onClick = onLeadingClick,
            shape = leadingShape,
            color = containerColor,
            contentColor = contentColor,
            border = border,
            enabled = enabled,
            interactionSource = leadingInteractionSource,
            modifier = Modifier
                .height(size.height)
                .clip(leadingShape)
                .expressiveBounce(leadingInteractionSource, enabled = enabled)
        ) {
            Row(
                modifier = Modifier
                    .padding(start = size.splitLeadingStart, end = size.splitLeadingEnd)
                    .defaultMinSize(minHeight = size.height),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(size.iconLabelSpace)
            ) {
                CompositionLocalProvider(LocalContentColor provides contentColor) {
                    leadingContent()
                }
            }
        }

        // 2. Trailing button (menu anchor / secondary action).
        Surface(
            onClick = onTrailingClick,
            shape = trailingShape,
            color = containerColor,
            contentColor = contentColor,
            border = border,
            enabled = enabled,
            interactionSource = trailingInteractionSource,
            modifier = Modifier
                .width(size.splitTrailingWidth)
                .height(size.height)
                .clip(trailingShape)
                .expressiveBounce(trailingInteractionSource, enabled = enabled)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(size.splitTrailingWidth)
                    .height(size.height)
            ) {
                Box(modifier = Modifier.offset(x = trailingIconOffset)) {
                    CompositionLocalProvider(LocalContentColor provides contentColor) {
                        trailingContent()
                    }
                }
            }
        }
    }
}

/**
 * Split button with the standard chevron disclosure on the trailing segment.
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
    size: ExpressiveButtonSize = ExpressiveButtonSize.Small,
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
                    .size(size.splitTrailingIcon)
                    .graphicsLayer { rotationZ = chevronRotation }
            )
        },
        onLeadingClick = onLeadingClick,
        onTrailingClick = onTrailingClick,
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        border = border,
        size = size,
        gap = gap,
        expanded = isExpanded,
        enabled = enabled
    )
}

/**
 * Material 3 Expressive icon button.
 *
 * `md.comp.icon-button.small.*`: 40 dp container, 24 dp icon, 8 dp padding, square shape
 * with 12 dp corners that morph to 8 dp while pressed (round variant is a full circle).
 * The previous version used a 20 dp icon inside a 14 dp squircle, which is neither of the
 * two official shapes.
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
    size: ExpressiveButtonSize = ExpressiveButtonSize.Small,
    round: Boolean = false,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val corner by animateDpAsState(
        targetValue = if (pressed) size.pressedCorner else size.squareCorner,
        animationSpec = cornerSpring(),
        label = "expressiveIconButtonCorner"
    )
    val shape: Shape = if (round) CircleShape else RoundedCornerShape(corner)

    Surface(
        onClick = onClick,
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        border = border,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = modifier
            .size(size.height)
            .clip(shape)
            .expressiveBounce(interactionSource, enabled = enabled)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(size.height)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = contentColor,
                modifier = Modifier.size(size.iconButtonIconSize)
            )
        }
    }
}

/**
 * Material 3 Expressive Connected Button Group item.
 *
 * `md.comp.button-group.connected.small.*`:
 * - 40 dp height, 2 dp seam between segments (the group owns the gap);
 * - the group's outer shape is fully round, so end segments get a 50% outer corner;
 * - inner corners are 8 dp at rest, 4 dp while pressed and fully round (50%) when the
 *   segment is selected;
 * - selection changes only that segment's own shape - it must not resize its neighbours.
 *
 * The old version was 36 dp tall with 16 dp outer corners, hard-coded 4 dp inner corners
 * and an animated checkmark that widened the selected segment and pushed the rest of the
 * row sideways. That checkmark does not exist in the Expressive spec and the width shift
 * is explicitly the *standard* (non-connected) group behaviour, so both are gone.
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
    size: ExpressiveButtonSize = ExpressiveButtonSize.Small,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val innerRadius by animateDpAsState(
        targetValue = when {
            selected -> size.fullCorner
            pressed -> size.groupInnerCornerPressed
            else -> size.groupInnerCorner
        },
        animationSpec = cornerSpring(),
        label = "expressiveGroupInnerCorner"
    )

    val shape: Shape = when {
        totalCount <= 1 -> RoundedCornerShape(CornerSize(50))
        index == 0 -> RoundedCornerShape(
            topStart = CornerSize(50),
            bottomStart = CornerSize(50),
            topEnd = CornerSize(innerRadius),
            bottomEnd = CornerSize(innerRadius)
        )
        index == totalCount - 1 -> RoundedCornerShape(
            topStart = CornerSize(innerRadius),
            bottomStart = CornerSize(innerRadius),
            topEnd = CornerSize(50),
            bottomEnd = CornerSize(50)
        )
        else -> RoundedCornerShape(CornerSize(innerRadius))
    }

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
                .height(size.height)
                .clip(shape)
                .background(bg)
                .border(width = 0.5.dp, color = borderColor, shape = shape)
                .clip(shape)
                .expressiveClickable(scaleDown = 0.95f, onClick = onClick)
                .padding(horizontal = size.containerSpace),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(size.iconLabelSpace)
            ) {
                leadingIcon?.invoke()
                CompositionLocalProvider(
                    LocalContentColor provides contentColor,
                    LocalTextStyle provides MaterialTheme.typography.labelLarge
                ) {
                    label()
                }
                trailingIcon?.invoke()
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
                .height(size.height)
                .clip(shape)
                .expressiveBounce(interactionSource, enabled = enabled)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = size.containerSpace),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(size.iconLabelSpace)
            ) {
                leadingIcon?.invoke()
                CompositionLocalProvider(
                    LocalContentColor provides contentColor,
                    LocalTextStyle provides MaterialTheme.typography.labelLarge
                ) {
                    label()
                }
                trailingIcon?.invoke()
            }
        }
    }
}
