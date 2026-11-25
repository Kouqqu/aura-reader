package com.aura.reader.ui.screens.library

import android.content.Intent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.reader.data.model.Book
import com.aura.reader.data.model.SavedWord
import com.aura.reader.ui.components.SyncBottomSheetSystemBars
import com.aura.reader.ui.components.jellyOverscroll
import com.aura.reader.ui.theme.LocalAppStrings
import java.util.Locale

private enum class VocabFilterMode {
    ALL, LEARNING, MASTERED
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun VocabularyBottomSheet(
    savedWords: List<SavedWord>,
    books: List<Book>,
    onToggleMastered: (String) -> Unit,
    onDeleteWord: (String) -> Unit,
    onOpenBookAtWord: (SavedWord) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    @Suppress("DEPRECATION")
    val clipboardManager = LocalClipboardManager.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var searchQuery by remember { mutableStateOf("") }
    var filterMode by remember { mutableStateOf(VocabFilterMode.ALL) }
    var selectedBookId by remember { mutableStateOf<String?>(null) }
    var isFlashcardMode by remember { mutableStateOf(false) }
    var flashcardIndex by remember { mutableIntStateOf(0) }
    var flashcardRevealed by remember { mutableStateOf(false) }

    var ttsReady by remember { mutableStateOf(false) }
    val tts = remember(context) {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsReady = true
            }
        }
        engine
    }
    DisposableEffect(Unit) {
        onDispose {
            runCatching {
                tts.stop()
                tts.shutdown()
            }
        }
    }

    fun speakWord(text: String) {
        if (!ttsReady) return
        val hasCyrillic = text.any { it in '\u0400'..'\u04FF' }
        tts.language = if (hasCyrillic) Locale("ru", "RU") else Locale.US
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "vocab_${System.currentTimeMillis()}")
    }

    val bookGroups = remember(savedWords, books) {
        val byBook = savedWords.groupBy { it.bookId }
        byBook.entries.mapNotNull { (bId, words) ->
            if (bId.isBlank()) return@mapNotNull null
            val title = books.firstOrNull { it.id == bId }?.title
                ?: words.firstOrNull { it.bookTitle.isNotBlank() }?.bookTitle
                ?: "Книга"
            Triple(bId, title, words.size)
        }.sortedByDescending { it.third }
    }

    val filteredWords = remember(savedWords, searchQuery, filterMode, selectedBookId) {
        val q = searchQuery.trim().lowercase()
        savedWords.filter { w ->
            val matchesBook = selectedBookId == null || w.bookId == selectedBookId
            val matchesStatus = when (filterMode) {
                VocabFilterMode.ALL -> true
                VocabFilterMode.LEARNING -> !w.isMastered
                VocabFilterMode.MASTERED -> w.isMastered
            }
            val matchesQuery = q.isBlank() ||
                w.word.lowercase().contains(q) ||
                w.definition.lowercase().contains(q) ||
                w.contextText.lowercase().contains(q) ||
                w.bookTitle.lowercase().contains(q)
            matchesBook && matchesStatus && matchesQuery
        }.sortedByDescending { it.timestamp }
    }

    val learningCount = remember(savedWords) { savedWords.count { !it.isMastered } }
    val masteredCount = remember(savedWords) { savedWords.count { it.isMastered } }

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
        SyncBottomSheetSystemBars()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 680.dp)
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${strings.savedWordsTabTitle} (${savedWords.size})",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (savedWords.isNotEmpty()) {
                        Text(
                            text = "На изучении: $learningCount  •  Изучено: $masteredCount",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (filteredWords.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                val csv = buildString {
                                    appendLine("Word\tDefinition\tBook\tContext")
                                    filteredWords.forEach { w ->
                                        val cleanWord = w.word.replace("\t", " ").replace("\n", " ")
                                        val cleanDef = w.definition.replace("\t", " ").replace("\n", " ")
                                        val cleanBook = w.bookTitle.replace("\t", " ")
                                        val cleanCtx = w.contextText.replace("\t", " ").replace("\n", " ")
                                        appendLine("$cleanWord\t$cleanDef\t$cleanBook\t$cleanCtx")
                                    }
                                }
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Aura Reader — Словарь")
                                    putExtra(Intent.EXTRA_TEXT, csv)
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Экспорт словаря"))
                            }
                        ) {
                            Icon(Icons.Rounded.Share, contentDescription = "Экспорт")
                        }
                        FilterChip(
                            selected = isFlashcardMode,
                            onClick = {
                                isFlashcardMode = !isFlashcardMode
                                flashcardIndex = 0
                                flashcardRevealed = false
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.School,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text(if (isFlashcardMode) "Список" else "Тренировка") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (isFlashcardMode && filteredWords.isNotEmpty()) {
                val deck = remember(filteredWords) {
                    filteredWords.filter { !it.isMastered }.ifEmpty { filteredWords }
                }
                val safeIdx = flashcardIndex.coerceIn(0, (deck.size - 1).coerceAtLeast(0))
                val cardWord = deck[safeIdx]

                Text(
                    text = "Карточка ${safeIdx + 1} из ${deck.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    onClick = { flashcardRevealed = !flashcardRevealed },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 240.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = cardWord.word,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { speakWord(cardWord.word) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Rounded.VolumeUp,
                                    contentDescription = "Озвучить",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        if (cardWord.bookTitle.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = cardWord.bookTitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (cardWord.contextText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "«${cardWord.contextText}»",
                                style = MaterialTheme.typography.bodySmall,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val fastEffects = MaterialTheme.motionScheme.fastEffectsSpec<Float>()
                        AnimatedContent(
                            targetState = flashcardRevealed,
                            transitionSpec = {
                                fadeIn(fastEffects) togetherWith fadeOut(fastEffects)
                            },
                            label = "flashcardReveal"
                        ) { revealed ->
                            if (revealed) {
                                Text(
                                    text = cardWord.definition,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                Text(
                                    text = "Нажмите на карточку, чтобы увидеть значение",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            flashcardRevealed = false
                            flashcardIndex = (safeIdx + 1) % deck.size
                        },
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.weight(1f).height(46.dp)
                    ) {
                        Text("Повторить")
                    }
                    Button(
                        onClick = {
                            if (!cardWord.isMastered) {
                                onToggleMastered(cardWord.id)
                            }
                            flashcardRevealed = false
                            flashcardIndex = if (safeIdx + 1 >= deck.size) 0 else safeIdx + 1
                        },
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.weight(1f).height(46.dp)
                    ) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Знаю")
                    }
                }
            } else {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Поиск слова или книги...") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Rounded.Close, contentDescription = null)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = filterMode == VocabFilterMode.ALL && selectedBookId == null,
                        onClick = {
                            filterMode = VocabFilterMode.ALL
                            selectedBookId = null
                        },
                        label = { Text("Все (${savedWords.size})") }
                    )
                    FilterChip(
                        selected = filterMode == VocabFilterMode.LEARNING,
                        onClick = {
                            filterMode = if (filterMode == VocabFilterMode.LEARNING) VocabFilterMode.ALL else VocabFilterMode.LEARNING
                        },
                        label = { Text("На изучении ($learningCount)") }
                    )
                    FilterChip(
                        selected = filterMode == VocabFilterMode.MASTERED,
                        onClick = {
                            filterMode = if (filterMode == VocabFilterMode.MASTERED) VocabFilterMode.ALL else VocabFilterMode.MASTERED
                        },
                        label = { Text("Изучено ($masteredCount)") }
                    )
                    bookGroups.forEach { (bId, title, count) ->
                        FilterChip(
                            selected = selectedBookId == bId,
                            onClick = {
                                selectedBookId = if (selectedBookId == bId) null else bId
                            },
                            label = {
                                Text(
                                    text = "$title ($count)",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (filteredWords.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "📖", fontSize = 32.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (savedWords.isEmpty()) strings.savedWordsEmpty else "Ничего не найдено",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .jellyOverscroll(allowTopBounce = false),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredWords, key = { it.id }) { item ->
                            val hasBook = item.bookId.isNotBlank() && books.any { it.id == item.bookId }
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = hasBook) {
                                        onOpenBookAtWord(item)
                                        onDismiss()
                                    }
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
                                        Text(
                                            text = item.word,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (item.isMastered) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = { speakWord(item.word) },
                                                modifier = Modifier.size(30.dp)
                                            ) {
                                                Icon(
                                                    Icons.AutoMirrored.Rounded.VolumeUp,
                                                    contentDescription = "Озвучить",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = { onToggleMastered(item.id) },
                                                modifier = Modifier.size(30.dp)
                                            ) {
                                                Icon(
                                                    if (item.isMastered) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                                    contentDescription = "Изучено",
                                                    tint = if (item.isMastered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    clipboardManager.setText(AnnotatedString("${item.word}: ${item.definition}"))
                                                    Toast.makeText(context, strings.wordCopied, Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(30.dp)
                                            ) {
                                                Icon(
                                                    Icons.Rounded.ContentCopy,
                                                    contentDescription = strings.copyWordDefinition,
                                                    tint = MaterialTheme.colorScheme.outline,
                                                    modifier = Modifier.size(17.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    onDeleteWord(item.id)
                                                    Toast.makeText(context, strings.savedWordDeleted, Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(30.dp)
                                            ) {
                                                Icon(
                                                    Icons.Rounded.Delete,
                                                    contentDescription = strings.delete,
                                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(17.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = item.definition,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    if (item.contextText.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "«${item.contextText}»",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontStyle = FontStyle.Italic,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    if (item.bookTitle.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Rounded.AutoStories,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(12.dp),
                                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = item.bookTitle,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
