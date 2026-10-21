package com.aura.reader.data.model

/** Positions use a block index plus an offset into its formatted text. */
object ReadingPosition {
    fun pageForAnchor(pages: List<List<Pair<Int, FormattedBlock>>>, block: Int, textOffset: Int): Int =
        pages.indexOfLast { page ->
            page.firstOrNull()?.let { (index, chunk) ->
                index < block || (index == block && chunk.sourceTextOffset <= textOffset)
            } ?: false
        }.coerceAtLeast(0)

    fun percent(book: Book, chapterIndex: Int, blockIndex: Int, textOffset: Int = 0): Int {
        if (book.chapters.isEmpty()) return 0
        fun blocks(chapter: Chapter) = chapter.blocks.ifEmpty {
            chapter.content.split("\n\n").filter { it.isNotBlank() }.map { FormattedBlock(BlockType.PARAGRAPH, it.trim()) }
        }
        fun weight(block: FormattedBlock): Long = if (block.type == BlockType.IMAGE) 1L else block.text.length.coerceAtLeast(1).toLong()
        val chapters = book.chapters.map { blocks(it) }
        val total = chapters.sumOf { it.sumOf(::weight).coerceAtLeast(1L) }
        val chapter = chapterIndex.coerceIn(chapters.indices)
        val current = chapters[chapter]
        val block = blockIndex.coerceIn(0, (current.size - 1).coerceAtLeast(0))
        var read = chapters.take(chapter).sumOf { it.sumOf(::weight).coerceAtLeast(1L) }
        read += current.take(block).sumOf(::weight)
        read += minOf(textOffset.coerceAtLeast(0).toLong(), current.getOrNull(block)?.let(::weight) ?: 0L)
        // Completion is an explicit reader action, not a side effect of showing the last block.
        return ((read.toDouble() / total.coerceAtLeast(1L)) * 100).toInt().coerceIn(0, 99)
    }
}
