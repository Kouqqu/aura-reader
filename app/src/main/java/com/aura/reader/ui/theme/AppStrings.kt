package com.aura.reader.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf

enum class AppLanguage(val code: String, val title: String) {
    RU("ru", "🇷🇺 Русский"),
    EN("en", "🇬🇧 English"),
    UK("uk", "🇺🇦 Українська"),
    BE("be", "🇧🇾 Беларуская"),
    PL("pl", "🇵🇱 Polski"),
    CS("cs", "🇨🇿 Čeština");

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
    val opdsCatalog: String
    val opdsCatalogSubtitle: String
    val opdsSearchHint: String
    val opdsMirrorTitle: String
    val opdsMirrorSubtitle: String
    val opdsConnectionErrorTitle: String
    val opdsConnectionErrorSubtitle: String
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
    val sortByRecent: String
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
    val quoteAction: String
    val quoteSavedNotification: String
    val textCopiedNotification: String
    val addQuotePlaceholder: String
    val noQuotesYet: String
    val noBookmarksYet: String

    val onlineCatalogSectionTitle: String
    val additionalFeaturesSectionTitle: String
    val customOpdsToggle: String
    val customOpdsSubtitle: String
    val customOpdsUrl: String
    val customOpdsOpenRoot: String

    val pageAnimationSectionTitle: String
    val pageAnimationSlide: String
    val pageAnimationInstant: String
    val pageAnimationCurl: String

    val twoColumnSpreadSectionTitle: String
    val twoColumnSpreadSubtitle: String
    val twoColumnSpreadAuto: String
    val twoColumnSpreadOff: String
    val twoColumnSpreadAlways: String

    val autoHyphenationTitle: String
    val autoHyphenationSubtitle: String

    val hapticFeedbackTitle: String
    val hapticFeedbackSubtitle: String

    val dictionaryTitle: String
    val dictionaryAction: String
    val translateAction: String
    val copyAction: String
    val dictionaryLoading: String
    val dictionaryNotFound: String
    val dictionarySourcePrefix: String
    val translationOriginalTitle: String
    val translationTargetTitle: String
    val translationFailed: String
    val closeDialog: String
    val understandFootnote: String
    fun minutesLeftInChapter(minutes: Int): String

    val parallaxCoverHint: String

    val shareBookFile: String
    val changeCover: String
    val searchCoverTitle: String
    val searchCoverOnline: String
    val pickFromGallery: String
    val removeCover: String
    val noCoversFoundOnline: String
    val coverUpdated: String
    val applyCover: String

    // Highlights & Quotes
    val highlightColorTitle: String
    val quoteSavedAsHighlight: String

    // Reading Stats Bottom Sheet
    val readingStatsSheetTitle: String
    val readingStreakDays: String
    fun readingStreakFormat(days: Int): String
    val readingStreakKeepGoing: String
    val readingTimeTodayCard: String
    val readingTimeTotalCard: String
    val readingTimeAvgCard: String
    val readingWeeklyActivityTitle: String
    fun totalHoursAndMinutes(hours: Int, minutes: Int): String
    val dayMon: String
    val dayTue: String
    val dayWed: String
    val dayThu: String
    val dayFri: String
    val daySat: String
    val daySun: String

    // Text to Speech (TTS)
    val ttsListenAction: String
    val ttsPlaying: String
    val ttsPaused: String
    val ttsSpeed: String
    val ttsStop: String
    val ttsNotificationTitle: String
    val ttsInitializing: String
    val ttsError: String
    val ttsLanguageNotSupported: String

    // App Widget
    val widgetContinueReading: String
    val widgetNoBook: String
    val widgetOpenApp: String

    // Reader Settings Strings
    val readerSettingsTitle: String
    val themeModeTitle: String
    val fontSizeTitle: String
    val fontFamilyTitle: String
    val fontFamilySerif: String
    val fontFamilySansSerif: String
    val fontFamilyMonospace: String
    val fontFamilySystem: String
    val lineHeightTitle: String
    val readingModeTitle: String
    val readingModePaged: String
    val readingModeScroll: String
    val lightImageBgTitle: String
    val lightImageBgSubtitle: String

    // Extended TTS Strings
    val ttsSettingsSheetTitle: String
    val ttsVoiceTitle: String
    val ttsVoiceDefault: String
    val ttsPitchTitle: String
    fun ttsParagraphProgress(current: Int, total: Int): String

    // Currently Reading & View Mode
    val currentlyReadingBadge: String
    val continueReadingAction: String
    val viewModeList: String
    val viewModeGrid: String
    // Beta 1.4.1 additions
    val resetStatsTitle: String
    val resetStatsConfirmTitle: String
    val resetStatsConfirmMessage: String
    val vpnNoticeTitle: String
    val vpnNoticeMessage: String
    val vpnNoticeUnderstood: String
    val devModeTitle: String
    val devModePasswordPrompt: String
    val devModePasswordPlaceholder: String
    val devModeActivated: String
    val devModeWrongPassword: String
    val updateChannelTitle: String
    val updateChannelSubtitle: String
    val updateChannelRelease: String
    val updateChannelBeta: String
    val sortCustomOrder: String
    val moveUp: String
    val moveDown: String
    val opdsDownloadMobi: String
    val opdsDownloadPdf: String

    // Beta 1.4.2 additions
    val syncThemesTitle: String
    val syncThemesSubtitle: String
    val readerThemeSettingsTitle: String
    val readerThemeSettingsSubtitle: String
    val specificFontTitle: String
    val fontDefault: String
    val bookInfoTitle: String
    val chapters: String
    val progress: String
    val pages: String
    val format: String
    val skipVersion: String
    val channelSwitchPrompt: String

    // Beta 1.4.3 additions
    val appFontTitle: String
    val appFontSubtitle: String
    val appFontDefault: String
    val appFontGoogleSans: String
    val resetReadingSpeedTitle: String
    val resetReadingSpeedSubtitle: String
    val resetReadingSpeedSuccess: String
}


class RuStrings : Strings {
    override val chooseAddMethod = "Добавить книги в библиотеку"
    override val selectFiles = "Выбрать файлы"
    override val selectFilesSubtitle = "Файлы .fb2, .epub, .pdf, .txt"
    override val scanFolder = "Сканировать папку"
    override val scanFolderSubtitle = "Рекурсивный поиск всех книг в папке устройства"
    override val selectThirdParty = "Сторонний проводник"
    override val selectThirdPartySubtitle = "Samsung «Мои файлы», Xiaomi, проводники"
    override val materialYouToggle = "Цвета Material You"
    override val materialYouSubtitle = "Адаптировать акцентные цвета под обои устройства"
    override val sortTitle = "Сортировка"
    override val sortByDefault = "По умолчанию"
    override val sortByRecent = "Недавние"
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
    override val newCollection = "Полка"
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
    override val opdsCatalog = "Онлайн-каталог"
    override val opdsCatalogSubtitle = "Поиск и скачивание книг онлайн через OPDS"
    override val opdsSearchHint = "Поиск по автору или названию..."
    override val opdsMirrorTitle = "Настройки OPDS"
    override val opdsMirrorSubtitle = "Адрес OPDS-сервера. При блокировках можно указать рабочее зеркало."
    override val opdsConnectionErrorTitle = "Не удалось связаться с сервером"
    override val opdsConnectionErrorSubtitle = "Возможно, с IP-адресов других стран всё заработает."
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
    override val quoteAction = "Цитата"
    override val quoteSavedNotification = "Цитата сохранена в закладках"
    override val textCopiedNotification = "Текст скопирован в буфер"
    override val addQuotePlaceholder = "Введите текст цитаты..."
    override val noQuotesYet = "Пока нет сохранённых цитат"
    override val noBookmarksYet = "Пока нет закладок"

    override val onlineCatalogSectionTitle = "Онлайн-каталог"
    override val customOpdsToggle = "Свой OPDS-каталог"
    override val customOpdsSubtitle = "Ввод своего адреса и отключение подборок встроенного каталога"
    override val additionalFeaturesSectionTitle = "Дополнительные функции"
    override val customOpdsUrl = "Адрес OPDS"
    override val customOpdsOpenRoot = "Открыть каталог"

    override val pageAnimationSectionTitle = "Анимация перелистывания"
    override val pageAnimationSlide = "Сдвиг"
    override val pageAnimationInstant = "Мгновенно"
    override val pageAnimationCurl = "Разворот"

    override val twoColumnSpreadSectionTitle = "Разворот книги (две страницы)"
    override val twoColumnSpreadSubtitle = "Автоматически на планшетах и экранах Fold"
    override val twoColumnSpreadAuto = "Авто"
    override val twoColumnSpreadOff = "1 страница"
    override val twoColumnSpreadAlways = "2 страницы"

    override val autoHyphenationTitle = "Перенос слов (дефисы)"
    override val autoHyphenationSubtitle = "Книжный перенос длинных слов по слогам"

    override val hapticFeedbackTitle = "Тактильный отклик"
    override val hapticFeedbackSubtitle = "Мягкая вибрация, следующая за пальцем"

