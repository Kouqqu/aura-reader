package com.aura.reader.data

import com.aura.reader.data.service.BookEnricher
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class BookEnricherTest {

    @Test
    fun cleanTitleStripsExtensionsAndBrackets() {
        val raw1 = "Гарри Поттер и Философский Камень.fb2.zip"
        assertEquals("Гарри Поттер и Философский Камень", BookEnricher.cleanTitle(raw1))

        val raw2 = "Мастер и Маргарита [Эксмо] (РОСМЭН).epub"
        assertEquals("Мастер и Маргарита", BookEnricher.cleanTitle(raw2))

        val raw3 = "  Властелин Колец: Братство Кольца.pdf  "
        assertEquals("Властелин Колец: Братство Кольца", BookEnricher.cleanTitle(raw3))
    }

    @Test
    fun cleanAuthorStripsParenthesesAndTrims() {
        val author1 = "Дж. К. Роулинг (пер. Спивак)"
        assertEquals("Дж. К. Роулинг", BookEnricher.cleanAuthor(author1))

        val author2 = "  Михаил Булгаков  "
        assertEquals("Михаил Булгаков", BookEnricher.cleanAuthor(author2))

        assertEquals("", BookEnricher.cleanAuthor(null))
    }

    @Test
    fun enrichReturnsNullOnBlankTitle() = runBlocking {
        val result = BookEnricher.enrich("   ", "Автор")
        assertNull(result)
    }
}
