package com.aura.reader.ui.screens.library

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.ui.platform.LocalDensity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

    BackHandler {
        if (searchQuery.isNotEmpty()) {
            onSearchQueryChange("")
        } else {
            onBack()
        }
    }

    val keyboardOpen = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val collageSubdued = searchQuery.isNotBlank() || keyboardOpen
    val heroHeight by animateDpAsState(if (collageSubdued) 110.dp else 310.dp, tween(350), label = "searchHeroHeight")
    val titleInset by animateDpAsState(if (collageSubdued) 0.dp else 185.dp, tween(350), label = "searchTitleInset")
    val trimmedQuery = searchQuery.trim()
    val searchResults = remember(trimmedQuery, books) {
        if (trimmedQuery.isBlank()) {
            emptyList()
        } else {
            books.filter { book ->
                book.title.contains(trimmedQuery, ignoreCase = true) ||
                book.author.contains(trimmedQuery, ignoreCase = true) ||
                (book.series?.contains(trimmedQuery, ignoreCase = true) == true) ||
                book.format.name.contains(trimmedQuery, ignoreCase = true) ||
                book.collections.any { it.contains(trimmedQuery, ignoreCase = true) } ||
                book.genres.any { it.contains(trimmedQuery, ignoreCase = true) }
            }
        }
    }

    val availableGenres = remember(books) {
        books.flatMap { it.genres }
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(screenBg)
            .statusBarsPadding()
    ) {
        SearchCoverCollage(books, subdued = collageSubdued, modifier = Modifier.height(heroHeight))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(titleInset))
            // Screen Header: Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 12.dp, start = 4.dp),
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

            // Main Content Area
            if (trimmedQuery.isBlank()) {
                // Recent Search History
                if (searchHistory.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp, bottom = 8.dp, start = 4.dp, end = 4.dp),
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

                // Available Genres Section
                if (availableGenres.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 18.dp, bottom = 8.dp, start = 4.dp, end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.catGenres,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(availableGenres) { genre ->
                            SearchGenreChip(
                                genre = genre,
                                isSelected = false,
                                isDark = isDark,
                                onClick = {
                                    onSearchQueryChange(genre)
                                    onSaveSearchQuery(genre)
                                }
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
                // Quick genre filter strip
                if (availableGenres.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    ) {
                        items(availableGenres) { genre ->
                            val isSelected = trimmedQuery.equals(genre, ignoreCase = true)
                            SearchGenreChip(
                                genre = genre,
                                isSelected = isSelected,
                                isDark = isDark,
                                onClick = {
                                    if (isSelected) {
                                        onSearchQueryChange("")
                                    } else {
                                        onSearchQueryChange(genre)
                                        onSaveSearchQuery(genre)
                                    }
                                }
                            )
                        }
                    }
                }

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

                            Text(
                                text = strings.noSearchResultsFound(trimmedQuery),
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
    genre: String,
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
            Icon(
                imageVector = Icons.Rounded.Category,
                contentDescription = null,
                tint = if (isSelected) contentColor else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = genre,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
