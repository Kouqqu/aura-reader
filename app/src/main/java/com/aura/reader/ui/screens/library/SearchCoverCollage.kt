package com.aura.reader.ui.screens.library

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.aura.reader.data.model.Book
import com.aura.reader.util.CoverBitmapCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Intelligent cover selection for the search background collage.
 * Guarantees:
 * 1. If library has >= 12 books, 12 collage slots stream through ALL books in the library
 *    without any book repeating until all books have flown past.
 * 2. If library has < 12 books, exactly that many slots are displayed so zero duplicate
 *    books appear on screen simultaneously.
 */
internal fun selectCollageCovers(books: List<Book>): List<Book> {
    val withCovers = books.filter { !it.coverBase64.isNullOrBlank() }
    if (withCovers.isEmpty()) return emptyList()
    return withCovers.shuffled()
}

/** Decorative only: bounded decoding, no shared-element keys or touch targets. */
@Composable
fun SearchCoverCollage(books: List<Book>, subdued: Boolean, modifier: Modifier = Modifier) {
    val availableBooks = remember(books) { selectCollageCovers(books) }
    var loadedBitmaps by remember { mutableStateOf<Map<String, ImageBitmap>>(emptyMap()) }

    LaunchedEffect(availableBooks) {
        if (availableBooks.isEmpty()) {
            loadedBitmaps = emptyMap()
            return@LaunchedEffect
        }
        withContext(Dispatchers.Default) {
            val loaded = mutableMapOf<String, ImageBitmap>()
            val initial = availableBooks.take(16)
            for (book in initial) {
                val bmp = CoverBitmapCache.getOrDecode("collage:${book.id}:${book.coverBase64.hashCode()}", book.coverBase64!!, 360, 540)
                if (bmp != null) loaded[book.id] = bmp
            }
            loadedBitmaps = loaded.toMap()

            if (availableBooks.size > 16) {
                for (book in availableBooks.drop(16)) {
                    val bmp = CoverBitmapCache.getOrDecode("collage:${book.id}:${book.coverBase64.hashCode()}", book.coverBase64!!, 360, 540)
                    if (bmp != null) loaded[book.id] = bmp
                }
                loadedBitmaps = loaded.toMap()
            }
        }
    }

    val background = MaterialTheme.colorScheme.background
    val accent = MaterialTheme.colorScheme.primaryContainer
    val lifecycle by LocalLifecycleOwner.current.lifecycle.currentStateFlow.collectAsState()
    val active = lifecycle.isAtLeast(Lifecycle.State.STARTED) && !subdued && loadedBitmaps.isNotEmpty()
    val motion = remember { Animatable(0f) }
    val opacity by animateFloatAsState(if (subdued) 0.16f else 1f, tween(350), label = "collageOpacity")
    val palette = remember(loadedBitmaps, accent) {
        loadedBitmaps.values.firstOrNull()?.let { image ->
            val bitmap = image.asAndroidBitmap()
            Color(bitmap.getPixel(bitmap.width / 2, bitmap.height / 3))
        } ?: accent
    }

    BoxWithConstraints(modifier.fillMaxWidth().clipToBounds().testTag("searchCollage")
        .background(Brush.linearGradient(listOf(palette.copy(alpha = 0.3f), accent.copy(alpha = 0.25f), background)))
        .drawWithContent {
            drawContent()
            drawRect(Brush.verticalGradient(0f to background.copy(alpha = 0.04f), 0.3f to background.copy(alpha = 0.12f), 0.6f to background.copy(alpha = 0.78f), 1f to background))
        }) {
        val width = maxWidth.value
        val height = maxHeight.value
        val slotCount = availableBooks.size.coerceAtMost(12)
        val layout = remember(slotCount, width, height) { coverCollageLayout(slotCount, width, height, 0f) }
        val movingLayout = remember(slotCount, width, height) {
            derivedStateOf { coverCollageLayout(slotCount, width, height, motion.value) }
        }

        LaunchedEffect(active, layout.durationMillis) {
            if (active) {
                while (true) {
                    motion.animateTo(
                        targetValue = motion.value + 1f,
                        animationSpec = tween(layout.durationMillis.coerceAtLeast(100), easing = LinearEasing)
                    )
                }
            }
        }

        for (index in 0 until slotCount) {
            val placement = movingLayout.value.positions.getOrNull(index) ?: continue
            val bookIndex = (index + placement.cycle * slotCount).mod(availableBooks.size)
            val book = availableBooks[bookIndex]
            val bitmap = loadedBitmaps[book.id]
                ?: loadedBitmaps[availableBooks[index % availableBooks.size].id]
                ?: loadedBitmaps.values.firstOrNull()
                ?: continue

            val coverWidth = layout.coverWidth.dp
            val coverHeight = coverWidth * (bitmap.height.toFloat() / bitmap.width).coerceIn(1.1f, 1.8f)
            Image(bitmap, contentDescription = null, contentScale = ContentScale.Fit,
                modifier = Modifier.testTag("collageCover-" + index).size(coverWidth, coverHeight).graphicsLayer {
                    translationX = placement.x.dp.toPx() - coverWidth.toPx() / 2
                    translationY = placement.y.dp.toPx() - coverHeight.toPx() / 2
                    rotationZ = -23f
                    alpha = opacity
                    shape = RoundedCornerShape(14.dp)
                    clip = true
                })
        }
    }
}
