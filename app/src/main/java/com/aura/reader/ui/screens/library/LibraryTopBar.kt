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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.aura.reader.data.model.HolidaySeason
import com.aura.reader.data.model.LocalHolidaySeason

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
internal fun LibraryTopBar(
    isSearchExpanded: Boolean,
    isGlass: Boolean,
    isThemeDark: Boolean,
    searchQuery: String,
    libraryViewMode: String,
    scrollBehavior: TopAppBarScrollBehavior,
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
        LargeTopAppBar(
        title = {
            val holidaySeason = LocalHolidaySeason.current
            val isSmallTitle = LocalTextStyle.current.fontSize <= MaterialTheme.typography.titleLarge.fontSize

            val logoBoxSize = if (isSmallTitle) 28.dp else 46.dp
            val logoCorner = if (isSmallTitle) 8.dp else 15.dp
            val logoWidth = if (isSmallTitle) 15.dp else 25.dp
            val logoHeight = if (isSmallTitle) 18.dp else 30.dp
            val titleFontSize = if (isSmallTitle) 18.sp else 28.sp
            val titleSpacing = if (isSmallTitle) 8.dp else 12.dp
            val badgeScale = if (isSmallTitle) 0.85f else 1f

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(titleSpacing)
                ) {
                    Surface(
                        shape = RoundedCornerShape(logoCorner),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(logoBoxSize)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            AuraLogoIcon(
                                modifier = Modifier.size(width = logoWidth, height = logoHeight),
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
                            scale = badgeScale
                        )
                    }
                }

                if (!isSmallTitle && holidaySeason != HolidaySeason.NONE) {
                    Text(
                        text = holidaySeason.greetingText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = holidaySeason.primaryAccent,
                        letterSpacing = 0.2.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        actions = {
            if (!isGlass) {
                // Search Button in Material mode
                IconButton(onClick = { onSearchExpandedChange(true) }) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = strings.searchInLibrary
                    )
                }

                // Settings Button in Material mode
                IconButton(onClick = { onOpenSettings() }) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = strings.settingsTitle
                    )
                }
            }
        },
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

@Composable
private fun SeasonalHeaderBadge(
    holidaySeason: HolidaySeason,
    onClick: (Offset) -> Unit,
    scale: Float = 1f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "holidayBadge")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "holidayOffsetY"
    )
    val rotation by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
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
        modifier = Modifier
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
                maxLines = 1
            )
            Text(
                text = holidaySeason.displayName,
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
