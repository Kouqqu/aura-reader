package com.aura.reader.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf

enum class AppLanguage(val code: String, val title: String) {
    RU("ru", "🇷🇺 Русский"),
    EN("en", "🇬🇧 English"),
    UK("uk", "🇺🇦 Українська"),
    BE("be", "🇧🇾 Беларуская"),
    PL("pl", "🇵🇱 Polski");

    companion object {
        fun fromCode(code: String): AppLanguage =
            entries.find { it.code.equals(code, ignoreCase = true) } ?: RU
    }
}

interface Strings {
    val appName: String
    val cancel: String
    val delete: String
    val save: String
    val copy: String
    val close: String
    val done: String
    val back: String
    val clear: String
    val previous: String
    val next: String
    val update: String
    val later: String

    val libraryTitle: String
    val addBooks: String
    val emptyLibraryTitle: String
    val emptyLibrarySubtitle: String
    val openSampleBook: String
    val openFile: String
    val recentBooks: String
    val searchHint: String
    val noBooksFound: String
    val deleteBookTitle: String
    fun deleteBookMessage(title: String): String
    val todayReadingTime: String
    fun minutesRead(minutes: Int): String
    val readingStatsToggle: String
    val readingStatsSubtitle: String
    val readingStatsEmpty: String
    val progressRead: String
    fun searchResultsCount(count: Int): String
    fun noSearchResultsFound(query: String): String

    val chooseAddMethod: String
    val selectFiles: String
    val selectFilesSubtitle: String
    val scanFolder: String
    val scanFolderSubtitle: String
    val flibustaCatalog: String
    val flibustaCatalogSubtitle: String
    val flibustaSearchHint: String
    val flibustaMirrorTitle: String
    val flibustaMirrorSubtitle: String
    val flibustaConnectionErrorTitle: String
    val flibustaConnectionErrorSubtitle: String
    val downloadFb2: String
    val downloadEpub: String
    val downloading: String
    val openBookAction: String
    val annotation: String
    val reset: String
    val retry: String
    val download: String
    val loading: String
    val donate: String
    val moreOptions: String
    val fontAndTheme: String
    val bookmarksAndQuotes: String

    val selectThirdParty: String
    val selectThirdPartySubtitle: String
    val materialYouToggle: String
    val materialYouSubtitle: String
    val sortTitle: String
    val sortByDefault: String
    val sortByPopularDesc: String
    val sortByPopularAsc: String
    val sortByTitleAsc: String
    val sortByTitleDesc: String
    val sortByAuthorAsc: String
    val sortByAuthorDesc: String
    val sortByYearDesc: String
    val sortByYearAsc: String
    val sortByPopularity: String
    val sortByTitle: String
    val sortByAuthor: String
    val catNew: String
    val catPopular: String
    val catAuthors: String
    val catGenres: String
    val catNewSubtitle: String
    val catPopularSubtitle: String
    val catAuthorsSubtitle: String
    val catGenresSubtitle: String
    val catalogHomeTitle: String
    val collectionsTitle: String
    val colAll: String
    val colReading: String
    val colFavorites: String
    val colUnread: String
    val colFinished: String
    val newCollection: String
    val createCollectionDialogTitle: String
    val collectionNamePlaceholder: String
    val addToCollection: String
    val markAsFinished: String
    val resetProgress: String
    fun deleteCollectionConfirm(name: String): String

    val searchInLibrary: String
    val searchLibraryPlaceholder: String

    val backupSectionTitle: String
    val createBackupTitle: String
    val createBackupSubtitle: String
    val sendToGoogleDriveTitle: String
    val sendToGoogleDriveSubtitle: String
    val restoreBackupTitle: String
    val restoreBackupSubtitle: String
    fun backupCreatedSuccess(count: Int): String
    fun restoreCompletedSuccess(count: Int): String
    val backupError: String
    val shareBackupTitle: String
    val catalogHomeSubtitle: String
    fun downloadsCount(count: Int): String
    fun yearLabel(year: String): String

    val settingsTitle: String
    val themeSectionTitle: String
    val themeSystem: String
    val themeLight: String
    val themeDark: String
    val themeAmoled: String
    val themeSepia: String
    val languageSectionTitle: String
    val updatesSectionTitle: String
    val updateNotificationsToggle: String
    val updateNotificationsSubtitle: String
    val checkUpdatesNow: String
    val checkingUpdates: String
    val upToDate: String
    fun currentVersion(version: String): String
    fun updateAvailableTitle(version: String): String
    val updateBannerSubtitle: String
    val downloadingUpdate: String
    val aboutSectionTitle: String
    val githubRepository: String

    val chapter: String
    val ofChapters: String
    val page: String
    val ofPages: String
    val prevChapter: String
    val nextChapter: String
    val prevPage: String
    val nextPage: String
    val contents: String
    val bookmarks: String
    val quotes: String
    val searchInBook: String
    val searchInBookHint: String
    val noMatchesFound: String
    val footnoteTitle: String
    val pagingHint: String
    val loadingBook: String
    val closeSearch: String
    val chaptersNavigationHint: String

    val saveQuoteTitle: String
    val saveQuoteAction: String
    val quoteSavedNotification: String
    val textCopiedNotification: String
    val addQuotePlaceholder: String
    val noQuotesYet: String
    val noBookmarksYet: String
}

