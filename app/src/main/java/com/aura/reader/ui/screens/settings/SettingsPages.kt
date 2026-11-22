package com.aura.reader.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aura.reader.data.model.AppDesignStyle
import com.aura.reader.data.model.AppSettings
import com.aura.reader.data.model.ReaderThemeMode
import com.aura.reader.ui.components.PixelFullScreenBurst
import com.aura.reader.ui.components.jellyOverscroll
import com.aura.reader.ui.screens.library.UpdateCheckState
import com.aura.reader.ui.theme.AppLanguage
import com.aura.reader.ui.theme.LocalAppStrings

@Composable
internal fun SettingsPageHeader(
    title: String,
    icon: ImageVector,
    isGlass: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 16.dp, bottom = 12.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(42.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Settings Page 1: Interface & Appearance.
 * Hosts app style (M3 vs Liquid Glass), themes, seasonal atmosphere, language, and fonts.
 */
@Composable
fun SettingsInterfacePage(
    appSettings: AppSettings,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    syncThemesWithApp: Boolean,
    onSyncThemesChange: (Boolean) -> Unit,
    onOpenReaderThemeSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val isGlass = appSettings.appDesignStyle == AppDesignStyle.LIQUID_GLASS
    val sysDark = isSystemInDarkTheme()
    val isThemeDark = when (appSettings.themeMode) {
        ReaderThemeMode.LIGHT, ReaderThemeMode.SEPIA -> false
        ReaderThemeMode.DARK, ReaderThemeMode.AMOLED -> true
        ReaderThemeMode.SYSTEM_DYNAMIC -> sysDark
    }
    val isSepia = appSettings.themeMode == ReaderThemeMode.SEPIA
    val isAmoled = appSettings.themeMode == ReaderThemeMode.AMOLED

    var showLanguageDialog by remember { mutableStateOf(false) }
    var sheetCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var burstTriggerKey by remember { mutableLongStateOf(0L) }
    var burstOrigin by remember { mutableStateOf(Offset.Zero) }
    var burstColors by remember { mutableStateOf(emptyList<Color>()) }

    CompositionLocalProvider(
        LocalIsLiquidGlass provides isGlass,
        LocalIsThemeDark provides isThemeDark,
        LocalIsSepia provides isSepia,
        LocalIsAmoled provides isAmoled
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .onGloballyPositioned { sheetCoordinates = it }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .jellyOverscroll(allowTopBounce = true)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                SettingsPageHeader(
                    title = strings.settingsCategoryInterface,
                    icon = Icons.Rounded.Palette,
                    isGlass = isGlass
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (!appSettings.eInkModeEnabled) {
                    SettingsDesignStyleSection(
                        appDesignStyle = appSettings.appDesignStyle,
                        isThemeDark = isThemeDark,
                        onAppDesignStyleChange = { style -> onUpdateSettings { it.copy(appDesignStyle = style) } }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                SettingsThemeSection(
                    currentTheme = appSettings.themeMode,
                    sysDark = sysDark,
                    isGlass = isGlass,
                    isThemeDark = isThemeDark,
                    isSepia = isSepia,
                    sheetCoordinates = sheetCoordinates,
                    syncThemesWithApp = syncThemesWithApp,
                    onSyncThemesChange = onSyncThemesChange,
                    materialYouEnabled = appSettings.materialYouEnabled,
                    onToggleMaterialYou = { v -> onUpdateSettings { it.copy(materialYouEnabled = v) } },
                    colorPreset = appSettings.colorPreset,
                    onColorPresetChange = { p -> onUpdateSettings { it.copy(colorPreset = p) } },
                    seasonalEffectsEnabled = appSettings.seasonalEffectsEnabled,
                    onToggleSeasonalEffects = { v -> onUpdateSettings { it.copy(seasonalEffectsEnabled = v) } },
                    holidayOverride = appSettings.holidayOverride,
                    onHolidayOverrideChange = { h -> onUpdateSettings { it.copy(holidayOverride = h) } },
                    eInkModeEnabled = appSettings.eInkModeEnabled,
                    onToggleEInkMode = { v ->
                        onUpdateSettings {
                            it.copy(
                                eInkModeEnabled = v,
                                appDesignStyle = if (v) AppDesignStyle.MATERIAL else it.appDesignStyle,
                                themeMode = if (v) ReaderThemeMode.LIGHT else it.themeMode
                            )
                        }
                    },
                    onOpenReaderThemeSettings = onOpenReaderThemeSettings,
                    onThemePicked = { mode, origin, colors ->
                        burstOrigin = origin
                        burstColors = colors
                        burstTriggerKey = System.currentTimeMillis()
                        onUpdateSettings { it.copy(themeMode = mode) }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                SettingsLanguageSection(
                    currentLanguage = appSettings.language,
                    appFont = appSettings.appFont,
                    isGlass = isGlass,
                    onAppFontChange = { f -> onUpdateSettings { it.copy(appFont = f) } },
                    onOpenLanguagePicker = { showLanguageDialog = true }
                )

                Spacer(modifier = Modifier.height(110.dp))
                Spacer(modifier = Modifier.navigationBarsPadding())
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

        if (showLanguageDialog) {
            AlertDialog(
                onDismissRequest = { showLanguageDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Translate,
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
                            val isSelected = appSettings.language == lang
                            Surface(
                                onClick = {
                                    val nextFont = if (lang != AppLanguage.UK && lang != AppLanguage.EN && appSettings.appFont == "ZAKON") {
                                        "GOOGLE_SANS"
                                    } else {
                                        appSettings.appFont
                                    }
                                    onUpdateSettings { it.copy(language = lang, appFont = nextFont) }
                                    showLanguageDialog = false
                                },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = lang.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
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
    }
}

/**
 * Settings Page 2: General & Core features.
 * Hosts reading features (stats, speed, OPDS), updates, and backup/restore.
 */
@Composable
fun SettingsGeneralPage(
    appSettings: AppSettings,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    onResetReadingSpeed: () -> Unit,
    updateCheckState: UpdateCheckState,
    onCheckUpdates: () -> Unit,
    onExportBackup: () -> Unit,
    onSendToGoogleDrive: () -> Unit,
    onRestoreBackup: () -> Unit,
    onDownloadMissingCovers: (() -> Unit)? = null,
    onDownloadMissingMetadata: (() -> Unit)? = null,
    booksWithoutCoversCount: Int = 0,
    booksWithoutMetadataCount: Int = 0,
    onClearCache: (() -> Unit)? = null,
    cacheSizeString: String = "0 КБ",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val isGlass = appSettings.appDesignStyle == AppDesignStyle.LIQUID_GLASS
    val currentAppVersion: String = remember(context) {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "2.3.0"
    }

    var showResetSpeedConfirmDialog by remember { mutableStateOf(false) }
    var showChannelDialog by remember { mutableStateOf(false) }
    var showDevPasswordDialog by remember { mutableStateOf(false) }
    var devPasswordInput by remember { mutableStateOf("") }
    var devPasswordError by remember { mutableStateOf(false) }
    var versionTapCount by remember { mutableIntStateOf(0) }

    CompositionLocalProvider(
        LocalIsLiquidGlass provides isGlass,
        LocalIsThemeDark provides isSystemInDarkTheme()
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .jellyOverscroll(allowTopBounce = true)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsPageHeader(
                title = strings.settingsCategoryGeneral,
                icon = Icons.Rounded.Tune,
                isGlass = isGlass
            )

            Spacer(modifier = Modifier.height(8.dp))

            SettingsAdditionalFeaturesSection(
                isGlass = isGlass,
                appSettings = appSettings,
                onUpdateSettings = onUpdateSettings,
                onResetReadingSpeed = { showResetSpeedConfirmDialog = true },
                onDownloadMissingCovers = onDownloadMissingCovers,
                onDownloadMissingMetadata = onDownloadMissingMetadata,
                booksWithoutCoversCount = booksWithoutCoversCount,
                booksWithoutMetadataCount = booksWithoutMetadataCount,
                onClearCache = onClearCache,
                cacheSizeString = cacheSizeString
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingsUpdatesSection(
                isGlass = isGlass,
                appSettings = appSettings,
                onUpdateSettings = onUpdateSettings,
                currentAppVersion = currentAppVersion,
                updateCheckState = updateCheckState,
                onUpdateChannelChange = { ch -> onUpdateSettings { it.copy(updateChannel = ch) } },
                onCheckUpdates = onCheckUpdates,
                onVersionTap = {
                    if (appSettings.developerModeEnabled) {
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

            Spacer(modifier = Modifier.height(16.dp))

            SettingsBackupSection(
                isGlass = isGlass,
                onExportBackup = onExportBackup,
                onSendToGoogleDrive = onSendToGoogleDrive,
                onRestoreBackup = onRestoreBackup
            )

            Spacer(modifier = Modifier.height(110.dp))
            Spacer(modifier = Modifier.navigationBarsPadding())
        }

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

        if (showChannelDialog) {
            var selectedChannel by remember { mutableStateOf(appSettings.updateChannel) }
            AlertDialog(
                onDismissRequest = { showChannelDialog = false },
                title = { Text(strings.updateChannelTitle, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
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
                            onUpdateSettings { it.copy(updateChannel = selectedChannel) }
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
                                onUpdateSettings { it.copy(developerModeEnabled = true) }
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
    }
}

/**
 * Settings Page 3: About application.
 * Hosts app info, GitHub, Telegram, donate, and reset onboarding.
 */
@Composable
fun SettingsAboutPage(
    appDesignStyle: AppDesignStyle,
    onOpenOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val isGlass = appDesignStyle == AppDesignStyle.LIQUID_GLASS

    CompositionLocalProvider(
        LocalIsLiquidGlass provides isGlass,
        LocalIsThemeDark provides isSystemInDarkTheme()
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .jellyOverscroll(allowTopBounce = true)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsPageHeader(
                title = strings.settingsCategoryAbout,
                icon = Icons.Rounded.Info,
                isGlass = isGlass
            )

            Spacer(modifier = Modifier.height(8.dp))

            SettingsAboutSection(
                isGlass = isGlass,
                isFullScreen = true,
                onDismiss = {},
                onOpenOnboarding = onOpenOnboarding
            )

            Spacer(modifier = Modifier.height(110.dp))
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}
