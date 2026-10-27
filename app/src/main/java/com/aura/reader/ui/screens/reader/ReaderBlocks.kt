package com.aura.reader.ui.screens.reader

import com.aura.reader.data.model.ReaderThemeMode
import com.aura.reader.data.model.BookLink
import com.aura.reader.data.model.Quote
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import com.aura.reader.ui.theme.LocalAppStrings
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.aura.reader.data.model.BlockType
import com.aura.reader.data.model.FormattedBlock
import com.aura.reader.data.model.ReaderSettings
import java.io.File
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

// ------------------------------------------------------------------------------------------------
// BLOCK RENDERER WITH SEARCH HIGHLIGHTING & FOOTNOTES
// ------------------------------------------------------------------------------------------------

@Composable
fun RenderBlock(
    block: FormattedBlock,
    settings: ReaderSettings,
    resolvedFontFamily: FontFamily,
    searchQuery: String,
    footnotes: Map<String, String>,
    quotes: List<Quote> = emptyList(),
    ttsWordStart: Int = -1,
    ttsWordEnd: Int = -1,
    ttsSpokenWord: String = "",
    onFootnoteClick: (ref: String, content: String) -> Unit,
    onNavigateToChapter: ((chapterIndex: Int, blockIndex: Int) -> Unit)? = null,
    onToggleControls: () -> Unit,
    onSaveQuote: (String) -> Unit
) {
    when (block.type) {
        BlockType.TITLE -> {
            val titleStyle = TextStyle(
                fontSize = (settings.fontSizeSp * 1.35f).sp,
                lineHeight = (settings.fontSizeSp * settings.lineHeightMultiplier * 1.25f).sp,
                fontFamily = resolvedFontFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            if (block.links.isNotEmpty()) {
                InteractiveText(
                    rawText = block.text,
                    style = titleStyle,
                    searchQuery = searchQuery,
                    footnotes = footnotes,
                    quotes = quotes,
                    links = block.links,
                    ttsWordStart = ttsWordStart,
                    ttsWordEnd = ttsWordEnd,
                    ttsSpokenWord = ttsSpokenWord,
                    onFootnoteClick = onFootnoteClick,
                    onNavigateToChapter = onNavigateToChapter,
                    onToggleControls = onToggleControls,
                    onSaveQuote = onSaveQuote,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 14.dp)
                )
            } else {
                Text(
                    text = block.text,
                    style = titleStyle,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 14.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onToggleControls() }
                )
            }
        }
        BlockType.SUBTITLE -> {
            val subtitleStyle = TextStyle(
                fontSize = (settings.fontSizeSp * 1.15f).sp,
                lineHeight = (settings.fontSizeSp * settings.lineHeightMultiplier * 1.15f).sp,
                fontFamily = resolvedFontFamily,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            if (block.links.isNotEmpty()) {
                InteractiveText(
                    rawText = block.text,
                    style = subtitleStyle,
                    searchQuery = searchQuery,
                    footnotes = footnotes,
                    quotes = quotes,
                    links = block.links,
                    ttsWordStart = ttsWordStart,
                    ttsWordEnd = ttsWordEnd,
                    ttsSpokenWord = ttsSpokenWord,
                    onFootnoteClick = onFootnoteClick,
                    onNavigateToChapter = onNavigateToChapter,
                    onToggleControls = onToggleControls,
                    onSaveQuote = onSaveQuote,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                )
            } else {
                Text(
                    text = block.text,
                    style = subtitleStyle,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onToggleControls() }
                )
            }
        }
        BlockType.EPIGRAPH -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 36.dp, end = 8.dp, top = 4.dp, bottom = 12.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = block.text,
                    fontSize = (settings.fontSizeSp * 0.92f).sp,
                    lineHeight = (settings.fontSizeSp * settings.lineHeightMultiplier).sp,
                    fontFamily = resolvedFontFamily,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.End,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f)
                )
                if (!block.subText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = block.subText,
                        fontSize = (settings.fontSizeSp * 0.82f).sp,
                        fontFamily = resolvedFontFamily,
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
                    )
                }
            }
        }
        BlockType.VERSE -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 28.dp, end = 16.dp, top = 6.dp, bottom = 12.dp)
            ) {
                Text(
                    text = block.text,
                    fontSize = (settings.fontSizeSp * 0.95f).sp,
                    lineHeight = (settings.fontSizeSp * settings.lineHeightMultiplier * 0.95f).sp,
                    fontFamily = resolvedFontFamily,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
        BlockType.DIVIDER -> {
            Spacer(modifier = Modifier.height(16.dp))
        }
        BlockType.IMAGE -> {
            val context = LocalContext.current
            var imageModel by remember(block.text) {
                mutableStateOf<Any?>(if (block.text.startsWith("aura-pdf:")) null else File(block.text))
            }
            LaunchedEffect(block.text) {
                if (block.text.startsWith("aura-pdf:")) {
                    imageModel = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        try { com.aura.reader.data.parser.PdfPageCache.render(context, block.text) }
                        catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
                        catch (_: Exception) { null }
                    }
                }
            }
            val isComicOrPdf = block.subText?.contains(" / ") == true || block.subText?.startsWith("Страница ") == true
            if (isComicOrPdf) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onToggleControls() },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageModel)
                            .crossfade(true)
                            .build(),
                        contentDescription = block.subText,
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                    )
                }
            } else {
                val isSystemDark = isSystemInDarkTheme()
                val isDark = settings.themeMode == com.aura.reader.data.model.ReaderThemeMode.DARK ||
                        settings.themeMode == com.aura.reader.data.model.ReaderThemeMode.AMOLED ||
                        (settings.themeMode == com.aura.reader.data.model.ReaderThemeMode.SYSTEM_DYNAMIC && isSystemDark)
                val shouldApplyLightCard = settings.lightImageBackground && isDark
                val imageCardBg = if (shouldApplyLightCard) {
                    Color(0xFFF5F4F0)
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLow
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onToggleControls() },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = imageCardBg
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (shouldApplyLightCard) 3.dp else 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(if (shouldApplyLightCard) 12.dp else 0.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(imageModel)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = block.subText,
                                contentScale = ContentScale.FillWidth,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(if (shouldApplyLightCard) 8.dp else 16.dp))
                                    .then(
                                        if (shouldApplyLightCard) {
                                            Modifier.background(Color.White)
                                        } else Modifier
                                    )
                            )
                        }
                    }
                    if (!block.subText.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = block.subText,
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
        BlockType.PARAGRAPH -> {
            val isContinuation = block.subText == "continuation"
            val paragraphStyle = TextStyle(
                fontSize = settings.fontSizeSp.sp,
                lineHeight = (settings.fontSizeSp * settings.lineHeightMultiplier).sp,
                fontFamily = resolvedFontFamily,
                color = MaterialTheme.colorScheme.onBackground,
                textIndent = if (isContinuation) TextIndent.None else TextIndent(firstLine = (settings.fontSizeSp * 1.2f).sp),
                hyphens = if (settings.autoHyphenation) Hyphens.Auto else Hyphens.None,
                lineBreak = if (settings.autoHyphenation) LineBreak.Paragraph else LineBreak.Simple
            )

            val formattedText = remember(block.text) { formatTypography(block.text.trim()) }

            InteractiveText(
                rawText = formattedText,
                style = paragraphStyle,
                searchQuery = searchQuery,
                footnotes = footnotes,
                quotes = quotes,
                links = block.links,
                ttsWordStart = ttsWordStart,
                ttsWordEnd = ttsWordEnd,
                ttsSpokenWord = ttsSpokenWord,
                onFootnoteClick = onFootnoteClick,
                onNavigateToChapter = onNavigateToChapter,
                onToggleControls = onToggleControls,
                onSaveQuote = onSaveQuote,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

// ------------------------------------------------------------------------------------------------
// INTERACTIVE TEXT (FOOTNOTES & SEARCH HIGHLIGHT)
// ------------------------------------------------------------------------------------------------

@Composable
fun InteractiveText(
    rawText: String,
    style: TextStyle,
    searchQuery: String,
    footnotes: Map<String, String>,
    quotes: List<Quote> = emptyList(),
    links: List<BookLink> = emptyList(),
    ttsWordStart: Int = -1,
    ttsWordEnd: Int = -1,
    ttsSpokenWord: String = "",
    onFootnoteClick: (ref: String, content: String) -> Unit,
    onNavigateToChapter: ((chapterIndex: Int, blockIndex: Int) -> Unit)? = null,
    onToggleControls: () -> Unit,
    onSaveQuote: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val annotated = remember(rawText, searchQuery, footnotes, quotes, links, primaryColor, ttsWordStart, ttsWordEnd, ttsSpokenWord) {
        buildAnnotatedString {
            append(rawText)

            // Highlight real-time spoken TTS word
            if (ttsWordStart >= 0 || ttsSpokenWord.isNotBlank()) {
                var hlStart = -1
                var hlEnd = -1

                // 1. Direct offset match check (fastest and most accurate)
                if (ttsWordStart in 0 until rawText.length) {
                    val candidateEnd = if (ttsWordEnd in (ttsWordStart + 1)..rawText.length) ttsWordEnd else {
                        var spaceIdx = -1
                        for (i in ttsWordStart until rawText.length) {
                            if (rawText[i].isWhitespace()) {
                                spaceIdx = i
                                break
                            }
                        }
                        if (spaceIdx != -1) spaceIdx else rawText.length
                    }
                    val candidateWord = rawText.substring(ttsWordStart, candidateEnd).trim { it <= ' ' || it in "\"'«»“”„“.,!?:;()[]{}–—-" }
                    val cleanSpoken = ttsSpokenWord.trim { it <= ' ' || it in "\"'«»“”„“.,!?:;()[]{}–—-" }
                    if (candidateWord.isNotBlank() && cleanSpoken.isNotBlank()) {
                        if (candidateWord.equals(cleanSpoken, ignoreCase = true) ||
                            (candidateWord.length >= 3 && cleanSpoken.length >= 3 &&
                             (candidateWord.startsWith(cleanSpoken, ignoreCase = true) || cleanSpoken.startsWith(candidateWord, ignoreCase = true)))) {
                            hlStart = ttsWordStart
                            hlEnd = candidateEnd
                        }
                    } else if (candidateWord.isNotBlank() && cleanSpoken.isBlank()) {
                        hlStart = ttsWordStart
                        hlEnd = candidateEnd
                    }
                }

                // 2. Proximity search if typography or accents caused slight character index drift
                if (hlStart == -1 && ttsSpokenWord.isNotBlank()) {
                    val cleanSpoken = ttsSpokenWord.trim { it <= ' ' || it in "\"'«»“”„“.,!?:;()[]{}–—-" }
                    if (cleanSpoken.length >= 3) {
                        val windowStart = if (ttsWordStart >= 0) maxOf(0, ttsWordStart - 6) else 0
                        val windowEnd = if (ttsWordStart >= 0) minOf(rawText.length, ttsWordStart + cleanSpoken.length + 6) else rawText.length
                        val windowText = rawText.substring(windowStart, windowEnd)
                        val relIdx = windowText.indexOf(cleanSpoken, ignoreCase = true)
                        if (relIdx != -1) {
                            hlStart = windowStart + relIdx
                            hlEnd = hlStart + cleanSpoken.length
                        }
                    }
                }

                // 3. Fallback to reported start/end indices
                if (hlStart == -1 && ttsWordStart in 0 until rawText.length) {
                    hlStart = ttsWordStart
                    hlEnd = if (ttsWordEnd in (ttsWordStart + 1)..rawText.length) ttsWordEnd else {
                        var spaceIdx = -1
                        for (i in ttsWordStart until rawText.length) {
                            if (rawText[i].isWhitespace()) {
                                spaceIdx = i
                                break
                            }
                        }
                        if (spaceIdx != -1) spaceIdx else rawText.length
                    }
                }

                if (hlStart in 0 until rawText.length && hlEnd > hlStart) {
                    addStyle(
                        style = SpanStyle(
                            background = primaryColor.copy(alpha = 0.28f),
                            color = primaryColor,
                            fontWeight = FontWeight.Bold
                        ),
                        start = hlStart,
                        end = minOf(hlEnd, rawText.length)
                    )
                }
            }

            // Highlight saved quotes
            for (quote in quotes) {
                val qText = quote.text.trim()
                if (qText.length >= 2) {
                    var start = 0
                    while (start < rawText.length) {
                        val idx = rawText.indexOf(qText, start, ignoreCase = true)
                        if (idx == -1) break
                        addStyle(
                            style = SpanStyle(
                                background = Color(quote.color).copy(alpha = 0.38f)
                            ),
                            start = idx,
                            end = idx + qText.length
                        )
                        start = idx + qText.length
                    }
                }
            }

            // Highlight in-book search results
            val q = searchQuery.trim()
            if (q.length >= 2) {
                var start = 0
                while (start < rawText.length) {
                    val idx = rawText.indexOf(q, start, ignoreCase = true)
                    if (idx == -1) break
                    addStyle(
                        style = SpanStyle(
                            background = Color(0xFFFFD54F),
                            color = Color(0xFF1A1A1A),
                            fontWeight = FontWeight.Bold
                        ),
                        start = idx,
                        end = idx + q.length
                    )
                    start = idx + q.length
                }
            }

            // 1. Explicit internal links and TOC references from book parser
            for (bookLink in links) {
                val s = bookLink.start.coerceIn(0, rawText.length)
                val e = bookLink.end.coerceIn(s, rawText.length)
                if (s < e) {
                    val target = bookLink.target
                    val isExternal = target.startsWith("http://") || target.startsWith("https://") || target.startsWith("mailto:")
                    val isNav = target.startsWith("nav:") || footnotes[target]?.startsWith("nav:") == true

                    val linkColor = if (isNav) primaryColor else if (isExternal) Color(0xFF1976D2) else Color(0xFF2E7D32)
                    addStyle(
                        style = SpanStyle(
                            color = linkColor,
                            textDecoration = TextDecoration.Underline,
                            fontWeight = FontWeight.SemiBold
                        ),
                        start = s,
                        end = e
                    )
                    if (isExternal) {
                        addLink(LinkAnnotation.Url(url = target), s, e)
                    } else {
                        val link = LinkAnnotation.Clickable(
                            tag = target,
                            linkInteractionListener = {
                                val resolvedNav = if (target.startsWith("nav:")) target else footnotes[target]
                                if (resolvedNav != null && resolvedNav.startsWith("nav:")) {
                                    val parts = resolvedNav.removePrefix("nav:").split(":")
                                    val ch = parts.getOrNull(0)?.toIntOrNull() ?: 0
                                    val blk = parts.getOrNull(1)?.toIntOrNull() ?: 0
                                    if (onNavigateToChapter != null) {
                                        onNavigateToChapter(ch, blk)
                                    } else {
                                        onFootnoteClick(target, resolvedNav)
                                    }
                                } else {
                                    val linkText = rawText.substring(s, e)
                                    val content = resolveFootnoteText(target, footnotes)
                                        ?: resolveFootnoteText(linkText, footnotes)
                                        ?: footnotes[target]
                                        ?: "${strings.footnoteTitle}: $linkText"
                                    onFootnoteClick(target, content)
                                }
                            }
                        )
                        addLink(link, s, e)
                    }
                }
            }

            // 2. Highlight and handle web URL links (e.g. https://... or http://...)
            val urlRegex = Regex("""https?://[^\s<>"'{}|\\^`]+""")
            for (match in urlRegex.findAll(rawText)) {
                if (links.any { it.start <= match.range.first && it.end >= match.range.last + 1 }) continue
                val url = match.value
                addStyle(
                    style = SpanStyle(
                        color = Color(0xFF1976D2),
                        textDecoration = TextDecoration.Underline
                    ),
                    start = match.range.first,
                    end = match.range.last + 1
                )
                val link = LinkAnnotation.Url(url = url)
                addLink(link, match.range.first, match.range.last + 1)
            }

            // 3. Highlight footnote links e.g. [1], [note], {1}, ¹⁴, »14
            val footnoteRegex = Regex("""\[([a-zA-Z0-9а-яА-ЯёЁ_\s-]{1,25})\]|\{([0-9]+)\}|([¹²³⁴⁵⁶⁷⁸⁹⁰]+)|»\s*(\d{1,4})""")
            for (match in footnoteRegex.findAll(rawText)) {
                if (links.any { it.start <= match.range.first && it.end >= match.range.last + 1 }) continue
                val matchValue = match.value
                val ref = matchValue.removePrefix("»").trim()
                val resolved = resolveFootnoteText(ref, footnotes)
                val isExplicitRef = matchValue.startsWith("[") || matchValue.startsWith("{") || matchValue.any { it in "¹²³⁴⁵⁶⁷⁸⁹⁰" }
                if (resolved != null || (footnotes.isNotEmpty() && isExplicitRef)) {
                    val isNav = resolved?.startsWith("nav:") == true
                    addStyle(
                        style = SpanStyle(
                            color = if (isNav) primaryColor else Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (isNav) TextDecoration.Underline else TextDecoration.None
                        ),
                        start = match.range.first,
                        end = match.range.last + 1
                    )
                    val link = LinkAnnotation.Clickable(
                        tag = ref,
                        linkInteractionListener = {
                            val content = resolveFootnoteText(ref, footnotes) ?: footnotes[ref] ?: "${strings.footnoteTitle}: $ref"
                            if (content.startsWith("nav:")) {
                                val parts = content.removePrefix("nav:").split(":")
                                val ch = parts.getOrNull(0)?.toIntOrNull() ?: 0
                                val blk = parts.getOrNull(1)?.toIntOrNull() ?: 0
                                if (onNavigateToChapter != null) {
                                    onNavigateToChapter(ch, blk)
                                } else {
                                    onFootnoteClick(ref, content)
                                }
                            } else {
                                onFootnoteClick(ref, content)
                            }
                        }
                    )
                    addLink(link, match.range.first, match.range.last + 1)
                }
            }
        }
    }

    Text(
        text = annotated,
        style = style,
        modifier = modifier
    )
}

fun resolveFootnoteText(ref: String, footnotes: Map<String, String>): String? {
    if (footnotes.isEmpty()) return null
    val clean = ref.trim()
        .removePrefix("»").trim()
        .removePrefix("[").removeSuffix("]")
        .removePrefix("{").removeSuffix("}")
        .trim()

    val asciiClean = clean.map { char ->
        when (char) {
            '⁰' -> '0'; '¹' -> '1'; '²' -> '2'; '³' -> '3'; '⁴' -> '4'
            '⁵' -> '5'; '⁶' -> '6'; '⁷' -> '7'; '⁸' -> '8'; '⁹' -> '9'
            else -> char
        }
    }.joinToString("")

    val direct = footnotes[ref]
        ?: footnotes[clean]
        ?: footnotes[asciiClean]
        ?: footnotes["[$clean]"]
        ?: footnotes["[$asciiClean]"]
        ?: footnotes["{$clean}"]
        ?: footnotes["{$asciiClean}"]
        ?: footnotes["#$clean"]
        ?: footnotes["#$asciiClean"]
        ?: footnotes["n$clean"]
        ?: footnotes["n$asciiClean"]
        ?: footnotes["n_$clean"]
        ?: footnotes["n_$asciiClean"]
        ?: footnotes["fn$clean"]
        ?: footnotes["fn$asciiClean"]
        ?: footnotes["note$clean"]
        ?: footnotes["note$asciiClean"]
        ?: footnotes["note_$clean"]
        ?: footnotes["note_$asciiClean"]

    if (direct != null && footnotes.containsKey(direct) && footnotes[direct] != direct) {
        return footnotes[direct]
    }
    if (direct != null) return direct

    return footnotes.entries.firstOrNull {
        it.key.equals(ref, ignoreCase = true) ||
        it.key.equals(clean, ignoreCase = true) ||
        it.key.equals(asciiClean, ignoreCase = true) ||
        it.key.contains(clean, ignoreCase = true) ||
        it.key.contains(asciiClean, ignoreCase = true)
    }?.value
}


// ------------------------------------------------------------------------------------------------
// TYPOGRAPHY FORMATTING HELPER
// ------------------------------------------------------------------------------------------------

fun formatTypography(text: String): String {
    if (text.isEmpty()) return text
    var result = text
    // Dialogue dashes at line start: "- " or "-- " or "— " -> "—\u00A0"
    result = result.replace(Regex("(^|(?<=\\n))[-—–]\\s+"), "—\u00A0")
    // Sentence dashes inside text: attach to previous word with non-breaking space
    result = result.replace(Regex("(?<=\\S)\\s+[-—–]\\s+"), "\u00A0— ")
    return result
}
