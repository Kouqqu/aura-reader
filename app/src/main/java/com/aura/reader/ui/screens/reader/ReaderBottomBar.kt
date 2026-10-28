package com.aura.reader.ui.screens.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import com.aura.reader.ui.components.LiquidGlassSurface
import androidx.compose.ui.platform.LocalHapticFeedback
import com.aura.reader.ui.theme.LocalAppStrings
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aura.reader.data.model.Chapter
import com.aura.reader.data.model.ReaderSettings
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun BoxScope.ReaderBottomBar(
    visible: Boolean,
    chapters: List<Chapter>,
    currentChapter: Chapter?,
    currentChapterIndex: Int,
    currentPagingPage: Int,
    totalPagingPages: Int,
    bookProgressPercent: Int,
    onRequestPage: (Int) -> Unit,
    onOpenChapters: () -> Unit,
    settings: ReaderSettings,
    isGlass: Boolean,
    isThemeDark: Boolean,
    isSepiaTheme: Boolean,
    viewModel: ReaderViewModel
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var lastPageSliderStep by remember { mutableIntStateOf(-1) }
    var lastChapterSliderStep by remember { mutableIntStateOf(-1) }

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
                    val bottomBarContent: @Composable () -> Unit = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            if (chapters.isNotEmpty()) {
                                if (settings.pagingMode) {
                                    // --- Paging Mode: Chapter Switcher Header + Page Slider ---
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = { viewModel.prevChapter() },
                                            enabled = currentChapterIndex > 0,
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                Icons.Rounded.SkipPrevious,
                                                contentDescription = strings.prevChapter,
                                                tint = if (currentChapterIndex > 0) {
                                                    if (isGlass) (if (isThemeDark) Color.White else Color(0xFF191B1F)) else MaterialTheme.colorScheme.onSurface
                                                } else {
                                                    (if (isThemeDark) Color.White else Color.Black).copy(alpha = 0.25f)
                                                }
                                            )
                                        }

                                        Row(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable { onOpenChapters() }
                                                .padding(horizontal = 6.dp, vertical = 4.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(
                                                modifier = Modifier.weight(1f, fill = false),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                val realChapterTitle = currentChapter?.title?.takeIf { it.isNotBlank() }
                                                    ?: "${strings.chapter} ${currentChapterIndex + 1}"
                                                Text(
                                                    text = realChapterTitle,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (isGlass) (if (isThemeDark) Color.White else Color(0xFF191B1F)) else MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "${strings.chapter} ${currentChapterIndex + 1} ${strings.ofChapters} ${chapters.size}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Rounded.ChevronRight,
                                                contentDescription = strings.contents,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = { viewModel.nextChapter() },
                                            enabled = currentChapterIndex < chapters.size - 1,
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                Icons.Rounded.SkipNext,
                                                contentDescription = strings.nextChapter,
                                                tint = if (currentChapterIndex < chapters.size - 1) {
                                                    if (isGlass) (if (isThemeDark) Color.White else Color(0xFF191B1F)) else MaterialTheme.colorScheme.onSurface
                                                } else {
                                                    (if (isThemeDark) Color.White else Color.Black).copy(alpha = 0.25f)
                                                }
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Page Slider
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = {
                                                if (currentPagingPage > 0) {
                                                    val target = currentPagingPage - 1
                                                    lastPageSliderStep = target
                                                    if (settings.hapticFeedbackEnabled) {
                                                        performSliderTick(context, haptic)
                                                    }
                                                    onRequestPage(target)
                                                }
                                            },
                                            enabled = currentPagingPage > 0,
                                            modifier = Modifier.size(36.dp)
                                         ) {
                                             Icon(
                                                 Icons.Rounded.ChevronLeft,
                                                 contentDescription = strings.prevPage,
                                                 tint = if (currentPagingPage > 0) {
                                                     if (isGlass) (if (isThemeDark) Color.White else Color(0xFF191B1F)) else MaterialTheme.colorScheme.onSurface
                                                 } else {
                                                     (if (isThemeDark) Color.White else Color.Black).copy(alpha = 0.25f)
                                                 }
                                             )
                                         }

                                         Slider(
                                             value = currentPagingPage.toFloat().coerceIn(0f, (totalPagingPages - 1).coerceAtLeast(0).toFloat()),
                                             onValueChange = { newVal ->
                                                 val step = newVal.toInt()
                                                 if (step != lastPageSliderStep) {
                                                     lastPageSliderStep = step
                                                     if (settings.hapticFeedbackEnabled) {
                                                         performSliderTick(context, haptic)
                                                     }
                                                 }
                                                 onRequestPage(step)
                                             },
                                             valueRange = 0f..(totalPagingPages - 1).coerceAtLeast(1).toFloat(),
                                             steps = (totalPagingPages - 2).coerceAtLeast(0),
                                             modifier = Modifier.weight(1f),
                                             colors = SliderDefaults.colors(
                                                 thumbColor = MaterialTheme.colorScheme.primary,
                                                 activeTrackColor = MaterialTheme.colorScheme.primary,
                                                 inactiveTrackColor = if (isGlass) {
                                                     if (isThemeDark) Color.White.copy(alpha = 0.18f) else Color.Black.copy(alpha = 0.12f)
                                                 } else MaterialTheme.colorScheme.surfaceContainerHighest
                                             )
                                         )

                                         IconButton(
                                             onClick = {
                                                 if (currentPagingPage < totalPagingPages - 1) {
                                                     val target = currentPagingPage + 1
                                                     lastPageSliderStep = target
                                                     if (settings.hapticFeedbackEnabled) {
                                                         performSliderTick(context, haptic)
                                                     }
                                                     onRequestPage(target)
                                                 }
                                             },
                                             enabled = currentPagingPage < totalPagingPages - 1,
                                             modifier = Modifier.size(36.dp)
                                         ) {
                                             Icon(
                                                 Icons.Rounded.ChevronRight,
                                                 contentDescription = strings.nextPage,
                                                 tint = if (currentPagingPage < totalPagingPages - 1) {
                                                     if (isGlass) (if (isThemeDark) Color.White else Color(0xFF191B1F)) else MaterialTheme.colorScheme.onSurface
                                                 } else {
                                                     (if (isThemeDark) Color.White else Color.Black).copy(alpha = 0.25f)
                                                 }
                                             )
                                         }
                                    }

                                    Text(
                                        text = "${strings.page} ${currentPagingPage + 1} ${strings.ofPages} $totalPagingPages  •  ${strings.pagingHint}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isGlass) (if (isThemeDark) Color.White.copy(alpha = 0.70f) else Color.Black.copy(alpha = 0.65f)) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                } else {
                                    // --- Continuous Mode: Chapter Slider ---
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { onOpenChapters() }
                                            .padding(horizontal = 6.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f, fill = false)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.AutoStories,
                                                contentDescription = strings.contents,
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            val realChapterTitle = currentChapter?.title?.takeIf { it.isNotBlank() }
                                                ?: "${strings.chapter} ${currentChapterIndex + 1}"
                                            Text(
                                                text = realChapterTitle,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isGlass) (if (isThemeDark) Color.White else Color(0xFF191B1F)) else MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                            Text(
                                                text = " (${currentChapterIndex + 1}/${chapters.size})",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isGlass) (if (isThemeDark) Color.White.copy(alpha = 0.60f) else Color.Black.copy(alpha = 0.55f)) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Rounded.ChevronRight,
                                                contentDescription = strings.contents,
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        val progress = bookProgressPercent
                                        Text(
                                            text = "$progress%",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = { viewModel.prevChapter() },
                                            enabled = currentChapterIndex > 0,
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                 Icons.Rounded.ChevronLeft,
                                                 contentDescription = strings.prevChapter,
                                                 tint = if (currentChapterIndex > 0) {
                                                     if (isGlass) (if (isThemeDark) Color.White else Color(0xFF191B1F)) else MaterialTheme.colorScheme.onSurface
                                                 } else {
                                                     (if (isThemeDark) Color.White else Color.Black).copy(alpha = 0.25f)
                                                 }
                                             )
                                        }

                                        Slider(
                                            value = currentChapterIndex.toFloat(),
                                            onValueChange = { newVal ->
                                                val step = newVal.toInt()
                                                if (step != lastChapterSliderStep) {
                                                    lastChapterSliderStep = step
                                                    if (settings.hapticFeedbackEnabled) {
                                                        performSliderTick(context, haptic)
                                                    }
                                                }
                                                viewModel.setChapter(step)
                                            },
                                            valueRange = 0f..(chapters.size - 1).coerceAtLeast(1).toFloat(),
                                            steps = (chapters.size - 2).coerceAtLeast(0),
                                            modifier = Modifier.weight(1f),
                                            colors = SliderDefaults.colors(
                                                thumbColor = MaterialTheme.colorScheme.primary,
                                                activeTrackColor = MaterialTheme.colorScheme.primary
                                            )
                                        )

                                        IconButton(
                                            onClick = { viewModel.nextChapter() },
                                            enabled = currentChapterIndex < chapters.size - 1,
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                 Icons.Rounded.ChevronRight,
                                                 contentDescription = strings.nextChapter,
                                                 tint = if (currentChapterIndex < chapters.size - 1) {
                                                     if (isGlass) (if (isThemeDark) Color.White else Color(0xFF191B1F)) else MaterialTheme.colorScheme.onSurface
                                                 } else {
                                                     (if (isThemeDark) Color.White else Color.Black).copy(alpha = 0.25f)
                                                 }
                                             )
                                        }
                                    }

                                    Text(
                                        text = strings.chaptersNavigationHint,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isGlass) (if (isThemeDark) Color.White.copy(alpha = 0.70f) else Color.Black.copy(alpha = 0.65f)) else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                }
                            }
                        }
                    }

                    if (isGlass) {
                        val bottomBarBg = if (isSepiaTheme) Color(0xF4FAF0E6) else null
                        LiquidGlassSurface(
                            shape = RoundedCornerShape(24.dp),
                            shadowElevation = 10.dp,
                            isDark = isThemeDark,
                            tintColor = bottomBarBg,
                            modifier = Modifier.fillMaxWidth(),
                            content = bottomBarContent
                        )
                    } else {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.98f),
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                            modifier = Modifier.fillMaxWidth(),
                            content = { bottomBarContent() }
                        )
                    }
                }
}

private fun performSliderTick(context: Context, haptic: HapticFeedback) {
    try {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && vibrator?.hasVibrator() == true) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    } catch (_: Exception) {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
}