    override val dictionaryTitle = "Толковый словарь"
    override val dictionaryAction = "Словарь"
    override val translateAction = "Перевод"
    override val copyAction = "Копировать"
    override val dictionaryLoading = "Поиск толкования…"
    override val dictionaryNotFound = "Толкование не найдено"
    override val dictionarySourcePrefix = "Источник:"
    override val translationOriginalTitle = "Оригинал:"
    override val translationTargetTitle = "Перевод:"
    override val translationFailed = "Не удалось перевести текст"
    override val closeDialog = "Закрыть"
    override val understandFootnote = "Понятно"
    override fun minutesLeftInChapter(minutes: Int) = "$minutes мин до конца главы"
    override val parallaxCoverHint = "Наклоняйте устройство или проведите пальцем для 3D-эффекта"
    override val shareBookFile = "Поделиться файлом"
    override val changeCover = "Сменить обложку"
    override val searchCoverTitle = "Выбор обложки"
    override val searchCoverOnline = "Искать в сети"
    override val pickFromGallery = "Выбрать из галереи"
    override val removeCover = "Сбросить обложку"
    override val noCoversFoundOnline = "Обложки не найдены"
    override val coverUpdated = "Обложка обновлена"
    override val applyCover = "Применить"


    override val highlightColorTitle = "Цвет маркера"
    override val quoteSavedAsHighlight = "Цитата сохранена и выделена в тексте"
    override val readingStatsSheetTitle = "Статистика чтения"
    override val readingStreakDays = "Ударный режим"
    override fun readingStreakFormat(days: Int) = "$days ${if (days % 10 == 1 && days % 100 != 11) "день" else if (days % 10 in 2..4 && days % 100 !in 12..14) "дня" else "дней"} подряд 🔥"
    override val readingStreakKeepGoing = "Читайте каждый день, чтобы поддерживать серию!"
    override val readingTimeTodayCard = "Сегодня"
    override val readingTimeTotalCard = "Всего прочитано"
    override val readingTimeAvgCard = "В среднем в день"
    override val readingWeeklyActivityTitle = "Активность за 7 дней"
    override fun totalHoursAndMinutes(hours: Int, minutes: Int) = if (hours > 0) "$hours ч $minutes мин" else "$minutes мин"
    override val dayMon = "Пн"
    override val dayTue = "Вт"
    override val dayWed = "Ср"
    override val dayThu = "Чт"
    override val dayFri = "Пт"
    override val daySat = "Сб"
    override val daySun = "Вс"
    override val ttsListenAction = "Озвучить (TTS)"
    override val ttsPlaying = "Воспроизведение"
    override val ttsPaused = "Пауза"
    override val ttsSpeed = "Скорость"
    override val ttsStop = "Остановить"
    override val ttsNotificationTitle = "Озвучка книги"
    override val ttsInitializing = "Инициализация синтезатора речи..."
    override val ttsError = "Синтезатор речи недоступен"
    override val ttsLanguageNotSupported = "Язык книги не поддерживается синтезатором"
    override val widgetContinueReading = "Продолжить чтение"
    override val widgetNoBook = "Книги не открыты"
    override val widgetOpenApp = "Открыть Aura Reader"

    override val readerSettingsTitle = "Настройки чтения"
    override val themeModeTitle = "Тема оформления"
    override val fontSizeTitle = "Размер шрифта"
    override val fontFamilyTitle = "Гарнитура шрифта"
    override val fontFamilySerif = "С засечками"
    override val fontFamilySansSerif = "Без засечек"
    override val fontFamilyMonospace = "Моноширинный"
    override val fontFamilySystem = "Системный"
    override val lineHeightTitle = "Межстрочный интервал"
    override val readingModeTitle = "Режим чтения"
    override val readingModePaged = "Постраничный"
    override val readingModeScroll = "Свиток"
    override val lightImageBgTitle = "Светлая подложка под иллюстрации"
    override val lightImageBgSubtitle = "Светлый фон для контрастности картинок в темных темах"

    override val ttsSettingsSheetTitle = "Озвучка книги"
    override val ttsVoiceTitle = "Голос"
    override val ttsVoiceDefault = "По умолчанию"
    override val ttsPitchTitle = "Высота тона"
    override fun ttsParagraphProgress(current: Int, total: Int) = "Абзац $current из $total"

    override val currentlyReadingBadge = "СЕЙЧАС ЧИТАЮ"
    override val continueReadingAction = "Продолжить чтение"
    override val viewModeList = "Список"
    override val viewModeGrid = "Сетка"
    override val resetStatsTitle = "Сбросить статистику"
    override val resetStatsConfirmTitle = "Сбросить статистику чтения?"
    override val resetStatsConfirmMessage = "Все прочитанные минуты, стрики и графики активности будут обнулены. Это действие нельзя отменить."
    override val vpnNoticeTitle = "Онлайн-каталог и VPN"
    override val vpnNoticeMessage = "Встроенный каталог книг в России может быть заблокирован провайдерами и стабильно работает только при включенном VPN."
    override val vpnNoticeUnderstood = "Понятно"
    override val devModeTitle = "Режим разработчика"
    override val devModePasswordPrompt = "Введите пароль разработчика"
    override val devModePasswordPlaceholder = "Пароль"
    override val devModeActivated = "Режим разработчика активирован!"
    override val devModeWrongPassword = "Неверный пароль"
    override val updateChannelTitle = "Канал обновлений"
    override val updateChannelSubtitle = "Выбор ветки для проверки обновлений приложения"
    override val updateChannelRelease = "Стабильный (Release)"
    override val updateChannelBeta = "Бета (Beta)"
    override val sortCustomOrder = "Свой порядок"
    override val moveUp = "Переместить выше"
    override val moveDown = "Переместить ниже"
    override val opdsDownloadMobi = "Скачать MOBI"
    override val opdsDownloadPdf = "Скачать PDF"

    // Beta 1.4.2 additions
    override val syncThemesTitle = "Синхронизировать темы"
    override val syncThemesSubtitle = "Одинаковая тема для интерфейса приложения и режима чтения"
    override val readerThemeSettingsTitle = "Оформление книг"
    override val readerThemeSettingsSubtitle = "Настройка тем, шрифтов и анимаций для режима чтения"
    override val specificFontTitle = "Гарнитура шрифта"
    override val fontDefault = "По умолчанию"
    override val bookInfoTitle = "О книге"
    override val chapters = "Главы"
    override val progress = "Прогресс"
    override val pages = "Страницы"
    override val format = "Формат"
    override val skipVersion = "Не напоминать"
    override val channelSwitchPrompt = "Выберите канал обновлений:"

    // Beta 1.4.3 additions
    override val appFontTitle = "Шрифт приложения"
    override val appFontSubtitle = "Шрифт элементов интерфейса"
    override val appFontDefault = "По умолчанию"
    override val appFontGoogleSans = "Google Sans"
    override val resetReadingSpeedTitle = "Сбросить скорость чтения"
    override val resetReadingSpeedSubtitle = "Вернуть среднюю скорость к 200 сл/мин"
    override val resetReadingSpeedSuccess = "Скорость чтения сброшена (200 сл/мин)"
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
    override val sortByRecent = "Recent"
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
    override val newCollection = "Shelf"
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
    override val opdsCatalog = "Online Catalog"
    override val opdsCatalogSubtitle = "Search and download books online via OPDS"
    override val opdsSearchHint = "Search by author or title..."
    override val opdsMirrorTitle = "OPDS Settings"
    override val opdsMirrorSubtitle = "OPDS server address. You can specify a working mirror if blocked."
    override val opdsConnectionErrorTitle = "Could not connect to server"
    override val opdsConnectionErrorSubtitle = "It might work when connecting from other countries."
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
    override val quoteAction = "Quote"
    override val quoteSavedNotification = "Quote saved to bookmarks"
    override val textCopiedNotification = "Text copied to clipboard"
    override val addQuotePlaceholder = "Enter quote text..."
    override val noQuotesYet = "No quotes saved yet"
    override val noBookmarksYet = "No bookmarks yet"

    override val onlineCatalogSectionTitle = "Online catalog"
    override val customOpdsToggle = "Custom OPDS catalog"
    override val customOpdsSubtitle = "Enter custom address and disable built-in catalog feeds"
    override val additionalFeaturesSectionTitle = "Additional features"
    override val customOpdsUrl = "OPDS URL"
    override val customOpdsOpenRoot = "Open catalog"

    override val pageAnimationSectionTitle = "Page turn animation"
    override val pageAnimationSlide = "Slide"
    override val pageAnimationInstant = "Instant"
    override val pageAnimationCurl = "Curl"

    override val twoColumnSpreadSectionTitle = "Two-page spread"
    override val twoColumnSpreadSubtitle = "Automatic on tablets and Fold screens"
    override val twoColumnSpreadAuto = "Auto"
    override val twoColumnSpreadOff = "1 page"
    override val twoColumnSpreadAlways = "2 pages"

    override val autoHyphenationTitle = "Hyphenation"
    override val autoHyphenationSubtitle = "Syllable hyphenation for long words"

    override val hapticFeedbackTitle = "Haptic feedback"
    override val hapticFeedbackSubtitle = "Soft vibration following finger movement"

