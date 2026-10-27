package com.aura.reader.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aura.reader.R
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.sp
import com.aura.reader.data.model.AppDesignStyle
import com.aura.reader.ui.components.LiquidGlassSurface
import com.aura.reader.ui.components.LiquidGlassSwitch
import com.aura.reader.ui.components.LiquidGlassFilterChip
import com.aura.reader.data.model.ReaderThemeMode
import com.aura.reader.data.opds.OpdsService
import com.aura.reader.ui.components.PixelFullScreenBurst
import com.aura.reader.ui.components.expressiveBounce
import com.aura.reader.ui.components.expressiveClickable
import com.aura.reader.ui.theme.AmoledBackground
import com.aura.reader.ui.theme.AmoledText
import com.aura.reader.ui.theme.AppLanguage
import com.aura.reader.ui.theme.LocalAppStrings
import com.aura.reader.ui.theme.SepiaBackground
import com.aura.reader.ui.theme.SepiaText

private val LocalIsLiquidGlass = compositionLocalOf { false }
private val LocalIsThemeDark = compositionLocalOf { true }
private val LocalIsSepia = compositionLocalOf { false }
private val LocalIsAmoled = compositionLocalOf { false }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

@Composable
fun SettingsBottomSheet(
    currentTheme: ReaderThemeMode,
    currentLanguage: AppLanguage,
    updateNotificationsEnabled: Boolean,
    readingStatsEnabled: Boolean,
    materialYouEnabled: Boolean,
    anonymousAnalyticsEnabled: Boolean = true,
    isFullScreen: Boolean = false,
    onDismiss: () -> Unit,
    onThemeChange: (ReaderThemeMode) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onToggleUpdateNotifications: (Boolean) -> Unit,
    onToggleAnonymousAnalytics: (Boolean) -> Unit = {},
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
    appDesignStyle: AppDesignStyle = AppDesignStyle.LIQUID_GLASS,
    onAppDesignStyleChange: (AppDesignStyle) -> Unit = {},
    glassClarity: Float = 0.85f,
    onGlassClarityChange: (Float) -> Unit = {},
    appFont: String = "DEFAULT",
    onAppFontChange: (String) -> Unit = {},
    onResetReadingSpeed: () -> Unit = {},
    onOpenOnboarding: () -> Unit = {}
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
    var showLanguageDialog by remember { mutableStateOf(false) }

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

    var showResetSpeedConfirmDialog by remember { mutableStateOf(false) }

    if (showResetSpeedConfirmDialog) {
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

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(strings.languageSectionTitle, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppLanguage.entries.forEach { lang ->
                        val isSelected = currentLanguage == lang
                        Surface(
                            onClick = {
                                onLanguageChange(lang)
                                showLanguageDialog = false
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = lang.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    val sheetContent = @Composable {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { sheetCoordinates = it }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (isFullScreen) Modifier.padding(top = 16.dp, bottom = 4.dp) else Modifier)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                }

            Spacer(modifier = Modifier.height(20.dp))

            val sysDark = isSystemInDarkTheme()
            val isThemeDark = when (currentTheme) {
                ReaderThemeMode.LIGHT, ReaderThemeMode.SEPIA -> false
                ReaderThemeMode.DARK, ReaderThemeMode.AMOLED -> true
                ReaderThemeMode.SYSTEM_DYNAMIC -> sysDark
            }
            val isSepia = currentTheme == ReaderThemeMode.SEPIA
            val isGlass = appDesignStyle == AppDesignStyle.LIQUID_GLASS

            SettingsDesignStyleSection(
                appDesignStyle = appDesignStyle,
                isThemeDark = isThemeDark,
                onAppDesignStyleChange = onAppDesignStyleChange
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingsThemeSection(
                currentTheme = currentTheme,
                sysDark = sysDark,
                isGlass = isGlass,
                isThemeDark = isThemeDark,
                isSepia = isSepia,
                sheetCoordinates = sheetCoordinates,
                syncThemesWithApp = syncThemesWithApp,
                onSyncThemesChange = onSyncThemesChange,
                materialYouEnabled = materialYouEnabled,
                onToggleMaterialYou = onToggleMaterialYou,
                onOpenReaderThemeSettings = onOpenReaderThemeSettings,
                onThemePicked = { mode, origin, colors ->
                    burstOrigin = origin
                    burstColors = colors
                    burstTriggerKey = System.currentTimeMillis()
                    onThemeChange(mode)
                }
            )

            SettingsLanguageSection(
                currentLanguage = currentLanguage,
                appFont = appFont,
                isGlass = isGlass,
                onAppFontChange = onAppFontChange,
                onOpenLanguagePicker = { showLanguageDialog = true }
            )

            Spacer(modifier = Modifier.height(20.dp))

            SettingsAdditionalFeaturesSection(
                isGlass = isGlass,
                readingStatsEnabled = readingStatsEnabled,
                onToggleReadingStats = onToggleReadingStats,
                customOpdsEnabled = customOpdsEnabled,
                customOpdsUrl = customOpdsUrl,
                onToggleCustomOpds = onToggleCustomOpds,
                onCustomOpdsUrlChange = onCustomOpdsUrlChange,
                onResetReadingSpeed = { showResetSpeedConfirmDialog = true }
            )

            Spacer(modifier = Modifier.height(20.dp))

            SettingsUpdatesSection(
                isGlass = isGlass,
                updateNotificationsEnabled = updateNotificationsEnabled,
                onToggleUpdateNotifications = onToggleUpdateNotifications,
                anonymousAnalyticsEnabled = anonymousAnalyticsEnabled,
                onToggleAnonymousAnalytics = onToggleAnonymousAnalytics,
                currentAppVersion = currentAppVersion,
                developerModeEnabled = developerModeEnabled,
                updateChannel = updateChannel,
                onUpdateChannelChange = onUpdateChannelChange,
                onCheckUpdates = onCheckUpdates,
                onVersionTap = {
                    if (developerModeEnabled) {
                        showChannelDialog = true
                    } else {
                        versionTapCount++
                        if (versionTapCount >= 7) {
                            versionTapCount = 0
                            showDevPasswordDialog = true
                        } else if (versionTapCount >= 3) {
                            val remaining = 7 - versionTapCount
                            Toast.makeText(context, "$remaining", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            SettingsBackupSection(
                onExportBackup = onExportBackup,
                onSendToGoogleDrive = onSendToGoogleDrive,
                onRestoreBackup = onRestoreBackup
            )

            Spacer(modifier = Modifier.height(20.dp))

            SettingsAboutSection(
                isFullScreen = isFullScreen,
                onDismiss = onDismiss,
                onOpenOnboarding = onOpenOnboarding
            )
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

    val isGlass = appDesignStyle == AppDesignStyle.LIQUID_GLASS
    val sysDark = isSystemInDarkTheme()
    val isThemeDark = when (currentTheme) {
        ReaderThemeMode.LIGHT, ReaderThemeMode.SEPIA -> false
        ReaderThemeMode.DARK, ReaderThemeMode.AMOLED -> true
        ReaderThemeMode.SYSTEM_DYNAMIC -> sysDark
    }
    val isSepia = currentTheme == ReaderThemeMode.SEPIA
    val isAmoled = currentTheme == ReaderThemeMode.AMOLED

    CompositionLocalProvider(
        LocalIsLiquidGlass provides isGlass,
        LocalIsThemeDark provides isThemeDark,
        LocalIsSepia provides isSepia,
        LocalIsAmoled provides isAmoled
    ) {
        if (isFullScreen) {
            val screenBg = if (isGlass) {
                if (isSepia) {
                    Color(0xFFFAF0E6)
                } else if (isThemeDark) {
                    if (currentTheme == ReaderThemeMode.AMOLED) Color(0xFF000000) else Color(0xFF0F1117)
                } else {
                    Color(0xFFF2F3F8)
                }
            } else {
                MaterialTheme.colorScheme.background
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(screenBg)
                    .statusBarsPadding()
            ) {
                sheetContent()
            }
        } else {
            ModalBottomSheet(
                onDismissRequest = onDismiss,
                sheetState = sheetState,
                dragHandle = { BottomSheetDefaults.DragHandle() },
                containerColor = if (isGlass) {
                    if (isSepia) {
                        Color(0xFFFAF0E6)
                    } else if (isThemeDark) {
                        if (currentTheme == ReaderThemeMode.AMOLED) Color(0xFF000000) else Color(0xFF141722)
                    } else {
                        Color(0xFFF2F3F8)
                    }
                } else MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 24.dp)
            ) {
                sheetContent()
            }
        }
    }
}


@Composable
internal fun ExpressiveThemePill(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    previewBgColor: Color,
    previewTextColor: Color,
    getSheetCoordinates: () -> LayoutCoordinates?,
    onClick: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    var pillCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isGlass = LocalIsLiquidGlass.current
    val isDark = LocalIsThemeDark.current

    val containerColor = if (isSelected) {
        previewBgColor
    } else {
        if (isGlass) {
            if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.05f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
        }
    }
    val contentColor = if (isSelected) previewTextColor else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        if (isGlass) {
            if (isDark) Color.White.copy(alpha = 0.18f) else Color.Black.copy(alpha = 0.12f)
        } else {
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
        }
    }
    val borderWidth = if (isSelected) 2.dp else 0.85.dp

    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            val sheet = getSheetCoordinates()
            val btn = pillCoordinates
            val centerOffset = if (sheet != null && sheet.isAttached && btn != null && btn.isAttached) {
                sheet.localPositionOf(btn, Offset(btn.size.width / 2f, btn.size.height / 2f))
            } else {
                Offset(btn?.size?.width?.div(2f) ?: 100f, 100f)
            }
            onClick(centerOffset)
        },
        interactionSource = interactionSource,
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(borderWidth, borderColor),
        modifier = modifier
            .height(48.dp)
            .expressiveBounce(interactionSource)
            .onGloballyPositioned { pillCoordinates = it }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = contentColor,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

@Composable
internal fun SettingsIconBox(
    icon: ImageVector,
    tint: Color = MaterialTheme.colorScheme.primary,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f),
    contentDescription: String? = null
) {
    val isGlass = LocalIsLiquidGlass.current
    val isDark = LocalIsThemeDark.current
    val bg = if (isGlass) {
        if (isDark) Color(0x35283448) else Color(0x10000000)
    } else {
        containerColor
    }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bg,
        modifier = Modifier.size(38.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
internal fun SettingsPainterIconBox(
    painter: Painter,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
    contentDescription: String? = null
) {
    val isGlass = LocalIsLiquidGlass.current
    val isDark = LocalIsThemeDark.current
    val bg = if (isGlass) {
        if (isDark) Color(0x35283448) else Color(0x10000000)
    } else {
        containerColor
    }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bg,
        modifier = Modifier.size(38.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painter,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ExpressiveStylePill(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isGlass = LocalIsLiquidGlass.current
    val isDark = LocalIsThemeDark.current

    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        if (isGlass) {
            if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
        } else MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
    }
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
    val borderWidth = if (isSelected) 1.5.dp else 1.dp

    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        interactionSource = interactionSource,
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        border = BorderStroke(borderWidth, borderColor),
        modifier = modifier
            .height(44.dp)
            .expressiveBounce(interactionSource)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = contentColor,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

@Composable
internal fun ExpressiveSettingsGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val isGlass = LocalIsLiquidGlass.current
    if (isGlass) {
        val isDark = LocalIsThemeDark.current
        val isSepia = LocalIsSepia.current
        val isAmoled = LocalIsAmoled.current
        val cardColor = if (isDark) {
            if (isAmoled) Color(0xFF141419) else Color(0xFF1F2129)
        } else if (isSepia) {
            Color(0xFFFAF2E7)
        } else {
            Color(0xFFFFFFFF)
        }
        LiquidGlassSurface(
            shape = RoundedCornerShape(22.dp),
            isDark = isDark,
            shadowElevation = if (isDark) 0.dp else 3.dp,
            tintColor = cardColor,
            modifier = modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                content = content
            )
        }
    } else {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            modifier = modifier.fillMaxWidth(),
            content = content
        )
    }
}

@Composable
internal fun ExpressiveDivider() {
    val isGlass = LocalIsLiquidGlass.current
    HorizontalDivider(
        modifier = Modifier.padding(start = 66.dp, end = 16.dp),
        color = if (isGlass) {
            if (LocalIsThemeDark.current) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f)
        } else {
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        }
    )
}

@Composable
internal fun ExpressiveSettingsRow(
    title: String,
    subtitle: String? = null,
    icon: @Composable () -> Unit,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit
) {
    val rowModifier = if (onClick != null) {
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .expressiveClickable(scaleDown = 0.98f, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    }

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f).padding(end = 12.dp)
        ) {
            icon()
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        trailing()
    }
}
