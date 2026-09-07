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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.List
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
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
    val book by viewModel.currentBook.collectAsState()
    val settings by viewModel.readerSettings.collectAsState()
    val currentChapterIndex by viewModel.currentChapterIndex.collectAsState()
    val savedOffset by viewModel.savedScrollOffset.collectAsState()

    var showControls by remember { mutableStateOf(true) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showChaptersSheet by remember { mutableStateOf(false) }
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
                            IconButton(onClick = { showChaptersSheet = true }) {
                                Icon(Icons.Default.List, contentDescription = "Оглавление")
                            }
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

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (chapters.isNotEmpty()) {
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
                                settings = settings,
                                resolvedFontFamily = resolvedFontFamily,
                                pageIndex = pageIndex,
                                chaptersCount = chapters.size,
                                onToggleControls = { showControls = !showControls },
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
                AnimatedVisibility(
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
                                    text = "Перемещение по главам (или свайп влево/вправо)",
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
                    onFontFamilyChange = { viewModel.setFontFamily(it) }
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
        }
    }
}

@OptIn(
    ExperimentalMaterial3Api::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class
)
@Composable
fun ChapterContentView(
    chapter: Chapter,
    isCurrentChapter: Boolean,
    initialScrollOffset: Int,
    settings: ReaderSettings,
    resolvedFontFamily: FontFamily,
    pageIndex: Int,
    chaptersCount: Int,
    onToggleControls: () -> Unit,
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
                    )
                }
                BlockType.EPIGRAPH -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 48.dp, end = 8.dp, top = 8.dp, bottom = 24.dp),
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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
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
                                    .clip(RoundedCornerShape(16.dp))
                            )
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
                    Text(
                        text = block.text,
                        fontSize = settings.fontSizeSp.sp,
                        lineHeight = (settings.fontSizeSp * settings.lineHeightMultiplier).sp,
                        fontFamily = resolvedFontFamily,
                        textAlign = TextAlign.Start,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = TextStyle(
                            textIndent = TextIndent(firstLine = (settings.fontSizeSp * 1.2f).sp)
                        ),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            }
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
