package com.aura.reader.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.ui.graphics.luminance
import androidx.compose.material3.MaterialTheme
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
    allowTopBounce: Boolean = true,
    allowBottomBounce: Boolean = true,
    dampingRatio: Float = Spring.DampingRatioNoBouncy,
    stiffness: Float = Spring.StiffnessMediumLow,
    maxOverscroll: Float = 60f
): Modifier = composed {
    if (!enabled) return@composed this

    val coroutineScope = rememberCoroutineScope()
    val offsetY = remember { Animatable(0f) }

    val connection = remember(allowTopBounce, allowBottomBounce, dampingRatio, stiffness, maxOverscroll) {
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
                    if (available.y > 0f && !allowTopBounce) return Offset.Zero
                    if (available.y < 0f && !allowBottomBounce) return Offset.Zero

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
                val current = offsetY.value
                if (current != 0f) {
                    // Finger was released while overscrolled: spring back cleanly to 0
                    offsetY.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(dampingRatio = dampingRatio, stiffness = stiffness),
                        initialVelocity = (available.y * 0.08f).coerceIn(-maxOverscroll, maxOverscroll)
                    )
                    // Consume available velocity completely so onPostFling never triggers a duplicate bounce!
                    return available
                }
                return Velocity.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (available.y != 0f) {
                    val allowBounce = if (available.y > 0f) allowTopBounce else allowBottomBounce
                    if (allowBounce) {
                        val bounceImpulse = (available.y * 0.045f).coerceIn(-maxOverscroll * 0.65f, maxOverscroll * 0.65f)
                        if (bounceImpulse != 0f) {
                            offsetY.snapTo(bounceImpulse)
                            offsetY.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(dampingRatio = dampingRatio, stiffness = stiffness)
                            )
                            return available
                        }
                    }
                } else if (offsetY.value != 0f) {
                    offsetY.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(dampingRatio = dampingRatio, stiffness = stiffness)
                    )
                    return available
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
 * Backward-compatibility wrapper for bottom sheets.
 */
fun Modifier.sheetScrollIsolation(
    isAtTop: () -> Boolean = { true }
): Modifier = this

/**
 * Synchronizes the status bar appearance and ensures transparent edge-to-edge
 * for ModalBottomSheet dialogs and the hosting activity window.
 */
@Composable
fun SyncBottomSheetSystemBars(
    isThemeDark: Boolean = MaterialTheme.colorScheme.surface.luminance() < 0.5f
) {
    val view = LocalView.current
    androidx.compose.runtime.DisposableEffect(view, isThemeDark) {
        fun findDialogWindow(v: android.view.View): android.view.Window? {
            var curr: android.view.ViewParent? = v.parent
            while (curr != null) {
                if (curr is androidx.compose.ui.window.DialogWindowProvider) {
                    return curr.window
                }
                curr = curr.parent
            }
            return null
        }

        val dialogWindow = findDialogWindow(view)
        if (dialogWindow != null) {
            dialogWindow.statusBarColor = android.graphics.Color.TRANSPARENT
            androidx.core.view.WindowCompat.setDecorFitsSystemWindows(dialogWindow, false)
            val insetsController = androidx.core.view.WindowCompat.getInsetsController(dialogWindow, dialogWindow.decorView)
            insetsController.isAppearanceLightStatusBars = !isThemeDark
        }

        val activityWindow = (view.context as? android.app.Activity)?.window
        if (activityWindow != null) {
            val insetsController = androidx.core.view.WindowCompat.getInsetsController(activityWindow, activityWindow.decorView)
            insetsController.isAppearanceLightStatusBars = !isThemeDark
        }

        onDispose {
            val actWindow = (view.context as? android.app.Activity)?.window
            if (actWindow != null) {
                val insetsController = androidx.core.view.WindowCompat.getInsetsController(actWindow, actWindow.decorView)
                insetsController.isAppearanceLightStatusBars = !isThemeDark
            }
        }
    }
}