    override val dictionaryTitle = "Dictionary"
    override val dictionaryAction = "Dictionary"
    override val translateAction = "Translate"
    override val copyAction = "Copy"
    override val dictionaryLoading = "Looking up definition…"
    override val dictionaryNotFound = "No definition found"
    override val dictionarySourcePrefix = "Source:"
    override val translationOriginalTitle = "Original:"
    override val translationTargetTitle = "Translation:"
    override val translationFailed = "Failed to translate text"
    override val closeDialog = "Close"
    override val understandFootnote = "Got it"
    override fun minutesLeftInChapter(minutes: Int) = "$minutes min left in chapter"
    override val parallaxCoverHint = "Tilt device or drag finger for 3D effect"
    override val shareBookFile = "Share book file"
    override val changeCover = "Change cover"
    override val searchCoverTitle = "Choose Cover"
    override val searchCoverOnline = "Search online"
    override val pickFromGallery = "Pick from gallery"
    override val removeCover = "Reset cover"
    override val noCoversFoundOnline = "No covers found"
    override val coverUpdated = "Cover updated"
    override val applyCover = "Apply"


    override val highlightColorTitle = "Highlight color"
    override val quoteSavedAsHighlight = "Quote saved & highlighted in text"
    override val readingStatsSheetTitle = "Reading Statistics"
    override val readingStreakDays = "Reading Streak"
    override fun readingStreakFormat(days: Int) = "$days day${if (days == 1) "" else "s"} streak 🔥"
    override val readingStreakKeepGoing = "Read every day to keep your streak going!"
    override val readingTimeTodayCard = "Today"
    override val readingTimeTotalCard = "Total read time"
    override val readingTimeAvgCard = "Daily average"
    override val readingWeeklyActivityTitle = "Last 7 days activity"
    override fun totalHoursAndMinutes(hours: Int, minutes: Int) = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    override val dayMon = "Mon"
    override val dayTue = "Tue"
    override val dayWed = "Wed"
    override val dayThu = "Thu"
    override val dayFri = "Fri"
    override val daySat = "Sat"
    override val daySun = "Sun"
    override val ttsListenAction = "Read aloud (TTS)"
    override val ttsPlaying = "Playing"
    override val ttsPaused = "Paused"
    override val ttsSpeed = "Speed"
    override val ttsStop = "Stop"
    override val ttsNotificationTitle = "Audio Narration"
    override val ttsInitializing = "Initializing speech engine..."
    override val ttsError = "Text-to-speech engine unavailable"
    override val ttsLanguageNotSupported = "Language not supported by speech engine"
    override val widgetContinueReading = "Continue reading"
    override val widgetNoBook = "No books read yet"
    override val widgetOpenApp = "Open Aura Reader"

    override val readerSettingsTitle = "Reading Settings"
    override val themeModeTitle = "Theme Mode"
    override val fontSizeTitle = "Font Size"
    override val fontFamilyTitle = "Font Family"
    override val fontFamilySerif = "Serif"
    override val fontFamilySansSerif = "Sans-Serif"
    override val fontFamilyMonospace = "Monospace"
    override val fontFamilySystem = "System"
    override val lineHeightTitle = "Line Spacing"
    override val readingModeTitle = "Reading Mode"
    override val readingModePaged = "Paged"
    override val readingModeScroll = "Continuous Scroll"
    override val lightImageBgTitle = "Light Background for Images"
    override val lightImageBgSubtitle = "White underlay for better contrast in dark themes"

    override val ttsSettingsSheetTitle = "Audio Narration"
    override val ttsVoiceTitle = "Voice"
    override val ttsVoiceDefault = "System Default"
    override val ttsPitchTitle = "Pitch"
    override fun ttsParagraphProgress(current: Int, total: Int) = "Paragraph $current of $total"

    override val currentlyReadingBadge = "CURRENTLY READING"
    override val continueReadingAction = "Continue Reading"
    override val viewModeList = "List"
    override val viewModeGrid = "Grid"
    override val resetStatsTitle = "Reset Statistics"
    override val resetStatsConfirmTitle = "Reset reading statistics?"
    override val resetStatsConfirmMessage = "All read minutes, streaks, and activity charts will be reset. This action cannot be undone."
    override val vpnNoticeTitle = "Online Catalog & VPN"
    override val vpnNoticeMessage = "The built-in book catalog may be blocked in Russia and requires an active VPN connection."
    override val vpnNoticeUnderstood = "Got it"
    override val devModeTitle = "Developer Mode"
    override val devModePasswordPrompt = "Enter developer password"
    override val devModePasswordPlaceholder = "Password"
    override val devModeActivated = "Developer mode activated!"
    override val devModeWrongPassword = "Incorrect password"
    override val updateChannelTitle = "Update Channel"
    override val updateChannelSubtitle = "Choose release branch for app updates"
    override val updateChannelRelease = "Release"
    override val updateChannelBeta = "Beta"
    override val sortCustomOrder = "Custom Order"
    override val moveUp = "Move up"
    override val moveDown = "Move down"
    override val opdsDownloadMobi = "Download MOBI"
    override val opdsDownloadPdf = "Download PDF"

    // Beta 1.4.2 additions
    override val syncThemesTitle = "Sync themes"
    override val syncThemesSubtitle = "Use the same theme for app UI and reader"
    override val readerThemeSettingsTitle = "Book appearance"
    override val readerThemeSettingsSubtitle = "Customize themes, fonts, and animations for reading"
    override val specificFontTitle = "Font typeface"
    override val fontDefault = "Default"
    override val bookInfoTitle = "About Book"
    override val chapters = "Chapters"
    override val progress = "Progress"
    override val pages = "Pages"
    override val format = "Format"
    override val skipVersion = "Don't remind"
    override val channelSwitchPrompt = "Select update channel:"

    // Beta 1.4.3 additions
    override val appFontTitle = "App Font"
    override val appFontSubtitle = "Font used for UI elements"
    override val appFontDefault = "System Default"
    override val appFontGoogleSans = "Google Sans"
    override val resetReadingSpeedTitle = "Reset reading speed"
    override val resetReadingSpeedSubtitle = "Reset average speed to 200 wpm"
    override val resetReadingSpeedSuccess = "Reading speed reset (200 wpm)"
}

class UkStrings : Strings {
    override val chooseAddMethod = "Додати книги до бібліотеки"
    override val selectFiles = "Обрати файли"
    override val selectFilesSubtitle = "Файли .fb2, .epub, .pdf, .txt"
    override val scanFolder = "Сканувати папку"
    override val scanFolderSubtitle = "Рекурсивний пошук усіх книг у папці пристрою"
    override val selectThirdParty = "Сторонній провідник"
    override val selectThirdPartySubtitle = "Samsung «Мої файли», Xiaomi, провідники"
    override val materialYouToggle = "Кольори Material You"
    override val materialYouSubtitle = "Адаптувати акцентні кольори під шпалери пристрою"
    override val sortTitle = "Сортування"
    override val sortByDefault = "За замовчуванням"
    override val sortByRecent = "Нещодавні"
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
    override val newCollection = "Полиця"
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
    override val opdsCatalog = "Онлайн-каталог"
    override val opdsCatalogSubtitle = "Пошук та завантаження книг онлайн через OPDS"
    override val opdsSearchHint = "Пошук за автором або назвою..."
    override val opdsMirrorTitle = "Налаштування OPDS"
    override val opdsMirrorSubtitle = "Адреса OPDS-сервера. У разі блокувань можна вказати робоче дзеркало."
    override val opdsConnectionErrorTitle = "Не вдалося з'єднатися з сервером"
    override val opdsConnectionErrorSubtitle = "Можливо, з IP-адрес інших країн усе запрацює."
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
    override val quoteAction = "Цитата"
    override val quoteSavedNotification = "Цитату збережено у закладках"
    override val textCopiedNotification = "Текст скопійовано у буфер"
    override val addQuotePlaceholder = "Введіть текст цитати..."
    override val noQuotesYet = "Поки немає збережених цитат"
    override val noBookmarksYet = "Поки немає закладок"

    override val onlineCatalogSectionTitle = "Онлайн-каталог"
    override val customOpdsToggle = "Власний OPDS-каталог"
    override val customOpdsSubtitle = "Введення власної адреси та вимкнення добірок вбудованого каталогу"
    override val additionalFeaturesSectionTitle = "Додаткові функції"
    override val customOpdsUrl = "Адреса OPDS"
    override val customOpdsOpenRoot = "Відкрити каталог"

    override val pageAnimationSectionTitle = "Анімація гортання"
    override val pageAnimationSlide = "Зсув"
    override val pageAnimationInstant = "Миттєво"
    override val pageAnimationCurl = "Розгортання"

    override val twoColumnSpreadSectionTitle = "Книжковий розворот (дві сторінки)"
    override val twoColumnSpreadSubtitle = "Автоматично на планшетах і екранах Fold"
    override val twoColumnSpreadAuto = "Авто"
    override val twoColumnSpreadOff = "1 сторінка"
    override val twoColumnSpreadAlways = "2 сторінки"

    override val autoHyphenationTitle = "Перенесення слів"
    override val autoHyphenationSubtitle = "Книжкове перенесення довгих слів за складами"

    override val hapticFeedbackTitle = "Тактильний відгук"
    override val hapticFeedbackSubtitle = "М'яка вібрація за рухом пальця"

