package com.aura.reader.ui.screens.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.reader.service.TtsState
import com.aura.reader.ui.components.LiquidGlassSurface
import com.aura.reader.ui.theme.Strings
import kotlin.math.abs

@Composable
fun TtsControlBar(
    ttsState: TtsState,
    strings: Strings,
    onPlayPause: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onStop: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    onClick: () -> Unit = {},
    isGlass: Boolean = false,
    isDark: Boolean = false,
    modifier: Modifier = Modifier
) {
    val ttsContent: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Headphones,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${strings.ttsNotificationTitle} (${ttsState.currentParagraphIndex + 1}/${ttsState.totalParagraphs.coerceAtLeast(1)})",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = ttsState.currentText.ifBlank { ttsState.chapterTitle },
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = onPrev,
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipPrevious,
                    contentDescription = strings.previous,
                    modifier = Modifier.size(18.dp)
                )
            }

            FilledIconButton(
                onClick = onPlayPause,
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(
                    imageVector = if (ttsState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (ttsState.isPlaying) strings.ttsPaused else strings.ttsPlaying,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = onNext,
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipNext,
                    contentDescription = strings.next,
                    modifier = Modifier.size(18.dp)
                )
            }

            val speeds = listOf(0.8f, 1.0f, 1.25f, 1.5f, 2.0f)
            TextButton(
                onClick = {
                    val currentIdx = speeds.indexOfFirst { abs(it - ttsState.speed) < 0.05f }
                    val nextSpeed = speeds[(if (currentIdx >= 0) currentIdx + 1 else 1) % speeds.size]
                    onSpeedChange(nextSpeed)
                },
                contentPadding = PaddingValues(horizontal = 2.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Text(
                    text = "${ttsState.speed}x",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onStop,
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = strings.ttsStop,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }

    if (isGlass) {
        val ttsBg = if (isDark) Color(0xEB18191E) else Color(0xF2F5F6F8)
        LiquidGlassSurface(
            shape = RoundedCornerShape(18.dp),
            shadowElevation = 8.dp,
            isDark = isDark,
            tintColor = ttsBg,
            modifier = modifier
                .fillMaxWidth()
                .clickable { onClick() },
            content = ttsContent
        )
    } else {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.98f),
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = modifier
                .fillMaxWidth()
                .clickable { onClick() },
            content = { ttsContent() }
        )
    }
}
