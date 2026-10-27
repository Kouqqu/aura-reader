package com.aura.reader.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aura.reader.R
import com.aura.reader.ui.theme.LocalAppStrings

@Composable
internal fun SettingsBackupSection(
    onExportBackup: () -> Unit,
    onSendToGoogleDrive: () -> Unit,
    onRestoreBackup: () -> Unit
) {
    val strings = LocalAppStrings.current

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
}

@Composable
internal fun SettingsAboutSection(
    isFullScreen: Boolean,
    onDismiss: () -> Unit,
    onOpenOnboarding: () -> Unit
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current

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
        ExpressiveDivider()
        ExpressiveSettingsRow(
            title = strings.telegramChannelTitle,
            subtitle = strings.telegramChannelSubtitle,
            icon = {
                SettingsPainterIconBox(
                    painter = painterResource(R.drawable.ic_telegram),
                    tint = Color(0xFF2AABEE),
                    containerColor = Color(0xFF2AABEE).copy(alpha = 0.15f)
                )
            },
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/koukkuteka"))
                context.startActivity(intent)
            },
            trailing = {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        )
        ExpressiveDivider()
        ExpressiveSettingsRow(
            title = strings.quickGuideTitle,
            subtitle = strings.quickGuideSubtitle,
            icon = {
                SettingsIconBox(
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    tint = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                )
            },
            onClick = {
                onDismiss()
                onOpenOnboarding()
            },
            trailing = {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        )
    }

    Spacer(modifier = Modifier.height(24.dp))
    Text(
        text = "Stworzone z ❤️ przez Koukku",
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )
    Spacer(modifier = Modifier.height(if (isFullScreen) 160.dp else 40.dp))
}
