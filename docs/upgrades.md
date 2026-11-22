# Build toolchain and dependency upgrades

This file records why the build stack looks the way it does, so that a future
session (human or assistant) does not have to rediscover it.

Last updated: 2026-11-23, release `2.3.0-beta.55` (`versionCode 220`).
`2.3.0-beta.55` changed no dependencies:
- Onboarding redesign: Redesigned the 4-slide introductory guide (`OnboardingScreen.kt`) to accurately showcase Aura Reader features:
  - Slide 1 («Добро пожаловать»): Design style picker (Liquid Glass vs Material 3) and 3-way theme selector (Light, Dark, System) with Dark theme active by default on fresh installs.
  - Slide 2 («Зажми слово»): Realistic book text selection mockup (`«Он улыбнулся в ответ»`) featuring actual reading toolbar actions: `[ Цитата ] [ Словарь ] [ Перевод ]`, strictly named «Словарь» without AI slop.
  - Slide 3 («И ещё много всего»): 2x2 grid of clean feature tiles: OPDS Catalogs, Read-Aloud TTS, Comics & PDF, and high-contrast E-Ink mode.
  - Slide 4 («Приятного чтения!»): Aura Reader squircle logo with prominent `[ Начать читать ]` primary button navigating directly to the library.
- Reading navigation hints: Enhanced the in-reader tap zones hint overlay (`ReaderScreen.kt`, `AppStrings.kt`) to explicitly inform users that swiping also turns pages alongside edge taps («(или свайп вправо)» / «(или свайп влево)»).

Last updated: 2026-11-22, release `2.3.0-beta.54` (`versionCode 219`).
`2.3.0-beta.54` changed no dependencies:
- Settings UI dynamic color correction for E-Ink: Fixed black-on-black text contrast in Interface, General, and About settings pages by properly scoping `LocalIsLiquidGlass`, `LocalIsThemeDark`, and `LocalIsAmoled` when E-Ink mode is active (`SettingsPages.kt`, `SettingsBottomSheet.kt`). Cards now render in clean, high-contrast Material 3 light surfaces (`surfaceContainer`) with crisp dark text.
- E-Ink Mode description polish: Simplified subtitle and active description to «Специально для электронных книг» across Russian and English localizations (`AppStrings.kt`, `SettingsThemeSection.kt`).

Last updated: 2026-11-22, release `2.3.0-beta.53` (`versionCode 218`).
`2.3.0-beta.53` changed no dependencies:
- Non-destructive E-Ink state persistence: Decoupled E-Ink toggle from underlying user preferences (`SettingsPages.kt`). Toggling E-Ink mode preserves previously saved `appDesignStyle` (e.g. Liquid Glass) and `themeMode` (e.g. Dark/AMOLED/Sepia) in DataStore, applying high-contrast Material monochrome dynamically without overwriting user choices, and seamlessly restoring original appearance when E-Ink is disabled (`MainActivity.kt`, `LibraryScreen.kt`).
- Pure Monochrome Reader Settings in E-Ink: Fixed `LocalEInkMode` propagation in `ReaderScreen.kt`. Strictly hide Theme Mode picker, Reading Mode switch, Page Turn Animation row, Two-Column Spread row, Haptic Feedback switch, and Light Image Background toggle in E-Ink mode. Enforced pure monochrome Black/White styling on sliders, font size labels, chips, and dropdown pickers with zero blue accent bleeds (`ReaderSettingsBottomSheet.kt`).
- Instant Library Screen Swipes: Configured `HorizontalPager` with `libraryInstantSwipe` gesture detector and `userScrollEnabled = !isReorderMode && !isEInk` in `LibraryScreen.kt`, replacing continuous animated screen dragging between Library and Search with instant page flips on E-Ink. Removed empty search cover marquee offset in `LibrarySearchScreen.kt`.
- Vocabulary Word Teleportation & Highlight: Tapping any saved word in the "Словарь" tab (`BookmarksAndQuotesBottomSheet.kt`) teleports the reader directly to the word's chapter and block in the book, closes the sheet, and highlights the word for 2.5 seconds using dynamic query matching before cleanly fading (`ReaderViewModel.kt`, `ReaderScreen.kt`).

Last updated: 2026-11-22, release `2.3.0-beta.52` (`versionCode 217`).
`2.3.0-beta.52` changed no dependencies (updated version matrix documentation to reflect Haze 2.1.0, Navigation Compose 2.10.2, Jsoup 1.24.1):
- Full E-Ink Mode refinement: Automatically sets pure white theme (`ReaderThemeMode.LIGHT`) across application and reader when E-Ink is enabled; hides book theme selector and vibration controls from reader settings; turns all UI accent tokens into high-contrast pure black and white monochrome with custom black text selection handles (`Theme.kt`, `SettingsPages.kt`, `ReaderScreen.kt`, `ReaderSettingsBottomSheet.kt`).
- Instant E-Ink Swipe: Re-engineered horizontal swipe gesture for instant page-turn mode (`PageTurnAnimation.INSTANT` and E-Ink), discarding partial-frame dragging to prevent ghosting, and executing instantaneous page flips upon reaching drag threshold (`ChapterPagingView.kt`). Completely suppressed finger-following haptics in E-Ink.
- Search static optimization for E-Ink: Disabled animated cover marquee/collage (`SearchCoverCollage`) in `LibrarySearchScreen.kt` when E-Ink mode is active, preventing redundant screen refresh cycles and extending battery life.
- EPUB parser resilience: Replaced strict `require` duplicate check with non-crashing warning logger and added case-insensitive ZIP entry resolution (`findEntry`), preventing crashes on EPUBs with duplicate or non-standard directory entries (`EpubParser.kt`).
- FB2 ZIP and charset detection: Added safe `.fb2.zip` entry lookup preventing unexpected EOF crashes, and implemented XML header prologue & byte sniffing with fallback to Windows-1251 for legacy Russian FB2 files without UTF-8 declarations (`Fb2Parser.kt`).
- Atomic move filesystem fallback: Guarded `Files.move` with `AtomicMoveNotSupportedException` catch to safely fall back to standard `REPLACE_EXISTING` across diverse Android mount points (`BookIngestionPipeline.kt`).

Last updated: 2026-11-22, release `2.3.0-beta.51` (`versionCode 216`).
`2.3.0-beta.51` changed no dependencies:
- E-Ink Mode deep integration: Automatically enforces Material 3 design style and hides design style options; hides theme cards, color presets, Material You, and seasonal atmospheric effects while active; locks reader to paged mode with instant page transitions and suppresses all spring/physics overscroll and bounce animations (`SettingsPages.kt`, `SettingsThemeSection.kt`, `ReaderSettingsBottomSheet.kt`, `ReaderScreen.kt`, `ExpressiveModifiers.kt`, `LibraryScreen.kt`).
- Crash log dialog cleanup: Removed duplicated "Сбоев не зафиксировано" subtitle from the dialog header, keeping it centered cleanly in the body (`CrashLogDialog.kt`). Guarded `CrashLog.install` against multiple invocations.
- Dictionary & Translation UI refinement: Removed redundant, overflowing bottom "Закрыть" button from `DictionaryDialog` and `TranslationDialog`, keeping clean header close button and action buttons (`ReaderDialogs.kt`).
- Accurate book context sentence extraction: Fixed context sentence capturing in `ReaderScreen.kt` so that saving a word extracts the actual sentence from the book text rather than falling back to the system clipboard.
- Vocabulary tab UX polish: Streamlined empty state in `BookmarksAndQuotesBottomSheet.kt` by removing redundant subtitle text; prevented duplicate saved word entries in `PreferencesManager.kt`.

