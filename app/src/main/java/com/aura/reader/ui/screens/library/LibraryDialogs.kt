package com.aura.reader.ui.screens.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.height
import androidx.compose.ui.text.style.TextOverflow
import com.aura.reader.data.model.Book
import com.aura.reader.data.updater.UpdateInfo
import com.aura.reader.ui.theme.LocalAppStrings

@Composable
fun LibraryDialogs(
    updateInfo: UpdateInfo?,
    showNewCollectionDialog: Boolean,
    collectionToDelete: String?,
    bookForCollections: Book?,
    userCollections: List<String>,
    onDismissUpdate: () -> Unit,
    onOpenUpdatePage: (String) -> Unit,
    onSkipVersion: (String) -> Unit,
    onDismissNewCollection: () -> Unit,
    onCreateCollection: (String) -> Unit,
    onDismissDeleteCollection: () -> Unit,
    onRemoveCollection: (String) -> Unit,
    onDismissBookCollections: () -> Unit,
    onSaveBookCollections: (String, List<String>) -> Unit,
    onSaveBookPlacement: (bookId: String, collections: List<String>, manualShelf: String?, isFavorite: Boolean) -> Unit = { _, _, _, _ -> },
    onNewCollectionRequested: () -> Unit
) {
    val strings = LocalAppStrings.current

    // In-App Update Dialog
    updateInfo?.let { info ->
        AlertDialog(
            onDismissRequest = { onDismissUpdate() },
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
                    onDismissUpdate()
                    onOpenUpdatePage(info.releaseUrl)
                }) {
                    Text(strings.download)
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = { onSkipVersion(info.latestVersion) }) {
                        Text(strings.skipVersion, color = MaterialTheme.colorScheme.outline)
                    }
                    TextButton(onClick = { onDismissUpdate() }) {
                        Text(strings.later)
                    }
                }
            }
        )
    }

    // Dialog: Create New Collection
    if (showNewCollectionDialog) {
        var newColName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { onDismissNewCollection() },
            title = { Text(strings.createCollectionDialogTitle, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newColName,
                    onValueChange = { newColName = it },
                    placeholder = { Text(strings.collectionNamePlaceholder) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newColName.isNotBlank()) {
                            onCreateCollection(newColName.trim())
                        }
                        onDismissNewCollection()
                    },
                    enabled = newColName.isNotBlank()
                ) {
                    Text(strings.save)
                }
            },
            dismissButton = {
                TextButton(onClick = { onDismissNewCollection() }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    // Dialog: Delete Custom Collection
    collectionToDelete?.let { colName ->
        AlertDialog(
            onDismissRequest = { onDismissDeleteCollection() },
            title = { Text(strings.delete, fontWeight = FontWeight.Bold) },
            text = { Text(strings.deleteCollectionConfirm(colName)) },
            confirmButton = {
                Button(
                    onClick = {
                        onRemoveCollection(colName)
                        onDismissDeleteCollection()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(strings.delete)
                }
            },
            dismissButton = {
                TextButton(onClick = { onDismissDeleteCollection() }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    // Dialog: Add/Remove Book Collections & Manual Shelving
    bookForCollections?.let { b ->
        val currentSelected = remember(b.collections) { b.collections.toMutableStateList() }
        var selectedManualShelf by remember(b.manualShelf) { mutableStateOf(b.manualShelf) }
        var isFavorite by remember(b.isFavorite) { mutableStateOf(b.isFavorite) }

        AlertDialog(
            onDismissRequest = { onDismissBookCollections() },
            title = {
                Column {
                    Text(strings.addToCollection, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        b.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Полка книги",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    val shelves = listOf(
                        Triple(null, "Авто (по прогрессу)", "Определяется автоматически"),
                        Triple("READING", "Читаю", "Закрепить на полке «Читаю»"),
                        Triple("UNREAD", "Непрочитанные", "Оставить на полке «Непрочитанные»"),
                        Triple("FINISHED", "Прочитано", "Закрепить на полке «Прочитано»")
                    )

                    shelves.forEach { (shelfKey, title, hint) ->
                        val isSelected = selectedManualShelf == shelfKey
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedManualShelf = shelfKey }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedManualShelf = shelfKey }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
                                Text(hint, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isFavorite = !isFavorite }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isFavorite,
                            onCheckedChange = { isFavorite = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("В избранном", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Пользовательские коллекции",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    if (userCollections.isEmpty()) {
                        Text(
                            text = "Пока нет созданных коллекций",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    } else {
                        for (col in userCollections) {
                            val isChecked = currentSelected.contains(col)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isChecked) currentSelected.remove(col) else currentSelected.add(col)
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { chk ->
                                        if (chk) currentSelected.add(col) else currentSelected.remove(col)
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(col, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    TextButton(onClick = { onNewCollectionRequested() }) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.newCollection)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    onSaveBookPlacement(b.id, currentSelected.toList(), selectedManualShelf, isFavorite)
                    onSaveBookCollections(b.id, currentSelected.toList())
                    onDismissBookCollections()
                }) {
                    Text(strings.save)
                }
            },
            dismissButton = {
                TextButton(onClick = { onDismissBookCollections() }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}
