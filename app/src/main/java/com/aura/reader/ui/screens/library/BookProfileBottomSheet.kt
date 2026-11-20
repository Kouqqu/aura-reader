package com.aura.reader.ui.screens.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.reader.data.model.Book
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
    "Антиутопия"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BookProfileBottomSheet(
    book: Book,
    onDismiss: () -> Unit,
    onSave: (title: String, author: String, series: String?, seriesNumber: Int?, genres: List<String>, description: String?) -> Unit,
    onChangeCover: () -> Unit,
    isGlass: Boolean = false,
    isDark: Boolean = false
) {
    val strings = LocalAppStrings.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember { mutableStateOf(book.title) }
    var author by remember { mutableStateOf(book.author) }
    var series by remember { mutableStateOf(book.series.orEmpty()) }
    var seriesNumber by remember { mutableStateOf(book.seriesNumber?.toString().orEmpty()) }
    var genres by remember { mutableStateOf(book.genres) }
    var description by remember { mutableStateOf(book.description.orEmpty()) }
    var showAddGenreDialog by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("d MMMM yyyy, HH:mm", Locale.getDefault()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = if (isGlass) {
            if (isDark) Color(0xEC181926) else Color(0xF4FFFFFF)
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Header: Title & Cover
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 82.dp, height = 118.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onChangeCover() }
                ) {
                    BookCoverView(
                        coverBase64 = book.coverBase64,
                        title = book.title,
                        bookId = book.id,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(strings.bookTitleLabel) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

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

            Spacer(modifier = Modifier.height(16.dp))

            // Badges row: Format, Size, Reading progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val formatBadge = book.format.name.uppercase()
                val sizeBadge = formatFileSize(book.fileSizeBytes)
                val progressBadge = "${book.progressPercent}%"

                QuickInfoPill(
                    icon = Icons.Rounded.Description,
                    label = formatBadge,
                    modifier = Modifier.weight(1f)
                )
                QuickInfoPill(
                    icon = Icons.Rounded.Storage,
                    label = sizeBadge,
                    modifier = Modifier.weight(1f)
                )
                QuickInfoPill(
                    icon = Icons.Rounded.AutoStories,
                    label = progressBadge,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Series Section
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
                ),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = strings.seriesSectionTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
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
                            onValueChange = { seriesNumber = it.filter { ch -> ch.isDigit() } },
                            label = { Text(strings.seriesNumberLabel) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Genres Section
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
                ),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
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
                        OutlinedButton(
                            onClick = { showAddGenreDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.addGenre, style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (genres.isEmpty()) {
                        Text(
                            text = strings.noGenresAdded,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            for (genre in genres) {
                                InputChip(
                                    selected = false,
                                    onClick = {},
                                    label = { Text(genre) },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clickable { genres = genres - genre }
                                        )
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = InputChipDefaults.inputChipColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f)
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Description / Synopsis Section
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
                ),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
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
                        minLines = 3,
                        maxLines = 8,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dates & System Metadata Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
                ),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = strings.metadataSectionTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    MetadataRow(
                        icon = Icons.Rounded.CalendarToday,
                        label = strings.dateAddedLabel,
                        value = dateFormat.format(Date(book.addedTimestamp))
                    )
                    MetadataRow(
                        icon = Icons.Rounded.History,
                        label = strings.firstOpenedLabel,
                        value = if (book.firstOpenedTimestamp != null) dateFormat.format(Date(book.firstOpenedTimestamp)) else strings.neverOpened
                    )
                    MetadataRow(
                        icon = Icons.Rounded.AutoStories,
                        label = strings.lastReadLabel,
                        value = dateFormat.format(Date(book.lastReadTimestamp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Save Button
            Button(
                onClick = {
                    val num = seriesNumber.toIntOrNull()
                    onSave(title, author, series.ifBlank { null }, num, genres, description.ifBlank { null })
                    onDismiss()
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.saveChanges)
            }
        }
    }

    if (showAddGenreDialog) {
        var customGenre by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddGenreDialog = false },
            title = {
                Text(strings.addGenreTitle, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = customGenre,
                        onValueChange = { customGenre = it },
                        label = { Text(strings.customGenreLabel) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = strings.suggestedGenresLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (suggested in POPULAR_GENRES) {
                            val alreadyHas = genres.any { it.equals(suggested, ignoreCase = true) }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (alreadyHas) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable(enabled = !alreadyHas) {
                                        genres = genres + suggested
                                    }
                            ) {
                                Text(
                                    text = suggested,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (alreadyHas) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = customGenre.trim()
                        if (trimmed.isNotBlank() && genres.none { it.equals(trimmed, ignoreCase = true) }) {
                            genres = genres + trimmed
                        }
                        showAddGenreDialog = false
                    },
                    shape = RoundedCornerShape(10.dp)
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
private fun QuickInfoPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MetadataRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
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
