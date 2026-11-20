# Library and reading regression checks

## Local checks

Use JDK 21 (CI) or another JDK supported by Gradle 9.8, and Android SDK 37 (set
ANDROID_HOME or local.properties). See docs/upgrades.md for the toolchain
rationale and the signing requirements.

~~~sh
./gradlew testDebugUnitTest lintDebug assembleDebug
~~~

For an attached emulator/device:

~~~sh
./gradlew connectedDebugAndroidTest -PdebugApplicationIdSuffix=.verification
~~~

The optional application ID suffix isolates the verification APK from an existing debug installation. It does not affect release builds.

The regression suite covers:

- libraries larger than 30 books, reload and concurrent imports;
- distinct content URIs with identical filenames, including Cyrillic names;
- preserving favorites, collections and precise positions on reimport;
- rejecting stale progress updates after switching books;
- invalid backups, validation before installation, metadata write failure, repeat restore and backup round trips;
- EPUB metadata/text and bounded stream reads;
- long-paragraph anchors and progress weighted by text size;
- actual Android PDF rendering, bounded page cache and cache regeneration;
- Compose scroll restoration and font-size reflow.

## Data compatibility and behavior

Existing library JSON and version-1 backups remain readable. New text/pixel offsets default to zero when absent.

Restore merges books by ID, retains unrelated library entries and merges bookmarks/quotes by ID. Incoming records replace the matching ID. Collections are combined. Reader settings present in the backup are restored in a single DataStore transaction together with the metadata.

Restored files use content-addressed names and do not overwrite original book files. Metadata paths are updated to the installed files. A failed metadata write removes the newly installed copies. A process interruption before metadata commit may leave unused copies, while original library files remain available. Original files are retained after successful restore; restoring a changed revision can therefore use additional storage.

PDF imports stream to disk and initially render only a cover thumbnail. Full-size pages are rendered as requested and the JPEG cache retains at most 12 pages. Clearing the cache does not remove the source PDF.

Import limits: PDF/EPUB sources 512 MiB, other sources 128 MiB; EPUB entries 32 MiB each and 512 MiB expanded in total. Oversized files return an import error. Page anchors use formatted-text offsets; scroll anchors use pixel offsets. Conversion of an intra-paragraph position between scroll and paging modes is not part of this change.

## CI

Pull requests to beta run unit tests, lint and a debug build without release signing secrets. Branch pushes preserve the existing signed release workflow. Instrumentation tests require an emulator and are run separately.

## Dock optics experiment (2.0.1-beta.2)

The selection uses a Snell-law beveled capsule model adapted from liquid_glass_easy 5.0.0, with MIT attribution shipped in assets/licenses/liquid_glass_easy.txt. No lighting pass is added; tap activation and the dock layout are unchanged. RuntimeShader compilation is cached, while RenderEffect is rebound after its uniform values change.

DockRefractionTest runs four Android GPU checks: bending on both axes with an unchanged center/outside, uniform-color preservation without glints, comparison against the legacy stretch model, and tap/drag navigation.

The add button now uses a centered, non-bouncy press response (46–52dp inner circle in a 60dp button), without rotation, drag offsets or squash. Standard clickable semantics handle cancelled gestures and accessibility.

## Moving search collage and direct dock tracking — 2.0.1-beta.3

Search renders up to eight real library covers, decoded off the UI thread at bounded thumbnail size. One to three covers use sparse layouts without repeats; missing covers use the theme background. Motion loops slowly and pauses while typing, with the keyboard visible, or below STARTED lifecycle state. Liquid Glass uses a fading header; Material adds a header inside the existing library grid only while search is expanded.

Dock contact immediately positions the lens under the finger, movement tracks directly, release commits the nearest section, and cancellation preserves selection. Five dock GPU/interaction checks and the collage render/motion/pause checks passed on API 37 (seven instrumentation methods including isolated preview fixture setup). JVM regression tests and lint passed locally before final fade/title positioning adjustment; CI rechecks the final source. Full UI checked with real keyboard/results in Glass and inline search in Material. Preview uses generated fixture covers in the separate verification package.

CI now uses Gradle setup-gradle v4 with writeable beta caches and task output caching, reuses the daemon between verification and release, and cancels superseded branch runs. All tests and lint remain required before publishing. The first run warms the new cache; no fixed speed improvement is claimed. See https://github.com/gradle/actions/blob/main/docs/setup-gradle.md.

The final motion is a rigid linear diagonal stream to the right and downward, with fixed -23 degree cover rotation. Pure geometry checks sample 0–100% of the loop at phone/tablet widths, verifying a 24dp minimum gap in rotated cover coordinates, constant positive x/y displacement, and wrap only when both old/new positions are fully outside the viewport. Sparse libraries use a single stream; larger libraries use an oversized rotated grid. Layer geometry captures the immutable cover count to survive asynchronous library changes.
## Continuous collage loop, smooth dock spring gliding, and OPDS deletion cleanup — 2.0.1-beta.4

- Continuous Search Collage: Sparse libraries (1–3 books) distribute covers uniformly across the period (`period / count`), and larger libraries (4–8 books) use an evenly distributed diagonal wavefront slot layout `[0, 5, 6, 11, 1, 2, 3, 4]`. This ensures the search background never empties out at any phase of the infinite loop (verified across phone/tablet widths: min visible covers >= 2 at all times).
- Smooth Dock Gliding: Restored dual-mode touch-slop handling in `FloatingNavigationIsland`. Quick taps glide the selection pill smoothly between dock slots using critically damped spring physics (`stiffness = 320f`), eliminating instant snap teleportation while preserving responsive 1:1 finger tracking during drags.
- OPDS Book Deletion & Re-download: `BookRepository.removeBook` now cleanly deletes physical files from `context.filesDir/books/` and URI file paths upon book removal, and `OpdsViewModel` observes library changes to synchronize `_downloadedBooks`, enabling users to re-download deleted OPDS books without getting stuck on "Read".
## Clustered 2-3 book row collage and Material search transition — 2.0.1-beta.5

- Clustered 2–3 Book Rows: In `CoverCollageLayout.kt`, updated slot sequence to `[1, 2, 5, 10, 6, 9, 3, 7]` and reduced row height aspect ratio from 1.8f to 1.6f. Every row now has 2 to 3 adjacent books clustered in central columns 1–3, eliminating lonely outer-column outliers and closing the wide vertical row gap.
- Material Search Bar Animation: In `LibraryScreen.kt`, wrapped topBar with `AnimatedContent` for a smooth slide/expand and fade transition between `LargeTopAppBar` and the search bar. Added `AnimatedVisibility` for the library search hero collage and hardware back button handling to smoothly collapse search.

## In-book links, dense search collage and instant dock glide — 2.0.1-beta.6

- **In-Book Link Navigation**:
  - Full support for internal links across EPUB and FB2 formats, including inline Table of Contents (e.g. "Содержание", "Пометки", "Примечания"), internal chapter anchors, and cross-references.
  - Links are rendered with clickable theme styling and underline, navigating directly to the targeted chapter and section without requiring manual TOC navigation.
  - External links (URLs) open via system browser/intents; footnote references retain the dedicated bottom sheet.
- **Dense 12-Slot Search Cover Collage**:
  - Collage slots expanded to 12 positions (4 columns x 3 rows), ensuring all rows consistently display 2–3 covers side-by-side without vertical gaps or empty spaces.
  - Smart stride distribution tiles available book covers across slots with zero adjacent duplicates horizontally and vertically, even for small libraries (1–3 books).
  - Shuffled initialization on every search entry guarantees varied starting positions and random arrangements.
- **Instant Dock Navigation Glide**:
  - Touching down on any dock tab instantly activates tab transition, causing the glass pill to smoothly glide under the finger with zero delay (no long-press required).
  - Preserves natural spring physics (`stiffness = 380f`, critically damped) for a smooth gliding motion rather than a jarring teleport.

## Theme alignment for glass controls and the reader — 2.2.0-beta.13

- Liquid Glass switches used the iOS green. They now take the Material You primary colour (`primaryContainer` in dark themes) and follow the app theme rather than the system theme, including the pressed-state reflex.
- The AMOLED scheme mapped `primaryContainer` to its near-black surface, which made settings icon plates and the selected design-style card disappear when Material You colours were off. AMOLED now has a real accent container and a higher icon-plate alpha.
- `AuraReaderTheme` gained a `readerMode` flag: the reader keeps accent colours but swaps wallpaper-tinted surfaces for neutral dark greys, so the page colour no longer shifts with the wallpaper.
- Dropdown menus no longer use the blue-tinted translucent fill. A shared `liquidGlassMenuAppearance()` gives the list, grid, hero, sort and reader menus a neutral frosted container with a light rim.

Verified with `testDebugUnitTest lintDebug assembleDebug` locally and a green CI run; the published release is a prerelease and `latest` still points at v2.1.2.

## Splitting the largest screen files (refactor branch)

Structural refactor only: the bodies of the moved functions were copied verbatim, imports were rewritten for their new files, and no UI or behaviour changed. Checked by comparing every non-blank, non-`import` line of the previous files against the new set (each line appears exactly once, so nothing was dropped or duplicated) and by running `testDebugUnitTest lintDebug assembleDebug`.

