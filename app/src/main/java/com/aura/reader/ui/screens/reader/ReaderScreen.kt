package com.aura.reader.ui.screens.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLayoutResult
import com.aura.reader.ui.theme.LocalAppStrings
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.aura.reader.data.model.BlockType
import com.aura.reader.data.model.Chapter
import com.aura.reader.data.model.FormattedBlock
import com.aura.reader.data.model.ReaderFontFamily
import com.aura.reader.data.model.ReaderSettings
import com.aura.reader.ui.theme.AuraReaderTheme
import kotlinx.coroutines.launch
import java.io.File

@OptIn(
    ExperimentalMaterial3Api::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class
)
@Composable
fun ReaderScreen(
    viewModel: ReaderViewModel,
    onNavigateBack: () -> Unit
) {
    // Active reading timer tracker
    DisposableEffect(Unit) {
        viewModel.onResumeReading()
        onDispose {
            viewModel.onPauseReading()
        }
    }

    val book by viewModel.currentBook.collectAsState()
    val settings by viewModel.readerSettings.collectAsState()
    val currentChapterIndex by viewModel.currentChapterIndex.collectAsState()
    val savedOffset by viewModel.savedScrollOffset.collectAsState()

    // Search state
    var isSearchActive by remember { mutableStateOf(false) }
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val currentSearchIndex by viewModel.currentSearchIndex.collectAsState()
    val targetScrollOffset by viewModel.targetScrollOffset.collectAsState()

    // Bookmarks and Quotes
    val bookmarks by viewModel.bookmarks.collectAsState()
    val quotes by viewModel.quotes.collectAsState()
    val isCurrentChapterBookmarked = remember(bookmarks, book?.id, currentChapterIndex) {
        book?.id?.let { bId ->
            bookmarks.any { it.bookId == bId && it.chapterIndex == currentChapterIndex }
        } ?: false
    }

    var showControls by remember { mutableStateOf(true) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showChaptersSheet by remember { mutableStateOf(false) }
    var showBookmarksQuotesSheet by remember { mutableStateOf(false) }
    var selectedFootnote by remember { mutableStateOf<Pair<String, String>?>(null) }
    var quoteToSave by remember { mutableStateOf<String?>(null) }
    val strings = LocalAppStrings.current
    var currentPagingPage by remember { mutableIntStateOf(0) }
    var totalPagingPages by remember { mutableIntStateOf(1) }
    var requestPagingPage by remember { mutableStateOf<Int?>(null) }
    var showReaderMenu by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val defaultToolbar = LocalTextToolbar.current
    val clipboardManager = LocalClipboardManager.current
    var selectedQuoteCopyAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val customTextToolbar = remember(defaultToolbar) {
        object : TextToolbar {
            override val status: TextToolbarStatus
                get() = defaultToolbar.status

            override fun hide() {
                selectedQuoteCopyAction = null
                defaultToolbar.hide()
            }

            override fun showMenu(
                rect: Rect,
                onCopyRequested: (() -> Unit)?,
                onPasteRequested: (() -> Unit)?,
                onCutRequested: (() -> Unit)?,
                onSelectAllRequested: (() -> Unit)?
            ) {
                selectedQuoteCopyAction = onCopyRequested
                defaultToolbar.showMenu(rect, onCopyRequested, onPasteRequested, onCutRequested, onSelectAllRequested)
            }
        }
    }

    AuraReaderTheme(themeMode = settings.themeMode) {
      CompositionLocalProvider(LocalTextToolbar provides customTextToolbar) {
        val resolvedFontFamily = when (settings.fontFamily) {
            ReaderFontFamily.SERIF -> FontFamily.Serif
            ReaderFontFamily.SANS_SERIF -> FontFamily.SansSerif
            ReaderFontFamily.MONOSPACE -> FontFamily.Monospace
            ReaderFontFamily.SYSTEM_DEFAULT -> FontFamily.Default
        }

        val chapters = book?.chapters ?: emptyList()
        val currentChapter = chapters.getOrNull(currentChapterIndex)
        val footnotes = book?.footnotes ?: emptyMap()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
            // Top Bar with expandVertically / shrinkVertically so text naturally shifts below it
            AnimatedVisibility(
                visible = showControls,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    shadowElevation = 4.dp
                ) {
                    if (isSearchActive) {
                        // Search in Book Top Bar
                        TopAppBar(
                            modifier = Modifier.statusBarsPadding(),
                            navigationIcon = {
                                IconButton(onClick = {
                                    isSearchActive = false
                                    viewModel.clearSearch()
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = strings.closeSearch
                                    )
                                }
                            },
                            title = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
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
                            },
                            actions = {
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
                                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = strings.previous)
                                    }
                                    IconButton(
                                        onClick = { viewModel.nextSearchResult() },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = strings.next)
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        isSearchActive = false
                                        viewModel.clearSearch()
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = strings.clear)
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                        )
                    } else {
                        // Standard Top Bar
                        TopAppBar(
                            modifier = Modifier.statusBarsPadding(),
                            title = {
                                Column {
                                    Text(
                                        text = book?.title ?: strings.appName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = currentChapter?.title ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            },
                            navigationIcon = {
                                IconButton(onClick = onNavigateBack) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = strings.back
                                    )
                                }
                            },
                            actions = {
                                // Bookmark Toggle Icon
                                IconButton(onClick = {
                                    val preview = currentChapter?.content?.take(100) ?: ""
                                    viewModel.toggleBookmark(preview)
                                }) {
                                    Icon(
                                        imageVector = if (isCurrentChapterBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = strings.bookmarks,
                                        tint = if (isCurrentChapterBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                // Three-dots Overflow Menu
                                Box {
                                    IconButton(onClick = { showReaderMenu = true }) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = strings.moreOptions
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = showReaderMenu,
                                        onDismissRequest = { showReaderMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text(strings.searchInBook) },
                                            leadingIcon = {
                                                Icon(Icons.Default.Search, contentDescription = null)
                                            },
                                            onClick = {
                                                showReaderMenu = false
                                                isSearchActive = true
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text(strings.contents) },
                                            leadingIcon = {
                                                Icon(Icons.Default.List, contentDescription = null)
                                            },
                                            onClick = {
                                                showReaderMenu = false
                                                showChaptersSheet = true
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text(strings.bookmarksAndQuotes) },
                                            leadingIcon = {
                                                Icon(Icons.Default.CollectionsBookmark, contentDescription = null)
                                            },
                                            onClick = {
                                                showReaderMenu = false
                                                showBookmarksQuotesSheet = true
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text(strings.fontAndTheme) },
                                            leadingIcon = {
                                                Icon(Icons.Default.FormatSize, contentDescription = null)
                                            },
                                            onClick = {
                                                showReaderMenu = false
                                                showSettingsSheet = true
                                            }
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent
                            )
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = !showControls,
                enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
            ) {
                Spacer(modifier = Modifier.statusBarsPadding().height(10.dp))
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                if (chapters.isNotEmpty()) {
                    if (settings.pagingMode && currentChapter != null) {
                        // --- Paging Mode (Листание страниц) ---
                        ChapterPagingView(
                            chapter = currentChapter,
                            chapterIndex = currentChapterIndex,
                            totalChapters = chapters.size,
                            settings = settings,
                            resolvedFontFamily = resolvedFontFamily,
                            searchQuery = searchQuery,
                            footnotes = footnotes,
                            targetBlockIndex = targetScrollOffset,
                            targetPage = requestPagingPage,
                            onConsumeTargetBlock = { viewModel.consumeTargetScrollOffset() },
                            onConsumeTargetPage = { requestPagingPage = null },
                            onToggleControls = { showControls = !showControls },
                            onPrevChapter = { viewModel.prevChapter() },
                            onNextChapter = { viewModel.nextChapter() },
                            onFootnoteClick = { ref, content -> selectedFootnote = ref to content },
                            onSaveQuote = { quoteToSave = it },
                            onPageChange = { page, total ->
                                currentPagingPage = page
                                totalPagingPages = total
                            },
                            onUpdateProgress = { pageIdx, totalPages ->
                                viewModel.updateScrollProgress(pageIdx, totalPages)
                            }
                        )
                    } else {
                        // --- Continuous Mode (Непрерывная лента со свайпом глав) ---
                        val pagerState = rememberPagerState(
                            initialPage = currentChapterIndex.coerceIn(0, chapters.size - 1),
                            pageCount = { chapters.size }
                        )

                        LaunchedEffect(pagerState.currentPage) {
                            if (pagerState.currentPage != currentChapterIndex) {
                                viewModel.setChapter(pagerState.currentPage)
                            }
                        }

                        LaunchedEffect(currentChapterIndex) {
                            if (pagerState.currentPage != currentChapterIndex && currentChapterIndex in chapters.indices) {
                                pagerState.animateScrollToPage(currentChapterIndex)
                            }
                        }

                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { pageIndex ->
                            val chapter = chapters.getOrNull(pageIndex)
                            if (chapter != null) {
                                ChapterContentView(
                                    chapter = chapter,
                                    isCurrentChapter = pageIndex == currentChapterIndex,
                                    initialScrollOffset = if (pageIndex == currentChapterIndex) savedOffset else 0,
                                    targetScrollOffset = if (pageIndex == currentChapterIndex) targetScrollOffset else null,
                                    onConsumeTargetScrollOffset = { viewModel.consumeTargetScrollOffset() },
                                    settings = settings,
                                    resolvedFontFamily = resolvedFontFamily,
                                    pageIndex = pageIndex,
                                    chaptersCount = chapters.size,
                                    searchQuery = searchQuery,
                                    footnotes = footnotes,
                                    onToggleControls = { showControls = !showControls },
                                    onFootnoteClick = { ref, content -> selectedFootnote = ref to content },
                                    onSaveQuote = { quoteToSave = it },
                                    onPrevChapter = {
                                        if (pageIndex > 0) {
                                            coroutineScope.launch { pagerState.animateScrollToPage(pageIndex - 1) }
                                        }
                                    },
                                    onNextChapter = {
                                        if (pageIndex < chapters.size - 1) {
                                            coroutineScope.launch { pagerState.animateScrollToPage(pageIndex + 1) }
                                        }
                                    },
                                    onUpdateProgress = { firstVisible, totalItems ->
                                        if (pageIndex == currentChapterIndex) {
                                            viewModel.updateScrollProgress(firstVisible, totalItems)
                                        }
                                    }
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = strings.loadingBook,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }

        // Bottom Controls Bar with Material 3 Expressive Floating Card
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.98f),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
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
                                            Icon(Icons.Default.SkipPrevious, contentDescription = strings.prevChapter)
                                        }

                                        Column(
                                            modifier = Modifier.weight(1f),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "${strings.chapter} ${currentChapterIndex + 1} ${strings.ofChapters} ${chapters.size}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = currentChapter?.title ?: "",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        IconButton(
                                            onClick = { viewModel.nextChapter() },
                                            enabled = currentChapterIndex < chapters.size - 1,
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(Icons.Default.SkipNext, contentDescription = strings.nextChapter)
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
                                                    requestPagingPage = currentPagingPage - 1
                                                }
                                            },
                                            enabled = currentPagingPage > 0,
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(Icons.Default.ChevronLeft, contentDescription = strings.prevPage)
                                        }

                                        Slider(
                                            value = currentPagingPage.toFloat().coerceIn(0f, (totalPagingPages - 1).coerceAtLeast(0).toFloat()),
                                            onValueChange = { requestPagingPage = it.toInt() },
                                            valueRange = 0f..(totalPagingPages - 1).coerceAtLeast(1).toFloat(),
                                            steps = (totalPagingPages - 2).coerceAtLeast(0),
                                            modifier = Modifier.weight(1f),
                                            colors = SliderDefaults.colors(
                                                thumbColor = MaterialTheme.colorScheme.primary,
                                                activeTrackColor = MaterialTheme.colorScheme.primary
                                            )
                                        )

                                        IconButton(
                                            onClick = {
                                                if (currentPagingPage < totalPagingPages - 1) {
                                                    requestPagingPage = currentPagingPage + 1
                                                }
                                            },
                                            enabled = currentPagingPage < totalPagingPages - 1,
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(Icons.Default.ChevronRight, contentDescription = strings.nextPage)
                                        }
                                    }

                                    Text(
                                        text = "${strings.page} ${currentPagingPage + 1} ${strings.ofPages} $totalPagingPages  •  ${strings.pagingHint}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                } else {
                                    // --- Continuous Mode: Chapter Slider ---
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AutoStories,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "${strings.chapter} ${currentChapterIndex + 1} ${strings.ofChapters} ${chapters.size}: ${currentChapter?.title ?: ""}",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        val progress = (((currentChapterIndex + 1).toFloat() / chapters.size) * 100).toInt()
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
                                            Icon(Icons.Default.ChevronLeft, contentDescription = strings.prevChapter)
                                        }

                                        Slider(
                                            value = currentChapterIndex.toFloat(),
                                            onValueChange = { viewModel.setChapter(it.toInt()) },
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
                                            Icon(Icons.Default.ChevronRight, contentDescription = strings.nextChapter)
                                        }
                                    }

                                    Text(
                                        text = strings.chaptersNavigationHint,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                }
                            }
                        }
                    }
                }

            // Bottom Sheets
            if (showSettingsSheet) {
                ReaderSettingsBottomSheet(
                    settings = settings,
                    onDismiss = { showSettingsSheet = false },
                    onFontSizeChange = { viewModel.setFontSize(it) },
                    onLineHeightChange = { viewModel.setLineHeight(it) },
                    onThemeModeChange = { viewModel.setThemeMode(it) },
                    onFontFamilyChange = { viewModel.setFontFamily(it) },
                    onLightImageBackgroundChange = { viewModel.setLightImageBackground(it) },
                    onPagingModeChange = { viewModel.setPagingMode(it) }
                )
            }

            if (showChaptersSheet) {
                ChaptersBottomSheet(
                    chapters = chapters,
                    currentChapterIndex = currentChapterIndex,
                    onChapterSelected = { viewModel.setChapter(it) },
                    onDismiss = { showChaptersSheet = false }
                )
            }

            if (showBookmarksQuotesSheet && book != null) {
                BookmarksAndQuotesBottomSheet(
                    bookmarks = bookmarks,
                    quotes = quotes,
                    currentBookId = book!!.id,
                    onDismiss = { showBookmarksQuotesSheet = false },
                    onBookmarkClick = { chIdx, offset ->
                        viewModel.setChapter(chIdx)
                    },
                    onDeleteBookmark = { viewModel.removeBookmark(it) },
                    onAddQuote = { viewModel.addQuote(it) },
                    onDeleteQuote = { viewModel.removeQuote(it) }
                )
            }

            selectedFootnote?.let { (ref, content) ->
                FootnoteBottomSheet(
                    refLabel = ref,
                    content = content,
                    onDismiss = { selectedFootnote = null }
                )
            }

            // Floating pill to save selected text as quote
            AnimatedVisibility(
                visible = selectedQuoteCopyAction != null,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (showControls) 130.dp else 48.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shadowElevation = 8.dp,
                    tonalElevation = 6.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .clickable {
                                val action = selectedQuoteCopyAction
                                action?.invoke()
                                val clip = clipboardManager.getText()?.text
                                if (!clip.isNullOrBlank()) {
                                    quoteToSave = clip.trim()
                                }
                                customTextToolbar.hide()
                            }
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatQuote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = strings.saveQuoteAction,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            quoteToSave?.let { quoteText ->
                SaveQuoteBottomSheet(
                    initialText = quoteText,
                    bookTitle = book?.title ?: "",
                    chapterTitle = currentChapter?.title ?: "",
                    onDismiss = { quoteToSave = null },
                    onSaveQuote = {
                        viewModel.addQuote(it)
                        quoteToSave = null
                    }
                )
            }
        }
      }
    }
}

