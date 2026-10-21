package com.aura.reader

import com.aura.reader.ui.screens.library.coverCollageLayout
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.*

class CoverCollageLayoutTest {
    private val c = cos(Math.toRadians(-23.0)).toFloat()
    private val s = sin(Math.toRadians(-23.0)).toFloat()

    @Test fun rotatedCoversKeepAtLeastTwentyFourDpGapThroughoutTheLoop() {
        for (width in listOf(280f, 400f, 800f)) for (count in 1..8) for (step in 0..200) {
            val layout = coverCollageLayout(count, width, 310f, step / 200f)
            for (i in layout.positions.indices) for (j in i + 1 until layout.positions.size) {
                val dx = layout.positions[i].x - layout.positions[j].x
                val dy = layout.positions[i].y - layout.positions[j].y
                val localX = abs(c * dx + s * dy)
                val localY = abs(-s * dx + c * dy)
                assertTrue("count=$count phase=$step width=$width pair=$i,$j", localX >= layout.coverWidth + 23.9f || localY >= layout.maxCoverHeight + 23.9f)
            }
        }
    }

    @Test fun visibleCoversMoveLinearlyRightAndDown() {
        for (count in 1..8) {
            val a = coverCollageLayout(count, 400f, 310f, 0.04f)
            val b = coverCollageLayout(count, 400f, 310f, 0.041f)
            val d = coverCollageLayout(count, 400f, 310f, 0.042f)
            for (i in a.positions.indices) {
                val dx = b.positions[i].x - a.positions[i].x
                val dy = b.positions[i].y - a.positions[i].y
                assertTrue(dx > 0f && dy > 0f)
                assertEquals(dx, d.positions[i].x - b.positions[i].x, 0.002f)
                assertEquals(dy, d.positions[i].y - b.positions[i].y, 0.002f)
            }
        }
    }

    @Test fun wrappingNeverTeleportsAVisibleCover() {
        for (width in listOf(280f, 400f, 800f)) for (count in 1..8) for (step in 0..1000) {
            val a = coverCollageLayout(count, width, 310f, step / 1000f)
            val b = coverCollageLayout(count, width, 310f, step / 1000f + 0.0001f)
            val halfWidth = (c * a.coverWidth - s * a.maxCoverHeight) / 2
            val halfHeight = (-s * a.coverWidth + c * a.maxCoverHeight) / 2
            fun visible(x: Float, y: Float) = x + halfWidth > 0 && x - halfWidth < width && y + halfHeight > 0 && y - halfHeight < 310f
            for (i in a.positions.indices) {
                if (abs(b.positions[i].x - a.positions[i].x) > 20f || abs(b.positions[i].y - a.positions[i].y) > 20f) {
                    assertFalse("visible before wrap", visible(a.positions[i].x, a.positions[i].y))
                    assertFalse("visible after wrap", visible(b.positions[i].x, b.positions[i].y))
                }
            }
        }
    }
}
