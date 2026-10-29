# Build toolchain and dependency upgrades

This file records why the build stack looks the way it does, so that a future
session (human or assistant) does not have to rediscover it.

Last updated: 2026-10-29, release `2.2.0-beta.39` (`versionCode 156`).
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
| Compose BOM | 2026.09.00 | Material3 is BOM-managed, no explicit version |
| Navigation Compose | 2.10.2 | |
| Lifecycle | 2.11.0 | |
| Core KTX | 1.19.1 | |
| Activity Compose | 1.13.0 | |
| Core SplashScreen | 1.2.0 | starting window on Android 12+ and compat below |
| DataStore | 1.3.0 | |
| Coroutines | 1.11.0 | |
| Haze | 1.7.3 | Liquid Glass blur |
| jsoup | 1.23.2 | HTML parsing for EPUB/FB2 |
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
- `material3` lost its explicit version; it now comes from the Compose BOM.
- `androidx.compose.ui:ui-text-google-fonts` and `androidx.palette:palette-ktx`
  remain without catalog entries (minor cleanup, not required).

## Bundled assets

`2.2.0-beta.19` added `app/src/main/res/font/marcellus_regular.ttf` (Marcellus,
SIL Open Font License 1.1, by Astigmatic / Brian J. Bonislawsky) for the "Aura
Reader" wordmark. `2.2.0-beta.34` added
`app/src/main/res/raw/halloween_sound.mp3`, the Uppbeat track "Spooky Ghosts"
(Halloween intro music) by Gfx Sounds; its attribution lives in
`app/src/main/assets/licenses/uppbeat_halloween_sound.txt`.

User fonts are a runtime feature, not a build dependency:
`PreferencesManager.importCustomFont` copies a picked TTF/OTF into
`filesDir/custom_fonts`, the selection is stored in `APP_FONT_KEY` as
`CUSTOM:<filename>`, and the default is `GOOGLE_SANS`. There is no size limit and
no validation beyond `Typeface.createFromFile`; a file that fails to open is
deleted on import.

All seven bundled fonts are SIL Open Font License 1.1, as stated in each file's
`name` table, and are listed with their copyright lines and trademarks in
`app/src/main/assets/licenses/fonts.txt`. That includes Google Sans: the bundled
copy declares itself "Copyright 2025 The Google Sans Project Authors
(github.com/googlefonts/googlesans)" and OFL 1.1, not the older proprietary
build.

## Source changes the upgrade required

| Library | Change | Fix |
|---|---|---|
| Haze 1.7 | `Modifier.hazeChild(shape = ...)` was removed ("Remove shape clipping", haze #287) | clip explicitly: `Modifier.clip(shape).hazeChild(...)` in `LiquidGlass.kt` (3 call sites) |
| Compose 1.12 (BOM 2026.09) | `Modifier.sharedElement(state = ...)` renamed | `sharedContentState = ...` in `LibraryScreen.kt`, `ReaderScreen.kt` |
| Compose 1.12 (BOM 2026.09) | text selection moved to the new context-menu path, which ignores `LocalTextToolbar`, so the reader's custom selection menu (quote, dictionary, translation, TTS) never appeared | `ComposeFoundationFlags.isNewContextMenuEnabled = false` in `MainActivity.onCreate`, before the first composition; the classic `TextToolbar` path is used again |

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
