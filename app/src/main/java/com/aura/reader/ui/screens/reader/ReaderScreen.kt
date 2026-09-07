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
    val coroutineScope = rememberCoroutineScope()

    AuraReaderTheme(themeMode = settings.themeMode) {
        val resolvedFontFamily = when (settings.fontFamily) {
            ReaderFontFamily.SERIF -> FontFamily.Serif
            ReaderFontFamily.SANS_SERIF -> FontFamily.SansSerif
            ReaderFontFamily.MONOSPACE -> FontFamily.Monospace
            ReaderFontFamily.SYSTEM_DEFAULT -> FontFamily.Default
        }

        val chapters = book?.chapters ?: emptyList()
        val currentChapter = chapters.getOrNull(currentChapterIndex)
        val footnotes = book?.footnotes ?: emptyMap()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
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
                                        contentDescription = "Закрыть поиск"
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
                                            text = "Поиск по тексту...",
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
                                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Предыдущее")
                                    }
                                    IconButton(
                                        onClick = { viewModel.nextSearchResult() },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Следующее")
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        isSearchActive = false
                                        viewModel.clearSearch()
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Очистить")
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
                                        text = book?.title ?: "Читалка",
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
                                        contentDescription = "Назад"
                                    )
                                }
                            },
                            actions = {
                                // Search Icon
                                IconButton(onClick = { isSearchActive = true }) {
                                    Icon(Icons.Default.Search, contentDescription = "Поиск в книге")
                                }
                                // Bookmark Toggle Icon
                                IconButton(onClick = {
                                    val preview = currentChapter?.content?.take(100) ?: ""
                                    viewModel.toggleBookmark(preview)
                                }) {
                                    Icon(
                                        imageVector = if (isCurrentChapterBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = "Закладка",
                                        tint = if (isCurrentChapterBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                // Bookmarks and Quotes List
                                IconButton(onClick = { showBookmarksQuotesSheet = true }) {
                                    Icon(Icons.Default.CollectionsBookmark, contentDescription = "Закладки и цитаты")
                                }
                                // Table of Contents
                                IconButton(onClick = { showChaptersSheet = true }) {
                                    Icon(Icons.Default.List, contentDescription = "Оглавление")
                                }
                                // Reader Settings
                                IconButton(onClick = { showSettingsSheet = true }) {
                                    Icon(Icons.Default.FormatSize, contentDescription = "Настройки")
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent
                            )
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
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
                            onConsumeTargetBlock = { viewModel.consumeTargetScrollOffset() },
                            onToggleControls = { showControls = !showControls },
                            onPrevChapter = { viewModel.prevChapter() },
                            onNextChapter = { viewModel.nextChapter() },
                            onFootnoteClick = { ref, content -> selectedFootnote = ref to content },
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
                            text = "Загрузка книги...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                // Bottom Controls Bar with Material 3 Expressive Floating Card
                androidx.compose.animation.AnimatedVisibility(
                    visible = showControls,
                    enter = fadeIn() + slideInVertically { it },
                    exit = fadeOut() + slideOutVertically { it },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.98f)
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
                                            text = "Глава ${currentChapterIndex + 1} из ${chapters.size}: ${currentChapter?.title ?: ""}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
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
                                        Icon(Icons.Default.ChevronLeft, contentDescription = "Предыдущая глава")
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
                                        Icon(Icons.Default.ChevronRight, contentDescription = "Следующая глава")
                                    }
                                }

                                Text(
                                    text = if (settings.pagingMode) "Листание по страницам тапом по краям экрана" else "Перемещение по главам (или свайп влево/вправо)",
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
    onPrevChapter: () -> Unit,
    onNextChapter: () -> Unit,
    onUpdateProgress: (Int, Int) -> Unit
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialScrollOffset)

    if (isCurrentChapter) {
        val firstVisibleIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
        val firstVisibleOffset by remember { derivedStateOf { listState.firstVisibleItemScrollOffset } }
        LaunchedEffect(firstVisibleIndex, firstVisibleOffset) {
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
                onToggleControls = onToggleControls
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
                    Text("Предыдущая", maxLines = 1)
                }

                FilledTonalButton(
                    onClick = onNextChapter,
                    enabled = pageIndex < chaptersCount - 1,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Следующая", maxLines = 1)
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
    onConsumeTargetBlock: () -> Unit,
    onToggleControls: () -> Unit,
    onPrevChapter: () -> Unit,
    onNextChapter: () -> Unit,
    onFootnoteClick: (ref: String, content: String) -> Unit,
    onUpdateProgress: (Int, Int) -> Unit
) {
    val blocks = remember(chapter) {
        if (chapter.blocks.isNotEmpty()) {
            chapter.blocks
        } else {
            chapter.content.split("\n\n")
                .filter { it.isNotBlank() }
                .map { FormattedBlock(BlockType.PARAGRAPH, it.trim()) }
        }
    }

    // Paginate blocks based on font size
    val pages = remember(blocks, settings.fontSizeSp) {
        paginateBlocks(blocks, settings.fontSizeSp)
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { pages.size.coerceAtLeast(1) }
    )
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(chapterIndex) {
        pagerState.scrollToPage(0)
    }

    LaunchedEffect(pagerState.currentPage) {
        onUpdateProgress(pagerState.currentPage, pages.size.coerceAtLeast(1))
    }

    // Jump to search match page if target is given
    LaunchedEffect(targetBlockIndex) {
        if (targetBlockIndex != null) {
            val targetPage = pages.indexOfFirst { page ->
                page.any { it.first == targetBlockIndex }
            }
            if (targetPage >= 0) {
                pagerState.animateScrollToPage(targetPage)
            }
            onConsumeTargetBlock()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIdx ->
            val pageBlocks = pages.getOrNull(pageIdx) ?: emptyList()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 22.dp, vertical = 20.dp)
                    .padding(bottom = 40.dp),
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
                        onToggleControls = onToggleControls
                    )
                }
            }
        }

        // Tap Navigation Overlays: Left 25% = Prev, Right 25% = Next, Center 50% = Controls
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.25f)
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
                    .weight(0.5f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onToggleControls()
                    }
            )

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.25f)
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
        }

        // Bottom Page Counter
        Text(
            text = "Стр. ${pagerState.currentPage + 1} из ${pages.size}  •  Гл. ${chapterIndex + 1} из $totalChapters",
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

private fun paginateBlocks(
    blocks: List<FormattedBlock>,
    fontSizeSp: Float
): List<List<Pair<Int, FormattedBlock>>> {
    if (blocks.isEmpty()) return listOf(emptyList())

    // Target characters per page (scaled with font size)
    val targetChars = (14000 / fontSizeSp.coerceIn(12f, 32f)).toInt().coerceIn(400, 1200)
    val pages = mutableListOf<List<Pair<Int, FormattedBlock>>>()
    var currentPage = mutableListOf<Pair<Int, FormattedBlock>>()
    var currentChars = 0

    for ((index, block) in blocks.withIndex()) {
        if (block.type == BlockType.IMAGE) {
            if (currentPage.isNotEmpty()) {
                pages.add(ArrayList(currentPage))
                currentPage.clear()
                currentChars = 0
            }
            pages.add(listOf(index to block))
            continue
        }

        if (block.type == BlockType.TITLE) {
            if (currentPage.isNotEmpty()) {
                pages.add(ArrayList(currentPage))
                currentPage.clear()
                currentChars = 0
            }
            currentPage.add(index to block)
            currentChars += block.text.length
            continue
        }

        // Long paragraphs split cleanly into sentence chunks
        if (block.text.length > targetChars) {
            if (currentPage.isNotEmpty()) {
                pages.add(ArrayList(currentPage))
                currentPage.clear()
                currentChars = 0
            }
            val chunks = splitParagraph(block.text, targetChars)
            for (chunk in chunks) {
                pages.add(listOf(index to block.copy(text = chunk)))
            }
            continue
        }

        if (currentChars + block.text.length > targetChars && currentPage.isNotEmpty()) {
            pages.add(ArrayList(currentPage))
            currentPage.clear()
            currentChars = 0
        }

        currentPage.add(index to block)
        currentChars += block.text.length
    }

    if (currentPage.isNotEmpty()) {
        pages.add(currentPage)
    }

    return if (pages.isEmpty()) listOf(emptyList()) else pages
}

private fun splitParagraph(text: String, maxChars: Int): List<String> {
    if (text.length <= maxChars) return listOf(text)
    val result = mutableListOf<String>()
    var remaining = text
    while (remaining.length > maxChars) {
        var splitPoint = -1
        for (delimiter in listOf(". ", "! ", "? ", "\n", "; ", ", ", " ")) {
            val idx = remaining.lastIndexOf(delimiter, maxChars)
            if (idx > maxChars / 2) {
                splitPoint = idx + delimiter.length
                break
            }
        }
        if (splitPoint == -1) splitPoint = maxChars
        result.add(remaining.substring(0, splitPoint).trim())
        remaining = remaining.substring(splitPoint).trim()
    }
    if (remaining.isNotBlank()) {
        result.add(remaining)
    }
    return result
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
    onToggleControls: () -> Unit
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
                    .padding(start = 48.dp, end = 8.dp, top = 8.dp, bottom = 24.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onToggleControls() },
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
                    .padding(start = 32.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onToggleControls() }
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
                modifier = Modifier.padding(bottom = 12.dp)
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
    modifier: Modifier = Modifier
) {
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
            val footnoteRegex = Regex("\\[([a-zA-Z0-9а-яА-ЯёЁ_\\s-]{1,20})\\]|\\{([0-9]+)\\}")
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
                    addStringAnnotation(
                        tag = "FOOTNOTE",
                        annotation = ref,
                        start = match.range.first,
                        end = match.range.last + 1
                    )
                }
            }
        }
    }

    ClickableText(
        text = annotated,
        style = style,
        onClick = { offset ->
            val annotations = annotated.getStringAnnotations(tag = "FOOTNOTE", start = offset, end = offset)
            if (annotations.isNotEmpty()) {
                val ref = annotations.first().item
                val content = resolveFootnoteText(ref, footnotes) ?: footnotes[ref] ?: "Примечание: $ref"
                onFootnoteClick(ref, content)
            } else {
                onToggleControls()
            }
        },
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
