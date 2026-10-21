package com.aura.reader.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.reader.ui.theme.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBooksBottomSheet(
    onDismiss: () -> Unit,
    onSelectFiles: () -> Unit,
    onScanFolder: () -> Unit,
    onOpenCatalog: () -> Unit,
    isLiquidGlass: Boolean = false,
    isDark: Boolean = isSystemInDarkTheme()
) {
    val strings = LocalAppStrings.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isAmoled = isDark && MaterialTheme.colorScheme.background == Color.Black

    val sheetBgColor = if (isLiquidGlass) {
        if (isDark) {
            if (isAmoled) Color(0xFF121316) else Color(0xFF181A20)
        } else {
            Color(0xFFF6F7FA)
        }
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }

    val sheetBorderColor = if (isLiquidGlass) {
        if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
    } else Color.Transparent

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = sheetBgColor,
        scrimColor = Color.Black.copy(alpha = if (isLiquidGlass) 0.65f else 0.45f),
        dragHandle = {
            if (isLiquidGlass) {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .size(width = 36.dp, height = 4.5.dp)
                        .clip(CircleShape)
                        .background(
                            if (isDark) Color.White.copy(alpha = 0.22f) else Color.Black.copy(alpha = 0.15f)
                        )
                )
            } else {
                BottomSheetDefaults.DragHandle()
            }
        },
        modifier = if (isLiquidGlass) {
            Modifier.border(
                width = 0.6.dp,
                color = sheetBorderColor,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            )
        } else Modifier
    ) {
        if (isLiquidGlass) {
            // Liquid Glass Apple-style layout
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                // Header with Title and Close button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = strings.addBooks,
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF1C1B1F)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = strings.chooseAddMethod,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            color = if (isDark) Color.White.copy(alpha = 0.70f) else Color.Black.copy(alpha = 0.60f)
                        )
                    }

                    Surface(
                        onClick = onDismiss,
                        shape = CircleShape,
                        color = if (isDark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.06f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = strings.cancel,
                                tint = if (isDark) Color.White else Color(0xFF1C1B1F),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Apple Inset Grouped Card
                val groupBg = if (isDark) Color(0xFF1E212E) else Color(0xFFFFFFFF)
                val groupBorderColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = groupBg,
                    border = androidx.compose.foundation.BorderStroke(0.6.dp, groupBorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Option 1: Select Files
                        AddMethodRow(
                            icon = Icons.Default.InsertDriveFile,
                            title = strings.selectFiles,
                            subtitle = strings.selectFilesSubtitle,
                            isDark = isDark,
                            onClick = {
                                onDismiss()
                                onSelectFiles()
                            }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(start = 66.dp, end = 16.dp),
                            thickness = 0.5.dp,
                            color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
                        )

                        // Option 2: Scan Folder
                        AddMethodRow(
                            icon = Icons.Default.FolderOpen,
                            title = strings.scanFolder,
                            subtitle = strings.scanFolderSubtitle,
                            isDark = isDark,
                            onClick = {
                                onDismiss()
                                onScanFolder()
                            }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(start = 66.dp, end = 16.dp),
                            thickness = 0.5.dp,
                            color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
                        )

                        // Option 3: OPDS Online Catalog
                        AddMethodRow(
                            icon = Icons.Default.CloudDownload,
                            title = strings.opdsCatalog,
                            subtitle = strings.opdsCatalogSubtitle,
                            isDark = isDark,
                            onClick = {
                                onDismiss()
                                onOpenCatalog()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        } else {
            // Pure Material Design 3 layout (matching original design)
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

                MaterialAddMethodItem(
                    icon = Icons.Default.InsertDriveFile,
                    title = strings.selectFiles,
                    subtitle = strings.selectFilesSubtitle,
                    onClick = {
                        onDismiss()
                        onSelectFiles()
                    }
                )

                MaterialAddMethodItem(
                    icon = Icons.Default.FolderOpen,
                    title = strings.scanFolder,
                    subtitle = strings.scanFolderSubtitle,
                    onClick = {
                        onDismiss()
                        onScanFolder()
                    }
                )

                MaterialAddMethodItem(
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
private fun MaterialAddMethodItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AddMethodRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isDark: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.18f else 0.12f),
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

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) Color.White else Color(0xFF1C1B1F)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                color = if (isDark) Color.White.copy(alpha = 0.70f) else Color.Black.copy(alpha = 0.60f)
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = if (isDark) Color.White.copy(alpha = 0.40f) else Color.Black.copy(alpha = 0.35f),
            modifier = Modifier.size(18.dp)
        )
    }
}