Last updated: 2026-11-22, release `2.3.0-beta.50` (`versionCode 215`).
`2.3.0-beta.50` changed no dependencies:
- 120 Hz display refresh enforcement: Added `HighRefresh.apply(window, isEInk)` to lock the display refresh rate to the device's highest supported mode (`preferredDisplayModeId`), bypassing vendor-specific 60 Hz throttling for butter-smooth 120 FPS scrolling and animations (`HighRefresh.kt`, `MainActivity.kt`). Automatically disabled on E-Ink displays.
- In-app Crash Log: Installed global uncaught exception logger in `MainActivity` writing up to 10 rotating crash traces to `filesDir/crashes/` (`CrashLog.kt`). Added an accessible "Журнал сбоев" entry under "О приложении" in settings with `CrashLogDialog.kt`, providing clear stack trace inspection, one-tap system diagnostic report copying, and log clearance.
- E-Ink Mode: Added dedicated hardware detection (`EInkDetector.kt`) and toggle in Interface settings (`appSettings.eInkModeEnabled`). When active, enforces pure black-and-white high-contrast palette in `AuraReaderTheme`, disables Haze frosted glass blur, suppresses high refresh rate, and turns off decorative UI animations.
- Personal Vocabulary («Мой словарик»): Added "В словарик" quick-save button directly inside dictionary lookup (`DictionaryDialog`) and translation (`TranslationDialog`) popups with automatic context sentence capture (`SavedWord`, `BookRepository.kt`, `PreferencesManager.kt`). Added a dedicated 3rd tab in the bookmarks and quotes bottom sheet (`BookmarksAndQuotesBottomSheet.kt`) with word definitions, search, copy, and deletion actions. Fully serialized into `.aurabackup` v2 schema (`BackupManager.kt`, `BackupArchive.kt`).

Last updated: 2026-11-22, release `2.3.0-beta.49` (`versionCode 214`).
`2.3.0-beta.49` changed no dependencies:
- Enforce native `PdfRenderer` and bitmap resource teardown on reader pause/stop (`onDispose`), eliminating native memory leaks during extensive PDF reading sessions. Added automated cache cleanup routines for temporary rendered pages.

Last updated: 2026-11-22, release `2.3.0-beta.48` (`versionCode 213`).
`2.3.0-beta.48` changed no dependencies:
- Pre-reserve comic page dimensions with `GraphicPageDimensionsCache`, eliminating 0dp layout shifts during fast scrolling and lazy loading. Decoupled slider drag state from underlying pager transitions to ensure butter-smooth seek bar interaction.

Last updated: 2026-11-22, release `2.3.0-beta.47` (`versionCode 212`).
`2.3.0-beta.47` changed no dependencies:
- Instant slider navigation, 100% ribbon completion, stable slider dragging without jumps. Fixed bottom bar slider responsiveness during live dragging in both continuous scroll ribbon mode and paged mode. Ensured 100% completion correctly maps to the last graphic page.

Last updated: 2026-11-22, release `2.3.0-beta.46` (`versionCode 211`).
`2.3.0-beta.46` changed no dependencies:
- Optimize PDF and comic rendering, eliminate spontaneous rewind and exit progress reset. Fixed page rendering lifecycle in `ChapterContentView` and `ChapterPagingView` to prevent unintended scroll jumping and position loss when exiting or reloading large graphic documents.

Last updated: 2026-11-22, release `2.3.0-beta.45` (`versionCode 210`).
`2.3.0-beta.45` changed no dependencies:
- Fixed PDF & comic architecture across both continuous scroll and paging modes: Reunified PDF, CBZ, and CBT page modeling to maintain clean page-level chapters (`"Страница N"`), fixing the table of contents (which previously showed raw file hashes) and the bottom bar slider (which was previously locked at 0 / disabled).
- True continuous vertical scroll tape for graphic books (свиток): Added dedicated `LazyColumn` ribbon view for PDF, CBZ, and CBT graphic books in continuous scroll mode, allowing smooth, uninterrupted vertical scrolling through all pages without stops or "Next chapter" buttons.
- Direct horizontal page flips in paging mode: Added dedicated seamless `HorizontalPager` for PDF, CBZ, and CBT in paging mode with zero intermediate chapter transition spreads, fluid 1:1 finger tracking, edge taps, and double-tap / pinch zoom.
- Bounded PDF disk page cache & auto-recovery: Expanded `PdfPageCache` retention from 11 to 200 pages so previously read pages never vanish from disk when scrolling back, and added automatic re-render recovery on Coil image errors with a subtle loading spinner.
- Eliminated premature book completion & progress jumping: Fixed `ChapterContentView` and `ChapterPagingView` premature `onBookFinished()` triggers during initial layout passes and last-page viewing, restoring smooth, accurate reading progress percentages (0% to 100%).
- Reliable reading position persistence: Removed conflicting single-chapter offset hacks in `BookRepository` and `ReaderViewModel`, ensuring books always reopen at the exact saved page.

