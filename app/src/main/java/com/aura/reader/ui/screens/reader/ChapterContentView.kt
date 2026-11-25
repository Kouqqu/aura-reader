package com.aura.reader.ui.screens.reader

import com.aura.reader.data.model.Book
import com.aura.reader.data.model.Quote
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.draw.clip
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.hazeSource
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.text.selection.rememberSelectionState
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.runtime.snapshotFlow
import com.aura.reader.ui.theme.LocalAppStrings
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.isActive
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material3.Surface
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import com.aura.reader.ui.components.jellyOverscroll
import com.aura.reader.data.model.BlockType
import com.aura.reader.data.model.Chapter
import com.aura.reader.data.model.FormattedBlock
import com.aura.reader.data.model.ReaderSettings

// ------------------------------------------------------------------------------------------------
// CONTINUOUS SCROLL VIEW
// ------------------------------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterContentView(
    chapter: Chapter,
    isCurrentChapter: Boolean,
    initialScrollOffset: Int,
    initialPixelOffset: Int = 0,
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
    nextBookInSeries: Book? = null,
    onOpenNextBook: (Book) -> Unit = {},
    onPrevChapter: () -> Unit,
    onNextChapter: () -> Unit,
    onUpdateProgress: (Int, Int, Int, Int, Boolean) -> Unit,
    ttsState: com.aura.reader.service.TtsState = com.aura.reader.service.TtsState(),
    isTtsHighlightActive: Boolean = false,
    onSelectionTextChange: (String) -> Unit = {},
    isAutoScrolling: Boolean = false,
    autoScrollSpeedMultiplier: Float = 1.0f,
    overallProgress: Int? = null,
    userWpm: Float = 200f,
    onBookFinished: () -> Unit = {}
) {
    val selectionState = rememberSelectionState()

    LaunchedEffect(selectionState.selectedTexts) {
        onSelectionTextChange(
            selectionState.selectedTexts.joinToString("\n") { it.text }.trim()
        )
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

    val clampedInitialOffset = remember(initialScrollOffset, blocks.size) {
        if (blocks.isEmpty()) 0 else initialScrollOffset.coerceIn(0, (blocks.size - 1).coerceAtLeast(0))
    }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = clampedInitialOffset, initialFirstVisibleItemScrollOffset = initialPixelOffset.coerceAtLeast(0))

    if (isCurrentChapter) {
        LaunchedEffect(isCurrentChapter, clampedInitialOffset, initialPixelOffset) {
            if (isCurrentChapter && (clampedInitialOffset != listState.firstVisibleItemIndex || (clampedInitialOffset == 0 && initialPixelOffset == 0 && listState.firstVisibleItemScrollOffset > 0))) {
                listState.scrollToItem(clampedInitialOffset, initialPixelOffset.coerceAtLeast(0))
            }
        }

        LaunchedEffect(ttsState.currentOriginalBlockIndex, isTtsHighlightActive) {
            if (isTtsHighlightActive && ttsState.currentOriginalBlockIndex in blocks.indices) {
                listState.animateScrollToItem(ttsState.currentOriginalBlockIndex)
            }
        }

        LaunchedEffect(listState, chapter.id) {
            snapshotFlow {
                val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
                val isAtLastBlock = lastVisible >= blocks.size - 1
                val reachedEnd = isAtLastBlock && !listState.canScrollForward
                Triple(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset, reachedEnd)
            }.collect { (index, pixel, atEnd) ->
                if (blocks.isNotEmpty()) {
                    val isLast = atEnd && pageIndex == chaptersCount - 1
                    onUpdateProgress(index, blocks.size, 0, pixel, isLast)
                    if (isLast && listState.isScrollInProgress && blocks.size > 1) {
                        onBookFinished()
                    }
                }
            }
        }

        LaunchedEffect(targetScrollOffset) {
            if (targetScrollOffset != null) {
                listState.animateScrollToItem(targetScrollOffset.coerceAtLeast(0))
                onConsumeTargetScrollOffset()
            }
        }

        val density = LocalDensity.current
        LaunchedEffect(isAutoScrolling, autoScrollSpeedMultiplier) {
            if (isAutoScrolling) {
                val baseSpeed = 48f * autoScrollSpeedMultiplier
                val speedPx = with(density) { baseSpeed.dp.toPx() }
                var lastTime = 0L
                while (isActive && isAutoScrolling) {
                    withFrameNanos { now ->
                        if (lastTime != 0L) {
                            val dt = (now - lastTime) / 1_000_000_000f
                            val deltaPx = speedPx * dt
                            listState.dispatchRawDelta(deltaPx)
                        }
                        lastTime = now
                    }
                }
            }
        }
    }

    val strings = LocalAppStrings.current
    val statusBarTopInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val cutoutTopInset = WindowInsets.displayCutout.asPaddingValues().calculateTopPadding()
    val topPaddingDp = maxOf(statusBarTopInset, cutoutTopInset, 42.dp) + 6.dp

    val isComicOrManga = remember(blocks) {
        blocks.isNotEmpty() && blocks.all { it.type == BlockType.IMAGE }
    }

    val statusPillHazeState = remember { HazeState() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        androidx.compose.runtime.key(selectionResetKey) {
            SelectionContainer(
                state = selectionState,
                modifier = Modifier.hazeSource(statusPillHazeState)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .jellyOverscroll()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onToggleControls()
                        },
                    contentPadding = PaddingValues(
                        start = if (isComicOrManga) 0.dp else 22.dp,
                        end = if (isComicOrManga) 0.dp else 22.dp,
                        top = topPaddingDp,
                        bottom = if (isComicOrManga) 80.dp else 140.dp
                    )
                ) {
                itemsIndexed(blocks) { blockIndex, block ->
                    val isSpeaking = isTtsHighlightActive && isCurrentChapter && (blockIndex == ttsState.currentOriginalBlockIndex)
                    val highlightWords = isSpeaking && ttsState.isWordHighlightEnabled
                    RenderBlock(
                        block = block,
                        settings = settings,
                        resolvedFontFamily = resolvedFontFamily,
                        searchQuery = searchQuery,
                        footnotes = footnotes,
                        quotes = quotes,
                        ttsWordStart = if (highlightWords) ttsState.currentWordStart else -1,
                        ttsWordEnd = if (highlightWords) ttsState.currentWordEnd else -1,
                        ttsSpokenWord = if (highlightWords) ttsState.currentSpokenWord else "",
                        onFootnoteClick = onFootnoteClick,
                        onNavigateToChapter = { ch, blk -> onFootnoteClick("", "nav:$ch:$blk") },
                        onToggleControls = onToggleControls,
                        onSaveQuote = onSaveQuote,
                        isGraphicPage = isComicOrManga,
                        isInScrollMode = true
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
                                Icons.Rounded.ChevronLeft,
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
                                Icons.Rounded.ChevronRight,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    if (pageIndex == chaptersCount - 1 && nextBookInSeries != null) {
                        val nextBook = nextBookInSeries
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenNextBook(nextBook) }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = strings.nextInSeriesPrompt(nextBook.title),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    FilledTonalButton(
                                        onClick = { onOpenNextBook(nextBook) },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(strings.nextInSeriesAction)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(64.dp))
                }
            }
        }
    }

        if (!isComicOrManga && blocks.isNotEmpty()) {
            val totalChapterChars = remember(blocks) {
                blocks.sumOf { it.text.length.coerceAtLeast(1) }
            }
            val estTotalPages = remember(totalChapterChars) {
                (totalChapterChars / 1400).coerceAtLeast(1)
            }
            val currentIdx = listState.firstVisibleItemIndex.coerceIn(0, (blocks.size - 1).coerceAtLeast(0))
            val atScrollEnd = !listState.canScrollForward && (listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) >= blocks.size - 1
            val readChars = remember(blocks, currentIdx) {
                blocks.take(currentIdx).sumOf { it.text.length.coerceAtLeast(1) }
            }
            val estCurrentPage = if (atScrollEnd) {
                estTotalPages
            } else {
                ((readChars.toFloat() / totalChapterChars.coerceAtLeast(1)) * estTotalPages).toInt().plus(1).coerceIn(1, estTotalPages)
            }
            val remainingWords = remember(blocks, currentIdx) {
                var w = 0
                for (i in currentIdx until blocks.size) {
                    w += blocks[i].text.split(Regex("\\s+")).count { it.isNotBlank() }
                }
                w
            }
            val minutesLeft = (remainingWords / userWpm.coerceAtLeast(80f)).roundToInt().coerceAtLeast(1)
            val fallbackPercent = (((pageIndex + (estCurrentPage.toFloat() / estTotalPages.coerceAtLeast(1))) / chaptersCount.coerceAtLeast(1)) * 100).toInt().coerceIn(0, 100)
            val displayPercent = if (atScrollEnd && pageIndex >= chaptersCount - 1) 100 else (overallProgress ?: fallbackPercent)

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.72f),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(bottom = 18.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .hazeBlur(
                        input = HazeInput.Sources(statusPillHazeState),
                        style = HazeBlurStyle {
                            blurRadius(18.dp)
                            noiseFactor(0.015f)
                        }
                    )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoStories,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "${strings.page} $estCurrentPage/$estTotalPages  •  ~$minutesLeft мин  •  $displayPercent%",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
}
}