    override val dictionaryTitle = "Тлумачний словник"
    override val dictionaryAction = "Словник"
    override val translateAction = "Переклад"
    override val copyAction = "Копіювати"
    override val dictionaryLoading = "Пошук тлумачення…"
    override val dictionaryNotFound = "Тлумачення не знайдено"
    override val dictionarySourcePrefix = "Джерело:"
    override val translationOriginalTitle = "Оригінал:"
    override val translationTargetTitle = "Переклад:"
    override val translationFailed = "Не вдалося перекласти текст"
    override val closeDialog = "Закрити"
    override val understandFootnote = "Зрозуміло"
    override fun minutesLeftInChapter(minutes: Int) = "$minutes хв до кінця розділу"
    override val parallaxCoverHint = "Нахиляйте пристрій або проведіть пальцем для 3D-ефекту"
    override val shareBookFile = "Поділитися файлом"
    override val changeCover = "Змінити обкладинку"
    override val searchCoverTitle = "Вибір обкладинки"
    override val searchCoverOnline = "Шукати в мережі"
    override val pickFromGallery = "Обрати з галереї"
    override val removeCover = "Скинути обкладинку"
    override val noCoversFoundOnline = "Обкладинки не знайдено"
    override val coverUpdated = "Обкладинку оновлено"
    override val applyCover = "Застосувати"


    override val highlightColorTitle = "Колір маркера"
    override val quoteSavedAsHighlight = "Цитату збережено та виділено у тексті"
    override val readingStatsSheetTitle = "Статистика читання"
    override val readingStreakDays = "Ударний режим"
    override fun readingStreakFormat(days: Int) = "$days ${if (days % 10 == 1 && days % 100 != 11) "день" else if (days % 10 in 2..4 && days % 100 !in 12..14) "дні" else "днів"} поспіль 🔥"
    override val readingStreakKeepGoing = "Читайте щодня, щоб зберігати серію!"
    override val readingTimeTodayCard = "Сьогодні"
    override val readingTimeTotalCard = "Всього прочитано"
    override val readingTimeAvgCard = "У середньому на день"
    override val readingWeeklyActivityTitle = "Активність за 7 днів"
    override fun totalHoursAndMinutes(hours: Int, minutes: Int) = if (hours > 0) "$hours год $minutes хв" else "$minutes хв"
    override val dayMon = "Пн"
    override val dayTue = "Вт"
    override val dayWed = "Ср"
    override val dayThu = "Чт"
    override val dayFri = "Пт"
    override val daySat = "Сб"
    override val daySun = "Нд"
    override val ttsListenAction = "Озвучити (TTS)"
    override val ttsPlaying = "Відтворення"
    override val ttsPaused = "Пауза"
    override val ttsSpeed = "Швидкість"
    override val ttsStop = "Зупинити"
    override val ttsNotificationTitle = "Озвучення книги"
    override val ttsInitializing = "Ініціалізація синтезатора мовлення..."
    override val ttsError = "Синтезатор мовлення недоступний"
    override val ttsLanguageNotSupported = "Мова книги не підтримується синтезатором"
    override val widgetContinueReading = "Продовжити читання"
    override val widgetNoBook = "Книги не відкриті"
    override val widgetOpenApp = "Відкрити Aura Reader"

    override val readerSettingsTitle = "Налаштування читання"
    override val themeModeTitle = "Тема оформлення"
    override val fontSizeTitle = "Розмір шрифту"
    override val fontFamilyTitle = "Гарнітура шрифту"
    override val fontFamilySerif = "Із зарубками"
    override val fontFamilySansSerif = "Без зарубок"
    override val fontFamilyMonospace = "Моноширинний"
    override val fontFamilySystem = "Системний"
    override val lineHeightTitle = "Міжрядковий інтервал"
    override val readingModeTitle = "Режим читання"
    override val readingModePaged = "Посторінковий"
    override val readingModeScroll = "Сувій"
    override val lightImageBgTitle = "Світла підкладка для ілюстрацій"
    override val lightImageBgSubtitle = "Світлий фон для контрастності картинок у темних темах"

    override val ttsSettingsSheetTitle = "Озвучення книги"
    override val ttsVoiceTitle = "Голос"
    override val ttsVoiceDefault = "За замовчуванням"
    override val ttsPitchTitle = "Висота тону"
    override fun ttsParagraphProgress(current: Int, total: Int) = "Абзац $current з $total"

    override val currentlyReadingBadge = "ЗАРАЗ ЧИТАЮ"
    override val continueReadingAction = "Продовжити читання"
    override val viewModeList = "Список"
    override val viewModeGrid = "Сітка"
    override val resetStatsTitle = "Скинути статистику"
    override val resetStatsConfirmTitle = "Скинути статистику читання?"
    override val resetStatsConfirmMessage = "Усі прочитані хвилини, стрики та графіки активності будуть обнулені. Цю дію не можна скасувати."
    override val vpnNoticeTitle = "Онлайн-каталог та VPN"
    override val vpnNoticeMessage = "Вбудований каталог книг у РФ може бути заблокований провайдерами і стабільно працює лише з увімкненим VPN."
    override val vpnNoticeUnderstood = "Зрозуміло"
    override val devModeTitle = "Режим розробника"
    override val devModePasswordPrompt = "Введіть пароль розробника"
    override val devModePasswordPlaceholder = "Пароль"
    override val devModeActivated = "Режим розробника активовано!"
    override val devModeWrongPassword = "Невірний пароль"
    override val updateChannelTitle = "Канал оновлень"
    override val updateChannelSubtitle = "Вибір гілки для перевірки оновлень програми"
    override val updateChannelRelease = "Стабільний (Release)"
    override val updateChannelBeta = "Бета (Beta)"
    override val sortCustomOrder = "Власний порядок"
    override val moveUp = "Перемістити вище"
    override val moveDown = "Перемістити нижче"
    override val opdsDownloadMobi = "Завантажити MOBI"
    override val opdsDownloadPdf = "Завантажити PDF"

    // Beta 1.4.2 additions
    override val syncThemesTitle = "Синхронізувати теми"
    override val syncThemesSubtitle = "Однакова тема для інтерфейсу та читання"
    override val readerThemeSettingsTitle = "Оформлення книг"
    override val readerThemeSettingsSubtitle = "Налаштування тем, шрифтів та анімацій"
    override val specificFontTitle = "Гарнітура шрифту"
    override val fontDefault = "За замовчуванням"
    override val bookInfoTitle = "Про книгу"
    override val chapters = "Розділи"
    override val progress = "Прогрес"
    override val pages = "Сторінки"
    override val format = "Формат"
    override val skipVersion = "Не нагадувати"
    override val channelSwitchPrompt = "Оберіть канал оновлень:"

    // Beta 1.4.3 additions
    override val appFontTitle = "Шрифт програми"
    override val appFontSubtitle = "Шрифт елементів інтерфейсу"
    override val appFontDefault = "За замовчуванням"
    override val appFontGoogleSans = "Google Sans"
    override val resetReadingSpeedTitle = "Скинути швидкість читання"
    override val resetReadingSpeedSubtitle = "Повернути середню швидкість до 200 сл/хв"
    override val resetReadingSpeedSuccess = "Швидкість читання скинуто (200 сл/хв)"
}

class BeStrings : Strings {
    override val chooseAddMethod = "Дадаць кнігі ў бібліятэку"
    override val selectFiles = "Абраць файлы"
    override val selectFilesSubtitle = "Файлы .fb2, .epub, .pdf, .txt"
    override val scanFolder = "Сканаваць папку"
    override val scanFolderSubtitle = "Рэкурсіўны пошук усіх кніг у папцы прылады"
    override val selectThirdParty = "Сторонні праваднік"
    override val selectThirdPartySubtitle = "Samsung «Мае файлы», Xiaomi, праваднікі"
    override val materialYouToggle = "Колеры Material You"
    override val materialYouSubtitle = "Адаптаваць акцэнтныя колеры пад шпалеры прылады"
    override val sortTitle = "Сартаванне"
    override val sortByDefault = "Па змаўчанні"
    override val sortByRecent = "Нядаўнія"
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
    override val newCollection = "Паліца"
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
    override val opdsCatalog = "Анлайн-каталог"
    override val opdsCatalogSubtitle = "Пошук і спампоўванне кніг анлайн праз OPDS"
    override val opdsSearchHint = "Пошук па аўтару або назве..."
    override val opdsMirrorTitle = "Налады OPDS"
    override val opdsMirrorSubtitle = "Адрас OPDS-сервера. Пры блакіроўках можна ўказаць працоўнае люстэрка."
    override val opdsConnectionErrorTitle = "Не ўдалося звязацца з серверам"
    override val opdsConnectionErrorSubtitle = "Магчыма, з IP-адрасоў іншых краін усё запрацуе."
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
    override val quoteAction = "Цытата"
    override val quoteSavedNotification = "Цытата захавана ў закладках"
    override val textCopiedNotification = "Тэкст скапіяваны ў буфер"
    override val addQuotePlaceholder = "Увядзіце тэкст цытаты..."
    override val noQuotesYet = "Пакуль няма захаваных цытат"
    override val noBookmarksYet = "Пакуль няма закладак"

    override val onlineCatalogSectionTitle = "Анлайн-каталог"
    override val customOpdsToggle = "Уласны OPDS-каталог"
    override val customOpdsSubtitle = "Увод свайго адраса і адключэнне падборак убудаванага каталога"
    override val additionalFeaturesSectionTitle = "Дадатковыя функцыі"
    override val customOpdsUrl = "Адрас OPDS"
    override val customOpdsOpenRoot = "Адкрыць каталог"

