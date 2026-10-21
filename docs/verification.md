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
