# Aura Reader 📖✨

Современная, быстрая и эстетичная читалка книг для Android с дизайном **Material 3 Expressive (Material You)**.

![Android](https://img.shields.io/badge/Android-8.0%2B-green.svg)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0.20-purple.svg)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-blue.svg)
![GitHub Actions](https://img.shields.io/badge/CI%2FCD-Auto%20APK%20Build-orange.svg)

---

## 🌟 Возможности приложения

- 🎨 **Material 3 Expressive & Material You**:
  - **Dynamic Color**: автоматическая адаптация цветов приложения под палитру обоев смартфона (Android 12+).
  - Выразительные плавающие панели управления, скруглённые карточки, аккуратные пружинные анимации.
- 📚 **Поддерживаемые форматы**:
  - **FB2** и **FB2.ZIP** (быстрый нативный XML-парсер, поддержка обложек, разделов и оглавления).
  - **EPUB** (распаковка архива, извлечение манифеста OPF и чистка XHTML-глав через Jsoup).
  - **TXT** (простые текстовые заметки и файлы).
- ⚙️ **Гибкие настройки для чтения**:
  - **Темы оформления**: Dynamic Material You, Светлая, Сепия (тёплая бумага) и AMOLED (глубокий чёрный).
  - **Шрифты**: С засечками (Serif), Без засечек (Sans-Serif) и Моноширинный.
  - Регулировка размера шрифта и межстрочного интервала.
- 🚀 **Удобство**:
  - Полноэкранный режим: панели плавно скрываются по тапу в центре экрана.
  - Оглавление с быстрым переходом по главам.
  - Сохранение прогресса чтения для каждой книги (DataStore).
  - Интеграция с Android: книги можно открывать напрямую из проводника или любого мессенджера.

---

## 📱 Как собрать APK и установить на телефон (без Android Studio!)

В проект встроен готовый скрипт **GitHub Actions** (`.github/workflows/build-apk.yml`), который собирает готовый `.apk` в облаке GitHub:

1. **Создайте репозиторий на GitHub:**
   - Перейдите на [github.com/new](https://github.com/new) и создайте новый репозиторий (например, `aura-reader`).

2. **Загрузите проект в репозиторий:**
   В терминале в папке проекта выполните:
   ```bash
   git init
   git add .
   git commit -m "Initial commit: Aura Reader with Material 3 Expressive"
   git branch -M main
   git remote add origin https://github.com/ВАШ_ЛОГИН/aura-reader.git
   git push -u origin main
   ```

3. **Скачайте готовый APK:**
   - Перейдите во вкладку **Actions** вашего репозитория на GitHub.
   - Вы увидите запущенный процесс **Build Android APK**.
   - Примерно через 2-3 минуты сборка завершится успешно.
   - Нажмите на сборку и в разделе **Artifacts** скачайте **`AuraReader-debug-apk`**.
   - Распакуйте архив на смартфоне и установите полученный `.apk` файл!

---

## 💻 Сборка через Android Studio (если решите установить)

1. Откройте Android Studio (версия Koala / Ladybug или новее).
2. Выберите **File -> Open...** и укажите папку `aura_reader`.
3. Дождитесь автоматической синхронизации Gradle.
4. Подключите телефон по USB (или запустите эмулятор) и нажмите зелёную кнопку **Run** (Shift + F10).