    override val pageAnimationSectionTitle = "Анімацыя гартання"
    override val pageAnimationSlide = "Зрух"
    override val pageAnimationInstant = "Імгненна"
    override val pageAnimationCurl = "Разгортванне"

    override val twoColumnSpreadSectionTitle = "Кніжны разварот (дзве старонкі)"
    override val twoColumnSpreadSubtitle = "Аўтаматычна на планшэтах і экранах Fold"
    override val twoColumnSpreadAuto = "Аўта"
    override val twoColumnSpreadOff = "1 старонка"
    override val twoColumnSpreadAlways = "2 старонкі"

    override val autoHyphenationTitle = "Перанос слоў"
    override val autoHyphenationSubtitle = "Кніжны перанос доўгіх слоў па складах"

    override val hapticFeedbackTitle = "Тактыльны водгук"
    override val hapticFeedbackSubtitle = "Мяккая вібрацыя за рухам пальца"

    override val dictionaryTitle = "Тлумачальны слоўнік"
    override val dictionaryAction = "Слоўнік"
    override val translateAction = "Пераклад"
    override val copyAction = "Капіяваць"
    override val dictionaryLoading = "Пошук тлумачэння…"
    override val dictionaryNotFound = "Тлумачэнне не знойдзена"
    override val dictionarySourcePrefix = "Крыніца:"
    override val translationOriginalTitle = "Арыгінал:"
    override val translationTargetTitle = "Пераклад:"
    override val translationFailed = "Не ўдалося перакласці тэкст"
    override val closeDialog = "Закрыць"
    override val understandFootnote = "Зразумела"
    override fun minutesLeftInChapter(minutes: Int) = "$minutes хв да канца раздзела"
    override val parallaxCoverHint = "Нахіляйце прыладу або правядзіце пальцам для 3D-эфекту"
    override val shareBookFile = "Падзяліцца файлам"
    override val changeCover = "Змяніць вокладку"
    override val searchCoverTitle = "Выбар вокладкі"
    override val searchCoverOnline = "Шукаць у сетцы"
    override val pickFromGallery = "Выбраць з галерэі"
    override val removeCover = "Скінуць вокладку"
    override val noCoversFoundOnline = "Вокладкі не знойдзены"
    override val coverUpdated = "Вокладка абноўлена"
    override val applyCover = "Ужыць"


    override val highlightColorTitle = "Колер маркера"
    override val quoteSavedAsHighlight = "Цытата захавана і вылучана ў тэксце"
    override val readingStatsSheetTitle = "Статыстыка чытання"
    override val readingStreakDays = "Ударны рэжым"
    override fun readingStreakFormat(days: Int) = "$days ${if (days % 10 == 1 && days % 100 != 11) "дзень" else if (days % 10 in 2..4 && days % 100 !in 12..14) "дні" else "дзён"} запар 🔥"
    override val readingStreakKeepGoing = "Чытайце штодзень, каб захоўваць серыю!"
    override val readingTimeTodayCard = "Сёння"
    override val readingTimeTotalCard = "Усяго прачытана"
    override val readingTimeAvgCard = "У сярэднім за дзень"
    override val readingWeeklyActivityTitle = "Актыўнасць за 7 дзён"
    override fun totalHoursAndMinutes(hours: Int, minutes: Int) = if (hours > 0) "$hours гадз $minutes хв" else "$minutes хв"
    override val dayMon = "Пн"
    override val dayTue = "Аў"
    override val dayWed = "Ср"
    override val dayThu = "Чц"
    override val dayFri = "Пт"
    override val daySat = "Сб"
    override val daySun = "Нд"
    override val ttsListenAction = "Агучыць (TTS)"
    override val ttsPlaying = "Прайграванне"
    override val ttsPaused = "Паўза"
    override val ttsSpeed = "Хуткасць"
    override val ttsStop = "Спыніць"
    override val ttsNotificationTitle = "Агучванне кнігі"
    override val ttsInitializing = "Ініцыялізацыя сінтэзатара маўлення..."
    override val ttsError = "Сінтэзатар маўлення недаступны"
    override val ttsLanguageNotSupported = "Мова кнігі не падтрымліваецца сінтэзатарам"
    override val widgetContinueReading = "Працягнуць чытанне"
    override val widgetNoBook = "Кнігі не адкрыты"
    override val widgetOpenApp = "Адкрыць Aura Reader"

    override val readerSettingsTitle = "Налады чытання"
    override val themeModeTitle = "Тэма афармлення"
    override val fontSizeTitle = "Памер шрыфту"
    override val fontFamilyTitle = "Гарнітура шрыфту"
    override val fontFamilySerif = "З засечкамі"
    override val fontFamilySansSerif = "Без засечак"
    override val fontFamilyMonospace = "Монашырынны"
    override val fontFamilySystem = "Сістэмны"
    override val lineHeightTitle = "Міжрадковы інтэрвал"
    override val readingModeTitle = "Рэжым чытання"
    override val readingModePaged = "Пастаронкавы"
    override val readingModeScroll = "Скрутак"
    override val lightImageBgTitle = "Светлая падкладка пад ілюстрацыі"
    override val lightImageBgSubtitle = "Светлы фон для кантраснасці малюнкаў у цёмных тэмах"

    override val ttsSettingsSheetTitle = "Агучванне кнігі"
    override val ttsVoiceTitle = "Голас"
    override val ttsVoiceDefault = "Па змаўчанні"
    override val ttsPitchTitle = "Вышыня тону"
    override fun ttsParagraphProgress(current: Int, total: Int) = "Абзац $current з $total"

    override val currentlyReadingBadge = "ЗАРАЗ ЧЫТАЮ"
    override val continueReadingAction = "Працягнуць чытанне"
    override val viewModeList = "Спіс"
    override val viewModeGrid = "Сетка"
    override val resetStatsTitle = "Сбросить статистику"
    override val resetStatsConfirmTitle = "Сбросить статистику чтения?"
    override val resetStatsConfirmMessage = "Все прочитанные минуты, стрики и графики активности будут обнулены. Это действие нельзя отменить."
    override val vpnNoticeTitle = "Онлайн-каталог и VPN"
    override val vpnNoticeMessage = "Встроенный каталог книг в России может быть заблокирован провайдерами и стабильно работает только при включенном VPN."
    override val vpnNoticeUnderstood = "Понятно"
    override val devModeTitle = "Режим разработчика"
    override val devModePasswordPrompt = "Введите пароль разработчика"
    override val devModePasswordPlaceholder = "Пароль"
    override val devModeActivated = "Режим разработчика активирован!"
    override val devModeWrongPassword = "Неверный пароль"
    override val updateChannelTitle = "Канал обновлений"
    override val updateChannelSubtitle = "Выбор ветки для проверки обновлений приложения"
    override val updateChannelRelease = "Стабильный (Release)"
    override val updateChannelBeta = "Бета (Beta)"
    override val sortCustomOrder = "Свой порядок"
    override val moveUp = "Перамясціць вышэй"
    override val moveDown = "Перамясціць ніжэй"
    override val opdsDownloadMobi = "Спампаваць MOBI"
    override val opdsDownloadPdf = "Спампаваць PDF"

    // Beta 1.4.2 additions
    override val syncThemesTitle = "Сінхранізаваць тэмы"
    override val syncThemesSubtitle = "Аднолькавая тэма для інтэрфейсу і чытання"
    override val readerThemeSettingsTitle = "Афармленне кніг"
    override val readerThemeSettingsSubtitle = "Налада тэм, шрыфтоў і анімацый"
    override val specificFontTitle = "Гарнітура шрыфта"
    override val fontDefault = "Па змаўчанні"
    override val bookInfoTitle = "Пра кнігу"
    override val chapters = "Раздзелы"
    override val progress = "Прагрэс"
    override val pages = "Старонкі"
    override val format = "Фармат"
    override val skipVersion = "Не нагадваць"
    override val channelSwitchPrompt = "Выберыце канал абнаўленняў:"

    // Beta 1.4.3 additions
    override val appFontTitle = "Шрыфт праграмы"
    override val appFontSubtitle = "Шрыфт элементаў інтэрфейсу"
    override val appFontDefault = "Па змаўчанні"
    override val appFontGoogleSans = "Google Sans"
    override val resetReadingSpeedTitle = "Скінуць хуткасць чытання"
    override val resetReadingSpeedSubtitle = "Вярнуць сярэднюю хуткасць да 200 сл/хв"
    override val resetReadingSpeedSuccess = "Хуткасць чытання скінута (200 сл/хв)"
}

