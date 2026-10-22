package com.aura.reader.ui.screens.library

import kotlin.math.*

internal data class CoverPlacement(val x: Float, val y: Float)
internal data class CoverLayout(val coverWidth: Float, val maxCoverHeight: Float, val durationMillis: Int, val positions: List<CoverPlacement>)

/** Positions in dp. A rigid translation preserves the gaps between rotated books. */
internal fun coverCollageLayout(count: Int, width: Float, height: Float, phase: Float): CoverLayout {
    val angle = Math.toRadians(-23.0)
    val c = cos(angle).toFloat()
    val s = sin(angle).toFloat()
    val gap = 24f
    val coverWidth = width * if (count < 4) 0.4f else 0.42f
    val coverHeight = coverWidth * 1.8f
    fun wrap(value: Float, period: Float): Float = ((value + period / 2) % period + period) % period - period / 2
    if (count < 4) {
        // Sparse libraries form a single diagonal stream. The wrap happens only
        // once the full rotated cover has left the viewport.
        val slope = 0.7f
        val localDx = c + s * slope
        val localDy = -s + c * slope
        val spacing = min((coverWidth + gap) / localDx, (coverHeight + gap) / localDy)
        val boundsWidth = c * coverWidth - s * coverHeight
        val boundsHeight = -s * coverWidth + c * coverHeight
        val exit = min((width + boundsWidth) / 2, (height + boundsHeight) / (2 * slope))
        val period = max(2 * exit + gap * 2, count * spacing).coerceAtLeast(1f)
        val stepSpacing = period / count
        return CoverLayout(coverWidth, coverHeight, (period / 7f * 1000).toInt(), List(count) { index ->
            val t = wrap(index * stepSpacing + phase * period, period)
            CoverPlacement(width / 2 + t, height / 2 + slope * t)
        })
    }
    // An oversized grid in the books' rotated coordinate system. All cells
    // translate together, then wrap beyond the viewport including cover extents.
    val cellWidth = coverWidth + gap
    val cellHeight = coverHeight + gap
    val columns = max(4, ceil((c * width - s * height + coverWidth + 2 * gap) / cellWidth).toInt())
    val rows = max(3, ceil((-s * width + c * height + coverHeight + 2 * gap) / cellHeight).toInt())
    val periodU = columns * cellWidth
    val periodV = rows * cellHeight
    // Maximally distributed diagonal wavefront: guarantees at least 2 covers are visible at all times
    val slots = listOf(0, 5, 6, 11, 1, 2, 3, 4)
    val speedX = c * periodU - s * periodV
    return CoverLayout(coverWidth, coverHeight, (speedX / 7f * 1000).toInt(), List(count.coerceAtMost(8)) { index ->
        val slot = slots[index]
        val u = wrap((slot % 4 - 1.5f) * cellWidth + phase * periodU, periodU)
        val v = wrap((slot / 4 - 1f) * cellHeight + phase * periodV, periodV)
        CoverPlacement(width / 2 + c * u - s * v, height / 2 + s * u + c * v)
    })
}
