package com.aura.reader.ui.components

import android.view.HapticFeedbackConstants
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView

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
