package com.aura.reader.data.services

/** Target/source language offered by the in-reader translation dialog. */
data class TranslationLanguage(
    val code: String,
    val label: String
)

/** Value for "detect the language automatically" in the source selector. */
const val AUTO_TRANSLATION_LANGUAGE = "auto"

/**
 * Labels are written in their own language, so the list needs no localization
 * and every user recognises their language.
 */
val translationLanguages = listOf(
    TranslationLanguage("en", "English"),
    TranslationLanguage("ru", "Русский"),
    TranslationLanguage("uk", "Українська"),
    TranslationLanguage("be", "Беларуская"),
    TranslationLanguage("pl", "Polski"),
    TranslationLanguage("cs", "Čeština"),
    TranslationLanguage("he", "עברית"),
    TranslationLanguage("de", "Deutsch"),
    TranslationLanguage("fr", "Français"),
    TranslationLanguage("es", "Español"),
    TranslationLanguage("it", "Italiano"),
    TranslationLanguage("pt", "Português"),
    TranslationLanguage("tr", "Türkçe"),
    TranslationLanguage("ar", "العربية"),
    TranslationLanguage("zh", "中文"),
    TranslationLanguage("ja", "日本語"),
    TranslationLanguage("ko", "한국어")
)

fun translationLanguageLabel(code: String): String =
    translationLanguages.firstOrNull { it.code == code }?.label ?: code
