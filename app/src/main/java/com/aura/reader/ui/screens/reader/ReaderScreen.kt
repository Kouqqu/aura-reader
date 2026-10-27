package com.aura.reader.ui.screens.reader

import androidx.compose.foundation.BorderStroke
import com.aura.reader.data.model.ReaderThemeMode
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.ExperimentalSharedTransitionApi
import com.aura.reader.data.model.Book
import com.aura.reader.data.model.Quote
import com.aura.reader.util.BookShareUtils
import androidx.compose.material.icons.filled.Share
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import com.aura.reader.data.model.AppDesignStyle
import com.aura.reader.ui.components.LiquidGlassSurface
import com.aura.reader.ui.components.liquidGlassMenuAppearance
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.Font
import androidx.activity.compose.BackHandler
import android.app.Activity
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.widthIn
import com.aura.reader.R
import com.aura.reader.ui.theme.LocalAppStrings
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SkipNext
import com.aura.reader.data.services.DictionaryService
import com.aura.reader.data.services.WordDefinition
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.TextButton
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aura.reader.data.model.Chapter
import com.aura.reader.data.model.ReaderFontFamily
import com.aura.reader.ui.theme.AuraReaderTheme
import kotlinx.coroutines.launch
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

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
    val savedTextOffset by viewModel.savedTextOffset.collectAsState()
    val savedPixelOffset by viewModel.savedPixelOffset.collectAsState()
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
    val isGlass = settings.appDesignStyle == AppDesignStyle.LIQUID_GLASS
    val isThemeDark = when (settings.themeMode) {
        ReaderThemeMode.LIGHT, ReaderThemeMode.SEPIA -> false
        ReaderThemeMode.DARK, ReaderThemeMode.AMOLED -> true
        ReaderThemeMode.SYSTEM_DYNAMIC -> isSystemInDarkTheme()
    }
    val isSepiaTheme = settings.themeMode == ReaderThemeMode.SEPIA
    val currentChapterQuotes = remember(quotes, book?.id, currentChapterIndex) {
        val bId = book?.id
        if (bId != null) quotes.filter { it.bookId == bId && it.chapterIndex == currentChapterIndex } else emptyList()
    }
    val isCurrentChapterBookmarked = remember(bookmarks, book?.id, currentChapterIndex) {
        book?.id?.let { bId ->
            bookmarks.any { it.bookId == bId && it.chapterIndex == currentChapterIndex }
        } ?: false
    }

    val nextBookInSeries by viewModel.nextBookInSeries.collectAsState()
    var showNextInSeriesDialog by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(true) }
    var showBookInfoDialog by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showTtsSheet by remember { mutableStateOf(false) }
    val isTtsHighlightActive = ttsState.isServiceRunning && !showTtsSheet && ttsState.isPlaying
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
    var lastCopiedText by remember { mutableStateOf<String?>(null) }
    val interceptedClipboardManager = remember(clipboardManager) {
        object : androidx.compose.ui.platform.ClipboardManager {
            override fun getText(): androidx.compose.ui.text.AnnotatedString? = clipboardManager.getText()
            override fun setText(annotatedString: androidx.compose.ui.text.AnnotatedString) {
                lastCopiedText = annotatedString.text
                clipboardManager.setText(annotatedString)
            }
            override fun hasText(): Boolean = clipboardManager.hasText()
        }
    }
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
                lastCopiedText = null
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
        materialYou = materialYouEnabled,
        readerMode = true
    ) {
      CompositionLocalProvider(LocalTextToolbar provides customTextToolbar, LocalClipboardManager provides interceptedClipboardManager) {
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
                "Google Sans" -> FontFamily(Font(R.font.google_sans_regular))
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
                        androidx.compose.runtime.key(book?.id, currentChapterIndex) {
                            ChapterPagingView(
                                chapter = currentChapter,
                                chapterIndex = currentChapterIndex,
                                initialBlockIndex = savedOffset,
                                initialTextOffset = savedTextOffset,
                                overallProgress = book?.progressPercent,
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
                                onFootnoteClick = { ref, content ->
                                    if (content.startsWith("nav:")) {
                                        val parts = content.removePrefix("nav:").split(":")
                                        val ch = parts.getOrNull(0)?.toIntOrNull() ?: 0
                                        val blk = parts.getOrNull(1)?.toIntOrNull() ?: 0
                                        viewModel.setChapter(ch, blk)
                                    } else {
                                        selectedFootnote = ref to content
                                    }
                                },
                                onSaveQuote = { quoteToSave = it },
                                onPageChange = { page, total ->
                                    currentPagingPage = page
                                    totalPagingPages = total
                                },
                                onUpdateProgress = { blockIdx, totalBlocks, textOffset, pixelOffset, atEnd ->
                                    viewModel.updateScrollProgress(blockIdx, totalBlocks, textOffset, pixelOffset, atEnd)
                                },
                                onBookFinished = {
                                    if (nextBookInSeries != null) {
                                        showNextInSeriesDialog = true
                                    }
                                },
                                ttsState = ttsState,
                                isTtsHighlightActive = isTtsHighlightActive
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
                                    initialPixelOffset = if (pageIndex == currentChapterIndex) savedPixelOffset else 0,
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
                                    onFootnoteClick = { ref, content ->
                                    if (content.startsWith("nav:")) {
                                        val parts = content.removePrefix("nav:").split(":")
                                        val ch = parts.getOrNull(0)?.toIntOrNull() ?: 0
                                        val blk = parts.getOrNull(1)?.toIntOrNull() ?: 0
                                        viewModel.setChapter(ch, blk)
                                    } else {
                                        selectedFootnote = ref to content
                                    }
                                },
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
                                    onUpdateProgress = { firstVisible, totalItems, textOffset, pixelOffset, atEnd ->
                                        if (pageIndex == currentChapterIndex) {
                                            viewModel.updateScrollProgress(firstVisible, totalItems, textOffset, pixelOffset, atEnd)
                                        }
                                    },
                                    nextBookInSeries = nextBookInSeries,
                                    onOpenNextBook = { viewModel.openNextBookInSeries(it) },
                                    ttsState = ttsState,
                                    isTtsHighlightActive = isTtsHighlightActive
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
                                    sharedContentState = rememberSharedContentState(key = "book_cover_${currentBook.id}"),
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
                                tint = if (ttsState.isServiceRunning) MaterialTheme.colorScheme.primary else if (isGlass) (if (isThemeDark) Color.White else Color(0xFF191B1F)) else MaterialTheme.colorScheme.onSurface
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
                                tint = if (isCurrentChapterBookmarked) MaterialTheme.colorScheme.primary else if (isGlass) (if (isThemeDark) Color.White else Color(0xFF191B1F)) else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Three-dots Overflow Menu
                        Box {
                            IconButton(onClick = { showReaderMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
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
                    val bottomBarContent: @Composable () -> Unit = {
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
                                            Icon(
                                                Icons.Default.SkipPrevious,
                                                contentDescription = strings.prevChapter,
                                                tint = if (currentChapterIndex > 0) {
                                                    if (isGlass) (if (isThemeDark) Color.White else Color(0xFF191B1F)) else MaterialTheme.colorScheme.onSurface
                                                } else {
                                                    (if (isThemeDark) Color.White else Color.Black).copy(alpha = 0.25f)
                                                }
                                            )
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
                                                    color = if (isGlass) (if (isThemeDark) Color.White else Color(0xFF191B1F)) else MaterialTheme.colorScheme.onSurface,
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
                                            Icon(
                                                Icons.Default.SkipNext,
                                                contentDescription = strings.nextChapter,
                                                tint = if (currentChapterIndex < chapters.size - 1) {
                                                    if (isGlass) (if (isThemeDark) Color.White else Color(0xFF191B1F)) else MaterialTheme.colorScheme.onSurface
                                                } else {
                                                    (if (isThemeDark) Color.White else Color.Black).copy(alpha = 0.25f)
                                                }
                                            )
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
                                                    val target = currentPagingPage - 1
                                                    lastPageSliderStep = target
                                                    if (settings.hapticFeedbackEnabled) {
                                                        performSliderTick(context, haptic)
                                                    }
                                                    requestPagingPage = target
                                                }
                                            },
                                            enabled = currentPagingPage > 0,
                                            modifier = Modifier.size(36.dp)
                                         ) {
                                             Icon(
                                                 Icons.Default.ChevronLeft,
                                                 contentDescription = strings.prevPage,
                                                 tint = if (currentPagingPage > 0) {
                                                     if (isGlass) (if (isThemeDark) Color.White else Color(0xFF191B1F)) else MaterialTheme.colorScheme.onSurface
                                                 } else {
                                                     (if (isThemeDark) Color.White else Color.Black).copy(alpha = 0.25f)
                                                 }
                                             )
                                         }

                                         Slider(
                                             value = currentPagingPage.toFloat().coerceIn(0f, (totalPagingPages - 1).coerceAtLeast(0).toFloat()),
                                             onValueChange = { newVal ->
                                                 val step = newVal.toInt()
                                                 if (step != lastPageSliderStep) {
                                                     lastPageSliderStep = step
                                                     if (settings.hapticFeedbackEnabled) {
                                                         performSliderTick(context, haptic)
                                                     }
                                                 }
                                                 requestPagingPage = step
                                             },
                                             valueRange = 0f..(totalPagingPages - 1).coerceAtLeast(1).toFloat(),
                                             steps = (totalPagingPages - 2).coerceAtLeast(0),
                                             modifier = Modifier.weight(1f),
                                             colors = SliderDefaults.colors(
                                                 thumbColor = MaterialTheme.colorScheme.primary,
                                                 activeTrackColor = MaterialTheme.colorScheme.primary,
                                                 inactiveTrackColor = if (isGlass) {
                                                     if (isThemeDark) Color.White.copy(alpha = 0.18f) else Color.Black.copy(alpha = 0.12f)
                                                 } else MaterialTheme.colorScheme.surfaceContainerHighest
                                             )
                                         )

                                         IconButton(
                                             onClick = {
                                                 if (currentPagingPage < totalPagingPages - 1) {
                                                     val target = currentPagingPage + 1
                                                     lastPageSliderStep = target
                                                     if (settings.hapticFeedbackEnabled) {
                                                         performSliderTick(context, haptic)
                                                     }
                                                     requestPagingPage = target
                                                 }
                                             },
                                             enabled = currentPagingPage < totalPagingPages - 1,
                                             modifier = Modifier.size(36.dp)
                                         ) {
                                             Icon(
                                                 Icons.Default.ChevronRight,
                                                 contentDescription = strings.nextPage,
                                                 tint = if (currentPagingPage < totalPagingPages - 1) {
                                                     if (isGlass) (if (isThemeDark) Color.White else Color(0xFF191B1F)) else MaterialTheme.colorScheme.onSurface
                                                 } else {
                                                     (if (isThemeDark) Color.White else Color.Black).copy(alpha = 0.25f)
                                                 }
                                             )
                                         }
                                    }

                                    Text(
                                        text = "${strings.page} ${currentPagingPage + 1} ${strings.ofPages} $totalPagingPages  •  ${strings.pagingHint}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isGlass) (if (isThemeDark) Color.White.copy(alpha = 0.70f) else Color.Black.copy(alpha = 0.65f)) else MaterialTheme.colorScheme.onSurfaceVariant,
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
                                                color = if (isGlass) (if (isThemeDark) Color.White else Color(0xFF191B1F)) else MaterialTheme.colorScheme.onSurface,
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

                                        val progress = book?.progressPercent ?: 0
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
                                            Icon(
                                                 Icons.Default.ChevronLeft,
                                                 contentDescription = strings.prevChapter,
                                                 tint = if (currentChapterIndex > 0) {
                                                     if (isGlass) (if (isThemeDark) Color.White else Color(0xFF191B1F)) else MaterialTheme.colorScheme.onSurface
                                                 } else {
                                                     (if (isThemeDark) Color.White else Color.Black).copy(alpha = 0.25f)
                                                 }
                                             )
                                        }

                                        Slider(
                                            value = currentChapterIndex.toFloat(),
                                            onValueChange = { newVal ->
                                                val step = newVal.toInt()
                                                if (step != lastChapterSliderStep) {
                                                    lastChapterSliderStep = step
                                                    if (settings.hapticFeedbackEnabled) {
                                                        performSliderTick(context, haptic)
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
                                            Icon(
                                                 Icons.Default.ChevronRight,
                                                 contentDescription = strings.nextChapter,
                                                 tint = if (currentChapterIndex < chapters.size - 1) {
                                                     if (isGlass) (if (isThemeDark) Color.White else Color(0xFF191B1F)) else MaterialTheme.colorScheme.onSurface
                                                 } else {
                                                     (if (isThemeDark) Color.White else Color.Black).copy(alpha = 0.25f)
                                                 }
                                             )
                                        }
                                    }

                                    Text(
                                        text = strings.chaptersNavigationHint,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isGlass) (if (isThemeDark) Color.White.copy(alpha = 0.70f) else Color.Black.copy(alpha = 0.65f)) else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                }
                            }
                        }
                    }

                    if (isGlass) {
                        val bottomBarBg = if (isSepiaTheme) Color(0xF4FAF0E6) else null
                        LiquidGlassSurface(
                            shape = RoundedCornerShape(24.dp),
                            shadowElevation = 10.dp,
                            isDark = isThemeDark,
                            tintColor = bottomBarBg,
                            modifier = Modifier.fillMaxWidth(),
                            content = bottomBarContent
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
                            content = { bottomBarContent() }
                        )
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
                        .padding(bottom = if (showControls) 175.dp else (44.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()))
                ) {
                    TtsControlBar(
                        ttsState = ttsState,
                        strings = strings,
                        onPlayPause = { com.aura.reader.service.BookTtsService.playPause(context) },
                        onPrev = { com.aura.reader.service.BookTtsService.prev(context) },
                        onNext = { com.aura.reader.service.BookTtsService.next(context) },
                        onStop = { com.aura.reader.service.BookTtsService.stop(context) },
                        onSpeedChange = { spd -> com.aura.reader.service.BookTtsService.setSpeed(context, spd) },
                        onClick = { showTtsSheet = true },
                        isGlass = isGlass,
                        isDark = isThemeDark
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
                    onVoiceChange = { voice -> com.aura.reader.service.BookTtsService.setVoice(context, voice) },
                    onPreviewVoice = { voice -> com.aura.reader.service.BookTtsService.previewVoice(context, voice) },
                    onSeekParagraph = { idx -> com.aura.reader.service.BookTtsService.seekParagraph(context, idx) },
                    onHighlightDelayChange = { delay -> com.aura.reader.service.BookTtsService.setHighlightDelay(context, delay) },
                    onToggleWordHighlight = { enabled -> com.aura.reader.service.BookTtsService.setWordHighlightEnabled(context, enabled) },
                    isGlass = isGlass
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
                    onFontNameChange = { viewModel.setFontName(it) },
                    onResetReadingSpeed = { viewModel.resetReadingSpeed() }
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

            val currentBook = book
            if (showBookmarksQuotesSheet && currentBook != null) {
                BookmarksAndQuotesBottomSheet(
                    bookmarks = bookmarks,
                    quotes = quotes,
                    currentBookId = currentBook.id,
                    onDismiss = { showBookmarksQuotesSheet = false },
                    onBookmarkClick = { chIdx, offset ->
                        viewModel.setChapter(chIdx)
                    },
                    onDeleteBookmark = { viewModel.removeBookmark(it) },
                    onAddQuote = { viewModel.addQuote(it, 0xFFFFF59DL) },
                    onDeleteQuote = { viewModel.removeQuote(it) }
                )
            }

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

            val nextBook = nextBookInSeries
            if (showNextInSeriesDialog && nextBook != null) {
                AlertDialog(
                    onDismissRequest = { showNextInSeriesDialog = false },
                    title = {
                        Text(
                            text = if (!currentBook?.series.isNullOrBlank()) "${strings.seriesLabel}: ${currentBook?.series}" else strings.nextInSeriesAction,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text(
                            text = strings.nextInSeriesPrompt(nextBook.title),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showNextInSeriesDialog = false
                                viewModel.openNextBookInSeries(nextBook)
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(strings.nextInSeriesAction)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showNextInSeriesDialog = false }
                        ) {
                            Text(strings.close)
                        }
                    },
                    shape = RoundedCornerShape(20.dp)
                )
            }

            selectedFootnote?.let { (ref, content) ->
                FootnoteDialog(
                    refLabel = ref,
                    content = content,
                    onDismiss = { selectedFootnote = null }
                )
            }

            // Selection toolbar popup (no full-screen scrim so selection handles can be freely dragged)
            val currentSelectionRect = selectionRect
            if (selectedQuoteCopyAction != null && currentSelectionRect != null) {
                val density = LocalDensity.current
                val rect = currentSelectionRect

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
                        dismissOnClickOutside = false
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
                        ),
                        modifier = Modifier.widthIn(max = (LocalConfiguration.current.screenWidthDp.dp - 24.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 6.dp, vertical = 2.dp),
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

                            Box(modifier = Modifier.height(14.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)))

                            // 5. Read from here (TTS)
                            TextButton(
                                onClick = {
                                    val action = selectedQuoteCopyAction
                                    action?.invoke()
                                    val clip = (lastCopiedText ?: clipboardManager.getText()?.text)?.trim()
                                    selectionResetKey++
                                    customTextToolbar.hide()
                                    if (!clip.isNullOrBlank()) {
                                        viewModel.startTts(context, selectedText = clip)
                                    } else {
                                        viewModel.startTts(context)
                                    }
                                    showTtsSheet = true
                                },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(strings.ttsStartFromHere, style = MaterialTheme.typography.labelSmall, maxLines = 1, softWrap = false)
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






private fun performSliderTick(context: Context, haptic: HapticFeedback) {
    try {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && vibrator?.hasVibrator() == true) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    } catch (_: Exception) {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
}
