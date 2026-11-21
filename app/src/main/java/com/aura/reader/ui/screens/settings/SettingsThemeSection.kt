package com.aura.reader.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aura.reader.data.model.AndroidColorPreset
import com.aura.reader.ui.components.LiquidGlassSurface
import com.aura.reader.ui.components.LiquidGlassSwitch
import com.aura.reader.ui.components.expressiveBounce
import com.aura.reader.data.model.ReaderThemeMode
import com.aura.reader.ui.theme.AmoledBackground
import com.aura.reader.ui.theme.AmoledText
import com.aura.reader.ui.theme.LocalAppStrings
import com.aura.reader.ui.theme.SepiaBackground
import com.aura.reader.ui.theme.SepiaText

@Composable
internal fun SettingsThemeSection(
    currentTheme: ReaderThemeMode,
    sysDark: Boolean,
    isGlass: Boolean,
    isThemeDark: Boolean,
    isSepia: Boolean,
    sheetCoordinates: LayoutCoordinates?,
    syncThemesWithApp: Boolean,
    onSyncThemesChange: (Boolean) -> Unit,
    materialYouEnabled: Boolean,
    onToggleMaterialYou: (Boolean) -> Unit,
    colorPreset: String = "PURPLE",
    onColorPresetChange: (String) -> Unit = {},
    seasonalEffectsEnabled: Boolean = true,
    onToggleSeasonalEffects: (Boolean) -> Unit = {},
    holidayOverride: String = "AUTO",
    onHolidayOverrideChange: (String) -> Unit = {},
    onOpenReaderThemeSettings: () -> Unit,
    onThemePicked: (ReaderThemeMode, Offset, List<Color>) -> Unit
) {
    val strings = LocalAppStrings.current

    // 1. Theme Section
    val lightColors = listOf(Color(0xFFFFF9E6), Color(0xFFFFE082), Color(0xFFF5F5F5), Color(0xFFE0E0E0), Color(0xFFFFFDE7), Color(0xFFFFFFFF))
    val darkColors = listOf(Color(0xFFECEFF1), Color(0xFFCFD8DC), Color(0xFFB0BEC5), Color(0xFF90A4AE), Color(0xFF78909C), Color(0xFFFFFFFF))
    val sepiaColors = listOf(Color(0xFFFAF0E6), Color(0xFFD7CCC8), Color(0xFFBCAAA4), Color(0xFFFFE0B2), Color(0xFFEFEBE9), Color(0xFFFFFFFF))
    val amoledColors = listOf(Color(0xFFFFFFFF), Color(0xFFF5F5F5), Color(0xFFEEEEEE), Color(0xFFE0E0E0), Color(0xFFBDBDBD), Color(0xFFFFFFFF))

    var expanded by rememberSaveable { mutableStateOf(true) }
    SettingsFoldHeader(
        title = strings.themeSectionTitle,
        isGlass = isGlass,
        expanded = expanded,
        onToggle = { expanded = !expanded }
    )

    SettingsFoldContent(isGlass = isGlass, expanded = expanded) {

    // Segmented Theme Switcher Card
    val themeCardContent: @Composable () -> Unit = {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExpressiveThemePill(
                    label = strings.themeLight,
                    icon = Icons.Rounded.LightMode,
                    isSelected = currentTheme == ReaderThemeMode.LIGHT,
                    previewBgColor = Color(0xFFFFFFFF),
                    previewTextColor = Color(0xFF1D1B20),
                    getSheetCoordinates = { sheetCoordinates },
                    onClick = { origin -> onThemePicked(ReaderThemeMode.LIGHT, origin, lightColors) },
                    modifier = Modifier.weight(1f)
                )
                ExpressiveThemePill(
                    label = strings.themeDark,
                    icon = Icons.Rounded.DarkMode,
                    isSelected = currentTheme == ReaderThemeMode.DARK,
                    previewBgColor = Color(0xFF121212),
                    previewTextColor = Color(0xFFEDEDED),
                    getSheetCoordinates = { sheetCoordinates },
                    onClick = { origin -> onThemePicked(ReaderThemeMode.DARK, origin, darkColors) },
                    modifier = Modifier.weight(1f)
                )
                ExpressiveThemePill(
                    label = strings.themeSepia,
                    icon = Icons.AutoMirrored.Rounded.MenuBook,
                    isSelected = currentTheme == ReaderThemeMode.SEPIA,
                    previewBgColor = SepiaBackground,
                    previewTextColor = SepiaText,
                    getSheetCoordinates = { sheetCoordinates },
                    onClick = { origin -> onThemePicked(ReaderThemeMode.SEPIA, origin, sepiaColors) },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExpressiveThemePill(
                    label = strings.themeSystem,
                    icon = Icons.Rounded.BrightnessAuto,
                    isSelected = currentTheme == ReaderThemeMode.SYSTEM_DYNAMIC,
                    previewBgColor = if (sysDark) Color(0xFF1E1E1E) else Color(0xFFF0F3F6),
                    previewTextColor = if (sysDark) Color(0xFFEDEDED) else Color(0xFF1D1B20),
                    getSheetCoordinates = { sheetCoordinates },
                    onClick = { origin -> onThemePicked(ReaderThemeMode.SYSTEM_DYNAMIC, origin, if (sysDark) darkColors else lightColors) },
                    modifier = Modifier.weight(1.35f)
                )
                ExpressiveThemePill(
                    label = strings.themeAmoled,
                    icon = Icons.Rounded.Contrast,
                    isSelected = currentTheme == ReaderThemeMode.AMOLED,
                    previewBgColor = AmoledBackground,
                    previewTextColor = AmoledText,
                    getSheetCoordinates = { sheetCoordinates },
                    onClick = { origin -> onThemePicked(ReaderThemeMode.AMOLED, origin, amoledColors) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    if (isGlass) {
        LiquidGlassSurface(
            shape = RoundedCornerShape(24.dp),
            isDark = isThemeDark,
            shadowElevation = 8.dp,
            tintColor = if (isThemeDark) {
                if (materialYouEnabled) MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.50f)
                else MaterialTheme.colorScheme.surfaceContainer
            } else if (isSepia) Color(0x60F4E8D6) else Color(0x85FFFFFF),
            modifier = Modifier.fillMaxWidth(),
            content = themeCardContent
        )
    } else {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            modifier = Modifier.fillMaxWidth(),
            content = { themeCardContent() }
        )
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Appearance Settings Group Card
    val setSeasonalEffects: (Boolean) -> Unit = { enabled ->
        onToggleSeasonalEffects(enabled)
        // Turning the atmosphere back on after "Standard" should not leave it dead:
        // fall back to the calendar unless a specific season is already chosen.
        if (enabled && holidayOverride == "OFF") onHolidayOverrideChange("AUTO")
    }
    ExpressiveSettingsGroup {
        ExpressiveSettingsRow(
            title = strings.syncThemesTitle,
            subtitle = strings.syncThemesSubtitle,
            icon = { SettingsIconBox(Icons.Rounded.Palette) },
            onClick = { onSyncThemesChange(!syncThemesWithApp) },
            trailing = {
                if (isGlass) {
                    LiquidGlassSwitch(
                        checked = syncThemesWithApp,
                        onCheckedChange = onSyncThemesChange
                    )
                } else {
                    Switch(
                        checked = syncThemesWithApp,
                        onCheckedChange = onSyncThemesChange
                    )
                }
            }
        )
        ExpressiveDivider()
        ExpressiveSettingsRow(
            title = strings.readerThemeSettingsTitle,
            subtitle = strings.readerThemeSettingsSubtitle,
            icon = { SettingsIconBox(Icons.AutoMirrored.Rounded.MenuBook) },
            onClick = { onOpenReaderThemeSettings() },
            trailing = {
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )
    }

    Spacer(modifier = Modifier.height(14.dp))

    ExpressiveSettingsGroup {
        ExpressiveSettingsRow(
            title = strings.materialYouToggle,
            subtitle = strings.materialYouSubtitle,
            icon = { SettingsIconBox(Icons.Rounded.AutoAwesome) },
            onClick = { onToggleMaterialYou(!materialYouEnabled) },
            trailing = {
                if (isGlass) {
                    LiquidGlassSwitch(
                        checked = materialYouEnabled,
                        onCheckedChange = onToggleMaterialYou
                    )
                } else {
                    Switch(
                        checked = materialYouEnabled,
                        onCheckedChange = onToggleMaterialYou,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        )
        ExpressiveDivider()
        AndroidColorPresetsSection(
            selectedPreset = colorPreset,
            materialYouEnabled = materialYouEnabled,
            isGlass = isGlass,
            isThemeDark = isThemeDark,
            onPresetSelected = { presetId ->
                onColorPresetChange(presetId)
                if (materialYouEnabled) onToggleMaterialYou(false)
            }
        )
        ExpressiveDivider()
        ExpressiveSettingsRow(
            title = strings.seasonalEffectsToggle,
            subtitle = strings.seasonalEffectsSubtitle,
            icon = { SettingsIconBox(Icons.Rounded.Celebration) },
            onClick = { setSeasonalEffects(!seasonalEffectsEnabled) },
            trailing = {
                if (isGlass) {
                    LiquidGlassSwitch(
                        checked = seasonalEffectsEnabled,
                        onCheckedChange = setSeasonalEffects
                    )
                } else {
                    Switch(
                        checked = seasonalEffectsEnabled,
                        onCheckedChange = setSeasonalEffects,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        )
    }

    AnimatedVisibility(
        visible = seasonalEffectsEnabled,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Column {
            Spacer(modifier = Modifier.height(18.dp))
            SettingsHolidayStyleSection(
                holidayOverride = holidayOverride,
                seasonalEffectsEnabled = seasonalEffectsEnabled,
                isGlass = isGlass,
                isThemeDark = isThemeDark,
                onHolidayStylePicked = { style ->
                    if (style == "OFF") {
                        onHolidayOverrideChange("OFF")
                        if (seasonalEffectsEnabled) onToggleSeasonalEffects(false)
                    } else if (style == "AUTO") {
                        onHolidayOverrideChange("AUTO")
                    } else {
                        onHolidayOverrideChange(style)
                        if (!seasonalEffectsEnabled) onToggleSeasonalEffects(true)
                    }
                }
            )
        }
    }

    Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun AndroidColorPresetsSection(
    selectedPreset: String,
    materialYouEnabled: Boolean,
    isGlass: Boolean,
    isThemeDark: Boolean,
    onPresetSelected: (String) -> Unit
) {
    val strings = LocalAppStrings.current
    val haptic = LocalHapticFeedback.current
    val presets = AndroidColorPreset.entries

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = strings.androidColorPresetsTitle,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            presets.forEach { preset ->
                val isSelected = !materialYouEnabled && selectedPreset.equals(preset.id, ignoreCase = true)
                val interactionSource = remember { MutableInteractionSource() }

                val ringColor = if (isSelected) {
                    preset.swatchColor
                } else if (isGlass) {
                    if (isThemeDark) Color.White.copy(alpha = 0.22f) else Color.Black.copy(alpha = 0.16f)
                } else {
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.40f)
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(42.dp)
                        .expressiveBounce(interactionSource)
                        .clip(CircleShape)
                        .clickable(interactionSource = interactionSource, indication = null) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onPresetSelected(preset.id)
                        }
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.Transparent,
                        border = BorderStroke(if (isSelected) 3.dp else 1.dp, ringColor),
                        modifier = Modifier.size(42.dp)
                    ) {}

                    Surface(
                        shape = CircleShape,
                        color = preset.swatchColor,
                        modifier = Modifier.size(if (isSelected) 30.dp else 34.dp)
                    ) {
                        if (isSelected) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
