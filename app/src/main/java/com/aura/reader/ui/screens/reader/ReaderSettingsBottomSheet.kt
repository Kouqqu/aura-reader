package com.aura.reader.ui.screens.reader

import com.aura.reader.ui.components.jellyOverscroll
import com.aura.reader.ui.components.sheetScrollIsolation
import com.aura.reader.ui.components.SyncBottomSheetSystemBars
import androidx.compose.foundation.layout.statusBarsPadding

import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.fillMaxSize

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.aura.reader.ui.theme.LocalAppStrings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.isSystemInDarkTheme
import com.aura.reader.data.model.AppDesignStyle
import com.aura.reader.ui.components.LiquidGlassSwitch
import com.aura.reader.data.model.PageTurnAnimation
import com.aura.reader.data.model.ReaderFontFamily
import com.aura.reader.data.model.ReaderSettings
import com.aura.reader.data.model.ReaderThemeMode
import com.aura.reader.data.model.TwoColumnMode
import com.aura.reader.ui.components.PixelFullScreenBurst
import androidx.compose.ui.layout.LayoutCoordinates
import com.aura.reader.ui.components.triggerThemeHaptic
import com.aura.reader.ui.theme.AmoledBackground
import com.aura.reader.ui.theme.AmoledText
import com.aura.reader.ui.theme.SepiaBackground
import com.aura.reader.ui.theme.SepiaText
import android.widget.Toast
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.platform.LocalContext

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
    onHapticFeedbackChange: (Boolean) -> Unit = {},
    onFontNameChange: (String) -> Unit = {},
    onResetReadingSpeed: (() -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var sheetCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var burstTriggerKey by remember { mutableStateOf(0L) }
    var burstOrigin by remember { mutableStateOf(Offset.Zero) }
    var burstColors by remember { mutableStateOf<List<Color>>(emptyList()) }
    var showResetSpeedConfirmDialog by remember { mutableStateOf(false) }

    if (showResetSpeedConfirmDialog && onResetReadingSpeed != null) {
        AlertDialog(
            onDismissRequest = { showResetSpeedConfirmDialog = false },
            title = { Text(strings.resetReadingSpeedConfirmTitle, fontWeight = FontWeight.Bold) },
            text = { Text(strings.resetReadingSpeedConfirmMessage, style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                Button(
                    onClick = {
                        onResetReadingSpeed()
                        showResetSpeedConfirmDialog = false
                        Toast.makeText(context, strings.resetReadingSpeedSuccess, Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(strings.resetReadingSpeedTitle)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetSpeedConfirmDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    val isGlass = settings.appDesignStyle == AppDesignStyle.LIQUID_GLASS
    val sysDark = isSystemInDarkTheme()
    val isThemeDark = when (settings.themeMode) {
        ReaderThemeMode.LIGHT, ReaderThemeMode.SEPIA -> false
        ReaderThemeMode.DARK, ReaderThemeMode.AMOLED -> true
        ReaderThemeMode.SYSTEM_DYNAMIC -> sysDark
    }
    val isAmoled = settings.themeMode == ReaderThemeMode.AMOLED
    val isSepia = settings.themeMode == ReaderThemeMode.SEPIA

    // Solid opaque sheet background: ZERO bleed-through from underlying screens
    val sheetBgColor = if (isGlass) {
        if (isSepia) {
            Color(0xFFFAF0E6)
        } else if (isThemeDark) {
            if (isAmoled) Color(0xFF000000) else Color(0xFF14151C)
        } else {
            Color(0xFFF2F3F8)
        }
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }

    val sheetBorderColor = if (isGlass) {
        if (isThemeDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
    } else Color.Transparent

    // Card background with clean contrast in neutral slate grey-blue
    val cardBg = if (isGlass) {
        if (isThemeDark) {
            if (isAmoled) Color(0xFF141419) else Color(0xFF1F2129)
        } else {
            Color(0xFFFFFFFF)
        }
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    val cardBorder = if (isGlass) {
        if (isThemeDark) Color.White.copy(alpha = 0.09f) else Color.Black.copy(alpha = 0.06f)
    } else Color.Transparent

    val headerTitleColor = if (isGlass) {
        if (isThemeDark) Color.White else Color(0xFF1A1C1E)
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    // Design system purple primary accent for section titles across all modes
    val sectionTitleColor = MaterialTheme.colorScheme.primary

    val itemTitleColor = if (isGlass) {
        if (isThemeDark) Color.White else Color(0xFF1A1C1E)
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val itemSubtitleColor = if (isGlass) {
        if (isThemeDark) Color(0xFFB0B3BA) else Color(0xFF49454F)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val chipColors = FilterChipDefaults.filterChipColors(
        containerColor = if (isGlass) {
            if (isThemeDark) {
                if (isAmoled) Color(0xFF141419) else Color(0xFF1F2129)
            } else Color(0xFFEEEEF2)
        } else MaterialTheme.colorScheme.surfaceContainerLow,
        labelColor = if (isGlass) {
            if (isThemeDark) Color(0xFFE2E2E6) else Color(0xFF1C1B1F)
        } else MaterialTheme.colorScheme.onSurface,
        iconColor = MaterialTheme.colorScheme.primary,
        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
    )
    val chipBorder = if (isGlass) {
        FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = false,
            borderColor = if (isThemeDark) Color.White.copy(alpha = 0.14f) else Color.Black.copy(alpha = 0.10f),
            selectedBorderColor = MaterialTheme.colorScheme.primary
        )
    } else FilterChipDefaults.filterChipBorder(enabled = true, selected = false)

    val textFieldColors = if (isGlass) {
        OutlinedTextFieldDefaults.colors(
            focusedTextColor = itemTitleColor,
            unfocusedTextColor = itemTitleColor,
            focusedContainerColor = cardBg,
            unfocusedContainerColor = cardBg,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = if (isThemeDark) Color.White.copy(alpha = 0.20f) else Color.Black.copy(alpha = 0.15f),
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = itemSubtitleColor,
            focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
            unfocusedTrailingIconColor = itemSubtitleColor
        )
    } else {
        OutlinedTextFieldDefaults.colors()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = {
            if (isGlass) {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .size(width = 36.dp, height = 4.5.dp)
                        .clip(CircleShape)
                        .background(
                            if (isThemeDark) Color.White.copy(alpha = 0.22f) else Color.Black.copy(alpha = 0.15f)
                        )
                )
            } else {
                BottomSheetDefaults.DragHandle()
            }
        },
        containerColor = sheetBgColor,
        scrimColor = Color.Black.copy(alpha = if (isGlass) 0.70f else 0.50f),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier
            .statusBarsPadding()
            .padding(top = 24.dp)
    ) {
        SyncBottomSheetSystemBars(isThemeDark = isThemeDark)
        val contentScrollState = rememberScrollState()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { sheetCoordinates = it }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .jellyOverscroll(allowTopBounce = false)
                    .verticalScroll(contentScrollState)
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = strings.readerSettingsTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = headerTitleColor,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // --- Theme Mode ---
                Text(
                    text = strings.themeModeTitle,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = sectionTitleColor
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val lightColors = listOf(Color(0xFFFFF9E6), Color(0xFFFFE082), Color(0xFFF5F5F5), Color(0xFFE0E0E0), Color(0xFFFFFDE7), Color(0xFFFFFFFF))
                    val darkColors = listOf(Color(0xFFECEFF1), Color(0xFFCFD8DC), Color(0xFFB0BEC5), Color(0xFF90A4AE), Color(0xFF78909C), Color(0xFFFFFFFF))
                    val sepiaColors = listOf(Color(0xFFFAF0E6), Color(0xFFD7CCC8), Color(0xFFBCAAA4), Color(0xFFFFE0B2), Color(0xFFEFEBE9), Color(0xFFFFFFFF))
                    val amoledColors = listOf(Color(0xFFFFFFFF), Color(0xFFF5F5F5), Color(0xFFEEEEEE), Color(0xFFE0E0E0), Color(0xFFBDBDBD), Color(0xFFFFFFFF))

                    fun handleThemeClick(mode: ReaderThemeMode, origin: Offset, colors: List<Color>) {
                        burstOrigin = origin
                        burstColors = colors
                        burstTriggerKey = System.currentTimeMillis()
                        onThemeModeChange(mode)
                    }

                    ThemeOptionButton(
                        label = strings.themeLight,
                        bgColor = Color(0xFFFFFFFF),
                        textColor = Color(0xFF1D1B20),
                        isSelected = settings.themeMode == ReaderThemeMode.LIGHT,
                        hapticEnabled = settings.hapticFeedbackEnabled,
                        getSheetCoordinates = { sheetCoordinates },
                        isGlass = isGlass,
                        isDark = isThemeDark,
                        modifier = Modifier.weight(1f),
                        onClick = { origin -> handleThemeClick(ReaderThemeMode.LIGHT, origin, lightColors) }
                    )

                    ThemeOptionButton(
                        label = strings.themeDark,
                        bgColor = Color(0xFF1E2125),
                        textColor = Color(0xFFE2E2E6),
                        isSelected = settings.themeMode == ReaderThemeMode.DARK || settings.themeMode == ReaderThemeMode.SYSTEM_DYNAMIC,
                        hapticEnabled = settings.hapticFeedbackEnabled,
                        getSheetCoordinates = { sheetCoordinates },
                        isGlass = isGlass,
                        isDark = isThemeDark,
                        modifier = Modifier.weight(1f),
                        onClick = { origin -> handleThemeClick(ReaderThemeMode.DARK, origin, darkColors) }
                    )

                    ThemeOptionButton(
                        label = strings.themeSepia,
                        bgColor = SepiaBackground,
                        textColor = SepiaText,
                        isSelected = settings.themeMode == ReaderThemeMode.SEPIA,
                        hapticEnabled = settings.hapticFeedbackEnabled,
                        getSheetCoordinates = { sheetCoordinates },
                        isGlass = isGlass,
                        isDark = isThemeDark,
                        modifier = Modifier.weight(1f),
                        onClick = { origin -> handleThemeClick(ReaderThemeMode.SEPIA, origin, sepiaColors) }
                    )

                    ThemeOptionButton(
                        label = strings.themeAmoled,
                        bgColor = AmoledBackground,
                        textColor = AmoledText,
                        isSelected = settings.themeMode == ReaderThemeMode.AMOLED,
                        hapticEnabled = settings.hapticFeedbackEnabled,
                        getSheetCoordinates = { sheetCoordinates },
                        isGlass = isGlass,
                        isDark = isThemeDark,
                        modifier = Modifier.weight(1f),
                        onClick = { origin -> handleThemeClick(ReaderThemeMode.AMOLED, origin, amoledColors) }
                    )
                }

                if (onMaterialYouChange != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    androidx.compose.material3.Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = cardBg,
                        border = if (isGlass) BorderStroke(0.6.dp, cardBorder) else null,
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
                                    color = itemTitleColor
                                )
                                Text(
                                    text = strings.materialYouSubtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = itemSubtitleColor
                                )
                            }
                            if (isGlass) {
                                LiquidGlassSwitch(
                                    checked = materialYouEnabled,
                                    onCheckedChange = onMaterialYouChange
                                )
                            } else {
                                Switch(
                                    checked = materialYouEnabled,
                                    onCheckedChange = onMaterialYouChange
                                )
                            }
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
                        fontWeight = FontWeight.Bold,
                        color = sectionTitleColor
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
                    Text("A", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = itemSubtitleColor)
                    Slider(
                        value = settings.fontSizeSp,
                        onValueChange = onFontSizeChange,
                        valueRange = 12f..32f,
                        steps = 9,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                    )
                    Text("A", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = itemTitleColor)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- Font Family ---
                Text(
                    text = strings.fontFamilyTitle,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = sectionTitleColor
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = settings.fontFamily == ReaderFontFamily.SERIF,
                        onClick = {
                            onFontFamilyChange(ReaderFontFamily.SERIF)
                        },
                        label = { Text(strings.fontFamilySerif, maxLines = 1, softWrap = false) },
                        colors = chipColors,
                        border = chipBorder,
                        leadingIcon = if (settings.fontFamily == ReaderFontFamily.SERIF) {
                            { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )

                    FilterChip(
                        selected = settings.fontFamily == ReaderFontFamily.SANS_SERIF,
                        onClick = {
                            onFontFamilyChange(ReaderFontFamily.SANS_SERIF)
                        },
                        label = { Text(strings.fontFamilySansSerif, maxLines = 1, softWrap = false) },
                        colors = chipColors,
                        border = chipBorder,
                        leadingIcon = if (settings.fontFamily == ReaderFontFamily.SANS_SERIF) {
                            { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )

                    FilterChip(
                        selected = settings.fontFamily == ReaderFontFamily.MONOSPACE,
                        onClick = {
                            onFontFamilyChange(ReaderFontFamily.MONOSPACE)
                        },
                        label = { Text(strings.fontFamilyMonospace, maxLines = 1, softWrap = false) },
                        colors = chipColors,
                        border = chipBorder,
                        leadingIcon = if (settings.fontFamily == ReaderFontFamily.MONOSPACE) {
                            { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }

                // Specific Font Typeface Dropdown
                val customFonts = remember(context) {
                    val dir = java.io.File(context.filesDir, "custom_fonts")
                    dir.listFiles { file ->
                        file.isFile && (file.extension.equals("ttf", ignoreCase = true) || file.extension.equals("otf", ignoreCase = true))
                    }?.sortedBy { it.nameWithoutExtension.lowercase() } ?: emptyList()
                }

                val fontChoices = (when (settings.fontFamily) {
                    ReaderFontFamily.SERIF -> listOf(strings.fontDefault, "Literata", "PT Serif", "Lora")
                    ReaderFontFamily.SANS_SERIF -> listOf(strings.fontDefault, "Google Sans", "Inter")
                    ReaderFontFamily.MONOSPACE -> listOf(strings.fontDefault, "JetBrains Mono")
                    ReaderFontFamily.SYSTEM_DEFAULT -> listOf(strings.fontDefault)
                }) + customFonts.map { "CUSTOM:${it.name}" }

                var fontDropdownExpanded by remember { mutableStateOf(false) }
                val currentFontLabel = when {
                    settings.fontName.isBlank() || settings.fontName == strings.fontDefault -> strings.fontDefault
                    settings.fontName.startsWith("CUSTOM:") -> settings.fontName.removePrefix("CUSTOM:").substringBeforeLast(".")
                    else -> settings.fontName
                }

                ExposedDropdownMenuBox(
                    expanded = fontDropdownExpanded,
                    onExpandedChange = { fontDropdownExpanded = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    OutlinedTextField(
                        value = currentFontLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(strings.specificFontTitle) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fontDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors
                    )
                    ExposedDropdownMenu(
                        expanded = fontDropdownExpanded,
                        onDismissRequest = { fontDropdownExpanded = false }
                    ) {
                        fontChoices.forEach { fontOption ->
                            val isSelected = (settings.fontName.isBlank() && fontOption == strings.fontDefault) || settings.fontName == fontOption
                            val fontDisplay = if (fontOption.startsWith("CUSTOM:")) {
                                fontOption.removePrefix("CUSTOM:").substringBeforeLast(".")
                            } else {
                                fontOption
                            }
                            DropdownMenuItem(
                                text = { Text(fontDisplay) },
                                onClick = {
                                    val toSave = if (fontOption == strings.fontDefault) "" else fontOption
                                    onFontNameChange(toSave)
                                    fontDropdownExpanded = false
                                },
                                trailingIcon = if (isSelected) {
                                    { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- Line Height ---
                Text(
                    text = strings.lineHeightTitle,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = sectionTitleColor
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = settings.lineHeightMultiplier == 1.2f,
                        onClick = { onLineHeightChange(1.2f) },
                        label = { Text("1.2x", maxLines = 1, softWrap = false) },
                        colors = chipColors,
                        border = chipBorder
                    )
                    FilterChip(
                        selected = settings.lineHeightMultiplier == 1.5f,
                        onClick = { onLineHeightChange(1.5f) },
                        label = { Text("1.5x", maxLines = 1, softWrap = false) },
                        colors = chipColors,
                        border = chipBorder
                    )
                    FilterChip(
                        selected = settings.lineHeightMultiplier == 1.8f,
                        onClick = { onLineHeightChange(1.8f) },
                        label = { Text("1.8x", maxLines = 1, softWrap = false) },
                        colors = chipColors,
                        border = chipBorder
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- Reading Mode (Continuous / Paging) ---
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = cardBg,
                    border = if (isGlass) BorderStroke(0.6.dp, cardBorder) else null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPagingModeChange(!settings.pagingMode) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = strings.readingModeTitle,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = itemTitleColor
                            )
                            Text(
                                text = if (settings.pagingMode) strings.readingModePaged else strings.readingModeScroll,
                                style = MaterialTheme.typography.bodySmall,
                                color = itemSubtitleColor
                            )
                        }
                        if (isGlass) {
                            LiquidGlassSwitch(
                                checked = settings.pagingMode,
                                onCheckedChange = onPagingModeChange
                            )
                        } else {
                            Switch(
                                checked = settings.pagingMode,
                                onCheckedChange = onPagingModeChange
                            )
                        }
                    }
                }

                if (settings.pagingMode) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = strings.pageAnimationSectionTitle,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = sectionTitleColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = settings.pageAnimation == PageTurnAnimation.SLIDE,
                            onClick = { onPageAnimationChange(PageTurnAnimation.SLIDE) },
                            label = { Text(strings.pageAnimationSlide, maxLines = 1, softWrap = false) },
                            colors = chipColors,
                            border = chipBorder
                        )
                        FilterChip(
                            selected = settings.pageAnimation == PageTurnAnimation.INSTANT,
                            onClick = { onPageAnimationChange(PageTurnAnimation.INSTANT) },
                            label = { Text(strings.pageAnimationInstant, maxLines = 1, softWrap = false) },
                            colors = chipColors,
                            border = chipBorder
                        )
                        FilterChip(
                            selected = settings.pageAnimation == PageTurnAnimation.CURL,
                            onClick = { onPageAnimationChange(PageTurnAnimation.CURL) },
                            label = { Text(strings.pageAnimationCurl, maxLines = 1, softWrap = false) },
                            colors = chipColors,
                            border = chipBorder
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = strings.twoColumnSpreadSectionTitle,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = sectionTitleColor
                    )
                    Text(
                        text = strings.twoColumnSpreadSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = itemSubtitleColor,
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
                            label = { Text(strings.twoColumnSpreadAuto, maxLines = 1, softWrap = false) },
                            colors = chipColors,
                            border = chipBorder
                        )
                        FilterChip(
                            selected = settings.twoColumnMode == TwoColumnMode.OFF,
                            onClick = { onTwoColumnModeChange(TwoColumnMode.OFF) },
                            label = { Text(strings.twoColumnSpreadOff, maxLines = 1, softWrap = false) },
                            colors = chipColors,
                            border = chipBorder
                        )
                        FilterChip(
                            selected = settings.twoColumnMode == TwoColumnMode.ALWAYS,
                            onClick = { onTwoColumnModeChange(TwoColumnMode.ALWAYS) },
                            label = { Text(strings.twoColumnSpreadAlways, maxLines = 1, softWrap = false) },
                            colors = chipColors,
                            border = chipBorder
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- Haptic Feedback ---
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = cardBg,
                    border = if (isGlass) BorderStroke(0.6.dp, cardBorder) else null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onHapticFeedbackChange(!settings.hapticFeedbackEnabled) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = strings.hapticFeedbackTitle,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = itemTitleColor
                            )
                            Text(
                                text = strings.hapticFeedbackSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = itemSubtitleColor
                            )
                        }
                        if (isGlass) {
                            LiquidGlassSwitch(
                                checked = settings.hapticFeedbackEnabled,
                                onCheckedChange = onHapticFeedbackChange
                            )
                        } else {
                            Switch(
                                checked = settings.hapticFeedbackEnabled,
                                onCheckedChange = onHapticFeedbackChange
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- Auto Hyphenation ---
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = cardBg,
                    border = if (isGlass) BorderStroke(0.6.dp, cardBorder) else null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAutoHyphenationChange(!settings.autoHyphenation) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = strings.autoHyphenationTitle,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = itemTitleColor
                            )
                            Text(
                                text = strings.autoHyphenationSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = itemSubtitleColor
                            )
                        }
                        if (isGlass) {
                            LiquidGlassSwitch(
                                checked = settings.autoHyphenation,
                                onCheckedChange = onAutoHyphenationChange
                            )
                        } else {
                            Switch(
                                checked = settings.autoHyphenation,
                                onCheckedChange = onAutoHyphenationChange
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- Light Background for Illustrations in Dark Themes ---
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = cardBg,
                    border = if (isGlass) BorderStroke(0.6.dp, cardBorder) else null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLightImageBackgroundChange(!settings.lightImageBackground) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = strings.lightImageBgTitle,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = itemTitleColor
                            )
                            Text(
                                text = strings.lightImageBgSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = itemSubtitleColor
                            )
                        }
                        if (isGlass) {
                            LiquidGlassSwitch(
                                checked = settings.lightImageBackground,
                                onCheckedChange = onLightImageBackgroundChange
                            )
                        } else {
                            Switch(
                                checked = settings.lightImageBackground,
                                onCheckedChange = onLightImageBackgroundChange
                            )
                        }
                    }
                }

                if (onResetReadingSpeed != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { showResetSpeedConfirmDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = if (isGlass) {
                            ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        } else ButtonDefaults.outlinedButtonColors(),
                        border = if (isGlass) {
                            BorderStroke(0.8.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                        } else ButtonDefaults.outlinedButtonBorder(enabled = true),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Speed,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.resetReadingSpeedTitle)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Spacer(modifier = Modifier.navigationBarsPadding())
                Spacer(modifier = Modifier.height(32.dp))
            }

            if (burstTriggerKey > 0L && burstColors.isNotEmpty()) {
                PixelFullScreenBurst(
                    triggerKey = burstTriggerKey,
                    origin = burstOrigin,
                    colors = burstColors,
                    durationMillis = 2400,
                    modifier = Modifier.matchParentSize()
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
    hapticEnabled: Boolean,
    getSheetCoordinates: () -> LayoutCoordinates?,
    isGlass: Boolean = false,
    isDark: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: (Offset) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    var buttonCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    val border = if (isSelected) {
        androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else if (isGlass) {
        androidx.compose.foundation.BorderStroke(0.6.dp, if (isDark) Color.White.copy(alpha = 0.14f) else Color.Black.copy(alpha = 0.08f))
    } else {
        androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    }

    Surface(
        onClick = {
            if (hapticEnabled) {
                triggerThemeHaptic(context)
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
            }
            val sheet = getSheetCoordinates()
            val btn = buttonCoordinates
            val centerOffset = if (sheet != null && sheet.isAttached && btn != null && btn.isAttached) {
                sheet.localPositionOf(btn, Offset(btn.size.width / 2f, btn.size.height / 2f))
            } else {
                Offset(btn?.size?.width?.div(2f) ?: 100f, 100f)
            }
            onClick(centerOffset)
        },
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        border = border,
        modifier = modifier
            .height(52.dp)
            .onGloballyPositioned { coords ->
                buttonCoordinates = coords
            }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
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
}
