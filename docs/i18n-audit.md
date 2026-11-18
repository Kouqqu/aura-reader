# UI localization audit

Date: 2026-11-18. State: `2.3.0-beta.1` (the first beta of the 2.3 cycle, on top of
the stable 2.2.1).
Nothing in this file has been fixed yet; it is the inventory of what still breaks
the "7 languages" promise, so a future session does not have to repeat the search.

The app stores every user-visible label in `Strings`
(`app/src/main/java/com/aura/reader/ui/theme/AppStrings.kt`), and all seven
implementations (`Ru`, `En`, `Uk`, `Be`, `Pl`, `Cs`, `He`) currently define all
389 members. The gaps below are places that never went through `Strings`.

## 1. Hardcoded Russian inside Compose screens

| Screen | File | Strings |
|---|---|---|
| Bookmarks & quotes sheet | `ui/screens/reader/BookmarksAndQuotesBottomSheet.kt` | tab titles "Закладки (N)" / "Цитаты (N)", both empty states, the bookmark hint, the quote placeholder, "Добавить", "Удалить закладку", "Удалить цитату", "Глава N" |
| Chapters sheet | `ui/screens/reader/ChaptersBottomSheet.kt` | "Оглавление (N)", fallback "Глава N" |
| Footnote sheet | `ui/screens/reader/FootnoteBottomSheet.kt` | "Сноска N" / "Сноска", "Закрыть" |
| Reader return pill (beta.45) | `ui/screens/reader/ReaderScreen.kt` | "Вернуться: <глава>", fallback "Глава N" |
| Update channel dialog | `ui/screens/settings/SettingsBottomSheet.kt` | "Официальные стабильные сборки", "Предварительные сборки с новыми функциями", toast "<канал>. Проверка обновлений..." |
| Cover search sheet | `ui/screens/library/CoverSearchBottomSheet.kt` | "Название + Автор", "Только название", "Автор", two failure toasts |
| Book shelf dialog | `ui/screens/library/LibraryDialogs.kt` | "Полка книги", "Авто (по прогрессу)", "Определяется автоматически", three shelf hints, "В избранном", "Пользовательские коллекции", "Пока нет созданных коллекций" |
| Onboarding community card | `ui/screens/onboarding/OnboardingScreen.kt` | "Официальный канал обновлений и предложений" (twice) |
| Reading stats charts | `ui/screens/library/ReadingStatsBottomSheet.kt` | minute suffix "м" |

Existing keys already cover part of this and only need wiring: `bookmarks`,
`quotes`, `noBookmarksYet`, `noQuotesYet`, `addQuotePlaceholder`,
`saveQuoteAction`, `chapter`, `contents`, `footnoteTitle`, `close`,
`checkingUpdates`, `colReading`, `colUnread`, `colFinished`, `colFavorites`,
`collectionsTitle`, `coverSaveError`.

New keys are needed for: delete-bookmark/delete-quote labels, the bookmark
hint, the "return to" verb, the two update-channel descriptions, the three
cover-search chip labels and two cover error toasts, the shelf dialog title and
hints, the Telegram card description, and a short minute suffix.

## 2. Russian outside Compose (needs a language argument)

| Place | File | Strings |
|---|---|---|
| Update notification | `data/updater/AppUpdateManager.kt` | channel name, channel description, "Доступно обновление Aura Reader!", "Вышла новая версия X..." |
| Share failures | `util/BookShareUtils.kt` | two toasts |
| Update changelog fallback | `data/updater/AppUpdateManager.kt` | "Сборка X Aura Reader с новыми изменениями." |

These functions only receive a `Context`, so they need the stored language
(`PreferencesManager`) or a `Strings` parameter.

## 3. Content-level fallbacks (language of the parsed book)

Chapter titles such as `"Глава N"` are produced in `ReaderViewModel`,
`Fb2Parser`, `EpubParser`, `MobiParser`, `TxtParser`, `PdfParser` and
`BookRepository`. They are not Compose code and have no access to `Strings`;
they surface in the reader, bookmarks and quotes. Low priority, but they are
Russian for every language.

## 4. Belarusian entries that are still Russian

`BeStrings` copies the Russian text for the whole VPN notice, the developer
mode dialogs and the update-channel block:

- `vpnNoticeTitle`, `vpnNoticeMessage`, `vpnNoticeUnderstood`
- `devModeTitle`, `devModePasswordPrompt`, `devModePasswordPlaceholder`,
  `devModeActivated`, `devModeWrongPassword`
- `updateChannelTitle`, `updateChannelSubtitle`, `updateChannelRelease`,
  `updateChannelBeta`
- `resetStatsTitle`, `resetStatsConfirmTitle`, `resetStatsConfirmMessage`
- `sortCustomOrder`

`EnStrings`, `PlStrings`, `CsStrings` and `HeStrings` have no such copies
(identical values there are brand names such as "Aura Reader" or "AMOLED").

## 5. Not a localization issue

`SettingsSections.kt` keeps the developer season chips ("Хэллоуин",
"💗 Валентин", "🐣 Пасха", ...) in Russian on purpose: that row is only visible
after the developer password and was explicitly left untranslated.