Last updated: 2026-11-22, release `2.3.0-beta.44` (`versionCode 209`).
`2.3.0-beta.44` changed no dependencies:
- Clean Reader Settings sheet: Removed Material You theme toggle from book reader settings, keeping book styling strictly dedicated to typography and reading preferences (`ReaderSettingsBottomSheet.kt`).
- Online catalog file size formatting: Formatted OPDS file sizes in megabytes (MB) across the catalog instead of kilobytes (`OpdsService.kt`).
- Per-book and per-format reading mode defaults & overrides: PDF and comic formats (CBZ, CBR, CBT) now default to continuous scroll mode (свиток), while regular books default to user preference; changing reading mode persists per-book in DataStore and restores across reading sessions (`PreferencesManager.kt`, `ReaderViewModel.kt`, `BackupManager.kt`).
- Unified PDF single-chapter architecture & seamless transitions: Restructured `PdfParser.kt` to assemble all PDF pages as blocks within a single chapter instead of generating separate dummy chapters per page, completely eliminating flashing `ChapterTransitionSpread` screens between pages in paging mode and enabling true uninterrupted continuous vertical scrolling in scroll mode (`PdfParser.kt`, `BookRepository.kt`, `ChapterPagingView.kt`).
- Selective typography for header greeting vs holiday congratulations: Restored large prominent `24.sp` typography for regular daily greetings ("Доброе утро", "Добрый день"), while retaining neat `14.sp` subtitle typography exclusively for longer holiday congratulations to prevent truncation (`LibraryTopBar.kt`).
`2.3.0-beta.43` changed no dependencies:
- Clean comic & manga page swipe gesture pipeline: Replaced unconditional gesture consumption on comic/manga images with unconsumed single-finger gesture passthrough when unzoomed (`zoomScale <= 1.05f`), allowing horizontal swipe across the entire image to turn pages naturally via `HorizontalPager` and instant swipe handlers without interference from zooming; added two-finger pinch zoom, bounded 1-finger and 2-finger panning while zoomed in, and left/right 72dp edge tap page navigation on comic images (`ReaderBlocks.kt`, `ChapterPagingView.kt`, `ChapterContentView.kt`).
- Removed confusing Manga RTL mode from settings: Removed the Japanese Right-to-Left manga toggle from the reader settings bottom sheet and restored standard LTR page flipping across the reader, while retaining backward compatibility in the settings/backup schema (`ReaderSettingsBottomSheet.kt`, `ChapterPagingView.kt`).
- Proportionate holiday greetings typography: Scaled holiday greeting font size down from large headline (`24.sp`) to clean secondary text (`14.sp`, `bodyMedium` with `FontWeight.Medium`), preventing long holiday strings ("Со светлым Рождеством Христовым!", "Счастливого Нового года!") from being truncated with ellipsis on mobile screens (`LibraryTopBar.kt`).
`2.3.0-beta.42` changed no dependencies:
- Frosted glass blur in scroll mode: Fixed interface blur parity with paging mode by rendering a solid `MaterialTheme.colorScheme.background` within the `hazeSource` capture hierarchy in `ReaderScreen.kt` and `ChapterContentView.kt`, and eliminating conflicting child `hazeSource` registrations on `LazyColumn` so `readerHazeState` captures an opaque, solid backdrop with crisp text at 120 FPS across both paging and continuous scroll modes.
- Search screen edge-to-edge layout: Removed outer `statusBarsPadding()` from the search container and moved it to the inner content column, enabling `SearchCoverCollage` to extend edge-to-edge behind the translucent status bar and positioning the "Поиск" title and search bar immediately beneath the status bar, harmonizing with the rest of the application (`LibrarySearchScreen.kt`).
- Comprehensive backup & restore audit: Upgraded `.aurabackup` archive schema to version 2 (`BackupManager.kt`, `BackupArchive.kt`, `PreferencesManager.kt`). Backs up and restores user custom fonts (`custom_fonts/*.ttf, *.otf`), complete reading statistics history (`reading_stats_json` with idempotent date-keyed seconds merge), and all reader and library settings (`mangaModeRtl`, `fontName`, `twoColumnMode`, `pageAnimation`, `autoHyphenation`, `lightImageBackground`, `hapticFeedbackEnabled`, `syncThemesWithApp`, `appThemeMode`, `appDesignStyle`, `glassClarity`, `libraryViewMode`, `librarySortOrder`, `customBookOrder`, `autoFetchMetadata`, `userAverageWpm`, `seasonalEffectsEnabled`), while maintaining seamless backward compatibility with version 1 backups.
- Comic & Manga reader experience:
  - Added Manga Mode with Right-to-Left (RTL) reading direction toggle in reader settings (`mangaModeRtl`, `CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl)`), adapting both horizontal page swipes and edge tap detection.
  - Interactive comic image zoom: Added double-tap to zoom (2.5x centered on tapped panel) and smooth pinch-to-zoom (1.0x to 4.0x) with bounded pan navigation in `ReaderBlocks.kt`.
  - Full-bleed comic presentation: Eliminated horizontal margins on single-image pages in `ChapterPagingView.kt` and across image-only chapters in `ChapterContentView.kt` (continuous webtoon/manhwa mode).
- Extended format support (`clach04/sample_reading_media` audit):
  - Added native `MarkdownParser` supporting `.md` and `.markdown` with YAML frontmatter metadata extraction, headings, epigraphs, dividers, and lists (`BookFormat.MD`).
  - Added native `HtmlParser` supporting `.html` and `.htm` web documents using JSoup, extracting document metadata, headings, blockquotes, paragraphs, and embedded base64 illustrations (`BookFormat.HTML`).
  - Added native `CbtParser` supporting uncompressed TAR comic archives (`.cbt`).
  - Added `.fbz` alias for FictionBook compressed archives and enabled `.html` / `.md` discovery within standard ZIP archives.
  - Localized format labels and manga settings across all 7 supported languages in `AppStrings.kt`.
