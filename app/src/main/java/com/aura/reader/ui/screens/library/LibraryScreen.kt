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
import dev.chrisbanes.haze.hazeSource
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
import com.aura.reader.ui.components.LiquidGlassMenuContainer
import com.aura.reader.ui.components.LiquidGlassMenuItem
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import com.aura.reader.data.model.AppDesignStyle
import com.aura.reader.data.model.ReaderThemeMode
import com.aura.reader.ui.components.FloatingNavigationIsland
import com.aura.reader.ui.components.FloatingAddGlassButton
import com.aura.reader.ui.components.MorphingFloatingBottomBar
import com.aura.reader.ui.components.LiquidGlassFilterChip
import com.aura.reader.ui.components.LiquidGlassCard
import com.aura.reader.ui.components.liquidGlassMenuAppearance
import com.aura.reader.ui.components.PopupBlurBehind
import com.aura.reader.ui.components.ParallaxCoverViewer
import com.aura.reader.util.BookShareUtils
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import com.aura.reader.ui.screens.settings.SettingsInterfacePage
import com.aura.reader.ui.screens.settings.SettingsGeneralPage
import com.aura.reader.ui.screens.settings.SettingsAboutPage
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material3.HorizontalDivider
import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
import kotlin.coroutines.cancellation.CancellationException
import com.aura.reader.ui.components.LiquidGlassSurface
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.style.TextOverflow
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import com.aura.reader.ui.components.ExpressiveButtonSize
import com.aura.reader.ui.components.ExpressiveSplitButton
import com.aura.reader.ui.components.PixelFullScreenBurst
import com.aura.reader.ui.components.triggerThemeHaptic
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.reader.data.model.Book
import com.aura.reader.data.model.isReadingStarted
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import com.aura.reader.data.model.HolidaySeason
import com.aura.reader.data.model.LocalHolidaySeason

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

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 5 })
    val activePage = if (pagerState.isScrollInProgress) pagerState.targetPage else pagerState.currentPage
    val isSettingsOpen = activePage >= 2
    val selectedMainTab = when {
        activePage == 1 -> 1
        activePage >= 2 -> 2
        else -> 0
    }
    val selectedSettingsCategory = (activePage - 2).coerceIn(0, 2)

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
    val updateCheckState by viewModel.updateCheckState.collectAsState()
    val updateInstallState by viewModel.updateInstallState.collectAsState()
    val foundFiles by viewModel.foundDeviceFiles.collectAsState()
    val readerSettings by viewModel.readerSettings.collectAsState()
    val isGlass = readerSettings.appDesignStyle == AppDesignStyle.LIQUID_GLASS
    val sysDark = isSystemInDarkTheme()
    val isThemeDark = when (readerSettings.appThemeMode) {
        ReaderThemeMode.LIGHT, ReaderThemeMode.SEPIA -> false
        ReaderThemeMode.DARK, ReaderThemeMode.AMOLED -> true
        ReaderThemeMode.SYSTEM_DYNAMIC -> sysDark
    }
    val isSepiaTheme = readerSettings.appThemeMode == ReaderThemeMode.SEPIA
    val appSettings by viewModel.appSettings.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val updateNotificationsEnabled by viewModel.updateNotificationsEnabled.collectAsState()
    val anonymousAnalyticsEnabled by viewModel.anonymousAnalyticsEnabled.collectAsState()
    val readingStatsEnabled by viewModel.readingStatsEnabled.collectAsState()
    val autoFetchMetadataEnabled by viewModel.autoFetchMetadataEnabled.collectAsState()
    val materialYouEnabled by viewModel.materialYouEnabled.collectAsState()
    val colorPreset by viewModel.colorPreset.collectAsState()
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
    val holidaySeason = LocalHolidaySeason.current
    val lastHolidaySoundSeason by viewModel.lastHolidaySoundSeason.collectAsState()
    val trashCount by viewModel.trashCount.collectAsState()
    var selectedBookForProfile by remember { mutableStateOf<Book?>(null) }
    var showClearTrashConfirmDialog by remember { mutableStateOf(false) }
    var bookToDeletePermanently by remember { mutableStateOf<Book?>(null) }

    var holidayBurstKey by remember { mutableLongStateOf(0L) }
    var holidayBurstOrigin by remember { mutableStateOf(Offset.Zero) }

    androidx.compose.runtime.LaunchedEffect(holidaySeason, seasonalEffectsEnabled, lastHolidaySoundSeason) {
        if (seasonalEffectsEnabled && holidaySeason != HolidaySeason.NONE) {
            val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            val seasonKey = "${holidaySeason.id}_$currentYear"
            if (lastHolidaySoundSeason != seasonKey) {
                viewModel.markHolidaySoundPlayed(seasonKey)
                com.aura.reader.util.HolidaySoundPlayer.playHolidaySound(context, holidaySeason)
            }
        }
    }

    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            com.aura.reader.util.HolidaySoundPlayer.stop()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()
    var showAddBooksSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }

    val pageAnimationSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = 420f
    )
    val smoothScrollToPage: (Int) -> Unit = { page ->
        scope.launch {
            pagerState.animateScrollToPage(page, animationSpec = pageAnimationSpec)
        }
    }

    BackHandler(enabled = showAddBooksSheet) {
        showAddBooksSheet = false
    }

    BackHandler(enabled = !isGlass && showSettingsSheet) {
        showSettingsSheet = false
    }

    BackHandler(enabled = pagerState.currentPage != 0 && !showAddBooksSheet && !isReorderMode) {
        smoothScrollToPage(0)
    }

    LaunchedEffect(isGlass) {
        if (showSettingsSheet) {
            showSettingsSheet = false
        }
    }
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

    val privateBookIds by viewModel.privateBookIds.collectAsState()
    val currentHeroBook = remember(recentBooks, privateBookIds) {
        recentBooks.firstOrNull { it.isReadingStarted && it.id !in privateBookIds }
    }
    val hasPrivatePassword by viewModel.hasPrivatePassword.collectAsState()
    val isPrivateUnlocked by viewModel.isPrivateFolderUnlocked.collectAsState()
    var showSetupPrivatePasswordDialog by remember { mutableStateOf(false) }
    var pendingPrivateBookId by remember { mutableStateOf<String?>(null) }
    var showUnlockPrivatePasswordDialog by remember { mutableStateOf(false) }
    var showChangePrivatePasswordDialog by remember { mutableStateOf(false) }
    var showAddBooksToPrivateDialog by remember { mutableStateOf(false) }
    var privateFolderError by remember { mutableStateOf<String?>(null) }

    var selectedBookIds by remember { mutableStateOf(setOf<String>()) }
    val isSelectionMode = selectedBookIds.isNotEmpty()
    var showBatchCollectionDialog by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showHeroMenu by remember { mutableStateOf(false) }
    var rootCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var sortButtonCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    BackHandler(enabled = showSortMenu) {
        showSortMenu = false
    }

    BackHandler(enabled = showHeroMenu) {
        showHeroMenu = false
    }

    PredictiveBackHandler(enabled = isSelectionMode) { progress ->
        try {
            progress.collect { }
            selectedBookIds = emptySet()
        } catch (_: CancellationException) {}
    }

    val handleTogglePrivate: (Book) -> Unit = { book ->
        if (book.id in privateBookIds) {
            viewModel.removeBookFromPrivateFolder(book.id)
        } else {
            if (!hasPrivatePassword) {
                pendingPrivateBookId = book.id
                showSetupPrivatePasswordDialog = true
            } else {
                viewModel.addBooksToPrivateFolder(setOf(book.id))
            }
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
            val libraryPageOffset by remember(pagerState) {
                derivedStateOf {
                    val page = pagerState.currentPage
                    val offset = pagerState.currentPageOffsetFraction
                    if (page == 0) {
                        offset.coerceIn(0f, 1f)
                    } else if (page == 1 && offset < 0f) {
                        (1f + offset).coerceIn(0f, 1f)
                    } else {
                        1f
                    }
                }
            }
            if (libraryPageOffset < 0.999f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            val progress = libraryPageOffset
                            alpha = (1f - progress * 1.35f).coerceIn(0f, 1f)
                            translationX = -progress * size.width * 0.30f
                        }
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
                        onOpenSettings = {
                            smoothScrollToPage(2)
                        },
                        onHolidayBadgeClick = { origin ->
                            holidayBurstKey = System.currentTimeMillis()
                            holidayBurstOrigin = origin
                            triggerThemeHaptic(context)
                            com.aura.reader.util.HolidaySoundPlayer.playHolidaySound(context, holidaySeason)
                        }
                    )
                }
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
            } else {
                MorphingFloatingBottomBar(
                    isSettingsOpen = isSettingsOpen,
                    selectedMainTab = selectedMainTab,
                    selectedSettingsCategory = selectedSettingsCategory,
                    designStyle = readerSettings.appDesignStyle,
                    clarity = readerSettings.glassClarity,
                    isDark = isThemeDark,
                    onMyBooksClick = {
                        if (pagerState.currentPage != 0) {
                            smoothScrollToPage(0)
                        } else {
                            scope.launch { gridState.animateScrollToItem(0) }
                        }
                    },
                    onSearchClick = {
                        smoothScrollToPage(1)
                    },
                    onSettingsClick = {
                        smoothScrollToPage(2)
                    },
                    onBackFromSettingsClick = {
                        smoothScrollToPage(0)
                    },
                    onSelectSettingsCategory = { categoryIndex ->
                        smoothScrollToPage(2 + categoryIndex)
                    },
                    onAddBooksClick = {
                        if (pagerState.currentPage != 0) {
                            smoothScrollToPage(0)
                        }
                        showAddBooksSheet = true
                    }
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { rootCoordinates = it }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(hazeState)
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

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = !isReorderMode
            ) { page ->
                when (page) {
                    0 -> {
                        val showHeroCard = currentHeroBook != null &&
                                searchQuery.isBlank() &&
                                (activeCollectionFilter.type == CollectionFilterType.ALL || activeCollectionFilter.type == CollectionFilterType.READING) &&
                                librarySortOrder != LibrarySortOption.CUSTOM &&
                                !isReorderMode

                        val displayedRecentBooks = if (showHeroCard) {
                            filteredRecentBooks.filter { it.id != currentHeroBook.id }
                        } else {
                            filteredRecentBooks
                        }

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
                                                        installState = updateInstallState,
                                                        onDownloadAndInstall = {
                                                            viewModel.downloadAndInstallUpdate(context)
                                                        },
                                                        onInstallDownloaded = {
                                                            viewModel.installDownloadedUpdate(context)
                                                        },
                                                        onOpenUpdatePage = { url ->
                                                            viewModel.openUpdatePage(context, url)
                                                        }
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
                                                    hasSeries = remember(recentBooks, privateBookIds) { recentBooks.any { !it.series.isNullOrBlank() && it.id !in privateBookIds } },
                                                    userCollections = userCollections,
                                                    isGlass = isGlass,
                                                    isThemeDark = isThemeDark,
                                                    onFilterChange = { viewModel.setCollectionFilter(it) },
                                                    onDeleteCollection = { collectionToDelete = it },
                                                    onNewCollection = { showNewCollectionDialog = true },
                                                    onPrivateFolderClick = {
                                                        if (activeCollectionFilter.type == CollectionFilterType.PRIVATE_FOLDER) {
                                                            viewModel.lockPrivateFolder()
                                                        } else if (!hasPrivatePassword) {
                                                            showSetupPrivatePasswordDialog = true
                                                        } else if (isPrivateUnlocked) {
                                                            viewModel.setCollectionFilter(ActiveCollectionFilter(CollectionFilterType.PRIVATE_FOLDER))
                                                        } else {
                                                            privateFolderError = null
                                                            showUnlockPrivatePasswordDialog = true
                                                        }
                                                    },
                                                    trashCount = trashCount,
                                                    onTrashClick = {
                                                        if (activeCollectionFilter.type == CollectionFilterType.TRASH) {
                                                            viewModel.setCollectionFilter(ActiveCollectionFilter(CollectionFilterType.ALL))
                                                        } else {
                                                            viewModel.setCollectionFilter(ActiveCollectionFilter(CollectionFilterType.TRASH))
                                                        }
                                                    }
                                                )
                                            }

                                            if (activeCollectionFilter.type == CollectionFilterType.PRIVATE_FOLDER) {
                                                item(key = "private_folder_banner", span = { GridItemSpan(maxLineSpan) }) {
                                                    PrivateFolderBanner(
                                                        bookCount = filteredRecentBooks.size,
                                                        isGlass = isGlass,
                                                        isDark = isThemeDark,
                                                        onAddBooks = { showAddBooksToPrivateDialog = true },
                                                        onChangePassword = {
                                                            privateFolderError = null
                                                            showChangePrivatePasswordDialog = true
                                                        },
                                                        onLock = { viewModel.lockPrivateFolder() },
                                                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                                    )
                                                }
                                            }

                                            if (activeCollectionFilter.type == CollectionFilterType.TRASH) {
                                                item(key = "trash_banner", span = { GridItemSpan(maxLineSpan) }) {
                                                    TrashBanner(
                                                        bookCount = filteredRecentBooks.size,
                                                        isGlass = isGlass,
                                                        isDark = isThemeDark,
                                                        onClearTrash = { showClearTrashConfirmDialog = true },
                                                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                                    )
                                                }
                                            }

                                            if (showHeroCard) {
                                                val heroBook = currentHeroBook ?: return@LazyVerticalGrid
                                                item(key = "currently_reading_hero", span = { GridItemSpan(maxLineSpan) }) {
                                                    CurrentlyReadingHeroCard(
                                                        book = heroBook,
                                                        onBookClick = onBookSelected,
                                                        onDelete = { bookToDelete = heroBook },
                                                        onToggleFavorite = { viewModel.toggleFavorite(heroBook.id) },
                                                        onAddToCollection = { bookForCollections = heroBook },
                                                        onSetProgress = { prog -> viewModel.setBookReadingProgress(heroBook.id, prog) },
                                                        onCoverClick = { selectedBookForProfile = heroBook },
                                                        onShare = { BookShareUtils.shareBookFile(context, heroBook) },
                                                        onChangeCover = { bookForCoverChange = heroBook },
                                                        isPrivate = heroBook.id in privateBookIds,
                                                        onTogglePrivate = { handleTogglePrivate(heroBook) },
                                                        onOpenMenu = { showHeroMenu = true },
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
                                                        MaterialTheme.colorScheme.surfaceContainerHigh
                                                    }
                                                    val buttonBorder = if (isGlass) {
                                                        BorderStroke(0.5.dp, if (isThemeDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.10f))
                                                    } else {
                                                        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.50f))
                                                    }

                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        // 1. Sort Button [ ≡ Последние ]
                                                        if (searchQuery.isBlank()) {
                                                            val sortLabel = when (librarySortOrder) {
                                                                LibrarySortOption.RECENT -> strings.sortByRecent
                                                                LibrarySortOption.TITLE -> strings.sortByTitle
                                                                LibrarySortOption.AUTHOR -> strings.sortByAuthor
                                                                LibrarySortOption.SERIES -> strings.sortBySeries
                                                                LibrarySortOption.CUSTOM -> strings.sortCustomOrder
                                                            }
                                                            Box(
                                                                modifier = Modifier.onGloballyPositioned { sortButtonCoordinates = it }
                                                            ) {
                                                                ExpressiveSplitButton(
                                                                    leadingContent = {
                                                                        Icon(
                                                                            imageVector = Icons.AutoMirrored.Rounded.Sort,
                                                                            contentDescription = strings.sortTitle,
                                                                            modifier = Modifier.size(20.dp)
                                                                        )
                                                                        Text(
                                                                            text = sortLabel,
                                                                            style = MaterialTheme.typography.labelLarge
                                                                        )
                                                                    },
                                                                    trailingContent = {
                                                                        Icon(
                                                                            imageVector = if (libraryViewMode == "GRID") Icons.AutoMirrored.Rounded.ViewList else Icons.Rounded.GridView,
                                                                            contentDescription = if (libraryViewMode == "GRID") strings.viewModeList else strings.viewModeGrid,
                                                                            tint = MaterialTheme.colorScheme.onSurface,
                                                                            modifier = Modifier.size(22.dp)
                                                                        )
                                                                    },
                                                                    onLeadingClick = { showSortMenu = true },
                                                                    onTrailingClick = { viewModel.toggleLibraryViewMode() },
                                                                    containerColor = buttonBg,
                                                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                                                    border = buttonBorder,
                                                                    size = ExpressiveButtonSize.Small
                                                                )
                                                            }
                                                        }

                                                        if (displayedRecentBooks.isNotEmpty() && librarySortOrder == LibrarySortOption.CUSTOM && !isSelectionMode) {
                                                            Surface(
                                                                shape = CircleShape,
                                                                color = buttonBg,
                                                                border = buttonBorder,
                                                                onClick = {
                                                                    selectedBookIds = setOf(displayedRecentBooks.first().id)
                                                                },
                                                                modifier = Modifier.size(36.dp)
                                                            ) {
                                                                Box(contentAlignment = Alignment.Center) {
                                                                    Icon(
                                                                        imageVector = Icons.Rounded.Checklist,
                                                                        contentDescription = strings.selectBooks,
                                                                        tint = MaterialTheme.colorScheme.onSurface,
                                                                        modifier = Modifier.size(18.dp)
                                                                    )
                                                                }
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
                                            } else if (displayedRecentBooks.isEmpty() && activeCollectionFilter.type == CollectionFilterType.PRIVATE_FOLDER) {
                                                item(span = { GridItemSpan(maxLineSpan) }) {
                                                    Surface(
                                                        shape = RoundedCornerShape(16.dp),
                                                        color = if (isGlass) {
                                                            if (isThemeDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f)
                                                        } else {
                                                            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.4f)
                                                        },
                                                        border = BorderStroke(
                                                            0.5.dp,
                                                            if (isGlass) {
                                                                if (isThemeDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)
                                                            } else {
                                                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                                            }
                                                        ),
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(vertical = 16.dp)
                                                    ) {
                                                        Column(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(24.dp),
                                                            horizontalAlignment = Alignment.CenterHorizontally,
                                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Rounded.LockOpen,
                                                                contentDescription = null,
                                                                tint = MaterialTheme.colorScheme.primary,
                                                                modifier = Modifier.size(40.dp)
                                                            )
                                                            Text(
                                                                text = strings.privateFolderEmptyTitle,
                                                                style = MaterialTheme.typography.titleMedium,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                            Text(
                                                                text = strings.privateFolderEmptyDesc,
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                            )
                                                        }
                                                    }
                                                }
                                            } else if (displayedRecentBooks.isEmpty() && activeCollectionFilter.type == CollectionFilterType.TRASH) {
                                                item(span = { GridItemSpan(maxLineSpan) }) {
                                                    Surface(
                                                        shape = RoundedCornerShape(16.dp),
                                                        color = if (isGlass) {
                                                            if (isThemeDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f)
                                                        } else {
                                                            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.4f)
                                                        },
                                                        border = BorderStroke(
                                                            0.5.dp,
                                                            if (isGlass) {
                                                                if (isThemeDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)
                                                            } else {
                                                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                                            }
                                                        ),
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(vertical = 16.dp)
                                                    ) {
                                                        Column(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(24.dp),
                                                            horizontalAlignment = Alignment.CenterHorizontally,
                                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Rounded.DeleteOutline,
                                                                contentDescription = null,
                                                                tint = MaterialTheme.colorScheme.outline,
                                                                modifier = Modifier.size(40.dp)
                                                            )
                                                            Text(
                                                                text = strings.trashEmptyTitle,
                                                                style = MaterialTheme.typography.titleMedium,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                            Text(
                                                                text = strings.trashEmptyDesc,
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            val canReorder = librarySortOrder == LibrarySortOption.CUSTOM && !isSelectionMode && activeCollectionFilter.type != CollectionFilterType.TRASH

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
                                                        isSelectionMode = isSelectionMode,
                                                        isSelected = book.id in selectedBookIds,
                                                        canReorder = canReorder,
                                                        onToggleSelect = {
                                                            selectedBookIds = if (book.id in selectedBookIds) {
                                                                selectedBookIds - book.id
                                                            } else {
                                                                selectedBookIds + book.id
                                                            }
                                                        },
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
                                                        onDelete = {
                                                            if (activeCollectionFilter.type == CollectionFilterType.TRASH) {
                                                                bookToDeletePermanently = book
                                                            } else {
                                                                viewModel.moveToTrash(book.id)
                                                                scope.launch {
                                                                    val res = snackbarHostState.showSnackbar(
                                                                        message = strings.movedToTrashSnackbar,
                                                                        actionLabel = strings.undo,
                                                                        duration = SnackbarDuration.Short
                                                                    )
                                                                    if (res == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                                                                        viewModel.restoreFromTrash(book.id)
                                                                    }
                                                                }
                                                            }
                                                        },
                                                        onRestore = {
                                                             viewModel.restoreFromTrash(book.id)
                                                             scope.launch {
                                                                 snackbarHostState.showSnackbar(strings.restoredFromTrashSnackbar)
                                                             }
                                                        },
                                                        onDeletePermanently = {
                                                            bookToDeletePermanently = book
                                                        },
                                                        onOpenProfile = {
                                                            selectedBookForProfile = book
                                                        },
                                                        onToggleFavorite = { viewModel.toggleFavorite(book.id) },
                                                        onAddToCollection = { bookForCollections = book },
                                                        onSetProgress = { prog -> viewModel.setBookReadingProgress(book.id, prog) },
                                                        onCoverClick = { inspectingCoverBook = book },
                                                        onShare = { BookShareUtils.shareBookFile(context, book) },
                                                        onChangeCover = { bookForCoverChange = book },
                                                        isPrivate = book.id in privateBookIds,
                                                        onTogglePrivate = { handleTogglePrivate(book) }
                                                    )
                                                } else {
                                                    BookCard(
                                                        book = book,
                                                        index = index,
                                                        isGlass = isGlass,
                                                        isDark = isThemeDark,
                                                        isReorderMode = isReorderMode,
                                                        isSelectionMode = isSelectionMode,
                                                        isSelected = book.id in selectedBookIds,
                                                        canReorder = canReorder,
                                                        onToggleSelect = {
                                                            selectedBookIds = if (book.id in selectedBookIds) {
                                                                selectedBookIds - book.id
                                                            } else {
                                                                selectedBookIds + book.id
                                                            }
                                                        },
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
                                                        onDelete = {
                                                            if (activeCollectionFilter.type == CollectionFilterType.TRASH) {
                                                                bookToDeletePermanently = book
                                                            } else {
                                                                viewModel.moveToTrash(book.id)
                                                                scope.launch {
                                                                    val res = snackbarHostState.showSnackbar(
                                                                        message = strings.movedToTrashSnackbar,
                                                                        actionLabel = strings.undo,
                                                                        duration = SnackbarDuration.Short
                                                                    )
                                                                    if (res == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                                                                        viewModel.restoreFromTrash(book.id)
                                                                    }
                                                                }
                                                            }
                                                        },
                                                        onRestore = {
                                                             viewModel.restoreFromTrash(book.id)
                                                             scope.launch {
                                                                 snackbarHostState.showSnackbar(strings.restoredFromTrashSnackbar)
                                                             }
                                                        },
                                                        onDeletePermanently = {
                                                            bookToDeletePermanently = book
                                                        },
                                                        onOpenProfile = {
                                                            selectedBookForProfile = book
                                                        },
                                                        onToggleFavorite = { viewModel.toggleFavorite(book.id) },
                                                        onAddToCollection = { bookForCollections = book },
                                                        onSetProgress = { prog -> viewModel.setBookReadingProgress(book.id, prog) },
                                                        onCoverClick = { inspectingCoverBook = book },
                                                        onShare = { BookShareUtils.shareBookFile(context, book) },
                                                        onChangeCover = { bookForCoverChange = book },
                                                        isPrivate = book.id in privateBookIds,
                                                        onTogglePrivate = { handleTogglePrivate(book) }
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
                            books = remember(recentBooks, privateBookIds) { recentBooks.filter { it.deletedAtTimestamp == null && it.id !in privateBookIds } },
                            onBookClick = onBookSelected,
                            onDeleteBook = { book ->
                                viewModel.moveToTrash(book.id)
                                scope.launch {
                                    val res = snackbarHostState.showSnackbar(
                                        message = strings.movedToTrashSnackbar,
                                        actionLabel = strings.undo,
                                        duration = SnackbarDuration.Short
                                    )
                                    if (res == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                                        viewModel.restoreFromTrash(book.id)
                                    }
                                }
                            },
                            onToggleFavorite = { book -> viewModel.toggleFavorite(book.id) },
                            onAddToCollection = { book -> bookForCollections = book },
                            onSetProgress = { book, prog -> viewModel.setBookReadingProgress(book.id, prog) },
                            onCoverClick = { book -> inspectingCoverBook = book },
                            onShare = { book -> BookShareUtils.shareBookFile(context, book) },
                            onChangeCover = { book -> bookForCoverChange = book },
                            onBack = { smoothScrollToPage(0) },
                            libraryViewMode = libraryViewMode,
                            isDark = isThemeDark,
                            clarity = readerSettings.glassClarity
                        )
                    }
                    2 -> {
                        SettingsInterfacePage(
                            appSettings = appSettings,
                            onUpdateSettings = { viewModel.updateAppSettings(it) },
                            syncThemesWithApp = readerSettings.syncThemesWithApp,
                            onSyncThemesChange = { viewModel.setSyncThemesWithApp(it) },
                            onOpenReaderThemeSettings = {
                                showReaderThemeSheet = true
                            }
                        )
                    }
                    3 -> {
                        val booksWithoutCoversCount = remember(recentBooks) {
                            recentBooks.count { it.deletedAtTimestamp == null && it.coverBase64.isNullOrBlank() }
                        }
                        val booksWithoutMetadataCount = remember(recentBooks) {
                            recentBooks.count { it.deletedAtTimestamp == null && (it.description.isNullOrBlank() || it.genres.isEmpty()) }
                        }
                        var cacheSizeString by remember {
                            mutableStateOf(
                                com.aura.reader.data.storage.AppStorageManager.formatSize(
                                    com.aura.reader.data.storage.AppStorageManager.getCacheSizeBytes(context)
                                )
                            )
                        }
                        SettingsGeneralPage(
                            appSettings = appSettings,
                            onUpdateSettings = { viewModel.updateAppSettings(it) },
                            onResetReadingSpeed = { viewModel.resetReadingSpeed() },
                            updateCheckState = updateCheckState,
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
                            onDownloadMissingCovers = {
                                Toast.makeText(context, strings.downloadMissingCovers + "...", Toast.LENGTH_SHORT).show()
                                viewModel.autoDownloadMissingCovers(recentBooks) { count ->
                                    Toast.makeText(context, strings.coversDownloadedSuccess(count), Toast.LENGTH_SHORT).show()
                                }
                            },
                            onDownloadMissingMetadata = {
                                Toast.makeText(context, strings.downloadMissingMetadata + "...", Toast.LENGTH_SHORT).show()
                                viewModel.autoEnrichMissingMetadata(recentBooks) { count ->
                                    Toast.makeText(context, strings.metadataDownloadedSuccess(count), Toast.LENGTH_SHORT).show()
                                }
                            },
                            booksWithoutCoversCount = booksWithoutCoversCount,
                            booksWithoutMetadataCount = booksWithoutMetadataCount,
                            onClearCache = {
                                com.aura.reader.data.storage.AppStorageManager.clearAllCache(context)
                                cacheSizeString = com.aura.reader.data.storage.AppStorageManager.formatSize(
                                    com.aura.reader.data.storage.AppStorageManager.getCacheSizeBytes(context)
                                )
                                Toast.makeText(context, strings.clearAppCacheSuccess, Toast.LENGTH_SHORT).show()
                            },
                            cacheSizeString = cacheSizeString
                        )
                    }
                    4 -> {
                        SettingsAboutPage(
                            appDesignStyle = readerSettings.appDesignStyle,
                            onOpenOnboarding = onOpenOnboarding
                        )
                    }
                }
            }
        }

        // --- In-Window Sort Menu Overlay (placed outside hazeSource to sample genuine blur) ---
        AnimatedVisibility(
            visible = showSortMenu,
            enter = fadeIn(tween(200, easing = FastOutSlowInEasing)),
            exit = fadeOut(tween(160, easing = FastOutSlowInEasing))
        ) {
            val menuWidthDp = 240.dp
            val estimatedMenuHeightDp = 250.dp
            val menuWidthPx = with(density) { menuWidthDp.toPx() }
            val estimatedMenuHeightPx = with(density) { estimatedMenuHeightDp.toPx() }
            val gapPx = with(density) { 6.dp.toPx() }
            val marginPx = with(density) { 16.dp.toPx() }

            val isAttached = rootCoordinates?.isAttached == true && sortButtonCoordinates?.isAttached == true
            val (menuX, menuY, isBelow) = remember(showSortMenu, rootCoordinates, sortButtonCoordinates) {
                if (isAttached) {
                    val root = rootCoordinates!!
                    val btn = sortButtonCoordinates!!
                    val btnPos = root.localPositionOf(btn, Offset.Zero)
                    val btnSize = btn.size
                    val rootSize = root.size

                    val btnRight = btnPos.x + btnSize.width
                    val btnBottom = btnPos.y + btnSize.height
                    val btnTop = btnPos.y

                    val targetX = (btnRight - menuWidthPx).coerceIn(marginPx, (rootSize.width - menuWidthPx - marginPx).coerceAtLeast(marginPx))
                    val spaceBelow = rootSize.height - btnBottom
                    val below = spaceBelow >= estimatedMenuHeightPx + marginPx
                    val targetY = if (below) {
                        btnBottom + gapPx
                    } else {
                        (btnTop - estimatedMenuHeightPx - gapPx).coerceAtLeast(marginPx)
                    }
                    Triple(targetX, targetY, below)
                } else {
                    Triple(marginPx, with(density) { 140.dp.toPx() }, true)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { showSortMenu = false }
            ) {
                Box(
                    modifier = Modifier
                        .offset { IntOffset(menuX.roundToInt(), menuY.roundToInt()) }
                        .width(menuWidthDp)
                ) {
                    AnimatedVisibility(
                        visible = showSortMenu,
                        enter = fadeIn(tween(220, easing = FastOutSlowInEasing)) + scaleIn(
                            initialScale = 0.85f,
                            transformOrigin = if (isBelow) TransformOrigin(0.9f, 0.05f) else TransformOrigin(0.9f, 0.95f),
                            animationSpec = tween(220, easing = FastOutSlowInEasing)
                        ),
                        exit = fadeOut(tween(160, easing = FastOutSlowInEasing)) + scaleOut(
                            targetScale = 0.85f,
                            transformOrigin = if (isBelow) TransformOrigin(0.9f, 0.05f) else TransformOrigin(0.9f, 0.95f),
                            animationSpec = tween(160, easing = FastOutSlowInEasing)
                        )
                    ) {
                        LiquidGlassMenuContainer(
                            isGlass = isGlass,
                            isDark = isThemeDark,
                            isSepia = isSepiaTheme,
                            hazeState = hazeState
                        ) {
                            val isRecent = librarySortOrder == LibrarySortOption.RECENT
                            LiquidGlassMenuItem(
                                text = strings.sortByRecent,
                                leadingIcon = {
                                    Icon(Icons.AutoMirrored.Rounded.Sort, contentDescription = null, tint = if (isRecent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                                },
                                trailingIcon = if (isRecent) {
                                    { Icon(Icons.Rounded.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) }
                                } else null,
                                textColor = if (isRecent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isRecent) FontWeight.SemiBold else FontWeight.Normal,
                                onClick = {
                                    viewModel.setLibrarySortOrder(LibrarySortOption.RECENT)
                                    showSortMenu = false
                                }
                            )
                            val isTitle = librarySortOrder == LibrarySortOption.TITLE
                            LiquidGlassMenuItem(
                                text = strings.sortByTitle,
                                leadingIcon = {
                                    Icon(Icons.Rounded.Title, contentDescription = null, tint = if (isTitle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                                },
                                trailingIcon = if (isTitle) {
                                    { Icon(Icons.Rounded.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) }
                                } else null,
                                textColor = if (isTitle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isTitle) FontWeight.SemiBold else FontWeight.Normal,
                                onClick = {
                                    viewModel.setLibrarySortOrder(LibrarySortOption.TITLE)
                                    showSortMenu = false
                                }
                            )
                            val isAuthor = librarySortOrder == LibrarySortOption.AUTHOR
                            LiquidGlassMenuItem(
                                text = strings.sortByAuthor,
                                leadingIcon = {
                                    Icon(Icons.Rounded.Person, contentDescription = null, tint = if (isAuthor) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                                },
                                trailingIcon = if (isAuthor) {
                                    { Icon(Icons.Rounded.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) }
                                } else null,
                                textColor = if (isAuthor) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isAuthor) FontWeight.SemiBold else FontWeight.Normal,
                                onClick = {
                                    viewModel.setLibrarySortOrder(LibrarySortOption.AUTHOR)
                                    showSortMenu = false
                                }
                            )
                            val isSeries = librarySortOrder == LibrarySortOption.SERIES
                            LiquidGlassMenuItem(
                                text = strings.sortBySeries,
                                leadingIcon = {
                                    Icon(Icons.Rounded.AutoStories, contentDescription = null, tint = if (isSeries) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                                },
                                trailingIcon = if (isSeries) {
                                    { Icon(Icons.Rounded.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) }
                                } else null,
                                textColor = if (isSeries) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSeries) FontWeight.SemiBold else FontWeight.Normal,
                                onClick = {
                                    viewModel.setLibrarySortOrder(LibrarySortOption.SERIES)
                                    showSortMenu = false
                                }
                            )
                            val isCustom = librarySortOrder == LibrarySortOption.CUSTOM
                            LiquidGlassMenuItem(
                                text = strings.sortCustomOrder,
                                leadingIcon = {
                                    Icon(Icons.Rounded.CollectionsBookmark, contentDescription = null, tint = if (isCustom) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                                },
                                trailingIcon = if (isCustom) {
                                    { Icon(Icons.Rounded.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) }
                                } else null,
                                textColor = if (isCustom) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isCustom) FontWeight.SemiBold else FontWeight.Normal,
                                onClick = {
                                    viewModel.setLibrarySortOrder(LibrarySortOption.CUSTOM)
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // --- In-Window Hero Card Menu Overlay (outside hazeSource to sample genuine blur) ---
        val heroBook = currentHeroBook
        if (heroBook != null) {
            val heroHasCover = !heroBook.coverBase64.isNullOrBlank()
            val heroIsPrivate = heroBook.id in privateBookIds

            AnimatedVisibility(
                visible = showHeroMenu,
                enter = fadeIn(tween(220, easing = FastOutSlowInEasing)),
                exit = fadeOut(tween(180, easing = FastOutSlowInEasing))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { showHeroMenu = false }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        AnimatedVisibility(
                            visible = showHeroMenu,
                            enter = fadeIn(tween(240, easing = FastOutSlowInEasing)) + scaleIn(
                                initialScale = 0.80f,
                                transformOrigin = TransformOrigin(0.95f, 0.05f),
                                animationSpec = tween(240, easing = FastOutSlowInEasing)
                            ),
                            exit = fadeOut(tween(180, easing = FastOutSlowInEasing)) + scaleOut(
                                targetScale = 0.82f,
                                transformOrigin = TransformOrigin(0.95f, 0.05f),
                                animationSpec = tween(180, easing = FastOutSlowInEasing)
                            ),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 130.dp, end = 8.dp)
                                .width(260.dp)
                        ) {
                            LiquidGlassMenuContainer(
                                isGlass = isGlass,
                                isDark = isThemeDark,
                                isSepia = isSepiaTheme,
                                hazeState = hazeState
                            ) {
                                LiquidGlassMenuItem(
                                    text = strings.shareBookFile,
                                    leadingIcon = {
                                        Icon(Icons.Rounded.Share, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    },
                                    onClick = {
                                        showHeroMenu = false
                                        BookShareUtils.shareBookFile(context, heroBook)
                                    }
                                )
                                LiquidGlassMenuItem(
                                    text = if (heroHasCover) strings.changeCover else strings.downloadCover,
                                    leadingIcon = {
                                        Icon(if (heroHasCover) Icons.Rounded.Image else Icons.Rounded.Download, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    },
                                    onClick = {
                                        showHeroMenu = false
                                        bookForCoverChange = heroBook
                                    }
                                )
                                LiquidGlassMenuItem(
                                    text = strings.addToCollection,
                                    leadingIcon = {
                                        Icon(Icons.Rounded.FolderOpen, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    },
                                    onClick = {
                                        showHeroMenu = false
                                        bookForCollections = heroBook
                                    }
                                )
                                LiquidGlassMenuItem(
                                    text = if (heroIsPrivate) strings.privateFolderRemoveBook else strings.privateFolderMoveToPrivate,
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (heroIsPrivate) Icons.Rounded.LockOpen else Icons.Rounded.Lock,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    onClick = {
                                        showHeroMenu = false
                                        handleTogglePrivate(heroBook)
                                    }
                                )
                                if (heroBook.progressPercent < 100) {
                                    LiquidGlassMenuItem(
                                        text = strings.markAsFinished,
                                        leadingIcon = {
                                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        },
                                        onClick = {
                                            showHeroMenu = false
                                            viewModel.setBookReadingProgress(heroBook.id, 100)
                                        }
                                    )
                                }
                                if (heroBook.progressPercent > 0) {
                                    LiquidGlassMenuItem(
                                        text = strings.resetProgress,
                                        leadingIcon = {
                                            Icon(Icons.Rounded.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        },
                                        onClick = {
                                            showHeroMenu = false
                                            viewModel.setBookReadingProgress(heroBook.id, 0)
                                        }
                                    )
                                }
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                                LiquidGlassMenuItem(
                                    text = strings.delete,
                                    textColor = MaterialTheme.colorScheme.error,
                                    leadingIcon = {
                                        Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    },
                                    onClick = {
                                        showHeroMenu = false
                                        bookToDelete = heroBook
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Multi-Select Floating Action Bar (Placed outside hazeSource to sample genuine blur) ---
        AnimatedVisibility(
            visible = isSelectionMode,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 96.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth()
        ) {
            MultiSelectBarContainer(
                isGlass = isGlass,
                isThemeDark = isThemeDark,
                isSepia = isSepiaTheme,
                hazeState = hazeState,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { selectedBookIds = emptySet() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = strings.cancel,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.selectedCount(selectedBookIds.size),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        val allDisplayedSelected = filteredRecentBooks.isNotEmpty() &&
                            filteredRecentBooks.all { it.id in selectedBookIds }
                        TextButton(
                            onClick = {
                                selectedBookIds = if (allDisplayedSelected) {
                                    emptySet()
                                } else {
                                    filteredRecentBooks.map { it.id }.toSet()
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (allDisplayedSelected) strings.deselectAll else strings.selectAll,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isTrashView = activeCollectionFilter.type == CollectionFilterType.TRASH

                        if (isTrashView) {
                            MultiSelectActionButton(
                                icon = Icons.Rounded.Restore,
                                label = strings.batchRestore,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    val count = selectedBookIds.size
                                    viewModel.batchRestoreFromTrash(selectedBookIds)
                                    selectedBookIds = emptySet()
                                    scope.launch {
                                        snackbarHostState.showSnackbar(strings.restoredFromTrashSnackbar)
                                    }
                                }
                            )

                            MultiSelectActionButton(
                                icon = Icons.Rounded.DeleteForever,
                                label = strings.batchDeletePermanently,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    val toDelete = selectedBookIds
                                    viewModel.batchDeletePermanently(toDelete)
                                    selectedBookIds = emptySet()
                                    scope.launch {
                                        snackbarHostState.showSnackbar(strings.deletePermanently)
                                    }
                                }
                            )
                        } else {
                            MultiSelectActionButton(
                                icon = Icons.Rounded.FolderOpen,
                                label = strings.batchAddToCollection,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f),
                                onClick = { showBatchCollectionDialog = true }
                            )

                            val allArePrivate = selectedBookIds.isNotEmpty() && selectedBookIds.all { it in privateBookIds }
                            MultiSelectActionButton(
                                icon = if (allArePrivate) Icons.Rounded.LockOpen else Icons.Rounded.Lock,
                                label = if (allArePrivate) strings.batchRemoveFromPrivate else strings.batchMakePrivate,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    val count = selectedBookIds.size
                                    viewModel.batchTogglePrivate(selectedBookIds, !allArePrivate)
                                    selectedBookIds = emptySet()
                                    scope.launch {
                                        val msg = if (allArePrivate) strings.batchUnprivateSnackbar(count) else strings.batchPrivateSnackbar(count)
                                        snackbarHostState.showSnackbar(msg)
                                    }
                                }
                            )

                            val allRead = selectedBookIds.isNotEmpty() && filteredRecentBooks.filter { it.id in selectedBookIds }.all { it.progressPercent >= 100 }
                            MultiSelectActionButton(
                                icon = if (allRead) Icons.Rounded.Refresh else Icons.Rounded.CheckCircle,
                                label = if (allRead) strings.batchMarkUnread else strings.batchMarkRead,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    val count = selectedBookIds.size
                                    val newProgress = if (allRead) 0 else 100
                                    viewModel.batchSetReadingProgress(selectedBookIds, newProgress)
                                    selectedBookIds = emptySet()
                                    scope.launch {
                                        snackbarHostState.showSnackbar(strings.batchMarkedReadSnackbar(count))
                                    }
                                }
                            )

                            MultiSelectActionButton(
                                icon = Icons.Rounded.Delete,
                                label = strings.batchMoveToTrash,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    val count = selectedBookIds.size
                                    val deletedIds = selectedBookIds
                                    viewModel.batchMoveToTrash(deletedIds)
                                    selectedBookIds = emptySet()
                                    scope.launch {
                                        val res = snackbarHostState.showSnackbar(
                                            message = strings.batchMovedToTrashSnackbar(count),
                                            actionLabel = strings.undo,
                                            duration = SnackbarDuration.Short
                                        )
                                        if (res == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                                            viewModel.batchRestoreFromTrash(deletedIds)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

    LibraryDialogs(
        updateInfo = updateInfo,
        updateInstallState = updateInstallState,
        showNewCollectionDialog = showNewCollectionDialog,
        collectionToDelete = collectionToDelete,
        bookForCollections = bookForCollections,
        userCollections = userCollections,
        onDismissUpdate = { viewModel.dismissUpdateDialog() },
        onDownloadAndInstall = { viewModel.downloadAndInstallUpdate(context) },
        onInstallDownloaded = { viewModel.installDownloadedUpdate(context) },
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

    if (!isGlass && showSettingsSheet) {
        SettingsBottomSheet(
            appSettings = appSettings,
            onUpdateSettings = { viewModel.updateAppSettings(it) },
            readerSettings = readerSettings,
            onUpdateReaderSettings = { transform ->
                val next = transform(readerSettings)
                if (next.syncThemesWithApp != readerSettings.syncThemesWithApp) {
                    viewModel.setSyncThemesWithApp(next.syncThemesWithApp)
                }
            },
            onDismiss = { showSettingsSheet = false },
            updateCheckState = updateCheckState,
            onCheckUpdates = {
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
            onOpenReaderThemeSettings = {
                showSettingsSheet = false
                showReaderThemeSheet = true
            },
            onResetReadingSpeed = { viewModel.resetReadingSpeed() },
            onOpenOnboarding = onOpenOnboarding
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

    if (showSetupPrivatePasswordDialog) {
        SetupPrivatePasswordDialog(
            onDismiss = {
                showSetupPrivatePasswordDialog = false
                pendingPrivateBookId = null
            },
            onSavePassword = { password ->
                viewModel.setPrivateFolderPassword(password) {
                    showSetupPrivatePasswordDialog = false
                    pendingPrivateBookId?.let { bookId ->
                        viewModel.addBooksToPrivateFolder(setOf(bookId))
                        pendingPrivateBookId = null
                    }
                }
            }
        )
    }

    if (showUnlockPrivatePasswordDialog) {
        UnlockPrivateFolderDialog(
            errorMessage = privateFolderError,
            onDismiss = {
                showUnlockPrivatePasswordDialog = false
                privateFolderError = null
            },
            onUnlock = { password ->
                viewModel.unlockPrivateFolder(
                    password = password,
                    onSuccess = {
                        showUnlockPrivatePasswordDialog = false
                        privateFolderError = null
                    },
                    onError = {
                        privateFolderError = strings.privateFolderWrongPassword
                    }
                )
            }
        )
    }

    if (showChangePrivatePasswordDialog) {
        ChangePrivatePasswordDialog(
            errorMessage = privateFolderError,
            onDismiss = {
                showChangePrivatePasswordDialog = false
                privateFolderError = null
            },
            onChangePassword = { oldPassword, newPassword ->
                viewModel.changePrivateFolderPassword(
                    oldPassword = oldPassword,
                    newPassword = newPassword,
                    onSuccess = {
                        showChangePrivatePasswordDialog = false
                        privateFolderError = null
                        scope.launch {
                            snackbarHostState.showSnackbar(strings.privateFolderPasswordChanged)
                        }
                    },
                    onError = {
                        privateFolderError = strings.privateFolderWrongPassword
                    }
                )
            }
        )
    }

    if (showAddBooksToPrivateDialog) {
        val availableBooksForPrivate = remember(recentBooks, privateBookIds) {
            recentBooks.filter { it.id !in privateBookIds }
        }
        AddBooksToPrivateFolderDialog(
            availableBooks = availableBooksForPrivate,
            onDismiss = { showAddBooksToPrivateDialog = false },
            onConfirm = { selectedIds ->
                viewModel.addBooksToPrivateFolder(selectedIds)
                showAddBooksToPrivateDialog = false
            }
        )
    }

    bookToDeletePermanently?.let { book ->
        AlertDialog(
            onDismissRequest = { bookToDeletePermanently = null },
            title = { Text(strings.deletePermanentlyConfirmTitle, fontWeight = FontWeight.Bold) },
            text = { Text(strings.deletePermanentlyConfirmDesc) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePermanently(book.id)
                        bookToDeletePermanently = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(strings.deletePermanently, color = MaterialTheme.colorScheme.onError)
                }
            },
            dismissButton = {
                TextButton(onClick = { bookToDeletePermanently = null }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    if (showClearTrashConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearTrashConfirmDialog = false },
            title = { Text(strings.clearTrashConfirmTitle, fontWeight = FontWeight.Bold) },
            text = { Text(strings.clearTrashConfirmDesc) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearTrash()
                        showClearTrashConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(strings.clearTrash, color = MaterialTheme.colorScheme.onError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearTrashConfirmDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    selectedBookForProfile?.let { book ->
        BookProfileBottomSheet(
            book = book,
            onDismiss = { selectedBookForProfile = null },
            onSave = { title, author, series, seriesNumber, genres, description ->
                viewModel.updateBookMetadata(
                    bookId = book.id,
                    title = title,
                    author = author,
                    series = series,
                    seriesNumber = seriesNumber,
                    genres = genres,
                    description = description
                )
                selectedBookForProfile = null
                scope.launch {
                    snackbarHostState.showSnackbar(strings.metadataSaved)
                }
            },
            onOpenBook = {
                val target = book
                selectedBookForProfile = null
                onBookSelected(target)
            },
            onInspectCover = {
                val target = book
                inspectingCoverBook = target
            },
            onChangeCover = {
                val target = book
                bookForCoverChange = target
            },
            onToggleRead = {
                val newProgress = if (book.progressPercent >= 100) 0 else 100
                viewModel.setBookReadingProgress(book.id, newProgress)
            },
            isGlass = isGlass,
            isDark = isThemeDark
        )
    }

    // --- Batch Collection Dialog ---
    if (showBatchCollectionDialog) {
        val existingCollections by viewModel.userCollections.collectAsState()
        var newColName by remember { mutableStateOf("") }
        var showCreateNewField by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showBatchCollectionDialog = false },
            title = { Text(strings.batchAddToCollection) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = strings.selectedCount(selectedBookIds.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (existingCollections.isNotEmpty()) {
                        androidx.compose.foundation.layout.FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            existingCollections.forEach { col ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                                    onClick = {
                                        val count = selectedBookIds.size
                                        viewModel.batchAddToCollection(selectedBookIds, col)
                                        val msg = strings.batchAddedToCollectionSnackbar(count, col)
                                        showBatchCollectionDialog = false
                                        selectedBookIds = emptySet()
                                        scope.launch { snackbarHostState.showSnackbar(msg) }
                                    }
                                ) {
                                    Text(
                                        text = col,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                    if (!showCreateNewField) {
                        androidx.compose.material3.OutlinedButton(
                            onClick = { showCreateNewField = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.newCollection)
                        }
                    } else {
                        OutlinedTextField(
                            value = newColName,
                            onValueChange = { newColName = it },
                            label = { Text(strings.collectionNamePlaceholder) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                if (showCreateNewField) {
                    Button(
                        onClick = {
                            val name = newColName.trim()
                            if (name.isNotBlank()) {
                                viewModel.addUserCollection(name)
                                val count = selectedBookIds.size
                                viewModel.batchAddToCollection(selectedBookIds, name)
                                val msg = strings.batchAddedToCollectionSnackbar(count, name)
                                showBatchCollectionDialog = false
                                selectedBookIds = emptySet()
                                scope.launch { snackbarHostState.showSnackbar(msg) }
                            }
                        }
                    ) {
                        Text(strings.save)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatchCollectionDialog = false }) {
                    Text(strings.cancel)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
    }
}

@Composable
private fun MultiSelectActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
            color = tint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MultiSelectBarContainer(
    isGlass: Boolean,
    isThemeDark: Boolean,
    isSepia: Boolean,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (isGlass) {
        val tint = if (isSepia) Color(0x66FAF0E6) else null
        LiquidGlassSurface(
            shape = RoundedCornerShape(22.dp),
            shadowElevation = 10.dp,
            isDark = isThemeDark,
            tintColor = tint,
            hazeState = hazeState,
            modifier = modifier,
            content = content
        )
    } else {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSepia) Color(0xFFE6DDCF) else MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = modifier
        ) {
            content()
        }
    }
}

