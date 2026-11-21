package com.aura.reader.ui.screens.library

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Surface
import androidx.compose.ui.unit.sp
import com.aura.reader.ui.components.expressiveClickable
import androidx.compose.foundation.background
import com.aura.reader.ui.components.LiquidGlassFilterChip
import com.aura.reader.ui.components.ExpressiveConnectedButtonItem
import com.aura.reader.ui.components.LiquidGlassCard
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.Timer
import com.aura.reader.ui.theme.LocalAppStrings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aura.reader.R
import com.aura.reader.data.updater.UpdateInfo

@Composable
internal fun LibraryUpdateBanner(
    update: UpdateInfo,
    isGlass: Boolean,
    isThemeDark: Boolean,
    installState: UpdateInstallState = UpdateInstallState.Idle,
    onDownloadAndInstall: () -> Unit,
    onInstallDownloaded: () -> Unit = {},
    onOpenUpdatePage: (String) -> Unit
) {
    val strings = LocalAppStrings.current

    LiquidGlassCard(
        isGlass = isGlass,
        isDark = isThemeDark,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.SystemUpdate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.updateAvailableTitle(update.latestVersion),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = strings.updateBannerSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onDownloadAndInstall,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        strings.updateAction,
                        maxLines = 1
                    )
                }
                OutlinedButton(
                    onClick = { onOpenUpdatePage(update.releaseUrl) },
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_github),
                        contentDescription = strings.downloadFromGithub,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