`2.3.0-beta.41` changed no dependencies:
- Authentic human ambient recordings: Replaced procedural/synthetic sounds for rain and fire with real, high-quality human field recordings (hearth fireplace with crackling embers and natural rain, Freesound CC0 / public domain via Muges/ambientsounds), crossfade-looped seamlessly and volume-matched with forest and ocean (`ambient_fire.ogg`, `ambient_rain.ogg`).
- README badge layout: Re-positioned Telegram and Donation links/badges directly beneath the top project badges (License, Release, Android platform, Kotlin) in `README.md`.
- Top bar aura brightness dimming & search header fix: Restored `LibraryTopBar` to `Scaffold(topBar = { ... })` and removed double top padding on Page 1 (search screen), eliminating the black band at the top; modulated aura brightness directly in `drawBehind` (`auraAlpha = (1f - libraryPageOffset)`) without container layer clipping, allowing full-radius 150dp aura to dim smoothly during pager swipe while title and greeting animate with parallax and fade (`LibraryTopBar.kt`, `LibraryScreen.kt`).
- Reader word hyphenation strictness: Upgraded paragraph line breaking to `LineBreak(strategy = HighQuality, strictness = Loose)` so that auto-hyphenation triggers more naturally across Russian and multilingual texts without creating ragged gaps at line ends (`ReaderBlocks.kt`, `ReaderPagination.kt`).
- Previous chapter page navigation anchor: Fixed page turn backward from chapter start to reliably land on the very last page/spread of the previous chapter by passing `Int.MAX_VALUE` and resolving `ReadingPosition.pageForAnchor` to `pages.lastIndex` (`ReaderViewModel.kt`, `ReadingPosition.kt`, `ChapterPagingView.kt`).
- Frosted glass blur in continuous scroll mode: Fixed interface blur in scroll mode by passing `HazeState` directly to `LazyColumn` (`hazeSource`) and removing `navigationBarsPadding` from the content container so the blur snapshot continuously updates at 120 FPS as the reader scrolls behind the top bar, bottom bar, and menus (`ChapterContentView.kt`, `ReaderScreen.kt`).
`2.3.0-beta.40` changed no dependencies:
- Grid view interaction: Clicking a book cover in the grid layout now immediately opens the book for reading, while opening the book profile bottom sheet is moved to the 3-dots action menu on that cover (`BookCards.kt`).
- Batch multi-select shelves: Multi-select collection picker now includes built-in shelves ("Читаю", "Прочитано", "Непрочитанные", "В избранное") alongside custom collections, enabling immediate library organization after batch import (`LibraryScreen.kt`, `LibraryViewModel.kt`, `BookRepository.kt`).
- Bionic reading font: Added "Бионический" font mode across all reader font families bolding fixation letters (`applyBionicStyles`, `ReaderBlocks.kt`, `ReaderPagination.kt`, `ReaderSettingsBottomSheet.kt`).
- Scroll mode auto-scroll: Added continuous, smooth auto-scroll on `withFrameNanos` active exclusively when scroll mode is enabled (`!settings.pagingMode`), featuring a floating speed adjustment controller and 3-dots menu trigger (`ChapterContentView.kt`, `ReaderScreen.kt`).
- Comprehensive localization coverage: Added complete, idiomatic translations for all 128 application strings across all 7 supported languages (RU, EN, UK, BE, PL, CS, HE) in `AppStrings.kt`.
- Third-party open source licenses: Added missing library and asset attributions in `LicensesDialog.kt` (Coil, OkHttp, TelemetryDeck, AndroidX Palette, Procedural Ambient Audio).
- Restored original aura glow & synchronized pager transition: Reverted top bar aura radius to 150dp and moved `LibraryTopBar` into Page 0 inside `HorizontalPager`, preventing aura splitting, horizontal edge clipping, and premature fade-out (`LibraryTopBar.kt`, `LibraryScreen.kt`).
- Organic ambient sound synthesis: Re-synthesized rain and fireplace audio loops (`ambient_rain.ogg`, `ambient_fire.ogg`) with warm, soothing, natural textures and seamless looping.
- Status bar icon contrast in Light/Sepia: Enforced dark system status bar and navigation bar icons (`isAppearanceLightStatusBars = !isDark`) in Light and Sepia themes via `SideEffect` in `AuraReaderTheme` (`Theme.kt`).
`2.3.0-beta.39` changed no dependencies: it integrates seamless ambient looping sounds into the reader screen via `AmbientSoundPlayer` (Rain, Fireplace, Forest, Ocean) and `AmbientSoundBottomSheet` accessible directly from the 3-dots menu; fixes the broken frosted glass blur in the reader 3-dots menu (`LiquidGlassMenuContainer`) by removing the opaque matte bluish fill (`0xE818181E`) in favor of translucent obsidian (`Color(0x65141416)`) and neutral dark surface tokens; eliminates the top bar logo aura cutoff line at the stats banner by calibrating the halo radius to 70.dp with radial alpha vanishing within the top bar bounds and harmonizing pager translation/alpha; and preserves the layered predictive back transition.
`2.3.0-beta.38` changed no dependencies: it completely cuts out the logo halo glow aura (`drawBehind`) in White (Light) and Sepia themes (`if (isThemeDark && scrollAlpha > 0.005f)`), keeping the living luminous mesh active strictly in Dark and AMOLED themes; overrides Navigation Compose's default predictive back exit scale (`DefaultNavTransitions.getPredictivePopExitTransition()`, which forced `scaleOut(0.7f)`) by supplying `predictivePopExitTransition` and `predictivePopEnterTransition` directly to `NavHost`, ensuring the screen slides purely horizontally to the side (`slideOutOfContainer(SlideDirection.End)`) without shrinking towards the center while the underlying screen slides in with gentle parallax and fade; and harmonizes the Library-to-Search tab transition by replacing the discrete `AnimatedVisibility` with a continuous `derivedStateOf` page offset that slides (-30% parallax) and fades the top bar as a single pre-composited `graphicsLayer`, preventing the aura and header text from splitting into two disjointed phases or jumping.
`2.3.0-beta.37` changed no dependencies: it restores the search flying books collage (`SearchCoverCollage.kt`, `CoverCollageLayout.kt`) to its exact original design; fixes predictive back across Reader and Catalog screens by eliminating custom interceptors that occluded background composables, delegating route transitions natively to `NavHost` with gentle parallax pop-enter curves so the actual library screen is visible behind the exiting view; and purges muddy/dirty dark pigments from the light and sepia top bar logo aura, returning to luminous clean signature pastels (warm amber `#FFB74D`, soft rose `#F06292`, lavender `#7E57C2`, celestial teal `#26A69A`) with a delicate 0.20 alpha and soft radial diffusion.
`2.3.0-beta.36` changed no dependencies: it refactors search screen genre filters into a compact row of primary macro-genres paired with a "Все жанры" pill launching `SearchGenresBottomSheet` with real-time genre search and book counts; configures `BookProfileBottomSheet` to open at half-screen (`skipPartiallyExpanded = false`); eliminates purple screen artifacts during predictive back gestures by updating `splash_background` to neutral backgrounds (`#121212` in dark, `#FAF0E6` in light); and slows down settings accordion fold animations to a smooth 350ms easing with synchronized chevrons.
`2.3.0-beta.35` changed no dependencies: it lightens collapsible settings fold headers into sleek, thin capsules with micro-borders and bare rotating chevrons, removing bulky circle badges; eliminates manual batch download buttons from settings in favor of a clean background auto-fetch toggle.
`2.3.0-beta.34` changed no dependencies: it resolves hardcoded cold bluish tints (`#1F2129`, `#1D212E`, `#283448`) in Liquid Glass settings surfaces, strictly binding them to theme tokens (`#121212` background, `#1E1E1E` cards); breaks monolithic settings groups into focused island cards (Catalog/OPDS and Reading/Maintenance).
`2.3.0-beta.33` changed no dependencies: it calibrates the dark theme palette to match the neutral obsidian aesthetic (`#121212` background, `#1E1E1E` card surfaces) eliminating cold blueish/purple tints; declutters settings screens by streamlining the reading speed reset into a standard row, wrapping holiday atmosphere controls in `AnimatedVisibility(seasonalEffectsEnabled)` to collapse when disabled, and animating custom OPDS inputs.
`2.3.0-beta.32` changed no dependencies: it removes the `REQUEST_INSTALL_PACKAGES` permission and internal APK downloader/installer routines to eliminate false positive Google Play Protect "malicious code" (dropper) warnings, delegating app updates directly and cleanly to the system browser; names release APK assets with the version tag (`AuraReader-v${APP_VERSION}.apk`); simplifies update dialogs and banners; and incorporates cache cleanup management.
`2.3.0-beta.31` changed no dependencies: it centers and lowers the GTA VI launcher icon Å letter by 14px for balanced composition with the palm trees and sunset gradient; introduces `AppStorageManager` with a manual "Очистить кэш приложения" button in Settings and automatic background cleanup of legacy temp files and APKs.
`2.3.0-beta.18` changed no dependencies: it resolves the root cause of the missing blur across the application by migrating Haze calls to modern `hazeEffect` with `HazeMaterials.regular(containerColor = ...)` and removing the opaque `0xEE` (93%) background color fill that was painted over the blur; adds an ethereal luminous purple ambient aura tint (`primary.copy(alpha = 0.16f)`) to the floating reader top and bottom bars in Liquid Glass mode with rich 24dp frosted glass diffusion; and harmonizes the visual language and contrast of the Daily Reading Stats banner (`LibraryReadingStatsBanner`) with the collection filter chips row (`LibraryCollectionChips`) using unified translucent glass material (`Color.White.copy(0.06f)` in dark, `Color.Black.copy(0.04f)` in light), matched 0.6dp borders, and a balanced amber streak badge.
`2.3.0-beta.17` changed no dependencies: it completely resolves the Book Profile bottom sheet (`BookProfileBottomSheet`) see-through transparency and text bleed-through artifacts by enforcing 100% opaque obsidian backgrounds (`#141416` in dark, `#F7F7F9` in light) and solid elevated cards (`#1C1C1F`); wires real-time hardware-accelerated frosted glass blur via `HazeState` (`dev.chrisbanes.haze`) to `ReaderScreen`, providing dynamic 120 FPS frosted glass blur behind `ReaderTopBar` and `ReaderBottomBar`; purges all cold blueish casts (`#181A20`, `#1F2129`, `#14151C`) from reader bars, reader settings bottom sheet, and TTS bar in favor of pure neutral obsidian graphite (`#141416`, `#1C1C1F`, `#242429`); elevates the reading streak («стрик») into a celebratory fiery achievement badge with vivid amber borders (`#FF9500`) and translucent glowing fill in both `LibraryBanners` and `ReadingStatsBottomSheet`; and refines inline action chips with crisp specular glass borders in Liquid Glass mode.
`2.3.0-beta.16` changed no dependencies: it delivers the Ambient Editorial visual refinement specifically for Liquid Glass mode (`AppDesignStyle.LIQUID_GLASS`) while strictly preserving Material Design unchanged; declutters the book card header by relocating the format badge from the crowded top-right row down directly above the progress bar (`FB2` format capsule on the left, reading percentage on the right, and the rounded `LinearProgressIndicator` line underneath with translucent track); unifies the Hero «Сейчас читаю» card accents with the primary theme/Aura glow eliminating clashy peach/orange overrides on neutral/monochrome book covers; and softens the Book Profile bottom sheet into an airy, editorial layout with translucent frosted surfaces (`0x1AFFFFFF`) and ultra-thin specular borders (`0.08f` white alpha) eliminating the boxy «cards-in-cards» effect.
`2.3.0-beta.15` changed no dependencies: it executes the deep module architectural refactoring across the codebase by implementing four foundational abstractions: (1) consolidated `AppSettings` domain model (`com.aura.reader.data.model.AppSettings`) eliminating the 30+ parameter prop-drilling pipe across `PreferencesManager` -> `LibraryViewModel` -> `LibraryScreen` -> `SettingsGeneralPage` / `SettingsInterfacePage` / `SettingsBottomSheet` -> `SettingsSections`; (2) extracted deep `BookIngestionPipeline` (`com.aura.reader.data.parser.BookIngestionPipeline`) absorbing 230+ lines of MIME magic-byte sniffing (EPUB, FB2, TXT, CBZ, MOBI, PDF), container archive examination, parser dispatching, image caching, and atomic temp-file staging from `BookRepository`; (3) unified online metadata and cover retrieval facade behind `BookEnricher` (`com.aura.reader.data.service.BookEnricher`) with robust publisher and extension sanitization, Base64 conversion, and background auto-enrichment; and (4) consolidated single and multi-select book actions into `BookActionDispatcher` (`BookAction`, `BookTarget`) in `LibraryViewModel`.
`2.3.0-beta.14` changed no dependencies: it streamlines the inline book action tray in both list and grid views by removing redundant buttons («Выбрать книги», «О книге», «Сменить обложку»), compacting button spacing to 5.dp with 34.dp minimum touch heights, and converting share and delete actions to clean icon-only chips; migrates cover replacement into the Book Profile bottom sheet (`BookProfileBottomSheet`) via direct thumbnail badge actions and edit mode buttons; eliminates in-place edit buttons and empty bracket placeholders `[]` from the view mode of genres and synopsis sections, renaming the synopsis section to pure «Описание»; and introduces multi-source online metadata fetching (LitRes API, FantLab API, and OpenLibrary API) directly in the Book Profile edit mode and automatic background metadata retrieval for imported books backed by an «Автозагрузка данных книги» toggle in General Settings.
`2.3.0-beta.13` changed no dependencies: it implements Multi-Select (пакетное управление) with an interactive floating batch action bar (add to collection, toggle private folder, mark read/unread, move to trash, restore, delete permanently, and select/deselect all), adds unsaved changes protection in the Book Profile edit mode (intercepting back gestures, dismisses, and close actions with a save/discard/cancel dialog), introduces an interactive tap zones onboarding tutorial for the reader (`ReaderTapZonesHintOverlay`) with persistent DataStore completion flag, enables seamless full-screen cover inspection directly from the Book Profile without sheet dismissal, and redesigns the Book Profile with a deep matte charcoal/graphite palette (`#141416`, `#1C1C1F`, `#28282D`) eliminating blueish tints, removing the redundant "Читать" button, placing the edit button into the top header, and tightening the genre chip layout.
`2.3.0-beta.12` changed no dependencies: it completely redesigns the Book Profile bottom sheet (`BookProfileBottomSheet`) in Aura Reader's own signature design, removing unwanted copied tags («теги», «фанфик» hints), eliminating all translucent see-through layers with 100% opaque deep-slate sheet containers and solid cards (`#161722` / `#212330` in dark, `#F7F6FA` / `#FFFFFF` in light), and fixing text contrast with explicit bright white titles (`#FFFFFF`) and readable subtitles (`#B4B0C2`) across both dark and light modes.
`2.3.0-beta.11` changed no dependencies: it reverts all in-app GTA VI UI overrides (removing settings toggle, top bar override, and preference keys) while locking the GTA VI Vice City seasonal emblem exclusively to the launcher icon (`ic_launcher` / `ic_launcher_round`), omitting the `<monochrome>` adaptive tag to completely prevent Android 13+ Material You dynamic color tinting until end of November 2026. It resolves the crash on «О книге» by eliminating duplicate SharedTransition keys in `BookCoverView`, opens the new Shelest-inspired Book Profile bottom sheet on cover click with both view mode and detailed edit mode (title, author, series/cycle, genres, synopsis, and full-screen cover inspection), removes the redundant favorite chip from the inline book action tray, and tightens chip paddings for an ultra-compact drawer.
`2.3.0-beta.10` changed no dependencies: it removes the explanatory subtitle under «Лицензии стороннего ПО» in settings, integrates the temporary GTA VI Vice City seasonal logo asset (`ic_aura_logo_vice.xml`, `logo_vice_gta6.svg`) celebrating the GTA 6 event with sunset aura glow in the top bar and toggleable in interface settings, redesigns the book 3-dots action menu into a smooth inline accordion expanding directly out of the bottom of the card, introduces a full Trash / Recycle Bin («Корзина») system with 15-day retention policy and batch/individual restore or deletion, and adds the interactive Book Profile bottom sheet (`BookProfileBottomSheet`) with genre tags, series metadata, synopsis, and file technical details.
`2.3.0-beta.9` changed no dependencies: it anchors the top bar logo aura glow dynamically to the expanded title logo coordinates using `localPositionOf` and ignores small pinned title measurements, eliminating any displacement or upward drift. It adds a full Open Source Licenses dialog (`LicensesDialog`) with detailed attribution cards accessible directly from the «О приложении» settings page, removes the redundant folding chevron in the single-section «О приложении» category, and introduces the encrypted Private Folder («Личная папка») with SHA-256 password setup, unlock verification, password change dialogs, book selection from the main library, and complete isolation from public searches and main collections.
`2.3.0-beta.8` changed no dependencies: it resolves the root cause of the top bar logo aura clipping by moving the halo glow rendering out of `LargeTopAppBar`'s internal bounds-clipped title container to the root `Box` of `LibraryTopBar`, tracking the logo's global offset so the aura radiates seamlessly across the status bar and header without any straight-edge cutoffs. It redesigns the holiday style picker into a sleek, compact folding card with a rotating chevron, removes the redundant "Авто" and "Стандарт" buttons (tapping an active holiday chip toggles it off, reverting to the calendar schedule), and adds ready-made Android Material color preset circle swatches (Lavender, Ocean, Teal, Forest, Amber, Rose, Indigo, Monochrome) in Interface settings.
`2.3.0-beta.7` changed no dependencies: it matches page switching animation speed precisely to the 2.2.1 release baseline (`spring(dampingRatio = NoBouncy, stiffness = 420f)`), scales the logo letter inside the app top bar for balanced proportion (34.dp inside 46.dp squircle), scales down the launcher icon letter for proper safe-zone breathing room, renders a distinguished crossbar with negative space and gradient for Android 13+ themed icons, fixes the halo glow clipping issue by calibrating the halo radius to 1.05x with soft alpha falloff, and moves the «Перейти на GitHub» button in the update dialog to the very top above the changelog.
`2.3.0-beta.6` changed no dependencies: it updates the official app logo and icon assets with the new stylized 'Å' emblem (violet `#A78BFA` accents and `#1E1B4B` deep indigo background), updating adaptive icons, round launcher icons, Android 13+ monochrome themed icons, splash screen, and top bar emblem with halo glow. It restores the responsive dock pill kinematics in `FloatingNavigationIsland` (`tapTargetSlot` instant center landing on button press), and reverts screen paging and dock morphing animation speeds to snappy, natural curves without artificial delays.
`2.3.0-beta.5` changed no dependencies: it calms and smooths page switching animation in `HorizontalPager` with a deliberate 450 ms `FastOutSlowInEasing` tween, restricts the bottom dock strictly to `AppDesignStyle.LIQUID_GLASS` while keeping Material 3 in its classic configuration (`ExtendedFloatingActionButton` at `FabPosition.End`, modal `SettingsBottomSheet` triggered from `LibraryTopBar`, top search bar), and fixes the dock pill gesture kinematics in `FloatingNavigationIsland` to instantly snap under finger on touch down (`stiffness = 1200f`) eliminating the previous delayed two-step jump to the button slot.
`2.3.0-beta.4` changed no dependencies: it implements the Chumpy-inspired morphing bottom dock and unified `HorizontalPager` architecture. All main screens («Мои книги» [0], «Поиск» [1]) and settings categories («Интерфейс» [2], «Основные» [3], «О приложении» [4]) now reside consecutively in one smooth 5-page pager. Tapping «Настройки» smoothly animates out the standard dock buttons and morphs into a circular «Назад» (←) button on the left and an expanding settings category capsule on the right with spring animations. The previous duplicate modal sheet layer was removed, completely resolving all settings card overlap issues.
`2.3.0-beta.3` changed no dependencies: it fixes the settings layout collision in `SettingsFoldContent` by laying out folded items in a `Column` with 14.dp vertical spacing, introduces tactile Chumpy-style folding cards with 90° chevron rotation indicators, adds bottom padding (110.dp + `navigationBarsPadding`) to prevent the bottom navigation dock from obscuring settings, enhances the Rockstar logo glow to radius 1.6x / slot 3.2x with rich radial alpha stops, and brings the M3 1.5.0 centered `BookCardMenu` to grid mode via a 3-dots button on book covers.
`2.3.0-beta.2` is the first 2.3 build with a new Material 3: the Compose BOM stays
at `2026.09.00` (Compose 1.12.1) and `androidx.compose.material3:material3` is
pinned to `1.5.0` explicitly, because the newer BOM `2026.11.01` would pull
Compose 1.13.0 and demand compileSdk 37.1, which is not installed. Material 3
1.5.0 itself only needs compileSdk 37.0. With it come the expressive menus
(`DropdownMenuPopup`), custom menu anchors, `MaterialTheme.motionScheme` and the
fold/chevron APIs used by the settings sheet.
`2.3.0-beta.1` changed no dependency, plugin or toolchain versions: it bumps
`versionCode`/`versionName` and ships the holiday style picker, the greeting alignment,
the logo glow and the modern wavy OPDS progress (see `docs/verification.md`).
`2.2.1` changed no dependency, plugin or toolchain versions: it bumps `versionCode`/`versionName` for the official release consolidating all 2.2.1-beta improvements (edge-margin footnote tap priority, persistent bottom progress mode, quote navigation with return pill, automatic unread collection sorting, M3 Expressive token-accurate button redesign, day-time greetings at 24 sp headline scale, and Zakon licence notice; see `docs/verification.md`).
`2.2.1-beta.1` changed no dependency, plugin or toolchain versions: it bumps `versionCode`/`versionName`, corrects the Zakon licence notice, rebuilds the split/icon/connected buttons from the published M3 Expressive token tables, and adds the day-time greeting under the app name (see `docs/verification.md`).
`2.2.0-beta.45` also changed no dependency, plugin or toolchain versions: it bumps `versionCode`/`versionName` and adds edge-margin footnote tap priority, persistent reader bottom progress mode, quotation direct navigation with return pill, and automatic unread collection classification (see `docs/verification.md`).
`2.2.0-beta.14` changed no build files: it merged the UI-file split (see
`docs/verification.md`) and bumped the version only. `2.2.0-beta.15` turned off
Compose's new text context menu so the reader keeps its custom selection
toolbar (see the table below). `2.2.0-beta.16` also changed no build files: it
adds the translation language picker and stops selection actions from copying
to the clipboard. `2.2.0-beta.17` adds `androidx.core:core-splashscreen 1.2.0`
for the new starting window (see below).

