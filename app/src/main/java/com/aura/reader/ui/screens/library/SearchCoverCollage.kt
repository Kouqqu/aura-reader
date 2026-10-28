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
 * 1. If library has >= 12 books, ALL 12 collage slots get unique books (zero duplicates).
 * 2. If library has < 12 books, all available books are shown before repeating.
 * 3. Never allows vertical collisions (identical books directly above/below each other).
 * 4. Never allows adjacent horizontal collisions.
 */
internal fun selectCollageCovers(books: List<Book>): List<Book> {
    val withCovers = books.filter { !it.coverBase64.isNullOrBlank() }
    if (withCovers.isEmpty()) return emptyList()

    val totalSlots = 12
    val slots = listOf(1, 2, 5, 10, 6, 9, 3, 7, 0, 4, 8, 11)
    val L = withCovers.size
    val shuffled = withCovers.shuffled()

    // Rule 1: Plenty of books -> strictly 12 distinct covers
    if (L >= totalSlots) {
        val picked = shuffled.take(totalSlots)
        return List(totalSlots) { picked[it] }
    }

    // Only one book
    if (L == 1) {
        return List(totalSlots) { shuffled[0] }
    }

    // Rule 2: Exhaustive pool before repeating
    val pool = ArrayList<Book>(totalSlots)
    while (pool.size < totalSlots) {
        pool.addAll(shuffled)
    }
    val candidatePool = pool.take(totalSlots).toMutableList()

    val cells = listOf(
        0 to 0, 0 to 1, 0 to 2, 0 to 3,
        1 to 0, 1 to 1, 1 to 2, 1 to 3,
        2 to 0, 2 to 1, 2 to 2, 2 to 3
    )

    var bestGrid: Map<Pair<Int, Int>, Book>? = null
    for (attempt in 0..120) {
        val cand = candidatePool.shuffled().toMutableList()
        val grid = mutableMapOf<Pair<Int, Int>, Book>()
        var success = true
        for ((r, c) in cells) {
            val valid = cand.firstOrNull { book ->
                val topMatch = r > 0 && grid[r - 1 to c]?.id == book.id
                val leftMatch = c > 0 && grid[r to c - 1]?.id == book.id
                val wrapLeftMatch = c == 3 && L >= 2 && grid[r to 0]?.id == book.id
                val wrapTopMatch = r == 2 && L >= 3 && grid[0 to c]?.id == book.id
                !topMatch && !leftMatch && !wrapLeftMatch && !wrapTopMatch
            }
            if (valid != null) {
                grid[r to c] = valid
                cand.remove(valid)
            } else {
                success = false
                break
            }
        }
        if (success) {
            bestGrid = grid
            break
        }
    }

    val grid = bestGrid ?: run {
        // Deterministic checkerboard fallback for extreme small L
        val fallback = mutableMapOf<Pair<Int, Int>, Book>()
        for ((r, c) in cells) {
            fallback[r to c] = shuffled[(r + c) % L]
        }
        fallback
    }

    return List(totalSlots) { index ->
        val slot = slots[index]
        val r = slot / 4
        val c = slot % 4
        grid[r to c] ?: shuffled[index % L]
    }
}

/** Decorative only: bounded decoding, no shared-element keys or touch targets. */
@Composable
fun SearchCoverCollage(books: List<Book>, subdued: Boolean, modifier: Modifier = Modifier) {
    val covers = remember(books) { selectCollageCovers(books) }
    var images by remember { mutableStateOf<List<ImageBitmap>>(emptyList()) }
    LaunchedEffect(covers) {
        images = withContext(Dispatchers.Default) {
            val uniqueBitmaps = covers.distinctBy { it.id }.associate { book ->
                book.id to CoverBitmapCache.getOrDecode("collage:${book.id}:${book.coverBase64.hashCode()}", book.coverBase64!!, 360, 540)
            }
            covers.mapNotNull { book -> uniqueBitmaps[book.id] }
        }
    }
    val background = MaterialTheme.colorScheme.background
    val accent = MaterialTheme.colorScheme.primaryContainer
    val lifecycle by LocalLifecycleOwner.current.lifecycle.currentStateFlow.collectAsState()
    val active = lifecycle.isAtLeast(Lifecycle.State.STARTED) && !subdued && images.isNotEmpty()
    val motion = remember { Animatable(0f) }
    val opacity by animateFloatAsState(if (subdued) 0.16f else 1f, tween(350), label = "collageOpacity")
    val palette = remember(images, accent) {
        images.firstOrNull()?.let { image ->
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
        val count = images.size
        val layout = remember(count, width, height) { coverCollageLayout(count, width, height, 0f) }
        val movingLayout = remember(count, width, height) {
            derivedStateOf { coverCollageLayout(count, width, height, motion.value) }
        }
        LaunchedEffect(active, layout.durationMillis) {
            if (active) motion.animateTo(motion.value + 1f,
                infiniteRepeatable(tween(layout.durationMillis, easing = LinearEasing)))
        }
        images.forEachIndexed { index, bitmap ->
            val coverWidth = layout.coverWidth.dp
            val coverHeight = coverWidth * (bitmap.height.toFloat() / bitmap.width).coerceIn(1.1f, 1.8f)
            Image(bitmap, contentDescription = null, contentScale = ContentScale.Fit,
                modifier = Modifier.testTag("collageCover-" + index).size(coverWidth, coverHeight).graphicsLayer {
                    val position = movingLayout.value.positions[index]
                    translationX = position.x.dp.toPx() - coverWidth.toPx() / 2
                    translationY = position.y.dp.toPx() - coverHeight.toPx() / 2
                    rotationZ = -23f
                    alpha = opacity
                    shape = RoundedCornerShape(14.dp)
                    clip = true
                })
        }
    }
}
