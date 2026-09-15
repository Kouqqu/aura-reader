package com.aura.reader.data.model

import androidx.compose.runtime.Immutable

enum class BookFormat {
    FB2,
    EPUB,
    TXT,
    PDF,
    MOBI
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
data class FormattedBlock(
    val type: BlockType,
    val text: String,
    val subText: String? = null // Author of epigraph / quote
)

@Immutable
data class Chapter(
    val id: String,
    val title: String,
    val content: String,
    val blocks: List<FormattedBlock> = emptyList(),
    val order: Int
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
    val collections: List<String> = emptyList()
)

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
    val color: Long = 0xFFFFF59D
)

