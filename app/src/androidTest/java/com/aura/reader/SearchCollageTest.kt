package com.aura.reader

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.Base64
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.aura.reader.data.model.Book
import com.aura.reader.data.model.BookFormat
import com.aura.reader.ui.screens.library.SearchCoverCollage
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream
import java.io.File

@RunWith(AndroidJUnit4::class)
class SearchCollageTest {
    @get:Rule val compose = createComposeRule()

    private fun books(count: Int): List<Book> = List(count) { index ->
        val bitmap = Bitmap.createBitmap(180, 270, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawColor(intArrayOf(0xff315c75.toInt(), 0xffc06345.toInt(), 0xff656441.toInt(), 0xff7b4268.toInt())[index % 4])
        paint.color = android.graphics.Color.WHITE
        paint.textSize = 25f
        canvas.drawText("AURA", 16f, 55f, paint)
        canvas.drawText("BOOK ${index + 1}", 16f, 92f, paint)
        paint.strokeWidth = 3f
        canvas.drawLine(18f, 200f, 160f, 120f, paint)
        val bytes = ByteArrayOutputStream().apply { bitmap.compress(Bitmap.CompressFormat.PNG, 100, this) }.toByteArray()
        bitmap.recycle()
        Book("fixture-$index", "Book $index", coverBase64 = Base64.encodeToString(bytes, Base64.NO_WRAP), format = BookFormat.EPUB, uriString = "fixture:$index")
    }

    @Test fun sparseAndFullLibrariesRenderWithoutRepeatingCoversAndAnimate() {
        var library by mutableStateOf(books(2))
        var subdued by mutableStateOf(false)
        var dark by mutableStateOf(false)
        compose.mainClock.autoAdvance = false
        compose.setContent {
            MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
                SearchCoverCollage(library, subdued, Modifier.width(380.dp).height(300.dp).testTag("hero"))
            }
        }
        compose.waitUntil(10000) { compose.mainClock.advanceTimeByFrame(); compose.onAllNodesWithTag("collageCover-1").fetchSemanticsNodes().size == 1 }
        compose.mainClock.autoAdvance = false
        compose.mainClock.advanceTimeBy(500)
        val first = compose.onNodeWithTag("hero").captureToImage()
        compose.mainClock.advanceTimeBy(6000)
        val moved = compose.onNodeWithTag("hero").captureToImage()
        val a = first.toPixelMap(); val b = moved.toPixelMap()
        var differences = 0
        for (y in 0 until first.height step 8) for (x in 0 until first.width step 8) {
            if (a[x,y] != b[x,y]) differences++
        }
        assertTrue("Cover collage must move", differences > 100)
        save("collage-two-light.png")
        compose.runOnIdle { subdued = true }
        compose.mainClock.advanceTimeBy(500)
        val stopped = compose.onNodeWithTag("hero").captureToImage()
        compose.mainClock.advanceTimeBy(3000)
        val still = compose.onNodeWithTag("hero").captureToImage()
        assertTrue("Typing pauses motion", stopped.asAndroidBitmap().sameAs(still.asAndroidBitmap()))
        for (count in listOf(1, 8, 0)) {
            compose.runOnIdle { library = books(count); subdued = false; dark = count == 8 }
            compose.waitUntil(10000) { compose.mainClock.advanceTimeByFrame(); (0..8).count { compose.onAllNodesWithTag("collageCover-" + it).fetchSemanticsNodes().isNotEmpty() } == count }
            compose.mainClock.advanceTimeBy(500)
            if (count > 0) compose.onNodeWithTag("collageCover-$count").assertDoesNotExist()
            save("collage-$count.png")
        }
    }

    @Test fun prepareSearchPreviewLibrary() {
        // Only used in the separate verification application for manual visual QA.
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val preferences = com.aura.reader.data.preferences.PreferencesManager(context)
        val array = org.json.JSONArray()
        books(8).forEach { book -> array.put(org.json.JSONObject().apply {
            put("id", book.id); put("title", book.title); put("author", "Aura preview")
            put("coverBase64", book.coverBase64); put("format", "EPUB"); put("uriString", book.uriString)
        }) }
        kotlinx.coroutines.runBlocking { preferences.saveRecentBooksJson(array.toString()) }
    }

    private fun save(name: String) {
        val image = compose.onNodeWithTag("hero").captureToImage().asAndroidBitmap()
        val directory = InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null)!!
        File(directory, name).outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
