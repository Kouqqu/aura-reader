package com.aura.reader.ui.screens.settings

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aura.reader.R
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import com.aura.reader.ui.components.PixelFullScreenBurst
import com.aura.reader.ui.screens.reader.ThemeOptionButton
import com.aura.reader.ui.theme.AmoledBackground
import com.aura.reader.ui.theme.AmoledText
import com.aura.reader.ui.theme.SepiaBackground
import com.aura.reader.ui.theme.SepiaText
import com.aura.reader.data.model.ReaderThemeMode
import com.aura.reader.data.opds.OpdsService
import com.aura.reader.ui.theme.AppLanguage
import com.aura.reader.ui.theme.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsBottomSheet(
    currentTheme: ReaderThemeMode,
    currentLanguage: AppLanguage,
    updateNotificationsEnabled: Boolean,
    readingStatsEnabled: Boolean,
    materialYouEnabled: Boolean,
    onDismiss: () -> Unit,
    onThemeChange: (ReaderThemeMode) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onToggleUpdateNotifications: (Boolean) -> Unit,
    onToggleReadingStats: (Boolean) -> Unit,
    onToggleMaterialYou: (Boolean) -> Unit,
    onCheckUpdates: () -> Unit,
    customOpdsEnabled: Boolean = false,
    customOpdsUrl: String = "",
    onToggleCustomOpds: (Boolean) -> Unit = {},
    onCustomOpdsUrlChange: (String) -> Unit = {},
    onExportBackup: () -> Unit = {},
    onSendToGoogleDrive: () -> Unit = {},
    onRestoreBackup: () -> Unit = {},
    developerModeEnabled: Boolean = false,
    updateChannel: String = "RELEASE",
    onToggleDeveloperMode: (Boolean) -> Unit = {},
    onUpdateChannelChange: (String) -> Unit = {},
    readerTheme: ReaderThemeMode = ReaderThemeMode.SYSTEM_DYNAMIC,
    syncThemesWithApp: Boolean = false,
    onSyncThemesChange: (Boolean) -> Unit = {},
    onReaderThemeChange: (ReaderThemeMode) -> Unit = {},
    onOpenReaderThemeSettings: () -> Unit = {},
    appFont: String = "DEFAULT",
    onAppFontChange: (String) -> Unit = {},
    onResetReadingSpeed: () -> Unit = {}
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val currentAppVersion = remember(context) {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "1.1.9"
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var sheetCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var burstOrigin by remember { mutableStateOf(Offset.Zero) }
    var burstColors by remember { mutableStateOf<List<Color>>(emptyList()) }
    var burstTriggerKey by remember { mutableLongStateOf(0L) }

    var versionTapCount by remember { mutableIntStateOf(0) }
    var showDevPasswordDialog by remember { mutableStateOf(false) }
    var showChannelDialog by remember { mutableStateOf(false) }
    var devPasswordInput by remember { mutableStateOf("") }
    var devPasswordError by remember { mutableStateOf(false) }

    if (showChannelDialog) {
        var selectedChannel by remember { mutableStateOf(updateChannel) }
        AlertDialog(
            onDismissRequest = { showChannelDialog = false },
            title = { Text(strings.updateChannelTitle, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(strings.channelSwitchPrompt, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedChannel = "RELEASE" }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedChannel == "RELEASE",
                            onClick = { selectedChannel = "RELEASE" }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(strings.updateChannelRelease, fontWeight = FontWeight.SemiBold)
                            Text("Официальные стабильные сборки", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedChannel = "BETA" }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedChannel == "BETA",
                            onClick = { selectedChannel = "BETA" }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(strings.updateChannelBeta, fontWeight = FontWeight.SemiBold)
                            Text("Предварительные сборки с новыми функциями", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateChannelChange(selectedChannel)
                        showChannelDialog = false
                        onCheckUpdates()
                        val channelName = if (selectedChannel == "BETA") strings.updateChannelBeta else strings.updateChannelRelease
                        Toast.makeText(context, "$channelName. Проверка обновлений...", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(strings.save)
                }
            },
            dismissButton = {
                TextButton(onClick = { showChannelDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    if (showDevPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showDevPasswordDialog = false },
            title = { Text(strings.devModeTitle, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(strings.devModePasswordPrompt, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = devPasswordInput,
                        onValueChange = {
                            devPasswordInput = it
                            devPasswordError = false
                        },
                        placeholder = { Text(strings.devModePasswordPlaceholder) },
                        singleLine = true,
                        isError = devPasswordError,
                        supportingText = if (devPasswordError) {
                            { Text(strings.devModeWrongPassword, color = MaterialTheme.colorScheme.error) }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (devPasswordInput.trim().equals("aura", ignoreCase = true)) {
                            onToggleDeveloperMode(true)
                            showDevPasswordDialog = false
                            devPasswordInput = ""
                            showChannelDialog = true
                        } else {
                            devPasswordError = true
                        }
                    }
                ) {
                    Text(strings.save)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDevPasswordDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }

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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { sheetCoordinates = it }
        ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = strings.settingsTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 1. Theme Section
            Text(
                text = strings.themeSectionTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))

            val lightColors = listOf(Color(0xFFFFF9E6), Color(0xFFFFE082), Color(0xFFF5F5F5), Color(0xFFE0E0E0), Color(0xFFFFFDE7), Color(0xFFFFFFFF))
            val darkColors = listOf(Color(0xFFECEFF1), Color(0xFFCFD8DC), Color(0xFFB0BEC5), Color(0xFF90A4AE), Color(0xFF78909C), Color(0xFFFFFFFF))
            val sepiaColors = listOf(Color(0xFFFAF0E6), Color(0xFFD7CCC8), Color(0xFFBCAAA4), Color(0xFFFFE0B2), Color(0xFFEFEBE9), Color(0xFFFFFFFF))
            val amoledColors = listOf(Color(0xFFFFFFFF), Color(0xFFF5F5F5), Color(0xFFEEEEEE), Color(0xFFE0E0E0), Color(0xFFBDBDBD), Color(0xFFFFFFFF))

            fun handleThemeClick(mode: ReaderThemeMode, origin: Offset, colors: List<Color>) {
                burstOrigin = origin
                burstColors = colors
                burstTriggerKey = System.currentTimeMillis()
                onThemeChange(mode)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ThemeOptionButton(
                    label = strings.themeLight,
                    bgColor = Color(0xFFFFFFFF),
                    textColor = Color(0xFF1D1B20),
                    isSelected = currentTheme == ReaderThemeMode.LIGHT,
                    hapticEnabled = true,
                    getSheetCoordinates = { sheetCoordinates },
                    modifier = Modifier.weight(1f),
                    onClick = { origin -> handleThemeClick(ReaderThemeMode.LIGHT, origin, lightColors) }
                )

                ThemeOptionButton(
                    label = strings.themeDark,
                    bgColor = Color(0xFF1E2125),
                    textColor = Color(0xFFE2E2E6),
                    isSelected = currentTheme == ReaderThemeMode.DARK,
                    hapticEnabled = true,
                    getSheetCoordinates = { sheetCoordinates },
                    modifier = Modifier.weight(1f),
                    onClick = { origin -> handleThemeClick(ReaderThemeMode.DARK, origin, darkColors) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ThemeOptionButton(
                    label = strings.themeSepia,
                    bgColor = SepiaBackground,
                    textColor = SepiaText,
                    isSelected = currentTheme == ReaderThemeMode.SEPIA,
                    hapticEnabled = true,
                    getSheetCoordinates = { sheetCoordinates },
                    modifier = Modifier.weight(1f),
                    onClick = { origin -> handleThemeClick(ReaderThemeMode.SEPIA, origin, sepiaColors) }
                )

                ThemeOptionButton(
                    label = strings.themeAmoled,
                    bgColor = AmoledBackground,
                    textColor = AmoledText,
                    isSelected = currentTheme == ReaderThemeMode.AMOLED,
                    hapticEnabled = true,
                    getSheetCoordinates = { sheetCoordinates },
                    modifier = Modifier.weight(1f),
                    onClick = { origin -> handleThemeClick(ReaderThemeMode.AMOLED, origin, amoledColors) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // System Auto-Theme Button
            val isSysDark = androidx.compose.foundation.isSystemInDarkTheme()
            ThemeOptionButton(
                label = "${strings.themeSystem} (${if (isSysDark) strings.themeDark else strings.themeLight})",
                bgColor = if (isSysDark) Color(0xFF252930) else Color(0xFFF0F3F6),
                textColor = if (isSysDark) Color(0xFFE2E2E6) else Color(0xFF1D1B20),
                isSelected = currentTheme == ReaderThemeMode.SYSTEM_DYNAMIC,
                hapticEnabled = true,
                getSheetCoordinates = { sheetCoordinates },
                modifier = Modifier.fillMaxWidth(),
                onClick = { origin -> handleThemeClick(ReaderThemeMode.SYSTEM_DYNAMIC, origin, if (isSysDark) darkColors else lightColors) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Sync Themes Toggle Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSyncThemesChange(!syncThemesWithApp) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp)
                    ) {
                        Text(
                            text = strings.syncThemesTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = strings.syncThemesSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = syncThemesWithApp,
                        onCheckedChange = onSyncThemesChange
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Reader Theme Settings Navigation Button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenReaderThemeSettings() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = strings.readerThemeSettingsTitle,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = strings.readerThemeSettingsSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Material You Switch Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleMaterialYou(!materialYouEnabled) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp)
                    ) {
                        Text(
                            text = strings.materialYouToggle,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = strings.materialYouSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

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

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(20.dp))

            // 2. Language Section
            Text(
                text = strings.languageSectionTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppLanguage.entries.forEach { lang ->
                    val selected = currentLanguage == lang
                    FilterChip(
                        selected = selected,
                        onClick = { onLanguageChange(lang) },
                        label = { Text(lang.title) },
                        leadingIcon = if (selected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(20.dp))

            // App Font Section
            Text(
                text = strings.appFontTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = strings.appFontSubtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = appFont == "DEFAULT",
                    onClick = { onAppFontChange("DEFAULT") },
                    label = { Text(strings.appFontDefault) },
                    leadingIcon = if (appFont == "DEFAULT") {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )

                FilterChip(
                    selected = appFont == "GOOGLE_SANS",
                    onClick = { onAppFontChange("GOOGLE_SANS") },
                    label = { Text(strings.appFontGoogleSans) },
                    leadingIcon = if (appFont == "GOOGLE_SANS") {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(20.dp))

            // 3. Additional Features Section (Reading stats & Custom catalog)
            Text(
                text = strings.additionalFeaturesSectionTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Feature 1: Reading Stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = strings.readingStatsToggle,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = strings.readingStatsSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = readingStatsEnabled,
                            onCheckedChange = onToggleReadingStats,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }

                    if (readingStatsEnabled) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                onResetReadingSpeed()
                                Toast.makeText(context, strings.resetReadingSpeedSuccess, Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.resetReadingSpeedTitle)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Feature 2: Custom OPDS Catalog
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = strings.customOpdsToggle,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = strings.customOpdsSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = customOpdsEnabled,
                            onCheckedChange = onToggleCustomOpds,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }

                    if (customOpdsEnabled) {
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(14.dp))

                        var urlText by remember(customOpdsUrl) {
                            mutableStateOf(if (customOpdsUrl == OpdsService.DEFAULT_BASE_URL || customOpdsUrl.contains("flibusta", ignoreCase = true)) "" else customOpdsUrl)
                        }

                        OutlinedTextField(
                            value = urlText,
                            onValueChange = {
                                urlText = it
                                onCustomOpdsUrlChange(it)
                            },
                            placeholder = { Text("https://example.com/opds") },
                            label = { Text(strings.customOpdsUrl) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                if (urlText.isNotBlank()) {
                                    IconButton(onClick = {
                                        urlText = ""
                                        onCustomOpdsUrlChange("")
                                    }) {
                                        Icon(Icons.Default.Clear, contentDescription = strings.clear)
                                    }
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(20.dp))

            // 4. Updates Section
            Text(
                text = strings.updatesSectionTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = strings.updateNotificationsToggle,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = strings.updateNotificationsSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = updateNotificationsEnabled,
                            onCheckedChange = onToggleUpdateNotifications,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = strings.currentVersion(currentAppVersion),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.clickable {
                                if (developerModeEnabled) {
                                    showChannelDialog = true
                                } else {
                                    versionTapCount++
                                    if (versionTapCount >= 7) {
                                        versionTapCount = 0
                                        showDevPasswordDialog = true
                                    } else if (versionTapCount >= 3) {
                                        val remaining = 7 - versionTapCount
                                        Toast.makeText(context, "Осталось нажать $remaining раз(а)", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )

                        OutlinedButton(
                            onClick = onCheckUpdates,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.checkUpdatesNow)
                        }
                    }

                    if (developerModeEnabled) {
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = strings.updateChannelTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = strings.updateChannelSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = updateChannel == "RELEASE",
                                onClick = {
                                    onUpdateChannelChange("RELEASE")
                                    onCheckUpdates()
                                    Toast.makeText(context, "${strings.updateChannelRelease}. Проверка обновлений...", Toast.LENGTH_SHORT).show()
                                },
                                label = { Text(strings.updateChannelRelease) }
                            )
                            FilterChip(
                                selected = updateChannel == "BETA",
                                onClick = {
                                    onUpdateChannelChange("BETA")
                                    onCheckUpdates()
                                    Toast.makeText(context, "${strings.updateChannelBeta}. Проверка обновлений...", Toast.LENGTH_SHORT).show()
                                },
                                label = { Text(strings.updateChannelBeta) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(20.dp))

            // 5. Backup & Sync Section
            Text(
                text = strings.backupSectionTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Export to local file / Google Drive SAF
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onExportBackup),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.createBackupTitle,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = strings.createBackupSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // Send to Google Drive via Android Share
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onSendToGoogleDrive),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.sendToGoogleDriveTitle,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = strings.sendToGoogleDriveSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // Restore from backup
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onRestoreBackup),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.restoreBackupTitle,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = strings.restoreBackupSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(20.dp))

            // 6. About, GitHub & Donate Section
            Text(
                text = strings.aboutSectionTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Donate Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://dalink.to/koukku"))
                        context.startActivity(intent)
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = strings.donate,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.donate,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "https://dalink.to/koukku",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // GitHub Repository Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Kouqqu/aura-reader"))
                        context.startActivity(intent)
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_github),
                        contentDescription = "GitHub",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.githubRepository,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "https://github.com/Kouqqu/aura-reader",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
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
