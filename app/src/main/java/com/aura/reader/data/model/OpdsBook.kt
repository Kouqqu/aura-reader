package com.aura.reader.data.model

enum class OpdsSearchType {
    ALL,
    BOOKS,
    SERIES,
    AUTHORS
}

data class OpdsSearchResult(
    val query: String,
    val books: List<OpdsBook> = emptyList(),
    val series: List<OpdsBook> = emptyList(),
    val authors: List<OpdsBook> = emptyList()
) {
    val totalCount: Int
        get() = books.size + series.size + authors.size

    val isEmpty: Boolean
        get() = books.isEmpty() && series.isEmpty() && authors.isEmpty()
}

data class OpdsBook(
    val id: String,
    val title: String,
    val author: String = "",
    val annotation: String = "",
    val coverUrl: String? = null,
    val fb2Url: String? = null,
    val epubUrl: String? = null,
    val mobiUrl: String? = null,
    val pdfUrl: String? = null,
    val downloadSize: String? = null,
    val isCategory: Boolean = false,
    val categoryPath: String? = null,
    val year: String? = null,
    val language: String? = null,
    val formatInfo: String? = null,
    val downloadsCount: Int = 0,
    val isSeries: Boolean = false,
    val isAuthorCategory: Boolean = false,
    val itemCount: Int? = null,
    val categories: List<String> = emptyList()
) {
    val recommendedFormat: BookFormat?
        get() {
            val haystack = (categories + listOf(title)).joinToString(" ").lowercase()
            val preferEpub = STUDY_TECH_KEYWORDS.any { haystack.contains(it) }
            return when {
                preferEpub && epubUrl != null -> BookFormat.EPUB
                !preferEpub && fb2Url != null -> BookFormat.FB2
                epubUrl != null -> BookFormat.EPUB
                fb2Url != null -> BookFormat.FB2
                mobiUrl != null -> BookFormat.MOBI
                pdfUrl != null -> BookFormat.PDF
                else -> null
            }
        }

    companion object {
        private val STUDY_TECH_KEYWORDS = listOf(
            "учеб", "пособи", "справоч", "науч", "техн", "программ", "информат",
            "математ", "физик", "хими", "биолог", "эконом", "право", "юрид",
            "медиц", "словар", "энциклоп", "компьютер", "руководств", "лекци",
            "study", "textbook", "science", "tech", "programming", "reference",
            "manual", "nonfiction", "non-fiction", "computers", "education", "documentation"
        )
    }
}
