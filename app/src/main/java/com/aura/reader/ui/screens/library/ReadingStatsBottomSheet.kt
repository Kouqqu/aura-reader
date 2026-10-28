package com.aura.reader.ui.screens.library

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.reader.data.preferences.DayReadingStat
import com.aura.reader.data.preferences.ReadingStatsData
import com.aura.reader.ui.theme.LocalAppStrings
import com.aura.reader.ui.theme.Strings
import androidx.compose.foundation.isSystemInDarkTheme
import com.aura.reader.ui.components.LiquidGlassSurface
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingStatsBottomSheet(
    statsData: ReadingStatsData,
    onDismiss: () -> Unit,
    onResetStats: () -> Unit = {},
    isLiquidGlass: Boolean = false,
    isDark: Boolean = isSystemInDarkTheme()
) {
    val strings = LocalAppStrings.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isAmoled = isDark && MaterialTheme.colorScheme.background == Color.Black

    val sheetBgColor = if (isLiquidGlass) {
        if (isDark) {
            if (isAmoled) Color(0xFF000000) else Color(0xFF14151C)
        } else {
            Color(0xFFF2F3F8)
        }
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }

    val sheetBorderColor = if (isLiquidGlass) {
        if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
    } else Color.Transparent

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
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
        containerColor = sheetBgColor,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Rounded.Timeline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = strings.readingStatsSheetTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Streak Card
            StreakBanner(statsData = statsData, strings = strings, isGlass = isLiquidGlass, isDark = isDark)

            Spacer(modifier = Modifier.height(16.dp))

            // 3 Quick Stat Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = strings.readingTimeTodayCard,
                    value = strings.minutesRead(statsData.todayMinutes),
                    icon = Icons.Rounded.Schedule,
                    isGlass = isLiquidGlass,
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = strings.readingTimeTotalCard,
                    value = strings.totalHoursAndMinutes(statsData.totalMinutes / 60, statsData.totalMinutes % 60),
                    icon = Icons.Rounded.CalendarMonth,
                    isGlass = isLiquidGlass,
                    isDark = isDark,
                    modifier = Modifier.weight(1.2f)
                )
                StatCard(
                    title = strings.readingTimeAvgCard,
                    value = strings.minutesRead(statsData.dailyAverageMinutes),
                    icon = Icons.Rounded.BarChart,
                    isGlass = isLiquidGlass,
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Weekly Activity Chart Card
            val chartContent: @Composable () -> Unit = {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = strings.readingWeeklyActivityTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    WeeklyActivityChart(
                        weeklyStats = statsData.weeklyStats,
                        strings = strings,
                        isDark = isDark
                    )
                }
            }

            if (isLiquidGlass) {
                LiquidGlassSurface(
                    shape = RoundedCornerShape(20.dp),
                    isDark = isDark,
                    shadowElevation = 0.dp,
                    tintColor = null,
                    modifier = Modifier.fillMaxWidth(),
                    content = chartContent
                )
            } else {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    content = { chartContent() }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            var showConfirmReset by remember { mutableStateOf(false) }

            if (showConfirmReset) {
                AlertDialog(
                    onDismissRequest = { showConfirmReset = false },
                    title = { Text(strings.resetStatsConfirmTitle, fontWeight = FontWeight.Bold) },
                    text = { Text(strings.resetStatsConfirmMessage) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showConfirmReset = false
                                onResetStats()
                            }
                        ) {
                            Text(strings.resetStatsTitle, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirmReset = false }) {
                            Text(strings.cancel)
                        }
                    }
                )
            }

            OutlinedButton(
                onClick = { showConfirmReset = true },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.resetStatsTitle)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(strings.close, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun StreakBanner(
    statsData: ReadingStatsData,
    strings: Strings,
    isGlass: Boolean = false,
    isDark: Boolean = isSystemInDarkTheme()
) {
    val streak = statsData.currentStreakDays
    val hasStreak = streak > 0

    val bannerContent: @Composable () -> Unit = {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (hasStreak) Color(0xFFFF9500).copy(alpha = if (isDark) 0.18f else 0.12f)
                        else if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocalFireDepartment,
                    contentDescription = null,
                    tint = if (hasStreak) Color(0xFFFF9500) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (hasStreak) strings.readingStreakFormat(streak) else strings.readingStreakDays,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (hasStreak) strings.readingStreakKeepGoing else strings.readingStatsEmpty,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )
            }
        }
    }

    if (isGlass) {
        LiquidGlassSurface(
            shape = RoundedCornerShape(20.dp),
            isDark = isDark,
            shadowElevation = 0.dp,
            tintColor = null,
            modifier = Modifier.fillMaxWidth(),
            content = bannerContent
        )
    } else {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (hasStreak) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
            content = bannerContent
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isGlass: Boolean = false,
    isDark: Boolean = isSystemInDarkTheme(),
    modifier: Modifier = Modifier
) {
    val cardContent: @Composable () -> Unit = {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                softWrap = true
            )
        }
    }

    if (isGlass) {
        LiquidGlassSurface(
            shape = RoundedCornerShape(16.dp),
            isDark = isDark,
            shadowElevation = 0.dp,
            tintColor = null,
            modifier = modifier,
            content = cardContent
        )
    } else {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            modifier = modifier,
            content = { cardContent() }
        )
    }
}