// ------------------------------------------------------------------------------------------------
// CONTINUOUS SCROLL VIEW
// ------------------------------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterContentView(
    chapter: Chapter,
    isCurrentChapter: Boolean,
    initialScrollOffset: Int,
    targetScrollOffset: Int?,
    onConsumeTargetScrollOffset: () -> Unit,
    settings: ReaderSettings,
    resolvedFontFamily: FontFamily,
    pageIndex: Int,
    chaptersCount: Int,
    searchQuery: String,
    footnotes: Map<String, String>,
    onToggleControls: () -> Unit,
    onFootnoteClick: (ref: String, content: String) -> Unit,
    onSaveQuote: (String) -> Unit,
    onPrevChapter: () -> Unit,
    onNextChapter: () -> Unit,
    onUpdateProgress: (Int, Int) -> Unit
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialScrollOffset)

    if (isCurrentChapter) {
        val firstVisibleIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
        LaunchedEffect(firstVisibleIndex) {
            val totalItems = listState.layoutInfo.totalItemsCount
            if (totalItems > 0) {
                onUpdateProgress(firstVisibleIndex, totalItems)
            }
        }

        LaunchedEffect(targetScrollOffset) {
            if (targetScrollOffset != null) {
                listState.animateScrollToItem(targetScrollOffset.coerceAtLeast(0))
                onConsumeTargetScrollOffset()
            }
        }
    }

    val blocks = remember(chapter) {
        if (chapter.blocks.isNotEmpty()) {
            chapter.blocks
        } else {
            chapter.content.split("\n\n")
                .filter { it.isNotBlank() }
                .map { FormattedBlock(BlockType.PARAGRAPH, it.trim()) }
        }
    }

    val strings = LocalAppStrings.current
    SelectionContainer {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onToggleControls()
                },
            contentPadding = PaddingValues(
                start = 22.dp,
                end = 22.dp,
                top = 28.dp,
                bottom = 140.dp
            )
        ) {
            items(blocks) { block ->
                RenderBlock(
                    block = block,
                    settings = settings,
                    resolvedFontFamily = resolvedFontFamily,
                    searchQuery = searchQuery,
                    footnotes = footnotes,
                    onFootnoteClick = onFootnoteClick,
                    onToggleControls = onToggleControls,
                    onSaveQuote = onSaveQuote
                )
            }

            // Symmetrical Prev/Next buttons at the bottom of chapter
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onPrevChapter,
                        enabled = pageIndex > 0,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            Icons.Default.ChevronLeft,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.previous, maxLines = 1)
                    }

                    FilledTonalButton(
                        onClick = onNextChapter,
                        enabled = pageIndex < chaptersCount - 1,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(strings.next, maxLines = 1)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// PAGING MODE VIEW
// ------------------------------------------------------------------------------------------------

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ChapterPagingView(
    chapter: Chapter,
    chapterIndex: Int,
    totalChapters: Int,
    settings: ReaderSettings,
    resolvedFontFamily: FontFamily,
    searchQuery: String,
    footnotes: Map<String, String>,
    targetBlockIndex: Int?,
    targetPage: Int?,
    onConsumeTargetBlock: () -> Unit,
    onConsumeTargetPage: () -> Unit,
    onToggleControls: () -> Unit,
    onPrevChapter: () -> Unit,
    onNextChapter: () -> Unit,
    onFootnoteClick: (ref: String, content: String) -> Unit,
    onSaveQuote: (String) -> Unit,
    onPageChange: (Int, Int) -> Unit,
    onUpdateProgress: (Int, Int) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenHeightDp = maxHeight.value
        val screenWidthDp = maxWidth.value

        val blocks = remember(chapter) {
            if (chapter.blocks.isNotEmpty()) {
                chapter.blocks
            } else {
                chapter.content.split("\n\n")
                    .filter { it.isNotBlank() }
                    .map { FormattedBlock(BlockType.PARAGRAPH, it.trim()) }
            }
        }

        val pages = remember(blocks, settings.fontSizeSp, settings.lineHeightMultiplier, screenHeightDp, screenWidthDp) {
            paginateBlocks(blocks, settings.fontSizeSp, settings.lineHeightMultiplier, screenHeightDp, screenWidthDp)
        }

        val pagerState = rememberPagerState(
            initialPage = 0,
            pageCount = { pages.size.coerceAtLeast(1) }
        )
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(chapterIndex) {
            pagerState.scrollToPage(0)
        }

        LaunchedEffect(pagerState.currentPage, pages.size) {
            onPageChange(pagerState.currentPage, pages.size)
            onUpdateProgress(pagerState.currentPage, pages.size.coerceAtLeast(1))
        }

        // React to requested page from page slider
        LaunchedEffect(targetPage) {
            if (targetPage != null) {
                if (targetPage in pages.indices && targetPage != pagerState.currentPage) {
                    pagerState.scrollToPage(targetPage)
                }
                onConsumeTargetPage()
            }
        }

        // Jump to search match page if target is given
        LaunchedEffect(targetBlockIndex) {
            if (targetBlockIndex != null) {
                val foundPage = pages.indexOfFirst { page ->
                    page.any { it.first == targetBlockIndex }
                }
                if (foundPage >= 0) {
                    pagerState.animateScrollToPage(foundPage)
                }
                onConsumeTargetBlock()
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIdx ->
            val pageBlocks = pages.getOrNull(pageIdx) ?: emptyList()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onToggleControls() }
            ) {
                SelectionContainer {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp, vertical = 4.dp)
                            .padding(bottom = 32.dp),
                        verticalArrangement = Arrangement.Top
                    ) {
                        for ((_, block) in pageBlocks) {
                            RenderBlock(
                                block = block,
                                settings = settings,
                                resolvedFontFamily = resolvedFontFamily,
                                searchQuery = searchQuery,
                                footnotes = footnotes,
                                onFootnoteClick = onFootnoteClick,
                                onToggleControls = onToggleControls,
                                onSaveQuote = onSaveQuote
                            )
                        }
                    }
                }
            }
        }

        // Thin margin tap zones for fast page flipping (outer padding area only)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(28.dp)
                .align(Alignment.CenterStart)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (pagerState.currentPage > 0) {
                        coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                    } else {
                        onPrevChapter()
                    }
                }
        )

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(28.dp)
                .align(Alignment.CenterEnd)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (pagerState.currentPage < pages.size - 1) {
                        coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    } else {
                        onNextChapter()
                    }
                }
        )

        // Bottom Page Counter
        val strings = LocalAppStrings.current
        Text(
            text = "${strings.page} ${pagerState.currentPage + 1} ${strings.ofPages} ${pages.size}  •  ${strings.chapter} ${chapterIndex + 1} ${strings.ofChapters} $totalChapters",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )
    }
}

