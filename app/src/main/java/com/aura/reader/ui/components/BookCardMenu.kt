package com.aura.reader.ui.components

import androidx.collection.mutableIntListOf
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorPosition
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/**
 * A card menu that grows out of the card it belongs to.
 *
 * The stock `DropdownMenu` hangs off a corner, which reads as a stranger's menu on
 * a screen of wide cards. Material 3 1.5 split the dropdown in two and left the
 * good half public: [DropdownMenuPopup] is the bare popup, so we can place a
 * surface ourselves, centered under the card, and let it settle with the menu's
 * own scale-and-fade instead of a hard cut.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BookCardMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    isGlass: Boolean,
    isDark: Boolean,
    shape: Shape = RoundedCornerShape(20.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val appearance = liquidGlassMenuAppearance(isGlass, isDark)
    val motion = MaterialTheme.motionScheme
    val density = LocalDensity.current
    val gap = with(density) { MenuCardGap.roundToPx() }

    // One horizontal candidate — the card's own middle. The provider clamps it
    // back into the window if the card is half-scrolled off screen.
    val anchor = remember(gap) {
        MenuAnchorPosition.Custom(
            xCandidates = {
                mutableIntListOf(anchorBounds.center.x - menuSize.width / 2)
            },
            yCandidates = {
                val below = anchorBounds.bottom + gap
                val above = anchorBounds.top - gap - menuSize.height
                mutableIntListOf(below, above)
            }
        )
    }
    val positionProvider = MenuDefaults.rememberDropdownMenuPopupPositionProvider(anchor)

    val expandedState = remember { MutableTransitionState(false) }
    expandedState.targetState = expanded
    val settle = updateTransition(expandedState, label = "bookCardMenu")
    val alpha by settle.animateFloat(
        transitionSpec = { motion.defaultEffectsSpec() },
        label = "bookCardMenuAlpha"
    ) { if (it) 1f else 0f }
    val scale by settle.animateFloat(
        transitionSpec = { motion.defaultSpatialSpec() },
        label = "bookCardMenuScale"
    ) { if (it) 1f else 0.82f }

    // The state keeps the popup standing through its own exit, so the last frame
    // of the fade is still drawn; then it goes away entirely.
    if (expandedState.currentState || expandedState.targetState) {
        DropdownMenuPopup(
            expanded = true,
            onDismissRequest = onDismissRequest,
            popupPositionProvider = positionProvider,
            properties = MenuDefaults.DefaultMenuProperties
        ) {
            Surface(
                shape = shape,
                color = appearance.containerColor,
                tonalElevation = MenuDefaults.TonalElevation,
                shadowElevation = appearance.shadowElevation,
                border = appearance.border,
                modifier = Modifier.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                    transformOrigin = positionProvider.transformOrigin
                }
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    content()
                }
            }
        }
    }
}

/** The breath of air between a card and its menu. */
private val MenuCardGap = 8.dp