class RuStrings : Strings {
    override val chooseAddMethod = "Добавить книги в библиотеку"
    override val selectFiles = "Выбрать файлы"
    override val selectFilesSubtitle = "Файлы .fb2, .epub, .fb2.zip"
    override val scanFolder = "Сканировать папку"
    override val scanFolderSubtitle = "Рекурсивный поиск всех книг в папке устройства"
    override val selectThirdParty = "Сторонний проводник"
    override val selectThirdPartySubtitle = "Samsung «Мои файлы», Xiaomi, проводники"
    override val materialYouToggle = "Цвета Material You"
    override val materialYouSubtitle = "Адаптировать акцентные цвета под обои устройства"
    override val sortTitle = "Сортировка"
    override val sortByDefault = "По умолчанию"
    override val sortByPopularDesc = "Самые популярные 🔥"
    override val sortByPopularAsc = "Менее популярные 📉"
    override val sortByTitleAsc = "По названию (А → Я)"
    override val sortByTitleDesc = "По названию (Я → А)"
    override val sortByAuthorAsc = "По автору (А → Я)"
    override val sortByAuthorDesc = "По автору (Я → А)"
    override val sortByYearDesc = "Сначала новые 📅"
    override val sortByYearAsc = "Сначала старые ⏳"
    override val sortByPopularity = "По популярности"
    override val sortByTitle = "По названию"
    override val sortByAuthor = "По автору"
    override val catNew = "🔥 Новинки"
    override val catPopular = "⭐ Популярное"
    override val catAuthors = "✍️ Авторы"
    override val catGenres = "🏷️ Жанры"
    override val catNewSubtitle = "Свежие поступления и обновления"
    override val catPopularSubtitle = "Самые читаемые книги каталога"
    override val catAuthorsSubtitle = "Алфавитный указатель писателей"
    override val catGenresSubtitle = "Книги по жанрам и темам"
    override val catalogHomeTitle = "Найдётся всё"
    override val collectionsTitle = "Коллекции"
    override val colAll = "Все"
    override val colReading = "Читаю"
    override val colFavorites = "Избранное"
    override val colUnread = "К прочтению"
    override val colFinished = "Прочитано"
    override val newCollection = "+ Полка"
    override val createCollectionDialogTitle = "Новая коллекция"
    override val collectionNamePlaceholder = "Название полки"
    override val addToCollection = "В коллекцию"
    override val markAsFinished = "Отметить прочитанной"
    override val resetProgress = "Сбросить прогресс"
    override fun deleteCollectionConfirm(name: String) = "Удалить коллекцию «$name»? Книги останутся в библиотеке."

    override val searchInLibrary = "Поиск по библиотеке"
    override val searchLibraryPlaceholder = "Название, автор или формат..."

    override val backupSectionTitle = "Резервное копирование и синхронизация"
    override val createBackupTitle = "Сохранить резервную копию"
    override val createBackupSubtitle = "Экспорт книг, цитат, закладок и настроек в файл"
    override val sendToGoogleDriveTitle = "Отправить на Google Диск"
    override val sendToGoogleDriveSubtitle = "Быстрое сохранение через Google Диск или другие приложения"
    override val restoreBackupTitle = "Восстановить из резервной копии"
    override val restoreBackupSubtitle = "Восстановление библиотеки и прогресса из файла бэкапа"
    override fun backupCreatedSuccess(count: Int) = "Резервная копия успешно создана ($count книг)"
    override fun restoreCompletedSuccess(count: Int) = "Данные успешно восстановлены ($count книг)"
    override val backupError = "Ошибка при работе с резервной копией"
    override val shareBackupTitle = "Резервная копия Aura Reader" 
    override val catalogHomeSubtitle = "Тысячи книг в свободном доступе"
    override fun downloadsCount(count: Int) = "$count скачиваний"
    override fun yearLabel(year: String) = "$year г."
    override val flibustaCatalog = "Онлайн-каталог"
    override val flibustaCatalogSubtitle = "Поиск и скачивание книг онлайн через OPDS"
    override val flibustaSearchHint = "Поиск по автору или названию..."
    override val flibustaMirrorTitle = "Настройки OPDS"
    override val flibustaMirrorSubtitle = "Адрес OPDS-сервера. При блокировках можно указать рабочее зеркало."
    override val flibustaConnectionErrorTitle = "Не удалось связаться с сервером"
    override val flibustaConnectionErrorSubtitle = "Возможно, с IP-адресов других стран всё заработает."
    override val downloadFb2 = "Скачать FB2"
    override val downloadEpub = "Скачать EPUB"
    override val downloading = "Загрузка..."
    override val openBookAction = "Читать"
    override val annotation = "Аннотация"
    override val reset = "Сброс"
    override val appName = "Aura Reader"
    override val cancel = "Отмена"
    override val delete = "Удалить"
    override val save = "Сохранить"
    override val copy = "Скопировать"
    override val close = "Закрыть"
    override val done = "Готово"
    override val back = "Назад"
    override val clear = "Очистить"
    override val previous = "Предыдущее"
    override val next = "Следующее"
    override val update = "Обновить"
    override val later = "Позже"

    override val libraryTitle = "Aura Reader"
    override val addBooks = "Добавить книги"
    override val emptyLibraryTitle = "Библиотека пуста"
    override val emptyLibrarySubtitle = "Добавьте книги в формате EPUB или FB2 для начала чтения"
    override val openSampleBook = "Открыть демонстрационную книгу"
    override val openFile = "Выбрать файлы на устройстве"
    override val recentBooks = "Недавние книги"
    override val searchHint = "Поиск по книгам и авторам..."
    override val noBooksFound = "Книги не найдены"
    override val deleteBookTitle = "Удалить книгу?"
    override fun deleteBookMessage(title: String) = "Книга «$title» будет удалена из библиотеки и списка недавних."
    override val todayReadingTime = "Сегодня прочитано"
    override fun minutesRead(minutes: Int) = "$minutes мин"
    override val readingStatsToggle = "Учитывать время чтения"
    override val readingStatsSubtitle = "Отображать карточку прочитанных минут на главном экране"
    override val readingStatsEmpty = "Время для чтения! Начните сегодня"
    override val progressRead = "Прочитано"
    override fun searchResultsCount(count: Int) = "Результаты поиска ($count)"
    override fun noSearchResultsFound(query: String) = "Ничего не найдено по запросу «$query»"

    override val retry = "Повторить"
    override val download = "Скачать"
    override val loading = "Загрузка..."
    override val donate = "Поддержать автора"
    override val moreOptions = "Дополнительно"
    override val fontAndTheme = "Шрифт и тема"
    override val bookmarksAndQuotes = "Закладки и цитаты"

    override val settingsTitle = "Настройки"
    override val themeSectionTitle = "Тема оформления"
    override val themeSystem = "Системная"
    override val themeLight = "Светлая"
    override val themeDark = "Тёмная"
    override val themeAmoled = "AMOLED"
    override val themeSepia = "Сепия"
    override val languageSectionTitle = "Язык интерфейса"
    override val updatesSectionTitle = "Обновления"
    override val updateNotificationsToggle = "Уведомлять об обновлениях"
    override val updateNotificationsSubtitle = "Показывать баннер при появлении новой версии"
    override val checkUpdatesNow = "Проверить обновления"
    override val checkingUpdates = "Проверка обновлений..."
    override val upToDate = "У вас установлена последняя версия"
    override fun currentVersion(version: String) = "Версия $version"
    override fun updateAvailableTitle(version: String) = "Доступно обновление $version"
    override val updateBannerSubtitle = "Нажмите для быстрой установки"
    override val downloadingUpdate = "Загрузка обновления"
    override val aboutSectionTitle = "О приложении"
    override val githubRepository = "Репозиторий на GitHub"

