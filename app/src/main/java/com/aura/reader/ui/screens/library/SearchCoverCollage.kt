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

/** Decorative only: bounded decoding, no shared-element keys or touch targets. */
@Composable
fun SearchCoverCollage(books: List<Book>, subdued: Boolean, modifier: Modifier = Modifier) {
    val covers = remember(books) {
        val withCovers = books.filter { !it.coverBase64.isNullOrBlank() }
        if (withCovers.size > 8) {
            withCovers.shuffled(java.util.Random(withCovers.hashCode().toLong())).take(8)
        } else {
            withCovers
        }
    }
    var images by remember { mutableStateOf<List<ImageBitmap>>(emptyList()) }
    LaunchedEffect(covers) {
        images = withContext(Dispatchers.Default) {
            covers.mapNotNull { book ->
                CoverBitmapCache.getOrDecode("collage:${book.id}:${book.coverBase64.hashCode()}", book.coverBase64!!, 360, 540)
            }
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
