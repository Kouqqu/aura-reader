package com.aura.reader.data.model

import androidx.compose.runtime.Immutable

enum class BookFormat {
    FB2,
    EPUB,
    TXT,
    PDF,
    MOBI,
    CBZ,
    HTML,
    MD
}

enum class BlockType {
    TITLE,          // Main chapter/section title (centered, bold, prominent)
    SUBTITLE,       // Subtitle / section heading (centered, semi-bold)
    EPIGRAPH,       // Epigraph / quote (italic, indented/right-aligned, optional author)
    PARAGRAPH,      // Standard body paragraph (with book-style text indent)
    VERSE,          // Poem verse lines
    DIVIDER,        // Section break / asterisks
    IMAGE           // Illustration / picture: text = local file path / URI, subText = caption
}

@Immutable
data class BookLink(
    val start: Int,
    val end: Int,
    val target: String,
    val isFootnote: Boolean = false
)

@Immutable
data class FormattedBlock(
    val type: BlockType,
    val text: String,
    val subText: String? = null, // Author of epigraph / quote
    val sourceTextOffset: Int = 0,
    val links: List<BookLink> = emptyList()
)

@Immutable
data class Chapter(
    val id: String,
    val title: String,
    val content: String,
    val blocks: List<FormattedBlock> = emptyList(),
    val order: Int,
    val depth: Int = 0
)

@Immutable
data class Book(
    val id: String,
    val title: String,
    val author: String = "",
    val coverBase64: String? = null,
    val format: BookFormat,
    val uriString: String,
    val chapters: List<Chapter> = emptyList(),
    val footnotes: Map<String, String> = emptyMap(),
    val currentChapterIndex: Int = 0,
    val currentScrollOffset: Int = 0,
    val progressPercent: Int = 0,
    val lastReadTimestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val collections: List<String> = emptyList(),
    val series: String? = null,
    val seriesNumber: Int? = null,
    val currentBlockTextOffset: Int = 0,
    val currentScrollPixelOffset: Int = 0,
    val manualShelf: String? = null,
    val deletedAtTimestamp: Long? = null,
    val fileSizeBytes: Long = 0L,
    val addedTimestamp: Long = System.currentTimeMillis(),
    val firstOpenedTimestamp: Long? = null,
    val description: String? = null,
    val genres: List<String> = emptyList(),
    val charCount: Long = 0L,
    val wordCount: Int = 0
)

fun Book.withComputedVolume(): Book {
    if (charCount > 0L && wordCount > 0) return this
    var chars = 0L
    var words = 0
    for (ch in chapters) {
        if (ch.blocks.isNotEmpty()) {
            for (b in ch.blocks) {
                if (b.type != BlockType.IMAGE && b.type != BlockType.DIVIDER) {
                    val t = b.text.trim()
                    if (t.isNotEmpty()) {
                        chars += t.length
                        words += t.split(Regex("\\s+")).count { it.isNotBlank() }
                    }
                }
            }
        } else if (ch.content.isNotBlank()) {
            chars += ch.content.length
            words += ch.content.split(Regex("\\s+")).count { it.isNotBlank() }
        }
    }
    return copy(charCount = chars, wordCount = words)
}

fun Book.estimatedPaperPages(): Int {
    if (charCount <= 0L) return 0
    return (charCount / 1800L).toInt().coerceAtLeast(1)
}

fun Book.estimatedReadingMinutes(wpm: Float = 200f): Int {
    if (wordCount <= 0) return 0
    return (wordCount / wpm.coerceAtLeast(80f)).toInt().coerceAtLeast(1)
}

fun Book.formatVolumeShort(wpm: Float = 200f): String? {
    val pages = estimatedPaperPages()
    val mins = estimatedReadingMinutes(wpm)
    if (pages <= 0 || mins <= 0) return null
    val timeStr = if (mins >= 60) {
        val h = mins / 60
        val m = mins % 60
        if (m >= 10) "~$h ч $m мин" else "~$h ч"
    } else {
        "~$mins мин"
    }
    return "~$pages стр. · $timeStr"
}

@Immutable
data class Bookmark(
    val id: String = java.util.UUID.randomUUID().toString(),
    val bookId: String,
    val chapterIndex: Int,
    val scrollOffset: Int,
    val chapterTitle: String,
    val previewText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Immutable
data class Quote(
    val id: String = java.util.UUID.randomUUID().toString(),
    val bookId: String,
    val bookTitle: String,
    val chapterIndex: Int,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val color: Long = 0xFFFFF59D,
    val blockIndex: Int = -1
)

@Immutable
data class SavedWord(
    val id: String = java.util.UUID.randomUUID().toString(),
    val bookId: String,
    val bookTitle: String = "",
    val word: String,
    val definition: String,
    val contextText: String = "",
    val chapterIndex: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val isMastered: Boolean = false
)

val Book.isReadingStarted: Boolean
    get() = when (manualShelf) {
        "READING" -> true
        "UNREAD", "FINISHED" -> false
        else -> (progressPercent in 1..99) ||
                (progressPercent < 100 && (
                    currentChapterIndex > 0 ||
                    currentScrollOffset > 0 ||
                    currentBlockTextOffset > 0 ||
                    currentScrollPixelOffset > 0
                ))
    }

val Book.isUnread: Boolean
    get() = when (manualShelf) {
        "UNREAD" -> true
        "READING", "FINISHED" -> false
        else -> progressPercent == 0 && !isReadingStarted
    }

val Book.isFinished: Boolean
    get() = when (manualShelf) {
        "FINISHED" -> true
        "READING", "UNREAD" -> false
        else -> progressPercent >= 100
    }


