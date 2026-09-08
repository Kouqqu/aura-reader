package com.aura.reader.ui.screens.library

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.ManageSearch
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.MenuBook
import com.aura.reader.ui.screens.settings.SettingsBottomSheet
import com.aura.reader.ui.theme.LocalAppStrings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aura.reader.R
import com.aura.reader.data.model.Book
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onBookSelected: (Book) -> Unit,
    onOpenFlibusta: () -> Unit
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val recentBooks by viewModel.recentBooks.collectAsState()
    val filteredRecentBooks by viewModel.filteredRecentBooks.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val todayMinutes by viewModel.todayReadingMinutes.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val updateInfo by viewModel.updateInfo.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val foundFiles by viewModel.foundDeviceFiles.collectAsState()
    val readerSettings by viewModel.readerSettings.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val updateNotificationsEnabled by viewModel.updateNotificationsEnabled.collectAsState()
    val readingStatsEnabled by viewModel.readingStatsEnabled.collectAsState()
    val materialYouEnabled by viewModel.materialYouEnabled.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showAddBooksSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var bookToDelete by remember { mutableStateOf<Book?>(null) }

    LaunchedEffect(Unit) {
        viewModel.checkForUpdates(manual = false, context = context)
    }

    // Delete Book Confirmation Dialog
    bookToDelete?.let { book ->
        AlertDialog(
            onDismissRequest = { bookToDelete = null },
            title = { Text(strings.deleteBookTitle, fontWeight = FontWeight.Bold) },
            text = { Text(strings.deleteBookMessage(book.title)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeBook(book.id)
                        bookToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(strings.delete, color = MaterialTheme.colorScheme.onError)
                }
            },
            dismissButton = {
                TextButton(onClick = { bookToDelete = null }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    // Universal file picker: uses Intent.createChooser(ACTION_GET_CONTENT) with */*
    // This allows picking from system DocumentsUI AND third-party file managers (Samsung, Xiaomi, Total Commander, etc.)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uris = mutableListOf<Uri>()
        result.data?.clipData?.let { clipData ->
            for (i in 0 until clipData.itemCount) {
                uris.add(clipData.getItemAt(i).uri)
            }
        } ?: result.data?.data?.let { uris.add(it) }

        if (uris.isNotEmpty()) {
            viewModel.importBooksWithValidation(context, uris)
        }
    }

    val launchFilePicker = {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        filePickerLauncher.launch(Intent.createChooser(intent, strings.selectFiles))
    }

    // SAF OpenDocumentTree folder scanning
    val openFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { treeUri: Uri? ->
        if (treeUri != null) {
            viewModel.scanAndImportFolder(context, treeUri)
        }
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is LibraryUiState.Success -> {
                viewModel.resetUiState()
                onBookSelected(state.book)
            }
            is LibraryUiState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(state.message)
                }
                viewModel.resetUiState()
            }
            else -> Unit
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                AuraLogoIcon(
                                    modifier = Modifier.size(width = 20.dp, height = 24.dp),
                                    bookColor = MaterialTheme.colorScheme.primary,
                                    lineColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            }
                        }
                        Text(
                            text = strings.appName,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    // Settings Sheet
                    IconButton(onClick = { showSettingsSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = strings.settingsTitle
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddBooksSheet = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(strings.addBooks) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (showAddBooksSheet) {
            AddBooksBottomSheet(
                onDismiss = { showAddBooksSheet = false },
                onSelectFiles = { launchFilePicker() },
                onScanFolder = { openFolderLauncher.launch(null) },
                onOpenFlibusta = onOpenFlibusta
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (recentBooks.isEmpty() && uiState !is LibraryUiState.Loading) {
                EmptyLibraryView(
                    onAddBooks = { showAddBooksSheet = true },
                    onOpenSample = { viewModel.openSampleBook() }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Instant Update Notification Banner
                    if (updateInfo != null && updateInfo!!.isAvailable && updateNotificationsEnabled) {
                        item {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SystemUpdate,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = strings.updateAvailableTitle(updateInfo!!.latestVersion),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = strings.updateBannerSubtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            viewModel.startUpdateDownload(context, updateInfo!!.downloadUrl)
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Text(strings.update)
                                    }
                                }
                            }
                        }
                    }

                    // Daily Reading Stats (Toggleable in Settings)
                    if (readingStatsEnabled) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = if (todayMinutes > 0) "${strings.todayReadingTime}: ${strings.minutesRead(todayMinutes)}" else strings.readingStatsEmpty,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }

                    // Search Field
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text(strings.searchHint) },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                        Icon(Icons.Default.Close, contentDescription = strings.clear)
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Text(
                            text = if (searchQuery.isBlank()) strings.recentBooks else strings.searchResultsCount(filteredRecentBooks.size),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                        )
                    }

                    if (filteredRecentBooks.isEmpty() && searchQuery.isNotBlank()) {
                        item {
                            Text(
                                text = strings.noSearchResultsFound(searchQuery),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                    }

                    items(filteredRecentBooks, key = { it.id }) { book ->
                        BookCard(
                            book = book,
                            onClick = {
                                viewModel.openBook(book)
                            },
                            onDelete = {
                                bookToDelete = book
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(72.dp)) // padding for FAB
                    }
                }
            }

            if (downloadProgress != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        tonalElevation = 6.dp,
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = strings.downloadingUpdate,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            val percent = ((downloadProgress ?: 0f) * 100).toInt().coerceIn(0, 100)
                            Text(
                                text = "$percent%",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            LinearProgressIndicator(
                                progress = { downloadProgress ?: 0f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                            )
                        }
                    }
                }
            } else if (uiState is LibraryUiState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    // In-App Update Dialog
    updateInfo?.let { info ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissUpdateDialog() },
            title = {
                Text(
                    text = strings.updateAvailableTitle(info.latestVersion),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = info.changelog,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.dismissUpdateDialog()
                    viewModel.startUpdateDownload(context, info.downloadUrl)
                }) {
                    Text(strings.update)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissUpdateDialog() }) {
                    Text(strings.later)
                }
            }
        )
    }

    if (showSettingsSheet) {
        SettingsBottomSheet(
            currentTheme = readerSettings.themeMode,
            currentLanguage = appLanguage,
            updateNotificationsEnabled = updateNotificationsEnabled,
            readingStatsEnabled = readingStatsEnabled,
            materialYouEnabled = materialYouEnabled,
            onDismiss = { showSettingsSheet = false },
            onThemeChange = { viewModel.setThemeMode(it) },
            onLanguageChange = { viewModel.setAppLanguage(it) },
            onToggleUpdateNotifications = { viewModel.setUpdateNotificationsEnabled(it) },
            onToggleReadingStats = { viewModel.setReadingStatsEnabled(it) },
            onToggleMaterialYou = { viewModel.setMaterialYouEnabled(it) },
            onCheckUpdates = {
                showSettingsSheet = false
                viewModel.checkForUpdates(manual = true, context = context)
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookCard(
    book: Book,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val strings = LocalAppStrings.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onDelete
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BookCoverView(
                coverBase64 = book.coverBase64,
                title = book.title,
                modifier = Modifier
                    .size(width = 64.dp, height = 90.dp)
                    .clip(RoundedCornerShape(10.dp))
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text(
                                text = book.format.name,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }

                if (book.author.isNotBlank()) {
                    Text(
                        text = book.author,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = strings.progressRead,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "${book.progressPercent}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LinearProgressIndicator(
                        progress = { (book.progressPercent / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                }
            }
        }
    }
}

@Composable
fun BookCoverView(
    coverBase64: String?,
    title: String,
    modifier: Modifier = Modifier
) {
    val bitmap = remember(coverBase64) {
        if (!coverBase64.isNullOrBlank()) {
            try {
                val decoded = Base64.decode(coverBase64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
            } catch (e: Exception) {
                null
            }
        } else null
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = title,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Book,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun EmptyLibraryView(
    onAddBooks: () -> Unit,
    onOpenSample: () -> Unit
) {
    val strings = LocalAppStrings.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(28.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.MenuBook,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = strings.emptyLibraryTitle,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = strings.emptyLibrarySubtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onAddBooks,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(strings.addBooks)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onOpenSample,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.AutoStories, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(strings.openSampleBook)
        }
    }
}

@Composable
fun AuraLogoIcon(
    modifier: Modifier = Modifier.size(width = 20.dp, height = 24.dp),
    bookColor: Color = MaterialTheme.colorScheme.primary,
    lineColor: Color = MaterialTheme.colorScheme.primaryContainer
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Book body with rounded corners
        drawRoundRect(
            color = bookColor,
            topLeft = Offset(0f, 0f),
            size = Size(w, h),
            cornerRadius = CornerRadius(w * 0.16f, w * 0.16f)
        )

        // 3 lines representing lines of text on the book
        val left = w * 0.20f
        val rightLong = w * 0.80f
        val rightShort = w * 0.58f
        val lineH = h * 0.085f
        val lineR = CornerRadius(lineH / 2, lineH / 2)

        // Line 1 (top)
        drawRoundRect(
            color = lineColor,
            topLeft = Offset(left, h * 0.26f),
            size = Size(rightLong - left, lineH),
            cornerRadius = lineR
        )
        // Line 2 (middle)
        drawRoundRect(
            color = lineColor,
            topLeft = Offset(left, h * 0.46f),
            size = Size(rightLong - left, lineH),
            cornerRadius = lineR
        )
        // Line 3 (bottom - short)
        drawRoundRect(
            color = lineColor,
            topLeft = Offset(left, h * 0.66f),
            size = Size(rightShort - left, lineH),
            cornerRadius = lineR
        )
    }
}