    override val chapter = "Глава"
    override val ofChapters = "из"
    override val page = "Стр."
    override val ofPages = "из"
    override val prevChapter = "Предыдущая глава"
    override val nextChapter = "Следующая глава"
    override val prevPage = "Предыдущая страница"
    override val nextPage = "Следующая страница"
    override val contents = "Оглавление"
    override val bookmarks = "Закладки"
    override val quotes = "Цитаты"
    override val searchInBook = "Поиск по тексту"
    override val searchInBookHint = "Введите слово или фразу..."
    override val noMatchesFound = "Совпадений не найдено"
    override val footnoteTitle = "Примечание"
    override val pagingHint = "Листание тапом по краям"
    override val loadingBook = "Загрузка книги..."
    override val closeSearch = "Закрыть поиск"
    override val chaptersNavigationHint = "Перемещение по главам (или свайп влево/вправо)"

    override val saveQuoteTitle = "Сохранить цитату"
    override val saveQuoteAction = "Сохранить цитату"
    override val quoteSavedNotification = "Цитата сохранена в закладках"
    override val textCopiedNotification = "Текст скопирован в буфер"
    override val addQuotePlaceholder = "Введите текст цитаты..."
    override val noQuotesYet = "Пока нет сохранённых цитат"
    override val noBookmarksYet = "Пока нет закладок"
}

class EnStrings : Strings {
    override val chooseAddMethod = "Add books to library"
    override val selectFiles = "Select files"
    override val selectFilesSubtitle = ".fb2, .epub, .fb2.zip files"
    override val scanFolder = "Scan folder"
    override val scanFolderSubtitle = "Recursive search for books in device folder"
    override val selectThirdParty = "Third-party file manager"
    override val selectThirdPartySubtitle = "Samsung My Files, Xiaomi, file managers"
    override val materialYouToggle = "Material You colors"
    override val materialYouSubtitle = "Adapt accent colors to device wallpaper"
    override val sortTitle = "Sort by"
    override val sortByDefault = "Default"
    override val sortByPopularDesc = "Most popular 🔥"
    override val sortByPopularAsc = "Least popular 📉"
    override val sortByTitleAsc = "Title (A → Z)"
    override val sortByTitleDesc = "Title (Z → A)"
    override val sortByAuthorAsc = "Author (A → Z)"
    override val sortByAuthorDesc = "Author (Z → A)"
    override val sortByYearDesc = "Newest first 📅"
    override val sortByYearAsc = "Oldest first ⏳"
    override val sortByPopularity = "Popularity"
    override val sortByTitle = "By Title"
    override val sortByAuthor = "By Author"
    override val catNew = "🔥 New"
    override val catPopular = "⭐ Popular"
    override val catAuthors = "✍️ Authors"
    override val catGenres = "🏷️ Genres"
    override val catNewSubtitle = "Fresh additions and updates"
    override val catPopularSubtitle = "Most read books in the catalog"
    override val catAuthorsSubtitle = "Alphabetical author index"
    override val catGenresSubtitle = "Books by genre and subject"
    override val catalogHomeTitle = "Find any book"
    override val collectionsTitle = "Collections"
    override val colAll = "All"
    override val colReading = "Reading"
    override val colFavorites = "Favorites"
    override val colUnread = "To Read"
    override val colFinished = "Completed"
    override val newCollection = "+ Shelf"
    override val createCollectionDialogTitle = "New Collection"
    override val collectionNamePlaceholder = "Shelf name"
    override val addToCollection = "Add to collection"
    override val markAsFinished = "Mark as completed"
    override val resetProgress = "Reset progress"
    override fun deleteCollectionConfirm(name: String) = "Delete collection \"$name\"? Books will remain in library."

    override val searchInLibrary = "Search library"
    override val searchLibraryPlaceholder = "Title, author or format..."

    override val backupSectionTitle = "Backup & Synchronization"
    override val createBackupTitle = "Export backup file"
    override val createBackupSubtitle = "Export books, quotes, bookmarks and settings to a file"
    override val sendToGoogleDriveTitle = "Send to Google Drive"
    override val sendToGoogleDriveSubtitle = "Quick save via Google Drive or other apps"
    override val restoreBackupTitle = "Restore from backup"
    override val restoreBackupSubtitle = "Restore library and progress from a backup file"
    override fun backupCreatedSuccess(count: Int) = "Backup created successfully ($count books)"
    override fun restoreCompletedSuccess(count: Int) = "Data restored successfully ($count books)"
    override val backupError = "Error processing backup file"
    override val shareBackupTitle = "Aura Reader Backup" 
    override val catalogHomeSubtitle = "Thousands of books freely available"
    override fun downloadsCount(count: Int) = "$count downloads"
    override fun yearLabel(year: String) = "Year $year"
    override val flibustaCatalog = "Online Catalog"
    override val flibustaCatalogSubtitle = "Search and download books online via OPDS"
    override val flibustaSearchHint = "Search by author or title..."
    override val flibustaMirrorTitle = "OPDS Settings"
    override val flibustaMirrorSubtitle = "OPDS server address. You can specify a working mirror if blocked."
    override val flibustaConnectionErrorTitle = "Could not connect to server"
    override val flibustaConnectionErrorSubtitle = "It might work when connecting from other countries."
    override val downloadFb2 = "Download FB2"
    override val downloadEpub = "Download EPUB"
    override val downloading = "Downloading..."
    override val openBookAction = "Read"
    override val annotation = "Annotation"
    override val reset = "Reset"
    override val appName = "Aura Reader"
    override val cancel = "Cancel"
    override val delete = "Delete"
    override val save = "Save"
    override val copy = "Copy"
    override val close = "Close"
    override val done = "Done"
    override val back = "Back"
    override val clear = "Clear"
    override val previous = "Previous"
    override val next = "Next"
    override val update = "Update"
    override val later = "Later"

