package com.aura.reader.ui.screens.reader

import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.reader.data.model.BlockType
import com.aura.reader.data.model.BookLink
import com.aura.reader.data.model.FormattedBlock
import com.aura.reader.data.model.ReaderSettings
import kotlin.math.ceil

// ------------------------------------------------------------------------------------------------
// PAGINATION HELPER
// ------------------------------------------------------------------------------------------------

internal fun paginateBlocks(
    blocks: List<FormattedBlock>,
    settings: ReaderSettings,
    resolvedFontFamily: FontFamily,
    textMeasurer: TextMeasurer,
    density: Density,
    contentHeightPx: Int,
    columnWidthPx: Int,
    baseTextStyle: TextStyle = TextStyle.Default,
    footnotes: Map<String, String> = emptyMap()
): List<List<Pair<Int, FormattedBlock>>> {
    if (blocks.isEmpty()) return listOf(emptyList())

    val safeWidthPx = columnWidthPx.coerceAtLeast(100)
    // Minimal margin for font ascender/descender overhang and antialiasing
    val safetyBufferPx = with(density) { 4.dp.roundToPx() }
    val maxHeightPx = (contentHeightPx - safetyBufferPx).coerceAtLeast(100)
    val paragraphSpacingPx = with(density) { 8.dp.roundToPx() }

    val pages = mutableListOf<List<Pair<Int, FormattedBlock>>>()
    var currentPage = mutableListOf<Pair<Int, FormattedBlock>>()
    var currentHeightPx = 0

    fun flushPage() {
        if (currentPage.isNotEmpty()) {
            pages.add(ArrayList(currentPage))
            currentPage.clear()
            currentHeightPx = 0
        }
    }

    val isBionic = settings.fontName.equals("Bionic", ignoreCase = true) ||
            settings.fontName.equals("Бионический", ignoreCase = true)

    fun measureBlockText(
        text: String,
        style: TextStyle,
        maxWidth: Int = safeWidthPx,
        links: List<BookLink> = emptyList(),
        useRichSpans: Boolean = false
    ): TextLayoutResult {
        return if (useRichSpans && (isBionic || links.isNotEmpty() || footnotes.isNotEmpty())) {
            val annotated = buildMeasuredBlockText(
                rawText = text,
                links = links,
                footnotes = footnotes,
                isBionic = isBionic
            )
            textMeasurer.measure(
                text = annotated,
                style = style,
                constraints = Constraints(maxWidth = maxWidth)
            )
        } else {
            textMeasurer.measure(
                text = text,
                style = style,
                constraints = Constraints(maxWidth = maxWidth)
            )
        }
    }

    for ((originalIndex, block) in blocks.withIndex()) {
        when (block.type) {
            BlockType.IMAGE -> {
                val isGraphicBlock = block.text.startsWith("aura-pdf:") ||
                    block.text.startsWith("aurapdf://") ||
                    block.text.contains("cbz_", ignoreCase = true) ||
                    block.text.contains("cbr_", ignoreCase = true) ||
                    block.text.contains("cbt_", ignoreCase = true) ||
                    block.subText?.contains("comic", ignoreCase = true) == true ||
                    block.subText?.contains("pdf", ignoreCase = true) == true ||
                    block.subText?.contains("image_page", ignoreCase = true) == true

                if (isGraphicBlock) {
                    flushPage()
                    pages.add(listOf(originalIndex to block))
                } else {
                    val ratio = com.aura.reader.util.GraphicPageDimensionsCache
                        .getOrReadAspectRatio(block.text)
                        .coerceIn(0.25f, 4.0f)
                    val cardInnerWidthPx = (safeWidthPx - with(density) { 24.dp.roundToPx() }).coerceAtLeast(100)
                    val rawImgHeightPx = (cardInnerWidthPx / ratio).toInt()
                    val maxImgHeightPx = minOf(with(density) { 420.dp.roundToPx() }, (maxHeightPx * 0.68f).toInt())
                    val imgHeightPx = minOf(rawImgHeightPx, maxImgHeightPx)

                    var captionHeightPx = 0
                    if (!block.subText.isNullOrBlank()) {
                        val capStyle = baseTextStyle.merge(
                            TextStyle(
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                fontStyle = FontStyle.Italic,
                                textAlign = TextAlign.Center
                            )
                        )
                        val capWidthPx = (safeWidthPx - with(density) { 32.dp.roundToPx() }).coerceAtLeast(80)
                        val capLayout = textMeasurer.measure(
                            block.subText,
                            capStyle,
                            constraints = Constraints(maxWidth = capWidthPx)
                        )
                        captionHeightPx = capLayout.size.height + with(density) { 8.dp.roundToPx() }
                    }

                    val verticalPadPx = with(density) { 28.dp.roundToPx() + 24.dp.roundToPx() }
                    val totalImageBlockHeightPx = imgHeightPx + captionHeightPx + verticalPadPx
                    if (currentHeightPx + totalImageBlockHeightPx > maxHeightPx && currentPage.isNotEmpty()) {
                        flushPage()
                    }
                    currentPage.add(originalIndex to block)
                    currentHeightPx += totalImageBlockHeightPx
                }
            }
            BlockType.TITLE -> {
                val style = readerTitleTextStyle(settings, resolvedFontFamily, baseTextStyle)
                val formattedTitle = formatTypography(block.text)
                val layout = measureBlockText(
                    text = formattedTitle,
                    style = style,
                    links = block.links,
                    useRichSpans = block.links.isNotEmpty()
                )
                val titlePaddingPx = with(density) { 8.dp.roundToPx() + 14.dp.roundToPx() }
                val totalTitleHeight = layout.size.height + titlePaddingPx
                if (currentHeightPx > (maxHeightPx * 0.45f).toInt() || (currentHeightPx + totalTitleHeight > maxHeightPx && currentPage.isNotEmpty())) {
                    flushPage()
                }
                currentPage.add(originalIndex to block)
                currentHeightPx += totalTitleHeight
            }
            BlockType.SUBTITLE -> {
                val style = readerSubtitleTextStyle(settings, resolvedFontFamily, baseTextStyle)
                val formattedSubtitle = formatTypography(block.text)
                val layout = measureBlockText(
                    text = formattedSubtitle,
                    style = style,
                    links = block.links,
                    useRichSpans = block.links.isNotEmpty()
                )
                val subPaddingPx = with(density) { 14.dp.roundToPx() }
                val totalSubHeight = layout.size.height + subPaddingPx
                if (currentHeightPx > (maxHeightPx * 0.6f).toInt() || (currentHeightPx + totalSubHeight > maxHeightPx && currentPage.isNotEmpty())) {
                    flushPage()
                }
                currentPage.add(originalIndex to block)
                currentHeightPx += totalSubHeight
            }
            BlockType.DIVIDER -> {
                val dividerHeightPx = with(density) { 16.dp.roundToPx() }
                if (currentHeightPx + dividerHeightPx > maxHeightPx && currentPage.isNotEmpty()) {
                    flushPage()
                }
                currentPage.add(originalIndex to block)
                currentHeightPx += dividerHeightPx
            }
            BlockType.EPIGRAPH -> {
                val epigraphWidthPx = (safeWidthPx - with(density) { 36.dp.roundToPx() + 8.dp.roundToPx() }).coerceAtLeast(80)
                val style = readerEpigraphTextStyle(settings, resolvedFontFamily, baseTextStyle)
                val layout = measureBlockText(formatTypography(block.text), style, maxWidth = epigraphWidthPx)
                var totalHeight = layout.size.height + with(density) { 4.dp.roundToPx() + 12.dp.roundToPx() }
                if (!block.subText.isNullOrBlank()) {
                    val subStyle = readerEpigraphAuthorTextStyle(settings, resolvedFontFamily, baseTextStyle)
                    val subLayout = textMeasurer.measure(block.subText, subStyle, constraints = Constraints(maxWidth = epigraphWidthPx))
                    totalHeight += subLayout.size.height + with(density) { 4.dp.roundToPx() }
                }
                if (currentHeightPx + totalHeight > maxHeightPx && currentPage.isNotEmpty()) {
                    flushPage()
                }
                currentPage.add(originalIndex to block)
                currentHeightPx += totalHeight
            }
            BlockType.VERSE -> {
                val verseWidthPx = (safeWidthPx - with(density) { 28.dp.roundToPx() + 16.dp.roundToPx() }).coerceAtLeast(80)
                val style = readerVerseTextStyle(settings, resolvedFontFamily, baseTextStyle)
                val layout = measureBlockText(formatTypography(block.text), style, maxWidth = verseWidthPx)
                val totalHeight = layout.size.height + with(density) { 6.dp.roundToPx() + 12.dp.roundToPx() }
                if (currentHeightPx + totalHeight > maxHeightPx && currentPage.isNotEmpty()) {
                    flushPage()
                }
                currentPage.add(originalIndex to block)
                currentHeightPx += totalHeight
            }
            BlockType.PARAGRAPH -> {
                val formattedText = formatTypography(block.text.trim())
                var currentOffset = 0
                var isContinuation = false
                var loopGuard = 0

                while (currentOffset < formattedText.length && loopGuard++ < 1000) {
                    while (currentOffset < formattedText.length && formattedText[currentOffset].isWhitespace()) {
                        currentOffset++
                    }
                    if (currentOffset >= formattedText.length) break

                    val remainingText = formattedText.substring(currentOffset)
                    val availableHeightPx = maxHeightPx - currentHeightPx
                    if (availableHeightPx <= 0) {
                        flushPage()
                        continue
                    }

                    val style = readerParagraphTextStyle(
                        settings = settings,
                        resolvedFontFamily = resolvedFontFamily,
                        isContinuation = isContinuation,
                        baseStyle = baseTextStyle
                    )

                    val layoutResult = measureBlockText(
                        text = remainingText,
                        style = style,
                        links = if (currentOffset == 0) block.links else emptyList(),
                        useRichSpans = true
                    )

                    // Whole remaining paragraph fits comfortably above bottom boundary
                    if (layoutResult.size.height <= availableHeightPx) {
                        currentPage.add(originalIndex to block.copy(
                            text = remainingText,
                            sourceTextOffset = currentOffset,
                            subText = if (isContinuation) "continuation" else block.subText
                        ))
                        currentHeightPx += layoutResult.size.height + paragraphSpacingPx
                        currentOffset = formattedText.length
                        continue
                    }

                    // Otherwise count how many lines fit completely inside availableHeightPx
                    var fittingLines = 0
                    for (lineIdx in 0 until layoutResult.lineCount) {
                        val lineBottomPx = ceil(layoutResult.getLineBottom(lineIdx).toDouble()).toInt()
                        if (lineBottomPx <= availableHeightPx) {
                            fittingLines = lineIdx + 1
                        } else {
                            break
                        }
                    }

                    // Avoid orphan single lines when page already has blocks, and never force 0-line fit on non-empty page
                    if ((fittingLines == 0 || (fittingLines < 2 && currentPage.isNotEmpty())) && currentPage.isNotEmpty()) {
                        flushPage()
                        continue
                    }

                    val targetLines = fittingLines.coerceAtLeast(1)
                    val lastLineIdx = (targetLines - 1).coerceIn(0, layoutResult.lineCount - 1)
                    var endOffset = layoutResult.getLineEnd(lastLineIdx, visibleEnd = true).coerceIn(0, remainingText.length)

                    // Snap to word boundary so Hyphens.Auto doesn't cut mid-word across pages
                    if (endOffset in 1 until remainingText.length &&
                        !remainingText[endOffset].isWhitespace() &&
                        !remainingText[endOffset - 1].isWhitespace()
                    ) {
                        val lastSpace = remainingText.lastIndexOf(' ', endOffset - 1)
                        if (lastSpace > 0) {
                            endOffset = lastSpace
                        }
                    }

                    // Re-measure the sliced chunk itself!
                    // LineBreak.Strategy.HighQuality re-balances paragraphs when shortened, which can add +1 line
                    // if we only measure the unsplit paragraph. Shrink until the actual rendered chunk fits.
                    var verifiedEnd = endOffset
                    var verifyGuard = 0
                    while (verifiedEnd > 0 && verifyGuard++ < 24) {
                        val candidateChunk = remainingText.substring(0, verifiedEnd).trimEnd()
                        if (candidateChunk.isEmpty()) {
                            verifiedEnd = 0
                            break
                        }
                        val chunkLayout = measureBlockText(
                            text = candidateChunk,
                            style = style,
                            links = if (currentOffset == 0) block.links else emptyList(),
                            useRichSpans = true
                        )
                        if (chunkLayout.size.height <= availableHeightPx) {
                            break
                        }
                        if (chunkLayout.lineCount > 1) {
                            val prevLineEnd = chunkLayout.getLineEnd(chunkLayout.lineCount - 2, visibleEnd = true)
                                .coerceIn(0, candidateChunk.length - 1)
                            val spaceBefore = candidateChunk.lastIndexOf(' ', prevLineEnd)
                            verifiedEnd = if (spaceBefore > 0) spaceBefore else prevLineEnd
                        } else {
                            val spaceBefore = candidateChunk.lastIndexOf(' ', (verifiedEnd - 1).coerceAtLeast(0))
                            verifiedEnd = if (spaceBefore > 0) spaceBefore else 0
                        }
                    }

                    if (verifiedEnd <= 0) {
                        if (currentPage.isNotEmpty()) {
                            flushPage()
                            continue
                        }
                        // Extreme fallback (single unbreakable token wider/taller than whole screen): binary search char fit
                        var lo = 1
                        var hi = remainingText.length
                        var best = 1
                        while (lo <= hi) {
                            val mid = (lo + hi) ushr 1
                            val sub = remainingText.substring(0, mid)
                            val subLayout = measureBlockText(text = sub, style = style, useRichSpans = true)
                            if (subLayout.size.height <= availableHeightPx) {
                                best = mid
                                lo = mid + 1
                            } else {
                                hi = mid - 1
                            }
                        }
                        verifiedEnd = best.coerceIn(1, remainingText.length)
                    }

                    val chunk = remainingText.substring(0, verifiedEnd).trimEnd()
                    if (chunk.isNotEmpty()) {
                        currentPage.add(originalIndex to block.copy(
                            text = chunk,
                            sourceTextOffset = currentOffset,
                            subText = if (isContinuation) "continuation" else block.subText
                        ))
                    }
                    flushPage()
                    currentOffset += verifiedEnd
                    isContinuation = true
                }
            }
        }
    }

    flushPage()
    return if (pages.isEmpty()) listOf(emptyList()) else pages
}
