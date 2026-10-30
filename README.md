# Aura Reader

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![GitHub release](https://img.shields.io/github/v/release/Kouqqu/aura-reader?color=green)](https://github.com/Kouqqu/aura-reader/releases/latest)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)

---

## 🇬🇧 English

A modern, fast, and elegant Android e-reader built with Jetpack Compose, Material 3, and Liquid Glass design.

Crafted for immersive and comfortable reading without ads or trackers.

### 📚 Key Features:
- **Ultra-Compact & Fast**: Highly optimized build weighing only **~5 MB** with instant launch and silky-smooth animations.
- **Rich Format Support**: Reads **EPUB**, **FB2**, **FB2.ZIP**, plain **TXT**, **MOBI**, **AZW3**, **PDF**, and comic books (**CBZ**).
- **Dual Visual Styles**:
  - **Liquid Glass**: Apple iOS-inspired frosted glass dock, translucent blur materials, fluid navigation tablet physics, and an interactive jelly-glass add button with organic wobble mechanics.
  - **Material 3 Expressive**: Google's modern design with Material You dynamic color palette integration, Connected Button Groups, and Split Action Pairs.
- **OPDS Catalogs**: Browse and download books directly from online OPDS library catalogs with graceful slow-connection handling.
- **Reading Modes**: Realistic **3D Page Flip**, **Page Peel/Curl**, **Paginated slide**, and smooth **Continuous scroll**.
- **Interactive 3D Parallax Covers**: Tilt your device or drag with your finger to examine book covers in real-time 3D with realistic lighting and gyroscope physics.
- **Magazine Hero Card**: "Now Reading" card on the home screen with dynamic Aura Glow palette shadows and reading stats.
- **Home Screen Widget**: Convenient Android widget displaying your current book cover and reading progress.
- **Text-to-Speech (TTS)**: Background voice playback via system `MediaSessionCompat` with real-time spoken word highlighting, auto-scroll, paragraph seek, start-from-word, lock screen controls, and voice selection.
- **Dictionary & Translation**: Instant popup footnotes, smart offline dictionary looking up root lemmas, and multi-language translator right inside the reader.
- **Full-Text Search**: In-book keyword search with highlights and quick chapter jumping, plus fast library-wide search.
- **Quotes & Highlights**: Save text excerpts with colored highlighter options (yellow, green, blue, pink, purple) and multi-paragraph selection.
- **Reading Statistics & Celebrations**: Track daily streaks, reading speed (words/min), total hours, weekly interactive activity charts, and celebration confetti with a gold trophy upon completing a book.
- **Typography & Custom Fonts**: Import any custom TTF/OTF font for the application interface, support for Ukrainian typeface Zakon, custom font sizes, line height, serif/sans/mono reading fonts, plus Light, Dark, Sepia, and Pure Black (AMOLED) palettes.
- **Festive Seasons & Holidays**: Optional seasonal atmosphere for Halloween, New Year, Christmas, Valentine's Day, and Easter with bespoke palettes, greetings, and animations.
- **Backup & Restore**: Export and import your library, bookmarks, and reading progress locally.
- **Privacy First**: No ads. Includes optional anonymous reader telemetry (TelemetryDeck) counting active launches without collecting personal data, with a direct opt-out toggle in Settings. See [PRIVACY.md](PRIVACY.md).
- **Multilingual**: Fully localized into **7 languages**: 🇬🇧 English, 🇷🇺 Русский, 🇺🇦 Українська, 🇧🇾 Беларуская, 🇵🇱 Polski, 🇨🇿 Čeština, 🇮🇱 עברית.
- **Auto-Updates**: In-app one-tap update checks and seamless installation.

### 📥 How to Install:
Head over to the [Releases](https://github.com/Kouqqu/aura-reader/releases/latest) section, download **`AuraReader.apk`**, and install it on your Android device. Subsequent updates can be installed seamlessly from within the app.

### ⚖️ License:
This project is open-source under the terms of the [GNU General Public License v3.0](LICENSE).

---

## 🇷🇺 Русский

Современная, быстрая и плавная читалка для Android с дизайном Material 3 и Liquid Glass.

Создана для комфортного чтения без рекламы и назойливых элементов.

### 📚 Основные возможности:
- **Сверхкомпактность и скорость**: Оптимизированное приложение весом всего **~5 МБ** с мгновенным запуском и плавной работой.
- **Широкая поддержка форматов**: Читает **EPUB**, **FB2**, **FB2.ZIP**, обычный **TXT**, **MOBI**, **AZW3**, **PDF** и комиксы **CBZ**.
- **Два стиля оформления**:
  - **Liquid Glass**: Вдохновлённая эстетикой Apple iOS нижняя стеклянная панель (док), плавное размытие (frosted glass), световые блики, скользящая таблетка навигации и живая желейная кнопка добавления с воблинг-физикой.
  - **Material 3 Expressive**: Современный стиль Google с поддержкой адаптивных системных цветов Material You, соединёнными группами кнопок (Connected Button Groups) и раздельными парами действий (Split Buttons).
- **OPDS-каталоги**: Поиск, просмотр и скачивание книг напрямую из сетевых электронных библиотек с терпеливым ожиданием при медленном отклике сервера.
- **Режимы чтения**: Физический **3D-разворот страниц (Flip)**, **стягивание страницы (Curl)**, **постраничный сдвиг** и **непрерывная лента**.
- **3D Parallax обложки**: Интерактивный 3D-просмотр обложек с реакцией на датчик наклона (гироскоп), жесты пальцем и динамические блики.
- **Журнальная карточка «Сейчас читаю»**: Стильная карточка на главном экране с мягким свечением Aura Glow в цвет обложки.
- **Виджет рабочего стола**: Удобный виджет для экрана телефона с обложкой и процентом прогресса текущей книги.
- **Фоновая озвучка (TTS)**: Чтение текста вслух в фоне через `MediaSessionCompat` с живой подсветкой читаемого слова в реальном времени, автопрокруткой страниц, переходом к любому слову, управлением из шторки уведомлений и экрана блокировки.
- **Словарь, переводчик и сноски**: Всплывающие сноски без потери позиции в тексте, умный поиск словарных форм слов и встроенный переводчик с любого языка.
- **Полнотекстовый поиск**: Поиск слов и фраз внутри книги с подсветкой и навигацией, а также поиск по всей библиотеке.
- **Цитаты и маркеры**: Сохранение фрагментов текста с удобным выделением предложений и абзацев и выбором цвета хайлайта (жёлтый, зелёный, синий, розовый, фиолетовый).
- **Интерактивная статистика и поздравления**: Подсчёт времени чтения, страниц, темпа (слов/мин), стрик дней, анимированные графики активности, золотой кубок и праздничное конфетти при завершении книги.
- **Типографика и свои шрифты**: Подключение любого собственного шрифта (TTF/OTF) для интерфейса приложения, новый украинский шрифт Zakon, настройка кегля, межстрочного интервала, книжные отступы, темы: Светлая, Тёмная, Сепия и AMOLED.
- **Праздничная атмосфера**: Сезонное оформление к Хэллоуину, Новому году, Рождеству, Дню святого Валентина и Пасхе с уютными палитрами, поздравлениями и возможностью отключения в настройках.
- **Резервное копирование**: Экспорт и импорт библиотечной базы и закладок в один файл.
- **Приватность**: Без рекламы и трекеров. Включает опциональную анонимную статистику (TelemetryDeck) для подсчёта активных читателей без сбора персональных данных, которую можно в любой момент выключить тумблером в Настройках.
- **Мультиязычность**: Полная локализация на **7 языков**: 🇷🇺 Русский, 🇬🇧 English, 🇺🇦 Українська, 🇧🇾 Беларуская, 🇵🇱 Polski, 🇨🇿 Čeština, 🇮🇱 עברית.
- **Автообновления**: Проверка и быстрая установка свежих версий прямо из приложения.

### 📥 Как установить:
Зайдите в раздел [Releases](https://github.com/Kouqqu/aura-reader/releases/latest), скачайте файл **`AuraReader.apk`** и установите на телефон. Дальше приложение сможет обновляться прямо внутри.

### ⚖️ Лицензия:
Проект распространяется под свободной лицензией [GNU General Public License v3.0](LICENSE).