    override val libraryTitle = "Aura Reader"
    override val addBooks = "Add Books"
    override val emptyLibraryTitle = "Library is empty"
    override val emptyLibrarySubtitle = "Add EPUB or FB2 books to begin reading"
    override val openSampleBook = "Open sample book"
    override val openFile = "Select files on device"
    override val recentBooks = "Recent Books"
    override val searchHint = "Search books and authors..."
    override val noBooksFound = "No books found"
    override val deleteBookTitle = "Delete book?"
    override fun deleteBookMessage(title: String) = "Book \"$title\" will be removed from library."
    override val todayReadingTime = "Read today"
    override fun minutesRead(minutes: Int) = "$minutes min"
    override val readingStatsToggle = "Track reading time"
    override val readingStatsSubtitle = "Display read minutes card on the main screen"
    override val readingStatsEmpty = "Time to read! Start today"
    override val progressRead = "Read"
    override fun searchResultsCount(count: Int) = "Search results ($count)"
    override fun noSearchResultsFound(query: String) = "No results found for \"$query\""

    override val retry = "Retry"
    override val download = "Download"
    override val loading = "Loading..."
    override val donate = "Support Author"
    override val moreOptions = "More options"
    override val fontAndTheme = "Font & Theme"
    override val bookmarksAndQuotes = "Bookmarks & Quotes"

    override val settingsTitle = "Settings"
    override val themeSectionTitle = "Theme"
    override val themeSystem = "System"
    override val themeLight = "Light"
    override val themeDark = "Dark"
    override val themeAmoled = "AMOLED"
    override val themeSepia = "Sepia"
    override val languageSectionTitle = "Language"
    override val updatesSectionTitle = "Updates"
    override val updateNotificationsToggle = "Update notifications"
    override val updateNotificationsSubtitle = "Show banner when a new version is available"
    override val checkUpdatesNow = "Check for updates"
    override val checkingUpdates = "Checking for updates..."
    override val upToDate = "You have the latest version"
    override fun currentVersion(version: String) = "Version $version"
    override fun updateAvailableTitle(version: String) = "Update available $version"
    override val updateBannerSubtitle = "Tap for fast installation"
    override val downloadingUpdate = "Downloading update"
    override val aboutSectionTitle = "About"
    override val githubRepository = "GitHub Repository"

    override val chapter = "Chapter"
    override val ofChapters = "of"
    override val page = "Page"
    override val ofPages = "of"
    override val prevChapter = "Previous Chapter"
    override val nextChapter = "Next Chapter"
    override val prevPage = "Previous Page"
    override val nextPage = "Next Page"
    override val contents = "Contents"
    override val bookmarks = "Bookmarks"
    override val quotes = "Quotes"
    override val searchInBook = "Search in Book"
    override val searchInBookHint = "Enter word or phrase..."
    override val noMatchesFound = "No matches found"
    override val footnoteTitle = "Footnote"
    override val pagingHint = "Tap edges to turn pages"
    override val loadingBook = "Loading book..."
    override val closeSearch = "Close search"
    override val chaptersNavigationHint = "Navigate chapters (or swipe left/right)"

    override val saveQuoteTitle = "Save Quote"
    override val saveQuoteAction = "Save Quote"
    override val quoteSavedNotification = "Quote saved to bookmarks"
    override val textCopiedNotification = "Text copied to clipboard"
    override val addQuotePlaceholder = "Enter quote text..."
    override val noQuotesYet = "No quotes saved yet"
    override val noBookmarksYet = "No bookmarks yet"
}

class UkStrings : Strings {
    override val chooseAddMethod = "Додати книги до бібліотеки"
    override val selectFiles = "Обрати файли"
    override val selectFilesSubtitle = "Файли .fb2, .epub, .fb2.zip"
    override val scanFolder = "Сканувати папку"
    override val scanFolderSubtitle = "Рекурсивний пошук усіх книг у папці пристрою"
    override val selectThirdParty = "Сторонній провідник"
    override val selectThirdPartySubtitle = "Samsung «Мої файли», Xiaomi, провідники"
    override val materialYouToggle = "Кольори Material You"
    override val materialYouSubtitle = "Адаптувати акцентні кольори під шпалери пристрою"
    override val sortTitle = "Сортування"
    override val sortByDefault = "За замовчуванням"
    override val sortByPopularDesc = "Найпопулярніші 🔥"
    override val sortByPopularAsc = "Менш популярні 📉"
    override val sortByTitleAsc = "За назвою (А → Я)"
    override val sortByTitleDesc = "За назвою (Я → А)"
    override val sortByAuthorAsc = "За автором (А → Я)"
    override val sortByAuthorDesc = "За автором (Я → А)"
    override val sortByYearDesc = "Спочатку нові 📅"
    override val sortByYearAsc = "Спочатку старі ⏳"
    override val sortByPopularity = "За популярністю"
    override val sortByTitle = "За назвою"
    override val sortByAuthor = "За автором"
    override val catNew = "🔥 Новинки"
    override val catPopular = "⭐ Популярне"
    override val catAuthors = "✍️ Автори"
    override val catGenres = "🏷️ Жанри"
    override val catNewSubtitle = "Свіжі надходження та новинки"
    override val catPopularSubtitle = "Найбільш популярні книги каталогу"
    override val catAuthorsSubtitle = "Алфавітний покажчик авторів"
    override val catGenresSubtitle = "Книги за жанрами та тематиками"
    override val catalogHomeTitle = "Знайдеться все"
    override val collectionsTitle = "Колекції"
    override val colAll = "Усі"
    override val colReading = "Читаю"
    override val colFavorites = "Обране"
    override val colUnread = "До читання"
    override val colFinished = "Прочитано"
    override val newCollection = "+ Полиця"
    override val createCollectionDialogTitle = "Нова колекція"
    override val collectionNamePlaceholder = "Назва полиці"
    override val addToCollection = "До колекції"
    override val markAsFinished = "Позначити прочитаною"
    override val resetProgress = "Скинути прогрес"
    override fun deleteCollectionConfirm(name: String) = "Видалити колекцію «$name»? Книги залишаться в бібліотеці."

    override val searchInLibrary = "Пошук у бібліотеці"
    override val searchLibraryPlaceholder = "Назва, автор або формат..."

