package com.aura.reader.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.aura.reader.data.model.AppDesignStyle
import com.aura.reader.ui.theme.LocalAppStrings
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur

/**
 * Circular back button for the morphing settings dock.
 * Appears on the left side when in settings mode.
 */
@Composable
fun RoundBackGlassButton(
    onClick: () -> Unit,
    isGlass: Boolean,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val strings = LocalAppStrings.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val containerScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 650f),
        label = "backButtonScale"
    )
    val expansionProgress by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 650f),
        label = "backButtonPress"
    )

    val isAmoled = isDark && MaterialTheme.colorScheme.background == Color.Black
    val hazeState = LocalHazeState.current
    val hazeModifier = if (isGlass && hazeState != null) {
        Modifier.clip(CircleShape).hazeBlur(
            input = HazeInput.Sources(hazeState),
            style = HazeBlurStyle {
                blurRadius(16.dp)
                noiseFactor(0f)
            }
        )
    } else Modifier

    val dockBgColor = if (isGlass) {
        if (isDark) {
            if (isAmoled) Color(0xF20A0A0C) else Color(0xEE141416)
        } else {
            Color(0xF4F7F8FA)
        }
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.96f)
    }

    val dockBorderColor = if (isGlass) {
        if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    }

    val pillBgColor = if (isGlass) {
        if (isDark) {
            if (isAmoled) Color(0x10FFFFFF) else Color(0x0EFFFFFF)
        } else {
            Color(0x08000000)
        }
    } else {
        Color.Transparent
    }

    val borderAlpha = if (isDark) {
        0.05f + 0.22f * expansionProgress
    } else {
        0.04f + 0.16f * expansionProgress
    }
    val pillBorderColor = if (isGlass) {
        if (isDark) Color.White.copy(alpha = borderAlpha) else Color.Black.copy(alpha = borderAlpha)
    } else {
        Color.Transparent
    }
    val borderWidthDp = (0.5f + 0.4f * expansionProgress).dp

    Box(
        modifier = modifier
            .then(
                if (!isDark) {
                    Modifier.shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        clip = false,
                        ambientColor = Color.Black.copy(alpha = 0.06f),
                        spotColor = Color.Black.copy(alpha = 0.08f)
                    )
                } else Modifier
            )
            .size(60.dp)
            .graphicsLayer {
                scaleX = containerScale
                scaleY = containerScale
            }
            .clip(CircleShape)
            .then(hazeModifier)
            .background(dockBgColor)
            .border(
                width = 0.6.dp,
                color = dockBorderColor,
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        val pillSize = (46 + 6 * expansionProgress).dp
        Box(
            modifier = Modifier
                .size(pillSize)
                .clip(CircleShape)
                .background(pillBgColor)
                .border(
                    width = borderWidthDp,
                    color = pillBorderColor,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = strings.back,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Floating capsule dock for settings categories:
 * - Interface (0)
 * - General (1)
 * - About (2)
 *
 * The selected category expands with its label text, while inactive categories
 * display only their icons.
 */
@Composable
fun SettingsCategoryDock(
    selectedCategory: Int, // 0 = Interface, 1 = General, 2 = About
    onSelectCategory: (Int) -> Unit,
    isGlass: Boolean,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val strings = LocalAppStrings.current
    val dockShape = RoundedCornerShape(30.dp)

    val isAmoled = isDark && MaterialTheme.colorScheme.background == Color.Black
    val hazeState = LocalHazeState.current
    val hazeModifier = if (isGlass && hazeState != null) {
        Modifier.clip(dockShape).hazeBlur(
            input = HazeInput.Sources(hazeState),
            style = HazeBlurStyle {
                blurRadius(16.dp)
                noiseFactor(0f)
            }
        )
    } else Modifier

    val dockBgColor = if (isGlass) {
        if (isDark) {
            if (isAmoled) Color(0xF20A0A0C) else Color(0xEE141416)
        } else {
            Color(0xF4F7F8FA)
        }
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.96f)
    }

    val dockBorderColor = if (isGlass) {
        if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    }

    val categories = listOf(
        Icons.Rounded.Palette to strings.settingsCategoryInterface,
        Icons.Rounded.Tune to strings.settingsCategoryGeneral,
        Icons.Rounded.Info to strings.settingsCategoryAbout
    )

    Box(
        modifier = modifier
            .then(
                if (!isDark) {
                    Modifier.shadow(
                        elevation = 8.dp,
                        shape = dockShape,
                        clip = false,
                        ambientColor = Color.Black.copy(alpha = 0.06f),
                        spotColor = Color.Black.copy(alpha = 0.08f)
                    )
                } else Modifier
            )
            .height(60.dp)
            .clip(dockShape)
            .then(hazeModifier)
            .background(dockBgColor)
            .border(
                width = 0.6.dp,
                color = dockBorderColor,
                shape = dockShape
            )
            .padding(horizontal = 6.dp, vertical = 5.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            categories.forEachIndexed { index, (icon, title) ->
                val isSelected = selectedCategory == index
                val itemShape = RoundedCornerShape(25.dp)

                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSelectCategory(index)
                    },
                    shape = itemShape,
                    color = if (isSelected) {
                        if (isGlass) {
                            if (isDark) Color(0x22FFFFFF) else Color(0x14000000)
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        }
                    } else Color.Transparent,
                    border = if (isSelected && isGlass) {
                        BorderStroke(
                            0.6.dp,
                            if (isDark) Color.White.copy(alpha = 0.16f) else Color.Black.copy(alpha = 0.12f)
                        )
                    } else null,
                    modifier = Modifier
                        .fillMaxHeight()
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = if (isSelected) 14.dp else 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = if (isSelected) {
                                if (isGlass) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                if (isDark) Color.White.copy(alpha = 0.45f) else Color(0x8A1C1D24)
                            },
                            modifier = Modifier.size(20.dp)
                        )
                        AnimatedVisibility(
                            visible = isSelected,
                            enter = fadeIn(tween(180)) + expandHorizontally(
                                animationSpec = spring<IntSize>(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            ),
                            exit = fadeOut(tween(120)) + shrinkHorizontally(
                                animationSpec = spring<IntSize>(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isGlass) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Clip
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Master morphing dock that transitions between Main mode and Settings mode.
 */
@Composable
fun MorphingFloatingBottomBar(
    isSettingsOpen: Boolean,
    selectedMainTab: Int, // 0 = Books, 1 = Search
    selectedSettingsCategory: Int, // 0 = Interface, 1 = General, 2 = About
    designStyle: AppDesignStyle,
    clarity: Float,
    isDark: Boolean,
    onMyBooksClick: () -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onBackFromSettingsClick: () -> Unit,
    onSelectSettingsCategory: (Int) -> Unit,
    onAddBooksClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isGlass = designStyle == AppDesignStyle.LIQUID_GLASS

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedContent(
            targetState = isSettingsOpen,
            transitionSpec = {
                if (targetState) {
                    // Main -> Settings
                    (slideInHorizontally(
                        animationSpec = spring(dampingRatio = 0.82f, stiffness = 420f)
                    ) { width -> (width * 0.35f).toInt() } + fadeIn(tween(200)))
                        .togetherWith(
                            slideOutHorizontally(
                                animationSpec = spring(dampingRatio = 0.82f, stiffness = 420f)
                            ) { width -> (-width * 0.35f).toInt() } + fadeOut(tween(160))
                        )
                        .using(SizeTransform(clip = false))
                } else {
                    // Settings -> Main
                    (slideInHorizontally(
                        animationSpec = spring(dampingRatio = 0.82f, stiffness = 420f)
                    ) { width -> (-width * 0.35f).toInt() } + fadeIn(tween(200)))
                        .togetherWith(
                            slideOutHorizontally(
                                animationSpec = spring(dampingRatio = 0.82f, stiffness = 420f)
                            ) { width -> (width * 0.35f).toInt() } + fadeOut(tween(160))
                        )
                        .using(SizeTransform(clip = false))
                }
            },
            label = "dockMorphTransition"
        ) { inSettings ->
            if (inSettings) {
                // Settings Dock: [ Back (←) ] + [ Categories Capsule ]
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    RoundBackGlassButton(
                        onClick = onBackFromSettingsClick,
                        isGlass = isGlass,
                        isDark = isDark
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    SettingsCategoryDock(
                        selectedCategory = selectedSettingsCategory,
                        onSelectCategory = onSelectSettingsCategory,
                        isGlass = isGlass,
                        isDark = isDark
                    )
                }
            } else {
                // Main Dock: [ Navigation Island: Books | Search | Settings ] + [ + Add ]
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    FloatingNavigationIsland(
                        designStyle = designStyle,
                        clarity = clarity,
                        selectedTab = selectedMainTab,
                        isDark = isDark,
                        onMyBooksClick = onMyBooksClick,
                        onSearchClick = onSearchClick,
                        onSettingsClick = onSettingsClick
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    FloatingAddGlassButton(
                        onClick = onAddBooksClick,
                        isDark = isDark,
                        isGlass = isGlass
                    )
                }
            }
        }
    }
}