`2.2.0-beta.18` … `2.2.0-beta.39` changed no dependency, plugin or toolchain
version: every commit in that range only bumps `versionCode`/`versionName`.
They add two binary assets and one runtime feature that do not affect the build
stack: see "Bundled assets" below.

## Current versions

| Component | Version | Notes |
|---|---|---|
| Gradle | 9.8.2 | `gradle/wrapper/gradle-wrapper.properties` |
| Android Gradle Plugin | 9.4.1 | `gradle/libs.versions.toml` |
| Kotlin | 2.4.21 | Compose compiler plugin only; Kotlin itself is built into AGP 9 |
| compileSdk | 37 | required by 2026 androidx artifacts |
| targetSdk | 36 | Play requirement since August 2026 |
| minSdk | 26 | unchanged |
| JDK | CI: 21 · local: 21 or 25 | both verified with `testDebugUnitTest lintDebug assembleDebug` |
| Compose BOM | 2026.11.01 | Compose 1.12 foundation/ui/animation |
| Compose Material 3 | 1.5.0 | Expressive Material 3 tokens |
| Navigation Compose | 2.10.2 | modern single-activity navigation graph |
| Lifecycle | 2.11.0 | |
| Core KTX | 1.19.1 | |
| Activity Compose | 1.13.0 | |
| Core SplashScreen | 1.2.0 | starting window on Android 12+ and compat below |
| DataStore | 1.3.0 | |
| Coroutines | 1.11.0 | |
| Haze | 2.1.0 | Liquid Glass blur & material3 extensions |
| jsoup | 1.24.1 | HTML parsing for EPUB/FB2 |
| Robolectric | 4.17 | unit tests |

