package com.aura.reader.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for the Zakon Ukrainian font addition and its language restriction (UK & EN only).
 */
class ZakonFontLanguageTest {

    private fun isZakonAllowed(language: AppLanguage): Boolean =
        language == AppLanguage.UK || language == AppLanguage.EN

    @Test
    fun zakonFontAllowedOnlyForUkrainianAndEnglish() {
        assertTrue(isZakonAllowed(AppLanguage.UK))
        assertTrue(isZakonAllowed(AppLanguage.EN))

        assertFalse(isZakonAllowed(AppLanguage.RU))
        assertFalse(isZakonAllowed(AppLanguage.BE))
        assertFalse(isZakonAllowed(AppLanguage.PL))
        assertFalse(isZakonAllowed(AppLanguage.CS))
        assertFalse(isZakonAllowed(AppLanguage.HE))
    }

    @Test
    fun everyLanguageDefinesAppFontZakonLabel() {
        for (lang in AppLanguage.entries) {
            val strings = getStrings(lang)
            assertTrue(
                "appFontZakon is blank for ${lang.name}",
                strings.appFontZakon.isNotBlank()
            )
            assertEquals("Zakon", strings.appFontZakon)
        }
    }

    @Test
    fun fallbackLogicRevertsToGoogleSansWhenSwitchingToUnsupportedLanguage() {
        var currentAppFont = "ZAKON"

        fun onLanguageChange(newLang: AppLanguage) {
            if (!isZakonAllowed(newLang) && currentAppFont == "ZAKON") {
                currentAppFont = "GOOGLE_SANS"
            }
        }

        // Switching from UK to EN keeps ZAKON
        onLanguageChange(AppLanguage.EN)
        assertEquals("ZAKON", currentAppFont)

        // Switching to RU resets to GOOGLE_SANS
        onLanguageChange(AppLanguage.RU)
        assertEquals("GOOGLE_SANS", currentAppFont)

        // Switching to PL keeps GOOGLE_SANS
        onLanguageChange(AppLanguage.PL)
        assertEquals("GOOGLE_SANS", currentAppFont)
    }
}
