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

    @Test
    fun cleanAnnotationStripsLitresDisclaimerAndPreservesStory() {
        val rawHtml = """
            <p>В Москве 1930-х годов появляется Воланд со своей свитой.</p>
            <p>Администрация сайта Литрес не несет ответственности за представленную информацию. Могут иметься медицинские противопоказания.</p>
            <p>Мастер и Маргарита обретают вечный покой в ином мире.</p>
        """.trimIndent()

        val cleaned = BookEnricher.cleanAnnotation(rawHtml)
        assertTrue(cleaned.contains("В Москве 1930-х годов появляется Воланд"))
        assertTrue(cleaned.contains("Мастер и Маргарита обретают вечный покой"))
        assertFalse(cleaned.contains("Администрация сайта Литрес"))
        assertFalse(cleaned.contains("Могут иметься медицинские противопоказания"))
    }

    @Test
    fun genreClassifierCategorizesDetailedGenresCorrectly() {
        val genres = listOf("Зарубежные детективы", "Исторические романы")
        val macros = com.aura.reader.data.model.GenreClassifier.getMacroGenresForBook(genres)
        assertTrue(macros.contains(com.aura.reader.data.model.MacroGenre.DETECTIVE))

        val dummyBook = com.aura.reader.data.model.Book(
            id = "test_1",
            title = "Приключения Шерлока Холмса",
            author = "Артур Конан Дойл",
            format = com.aura.reader.data.model.BookFormat.FB2,
            uriString = "file:///dummy.fb2",
            genres = listOf("Классический детектив")
        )

        // Matches macro category
        assertTrue(com.aura.reader.data.model.GenreClassifier.bookMatchesFilter(dummyBook, "Детективы"))
        // Matches exact substring
        assertTrue(com.aura.reader.data.model.GenreClassifier.bookMatchesFilter(dummyBook, "Классический"))
        // Does not match unrelated
        assertFalse(com.aura.reader.data.model.GenreClassifier.bookMatchesFilter(dummyBook, "Фантастика"))
    }
}
