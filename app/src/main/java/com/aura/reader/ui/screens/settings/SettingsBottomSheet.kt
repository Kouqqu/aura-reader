package com.aura.reader.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MenuBook
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
import androidx.compose.ui.unit.dp
import com.aura.reader.R
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import com.aura.reader.data.model.AppDesignStyle
import com.aura.reader.ui.components.NavigationIslandPreviewCard
import com.aura.reader.ui.components.LiquidGlassSurface
import com.aura.reader.data.model.ReaderThemeMode
import com.aura.reader.data.opds.OpdsService
import com.aura.reader.ui.components.PixelFullScreenBurst
import com.aura.reader.ui.components.expressiveBounce
import com.aura.reader.ui.theme.AmoledBackground
import com.aura.reader.ui.theme.AmoledText
import com.aura.reader.ui.theme.AppLanguage
import com.aura.reader.ui.theme.LocalAppStrings
import com.aura.reader.ui.theme.SepiaBackground
import com.aura.reader.ui.theme.SepiaText

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

@Composable
fun SettingsBottomSheet(
    currentTheme: ReaderThemeMode,
    currentLanguage: AppLanguage,
    updateNotificationsEnabled: Boolean,
    readingStatsEnabled: Boolean,
    materialYouEnabled: Boolean,
    anonymousAnalyticsEnabled: Boolean = true,
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
    appDesignStyle: AppDesignStyle = AppDesignStyle.MATERIAL,
    onAppDesignStyleChange: (AppDesignStyle) -> Unit = {},
    glassClarity: Float = 0.85f,
    onGlassClarityChange: (Float) -> Unit = {},
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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = if (appDesignStyle == AppDesignStyle.LIQUID_GLASS) {
            if (androidx.compose.foundation.isSystemInDarkTheme()) Color(0xFF0D0E12) else Color(0xFFF2F3F8)
        } else MaterialTheme.colorScheme.surfaceContainerHigh,
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

            val isSysDark = isSystemInDarkTheme()

            // 0. Design Style Section (Material 3 vs Mornye Liquid Glass)
            Text(
                text = strings.designStyleTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Option 1: Material 3
                val isMatSelected = appDesignStyle == AppDesignStyle.MATERIAL
                Card(
                    onClick = { onAppDesignStyleChange(AppDesignStyle.MATERIAL) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isMatSelected) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
                        } else {
                            if (isSysDark) Color(0x661E2028) else Color(0x66FFFFFF)
                        }
                    ),
                    border = BorderStroke(
                        if (isMatSelected) 2.dp else 1.dp,
                        if (isMatSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = if (isSysDark) 0.15f else 0.45f)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            if (isMatSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = strings.designStyleMaterial,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isMatSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = strings.designStyleDescMaterial,
                            style = MaterialTheme.typography.bodySmall,
                            color = (if (isMatSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface).copy(alpha = 0.70f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Option 2: Liquid Glass (Mornye / Apple)
                val isGlassSelected = appDesignStyle == AppDesignStyle.LIQUID_GLASS
                Card(
                    onClick = { onAppDesignStyleChange(AppDesignStyle.LIQUID_GLASS) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isGlassSelected) {
                            if (isSysDark) Color(0xA6202432) else Color(0xD9FFFFFF)
                        } else {
                            if (isSysDark) Color(0x661E2028) else Color(0x66FFFFFF)
                        }
                    ),
                    border = BorderStroke(
                        if (isGlassSelected) 2.dp else 1.dp,
                        if (isGlassSelected) {
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = if (isSysDark) 0.85f else 0.95f),
                                    Color.White.copy(alpha = 0.20f)
                                )
                            )
                        } else {
                            SolidColor(Color.White.copy(alpha = if (isSysDark) 0.15f else 0.45f))
                        }
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                modifier = Modifier.height(22.dp)
                            ) {
                                Text(
                                    text = "iOS",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = strings.designStyleLiquidGlass,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = strings.designStyleDescLiquidGlass,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.70f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Live Interactive Preview of the Island
            NavigationIslandPreviewCard(
                designStyle = appDesignStyle,
                clarity = glassClarity
            )

            // Glass Clarity slider (only shown when Liquid Glass is active)
            if (appDesignStyle == AppDesignStyle.LIQUID_GLASS) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSysDark) Color(0x66181A22) else Color(0x80FFFFFF)
                    ),
                    border = BorderStroke(
                        1.dp,
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = if (isSysDark) 0.35f else 0.65f),
                                Color.White.copy(alpha = 0.08f)
                            )
                        )
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = strings.glassClarityTitle,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${(glassClarity * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = strings.glassClarityDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = glassClarity,
                            onValueChange = onGlassClarityChange,
                            valueRange = 0.20f..1.00f,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Theme Section
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

            Text(
                text = strings.themeSectionTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            // Segmented Theme Switcher Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
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
                            onClick = { origin -> handleThemeClick(ReaderThemeMode.LIGHT, origin, lightColors) },
                            modifier = Modifier.weight(1f)
                        )
                        ExpressiveThemePill(
                            label = strings.themeDark,
                            icon = Icons.Default.DarkMode,
                            isSelected = currentTheme == ReaderThemeMode.DARK,
                            previewBgColor = Color(0xFF1E2125),
                            previewTextColor = Color(0xFFE2E2E6),
                            getSheetCoordinates = { sheetCoordinates },
                            onClick = { origin -> handleThemeClick(ReaderThemeMode.DARK, origin, darkColors) },
                            modifier = Modifier.weight(1f)
                        )
                        ExpressiveThemePill(
                            label = strings.themeSepia,
                            icon = Icons.Default.MenuBook,
                            isSelected = currentTheme == ReaderThemeMode.SEPIA,
                            previewBgColor = SepiaBackground,
                            previewTextColor = SepiaText,
                            getSheetCoordinates = { sheetCoordinates },
                            onClick = { origin -> handleThemeClick(ReaderThemeMode.SEPIA, origin, sepiaColors) },
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
                            previewBgColor = if (isSysDark) Color(0xFF252930) else Color(0xFFF0F3F6),
                            previewTextColor = if (isSysDark) Color(0xFFE2E2E6) else Color(0xFF1D1B20),
                            getSheetCoordinates = { sheetCoordinates },
                            onClick = { origin -> handleThemeClick(ReaderThemeMode.SYSTEM_DYNAMIC, origin, if (isSysDark) darkColors else lightColors) },
                            modifier = Modifier.weight(1.35f)
                        )
                        ExpressiveThemePill(
                            label = strings.themeAmoled,
                            icon = Icons.Default.Contrast,
                            isSelected = currentTheme == ReaderThemeMode.AMOLED,
                            previewBgColor = AmoledBackground,
                            previewTextColor = AmoledText,
                            getSheetCoordinates = { sheetCoordinates },
                            onClick = { origin -> handleThemeClick(ReaderThemeMode.AMOLED, origin, amoledColors) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
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
                        Switch(
                            checked = syncThemesWithApp,
                            onCheckedChange = onSyncThemesChange
                        )
                    }
                )
                ExpressiveDivider()
                ExpressiveSettingsRow(
                    title = strings.readerThemeSettingsTitle,
                    subtitle = strings.readerThemeSettingsSubtitle,
                    icon = { SettingsIconBox(Icons.Default.MenuBook) },
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
                        Switch(
                            checked = materialYouEnabled,
                            onCheckedChange = onToggleMaterialYou,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Language & Fonts Section
            Text(
                text = strings.languageSectionTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            ExpressiveSettingsGroup {
                ExpressiveSettingsRow(
                    title = strings.languageSectionTitle,
                    subtitle = currentLanguage.title,
                    icon = { SettingsIconBox(Icons.Default.Translate) },
                    onClick = { showLanguageDialog = true },
                    trailing = {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = currentLanguage.title,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                )
                ExpressiveDivider()
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = strings.appFontTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
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
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Additional Features Section
            Text(
                text = strings.additionalFeaturesSectionTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            ExpressiveSettingsGroup {
                ExpressiveSettingsRow(
                    title = strings.readingStatsToggle,
                    subtitle = strings.readingStatsSubtitle,
                    icon = { SettingsIconBox(Icons.Default.BarChart) },
                    onClick = { onToggleReadingStats(!readingStatsEnabled) },
                    trailing = {
                        Switch(
                            checked = readingStatsEnabled,
                            onCheckedChange = onToggleReadingStats,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                )
                ExpressiveDivider()
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f).padding(end = 12.dp)
                        ) {
                            SettingsIconBox(Icons.Default.Speed)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = strings.readingSpeedSettingTitle,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = strings.resetReadingSpeedSubtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { showResetSpeedConfirmDialog = true },
                        shape = RoundedCornerShape(12.dp),
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
                ExpressiveDivider()
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f).padding(end = 12.dp)
                        ) {
                            SettingsIconBox(Icons.Default.Dns)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = strings.customOpdsToggle,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = strings.customOpdsSubtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
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

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Updates & Analytics Section
            Text(
                text = strings.updatesSectionTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            ExpressiveSettingsGroup {
                ExpressiveSettingsRow(
                    title = strings.updateNotificationsToggle,
                    subtitle = strings.updateNotificationsSubtitle,
                    icon = { SettingsIconBox(Icons.Default.Notifications) },
                    onClick = { onToggleUpdateNotifications(!updateNotificationsEnabled) },
                    trailing = {
                        Switch(
                            checked = updateNotificationsEnabled,
                            onCheckedChange = onToggleUpdateNotifications,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                )
                ExpressiveDivider()
                ExpressiveSettingsRow(
                    title = strings.anonymousAnalyticsToggle,
                    subtitle = strings.anonymousAnalyticsSubtitle,
                    icon = { SettingsIconBox(Icons.Default.Analytics) },
                    onClick = { onToggleAnonymousAnalytics(!anonymousAnalyticsEnabled) },
                    trailing = {
                        Switch(
                            checked = anonymousAnalyticsEnabled,
                            onCheckedChange = onToggleAnonymousAnalytics,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                )
                ExpressiveDivider()
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f).padding(end = 12.dp)
                        ) {
                            SettingsIconBox(Icons.Default.SystemUpdate)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = strings.currentVersion(currentAppVersion),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                            Toast.makeText(context, "$remaining", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            )
                        }
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
                                    Toast.makeText(context, "${strings.updateChannelRelease}...", Toast.LENGTH_SHORT).show()
                                },
                                label = { Text(strings.updateChannelRelease) }
                            )
                            FilterChip(
                                selected = updateChannel == "BETA",
                                onClick = {
                                    onUpdateChannelChange("BETA")
                                    onCheckUpdates()
                                    Toast.makeText(context, "${strings.updateChannelBeta}...", Toast.LENGTH_SHORT).show()
                                },
                                label = { Text(strings.updateChannelBeta) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. Backup & Sync Section
            Text(
                text = strings.backupSectionTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            ExpressiveSettingsGroup {
                ExpressiveSettingsRow(
                    title = strings.createBackupTitle,
                    subtitle = strings.createBackupSubtitle,
                    icon = { SettingsIconBox(Icons.Default.CloudDownload) },
                    onClick = onExportBackup,
                    trailing = {
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                )
                ExpressiveDivider()
                ExpressiveSettingsRow(
                    title = strings.sendToGoogleDriveTitle,
                    subtitle = strings.sendToGoogleDriveSubtitle,
                    icon = { SettingsIconBox(Icons.Default.Share) },
                    onClick = onSendToGoogleDrive,
                    trailing = {
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                )
                ExpressiveDivider()
                ExpressiveSettingsRow(
                    title = strings.restoreBackupTitle,
                    subtitle = strings.restoreBackupSubtitle,
                    icon = { SettingsIconBox(Icons.Default.History) },
                    onClick = onRestoreBackup,
                    trailing = {
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 6. About, GitHub & Donate Section
            Text(
                text = strings.aboutSectionTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            ExpressiveSettingsGroup {
                ExpressiveSettingsRow(
                    title = strings.donate,
                    subtitle = "https://dalink.to/koukku",
                    icon = {
                        SettingsIconBox(
                            icon = Icons.Default.Favorite,
                            tint = MaterialTheme.colorScheme.error,
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                        )
                    },
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://dalink.to/koukku"))
                        context.startActivity(intent)
                    },
                    trailing = {
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                )
                ExpressiveDivider()
                ExpressiveSettingsRow(
                    title = strings.githubRepository,
                    subtitle = "https://github.com/Kouqqu/aura-reader",
                    icon = {
                        SettingsPainterIconBox(
                            painter = painterResource(R.drawable.ic_github),
                            tint = MaterialTheme.colorScheme.onSurface,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    },
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Kouqqu/aura-reader"))
                        context.startActivity(intent)
                    },
                    trailing = {
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                )
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


@Composable
private fun ExpressiveThemePill(
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

    val containerColor = if (isSelected) previewBgColor else MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
    val contentColor = if (isSelected) previewTextColor else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
    val borderWidth = if (isSelected) 2.dp else 1.dp

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
private fun SettingsIconBox(
    icon: ImageVector,
    tint: Color = MaterialTheme.colorScheme.primary,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
    contentDescription: String? = null
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
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
private fun SettingsPainterIconBox(
    painter: Painter,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
    contentDescription: String? = null
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
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

    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
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
private fun ExpressiveSettingsGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = modifier.fillMaxWidth(),
        content = content
    )
}

@Composable
private fun ExpressiveDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 66.dp, end = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    )
}

@Composable
private fun ExpressiveSettingsRow(
    title: String,
    subtitle: String? = null,
    icon: @Composable () -> Unit,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit
) {
    val rowModifier = if (onClick != null) {
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