## Why AGP 9 instead of staying on 8.x

The 2026 androidx release train (`core-ktx 1.19`, `activity 1.13`, Composable
BOM `2026.09`) declares two hard requirements:

- it must be compiled against **compileSdk 37**;
- it must be built with **AGP 9.1 or newer**.

AGP 8.13.2 refuses such a build with a list of "requires Android Gradle plugin
9.1.0 or higher" errors, and its maximum supported compileSdk is 36. Mixing new
libraries with old AGP is not possible, so the jump was made in one step
instead of upgrading androidx first and AGP later.

`targetSdk` was raised only to 36, not 37: 36 is what Google Play requires, and
37 would need separate runtime-behavior testing (edge-to-edge enforcement,
predictive back, etc.).

## What changed in the build files

- The `org.jetbrains.kotlin.android` plugin was **removed** from the root and
  app build scripts and from the version catalog. AGP 9 ships Kotlin support
  built in and fails the build if the old plugin is applied.
- `kotlinOptions { jvmTarget = "17" }` was deleted. It is an error in Kotlin
  2.4, and with built-in Kotlin the JVM target follows `compileOptions`
  (`JavaVersion.VERSION_17`).
- `material3` is pinned to `1.5.0` while the rest of Compose stays on the
  `2026.09.00` BOM. Using BOM `2026.11.01` instead would provide material3 1.5.0
  but also upgrade foundation/ui/animation to 1.13.0, whose AAR metadata requires
  `compileSdk 37.1` (a platform minor version that is a separate SDK install).
  Material 3 1.5.0 itself declares `minCompileSdk=37`, so the pin keeps the whole
  stack buildable against the installed `android-37.0` platform.
