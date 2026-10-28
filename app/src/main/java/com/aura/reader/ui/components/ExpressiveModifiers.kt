package com.aura.reader.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Material 3 Expressive spring physics bounce modifier.
 * Scales down dynamically on touch press and springs back on release with organic damping.
 */
@Composable
fun Modifier.expressiveBounce(
    interactionSource: MutableInteractionSource,
    scaleDown: Float = 0.96f,
    enabled: Boolean = true
): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "expressiveBounce"
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Clickable modifier with Material 3 Expressive bouncy spring feedback and subtle haptic tick.
 * [hasRipple] defaults to false to provide an authentic iOS / Liquid Glass press feel without square Android ripples.
 */
fun Modifier.expressiveClickable(
    scaleDown: Float = 0.96f,
    enabled: Boolean = true,
    hapticFeedback: Boolean = true,
    hasRipple: Boolean = false,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val view = LocalView.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "expressiveClickableScale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = if (hasRipple) LocalIndication.current else null,
            enabled = enabled
        ) {
            if (hapticFeedback) {
                try {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                } catch (e: Exception) {}
            }
            onClick()
        }
}


/**
 * Jelly spring physics overscroll and rebound modifier.
 * When scrolling or flinging reaches the edges, absorbs kinetic energy and rebounds
 * with elastic, organic spring damping ("желейный отскок").
 */
fun Modifier.jellyOverscroll(
    enabled: Boolean = true,
    dampingRatio: Float = 0.98f,
    stiffness: Float = Spring.StiffnessMediumLow,
    maxOverscroll: Float = 72f
): Modifier = composed {
    if (!enabled) return@composed this

    val coroutineScope = rememberCoroutineScope()
    val offsetY = remember { Animatable(0f) }

    val connection = remember(dampingRatio, stiffness, maxOverscroll) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val current = offsetY.value
                if (current != 0f) {
                    if ((current > 0f && available.y < 0f) || (current < 0f && available.y > 0f)) {
                        val consumed = if (abs(available.y) >= abs(current)) -current else available.y
                        coroutineScope.launch {
                            offsetY.snapTo(current + consumed)
                        }
                        return Offset(0f, consumed)
                    }
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (available.y != 0f && source == NestedScrollSource.UserInput) {
                    val current = offsetY.value
                    val resistance = (1f - (abs(current) / (maxOverscroll * 1.3f)).coerceIn(0f, 0.85f)) * 0.28f
                    val newOffset = (current + available.y * resistance).coerceIn(-maxOverscroll, maxOverscroll)
                    coroutineScope.launch {
                        offsetY.snapTo(newOffset)
                    }
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (offsetY.value != 0f) {
                    offsetY.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(dampingRatio = dampingRatio, stiffness = stiffness),
                        initialVelocity = available.y * 0.12f
                    )
                }
                return Velocity.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                val current = offsetY.value
                if (available.y != 0f || current != 0f) {
                    val bounceImpulse = (available.y * 0.055f).coerceIn(-maxOverscroll * 0.75f, maxOverscroll * 0.75f)
                    if (bounceImpulse != 0f && current == 0f) {
                        offsetY.snapTo(bounceImpulse)
                    }
                    offsetY.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(dampingRatio = dampingRatio, stiffness = stiffness),
                        initialVelocity = available.y * 0.14f
                    )
                }
                return Velocity.Zero
            }
        }
    }

    this
        .nestedScroll(connection)
        .graphicsLayer {
            translationY = offsetY.value
        }
}

/**
 * Protects BottomSheet scroll content from accidental fling collapses while ensuring
 * that a user can swipe down from anywhere on the sheet to dismiss it once scrolled to the top.
 */
fun Modifier.sheetScrollIsolation(
    isAtTop: () -> Boolean = { true }
): Modifier = composed {
    val connection = remember(isAtTop) {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                // If user is actively dragging down at the top, pass to ModalBottomSheet to dismiss smoothly from anywhere
                if (available.y > 0f && isAtTop()) {
                    return Offset.Zero
                }
                // Absorb leftover delta when hitting bottom or upward scroll so sheet doesn't jitter
                return available
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                // Absorb leftover fling velocity so rapid scroll to the top doesn't accidentally slam the sheet shut
                return available
            }
        }
    }
    this.nestedScroll(connection)
}
