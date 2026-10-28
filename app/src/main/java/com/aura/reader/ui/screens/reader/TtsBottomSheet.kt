package com.aura.reader.ui.screens.reader

import androidx.compose.foundation.layout.statusBarsPadding
import com.aura.reader.ui.components.SyncBottomSheetSystemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.foundation.lazy.LazyRow
import java.util.Locale
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.ui.graphics.Color

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.aura.reader.ui.components.jellyOverscroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FormatColorText
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.reader.service.BookTtsService
import com.aura.reader.ui.components.LiquidGlassSwitch
import com.aura.reader.service.TtsState
import com.aura.reader.ui.theme.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TtsBottomSheet(
    ttsState: TtsState,
    onDismiss: () -> Unit,
    onPlayPause: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onStop: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    onPitchChange: (Float) -> Unit,
    onVoiceChange: (String) -> Unit,
    onPreviewVoice: (String) -> Unit = {},
    onSeekParagraph: (Int) -> Unit = {},
    onHighlightDelayChange: (Int) -> Unit = {},
    onToggleWordHighlight: (Boolean) -> Unit = {},
    isGlass: Boolean = false
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val strings = LocalAppStrings.current
    var isVoiceDropdownExpanded by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier
            .statusBarsPadding()
            .padding(top = 24.dp)
    ) {
        SyncBottomSheetSystemBars()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .jellyOverscroll(allowTopBounce = false)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Headphones,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = strings.ttsSettingsSheetTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = ttsState.chapterTitle.ifBlank { ttsState.bookTitle },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = strings.close)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Current Paragraph Preview Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.8f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.ttsParagraphProgress(
                                ttsState.currentParagraphIndex + 1,
                                ttsState.totalParagraphs.coerceAtLeast(1)
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (ttsState.isPlaying) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = strings.ttsPlaying,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (ttsState.totalParagraphs > 1) {
                        var isDraggingSlider by remember { mutableStateOf(false) }
                        var sliderVal by remember(ttsState.currentParagraphIndex) {
                            mutableFloatStateOf(ttsState.currentParagraphIndex.toFloat())
                        }

                        Slider(
                            value = if (isDraggingSlider) sliderVal else ttsState.currentParagraphIndex.toFloat(),
                            onValueChange = {
                                isDraggingSlider = true
                                sliderVal = it
                            },
                            onValueChangeFinished = {
                                isDraggingSlider = false
                                onSeekParagraph(sliderVal.toInt().coerceIn(0, ttsState.totalParagraphs - 1))
                            },
                            valueRange = 0f..(ttsState.totalParagraphs - 1).toFloat(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                        )
                    } else {
                        val progress = if (ttsState.totalParagraphs > 0) {
                            (ttsState.currentParagraphIndex.toFloat() + 1f) / ttsState.totalParagraphs.toFloat()
                        } else 0f

                        LinearProgressIndicator(
                            progress = { progress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (ttsState.currentText.isNotBlank()) ttsState.currentText else strings.ttsInitializing,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Playback Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPrev,
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipPrevious,
                        contentDescription = strings.previous,
                        modifier = Modifier.size(32.dp)
                    )
                }

                FilledIconButton(
                    onClick = onPlayPause,
                    modifier = Modifier.size(68.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = if (ttsState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (ttsState.isPlaying) strings.ttsPaused else strings.ttsPlaying,
                        modifier = Modifier.size(36.dp)
                    )
                }

                IconButton(
                    onClick = onNext,
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = strings.next,
                        modifier = Modifier.size(32.dp)
                    )
                }

                IconButton(
                    onClick = onStop,
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Stop,
                        contentDescription = strings.ttsStop,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Speed Control
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.Speed,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.ttsSpeed,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = "${ttsState.speed}x",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val speedOptions = listOf(0.5f, 0.8f, 1.0f, 1.25f, 1.5f, 2.0f)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                speedOptions.forEach { spd ->
                    val isSelected = kotlin.math.abs(ttsState.speed - spd) < 0.05f
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSpeedChange(spd) },
                        label = { Text("${spd}x") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Pitch Control
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.RecordVoiceOver,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.ttsPitchTitle,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = String.format("%.2fx", ttsState.pitch),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Slider(
                value = ttsState.pitch,
                onValueChange = onPitchChange,
                valueRange = 0.75f..1.25f,
                steps = 4,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Word Highlighting Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f).padding(end = 12.dp)
                ) {
                    Icon(
                        Icons.Rounded.FormatColorText,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = strings.ttsWordHighlightTitle,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = strings.ttsWordHighlightSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (isGlass) {
                    LiquidGlassSwitch(
                        checked = ttsState.isWordHighlightEnabled,
                        onCheckedChange = { onToggleWordHighlight(it) }
                    )
                } else {
                    Switch(
                        checked = ttsState.isWordHighlightEnabled,
                        onCheckedChange = { onToggleWordHighlight(it) }
                    )
                }
            }

            // Highlight Synchronization Options (only when word highlight is enabled)
            if (ttsState.isWordHighlightEnabled) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Sync,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.ttsSyncTitle,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = if (ttsState.highlightDelayMs < 0) strings.ttsSyncAuto else "${ttsState.highlightDelayMs} ms",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val delayOptions = listOf(-1, 0, 150, 300, 450, 600)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    delayOptions.forEach { delayMs ->
                        val isSelected = ttsState.highlightDelayMs == delayMs
                        val label = if (delayMs < 0) strings.ttsSyncAuto else "${delayMs}ms"
                        FilterChip(
                            selected = isSelected,
                            onClick = { onHighlightDelayChange(delayMs) },
                            label = { Text(label) }
                        )
                    }
                }
            }

            // Voice / Language Selector
            if (ttsState.availableVoices.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = strings.ttsVoiceTitle,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                val currentVoiceName = ttsState.selectedVoiceName
                val currentVoice = ttsState.availableVoices.firstOrNull { it.name == currentVoiceName }
                val currentVoiceLabel = currentVoice?.let { v ->
                    if (v.languageDisplayName.isNotBlank()) "${v.languageDisplayName}: ${v.displayName}"
                    else v.displayName
                } ?: strings.ttsVoiceDefault

                OutlinedCard(
                    onClick = { isVoiceDropdownExpanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentVoiceLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Rounded.ExpandMore, contentDescription = null)
                    }
                }

                if (isVoiceDropdownExpanded) {
                    var voiceSearchQuery by remember { mutableStateOf("") }
                    val distinctLanguages = remember(ttsState.availableVoices) {
                        ttsState.availableVoices
                            .map { it.languageDisplayName to it.languageCode }
                            .distinctBy { it.second }
                            .filter { it.first.isNotBlank() }
                    }

                    val systemLang = remember { Locale.getDefault().language.lowercase(Locale.ROOT) }
                    var selectedLangCode by remember(ttsState.selectedVoiceName, distinctLanguages) {
                        val voiceLang = ttsState.availableVoices.firstOrNull { it.name == currentVoiceName }?.languageCode
                        val defaultCode = when {
                            voiceLang != null && voiceLang.isNotBlank() -> voiceLang
                            distinctLanguages.any { it.second == systemLang } -> systemLang
                            distinctLanguages.isNotEmpty() -> distinctLanguages.first().second
                            else -> ""
                        }
                        mutableStateOf(defaultCode)
                    }

                    val filteredVoices = remember(voiceSearchQuery, selectedLangCode, ttsState.availableVoices) {
                        var list = ttsState.availableVoices
                        if (selectedLangCode.isNotBlank()) {
                            list = list.filter { it.languageCode == selectedLangCode }
                        }
                        if (voiceSearchQuery.isNotBlank()) {
                            list = list.filter {
                                it.displayName.contains(voiceSearchQuery, ignoreCase = true) ||
                                it.languageDisplayName.contains(voiceSearchQuery, ignoreCase = true) ||
                                it.name.contains(voiceSearchQuery, ignoreCase = true)
                            }
                        }
                        list
                    }

                    AlertDialog(
                        onDismissRequest = { isVoiceDropdownExpanded = false },
                        title = {
                            Text(
                                text = strings.ttsVoiceTitle,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        text = {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                if (distinctLanguages.size > 1) {
                                    var isLangDropdownOpen by remember { mutableStateOf(false) }
                                    val currentLangText = if (selectedLangCode.isBlank()) {
                                        strings.ttsAllLanguages
                                    } else {
                                        val found = distinctLanguages.firstOrNull { it.second == selectedLangCode }
                                        val count = ttsState.availableVoices.count { it.languageCode == selectedLangCode }
                                        if (found != null) "${found.first} ($count)" else strings.ttsAllLanguages
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 10.dp)
                                    ) {
                                        Surface(
                                            onClick = { isLangDropdownOpen = true },
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Language,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Text(
                                                        text = currentLangText,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                                Icon(
                                                    imageVector = Icons.Rounded.ArrowDropDown,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        DropdownMenu(
                                            expanded = isLangDropdownOpen,
                                            onDismissRequest = { isLangDropdownOpen = false },
                                            modifier = Modifier.heightIn(max = 280.dp)
                                        ) {
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = strings.ttsAllLanguages,
                                                        fontWeight = if (selectedLangCode.isBlank()) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                },
                                                onClick = {
                                                    selectedLangCode = ""
                                                    isLangDropdownOpen = false
                                                },
                                                leadingIcon = {
                                                    if (selectedLangCode.isBlank()) {
                                                        Icon(Icons.Rounded.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                                    }
                                                }
                                            )
                                            distinctLanguages.forEach { (langName, langCode) ->
                                                val count = ttsState.availableVoices.count { it.languageCode == langCode }
                                                val isSelected = selectedLangCode == langCode
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            text = "$langName ($count)",
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                        )
                                                    },
                                                    onClick = {
                                                        selectedLangCode = langCode
                                                        isLangDropdownOpen = false
                                                    },
                                                    leadingIcon = {
                                                        if (isSelected) {
                                                            Icon(Icons.Rounded.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                if (ttsState.availableVoices.size > 6) {
                                    OutlinedTextField(
                                        value = voiceSearchQuery,
                                        onValueChange = { voiceSearchQuery = it },
                                        placeholder = { Text(strings.searchLibraryPlaceholder) },
                                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 8.dp)
                                    )
                                }

                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 340.dp)
                                ) {
                                    items(filteredVoices, key = { it.name }) { voice ->
                                        val isSelected = voice.name == currentVoiceName
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                                    else Color.Transparent
                                                )
                                                .clickable {
                                                    onVoiceChange(voice.name)
                                                    isVoiceDropdownExpanded = false
                                                }
                                                .padding(horizontal = 14.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = voice.displayName,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                )
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    modifier = Modifier.padding(top = 2.dp)
                                                ) {
                                                    if (selectedLangCode.isBlank() && voice.languageDisplayName.isNotBlank()) {
                                                        Text(
                                                            text = voice.languageDisplayName,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                    Surface(
                                                        color = if (voice.isNetworkRequired) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                                        shape = RoundedCornerShape(6.dp)
                                                    ) {
                                                        Text(
                                                            text = if (voice.isNetworkRequired) strings.ttsOnlineQuality else strings.ttsOfflineQuality,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                IconButton(
                                                    onClick = { onPreviewVoice(voice.name) },
                                                    modifier = Modifier.size(36.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(19.dp)
                                                    )
                                                }
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Check,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(20.dp).padding(start = 2.dp)
                                                    )
                                                } else {
                                                    Spacer(modifier = Modifier.width(20.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { isVoiceDropdownExpanded = false }) {
                                Text(strings.cancel)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}
