package com.aura.reader.ui.screens.flibusta

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.aura.reader.data.model.Book
import com.aura.reader.data.model.BookFormat
import com.aura.reader.data.model.FlibustaBook
import com.aura.reader.ui.theme.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FlibustaScreen(
    viewModel: FlibustaViewModel,
    onNavigateBack: () -> Unit,
    onOpenBook: (Book) -> Unit
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val focusManager = LocalFocusManager.current

    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val baseUrl by viewModel.baseUrl.collectAsState()
    val activeDownloads by viewModel.activeDownloads.collectAsState()
    val downloadedBooks by viewModel.downloadedBooks.collectAsState()
    val selectedBookForDetails by viewModel.selectedBookForDetails.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val canGoBack by viewModel.canGoBack.collectAsState()
    var isSearchFocused by remember { mutableStateOf(false) }

    BackHandler(enabled = canGoBack) {
        viewModel.navigateBack()
    }

    var showMirrorDialog by remember { mutableStateOf(false) }
    var mirrorInput by remember(baseUrl) { mutableStateOf(baseUrl) }

    // Mirror Settings Dialog
    if (showMirrorDialog) {
        AlertDialog(
            onDismissRequest = { showMirrorDialog = false },
            title = { Text(strings.flibustaMirrorTitle, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = strings.flibustaMirrorSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = mirrorInput,
                        onValueChange = { mirrorInput = it },
                        singleLine = true,
                        label = { Text("URL OPDS") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setBaseUrl(mirrorInput)
                        showMirrorDialog = false
                    }
                ) {
                    Text(strings.save)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mirrorInput = "http://flibusta.is/opds"
                        viewModel.setBaseUrl(mirrorInput)
                        showMirrorDialog = false
                    }
                ) {
                    Text(strings.reset)
                }
            }
        )
    }

    // Book Details Bottom Sheet
    selectedBookForDetails?.let { book ->
        FlibustaBookDetailsBottomSheet(
            book = book,
            downloadProgress = activeDownloads[book.id],
            downloadedBook = downloadedBooks[book.id],
            onDismiss = { viewModel.selectBookForDetails(null) },
            onDownload = { format ->
                viewModel.downloadBook(context, book, format)
            },
            onOpenBook = { openedBook ->
                viewModel.selectBookForDetails(null)
                onOpenBook(openedBook)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = strings.flibustaCatalog,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (canGoBack) {
                            viewModel.navigateBack()
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.back
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showMirrorDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = strings.flibustaMirrorTitle,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input Field (Redesigned Pill Shape)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { newQuery ->
                    viewModel.setSearchQuery(newQuery)
                    if (newQuery.isEmpty()) {
                        viewModel.clearSearchAndReturnHome(strings.catNew)
                    }
                },
                placeholder = {
                    Text(
                        text = strings.flibustaSearchHint,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            focusManager.clearFocus()
                            viewModel.clearSearchAndReturnHome(strings.catNew)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = strings.clear,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                textStyle = MaterialTheme.typography.bodyMedium,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        focusManager.clearFocus()
                        viewModel.search()
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .onFocusChanged { isSearchFocused = it.isFocused }
            )

            // Search History Chips (visible only when search bar is focused)
            AnimatedVisibility(visible = isSearchFocused && searchQuery.isEmpty() && searchHistory.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    for (query in searchHistory) {
                        AssistChip(
                            onClick = {
                                viewModel.setSearchQuery(query)
                                viewModel.search(query)
                            },
                            label = { Text(query) }
                        )
                    }
                    TextButton(onClick = { viewModel.clearHistory() }) {
                        Text(strings.clear, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Catalog Home Welcome Banner
            if (searchQuery.isEmpty() && !canGoBack) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Text(
                            text = strings.catalogHomeTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = strings.catalogHomeSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Quick Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = false,
                    onClick = { viewModel.loadCategory("/opds/new", strings.catNew) },
                    label = { Text(strings.catNew) }
                )
                FilterChip(
                    selected = false,
                    onClick = { viewModel.loadCategory("/opds/pop", strings.catPopular) },
                    label = { Text(strings.catPopular) }
                )
                FilterChip(
                    selected = false,
                    onClick = { viewModel.loadCategory("/opds/authorsindex", strings.catAuthors) },
                    label = { Text(strings.catAuthors) }
                )
                FilterChip(
                    selected = false,
                    onClick = { viewModel.loadCategory("/opds/genres", strings.catGenres) },
                    label = { Text(strings.catGenres) }
                )
            }

            // Sorting & Language Selector Row
            var showSortDropdown by remember { mutableStateOf(false) }
            var showLangDropdown by remember { mutableStateOf(false) }
            val currentSortLabel = when (sortOption) {
                CatalogSortOption.DEFAULT -> strings.sortByDefault
                CatalogSortOption.POPULAR_DESC -> strings.sortByPopularDesc
                CatalogSortOption.POPULAR_ASC -> strings.sortByPopularAsc
                CatalogSortOption.TITLE_ASC -> strings.sortByTitleAsc
                CatalogSortOption.TITLE_DESC -> strings.sortByTitleDesc
                CatalogSortOption.AUTHOR_ASC -> strings.sortByAuthorAsc
                CatalogSortOption.AUTHOR_DESC -> strings.sortByAuthorDesc
                CatalogSortOption.YEAR_DESC -> strings.sortByYearDesc
                CatalogSortOption.YEAR_ASC -> strings.sortByYearAsc
            }
            val currentLangLabel = when (selectedLanguage?.lowercase()) {
                "ru" -> strings.langRussian
                "en" -> strings.langEnglish
                "uk" -> strings.langUkrainian
                "be" -> strings.langBelarusian
                "pl" -> strings.langPolish
                "other" -> strings.langOther
                else -> strings.allLanguages
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sort Dropdown
                Box {
                    FilterChip(
                        selected = sortOption != CatalogSortOption.DEFAULT,
                        onClick = { showSortDropdown = true },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "${strings.sortTitle}: $currentSortLabel ▾",
                                maxLines = 1
                            )
                        }
                    )

                    DropdownMenu(
                        expanded = showSortDropdown,
                        onDismissRequest = { showSortDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(strings.sortByDefault) },
                            onClick = {
                                viewModel.setSortOption(CatalogSortOption.DEFAULT)
                                showSortDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(strings.sortByPopularDesc) },
                            onClick = {
                                viewModel.setSortOption(CatalogSortOption.POPULAR_DESC)
                                showSortDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(strings.sortByPopularAsc) },
                            onClick = {
                                viewModel.setSortOption(CatalogSortOption.POPULAR_ASC)
                                showSortDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(strings.sortByTitleAsc) },
                            onClick = {
                                viewModel.setSortOption(CatalogSortOption.TITLE_ASC)
                                showSortDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(strings.sortByTitleDesc) },
                            onClick = {
                                viewModel.setSortOption(CatalogSortOption.TITLE_DESC)
                                showSortDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(strings.sortByAuthorAsc) },
                            onClick = {
                                viewModel.setSortOption(CatalogSortOption.AUTHOR_ASC)
                                showSortDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(strings.sortByAuthorDesc) },
                            onClick = {
                                viewModel.setSortOption(CatalogSortOption.AUTHOR_DESC)
                                showSortDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(strings.sortByYearDesc) },
                            onClick = {
                                viewModel.setSortOption(CatalogSortOption.YEAR_DESC)
                                showSortDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(strings.sortByYearAsc) },
                            onClick = {
                                viewModel.setSortOption(CatalogSortOption.YEAR_ASC)
                                showSortDropdown = false
                            }
                        )
                    }
                }

                // Language Filter Dropdown
                Box {
                    FilterChip(
                        selected = selectedLanguage != null,
                        onClick = { showLangDropdown = true },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "${strings.filterLanguage}: $currentLangLabel ▾",
                                maxLines = 1
                            )
                        }
                    )

                    DropdownMenu(
                        expanded = showLangDropdown,
                        onDismissRequest = { showLangDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(strings.allLanguages) },
                            onClick = {
                                viewModel.setSelectedLanguage(null)
                                showLangDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(strings.langRussian) },
                            onClick = {
                                viewModel.setSelectedLanguage("ru")
                                showLangDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(strings.langEnglish) },
                            onClick = {
                                viewModel.setSelectedLanguage("en")
                                showLangDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(strings.langUkrainian) },
                            onClick = {
                                viewModel.setSelectedLanguage("uk")
                                showLangDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(strings.langBelarusian) },
                            onClick = {
                                viewModel.setSelectedLanguage("be")
                                showLangDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(strings.langPolish) },
                            onClick = {
                                viewModel.setSelectedLanguage("pl")
                                showLangDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(strings.langOther) },
                            onClick = {
                                viewModel.setSelectedLanguage("other")
                                showLangDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Main Content Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (val state = uiState) {
                    is FlibustaUiState.Loading -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = strings.loading,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    is FlibustaUiState.Error -> {
                        FlibustaErrorView(
                            isConnectionError = state.isConnectionError,
                            errorMessage = state.message,
                            onRetry = { viewModel.retry() },
                            onOpenMirrorSettings = { showMirrorDialog = true }
                        )
                    }
                    is FlibustaUiState.Success -> {
                        if (state.books.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = strings.noBooksFound,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        } else {
                            val sortedBooks = remember(state.books, sortOption, selectedLanguage) {
                                viewModel.getSortedBooks(state.books, sortOption, selectedLanguage)
                            }

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (state.currentTitle.isNotBlank()) {
                                    item {
                                        Text(
                                            text = state.currentTitle,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                    }
                                }

                                itemsIndexed(sortedBooks, key = { index, book -> "${book.id}_${book.title}_$index" }) { index, book ->
                                    val progress = activeDownloads[book.id]
                                    val downloaded = downloadedBooks[book.id]

                                    FlibustaBookCard(
                                        book = book,
                                        downloadProgress = progress,
                                        downloadedBook = downloaded,
                                        onClick = {
                                            if (book.isCategory && book.categoryPath != null) {
                                                viewModel.loadCategory(book.categoryPath, book.title)
                                            } else {
                                                viewModel.selectBookForDetails(book)
                                            }
                                        },
                                        onDownloadFb2 = {
                                            viewModel.downloadBook(context, book, BookFormat.FB2)
                                        },
                                        onOpenBook = { b ->
                                            onOpenBook(b)
                                        }
                                    )
                                }
                            }
                        }
                    }
                    FlibustaUiState.Idle -> {
                        // Empty idle state
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlibustaBookCard(
    book: FlibustaBook,
    downloadProgress: Int?,
    downloadedBook: Book?,
    onClick: () -> Unit,
    onDownloadFb2: () -> Unit,
    onOpenBook: (Book) -> Unit
) {
    val strings = LocalAppStrings.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Book cover thumbnail or Category icon
            Surface(
                modifier = Modifier
                    .size(width = 56.dp, height = 80.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                if (book.isCategory) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AutoStories,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                } else if (!book.coverUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(book.coverUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = book.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Book Information
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (book.author.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = book.author,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (book.annotation.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = book.annotation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Download Progress or Format Badges
                Spacer(modifier = Modifier.height(6.dp))
                if (downloadProgress != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { downloadProgress / 100f },
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "$downloadProgress%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else if (!book.isCategory) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (book.fb2Url != null) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                            ) {
                                Text(
                                    text = "FB2",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (book.epubUrl != null) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                            ) {
                                Text(
                                    text = "EPUB",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (book.downloadSize != null) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = book.downloadSize,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (book.downloadsCount > 0) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Download,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "${book.downloadsCount}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }
                        if (!book.year.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = strings.yearLabel(book.year),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (!book.language.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = book.language,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action Button
            if (book.isCategory) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (downloadedBook != null) {
                Button(
                    onClick = { onOpenBook(downloadedBook) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(strings.openBookAction, style = MaterialTheme.typography.labelMedium)
                }
            } else if (downloadProgress == null) {
                IconButton(onClick = onDownloadFb2) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = strings.download,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun FlibustaErrorView(
    isConnectionError: Boolean,
    errorMessage: String,
    onRetry: () -> Unit,
    onOpenMirrorSettings: () -> Unit
) {
    val strings = LocalAppStrings.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isConnectionError) Icons.Default.CloudOff else Icons.Default.Refresh,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = if (isConnectionError) strings.flibustaConnectionErrorTitle else "Ошибка запроса",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isConnectionError) strings.flibustaConnectionErrorSubtitle else errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(strings.retry)
        }

        if (isConnectionError) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onOpenMirrorSettings,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Dns, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.flibustaMirrorTitle)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FlibustaBookDetailsBottomSheet(
    book: FlibustaBook,
    downloadProgress: Int?,
    downloadedBook: Book?,
    onDismiss: () -> Unit,
    onDownload: (BookFormat) -> Unit,
    onOpenBook: (Book) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val strings = LocalAppStrings.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Large Cover
                Surface(
                    modifier = Modifier
                        .size(width = 100.dp, height = 145.dp),
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 4.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    if (!book.coverUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(book.coverUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = book.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }

                // Title, Author, Info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (book.author.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = book.author,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        if (!book.year.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = strings.yearLabel(book.year),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        if (book.downloadSize != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = book.downloadSize,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        if (book.downloadsCount > 0) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Download,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp),
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = strings.downloadsCount(book.downloadsCount),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }
                        if (!book.language.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = book.language,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Annotation
            if (book.annotation.isNotBlank()) {
                Text(
                    text = strings.annotation,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = book.annotation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Download Controls / Open Book Button
            if (downloadProgress != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = strings.downloading,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "$downloadProgress%",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { downloadProgress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else if (downloadedBook != null) {
                Button(
                    onClick = { onOpenBook(downloadedBook) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.openBookAction, fontWeight = FontWeight.Bold)
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (book.fb2Url != null) {
                        Button(
                            onClick = { onDownload(BookFormat.FB2) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.downloadFb2)
                        }
                    }
                    if (book.epubUrl != null) {
                        OutlinedButton(
                            onClick = { onDownload(BookFormat.EPUB) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.downloadEpub)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
