package com.aura.reader.ui.screens.reader

import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.reader.data.model.BlockType
import com.aura.reader.data.model.FormattedBlock
import com.aura.reader.data.model.ReaderSettings

// ------------------------------------------------------------------------------------------------
// PAGINATION HELPER
// ------------------------------------------------------------------------------------------------

internal fun paginateBlocks(
    blocks: List<FormattedBlock>,
    settings: ReaderSettings,
    resolvedFontFamily: FontFamily,
    textMeasurer: TextMeasurer,
    density: Density,
    contentHeightDp: Float = 600f,
    screenWidthDp: Float = 380f,
    columnWidthDp: Float? = null
): List<List<Pair<Int, FormattedBlock>>> {
    if (blocks.isEmpty()) return listOf(emptyList())

    val usableWidthDp = columnWidthDp ?: (screenWidthDp - 48f).coerceAtLeast(100f)
    val maxWidthPx = with(density) { usableWidthDp.dp.roundToPx() }

    val safetyBufferPx = with(density) { 18.dp.toPx() }
    val maxHeightPx = (with(density) { contentHeightDp.dp.toPx() } - safetyBufferPx).coerceAtLeast(100f)
    val firstPageExtraBufferPx = with(density) { 48.dp.toPx() }
    val paragraphSpacingPx = with(density) { 8.dp.toPx() }

    val pages = mutableListOf<List<Pair<Int, FormattedBlock>>>()
    var currentPage = mutableListOf<Pair<Int, FormattedBlock>>()
    var currentHeightPx = 0f

    fun getEffectiveMaxHeightPx(): Float {
        val isFirstPage = pages.isEmpty() || currentPage.any { it.second.type == BlockType.TITLE }
        return if (isFirstPage) {
            (maxHeightPx - firstPageExtraBufferPx).coerceAtLeast(100f)
        } else {
            maxHeightPx
        }
    }

    fun flushPage() {
        if (currentPage.isNotEmpty()) {
            pages.add(ArrayList(currentPage))
            currentPage.clear()
            currentHeightPx = 0f
        }
    }

    val isBionic = settings.fontName.equals("Bionic", ignoreCase = true) ||
            settings.fontName.equals("Бионический", ignoreCase = true)

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
                    val cardInnerWidthPx = (maxWidthPx - with(density) { 24.dp.toPx() }).coerceAtLeast(100f)
                    val rawImgHeightPx = cardInnerWidthPx / ratio
                    val maxImgHeightPx = minOf(with(density) { 420.dp.toPx() }, maxHeightPx * 0.68f)
                    val imgHeightPx = minOf(rawImgHeightPx, maxImgHeightPx)

                    var captionHeightPx = 0f
                    if (!block.subText.isNullOrBlank()) {
                        val capStyle = TextStyle(
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            fontStyle = FontStyle.Italic,
                            textAlign = TextAlign.Center
                        )
                        val capLayout = textMeasurer.measure(
                            block.subText,
                            capStyle,
                            constraints = Constraints(maxWidth = maxWidthPx)
                        )
                        captionHeightPx = capLayout.size.height + with(density) { 8.dp.toPx() }
                    }

                    val totalImageBlockHeightPx = imgHeightPx + captionHeightPx + with(density) { 52.dp.toPx() }
                    val effectiveMax = getEffectiveMaxHeightPx()
                    if (currentHeightPx + totalImageBlockHeightPx > effectiveMax && currentPage.isNotEmpty()) {
                        flushPage()
                    }
                    currentPage.add(originalIndex to block)
                    currentHeightPx += totalImageBlockHeightPx
                }
            }
            BlockType.TITLE -> {
                val style = TextStyle(
                    fontSize = (settings.fontSizeSp * 1.35f).sp,
                    lineHeight = (settings.fontSizeSp * settings.lineHeightMultiplier * 1.25f).sp,
                    fontFamily = resolvedFontFamily,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                val layout = textMeasurer.measure(block.text, style, constraints = Constraints(maxWidth = maxWidthPx))
                val titlePaddingPx = with(density) { 22.dp.toPx() } // 8dp top + 14dp bottom
                val totalTitleHeight = layout.size.height + titlePaddingPx
                val effectiveMax = getEffectiveMaxHeightPx()
                if (currentHeightPx > effectiveMax * 0.45f || (currentHeightPx + totalTitleHeight > effectiveMax && currentPage.isNotEmpty())) {
                    flushPage()
                }
                currentPage.add(originalIndex to block)
                currentHeightPx += totalTitleHeight
            }
            BlockType.SUBTITLE -> {
                val style = TextStyle(
                    fontSize = (settings.fontSizeSp * 1.15f).sp,
                    lineHeight = (settings.fontSizeSp * settings.lineHeightMultiplier * 1.15f).sp,
                    fontFamily = resolvedFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                val layout = textMeasurer.measure(block.text, style, constraints = Constraints(maxWidth = maxWidthPx))
                val subPaddingPx = with(density) { 14.dp.toPx() } // 14dp bottom
                val totalSubHeight = layout.size.height + subPaddingPx
                val effectiveMax = getEffectiveMaxHeightPx()
                if (currentHeightPx > effectiveMax * 0.6f || (currentHeightPx + totalSubHeight > effectiveMax && currentPage.isNotEmpty())) {
                    flushPage()
                }
                currentPage.add(originalIndex to block)
                currentHeightPx += totalSubHeight
            }
            BlockType.DIVIDER -> {
                val dividerHeightPx = with(density) { 16.dp.toPx() }
                val effectiveMax = getEffectiveMaxHeightPx()
                if (currentHeightPx + dividerHeightPx > effectiveMax && currentPage.isNotEmpty()) {
                    flushPage()
                }
                currentPage.add(originalIndex to block)
                currentHeightPx += dividerHeightPx
            }
            BlockType.EPIGRAPH -> {
                val epigraphWidthPx = with(density) { (usableWidthDp - 44f).coerceAtLeast(80f).dp.roundToPx() }
                val style = TextStyle(
                    fontSize = (settings.fontSizeSp * 0.92f).sp,
                    lineHeight = (settings.fontSizeSp * settings.lineHeightMultiplier).sp,
                    fontFamily = resolvedFontFamily,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.End,
                    hyphens = if (settings.autoHyphenation) Hyphens.Auto else Hyphens.None,
                    lineBreak = if (settings.autoHyphenation) LineBreak(strategy = LineBreak.Strategy.HighQuality, strictness = LineBreak.Strictness.Loose, wordBreak = LineBreak.WordBreak.Default) else LineBreak.Simple
                )
                val layout = textMeasurer.measure(formatTypography(block.text), style, constraints = Constraints(maxWidth = epigraphWidthPx))
                var totalHeight = layout.size.height + with(density) { 16.dp.toPx() } // 4dp top + 12dp bottom
                if (!block.subText.isNullOrBlank()) {
                    val subStyle = TextStyle(
                        fontSize = (settings.fontSizeSp * 0.82f).sp,
                        fontFamily = resolvedFontFamily,
                        textAlign = TextAlign.End
                    )
                    val subLayout = textMeasurer.measure(block.subText, subStyle, constraints = Constraints(maxWidth = epigraphWidthPx))
                    totalHeight += subLayout.size.height + with(density) { 4.dp.toPx() } // 4dp spacer
                }
                val effectiveMax = getEffectiveMaxHeightPx()
                if (currentHeightPx + totalHeight > effectiveMax && currentPage.isNotEmpty()) {
                    flushPage()
                }
                currentPage.add(originalIndex to block)
                currentHeightPx += totalHeight
            }
            BlockType.VERSE -> {
                val verseWidthPx = with(density) { (usableWidthDp - 44f).coerceAtLeast(80f).dp.roundToPx() }
                val style = TextStyle(
                    fontSize = (settings.fontSizeSp * 0.95f).sp,
                    lineHeight = (settings.fontSizeSp * settings.lineHeightMultiplier * 0.95f).sp,
                    fontFamily = resolvedFontFamily,
                    fontStyle = FontStyle.Italic,
                    hyphens = if (settings.autoHyphenation) Hyphens.Auto else Hyphens.None,
                    lineBreak = if (settings.autoHyphenation) LineBreak(strategy = LineBreak.Strategy.HighQuality, strictness = LineBreak.Strictness.Loose, wordBreak = LineBreak.WordBreak.Default) else LineBreak.Simple
                )
                val layout = textMeasurer.measure(formatTypography(block.text), style, constraints = Constraints(maxWidth = verseWidthPx))
                val totalHeight = layout.size.height + with(density) { 18.dp.toPx() } // 6dp top + 12dp bottom
                val effectiveMax = getEffectiveMaxHeightPx()
                if (currentHeightPx + totalHeight > effectiveMax && currentPage.isNotEmpty()) {
                    flushPage()
                }
                currentPage.add(originalIndex to block)
                currentHeightPx += totalHeight
            }
            BlockType.PARAGRAPH -> {
                val formattedText = formatTypography(block.text.trim())
                var remainingText = formattedText
                var isContinuation = false
                var loopGuard = 0

                while (remainingText.isNotEmpty() && loopGuard++ < 1000) {
                    val effectiveMax = getEffectiveMaxHeightPx()
                    val availableHeightPx = effectiveMax - currentHeightPx

                    val style = TextStyle(
                        fontSize = settings.fontSizeSp.sp,
                        lineHeight = (settings.fontSizeSp * settings.lineHeightMultiplier).sp,
                        fontFamily = resolvedFontFamily,
                        textIndent = if (isContinuation) TextIndent.None else TextIndent(firstLine = (settings.fontSizeSp * 1.2f).sp),
                        hyphens = if (settings.autoHyphenation) Hyphens.Auto else Hyphens.None,
                        lineBreak = if (settings.autoHyphenation) LineBreak(strategy = LineBreak.Strategy.HighQuality, strictness = LineBreak.Strictness.Loose, wordBreak = LineBreak.WordBreak.Default) else LineBreak.Simple
                    )

                    val layoutResult = if (isBionic) {
                        val annotated = androidx.compose.ui.text.buildAnnotatedString {
                            append(remainingText)
                            applyBionicStyles(remainingText)
                        }
                        textMeasurer.measure(
                            text = annotated,
                            style = style,
                            constraints = Constraints(maxWidth = maxWidthPx)
                        )
                    } else {
                        textMeasurer.measure(
                            text = remainingText,
                            style = style,
                            constraints = Constraints(maxWidth = maxWidthPx)
                        )
                    }

                    if (layoutResult.size.height + paragraphSpacingPx <= availableHeightPx) {
                        currentPage.add(originalIndex to block.copy(
                            text = remainingText,
                            sourceTextOffset = formattedText.length - remainingText.length,
                            subText = if (isContinuation) "continuation" else block.subText
                        ))
                        currentHeightPx += layoutResult.size.height + paragraphSpacingPx
                        remainingText = ""
                    } else {
                        var fittingLines = 0
                        for (lineIdx in 0 until layoutResult.lineCount) {
                            val lineBottom = layoutResult.getLineBottom(lineIdx)
                            if (lineBottom + paragraphSpacingPx <= availableHeightPx) {
                                fittingLines = lineIdx + 1
                            } else {
                                break
                            }
                        }

                        if (fittingLines < 2 && currentPage.isNotEmpty()) {
                            flushPage()
                            continue
                        }

                        val linesToTake = fittingLines.coerceAtLeast(1)
                        val lastLineIdx = (linesToTake - 1).coerceIn(0, layoutResult.lineCount - 1)
                        var endOffset = layoutResult.getLineEnd(lastLineIdx, visibleEnd = true).coerceIn(0, remainingText.length)
                        if (endOffset in 1 until remainingText.length && !remainingText[endOffset].isWhitespace() && !remainingText[endOffset - 1].isWhitespace()) {
                            val lastSpace = remainingText.lastIndexOf(' ', endOffset)
                            if (lastSpace > 0) {
                                endOffset = lastSpace
                            }
                        }

                        if (endOffset <= 0 || endOffset >= remainingText.length) {
                            currentPage.add(originalIndex to block.copy(
                                text = remainingText,
                                sourceTextOffset = formattedText.length - remainingText.length,
                                subText = if (isContinuation) "continuation" else block.subText
                            ))
                            flushPage()
                            remainingText = ""
                        } else {
                            val chunk = remainingText.substring(0, endOffset).trim()
                            val nextChunk = remainingText.substring(endOffset).trim()

                            if (chunk.isNotEmpty()) {
                                currentPage.add(originalIndex to block.copy(
                                    text = chunk,
                                    sourceTextOffset = formattedText.length - remainingText.length,
                                    subText = if (isContinuation) "continuation" else block.subText
                                ))
                            }
                            flushPage()
                            remainingText = nextChunk
                            isContinuation = true
                        }
                    }
                }
            }
        }
    }

    flushPage()
    return if (pages.isEmpty()) listOf(emptyList()) else pages
}