    override val backupSectionTitle = "Резервне копіювання та синхронізація"
    override val createBackupTitle = "Зберегти резервну копію"
    override val createBackupSubtitle = "Експорт книг, цитат, закладок і налаштувань у файл"
    override val sendToGoogleDriveTitle = "Надіслати на Google Диск"
    override val sendToGoogleDriveSubtitle = "Швидке збереження через Google Диск або інші додатки"
    override val restoreBackupTitle = "Відновити з резервної копії"
    override val restoreBackupSubtitle = "Відновлення бібліотеки та прогресу з файлу бекапу"
    override fun backupCreatedSuccess(count: Int) = "Резервну копію успішно створено ($count книг)"
    override fun restoreCompletedSuccess(count: Int) = "Дані успішно відновлено ($count книг)"
    override val backupError = "Помилка при роботі з резервною копією"
    override val shareBackupTitle = "Резервна копія Aura Reader" 
    override val catalogHomeSubtitle = "Тисячі книг у вільному доступі"
    override fun downloadsCount(count: Int) = "$count завантажень"
    override fun yearLabel(year: String) = "$year р."
    override val flibustaCatalog = "Онлайн-каталог"
    override val flibustaCatalogSubtitle = "Пошук та завантаження книг онлайн через OPDS"
    override val flibustaSearchHint = "Пошук за автором або назвою..."
    override val flibustaMirrorTitle = "Налаштування OPDS"
    override val flibustaMirrorSubtitle = "Адреса OPDS-сервера. У разі блокувань можна вказати робоче дзеркало."
    override val flibustaConnectionErrorTitle = "Не вдалося з'єднатися з сервером"
    override val flibustaConnectionErrorSubtitle = "Можливо, з IP-адрес інших країн усе запрацює."
    override val downloadFb2 = "Завантажити FB2"
    override val downloadEpub = "Завантажити EPUB"
    override val downloading = "Завантаження..."
    override val openBookAction = "Читати"
    override val annotation = "Анотація"
    override val reset = "Скинути"
    override val appName = "Aura Reader"
    override val cancel = "Скасувати"
    override val delete = "Видалити"
    override val save = "Зберегти"
    override val copy = "Скопіювати"
    override val close = "Закрити"
    override val done = "Готово"
    override val back = "Назад"
    override val clear = "Очистити"
    override val previous = "Попереднє"
    override val next = "Наступне"
    override val update = "Оновити"
    override val later = "Пізніше"

    override val libraryTitle = "Aura Reader"
    override val addBooks = "Додати книги"
    override val emptyLibraryTitle = "Бібліотека порожня"
    override val emptyLibrarySubtitle = "Додайте книги у форматі EPUB або FB2 для початку читання"
    override val openSampleBook = "Відкрити демонстраційну книгу"
    override val openFile = "Обрати файли на пристрої"
    override val recentBooks = "Нещодавні книги"
    override val searchHint = "Пошук книг та авторів..."
    override val noBooksFound = "Книги не знайдено"
    override val deleteBookTitle = "Видалити книгу?"
    override fun deleteBookMessage(title: String) = "Книгу «$title» буде видалено з бібліотеки."
    override val todayReadingTime = "Сьогодні прочитано"
    override fun minutesRead(minutes: Int) = "$minutes хв"
    override val readingStatsToggle = "Враховувати час читання"
    override val readingStatsSubtitle = "Відображати картку прочитаних хвилин на головному екрані"
    override val readingStatsEmpty = "Час читати! Почніть сьогодні"
    override val progressRead = "Прочитано"
    override fun searchResultsCount(count: Int) = "Результати пошуку ($count)"
    override fun noSearchResultsFound(query: String) = "Нічого не знайдено за запитом «$query»"

    override val retry = "Повторити"
    override val download = "Завантажити"
    override val loading = "Завантаження..."
    override val donate = "Підтримати автора"
    override val moreOptions = "Додатково"
    override val fontAndTheme = "Шрифт і тема"
    override val bookmarksAndQuotes = "Закладки та цитати"

    override val settingsTitle = "Налаштування"
    override val themeSectionTitle = "Тема оформлення"
    override val themeSystem = "Системна"
    override val themeLight = "Світла"
    override val themeDark = "Темна"
    override val themeAmoled = "AMOLED"
    override val themeSepia = "Сепія"
    override val languageSectionTitle = "Мова інтерфейсу"
    override val updatesSectionTitle = "Оновлення"
    override val updateNotificationsToggle = "Сповіщати про оновлення"
    override val updateNotificationsSubtitle = "Показувати банер при виході нової версії"
    override val checkUpdatesNow = "Перевірити оновлення"
    override val checkingUpdates = "Перевірка оновлень..."
    override val upToDate = "У вас встановлено найновішу версію"
    override fun currentVersion(version: String) = "Версія $version"
    override fun updateAvailableTitle(version: String) = "Доступне оновлення $version"
    override val updateBannerSubtitle = "Натисніть для швидкого встановлення"
    override val downloadingUpdate = "Завантаження оновлення"
    override val aboutSectionTitle = "Про додаток"
    override val githubRepository = "Репозиторій на GitHub"

    override val chapter = "Розділ"
    override val ofChapters = "з"
    override val page = "Стор."
    override val ofPages = "з"
    override val prevChapter = "Попередній розділ"
    override val nextChapter = "Наступний розділ"
    override val prevPage = "Попередня сторінка"
    override val nextPage = "Наступна сторінка"
    override val contents = "Зміст"
    override val bookmarks = "Закладки"
    override val quotes = "Цитати"
    override val searchInBook = "Пошук у тексті"
    override val searchInBookHint = "Введіть слово або фразу..."
    override val noMatchesFound = "Збігів не знайдено"
    override val footnoteTitle = "Примітка"
    override val pagingHint = "Гортання тапом по краях"
    override val loadingBook = "Завантаження книги..."
    override val closeSearch = "Закрити пошук"
    override val chaptersNavigationHint = "Переміщення по розділах (або свайп ліворуч/праворуч)"

    override val saveQuoteTitle = "Зберегти цитату"
    override val saveQuoteAction = "Зберегти цитату"
    override val quoteSavedNotification = "Цитату збережено у закладках"
    override val textCopiedNotification = "Текст скопійовано у буфер"
    override val addQuotePlaceholder = "Введіть текст цитати..."
    override val noQuotesYet = "Поки немає збережених цитат"
    override val noBookmarksYet = "Поки немає закладок"
}