internal fun LibraryReadingStatsBanner(
    isGlass: Boolean,
    isThemeDark: Boolean,
    todayMinutes: Int,
    currentStreakDays: Int,
    onClick: () -> Unit
) {
    val strings = LocalAppStrings.current

    if (isGlass) {
        val bannerBg = if (isThemeDark) {
            Color.White.copy(alpha = 0.06f)
        } else {
            Color.Black.copy(alpha = 0.04f)
        }
        val bannerBorder = if (isThemeDark) {
            Color.White.copy(alpha = 0.08f)
        } else {
            Color.Black.copy(alpha = 0.05f)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(13.dp))
                .background(bannerBg)
                .border(width = 0.6.dp, color = bannerBorder, shape = RoundedCornerShape(13.dp))
                .expressiveClickable(scaleDown = 0.98f, onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (todayMinutes > 0) "${strings.todayReadingTime}: ${strings.minutesRead(todayMinutes)}" else strings.readingStatsEmpty,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (currentStreakDays > 0) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFF9500).copy(alpha = if (isThemeDark) 0.14f else 0.10f),
                        border = BorderStroke(0.6.dp, Color(0xFFFF9500).copy(alpha = if (isThemeDark) 0.30f else 0.22f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "\uD83D\uDD25",
                                fontSize = 11.sp
                            )
                            Text(
                                text = "$currentStreakDays",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF9500)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    } else {
        LiquidGlassCard(
            isGlass = false,
            isDark = isThemeDark,
            shape = RoundedCornerShape(13.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(13.dp))
                .clickable(onClick = onClick)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (todayMinutes > 0) "${strings.todayReadingTime}: ${strings.minutesRead(todayMinutes)}" else strings.readingStatsEmpty,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (currentStreakDays > 0) {
                    Text(
                        text = "\uD83D\uDD25 ${currentStreakDays}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

@Composable
internal fun LibraryCollectionChips(
    activeFilter: ActiveCollectionFilter,
    hasSeries: Boolean,
    userCollections: List<String>,
    isGlass: Boolean,
    isThemeDark: Boolean,
    onFilterChange: (ActiveCollectionFilter) -> Unit,
    onDeleteCollection: (String) -> Unit,
    onNewCollection: () -> Unit,
    onPrivateFolderClick: () -> Unit = {},
    trashCount: Int = 0,
    onTrashClick: () -> Unit = {}
) {
    val strings = LocalAppStrings.current

    val systemFilters = remember(hasSeries, strings) {
        buildList {
            add(CollectionFilterType.ALL to strings.colAll)
            add(CollectionFilterType.READING to strings.colReading)
            add(CollectionFilterType.FAVORITES to strings.colFavorites)
            add(CollectionFilterType.UNREAD to strings.colUnread)
            add(CollectionFilterType.FINISHED to strings.colFinished)
            if (hasSeries) {
                add(CollectionFilterType.SERIES to strings.colSeries)
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Material 3 Expressive Connected Button Group (System Filters)
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            systemFilters.forEachIndexed { index, (type, labelText) ->
                val isSelected = activeFilter.type == type
                val leadingIcon: (@Composable () -> Unit)? = if (type == CollectionFilterType.FAVORITES) {
                    {
                        Icon(
                            imageVector = Icons.Rounded.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                } else null

                ExpressiveConnectedButtonItem(
                    selected = isSelected,
                    onClick = { onFilterChange(ActiveCollectionFilter(type)) },
                    index = index,
                    totalCount = systemFilters.size,
                    label = { Text(labelText) },
                    leadingIcon = leadingIcon,
                    isGlass = isGlass,
                    isDark = isThemeDark
                )
            }
        }

        // 2. Custom User Collections (Connected Button Group if 2+, or standalone squircle if 1)
        if (userCollections.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                userCollections.forEachIndexed { index, colName ->
                    val isSelected = activeFilter.type == CollectionFilterType.CUSTOM && activeFilter.customName == colName
                    ExpressiveConnectedButtonItem(
                        selected = isSelected,
                        onClick = { onFilterChange(ActiveCollectionFilter(CollectionFilterType.CUSTOM, colName)) },
                        index = index,
                        totalCount = userCollections.size,
                        label = { Text(colName) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = strings.delete,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { onDeleteCollection(colName) }
                            )
                        },
                        isGlass = isGlass,
                        isDark = isThemeDark
                    )
                }
            }
        }

        // 3. New Collection Action Button
        LiquidGlassFilterChip(
            selected = false,
            onClick = onNewCollection,
            leadingIcon = { Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp)) },
            label = { Text(strings.newCollection) },
            isGlass = isGlass,
            isDark = isThemeDark
        )

        // 4. Private Folder Chip
        val isPrivateSelected = activeFilter.type == CollectionFilterType.PRIVATE_FOLDER
        LiquidGlassFilterChip(
            selected = isPrivateSelected,
            onClick = onPrivateFolderClick,
            leadingIcon = {
                Icon(
                    imageVector = if (isPrivateSelected) Icons.Rounded.LockOpen else Icons.Rounded.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (isPrivateSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            label = { Text(strings.privateFolderTitle) },
            isGlass = isGlass,
            isDark = isThemeDark
        )

        // 5. Trash Chip (Корзина)
        if (trashCount > 0 || activeFilter.type == CollectionFilterType.TRASH) {
            val isTrashSelected = activeFilter.type == CollectionFilterType.TRASH
            LiquidGlassFilterChip(
                selected = isTrashSelected,
                onClick = onTrashClick,
                leadingIcon = {
                    Icon(
                        imageVector = if (isTrashSelected) Icons.Rounded.Delete else Icons.Rounded.DeleteOutline,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isTrashSelected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                label = { Text(if (trashCount > 0) "${strings.trashTitle} ($trashCount)" else strings.trashTitle) },
                isGlass = isGlass,
                isDark = isThemeDark
            )
        }
    }
}

@Composable
internal fun TrashBanner(
    bookCount: Int,
    isGlass: Boolean,
    isDark: Boolean,
    onClearTrash: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current

    androidx.compose.material3.Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (isGlass) {
            if (isDark) androidx.compose.ui.graphics.Color(0x352B1E22) else androidx.compose.ui.graphics.Color(0x75FFF0F2)
        } else {
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
        },
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isGlass) {
                if (isDark) androidx.compose.ui.graphics.Color.White.copy(alpha = 0.12f) else androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.08f)
            } else {
                MaterialTheme.colorScheme.error.copy(alpha = 0.25f)
            }
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            androidx.compose.foundation.layout.Box(
                contentAlignment = androidx.compose.ui.Alignment.Center,
                modifier = Modifier
                    .size(34.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f))
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(10.dp))
            androidx.compose.foundation.layout.Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strings.trashTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = strings.trashRetentionDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (bookCount > 0) {
                OutlinedButton(
                    onClick = onClearTrash,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(strings.clearTrash, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
