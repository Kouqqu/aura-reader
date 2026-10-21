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
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.cos

/** Decorative only: bounded decoding, no shared-element keys or touch targets. */
@Composable
fun SearchCoverCollage(books: List<Book>, subdued: Boolean, modifier: Modifier = Modifier) {
    val covers = remember(books) {
        books.asSequence().filter { !it.coverBase64.isNullOrBlank() }.take(8).toList()
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
    // Sine/cosine meet seamlessly at the loop boundary. The system animation
    // duration scale is respected. No animation runs offscreen or while typing.
    val motion = remember { Animatable(0f) }
    LaunchedEffect(active) {
        if (active) motion.animateTo(motion.value + (2 * PI).toFloat(),
            infiniteRepeatable(tween(48000, easing = LinearEasing)))
    }
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
        val width = maxWidth
        val height = maxHeight
        images.forEachIndexed { index, bitmap ->
            // Few covers get a spacious composition, never duplicate images.
            val count = images.size
            val x: Float
            val y: Float
            val coverWidth = width * if (count < 4) 0.54f else 0.41f
            if (count == 1) {
                x = 0.66f; y = 0.28f
            } else if (count < 4) {
                x = if (count == 2) 0.18f + index * 0.6f else index * 0.5f
                y = if (index % 2 == 0) 0.2f else 0.53f
            } else {
                x = -0.02f + (index % 3) * 0.45f + (index / 3 % 2) * 0.1f
                y = -0.1f + (index / 3) * 0.63f
            }
            val coverHeight = coverWidth * (bitmap.height.toFloat() / bitmap.width).coerceIn(1.1f, 1.8f)
            Image(bitmap, contentDescription = null, contentScale = ContentScale.Fit,
                modifier = Modifier.testTag("collageCover-" + index).size(coverWidth, coverHeight).graphicsLayer {
                    val t = motion.value + index * 0.82f
                    translationX = (width * x - coverWidth / 2).toPx() + 20.dp.toPx() * sin(t)
                    translationY = (height * y - coverHeight / 2).toPx() + 16.dp.toPx() * cos(t)
                    rotationZ = -23f + 2f * sin(t)
                    alpha = opacity
                    shape = RoundedCornerShape(14.dp)
                    clip = true
                })
        }
    }
}
