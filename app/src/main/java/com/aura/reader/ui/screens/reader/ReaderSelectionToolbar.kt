package com.aura.reader.ui.screens.reader

import androidx.compose.foundation.BorderStroke
import com.aura.reader.data.model.Quote
import androidx.compose.foundation.background
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.widthIn
import com.aura.reader.ui.theme.LocalAppStrings
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ReaderSelectionToolbar(
    anchorRect: Rect,
    onDismissRequest: () -> Unit,
    onCopy: () -> Unit,
    onQuote: () -> Unit,
    onDictionary: () -> Unit,
    onTranslate: () -> Unit,
    onReadFromHere: () -> Unit
) {
    val strings = LocalAppStrings.current
    val density = LocalDensity.current
    val rect = anchorRect
    Popup(
        popupPositionProvider = object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize
            ): IntOffset {
                val targetX = (rect.left + (rect.width - popupContentSize.width) / 2f).toInt()
                val minX = with(density) { 12.dp.toPx() }.toInt()
                val maxX = (windowSize.width - popupContentSize.width - minX).coerceAtLeast(minX)
                val clampedX = targetX.coerceIn(minX, maxX)
                val spaceAbove = rect.top - with(density) { 8.dp.toPx() }
                val targetY = if (spaceAbove >= popupContentSize.height + with(density) { 48.dp.toPx() }) {
                    (rect.top - popupContentSize.height - with(density) { 8.dp.toPx() }).toInt()
                } else {
                    (rect.bottom + with(density) { 8.dp.toPx() }).toInt()
                }
                val minY = with(density) { 40.dp.toPx() }.toInt()
                val maxY = (windowSize.height - popupContentSize.height - with(density) { 20.dp.toPx() }).toInt()
                val clampedY = targetY.coerceIn(minY, maxY.coerceAtLeast(minY))
                return IntOffset(clampedX, clampedY)
            }
        },
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(
            focusable = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            shadowElevation = 8.dp,
            tonalElevation = 6.dp,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            ),
            modifier = Modifier.widthIn(max = (LocalConfiguration.current.screenWidthDp.dp - 24.dp))
        ) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // 1. Copy
                TextButton(
                    onClick = { onCopy() },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(strings.copy, style = MaterialTheme.typography.labelSmall, maxLines = 1, softWrap = false)
                }
                Box(modifier = Modifier.height(14.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)))
                // 2. Quote
                TextButton(
                    onClick = { onQuote() },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Rounded.FormatQuote, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(strings.quoteAction, style = MaterialTheme.typography.labelSmall, maxLines = 1, softWrap = false)
                }
                Box(modifier = Modifier.height(14.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)))
                // 3. Dictionary
                TextButton(
                    onClick = { onDictionary() },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Rounded.AutoStories, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(strings.dictionaryAction, style = MaterialTheme.typography.labelSmall, maxLines = 1, softWrap = false)
                }
                Box(modifier = Modifier.height(14.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)))
                // 4. Translation
                TextButton(
                    onClick = { onTranslate() },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Rounded.Translate, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(strings.translateAction, style = MaterialTheme.typography.labelSmall, maxLines = 1, softWrap = false)
                }
                Box(modifier = Modifier.height(14.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)))
                // 5. Read from here (TTS)
                TextButton(
                    onClick = { onReadFromHere() },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(strings.ttsStartFromHere, style = MaterialTheme.typography.labelSmall, maxLines = 1, softWrap = false)
                }
            }
        }
    }
}
