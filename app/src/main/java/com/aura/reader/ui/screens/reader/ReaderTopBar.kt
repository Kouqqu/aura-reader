package com.aura.reader.ui.screens.reader

import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.ExperimentalSharedTransitionApi
import com.aura.reader.data.model.Book
import com.aura.reader.util.BookShareUtils
import androidx.compose.material.icons.rounded.Share
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import com.aura.reader.ui.components.LiquidGlassSurface
import com.aura.reader.ui.components.liquidGlassMenuAppearance
import androidx.compose.material3.DropdownMenuItem
import com.aura.reader.ui.theme.LocalAppStrings
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CollectionsBookmark
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aura.reader.data.model.Chapter
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)
@Composable
fun BoxScope.ReaderTopBar(
    visible: Boolean,
    book: Book?,
    currentChapter: Chapter?,
    searchQuery: String,
    searchResults: List<ReaderViewModel.SearchMatch>,
    currentSearchIndex: Int,
    isCurrentChapterBookmarked: Boolean,
    ttsState: com.aura.reader.service.TtsState,
    isGlass: Boolean,
    isThemeDark: Boolean,
    isSepiaTheme: Boolean,
    viewModel: ReaderViewModel,
    onNavigateBack: () -> Unit,
    onOpenBookInfo: () -> Unit,
    onOpenTtsSheet: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    var isSearchActive by remember { mutableStateOf(false) }
    var showReaderMenu by remember { mutableStateOf(false) }

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            val topBarContent: @Composable () -> Unit = {
                if (isSearchActive) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            isSearchActive = false
                            viewModel.clearSearch()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = strings.closeSearch
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = strings.searchInBookHint,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.performSearch(it) },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (searchResults.isNotEmpty()) {
                            Text(
                                text = "${currentSearchIndex + 1}/${searchResults.size}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            IconButton(
                                onClick = { viewModel.prevSearchResult() },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Rounded.KeyboardArrowUp, contentDescription = strings.previous)
                            }
                            IconButton(
                                onClick = { viewModel.nextSearchResult() },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = strings.next)
                            }
                        }

                        IconButton(
                            onClick = {
                                isSearchActive = false
                                viewModel.clearSearch()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Rounded.Close, contentDescription = strings.clear)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            viewModel.saveCurrentProgress()
                            onNavigateBack()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = strings.back
                            )
                        }

                        val currentBook = book
                        val topBarCoverBitmap = remember(currentBook?.coverBase64) {
                            currentBook?.coverBase64?.let { base64 ->
                                try {
                                    val bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
                                    android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                                } catch (e: Exception) {
                                    null
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onOpenBookInfo() }
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(width = 28.dp, height = 40.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                if (topBarCoverBitmap != null) {
                                    Image(
                                        bitmap = topBarCoverBitmap,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.AutoStories,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp, end = 4.dp)
                            ) {
                                Text(
                                    text = book?.title ?: strings.appName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isGlass) (if (isThemeDark) Color.White else Color(0xFF191B1F)) else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = currentChapter?.title ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isGlass) (if (isThemeDark) Color.White.copy(alpha = 0.70f) else Color.Black.copy(alpha = 0.65f)) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Audio Narration (TTS) - only shown when narration is active
                        if (ttsState.isServiceRunning) {
                            IconButton(onClick = {
                                onOpenTtsSheet()
                            }) {
                                Icon(
                                    imageVector = if (ttsState.isPlaying) Icons.AutoMirrored.Rounded.VolumeUp else Icons.Rounded.Headphones,
                                    contentDescription = strings.ttsListenAction,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Bookmark Toggle Icon
                        IconButton(onClick = {
                            val preview = currentChapter?.content?.take(100) ?: ""
                            viewModel.toggleBookmark(preview)
                        }) {
                            Icon(
                                imageVector = if (isCurrentChapterBookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                                contentDescription = strings.bookmarks,
                                tint = if (isCurrentChapterBookmarked) MaterialTheme.colorScheme.primary else if (isGlass) (if (isThemeDark) Color.White else Color(0xFF191B1F)) else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Three-dots Overflow Menu
                        Box {
                            IconButton(onClick = { showReaderMenu = true }) {
                                Icon(
                                    imageVector = Icons.Rounded.MoreVert,
                                    contentDescription = strings.moreOptions,
                                    tint = if (isGlass) (if (isThemeDark) Color.White else Color(0xFF191B1F)) else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            val readerMenuAppearance = liquidGlassMenuAppearance(isGlass, isThemeDark)
                            DropdownMenu(
                                expanded = showReaderMenu,
                                onDismissRequest = { showReaderMenu = false },
                                shape = RoundedCornerShape(20.dp),
                                containerColor = readerMenuAppearance.containerColor,
                                border = readerMenuAppearance.border,
                                shadowElevation = readerMenuAppearance.shadowElevation
                            ) {
                                DropdownMenuItem(
                                    text = { Text(strings.ttsListenAction) },
                                    leadingIcon = {
                                        Icon(Icons.Rounded.Headphones, contentDescription = null)
                                    },
                                    onClick = {
                                        showReaderMenu = false
                                        if (!ttsState.isServiceRunning) {
                                            viewModel.startTts(context)
                                        }
                                        onOpenTtsSheet()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(strings.searchInBook) },
                                    leadingIcon = {
                                        Icon(Icons.Rounded.Search, contentDescription = null)
                                    },
                                    onClick = {
                                        showReaderMenu = false
                                        isSearchActive = true
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text(strings.bookmarksAndQuotes) },
                                    leadingIcon = {
                                        Icon(Icons.Rounded.CollectionsBookmark, contentDescription = null)
                                    },
                                    onClick = {
                                        showReaderMenu = false
                                        onOpenBookmarks()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(strings.fontAndTheme) },
                                    leadingIcon = {
                                        Icon(Icons.Rounded.FormatSize, contentDescription = null)
                                    },
                                    onClick = {
                                        showReaderMenu = false
                                        onOpenSettings()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (isGlass) {
                val topBarBg = if (isSepiaTheme) Color(0xF4FAF0E6) else null
                LiquidGlassSurface(
                    shape = RoundedCornerShape(24.dp),
                    shadowElevation = 10.dp,
                    isDark = isThemeDark,
                    tintColor = topBarBg,
                    modifier = Modifier.fillMaxWidth(),
                    content = topBarContent
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
                    content = { topBarContent() }
                )
            }
        }
}
