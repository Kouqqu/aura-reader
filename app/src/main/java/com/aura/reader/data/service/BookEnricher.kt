package com.aura.reader.data.service

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class BookEnrichment(
    val title: String? = null,
    val author: String? = null,
    val description: String? = null,
    val genres: List<String> = emptyList(),
    val series: String? = null,
    val seriesNumber: Int? = null,
    val coverBase64: String? = null,
    val coverUrl: String? = null,
    val source: String = ""
)

/**
 * Deep unified facade for all book metadata and cover enrichment operations.
 *
 * Encapsulates multi-source online searches (LitRes, FantLab, OpenLibrary, Flibusta),
 * title/author sanitization, cover retrieval, image compression, and Base64 conversion
 * behind a cohesive interface.
 */
object BookEnricher {

    suspend fun enrich(
        title: String,
        author: String? = null,
        includeCover: Boolean = false
    ): BookEnrichment? = withContext(Dispatchers.IO) {
        val meta = BookMetadataSearchService.fetchMetadata(title, author)
        val coverBase64 = if (includeCover) {
            val q = listOfNotNull(title, author).filter { it.isNotBlank() }.joinToString(" ")
            val covers = CoverSearchService.searchCovers(q, fallbackTitle = cleanTitle(title))
            covers.firstOrNull()?.let { CoverSearchService.downloadCoverAsBase64(it.highResUrl) }
        } else null

        if (meta == null && coverBase64 == null) return@withContext null

        BookEnrichment(
            title = meta?.title,
            author = meta?.author,
            description = meta?.description,
            genres = meta?.genres ?: emptyList(),
            series = meta?.series,
            seriesNumber = meta?.seriesNumber,
            coverBase64 = coverBase64,
            source = meta?.source ?: if (coverBase64 != null) "CoverSearch" else ""
        )
    }

    suspend fun fetchMetadata(
        title: String,
        author: String? = null
    ): BookEnrichment? {
        val meta = BookMetadataSearchService.fetchMetadata(title, author) ?: return null
        return BookEnrichment(
            title = meta.title,
            author = meta.author,
            description = meta.description,
            genres = meta.genres,
            series = meta.series,
            seriesNumber = meta.seriesNumber,
            source = meta.source
        )
    }

    suspend fun searchCovers(
        query: String,
        fallbackTitle: String? = null
    ): List<OnlineCover> {
        return CoverSearchService.searchCovers(query, fallbackTitle)
    }

    suspend fun downloadCoverAsBase64(url: String): String? {
        return CoverSearchService.downloadCoverAsBase64(url)
    }

    suspend fun processUriAsBase64(context: Context, uri: Uri): String? {
        return CoverSearchService.processUriAsBase64(context, uri)
    }

    fun cleanTitle(title: String): String {
        return CoverSearchService.cleanBookTitle(title)
    }

    fun cleanAuthor(author: String?): String {
        return CoverSearchService.cleanAuthorName(author.orEmpty())
    }

    fun cleanAnnotation(raw: String): String {
        return BookMetadataSearchService.cleanAnnotationText(raw)
    }
}
