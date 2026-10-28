package com.aura.reader.ui.screens.opds

import com.aura.reader.ui.components.ParallaxCoverViewer

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
import com.aura.reader.ui.components.WavyProgressIndicator
import androidx.compose.material.icons.rounded.Close

import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.automirrored.rounded.Sort
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.rounded.Person
import com.aura.reader.data.model.OpdsSearchType
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.aura.reader.data.model.Book
import com.aura.reader.data.model.BookFormat
import com.aura.reader.data.model.OpdsBook
import com.aura.reader.data.opds.OpdsUrl
import com.aura.reader.ui.theme.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OpdsScreen(
    viewModel: OpdsViewModel,
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
    val customOpdsEnabled by viewModel.customOpdsEnabled.collectAsState()
    val activeDownloads by viewModel.activeDownloads.collectAsState()
    val downloadedBooks by viewModel.downloadedBooks.collectAsState()
    val selectedBookForDetails by viewModel.selectedBookForDetails.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val canGoBack by viewModel.canGoBack.collectAsState()
    val vpnNoticeDismissed by viewModel.vpnNoticeDismissed.collectAsState()
    val searchScope by viewModel.searchScope.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    var isSearchFocused by remember { mutableStateOf(false) }
    var inspectingCoverBook by remember { mutableStateOf<OpdsBook?>(null) }

    LaunchedEffect(Unit) {
        viewModel.refreshConnectionStatus()
    }

    BackHandler(enabled = true) {
        if (canGoBack) {
            viewModel.navigateBack()
        } else {
            onNavigateBack()
        }
    }

    var showMirrorDialog by remember { mutableStateOf(false) }
    var mirrorInput by remember(baseUrl) { mutableStateOf(baseUrl) }

    // Mirror Settings Dialog
    if (showMirrorDialog) {
        val isUrlValid = OpdsUrl.isValid(mirrorInput)
        AlertDialog(
            onDismissRequest = { showMirrorDialog = false },
            title = { Text(strings.opdsMirrorTitle, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = strings.opdsMirrorSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = mirrorInput,
                        onValueChange = { mirrorInput = it },
                        singleLine = true,
                        isError = mirrorInput.isNotBlank() && !isUrlValid,
                        supportingText = if (mirrorInput.isNotBlank() && !isUrlValid) {
                            { Text(strings.opdsUrlInvalid) }
                        } else {
                            null
                        },
                        label = { Text("URL OPDS") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = isUrlValid,
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
                        viewModel.resetBaseUrl()
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
        OpdsBookDetailsBottomSheet(
            book = book,
            downloadProgress = activeDownloads[book.id],
            downloadedBook = downloadedBooks[book.id],
            onDismiss = { viewModel.selectBookForDetails(null) },
            onCoverClick = { inspectingCoverBook = book },
            onDownload = { format ->
                viewModel.downloadBook(context, book, format)
            },
            onOpenBook = { openedBook ->
                viewModel.selectBookForDetails(null)
                onOpenBook(openedBook)
            },
            onCancelDownload = { viewModel.cancelDownload(book.id) }
        )
    }

    inspectingCoverBook?.let { book ->
        ParallaxCoverViewer(
            book = book,
            onDismiss = { inspectingCoverBook = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = strings.opdsCatalog,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        CatalogConnectionIndicator(status = connectionState)
                    }
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
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = strings.back
                        )
                    }
                },
                actions = {
                    if (customOpdsEnabled) {
                        IconButton(onClick = { showMirrorDialog = true }) {
                            Icon(
                                imageVector = Icons.Rounded.Dns,
                                contentDescription = strings.opdsMirrorTitle,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
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
                        viewModel.clearSearchAndReturnHome()
                    }
                },
                placeholder = {
                    Text(
                        text = strings.opdsSearchHint,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            focusManager.clearFocus()
                            viewModel.clearSearchAndReturnHome()
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.Clear,
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
                        imageVector = Icons.Rounded.History,
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

            // Search Scope Filter Chips (All, Books, Series, Authors)
            val isSearchActive = searchQuery.isNotBlank() || (uiState is OpdsUiState.Success && (uiState as OpdsUiState.Success).searchResult != null)
            AnimatedVisibility(visible = isSearchActive) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = searchScope == OpdsSearchType.ALL,
                        onClick = { viewModel.setSearchScope(OpdsSearchType.ALL) },
                        label = { Text(strings.searchScopeAll) }
                    )
                    FilterChip(
                        selected = searchScope == OpdsSearchType.BOOKS,
                        onClick = { viewModel.setSearchScope(OpdsSearchType.BOOKS) },
                        label = { Text(strings.searchScopeBooks) }
                    )
                    FilterChip(
                        selected = searchScope == OpdsSearchType.SERIES,
                        onClick = { viewModel.setSearchScope(OpdsSearchType.SERIES) },
                        label = { Text(strings.searchScopeSeries) }
                    )
                    FilterChip(
                        selected = searchScope == OpdsSearchType.AUTHORS,
                        onClick = { viewModel.setSearchScope(OpdsSearchType.AUTHORS) },
                        label = { Text(strings.searchScopeAuthors) }
                    )
                }
            }

            // Dismissable VPN Notice Card for built-in catalog in RU
            if (!vpnNoticeDismissed && !customOpdsEnabled) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.vpnNoticeTitle,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = strings.vpnNoticeMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { viewModel.dismissVpnNotice() }
                            ) {
                                Text(strings.vpnNoticeUnderstood, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Sorting Selector Row (only shown when browsing categories or search results)
            if (uiState is OpdsUiState.Success) {
                var showSortDropdown by remember { mutableStateOf(false) }
                val isPop = sortOption == CatalogSortOption.POPULAR_DESC || sortOption == CatalogSortOption.POPULAR_ASC || sortOption == CatalogSortOption.DEFAULT
                val isTitle = sortOption == CatalogSortOption.TITLE_ASC || sortOption == CatalogSortOption.TITLE_DESC
                val isAuthor = sortOption == CatalogSortOption.AUTHOR_ASC || sortOption == CatalogSortOption.AUTHOR_DESC
                val isYear = sortOption == CatalogSortOption.YEAR_DESC || sortOption == CatalogSortOption.YEAR_ASC

                val currentSortLabel = when {
                    isPop -> "${strings.sortByPopularity}: ${if (sortOption == CatalogSortOption.POPULAR_ASC) strings.sortByPopularAsc else strings.sortByPopularDesc}"
                    isTitle -> "${strings.sortByTitle}: ${if (sortOption == CatalogSortOption.TITLE_DESC) "Я → А" else "А → Я"}"
                    isAuthor -> "${strings.sortByAuthor}: ${if (sortOption == CatalogSortOption.AUTHOR_DESC) "Я → А" else "А → Я"}"
                    isYear -> "${strings.sortByYear}: ${if (sortOption == CatalogSortOption.YEAR_ASC) strings.sortByYearAsc else strings.sortByYearDesc}"
                    else -> "${strings.sortByPopularity}: ${strings.sortByPopularDesc}"
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Surface(
                        onClick = { showSortDropdown = true },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.height(38.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Sort,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = currentSortLabel,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Icon(
                                imageVector = Icons.Rounded.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showSortDropdown,
                        onDismissRequest = { showSortDropdown = false },
                        shape = RoundedCornerShape(16.dp),
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        // 1. По читателям / популярности
                        val popIsDesc = sortOption != CatalogSortOption.POPULAR_ASC
                        CatalogSortMenuItem(
                            icon = Icons.Rounded.Star,
                            title = strings.sortByPopularity,
                            directionLabel = if (popIsDesc) strings.sortByPopularDesc else strings.sortByPopularAsc,
                            directionArrow = if (popIsDesc) Icons.Rounded.ArrowDownward else Icons.Rounded.ArrowUpward,
                            isSelected = isPop,
                            onItemClick = {
                                if (isPop) {
                                    viewModel.setSortOption(if (popIsDesc) CatalogSortOption.POPULAR_ASC else CatalogSortOption.POPULAR_DESC)
                                } else {
                                    viewModel.setSortOption(CatalogSortOption.POPULAR_DESC)
                                }
                                showSortDropdown = false
                            },
                            onToggleDirection = {
                                viewModel.setSortOption(if (popIsDesc) CatalogSortOption.POPULAR_ASC else CatalogSortOption.POPULAR_DESC)
                                showSortDropdown = false
                            }
                        )

                        // 2. По названию
                        val titleIsAsc = sortOption != CatalogSortOption.TITLE_DESC
                        CatalogSortMenuItem(
                            icon = Icons.AutoMirrored.Rounded.Sort,
                            title = strings.sortByTitle,
                            directionLabel = if (titleIsAsc) "А → Я" else "Я → А",
                            directionArrow = if (titleIsAsc) Icons.Rounded.ArrowDownward else Icons.Rounded.ArrowUpward,
                            isSelected = isTitle,
                            onItemClick = {
                                if (isTitle) {
                                    viewModel.setSortOption(if (titleIsAsc) CatalogSortOption.TITLE_DESC else CatalogSortOption.TITLE_ASC)
                                } else {
                                    viewModel.setSortOption(CatalogSortOption.TITLE_ASC)
                                }
                                showSortDropdown = false
                            },
                            onToggleDirection = {
                                viewModel.setSortOption(if (titleIsAsc) CatalogSortOption.TITLE_DESC else CatalogSortOption.TITLE_ASC)
                                showSortDropdown = false
                            }
                        )

                        // 3. По автору
                        val authorIsAsc = sortOption != CatalogSortOption.AUTHOR_DESC
                        CatalogSortMenuItem(
                            icon = Icons.Rounded.Person,
                            title = strings.sortByAuthor,
                            directionLabel = if (authorIsAsc) "А → Я" else "Я → А",
                            directionArrow = if (authorIsAsc) Icons.Rounded.ArrowDownward else Icons.Rounded.ArrowUpward,
                            isSelected = isAuthor,
                            onItemClick = {
                                if (isAuthor) {
                                    viewModel.setSortOption(if (authorIsAsc) CatalogSortOption.AUTHOR_DESC else CatalogSortOption.AUTHOR_ASC)
                                } else {
                                    viewModel.setSortOption(CatalogSortOption.AUTHOR_ASC)
                                }
                                showSortDropdown = false
                            },
                            onToggleDirection = {
                                viewModel.setSortOption(if (authorIsAsc) CatalogSortOption.AUTHOR_DESC else CatalogSortOption.AUTHOR_ASC)
                                showSortDropdown = false
                            }
                        )

                        // 4. По году издания
                        val yearIsDesc = sortOption != CatalogSortOption.YEAR_ASC
                        CatalogSortMenuItem(
                            icon = Icons.Rounded.CalendarToday,
                            title = strings.sortByYear,
                            directionLabel = if (yearIsDesc) strings.sortByYearDesc else strings.sortByYearAsc,
                            directionArrow = if (yearIsDesc) Icons.Rounded.ArrowDownward else Icons.Rounded.ArrowUpward,
                            isSelected = isYear,
                            onItemClick = {
                                if (isYear) {
                                    viewModel.setSortOption(if (yearIsDesc) CatalogSortOption.YEAR_ASC else CatalogSortOption.YEAR_DESC)
                                } else {
                                    viewModel.setSortOption(CatalogSortOption.YEAR_DESC)
                                }
                                showSortDropdown = false
                            },
                            onToggleDirection = {
                                viewModel.setSortOption(if (yearIsDesc) CatalogSortOption.YEAR_ASC else CatalogSortOption.YEAR_DESC)
                                showSortDropdown = false
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
                    is OpdsUiState.Loading -> {
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
                    is OpdsUiState.Error -> {
                        OpdsErrorView(
                            isConnectionError = state.isConnectionError,
                            errorMessage = state.message,
                            showMirrorOption = true,
                            onRetry = { viewModel.retry() },
                            onOpenMirrorSettings = { showMirrorDialog = true }
                        )
                    }
                    is OpdsUiState.Success -> {
                        if (state.books.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.MenuBook,
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
                            val res = state.searchResult
                            val isAllSearch = res != null && searchScope == OpdsSearchType.ALL
                            val sortedSearchResultBooks = remember(res?.books, sortOption) {
                                if (res != null) viewModel.getSortedBooks(res.books, sortOption) else emptyList()
                            }
                            val sortedStateBooks = remember(state.books, sortOption) {
                                viewModel.getSortedBooks(state.books, sortOption)
                            }

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (state.currentTitle.isNotBlank()) {
                                    item(key = "search_title_header") {
                                        Text(
                                            text = state.currentTitle,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                    }
                                }

                                if (isAllSearch && res != null) {
                                    // 1. Found Series Section
                                    if (res.series.isNotEmpty()) {
                                        item(key = "header_found_series") {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 8.dp, bottom = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.AutoStories,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = strings.foundSeriesCount(res.series.size),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }

                                        items(res.series, key = { "series_${it.id}_${it.title}" }) { seriesItem ->
                                            OpdsCategoryItemCard(
                                                item = seriesItem,
                                                isSeries = true,
                                                onClick = {
                                                    if (seriesItem.categoryPath != null) {
                                                        viewModel.loadCategory(seriesItem.categoryPath, seriesItem.title)
                                                    }
                                                }
                                            )
                                        }
                                    }

                                    // 2. Found Authors Section
                                    if (res.authors.isNotEmpty()) {
                                        item(key = "header_found_authors") {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 12.dp, bottom = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Person,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = strings.foundAuthorsCount(res.authors.size),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }

                                        items(res.authors, key = { "author_${it.id}_${it.title}" }) { authorItem ->
                                            OpdsCategoryItemCard(
                                                item = authorItem,
                                                isSeries = false,
                                                onClick = {
                                                    if (authorItem.categoryPath != null) {
                                                        viewModel.loadCategory(authorItem.categoryPath, authorItem.title)
                                                    }
                                                }
                                            )
                                        }
                                    }

                                    // 3. Found Books Section
                                    if (res.books.isNotEmpty()) {
                                        item(key = "header_found_books") {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 12.dp, bottom = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = strings.foundBooksCount(res.books.size),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }

                                        itemsIndexed(sortedSearchResultBooks, key = { index, book -> "${book.id}_${book.title}_$index" }) { index, book ->
                                            val progress = activeDownloads[book.id]
                                            val downloaded = downloadedBooks[book.id]

                                            OpdsBookCard(
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
                                                onCoverClick = {
                                                    inspectingCoverBook = book
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
                                } else {
                                    itemsIndexed(sortedStateBooks, key = { index, book -> "${book.id}_${book.title}_$index" }) { index, book ->
                                        val progress = activeDownloads[book.id]
                                        val downloaded = downloadedBooks[book.id]

                                        if (book.isSeries || book.isAuthorCategory) {
                                            OpdsCategoryItemCard(
                                                item = book,
                                                isSeries = book.isSeries,
                                                onClick = {
                                                    if (book.categoryPath != null) {
                                                        viewModel.loadCategory(book.categoryPath, book.title)
                                                    }
                                                }
                                            )
                                        } else {
                                            OpdsBookCard(
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
                                                onCoverClick = {
                                                    inspectingCoverBook = book
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
                        }
                    }
                    OpdsUiState.Idle -> {
                        OpdsHomeView(
                            customOpdsEnabled = customOpdsEnabled,
                            baseUrl = baseUrl,
                            onSelectCategory = { path, title ->
                                viewModel.loadCategory(path, title)
                            },
                            onOpenRootCatalog = {
                                viewModel.loadCategory("", strings.customOpdsOpenRoot)
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OpdsBookCard(
    book: OpdsBook,
    downloadProgress: Int?,
    downloadedBook: Book?,
    onClick: () -> Unit,
    onCoverClick: () -> Unit = {},
    onDownloadFb2: () -> Unit,
    onOpenBook: (Book) -> Unit,
    onCancelDownload: (() -> Unit)? = null
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
                    .size(width = 56.dp, height = 80.dp)
                    .then(
                        if (!book.isCategory) {
                            Modifier.clickable { onCoverClick() }
                        } else Modifier
                    ),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                if (book.isCategory) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.AutoStories,
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
                            imageVector = Icons.AutoMirrored.Rounded.MenuBook,
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        WavyProgressIndicator(
                            progress = downloadProgress / 100f,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "$downloadProgress%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (onCancelDownload != null) {
                            IconButton(
                                onClick = onCancelDownload,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Close,
                                    contentDescription = strings.cancel,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
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
                                        Icons.Rounded.Download,
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
                    imageVector = Icons.Rounded.ChevronRight,
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
                        imageVector = Icons.Rounded.Download,
                        contentDescription = strings.download,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun CatalogConnectionIndicator(status: CatalogConnectionState) {
    val strings = LocalAppStrings.current
    val (color, label) = when (status) {
        CatalogConnectionState.CHECKING -> Color(0xFFFFC107) to strings.opdsStatusChecking
        CatalogConnectionState.ONLINE -> Color(0xFF34C759) to strings.opdsStatusOnline
        CatalogConnectionState.OFFLINE -> Color(0xFFFF3B30) to strings.opdsStatusOffline
    }

    Box(
        modifier = Modifier
            .size(16.dp)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color.copy(alpha = 0.22f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
    }
}

@Composable
fun OpdsErrorView(
    isConnectionError: Boolean,
    errorMessage: String,
    showMirrorOption: Boolean = false,
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
                    imageVector = if (isConnectionError) Icons.Rounded.CloudOff else Icons.Rounded.Refresh,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = if (isConnectionError) strings.opdsConnectionErrorTitle else "Ошибка запроса",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isConnectionError) strings.opdsConnectionErrorSubtitle else errorMessage,
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
            Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(strings.retry)
        }

        if (isConnectionError && showMirrorOption) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onOpenMirrorSettings,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Rounded.Dns, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.opdsMirrorTitle)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OpdsBookDetailsBottomSheet(
    book: OpdsBook,
    downloadProgress: Int?,
    downloadedBook: Book?,
    onDismiss: () -> Unit,
    onCoverClick: () -> Unit = {},
    onDownload: (BookFormat) -> Unit,
    onOpenBook: (Book) -> Unit,
    onCancelDownload: (() -> Unit)? = null
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
                        .size(width = 100.dp, height = 145.dp)
                        .clickable { onCoverClick() },
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
                                imageVector = Icons.AutoMirrored.Rounded.MenuBook,
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
                                        Icons.Rounded.Download,
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = strings.downloading,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$downloadProgress%",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (onCancelDownload != null) {
                            IconButton(
                                onClick = onCancelDownload,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Close,
                                    contentDescription = strings.cancel,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    WavyProgressIndicator(
                        progress = downloadProgress / 100f,
                        modifier = Modifier.fillMaxWidth()
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
                    Icon(Icons.AutoMirrored.Rounded.MenuBook, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.openBookAction, fontWeight = FontWeight.Bold)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(18.dp))
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
                                Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(strings.downloadEpub)
                            }
                        }
                    }
                    if (book.mobiUrl != null || book.pdfUrl != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (book.mobiUrl != null) {
                                OutlinedButton(
                                    onClick = { onDownload(BookFormat.MOBI) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                ) {
                                    Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(strings.opdsDownloadMobi)
                                }
                            }
                            if (book.pdfUrl != null) {
                                OutlinedButton(
                                    onClick = { onDownload(BookFormat.PDF) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                ) {
                                    Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(strings.opdsDownloadPdf)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun OpdsHomeView(
    customOpdsEnabled: Boolean,
    baseUrl: String,
    onSelectCategory: (path: String, title: String) -> Unit,
    onOpenRootCatalog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Hero Welcome Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.AutoStories,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (customOpdsEnabled) strings.customOpdsToggle else strings.catalogHomeTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (customOpdsEnabled) baseUrl else strings.catalogHomeSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (!customOpdsEnabled) {
            // Category Cards Section (Opds curated feeds)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OpdsHomeCategoryCard(
                    emoji = "🔥",
                    title = strings.catNew,
                    subtitle = strings.catNewSubtitle,
                    onClick = { onSelectCategory("/opds/new", strings.catNew) }
                )

                OpdsHomeCategoryCard(
                    emoji = "⭐",
                    title = strings.catPopular,
                    subtitle = strings.catPopularSubtitle,
                    onClick = { onSelectCategory("/opds/pop", strings.catPopular) }
                )

                OpdsHomeCategoryCard(
                    emoji = "👤",
                    title = strings.catAuthors,
                    subtitle = strings.catAuthorsSubtitle,
                    onClick = { onSelectCategory("/opds/authorsindex", strings.catAuthors) }
                )

                OpdsHomeCategoryCard(
                    emoji = "🏷️",
                    title = strings.catGenres,
                    subtitle = strings.catGenresSubtitle,
                    onClick = { onSelectCategory("/opds/genres", strings.catGenres) }
                )
            }
        } else {
            // Custom OPDS mode: open root catalog card
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OpdsHomeCategoryCard(
                    emoji = "📚",
                    title = strings.customOpdsOpenRoot,
                    subtitle = baseUrl,
                    onClick = onOpenRootCatalog
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun OpdsHomeCategoryCard(
    emoji: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
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
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(end = 14.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun OpdsCategoryItemCard(
    item: OpdsBook,
    isSeries: Boolean,
    onClick: () -> Unit
) {
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
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (isSeries) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isSeries) Icons.Rounded.AutoStories else Icons.Rounded.Person,
                        contentDescription = null,
                        tint = if (isSeries) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                val countText = item.itemCount?.let { "$it книг" } ?: ""
                val subtitle = if (countText.isNotBlank()) countText else item.annotation
                if (subtitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}



@Composable
private fun CatalogSortMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    directionLabel: String,
    directionArrow: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onItemClick: () -> Unit,
    onToggleDirection: () -> Unit
) {
    DropdownMenuItem(
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(12.dp))
                Surface(
                    onClick = onToggleDirection,
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = directionLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            imageVector = directionArrow,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        },
        onClick = onItemClick
    )
}