- `androidx.compose.ui:ui-text-google-fonts` and `androidx.palette:palette-ktx`
  remain without catalog entries (minor cleanup, not required).

## Bundled assets

`2.2.0-beta.19` added `app/src/main/res/font/marcellus_regular.ttf` (Marcellus,
SIL Open Font License 1.1, by Astigmatic / Brian J. Bonislawsky) for the "Aura
Reader" wordmark. `2.2.0-beta.34` added
`app/src/main/res/raw/halloween_sound.mp3`, the Uppbeat track "Spooky Ghosts"
(Halloween intro music) by Gfx Sounds; its attribution lives in
`app/src/main/assets/licenses/uppbeat_halloween_sound.txt`. `2.2.0-beta.44`
added `app/src/main/res/font/zakon_regular.ttf` (Zakon, an optional Ukrainian
interface font); it is **not** OFL, see the licence note below.

User fonts are a runtime feature, not a build dependency:
`PreferencesManager.importCustomFont` copies a picked TTF/OTF into
`filesDir/custom_fonts`, the selection is stored in `APP_FONT_KEY` as
`CUSTOM:<filename>`, and the default is `GOOGLE_SANS`. There is no size limit and
no validation beyond `Typeface.createFromFile`; a file that fails to open is
deleted on import.

Seven of the eight bundled fonts (Google Sans, Inter, JetBrains Mono, Literata,
Lora, Marcellus and PT Serif) are SIL Open Font License 1.1, as stated in each
file's `name` table, and are listed with their copyright lines and trademarks in
`app/src/main/assets/licenses/fonts.txt`. That includes Google Sans: the bundled
copy declares itself "Copyright 2025 The Google Sans Project Authors
(github.com/googlefonts/googlesans)" and OFL 1.1, not the older proprietary
build.

The eighth font, Zakon (`2.2.0-beta.44`), is **not** OFL and is documented
separately in the same file. Its `name` table names the Yaroslav the Wise
National Library of Ukraine as the licence holder and points to
`www.mincult.gov.ua`; the download did not include the terms and they have not
been verified, so the notice only records what the font itself declares.

## Source changes the upgrade required