- `ReaderScreen.kt`: 3382 → 799 lines. Moved the continuous scroll view (`ChapterContentView.kt`), the paging view and chapter transition (`ChapterPagingView.kt`), the block renderer with `InteractiveText` and footnote helpers (`ReaderBlocks.kt`), the pagination algorithm (`ReaderPagination.kt`, `paginateBlocks` became `internal`), then the floating top bar (`ReaderTopBar.kt`), the bottom controls (`ReaderBottomBar.kt`) and the text-selection popup (`ReaderSelectionToolbar.kt`).
- `LibraryScreen.kt`: 2466 → 1143 lines. Moved the list/grid cards and cover view (`BookCards.kt`), the empty state with the logo (`LibraryEmptyState.kt`), the update/new-collection/delete-collection/book-collections dialogs (`LibraryDialogs.kt`), the top bar with the search field and view-mode switch (`LibraryTopBar.kt`, 194 lines), and the update banner, reading-stats banner and collection filter chips (`LibraryBanners.kt`, 252 lines).
- `SettingsBottomSheet.kt`: 1641 → 928 lines. Moved the theme picker with the appearance rows (`SettingsThemeSection.kt`), the backup plus about/GitHub/donate sections (`SettingsInfoSections.kt`), and the design-style, language/fonts, additional-features and updates/analytics sections (`SettingsSections.kt`, 580 lines). Their private helpers became `internal` so both files can share them.

The two reader bars keep their composables untouched. State that only they used (`isSearchActive`, `showReaderMenu`, the slider step trackers) now lives inside them; opening the info dialog, TTS, bookmarks, settings and chapters sheets arrives as callbacks, and `performSliderTick` moved with the bottom bar. The selection toolbar is now a plain popup that reports its five actions, while the clipboard, dictionary and translation logic stayed in `ReaderScreen`.

`LibraryDialogs` keeps the four library dialogs exactly as they were, with view-model calls replaced by callbacks and the dialog visibility flags still owned by `LibraryScreen`.

The library top bar receives the same state it read before and reports search expansion, query changes, the view-mode toggle and opening settings as callbacks; `hasSeries` for the chip row is still computed with `remember(recentBooks)` at the call site, so the chips only recompose when the series list changes.

In `SettingsBottomSheet` the whole-sheet state (burst animation, sheet coordinates, dialogs) stays in the screen lambda. The theme section receives the picked theme through one `onThemePicked(mode, origin, colors)` callback that still triggers the pixel burst before changing the theme, so the animation timing is unchanged.

The updates section keeps the version-label behaviour through a single `onVersionTap` callback: the seven-tap developer-mode counter, the "remaining taps" toast and opening the update-channel dialog all still run in `SettingsBottomSheet`, so the tap logic reads the same state in the same place.

No version bump in the refactor commits themselves: this carries no user-visible change and ships together with the next release.

## 2.2.0-beta.14 — the refactor merged into a beta

`refactor/split-large-files` was merged into `beta` with `--no-ff` (merge commit `b1d20bf`) and the version was bumped to `2.2.0-beta.14` (`versionCode 131`). The merge is the same content CI already built green on PR #5, so no source file changed again after that check.

- Local `testDebugUnitTest lintDebug assembleDebug` pass on the merged tree before pushing.
- Release notes in `.github/workflows/build-apk.yml` gained a Beta 14 block that says the change is internal, keeps the Beta 13 notes under "Ранее", and lists the refactored screens in the "what to check" checklist.
- `docs/upgrades.md` records that beta.14 changed no build files.

The refactor is structural only, so the expected result on a device is "looks and behaves exactly like beta.13". The items worth a manual look are the library top bar, banners and collection chips, every settings section (including the seven-tap version label), and the reader bars/selection toolbar.

Verification status: CI green on the PR before the merge; the beta.14 build itself is checked by the release workflow run for `versionCode 131`. No emulator was available locally, so the visual pass is left to the tester.

## 2.2.0-beta.15 — text selection menu and chapter list

Two regressions reported on beta.14.

**Selection menu missing.** Not caused by the file split: the moved toolbar code is unchanged. Foundation 1.12 (Compose BOM 2026.09, shipped since beta.10) enabled `ComposeFoundationFlags.isNewContextMenuEnabled` by default, and `SelectionManager` only calls `TextToolbar.showMenu` when that flag is off. With the flag on, selection goes through the new context-menu path, which never uses the app's `LocalTextToolbar`, so the custom quote/dictionary/translation/TTS popup could not appear. Checked by decompiling `foundation-android-1.12.1`: `ComposeFoundationFlags.isNewContextMenuInitiallyEnabled()` returns `true`, and `SelectionManager` reads the flag before branching to `TextToolbar.showMenu`.

Fix: `ComposeFoundationFlags.isNewContextMenuEnabled = false` at the top of `MainActivity.onCreate`, before the first composition (`@OptIn(ExperimentalFoundationApi::class)`). This restores the pre-1.12 behaviour for the whole app, so text fields keep the platform toolbar and the reader gets its custom menu back.

**Chapter list scrolling.** `ChaptersBottomSheet` put a `LazyColumn` inside a `Column` without a height bound; the column measures non-weighted children with an unlimited height, so the list laid out all chapters at full height and there was nothing left to scroll. The list now uses `Modifier.weight(1f, fill = false)`: long books scroll inside the sheet, short books keep the sheet compact.

Both changes verified with `testDebugUnitTest lintDebug assembleDebug`; on-device confirmation is the next step.

## 2.2.0-beta.16 — translation languages and a clean clipboard

**Translation from any language to any language.** `DictionaryService.translateText` gained a `sourceLang` parameter (default `auto`) and the cache key now includes it, so the same text can be cached for several directions. The translation dialog gets two chip menus: source (with an "Auto" entry) and target. The target defaults to the app language and the choice is remembered for the session; changing either menu re-runs the translation through the existing `runTranslation` path. The labels are the languages' own names, so no localization is needed; the only new string is `translateAuto` ("Auto"), added to all seven string sets.

**Selected text no longer goes through the clipboard.** The selection actions used to call the copy callback just to read the selected text, which overwrote the clipboard on every quote, dictionary, translation and read-from-here tap. Both reader views now own a `SelectionState` and report `selectedTexts` upward through `onSelectionTextChange`, so `ReaderScreen` has the text without copying; `currentSelectionText()` uses it and keeps the old copy-based path only as a fallback. The `SelectionState` is per view because two pager pages are composed at once and a shared state can only be bound to one container.

Verified with `testDebugUnitTest lintDebug assembleDebug`. On-device checks: the two chip menus in the translation dialog, and that the clipboard is untouched after the selection actions.

## 2.2.0-beta.17 — page-turn crash fix and the system splash

**Crash on page turning.** `2.2.0-beta.16` gave each reader view a
`SelectionState` and bound it to its `SelectionContainer`, which fixed the
clipboard problem but broke paged mode. `ChapterPagingView` remembered the state
at the top of the composable and reused it inside the page lambda of its own
`HorizontalPager`; the pager keeps the neighbouring page composed while a swipe
starts, so two `SelectionContainer`s tried to bind the same state. Compose
rejects that with `IllegalStateException: A SelectionState can only be bound to
one SelectionContainer`, thrown during composition — the app died on the first
swipe. The state is now created inside the page content
(`rememberSelectionState()` next to `SelectionContainer`, inside the existing
`key(selectionResetKey)` block), so every page has its own. Continuous mode was
not affected (each chapter page is its own composition), but it uses the same
saveable helper for consistency.

**System splash and non-blocking start.** `MainActivity` used to call
`runBlocking { preferencesManager.hasCompletedOnboarding.first() }` before
`setContent`, holding the main thread until DataStore answered and showing a
blank white window meanwhile (the app theme was
`Theme.Material.Light.NoActionBar`). The starting window is now a real splash:
`androidx.core:core-splashscreen 1.2.0`, a `Theme.AuraReader.Starting` theme with
the brand background, a dedicated `ic_splash_logo` vector and
`postSplashScreenTheme`, applied to `MainActivity` only. `installSplashScreen()`
runs before `super.onCreate()`, `setKeepOnScreenCondition` keeps the logo on
screen until the onboarding flag is known, and the flag is read in
`lifecycleScope` with a 3 s timeout; the nav graph is composed once the value
arrives. A timeout or a DataStore error opens the library (onboarding
reappears on the next launch) instead of freezing the splash.
`Theme.AuraReader` now uses the same `@color/splash_background` as its
`windowBackground` (with a `values-night` variant), so the transition to the
first Compose frame has no light flash.

Verified with `testDebugUnitTest lintDebug assembleDebug`. On-device checks:
swiping pages forward and back in paged mode, the selection menu after a page
turn, and a cold start (splash → library, and onboarding on a clean install).


## 2.2.0-beta.18 — custom fonts and a calmer glass switch

**Custom fonts.** The appearance section can now import a `.ttf` or `.otf` file
through the system picker. `PreferencesManager.importCustomFont` copies it into
`filesDir/custom_fonts` under its sanitized display name, opens the copy with
`Typeface.createFromFile` to reject broken files, and `getCustomFonts()` lists
the folder for the font picker. Deleting the font that is currently selected
falls back to the default. `APP_FONT_KEY` gained `GOOGLE_SANS` as the default and
`CUSTOM:<file>` as the custom-font value. The same commit moved the glass switch
to a blurred, refracting lens (`DockRefraction`) and restored the dark Material
You tint that beta.17 had lost; warning fixes in `BackupArchive`, `OpdsService`,
`MobiParser`, `BookTtsService` and the widget provider are included.

Verified by the release workflow for `versionCode 135`.

## 2.2.0-beta.19 — Marcellus branding and iOS-style switches

The "Aura Reader" wordmark in the library header and onboarding now uses the
bundled Marcellus serif (`res/font/marcellus_regular.ttf`) instead of the
previous system/sans styling. The liquid-glass switch was reworked toward the
iOS 26 look: spring physics, capsule geometry and a refracting knob. The same
release applied the Material You dynamic palette to the library and search
screens, which until then kept fixed accent tones.

Verified by the release workflow for `versionCode 136`.