class BeStrings : Strings {
    override val chooseAddMethod = "Дадаць кнігі ў бібліятэку"
    override val selectFiles = "Абраць файлы"
    override val selectFilesSubtitle = "Файлы .fb2, .epub, .fb2.zip"
    override val scanFolder = "Сканаваць папку"
    override val scanFolderSubtitle = "Рэкурсіўны пошук усіх кніг у папцы прылады"
    override val selectThirdParty = "Сторонні праваднік"
    override val selectThirdPartySubtitle = "Samsung «Мае файлы», Xiaomi, праваднікі"
    override val materialYouToggle = "Колеры Material You"
    override val materialYouSubtitle = "Адаптаваць акцэнтныя колеры пад шпалеры прылады"
    override val sortTitle = "Сартаванне"
    override val sortByDefault = "Па змаўчанні"
    override val sortByPopularDesc = "Самыя папулярныя 🔥"
    override val sortByPopularAsc = "Менш папулярныя 📉"
    override val sortByTitleAsc = "Па назве (А → Я)"
    override val sortByTitleDesc = "Па назве (Я → А)"
    override val sortByAuthorAsc = "Па аўтару (А → Я)"
    override val sortByAuthorDesc = "Па аўтару (Я → А)"
    override val sortByYearDesc = "Спачатку новыя 📅"
    override val sortByYearAsc = "Спачатку старыя ⏳"
    override val sortByPopularity = "Па папулярнасці"
    override val sortByTitle = "Па назве"
    override val sortByAuthor = "Па аўтару"
    override val catNew = "🔥 Навінкі"
    override val catPopular = "⭐ Папулярнае"
    override val catAuthors = "✍️ Аўтары"
    override val catGenres = "🏷️ Жанры"
    override val catNewSubtitle = "Свежыя паступленні і абнаўленні"
    override val catPopularSubtitle = "Самыя чытаныя кнігі каталога"
    override val catAuthorsSubtitle = "Алфавітны паказальнік пісьменнікаў"
    override val catGenresSubtitle = "Кнігі па жанрах і тэмах"
    override val catalogHomeTitle = "Знойдзецца ўсё"
    override val collectionsTitle = "Калекцыі"
    override val colAll = "Усе"
    override val colReading = "Чытаю"
    override val colFavorites = "Выбранае"
    override val colUnread = "Да чытання"
    override val colFinished = "Прачытана"
    override val newCollection = "+ Паліца"
    override val createCollectionDialogTitle = "Новая калекцыя"
    override val collectionNamePlaceholder = "Назва паліцы"
    override val addToCollection = "У калекцыю"
    override val markAsFinished = "Адзначыць як прачытанае"
    override val resetProgress = "Скінуць прагрэс"
    override fun deleteCollectionConfirm(name: String) = "Выдаліць калекцыю «$name»? Кнігі застануцца ў бібліятэцы."

    override val searchInLibrary = "Пошук у бібліятэцы"
    override val searchLibraryPlaceholder = "Назва, аўтар ці фармат..."

    override val backupSectionTitle = "Рэзервовае капіраванне і сінхранізацыя"
    override val createBackupTitle = "Захаваць рэзервовую копію"
    override val createBackupSubtitle = "Экспарт кніг, цытат, закладак і налад у файл"
    override val sendToGoogleDriveTitle = "Адправіць на Google Дыск"
    override val sendToGoogleDriveSubtitle = "Хуткае захаванне праз Google Дыск або іншыя праграмы"
    override val restoreBackupTitle = "Аднавіць з рэзервовай копіі"
    override val restoreBackupSubtitle = "Аднаўленне бібліятэкі і прагрэсу з файла бэкапу"
    override fun backupCreatedSuccess(count: Int) = "Рэзервовая копія паспяхова створана ($count кніг)"
    override fun restoreCompletedSuccess(count: Int) = "Даныя паспяхова адноўлены ($count кніг)"
    override val backupError = "Памылка пры працы з рэзервовай копіяй"
    override val shareBackupTitle = "Рэзервовая копія Aura Reader" 
    override val catalogHomeSubtitle = "Тысячы кніг у вольным доступе"
    override fun downloadsCount(count: Int) = "$count спампоўванняў"
    override fun yearLabel(year: String) = "$year г."
    override val flibustaCatalog = "Анлайн-каталог"
    override val flibustaCatalogSubtitle = "Пошук і спампоўванне кніг анлайн праз OPDS"
    override val flibustaSearchHint = "Пошук па аўтару або назве..."
    override val flibustaMirrorTitle = "Налады OPDS"
    override val flibustaMirrorSubtitle = "Адрас OPDS-сервера. Пры блакіроўках можна ўказаць працоўнае люстэрка."
    override val flibustaConnectionErrorTitle = "Не ўдалося звязацца з серверам"
    override val flibustaConnectionErrorSubtitle = "Магчыма, з IP-адрасоў іншых краін усё запрацуе."
    override val downloadFb2 = "Спампаваць FB2"
    override val downloadEpub = "Спампаваць EPUB"
    override val downloading = "Спампоўванне..."
    override val openBookAction = "Чытаць"
    override val annotation = "Анатацыя"
    override val reset = "Скінуць"
    override val appName = "Aura Reader"
    override val cancel = "Адмена"
    override val delete = "Выдаліць"
    override val save = "Захаваць"
    override val copy = "Скапіяваць"
    override val close = "Закрыць"
    override val done = "Гатова"
    override val back = "Назад"
    override val clear = "Ачысціць"
    override val previous = "Папярэдняе"
    override val next = "Наступнае"
    override val update = "Абнавіць"
    override val later = "Пазней"

    override val libraryTitle = "Aura Reader"
    override val addBooks = "Дадаць кнігі"
    override val emptyLibraryTitle = "Бібліятэка пустая"
    override val emptyLibrarySubtitle = "Дадайце кнігі ў фармаце EPUB або FB2 для пачатку чытання"
    override val openSampleBook = "Адкрыць дэманстрацыйную кнігу"
    override val openFile = "Абраць файлы на прыладзе"
    override val recentBooks = "Нядаўнія кнігі"
    override val searchHint = "Пошук па кнігах і аўтарах..."
    override val noBooksFound = "Кнігі не знойдзены"
    override val deleteBookTitle = "Выдаліць кнігу?"
    override fun deleteBookMessage(title: String) = "Кніга «$title» будзе выдалена з бібліятэкі."
    override val todayReadingTime = "Сёння прачытана"
    override fun minutesRead(minutes: Int) = "$minutes хв"
    override val readingStatsToggle = "Улічваць час чытання"
    override val readingStatsSubtitle = "Паказваць картку прачытаных хвілін на галоўным экране"
    override val readingStatsEmpty = "Час для чытання! Пачніце сёння"
    override val progressRead = "Прачытана"
    override fun searchResultsCount(count: Int) = "Вынікі пошуку ($count)"
    override fun noSearchResultsFound(query: String) = "Нічога не знойдзена па запыце «$query»"

    override val retry = "Паўтарыць"
    override val download = "Спампаваць"
    override val loading = "Загрузка..."
    override val donate = "Падтрымаць аўтара"
    override val moreOptions = "Дадаткова"
    override val fontAndTheme = "Шрыфт і тэма"
    override val bookmarksAndQuotes = "Закладкі і цытаты"

