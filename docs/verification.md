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