// ------------------------------------------------------------------------------------------------
// PAGINATION HELPER
// ------------------------------------------------------------------------------------------------

private fun calculatePageCapacity(
    fontSizeSp: Float,
    lineHeightMultiplier: Float,
    screenHeightDp: Float = 800f,
    screenWidthDp: Float = 380f
): Int {
    val fs = fontSizeSp.coerceIn(12f, 36f)
    val lh = lineHeightMultiplier.coerceIn(1.0f, 2.2f)
    val effectiveLineHeight = fs * lh
    val usableHeightDp = (screenHeightDp - 54f).coerceAtLeast(350f)
    val linesPerPage = (usableHeightDp / effectiveLineHeight).coerceIn(12f, 50f)
    val usableWidthDp = (screenWidthDp - 40f).coerceAtLeast(260f)
    val charsPerLine = (usableWidthDp / (fs * 0.46f)).coerceIn(24f, 65f)
    return (linesPerPage * charsPerLine).toInt().coerceIn(600, 3500)
}

private fun findBestBreak(text: String, targetLen: Int): Int {
    if (text.length <= targetLen) return text.length
    // Look for sentence endings strictly within bottom 12% [targetLen * 0.88 .. targetLen]
    // so pages are evenly and tightly filled
    val minSearchSentence = (targetLen * 0.88f).toInt().coerceAtLeast(0)
    val window = text.substring(0, targetLen.coerceAtMost(text.length))

    for (del in listOf(". ", "! ", "? ", ".\n", "!\n", "?\n")) {
        val idx = window.lastIndexOf(del)
        if (idx >= minSearchSentence) {
            return idx + del.length
        }
    }
    for (del in listOf("; ", ": ", ", ")) {
        val idx = window.lastIndexOf(del)
        if (idx >= (targetLen * 0.90f).toInt()) {
            return idx + del.length
        }
    }
    val spaceIdx = window.lastIndexOf(' ')
    if (spaceIdx >= (targetLen * 0.92f).toInt()) {
        return spaceIdx + 1
    }
    return if (spaceIdx > 0) spaceIdx + 1 else targetLen
}

