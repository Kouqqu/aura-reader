package com.aura.reader.ui.screens.library
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.automirrored.rounded.ArrowBack

import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.LocalTextStyle
import com.aura.reader.ui.theme.LocalAppStrings
import com.aura.reader.ui.theme.dayPeriodForHour
import com.aura.reader.ui.theme.greetingFor
import com.aura.reader.ui.theme.holidayDisplayName
import com.aura.reader.ui.theme.holidayGreeting
import com.aura.reader.ui.theme.rememberCurrentHour
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.aura.reader.data.model.HolidaySeason
import com.aura.reader.data.model.LocalHolidaySeason
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.sin
import kotlin.math.cos

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
internal fun LibraryTopBar(
    isSearchExpanded: Boolean,
    isGlass: Boolean,
    isThemeDark: Boolean,
    searchQuery: String,
    libraryViewMode: String,
    scrollBehavior: TopAppBarScrollBehavior,
    pageOffset: Float = 0f,
    onSearchExpandedChange: (Boolean) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleViewMode: () -> Unit,
    onOpenSettings: () -> Unit,
    onHolidayBadgeClick: (Offset) -> Unit = {}
) {
    val strings = LocalAppStrings.current

AnimatedContent(
    targetState = isSearchExpanded && !isGlass,
    transitionSpec = {
        if (targetState) {
            (fadeIn(animationSpec = tween(220, delayMillis = 40)) +
             slideInVertically(animationSpec = tween(260)) { -it / 2 } +
             expandVertically(animationSpec = tween(260)))
                .togetherWith(
                    fadeOut(animationSpec = tween(160)) +
                    shrinkVertically(animationSpec = tween(180))
                )
        } else {
            (fadeIn(animationSpec = tween(220, delayMillis = 40)) +
             expandVertically(animationSpec = tween(240)))
                .togetherWith(
                    fadeOut(animationSpec = tween(160)) +
                    slideOutVertically(animationSpec = tween(180)) { -it / 2 } +
                    shrinkVertically(animationSpec = tween(180))
                )
        }
    },
    label = "materialSearchTopBarTransition"
) { searching ->
    if (searching) {
        val searchFocusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(120)
            searchFocusRequester.requestFocus()
        }
        TopAppBar(
            title = {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { onSearchQueryChange(it) },
                    placeholder = { Text(strings.searchLibraryPlaceholder) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(searchFocusRequester)
                )
            },
            navigationIcon = {
                IconButton(onClick = {
                    onSearchExpandedChange(false)
                    onSearchQueryChange("")
                }) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.cancel)
                }
            },
            actions = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Rounded.Close, contentDescription = strings.clear)
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )
    } else {
        val holidaySeason = LocalHolidaySeason.current
        var topBarCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
        var expandedLogoCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

        val isHoliday = holidaySeason != HolidaySeason.NONE
        val nodeColors = if (isHoliday) {
            listOf(
                holidaySeason.primaryAccent,
                holidaySeason.secondaryAccent,
                Color(0xFF8A4DFF),
                holidaySeason.primaryAccent.copy(alpha = 0.85f)
            )
        } else {
            listOf(
                Color(0xFFFFB74D), // 1. Warm Golden Amber
                Color(0xFFF06292), // 2. Soft Rose-Coral
                Color(0xFF7E57C2), // 3. Lavender Royal Twilight
                Color(0xFF26A69A)  // 4. Celestial Sea Teal
            )
        }

        val auraTransition = rememberInfiniteTransition(label = "auraLivingMesh")
        val meshPhase by auraTransition.animateFloat(
            initialValue = 0f,
            targetValue = 62.831853f, // 10 * 2 * PI
            animationSpec = infiniteRepeatable(
                animation = tween(72000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "meshPhase"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coords ->
                    topBarCoords = coords
                }
                .drawBehind {
                    val top = topBarCoords
                    val logo = expandedLogoCoords
                    val collapsedFraction = scrollBehavior.state.collapsedFraction
                    val auraAlpha = (1f - pageOffset).coerceIn(0f, 1f)
                    val scrollAlpha = (1f - collapsedFraction / 0.35f).coerceIn(0f, 1f) * auraAlpha

                    if (isThemeDark && scrollAlpha > 0.005f) {
                        val cloudCenter = if (top != null && logo != null && top.isAttached && logo.isAttached) {
                            val logoPos = top.localPositionOf(logo, Offset(logo.size.width / 2f, logo.size.height / 2f))
                            Offset(logoPos.x + 56.dp.toPx(), logoPos.y)
                        } else {
                            Offset(size.width * 0.38f, size.height * 0.60f)
                        }

                        val baseRadiusPx = 150.dp.toPx()
                        val t = meshPhase

                        // Gentle, slow harmonic displacement (Simplex/Perlin noise-like organic wander)
                        val x1 = cloudCenter.x + baseRadiusPx * (0.34f * sin(0.21f * t) + 0.15f * cos(0.42f * t) - 0.10f)
                        val y1 = cloudCenter.y + baseRadiusPx * (0.26f * cos(0.27f * t) + 0.12f * sin(0.53f * t) - 0.06f)
                        val r1 = baseRadiusPx * (1.10f + 0.14f * sin(0.33f * t))
                        val a1 = 0.34f * (1.0f + 0.12f * cos(0.24f * t)) * scrollAlpha

                        val x2 = cloudCenter.x + baseRadiusPx * (0.38f * cos(0.19f * t + 1.4f) + 0.14f * sin(0.45f * t) + 0.08f)
                        val y2 = cloudCenter.y + baseRadiusPx * (0.30f * sin(0.31f * t + 0.8f) - 0.12f * cos(0.59f * t) + 0.10f)
                        val r2 = baseRadiusPx * (1.16f + 0.15f * cos(0.29f * t))
                        val a2 = 0.32f * (1.0f + 0.13f * sin(0.37f * t)) * scrollAlpha

                        val x3 = cloudCenter.x + baseRadiusPx * (0.35f * sin(0.24f * t + 2.8f) - 0.14f * cos(0.39f * t) - 0.14f)
                        val y3 = cloudCenter.y + baseRadiusPx * (0.34f * cos(0.22f * t + 2.1f) + 0.14f * sin(0.56f * t) + 0.06f)
                        val r3 = baseRadiusPx * (1.22f + 0.16f * sin(0.26f * t))
                        val a3 = 0.35f * (1.0f + 0.12f * cos(0.31f * t)) * scrollAlpha

                        val x4 = cloudCenter.x + baseRadiusPx * (0.39f * cos(0.27f * t + 4.2f) + 0.16f * sin(0.36f * t) + 0.16f)
                        val y4 = cloudCenter.y + baseRadiusPx * (0.28f * sin(0.20f * t + 3.5f) + 0.12f * cos(0.49f * t) - 0.10f)
                        val r4 = baseRadiusPx * (1.10f + 0.14f * cos(0.39f * t))
                        val a4 = 0.30f * (1.0f + 0.14f * sin(0.28f * t)) * scrollAlpha

                        val nodes = listOf(
                            Triple(Offset(x1, y1), r1, nodeColors[0] to a1),
                            Triple(Offset(x2, y2), r2, nodeColors[1] to a2),
                            Triple(Offset(x3, y3), r3, nodeColors[2] to a3),
                            Triple(Offset(x4, y4), r4, nodeColors[3] to a4)
                        )

                        val blend = BlendMode.Screen

                        for ((center, radius, colorAlpha) in nodes) {
                            val (color, alpha) = colorAlpha
                            drawCircle(
                                brush = Brush.radialGradient(
                                    0.00f to color.copy(alpha = alpha),
                                    0.28f to color.copy(alpha = alpha * 0.82f),
                                    0.52f to color.copy(alpha = alpha * 0.50f),
                                    0.74f to color.copy(alpha = alpha * 0.22f),
                                    0.90f to color.copy(alpha = alpha * 0.06f),
                                    1.00f to Color.Transparent,
                                    center = center,
                                    radius = radius
                                ),
                                radius = radius,
                                center = center,
                                blendMode = blend
                            )
                        }
                    }
                }
        ) {
            LargeTopAppBar(
                modifier = Modifier.graphicsLayer {
                    translationX = -pageOffset * size.width * 0.35f
                    alpha = (1f - pageOffset).coerceIn(0f, 1f)
                },
                title = {
                    val isSmallTitle = LocalTextStyle.current.fontSize <= MaterialTheme.typography.titleLarge.fontSize

                    val logoBoxSize = if (isSmallTitle) 28.dp else 46.dp
                    val logoCorner = if (isSmallTitle) 8.dp else 15.dp
                    val logoWidth = if (isSmallTitle) 15.dp else 25.dp
                    val logoHeight = if (isSmallTitle) 18.dp else 30.dp
                    val titleFontSize = if (isSmallTitle) 18.sp else 28.sp
                    val titleSpacing = if (isSmallTitle) 8.dp else 12.dp
                    val badgeScale = if (isSmallTitle) 0.85f else 1f

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(titleSpacing)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(logoCorner),
                                color = Color(0xFF1E1B4B),
                                modifier = Modifier
                                    .size(logoBoxSize)
                                    .onGloballyPositioned { logoCoords ->
                                        if (!isSmallTitle) {
                                            expandedLogoCoords = logoCoords
                                        }
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    AuraLogoIcon(
                                        modifier = Modifier.size(if (isSmallTitle) 20.dp else 34.dp),
                                        bookColor = MaterialTheme.colorScheme.primary,
                                        lineColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                }
                            }
                            val marcellusFamily = remember { androidx.compose.ui.text.font.FontFamily(androidx.compose.ui.text.font.Font(com.aura.reader.R.font.marcellus_regular)) }
                            Text(
                                text = strings.appName,
                                fontFamily = marcellusFamily,
                        style = if (isSmallTitle) {
                            MaterialTheme.typography.titleLarge.copy(
                                fontSize = titleFontSize,
                                letterSpacing = 0.2.sp
                            )
                        } else {
                            MaterialTheme.typography.headlineMedium.copy(
                                fontSize = titleFontSize,
                                letterSpacing = 0.6.sp
                            )
                        },
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (holidaySeason != HolidaySeason.NONE) {
                        SeasonalHeaderBadge(
                            holidaySeason = holidaySeason,
                            onClick = onHolidayBadgeClick,
                            scale = badgeScale,
                            modifier = Modifier.align(Alignment.CenterVertically).offset(y = (-1).dp)
                        )
                    }
                }

                if (!isSmallTitle) {
                    val isHoliday = holidaySeason != HolidaySeason.NONE
                    val greeting = if (isHoliday) {
                        strings.holidayGreeting(holidaySeason)
                    } else {
                        strings.greetingFor(dayPeriodForHour(rememberCurrentHour()))
                    }
                    Text(
                        text = greeting,
                        // The greeting reads as a clean secondary subtitle under the app name
                        modifier = Modifier.padding(start = logoBoxSize + titleSpacing),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            letterSpacing = 0.1.sp
                        ),
                        fontWeight = if (isHoliday) FontWeight.Medium else FontWeight.Normal,
                        color = if (isHoliday) {
                            holidaySeason.primaryAccent
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        actions = {},
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = if (isGlass) {
                if (isThemeDark) Color(0xB3000000) else Color(0xB3FFFFFF)
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
        )
    )
        }
    }
}
}

@Composable
private fun SeasonalHeaderBadge(
    holidaySeason: HolidaySeason,
    onClick: (Offset) -> Unit,
    scale: Float = 1f,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val infiniteTransition = rememberInfiniteTransition(label = "holidayBadge")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "holidayOffsetY"
    )
    val rotation by infiniteTransition.animateFloat(
        initialValue = -3.5f,
        targetValue = 3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "holidayRotation"
    )

    var badgeCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = holidaySeason.primaryAccent.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, holidaySeason.primaryAccent.copy(alpha = 0.35f)),
        modifier = modifier
            .heightIn(max = 28.dp)
            .onGloballyPositioned { badgeCoordinates = it }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                val center = badgeCoordinates?.boundsInRoot()?.center ?: Offset.Zero
                onClick(center)
            }
            .offset(y = offsetY.dp)
            .graphicsLayer {
                rotationZ = rotation
                scaleX = scale
                scaleY = scale
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = holidaySeason.iconEmoji,
                fontSize = 14.sp,
                maxLines = 1,
                modifier = Modifier.offset(y = (-1.5).dp)
            )
            Text(
                text = strings.holidayDisplayName(holidaySeason),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = holidaySeason.primaryAccent,
                fontSize = 11.sp,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
