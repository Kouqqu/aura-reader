package com.aura.reader.ui.screens.library

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.reader.data.preferences.DayReadingStat
import com.aura.reader.data.preferences.ReadingStatsData
import com.aura.reader.ui.theme.LocalAppStrings
import com.aura.reader.ui.theme.Strings
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingStatsBottomSheet(
    statsData: ReadingStatsData,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
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
                    imageVector = Icons.Default.Timeline,
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
            StreakBanner(statsData = statsData, strings = strings)

            Spacer(modifier = Modifier.height(16.dp))

            // 3 Quick Stat Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = strings.readingTimeTodayCard,
                    value = strings.minutesRead(statsData.todayMinutes),
                    icon = Icons.Default.Schedule,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = strings.readingTimeTotalCard,
                    value = strings.totalHoursAndMinutes(statsData.totalMinutes / 60, statsData.totalMinutes % 60),
                    icon = Icons.Default.CalendarMonth,
                    modifier = Modifier.weight(1.2f)
                )
                StatCard(
                    title = strings.readingTimeAvgCard,
                    value = strings.minutesRead(statsData.dailyAverageMinutes),
                    icon = Icons.Default.BarChart,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Weekly Activity Chart Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
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
                        strings = strings
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            FilledTonalButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(14.dp),
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
    strings: Strings
) {
    val streak = statsData.currentStreakDays
    val hasStreak = streak > 0

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (hasStreak) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (hasStreak) Color(0xFFFF9800).copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = if (hasStreak) Color(0xFFFF6D00) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (hasStreak) strings.readingStreakFormat(streak) else strings.readingStreakDays,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (hasStreak) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (hasStreak) strings.readingStreakKeepGoing else strings.readingStatsEmpty,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (hasStreak) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = modifier
    ) {
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
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun WeeklyActivityChart(
    weeklyStats: List<DayReadingStat>,
    strings: Strings
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
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    )

                    // Filled Bar
                    val barHeightDp = (80f * animatedRatio).coerceAtLeast(if (stat.minutes > 0) 8f else 0f)
                    if (barHeightDp > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(barHeightDp.dp)
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
