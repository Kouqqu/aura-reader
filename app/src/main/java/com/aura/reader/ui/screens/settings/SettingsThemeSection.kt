package com.aura.reader.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aura.reader.ui.components.LiquidGlassSurface
import com.aura.reader.ui.components.LiquidGlassSwitch
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
    onOpenReaderThemeSettings: () -> Unit,
    onThemePicked: (ReaderThemeMode, Offset, List<Color>) -> Unit
) {
    val strings = LocalAppStrings.current

    // 1. Theme Section
    val lightColors = listOf(Color(0xFFFFF9E6), Color(0xFFFFE082), Color(0xFFF5F5F5), Color(0xFFE0E0E0), Color(0xFFFFFDE7), Color(0xFFFFFFFF))
    val darkColors = listOf(Color(0xFFECEFF1), Color(0xFFCFD8DC), Color(0xFFB0BEC5), Color(0xFF90A4AE), Color(0xFF78909C), Color(0xFFFFFFFF))
    val sepiaColors = listOf(Color(0xFFFAF0E6), Color(0xFFD7CCC8), Color(0xFFBCAAA4), Color(0xFFFFE0B2), Color(0xFFEFEBE9), Color(0xFFFFFFFF))
    val amoledColors = listOf(Color(0xFFFFFFFF), Color(0xFFF5F5F5), Color(0xFFEEEEEE), Color(0xFFE0E0E0), Color(0xFFBDBDBD), Color(0xFFFFFFFF))

    Text(
        text = strings.themeSectionTitle,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )

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
                    icon = Icons.Default.LightMode,
                    isSelected = currentTheme == ReaderThemeMode.LIGHT,
                    previewBgColor = Color(0xFFFFFFFF),
                    previewTextColor = Color(0xFF1D1B20),
                    getSheetCoordinates = { sheetCoordinates },
                    onClick = { origin -> onThemePicked(ReaderThemeMode.LIGHT, origin, lightColors) },
                    modifier = Modifier.weight(1f)
                )
                ExpressiveThemePill(
                    label = strings.themeDark,
                    icon = Icons.Default.DarkMode,
                    isSelected = currentTheme == ReaderThemeMode.DARK,
                    previewBgColor = Color(0xFF1E2125),
                    previewTextColor = Color(0xFFE2E2E6),
                    getSheetCoordinates = { sheetCoordinates },
                    onClick = { origin -> onThemePicked(ReaderThemeMode.DARK, origin, darkColors) },
                    modifier = Modifier.weight(1f)
                )
                ExpressiveThemePill(
                    label = strings.themeSepia,
                    icon = Icons.AutoMirrored.Filled.MenuBook,
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
                    icon = Icons.Default.BrightnessAuto,
                    isSelected = currentTheme == ReaderThemeMode.SYSTEM_DYNAMIC,
                    previewBgColor = if (sysDark) Color(0xFF252930) else Color(0xFFF0F3F6),
                    previewTextColor = if (sysDark) Color(0xFFE2E2E6) else Color(0xFF1D1B20),
                    getSheetCoordinates = { sheetCoordinates },
                    onClick = { origin -> onThemePicked(ReaderThemeMode.SYSTEM_DYNAMIC, origin, if (sysDark) darkColors else lightColors) },
                    modifier = Modifier.weight(1.35f)
                )
                ExpressiveThemePill(
                    label = strings.themeAmoled,
                    icon = Icons.Default.Contrast,
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
                else Color(0x301D212E)
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
    ExpressiveSettingsGroup {
        ExpressiveSettingsRow(
            title = strings.syncThemesTitle,
            subtitle = strings.syncThemesSubtitle,
            icon = { SettingsIconBox(Icons.Default.Palette) },
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
            icon = { SettingsIconBox(Icons.AutoMirrored.Filled.MenuBook) },
            onClick = { onOpenReaderThemeSettings() },
            trailing = {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )
        ExpressiveDivider()
        ExpressiveSettingsRow(
            title = strings.materialYouToggle,
            subtitle = strings.materialYouSubtitle,
            icon = { SettingsIconBox(Icons.Default.AutoAwesome) },
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
    }

    Spacer(modifier = Modifier.height(20.dp))
}
