package com.aura.reader.ui.screens.onboarding

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.IconButton
import com.aura.reader.R
import com.aura.reader.data.model.AppDesignStyle
import com.aura.reader.data.model.ReaderSettings
import com.aura.reader.data.model.ReaderThemeMode
import com.aura.reader.data.preferences.PreferencesManager
import com.aura.reader.ui.components.LiquidGlassSurface
import com.aura.reader.ui.components.expressiveClickable
import com.aura.reader.ui.theme.LocalAppStrings
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    preferencesManager: PreferencesManager,
    onFinish: () -> Unit
) {
    val strings = LocalAppStrings.current
    val coroutineScope = rememberCoroutineScope()
    val isDark = isSystemInDarkTheme()

    val readerSettings by preferencesManager.readerSettings.collectAsState(initial = ReaderSettings())
    val appDesignStyle = readerSettings.appDesignStyle
    val isGlass = appDesignStyle == AppDesignStyle.LIQUID_GLASS

    // Default to dark theme for fresh onboarding
    LaunchedEffect(Unit) {
        if (readerSettings.appThemeMode == ReaderThemeMode.SYSTEM_DYNAMIC) {
            preferencesManager.updateAppThemeMode(ReaderThemeMode.DARK)
            preferencesManager.updateThemeMode(ReaderThemeMode.DARK)
        }
    }

    val totalSlides = 4
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { totalSlides })

    val finishOnboarding: () -> Unit = {
        coroutineScope.launch {
            preferencesManager.setHasCompletedOnboarding(true)
            onFinish()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar with App Logo and Skip Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(R.drawable.ic_aura_logo_squircle),
                        contentDescription = null,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    val marcellusFamily = remember { FontFamily(androidx.compose.ui.text.font.Font(R.font.marcellus_regular)) }
                    Text(
                        text = strings.appName,
                        fontFamily = marcellusFamily,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 19.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                TextButton(
                    onClick = finishOnboarding,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = strings.onboardingSkip,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Pager content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                when (page) {
                    0 -> OnboardingWelcomeAndStyleSlide(
                        currentStyle = appDesignStyle,
                        currentTheme = readerSettings.appThemeMode,
                        onStyleSelected = { style ->
                            coroutineScope.launch {
                                preferencesManager.setAppDesignStyle(style)
                            }
                        },
                        onThemeSelected = { mode ->
                            coroutineScope.launch {
                                preferencesManager.updateAppThemeMode(mode)
                                preferencesManager.updateThemeMode(mode)
                            }
                        }
                    )
                    1 -> OnboardingWordActionSlide(isGlass = isGlass)
                    2 -> OnboardingMoreFeaturesSlide(isGlass = isGlass)
                    3 -> OnboardingFinishSlide(
                        onFinish = finishOnboarding
                    )
                }
            }

            // Bottom Navigation Island with animated expanding pill indicator and Next/Finish button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Expanding pill page indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(totalSlides) { index ->
                        val isSelected = pagerState.currentPage == index
                        val width by animateDpAsState(
                            targetValue = if (isSelected) 28.dp else 8.dp,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "pagerIndicatorWidth"
                        )
                        val color = if (isSelected) {
                            if (isGlass) Color.White else MaterialTheme.colorScheme.primary
                        } else {
                            if (isGlass) Color.White.copy(alpha = if (isDark) 0.20f else 0.35f)
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
                        }

                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }

                // Action Pill Button (Next button on slides 0, 1, 2; on last slide button is in the center of the slide)
                val isLastPage = pagerState.currentPage == totalSlides - 1
                if (!isLastPage) {
                    if (isGlass) {
                        Surface(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                            shape = RoundedCornerShape(28.dp),
                            color = if (isDark) Color(0x28FFFFFF) else Color(0xF2FFFFFF),
                            border = null,
                            shadowElevation = if (isDark) 0.dp else 4.dp,
                            modifier = Modifier
                                .height(54.dp)
                                .expressiveClickable {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 24.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = strings.onboardingNext,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDark) Color.White else Color(0xFF191B1F)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                    contentDescription = null,
                                    tint = if (isDark) Color.White else Color(0xFF191B1F),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .height(54.dp)
                                .expressiveClickable {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                    }
                                },
                            contentPadding = PaddingValues(horizontal = 24.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = strings.onboardingNext,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
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
 * Slide 0: «Добро пожаловать» — Style selector and Theme mode selector (Dark by default).
 */
@Composable
private fun OnboardingWelcomeAndStyleSlide(
    currentStyle: AppDesignStyle,
    currentTheme: ReaderThemeMode,
    onStyleSelected: (AppDesignStyle) -> Unit,
    onThemeSelected: (ReaderThemeMode) -> Unit
) {
    val strings = LocalAppStrings.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Design Style Options
        MaterialStyleOptionCard(
            title = strings.designStyleMaterial,
            subtitle = strings.designStyleDescMaterial,
            isSelected = currentStyle == AppDesignStyle.MATERIAL,
            onClick = { onStyleSelected(AppDesignStyle.MATERIAL) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        LiquidGlassStyleOptionCard(
            title = strings.designStyleLiquidGlass,
            subtitle = strings.designStyleDescLiquidGlass,
            isSelected = currentStyle == AppDesignStyle.LIQUID_GLASS,
            onClick = { onStyleSelected(AppDesignStyle.LIQUID_GLASS) }
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Segmented Theme Mode Selector (Light / Dark / System)
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Light Mode
                ThemeOptionPill(
                    icon = Icons.Rounded.LightMode,
                    title = strings.themeLight,
                    isSelected = currentTheme == ReaderThemeMode.LIGHT,
                    onClick = { onThemeSelected(ReaderThemeMode.LIGHT) },
                    modifier = Modifier.weight(1f)
                )

                // Dark Mode (Active by default)
                ThemeOptionPill(
                    icon = Icons.Rounded.DarkMode,
                    title = strings.themeDark,
                    isSelected = currentTheme == ReaderThemeMode.DARK || currentTheme == ReaderThemeMode.AMOLED,
                    onClick = { onThemeSelected(ReaderThemeMode.DARK) },
                    modifier = Modifier.weight(1f)
                )

                // System Mode
                ThemeOptionPill(
                    icon = Icons.Rounded.BrightnessAuto,
                    title = strings.themeSystem,
                    isSelected = currentTheme == ReaderThemeMode.SYSTEM_DYNAMIC,
                    onClick = { onThemeSelected(ReaderThemeMode.SYSTEM_DYNAMIC) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = strings.onboardingWelcomeTitle,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = strings.onboardingWelcomeDesc,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ThemeOptionPill(
    icon: ImageVector,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        modifier = modifier.height(44.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

/**
 * Slide 1: «Зажми слово» — Text selection mockup with real toolbar buttons: Цитата, Словарь, Перевод.
 */
@Composable
private fun OnboardingWordActionSlide(isGlass: Boolean = false) {
    val strings = LocalAppStrings.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Book Text & Floating Selection Toolbar Mockup
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Floating Selection Toolbar Mockup
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shadowElevation = 8.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Button 1: Quote (Цитата)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.FormatQuote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = strings.quoteAction,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Box(
                            modifier = Modifier
                                .height(14.dp)
                                .width(1.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        )

                        // Button 2: Dictionary (Словарь)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.AutoStories,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = strings.dictionaryAction,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Box(
                            modifier = Modifier
                                .height(14.dp)
                                .width(1.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        )

                        // Button 3: Translation (Перевод)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Translate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = strings.translateAction,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Line of book text with highlighted word
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Он ",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 21.sp,
                            fontStyle = FontStyle.Italic
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.28f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "улыбнулся",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Italic
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = " в ответ",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 21.sp,
                            fontStyle = FontStyle.Italic
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = strings.onboardingWordActionTitle,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = strings.onboardingWordActionDesc,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Slide 2: «И ещё много всего» — 4 feature tiles (OPDS, TTS, Comics/PDF, E-Ink).
 */
@Composable
private fun OnboardingMoreFeaturesSlide(isGlass: Boolean = false) {
    val strings = LocalAppStrings.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 2x2 Grid of Feature Tiles
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FeatureSquareCard(
                    icon = Icons.Rounded.CloudDownload,
                    title = "Каталог OPDS",
                    subtitle = "Поиск и загрузка книг",
                    modifier = Modifier.weight(1f)
                )
                FeatureSquareCard(
                    icon = Icons.AutoMirrored.Rounded.VolumeUp,
                    title = "Чтение вслух",
                    subtitle = "Встроенный TTS-плеер",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FeatureSquareCard(
                    icon = Icons.Rounded.AutoStories,
                    title = "Комиксы и PDF",
                    subtitle = "Лента и масштаб",
                    modifier = Modifier.weight(1f)
                )
                FeatureSquareCard(
                    icon = Icons.Rounded.Contrast,
                    title = "Режим E-Ink",
                    subtitle = "Для электронных книг",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = strings.onboardingMoreFeaturesTitle,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = strings.onboardingMoreFeaturesDesc,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun FeatureSquareCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 15.sp
            )
        }
    }
}

/**
 * Slide 3: «Приятного чтения!» — Aura Reader squircle logo and Start Reading button.
 */
@Composable
private fun OnboardingFinishSlide(
    onFinish: () -> Unit
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Prominent Squircle Logo
        Image(
            painter = painterResource(R.drawable.ic_aura_logo_squircle),
            contentDescription = strings.appName,
            modifier = Modifier
                .size(112.dp)
                .clip(RoundedCornerShape(26.dp))
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = strings.onboardingEnjoyTitle,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = strings.onboardingEnjoyDesc,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onFinish,
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp),
            modifier = Modifier.height(52.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = strings.onboardingStartReading,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFF2AABEE).copy(alpha = 0.14f),
                border = BorderStroke(1.dp, Color(0xFF2AABEE).copy(alpha = 0.28f)),
                modifier = Modifier.size(44.dp)
            ) {
                IconButton(
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/koukkuteka")))
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_telegram),
                        contentDescription = strings.telegramChannelTitle,
                        tint = Color(0xFF2AABEE),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.size(44.dp)
            ) {
                IconButton(
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Kouqqu/aura-reader")))
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_github),
                        contentDescription = strings.githubRepository,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MaterialStyleOptionCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    val borderWidth = if (isSelected) 2.dp else 1.dp
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(borderWidth, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .expressiveClickable(scaleDown = 0.98f, onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Palette,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .border(
                        width = if (isSelected) 0.dp else 2.dp,
                        color = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape
                    )
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LiquidGlassStyleOptionCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val glassShape = RoundedCornerShape(22.dp)

    val cardBgColor = if (isDark) {
        if (isSelected) Color(0x2EFFFFFF) else Color(0x1AFFFFFF)
    } else {
        if (isSelected) Color(0xF5FFFFFF) else Color(0xD8FFFFFF)
    }

    val borderBrush = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = if (isSelected) 0.38f else 0.16f),
            Color.White.copy(alpha = if (isSelected) 0.10f else 0.04f)
        )
    )

    LiquidGlassSurface(
        shape = glassShape,
        isDark = isDark,
        tintColor = cardBgColor,
        highlightIntensity = 0f,
        shadowElevation = if (isDark) 0.dp else 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 0.8.dp,
                brush = borderBrush,
                shape = glassShape
            )
            .expressiveClickable(scaleDown = 0.98f, onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isDark) Color(0x22FFFFFF) else Color(0x0C000000)
                    )
                    .border(
                        width = 0.5.dp,
                        color = Color.White.copy(alpha = if (isDark) 0.18f else 0.25f),
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = if (isDark) Color.White else Color(0xFF191B1F),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) Color(0xFF007AFF) else Color.Transparent
                    )
                    .border(
                        width = if (isSelected) 0.dp else 1.5.dp,
                        color = if (isSelected) Color.Transparent else Color.White.copy(alpha = if (isDark) 0.22f else 0.35f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}
