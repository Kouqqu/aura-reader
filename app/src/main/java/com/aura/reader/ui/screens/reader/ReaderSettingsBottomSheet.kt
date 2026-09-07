package com.aura.reader.ui.screens.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.reader.data.model.ReaderFontFamily
import com.aura.reader.data.model.ReaderSettings
import com.aura.reader.data.model.ReaderThemeMode
import com.aura.reader.ui.theme.AmoledBackground
import com.aura.reader.ui.theme.AmoledText
import com.aura.reader.ui.theme.SepiaBackground
import com.aura.reader.ui.theme.SepiaText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderSettingsBottomSheet(
    settings: ReaderSettings,
    onDismiss: () -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onLineHeightChange: (Float) -> Unit,
    onThemeModeChange: (ReaderThemeMode) -> Unit,
    onFontFamilyChange: (ReaderFontFamily) -> Unit,
    onLightImageBackgroundChange: (Boolean) -> Unit,
    onPagingModeChange: (Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Настройки чтения",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // --- Theme Mode ---
            Text(
                text = "Тема оформления",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ThemeOptionButton(
                    label = "Material",
                    bgColor = MaterialTheme.colorScheme.primaryContainer,
                    textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    isSelected = settings.themeMode == ReaderThemeMode.SYSTEM_DYNAMIC,
                    modifier = Modifier.weight(1f),
                    onClick = { onThemeModeChange(ReaderThemeMode.SYSTEM_DYNAMIC) }
                )

                ThemeOptionButton(
                    label = "Светлая",
                    bgColor = Color(0xFFFFFFFF),
                    textColor = Color(0xFF1D1B20),
                    isSelected = settings.themeMode == ReaderThemeMode.LIGHT,
                    modifier = Modifier.weight(1f),
                    onClick = { onThemeModeChange(ReaderThemeMode.LIGHT) }
                )

                ThemeOptionButton(
                    label = "Сепия",
                    bgColor = SepiaBackground,
                    textColor = SepiaText,
                    isSelected = settings.themeMode == ReaderThemeMode.SEPIA,
                    modifier = Modifier.weight(1f),
                    onClick = { onThemeModeChange(ReaderThemeMode.SEPIA) }
                )

                ThemeOptionButton(
                    label = "AMOLED",
                    bgColor = AmoledBackground,
                    textColor = AmoledText,
                    isSelected = settings.themeMode == ReaderThemeMode.AMOLED,
                    modifier = Modifier.weight(1f),
                    onClick = { onThemeModeChange(ReaderThemeMode.AMOLED) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Font Size ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Размер шрифта",
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
                text = "Гарнитура шрифта",
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
                    label = { Text("С засечками") },
                    leadingIcon = if (settings.fontFamily == ReaderFontFamily.SERIF) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )

                FilterChip(
                    selected = settings.fontFamily == ReaderFontFamily.SANS_SERIF,
                    onClick = { onFontFamilyChange(ReaderFontFamily.SANS_SERIF) },
                    label = { Text("Без засечек") },
                    leadingIcon = if (settings.fontFamily == ReaderFontFamily.SANS_SERIF) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )

                FilterChip(
                    selected = settings.fontFamily == ReaderFontFamily.MONOSPACE,
                    onClick = { onFontFamilyChange(ReaderFontFamily.MONOSPACE) },
                    label = { Text("Моно") },
                    leadingIcon = if (settings.fontFamily == ReaderFontFamily.MONOSPACE) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Line Height ---
            Text(
                text = "Межстрочный интервал",
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
                    label = { Text("Компактный") }
                )
                FilterChip(
                    selected = settings.lineHeightMultiplier == 1.5f,
                    onClick = { onLineHeightChange(1.5f) },
                    label = { Text("Обычный") }
                )
                FilterChip(
                    selected = settings.lineHeightMultiplier == 1.8f,
                    onClick = { onLineHeightChange(1.8f) },
                    label = { Text("Просторный") }
                )
            }

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
                        text = "Постраничный режим",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (settings.pagingMode) "Листание страниц тапом по краям / свайпом" else "Непрерывная вертикальная лента",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = settings.pagingMode,
                    onCheckedChange = onPagingModeChange
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
                        text = "Светлая подложка картинок",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Оптимизация для прозрачных рисунков и формул в тёмных темах",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = settings.lightImageBackground,
                    onCheckedChange = onLightImageBackgroundChange
                )
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
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
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
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