    override val settingsTitle = "Налады"
    override val themeSectionTitle = "Тэма афармлення"
    override val themeSystem = "Сістэмная"
    override val themeLight = "Светлая"
    override val themeDark = "Цёмная"
    override val themeAmoled = "AMOLED"
    override val themeSepia = "Сепія"
    override val languageSectionTitle = "Мова інтэрфейсу"
    override val updatesSectionTitle = "Абнаўленні"
    override val updateNotificationsToggle = "Паведамляць пра абнаўленні"
    override val updateNotificationsSubtitle = "Паказваць банер пры з'яўленні новай версіі"
    override val checkUpdatesNow = "Праверыць абнаўленні"
    override val checkingUpdates = "Праверка абнаўленняў..."
    override val upToDate = "У вас усталявана апошняя версія"
    override fun currentVersion(version: String) = "Версія $version"
    override fun updateAvailableTitle(version: String) = "Даступна абнаўленне $version"
    override val updateBannerSubtitle = "Націсніце для хуткай устаноўкі"
    override val downloadingUpdate = "Спампоўванне абнаўлення"
    override val aboutSectionTitle = "Пра праграму"
    override val githubRepository = "Рэпазіторый на GitHub"

    override val chapter = "Раздзел"
    override val ofChapters = "з"
    override val page = "Стар."
    override val ofPages = "з"
    override val prevChapter = "Папярэдні раздзел"
    override val nextChapter = "Наступны раздзел"
    override val prevPage = "Папярэдняя старонка"
    override val nextPage = "Наступная старонка"
    override val contents = "Змест"
    override val bookmarks = "Закладкі"
    override val quotes = "Цытаты"
    override val searchInBook = "Пошук у тэксце"
    override val searchInBookHint = "Увядзіце слова або фразу..."
    override val noMatchesFound = "Супадзенняў не знойдзена"
    override val footnoteTitle = "Заўвага"
    override val pagingHint = "Гартанне тапам па краях"
    override val loadingBook = "Загрузка кнігі..."
    override val closeSearch = "Закрыць пошук"
    override val chaptersNavigationHint = "Перасоўванне па раздзелах (або свайп улева/управа)"

    override val saveQuoteTitle = "Захаваць цытату"
    override val saveQuoteAction = "Захаваць цытату"
    override val quoteSavedNotification = "Цытата захавана ў закладках"
    override val textCopiedNotification = "Тэкст скапіяваны ў буфер"
    override val addQuotePlaceholder = "Увядзіце тэкст цытаты..."
    override val noQuotesYet = "Пакуль няма захаваных цытат"
    override val noBookmarksYet = "Пакуль няма закладак"
}

class PlStrings : Strings {
    override val chooseAddMethod = "Dodaj książki do biblioteki"
    override val selectFiles = "Wybierz pliki"
    override val selectFilesSubtitle = "Pliki .fb2, .epub, .fb2.zip"
    override val scanFolder = "Skanuj folder"
    override val scanFolderSubtitle = "Rekurencyjne wyszukiwanie książek w folderze urządzenia"
    override val selectThirdParty = "Menedżer plików innej firmy"
    override val selectThirdPartySubtitle = "Samsung Moje pliki, Xiaomi, menedżery"
    override val materialYouToggle = "Kolory Material You"
    override val materialYouSubtitle = "Dostosuj kolory akcentów do tapety urządzenia"
    override val sortTitle = "Sortowanie"
    override val sortByDefault = "Domyślnie"
    override val sortByPopularDesc = "Najpopularniejsze 🔥"
    override val sortByPopularAsc = "Mniej popularne 📉"
    override val sortByTitleAsc = "Po tytule (A → Z)"
    override val sortByTitleDesc = "Po tytule (Z → A)"
    override val sortByAuthorAsc = "Po autorze (A → Z)"
    override val sortByAuthorDesc = "Po autorze (Z → A)"
    override val sortByYearDesc = "Najpierw nowe 📅"
    override val sortByYearAsc = "Najpierw stare ⏳"
    override val sortByPopularity = "Według popularności"
    override val sortByTitle = "Według tytułu"
    override val sortByAuthor = "Według autora"
    override val catNew = "🔥 Nowości"
    override val catPopular = "⭐ Popularne"
    override val catAuthors = "✍️ Autorzy"
    override val catGenres = "🏷️ Gatunki"
    override val catNewSubtitle = "Świeże dostawy i aktualizacje"
    override val catPopularSubtitle = "Najchętniej czytane książki w katalogu"
    override val catAuthorsSubtitle = "Indeks alfabetyczny autorów"
    override val catGenresSubtitle = "Książki według gatunków i tematów"
    override val catalogHomeTitle = "Znajdziesz wszystko"
    override val collectionsTitle = "Kolekcje"
    override val colAll = "Wszystkie"
    override val colReading = "Czytam"
    override val colFavorites = "Ulubione"
    override val colUnread = "Do przeczytania"
    override val colFinished = "Przeczytane"
    override val newCollection = "+ Półka"
    override val createCollectionDialogTitle = "Nowa kolekcja"
    override val collectionNamePlaceholder = "Nazwa półki"
    override val addToCollection = "Do kolekcji"
    override val markAsFinished = "Oznacz jako przeczytane"
    override val resetProgress = "Zresetuj postęp"
    override fun deleteCollectionConfirm(name: String) = "Usunąć kolekcję „$name”? Książki pozostaną w bibliotece."

    override val searchInLibrary = "Szukaj w bibliotece"
    override val searchLibraryPlaceholder = "Tytuł, autor lub format..."

