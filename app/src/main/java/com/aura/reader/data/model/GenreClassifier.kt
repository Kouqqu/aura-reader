package com.aura.reader.data.model

/**
 * Macro categories for organizing diverse and granular genres (e.g. from LitRes/FantLab)
 * into high-level, easily recognizable clusters for search filters and library navigation.
 */
enum class MacroGenre(
    val title: String,
    val iconEmoji: String,
    val keywords: List<String>
) {
    DETECTIVE(
        title = "Детективы",
        iconEmoji = "🔍",
        keywords = listOf("детектив", "триллер", "расследован", "криминал", "боевик-детектив", "следстви", "сыщик")
    ),
    SCI_FI(
        title = "Фантастика",
        iconEmoji = "🚀",
        keywords = listOf("фантастик", "космоопер", "киберпанк", "постапокалип", "антиутопи", "литрпг", "попаданц", "нф", "научная фантастика")
    ),
    FANTASY(
        title = "Фэнтези",
        iconEmoji = "🐉",
        keywords = listOf("фэнтези", "маги", "дракон", "ведьм", "эльф", "меча и магии", "эпическое фэнтези", "городское фэнтези")
    ),
    CLASSICS(
        title = "Классика",
        iconEmoji = "🏛️",
        keywords = listOf("классик", "литература 19", "литература 20", "древне", "историческая проза", "шедевр")
    ),
    ROMANCE(
        title = "Романы",
        iconEmoji = "💖",
        keywords = listOf("любовн", "романтич", "чувств", "сентиментальн", "эротик", "любовный роман")
    ),
    NON_FICTION(
        title = "Нон-фикшн",
        iconEmoji = "🧠",
        keywords = listOf("нон-фикшн", "биографи", "мемуар", "психолог", "бизнес", "саморазвит", "истори", "научн", "публицистик", "философи")
    ),
    HORROR(
        title = "Мистика и ужасы",
        iconEmoji = "👻",
        keywords = listOf("ужас", "хоррор", "мистик", "триллер-мистик", "призрак", "вампир")
    ),
    ADVENTURE(
        title = "Приключения",
        iconEmoji = "⚔️",
        keywords = listOf("приключен", "вестерн", "робинзонад", "путешеств", "мореплаван")
    ),
    KIDS(
        title = "Детское",
        iconEmoji = "🧒",
        keywords = listOf("детск", "сказк", "подростк", "для детей", "юношеск")
    );

    fun matches(genre: String): Boolean {
        val lower = genre.lowercase().trim()
        return keywords.any { lower.contains(it) }
    }
}

object GenreClassifier {

    /**
     * Resolves all macro-genres that this book belongs to based on its detailed genre tags.
     */
    fun getMacroGenresForBook(genres: List<String>): List<MacroGenre> {
        if (genres.isEmpty()) return emptyList()
        return MacroGenre.entries.filter { mg ->
            genres.any { mg.matches(it) }
        }
    }

    /**
     * Checks if a book matches the given genre filter.
     * If [filter] is a known MacroGenre (e.g. "Детективы"), it matches if any of the book's
     * detailed genres match that macro-genre.
     * Otherwise, matches if any detailed genre contains [filter] case-insensitively.
     */
    fun bookMatchesFilter(book: Book, filter: String): Boolean {
        val cleanFilter = filter.trim()
        if (cleanFilter.isBlank()) return true

        val macro = MacroGenre.entries.firstOrNull { it.title.equals(cleanFilter, ignoreCase = true) }
        if (macro != null) {
            return book.genres.any { macro.matches(it) }
        }

        return book.genres.any { it.contains(cleanFilter, ignoreCase = true) }
    }
}
