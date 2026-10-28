package com.aura.reader.ui.screens.library

import com.aura.reader.ui.components.expressiveClickable
import com.aura.reader.ui.components.LiquidGlassFilterChip
import com.aura.reader.ui.components.LiquidGlassCard
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aura.reader.data.updater.UpdateInfo

@Composable
internal fun LibraryUpdateBanner(
    update: UpdateInfo,
    isGlass: Boolean,
    isThemeDark: Boolean,
    onOpenUpdatePage: (String) -> Unit
) {
    val strings = LocalAppStrings.current

    LiquidGlassCard(
        isGlass = isGlass,
        isDark = isThemeDark,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    onOpenUpdatePage(update.releaseUrl)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(strings.download)
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LiquidGlassFilterChip(
            selected = activeFilter.type == CollectionFilterType.ALL,
            onClick = { onFilterChange(ActiveCollectionFilter(CollectionFilterType.ALL)) },
            label = { Text(strings.colAll) },
            isGlass = isGlass,
            isDark = isThemeDark
        )
        LiquidGlassFilterChip(
            selected = activeFilter.type == CollectionFilterType.READING,
            onClick = { onFilterChange(ActiveCollectionFilter(CollectionFilterType.READING)) },
            label = { Text(strings.colReading) },
            isGlass = isGlass,
            isDark = isThemeDark
        )
        LiquidGlassFilterChip(
            selected = activeFilter.type == CollectionFilterType.FAVORITES,
            onClick = { onFilterChange(ActiveCollectionFilter(CollectionFilterType.FAVORITES)) },
            leadingIcon = {
                Icon(
                    Icons.Rounded.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            },
            label = { Text(strings.colFavorites) },
            isGlass = isGlass,
            isDark = isThemeDark
        )
        LiquidGlassFilterChip(
            selected = activeFilter.type == CollectionFilterType.UNREAD,
            onClick = { onFilterChange(ActiveCollectionFilter(CollectionFilterType.UNREAD)) },
            label = { Text(strings.colUnread) },
            isGlass = isGlass,
            isDark = isThemeDark
        )
        LiquidGlassFilterChip(
            selected = activeFilter.type == CollectionFilterType.FINISHED,
            onClick = { onFilterChange(ActiveCollectionFilter(CollectionFilterType.FINISHED)) },
            label = { Text(strings.colFinished) },
            isGlass = isGlass,
            isDark = isThemeDark
        )
        if (hasSeries) {
            LiquidGlassFilterChip(
                selected = activeFilter.type == CollectionFilterType.SERIES,
                onClick = { onFilterChange(ActiveCollectionFilter(CollectionFilterType.SERIES)) },
                label = { Text(strings.colSeries) },
                isGlass = isGlass,
                isDark = isThemeDark
            )
        }
        for (colName in userCollections) {
            val isSelected = activeFilter.type == CollectionFilterType.CUSTOM && activeFilter.customName == colName
            LiquidGlassFilterChip(
                selected = isSelected,
                onClick = { onFilterChange(ActiveCollectionFilter(CollectionFilterType.CUSTOM, colName)) },
                trailingIcon = {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = strings.delete,
                        modifier = Modifier
                            .size(14.dp)
                            .clickable { onDeleteCollection(colName) }
                    )
                },
                label = { Text(colName) },
                isGlass = isGlass,
                isDark = isThemeDark
            )
        }
        LiquidGlassFilterChip(
            selected = false,
            onClick = { onNewCollection() },
            leadingIcon = { Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp)) },
            label = { Text(strings.newCollection) },
            isGlass = isGlass,
            isDark = isThemeDark
        )
    }
}
