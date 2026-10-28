package com.aura.reader.ui.screens.settings

import android.widget.Toast
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aura.reader.data.model.AppDesignStyle
import com.aura.reader.ui.components.LiquidGlassSwitch
import com.aura.reader.ui.components.LiquidGlassFilterChip
import com.aura.reader.data.opds.OpdsService
import com.aura.reader.ui.theme.AppLanguage
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.aura.reader.ui.theme.LocalAppStrings

@Composable
internal fun SettingsDesignStyleSection(
    appDesignStyle: AppDesignStyle,
    isThemeDark: Boolean,
    onAppDesignStyleChange: (AppDesignStyle) -> Unit
) {
    val strings = LocalAppStrings.current

// 0. Design Style Section (Material 3 vs Liquid Glass)
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isMatSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                if (isThemeDark) Color(0x33FFFFFF) else Color(0x18000000)
            }
        ),
        border = BorderStroke(
            if (isMatSelected) 1.5.dp else 1.dp,
            if (isMatSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = if (isThemeDark) 0.15f else 0.40f)
        ),
        modifier = Modifier
            .weight(1f)
            .height(50.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Palette,
                contentDescription = null,
                tint = if (isMatSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = strings.designStyleMaterial,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isMatSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
        }
    }

    // Option 2: Liquid Glass
    val isGlassSelected = appDesignStyle == AppDesignStyle.LIQUID_GLASS
    Card(
        onClick = { onAppDesignStyleChange(AppDesignStyle.LIQUID_GLASS) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGlassSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                if (isThemeDark) Color(0x33FFFFFF) else Color(0x18000000)
            }
        ),
        border = BorderStroke(
            if (isGlassSelected) 1.5.dp else 1.dp,
            if (isGlassSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = if (isThemeDark) 0.15f else 0.40f)
        ),
        modifier = Modifier
            .weight(1f)
            .height(50.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = if (isGlassSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = strings.designStyleLiquidGlass,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isGlassSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

}

@Composable
internal fun SettingsLanguageSection(
    currentLanguage: AppLanguage,
    appFont: String,
    isGlass: Boolean,
    onAppFontChange: (String) -> Unit,
    onOpenLanguagePicker: () -> Unit
) {
    val strings = LocalAppStrings.current

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
        onClick = { onOpenLanguagePicker() },
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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var customFonts by remember { mutableStateOf(getCustomFontFiles(context)) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val result = withContext(Dispatchers.IO) {
                    runCatching {
                        var name = "font_${System.currentTimeMillis()}.ttf"
                        context.contentResolver.query(
                            uri,
                            arrayOf(OpenableColumns.DISPLAY_NAME),
                            null,
                            null,
                            null
                        )?.use { cursor ->
                            if (cursor.moveToFirst()) {
                                val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                                if (nameIdx != -1) {
                                    val fetched = cursor.getString(nameIdx)
                                    if (!fetched.isNullOrBlank()) {
                                        name = fetched
                                    }
                                }
                            }
                        }
                        val sanitizedName = name.replace(Regex("[^a-zA-Z0-9._\\- ]"), "_")
                        val dir = File(context.filesDir, "custom_fonts").apply { mkdirs() }
                        val targetFile = File(dir, sanitizedName)
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            targetFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        } ?: error("Cannot open font stream")

                        // Validate font
                        val tf = android.graphics.Typeface.createFromFile(targetFile)
                        if (tf == null) {
                            targetFile.delete()
                            error("Invalid font file")
                        }
                        sanitizedName
                    }
                }
                if (result.isSuccess) {
                    val fileName = result.getOrThrow()
                    customFonts = getCustomFontFiles(context)
                    onAppFontChange("CUSTOM:$fileName")
                    Toast.makeText(context, strings.appFontImportSuccess, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, strings.appFontImportError, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

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
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. Google Sans (Default)
            val isGoogleSans = appFont == "GOOGLE_SANS"
            LiquidGlassFilterChip(
                selected = isGoogleSans,
                onClick = { onAppFontChange("GOOGLE_SANS") },
                label = { Text(strings.appFontGoogleSans) },
                isGlass = isGlass,
                leadingIcon = if (isGoogleSans) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null
            )

            // 2. System Default ("Системный")
            val isSystem = appFont == "SYSTEM" || appFont == "DEFAULT"
            LiquidGlassFilterChip(
                selected = isSystem,
                onClick = { onAppFontChange("SYSTEM") },
                label = { Text(strings.appFontDefault) },
                isGlass = isGlass,
                leadingIcon = if (isSystem) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null
            )

            // 3. User Imported Custom Fonts
            customFonts.forEach { fontFile ->
                val fontKey = "CUSTOM:${fontFile.name}"
                val isSelected = appFont == fontKey
                LiquidGlassFilterChip(
                    selected = isSelected,
                    onClick = { onAppFontChange(fontKey) },
                    label = {
                        Text(
                            text = fontFile.nameWithoutExtension.take(16),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    isGlass = isGlass,
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = strings.appFontCustomDelete,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable {
                                    fontFile.delete()
                                    customFonts = getCustomFontFiles(context)
                                    if (isSelected) {
                                        onAppFontChange("GOOGLE_SANS")
                                    }
                                },
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }

            // 4. Add Custom Font button
            LiquidGlassFilterChip(
                selected = false,
                onClick = {
                    filePickerLauncher.launch(
                        arrayOf(
                            "font/*",
                            "font/ttf",
                            "font/otf",
                            "application/x-font-ttf",
                            "application/x-font-otf",
                            "application/font-sfnt",
                            "application/octet-stream",
                            "*/*"
                        )
                    )
                },
                label = { Text(strings.appFontAddCustom) },
                isGlass = isGlass,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }
    }
}

}

@Composable
internal fun SettingsAdditionalFeaturesSection(
    isGlass: Boolean,
    readingStatsEnabled: Boolean,
    onToggleReadingStats: (Boolean) -> Unit,
    customOpdsEnabled: Boolean,
    customOpdsUrl: String,
    onToggleCustomOpds: (Boolean) -> Unit,
    onCustomOpdsUrlChange: (String) -> Unit,
    onResetReadingSpeed: () -> Unit
) {
    val strings = LocalAppStrings.current

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
            if (isGlass) {
                LiquidGlassSwitch(
                    checked = readingStatsEnabled,
                    onCheckedChange = onToggleReadingStats
                )
            } else {
                Switch(
                    checked = readingStatsEnabled,
                    onCheckedChange = onToggleReadingStats,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
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
            onClick = { onResetReadingSpeed() },
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
            if (isGlass) {
                LiquidGlassSwitch(
                    checked = customOpdsEnabled,
                    onCheckedChange = onToggleCustomOpds
                )
            } else {
                Switch(
                    checked = customOpdsEnabled,
                    onCheckedChange = onToggleCustomOpds,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
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

}

@Composable
internal fun SettingsUpdatesSection(
    isGlass: Boolean,
    updateNotificationsEnabled: Boolean,
    onToggleUpdateNotifications: (Boolean) -> Unit,
    anonymousAnalyticsEnabled: Boolean,
    onToggleAnonymousAnalytics: (Boolean) -> Unit,
    currentAppVersion: String,
    developerModeEnabled: Boolean,
    updateChannel: String,
    onUpdateChannelChange: (String) -> Unit,
    onCheckUpdates: () -> Unit,
    onVersionTap: () -> Unit
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current

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
            if (isGlass) {
                LiquidGlassSwitch(
                    checked = updateNotificationsEnabled,
                    onCheckedChange = onToggleUpdateNotifications
                )
            } else {
                Switch(
                    checked = updateNotificationsEnabled,
                    onCheckedChange = onToggleUpdateNotifications,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    )
    ExpressiveDivider()
    ExpressiveSettingsRow(
        title = strings.anonymousAnalyticsToggle,
        subtitle = strings.anonymousAnalyticsSubtitle,
        icon = { SettingsIconBox(Icons.Default.Analytics) },
        onClick = { onToggleAnonymousAnalytics(!anonymousAnalyticsEnabled) },
        trailing = {
            if (isGlass) {
                LiquidGlassSwitch(
                    checked = anonymousAnalyticsEnabled,
                    onCheckedChange = onToggleAnonymousAnalytics
                )
            } else {
                Switch(
                    checked = anonymousAnalyticsEnabled,
                    onCheckedChange = onToggleAnonymousAnalytics,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
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
                    modifier = Modifier.clickable { onVersionTap() }
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
                LiquidGlassFilterChip(
                    selected = updateChannel == "RELEASE",
                    isGlass = isGlass,
                    onClick = {
                        onUpdateChannelChange("RELEASE")
                        onCheckUpdates()
                        Toast.makeText(context, "${strings.updateChannelRelease}...", Toast.LENGTH_SHORT).show()
                    },
                    label = { Text(strings.updateChannelRelease) }
                )
                LiquidGlassFilterChip(
                    selected = updateChannel == "BETA",
                    isGlass = isGlass,
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

}


private fun getCustomFontFiles(context: android.content.Context): List<java.io.File> {
    val dir = java.io.File(context.filesDir, "custom_fonts")
    return dir.listFiles { file ->
        file.isFile && (file.extension.equals("ttf", ignoreCase = true) || file.extension.equals("otf", ignoreCase = true))
    }?.sortedBy { it.nameWithoutExtension.lowercase() } ?: emptyList()
}
