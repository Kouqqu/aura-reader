package com.aura.reader.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * A folding settings section header (Chumpy style) that rolls its body up and down.
 *
 * Works in both Material 3 Expressive and Liquid Glass modes:
 * the caption becomes an interactive row with an animated chevron, allowing long
 * settings sheets to stay clean, organized, and pleasant to navigate.
 */
@Composable
internal fun SettingsFoldHeader(
    title: String,
    isGlass: Boolean,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "settingsFoldChevron"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onToggle() }
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(18.dp)
                .graphicsLayer { rotationZ = chevronRotation }
        )
    }
}

/** The body of a folding settings section. */
@Composable
internal fun SettingsFoldContent(
    isGlass: Boolean,
    expanded: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = expanded,
        enter = expandVertically(MaterialTheme.motionScheme.defaultSpatialSpec()) +
            fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()),
        exit = shrinkVertically(MaterialTheme.motionScheme.defaultSpatialSpec()) +
            fadeOut(MaterialTheme.motionScheme.defaultEffectsSpec())
    ) {
        content()
    }
}
