package com.aura.reader.ui.screens.library

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.ui.platform.LocalDensity
import androidx.activity.compose.BackHandler
import com.aura.reader.data.model.GenreClassifier
import com.aura.reader.data.model.MacroGenre
import com.aura.reader.ui.theme.LocalEInkMode
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import com.aura.reader.ui.components.SyncBottomSheetSystemBars
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aura.reader.ui.components.jellyOverscroll
import androidx.compose.ui.unit.sp
import com.aura.reader.data.model.Book
import com.aura.reader.ui.components.LiquidGlassSurface
import com.aura.reader.ui.theme.LocalAppStrings

@Composable
fun LibrarySearchScreen(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    searchHistory: List<String>,
    onSelectHistoryQuery: (String) -> Unit,
    onRemoveHistoryQuery: (String) -> Unit,
    onClearSearchHistory: () -> Unit,
    onSaveSearchQuery: (String) -> Unit,
    books: List<Book>,
    onBookClick: (Book) -> Unit,
    onDeleteBook: (Book) -> Unit,
    onToggleFavorite: (Book) -> Unit,
    onAddToCollection: (Book) -> Unit,
    onSetProgress: (Book, Int) -> Unit,
    onCoverClick: (Book) -> Unit,
    onShare: (Book) -> Unit,
    onChangeCover: (Book) -> Unit,
    onBack: () -> Unit,
    libraryViewMode: String,
    isDark: Boolean,
    clarity: Float,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val isMaterialYou = com.aura.reader.ui.components.LocalMaterialYouEnabled.current
    val isAmoled = isDark && MaterialTheme.colorScheme.background == Color.Black
    val screenBg = if (isDark && isAmoled) {
        Color.Black
    } else {
        MaterialTheme.colorScheme.background
    }

    var selectedGenreFilter by rememberSaveable { mutableStateOf<String?>(null) }
    var showGenresBottomSheet by rememberSaveable { mutableStateOf(false) }

    BackHandler {
        if (selectedGenreFilter != null) {
            selectedGenreFilter = null
        } else if (searchQuery.isNotEmpty()) {
            onSearchQueryChange("")
        } else {
            onBack()
        }
    }

    val keyboardOpen = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val trimmedQuery = searchQuery.trim()
    val isFilteringOrSearching = trimmedQuery.isNotBlank() || selectedGenreFilter != null
    val collageSubdued = isFilteringOrSearching || keyboardOpen
    val heroHeight by animateDpAsState(if (collageSubdued) 100.dp else 320.dp, tween(350), label = "searchHeroHeight")
    val titleInset by animateDpAsState(if (collageSubdued) 0.dp else 165.dp, tween(350), label = "searchTitleInset")

    val searchResults = remember(trimmedQuery, selectedGenreFilter, books) {
        if (!isFilteringOrSearching) {
            emptyList()
        } else {
            books.filter { book ->
                val matchesGenre = selectedGenreFilter == null || GenreClassifier.bookMatchesFilter(book, selectedGenreFilter!!)
                val matchesQuery = if (trimmedQuery.isBlank()) {
                    true
                } else {
                    book.title.contains(trimmedQuery, ignoreCase = true) ||
                    book.author.contains(trimmedQuery, ignoreCase = true) ||
                    (book.series?.contains(trimmedQuery, ignoreCase = true) == true) ||
                    book.format.name.contains(trimmedQuery, ignoreCase = true) ||
                    book.collections.any { it.contains(trimmedQuery, ignoreCase = true) } ||
                    book.genres.any { it.contains(trimmedQuery, ignoreCase = true) }
                }
                matchesGenre && matchesQuery
            }
        }
    }

    val presentMacroGenres = remember(books) {
        books.flatMap { GenreClassifier.getMacroGenresForBook(it.genres) }.distinct()
    }
    val allMacroGenres = remember { MacroGenre.entries.toList() }
    val displayMacroGenres = remember(presentMacroGenres, allMacroGenres) {
        if (presentMacroGenres.isNotEmpty()) presentMacroGenres else allMacroGenres
    }

    val availableDetailedGenres = remember(books, displayMacroGenres) {
        val macroTitles = displayMacroGenres.map { it.title.lowercase() }.toSet()
        books.flatMap { it.genres }
            .map { it.trim() }
            .filter { it.isNotBlank() && it.lowercase() !in macroTitles }
            .distinct()
            .sorted()
    }

    val macroGenreCounts = remember(books, displayMacroGenres) {
        displayMacroGenres.associateWith { macro ->
            books.count { GenreClassifier.bookMatchesFilter(it, macro.title) }
        }
    }

    val detailedGenreCounts = remember(books, availableDetailedGenres) {
        availableDetailedGenres.associateWith { genre ->
            books.count { GenreClassifier.bookMatchesFilter(it, genre) }
        }
    }

    val isEInk = LocalEInkMode.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(screenBg)
    ) {
        if (!isEInk) {
            SearchCoverCollage(books, subdued = collageSubdued, modifier = Modifier.height(heroHeight))
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(titleInset))
            // Screen Header: Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp, start = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.search,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Frosted Glass Search Bar
            LiquidGlassSurface(
                shape = RoundedCornerShape(22.dp),
                isDark = isDark,
                shadowElevation = if (isAmoled) 0.dp else 4.dp,
                tintColor = if (isDark) {
                    if (isAmoled) Color(0x22FFFFFF)
                    else MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.65f)
                } else {
                    Color(0xCCFFFFFF)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = strings.searchLibraryPlaceholder,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isDark) Color.White.copy(alpha = 0.40f) else Color.Black.copy(alpha = 0.40f)
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = if (isDark) Color.White else Color.Black
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    if (searchQuery.isNotBlank()) {
                                        onSaveSearchQuery(searchQuery.trim())
                                    }
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearchQueryChange("") },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = strings.clear,
                                tint = if (isDark) Color.White.copy(alpha = 0.70f) else Color.Black.copy(alpha = 0.60f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Unified Filter Chips Strip: Compact Macro Categories + "Все жанры" Filter Sheet
            if (displayMacroGenres.isNotEmpty() || availableDetailedGenres.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 2.dp)
                ) {
                    // "Все жанры" sheet launcher pill
                    item {
                        val hasActiveFilter = selectedGenreFilter != null
                        val pillBg = if (hasActiveFilter) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else if (isDark) {
                            if (isAmoled) Color(0x1CFFFFFF) else MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.50f)
                        } else {
                            Color(0x95FFFFFF)
                        }
                        val pillContentColor = if (hasActiveFilter) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }

                        LiquidGlassSurface(
                            shape = RoundedCornerShape(16.dp),
                            isDark = isDark,
                            shadowElevation = if (isAmoled) 0.dp else if (hasActiveFilter) 3.dp else 1.dp,
                            tintColor = pillBg,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { showGenresBottomSheet = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Tune,
                                    contentDescription = strings.genresSectionTitle,
                                    tint = if (hasActiveFilter) pillContentColor else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = strings.genresSectionTitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (hasActiveFilter) FontWeight.SemiBold else FontWeight.Normal,
                                    color = pillContentColor
                                )
                            }
                        }
                    }

                    // If a custom/detailed genre filter is selected, pin it right after the button
                    val isMacroSelected = displayMacroGenres.any { it.title.equals(selectedGenreFilter, ignoreCase = true) }
                    if (selectedGenreFilter != null && !isMacroSelected) {
                        item {
                            SearchActiveFilterChip(
                                title = selectedGenreFilter!!,
                                isDark = isDark,
                                onClear = { selectedGenreFilter = null }
                            )
                        }
                    }

                    // Macro Genre chips (only ~5-7 macro categories with emojis!)
                    items(displayMacroGenres) { macro ->
                        val isSelected = selectedGenreFilter.equals(macro.title, ignoreCase = true)
                        SearchGenreChip(
                            title = macro.title,
                            iconEmoji = macro.iconEmoji,
                            isSelected = isSelected,
                            isDark = isDark,
                            onClick = {
                                selectedGenreFilter = if (isSelected) null else macro.title
                            }
                        )
                    }
                }
            }

            // Main Content Area
            if (!isFilteringOrSearching) {
                // Recent Search History
                if (searchHistory.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 8.dp, start = 4.dp, end = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.searchHistory,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        TextButton(onClick = onClearSearchHistory) {
                            Text(
                                text = strings.clearSearchHistory,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(searchHistory) { itemQuery ->
                            SearchHistoryChip(
                                query = itemQuery,
                                isDark = isDark,
                                onClick = { onSelectHistoryQuery(itemQuery) },
                                onRemove = { onRemoveHistoryQuery(itemQuery) }
                            )
                        }
                    }
                }

                // Centered Empty State Prompt
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        LiquidGlassSurface(
                            shape = CircleShape,
                            isDark = isDark,
                            shadowElevation = if (isAmoled) 0.dp else 4.dp,
                            tintColor = if (isDark) {
                                if (isAmoled) Color(0x20FFFFFF) else MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.50f)
                            } else {
                                Color(0xD0FFFFFF)
                            },
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = strings.search,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = strings.searchPromptSubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Search Results
                if (searchResults.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(bottom = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            LiquidGlassSurface(
                                shape = CircleShape,
                                isDark = isDark,
                                shadowElevation = if (isAmoled) 0.dp else 4.dp,
                                tintColor = if (isDark) {
                                    if (isAmoled) Color(0x20FFFFFF) else MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.50f)
                                } else {
                                    Color(0xD0FFFFFF)
                                },
                                modifier = Modifier.size(80.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.SearchOff,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            val emptyLabel = if (trimmedQuery.isNotBlank() && selectedGenreFilter != null) {
                                strings.noSearchResultsFound("$trimmedQuery (${selectedGenreFilter})")
                            } else if (trimmedQuery.isNotBlank()) {
                                strings.noSearchResultsFound(trimmedQuery)
                            } else {
                                strings.noSearchResultsFound(selectedGenreFilter ?: "")
                            }

                            Text(
                                text = emptyLabel,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = strings.searchPromptSubtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 8.dp, start = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.searchResultsCount(searchResults.size),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(if (libraryViewMode == "GRID") 2 else 1),
                        modifier = Modifier
                            .fillMaxSize()
                            .jellyOverscroll(),
                        contentPadding = PaddingValues(top = 4.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(
                            items = searchResults,
                            key = { _, book -> book.id }
                        ) { index, book ->
                            if (libraryViewMode == "GRID") {
                                BookGridCard(
                                    book = book,
                                    index = index,
                                    isGlass = true,
                                    isDark = isDark,
                                    onClick = {
                                        onSaveSearchQuery(trimmedQuery)
                                        onBookClick(book)
                                    },
                                    onDelete = { onDeleteBook(book) },
                                    onToggleFavorite = { onToggleFavorite(book) },
                                    onAddToCollection = { onAddToCollection(book) },
                                    onSetProgress = { prog -> onSetProgress(book, prog) },
                                    onCoverClick = { onCoverClick(book) },
                                    onShare = { onShare(book) },
                                    onChangeCover = { onChangeCover(book) }
                                )
                            } else {
                                BookCard(
                                    book = book,
                                    index = index,
                                    isGlass = true,
                                    isDark = isDark,
                                    onClick = {
                                        onSaveSearchQuery(trimmedQuery)
                                        onBookClick(book)
                                    },
                                    onDelete = { onDeleteBook(book) },
                                    onToggleFavorite = { onToggleFavorite(book) },
                                    onAddToCollection = { onAddToCollection(book) },
                                    onSetProgress = { prog -> onSetProgress(book, prog) },
                                    onCoverClick = { onCoverClick(book) },
                                    onShare = { onShare(book) },
                                    onChangeCover = { onChangeCover(book) }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showGenresBottomSheet) {
            SearchGenresBottomSheet(
                onDismiss = { showGenresBottomSheet = false },
                selectedGenre = selectedGenreFilter,
                onSelectGenre = { selectedGenreFilter = it },
                macroGenres = displayMacroGenres,
                macroCounts = macroGenreCounts,
                detailedGenres = availableDetailedGenres,
                detailedCounts = detailedGenreCounts,
                isDark = isDark
            )
        }
    }
}

@Composable
private fun SearchHistoryChip(
    query: String,
    isDark: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    val isAmoled = isDark && MaterialTheme.colorScheme.background == Color.Black
    LiquidGlassSurface(
        shape = RoundedCornerShape(16.dp),
        isDark = isDark,
        shadowElevation = if (isAmoled) 0.dp else 2.dp,
        tintColor = if (isDark) {
            if (isAmoled) Color(0x1CFFFFFF)
            else MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.50f)
        } else {
            Color(0x95FFFFFF)
        },
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = query,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun SearchGenreChip(
    title: String,
    iconEmoji: String? = null,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val isAmoled = isDark && MaterialTheme.colorScheme.background == Color.Black
    val surfaceColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else if (isDark) {
        if (isAmoled) Color(0x1CFFFFFF) else MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.50f)
    } else {
        Color(0x95FFFFFF)
    }
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    LiquidGlassSurface(
        shape = RoundedCornerShape(16.dp),
        isDark = isDark,
        shadowElevation = if (isAmoled) 0.dp else if (isSelected) 3.dp else 1.dp,
        tintColor = surfaceColor,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            } else if (!iconEmoji.isNullOrBlank()) {
                Text(
                    text = iconEmoji,
                    fontSize = 13.sp,
                    lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
            } else {
                Icon(
                    imageVector = Icons.Rounded.Category,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SearchActiveFilterChip(
    title: String,
    isDark: Boolean,
    onClear: () -> Unit
) {
    val isAmoled = isDark && MaterialTheme.colorScheme.background == Color.Black
    LiquidGlassSurface(
        shape = RoundedCornerShape(16.dp),
        isDark = isDark,
        shadowElevation = if (isAmoled) 0.dp else 2.dp,
        tintColor = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.clip(RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.FilterList,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
                onClick = onClear,
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun SearchGenresBottomSheet(
    onDismiss: () -> Unit,
    selectedGenre: String?,
    onSelectGenre: (String?) -> Unit,
    macroGenres: List<MacroGenre>,
    macroCounts: Map<MacroGenre, Int>,
    detailedGenres: List<String>,
    detailedCounts: Map<String, Int>,
    isDark: Boolean
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val strings = LocalAppStrings.current
    var query by remember { mutableStateOf("") }
    val trimmed = query.trim().lowercase()

    val filteredMacro = remember(macroGenres, trimmed) {
        if (trimmed.isEmpty()) macroGenres else macroGenres.filter { it.title.lowercase().contains(trimmed) }
    }
    val filteredDetailed = remember(detailedGenres, trimmed) {
        if (trimmed.isEmpty()) detailedGenres else detailedGenres.filter { it.lowercase().contains(trimmed) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier
            .statusBarsPadding()
            .padding(top = 24.dp)
    ) {
        SyncBottomSheetSystemBars(isThemeDark = isDark)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            // Header: Title + Reset Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = strings.genresSectionTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Фильтрация книг по жанру",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (selectedGenre != null) {
                    TextButton(onClick = {
                        onSelectGenre(null)
                        onDismiss()
                    }) {
                        Text(
                            text = "Сбросить",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Quick Search input inside sheet if there are many genres
            if (detailedGenres.size > 8) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .padding(bottom = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (query.isEmpty()) {
                                Text(
                                    text = "Найти жанр...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                            BasicTextField(
                                value = query,
                                onValueChange = { query = it },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (query.isNotEmpty()) {
                            IconButton(
                                onClick = { query = "" },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Scrollable genre chips content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (filteredMacro.isNotEmpty()) {
                    Text(
                        text = "Основные категории",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        filteredMacro.forEach { macro ->
                            val isSelected = selectedGenre.equals(macro.title, ignoreCase = true)
                            val count = macroCounts[macro] ?: 0
                            val countSuffix = if (count > 0) " ($count)" else ""
                            SearchGenreChip(
                                title = "${macro.title}$countSuffix",
                                iconEmoji = macro.iconEmoji,
                                isSelected = isSelected,
                                isDark = isDark,
                                onClick = {
                                    onSelectGenre(if (isSelected) null else macro.title)
                                    onDismiss()
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (filteredDetailed.isNotEmpty()) {
                    Text(
                        text = "Все жанры (${filteredDetailed.size})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        filteredDetailed.forEach { detailed ->
                            val isSelected = selectedGenre.equals(detailed, ignoreCase = true)
                            val count = detailedCounts[detailed] ?: 0
                            val countSuffix = if (count > 0) " ($count)" else ""
                            SearchGenreChip(
                                title = "$detailed$countSuffix",
                                iconEmoji = null,
                                isSelected = isSelected,
                                isDark = isDark,
                                onClick = {
                                    onSelectGenre(if (isSelected) null else detailed)
                                    onDismiss()
                                }
                            )
                        }
                    }
                }

                if (filteredMacro.isEmpty() && filteredDetailed.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ничего не найдено",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
