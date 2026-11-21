package com.aura.reader.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.reader.data.model.HolidaySeason
import com.aura.reader.ui.components.expressiveBounce
import com.aura.reader.ui.theme.LocalAppStrings
import com.aura.reader.ui.theme.holidayDisplayName

/**
 * Compact folding holiday style picker. Allows manually previewing any season
 * in a clean, compact dropdown card. Tapping an active holiday deselects it,
 * reverting smoothly back to the automatic calendar schedule.
 */
@Composable
internal fun SettingsHolidayStyleSection(
    holidayOverride: String,
    seasonalEffectsEnabled: Boolean,
    isGlass: Boolean,
    isThemeDark: Boolean,
    onHolidayStylePicked: (String) -> Unit
) {
    val strings = LocalAppStrings.current
    var expanded by rememberSaveable { mutableStateOf(false) }

    val activeHoliday = HolidaySeason.entries.firstOrNull { it != HolidaySeason.NONE && it.id == holidayOverride }
    val subtitleText = when {
        !seasonalEffectsEnabled || holidayOverride == "OFF" -> strings.holidayModeOff
        activeHoliday != null -> "${activeHoliday.iconEmoji} ${strings.holidayDisplayName(activeHoliday)}"
        else -> strings.holidayCalendarDefault
    }

    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "holidayFoldChevron"
    )

    val cardShape = RoundedCornerShape(20.dp)
    val containerColor = if (isGlass) {
        if (isThemeDark) MaterialTheme.colorScheme.surfaceContainer else Color(0x60FFFFFF)
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    Surface(
        shape = cardShape,
        color = containerColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header row with click to expand/collapse
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                ) {
                    if (activeHoliday != null) {
                        Text(text = activeHoliday.iconEmoji, fontSize = 18.sp)
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Celebration,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.holidayStyleTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (activeHoliday != null) activeHoliday.primaryAccent else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                    modifier = Modifier
                        .size(18.dp)
                        .graphicsLayer { rotationZ = chevronRotation }
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(MaterialTheme.motionScheme.defaultSpatialSpec()) +
                    fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()),
                exit = shrinkVertically(MaterialTheme.motionScheme.defaultSpatialSpec()) +
                    fadeOut(MaterialTheme.motionScheme.defaultEffectsSpec())
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Text(
                        text = strings.holidayStyleSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )

                    // Compact holiday items in 2 columns
                    val holidays = listOf(
                        HolidaySeason.HALLOWEEN to strings.holidayStyleHalloweenHint,
                        HolidaySeason.CHRISTMAS_WESTERN to strings.holidayStyleChristmasWesternHint,
                        HolidaySeason.NEW_YEAR to strings.holidayStyleNewYearHint,
                        HolidaySeason.CHRISTMAS_ORTHODOX to strings.holidayStyleChristmasOrthodoxHint,
                        HolidaySeason.OLD_NEW_YEAR to strings.holidayStyleWinterHint,
                        HolidaySeason.VALENTINE to strings.holidayStyleValentineHint,
                        HolidaySeason.EASTER to strings.holidayStyleEasterHint
                    )

                    holidays.chunked(2).forEachIndexed { index, rowItems ->
                        if (index > 0) Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowItems.forEach { (holiday, hint) ->
                                val isSelected = seasonalEffectsEnabled && holidayOverride == holiday.id
                                CompactHolidayChip(
                                    emoji = holiday.iconEmoji,
                                    label = strings.holidayDisplayName(holiday),
                                    hint = hint,
                                    accent = holiday.primaryAccent,
                                    isSelected = isSelected,
                                    isGlass = isGlass,
                                    isThemeDark = isThemeDark,
                                    onClick = {
                                        if (isSelected) {
                                            onHolidayStylePicked("AUTO")
                                        } else {
                                            onHolidayStylePicked(holiday.id)
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactHolidayChip(
    emoji: String,
    label: String,
    hint: String,
    accent: Color,
    isSelected: Boolean,
    isGlass: Boolean,
    isThemeDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }

    val containerColor = when {
        isSelected -> accent.copy(alpha = if (isThemeDark) 0.22f else 0.14f)
        isGlass -> if (isThemeDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.04f)
        else -> MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f)
    }
    val borderColor = if (isSelected) {
        accent
    } else if (isGlass) {
        if (isThemeDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.20f)
    }

    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        interactionSource = interactionSource,
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = BorderStroke(if (isSelected) 1.5.dp else 0.75.dp, borderColor),
        modifier = modifier
            .height(44.dp)
            .expressiveBounce(interactionSource)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) accent else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = hint,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
