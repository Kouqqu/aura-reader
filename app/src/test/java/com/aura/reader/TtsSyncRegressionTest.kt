package com.aura.reader

import com.aura.reader.service.TtsState
import com.aura.reader.ui.screens.reader.formatTypography
import org.junit.Assert.*
import org.junit.Test

class TtsSyncRegressionTest {

    @Test
    fun testStartFromWordOffsetCalculation() {
        val fullText = "В начале было Слово, и Слово было у Бога, и Слово было Бог."
        val selectedWord = "Слово"
        val wordOffset = fullText.indexOf(selectedWord) // 15
        assertTrue(wordOffset >= 0)

        val startFromWord = fullText.substring(wordOffset).trim()
        val leadingTrimmed = fullText.substring(wordOffset).indexOf(startFromWord)
        val exactOffset = wordOffset + (if (leadingTrimmed > 0) leadingTrimmed else 0)

        assertEquals(wordOffset, exactOffset)
        assertEquals(fullText.substring(wordOffset).trim(), startFromWord)

        // When TTS reports range (0, 5) for "Слово" in startFromWord:
        val ttsRangeStart = 0
        val ttsRangeEnd = 5
        val adjustedStart = ttsRangeStart + exactOffset
        val adjustedEnd = ttsRangeEnd + exactOffset

        assertEquals(wordOffset, adjustedStart)
        assertEquals(wordOffset + 5, adjustedEnd)
        assertEquals("Слово", fullText.substring(adjustedStart, adjustedEnd))
    }

    @Test
    fun testStartFromWordWithLeadingPunctuationOrSpace() {
        val fullText = "Капитан сказал:   «Полный вперед!» — и корабль тронулся."
        val targetSnippet = "Полный вперед"
        val wordOffset = fullText.indexOf(targetSnippet)
        val rawSlice = fullText.substring(wordOffset)
        val startFromWord = rawSlice.trim()
        val leadingTrimmed = rawSlice.indexOf(startFromWord)
        val exactOffset = wordOffset + (if (leadingTrimmed > 0) leadingTrimmed else 0)

        val firstWordSpoken = "Полный"
        val ttsRangeStart = 0
        val ttsRangeEnd = firstWordSpoken.length
        val adjustedStart = ttsRangeStart + exactOffset
        val adjustedEnd = ttsRangeEnd + exactOffset

        assertEquals(firstWordSpoken, fullText.substring(adjustedStart, adjustedEnd))
    }

    @Test
    fun testWordHighlightMatchingAccuracy() {
        val rawText = "Он вошел в дом и сел на стул. И подумал..."
        // Spoken word "и" at index 15
        val ttsWordStart = 15
        val ttsWordEnd = 16
        val ttsSpokenWord = "и"

        // Direct offset match check (as implemented in InteractiveText)
        var hlStart = -1
        var hlEnd = -1
        if (ttsWordStart in 0 until rawText.length) {
            val candidateEnd = if (ttsWordEnd in (ttsWordStart + 1)..rawText.length) ttsWordEnd else {
                val spaceIdx = rawText.indexOf(' ', ttsWordStart)
                if (spaceIdx != -1) spaceIdx else rawText.length
            }
            val candidateWord = rawText.substring(ttsWordStart, candidateEnd).trim { it <= ' ' || it in "\"'«»“”„“.,!?:;()[]{}–—-" }
            val cleanSpoken = ttsSpokenWord.trim { it <= ' ' || it in "\"'«»“”„“.,!?:;()[]{}–—-" }
            if (cleanSpoken.isEmpty() || candidateWord.equals(cleanSpoken, ignoreCase = true) ||
                candidateWord.startsWith(cleanSpoken, ignoreCase = true) || cleanSpoken.startsWith(candidateWord, ignoreCase = true)) {
                hlStart = ttsWordStart
                hlEnd = candidateEnd
            }
        }

        assertEquals(15, hlStart)
        assertEquals(16, hlEnd)
        assertEquals("и", rawText.substring(hlStart, hlEnd))
    }

    @Test
    fun testTtsStateChapterFinishedEventDefault() {
        val state = TtsState()
        assertEquals(0L, state.chapterFinishedEvent)
        assertFalse(state.isPlaying)
        assertFalse(state.isServiceRunning)
        assertEquals(-1, state.currentWordStart)
        assertEquals(-1, state.currentWordEnd)
    }

    @Test
    fun testTypographyPreservesDashesAndSpacing() {
        val original = "- Привет, - сказал он."
        val formatted = formatTypography(original)
        assertTrue(formatted.startsWith("—\u00A0"))
        assertTrue(formatted.contains("\u00A0— "))
    }
}
