package com.aura.reader.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.reader.data.model.HolidaySeason
import com.aura.reader.ui.components.expressiveBounce
import com.aura.reader.ui.theme.LocalAppStrings

/**
 * User-facing holiday style picker. Every season can be previewed at any time of
 * the year; "Auto" hands control back to the calendar and "Standard" removes the
 * decorations altogether. Plaques reuse the look of the theme pills so the two
 * pickers read as one family.
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
    val standardSelected = !seasonalEffectsEnabled || holidayOverride == "OFF"
    val autoSelected = seasonalEffectsEnabled && holidayOverride == "AUTO"

    Text(
        text = strings.holidayStyleTitle,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(start = 4.dp)
    )
    Text(
        text = strings.holidayStyleSubtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, top = 2.dp, bottom = 10.dp)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HolidayStylePlaque(
            icon = Icons.Rounded.BrightnessAuto,
            label = strings.holidayStyleAuto,
            hint = strings.holidayStyleAutoHint,
            accent = MaterialTheme.colorScheme.primary,
            isSelected = autoSelected,
            isGlass = isGlass,
            isThemeDark = isThemeDark,
            onClick = { onHolidayStylePicked("AUTO") },
            modifier = Modifier.weight(1f)
        )
        HolidayStylePlaque(
            icon = Icons.AutoMirrored.Rounded.MenuBook,
            label = strings.holidayStyleStandard,
            hint = strings.holidayStyleStandardHint,
            accent = MaterialTheme.colorScheme.primary,
            isSelected = standardSelected,
            isGlass = isGlass,
            isThemeDark = isThemeDark,
            onClick = { onHolidayStylePicked("OFF") },
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HolidayStylePlaque(
            emoji = HolidaySeason.HALLOWEEN.iconEmoji,
            label = strings.holidayHalloween,
            hint = strings.holidayStyleHalloweenHint,
            accent = HolidaySeason.HALLOWEEN.primaryAccent,
            isSelected = seasonalEffectsEnabled && holidayOverride == HolidaySeason.HALLOWEEN.id,
            isGlass = isGlass,
            isThemeDark = isThemeDark,
            onClick = { onHolidayStylePicked(HolidaySeason.HALLOWEEN.id) },
            modifier = Modifier.weight(1f)
        )
        HolidayStylePlaque(
            emoji = HolidaySeason.CHRISTMAS_WESTERN.iconEmoji,
            label = strings.holidayChristmas,
            hint = strings.holidayStyleChristmasWesternHint,
            accent = HolidaySeason.CHRISTMAS_WESTERN.primaryAccent,
            isSelected = seasonalEffectsEnabled && holidayOverride == HolidaySeason.CHRISTMAS_WESTERN.id,
            isGlass = isGlass,
            isThemeDark = isThemeDark,
            onClick = { onHolidayStylePicked(HolidaySeason.CHRISTMAS_WESTERN.id) },
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HolidayStylePlaque(
            emoji = HolidaySeason.NEW_YEAR.iconEmoji,
            label = strings.holidayNewYear,
            hint = strings.holidayStyleNewYearHint,
            accent = HolidaySeason.NEW_YEAR.primaryAccent,
            isSelected = seasonalEffectsEnabled && holidayOverride == HolidaySeason.NEW_YEAR.id,
            isGlass = isGlass,
            isThemeDark = isThemeDark,
            onClick = { onHolidayStylePicked(HolidaySeason.NEW_YEAR.id) },
            modifier = Modifier.weight(1f)
        )
        HolidayStylePlaque(
            emoji = HolidaySeason.CHRISTMAS_ORTHODOX.iconEmoji,
            label = strings.holidayChristmas,
            hint = strings.holidayStyleChristmasOrthodoxHint,
            accent = HolidaySeason.CHRISTMAS_ORTHODOX.primaryAccent,
            isSelected = seasonalEffectsEnabled && holidayOverride == HolidaySeason.CHRISTMAS_ORTHODOX.id,
            isGlass = isGlass,
            isThemeDark = isThemeDark,
            onClick = { onHolidayStylePicked(HolidaySeason.CHRISTMAS_ORTHODOX.id) },
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HolidayStylePlaque(
            emoji = HolidaySeason.OLD_NEW_YEAR.iconEmoji,
            label = strings.holidayWinterHolidays,
            hint = strings.holidayStyleWinterHint,
            accent = HolidaySeason.OLD_NEW_YEAR.primaryAccent,
            isSelected = seasonalEffectsEnabled && holidayOverride == HolidaySeason.OLD_NEW_YEAR.id,
            isGlass = isGlass,
            isThemeDark = isThemeDark,
            onClick = { onHolidayStylePicked(HolidaySeason.OLD_NEW_YEAR.id) },
            modifier = Modifier.weight(1f)
        )
        HolidayStylePlaque(
            emoji = HolidaySeason.VALENTINE.iconEmoji,
            label = strings.holidayValentine,
            hint = strings.holidayStyleValentineHint,
            accent = HolidaySeason.VALENTINE.primaryAccent,
            isSelected = seasonalEffectsEnabled && holidayOverride == HolidaySeason.VALENTINE.id,
            isGlass = isGlass,
            isThemeDark = isThemeDark,
            onClick = { onHolidayStylePicked(HolidaySeason.VALENTINE.id) },
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    HolidayStylePlaque(
        emoji = HolidaySeason.EASTER.iconEmoji,
        label = strings.holidayEaster,
        hint = strings.holidayStyleEasterHint,
        accent = HolidaySeason.EASTER.primaryAccent,
        isSelected = seasonalEffectsEnabled && holidayOverride == HolidaySeason.EASTER.id,
        isGlass = isGlass,
        isThemeDark = isThemeDark,
        onClick = { onHolidayStylePicked(HolidaySeason.EASTER.id) },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun HolidayStylePlaque(
    label: String,
    hint: String,
    accent: Color,
    isSelected: Boolean,
    isGlass: Boolean,
    isThemeDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    icon: ImageVector? = null
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }

    val containerColor = when {
        isSelected -> accent.copy(alpha = if (isThemeDark) 0.24f else 0.16f)
        isGlass -> if (isThemeDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.05f)
        else -> MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
    }
    val borderColor = if (isSelected) {
        accent
    } else if (isGlass) {
        if (isThemeDark) Color.White.copy(alpha = 0.18f) else Color.Black.copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
    }

    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        interactionSource = interactionSource,
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(if (isSelected) 2.dp else 0.85.dp, borderColor),
        modifier = modifier
            .heightIn(min = 58.dp)
            .expressiveBounce(interactionSource)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(alpha = if (isThemeDark) 0.22f else 0.14f))
            ) {
                if (emoji != null) {
                    Text(text = emoji, fontSize = 16.sp)
                } else if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = hint,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