## 2.2.0-beta.20 — dark theme and dark dock calibration

Calibrated the dark theme and the Material You palette so that the dynamic
colors remain readable on dark surfaces, brought back the dark glass navigation
dock, fixed the top-bar height mismatch on the library screen, and made the
settings sheet adopt the same adaptive styling as the rest of the app.

Verified by the release workflow for `versionCode 137`.

## 2.2.0-beta.21 — dock refraction without blur

`LiquidGlass` was rewritten around the dock's own optical refraction: the switch
knob and the dock now bend the content behind them instead of blurring it, and
the dock page transition moves in the same direction as the swipe. Net change is
a rewrite of two glass files rather than new features.

Verified by the release workflow for `versionCode 138`.

## 2.2.0-beta.22 — crystal switches, typography, Material Symbols

The switches became fully translucent ("crystal") instead of milky, Google Sans
was applied to the dock and bottom sheets through `Type.kt`, the branding header
grew, icons moved to Material Symbols Rounded, and the search screen's cover
collage learned to distribute covers by their aspect ratio instead of a fixed
grid.

Verified by the release workflow for `versionCode 139`.

## 2.2.0-beta.23 — rounded icons everywhere and the language row

Every screen moved to the Rounded icon set (38 files, mostly icon imports), the
switch lens geometry was refined again, and the language row in settings was
fixed after the Material Expressive rework: it had lost its icon container and
its selected-state styling.

Verified by the release workflow for `versionCode 140`.

## 2.2.0-beta.24 — media-library action bar

The library header gained a media-library style action bar with sort, filter and
layout buttons instead of burying those actions in the overflow menu. Switch
dimensions and the "held" transparency were tuned, and the settings sheet was
aligned with Material Expressive.

Verified by the release workflow for `versionCode 141`.

## 2.2.0-beta.25 — settings cleanup

Restored the icon containers in settings rows, removed placeholder rows that
beta.24 had drawn but not wired, and aligned the library sort/layout buttons with
the Expressive squircle shape used elsewhere. No behaviour change beyond the
icons and spacing.

Verified by the release workflow for `versionCode 142`.

## 2.2.0-beta.26 — catalog sorting and the back gesture

`OpdsScreen`/`OpdsViewModel`: the catalog back gesture now returns to the
catalog root instead of leaving the screen, duplicated category chips were
removed, and the catalog defaults to sorting by reader count. Sorting was
compacted into four categories with an ascending/descending toggle; the new
labels went into `AppStrings` for all seven languages.

Verified by the release workflow for `versionCode 143`.

## 2.2.0-beta.27 — sheet jitter and the first jelly overscroll

Fixed the bottom-sheet dismiss jitter that appeared when a drag was released at
the sheet edge, neutralized the TTS control bar colours, re-tuned the reader top
bar TTS and overflow buttons, added the book-format badge to the hero card,
introduced the spring-based "jelly" overscroll in `ExpressiveModifiers`, updated
the backup copy strings, and refined the parallax cover viewer and the reading
stats sheet.

Verified by the release workflow for `versionCode 144`.

## 2.2.0-beta.28 — half-height sheets and one-motion overscroll

Bottom sheets returned to their half-height form with explicit scroll isolation
(`sheetScrollIsolation`), so a flick inside a list no longer drags the sheet
down. The jelly overscroll was retuned for a single rebound (damping 0.92,
36 px cap, 0.16 resistance) and applied app-wide. Added the 100 % trophy icon and
confetti when a book is finished, and fixed the Reading shelf and the recent-book
hero card logic.

Verified by the release workflow for `versionCode 145`.

## 2.2.0-beta.29 — manual shelves and real chapter titles

`Book` gained `manualShelf` (`UNREAD`/`READING`/`FINISHED`, nullable for old
data), persisted by `BookRepository` and written by the new
`updateBookPlacement`. Automatic progress updates no longer move a book out of a
shelf the reader chose by hand: progress 0 maps to `UNREAD`, 100 to `FINISHED`,
and everything in between keeps the existing manual value. The bottom reader bar
shows the real chapter title, the overscroll got its single-rebound calibration
across dialogs and sheets, and sheets can be dismissed by swiping from anywhere.
`LibraryRegressionTest` gained the manual-shelf test.

Verified by the release workflow for `versionCode 146`.

## 2.2.0-beta.30 — collection deletion persistence

Deleting a user collection only changed the in-memory list, so it reappeared
after the next load; the repository now saves the list after deletion and the
deleted collection is removed from the books that referenced it. The same commit
propagated the jelly overscroll to the remaining settings sections, dialogs and
sheets. Regression tests cover adding and removing collections.

Verified by the release workflow for `versionCode 147`.

## 2.2.0-beta.31 — seasonal holiday events

New `HolidaySeason` enum (`NONE`, `HALLOWEEN`, `CHRISTMAS_WESTERN`, `NEW_YEAR`,
`CHRISTMAS_ORTHODOX`, `OLD_NEW_YEAR`) with `resolve(enabled, override, calendar)`
and `LocalHolidaySeason`. Automatic dates: Oct 28–Nov 3, Dec 20–26, Dec 27–Jan 5,
Jan 6–8 and Jan 9–15. A developer-only override (Auto/Halloween/Christmas/New
Year/Orthodox Christmas/Off) was added to the updates section, together with the
`seasonalEffectsEnabled` toggle (default on). The header gets an animated badge,
tapping it fires a pixel burst, and `Theme.kt` applies the seasonal palette on
every screen except the reader. `HolidaySeasonTest` covers the date boundaries
and the override.

At this point all season names, greetings and the settings labels were still
hardcoded Russian; beta.37 moves them into `AppStrings`.

Verified by the release workflow for `versionCode 148`.

## 2.2.0-beta.32 — pumpkin and frost palettes

Removed the duplicate bounce in the jelly overscroll (the settle animation ran
twice), enriched the Halloween palette (pumpkin orange with witch violet) and the
winter palette (frost blue with gold), and completed the winter calendar for both
Christmases, New Year and the Old New Year period. `HolidaySeasonTest` was
extended for the new ranges.

Verified by the release workflow for `versionCode 149`.

## 2.2.0-beta.33 — festive greetings in the library

The seasonal greeting became prominent instead of a small label: a banner above
the library list, a subtitle in the header, a line in the "currently reading"
hero card and in the empty state. The strings were still the Russian ones from
beta.31.

Verified by the release workflow for `versionCode 150`.

## 2.2.0-beta.34 — holiday sound

Added `res/raw/halloween_sound.mp3` (Uppbeat sound effect) and
`HolidaySoundPlayer`, which plays it once per season when the library opens and
on badge tap. The header greeting was simplified: the full banner text was
reduced to one line and the duplicate badges were removed, so the season is
shown once in the top bar and once in the hero card.

Verified by the release workflow for `versionCode 151`.

## 2.2.0-beta.35 — reader transitions and safe insets

`NavGraph` now gives the reader a directional slide plus fade on enter, exit and
pop (280 ms), the library logo collapses with the scroll instead of jumping, and
the reader bottom sheets respect the navigation-bar insets and use a white status
bar instead of the previous translucent one. The reader top bar spacing was
adjusted to match.

Verified by the release workflow for `versionCode 152`.

## 2.2.0-beta.36 — TTS foreground type and predictive back

**TTS crash.** `BookTtsService` started its foreground notification without a
service type. Android 14+ rejects that for a media-playback service, and the
crash appeared on Android 16/17. It now calls
`ServiceCompat.startForeground(..., FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)`,
matching the manifest declaration.

**Predictive back.** `ReaderScreen`, `OpdsScreen` and `LibraryScreen` registered
their `BackHandler` unconditionally, which swallowed the system back gesture even
when there was nothing to close. Each handler is now enabled only while its
dialog/sheet/search state is open.

**Visual fixes.** Removed the ghost border that remained after a bottom sheet
closed, stabilized the top-bar title size while the header collapses, fixed the
holiday badge layout, and switched the primary call-to-action buttons to
`primary`/`onPrimary`.

Verified with `testDebugUnitTest lintDebug assembleDebug` before pushing; the
release workflow built `versionCode 153`.

## 2.2.0-beta.37 — holiday strings in every language

Maintenance release: the app should look and behave exactly like beta.36.

**Holiday text is no longer Russian-only.** `HolidaySeason` carried
`displayName`, `badgeText` and `greetingText` as hardcoded Russian strings, so the
header badge and greeting stayed Russian in all seven languages. The three fields
are gone from the data model. `holidayDisplayName(season)` and
`holidayGreeting(season)` in `ui/theme/HolidayStrings.kt` now resolve the label
and the greeting through `Strings`, and `LibraryTopBar` calls them.
`badgeText` was unused and was deleted rather than translated.

**All seven string sets define the holiday labels.** `seasonalEffectsToggle`,
`seasonalEffectsSubtitle`, `holidayModeTitle`, `holidayModeSubtitle`,
`holidayModeAuto` and `holidayModeOff` were interface defaults returning Russian,
overridden only in Russian and English, so Ukrainian, Belarusian, Polish, Czech
and Hebrew silently fell back to Russian. They are now abstract and implemented
in every class, together with the new `holidayNone`, `holidayChristmas`,
`holidayWinterHolidays` and the five greeting strings. The developer-only
override chips in `SettingsSections.kt` keep their hardcoded Russian on purpose:
they are only visible after the seven-tap developer unlock.

**Sound attribution.** `assets/licenses/uppbeat_halloween_sound.txt` records
where `res/raw/halloween_sound.mp3` came from
(`https://uppbeat.io/sfx/category/occasion/halloween`), the Uppbeat credit
requirement and the fact that the exact track title/author was not recorded when
the file was saved.

