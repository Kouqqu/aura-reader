package com.aura.reader.ui.screens.library

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.aura.reader.data.service.BookEnricher
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.reader.data.model.Book
import com.aura.reader.data.model.formatVolumeShort
import com.aura.reader.ui.theme.LocalAppStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val POPULAR_GENRES = listOf(
    "Фантастика",
    "Фэнтези",
    "Детектив",
    "Роман",
    "Приключения",
    "Научпоп",
    "Триллер",
    "Классика",
    "Ужасы",
    "Психология",
    "История",
    "Киберпанк",
    "Антиутопия",
    "Биография",
    "Поэзия"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BookProfileBottomSheet(
    book: Book,
    onDismiss: () -> Unit,
    onSave: (title: String, author: String, series: String?, seriesNumber: Int?, genres: List<String>, description: String?) -> Unit,
    onOpenBook: () -> Unit = {},
    onInspectCover: () -> Unit = {},
    onChangeCover: () -> Unit = {},
    onToggleRead: () -> Unit = {},
    isGlass: Boolean = false,
    isDark: Boolean = false
) {
    val strings = LocalAppStrings.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    var isEditMode by remember { mutableStateOf(false) }

    var title by remember(book) { mutableStateOf(book.title) }
    var author by remember(book) { mutableStateOf(book.author) }
    var series by remember(book) { mutableStateOf(book.series.orEmpty()) }
    var seriesNumber by remember(book) { mutableStateOf(book.seriesNumber?.toString().orEmpty()) }
    var genres by remember(book) { mutableStateOf(book.genres) }
    var description by remember(book) { mutableStateOf(book.description.orEmpty()) }
    var showAddGenreDialog by remember { mutableStateOf(false) }

    // Data loss protection
    val hasUnsavedChanges = remember(title, author, series, seriesNumber, genres, description, book) {
        title.trim() != book.title ||
            author.trim() != book.author ||
            series.trim() != book.series.orEmpty() ||
            seriesNumber.trim() != (book.seriesNumber?.toString().orEmpty()) ||
            genres != book.genres ||
            description.trim() != book.description.orEmpty()
    }
    var showDiscardConfirmDialog by remember { mutableStateOf(false) }
    var pendingDismissAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val coroutineScope = rememberCoroutineScope()
    var isFetchingOnlineMetadata by remember { mutableStateOf(false) }
    var fetchStatusMessage by remember { mutableStateOf<String?>(null) }

    val dateFormat = remember { SimpleDateFormat("d MMMM yyyy", Locale.getDefault()) }

    // 100% opaque obsidian palette: completely eliminates see-through background bleeding and blue tint
    val sheetBg = if (isDark) Color(0xFF141416) else Color(0xFFF7F7F9)
    val sheetContentColor = if (isDark) Color(0xFFF5F5F7) else Color(0xFF161618)
    val cardBg = if (isDark) Color(0xFF1C1C1F) else Color(0xFFFFFFFF)
    val cardBorder = if (isGlass) {
        if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
    } else {
        if (isDark) Color(0xFF28282D) else Color(0xFFE4E4E8)
    }
    val titleColor = if (isDark) Color.White else Color(0xFF111113)
    val subtitleColor = if (isDark) Color(0xFF8E8E93) else Color(0xFF6E6E73)
    val bodyColor = if (isDark) Color(0xFFD1D1D6) else Color(0xFF2C2C2E)

    // Back handler for unsaved changes in edit mode
    BackHandler(enabled = isEditMode) {
        if (hasUnsavedChanges) {
            pendingDismissAction = { isEditMode = false }
            showDiscardConfirmDialog = true
        } else {
            isEditMode = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            if (isEditMode && hasUnsavedChanges) {
                pendingDismissAction = { onDismiss() }
                showDiscardConfirmDialog = true
            } else {
                onDismiss()
            }
        },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = sheetBg,
        contentColor = sheetContentColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 36.dp)
                .animateContentSize()
        ) {
            if (!isEditMode) {
                // ==========================================
                // AURA READER BOOK PROFILE: VIEW MODE
                // ==========================================

                // Header: Cover + Book Meta + Compact Edit Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // Cover thumbnail: tap opens full-screen cover viewer
                    Box(
                        modifier = Modifier
                            .size(width = 96.dp, height = 138.dp)
                            .clip(RoundedCornerShape(14.dp))
                    ) {
                        BookCoverView(
                            coverBase64 = book.coverBase64,
                            title = book.title,
                            bookId = null, // null prevents sharedElement duplicate key collision crash
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { onInspectCover() }
                        )

                        // Inspect Cover Pill (bottom-start)
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.72f),
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(5.dp)
                                .size(28.dp)
                                .clickable { onInspectCover() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.ZoomIn,
                                    contentDescription = strings.viewCover,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.White
                                )
                            }
                        }

                        // Change Cover Pill (bottom-end)
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.72f),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(5.dp)
                                .size(28.dp)
                                .clickable { onChangeCover() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.AddPhotoAlternate,
                                    contentDescription = strings.changeCover,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp,
                                lineHeight = 25.sp
                            ),
                            color = titleColor,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (author.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = author,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                ),
                                color = subtitleColor,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        val volumeInfo = remember(book.charCount, book.wordCount) {
                            val short = book.formatVolumeShort()
                            if (short != null && book.wordCount > 0) "$short · ${book.wordCount} слов" else short
                        }
                        if (volumeInfo != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = volumeInfo,
                                style = MaterialTheme.typography.labelSmall,
                                color = subtitleColor
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Series & Format Pills
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f)
                            ) {
                                Text(
                                    text = book.format.name.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            if (series.isNotBlank()) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f)
                                ) {
                                    val seriesLabel = if (seriesNumber.isNotBlank()) "$series · #$seriesNumber" else series
                                    Text(
                                        text = seriesLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Compact edit button right in the header
                    IconButton(
                        onClick = { isEditMode = true },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = strings.editAction,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Card 1: Прогресс чтения
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${book.progressPercent}% прочитано",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = titleColor
                                )
                                val statusText = if (book.progressPercent >= 100) {
                                    strings.markAsFinished
                                } else if (book.progressPercent > 0) {
                                    strings.inProgressReading
                                } else {
                                    strings.neverOpened
                                }
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = subtitleColor
                                )
                            }

                            val isCompleted = book.progressPercent >= 100
                            OutlinedButton(
                                onClick = onToggleRead,
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isCompleted) Icons.Rounded.Refresh else Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isCompleted) strings.resetProgress else strings.markAsFinished,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { (book.progressPercent / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            strokeCap = StrokeCap.Round,
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = if (isGlass) {
                                if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
                            } else MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Card 2: Жанры (аккуратная и компактная плашка)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Category,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.genresSectionTitle,
                                style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.5.sp),
                                fontWeight = FontWeight.Bold,
                                color = titleColor
                            )
                        }

                        if (genres.isNotEmpty()) {
                            FlowRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                genres.forEach { genre ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isDark) Color(0xFF252529) else Color(0xFFEFEFF4),
                                        border = BorderStroke(0.5.dp, cardBorder)
                                    ) {
                                        Text(
                                            text = genre,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 11.5.sp
                                            ),
                                            color = bodyColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = strings.noGenresAdded,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = subtitleColor,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Card 3: Описание
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.bookDescriptionSectionTitle,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = titleColor
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        if (description.isNotBlank()) {
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                color = bodyColor
                            )
                        } else {
                            Text(
                                text = strings.bookDescriptionEmptyHint,
                                style = MaterialTheme.typography.bodySmall,
                                fontStyle = FontStyle.Italic,
                                color = subtitleColor
                            )
                        }
                    }
                }

                if (genres.isEmpty() && description.isBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            isFetchingOnlineMetadata = true
                            fetchStatusMessage = null
                            coroutineScope.launch {
                                val meta = BookEnricher.fetchMetadata(book.title, book.author)
                                isFetchingOnlineMetadata = false
                                if (meta != null) {
                                    val newDesc = meta.description ?: ""
                                    val newGenres = meta.genres
                                    val newSeries = meta.series ?: series
                                    val newSeriesNum = meta.seriesNumber ?: seriesNumber.toIntOrNull()
                                    if (newDesc.isNotBlank()) description = newDesc
                                    if (newGenres.isNotEmpty()) genres = newGenres
                                    if (newSeries.isNotBlank()) series = newSeries
                                    if (newSeriesNum != null) seriesNumber = newSeriesNum.toString()
                                    onSave(title, author, newSeries.takeIf { it.isNotBlank() }, newSeriesNum, newGenres, newDesc.takeIf { it.isNotBlank() })
                                    fetchStatusMessage = strings.metadataFound(meta.source)
                                } else {
                                    fetchStatusMessage = strings.metadataNotFound
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isFetchingOnlineMetadata,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isFetchingOnlineMetadata) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.fetchingMetadata)
                        } else {
                            Icon(Icons.Rounded.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.fetchFromNetwork)
                        }
                    }
                    fetchStatusMessage?.let { msg ->
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Card 4: Сведения о файле
                val addedDateStr = remember(book.addedTimestamp) { dateFormat.format(Date(book.addedTimestamp)) }
                val openedDateStr = remember(book.firstOpenedTimestamp) {
                    book.firstOpenedTimestamp?.let { dateFormat.format(Date(it)) } ?: strings.neverOpened
                }
                val lastReadDateStr = remember(book.lastReadTimestamp) {
                    if (book.lastReadTimestamp > 0) dateFormat.format(Date(book.lastReadTimestamp)) else strings.neverOpened
                }

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.metadataSectionTitle,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = titleColor
                            )
                        }

                        DetailInfoRow(
                            icon = Icons.Rounded.Storage,
                            label = "Формат и размер",
                            value = "${book.format.name.uppercase()} · ${formatFileSize(book.fileSizeBytes)}",
                            labelColor = subtitleColor,
                            valueColor = titleColor
                        )

                        DetailInfoRow(
                            icon = Icons.Rounded.CalendarToday,
                            label = strings.dateAddedLabel,
                            value = addedDateStr,
                            labelColor = subtitleColor,
                            valueColor = titleColor
                        )

                        DetailInfoRow(
                            icon = Icons.Rounded.AutoStories,
                            label = strings.firstOpenedLabel,
                            value = openedDateStr,
                            labelColor = subtitleColor,
                            valueColor = titleColor
                        )

                        if (book.progressPercent > 0) {
                            DetailInfoRow(
                                icon = Icons.Rounded.Refresh,
                                label = strings.lastReadLabel,
                                value = lastReadDateStr,
                                labelColor = subtitleColor,
                                valueColor = titleColor
                            )
                        }
                    }
                }

            } else {
                // ==========================================
                // AURA READER BOOK PROFILE: EDIT MODE
                // ==========================================

                // Header: ← Редактирование книги | Сохранить
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            if (hasUnsavedChanges) {
                                pendingDismissAction = { isEditMode = false }
                                showDiscardConfirmDialog = true
                            } else {
                                isEditMode = false
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowBack,
                                contentDescription = strings.cancel,
                                tint = titleColor
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = strings.editBookTitle,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = titleColor
                        )
                    }

                    Button(
                        onClick = {
                            onSave(
                                title.trim(),
                                author.trim(),
                                series.trim().ifBlank { null },
                                seriesNumber.trim().toIntOrNull(),
                                genres,
                                description.trim().ifBlank { null }
                            )
                            isEditMode = false
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = strings.saveAction,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }

                // Edit Mode Quick Action Bar: Change Cover & Fetch from Network
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onChangeCover,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AddPhotoAlternate,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.changeCover,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            isFetchingOnlineMetadata = true
                            fetchStatusMessage = null
                            coroutineScope.launch {
                                val meta = BookEnricher.fetchMetadata(title, author)
                                isFetchingOnlineMetadata = false
                                if (meta != null) {
                                    if (!meta.description.isNullOrBlank()) description = meta.description
                                    if (meta.genres.isNotEmpty()) genres = (genres + meta.genres).distinct()
                                    if (!meta.series.isNullOrBlank() && series.isBlank()) series = meta.series
                                    if (meta.seriesNumber != null && seriesNumber.isBlank()) seriesNumber = meta.seriesNumber.toString()
                                    fetchStatusMessage = strings.metadataFound(meta.source)
                                } else {
                                    fetchStatusMessage = strings.metadataNotFound
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isFetchingOnlineMetadata,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isFetchingOnlineMetadata) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.fetchingMetadata, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.CloudDownload,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.fetchFromNetwork,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                fetchStatusMessage?.let { msg ->
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 10.dp, start = 4.dp)
                    )
                }

                // Card 1: Название и Автор
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Книга",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text(strings.bookTitleLabel) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = author,
                            onValueChange = { author = it },
                            label = { Text(strings.bookAuthorLabel) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Card 2: Цикл / Серия
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = strings.seriesSectionTitle,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = strings.cycleSectionSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = subtitleColor
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = series,
                                onValueChange = { series = it },
                                label = { Text(strings.seriesNameLabel) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(2.5f)
                            )
                            OutlinedTextField(
                                value = seriesNumber,
                                onValueChange = { seriesNumber = it.filter { char -> char.isDigit() } },
                                label = { Text(strings.seriesNumberLabel) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1.2f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Card 3: Жанры
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.genresSectionTitle,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            TextButton(
                                onClick = { showAddGenreDialog = true },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = strings.addGenre,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }

                        if (genres.isNotEmpty()) {
                            FlowRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                genres.forEach { genre ->
                                    InputChip(
                                        selected = true,
                                        onClick = { genres = genres.filterNot { it == genre } },
                                        label = { Text(genre, fontSize = 12.sp) },
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Rounded.Close,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        },
                                        colors = InputChipDefaults.inputChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = strings.noGenresAdded,
                                style = MaterialTheme.typography.bodySmall,
                                color = subtitleColor,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Card 4: Описание книги
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = strings.bookDescriptionSectionTitle,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = { Text(strings.bookDescriptionPlaceholder) },
                            minLines = 4,
                            maxLines = 10,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    // Unsaved Changes Confirmation Dialog
    if (showDiscardConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardConfirmDialog = false },
            title = { Text(strings.unsavedChangesTitle) },
            text = { Text(strings.unsavedChangesDesc) },
            confirmButton = {
                Button(
                    onClick = {
                        onSave(
                            title.trim(),
                            author.trim(),
                            series.trim().ifBlank { null },
                            seriesNumber.trim().toIntOrNull(),
                            genres,
                            description.trim().ifBlank { null }
                        )
                        showDiscardConfirmDialog = false
                        isEditMode = false
                        pendingDismissAction?.invoke()
                        pendingDismissAction = null
                    }
                ) {
                    Text(strings.saveAction)
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            // Discard unsaved changes and reset
                            title = book.title
                            author = book.author
                            series = book.series.orEmpty()
                            seriesNumber = book.seriesNumber?.toString().orEmpty()
                            genres = book.genres
                            description = book.description.orEmpty()
                            showDiscardConfirmDialog = false
                            isEditMode = false
                            pendingDismissAction?.invoke()
                            pendingDismissAction = null
                        }
                    ) {
                        Text(strings.discardChanges)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = {
                        showDiscardConfirmDialog = false
                        pendingDismissAction = null
                    }) {
                        Text(strings.keepEditing)
                    }
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Add Genre Dialog
    if (showAddGenreDialog) {
        var customGenre by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddGenreDialog = false },
            title = { Text(strings.addGenreTitle) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = customGenre,
                        onValueChange = { customGenre = it },
                        label = { Text(strings.customGenreLabel) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = strings.suggestedGenresLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        POPULAR_GENRES.forEach { suggestion ->
                            val alreadyAdded = suggestion in genres
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (alreadyAdded) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                                },
                                onClick = {
                                    if (!alreadyAdded) {
                                        genres = genres + suggestion
                                        showAddGenreDialog = false
                                    }
                                }
                            ) {
                                Text(
                                    text = suggestion,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    color = if (alreadyAdded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = customGenre.trim()
                        if (trimmed.isNotBlank() && trimmed !in genres) {
                            genres = genres + trimmed
                        }
                        showAddGenreDialog = false
                    }
                ) {
                    Text(strings.addGenre)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGenreDialog = false }) {
                    Text(strings.cancel)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun DetailInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    labelColor: Color,
    valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = labelColor
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = labelColor,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = valueColor
        )
    }
}

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "—"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    return if (mb >= 1.0) {
        String.format(Locale.getDefault(), "%.1f МБ", mb)
    } else {
        String.format(Locale.getDefault(), "%.0f КБ", kb)
    }
}
