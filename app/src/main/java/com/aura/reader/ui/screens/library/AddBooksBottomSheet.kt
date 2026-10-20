package com.aura.reader.ui.screens.library

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aura.reader.ui.components.expressiveClickable
import com.aura.reader.ui.components.LiquidGlassSurface
import com.aura.reader.ui.theme.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBooksBottomSheet(
    onDismiss: () -> Unit,
    onSelectFiles: () -> Unit,
    onScanFolder: () -> Unit,
    onOpenCatalog: () -> Unit,
    isLiquidGlass: Boolean = false
) {
    val strings = LocalAppStrings.current

    if (isLiquidGlass) {
        val isDark = isSystemInDarkTheme()
        // Floating glass card anchored above the floating navigation island
        // Navigation dock at bottom remains 100% visible!
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Deep backdrop scrim behind the popup to focus attention and dim underlying library content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.78f))
            )

            // Frosted Floating Glass Card with rich opaque backdrop so underlying text never bleeds through
            LiquidGlassSurface(
                shape = RoundedCornerShape(28.dp),
                shadowElevation = 24.dp,
                tintColor = if (isDark) Color(0xFF141722) else Color(0xFFFFFFFF),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 88.dp) // Anchored directly above the navigation island!
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {} // Consume click so card itself doesn't dismiss
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.chooseAddMethod,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            onClick = onDismiss,
                            shape = CircleShape,
                            color = if (isDark) Color(0x30FFFFFF) else Color(0x20000000),
                            modifier = Modifier.size(30.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = strings.cancel,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    AddMethodItem(
                        icon = Icons.Default.InsertDriveFile,
                        title = strings.selectFiles,
                        subtitle = strings.selectFilesSubtitle,
                        isGlass = true,
                        onClick = {
                            onDismiss()
                            onSelectFiles()
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    AddMethodItem(
                        icon = Icons.Default.FolderOpen,
                        title = strings.scanFolder,
                        subtitle = strings.scanFolderSubtitle,
                        isGlass = true,
                        onClick = {
                            onDismiss()
                            onScanFolder()
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    AddMethodItem(
                        icon = Icons.Default.CloudDownload,
                        title = strings.opdsCatalog,
                        subtitle = strings.opdsCatalogSubtitle,
                        isGlass = true,
                        onClick = {
                            onDismiss()
                            onOpenCatalog()
                        }
                    )
                }
            }
        }
    } else {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    text = strings.chooseAddMethod,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                AddMethodItem(
                    icon = Icons.Default.InsertDriveFile,
                    title = strings.selectFiles,
                    subtitle = strings.selectFilesSubtitle,
                    onClick = {
                        onDismiss()
                        onSelectFiles()
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                AddMethodItem(
                    icon = Icons.Default.FolderOpen,
                    title = strings.scanFolder,
                    subtitle = strings.scanFolderSubtitle,
                    onClick = {
                        onDismiss()
                        onScanFolder()
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                AddMethodItem(
                    icon = Icons.Default.CloudDownload,
                    title = strings.opdsCatalog,
                    subtitle = strings.opdsCatalogSubtitle,
                    onClick = {
                        onDismiss()
                        onOpenCatalog()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun AddMethodItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isGlass: Boolean = false,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val containerColor = if (isGlass) {
        if (isDark) Color(0x35232B3B) else Color(0x0C000000)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    val border = if (isGlass) {
        BorderStroke(
            0.85.dp,
            Brush.verticalGradient(
                listOf(
                    if (isDark) Color.White.copy(alpha = 0.22f) else Color.Black.copy(alpha = 0.12f),
                    Color.Transparent
                )
            )
        )
    } else null

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .expressiveClickable(scaleDown = 0.96f, onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        border = border
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