class PlStrings : Strings {
    override val chooseAddMethod = "Dodaj książki do biblioteki"
    override val selectFiles = "Wybierz pliki"
    override val selectFilesSubtitle = "Pliki .fb2, .epub, .pdf, .txt"
    override val scanFolder = "Skanuj folder"
    override val scanFolderSubtitle = "Rekurencyjne wyszukiwanie książek w folderze urządzenia"
    override val selectThirdParty = "Menedżer plików innej firmy"
    override val selectThirdPartySubtitle = "Samsung Moje pliki, Xiaomi, menedżery"
    override val materialYouToggle = "Kolory Material You"
    override val materialYouSubtitle = "Dostosuj kolory akcentów do tapety urządzenia"
    override val sortTitle = "Sortowanie"
    override val sortByDefault = "Domyślnie"
    override val sortByRecent = "Niedawne"
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
    override val newCollection = "Półka"
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
    override val opdsCatalog = "Katalog online"
    override val opdsCatalogSubtitle = "Wyszukiwanie i pobieranie książek online przez OPDS"
    override val opdsSearchHint = "Szukaj według autora lub tytułu..."
    override val opdsMirrorTitle = "Ustawienia OPDS"
    override val opdsMirrorSubtitle = "Adres serwera OPDS. W przypadku blokad można podać działające lustro."
    override val opdsConnectionErrorTitle = "Nie udało się połączyć z serwerem"
    override val opdsConnectionErrorSubtitle = "Być może połączenie zadziała z adresów IP innych krajów."
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
    override val quoteAction = "Cytat"
    override val quoteSavedNotification = "Cytat zapisany w zakładkach"
    override val textCopiedNotification = "Tekst skopiowany do schowka"
    override val addQuotePlaceholder = "Wpisz tekst cytatu..."
    override val noQuotesYet = "Brak zapisanych cytatów"
    override val noBookmarksYet = "Brak zakładek"

    override val onlineCatalogSectionTitle = "Katalog online"
    override val customOpdsToggle = "Własny katalog OPDS"
    override val customOpdsSubtitle = "Wprowadzanie własnego adresu i wyłączenie kolekcji wbudowanego katalogu"
    override val additionalFeaturesSectionTitle = "Dodatkowe funkcje"
    override val customOpdsUrl = "Adres OPDS"
    override val customOpdsOpenRoot = "Otwórz katalog"

    override val pageAnimationSectionTitle = "Animacja przewracania stron"
    override val pageAnimationSlide = "Przesunięcie"
    override val pageAnimationInstant = "Błyskawicznie"
    override val pageAnimationCurl = "Obrót"

    override val twoColumnSpreadSectionTitle = "Rozkładówka książki (dwie strony)"
    override val twoColumnSpreadSubtitle = "Automatycznie na tabletach i ekranach Fold"
    override val twoColumnSpreadAuto = "Auto"
    override val twoColumnSpreadOff = "1 strona"
    override val twoColumnSpreadAlways = "2 strony"

    override val autoHyphenationTitle = "Dzielenie wyrazów"
    override val autoHyphenationSubtitle = "Dzielenie długich słów na sylaby"

    override val hapticFeedbackTitle = "Wibracje haptyczne"
    override val hapticFeedbackSubtitle = "Miękkie wibracje podążające za ruchem palca"

    override val dictionaryTitle = "Słownik definicji"
    override val dictionaryAction = "Słownik"
    override val translateAction = "Tłumacz"
    override val copyAction = "Kopiuj"
    override val dictionaryLoading = "Szukanie definicji…"
    override val dictionaryNotFound = "Nie znaleziono definicji"
    override val dictionarySourcePrefix = "Źródło:"
    override val translationOriginalTitle = "Oryginał:"
    override val translationTargetTitle = "Tłumaczenie:"
    override val translationFailed = "Nie udało się przetłumaczyć tekstu"
    override val closeDialog = "Zamknij"
    override val understandFootnote = "Rozumiem"
    override fun minutesLeftInChapter(minutes: Int) = "$minutes min do końca rozdziału"
    override val parallaxCoverHint = "Przechyl urządzenie lub przeciągnij palcem, aby uzyskać efekt 3D"
    override val shareBookFile = "Udostępnij plik"
    override val changeCover = "Zmień okładkę"
    override val searchCoverTitle = "Wybór okładki"
    override val searchCoverOnline = "Szukaj w sieci"
    override val pickFromGallery = "Wybierz z galerii"
    override val removeCover = "Zresetuj okładkę"
    override val noCoversFoundOnline = "Nie znaleziono okładek"
    override val coverUpdated = "Okładka została zaktualizowana"
    override val applyCover = "Zastosuj"

    override val highlightColorTitle = "Kolor zakreślacza"
    override val quoteSavedAsHighlight = "Cytat zapisany i wyróżniony w tekście"
    override val readingStatsSheetTitle = "Statystyki czytania"
    override val readingStreakDays = "Seria czytania"
    override fun readingStreakFormat(days: Int) = "$days ${if (days == 1) "dzień" else "dni"} z rzędu 🔥"
    override val readingStreakKeepGoing = "Czytaj codziennie, aby utrzymać serię!"
    override val readingTimeTodayCard = "Dzisiaj"
    override val readingTimeTotalCard = "Łączny czas czytania"
    override val readingTimeAvgCard = "Średnio dziennie"
    override val readingWeeklyActivityTitle = "Aktywność z ostatnich 7 dni"
    override fun totalHoursAndMinutes(hours: Int, minutes: Int) = if (hours > 0) "${hours} godz. ${minutes} min" else "${minutes} min"
    override val dayMon = "Pn"
    override val dayTue = "Wt"
    override val dayWed = "Śr"
    override val dayThu = "Cz"
    override val dayFri = "Pt"
    override val daySat = "Sb"
    override val daySun = "Nd"
    override val ttsListenAction = "Czytaj na głos (TTS)"
    override val ttsPlaying = "Odtwarzanie"
    override val ttsPaused = "Pauza"
    override val ttsSpeed = "Prędkość"
    override val ttsStop = "Zatrzymaj"
    override val ttsNotificationTitle = "Lektor książki"
    override val ttsInitializing = "Inicjalizacja syntezatora mowy..."
    override val ttsError = "Syntezator mowy jest niedostępny"
    override val ttsLanguageNotSupported = "Język książki nie jest obsługiwany przez syntezator"
    override val widgetContinueReading = "Kontynuuj czytanie"
    override val widgetNoBook = "Brak otwartych książek"
    override val widgetOpenApp = "Otwórz Aura Reader"

    override val readerSettingsTitle = "Ustawienia czytania"
    override val themeModeTitle = "Motyw wyglądu"
    override val fontSizeTitle = "Rozmiar czcionki"
    override val fontFamilyTitle = "Krój czcionki"
    override val fontFamilySerif = "Szeryfowy"
    override val fontFamilySansSerif = "Bezszeryfowy"
    override val fontFamilyMonospace = "O stałej szerokości"
    override val fontFamilySystem = "Systemowy"
    override val lineHeightTitle = "Interlinia"
    override val readingModeTitle = "Tryb czytania"
    override val readingModePaged = "Stronicowy"
    override val readingModeScroll = "Ciągły"
    override val lightImageBgTitle = "Jasne tło dla ilustracji"
    override val lightImageBgSubtitle = "Jasny podkład dla kontrastu grafik w ciemnych motywach"

    override val ttsSettingsSheetTitle = "Lektor książki"
    override val ttsVoiceTitle = "Głos"
    override val ttsVoiceDefault = "Domyślny"
    override val ttsPitchTitle = "Wysokość tonu"
    override fun ttsParagraphProgress(current: Int, total: Int) = "Akapit $current z $total"

    override val currentlyReadingBadge = "TERAZ CZYTAM"
    override val continueReadingAction = "Kontynuuj czytanie"
    override val viewModeList = "Lista"
    override val viewModeGrid = "Siatka"
    override val resetStatsTitle = "Zresetuj statystyki"
    override val resetStatsConfirmTitle = "Zresetować statystyki czytania?"
    override val resetStatsConfirmMessage = "Wszystkie minuty czytania, serie i wykresy aktywności zostaną wyzerowane. Tej czynności nie można cofnąć."
    override val vpnNoticeTitle = "Katalog online i VPN"
    override val vpnNoticeMessage = "Wbudowany katalog książek w Rosji może być zablokowany i wymaga aktywnego połączenia VPN."
    override val vpnNoticeUnderstood = "Rozumiem"
    override val devModeTitle = "Tryb programisty"
    override val devModePasswordPrompt = "Wpisz hasło programisty"
    override val devModePasswordPlaceholder = "Hasło"
    override val devModeActivated = "Tryb programisty aktywowany!"
    override val devModeWrongPassword = "Nieprawidłowe hasło"
    override val updateChannelTitle = "Kanał aktualizacji"
    override val updateChannelSubtitle = "Wybór gałęzi sprawdzania aktualizacji aplikacji"
    override val updateChannelRelease = "Stabilny (Release)"
    override val updateChannelBeta = "Beta"
    override val sortCustomOrder = "Własna kolejność"
    override val moveUp = "Przenieś w górę"
    override val moveDown = "Przenieś w dół"
    override val opdsDownloadMobi = "Pobierz MOBI"
    override val opdsDownloadPdf = "Pobierz PDF"

    // Beta 1.4.2 additions
    override val syncThemesTitle = "Synchronizuj motywy"
    override val syncThemesSubtitle = "Używaj tego samego motywu w aplikacji i czytniku"
    override val readerThemeSettingsTitle = "Wygląd książki"
    override val readerThemeSettingsSubtitle = "Dostosuj motywy, czcionki i animacje"
    override val specificFontTitle = "Krój czcionki"
    override val fontDefault = "Domyślny"
    override val bookInfoTitle = "O książce"
    override val chapters = "Rozdziały"
    override val progress = "Postęp"
    override val pages = "Strony"
    override val format = "Format"
    override val skipVersion = "Nie przypominaj"
    override val channelSwitchPrompt = "Wybierz kanał aktualizacji:"

