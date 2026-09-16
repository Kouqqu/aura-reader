package com.aura.reader.ui.screens.reader

import androidx.compose.material3.CircularProgressIndicator

import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.ExperimentalSharedTransitionApi

import com.aura.reader.data.model.Quote
import com.aura.reader.ui.theme.Strings
import com.aura.reader.util.BookShareUtils
import androidx.compose.material.icons.filled.Share
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap

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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Rect
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.font.Font
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.runtime.snapshotFlow
import android.app.Activity
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.aura.reader.R
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import com.aura.reader.ui.theme.LocalAppStrings
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import kotlin.math.roundToInt

import com.aura.reader.data.model.PageTurnAnimation
import com.aura.reader.data.model.TwoColumnMode
import com.aura.reader.data.services.DictionaryService
import com.aura.reader.data.services.WordDefinition
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.TextButton
import kotlin.math.absoluteValue
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.drawscope.clipRect
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
import androidx.compose.runtime.mutableFloatStateOf
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
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    androidx.compose.animation.ExperimentalSharedTransitionApi::class
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
    val materialYouEnabled by viewModel.materialYouEnabled.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()

    // Search state
    var isSearchActive by remember { mutableStateOf(false) }
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val currentSearchIndex by viewModel.currentSearchIndex.collectAsState()
    val targetScrollOffset by viewModel.targetScrollOffset.collectAsState()

    // Bookmarks and Quotes
    val bookmarks by viewModel.bookmarks.collectAsState()
    val quotes by viewModel.quotes.collectAsState()
    val userWpm by viewModel.userAverageWpm.collectAsState()
    val ttsState by com.aura.reader.service.BookTtsService.ttsState.collectAsState()
    val currentChapterQuotes = remember(quotes, book?.id, currentChapterIndex) {
        val bId = book?.id
        if (bId != null) quotes.filter { it.bookId == bId && it.chapterIndex == currentChapterIndex } else emptyList()
    }
    val isCurrentChapterBookmarked = remember(bookmarks, book?.id, currentChapterIndex) {
        book?.id?.let { bId ->
            bookmarks.any { it.bookId == bId && it.chapterIndex == currentChapterIndex }
        } ?: false
    }

    var showControls by remember { mutableStateOf(true) }
    var showBookInfoDialog by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showTtsSheet by remember { mutableStateOf(false) }
    var showChaptersSheet by remember { mutableStateOf(false) }
    var showBookmarksQuotesSheet by remember { mutableStateOf(false) }
    var selectedFootnote by remember { mutableStateOf<Pair<String, String>?>(null) }
    var quoteToSave by remember { mutableStateOf<String?>(null) }
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val view = LocalView.current
    val window = remember(view) { (view.context as? Activity)?.window }

    DisposableEffect(showControls, window) {
        if (window != null) {
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            if (showControls) {
                insetsController.show(WindowInsetsCompat.Type.statusBars())
            } else {
                insetsController.hide(WindowInsetsCompat.Type.statusBars())
            }
        }
        onDispose {
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.show(WindowInsetsCompat.Type.statusBars())
            }
        }
    }

    var currentPagingPage by remember { mutableIntStateOf(0) }
    var totalPagingPages by remember { mutableIntStateOf(1) }
    var requestPagingPage by remember { mutableStateOf<Int?>(null) }
    var showReaderMenu by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val haptic = LocalHapticFeedback.current
    var lastPageSliderStep by remember { mutableIntStateOf(-1) }
    var lastChapterSliderStep by remember { mutableIntStateOf(-1) }

    var dictionaryWord by remember { mutableStateOf<String?>(null) }
    var dictionaryLoading by remember { mutableStateOf(false) }
    var dictionaryDefinition by remember { mutableStateOf<WordDefinition?>(null) }
    var dictionaryError by remember { mutableStateOf<String?>(null) }

    var translationText by remember { mutableStateOf<String?>(null) }
    var translationLoading by remember { mutableStateOf(false) }
    var translationResult by remember { mutableStateOf<String?>(null) }
    var translationError by remember { mutableStateOf<String?>(null) }

    val defaultToolbar = LocalTextToolbar.current
    val clipboardManager = LocalClipboardManager.current
    var selectedQuoteCopyAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var selectionRect by remember { mutableStateOf<Rect?>(null) }
    var textToolbarStatus by remember { mutableStateOf(TextToolbarStatus.Hidden) }
    var selectionResetKey by remember { mutableIntStateOf(0) }

    val customTextToolbar = remember(defaultToolbar) {
        object : TextToolbar {
            override val status: TextToolbarStatus
                get() = textToolbarStatus

            override fun hide() {
                textToolbarStatus = TextToolbarStatus.Hidden
                selectedQuoteCopyAction = null
                selectionRect = null
            }

            override fun showMenu(
                rect: Rect,
                onCopyRequested: (() -> Unit)?,
                onPasteRequested: (() -> Unit)?,
                onCutRequested: (() -> Unit)?,
                onSelectAllRequested: (() -> Unit)?
            ) {
                textToolbarStatus = TextToolbarStatus.Shown
                selectedQuoteCopyAction = onCopyRequested
                selectionRect = rect
            }
        }
    }

    BackHandler(enabled = selectedQuoteCopyAction != null) {
        selectionResetKey++
        customTextToolbar.hide()
    }

    AuraReaderTheme(
        themeMode = settings.themeMode,
        materialYou = materialYouEnabled
    ) {
      CompositionLocalProvider(LocalTextToolbar provides customTextToolbar) {
        val resolvedFontFamily = remember(settings.fontFamily, settings.fontName) {
            val fallback = when (settings.fontFamily) {
                ReaderFontFamily.SERIF -> FontFamily.Serif
                ReaderFontFamily.SANS_SERIF -> FontFamily.SansSerif
                ReaderFontFamily.MONOSPACE -> FontFamily.Monospace
                ReaderFontFamily.SYSTEM_DEFAULT -> FontFamily.Default
            }
            when (settings.fontName) {
                "Literata" -> FontFamily(Font(R.font.literata_regular))
                "PT Serif" -> FontFamily(Font(R.font.pt_serif_regular))
                "Lora" -> FontFamily(Font(R.font.lora_regular))
                "Inter" -> FontFamily(Font(R.font.inter_regular))
                "JetBrains Mono" -> FontFamily(Font(R.font.jetbrains_mono_regular))
                else -> fallback
            }
        }

        val chapters = book?.chapters ?: emptyList()
        val currentChapter = chapters.getOrNull(currentChapterIndex)
        val footnotes = book?.footnotes ?: emptyMap()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Stable Book Content Container (Never shifts or resizes on controls toggle)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
            ) {
                if (chapters.isNotEmpty()) {
                    if (settings.pagingMode && currentChapter != null) {
                        // --- Paging Mode (Листание страниц) ---
                        androidx.compose.runtime.key(currentChapterIndex) {
                            ChapterPagingView(
                                chapter = currentChapter,
                                chapterIndex = currentChapterIndex,
                                initialBlockIndex = savedOffset,
                                totalChapters = chapters.size,
                                prevChapter = chapters.getOrNull(currentChapterIndex - 1),
                                nextChapter = chapters.getOrNull(currentChapterIndex + 1),
                                settings = settings,
                                resolvedFontFamily = resolvedFontFamily,
                                searchQuery = searchQuery,
                                footnotes = footnotes,
                                quotes = currentChapterQuotes,
                                userWpm = userWpm,
                                selectionResetKey = selectionResetKey,
                                onPageTurn = { words -> viewModel.onPageOrSectionTurn(words) },
                                targetBlockIndex = targetScrollOffset,
                                targetPage = requestPagingPage,
                                onConsumeTargetBlock = { viewModel.consumeTargetScrollOffset() },
                                onConsumeTargetPage = { requestPagingPage = null },
                                onToggleControls = { showControls = !showControls },
                                onPrevChapter = { viewModel.prevChapter(startAtEnd = true) },
                                onNextChapter = { viewModel.nextChapter() },
                                onFootnoteClick = { ref, content -> selectedFootnote = ref to content },
                                onSaveQuote = { quoteToSave = it },
                                onPageChange = { page, total ->
                                    currentPagingPage = page
                                    totalPagingPages = total
                                },
                                onUpdateProgress = { blockIdx, totalBlocks ->
                                    viewModel.updateScrollProgress(blockIdx, totalBlocks)
                                }
                            )
                        }
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
                                    selectionResetKey = selectionResetKey,
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
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(36.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 3.dp
                            )
                            Text(
                                text = strings.loadingBook,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }

        // Floating Top Bar with Material 3 Expressive Floating Card
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
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
                                imageVector = Icons.Default.ArrowBack,
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
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = strings.back
                            )
                        }

                        val currentBook = book
                        val sharedTransitionScope = com.aura.reader.ui.navigation.LocalSharedTransitionScope.current
                        val animatedVisibilityScope = com.aura.reader.ui.navigation.LocalNavAnimatedVisibilityScope.current
                        val coverSharedModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null && currentBook != null) {
                            with(sharedTransitionScope) {
                                Modifier.sharedElement(
                                    state = rememberSharedContentState(key = "book_cover_${currentBook.id}"),
                                    animatedVisibilityScope = animatedVisibilityScope,
                                    boundsTransform = { _, _ ->
                                        tween(durationMillis = 350, easing = FastOutSlowInEasing)
                                    },
                                    clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(6.dp))
                                )
                            }
                        } else Modifier

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
                                .clickable { showBookInfoDialog = true }
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = coverSharedModifier
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
                                        imageVector = Icons.Default.AutoStories,
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
                        }

                        // Audio Narration (TTS)
                        IconButton(onClick = {
                            if (!ttsState.isServiceRunning) {
                                viewModel.startTts(context)
                            }
                            showTtsSheet = true
                        }) {
                            Icon(
                                imageVector = if (ttsState.isPlaying) Icons.Default.VolumeUp else Icons.Default.Headphones,
                                contentDescription = strings.ttsListenAction,
                                tint = if (ttsState.isServiceRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }

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
                                    text = { Text(strings.ttsListenAction) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Headphones, contentDescription = null)
                                    },
                                    onClick = {
                                        showReaderMenu = false
                                        if (!ttsState.isServiceRunning) {
                                            viewModel.startTts(context)
                                        }
                                        showTtsSheet = true
                                    }
                                )
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
                                    text = { Text(strings.shareBookFile) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Share, contentDescription = null)
                                    },
                                    onClick = {
                                        showReaderMenu = false
                                        book?.let { BookShareUtils.shareBookFile(context, it) }
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

                                        Row(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable { showChaptersSheet = true }
                                                .padding(horizontal = 6.dp, vertical = 4.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(
                                                modifier = Modifier.weight(1f, fill = false),
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
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.ChevronRight,
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
                                            onValueChange = { newVal ->
                                                val step = newVal.toInt()
                                                if (step != lastPageSliderStep) {
                                                    lastPageSliderStep = step
                                                    if (settings.hapticFeedbackEnabled) {
                                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    }
                                                }
                                                requestPagingPage = step
                                            },
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
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { showChaptersSheet = true }
                                            .padding(horizontal = 6.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f, fill = false)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AutoStories,
                                                contentDescription = strings.contents,
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
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.ChevronRight,
                                                contentDescription = strings.contents,
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.primary
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
                                            onValueChange = { newVal ->
                                                val step = newVal.toInt()
                                                if (step != lastChapterSliderStep) {
                                                    lastChapterSliderStep = step
                                                    if (settings.hapticFeedbackEnabled) {
                                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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

                // Floating TTS Player Bar when active
                androidx.compose.animation.AnimatedVisibility(
                    visible = ttsState.isServiceRunning && !showTtsSheet,
                    enter = androidx.compose.animation.slideInVertically(initialOffsetY = { it }) + androidx.compose.animation.fadeIn(),
                    exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { it }) + androidx.compose.animation.fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp)
                        .padding(bottom = if (showControls) 175.dp else 24.dp)
                ) {
                    TtsControlBar(
                        ttsState = ttsState,
                        strings = strings,
                        onPlayPause = { com.aura.reader.service.BookTtsService.playPause(context) },
                        onPrev = { com.aura.reader.service.BookTtsService.prev(context) },
                        onNext = { com.aura.reader.service.BookTtsService.next(context) },
                        onStop = { com.aura.reader.service.BookTtsService.stop(context) },
                        onSpeedChange = { spd -> com.aura.reader.service.BookTtsService.setSpeed(context, spd) },
                        onClick = { showTtsSheet = true }
                    )
                }

            // Bottom Sheets
            if (showTtsSheet) {
                TtsBottomSheet(
                    ttsState = ttsState,
                    onDismiss = { showTtsSheet = false },
                    onPlayPause = { com.aura.reader.service.BookTtsService.playPause(context) },
                    onPrev = { com.aura.reader.service.BookTtsService.prev(context) },
                    onNext = { com.aura.reader.service.BookTtsService.next(context) },
                    onStop = {
                        com.aura.reader.service.BookTtsService.stop(context)
                        showTtsSheet = false
                    },
                    onSpeedChange = { spd -> com.aura.reader.service.BookTtsService.setSpeed(context, spd) },
                    onPitchChange = { pitch -> com.aura.reader.service.BookTtsService.setPitch(context, pitch) },
                    onVoiceChange = { voice -> com.aura.reader.service.BookTtsService.setVoice(context, voice) }
                )
            }

            if (showSettingsSheet) {
                ReaderSettingsBottomSheet(
                    settings = settings,
                    materialYouEnabled = materialYouEnabled,
                    onMaterialYouChange = { viewModel.setMaterialYouEnabled(it) },
                    onDismiss = { showSettingsSheet = false },
                    onFontSizeChange = { viewModel.setFontSize(it) },
                    onLineHeightChange = { viewModel.setLineHeight(it) },
                    onThemeModeChange = { viewModel.setThemeMode(it) },
                    onFontFamilyChange = { viewModel.setFontFamily(it) },
                    onLightImageBackgroundChange = { viewModel.setLightImageBackground(it) },
                    onPagingModeChange = { viewModel.setPagingMode(it) },
                    onAutoHyphenationChange = { viewModel.setAutoHyphenation(it) },
                    onTwoColumnModeChange = { viewModel.setTwoColumnMode(it) },
                    onPageAnimationChange = { viewModel.setPageAnimation(it) },
                    onHapticFeedbackChange = { viewModel.setHapticFeedbackEnabled(it) },
                    onFontNameChange = { viewModel.setFontName(it) }
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
                    onAddQuote = { viewModel.addQuote(it, 0xFFFFF59DL) },
                    onDeleteQuote = { viewModel.removeQuote(it) }
                )
            }

            val currentBook = book
            if (showBookInfoDialog && currentBook != null) {
                BookInfoDialog(
                    book = currentBook,
                    currentChapter = currentChapter,
                    currentChapterIndex = currentChapterIndex,
                    totalChapters = chapters.size,
                    currentPage = currentPagingPage,
                    totalPages = totalPagingPages,
                    progressPercent = currentBook.progressPercent,
                    onDismiss = { showBookInfoDialog = false }
                )
            }

            selectedFootnote?.let { (ref, content) ->
                FootnoteDialog(
                    refLabel = ref,
                    content = content,
                    onDismiss = { selectedFootnote = null }
                )
            }

            // Scrim to dismiss selection toolbar when tapping anywhere on the screen
            if (selectedQuoteCopyAction != null && selectionRect != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            selectionResetKey++
                            customTextToolbar.hide()
                        }
                )

                val density = LocalDensity.current
                val rect = selectionRect!!

                Popup(
                    popupPositionProvider = object : PopupPositionProvider {
                        override fun calculatePosition(
                            anchorBounds: IntRect,
                            windowSize: IntSize,
                            layoutDirection: LayoutDirection,
                            popupContentSize: IntSize
                        ): IntOffset {
                            val targetX = (rect.left + (rect.width - popupContentSize.width) / 2f).toInt()
                            val minX = with(density) { 12.dp.toPx() }.toInt()
                            val maxX = (windowSize.width - popupContentSize.width - minX).coerceAtLeast(minX)
                            val clampedX = targetX.coerceIn(minX, maxX)

                            val spaceAbove = rect.top - with(density) { 8.dp.toPx() }
                            val targetY = if (spaceAbove >= popupContentSize.height + with(density) { 48.dp.toPx() }) {
                                (rect.top - popupContentSize.height - with(density) { 8.dp.toPx() }).toInt()
                            } else {
                                (rect.bottom + with(density) { 8.dp.toPx() }).toInt()
                            }
                            val minY = with(density) { 40.dp.toPx() }.toInt()
                            val maxY = (windowSize.height - popupContentSize.height - with(density) { 20.dp.toPx() }).toInt()
                            val clampedY = targetY.coerceIn(minY, maxY.coerceAtLeast(minY))

                            return IntOffset(clampedX, clampedY)
                        }
                    },
                    onDismissRequest = {
                        selectionResetKey++
                        customTextToolbar.hide()
                    },
                    properties = PopupProperties(
                        focusable = false,
                        dismissOnBackPress = true,
                        dismissOnClickOutside = true
                    )
                ) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        shadowElevation = 8.dp,
                        tonalElevation = 6.dp,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            // 1. Copy
                            TextButton(
                                onClick = {
                                    val action = selectedQuoteCopyAction
                                    action?.invoke()
                                    selectionResetKey++
                                    customTextToolbar.hide()
                                },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(strings.copy, style = MaterialTheme.typography.labelSmall, maxLines = 1, softWrap = false)
                            }

                            Box(modifier = Modifier.height(14.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)))

                            // 2. Quote
                            TextButton(
                                onClick = {
                                    val action = selectedQuoteCopyAction
                                    action?.invoke()
                                    val clip = clipboardManager.getText()?.text
                                    if (!clip.isNullOrBlank()) {
                                        quoteToSave = clip.trim()
                                    }
                                    selectionResetKey++
                                    customTextToolbar.hide()
                                },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.FormatQuote, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(strings.quoteAction, style = MaterialTheme.typography.labelSmall, maxLines = 1, softWrap = false)
                            }

                            Box(modifier = Modifier.height(14.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)))

                            // 3. Dictionary
                            TextButton(
                                onClick = {
                                    val action = selectedQuoteCopyAction
                                    action?.invoke()
                                    val clip = clipboardManager.getText()?.text?.trim()
                                    selectionResetKey++
                                    customTextToolbar.hide()
                                    if (!clip.isNullOrBlank()) {
                                        val word = clip.take(60)
                                        dictionaryWord = word
                                        dictionaryLoading = true
                                        dictionaryDefinition = null
                                        dictionaryError = null
                                        coroutineScope.launch {
                                             DictionaryService.lookupDefinition(word)
                                                .onSuccess { def ->
                                                    dictionaryDefinition = def
                                                    dictionaryLoading = false
                                                }
                                                .onFailure { err ->
                                                    dictionaryError = err.message ?: strings.dictionaryNotFound
                                                    dictionaryLoading = false
                                                }
                                        }
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.AutoStories, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(strings.dictionaryAction, style = MaterialTheme.typography.labelSmall, maxLines = 1, softWrap = false)
                            }

                            Box(modifier = Modifier.height(14.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)))

                            // 4. Translation
                            TextButton(
                                onClick = {
                                    val action = selectedQuoteCopyAction
                                    action?.invoke()
                                    val clip = clipboardManager.getText()?.text?.trim()
                                    selectionResetKey++
                                    customTextToolbar.hide()
                                    if (!clip.isNullOrBlank()) {
                                        translationText = clip
                                        translationLoading = true
                                        translationResult = null
                                        translationError = null
                                        coroutineScope.launch {
                                             DictionaryService.translateText(clip, targetLang = appLanguage.code)
                                                .onSuccess { res ->
                                                    translationResult = res
                                                    translationLoading = false
                                                }
                                                .onFailure { err ->
                                                    translationError = err.message ?: strings.translationFailed
                                                    translationLoading = false
                                                }
                                        }
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.Translate, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(strings.translateAction, style = MaterialTheme.typography.labelSmall, maxLines = 1, softWrap = false)
                            }
                        }
                    }
                }
            }

            quoteToSave?.let { quoteText ->
                SaveQuoteBottomSheet(
                    initialText = quoteText,
                    bookTitle = book?.title ?: "",
                    chapterTitle = currentChapter?.title ?: "",
                    onDismiss = { quoteToSave = null },
                    onSaveQuote = { text, color ->
                        viewModel.addQuote(text, color)
                        quoteToSave = null
                    }
                )
            }

            dictionaryWord?.let { word ->
                DictionaryDialog(
                    word = word,
                    isLoading = dictionaryLoading,
                    definition = dictionaryDefinition,
                    errorMessage = dictionaryError,
                    onDismiss = { dictionaryWord = null }
                )
            }

            translationText?.let { text ->
                TranslationDialog(
                    originalText = text,
                    isLoading = translationLoading,
                    translatedText = translationResult,
                    errorMessage = translationError,
                    onDismiss = { translationText = null }
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
    quotes: List<Quote> = emptyList(),
    selectionResetKey: Int = 0,
    onToggleControls: () -> Unit,
    onFootnoteClick: (ref: String, content: String) -> Unit,
    onSaveQuote: (String) -> Unit,
    onPrevChapter: () -> Unit,
    onNextChapter: () -> Unit,
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

    val clampedInitialOffset = remember(initialScrollOffset, blocks.size) {
        if (blocks.isEmpty()) 0 else initialScrollOffset.coerceIn(0, (blocks.size - 1).coerceAtLeast(0))
    }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = clampedInitialOffset)

    if (isCurrentChapter) {
        LaunchedEffect(clampedInitialOffset) {
            if (clampedInitialOffset > 0 && listState.firstVisibleItemIndex == 0) {
                listState.scrollToItem(clampedInitialOffset)
            }
        }

        val firstVisibleIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
        LaunchedEffect(firstVisibleIndex) {
            val totalItems = blocks.size
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

    val strings = LocalAppStrings.current
    val statusBarTopInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val cutoutTopInset = WindowInsets.displayCutout.asPaddingValues().calculateTopPadding()
    val topPaddingDp = maxOf(statusBarTopInset, cutoutTopInset, 42.dp) + 6.dp

    androidx.compose.runtime.key(selectionResetKey) {
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
                    top = topPaddingDp,
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
                        quotes = quotes,
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
}

// ------------------------------------------------------------------------------------------------
// PAGING MODE VIEW
// ------------------------------------------------------------------------------------------------

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ChapterPagingView(
    chapter: Chapter,
    chapterIndex: Int,
    initialBlockIndex: Int = 0,
    totalChapters: Int,
    prevChapter: Chapter? = null,
    nextChapter: Chapter? = null,
    settings: ReaderSettings,
    resolvedFontFamily: FontFamily,
    searchQuery: String,
    footnotes: Map<String, String>,
    quotes: List<Quote> = emptyList(),
    userWpm: Float = 200f,
    selectionResetKey: Int = 0,
    onPageTurn: (Int) -> Unit = {},
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

        val statusBarTopInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val cutoutTopInset = WindowInsets.displayCutout.asPaddingValues().calculateTopPadding()
        val topPaddingDp = maxOf(statusBarTopInset, cutoutTopInset, 42.dp) + 6.dp
        val bottomNavInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

        val pillClearanceDp = 48.dp
        val bottomPaddingDp = bottomNavInset + pillClearanceDp
        val usableContentHeightDp = (screenHeightDp - topPaddingDp.value - bottomPaddingDp.value).coerceAtLeast(120f)

        val isTwoColumn = when (settings.twoColumnMode) {
            TwoColumnMode.ALWAYS -> true
            TwoColumnMode.OFF -> false
            TwoColumnMode.AUTO -> screenWidthDp >= 600f
        }

        val columnWidthDp = if (isTwoColumn) {
            ((screenWidthDp - 72f) / 2f).coerceAtLeast(120f)
        } else {
            (screenWidthDp - 48f).coerceAtLeast(120f)
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

        val textMeasurer = rememberTextMeasurer()
        val density = LocalDensity.current

        val pages = remember(
            blocks,
            settings.fontSizeSp,
            settings.lineHeightMultiplier,
            settings.fontFamily,
            settings.autoHyphenation,
            settings.twoColumnMode,
            resolvedFontFamily,
            usableContentHeightDp,
            screenWidthDp
        ) {
            paginateBlocks(
                blocks = blocks,
                settings = settings,
                resolvedFontFamily = resolvedFontFamily,
                textMeasurer = textMeasurer,
                density = density,
                contentHeightDp = usableContentHeightDp,
                screenWidthDp = screenWidthDp,
                columnWidthDp = columnWidthDp
            )
        }

        val strings = LocalAppStrings.current

        val hasPrev = chapterIndex > 0 && prevChapter != null
        val hasNext = chapterIndex < totalChapters - 1 && nextChapter != null

        val contentSpreadsCount = remember(pages.size, isTwoColumn) {
            if (isTwoColumn) ((pages.size + 1) / 2).coerceAtLeast(1) else pages.size.coerceAtLeast(1)
        }

        val prevPageOffset = if (hasPrev) 1 else 0
        val nextPageOffset = if (hasNext) 1 else 0
        val totalPagerSpreads = prevPageOffset + contentSpreadsCount + nextPageOffset

        val calculatedInitialPage = remember(pages, initialBlockIndex, isTwoColumn) {
            if (pages.isEmpty()) 0
            else {
                val pageByBlock = pages.indexOfLast { page ->
                    page.any { it.first <= initialBlockIndex }
                }
                val page = if (pageByBlock in pages.indices) {
                    pageByBlock
                } else if (initialBlockIndex in pages.indices) {
                    initialBlockIndex
                } else {
                    0
                }
                if (isTwoColumn) page / 2 else page
            }
        }

        val initialPagerSpread = prevPageOffset + calculatedInitialPage
        val pagerState = rememberPagerState(
            initialPage = initialPagerSpread.coerceIn(prevPageOffset, (totalPagerSpreads - 1).coerceAtLeast(prevPageOffset)),
            pageCount = { totalPagerSpreads.coerceAtLeast(1) }
        )
        val coroutineScope = rememberCoroutineScope()
        var lastViewedBlockIndex by remember { mutableIntStateOf(initialBlockIndex) }
        val haptic = LocalHapticFeedback.current
        var lastHapticSpread by remember { mutableIntStateOf(-1) }
        var lastHapticOffset by remember { mutableFloatStateOf(pagerState.currentPage.toFloat()) }

        // Continuous finger-following haptics during page flip
        LaunchedEffect(pagerState.isScrollInProgress, pagerState.currentPageOffsetFraction) {
            if (pagerState.isScrollInProgress) {
                val currentOffset = pagerState.currentPage + pagerState.currentPageOffsetFraction
                val delta = kotlin.math.abs(currentOffset - lastHapticOffset)
                if (delta >= 0.12f) {
                    if (settings.hapticFeedbackEnabled) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                    lastHapticOffset = currentOffset
                }
            } else {
                lastHapticOffset = pagerState.currentPage.toFloat()
            }
        }

        LaunchedEffect(pagerState, totalPagerSpreads, hasPrev, hasNext) {
            snapshotFlow { pagerState.settledPage }.collect { settled ->
                if (hasPrev && settled == 0) {
                    if (settings.hapticFeedbackEnabled) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                    onPrevChapter()
                } else if (hasNext && settled == totalPagerSpreads - 1) {
                    if (settings.hapticFeedbackEnabled) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                    onNextChapter()
                }
            }
        }

        LaunchedEffect(pagerState.currentPage, prevPageOffset, contentSpreadsCount, isTwoColumn, pages) {
            val currentSpread = pagerState.currentPage - prevPageOffset
            if (currentSpread in 0 until contentSpreadsCount) {
                if (currentSpread != lastHapticSpread) {
                    if (lastHapticSpread != -1 && settings.hapticFeedbackEnabled) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                    lastHapticSpread = currentSpread
                }

                val activePageIdx = if (isTwoColumn) (currentSpread * 2).coerceIn(0, (pages.size - 1).coerceAtLeast(0)) else currentSpread
                onPageChange(activePageIdx, pages.size.coerceAtLeast(1))

                val currentBlock = pages.getOrNull(activePageIdx)?.firstOrNull()?.first ?: 0
                lastViewedBlockIndex = currentBlock
                onUpdateProgress(currentBlock, blocks.size.coerceAtLeast(1))
            }
        }

        LaunchedEffect(pages, isTwoColumn) {
            val foundPage = pages.indexOfLast { page ->
                page.any { it.first <= lastViewedBlockIndex }
            }.coerceIn(0, (pages.size - 1).coerceAtLeast(0))
            val targetSpread = if (isTwoColumn) foundPage / 2 else foundPage
            val targetPagerSpread = prevPageOffset + targetSpread
            if (targetPagerSpread != pagerState.currentPage && targetPagerSpread in prevPageOffset until (prevPageOffset + contentSpreadsCount)) {
                pagerState.scrollToPage(targetPagerSpread)
            }
        }

        // React to requested page from page slider
        LaunchedEffect(targetPage) {
            if (targetPage != null) {
                val targetSpread = if (isTwoColumn) targetPage / 2 else targetPage
                val targetPagerSpread = prevPageOffset + targetSpread
                if (targetPagerSpread in prevPageOffset until (prevPageOffset + contentSpreadsCount) && targetPagerSpread != pagerState.currentPage) {
                    if (settings.pageAnimation == PageTurnAnimation.INSTANT) {
                        pagerState.scrollToPage(targetPagerSpread)
                    } else {
                        pagerState.animateScrollToPage(targetPagerSpread)
                    }
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
                    val targetSpread = if (isTwoColumn) foundPage / 2 else foundPage
                    val targetPagerSpread = prevPageOffset + targetSpread
                    if (settings.pageAnimation == PageTurnAnimation.INSTANT) {
                        pagerState.scrollToPage(targetPagerSpread)
                    } else {
                        pagerState.animateScrollToPage(targetPagerSpread)
                    }
                }
                onConsumeTargetBlock()
            }
        }

        val instantSwipeModifier = if (settings.pageAnimation == PageTurnAnimation.INSTANT) {
            Modifier.pointerInput(pagerState.currentPage, totalPagerSpreads) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount < -25f) {
                        if (pagerState.currentPage < totalPagerSpreads - 1) {
                            coroutineScope.launch { pagerState.scrollToPage(pagerState.currentPage + 1) }
                        }
                    } else if (dragAmount > 25f) {
                        if (pagerState.currentPage > 0) {
                            coroutineScope.launch { pagerState.scrollToPage(pagerState.currentPage - 1) }
                        }
                    }
                }
            }
        } else Modifier

        HorizontalPager(
            state = pagerState,
            userScrollEnabled = settings.pageAnimation != PageTurnAnimation.INSTANT,
            modifier = Modifier
                .fillMaxSize()
                .then(instantSwipeModifier)
        ) { spreadIdx ->
            // Вычисляем, насколько страница сдвинута от центра (от -1.0 до 1.0)
            val pageOffset = ((pagerState.currentPage - spreadIdx) + pagerState.currentPageOffsetFraction)
            val animModifier = when (settings.pageAnimation) {
                PageTurnAnimation.CURL -> {
                    Modifier
                        .graphicsLayer {
                            // 1. Убираем стандартное плоское смещение Pager, возвращая страницу в центр
                            translationX = pageOffset * size.width

                            // 2. Устанавливаем точку вращения (левый корешок или правый край)
                            transformOrigin = TransformOrigin(
                                pivotFractionX = if (pageOffset > 0) 0f else 1f,
                                pivotFractionY = 0.5f
                            )

                            // 3. Вычисляем угол вращения (разворот на 180 градусов)
                            val rotation = pageOffset * -180f
                            // Ограничиваем, чтобы не улетало
                            rotationY = rotation.coerceIn(-180f, 180f)

                            // 4. Добавляем глубину (перспективу)
                            cameraDistance = 12f * density.density
                        }
                        // 5. Динамическое скрытие: скрываем тыльную сторону "листа" после поворота на 90 градусов
                        .graphicsLayer {
                            alpha = if (pageOffset.absoluteValue >= 0.5f) 0f else 1f
                        }
                        // 6. Реалистичная тень в месте сгиба/стыка страниц
                        .drawWithContent {
                            drawContent()
                            if (pageOffset != 0f) {
                                val shadowAlpha = (pageOffset.absoluteValue).coerceIn(0f, 0.6f)
                                val brush = if (pageOffset > 0) {
                                    Brush.horizontalGradient(
                                        colors = listOf(Color.Black.copy(alpha = shadowAlpha), Color.Transparent),
                                        startX = 0f,
                                        endX = size.width
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = shadowAlpha)),
                                        startX = 0f,
                                        endX = size.width
                                    )
                                }
                                drawRect(brush = brush)
                            }
                        }
                }
                PageTurnAnimation.REALISTIC_CURL -> {
                    Modifier
                        .graphicsLayer {
                            translationX = pageOffset * size.width
                            transformOrigin = TransformOrigin(
                                pivotFractionX = if (pageOffset > 0) 0.02f else 0.98f,
                                pivotFractionY = 0.5f
                            )
                            val rotation = pageOffset * -160f
                            rotationY = rotation.coerceIn(-160f, 160f)
                            cameraDistance = 16f * density.density
                            shadowElevation = if (pageOffset.absoluteValue > 0.02f) 16f else 0f
                        }
                        .graphicsLayer {
                            alpha = if (pageOffset.absoluteValue >= 0.5f) 0f else 1f
                        }
                        .drawWithContent {
                            drawContent()
                            if (pageOffset != 0f) {
                                val shadowAlpha = (pageOffset.absoluteValue * 0.75f).coerceIn(0f, 0.7f)
                                val brush = if (pageOffset > 0) {
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = shadowAlpha),
                                            Color.Black.copy(alpha = shadowAlpha * 0.4f),
                                            Color.Transparent
                                        ),
                                        startX = 0f,
                                        endX = size.width * 0.7f
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = shadowAlpha * 0.4f),
                                            Color.Black.copy(alpha = shadowAlpha)
                                        ),
                                        startX = size.width * 0.3f,
                                        endX = size.width
                                    )
                                }
                                drawRect(brush = brush)
                            }
                        }
                }
                PageTurnAnimation.SLIDE -> Modifier
                PageTurnAnimation.INSTANT -> Modifier
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(animModifier)
                    .background(MaterialTheme.colorScheme.background)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onToggleControls() }
            ) {
                if (hasPrev && spreadIdx == 0) {
                    ChapterTransitionSpread(
                        isNext = false,
                        chapterTitle = prevChapter?.title ?: "",
                        chapterNumber = chapterIndex,
                        settings = settings,
                        resolvedFontFamily = resolvedFontFamily,
                        onClick = onPrevChapter
                    )
                } else if (hasNext && spreadIdx == totalPagerSpreads - 1) {
                    ChapterTransitionSpread(
                        isNext = true,
                        chapterTitle = nextChapter?.title ?: "",
                        chapterNumber = chapterIndex + 2,
                        settings = settings,
                        resolvedFontFamily = resolvedFontFamily,
                        onClick = onNextChapter
                    )
                } else {
                    val contentSpreadIdx = spreadIdx - prevPageOffset
                    androidx.compose.runtime.key(selectionResetKey) {
                        SelectionContainer {
                            if (isTwoColumn) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 24.dp)
                                        .padding(top = topPaddingDp, bottom = bottomPaddingDp),
                                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                                ) {
                                    val leftPageBlocks = pages.getOrNull(contentSpreadIdx * 2) ?: emptyList()
                                    val rightPageBlocks = pages.getOrNull(contentSpreadIdx * 2 + 1) ?: emptyList()

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(),
                                        verticalArrangement = Arrangement.Top
                                    ) {
                                        for ((_, block) in leftPageBlocks) {
                                            RenderBlock(
                                                block = block,
                                                settings = settings,
                                                resolvedFontFamily = resolvedFontFamily,
                                                searchQuery = searchQuery,
                                                footnotes = footnotes,
                                                quotes = quotes,
                                                onFootnoteClick = onFootnoteClick,
                                                onToggleControls = onToggleControls,
                                                onSaveQuote = onSaveQuote
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .width(1.dp)
                                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                                    )

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(),
                                        verticalArrangement = Arrangement.Top
                                    ) {
                                        for ((_, block) in rightPageBlocks) {
                                            RenderBlock(
                                                block = block,
                                                settings = settings,
                                                resolvedFontFamily = resolvedFontFamily,
                                                searchQuery = searchQuery,
                                                footnotes = footnotes,
                                                quotes = quotes,
                                                onFootnoteClick = onFootnoteClick,
                                                onToggleControls = onToggleControls,
                                                onSaveQuote = onSaveQuote
                                            )
                                        }
                                    }
                                }
                            } else {
                                val pageBlocks = pages.getOrNull(contentSpreadIdx) ?: emptyList()
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 24.dp)
                                        .padding(top = topPaddingDp, bottom = bottomPaddingDp),
                                    verticalArrangement = Arrangement.Top
                                ) {
                                    for ((_, block) in pageBlocks) {
                                        RenderBlock(
                                            block = block,
                                            settings = settings,
                                            resolvedFontFamily = resolvedFontFamily,
                                            searchQuery = searchQuery,
                                            footnotes = footnotes,
                                            quotes = quotes,
                                            onFootnoteClick = onFootnoteClick,
                                            onToggleControls = onToggleControls,
                                            onSaveQuote = onSaveQuote
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Thin margin tap zones for fast page flipping (outer padding area only)
        val flipPage: (Int) -> Unit = { target ->
            coroutineScope.launch {
                if (settings.pageAnimation == PageTurnAnimation.INSTANT) {
                    pagerState.scrollToPage(target)
                } else {
                    pagerState.animateScrollToPage(target)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(64.dp)
                .align(Alignment.CenterStart)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (pagerState.currentPage > 0) {
                        flipPage(pagerState.currentPage - 1)
                    }
                }
        )

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(64.dp)
                .align(Alignment.CenterEnd)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (pagerState.currentPage < totalPagerSpreads - 1) {
                        flipPage(pagerState.currentPage + 1)
                    }
                }
        )

        // Kindle-style Bottom Reading Progress Indicator (cycles: % -> time remaining -> page count)
        val contentSpreadForProgress = (pagerState.currentPage - prevPageOffset).coerceIn(0, (contentSpreadsCount - 1).coerceAtLeast(0))
        val intraChapterProgress = if (contentSpreadsCount > 1) {
            (contentSpreadForProgress.toFloat() / (contentSpreadsCount - 1)).coerceIn(0f, 1f)
        } else 0f
        val overallPercent = (((chapterIndex + intraChapterProgress) / totalChapters.coerceAtLeast(1)) * 100).toInt().coerceIn(0, 100)

        val remainingWords = remember(pages, contentSpreadForProgress, isTwoColumn) {
            val startPage = if (isTwoColumn) contentSpreadForProgress * 2 else contentSpreadForProgress
            var words = 0
            for (p in startPage until pages.size) {
                val pBlocks = pages.getOrNull(p) ?: continue
                for ((_, b) in pBlocks) {
                    words += b.text.split(Regex("\\s+")).count { it.isNotBlank() }
                }
            }
            words
        }
        val minutesLeft = (remainingWords / userWpm.coerceAtLeast(80f)).roundToInt().coerceAtLeast(1)

        LaunchedEffect(pagerState.currentPage) {
            val pageBlocks = if (isTwoColumn) {
                val p1 = contentSpreadForProgress * 2
                val p2 = p1 + 1
                (pages.getOrNull(p1) ?: emptyList()) + (pages.getOrNull(p2) ?: emptyList())
            } else {
                pages.getOrNull(contentSpreadForProgress) ?: emptyList()
            }
            val wordsCount = pageBlocks.sumOf { (_, b) ->
                b.text.split(Regex("\\s+")).count { it.isNotBlank() }
            }
            onPageTurn(wordsCount)
        }

        var bottomProgressMode by remember { mutableIntStateOf(0) } // 0: %, 1: time left, 2: page of pages

        val bottomText = when (bottomProgressMode) {
            0 -> "$overallPercent%"
            1 -> strings.minutesLeftInChapter(minutesLeft)
            else -> {
                if (isTwoColumn) {
                    val p1 = contentSpreadForProgress * 2 + 1
                    val p2 = (contentSpreadForProgress * 2 + 2).coerceAtMost(pages.size)
                    if (p1 == p2) "${strings.page} $p1 ${strings.ofPages} ${pages.size}" else "${strings.page} $p1–$p2 ${strings.ofPages} ${pages.size}"
                } else {
                    "${strings.page} ${contentSpreadForProgress + 1} ${strings.ofPages} ${pages.size}"
                }
            }
        }

        Surface(
            onClick = {
                bottomProgressMode = (bottomProgressMode + 1) % 3
                if (settings.hapticFeedbackEnabled) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            },
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = when (bottomProgressMode) {
                        0 -> Icons.Default.MenuBook
                        1 -> Icons.Default.Timer
                        else -> Icons.Default.AutoStories
                    },
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = bottomText,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 3 interactive indicator dots showing the 3 modes: • ○ ○
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { idx ->
                        Box(
                            modifier = Modifier
                                .size(if (idx == bottomProgressMode) 4.5.dp else 3.dp)
                                .clip(CircleShape)
                                .background(
                                    if (idx == bottomProgressMode) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                                )
                        )
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// PAGINATION HELPER
// ------------------------------------------------------------------------------------------------

private fun paginateBlocks(
    blocks: List<FormattedBlock>,
    settings: ReaderSettings,
    resolvedFontFamily: FontFamily,
    textMeasurer: TextMeasurer,
    density: Density,
    contentHeightDp: Float = 600f,
    screenWidthDp: Float = 380f,
    columnWidthDp: Float? = null
): List<List<Pair<Int, FormattedBlock>>> {
    if (blocks.isEmpty()) return listOf(emptyList())

    val usableWidthDp = columnWidthDp ?: (screenWidthDp - 48f).coerceAtLeast(100f)
    val maxWidthPx = with(density) { usableWidthDp.dp.roundToPx() }

    val safetyBufferPx = with(density) { 18.dp.toPx() }
    val maxHeightPx = (with(density) { contentHeightDp.dp.toPx() } - safetyBufferPx).coerceAtLeast(100f)
    val firstPageExtraBufferPx = with(density) { 48.dp.toPx() }
    val paragraphSpacingPx = with(density) { 8.dp.toPx() }

    val pages = mutableListOf<List<Pair<Int, FormattedBlock>>>()
    var currentPage = mutableListOf<Pair<Int, FormattedBlock>>()
    var currentHeightPx = 0f

    fun getEffectiveMaxHeightPx(): Float {
        val isFirstPage = pages.isEmpty() || currentPage.any { it.second.type == BlockType.TITLE }
        return if (isFirstPage) {
            (maxHeightPx - firstPageExtraBufferPx).coerceAtLeast(100f)
        } else {
            maxHeightPx
        }
    }

    fun flushPage() {
        if (currentPage.isNotEmpty()) {
            pages.add(ArrayList(currentPage))
            currentPage.clear()
            currentHeightPx = 0f
        }
    }

    for ((originalIndex, block) in blocks.withIndex()) {
        when (block.type) {
            BlockType.IMAGE -> {
                flushPage()
                pages.add(listOf(originalIndex to block))
            }
            BlockType.TITLE -> {
                val style = TextStyle(
                    fontSize = (settings.fontSizeSp * 1.35f).sp,
                    lineHeight = (settings.fontSizeSp * settings.lineHeightMultiplier * 1.25f).sp,
                    fontFamily = resolvedFontFamily,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                val layout = textMeasurer.measure(block.text, style, constraints = Constraints(maxWidth = maxWidthPx))
                val titlePaddingPx = with(density) { 22.dp.toPx() } // 8dp top + 14dp bottom
                val totalTitleHeight = layout.size.height + titlePaddingPx
                val effectiveMax = getEffectiveMaxHeightPx()
                if (currentHeightPx > effectiveMax * 0.45f || (currentHeightPx + totalTitleHeight > effectiveMax && currentPage.isNotEmpty())) {
                    flushPage()
                }
                currentPage.add(originalIndex to block)
                currentHeightPx += totalTitleHeight
            }
            BlockType.SUBTITLE -> {
                val style = TextStyle(
                    fontSize = (settings.fontSizeSp * 1.15f).sp,
                    lineHeight = (settings.fontSizeSp * settings.lineHeightMultiplier * 1.15f).sp,
                    fontFamily = resolvedFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                val layout = textMeasurer.measure(block.text, style, constraints = Constraints(maxWidth = maxWidthPx))
                val subPaddingPx = with(density) { 14.dp.toPx() } // 14dp bottom
                val totalSubHeight = layout.size.height + subPaddingPx
                val effectiveMax = getEffectiveMaxHeightPx()
                if (currentHeightPx > effectiveMax * 0.6f || (currentHeightPx + totalSubHeight > effectiveMax && currentPage.isNotEmpty())) {
                    flushPage()
                }
                currentPage.add(originalIndex to block)
                currentHeightPx += totalSubHeight
            }
            BlockType.DIVIDER -> {
                val dividerHeightPx = with(density) { 16.dp.toPx() }
                val effectiveMax = getEffectiveMaxHeightPx()
                if (currentHeightPx + dividerHeightPx > effectiveMax && currentPage.isNotEmpty()) {
                    flushPage()
                }
                currentPage.add(originalIndex to block)
                currentHeightPx += dividerHeightPx
            }
            BlockType.EPIGRAPH -> {
                val epigraphWidthPx = with(density) { (usableWidthDp - 44f).coerceAtLeast(80f).dp.roundToPx() }
                val style = TextStyle(
                    fontSize = (settings.fontSizeSp * 0.92f).sp,
                    lineHeight = (settings.fontSizeSp * settings.lineHeightMultiplier).sp,
                    fontFamily = resolvedFontFamily,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.End,
                    hyphens = if (settings.autoHyphenation) Hyphens.Auto else Hyphens.None,
                    lineBreak = if (settings.autoHyphenation) LineBreak.Paragraph else LineBreak.Simple
                )
                val layout = textMeasurer.measure(formatTypography(block.text), style, constraints = Constraints(maxWidth = epigraphWidthPx))
                var totalHeight = layout.size.height + with(density) { 16.dp.toPx() } // 4dp top + 12dp bottom
                if (!block.subText.isNullOrBlank()) {
                    val subStyle = TextStyle(
                        fontSize = (settings.fontSizeSp * 0.82f).sp,
                        fontFamily = resolvedFontFamily,
                        textAlign = TextAlign.End
                    )
                    val subLayout = textMeasurer.measure(block.subText, subStyle, constraints = Constraints(maxWidth = epigraphWidthPx))
                    totalHeight += subLayout.size.height + with(density) { 4.dp.toPx() } // 4dp spacer
                }
                val effectiveMax = getEffectiveMaxHeightPx()
                if (currentHeightPx + totalHeight > effectiveMax && currentPage.isNotEmpty()) {
                    flushPage()
                }
                currentPage.add(originalIndex to block)
                currentHeightPx += totalHeight
            }
            BlockType.VERSE -> {
                val verseWidthPx = with(density) { (usableWidthDp - 44f).coerceAtLeast(80f).dp.roundToPx() }
                val style = TextStyle(
                    fontSize = (settings.fontSizeSp * 0.95f).sp,
                    lineHeight = (settings.fontSizeSp * settings.lineHeightMultiplier * 0.95f).sp,
                    fontFamily = resolvedFontFamily,
                    fontStyle = FontStyle.Italic,
                    hyphens = if (settings.autoHyphenation) Hyphens.Auto else Hyphens.None,
                    lineBreak = if (settings.autoHyphenation) LineBreak.Paragraph else LineBreak.Simple
                )
                val layout = textMeasurer.measure(formatTypography(block.text), style, constraints = Constraints(maxWidth = verseWidthPx))
                val totalHeight = layout.size.height + with(density) { 18.dp.toPx() } // 6dp top + 12dp bottom
                val effectiveMax = getEffectiveMaxHeightPx()
                if (currentHeightPx + totalHeight > effectiveMax && currentPage.isNotEmpty()) {
                    flushPage()
                }
                currentPage.add(originalIndex to block)
                currentHeightPx += totalHeight
            }
            BlockType.PARAGRAPH -> {
                var remainingText = formatTypography(block.text.trim())
                var isContinuation = false
                var loopGuard = 0

                while (remainingText.isNotEmpty() && loopGuard++ < 1000) {
                    val effectiveMax = getEffectiveMaxHeightPx()
                    val availableHeightPx = effectiveMax - currentHeightPx

                    val style = TextStyle(
                        fontSize = settings.fontSizeSp.sp,
                        lineHeight = (settings.fontSizeSp * settings.lineHeightMultiplier).sp,
                        fontFamily = resolvedFontFamily,
                        textIndent = if (isContinuation) TextIndent.None else TextIndent(firstLine = (settings.fontSizeSp * 1.2f).sp),
                        hyphens = if (settings.autoHyphenation) Hyphens.Auto else Hyphens.None,
                        lineBreak = if (settings.autoHyphenation) LineBreak.Paragraph else LineBreak.Simple
                    )

                    val layoutResult = textMeasurer.measure(
                        text = remainingText,
                        style = style,
                        constraints = Constraints(maxWidth = maxWidthPx)
                    )

                    if (layoutResult.size.height + paragraphSpacingPx <= availableHeightPx) {
                        currentPage.add(originalIndex to block.copy(
                            text = remainingText,
                            subText = if (isContinuation) "continuation" else block.subText
                        ))
                        currentHeightPx += layoutResult.size.height + paragraphSpacingPx
                        remainingText = ""
                    } else {
                        var fittingLines = 0
                        for (lineIdx in 0 until layoutResult.lineCount) {
                            val lineBottom = layoutResult.getLineBottom(lineIdx)
                            if (lineBottom + paragraphSpacingPx <= availableHeightPx) {
                                fittingLines = lineIdx + 1
                            } else {
                                break
                            }
                        }

                        if (fittingLines < 2 && currentPage.isNotEmpty()) {
                            flushPage()
                            continue
                        }

                        val linesToTake = fittingLines.coerceAtLeast(1)
                        val lastLineIdx = (linesToTake - 1).coerceIn(0, layoutResult.lineCount - 1)
                        var endOffset = layoutResult.getLineEnd(lastLineIdx, visibleEnd = true).coerceIn(0, remainingText.length)
                        if (endOffset in 1 until remainingText.length && !remainingText[endOffset].isWhitespace() && !remainingText[endOffset - 1].isWhitespace()) {
                            val lastSpace = remainingText.lastIndexOf(' ', endOffset)
                            if (lastSpace > 0) {
                                endOffset = lastSpace
                            }
                        }

                        if (endOffset <= 0 || endOffset >= remainingText.length) {
                            currentPage.add(originalIndex to block.copy(
                                text = remainingText,
                                subText = if (isContinuation) "continuation" else block.subText
                            ))
                            flushPage()
                            remainingText = ""
                        } else {
                            val chunk = remainingText.substring(0, endOffset).trim()
                            val nextChunk = remainingText.substring(endOffset).trim()

                            if (chunk.isNotEmpty()) {
                                currentPage.add(originalIndex to block.copy(
                                    text = chunk,
                                    subText = if (isContinuation) "continuation" else block.subText
                                ))
                            }
                            flushPage()
                            remainingText = nextChunk
                            isContinuation = true
                        }
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
    quotes: List<Quote> = emptyList(),
    onFootnoteClick: (ref: String, content: String) -> Unit,
    onToggleControls: () -> Unit,
    onSaveQuote: (String) -> Unit
) {
    when (block.type) {
        BlockType.TITLE -> {
            Text(
                text = block.text,
                style = TextStyle(
                    fontSize = (settings.fontSizeSp * 1.35f).sp,
                    lineHeight = (settings.fontSizeSp * settings.lineHeightMultiplier * 1.25f).sp,
                    fontFamily = resolvedFontFamily,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 14.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onToggleControls() }
            )
        }
        BlockType.SUBTITLE -> {
            Text(
                text = block.text,
                style = TextStyle(
                    fontSize = (settings.fontSizeSp * 1.15f).sp,
                    lineHeight = (settings.fontSizeSp * settings.lineHeightMultiplier * 1.15f).sp,
                    fontFamily = resolvedFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                ),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
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
            val isSystemDark = isSystemInDarkTheme()
            val isDark = settings.themeMode == com.aura.reader.data.model.ReaderThemeMode.DARK ||
                    settings.themeMode == com.aura.reader.data.model.ReaderThemeMode.AMOLED ||
                    (settings.themeMode == com.aura.reader.data.model.ReaderThemeMode.SYSTEM_DYNAMIC && isSystemDark)
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
                                .then(
                                    if (shouldApplyLightCard) {
                                        Modifier.background(Color.White)
                                    } else Modifier
                                )
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
            val isContinuation = block.subText == "continuation"
            val paragraphStyle = TextStyle(
                fontSize = settings.fontSizeSp.sp,
                lineHeight = (settings.fontSizeSp * settings.lineHeightMultiplier).sp,
                fontFamily = resolvedFontFamily,
                color = MaterialTheme.colorScheme.onBackground,
                textIndent = if (isContinuation) TextIndent.None else TextIndent(firstLine = (settings.fontSizeSp * 1.2f).sp),
                hyphens = if (settings.autoHyphenation) Hyphens.Auto else Hyphens.None,
                lineBreak = if (settings.autoHyphenation) LineBreak.Paragraph else LineBreak.Simple
            )

            val formattedText = remember(block.text) { formatTypography(block.text) }

            InteractiveText(
                rawText = formattedText,
                style = paragraphStyle,
                searchQuery = searchQuery,
                footnotes = footnotes,
                quotes = quotes,
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
    quotes: List<Quote> = emptyList(),
    onFootnoteClick: (ref: String, content: String) -> Unit,
    onToggleControls: () -> Unit,
    onSaveQuote: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val annotated = remember(rawText, searchQuery, footnotes, quotes) {
        buildAnnotatedString {
            append(rawText)

            // Highlight saved quotes
            for (quote in quotes) {
                val qText = quote.text.trim()
                if (qText.length >= 2) {
                    var start = 0
                    while (start < rawText.length) {
                        val idx = rawText.indexOf(qText, start, ignoreCase = true)
                        if (idx == -1) break
                        addStyle(
                            style = SpanStyle(
                                background = Color(quote.color).copy(alpha = 0.38f)
                            ),
                            start = idx,
                            end = idx + qText.length
                        )
                        start = idx + qText.length
                    }
                }
            }

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


@Composable
fun TtsControlBar(
    ttsState: com.aura.reader.service.TtsState,
    strings: Strings,
    onPlayPause: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onStop: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.98f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Headphones,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${strings.ttsNotificationTitle} (${ttsState.currentParagraphIndex + 1}/${ttsState.totalParagraphs.coerceAtLeast(1)})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )
                Text(
                    text = ttsState.currentText.ifBlank { ttsState.chapterTitle },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = onPrev,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = strings.previous,
                    modifier = Modifier.size(22.dp)
                )
            }

            FilledIconButton(
                onClick = onPlayPause,
                modifier = Modifier.size(42.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = if (ttsState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (ttsState.isPlaying) strings.ttsPaused else strings.ttsPlaying,
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(
                onClick = onNext,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = strings.next,
                    modifier = Modifier.size(22.dp)
                )
            }

            val speeds = listOf(0.8f, 1.0f, 1.25f, 1.5f, 2.0f)
            TextButton(
                onClick = {
                    val currentIdx = speeds.indexOfFirst { kotlin.math.abs(it - ttsState.speed) < 0.05f }
                    val nextSpeed = speeds[(if (currentIdx >= 0) currentIdx + 1 else 1) % speeds.size]
                    onSpeedChange(nextSpeed)
                },
                contentPadding = PaddingValues(horizontal = 4.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    text = "${ttsState.speed}x",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(
                onClick = onStop,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = strings.ttsStop,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// TYPOGRAPHY FORMATTING HELPER
// ------------------------------------------------------------------------------------------------

fun formatTypography(text: String): String {
    if (text.isEmpty()) return text
    var result = text
    // Dialogue dashes at line start: "- " or "-- " or "— " -> "—\u00A0"
    result = result.replace(Regex("(^|(?<=\\n))[-—–]\\s+"), "—\u00A0")
    // Sentence dashes inside text: attach to previous word with non-breaking space
    result = result.replace(Regex("(?<=\\S)\\s+[-—–]\\s+"), "\u00A0— ")
    return result
}

// ------------------------------------------------------------------------------------------------
// CHAPTER TRANSITION SPREAD (NATIVE PAGE FLIP TO PREV / NEXT CHAPTER)
// ------------------------------------------------------------------------------------------------

@Composable
fun ChapterTransitionSpread(
    isNext: Boolean,
    chapterTitle: String,
    chapterNumber: Int,
    settings: ReaderSettings,
    resolvedFontFamily: FontFamily,
    onClick: () -> Unit = {}
) {
    val strings = LocalAppStrings.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isNext) Icons.Default.SkipNext else Icons.Default.SkipPrevious,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isNext) strings.next.uppercase() else strings.previous.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.5.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "${strings.chapters} $chapterNumber",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        if (chapterTitle.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = chapterTitle,
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = resolvedFontFamily,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ------------------------------------------------------------------------------------------------
// INTERACTIVE BOOK INFO DIALOG (HEADER TAP)
// ------------------------------------------------------------------------------------------------

@Composable
fun BookInfoDialog(
    book: com.aura.reader.data.model.Book,
    currentChapter: com.aura.reader.data.model.Chapter?,
    currentChapterIndex: Int,
    totalChapters: Int,
    currentPage: Int,
    totalPages: Int,
    progressPercent: Int,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val coverBitmap = remember(book.coverBase64) {
        book.coverBase64?.let { base64 ->
            try {
                val bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
                android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(strings.close)
            }
        },
        title = {
            Text(
                text = strings.bookInfoTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Book Cover
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .size(width = 120.dp, height = 175.dp)
                        .padding(bottom = 16.dp)
                ) {
                    if (coverBitmap != null) {
                        Image(
                            bitmap = coverBitmap,
                            contentDescription = book.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoStories,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Title
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                // Author
                if (book.author.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = book.author,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Bar & Info Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.progress,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$progressPercent%",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { progressPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Chapter info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = strings.chapters,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${currentChapterIndex + 1} / $totalChapters",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (currentChapter != null && currentChapter.title.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentChapter.title,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (totalPages > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = strings.pages,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${currentPage + 1} / $totalPages",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = strings.format,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = book.format.name.uppercase(),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