@Composable
private fun WeeklyActivityChart(
    weeklyStats: List<DayReadingStat>,
    strings: Strings,
    isDark: Boolean = isSystemInDarkTheme()
) {
    val maxMinutes = (weeklyStats.maxOfOrNull { it.minutes } ?: 0).coerceAtLeast(30)
    var animationTriggered by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        animationTriggered = true
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        weeklyStats.forEach { stat ->
            val targetRatio = (stat.minutes.toFloat() / maxMinutes).coerceIn(0f, 1f)
            val animatedRatio by animateFloatAsState(
                targetValue = if (animationTriggered) targetRatio else 0f,
                animationSpec = tween(durationMillis = 600),
                label = "barHeight"
            )

            val dayName = when (stat.dayOfWeek) {
                Calendar.MONDAY -> strings.dayMon
                Calendar.TUESDAY -> strings.dayTue
                Calendar.WEDNESDAY -> strings.dayWed
                Calendar.THURSDAY -> strings.dayThu
                Calendar.FRIDAY -> strings.dayFri
                Calendar.SATURDAY -> strings.daySat
                Calendar.SUNDAY -> strings.daySun
                else -> ""
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                // Top minute label
                if (stat.minutes > 0) {
                    Text(
                        text = "${stat.minutes}м",
                        style = TextStyleCustom(fontSize = 10.sp),
                        fontWeight = if (stat.isToday) FontWeight.Bold else FontWeight.Normal,
                        color = if (stat.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Spacer(modifier = Modifier.height(14.dp))
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Bar
                Box(
                    modifier = Modifier
                        .width(18.dp)
                        .height(80.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    // Track background
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(9.dp))
                            .background(
                                if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
                            )
                    )

                    // Filled Bar (animated via Draw phase graphicsLayer scaleY to eliminate relayout jank)
                    if (targetRatio > 0f || stat.minutes > 0) {
                        val fillFraction = targetRatio.coerceAtLeast(if (stat.minutes > 0) 0.1f else 0f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(fillFraction)
                                .graphicsLayer {
                                    scaleY = animatedRatio
                                    transformOrigin = TransformOrigin(0.5f, 1f)
                                }
                                .clip(RoundedCornerShape(9.dp))
                                .background(
                                    if (stat.isToday) {
                                        Brush.verticalGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                            )
                                        )
                                    } else {
                                        Brush.verticalGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.secondary,
                                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                                            )
                                        )
                                    }
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Day of week label
                Text(
                    text = dayName,
                    style = TextStyleCustom(fontSize = 12.sp),
                    fontWeight = if (stat.isToday) FontWeight.Bold else FontWeight.Medium,
                    color = if (stat.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (stat.isToday) {
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                } else {
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}

private fun TextStyleCustom(fontSize: androidx.compose.ui.unit.TextUnit) = androidx.compose.ui.text.TextStyle(
    fontSize = fontSize,
    textAlign = TextAlign.Center
)
