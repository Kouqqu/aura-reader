package com.aura.reader.ui.screens.reader

import androidx.compose.foundation.BorderStroke
import com.aura.reader.data.model.Quote
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.text.selection.rememberSelectionState
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.runtime.snapshotFlow
import com.aura.reader.ui.theme.LocalAppStrings
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SkipNext
import kotlin.math.roundToInt
import com.aura.reader.data.model.PageTurnAnimation
import com.aura.reader.data.model.TwoColumnMode
import kotlin.math.absoluteValue
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.reader.data.model.BlockType
import com.aura.reader.data.model.Chapter
import com.aura.reader.data.model.FormattedBlock
import com.aura.reader.data.model.ReaderSettings
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

// ------------------------------------------------------------------------------------------------
// PAGING MODE VIEW
// ------------------------------------------------------------------------------------------------

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ChapterPagingView(
    chapter: Chapter,
    chapterIndex: Int,
    initialBlockIndex: Int = 0,
    initialTextOffset: Int = 0,
    overallProgress: Int? = null,
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
    onUpdateProgress: (Int, Int, Int, Int, Boolean) -> Unit,
    onBookFinished: () -> Unit = {},
    ttsState: com.aura.reader.service.TtsState = com.aura.reader.service.TtsState(),
    isTtsHighlightActive: Boolean = false,
    onSelectionTextChange: (String) -> Unit = {}
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

        val reflowAnchor = remember(pages) { initialBlockIndex to initialTextOffset }
        val calculatedInitialPage = remember(pages, isTwoColumn) {
            val page = com.aura.reader.data.model.ReadingPosition.pageForAnchor(pages, reflowAnchor.first, reflowAnchor.second)
            if (isTwoColumn) page / 2 else page
        }

        val initialPagerSpread = prevPageOffset + calculatedInitialPage
        val pagerState = rememberPagerState(
            initialPage = initialPagerSpread.coerceIn(prevPageOffset, (totalPagerSpreads - 1).coerceAtLeast(prevPageOffset)),
            pageCount = { totalPagerSpreads.coerceAtLeast(1) }
        )
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(ttsState.currentOriginalBlockIndex, isTtsHighlightActive) {
            if (isTtsHighlightActive && ttsState.currentOriginalBlockIndex >= 0) {
                val targetBlock = ttsState.currentOriginalBlockIndex
                val targetPageIdx = pages.indexOfFirst { pageList ->
                    pageList.any { it.first == targetBlock }
                }
                if (targetPageIdx != -1) {
                    val targetSpread = if (isTwoColumn) targetPageIdx / 2 else targetPageIdx
                    val targetPagerSpread = targetSpread + prevPageOffset
                    if (pagerState.currentPage != targetPagerSpread && targetPagerSpread in 0 until totalPagerSpreads) {
                        pagerState.animateScrollToPage(targetPagerSpread)
                    }
                }
            }
        }
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
                } else if (!hasNext && settled == totalPagerSpreads - 1) {
                    onBookFinished()
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
                val textOffset = pages.getOrNull(activePageIdx)?.firstOrNull()?.second?.sourceTextOffset ?: 0
                onUpdateProgress(currentBlock, blocks.size.coerceAtLeast(1), textOffset, 0,
                    !hasNext && activePageIdx + (if (isTwoColumn) 1 else 0) >= pages.lastIndex)
            }
        }

        LaunchedEffect(pages, isTwoColumn) {
            val targetPagerSpread = prevPageOffset + calculatedInitialPage
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
                    lastHapticSpread = targetSpread
                    lastHapticOffset = targetPagerSpread.toFloat()
                    pagerState.scrollToPage(targetPagerSpread)
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

        // Auto-turn page as TTS voice narration progresses
        LaunchedEffect(ttsState.currentOriginalBlockIndex, ttsState.currentWordStart, isTtsHighlightActive) {
            if (isTtsHighlightActive && ttsState.currentOriginalBlockIndex >= 0) {
                val targetPageIdx = pages.indexOfFirst { page ->
                    page.any { (origIdx, blk) ->
                        origIdx == ttsState.currentOriginalBlockIndex &&
                        (ttsState.currentWordStart < 0 || ttsState.currentWordStart >= blk.sourceTextOffset)
                    }
                }
                if (targetPageIdx >= 0) {
                    val targetSpread = if (isTwoColumn) targetPageIdx / 2 else targetPageIdx
                    val targetPagerSpread = prevPageOffset + targetSpread
                    if (targetPagerSpread != pagerState.currentPage && targetPagerSpread in prevPageOffset until (prevPageOffset + contentSpreadsCount)) {
                        if (settings.pageAnimation == PageTurnAnimation.INSTANT) {
                            pagerState.scrollToPage(targetPagerSpread)
                        } else {
                            pagerState.animateScrollToPage(targetPagerSpread)
                        }
                    }
                }
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
                        // Each pager page must own its SelectionState: the pager keeps
                        // neighbouring pages composed at the same time, and one state can
                        // only be bound to one SelectionContainer (otherwise the framework
                        // throws "A SelectionState can only be bound to one SelectionContainer").
                        val pageSelectionState = rememberSelectionState()
                        LaunchedEffect(pageSelectionState.selectedTexts) {
                            onSelectionTextChange(
                                pageSelectionState.selectedTexts
                                    .joinToString("\n") { it.text }
                                    .trim()
                            )
                        }
                        SelectionContainer(state = pageSelectionState) {
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
                                        for ((originalIndex, block) in leftPageBlocks) {
                                            val isSpeaking = isTtsHighlightActive && (originalIndex == ttsState.currentOriginalBlockIndex)
                                            val blockOffset = block.sourceTextOffset
                                            val highlightWords = isSpeaking && ttsState.isWordHighlightEnabled
                                            val adjustedStart = if (highlightWords && ttsState.currentWordStart >= 0) ttsState.currentWordStart - blockOffset else -1
                                            val adjustedEnd = if (highlightWords && ttsState.currentWordEnd >= 0) ttsState.currentWordEnd - blockOffset else -1
                                            val isWordInChunk = highlightWords && adjustedStart in 0 until block.text.length
                                            RenderBlock(
                                                block = block,
                                                settings = settings,
                                                resolvedFontFamily = resolvedFontFamily,
                                                searchQuery = searchQuery,
                                                footnotes = footnotes,
                                                quotes = quotes,
                                                ttsWordStart = if (isWordInChunk) adjustedStart else -1,
                                                ttsWordEnd = if (isWordInChunk) adjustedEnd else -1,
                                                ttsSpokenWord = if (isWordInChunk) ttsState.currentSpokenWord else "",
                                                onFootnoteClick = onFootnoteClick,
                                                onNavigateToChapter = { ch, blk -> onFootnoteClick("", "nav:$ch:$blk") },
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
                                        for ((originalIndex, block) in rightPageBlocks) {
                                            val isSpeaking = isTtsHighlightActive && (originalIndex == ttsState.currentOriginalBlockIndex)
                                            val blockOffset = block.sourceTextOffset
                                            val highlightWords = isSpeaking && ttsState.isWordHighlightEnabled
                                            val adjustedStart = if (highlightWords && ttsState.currentWordStart >= 0) ttsState.currentWordStart - blockOffset else -1
                                            val adjustedEnd = if (highlightWords && ttsState.currentWordEnd >= 0) ttsState.currentWordEnd - blockOffset else -1
                                            val isWordInChunk = highlightWords && adjustedStart in 0 until block.text.length
                                            RenderBlock(
                                                block = block,
                                                settings = settings,
                                                resolvedFontFamily = resolvedFontFamily,
                                                searchQuery = searchQuery,
                                                footnotes = footnotes,
                                                quotes = quotes,
                                                ttsWordStart = if (isWordInChunk) adjustedStart else -1,
                                                ttsWordEnd = if (isWordInChunk) adjustedEnd else -1,
                                                ttsSpokenWord = if (isWordInChunk) ttsState.currentSpokenWord else "",
                                                onFootnoteClick = onFootnoteClick,
                                                onNavigateToChapter = { ch, blk -> onFootnoteClick("", "nav:$ch:$blk") },
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
                                    for ((originalIndex, block) in pageBlocks) {
                                        val isSpeaking = isTtsHighlightActive && (originalIndex == ttsState.currentOriginalBlockIndex)
                                        val blockOffset = block.sourceTextOffset
                                        val highlightWords = isSpeaking && ttsState.isWordHighlightEnabled
                                        val adjustedStart = if (highlightWords && ttsState.currentWordStart >= 0) ttsState.currentWordStart - blockOffset else -1
                                        val adjustedEnd = if (highlightWords && ttsState.currentWordEnd >= 0) ttsState.currentWordEnd - blockOffset else -1
                                        val isWordInChunk = highlightWords && adjustedStart in 0 until block.text.length
                                        RenderBlock(
                                            block = block,
                                            settings = settings,
                                            resolvedFontFamily = resolvedFontFamily,
                                            searchQuery = searchQuery,
                                            footnotes = footnotes,
                                            quotes = quotes,
                                            ttsWordStart = if (isWordInChunk) adjustedStart else -1,
                                            ttsWordEnd = if (isWordInChunk) adjustedEnd else -1,
                                            ttsSpokenWord = if (isWordInChunk) ttsState.currentSpokenWord else "",
                                            onFootnoteClick = onFootnoteClick,
                                            onNavigateToChapter = { ch, blk -> onFootnoteClick("", "nav:$ch:$blk") },
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
        val overallPercent = overallProgress ?: (((chapterIndex + intraChapterProgress) / totalChapters.coerceAtLeast(1)) * 100).toInt().coerceIn(0, 100)

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
