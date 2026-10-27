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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
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
    val coroutineScope = rememberCoroutineScope()


    var dictionaryWord by remember { mutableStateOf<String?>(null) }
    var dictionaryLoading by remember { mutableStateOf(false) }
    var dictionaryDefinition by remember { mutableStateOf<WordDefinition?>(null) }
    var dictionaryError by remember { mutableStateOf<String?>(null) }

    var translationText by remember { mutableStateOf<String?>(null) }
    var translationLoading by remember { mutableStateOf(false) }
    var translationResult by remember { mutableStateOf<String?>(null) }
    var translationError by remember { mutableStateOf<String?>(null) }
    var translationSourceLang by remember { mutableStateOf("auto") }
    var translationTargetLang by remember(appLanguage.code) { mutableStateOf(appLanguage.code) }

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
    var selectedReaderText by remember { mutableStateOf<String?>(null) }
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
                selectedReaderText = null
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

    fun currentSelectionText(): String? {
        val fromState = selectedReaderText?.trim()
        if (!fromState.isNullOrBlank()) return fromState
        // Safety net: if the selection state is empty, fall back to the old
        // copy-to-read path so quote/dictionary/translation keep working.
        selectedQuoteCopyAction?.invoke()
        return clipboardManager.getText()?.text?.trim()?.takeIf { it.isNotBlank() }
    }

    fun runTranslation(text: String, sourceLang: String, targetLang: String) {
        translationText = text
        translationLoading = true
        translationResult = null
        translationError = null
        coroutineScope.launch {
            DictionaryService.translateText(
                text = text,
                sourceLang = sourceLang,
                targetLang = targetLang
            )
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
                                isTtsHighlightActive = isTtsHighlightActive,
                                onSelectionTextChange = { text ->
                                    if (text.isNotEmpty()) selectedReaderText = text
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
                                    isTtsHighlightActive = isTtsHighlightActive,
                                    onSelectionTextChange = { text ->
                                        if (text.isNotEmpty()) selectedReaderText = text
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

        ReaderTopBar(
            visible = showControls,
            book = book,
            currentChapter = currentChapter,
            searchQuery = searchQuery,
            searchResults = searchResults,
            currentSearchIndex = currentSearchIndex,
            isCurrentChapterBookmarked = isCurrentChapterBookmarked,
            ttsState = ttsState,
            isGlass = isGlass,
            isThemeDark = isThemeDark,
            isSepiaTheme = isSepiaTheme,
            viewModel = viewModel,
            onNavigateBack = onNavigateBack,
            onOpenBookInfo = { showBookInfoDialog = true },
            onOpenTtsSheet = { showTtsSheet = true },
            onOpenBookmarks = { showBookmarksQuotesSheet = true },
            onOpenSettings = { showSettingsSheet = true }
        )


        ReaderBottomBar(
            visible = showControls,
            chapters = chapters,
            currentChapter = currentChapter,
            currentChapterIndex = currentChapterIndex,
            currentPagingPage = currentPagingPage,
            totalPagingPages = totalPagingPages,
            bookProgressPercent = book?.progressPercent ?: 0,
            onRequestPage = { requestPagingPage = it },
            onOpenChapters = { showChaptersSheet = true },
            settings = settings,
            isGlass = isGlass,
            isThemeDark = isThemeDark,
            isSepiaTheme = isSepiaTheme,
            viewModel = viewModel
        )


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
                ReaderSelectionToolbar(
                    anchorRect = currentSelectionRect,
                    onDismissRequest = {
                        selectionResetKey++
                        customTextToolbar.hide()
                    },
                    onCopy = {
                        selectedQuoteCopyAction?.invoke()
                        selectionResetKey++
                        customTextToolbar.hide()
                    },
                    onQuote = {
                        val selection = currentSelectionText()
                        if (!selection.isNullOrBlank()) {
                            quoteToSave = selection.trim()
                        }
                        selectionResetKey++
                        customTextToolbar.hide()
                    },
                    onDictionary = {
                        val clip = currentSelectionText()
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
                    onTranslate = {
                        val clip = currentSelectionText()
                        selectionResetKey++
                        customTextToolbar.hide()
                        if (!clip.isNullOrBlank()) {
                            runTranslation(clip, translationSourceLang, translationTargetLang)
                        }
                    },
                    onReadFromHere = {
                        val clip = currentSelectionText()
                        selectionResetKey++
                        customTextToolbar.hide()
                        if (!clip.isNullOrBlank()) {
                            viewModel.startTts(context, selectedText = clip)
                        } else {
                            viewModel.startTts(context)
                        }
                        showTtsSheet = true
                    }
                )
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
                    sourceLanguage = translationSourceLang,
                    targetLanguage = translationTargetLang,
                    onSourceLanguageChange = { code ->
                        translationSourceLang = code
                        runTranslation(text, code, translationTargetLang)
                    },
                    onTargetLanguageChange = { code ->
                        translationTargetLang = code
                        runTranslation(text, translationSourceLang, code)
                    },
                    onDismiss = { translationText = null }
                )
            }
        }
      }
    }
}