    // Beta 1.4.3 additions
    override val appFontTitle = "Czcionka aplikacji"
    override val appFontSubtitle = "Czcionka elementów interfejsu"
    override val appFontDefault = "Domyślna"
    override val appFontGoogleSans = "Google Sans"
    override val resetReadingSpeedTitle = "Zresetuj prędkość czytania"
    override val resetReadingSpeedSubtitle = "Przywróć średnią prędkość 200 słów/min"
    override val resetReadingSpeedSuccess = "Prędkość czytania została zresetowana (200 słów/min)"
}

class CsStrings : Strings {
    override val appName = "Aura Reader"
    override val cancel = "Zrušit"
    override val delete = "Smazat"
    override val save = "Uložit"
    override val copy = "Kopírovat"
    override val close = "Zavřít"
    override val done = "Hotovo"
    override val back = "Zpět"
    override val clear = "Vymazat"
    override val previous = "Předchozí"
    override val next = "Další"
    override val update = "Aktualizovat"
    override val later = "Později"
    override val libraryTitle = "Knihovna"
    override val addBooks = "Přidat knihy"
    override val emptyLibraryTitle = "Knihovna je prázdná"
    override val emptyLibrarySubtitle = "Přidejte knihy ze zařízení nebo otevřete ukázkovou knihu"
    override val openSampleBook = "Otevřít ukázkovou knihu"
    override val openFile = "Otevřít soubor"
    override val recentBooks = "Nedávné knihy"
    override val searchHint = "Hledat v knihovně..."
    override val noBooksFound = "Nebyly nalezeny žádné knihy"
    override val deleteBookTitle = "Smazat knihu?"
    override fun deleteBookMessage(title: String) = "Opravdu chcete smazat knihu «$title»?"
    override val todayReadingTime = "Dnešní čtení"
    override fun minutesRead(minutes: Int) = "$minutes min"
    override val readingStatsToggle = "Statistika čtení"
    override val readingStatsSubtitle = "Sledovat čas čtení a aktivitu"
    override val readingStatsEmpty = "Zatím žádná data o čtení"
    override val progressRead = "Přečteno"
    override fun searchResultsCount(count: Int) = "Nalezeno: $count"
    override fun noSearchResultsFound(query: String) = "Pro dotaz «$query» nebylo nic nalezeno"
    override val chooseAddMethod = "Přidat knihy do knihovny"
    override val selectFiles = "Vybrat soubory"
    override val selectFilesSubtitle = "Soubory .fb2, .epub, .pdf, .txt"
    override val scanFolder = "Skenovat složku"
    override val scanFolderSubtitle = "Rekurzivní vyhledávání knih ve složce zařízení"
    override val opdsCatalog = "Katalog OPDS"
    override val opdsCatalogSubtitle = "Stahování knih z online katalogů a knihoven"
    override val opdsSearchHint = "Hledat v katalogu OPDS..."
    override val opdsMirrorTitle = "Vybrat zrcadlo"
    override val opdsMirrorSubtitle = "Přepnout alternativní server katalogu"
    override val opdsConnectionErrorTitle = "Chyba připojení"
    override val opdsConnectionErrorSubtitle = "Nepodařilo se připojit ke katalogu. Zkontrolujte připojení k internetu."
    override val downloadFb2 = "Stáhnout FB2"
    override val downloadEpub = "Stáhnout EPUB"
    override val downloading = "Stahování..."
    override val openBookAction = "Otevřít knihu"
    override val annotation = "Anotace"
    override val reset = "Obnovit"
    override val retry = "Zkusit znovu"
    override val download = "Stáhnout"
    override val loading = "Načítání..."
    override val donate = "Podpořit projekt"
    override val moreOptions = "Další možnosti"
    override val fontAndTheme = "Písmo a motiv"
    override val bookmarksAndQuotes = "Záložky a citáty"
    override val selectThirdParty = "Správce souborů třetí strany"
    override val selectThirdPartySubtitle = "Samsung Moje soubory, Xiaomi a další správci"
    override val materialYouToggle = "Dynamické barvy (Material You)"
    override val materialYouSubtitle = "Přizpůsobit barvy aplikace systémové paletě Androidu"
    override val sortTitle = "Řazení"
    override val sortByDefault = "Výchozí"
    override val sortByRecent = "Podle data přidání"
    override val sortByPopularDesc = "Nejprve oblíbené"
    override val sortByPopularAsc = "Nejprve méně oblíbené"
    override val sortByTitleAsc = "Podle názvu (A-Z)"
    override val sortByTitleDesc = "Podle názvu (Z-A)"
    override val sortByAuthorAsc = "Podle autora (A-Z)"
    override val sortByAuthorDesc = "Podle autora (Z-A)"
    override val sortByYearDesc = "Nejprve novější"
    override val sortByYearAsc = "Nejprve starší"
    override val sortByPopularity = "Podle oblíbenosti"
    override val sortByTitle = "Podle názvu"
    override val sortByAuthor = "Podle autora"
    override val catNew = "Nové"
    override val catPopular = "Oblíbené"
    override val catAuthors = "Autoři"
    override val catGenres = "Žánry"
    override val catNewSubtitle = "Poslední přidané knihy"
    override val catPopularSubtitle = "Nejstahovanější knihy"
    override val catAuthorsSubtitle = "Procházet podle autorů"
    override val catGenresSubtitle = "Procházet podle žánrů"
    override val catalogHomeTitle = "Katalog"
    override val collectionsTitle = "Sbírky"
    override val colAll = "Všechny"
    override val colReading = "Čtu"
    override val colFavorites = "Oblíbené"
    override val colUnread = "Nepřečtené"
    override val colFinished = "Přečtené"
    override val newCollection = "Nová sbírka"
    override val createCollectionDialogTitle = "Vytvořit sbírku"
    override val collectionNamePlaceholder = "Název sbírky"
    override val addToCollection = "Přidat do sbírky"
    override val markAsFinished = "Označit jako přečtené"
    override val resetProgress = "Obnovit průběh"
    override fun deleteCollectionConfirm(name: String) = "Opravdu chcete smazat sbírku «$name»?"
    override val searchInLibrary = "Hledat v knihovně"
    override val searchLibraryPlaceholder = "Název knihy nebo autor..."
    override val backupSectionTitle = "Zálohování a synchronizace"
    override val createBackupTitle = "Vytvořit zálohu"
    override val createBackupSubtitle = "Uložit knihy, záložky, citáty a historii čtení"
    override val sendToGoogleDriveTitle = "Odeslat na Google Disk"
    override val sendToGoogleDriveSubtitle = "Uložit záložní soubor do cloudového úložiště"
    override val restoreBackupTitle = "Obnovit ze zálohy"
    override val restoreBackupSubtitle = "Obnovit data z dříve uloženého souboru"
    override fun backupCreatedSuccess(count: Int) = "Záloha úspěšně vytvořena ($count knih)"
    override fun restoreCompletedSuccess(count: Int) = "Obnovení dokončeno ($count knih)"
    override val backupError = "Chyba při práci se zálohou"
    override val shareBackupTitle = "Sdílet zálohu"
    override val catalogHomeSubtitle = "Online knihovny a katalogy OPDS"
    override fun downloadsCount(count: Int) = "$count stažení"
    override fun yearLabel(year: String) = "$year rok"
    override val settingsTitle = "Nastavení"
    override val themeSectionTitle = "Vzhled aplikace"
    override val themeSystem = "Systémový"
    override val themeLight = "Světlý"
    override val themeDark = "Tmavý"
    override val themeAmoled = "AMOLED"
    override val themeSepia = "Sépie"
    override val languageSectionTitle = "Jazyk aplikace"
    override val updatesSectionTitle = "O aplikaci a aktualizace"
    override val updateNotificationsToggle = "Upozornění na aktualizace"
    override val updateNotificationsSubtitle = "Upozornit při vydání nové verze"
    override val checkUpdatesNow = "Zkontrolovat aktualizace"
    override val checkingUpdates = "Kontrola aktualizací..."
    override val upToDate = "Máte nejnovější verzi"
    override fun currentVersion(version: String) = "Verze $version"
    override fun updateAvailableTitle(version: String) = "K dispozici je nová verze $version"
    override val updateBannerSubtitle = "Klepnutím zahájíte stahování a instalaci"
    override val downloadingUpdate = "Stahování aktualizace..."
    override val aboutSectionTitle = "O aplikaci"
    override val githubRepository = "GitHub repozitář"
    override val chapter = "Kapitola"
    override val ofChapters = "z"
    override val page = "Stránka"
    override val ofPages = "z"
    override val prevChapter = "Předchozí kapitola"
    override val nextChapter = "Další kapitola"
    override val prevPage = "Předchozí stránka"
    override val nextPage = "Další stránka"
    override val contents = "Obsah"
    override val bookmarks = "Záložky"
    override val quotes = "Citáty"
    override val searchInBook = "Hledat v knize"
    override val searchInBookHint = "Zadejte text k vyhledání..."
    override val noMatchesFound = "Žádné shody"
    override val footnoteTitle = "Poznámka pod čarou"
    override val pagingHint = "Klepnutím na okraje můžete otáčet stránky"
    override val loadingBook = "Načítání knihy..."
    override val closeSearch = "Zavřít hledání"
    override val chaptersNavigationHint = "Navigace po kapitolách"
    override val saveQuoteTitle = "Uložit citát"
    override val saveQuoteAction = "Uložit"
    override val quoteAction = "Citovat"
    override val quoteSavedNotification = "Citát byl uložen"
    override val textCopiedNotification = "Text zkopírován do schránky"
    override val addQuotePlaceholder = "Komentář k citátu (volitelné)..."
    override val noQuotesYet = "Zatím žádné citáty"
    override val noBookmarksYet = "Zatím žádné záložky"
    override val onlineCatalogSectionTitle = "Online katalog"
    override val additionalFeaturesSectionTitle = "Další funkce"
    override val customOpdsToggle = "Vlastní katalog OPDS"
    override val customOpdsSubtitle = "Použít vlastní adresu URL katalogu knih"
    override val customOpdsUrl = "URL katalogu OPDS"
    override val customOpdsOpenRoot = "Otevřít kořenový katalog"
    override val pageAnimationSectionTitle = "Animace otáčení stránek"
    override val pageAnimationSlide = "Posun"
    override val pageAnimationInstant = "Okamžitě"
    override val pageAnimationCurl = "Otočení"
    override val twoColumnSpreadSectionTitle = "Dvoustránkové zobrazení"
    override val twoColumnSpreadSubtitle = "Automaticky na tabletech a skládacích zařízeních"
    override val twoColumnSpreadAuto = "Automaticky"
    override val twoColumnSpreadOff = "Vypnuto"
    override val twoColumnSpreadAlways = "Vždy"
    override val autoHyphenationTitle = "Dělení slov"
    override val autoHyphenationSubtitle = "Automatické dělení slov na konci řádků"
    override val hapticFeedbackTitle = "Haptická odezva"
    override val hapticFeedbackSubtitle = "Vibrace při otáčení stránek a ovládání"
    override val dictionaryTitle = "Slovník"
    override val dictionaryAction = "Definice"
    override val translateAction = "Přeložit"
    override val copyAction = "Kopírovat"
    override val dictionaryLoading = "Hledání ve slovníku..."
    override val dictionaryNotFound = "Význam slova nebyl nalezen"
    override val dictionarySourcePrefix = "Zdroj: Wiktionary"
    override val translationOriginalTitle = "Původní text"
    override val translationTargetTitle = "Překlad"
    override val translationFailed = "Překlad se nezdařil"
    override val closeDialog = "Zavřít"
    override val understandFootnote = "Rozumím"
    override fun minutesLeftInChapter(minutes: Int) = "Zbývá cca $minutes min do konce kapitoly"
    override val parallaxCoverHint = "Paralaxní 3D efekt obálky"
    override val shareBookFile = "Sdílet soubor knihy"
    override val changeCover = "Změnit obálku"
    override val searchCoverTitle = "Vyhledat obálku"
    override val searchCoverOnline = "Hledat obálku online"
    override val pickFromGallery = "Vybrat z galerie"
    override val removeCover = "Odstranit obálku"
    override val noCoversFoundOnline = "Online obálky nebyly nalezeny"
    override val coverUpdated = "Obálka byla aktualizována"
    override val applyCover = "Použít obálku"
    override val highlightColorTitle = "Barva zvýraznění"
    override val quoteSavedAsHighlight = "Uloženo jako zvýraznění"
    override val readingStatsSheetTitle = "Statistika čtení"
    override val readingStreakDays = "Dny v řadě"
    override fun readingStreakFormat(days: Int) = "$days d."
    override val readingStreakKeepGoing = "Skvělé! Pokračujte ve čtení každý den!"
    override val readingTimeTodayCard = "Dnes"
    override val readingTimeTotalCard = "Celkem"
    override val readingTimeAvgCard = "V průměru"
    override val readingWeeklyActivityTitle = "Týdenní aktivita"
    override fun totalHoursAndMinutes(hours: Int, minutes: Int) = "${hours}h ${minutes}m"
    override val dayMon = "Po"
    override val dayTue = "Út"
    override val dayWed = "St"
    override val dayThu = "Čt"
    override val dayFri = "Pá"
    override val daySat = "So"
    override val daySun = "Ne"
    override val ttsListenAction = "Poslouchat"
    override val ttsPlaying = "Přehrávání..."
    override val ttsPaused = "Pozastaveno"
    override val ttsSpeed = "Rychlost"
    override val ttsStop = "Zastavit"
    override val ttsNotificationTitle = "Čtení nahlas"
    override val ttsInitializing = "Příprava syntézy řeči..."
    override val ttsError = "Chyba syntézy řeči"
    override val ttsLanguageNotSupported = "Jazyk textu není podporován převodníkem řeči"
    override val widgetContinueReading = "Pokračovat ve čtení"
    override val widgetNoBook = "Žádná kniha k pokračování"
    override val widgetOpenApp = "Otevřít aplikaci"
    override val readerSettingsTitle = "Nastavení čtení"
    override val themeModeTitle = "Barevný motiv"
    override val fontSizeTitle = "Velikost písma"
    override val fontFamilyTitle = "Skupina písem"
    override val fontFamilySerif = "S patkami"
    override val fontFamilySansSerif = "Bez patek"
    override val fontFamilyMonospace = "Monoprostorové"
    override val fontFamilySystem = "Výchozí"
    override val lineHeightTitle = "Řádkování"
    override val readingModeTitle = "Režim čtení"
    override val readingModePaged = "Stránkování"
    override val readingModeScroll = "Plynulé posouvání"
    override val lightImageBgTitle = "Světlé pozadí obrázků"
    override val lightImageBgSubtitle = "Zachovat bílé pozadí pro ilustrace v tmavém motivu"
    override val ttsSettingsSheetTitle = "Nastavení čtení nahlas"
    override val ttsVoiceTitle = "Hlas"
    override val ttsVoiceDefault = "Výchozí hlas systému"
    override val ttsPitchTitle = "Výška hlasu"
    override fun ttsParagraphProgress(current: Int, total: Int) = "Odstavec $current z $total"
    override val currentlyReadingBadge = "Právě čtu"
    override val continueReadingAction = "Číst"
    override val viewModeList = "Seznam"
    override val viewModeGrid = "Mřížka"
    override val resetStatsTitle = "Obnovit statistiku"
    override val resetStatsConfirmTitle = "Obnovit historii čtení?"
    override val resetStatsConfirmMessage = "Všechna data o čase a dnech čtení budou vymazána."
    override val vpnNoticeTitle = "Doporučení k síti"
    override val vpnNoticeMessage = "Některé online katalogy mohou vyžadovat VPN pro přístup."
    override val vpnNoticeUnderstood = "Rozumím"
    override val devModeTitle = "Režim vývojáře"
    override val devModePasswordPrompt = "Zadejte heslo vývojáře pro odemčení funkcí:"
    override val devModePasswordPlaceholder = "Heslo"
    override val devModeActivated = "Režim vývojáře aktivován"
    override val devModeWrongPassword = "Nesprávné heslo"
    override val updateChannelTitle = "Kanál aktualizací"
    override val updateChannelSubtitle = "Vyberte kanál pro příjem nových verzí"
    override val updateChannelRelease = "Stabilní"
    override val updateChannelBeta = "Beta"
    override val sortCustomOrder = "Vlastní pořadí"
    override val moveUp = "Posunout nahoru"
    override val moveDown = "Posunout dolů"
    override val opdsDownloadMobi = "Stáhnout MOBI"
    override val opdsDownloadPdf = "Stáhnout PDF"
    override val syncThemesTitle = "Synchronizovat motivy"
    override val syncThemesSubtitle = "Stejný motiv pro rozhraní aplikace i režim čtení"
    override val readerThemeSettingsTitle = "Vzhled knih"
    override val readerThemeSettingsSubtitle = "Nastavení motivů, písem a animací pro režim čtení"
    override val specificFontTitle = "Krój písma"
    override val fontDefault = "Výchozí"
    override val bookInfoTitle = "O knize"
    override val chapters = "Kapitoly"
    override val progress = "Průběh"
    override val pages = "Stránky"
    override val format = "Formát"
    override val skipVersion = "Nepřipomínat"
    override val channelSwitchPrompt = "Vyberte kanál aktualizací:"
    override val appFontTitle = "Písmo aplikace"
    override val appFontSubtitle = "Písmo prvků rozhraní"
    override val appFontDefault = "Výchozí"
    override val appFontGoogleSans = "Google Sans"
    override val resetReadingSpeedTitle = "Obnovit rychlost čtení"
    override val resetReadingSpeedSubtitle = "Vrátit průměrnou rychlost na 200 slov/min"
    override val resetReadingSpeedSuccess = "Rychlost čtení byla obnovena (200 slov/min)"
}

fun getStrings(language: AppLanguage): Strings = when (language) {
    AppLanguage.RU -> RuStrings()
    AppLanguage.EN -> EnStrings()
    AppLanguage.UK -> UkStrings()
    AppLanguage.BE -> BeStrings()
    AppLanguage.PL -> PlStrings()
    AppLanguage.CS -> CsStrings()
}

val LocalAppStrings = staticCompositionLocalOf<Strings> { RuStrings() }