private fun paginateBlocks(
    blocks: List<FormattedBlock>,
    fontSizeSp: Float,
    lineHeightMultiplier: Float,
    screenHeightDp: Float = 800f,
    screenWidthDp: Float = 380f
): List<List<Pair<Int, FormattedBlock>>> {
    if (blocks.isEmpty()) return listOf(emptyList())

    val targetChars = calculatePageCapacity(fontSizeSp, lineHeightMultiplier, screenHeightDp, screenWidthDp)
    val pages = mutableListOf<List<Pair<Int, FormattedBlock>>>()
    var currentPage = mutableListOf<Pair<Int, FormattedBlock>>()
    var currentChars = 0

    fun flushPage() {
        if (currentPage.isNotEmpty()) {
            pages.add(ArrayList(currentPage))
            currentPage.clear()
            currentChars = 0
        }
    }

    for ((originalIndex, block) in blocks.withIndex()) {
        when (block.type) {
            BlockType.IMAGE -> {
                flushPage()
                pages.add(listOf(originalIndex to block))
            }
            BlockType.TITLE -> {
                if (currentChars > targetChars * 0.5f) {
                    flushPage()
                }
                currentPage.add(originalIndex to block)
                currentChars += (block.text.length + (targetChars * 0.07f).toInt())
            }
            BlockType.SUBTITLE -> {
                if (currentChars > targetChars * 0.6f) {
                    flushPage()
                }
                currentPage.add(originalIndex to block)
                currentChars += (block.text.length + (targetChars * 0.05f).toInt())
            }
            BlockType.DIVIDER -> {
                if (currentChars + 30 > targetChars) {
                    flushPage()
                } else {
                    currentPage.add(originalIndex to block)
                    currentChars += 30
                }
            }
            BlockType.EPIGRAPH, BlockType.VERSE -> {
                val weight = block.text.length + 30
                if (currentChars + weight > targetChars && currentPage.isNotEmpty()) {
                    flushPage()
                }
                currentPage.add(originalIndex to block)
                currentChars += weight
            }
            BlockType.PARAGRAPH -> {
                var remainingText = block.text.trim()
                var loopGuard = 0
                while (remainingText.isNotEmpty() && loopGuard++ < 1000) {
                    val availableChars = targetChars - currentChars
                    if (availableChars < 120 && currentPage.isNotEmpty()) {
                        flushPage()
                        continue
                    }

                    val effectiveAvailable = availableChars.coerceAtLeast(120)
                    if (remainingText.length <= effectiveAvailable) {
                        currentPage.add(originalIndex to block.copy(text = remainingText))
                        currentChars += remainingText.length + 20
                        remainingText = ""
                    } else {
                        val splitIndex = findBestBreak(remainingText, effectiveAvailable).coerceIn(1, remainingText.length)
                        val chunk = remainingText.substring(0, splitIndex).trim()
                        if (chunk.isNotEmpty()) {
                            currentPage.add(originalIndex to block.copy(text = chunk))
                        }
                        flushPage()
                        remainingText = remainingText.substring(splitIndex).trim()
                    }
                }
            }
        }
    }

    flushPage()
    return if (pages.isEmpty()) listOf(emptyList()) else pages
}