**Documentation and release notes.** This file now covers beta.18–beta.37,
`docs/upgrades.md` is up to date, and the release-notes block in
`.github/workflows/build-apk.yml` describes the actual changes instead of the
beta.17 text.

Verified with `testDebugUnitTest lintDebug assembleDebug`. The new
`HolidayStringsTest` checks that all seven languages name every season, greet
every active season (with an empty greeting for `NONE`) and define the six
holiday settings strings. On-device checks:
switch the app language to Ukrainian, Polish and Hebrew and confirm the header
badge and greeting change with it (Halloween is active, or force a season from
the developer override); the developer chips stay Russian as before.

## 2.2.0-beta.38 — correct sound credit and font notices

Assets and documentation only; no code change.

**Halloween sound credit.** beta.37 could only point at the Uppbeat category
page because the track details had not been recorded. The track is "Spooky
Ghosts" (Halloween intro music) by Gfx Sounds. The notice now names the track,
the author page (`https://uppbeat.io/contributors/gfx-sounds?type=sfx`) and the
category page it was downloaded from.

**Font licences.** All seven bundled TTFs declare SIL Open Font License 1.1 in
their own `name` table (checked by parsing the table directly). Google Sans is
the 2025 OFL release from `googlefonts/googlesans`, not the older proprietary
build; Inter, JetBrains Mono, Literata, Lora, Marcellus and PT Serif are OFL as
well. `assets/licenses/fonts.txt` lists every family with its copyright line,
trademark note and a pointer to the license text, next to the existing
`liquid_glass_easy.txt` and `uppbeat_halloween_sound.txt`.

**Documentation.** `docs/upgrades.md` no longer says the font notices are
missing and records the track/author of the sound.

Verified with `testDebugUnitTest lintDebug assembleDebug` and by listing
`assets/licenses/` inside the built APK, which now contains all three notices.

## 2.2.0-beta.39 — Valentine's Day and Easter

Two new seasons built on the existing machinery (palette, animated badge,
greeting, bursts, the seasonal-effects toggle and the developer override). The
reader is still excluded from the seasonal palette and no existing holiday
changed.

**Valentine's Day.** `VALENTINE` is active from Feb 10 to Feb 15, with a
rose/pink palette, a 💗 badge, hearts/roses bursts and a greeting in all seven
languages.

**Easter.** `EASTER` covers the short window around both traditions: two days
before to one day after Western Easter and Orthodox Easter. Dates are computed,
not tabulated: the Western date uses the anonymous Gregorian computus
(Meeus/Jones/Butcher), the Orthodox one uses the Meeus Julian computus converted
to the Gregorian calendar with the 13-day offset that is valid for 1900–2099.
When the two dates coincide (2025, 2028) the single window is used; when they are
weeks apart (2027: March 28 and May 2) both short windows are shown with the same
spring palette and one neutral greeting per language.

**Strings and UI.** `holidayValentine`, `holidayEaster` and their greetings were
added to all seven string sets, `HolidayStrings.kt` maps the two enum entries, and
`Theme.kt` gained light/dark container colours for both seasons. The developer
override row has two more hardcoded Russian chips ("💗 Валентин", "🐣 Пасха"),
like the rest of that row.

Verified with the focused `HolidaySeasonTest`/`HolidayStringsTest` runs and the
full `testDebugUnitTest lintDebug assembleDebug`. Date cases: Feb 9 → `NONE`,
Feb 10–15 → `VALENTINE`, Feb 16 → `NONE`; 2025 (both Easters on April 20) →
April 18–21; 2026 (Western April 5, Orthodox April 12) → April 3–6 and 10–13,
with April 8 → `NONE` between them; 2027 (March 28 and May 2) → March 26–29 and
April 30–May 3, with April 15 → `NONE`. On-device: force both seasons from the
developer row and check the badge, greeting, palette and burst.

## 2.2.0-beta.40 — Material 3 Expressive Split Buttons and action bar alignment

