package com.aura.reader.ui.screens.library

import com.aura.reader.ui.components.expressiveClickable
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
import androidx.compose.material.icons.rounded.Favorite
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
import com.aura.reader.ui.components.WavyProgressIndicator

@Composable
internal fun LibraryUpdateBanner(
    update: UpdateInfo,
    isGlass: Boolean,
    isThemeDark: Boolean,
    installState: UpdateInstallState,
    onDownloadAndInstall: () -> Unit,
    onInstallDownloaded: () -> Unit,
    onOpenUpdatePage: (String) -> Unit
) {
    val strings = LocalAppStrings.current
    val isDownloading = installState is UpdateInstallState.Downloading
    val progress = (installState as? UpdateInstallState.Downloading)?.progress
    val isReady = installState is UpdateInstallState.Ready ||
        installState is UpdateInstallState.PermissionRequired
    val needsPermission = installState is UpdateInstallState.PermissionRequired
    val failure = installState as? UpdateInstallState.Failed

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
                        text = when {
                            isDownloading -> strings.downloadingUpdate
                            needsPermission -> strings.installPermissionNeeded
                            failure != null -> strings.updateDownloadFailed
                            isReady -> strings.updateReadyToInstall
                            else -> strings.updateBannerSubtitle
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (failure != null || needsPermission) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            if (isDownloading) {
                Spacer(modifier = Modifier.height(12.dp))
                WavyProgressIndicator(
                    progress = progress ?: 0f,
                    modifier = Modifier.fillMaxWidth(),
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
                if (progress != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.End)
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
                    onClick = if (isReady) onInstallDownloaded else onDownloadAndInstall,
                    enabled = !isDownloading,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    if (isDownloading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        when {
                            isDownloading && progress != null -> "${(progress * 100).toInt()}%"
                            isDownloading -> strings.downloading
                            isReady -> strings.installUpdate
                            else -> strings.updateAction
                        },
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

    LiquidGlassCard(
        isGlass = isGlass,
        isDark = isThemeDark,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .expressiveClickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Timer,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (todayMinutes > 0) "${strings.todayReadingTime}: ${strings.minutesRead(todayMinutes)}" else strings.readingStatsEmpty,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (currentStreakDays > 0) {
                Text(
                    text = "\uD83D\uDD25 ${currentStreakDays}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
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
    onNewCollection: () -> Unit
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
    }
}
