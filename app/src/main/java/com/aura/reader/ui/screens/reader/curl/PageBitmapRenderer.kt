package com.aura.reader.ui.screens.reader.curl

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.view.View
import com.aura.reader.data.model.BlockType
import com.aura.reader.data.model.FormattedBlock
import com.aura.reader.data.model.ReaderSettings

object PageBitmapRenderer {

    fun captureViewToBitmap(view: View): Bitmap? {
        if (view.width <= 0 || view.height <= 0) return null
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    fun renderPageToBitmap(
        width: Int,
        height: Int,
        pageBlocks: List<Pair<Int, FormattedBlock>>,
        settings: ReaderSettings,
        backgroundColor: Int,
        textColor: Int,
        topPaddingPx: Float,
        bottomPaddingPx: Float,
        horizontalPaddingPx: Float,
        typeface: Typeface? = null,
        chapterTitle: String? = null
    ): Bitmap {
        val w = width.coerceAtLeast(100)
        val h = height.coerceAtLeast(100)
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(backgroundColor)

        val usableWidth = (w - horizontalPaddingPx * 2).coerceAtLeast(80f).toInt()
        var currentY = topPaddingPx

        if (chapterTitle != null && pageBlocks.isEmpty()) {
            val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = textColor
                this.textSize = settings.fontSizeSp * 1.5f * (w / 380f).coerceIn(0.8f, 2.0f)
                this.typeface = Typeface.create(typeface ?: Typeface.SERIF, Typeface.BOLD)
            }
            val titleLayout = createStaticLayout(chapterTitle, titlePaint, usableWidth, Layout.Alignment.ALIGN_CENTER)
            canvas.save()
            canvas.translate(horizontalPaddingPx, h * 0.4f)
            titleLayout.draw(canvas)
            canvas.restore()
            return bitmap
        }

        val baseTextSize = settings.fontSizeSp * (w / 380f).coerceIn(0.75f, 1.5f)
        val lineSpacingMultiplier = settings.lineHeightMultiplier

        val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = textColor
            this.textSize = baseTextSize
            this.typeface = typeface ?: Typeface.SERIF
        }

        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = textColor
            this.textSize = baseTextSize * 1.35f
            this.typeface = Typeface.create(typeface ?: Typeface.SERIF, Typeface.BOLD)
        }

        val quotePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = (textColor and 0x00FFFFFF) or 0xCC000000.toInt()
            this.textSize = baseTextSize * 0.95f
            this.typeface = Typeface.create(typeface ?: Typeface.SERIF, Typeface.ITALIC)
        }

        for ((_, block) in pageBlocks) {
            val paint = when (block.type) {
                BlockType.TITLE, BlockType.SUBTITLE -> titlePaint
                BlockType.EPIGRAPH, BlockType.VERSE -> quotePaint
                else -> bodyPaint
            }
            val align = when (block.type) {
                BlockType.TITLE -> Layout.Alignment.ALIGN_CENTER
                BlockType.EPIGRAPH -> Layout.Alignment.ALIGN_OPPOSITE
                else -> Layout.Alignment.ALIGN_NORMAL
            }
            val layout = createStaticLayout(block.text, paint, usableWidth, align, lineSpacingMultiplier)
            if (currentY + layout.height > h - bottomPaddingPx && currentY > topPaddingPx) {
                break
            }
            canvas.save()
            canvas.translate(horizontalPaddingPx, currentY)
            layout.draw(canvas)
            canvas.restore()
            currentY += layout.height + 12f
        }

        return bitmap
    }

    @Suppress("DEPRECATION")
    private fun createStaticLayout(
        text: CharSequence,
        paint: TextPaint,
        width: Int,
        align: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
        spacingMultiplier: Float = 1.0f
    ): StaticLayout {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
                .setAlignment(align)
                .setLineSpacing(0f, spacingMultiplier)
                .setIncludePad(false)
                .build()
        } else {
            StaticLayout(text, paint, width, align, spacingMultiplier, 0f, false)
        }
    }
}
