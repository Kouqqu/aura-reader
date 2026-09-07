package com.aura.reader.data.model

enum class BookFormat {
    FB2,
    EPUB,
    TXT
}

data class Chapter(
    val id: String,
    val title: String,
    val content: String,
    val order: Int
)

data class Book(
    val id: String,
    val title: String,
    val author: String = "",
    val coverBase64: String? = null,
    val format: BookFormat,
    val uriString: String,
    val chapters: List<Chapter> = emptyList(),
    val currentChapterIndex: Int = 0,
    val currentScrollOffset: Int = 0,
    val progressPercent: Int = 0,
    val lastReadTimestamp: Long = System.currentTimeMillis()
)
