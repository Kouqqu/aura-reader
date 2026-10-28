package com.aura.reader.ui.screens.reader

import com.aura.reader.data.model.Book
import com.aura.reader.data.model.Quote
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    onSelectionTextChange: (String) -> Unit = {}
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
        LaunchedEffect(clampedInitialOffset) {
            if (clampedInitialOffset > 0 && listState.firstVisibleItemIndex == 0) {
                listState.scrollToItem(clampedInitialOffset, initialPixelOffset.coerceAtLeast(0))
            }
        }

        LaunchedEffect(ttsState.currentOriginalBlockIndex, isTtsHighlightActive) {
            if (isTtsHighlightActive && ttsState.currentOriginalBlockIndex in blocks.indices) {
                listState.animateScrollToItem(ttsState.currentOriginalBlockIndex)
            }
        }

        LaunchedEffect(listState, chapter.id) {
            snapshotFlow { Triple(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset,
                listState.layoutInfo.totalItemsCount > 0 && !listState.canScrollForward) }
                .collect { (index, pixel, atEnd) ->
                    if (blocks.isNotEmpty()) onUpdateProgress(index, blocks.size, 0, pixel, atEnd && pageIndex == chaptersCount - 1)
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
        SelectionContainer(state = selectionState) {
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
}
