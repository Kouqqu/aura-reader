package com.aura.reader

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.font.FontFamily
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aura.reader.data.model.*
import com.aura.reader.ui.screens.reader.ChapterContentView
import com.aura.reader.ui.screens.reader.ChapterPagingView
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReaderPositionUiTest {
    @get:Rule val compose = createComposeRule()
    private val chapter = Chapter("chapter", "Long paragraph", "", listOf(
        FormattedBlock(BlockType.PARAGRAPH, "A long readable paragraph with many words. ".repeat(250))
    ), 0)

    @Test fun pageAnchorSurvivesFontSizeChange() {
        var fontSize by mutableFloatStateOf(18f)
        var savedText by mutableIntStateOf(1800)
        var latestText = -1
        compose.setContent {
            MaterialTheme {
                ChapterPagingView(
                    chapter = chapter, chapterIndex = 0, initialBlockIndex = 0, initialTextOffset = savedText,
                    totalChapters = 1, settings = ReaderSettings(fontSizeSp = fontSize, pagingMode = true, twoColumnMode = TwoColumnMode.OFF),
                    resolvedFontFamily = FontFamily.Serif, searchQuery = "", footnotes = emptyMap(),
                    targetBlockIndex = null, targetPage = null, onConsumeTargetBlock = {}, onConsumeTargetPage = {},
                    onToggleControls = {}, onPrevChapter = {}, onNextChapter = {}, onFootnoteClick = { _, _ -> },
                    onSaveQuote = {}, onPageChange = { _, _ -> },
                    onUpdateProgress = { _, _, text, _, _ -> latestText = text; savedText = text }
                )
            }
        }
        compose.waitForIdle()
        compose.runOnIdle { assertTrue("Must restore inside the paragraph", latestText in 1..1800) }
        var before = 0
        compose.runOnIdle { before = savedText; fontSize = 24f }
        compose.waitForIdle()
        compose.runOnIdle {
            assertTrue("Reflow must not jump forward", latestText <= before)
            assertTrue("Reflow must retain the nearby text", latestText >= (before - 1200).coerceAtLeast(1))
        }
    }

    @Test fun scrollRestoresPixelOffsetInsideLongParagraph() {
        var pixel = -1
        compose.setContent {
            MaterialTheme {
                ChapterContentView(
                    chapter = chapter, isCurrentChapter = true, initialScrollOffset = 0, initialPixelOffset = 350,
                    targetScrollOffset = null, onConsumeTargetScrollOffset = {}, settings = ReaderSettings(),
                    resolvedFontFamily = FontFamily.Serif, pageIndex = 0, chaptersCount = 1,
                    searchQuery = "", footnotes = emptyMap(), onToggleControls = {}, onFootnoteClick = { _, _ -> },
                    onSaveQuote = {}, onPrevChapter = {}, onNextChapter = {},
                    onUpdateProgress = { _, _, _, offset, _ -> pixel = offset }
                )
            }
        }
        compose.waitForIdle()
        compose.runOnIdle { assertEquals(350, pixel) }
    }
}
