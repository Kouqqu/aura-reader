# Library and reading regression checks

## Local checks

Use JDK 17 or 21 and Android SDK 35 (set ANDROID_HOME or local.properties).

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