// ------------------------------------------------------------------------------------------------
// BLOCK RENDERER WITH SEARCH HIGHLIGHTING & FOOTNOTES
// ------------------------------------------------------------------------------------------------

@Composable
fun RenderBlock(
    block: FormattedBlock,
    settings: ReaderSettings,
    resolvedFontFamily: FontFamily,
    searchQuery: String,
    footnotes: Map<String, String>,
    onFootnoteClick: (ref: String, content: String) -> Unit,
    onToggleControls: () -> Unit,
    onSaveQuote: (String) -> Unit
) {
    when (block.type) {
        BlockType.TITLE -> {
            Text(
                text = block.text,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = resolvedFontFamily,
                    textAlign = TextAlign.Center
                ),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 12.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onToggleControls() }
            )
        }
        BlockType.SUBTITLE -> {
            Text(
                text = block.text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = resolvedFontFamily,
                    textAlign = TextAlign.Center
                ),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onToggleControls() }
            )
        }
        BlockType.EPIGRAPH -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 36.dp, end = 8.dp, top = 4.dp, bottom = 12.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = block.text,
                    fontSize = (settings.fontSizeSp * 0.92f).sp,
                    lineHeight = (settings.fontSizeSp * settings.lineHeightMultiplier).sp,
                    fontFamily = resolvedFontFamily,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.End,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f)
                )
                if (!block.subText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = block.subText,
                        fontSize = (settings.fontSizeSp * 0.82f).sp,
                        fontFamily = resolvedFontFamily,
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
                    )
                }
            }
        }
        BlockType.VERSE -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 28.dp, end = 16.dp, top = 6.dp, bottom = 12.dp)
            ) {
                Text(
                    text = block.text,
                    fontSize = (settings.fontSizeSp * 0.95f).sp,
                    lineHeight = (settings.fontSizeSp * settings.lineHeightMultiplier * 0.95f).sp,
                    fontFamily = resolvedFontFamily,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
        BlockType.DIVIDER -> {
            Spacer(modifier = Modifier.height(16.dp))
        }
        BlockType.IMAGE -> {
            val isDark = settings.themeMode == com.aura.reader.data.model.ReaderThemeMode.AMOLED ||
                    settings.themeMode == com.aura.reader.data.model.ReaderThemeMode.SYSTEM_DYNAMIC
            val shouldApplyLightCard = settings.lightImageBackground && isDark
            val imageCardBg = if (shouldApplyLightCard) {
                Color(0xFFF5F4F0)
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onToggleControls() },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = imageCardBg
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (shouldApplyLightCard) 3.dp else 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(if (shouldApplyLightCard) 12.dp else 0.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(File(block.text))
                                .crossfade(true)
                                .build(),
                            contentDescription = block.subText,
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(if (shouldApplyLightCard) 8.dp else 16.dp))
                        )
                    }
                }
                if (!block.subText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = block.subText,
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
        BlockType.PARAGRAPH -> {
            val paragraphStyle = TextStyle(
                fontSize = settings.fontSizeSp.sp,
                lineHeight = (settings.fontSizeSp * settings.lineHeightMultiplier).sp,
                fontFamily = resolvedFontFamily,
                color = MaterialTheme.colorScheme.onBackground,
                textIndent = TextIndent(firstLine = (settings.fontSizeSp * 1.2f).sp)
            )

            InteractiveText(
                rawText = block.text,
                style = paragraphStyle,
                searchQuery = searchQuery,
                footnotes = footnotes,
                onFootnoteClick = onFootnoteClick,
                onToggleControls = onToggleControls,
                onSaveQuote = onSaveQuote,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

// ------------------------------------------------------------------------------------------------
// INTERACTIVE TEXT (FOOTNOTES & SEARCH HIGHLIGHT)
// ------------------------------------------------------------------------------------------------

@Composable
fun InteractiveText(
    rawText: String,
    style: TextStyle,
    searchQuery: String,
    footnotes: Map<String, String>,
    onFootnoteClick: (ref: String, content: String) -> Unit,
    onToggleControls: () -> Unit,
    onSaveQuote: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val annotated = remember(rawText, searchQuery, footnotes) {
        buildAnnotatedString {
            append(rawText)

            // Highlight in-book search results
            val q = searchQuery.trim()
            if (q.length >= 2) {
                var start = 0
                while (start < rawText.length) {
                    val idx = rawText.indexOf(q, start, ignoreCase = true)
                    if (idx == -1) break
                    addStyle(
                        style = SpanStyle(
                            background = Color(0xFFFFD54F),
                            color = Color(0xFF1A1A1A),
                            fontWeight = FontWeight.Bold
                        ),
                        start = idx,
                        end = idx + q.length
                    )
                    start = idx + q.length
                }
            }

            // Highlight footnote links e.g. [1], [note], {1}
            val footnoteRegex = Regex("""\[([a-zA-Z0-9а-яА-ЯёЁ_\s-]{1,20})\]|\{([0-9]+)\}""")
            for (match in footnoteRegex.findAll(rawText)) {
                val ref = match.value
                val resolved = resolveFootnoteText(ref, footnotes)
                if (resolved != null || footnotes.isNotEmpty()) {
                    addStyle(
                        style = SpanStyle(
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold
                        ),
                        start = match.range.first,
                        end = match.range.last + 1
                    )
                    val link = LinkAnnotation.Clickable(
                        tag = ref,
                        linkInteractionListener = {
                            val content = resolveFootnoteText(ref, footnotes) ?: footnotes[ref] ?: "${strings.footnoteTitle}: $ref"
                            onFootnoteClick(ref, content)
                        }
                    )
                    addLink(link, match.range.first, match.range.last + 1)
                }
            }
        }
    }

    Text(
        text = annotated,
        style = style,
        modifier = modifier
    )
}

fun resolveFootnoteText(ref: String, footnotes: Map<String, String>): String? {
    val clean = ref.trim().removePrefix("[").removeSuffix("]").removePrefix("{").removeSuffix("}").trim()
    val direct = footnotes[ref]
        ?: footnotes[clean]
        ?: footnotes["#$clean"]
        ?: footnotes["n$clean"]
        ?: footnotes["note$clean"]
        ?: footnotes["[$clean]"]

    if (direct != null && footnotes.containsKey(direct) && footnotes[direct] != direct) {
        return footnotes[direct]
    }
    if (direct != null) return direct

    return footnotes.entries.firstOrNull {
        it.key.equals(ref, ignoreCase = true) ||
        it.key.equals(clean, ignoreCase = true) ||
        it.key.contains(clean, ignoreCase = true)
    }?.value
}
