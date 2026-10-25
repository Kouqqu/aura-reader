# Privacy Policy — Aura Reader

Applies to Aura Reader `2.2.0-beta.9` and later builds until this document is
updated. Aura Reader is an open-source Android e-book reader
(`com.aura.reader`), published at <https://github.com/Kouqqu/aura-reader>.

**Short version:** the app has no accounts, no ads and no advertising ID. Your
books, reading positions, bookmarks and settings stay on your device. The only
automatic thing sent anywhere is a single anonymous "app opened" counter, which
you can switch off in Settings. Features that need the internet (cover search,
dictionary, translation, OPDS, update check) contact the service in question
only when you use them, and send only the text needed for that request.

---

## 1. Data that stays on your device

Stored in the app's private storage and never uploaded by the app:

- imported book files, extracted text, covers and thumbnails;
- reading progress, bookmarks, quotes, collections, reading statistics and
  streaks;
- all settings (theme, typography, TTS voice/speed, language, etc.);
- OPDS server addresses;
- the library index.

The app also looks for book files in `Downloads`, `Documents` and a `Books`
folder when scanning for new books. That scan is entirely local; no file names
ever leave the device.

Backups are exported only when you press "Export" and are written to the
location you choose.

## 2. Anonymous usage statistics (optional, on by default)