    override val backupSectionTitle = "Kopia zapasowa i synchronizacja"
    override val createBackupTitle = "Zapisz kopię zapasową"
    override val createBackupSubtitle = "Eksport książek, cytatów, zakładek i ustawień do pliku"
    override val sendToGoogleDriveTitle = "Wyślij na Dysk Google"
    override val sendToGoogleDriveSubtitle = "Szybkie zapisywanie na Dysku Google lub w innych aplikacjach"
    override val restoreBackupTitle = "Przywróć z kopii zapasowej"
    override val restoreBackupSubtitle = "Przywracanie biblioteki i postępów z pliku kopii"
    override fun backupCreatedSuccess(count: Int) = "Kopia zapasowa utworzona pomyślnie ($count książek)"
    override fun restoreCompletedSuccess(count: Int) = "Dane przywrócone pomyślnie ($count książek)"
    override val backupError = "Błąd podczas przetwarzania kopii zapasowej"
    override val shareBackupTitle = "Kopia zapasowa Aura Reader" 
    override val catalogHomeSubtitle = "Tysiące książek w wolnym dostępie"
    override fun downloadsCount(count: Int) = "$count pobrań"
    override fun yearLabel(year: String) = "Rok $year"
    override val flibustaCatalog = "Katalog online"
    override val flibustaCatalogSubtitle = "Wyszukiwanie i pobieranie książek online przez OPDS"
    override val flibustaSearchHint = "Szukaj według autora lub tytułu..."
    override val flibustaMirrorTitle = "Ustawienia OPDS"
    override val flibustaMirrorSubtitle = "Adres serwera OPDS. W przypadku blokad można podać działające lustro."
    override val flibustaConnectionErrorTitle = "Nie udało się połączyć z serwerem"
    override val flibustaConnectionErrorSubtitle = "Być może połączenie zadziała z adresów IP innych krajów."
    override val downloadFb2 = "Pobierz FB2"
    override val downloadEpub = "Pobierz EPUB"
    override val downloading = "Pobieranie..."
    override val openBookAction = "Czytaj"
    override val annotation = "Adnotacja"
    override val reset = "Resetuj"
    override val appName = "Aura Reader"
    override val cancel = "Anuluj"
    override val delete = "Usuń"
    override val save = "Zapisz"
    override val copy = "Kopiuj"
    override val close = "Zamknij"
    override val done = "Gotowe"
    override val back = "Wstecz"
    override val clear = "Wyczyść"
    override val previous = "Poprzednie"
    override val next = "Następne"
    override val update = "Aktualizuj"
    override val later = "Później"

    override val libraryTitle = "Aura Reader"
    override val addBooks = "Dodaj książki"
    override val emptyLibraryTitle = "Biblioteka jest pusta"
    override val emptyLibrarySubtitle = "Dodaj książki w formacie EPUB lub FB2, aby zacząć czytać"
    override val openSampleBook = "Otwórz przykładową książkę"
    override val openFile = "Wybierz pliki na urządzeniu"
    override val recentBooks = "Ostatnie książki"
    override val searchHint = "Szukaj książek i autorów..."
    override val noBooksFound = "Nie znaleziono książek"
    override val deleteBookTitle = "Usunąć książkę?"
    override fun deleteBookMessage(title: String) = "Książka „$title” zostanie usunięta z biblioteki."
    override val todayReadingTime = "Przeczytano dzisiaj"
    override fun minutesRead(minutes: Int) = "$minutes min"
    override val readingStatsToggle = "Śledź czas czytania"
    override val readingStatsSubtitle = "Pokaż przeczytane minuty na ekranie głównym"
    override val readingStatsEmpty = "Czas na czytanie! Zacznij dzisiaj"
    override val progressRead = "Przeczytano"
    override fun searchResultsCount(count: Int) = "Wyniki wyszukiwania ($count)"
    override fun noSearchResultsFound(query: String) = "Nie znaleziono wyników dla „$query”"

    override val retry = "Ponów"
    override val download = "Pobierz"
    override val loading = "Ładowanie..."
    override val donate = "Wesprzyj autora"
    override val moreOptions = "Więcej opcji"
    override val fontAndTheme = "Czcionka i motyw"
    override val bookmarksAndQuotes = "Zakładki i cytaty"

    override val settingsTitle = "Ustawienia"
    override val themeSectionTitle = "Motyw"
    override val themeSystem = "Systemowy"
    override val themeLight = "Jasny"
    override val themeDark = "Ciemny"
    override val themeAmoled = "AMOLED"
    override val themeSepia = "Sepia"
    override val languageSectionTitle = "Język interfejsu"
    override val updatesSectionTitle = "Aktualizacje"
    override val updateNotificationsToggle = "Powiadomienia o aktualizacjach"
    override val updateNotificationsSubtitle = "Pokaż baner, gdy dostępna jest nowa wersja"
    override val checkUpdatesNow = "Sprawdź aktualizacje"
    override val checkingUpdates = "Sprawdzanie aktualizacji..."
    override val upToDate = "Masz najnowszą wersję"
    override fun currentVersion(version: String) = "Wersja $version"
    override fun updateAvailableTitle(version: String) = "Dostępna aktualizacja $version"
    override val updateBannerSubtitle = "Dotknij, aby szybko zainstalować"
    override val downloadingUpdate = "Pobieranie aktualizacji"
    override val aboutSectionTitle = "O aplikacji"
    override val githubRepository = "Repozytorium GitHub"

    override val chapter = "Rozdział"
    override val ofChapters = "z"
    override val page = "Str."
    override val ofPages = "z"
    override val prevChapter = "Poprzedni rozdział"
    override val nextChapter = "Następny rozdział"
    override val prevPage = "Poprzednia strona"
    override val nextPage = "Następna strona"
    override val contents = "Spis treści"
    override val bookmarks = "Zakładki"
    override val quotes = "Cytaty"
    override val searchInBook = "Szukaj w książce"
    override val searchInBookHint = "Wpisz słowo lub frazę..."
    override val noMatchesFound = "Nie znaleziono wyników"
    override val footnoteTitle = "Przypis"
    override val pagingHint = "Dotknij krawędzi, aby zmienić stronę"
    override val loadingBook = "Ładowanie książki..."
    override val closeSearch = "Zamknij szukanie"
    override val chaptersNavigationHint = "Nawigacja po rozdziałach (lub przesunięcie w lewo/prawo)"

    override val saveQuoteTitle = "Zapisz cytat"
    override val saveQuoteAction = "Zapisz cytat"
    override val quoteSavedNotification = "Cytat zapisany w zakładkach"
    override val textCopiedNotification = "Tekst skopiowany do schowka"
    override val addQuotePlaceholder = "Wpisz tekst cytatu..."
    override val noQuotesYet = "Brak zapisanych cytatów"
    override val noBookmarksYet = "Brak zakładek"
}

fun getStrings(language: AppLanguage): Strings = when (language) {
    AppLanguage.RU -> RuStrings()
    AppLanguage.EN -> EnStrings()
    AppLanguage.UK -> UkStrings()
    AppLanguage.BE -> BeStrings()
    AppLanguage.PL -> PlStrings()
}

val LocalAppStrings = staticCompositionLocalOf<Strings> { RuStrings() }