| Library | Change | Fix |
|---|---|---|
| Haze 1.7 | `Modifier.hazeChild(shape = ...)` was removed ("Remove shape clipping", haze #287) | clip explicitly: `Modifier.clip(shape).hazeChild(...)` in `LiquidGlass.kt` (3 call sites) |
| Compose 1.12 (BOM 2026.09) | `Modifier.sharedElement(state = ...)` renamed | `sharedContentState = ...` in `LibraryScreen.kt`, `ReaderScreen.kt` |
| Compose 1.12 (BOM 2026.09) | text selection moved to the new context-menu path, which ignores `LocalTextToolbar`, so the reader's custom selection menu (quote, dictionary, translation, TTS) never appeared | `ComposeFoundationFlags.isNewContextMenuEnabled = false` in `MainActivity.onCreate`, before the first composition; the classic `TextToolbar` path is used again |
| Material 3 1.5.0 | `ExposedDropdownMenu` is now a top-level extension on `ExposedDropdownMenuBoxScope` instead of a member, so the previous call no longer resolved | explicit `import androidx.compose.material3.ExposedDropdownMenu` in `ReaderSettingsBottomSheet.kt` |

Both are visual: see the manual checklist below.

## TelemetryDeck initialization

The SDK ships a `ContentProvider` that starts TelemetryDeck from the manifest
before any activity code runs. That auto-start sent `Session.started` and
`newInstallDetected` signals even when the in-app "anonymous statistics" toggle
was off. The provider and the `com.telemetrydeck.appID` metadata are now removed
from the merged manifest; `AnalyticsManager` starts the SDK manually only after
reading the preference.

Additional cleanup in `2.2.0-beta.12`:

- `sendNewSessionBeganSignal(false)` — the app already sends its own `app_open`,
  so every foreground no longer adds a duplicate session event.
- Turning the toggle off calls `TelemetryDeck.stop()` and empties the persistent
  signal cache immediately, instead of waiting for the next launch.
- Every event carries `app_channel` (`stable`, `beta` or `debug`). Debug builds
  run in TelemetryDeck test mode; beta events can be filtered out of stable
  dashboards by this field.
- `app_update` is sent once when `versionCode` changes, with
  `previous_version_code`. A normal update keeps the same anonymous installation
  ID, so it does not inflate the unique-user count.

## Starting window (splash)

`2.2.0-beta.17` replaces the blank white launch window with a system splash:

- dependency `androidx.core:core-splashscreen 1.2.0` (Android 12+ shows the
  platform splash, older versions the library's compatibility view);
- `Theme.AuraReader.Starting` (`Theme.SplashScreen`) sets the background
  (`@color/splash_background`, with a `values-night` variant), the logo
  (`@drawable/ic_splash_logo`) and `postSplashScreenTheme`;
- the theme is applied to `MainActivity` only, so widgets and other components
  keep the normal theme;
- `installSplashScreen()` runs before `super.onCreate()`, and
  `setKeepOnScreenCondition` keeps the logo visible until the onboarding flag
  has been read;
- that flag is read in `lifecycleScope` with a 3 s timeout instead of the old
  blocking `runBlocking`, so the first frame is no longer delayed. If the
  preference cannot be read, the library opens and onboarding is shown on the
  next launch instead of hanging on the splash.

`Theme.AuraReader` also sets `android:windowBackground` to the same colour, so
there is no light flash between the splash and the first Compose frame.

## Deliberately not upgraded

| Library | Kept at | Why |
|---|---|---|
| Coil | 2.7.0 | Coil 3 is a package rename (`coil3.*`) plus a new network layer — a separate migration |
| OkHttp | 4.12.0 | OkHttp 5 is a major with API changes; only used for OPDS downloads |
| DocumentFile | 1.0.1 | newer 1.1.x is alpha-channel |

`allowBackup`, `largeHeap` and `usesCleartextTraffic` were reviewed and
intentionally left alone in this batch.

## Release signing

`signingConfigs.release` reads `SIGNING_KEY_STORE_PATH`, `SIGNING_STORE_PASSWORD`,
`SIGNING_KEY_ALIAS` and `SIGNING_KEY_PASSWORD`. CI fills them from repository
secrets and restores the keystore to `app/aura-release.jks`; the fallbacks in the
script (`aura-release.jks` / `aura`) only apply when nothing is configured.

The previous "if the keystore is missing, quietly sign with `app/debug.keystore`"
fallback was removed, and `enableV1Signing` was dropped (minSdk 26 only needs
v2/v3). The unused `debug` signing override that pointed at the same file was
removed too, so debug builds use the standard `~/.android/debug.keystore`. As a
result a release build **fails cleanly** when no keystore is present:

```
> Keystore file not set for signing config release
```

That is the intended behavior: a misconfigured build can no longer produce an
APK signed with a throwaway key. CI restores the real keystore in the
`Setup Release Keystore` step before `assembleRelease`.

For a local release sanity check, generate a temporary key with the default
file name, build, then delete it (both patterns are git-ignored). `keytool`
requires at least 6 characters, so supply the password through the environment
rather than relying on the 4-character `aura` fallback:

```sh
keytool -genkeypair -keystore app/aura-release.jks -storepass aura12 \
  -keypass aura12 -alias aura -dname "CN=Local Verify,O=Aura,C=US" \
  -keyalg RSA -validity 1000
SIGNING_STORE_PASSWORD=aura12 SIGNING_KEY_PASSWORD=aura12 ./gradlew assembleRelease
rm app/aura-release.jks
```

The resulting APK is signed with v2 and v3 schemes only (`v1: false`), which is
correct for minSdk 26. CI supplies the real passwords from repository secrets.

## Local build prerequisites

- **A JDK supported by Gradle 9.8.** CI uses JDK 21. The machine's default
  `JAVA_HOME` (JDK 25) also works — `testDebugUnitTest lintDebug assembleDebug`
  was verified on it. Older Gradle 8.x could not run on JDK 25 at all, which is
  why the `-Dorg.gradle.java.home=...jbr-21.0.11` workaround still appears in
  older notes; it is no longer required.
- Java/Kotlin bytecode target is pinned to 17 by `compileOptions` in
  `app/build.gradle.kts`, independent of the JDK that runs Gradle.
- **Android SDK platform 37** installed (`sdk.dir` in `local.properties`).
- **No keystore needed** for `testDebugUnitTest`, `lintDebug` or `assembleDebug`.

## Verification commands

```sh
./gradlew testDebugUnitTest lintDebug assembleDebug
./gradlew assembleRelease   # requires a keystore, see above
```

CI runs the first line on every push and pull request to `main`/`master`/`beta`,
plus `assembleDebug` for pull requests. Release APKs are published only from
`beta` (prerelease, when `versionName` contains `beta`) and `main` (stable).

## Manual checks after a toolchain jump

Automated checks cannot see these; run them on a device with the release APK:

1. **Liquid Glass** — dock, bottom sheets, cards, switches, the add button.
   Corners must stay rounded (the `Modifier.clip` replacement) and blur must
   still show content behind the glass.
2. **Shared element transition** — cover animation library → reader and back.
3. **TTS** — playback, word highlighting, paragraph slider, voice selection.
4. **Reader** — page flip/curl/scroll modes, font-size change, in-book links.
5. **Library** — import EPUB/FB2/MOBI/AZW3/CBZ/PDF, search, collections.
6. **OPDS** — catalog browsing and download.
7. **Widget** and **widget update** after progress changes.
8. **Backup/restore** round trip.
9. **Analytics toggle** — switch it off, relaunch, and confirm that no new
   `app_open`/session events arrive in TelemetryDeck live view (allow for the
   SDK cache delay).

## Side effect: smaller APK

| Release | APK |
|---|---|
| v2.1.0 | 5.11 MB |
| v2.2.0-beta.6 … beta.8 | 5.17 MB |
| **v2.2.0-beta.9** | **4.87 MB** |

AGP 9 changes the R8 defaults (`android.r8.optimizedResourceShrinking`,
`android.r8.strictFullModeForKeepRules`), which remove resources and default
constructors that earlier versions kept. About 300 KB (~6%) less. This was not
measured by bisecting the build, but the shrinker change is the most plausible
cause: no feature or asset was removed. The trade-off is that release-only
breakage becomes more likely, which is why the manual checklist above matters.

## Known warnings (not fixed)

The build prints deprecation warnings, most of them older than this upgrade:
`Icons.Filled.ArrowBack`/`VolumeUp`/`MenuBook` (use AutoMirrored),
`Modifier.menuAnchor()` (needs `ExposedDropdownMenuAnchorType`),
`outlinedButtonBorder` without `enabled`, `VIBRATOR_SERVICE`,
`Bitmap.inDither`, `Locale(String, String)`, and a `Nothing?`/`String`
mismatch in `BookAppWidgetProvider.kt:133`. They do not fail the build; fix
them opportunistically when touching those files.

## Rollback

The upgrade is three commits on `beta`:

```
6557234 chore(release): bump to 2.2.0-beta.9 (versionCode 126)
94dfb52 feat(import): associate .mobi, .azw3 and .cbz with the app
7bc75d4 chore(build): upgrade toolchain to Gradle 9.8.2, AGP 9.4.1, Kotlin 2.4.21
```

`adb29d6` is the last `2.2.0-beta.8` commit, the state before the jump.
