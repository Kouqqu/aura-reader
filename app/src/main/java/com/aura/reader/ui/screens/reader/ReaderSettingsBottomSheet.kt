package com.aura.reader.ui.screens.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.aura.reader.ui.theme.LocalAppStrings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.reader.data.model.PageTurnAnimation
import com.aura.reader.data.model.ReaderFontFamily
import com.aura.reader.data.model.ReaderSettings
import com.aura.reader.data.model.ReaderThemeMode
import com.aura.reader.data.model.TwoColumnMode
import com.aura.reader.ui.components.PixelButtonBurst
import com.aura.reader.ui.components.triggerThemeHaptic
import com.aura.reader.ui.theme.AmoledBackground
import com.aura.reader.ui.theme.AmoledText
import com.aura.reader.ui.theme.SepiaBackground
import com.aura.reader.ui.theme.SepiaText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderSettingsBottomSheet(
    settings: ReaderSettings,
    materialYouEnabled: Boolean = false,
    onMaterialYouChange: ((Boolean) -> Unit)? = null,
    onDismiss: () -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onLineHeightChange: (Float) -> Unit,
    onThemeModeChange: (ReaderThemeMode) -> Unit,
    onFontFamilyChange: (ReaderFontFamily) -> Unit,
    onLightImageBackgroundChange: (Boolean) -> Unit,
    onPagingModeChange: (Boolean) -> Unit,
    onAutoHyphenationChange: (Boolean) -> Unit = {},
    onTwoColumnModeChange: (TwoColumnMode) -> Unit = {},
    onPageAnimationChange: (PageTurnAnimation) -> Unit = {},
    onHapticFeedbackChange: (Boolean) -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val strings = LocalAppStrings.current
    val haptic = LocalHapticFeedback.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = strings.readerSettingsTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // --- Theme Mode ---
                Text(
                    text = strings.themeModeTitle,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val lightColors = listOf(Color(0xFFFFF9C4), Color(0xFFFFE082), Color(0xFFFFD54F), Color(0xFFFFFFFF), Color(0xFFFFB74D))
                    val darkColors = listOf(Color(0xFF90CAF9), Color(0xFF64B5F6), Color(0xFF42A5F5), Color(0xFFB39DDB), Color(0xFFE2E2E6))
                    val sepiaColors = listOf(Color(0xFFFFCC80), Color(0xFFFFB74D), Color(0xFFD7CCC8), Color(0xFFBCAAA4), Color(0xFFFFE0B2))
                    val amoledColors = listOf(Color(0xFFCE93D8), Color(0xFFBA68C8), Color(0xFF80DEEA), Color(0xFF4DD0E1), Color(0xFFFFFFFF))

                    ThemeOptionButton(
                        label = strings.themeLight,
                        bgColor = Color(0xFFFFFFFF),
                        textColor = Color(0xFF1D1B20),
                        isSelected = settings.themeMode == ReaderThemeMode.LIGHT,
                        burstColors = lightColors,
                        hapticEnabled = settings.hapticFeedbackEnabled,
                        modifier = Modifier.weight(1f),
                        onClick = { onThemeModeChange(ReaderThemeMode.LIGHT) }
                    )

                    ThemeOptionButton(
                        label = strings.themeDark,
                        bgColor = Color(0xFF1E2125),
                        textColor = Color(0xFFE2E2E6),
                        isSelected = settings.themeMode == ReaderThemeMode.DARK || settings.themeMode == ReaderThemeMode.SYSTEM_DYNAMIC,
                        burstColors = darkColors,
                        hapticEnabled = settings.hapticFeedbackEnabled,
                        modifier = Modifier.weight(1f),
                        onClick = { onThemeModeChange(ReaderThemeMode.DARK) }
                    )

                    ThemeOptionButton(
                        label = strings.themeSepia,
                        bgColor = SepiaBackground,
                        textColor = SepiaText,
                        isSelected = settings.themeMode == ReaderThemeMode.SEPIA,
                        burstColors = sepiaColors,
                        hapticEnabled = settings.hapticFeedbackEnabled,
                        modifier = Modifier.weight(1f),
                        onClick = { onThemeModeChange(ReaderThemeMode.SEPIA) }
                    )

                    ThemeOptionButton(
                        label = strings.themeAmoled,
                        bgColor = AmoledBackground,
                        textColor = AmoledText,
                        isSelected = settings.themeMode == ReaderThemeMode.AMOLED,
                        burstColors = amoledColors,
                        hapticEnabled = settings.hapticFeedbackEnabled,
                        modifier = Modifier.weight(1f),
                        onClick = { onThemeModeChange(ReaderThemeMode.AMOLED) }
                    )
                }

                if (onMaterialYouChange != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    androidx.compose.material3.Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(
                                    text = strings.materialYouToggle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = strings.materialYouSubtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = materialYouEnabled,
                                onCheckedChange = onMaterialYouChange
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- Font Size ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.fontSizeTitle,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${settings.fontSizeSp.toInt()} sp",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("A", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                    Slider(
                        value = settings.fontSizeSp,
                        onValueChange = onFontSizeChange,
                        valueRange = 12f..32f,
                        steps = 9,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                    )
                    Text("A", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- Font Family ---
                Text(
                    text = strings.fontFamilyTitle,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = settings.fontFamily == ReaderFontFamily.SERIF,
                        onClick = { onFontFamilyChange(ReaderFontFamily.SERIF) },
                        label = { Text(strings.fontFamilySerif) },
                        leadingIcon = if (settings.fontFamily == ReaderFontFamily.SERIF) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )

                    FilterChip(
                        selected = settings.fontFamily == ReaderFontFamily.SANS_SERIF,
                        onClick = { onFontFamilyChange(ReaderFontFamily.SANS_SERIF) },
                        label = { Text(strings.fontFamilySansSerif) },
                        leadingIcon = if (settings.fontFamily == ReaderFontFamily.SANS_SERIF) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )

                    FilterChip(
                        selected = settings.fontFamily == ReaderFontFamily.MONOSPACE,
                        onClick = { onFontFamilyChange(ReaderFontFamily.MONOSPACE) },
                        label = { Text(strings.fontFamilyMonospace) },
                        leadingIcon = if (settings.fontFamily == ReaderFontFamily.MONOSPACE) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- Line Height ---
                Text(
                    text = strings.lineHeightTitle,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = settings.lineHeightMultiplier == 1.2f,
                        onClick = { onLineHeightChange(1.2f) },
                        label = { Text("1.2x") }
                    )
                    FilterChip(
                        selected = settings.lineHeightMultiplier == 1.5f,
                        onClick = { onLineHeightChange(1.5f) },
                        label = { Text("1.5x") }
                    )
                    FilterChip(
                        selected = settings.lineHeightMultiplier == 1.8f,
                        onClick = { onLineHeightChange(1.8f) },
                        label = { Text("1.8x") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- Reading Mode (Continuous / Paging) ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onPagingModeChange(!settings.pagingMode) }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = strings.readingModeTitle,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (settings.pagingMode) strings.readingModePaged else strings.readingModeScroll,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.pagingMode,
                        onCheckedChange = onPagingModeChange
                    )
                }

                if (settings.pagingMode) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = strings.pageAnimationSectionTitle,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = settings.pageAnimation == PageTurnAnimation.SLIDE,
                            onClick = { onPageAnimationChange(PageTurnAnimation.SLIDE) },
                            label = { Text(strings.pageAnimationSlide, maxLines = 1, softWrap = false) }
                        )
                        FilterChip(
                            selected = settings.pageAnimation == PageTurnAnimation.INSTANT,
                            onClick = { onPageAnimationChange(PageTurnAnimation.INSTANT) },
                            label = { Text(strings.pageAnimationInstant, maxLines = 1, softWrap = false) }
                        )
                        FilterChip(
                            selected = settings.pageAnimation == PageTurnAnimation.CURL,
                            onClick = { onPageAnimationChange(PageTurnAnimation.CURL) },
                            label = { Text(strings.pageAnimationCurl, maxLines = 1, softWrap = false) }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = strings.twoColumnSpreadSectionTitle,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = strings.twoColumnSpreadSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = settings.twoColumnMode == TwoColumnMode.AUTO,
                            onClick = { onTwoColumnModeChange(TwoColumnMode.AUTO) },
                            label = { Text(strings.twoColumnSpreadAuto, maxLines = 1, softWrap = false) }
                        )
                        FilterChip(
                            selected = settings.twoColumnMode == TwoColumnMode.OFF,
                            onClick = { onTwoColumnModeChange(TwoColumnMode.OFF) },
                            label = { Text(strings.twoColumnSpreadOff, maxLines = 1, softWrap = false) }
                        )
                        FilterChip(
                            selected = settings.twoColumnMode == TwoColumnMode.ALWAYS,
                            onClick = { onTwoColumnModeChange(TwoColumnMode.ALWAYS) },
                            label = { Text(strings.twoColumnSpreadAlways, maxLines = 1, softWrap = false) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- Haptic Feedback ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onHapticFeedbackChange(!settings.hapticFeedbackEnabled) }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = strings.hapticFeedbackTitle,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = strings.hapticFeedbackSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.hapticFeedbackEnabled,
                        onCheckedChange = onHapticFeedbackChange
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- Auto Hyphenation ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onAutoHyphenationChange(!settings.autoHyphenation) }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = strings.autoHyphenationTitle,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = strings.autoHyphenationSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.autoHyphenation,
                        onCheckedChange = onAutoHyphenationChange
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- Light Background for Illustrations in Dark Themes ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onLightImageBackgroundChange(!settings.lightImageBackground) }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = strings.lightImageBgTitle,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = strings.lightImageBgSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.lightImageBackground,
                        onCheckedChange = onLightImageBackgroundChange
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Spacer(modifier = Modifier.navigationBarsPadding())
                Spacer(modifier = Modifier.height(32.dp))
            }


        }
    }
}

@Composable
fun ThemeOptionButton(
    label: String,
    bgColor: Color,
    textColor: Color,
    isSelected: Boolean,
    burstColors: List<Color>,
    hapticEnabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    var burstTrigger by remember { mutableStateOf(0L) }

    val borderModifier = if (isSelected) {
        Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
    } else {
        Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
    }

    Box(
        modifier = modifier
            .height(52.dp)
            .then(borderModifier)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .clickable {
                if (hapticEnabled) {
                    triggerThemeHaptic(context)
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                }
                burstTrigger = System.currentTimeMillis()
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )

        if (burstTrigger > 0L) {
            PixelButtonBurst(
                triggerKey = burstTrigger,
                colors = burstColors,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