Implements the official Material 3 Split Button and Expressive button guidelines
(https://m3.material.io/components/split-button/overview and
https://m3.material.io/components/all-buttons).

**Material 3 Expressive Split Button component.** Created `ExpressiveButtons.kt`
with `ExpressiveSplitButton` and `ExpressiveIconButton`:
- Two separate interactive segments: leading button (action/label) and trailing button (disclosure chevron).
- Expressive asymmetrical shapes: leading button has 20.dp outer corners and 4.dp inner corners; trailing button has 4.dp inner corners and 20.dp outer corners.
- 2.dp gap between segments with independent ripple effects and touch states.
- Standard 40.dp button height across all variants.
- Trailing chevron features smooth 180° animated rotation when the dropdown menu opens.

**Applied in Library and OPDS Catalog.**
- Library action bar: the Sort button is now a split button with the leading action and trailing chevron, paired with an Expressive 40.dp squircle layout toggle button.
- OPDS Catalog: the Sort bar uses `ExpressiveSplitButton`, allowing quick direction toggles by tapping the leading label segment, while the trailing chevron opens the full category dropdown.
- Currently Reading Hero Card: "Continue reading" button updated to standard M3 Expressive 40.dp pill shape.

**Localization fix.** Fixed typo in Belarusian Valentine greeting (`Кахання`).

Verified with `testDebugUnitTest lintDebug assembleDebug`.

## 2.2.0-beta.41 — Material 3 Expressive Action Pair, Filter Chips, and Ripple Clipping

Implements connected action pairs, strict touch ripple boundary clipping, and full Material 3 Expressive filter chip & progress bar guidelines (https://m3.material.io/components/split-button/overview and https://m3.material.io/components/chips/overview).

**Connected Action Pair in Library.**
- Combined Sort and View Mode into a unified Expressive Split Button pair: `[ ≡ Недавние ] [ ☷ ]`.
- Removed redundant disclosure arrow next to the sort label (clicking label directly opens the sort menu).
- Trailing segment of the split button directly toggles the library layout between Grid and List.

**Strict Shape & Ripple Clipping.**
- Fixed Android rectangular touch feedback (ripple bleed): `.clip(shape)` and `Surface(onClick = ..., shape = ...)` ensure ripple and pressed states never escape beyond rounded button and chip corners.

**Material 3 Expressive Filter Chips.**
- Updated `LiquidGlassFilterChip` in Material mode to standard M3 Expressive 36.dp height and 16.dp squircle radius (`RoundedCornerShape(16.dp)`).
- Dynamic styling with `secondaryContainer` / `outlineVariant` color tokens and `labelLarge` typography.
- Smooth animated checkmark indicator (`Icons.Rounded.Check`) on selection with `expandHorizontally() + fadeIn()`.

**Rounded Progress Indicators.**
- Updated `LinearProgressIndicator` across library cards (`CurrentlyReadingHeroCard.kt`, `BookCards.kt`), book details (`BookInfoDialog.kt`), and TTS player (`TtsBottomSheet.kt`) with `strokeCap = StrokeCap.Round` and standard 6.dp height.

Verified with `testDebugUnitTest lintDebug assembleDebug`.

## 2.2.0-beta.42 — Material 3 Expressive Connected Button Group, Menu Polish, and Sheet Scroll Isolation

Implements Connected Button Groups, M3 Menu alignment & trailing selection checkmarks, bottom sheet nested scroll isolation, and settings update check behavior (https://m3.material.io/components/menus/overview, https://m3.material.io/components/all-buttons, https://m3.material.io/components/segmented-buttons/overview).

**Settings Sheet Update Check Fix.**
- Fixed premature sheet dismissal: removed accidental `showSettingsSheet = false` inside `onCheckUpdates` in `LibraryScreen.kt`. Checking for updates now keeps the settings sheet open while displaying update status toast or dialog.

**Bottom Sheet Scroll Isolation & Touch Smoothness.**
- Resolved "цепляние" (sheet gesture hijacking): enabled `skipPartiallyExpanded = true` on `SettingsBottomSheet` so the sheet opens directly to full comfortable height, eliminating the awkward half-expanded state where upward scrolling in the middle of the sheet dragged the sheet rather than scrolling the list.
- Restored `sheetScrollIsolation`: absorbs post-fling kinetic momentum so rapid scrolling to the top never violently slams the sheet shut, while preserving intentional downward drag-to-dismiss when scrolled to the top.

**Material 3 Expressive Connected Button Group.**
- Created `ExpressiveConnectedButtonItem` in `ExpressiveButtons.kt`.
- Applied Connected Button Group geometry to the library collection filter bar (`[ Все ] [ Читаю ] [ Избранное ] [ Непрочитанные ] [ Прочитанные ] [ Серии ]`): segments connect with calibrated 2.dp gaps, 16.dp outer rounded corners and 4.dp inner touching corners, `secondaryContainer` selection container with animated checkmark, and strict ripple clipping.

**Material 3 Menu Alignment & Trailing Checkmarks.**
- Updated the Library sort dropdown menu to M3 specification:
  - 16.dp container corner radius.
  - Aligned semantic leading icons for every item (`Schedule`, `Sort`, `Person`, `AutoStories`, `CollectionsBookmark`, `CloudDownload`).
  - Selection indicator checkmark (`Icons.Rounded.Check`) positioned as `trailingIcon` so text labels remain perfectly left-aligned without layout shifts between selected and unselected states.

Verified with `testDebugUnitTest lintDebug assembleDebug`.

## 2.2.0-beta.43 — Half-Screen Settings Sheet Polish, Holiday Emoji Baseline, and OPDS Expressive Loading

Refines bottom sheet interaction to half-screen presentation with zero-latency swipe dismiss, corrects holiday emoji vertical alignment, and enhances OPDS catalog loading with Material 3 Expressive circular progress indicator and slow-connection grace messaging.

**Half-Screen Settings Bottom Sheet.**
- Restored half-screen default state: `skipPartiallyExpanded = false` in `SettingsBottomSheet.kt`.
- Resolved swipe-to-dismiss freeze: removed fling velocity interception in `sheetScrollIsolation` so downward flicks immediately close the sheet with fluid 120Hz physics without hanging or stalling.
- Guaranteed camera / status bar clearance with 24.dp status bar padding.

**Holiday Emoji Vertical Alignment.**
- Corrected emoji baseline shift in `LibraryTopBar.kt`: applied optical vertical offset `-1.5.dp` to the festive emoji and centered the badge relative to the "Aura Reader" title baseline.

**OPDS Expressive Circular Loading & Slow Connection Grace.**
- Material 3 Expressive Circular Progress Indicator: 48.dp size, 4.dp stroke width with rounded caps (`strokeCap = StrokeCap.Round`) and subtle background track.
- Graceful slow-connection handling: extended socket read/call timeout (45s/50s) and introduced an animated notice («Каталог отвечает чуть дольше обычного... Пожалуйста, подождите») when the server takes longer than 3.5 seconds to reply, before gracefully falling back to error view if completely unresponsive.
- Localized slow-connection message across all 7 supported languages in `AppStrings.kt`.

Verified with `testDebugUnitTest lintDebug assembleDebug`.

## 2.2.0-beta.44 — Ukrainian "Zakon" Interface Font with Strict Language Filter

Integrates the official Ukrainian typeface "Zakon" (designed by Oleksii Chekal, Henadii Zarechniuk, Dmytro Rastvortsev, and Andrij Shevchenko for the Yaroslav the Wise National Library of Ukraine & Ministry of Culture) as an interface font option, protected by a strict language-aware filter.

**Font Asset & Typography System.**
- Bundled `app/src/main/res/font/zakon_regular.ttf` (OpenType with CFF outlines).
- Registered attribution and license notice in `app/src/main/assets/licenses/fonts.txt`.
- Exposed in `Theme.kt` via `resolveAppFontFamily(context, appFont, currentLanguage)` with dynamic typography reconfiguration.

**Language-Aware Visibility Filter.**
- The "Zakon" font chip in settings (`SettingsSections.kt`) only appears when the active application language is Ukrainian (`AppLanguage.UK`) or English (`AppLanguage.EN`).
- The font's character map supports English (Basic Latin) and Ukrainian Cyrillic (with specific glyphs `І`, `Ї`, `Є`, `Ґ`, and apostrophe), but explicitly lacks Russian (`ы`, `э`, `ъ`, `ё`), Belarusian (`ў`), and Hebrew glyphs.
- Automatic Graceful Fallback: switching the app language to an unsupported language (Russian, Belarusian, Polish, Czech, Hebrew) while Zakon is selected automatically and safely resets the app font to `Google Sans` in both `LibraryViewModel.setAppLanguage` and `SettingsBottomSheet`, preventing "tofu" boxes or font rendering glitches.
- Runtime Safety in Theme: `resolveAppFontFamily` directly inspects `currentLanguage` and safely resolves to Google Sans if Zakon is requested on an unsupported language, providing multi-layered protection.

**Localization & Architecture.**
- Added `appFontZakon` string across all 7 supported languages in `AppStrings.kt`.
- Added `LocalAppLanguage` composition local in `AppStrings.kt` and provided it in `MainActivity.kt`.
- Created comprehensive regression unit tests in `ZakonFontLanguageTest.kt`.

Verified with `testDebugUnitTest lintDebug assembleDebug`.

## 2.2.0 — Official Stable Release

Official stable release consolidating the entire 2.2.0 cycle (betas 1–44):
- Live Text-to-Speech spoken word highlighting with auto-scroll and word seek.
- Material 3 Expressive Connected Button Groups, Split Action Pairs, and M3 Menus.
- Liquid Glass refined optics, iOS-style translucent switches, and floating blur dock.
- Custom font import (TTF/OTF) and the official Ukrainian typeface "Zakon" with language filter.
- Seasonal holiday events (Halloween, New Year, Christmas, Valentine's Day, Easter) with auto-computed dates and user toggles.
- Library shelf organization, reading goal celebration (confetti + trophy), and hero card polish.
- In-reader multi-language translation without clipboard interference.
- New formats: MOBI, AZW3, CBZ.
- OPDS catalog graceful slow-connection handling and M3 Expressive circular loading.
- 7 supported languages, including Hebrew (עברית).
- Bundled font and audio licensing notices in `assets/licenses/`.

Verified with `testDebugUnitTest lintDebug assembleDebug`.

## 2.2.0-beta.45 (versionCode 163)

Fixes edge annotation tap priority, remembers bottom progress indicator choice across sessions, adds quote-to-text direct navigation with return pill, and resolves unread library collection classification.

**1. Footnotes & Annotations Edge-Margin Tap Priority.**
- Replaced overlay margin tap boxes in `ChapterPagingView.kt` with pointer input tap gesture detection inside spread content.
- Taps on footnotes, annotations, and book links near screen edges now take immediate precedence and open the footnote or annotation dialog without unintentionally turning the page.
- Tapping outside link bounds on page edges continues to turn pages forwards and backwards smoothly.

**2. Persistent Bottom Reading Progress Indicator Mode.**
- Added `bottomProgressMode` (0: percentage, 1: reading time remaining, 2: page of pages) to `ReaderSettings` and `PreferencesManager` DataStore keys.
- Tapping the bottom indicator pill persists the chosen metric across books, sessions, and application restarts.

**3. Direct Quote Navigation & Return to Reading Position.**
- Added `blockIndex` to `Quote` model and serialization.
- Clicking any quote in the Bookmarks & Quotes bottom sheet now jumps directly to the quote's exact chapter and text block.
- Implemented a floating return pill (`[ ⤺ Вернуться: <Глава>  ✕ ]`) in `ReaderScreen.kt` when navigating via quotes, bookmarks, or footnote internal links, allowing instant return back to the active reading spot.

**4. Automatic Unread Collection ("К прочтению") Classification.**
- Fixed regression in `LibraryViewModel.kt` where `recentBooks.value.any { it.id == book.id }` erroneously marked every library book as already started.
- Introduced unified `isReadingStarted`, `isUnread`, and `isFinished` extensions on `Book` model for consistent filtering across collections and hero card display.
- Newly imported books with 0% progress now automatically populate the "К прочтению" collection until reading begins.

Verified with `testDebugUnitTest lintDebug`.

## 2.2.1-beta.1 (versionCode 164)

First beta after the 2.2.0 stable. Three things ride in it: the corrected licence
notice, the rebuilt M3 Expressive buttons, and the new day-time greeting under the app
name. No dependency, plugin or toolchain version changed. Version:
`2.2.1-beta.1` (`versionCode 164`); the beta release body in
`.github/workflows/build-apk.yml` now leads with 2.2.1 Beta 1 and demotes Beta 45 to
"earlier". The accumulated release body was trimmed from ~21k to ~3.3k characters:
GitHub rejects a single expression (the `body` scalar, which contains
`${{ ... }}` placeholders) longer than 21,000 characters, so the first push of this
beta failed at 0 s with "Invalid workflow file / Exceeded max expression length".
Older beta notes already live in their own releases, so no history was lost.

### Font notice correction and localization audit

No code changed in this step; it fixes a licence notice that beta.44 got wrong
and records the localization debt left by the 2.2.0 cycle.

**Zakon licence notice.** `assets/licenses/fonts.txt` still opened with "all
fonts are SIL OFL 1.1" and `docs/upgrades.md` still said "all seven bundled
fonts", but beta.44 had added an eighth font. Parsing `zakon_regular.ttf`
directly shows that its `name` table carries no OFL notice: it names the
Yaroslav the Wise National Library of Ukraine as the licence holder
(`www.mincult.gov.ua`) and lists Oleksii Chekal, Henadii Zarechniuk, Dmytro
Rastvortsev and Andrij Shevchenko as authors. The notice now separates the seven
OFL fonts from Zakon, states only what the font itself declares, and says that
the exact terms were not part of the download and were not verified. The same
distinction is repeated in `docs/upgrades.md`.

**Localization audit.** `docs/i18n-audit.md` lists every user-visible Russian
string that is still hardcoded outside `Strings` (bookmarks and quotes sheet,
chapters and footnote sheets, the beta.45 return pill, update-channel dialog,
cover search, book shelf dialog, update notification, share failures), the
chapter-title fallbacks produced by the parsers, and the Belarusian entries that
still contain Russian text. Nothing on that list was changed here.

Verified by rebuilding the APK and confirming the updated `fonts.txt` is inside
`assets/licenses/`.

### M3 Expressive buttons rebuilt from the published token tables

The beta.40-42 split button, icon button and connected button group were hand-written
guesses that only loosely resembled Material 3 Expressive. They were rebuilt against the
design tokens published on m3.material.io (design system `20543ce18892f7d9`, November 2026
build) rather than against screenshots.

**Where the numbers come from.** The specs pages are a JavaScript app, so the token tables
were read from the `TOKEN_TABLE*.json` payloads the pages fetch from
`m3.material.io/_dsm/data/dsdb-m3/...` and the aliases were resolved through
`md.sys.measurement.*` / `md.sys.shape.*`. The relevant sets are
`md.comp.button.*`, `md.comp.icon-button.*`, `md.comp.split-button.*` and
`md.comp.button-group.connected.*`; only XS-XL exist (32/40/56/96/136 dp), so the old
36 dp group height had no size in the spec at all.

**Split button** (`md.comp.split-button.small`): 40 dp tall; 2 dp seam; leading button
padded 16 dp start / 12 dp end (was 14/14); leading icon 20 dp (was 18); trailing button
48 x 40 dp with a 22 dp menu icon (was 40 x 40 with 20 dp); menu icon nudged -1 dp off
centre while unselected and centred once expanded; inner corners 4 dp at rest, 12 dp on
hover/focus/press (was a fixed 4 dp), and the trailing inner corners become fully round
while the menu is open - the `selected.inner-corner.corner-size = 50%` token. Both corner
morphs use the spec's fast spatial spring (damping 0.9 / stiffness 1400).

**Icon button** (`md.comp.icon-button.small`): 40 dp container, 24 dp icon (was 20), 8 dp
padding, square corners 12 dp morphing to 8 dp while pressed (was a fixed 14 dp squircle
that is neither the square nor the round variant). A `round` flag exposes the circular
variant.

**Connected button group** (`md.comp.button-group.connected.small`): 40 dp tall (was 36),
fully round outer ends (was 16 dp), inner corners 8 dp at rest and 4 dp while pressed
(was a fixed 4 dp), adding up to the pill-shaped selected segment that the 50% selected
token describes. The animated checkmark was removed: it is not part of the Expressive
anatomy and it widened the selected segment, which is the *standard* group behaviour -
connected groups are only allowed to change the shape of the selected button, never to
push their neighbours.

**Call sites.** The library sort/view control and the OPDS sort control now pass
`size = ExpressiveButtonSize.Small` instead of a raw height, use the 20 dp leading icon and
22 dp trailing icon, and drop the extra `SemiBold` weight because `labelLarge` already
defines the label weight.

**Still deliberately not spec-compliant** (left alone because they are the app's own design
language, not oversights): the library and OPDS split buttons keep their own container
colours instead of a strict filled/tonal/outlined token set, the connected group keeps its
"outlined at rest / tonal when selected" colour pairing, and both still scale slightly on
press (M3 Expressive only morphs the corners and draws a state layer). The library split
button's trailing segment is a view-mode toggle rather than a menu icon, which is also why
it is arguably a two-item button group rather than a split button.

**Test.** New `ExpressiveButtonTokensTest` pins every small-size token (heights, paddings,
corner radii, icon sizes, the 48 dp trailing target) plus the invariant that an icon button
is `icon + 2 * padding` at every size. Full run: `testDebugUnitTest lintDebug
assembleDebug` -> `BUILD SUCCESSFUL`, 78 tests, 0 failures.

### Day-time greeting under the app name

The library header used to leave the line under "Aura Reader" empty on an ordinary day;
it only filled on a holiday. It now greets the reader by the clock - "Good morning /
afternoon / evening / night" - while a holiday keeps its own greeting exactly as before
(holiday wins, so no existing season behaviour changed).

- **Tokens.** Four new `Strings` members (`greetingMorning`, `greetingAfternoon`,
  `greetingEvening`, `greetingNight`), implemented in all seven languages:
  RU "Доброе утро / Добрый день / Добрый вечер / Доброй ночи", EN, UK, BE, PL, CS and HE.
- **Boundaries.** `dayPeriodForHour()` in `ui/theme/Greetings.kt`: morning 5-11,
  afternoon 12-16, evening 17-22, night 23-4. A plain function, so the boundaries are
  pinned by tests instead of depending on the clock at composition time. Invalid hours
  throw.
- **Live update.** `rememberCurrentHour()` re-reads the wall clock once a minute, so a
  library left open past midnight changes its greeting without a restart. The ticker only
  runs while the greeting line is visible (i.e. while the large title is expanded).
- **Style.** Ordinary greeting: `headlineSmall` (24 sp), one step below the 28 sp app name
  and reading as a second header line rather than a caption; `onSurfaceVariant`, no
  letter-spacing, 4 dp below the name row. Holiday greeting keeps its season accent,
  `SemiBold` and 0.2 sp, now at the same size. The greeting still hides with the large
  title when the bar collapses.
- **Test.** New `GreetingTest` (5 tests): every hour 0-23 maps to a period, the 4/5, 11/12,
  16/17 and 22/23 boundaries, out-of-range rejection, every language has a non-blank
  greeting for every period, and the greetings are genuinely translated (RU vs EN vs HE).
  Full run: `testDebugUnitTest lintDebug assembleDebug` -> `BUILD SUCCESSFUL`, 83 tests,
  0 failures.

### Published verification

Release: [v2.2.1-beta.1](https://github.com/Kouqqu/aura-reader/releases/tag/v2.2.1-beta.1)
(prerelease; stable `v2.2.0` is still `latest`). Tag at `1a2137b`, CI run
`44144579245` succeeded in ~11 min. The published `AuraReader.apk` (5,491,060 bytes)
was downloaded and inspected:

- `aapt dump badging`: `versionCode='164'`, `versionName='2.2.1-beta.1'`.
- `assets/licenses/fonts.txt` is now 2,773 bytes (was 2,177) and mentions both OFL
  and Zakon - the corrected notice is actually inside the APK, unlike beta.45.
- `classes.dex` contains "Доброе утро", "Доброй ночи", "Dzień dobry" and "בוקר טוב",
  plus the `DayPeriod` enum - the greeting survived R8 and ships in all languages.

## 2.3.0-beta.3 (versionCode 168)

Third 2.3 beta: fixes the settings sheet child stacking bug, adopts authentic Chumpy-style folding cards with rotating chevron badges, adds bottom inset padding against the navigation dock, restores and doubles the Rockstar aura logo glow, and brings centered M3 1.5.0 BookCardMenu to both grid and list book items.

### SettingsFold layout collision fix & Chumpy cards
In `2.3.0-beta.2`, `SettingsFoldContent` wrapped multiple composables inside a standalone `AnimatedVisibility` which defaulted to a `Box` layout, causing theme cards, holiday styles, and settings groups to pile up at coordinate (0, 0).
- `SettingsFoldContent` now wraps its content in a `Column` with 14.dp vertical spacing, ensuring clear linear layout.
- `SettingsFoldHeader` is redesigned into a tactile, rounded tonal surface card with an animated 90° rotating chevron in a circular badge.
- Added 110.dp spacer plus `navigationBarsPadding` to the bottom of `SettingsBottomSheet`, so the floating dock never obscures bottom items like "Учитывать время чтения".

### Rockstar logo aura glow restored
The logo halo slot is increased to `logoBoxSize * 3.2f` and radius to `logoBoxSize * 1.6f`, with a rich radial gradient (alpha 0.85f -> 0.65f -> 0.35f -> 0.12f -> transparent). The glow extends 25–30 dp beyond the logo surface, breathing visibly in all themes and holiday modes.

### Centered BookCardMenu in grid & list modes
- In grid view (`BookGridCard`), a circular 3-dots `IconButton` is added to the top-right overlay of book covers.
- `BookCardMenu` is moved to the card root level in both `BookListItem` and `BookGridCard`, anchoring to the full card width so that `anchorBounds.center.x - menuSize.width / 2` places the dropdown menu centered under the card with M3 1.5.0 scale-and-fade animation.
- Grid menu now includes "mark as finished" and "reset progress" for parity with list mode.

## 2.3.0-beta.2 (versionCode 167)

Second 2.3 beta: the update plaque finally installs the update, Material 3 gets its
expressive menus, the logo glow shimmers, and the Liquid Glass settings sheet folds.
No dependency is upgraded blindly - see "Material 3 1.5.0 without a new compileSdk"
below for the one version that moved and why it moved alone.

### Update downloads and installs in place

Until now the update dialog could only open the GitHub release page, so every beta
was a manual download in a browser. The plaque and the dialog now carry a real
primary action that fetches the APK, hands it to Android's installer and leaves no
file behind.

- **`UpdateInfo`** gained `apkUrl` and `apkSizeBytes`. The GitHub API response is
  parsed for the `AuraReader.apk` asset instead of only the release page; if the
  asset is missing (or an old release predates it) the download falls back to
  `https://github.com/Kouqqu/aura-reader/releases/download/<tag>/AuraReader.apk`.
  The release notes are still shown, unchanged.
- **`AppUpdateManager.downloadApk`** writes into `cacheDir/updates`, wiping the
  folder first, with a 64 KB buffer, a 15 s connect / 30 s read timeout and progress
  reported as whole percents. It rejects a non-2xx response, an empty file, a file
  shorter than `Content-Length`, and - the useful one - a body whose first two bytes
  are not `PK`, so an HTML error page saved as `.apk` cannot be offered to the
  installer. A failed download deletes its own partial file.
- **`installApk`** returns one of `STARTED`, `PERMISSION_REQUIRED`, `FAILED`. When
  "install unknown apps" is not granted it opens the per-app settings page via
  `ACTION_MANAGE_UNKNOWN_APP_SOURCES` and reports `PERMISSION_REQUIRED`, so the
  button turns into "Install" and works on the second tap once the user is back -
  no extra explanation dialog. The APK is shared through the existing `FileProvider`
  with a read grant and the package-archive MIME type.
- **Cleanup.** `cleanupDownloadedApks` runs from `MainActivity.onCreate` and deletes
  everything in `cacheDir/updates`. Because installing an update restarts the app,
  the APK is gone immediately after the update; if the install is cancelled, the
  cache and the next start still clean it up. The manifest adds
  `REQUEST_INSTALL_PACKAGES`.
- **State.** `LibraryViewModel` owns two machines: `UpdateCheckState`
  (`Idle`/`Checking`/`UpToDate`/`Failed`) and `UpdateInstallState`
  (`Idle`/`Downloading(progress?)`/`Ready`/`PermissionRequired`/`Failed(detail)`).
  The download runs in a tracked job that is cancelled when the dialog is dismissed,
  so a dismissed dialog cannot install later behind the user's back. Manual checks no
  longer raise a red snackbar; a failure is shown in place.
- **UI.** Both the update dialog and the update plaque draw a `WavyProgressIndicator`
  while bytes arrive (indeterminate when the server sends no length), swap the
  primary button between "Download and install", "Install" and "Try again", and keep
  a compact GitHub button beside it as the escape hatch. Permission, download and
  install failures each get their own sentence instead of a generic error.
- **Strings.** Eight new `Strings` members in all seven languages:
  `updateAction`, `downloadAndInstall`, `installUpdate`, `downloadFromGithub`,
  `updateReadyToInstall`, `updateDownloadFailed`, `installPermissionNeeded`,
  `updateCheckFailed`.

### Material 3 1.5.0 without a new compileSdk

`androidx.compose.material3:material3` is now `1.5.0`, the first stable M3
Expressive release, pinned explicitly in `libs.versions.toml` because the
`2026.09.00` BOM still carries `1.4.0`. The BOM itself stays at `2026.09.00`
(Compose 1.12.1): BOM `2026.11.01` would carry material3 1.5.0 *and* Compose
1.13.0, whose AAR metadata demands `compileSdk 37.1`, and only `android-37.0` is
installed locally. Material 3 1.5.0 itself declares `minCompileSdk=37`, so the pin
keeps the whole stack on the platform that exists. Recorded in `docs/upgrades.md`.

### Settings sections fold in Liquid Glass

- New `ui/screens/settings/SettingsFold.kt`: `SettingsFoldHeader` turns the section
  caption into a tappable pill with a rotating chevron, and `SettingsFoldContent`
  wraps the body in `AnimatedVisibility` with the M3 `motionScheme` spatial/effects
  specs. Outside Liquid Glass the header is the same plain bold caption it always
  was and the body is always visible, so the Material design did not change.
- Applied to Theme, Language, Additional Features, Updates, Backup and About. Design
  Style stays unfolded because it is the first decision on the sheet.
- Sections start expanded; nothing is hidden by default, and the fold state survives
  configuration changes via `rememberSaveable`.
- `SettingsBackupSection` and `SettingsAboutSection` now take an `isGlass` flag from
  `SettingsBottomSheet`.

### Book card menu grows out of the card

The list and grid "three dots" menus used stock `DropdownMenu`, which anchors to a
corner. New `ui/components/BookCardMenu.kt` uses material3's `DropdownMenuPopup`
with a `MenuAnchorPosition.Custom` provider: the menu's top-centre sits under the
card's centre, flips above when there is no room below, and is clamped back into
the window when the card is half-scrolled. It scales from 0.82 and fades in with
`MaterialTheme.motionScheme`, and keeps the Liquid Glass / Material container
appearance from `liquidGlassMenuAppearance`. Both `BookCards.kt` call sites use it.

### Logo glow: shimmer instead of a single-colour breath

The 46 dp logo square was already haloed in beta.1, but the halo was clipped at the
top of the bar and only pulsed one colour. Now the halo is drawn in its own
`requiredSize(logoBoxSize * 1.42f)` slot behind the mark, so it fades to transparent
inside its own bounds instead of being cut by the bar; the radius is `0.70x` the
logo. Its colour drifts between primary/tertiary/secondary (or the two holiday
accents) on a 9 s linear phase while a separate 3.4 s breath changes the alpha, so
the two loops do not stay in step. The collapsed bar still has no halo.

### Update check state in Settings

Checking no longer opens a toast-and-nothing flow: the check button shows a spinner
and disables itself, and an inline row animates between "Checking", "You have the
latest version" and the new failure sentence. `updateCheckFailed` was added to all
seven `Strings` implementations.

**Build.** `testDebugUnitTest lintDebug assembleDebug` -> `BUILD SUCCESSFUL`, 84
tests, 0 failures. Not run on a device: no emulator or phone was connected, so the
shimmer, the folding headers and the card-anchored menu still need one visual pass,
and the in-app install path has not been exercised on a real phone (it needs the
"install unknown apps" grant).

## 2.3.0-beta.1 (versionCode 166)

First beta of the 2.3 cycle: four appearance changes. No dependency, plugin or toolchain
version changed. The tree sits on the stable `2.2.1` (`versionCode 165`) that was released
separately with Gemini.

### Holiday style picker in Appearance

Holidays used to be reachable only through the developer mode chips in the Updates section,
with hard-coded Russian labels. They are now a normal user-facing choice inside
Appearance, next to the theme pills.

- **New file** `ui/screens/settings/HolidayStyleSection.kt`: nine plaques in the same
  rounded-16 dp / 2 dp selected-border language as `ExpressiveThemePill` (press bounce,
  haptic tick, neutral glass or surface-container fill when unselected). Each plaque has a
  30 dp emoji or icon tile, a name and a date hint.
- **Rows.** `Auto` (by calendar) and `Standard` (no decoration) on the first row; then
  Halloween, Christmas (25 Dec), New Year, Christmas (7 Jan), Winter holidays
  (9-15 Jan), Valentine and Easter. Easter spans the last row.
- **Behaviour.** Picking a season also switches the master "Holiday atmosphere" toggle on,
  so the result is visible immediately. `Standard` writes the `OFF` override and switches
  the toggle off; toggling it back on with `OFF` still selected falls back to `AUTO`
  instead of leaving the atmosphere dead. Auto and every season keep the existing
  `HolidaySeason.resolve(enabled, override)` logic untouched.
- **Plumbing.** `holidayOverride` / `onHolidayOverrideChange` moved from
  `SettingsUpdatesSection` to `SettingsThemeSection` (via `SettingsBottomSheet`); the
  hard-coded developer chip row and its `horizontalScroll` imports were deleted.
- **Strings.** Thirteen new `Strings` members (`holidayStyleTitle`, `holidayStyleSubtitle`,
  `holidayStyleAuto`, `holidayStyleAutoHint`, `holidayStyleStandard`,
  `holidayStyleStandardHint`, and one date hint per season) in all seven languages. The
  total is now 389 members.

### Greeting aligned under the app name

The 24 sp greeting sat at the very left of the title column, i.e. optically under the logo
square, which made the header look shifted. It now starts at `logoBoxSize + titleSpacing`
(58 dp expanded), so the greeting and "Aura Reader" share one left edge. Nothing else about
the greeting changed.

### Rockstar-style logo glow

The 46 dp logo square now sits on a soft radial halo: `primary` on ordinary days, the
season accent on holidays. It is drawn with `drawBehind` at 1.22x the logo size, so it never
affects layout, and breathes between 45% and 100% alpha over 3.4 s with `FastOutSlowIn`.
It is suppressed in the collapsed bar, where the 28 dp logo has no room for a halo.

### Material 3 wavy download progress (OPDS)

`ui/components/WavyProgressIndicator.kt` was redrawn to the current Material shape instead
of a custom active-only sine:

- both the active segment and the track undulate with the same phase;
- a 4 dp flat gap separates them at the progress head;
- a 2 dp stop indicator closes the line, 4 dp before the edge;
- stroke 4 dp (was 3.5), wavelength 32 dp (was 22), amplitude 2.4 dp (was 2.8), 1.6 s loop
  (was 1.1 s), and a 10 dp damping envelope at both ends of every segment.

Both OPDS call sites (book row and book details) use it unchanged.

**Tests.** `HolidayStringsTest` now checks that all thirteen new style strings are non-blank
in all seven languages. `HolidaySeasonTest` pins the Winter (`OLD_NEW_YEAR`) override and
round-trips every picker id through `HolidaySeason.resolve`, so a renamed enum constant
cannot silently break a plaque.

**Build.** `testDebugUnitTest lintDebug assembleDebug` -> `BUILD SUCCESSFUL`, 84 tests,
0 failures. Not run on a device: no emulator or phone was connected, so the glow, the
plaque grid and the wave still need one visual pass.

### Published verification

Release: [v2.3.0-beta.1](https://github.com/Kouqqu/aura-reader/releases/tag/v2.3.0-beta.1)
(prerelease; `v2.2.1` is still `latest`). Tag at `9d300a8`, CI run `44355462271` succeeded
in ~7 min. The published `AuraReader.apk` (5,491,060 bytes, SHA-256
`8920b6eda338ad915e37ea053b485ad59c8ccee2343f79a0ffb108babd7ac897`) was downloaded and
inspected:

- `aapt dump badging`: `versionCode='166'`, `versionName='2.3.0-beta.1'`.
- `classes.dex` contains "Праздничный стиль", "Стандарт", "9–15 января" and the English
  and Hebrew style titles, plus the `WavyProgressIndicator` class and the "logoGlowPulse"
  animation label - the picker, the wave and the glow all survived R8.
- "Сезонный режим (тест)" is gone from the dex: the developer-only holiday chips really
  were removed, not just hidden.

The asset size happens to equal the previous beta's 5,491,060 bytes; the digests differ,
and the inspection above confirms the new content.

## 2.2.1 — Official Stable Release (versionCode 165)

Official stable release consolidating all 2.2.1 updates:
- **Header Greeting Scale.** Day-time greeting line under the app name scaled to 24 sp (`headlineMedium.copy(fontSize = 24.sp)`), standing one level below the 28 sp app name for balanced hierarchy.
- **Footnote Tap Priority.** Taps on footnotes, links, and annotations near screen edges take full precedence over margin page-turn gestures.
- **Persistent Bottom Progress.** Reader bottom progress pill mode (percentage, minutes left, page number) is saved across sessions and books.
- **Direct Quote Navigation & Return Pill.** Clicking quotes in the bookmarks bottom sheet jumps directly to the text block, showing a floating `[ ⤺ Вернуться: <Глава>  ✕ ]` chip to return immediately to the reading position.
- **Automated "To Read" Collection.** Newly imported books automatically land in the "К прочтению" collection until reading is initiated.
- **M3 Expressive Token Accuracy.** Split buttons, connected button groups, and action chips strictly match Google Material 3 Expressive token geometry and styling.

Verified with `testDebugUnitTest lintDebug`.

## 2.3.0-beta.4 — Morphing Bottom Dock & Unified HorizontalPager Architecture (versionCode 169)

Chumpy-inspired bottom dock transformation and consolidated single-pager architecture:
- **Unified HorizontalPager.** All top-level screens reside consecutively in a single `HorizontalPager(5)`:
  - Page 0: Library («Мои книги»)
  - Page 1: Search («Поиск»)
  - Page 2: Settings: Interface («Интерфейс»)
  - Page 3: Settings: General («Основные»)
  - Page 4: Settings: About («О приложении»)
- **Morphing Bottom Dock.** Transitioning to settings smoothly morphs the floating dock:
  - Main state: Floating navigation island («Мои книги», «Поиск», «Настройки») + Floating Add glass button.
  - Settings state: Circular 60dp «Назад» (←) button on the left + floating capsule with categories («Интерфейс», «Основные», «О приложении») on the right.
  - Active category tab expands to display its label, while inactive tabs remain compact icons.
  - Spring-driven slide and fade animations between states.
- **Back Navigation.** Pressing the dock's round back button (←) or Android system back smoothly slides the dock back to main mode and scrolls the pager back to «Мои книги» (page 0).
- **Elimination of Modal Layers.** Removed the duplicate `SettingsBottomSheet` modal and hacky `LaunchedEffect` style observer that caused overlapping elements in previous builds. All settings categories render cleanly inside dedicated pager screens.

Verified with `testDebugUnitTest lintDebug assembleDebug` (all 84 tests passed, 0 failures, exit code 0).

## 2.3.0-beta.5 — Smooth Screen Transitions, Material 3 Classic Restoration & Instant Pill Capture (versionCode 170)

- **Calibrated Screen Paging Speed.** Pager transition between screens tuned to a deliberate 450 ms `FastOutSlowInEasing` curve (`pageScrollSpec = tween(450, easing = FastOutSlowInEasing)`), restoring the comfortable, smooth pacing of previous releases.
- **Strict Style Separation.** The bottom dock is rendered strictly when `readerSettings.appDesignStyle == AppDesignStyle.LIQUID_GLASS`. Material 3 stays purely in its classic layout (`ExtendedFloatingActionButton` [ + Добавить книги ] anchored at `FabPosition.End`, modal `SettingsBottomSheet` triggered via TopBar gear button, search bar in TopBar, `userScrollEnabled = false`).
- **Instant Touch-Down Pill Capture.** Fixed `FloatingNavigationIsland` gesture kinematics: the pill now immediately targets the contact position `dragOffsetPx` on touch down with high-stiffness critically damped spring physics (`stiffness = 1200f`), eliminating the 250ms delayed hop from button center to finger. On release, it settles accurately onto the selected slot center.

Verified with `testDebugUnitTest lintDebug assembleDebug` (all 84 tests passed, 0 failures, exit code 0).

## 2.3.0-beta.6 — New Official Logo & Branding, Restored Responsive Pill Kinematics & Snappy Navigation (versionCode 171)

- **New Official Logo (Å).** Integrated the new official logo across all brand touchpoints:
  - Adaptive launcher icons (`ic_launcher.png`, `ic_launcher_round.png`, `ic_launcher_foreground.png`).
  - Android 13+ Material You monochrome themed icon (`ic_launcher_monochrome.png`).
  - Background color set to deep twilight indigo `#1E1B4B` in `colors.xml`.
  - Android 12+ splash screen animated icon (`ic_splash_logo.png`).
  - In-app `AuraLogoIcon` in `LibraryEmptyState` and `LibraryTopBar` with halo shimmer glow.
- **Restored Dock Pill Kinematics.** Restored the proven instant button snap (`tapTargetSlot`) in `FloatingNavigationIsland`:
  - Instantaneous target slot lock upon touch-down without waiting or detours.
  - Smooth finger tracking when dragging.
  - Eliminated the delayed or jumping transition artifact.
- **Natural, Snappy Screen Transitions.** Removed the 450 ms artificial tween delay in `HorizontalPager`, restoring instant and smooth spring paging with synchronized dock morphing.

Verified with `testDebugUnitTest lintDebug assembleDebug` (all 84 tests passed, 0 failures, exit code 0).

## 2.3.0-beta.7 — 2.2.1 Screen Animation Kinematics, Proportional Logo Scaling & Topbar Aura Fix (versionCode 172)

- **Authentic 2.2.1 Transition Dynamics.** Set `HorizontalPager` smooth scrolling to `spring<Float>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 420f)` matching the exact comfortable pacing of the 2.2.1 release.
- **Balanced Logo Proportions.**
  - Top bar emblem: tightly cropped logo scaled to 34.dp inside the 46.dp squircle container, properly filling the mark.
  - Launcher icon: scaled down letter to height 220px in 512px canvas (safe zone ~43%), preventing the oversized look on Android launchers.
- **Distinct Crossbar for Material You.** In `ic_launcher_monochrome.png`, the crossbar has a 2.5px negative space boundary separating it from the legs and a 60%–85% alpha gradient, making the smile arc clearly readable under Android 13+ dynamic palettes.
- **Aura Clipping Elimination.** Calibrated halo radius to 1.05x with radial alpha decay reaching 0 at boundary, keeping the shimmer aura cleanly within top bar padding and preventing vertical/horizontal cutoff.
- **Immediate «Перейти на GitHub» Access.** Relocated the GitHub update button to the top of `LibraryDialogs` update content right below the title, with updated localized string «Перейти на GitHub».

Verified with `testDebugUnitTest lintDebug assembleDebug` (all 84 tests passed, 0 failures, exit code 0).

## 2.3.0-beta.8 — Unclipped Top Bar Logo Aura, Compact Holiday Style Dropdown & Android Color Presets (versionCode 173)

- **Unclipped Top Bar Logo Aura Glow.** Fixed the root cause of the straight horizontal cut-off line above the logo:
  - In `LargeTopAppBar` (Material 3 `TwoRowsTopAppBar`), the expanded title container applies an internal `clipToBounds()`, which hard-clipped any halo glow extending above the 64dp pinned boundary.
  - Moved the halo glow rendering to `LibraryTopBar`'s root container `Box` with `Modifier.drawBehind`, tracking the logo's global coordinates.
  - The aura now radiates freely across the status bar, app name, and header with zero boundary clipping.
- **Compact Folding Holiday Style Picker.**
  - Converted "Праздничный стиль" into an expandable folding card (`SettingsHolidayStyleSection`) featuring a rotating chevron and real-time state subtitle.
  - Removed duplicate "Авто: по календарю" and "Стандарт: без оформления" cards, as the "Праздничная атмосфера" toggle already governs atmosphere activation.
  - Redesigned into a 2-column compact chip grid with emoji, concise date hints, and checkmark indicators. Tapping an active holiday deselects it and smoothly returns to the automatic calendar schedule.
- **Android Color Presets (Circular Swatches).**
  - Added standard Material 3 color presets in Interface settings: Purple (Lavender), Blue (Ocean), Teal, Green (Forest), Orange (Amber), Rose, Indigo, and Neutral (Monochrome).
  - Rendered as interactive 42.dp circular swatches with expressive bounce, outer accent rings, and checkmark indicators.
  - Tapping a preset selects that color theme across the app and smoothly switches from wallpaper colors to the selected preset palette.

Verified with `testDebugUnitTest lintDebug assembleDebug` (all tests passed, 0 failures, exit code 0).

## 2.3.0-beta.9 — Aura Glow Logo Anchor Fix, Open Source Licenses Viewer & Private Folder (versionCode 174)

- **Fixed Top Bar Logo Aura Position & Drift.**
  - Identified root cause of sporadic glow displacement: `LargeTopAppBar` evaluates the `title` composable twice in separate layouts (pinned small title at y ~ 20-40dp and expanded title at y ~ 70-110dp). Storing coordinates from the small title was overwriting the position and pulling the aura 60+ dp upward.
  - Resolved by gating layout coordinate capture with `if (!isSmallTitle)` and calculating the center dynamically in `drawBehind` using `top.localPositionOf(logo, ...)`. The aura remains strictly anchored to the logo mark during all scroll states.
- **Open Source Licenses Viewer (`LicensesDialog`).**
  - Added a dedicated, clean licenses viewer showcasing all 3rd-party software, OFL fonts (Google Sans, Literata, Merriweather, Roboto, Inter, Playfair Display, Fira Code), Zakon typeface, Liquid Glass, Haze, jsoup, and AndroidX/Jetpack Compose libraries.
  - Accessible via a new «Лицензии стороннего ПО» row in the «О приложении» settings section.
- **Removed Redundant Folding Chevron in "About" Settings.**
  - Cleaned up the «О приложении» settings page: removed the superfluous `SettingsFoldHeader` folding arrow since it contains a single consolidated group.
- **Private Folder («Личная папка»).**
  - Added secure Private Folder storage with SHA-256 password hashing.
  - Setup dialog on first visit, password prompt on subsequent visits, and in-folder password change option.
  - Ability to add books from the main library into the private folder, and remove them back to the general library from context menus.
  - Books inside the private folder are completely isolated and hidden from public library views, search results, and hero banners.
  - Added dedicated `🔒 Личная папка` chip in the collections row, action buttons in book dropdown menus, and in-folder banner with lock controls.

Verified with `testDebugUnitTest lintDebug assembleDebug` (all 84 tests passed, 0 failures, exit code 0).

