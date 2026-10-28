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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.TopAppBarScrollBehavior

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
    onOpenSettings: () -> Unit
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(15.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        AuraLogoIcon(
                            modifier = Modifier.size(width = 25.dp, height = 30.dp),
                            bookColor = MaterialTheme.colorScheme.primary,
                            lineColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    }
                }
                val marcellusFamily = remember { androidx.compose.ui.text.font.FontFamily(androidx.compose.ui.text.font.Font(com.aura.reader.R.font.marcellus_regular)) }
                Text(
                    text = strings.appName,
                    fontFamily = marcellusFamily,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 28.sp,
                        letterSpacing = 0.6.sp
                    )
                )
            }
        },
        actions = {
            // View Mode Switcher
            IconButton(onClick = { onToggleViewMode() }) {
                Icon(
                    imageVector = if (libraryViewMode == "GRID") Icons.AutoMirrored.Rounded.ViewList else Icons.Rounded.GridView,
                    contentDescription = if (libraryViewMode == "GRID") strings.viewModeList else strings.viewModeGrid
                )
            }

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
