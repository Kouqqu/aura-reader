package com.aura.reader.ui.screens.library
import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInHorizontally

import androidx.compose.runtime.CompositionLocalProvider
import com.aura.reader.ui.components.LocalHazeState
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import com.aura.reader.ui.components.expressiveClickable
import com.aura.reader.ui.components.jellyOverscroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed
import androidx.compose.material3.FabPosition
import com.aura.reader.data.model.AppDesignStyle
import com.aura.reader.data.model.ReaderThemeMode
import com.aura.reader.ui.components.FloatingNavigationIsland
import com.aura.reader.ui.components.FloatingAddGlassButton
import com.aura.reader.ui.components.LiquidGlassFilterChip
import com.aura.reader.ui.components.LiquidGlassCard
import com.aura.reader.ui.components.liquidGlassMenuAppearance
import com.aura.reader.ui.components.ParallaxCoverViewer
import com.aura.reader.util.BookShareUtils
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material3.HorizontalDivider
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.animation.core.tween
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.horizontalScroll
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.toMutableStateList
import android.content.Intent
import android.widget.Toast
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.Timer
import com.aura.reader.ui.screens.settings.SettingsBottomSheet
import com.aura.reader.ui.screens.reader.ReaderSettingsBottomSheet
import com.aura.reader.ui.theme.LocalAppStrings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import com.aura.reader.ui.components.PixelFullScreenBurst
import com.aura.reader.ui.components.triggerThemeHaptic
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aura.reader.data.model.Book
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onBookSelected: (Book) -> Unit,
    onOpenCatalog: () -> Unit,
    onOpenOnboarding: () -> Unit = {}
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val recentBooks by viewModel.recentBooks.collectAsState()
    val filteredRecentBooks by viewModel.filteredRecentBooks.collectAsState()
    val librarySortOrder by viewModel.librarySortOrder.collectAsState()
    var isReorderMode by remember { mutableStateOf(false) }
    var currentlyDraggedBookId by remember { mutableStateOf<String?>(null) }
    var currentDragOffset by remember { mutableStateOf(Offset.Zero) }
    var accumulatedDragOffset by remember { mutableStateOf(Offset.Zero) }
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    BackHandler(enabled = isReorderMode) {
        isReorderMode = false
        currentlyDraggedBookId = null
        currentDragOffset = Offset.Zero
        accumulatedDragOffset = Offset.Zero
    }

    var currentMainTab by rememberSaveable { mutableIntStateOf(0) }

    val infiniteTransition = rememberInfiniteTransition(label = "ios_wobble")
    val wobbleAngle by infiniteTransition.animateFloat(
        initialValue = -1.8f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(120, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wobbleAngle"
    )
    val wobbleOffset by infiniteTransition.animateFloat(
        initialValue = -1.2f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(140, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wobbleOffset"
    )
    val searchQuery by viewModel.searchQuery.collectAsState()
    val librarySearchHistory by viewModel.librarySearchHistory.collectAsState()
    val todayMinutes by viewModel.todayReadingMinutes.collectAsState()
    val readingStatsData by viewModel.readingStatsData.collectAsState()
    var showReadingStatsSheet by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    val updateInfo by viewModel.updateInfo.collectAsState()
    val foundFiles by viewModel.foundDeviceFiles.collectAsState()
    val readerSettings by viewModel.readerSettings.collectAsState()
    val isGlass = readerSettings.appDesignStyle == AppDesignStyle.LIQUID_GLASS
    val sysDark = isSystemInDarkTheme()
    val isThemeDark = when (readerSettings.appThemeMode) {
        ReaderThemeMode.LIGHT, ReaderThemeMode.SEPIA -> false
        ReaderThemeMode.DARK, ReaderThemeMode.AMOLED -> true
        ReaderThemeMode.SYSTEM_DYNAMIC -> sysDark
    }
    val appLanguage by viewModel.appLanguage.collectAsState()
    val updateNotificationsEnabled by viewModel.updateNotificationsEnabled.collectAsState()
    val anonymousAnalyticsEnabled by viewModel.anonymousAnalyticsEnabled.collectAsState()
    val readingStatsEnabled by viewModel.readingStatsEnabled.collectAsState()
    val materialYouEnabled by viewModel.materialYouEnabled.collectAsState()
    val userCollections by viewModel.userCollections.collectAsState()
    val activeCollectionFilter by viewModel.activeCollectionFilter.collectAsState()
    val libraryViewMode by viewModel.libraryViewMode.collectAsState()
    val customOpdsEnabled by viewModel.customOpdsEnabled.collectAsState()
    val catalogBaseUrl by viewModel.catalogBaseUrl.collectAsState()
    val developerModeEnabled by viewModel.developerModeEnabled.collectAsState()
    val updateChannel by viewModel.updateChannel.collectAsState()
    val appFont by viewModel.appFont.collectAsState()
    val seasonalEffectsEnabled by viewModel.seasonalEffectsEnabled.collectAsState()
    val holidayOverride by viewModel.holidayOverride.collectAsState()

    var holidayBurstKey by remember { mutableLongStateOf(0L) }
    var holidayBurstOrigin by remember { mutableStateOf(Offset.Zero) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()
    var showAddBooksSheet by remember { mutableStateOf(false) }
    BackHandler(enabled = showAddBooksSheet) {
        showAddBooksSheet = false
    }

    BackHandler(enabled = isGlass && currentMainTab != 0 && !showAddBooksSheet) {
        currentMainTab = 0
    }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showReaderThemeSheet by remember { mutableStateOf(false) }
    var bookToDelete by remember { mutableStateOf<Book?>(null) }
    var inspectingCoverBook by remember { mutableStateOf<Book?>(null) }
    var bookForCoverChange by remember { mutableStateOf<Book?>(null) }
    var isSearchExpanded by remember { mutableStateOf(false) }

    BackHandler(enabled = isSearchExpanded && !isGlass) {
        isSearchExpanded = false
        viewModel.setSearchQuery("")
    }
    var showNewCollectionDialog by remember { mutableStateOf(false) }
    var bookForCollections by remember { mutableStateOf<Book?>(null) }
    var collectionToDelete by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(readerSettings.appDesignStyle) {
        if (readerSettings.appDesignStyle == AppDesignStyle.LIQUID_GLASS && showSettingsSheet) {
            showSettingsSheet = false
            currentMainTab = 2
        } else if (readerSettings.appDesignStyle != AppDesignStyle.LIQUID_GLASS && currentMainTab == 2) {
            currentMainTab = 0
            showSettingsSheet = true
        }
    }

    // Backup & Restore Launchers
    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.exportBackup(
                uri = uri,
                contentResolver = context.contentResolver,
                onSuccess = { count ->
                    scope.launch { snackbarHostState.showSnackbar(strings.backupCreatedSuccess(count)) }
                },
                onError = { err ->
                    scope.launch { snackbarHostState.showSnackbar("${strings.backupError}: $err") }
                }
            )
        }
    }

    val restoreBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importBackup(
                uri = uri,
                contentResolver = context.contentResolver,
                onSuccess = { count ->
                    scope.launch { snackbarHostState.showSnackbar(strings.restoreCompletedSuccess(count)) }
                },
                onError = { err ->
                    scope.launch { snackbarHostState.showSnackbar("${strings.backupError}: $err") }
                }
            )
        }
    }

    val sendToGoogleDriveAction = {
        viewModel.exportBackupToTempFile(
            onSuccess = { file, count ->
                try {
                    val fileUri = androidx.core.content.FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/zip"
                        putExtra(Intent.EXTRA_STREAM, fileUri)
                        putExtra(Intent.EXTRA_SUBJECT, strings.shareBackupTitle)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, strings.sendToGoogleDriveTitle))
                } catch (e: Exception) {
                    scope.launch { snackbarHostState.showSnackbar("${strings.backupError}: ${e.localizedMessage}") }
                }
            },
            onError = { err ->
                scope.launch { snackbarHostState.showSnackbar("${strings.backupError}: $err") }
            }
        )
    }

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
        filePickerLauncher.launch(intent)
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
            is LibraryUiState.Message -> {
                scope.launch {
                    snackbarHostState.showSnackbar(state.message)
                }
                viewModel.resetUiState()
            }
            else -> Unit
        }
    }

    LaunchedEffect(isSearchExpanded) {
        if (isSearchExpanded) gridState.scrollToItem(0)
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val hazeState = remember { HazeState() }

    CompositionLocalProvider(LocalHazeState provides hazeState) {
        val mainScreenBg = if (isThemeDark && readerSettings.appThemeMode == ReaderThemeMode.AMOLED) {
            androidx.compose.ui.graphics.Color.Black
        } else {
            MaterialTheme.colorScheme.background
        }
        Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = mainScreenBg,
        topBar = {
            androidx.compose.animation.AnimatedVisibility(
                visible = currentMainTab == 0,
                enter = fadeIn(animationSpec = tween(220)),
                exit = fadeOut(animationSpec = tween(160))
            ) {
                LibraryTopBar(
                    isSearchExpanded = isSearchExpanded,
                    isGlass = isGlass,
                    isThemeDark = isThemeDark,
                    searchQuery = searchQuery,
                    libraryViewMode = libraryViewMode,
                    scrollBehavior = scrollBehavior,
                    onSearchExpandedChange = { isSearchExpanded = it },
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    onToggleViewMode = { viewModel.toggleLibraryViewMode() },
                    onOpenSettings = { showSettingsSheet = true },
                    onHolidayBadgeClick = { origin ->
                        holidayBurstKey = System.currentTimeMillis()
                        holidayBurstOrigin = origin
                        triggerThemeHaptic(context)
                    }
                )
            }
        },
        floatingActionButton = {
            if (isReorderMode) {
                ExtendedFloatingActionButton(
                    shape = RoundedCornerShape(20.dp),
                    onClick = { isReorderMode = false },
                    icon = { Icon(Icons.Rounded.Check, contentDescription = null) },
                    text = { Text(strings.done) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            } else if (isGlass) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FloatingNavigationIsland(
                        designStyle = readerSettings.appDesignStyle,
                        clarity = readerSettings.glassClarity,
                        selectedTab = currentMainTab,
                        isDark = isThemeDark,
                        onMyBooksClick = {
                            if (currentMainTab != 0) {
                                currentMainTab = 0
                            } else {
                                scope.launch { gridState.animateScrollToItem(0) }
                            }
                        },
                        onSearchClick = {
                            currentMainTab = 1
                        },
                        onSettingsClick = {
                            currentMainTab = 2
                        }
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    FloatingAddGlassButton(
                        onClick = {
                            if (currentMainTab != 0) {
                                currentMainTab = 0
                            }
                            showAddBooksSheet = true
                        },
                        isDark = isThemeDark
                    )
                }
            } else {
                ExtendedFloatingActionButton(
                    shape = RoundedCornerShape(20.dp),
                    onClick = { showAddBooksSheet = true },
                    icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                    text = { Text(strings.addBooks) },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        floatingActionButtonPosition = if (isGlass) FabPosition.Center else FabPosition.End,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .haze(hazeState)
        ) {
            if (showAddBooksSheet) {
                AddBooksBottomSheet(
                    onDismiss = { showAddBooksSheet = false },
                    onSelectFiles = { launchFilePicker() },
                    onScanFolder = { openFolderLauncher.launch(null) },
                    onOpenCatalog = onOpenCatalog,
                    isLiquidGlass = isGlass,
                    isDark = isThemeDark
                )
            }

            AnimatedContent(
                targetState = currentMainTab,
                transitionSpec = {
                    val forward = targetState > initialState
                    val slideFraction = 0.28f
                    if (forward) {
                        (slideInHorizontally(
                            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 420f)
                        ) { width -> (width * slideFraction).toInt() } + fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)))
                            .togetherWith(
                                slideOutHorizontally(
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 420f)
                                ) { width -> (-width * slideFraction).toInt() } + fadeOut(animationSpec = tween(180, easing = FastOutLinearInEasing))
                            )
                    } else {
                        (slideInHorizontally(
                            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 420f)
                        ) { width -> (-width * slideFraction).toInt() } + fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)))
                            .togetherWith(
                                slideOutHorizontally(
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 420f)
                                ) { width -> (width * slideFraction).toInt() } + fadeOut(animationSpec = tween(180, easing = FastOutLinearInEasing))
                            )
                    }
                },
                label = "dockTabNavigationTransition",
                modifier = Modifier.fillMaxSize()
            ) { tab ->
                when (tab) {
                    1 -> {
                        LibrarySearchScreen(
                                        searchQuery = searchQuery,
                                        onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                        searchHistory = librarySearchHistory,
                                        onSelectHistoryQuery = { query ->
                                            viewModel.setSearchQuery(query)
                                        },
                                        onRemoveHistoryQuery = { query ->
                                            viewModel.removeLibrarySearchQuery(query)
                                        },
                                        onClearSearchHistory = {
                                            viewModel.clearLibrarySearchHistory()
                                        },
                                        onSaveSearchQuery = { query ->
                                            viewModel.saveLibrarySearchQuery(query)
                                        },
                                        books = recentBooks,
                                        onBookClick = onBookSelected,
                                        onDeleteBook = { book -> bookToDelete = book },
                                        onToggleFavorite = { book -> viewModel.toggleFavorite(book.id) },
                                        onAddToCollection = { book -> bookForCollections = book },
                                        onSetProgress = { book, prog -> viewModel.setBookReadingProgress(book.id, prog) },
                                        onCoverClick = { book -> inspectingCoverBook = book },
                                        onShare = { book -> BookShareUtils.shareBookFile(context, book) },
                                        onChangeCover = { book -> bookForCoverChange = book },
                                        onBack = { currentMainTab = 0 },
                                        libraryViewMode = libraryViewMode,
                                        isDark = isThemeDark,
                                        clarity = readerSettings.glassClarity
                                    )
                    }
                    2 -> {
                        SettingsBottomSheet(
                                        currentTheme = readerSettings.appThemeMode,
                                        currentLanguage = appLanguage,
                                        updateNotificationsEnabled = updateNotificationsEnabled,
                                        anonymousAnalyticsEnabled = anonymousAnalyticsEnabled,
                                        readingStatsEnabled = readingStatsEnabled,
                                        materialYouEnabled = materialYouEnabled,
                                        seasonalEffectsEnabled = seasonalEffectsEnabled,
                                        onToggleSeasonalEffects = { viewModel.setSeasonalEffectsEnabled(it) },
                                        holidayOverride = holidayOverride,
                                        onHolidayOverrideChange = { viewModel.setHolidayOverride(it) },
                                        isFullScreen = true,
                                        onDismiss = { currentMainTab = 0 },
                                        onOpenOnboarding = onOpenOnboarding,
                                        onThemeChange = { viewModel.setAppThemeMode(it) },
                                        onLanguageChange = { viewModel.setAppLanguage(it) },
                                        onToggleUpdateNotifications = { viewModel.setUpdateNotificationsEnabled(it) },
                                        onToggleAnonymousAnalytics = { viewModel.setAnonymousAnalyticsEnabled(it) },
                                        onToggleReadingStats = { viewModel.setReadingStatsEnabled(it) },
                                        onToggleMaterialYou = { viewModel.setMaterialYouEnabled(it) },
                                        customOpdsEnabled = customOpdsEnabled,
                                        customOpdsUrl = catalogBaseUrl,
                                        onToggleCustomOpds = { viewModel.setCustomOpdsEnabled(it) },
                                        onCustomOpdsUrlChange = { viewModel.setCustomOpdsUrl(it) },
                                        developerModeEnabled = developerModeEnabled,
                                        updateChannel = updateChannel,
                                        onToggleDeveloperMode = { viewModel.setDeveloperModeEnabled(it) },
                                        onUpdateChannelChange = { viewModel.setUpdateChannel(it) },
                                        onCheckUpdates = {
                                            viewModel.checkForUpdates(manual = true, context = context)
                                        },
                                        onExportBackup = {
                                            exportBackupLauncher.launch("AuraReader_Backup_${System.currentTimeMillis()}.aurabackup")
                                        },
                                        onSendToGoogleDrive = {
                                            sendToGoogleDriveAction()
                                        },
                                        onRestoreBackup = {
                                            restoreBackupLauncher.launch(arrayOf("application/zip", "application/octet-stream", "*/*"))
                                        },
                                        readerTheme = readerSettings.themeMode,
                                        syncThemesWithApp = readerSettings.syncThemesWithApp,
                                        onSyncThemesChange = { viewModel.setSyncThemesWithApp(it) },
                                        onReaderThemeChange = { viewModel.setThemeMode(it) },
                                        onOpenReaderThemeSettings = {
                                            showReaderThemeSheet = true
                                        },
                                        appDesignStyle = readerSettings.appDesignStyle,
                                        onAppDesignStyleChange = { viewModel.setAppDesignStyle(it) },
                                        glassClarity = readerSettings.glassClarity,
                                        onGlassClarityChange = { viewModel.setGlassClarity(it) },
                                        appFont = appFont,
                                        onAppFontChange = { viewModel.setAppFont(it) },
                                        onResetReadingSpeed = { viewModel.resetReadingSpeed() }
                                    )
                    }
                    else -> {
                        Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(innerPadding)
                                    ) {
                                    if (recentBooks.isEmpty() && uiState !is LibraryUiState.Loading) {
                                        Column(Modifier.fillMaxSize()) {
                                            if (isSearchExpanded && !isGlass) {
                                                SearchCoverCollage(emptyList(), subdued = true, modifier = Modifier.height(100.dp))
                                            }
                                            Box(Modifier.weight(1f)) {
                                                EmptyLibraryView(
                                                    onAddBooks = { showAddBooksSheet = true },
                                                    onOpenSample = { viewModel.openSampleBook() }
                                                )
                                            }
                                        }
                                    } else {
                                        LazyVerticalGrid(
                                            columns = GridCells.Fixed(if (libraryViewMode == "GRID") 2 else 1),
                                            state = gridState,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .jellyOverscroll(),
                                            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            if (isSearchExpanded && !isGlass) {
                                                item(key = "searchCollage", span = { GridItemSpan(maxLineSpan) }) {
                                                    val keyboardOpen = WindowInsets.ime.getBottom(LocalDensity.current) > 0
                                                    val subdued = searchQuery.isNotBlank() || keyboardOpen
                                                    val height by animateDpAsState(if (subdued) 72.dp else 240.dp, tween(350), label = "materialSearchHero")
                                                    AnimatedVisibility(
                                                        visible = isSearchExpanded && !isGlass,
                                                        enter = expandVertically(tween(320)) + fadeIn(tween(250)),
                                                        exit = shrinkVertically(tween(250)) + fadeOut(tween(200))
                                                    ) {
                                                        SearchCoverCollage(
                                                            recentBooks,
                                                            subdued,
                                                            Modifier
                                                                .fillMaxWidth()
                                                                .height(height)
                                                                .clip(RoundedCornerShape(24.dp))
                                                        )
                                                    }
                                                }
                                            }
                                            // Instant Update Notification Banner
                                            val curUpdate = updateInfo
                                            if (curUpdate != null && curUpdate.isAvailable && updateNotificationsEnabled) {
                                                item(span = { GridItemSpan(maxLineSpan) }) {
                                                    LibraryUpdateBanner(
                                                        update = curUpdate,
                                                        isGlass = isGlass,
                                                        isThemeDark = isThemeDark,
                                                        onOpenUpdatePage = { url -> viewModel.openUpdatePage(context, url) }
                                                    )
                                                }
                                            }

                                            // Daily Reading Stats (Toggleable in Settings)
                                            if (readingStatsEnabled) {
                                                item(span = { GridItemSpan(maxLineSpan) }) {
                                                    LibraryReadingStatsBanner(
                                                        isGlass = isGlass,
                                                        isThemeDark = isThemeDark,
                                                        todayMinutes = todayMinutes,
                                                        currentStreakDays = readingStatsData.currentStreakDays,
                                                        onClick = { showReadingStatsSheet = true }
                                                    )
                                                }
                                            }

                                            // Collections Filter Chips Row
                                            item(span = { GridItemSpan(maxLineSpan) }) {
                                                LibraryCollectionChips(
                                                    activeFilter = activeCollectionFilter,
                                                    hasSeries = remember(recentBooks) { recentBooks.any { !it.series.isNullOrBlank() } },
                                                    userCollections = userCollections,
                                                    isGlass = isGlass,
                                                    isThemeDark = isThemeDark,
                                                    onFilterChange = { viewModel.setCollectionFilter(it) },
                                                    onDeleteCollection = { collectionToDelete = it },
                                                    onNewCollection = { showNewCollectionDialog = true }
                                                )
                                            }

                                            val currentHeroBook = recentBooks.firstOrNull { book ->
                                                if (book.manualShelf == "UNREAD" || book.manualShelf == "FINISHED") false
                                                else if (book.manualShelf == "READING") true
                                                else book.progressPercent < 100
                                            }
                                            val showHeroCard = currentHeroBook != null &&
                                                    searchQuery.isBlank() &&
                                                    (activeCollectionFilter.type == CollectionFilterType.ALL || activeCollectionFilter.type == CollectionFilterType.READING) &&
                                                    librarySortOrder != LibrarySortOption.CUSTOM &&
                                                    !isReorderMode

                                            val displayedRecentBooks = if (showHeroCard && currentHeroBook != null) {
                                                filteredRecentBooks.filter { it.id != currentHeroBook.id }
                                            } else {
                                                filteredRecentBooks
                                            }

                                            if (showHeroCard && currentHeroBook != null) {
                                                item(key = "currently_reading_hero", span = { GridItemSpan(maxLineSpan) }) {
                                                    CurrentlyReadingHeroCard(
                                                        book = currentHeroBook,
                                                        onBookClick = onBookSelected,
                                                        onDelete = { bookToDelete = currentHeroBook },
                                                        onToggleFavorite = { viewModel.toggleFavorite(currentHeroBook.id) },
                                                        onAddToCollection = { bookForCollections = currentHeroBook },
                                                        onSetProgress = { prog -> viewModel.setBookReadingProgress(currentHeroBook.id, prog) },
                                                        onCoverClick = { inspectingCoverBook = currentHeroBook },
                                                        onShare = { BookShareUtils.shareBookFile(context, currentHeroBook) },
                                                        onChangeCover = { bookForCoverChange = currentHeroBook },
                                                        isGlass = readerSettings.appDesignStyle == AppDesignStyle.LIQUID_GLASS,
                                                        isDark = isThemeDark,
                                                        clarity = readerSettings.glassClarity,
                                                        modifier = Modifier.padding(bottom = 6.dp)
                                                    )
                                                }
                                            }

                                            item(key = "section_header", span = { GridItemSpan(maxLineSpan) }) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(top = 8.dp, bottom = 8.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    val count = displayedRecentBooks.size
                                                    val countWord = when {
                                                        count % 100 in 11..19 -> "книг"
                                                        count % 10 == 1 -> "книга"
                                                        count % 10 in 2..4 -> "книги"
                                                        else -> "книг"
                                                    }
                                                    val countLabel = if (searchQuery.isNotBlank()) {
                                                        strings.searchResultsCount(count)
                                                    } else {
                                                        "$count $countWord"
                                                    }
                                                    Text(
                                                        text = countLabel,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Medium,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )

                                                    val buttonBg = if (isGlass) {
                                                        if (isThemeDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
                                                    } else {
                                                        MaterialTheme.colorScheme.surfaceContainer
                                                    }
                                                    val buttonBorder = if (isGlass) {
                                                        BorderStroke(0.5.dp, if (isThemeDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.10f))
                                                    } else null

                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        // 1. Sort Button [ ≡ Последние ]
                                                        if (searchQuery.isBlank()) {
                                                            Box {
                                                                var showSortMenu by remember { mutableStateOf(false) }
                                                                val sortLabel = when (librarySortOrder) {
                                                                    LibrarySortOption.RECENT -> strings.sortByRecent
                                                                    LibrarySortOption.TITLE -> strings.sortByTitle
                                                                    LibrarySortOption.AUTHOR -> strings.sortByAuthor
                                                                    LibrarySortOption.SERIES -> strings.sortBySeries
                                                                    LibrarySortOption.CUSTOM -> strings.sortCustomOrder
                                                                }
                                                                Surface(
                                                                    onClick = { showSortMenu = true },
                                                                    shape = RoundedCornerShape(12.dp),
                                                                    color = buttonBg,
                                                                    border = buttonBorder,
                                                                    modifier = Modifier.height(38.dp)
                                                                ) {
                                                                    Row(
                                                                        modifier = Modifier.padding(horizontal = 14.dp),
                                                                        verticalAlignment = Alignment.CenterVertically,
                                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                                    ) {
                                                                        Icon(
                                                                            imageVector = Icons.AutoMirrored.Rounded.Sort,
                                                                            contentDescription = strings.sortTitle,
                                                                            modifier = Modifier.size(18.dp),
                                                                            tint = MaterialTheme.colorScheme.onSurface
                                                                        )
                                                                        Text(
                                                                            text = sortLabel,
                                                                            style = MaterialTheme.typography.labelLarge,
                                                                            fontWeight = FontWeight.Medium,
                                                                            color = MaterialTheme.colorScheme.onSurface
                                                                        )
                                                                    }
                                                                }

                                                                val sortMenuAppearance = liquidGlassMenuAppearance(isGlass, isThemeDark)
                                                                DropdownMenu(
                                                                    expanded = showSortMenu,
                                                                    onDismissRequest = { showSortMenu = false },
                                                                    shape = RoundedCornerShape(20.dp),
                                                                    containerColor = sortMenuAppearance.containerColor,
                                                                    border = sortMenuAppearance.border,
                                                                    shadowElevation = sortMenuAppearance.shadowElevation
                                                                ) {
                                                                    DropdownMenuItem(
                                                                        text = { Text(strings.sortByRecent) },
                                                                        onClick = {
                                                                            viewModel.setLibrarySortOrder(LibrarySortOption.RECENT)
                                                                            showSortMenu = false
                                                                        },
                                                                        leadingIcon = {
                                                                            if (librarySortOrder == LibrarySortOption.RECENT) {
                                                                                Icon(Icons.Rounded.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                                                            }
                                                                        }
                                                                    )
                                                                    DropdownMenuItem(
                                                                        text = { Text(strings.sortByTitle) },
                                                                        onClick = {
                                                                            viewModel.setLibrarySortOrder(LibrarySortOption.TITLE)
                                                                            showSortMenu = false
                                                                        },
                                                                        leadingIcon = {
                                                                            if (librarySortOrder == LibrarySortOption.TITLE) {
                                                                                Icon(Icons.Rounded.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                                                            }
                                                                        }
                                                                    )
                                                                    DropdownMenuItem(
                                                                        text = { Text(strings.sortByAuthor) },
                                                                        onClick = {
                                                                            viewModel.setLibrarySortOrder(LibrarySortOption.AUTHOR)
                                                                            showSortMenu = false
                                                                        },
                                                                        leadingIcon = {
                                                                            if (librarySortOrder == LibrarySortOption.AUTHOR) {
                                                                                Icon(Icons.Rounded.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                                                            }
                                                                        }
                                                                    )
                                                                    DropdownMenuItem(
                                                                        text = { Text(strings.sortBySeries) },
                                                                        onClick = {
                                                                            viewModel.setLibrarySortOrder(LibrarySortOption.SERIES)
                                                                            showSortMenu = false
                                                                        },
                                                                        leadingIcon = {
                                                                            if (librarySortOrder == LibrarySortOption.SERIES) {
                                                                                Icon(Icons.Rounded.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                                                            }
                                                                        }
                                                                    )
                                                                    DropdownMenuItem(
                                                                        text = { Text(strings.sortCustomOrder) },
                                                                        onClick = {
                                                                            viewModel.setLibrarySortOrder(LibrarySortOption.CUSTOM)
                                                                            showSortMenu = false
                                                                        },
                                                                        leadingIcon = {
                                                                            if (librarySortOrder == LibrarySortOption.CUSTOM) {
                                                                                Icon(Icons.Rounded.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                                                            }
                                                                        }
                                                                    )

                                                                    val booksWithoutCovers = remember(recentBooks) { recentBooks.filter { it.coverBase64.isNullOrBlank() } }
                                                                    if (booksWithoutCovers.isNotEmpty()) {
                                                                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                                                        DropdownMenuItem(
                                                                            text = { Text(strings.downloadMissingCovers) },
                                                                            leadingIcon = {
                                                                                Icon(Icons.Rounded.CloudDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                                                            },
                                                                            onClick = {
                                                                                showSortMenu = false
                                                                                Toast.makeText(context, strings.downloadMissingCovers + "...", Toast.LENGTH_SHORT).show()
                                                                                viewModel.autoDownloadMissingCovers(recentBooks) { count ->
                                                                                    Toast.makeText(context, strings.coversDownloadedSuccess(count), Toast.LENGTH_SHORT).show()
                                                                                }
                                                                            }
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }

                                                        // 2. Layout Toggle Button [ ☷ ] / [ ☰ ]
                                                        Surface(
                                                            onClick = { viewModel.toggleLibraryViewMode() },
                                                            shape = RoundedCornerShape(12.dp),
                                                            color = buttonBg,
                                                            border = buttonBorder,
                                                            modifier = Modifier.size(38.dp)
                                                        ) {
                                                            Box(contentAlignment = Alignment.Center) {
                                                                Icon(
                                                                    imageVector = if (libraryViewMode == "GRID") Icons.AutoMirrored.Rounded.ViewList else Icons.Rounded.GridView,
                                                                    contentDescription = if (libraryViewMode == "GRID") strings.viewModeList else strings.viewModeGrid,
                                                                    tint = MaterialTheme.colorScheme.onSurface,
                                                                    modifier = Modifier.size(20.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            if (displayedRecentBooks.isEmpty() && searchQuery.isNotBlank()) {
                                                item(span = { GridItemSpan(maxLineSpan) }) {
                                                    Text(
                                                        text = strings.noSearchResultsFound(searchQuery),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.outline,
                                                        modifier = Modifier.padding(vertical = 16.dp)
                                                    )
                                                }
                                            }

                                            gridItemsIndexed(
                                                items = displayedRecentBooks,
                                                key = { _, book -> book.id }
                                            ) { index, book ->
                                                if (libraryViewMode == "GRID") {
                                                    BookGridCard(
                                                        book = book,
                                                        index = index,
                                                        isGlass = isGlass,
                                                        isDark = isThemeDark,
                                                        isReorderMode = isReorderMode,
                                                        wobbleAngle = wobbleAngle,
                                                        wobbleOffset = wobbleOffset,
                                                        currentlyDraggedBookId = currentlyDraggedBookId,
                                                        currentDragOffset = currentDragOffset,
                                                        onDragStart = {
                                                            isReorderMode = true
                                                            viewModel.startReorderMode(displayedRecentBooks)
                                                            currentlyDraggedBookId = book.id
                                                            currentDragOffset = Offset.Zero
                                                            accumulatedDragOffset = Offset.Zero
                                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        },
                                                        onDrag = { dragAmount ->
                                                            currentDragOffset += dragAmount
                                                            accumulatedDragOffset += dragAmount
                                                            val thresholdX = with(density) { 75.dp.toPx() }
                                                            val thresholdY = with(density) { 100.dp.toPx() }
                                                            if (accumulatedDragOffset.x > thresholdX) {
                                                                viewModel.moveBookCustomOrder(book.id, 1, displayedRecentBooks)
                                                                accumulatedDragOffset = Offset(accumulatedDragOffset.x - thresholdX, accumulatedDragOffset.y)
                                                                currentDragOffset = Offset(currentDragOffset.x - thresholdX, currentDragOffset.y)
                                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                            } else if (accumulatedDragOffset.x < -thresholdX) {
                                                                viewModel.moveBookCustomOrder(book.id, -1, displayedRecentBooks)
                                                                accumulatedDragOffset = Offset(accumulatedDragOffset.x + thresholdX, accumulatedDragOffset.y)
                                                                currentDragOffset = Offset(currentDragOffset.x + thresholdX, currentDragOffset.y)
                                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                            } else if (accumulatedDragOffset.y > thresholdY) {
                                                                viewModel.moveBookCustomOrder(book.id, 2, displayedRecentBooks)
                                                                accumulatedDragOffset = Offset(accumulatedDragOffset.x, accumulatedDragOffset.y - thresholdY)
                                                                currentDragOffset = Offset(currentDragOffset.x, currentDragOffset.y - thresholdY)
                                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                            } else if (accumulatedDragOffset.y < -thresholdY) {
                                                                viewModel.moveBookCustomOrder(book.id, -2, displayedRecentBooks)
                                                                accumulatedDragOffset = Offset(accumulatedDragOffset.x, accumulatedDragOffset.y + thresholdY)
                                                                currentDragOffset = Offset(currentDragOffset.x, currentDragOffset.y + thresholdY)
                                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                            }
                                                        },
                                                        onDragEnd = {
                                                            currentlyDraggedBookId = null
                                                            currentDragOffset = Offset.Zero
                                                            accumulatedDragOffset = Offset.Zero
                                                        },
                                                        onClick = { onBookSelected(book) },
                                                        onDelete = { bookToDelete = book },
                                                        onToggleFavorite = { viewModel.toggleFavorite(book.id) },
                                                        onAddToCollection = { bookForCollections = book },
                                                        onSetProgress = { prog -> viewModel.setBookReadingProgress(book.id, prog) },
                                                        onCoverClick = { inspectingCoverBook = book },
                                                        onShare = { BookShareUtils.shareBookFile(context, book) },
                                                        onChangeCover = { bookForCoverChange = book }
                                                    )
                                                } else {
                                                    BookCard(
                                                        book = book,
                                                        index = index,
                                                        isGlass = isGlass,
                                                        isDark = isThemeDark,
                                                        isReorderMode = isReorderMode,
                                                        wobbleAngle = wobbleAngle,
                                                        wobbleOffset = wobbleOffset,
                                                        currentlyDraggedBookId = currentlyDraggedBookId,
                                                        currentDragOffset = currentDragOffset,
                                                        onDragStart = {
                                                            isReorderMode = true
                                                            viewModel.startReorderMode(displayedRecentBooks)
                                                            currentlyDraggedBookId = book.id
                                                            currentDragOffset = Offset.Zero
                                                            accumulatedDragOffset = Offset.Zero
                                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        },
                                                        onDrag = { dragAmount ->
                                                            currentDragOffset += dragAmount
                                                            accumulatedDragOffset += dragAmount
                                                            val thresholdY = with(density) { 68.dp.toPx() }
                                                            if (accumulatedDragOffset.y > thresholdY) {
                                                                viewModel.moveBookCustomOrder(book.id, 1, displayedRecentBooks)
                                                                accumulatedDragOffset = Offset(accumulatedDragOffset.x, accumulatedDragOffset.y - thresholdY)
                                                                currentDragOffset = Offset(currentDragOffset.x, currentDragOffset.y - thresholdY)
                                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                            } else if (accumulatedDragOffset.y < -thresholdY) {
                                                                viewModel.moveBookCustomOrder(book.id, -1, displayedRecentBooks)
                                                                accumulatedDragOffset = Offset(accumulatedDragOffset.x, accumulatedDragOffset.y + thresholdY)
                                                                currentDragOffset = Offset(currentDragOffset.x, currentDragOffset.y + thresholdY)
                                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                            }
                                                        },
                                                        onDragEnd = {
                                                            currentlyDraggedBookId = null
                                                            currentDragOffset = Offset.Zero
                                                            accumulatedDragOffset = Offset.Zero
                                                        },
                                                        onClick = { onBookSelected(book) },
                                                        onDelete = { bookToDelete = book },
                                                        onToggleFavorite = { viewModel.toggleFavorite(book.id) },
                                                        onAddToCollection = { bookForCollections = book },
                                                        onSetProgress = { prog -> viewModel.setBookReadingProgress(book.id, prog) },
                                                        onCoverClick = { inspectingCoverBook = book },
                                                        onShare = { BookShareUtils.shareBookFile(context, book) },
                                                        onChangeCover = { bookForCoverChange = book }
                                                    )
                                                }
                                            }

                                            item(span = { GridItemSpan(maxLineSpan) }) {
                                                Spacer(modifier = Modifier.height(72.dp)) // padding for FAB
                                            }
                                        }
                                    }

                                    if (uiState is LibraryUiState.Loading) {
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
                }
            }
    }
}

    LibraryDialogs(
        updateInfo = updateInfo,
        showNewCollectionDialog = showNewCollectionDialog,
        collectionToDelete = collectionToDelete,
        bookForCollections = bookForCollections,
        userCollections = userCollections,
        onDismissUpdate = { viewModel.dismissUpdateDialog() },
        onOpenUpdatePage = { url -> viewModel.openUpdatePage(context, url) },
        onSkipVersion = { version -> viewModel.skipUpdateVersion(version) },
        onDismissNewCollection = { showNewCollectionDialog = false },
        onCreateCollection = { name -> viewModel.addUserCollection(name) },
        onDismissDeleteCollection = { collectionToDelete = null },
        onRemoveCollection = { name -> viewModel.removeUserCollection(name) },
        onDismissBookCollections = { bookForCollections = null },
        onSaveBookCollections = { id, cols -> viewModel.updateBookCollections(id, cols) },
        onSaveBookPlacement = { id, cols, shelf, fav -> viewModel.updateBookPlacement(id, cols, shelf, fav) },
        onNewCollectionRequested = { showNewCollectionDialog = true }
    )

    if (showSettingsSheet) {
        SettingsBottomSheet(
            currentTheme = readerSettings.appThemeMode,
            currentLanguage = appLanguage,
            updateNotificationsEnabled = updateNotificationsEnabled,
            anonymousAnalyticsEnabled = anonymousAnalyticsEnabled,
            readingStatsEnabled = readingStatsEnabled,
            materialYouEnabled = materialYouEnabled,
            seasonalEffectsEnabled = seasonalEffectsEnabled,
            onToggleSeasonalEffects = { viewModel.setSeasonalEffectsEnabled(it) },
            holidayOverride = holidayOverride,
            onHolidayOverrideChange = { viewModel.setHolidayOverride(it) },
            onDismiss = { showSettingsSheet = false },
            onOpenOnboarding = onOpenOnboarding,
            onThemeChange = { viewModel.setAppThemeMode(it) },
            onLanguageChange = { viewModel.setAppLanguage(it) },
            onToggleUpdateNotifications = { viewModel.setUpdateNotificationsEnabled(it) },
            onToggleAnonymousAnalytics = { viewModel.setAnonymousAnalyticsEnabled(it) },
            onToggleReadingStats = { viewModel.setReadingStatsEnabled(it) },
            onToggleMaterialYou = { viewModel.setMaterialYouEnabled(it) },
            customOpdsEnabled = customOpdsEnabled,
            customOpdsUrl = catalogBaseUrl,
            onToggleCustomOpds = { viewModel.setCustomOpdsEnabled(it) },
            onCustomOpdsUrlChange = { viewModel.setCustomOpdsUrl(it) },
            developerModeEnabled = developerModeEnabled,
            updateChannel = updateChannel,
            onToggleDeveloperMode = { viewModel.setDeveloperModeEnabled(it) },
            onUpdateChannelChange = { viewModel.setUpdateChannel(it) },
            onCheckUpdates = {
                showSettingsSheet = false
                viewModel.checkForUpdates(manual = true, context = context)
            },
            onExportBackup = {
                showSettingsSheet = false
                exportBackupLauncher.launch("AuraReader_Backup_${System.currentTimeMillis()}.aurabackup")
            },
            onSendToGoogleDrive = {
                showSettingsSheet = false
                sendToGoogleDriveAction()
            },
            onRestoreBackup = {
                showSettingsSheet = false
                restoreBackupLauncher.launch(arrayOf("application/zip", "application/octet-stream", "*/*"))
            },
            readerTheme = readerSettings.themeMode,
            syncThemesWithApp = readerSettings.syncThemesWithApp,
            onSyncThemesChange = { viewModel.setSyncThemesWithApp(it) },
            onReaderThemeChange = { viewModel.setThemeMode(it) },
            onOpenReaderThemeSettings = {
                showSettingsSheet = false
                showReaderThemeSheet = true
            },
            appDesignStyle = readerSettings.appDesignStyle,
            onAppDesignStyleChange = { viewModel.setAppDesignStyle(it) },
            glassClarity = readerSettings.glassClarity,
            onGlassClarityChange = { viewModel.setGlassClarity(it) },
            appFont = appFont,
            onAppFontChange = { viewModel.setAppFont(it) },
            onResetReadingSpeed = { viewModel.resetReadingSpeed() }
        )
    }

    if (showReaderThemeSheet) {
        ReaderSettingsBottomSheet(
            settings = readerSettings,
            materialYouEnabled = materialYouEnabled,
            onMaterialYouChange = { viewModel.setMaterialYouEnabled(it) },
            onDismiss = { showReaderThemeSheet = false },
            onFontSizeChange = { viewModel.updateFontSize(it) },
            onLineHeightChange = { viewModel.updateLineHeight(it) },
            onThemeModeChange = { viewModel.setThemeMode(it) },
            onFontFamilyChange = { viewModel.updateFontFamily(it) },
            onFontNameChange = { viewModel.updateFontName(it) },
            onLightImageBackgroundChange = { viewModel.updateLightImageBackground(it) },
            onPagingModeChange = { viewModel.updatePagingMode(it) },
            onAutoHyphenationChange = { viewModel.updateAutoHyphenation(it) },
            onTwoColumnModeChange = { viewModel.updateTwoColumnMode(it) },
            onPageAnimationChange = { viewModel.updatePageAnimation(it) },
            onHapticFeedbackChange = { viewModel.updateHapticFeedback(it) },
            onResetReadingSpeed = { viewModel.resetReadingSpeed() }
        )
    }

    inspectingCoverBook?.let { book ->
        ParallaxCoverViewer(
            book = book,
            onDismiss = { inspectingCoverBook = null }
        )
    }

    bookForCoverChange?.let { book ->
        CoverSearchBottomSheet(
            book = book,
            onDismiss = { bookForCoverChange = null },
            onCoverSelected = { newBase64 ->
                viewModel.updateBookCover(book.id, newBase64)
                bookForCoverChange = null
            }
        )
    }

    if (showReadingStatsSheet) {
        ReadingStatsBottomSheet(
            statsData = readingStatsData,
            onDismiss = { showReadingStatsSheet = false },
            onResetStats = {
                showReadingStatsSheet = false
                viewModel.resetReadingStats()
            },
            isLiquidGlass = isGlass,
            isDark = isThemeDark
        )
    }

    if (holidayBurstKey != 0L) {
        val holidaySeason = com.aura.reader.data.model.LocalHolidaySeason.current
        PixelFullScreenBurst(
            triggerKey = holidayBurstKey,
            origin = if (holidayBurstOrigin != Offset.Zero) {
                holidayBurstOrigin
            } else {
                Offset(
                    x = context.resources.displayMetrics.widthPixels / 2f,
                    y = context.resources.displayMetrics.heightPixels / 4f
                )
            },
            colors = holidaySeason.burstColors,
            symbols = holidaySeason.burstSymbols,
            durationMillis = 2600
        )
    }
    }
}

