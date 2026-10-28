package com.aura.reader.ui.screens.onboarding

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.reader.R
import com.aura.reader.data.model.AppDesignStyle
import com.aura.reader.data.model.ReaderSettings
import com.aura.reader.data.preferences.PreferencesManager
import com.aura.reader.ui.components.LiquidGlassSurface
import com.aura.reader.ui.components.expressiveBounce
import com.aura.reader.ui.components.expressiveClickable
import com.aura.reader.ui.theme.LocalAppStrings
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun OnboardingScreen(
    preferencesManager: PreferencesManager,
    onFinish: () -> Unit
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isDark = isSystemInDarkTheme()

    val readerSettings by preferencesManager.readerSettings.collectAsState(initial = ReaderSettings())
    val appDesignStyle = readerSettings.appDesignStyle
    val isGlass = appDesignStyle == AppDesignStyle.LIQUID_GLASS

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
            // Top Bar with Skip Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    val marcellusFamily = remember { androidx.compose.ui.text.font.FontFamily(androidx.compose.ui.text.font.Font(com.aura.reader.R.font.marcellus_regular)) }
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
                    0 -> OnboardingStyleSlide(
                        currentStyle = appDesignStyle,
                        onStyleSelected = { style ->
                            coroutineScope.launch {
                                preferencesManager.setAppDesignStyle(style)
                            }
                        }
                    )
                    1 -> OnboardingFormatsSlide(isGlass = isGlass)
                    2 -> OnboardingTtsSlide(isGlass = isGlass)
                    3 -> OnboardingCommunitySlide(
                        isGlass = isGlass,
                        onOpenTelegram = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/koukkuteka"))
                            context.startActivity(intent)
                        },
                        onOpenGithub = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Kouqqu/aura-reader"))
                            context.startActivity(intent)
                        }
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

                // Action Pill Button (Next -> Start Reading)
                val isLastPage = pagerState.currentPage == totalSlides - 1
                if (isGlass) {
                    Surface(
                        onClick = {
                            if (isLastPage) {
                                finishOnboarding()
                            } else {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        },
                        shape = RoundedCornerShape(28.dp),
                        color = if (isDark) Color(0x28FFFFFF) else Color(0xF2FFFFFF),
                        border = null,
                        shadowElevation = if (isDark) 0.dp else 4.dp,
                        modifier = Modifier
                            .height(54.dp)
                            .expressiveClickable {
                                if (isLastPage) {
                                    finishOnboarding()
                                } else {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                    }
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isLastPage) strings.onboardingStartReading else strings.onboardingNext,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) Color.White else Color(0xFF191B1F)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = if (isLastPage) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = if (isDark) Color.White else Color(0xFF191B1F),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            if (isLastPage) {
                                finishOnboarding()
                            } else {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
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
                                if (isLastPage) {
                                    finishOnboarding()
                                } else {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                    }
                                }
                            },
                        contentPadding = PaddingValues(horizontal = 24.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isLastPage) strings.onboardingStartReading else strings.onboardingNext,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = if (isLastPage) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
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

/**
 * Slide 0: Choose Design Style (Material 3 Expressive vs Liquid Glass) with instant reactive transformation.
 */
@Composable
private fun OnboardingStyleSlide(
    currentStyle: AppDesignStyle,
    onStyleSelected: (AppDesignStyle) -> Unit
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
        // Expressive Scalloped Flower Badge
        ExpressiveScallopedBadge(
            icon = Icons.Default.Palette,
            petalColor = MaterialTheme.colorScheme.primaryContainer,
            iconTint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = strings.onboardingStyleTitle,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = strings.onboardingStyleDesc,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Option 1: Material 3 Expressive (Solid tonal surfaces, expressive shape, dynamic swatches)
        MaterialStyleOptionCard(
            title = strings.designStyleMaterial,
            subtitle = strings.designStyleDescMaterial,
            isSelected = currentStyle == AppDesignStyle.MATERIAL,
            onClick = { onStyleSelected(AppDesignStyle.MATERIAL) }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Option 2: Liquid Glass (Frosted translucent gradient, specular glossy borders, neon cyan glow)
        LiquidGlassStyleOptionCard(
            title = strings.designStyleLiquidGlass,
            subtitle = strings.designStyleDescLiquidGlass,
            isSelected = currentStyle == AppDesignStyle.LIQUID_GLASS,
            onClick = { onStyleSelected(AppDesignStyle.LIQUID_GLASS) }
        )
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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(borderWidth, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .expressiveClickable(scaleDown = 0.98f, onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Material Tonal Icon Box
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

            }

            Spacer(modifier = Modifier.width(12.dp))

            // Material Expressive Selection Indicator
            Box(
                modifier = Modifier
                    .size(26.dp)
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
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
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
    val glassShape = RoundedCornerShape(24.dp)

    // Luminous, translucent frosted glass (clearly visible, frosted silver-grey in dark mode, NOT black!)
    val cardBgColor = if (isDark) {
        if (isSelected) Color(0x2EFFFFFF) else Color(0x1AFFFFFF)
    } else {
        if (isSelected) Color(0xF5FFFFFF) else Color(0xD8FFFFFF)
    }

    // Top-lit specular glass rim
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
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Frosted glass icon vessel
            Box(
                modifier = Modifier
                    .size(46.dp)
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
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = if (isDark) Color.White else Color(0xFF191B1F),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Clean iOS Selection Indicator
            Box(
                modifier = Modifier
                    .size(26.dp)
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
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Slide 1: Supported formats & OPDS catalogs
 */
@Composable
private fun OnboardingFormatsSlide(isGlass: Boolean = false) {
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
        ExpressiveScallopedBadge(
            icon = Icons.AutoMirrored.Filled.MenuBook,
            petalColor = MaterialTheme.colorScheme.secondaryContainer,
            iconTint = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = strings.onboardingFormatsTitle,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = strings.onboardingFormatsDesc,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        FeatureHighlightCard(
            icon = Icons.Default.AutoAwesome,
            title = "EPUB, FB2, MOBI, PDF, TXT, CBZ",
            description = "Умный парсер глав, сохранение изображений, сноски и таблицы",
            isGlass = isGlass
        )

        Spacer(modifier = Modifier.height(12.dp))

        FeatureHighlightCard(
            icon = Icons.AutoMirrored.Filled.MenuBook,
            title = "Каталоги OPDS",
            description = "Поиск и загрузка тысяч книг напрямую из онлайн-библиотек",
            isGlass = isGlass
        )

        Spacer(modifier = Modifier.height(12.dp))

        FeatureHighlightCard(
            icon = Icons.Default.BarChart,
            title = "Статистика чтения",
            description = "Трекинг времени, ежедневный стрик и точный персональный WPM",
            isGlass = isGlass
        )
    }
}

/**
 * Slide 2: Real-time TTS Narration with Word Highlight
 */
@Composable
private fun OnboardingTtsSlide(isGlass: Boolean = false) {
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
        ExpressiveScallopedBadge(
            icon = Icons.AutoMirrored.Filled.VolumeUp,
            petalColor = MaterialTheme.colorScheme.tertiaryContainer,
            iconTint = MaterialTheme.colorScheme.tertiary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = strings.onboardingTtsTitle,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = strings.onboardingTtsDesc,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        FeatureHighlightCard(
            icon = Icons.Default.AutoAwesome,
            title = "Подсветка слов в реальном времени",
            description = "Слово, произносимое прямо сейчас, мягко подсвечивается в тексте",
            isGlass = isGlass
        )

        Spacer(modifier = Modifier.height(12.dp))

        FeatureHighlightCard(
            icon = Icons.AutoMirrored.Filled.MenuBook,
            title = "Озвучка с любого места",
            description = "Нажмите на любое слово в книге, чтобы начать чтение именно с него",
            isGlass = isGlass
        )

        Spacer(modifier = Modifier.height(12.dp))

        FeatureHighlightCard(
            icon = Icons.AutoMirrored.Filled.VolumeUp,
            title = "Большой выбор голосов",
            description = "Высокое качество речи, регулировка темпа и высоты тона",
            isGlass = isGlass
        )
    }
}

/**
 * Slide 3: Community, Telegram channel & GitHub
 */
@Composable
private fun OnboardingCommunitySlide(
    isGlass: Boolean = false,
    onOpenTelegram: () -> Unit,
    onOpenGithub: () -> Unit
) {
    val strings = LocalAppStrings.current
    val scrollState = rememberScrollState()
    val isDark = isSystemInDarkTheme()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ExpressiveScallopedBadge(
            painterResId = R.drawable.ic_telegram,
            petalColor = Color(0xFF2AABEE).copy(alpha = 0.2f),
            iconTint = Color(0xFF2AABEE)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = strings.onboardingCommunityTitle,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = strings.onboardingCommunityDesc,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Telegram Channel Card
        if (isGlass) {
            val shape = RoundedCornerShape(22.dp)
            LiquidGlassSurface(
                shape = shape,
                isDark = isDark,
                shadowElevation = 4.dp,
                tintColor = if (isDark) Color(0x221E2330) else Color(0xD0FFFFFF),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.8.dp, Color.White.copy(alpha = if (isDark) 0.18f else 0.45f), shape)
                    .expressiveClickable(scaleDown = 0.98f, onClick = onOpenTelegram)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2AABEE).copy(alpha = 0.20f))
                            .border(1.dp, Color(0xFF2AABEE).copy(alpha = 0.40f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_telegram),
                            contentDescription = null,
                            tint = Color(0xFF2AABEE),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Telegram: @koukkuteka",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Официальный канал обновлений и предложений",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFF2AABEE),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .expressiveClickable(scaleDown = 0.98f, onClick = onOpenTelegram)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2AABEE).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_telegram),
                            contentDescription = null,
                            tint = Color(0xFF2AABEE),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Telegram: @koukkuteka",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Официальный канал обновлений и предложений",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFF2AABEE),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // GitHub Repository Card
        if (isGlass) {
            val shape = RoundedCornerShape(22.dp)
            LiquidGlassSurface(
                shape = shape,
                isDark = isDark,
                shadowElevation = 4.dp,
                tintColor = if (isDark) Color(0x221E2330) else Color(0xD0FFFFFF),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.8.dp, Color.White.copy(alpha = if (isDark) 0.18f else 0.45f), shape)
                    .expressiveClickable(scaleDown = 0.98f, onClick = onOpenGithub)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0x30FFFFFF) else Color(0x12000000))
                            .border(1.dp, Color.White.copy(alpha = if (isDark) 0.35f else 0.45f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_github),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "GitHub Repository",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Kouqqu/aura-reader",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .expressiveClickable(scaleDown = 0.98f, onClick = onOpenGithub)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_github),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "GitHub Repository",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Kouqqu/aura-reader",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureHighlightCard(
    icon: ImageVector,
    title: String,
    description: String,
    isGlass: Boolean = false
) {
    val isDark = isSystemInDarkTheme()
    val shape = RoundedCornerShape(20.dp)

    if (isGlass) {
        LiquidGlassSurface(
            shape = shape,
            isDark = isDark,
            shadowElevation = 4.dp,
            tintColor = if (isDark) Color(0x221E2330) else Color(0xD0FFFFFF),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 0.8.dp,
                    color = Color.White.copy(alpha = if (isDark) 0.18f else 0.45f),
                    shape = shape
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isDark) Color(0x3000C6FF) else Color(0x200284C7)
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = if (isDark) 0.35f else 0.55f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    } else {
        Card(
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
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
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


/**
 * Material 3 Expressive Scalloped Flower Badge Container
 * Generates an organic 12-lobed flower/starburst shape with smooth sinusoidal petals.
 */
@Composable
private fun ExpressiveScallopedBadge(
    icon: ImageVector? = null,
    painterResId: Int? = null,
    petalColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(110.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = size.minDimension / 2f * 0.82f
            val amplitude = size.minDimension / 2f * 0.12f
            val lobes = 10

            val path = Path()
            val points = 360
            for (i in 0 until points) {
                val theta = (i * 2.0 * PI / points).toFloat()
                val r = baseRadius + amplitude * cos(lobes * theta)
                val x = center.x + r * cos(theta)
                val y = center.y + r * sin(theta)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path = path, color = petalColor)
        }

        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(48.dp)
            )
        } else if (painterResId != null) {
            Icon(
                painter = painterResource(painterResId),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}