Aura Reader uses [TelemetryDeck](https://telemetrydeck.com) to count how many
people use the app. There is a toggle in **Settings → Anonymous statistics**;
turning it off stops all telemetry immediately, from the next launch onward.

### What is sent

| Event | When |
|---|---|
| `app_open` | once per app launch |
| `app_first_open` | once per installation |

Each event carries exactly these fields:

| Field | Value |
|---|---|
| `country` | two-letter country code, derived on the device from the SIM operator, then the mobile network, then the system locale. No GPS, no network lookup |
| `locale` | system language, e.g. `ru-RU` |
| `app_language` | the language selected inside the app, e.g. `ru`, `en`, `pl` |

The TelemetryDeck SDK additionally attaches the environment parameters it always
sends:

- app version and build number;
- device model, brand and CPU architecture;
- Android version, screen resolution and density, screen orientation;
- device time zone and locale;
- SDK name and version.

The SDK generates a random identifier per app installation, stored in the app's
private files. It is not linked to your Google account, an email address or a
device identifier, and it is destroyed when you uninstall the app or clear its
data.

### What is never sent

Book titles, authors, file names, book content, reading positions, bookmarks,
search queries, your OPDS servers, your IP-derived location, your name, your
email, your contacts. There is no advertising identifier, no crash-reporting SDK
and no other analytics service in the app.

### Who processes it

Signals are sent over HTTPS to TelemetryDeck GmbH (Germany, EU). Like any HTTPS
request, the server sees the connecting IP address; the app does not attach it to
the payload and does not store it. TelemetryDeck aggregates the signals into
counts. The data is not sold and is not used for advertising or profiling.

## 3. Third-party services the app contacts when you ask it to

Nothing below happens in the background except the update check, which is also
optional.

| Feature | Service | What is sent |
|---|---|---|
| Cover search | `api.litres.ru`, `api.fantlab.ru`, `itunes.apple.com`, `openlibrary.org` | the book title and/or author you search for |
| Dictionary | `ru.wiktionary.org`, `ru.wikipedia.org`, `en.wikipedia.org` | the word you look up |
| Translation | `translate.googleapis.com` | the selected text and target language |
| OPDS catalogs | the server you added | your OPDS requests. The app does not support catalog logins, so no username or password is sent. An `http://` catalog is unencrypted — prefer `https://` |
| Update check | `api.github.com`, `github.com` | nothing but a standard request; the new APK is downloaded from GitHub releases |
| Telegram / GitHub links | `t.me`, `github.com` | opened in your browser, subject to that site's own policy |

The automatic update check runs on the library screen only while **update
notifications** are enabled in Settings, and skips versions you dismissed.
Every other request is triggered by an explicit action of yours.

## 4. Android system backup

The app allows Android's built-in auto-backup (`allowBackup="true"`), so the
system may include app data in the backup of your device — typically to your own
Google account. That transfer is performed by Android, not by the app, and is
controlled in **Android Settings → System → Backup**. Disable it there if you do
not want it.

## 5. Permissions

| Permission | Why |
|---|---|
| `INTERNET` | OPDS, cover search, dictionary, translation, update check, telemetry |
| `VIBRATE` | haptic feedback on controls |
| `POST_NOTIFICATIONS` | TTS playback notification and update notices |
| `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | keep text-to-speech running with the screen off |

The app does not request location, camera, microphone, contacts, calendar, SMS,
phone state or "all files access". Books are opened through the system file
picker or the app's own folders.

## 6. Children

The app has no accounts, no user-generated content and no social features. It
does not knowingly collect personal data from anyone, including children.

## 7. Data removal

- **Telemetry:** switch it off in Settings. Previously sent signals carry only a
  random, non-reversible installation ID and cannot be traced back to a person.
- **Everything else:** clearing the app's data or uninstalling the app removes
  all locally stored content. Exported backups are files in the location you
  chose and must be deleted by you.

## 8. Changes

Material changes to this policy will be committed to this repository and noted
in the release description of the version that introduces them.

## 9. Contact

Bug reports and privacy questions: <https://github.com/Kouqqu/aura-reader/issues>
or the Telegram channel <https://t.me/koukkuteka>.

---

# Политика конфиденциальности — Aura Reader

Действует для сборок Aura Reader `2.2.0-beta.9` и новее, пока документ не
обновлён. Aura Reader — открытый Android-читалка (`com.aura.reader`),
<https://github.com/Kouqqu/aura-reader>.

**Коротко:** в приложении нет аккаунтов, рекламы и рекламного идентификатора.
Книги, позиции чтения, закладки и настройки остаются на устройстве. Единственное,
что отправляется автоматически, — анонимный счётчик «приложение открыто», и его
можно выключить в настройках. Всё остальное (поиск обложек, словарь, перевод,
OPDS, проверка обновлений) обращается в сеть только по вашему действию и передаёт
лишь текст, необходимый для запроса.

## 1. Что остаётся на устройстве

В приватном хранилище приложения и никуда не загружается: импортированные книги,
извлечённый текст, обложки и миниатюры; прогресс чтения, закладки, цитаты,
коллекции, статистика и серии; все настройки (тема, шрифты, голос и скорость TTS,
язык); адреса OPDS-серверов; индекс библиотеки.

При поиске новых книг приложение просматривает папки `Downloads`, `Documents` и
`Books`. Этот просмотр полностью локальный, имена файлов никуда не передаются.

Резервная копия создаётся только по нажатию «Экспорт» и сохраняется туда, куда
вы укажете.

## 2. Анонимная статистика (необязательно, включена по умолчанию)

Используется [TelemetryDeck](https://telemetrydeck.com), чтобы автор знал число
читателей. Переключатель — **Настройки → Анонимная статистика**. После
выключения отправка прекращается со следующего запуска.

**Что отправляется.** События `app_open` (один раз за запуск) и `app_first_open`
(один раз за установку). В каждом событии ровно три поля:

| Поле | Значение |
|---|---|
| `country` | двухбуквенный код страны, определённый на устройстве по SIM, затем по сети оператора, затем по системной локали. Без GPS и без сетевых запросов |
| `locale` | системный язык, например `ru-RU` |
| `app_language` | язык, выбранный внутри приложения: `ru`, `en`, `pl` и т. д. |

SDK TelemetryDeck дополнительно прикладывает свои стандартные параметры: версию
и номер сборки приложения; модель, бренд и архитектуру процессора устройства;
версию Android, разрешение и плотность экрана, ориентацию; часовой пояс и локаль;
имя и версию SDK.

SDK создаёт случайный идентификатор установки и хранит его в файлах приложения.
Он не связан с аккаунтом Google, почтой или идентификатором устройства и
удаляется при удалении приложения или очистке его данных.

**Что не отправляется никогда:** названия и авторы книг, имена файлов,
содержимое книг, позиции чтения, закладки, поисковые запросы, ваши OPDS-серверы,
местоположение по IP, имя, почта, контакты. Рекламного идентификатора,
SDK сбора падений и других аналитических сервисов в приложении нет.

**Кто обрабатывает.** Сигналы уходят по HTTPS в TelemetryDeck GmbH (Германия,
ЕС). Как и при любом HTTPS-запросе, сервер видит IP-адрес соединения; приложение
не включает его в данные и не сохраняет. TelemetryDeck агрегирует сигналы в
счётчики. Данные не продаются и не используются для рекламы или профилирования.

## 3. Сторонние сервисы, к которым приложение обращается по вашей команде

В фоне работает только проверка обновлений, и она тоже отключаемая.

| Функция | Сервис | Что передаётся |
|---|---|---|
| Поиск обложек | `api.litres.ru`, `api.fantlab.ru`, `itunes.apple.com`, `openlibrary.org` | название и/или автор книги, которую вы ищете |
| Словарь | `ru.wiktionary.org`, `ru.wikipedia.org`, `en.wikipedia.org` | искомое слово |
| Перевод | `translate.googleapis.com` | выделенный текст и язык перевода |
| OPDS-каталоги | сервер, который вы добавили | ваши OPDS-запросы. Вход в каталоги не поддерживается, логин и пароль не передаются. Каталог по `http://` не шифруется — лучше `https://` |
| Проверка обновлений | `api.github.com`, `github.com` | обычный запрос; новый APK скачивается из релизов GitHub |
| Ссылки Telegram / GitHub | `t.me`, `github.com` | открываются в браузере, действует политика этих сайтов |

Автоматическая проверка обновлений выполняется на экране библиотеки только при
включённых **уведомлениях об обновлениях**, пропущенные версии не показываются.
Все остальные запросы происходят только по вашему явному действию.

## 4. Системное резервное копирование Android

Приложение разрешает стандартное авто-копирование (`allowBackup="true"`), поэтому
система может включить данные приложения в резервную копию устройства — как
правило, в ваш собственный аккаунт Google. Эту передачу выполняет Android, а не
приложение; управляется в **Настройки Android → Система → Резервное копирование**.

## 5. Разрешения

| Разрешение | Зачем |
|---|---|
| `INTERNET` | OPDS, поиск обложек, словарь, перевод, обновления, статистика |
| `VIBRATE` | виброотклик элементов управления |
| `POST_NOTIFICATIONS` | уведомление плеера TTS и уведомления об обновлениях |
| `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | работа озвучки при выключенном экране |

Приложение не запрашивает геолокацию, камеру, микрофон, контакты, календарь, SMS,
состояние телефона и доступ ко всем файлам. Книги открываются через системный
выбор файлов или собственные папки приложения.

## 6. Дети

В приложении нет аккаунтов, пользовательского контента и социальных функций.
Оно сознательно не собирает персональные данные ни от кого, включая детей.

## 7. Удаление данных

- **Статистика:** выключается в настройках. Ранее отправленные сигналы содержат
  только случайный необратимый идентификатор установки и не могут быть связаны с
  человеком.
- **Всё остальное:** очистка данных приложения или его удаление стирает всё
  локальное содержимое. Экспортированные резервные копии — это файлы в выбранном
  вами месте, удалять их нужно самостоятельно.

## 8. Изменения

Существенные изменения политики фиксируются в этом репозитории и указываются в
описании релиза, который их вводит.

## 9. Контакты

Ошибки и вопросы о приватности: <https://github.com/Kouqqu/aura-reader/issues>,
Telegram-канал <https://t.me/koukkuteka>.
