package com.aura.reader.data.parser

import com.aura.reader.data.model.BlockType
import com.aura.reader.data.model.Book
import com.aura.reader.data.model.BookFormat
import com.aura.reader.data.model.Chapter
import com.aura.reader.data.model.FormattedBlock
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.UUID

object MobiParser {

    fun parse(
        inputStream: InputStream,
        uriString: String,
        fileName: String
    ): Book {
        val bytes = inputStream.readBytes()
        if (bytes.size < 78) {
            throw IllegalArgumentException("Файл $fileName слишком мал для формата MOBI")
        }

        val numRecords = ((bytes[76].toInt() and 0xFF) shl 8) or (bytes[77].toInt() and 0xFF)
        if (numRecords <= 0) {
            throw IllegalArgumentException("В файле $fileName не найдены записи")
        }

        val recordOffsets = IntArray(numRecords)
        var pos = 78
        for (i in 0 until numRecords) {
            if (pos + 4 > bytes.size) break
            val offset = ((bytes[pos].toInt() and 0xFF) shl 24) or
                    ((bytes[pos + 1].toInt() and 0xFF) shl 16) or
                    ((bytes[pos + 2].toInt() and 0xFF) shl 8) or
                    (bytes[pos + 3].toInt() and 0xFF)
            recordOffsets[i] = offset
            pos += 8
        }

        if (recordOffsets.isEmpty() || recordOffsets[0] >= bytes.size) {
            throw IllegalArgumentException("Повреждён заголовок $fileName")
        }

        val rec0Start = recordOffsets[0]
        val rec0End = if (numRecords > 1 && recordOffsets[1] in (rec0Start + 1)..bytes.size) recordOffsets[1] else bytes.size
        val rec0Size = rec0End - rec0Start
        if (rec0Size < 16) {
            throw IllegalArgumentException("Неверная нулевая запись MOBI в $fileName")
        }

        val compression = ((bytes[rec0Start].toInt() and 0xFF) shl 8) or (bytes[rec0Start + 1].toInt() and 0xFF)
        val textRecordCount = ((bytes[rec0Start + 8].toInt() and 0xFF) shl 8) or (bytes[rec0Start + 9].toInt() and 0xFF)

        var textEncoding = 1252L
        var extraFlags = 0
        var huffOffset = -1
        var huffCount = 0
        var extractedTitle: String? = null
        var extractedAuthor: String? = null
        var extractedSeries: String? = null
        var extractedSeriesNumber: Int? = null

        fun getRecord(idx: Int): ByteArray? {
            if (idx < 0 || idx >= numRecords) return null
            val rStart = recordOffsets[idx]
            val rEnd = if (idx + 1 < numRecords) recordOffsets[idx + 1] else bytes.size
            if (rStart >= bytes.size || rStart >= rEnd) return null
            return bytes.copyOfRange(rStart, minOf(rEnd, bytes.size))
        }

        if (rec0Size >= 32 &&
            bytes[rec0Start + 16] == 'M'.code.toByte() &&
            bytes[rec0Start + 17] == 'O'.code.toByte() &&
            bytes[rec0Start + 18] == 'B'.code.toByte() &&
            bytes[rec0Start + 19] == 'I'.code.toByte()
        ) {
            val headerLen = ((bytes[rec0Start + 20].toLong() and 0xFF) shl 24) or
                    ((bytes[rec0Start + 21].toLong() and 0xFF) shl 16) or
                    ((bytes[rec0Start + 22].toLong() and 0xFF) shl 8) or
                    (bytes[rec0Start + 23].toLong() and 0xFF)

            textEncoding = ((bytes[rec0Start + 28].toLong() and 0xFF) shl 24) or
                    ((bytes[rec0Start + 29].toLong() and 0xFF) shl 16) or
                    ((bytes[rec0Start + 30].toLong() and 0xFF) shl 8) or
                    (bytes[rec0Start + 31].toLong() and 0xFF)

            if (rec0Size >= 92) {
                val titleOffset = ((bytes[rec0Start + 84].toInt() and 0xFF) shl 24) or
                        ((bytes[rec0Start + 85].toInt() and 0xFF) shl 16) or
                        ((bytes[rec0Start + 86].toInt() and 0xFF) shl 8) or
                        (bytes[rec0Start + 87].toInt() and 0xFF)
                val titleLength = ((bytes[rec0Start + 88].toInt() and 0xFF) shl 24) or
                        ((bytes[rec0Start + 89].toInt() and 0xFF) shl 16) or
                        ((bytes[rec0Start + 90].toInt() and 0xFF) shl 8) or
                        (bytes[rec0Start + 91].toInt() and 0xFF)

                val absTitleStart = rec0Start + titleOffset
                val absTitleEnd = absTitleStart + titleLength
                if (titleOffset > 0 && titleLength in 1..512 && absTitleEnd <= bytes.size) {
                    val cs = if (textEncoding == 65001L) StandardCharsets.UTF_8 else Charset.forName("windows-1251")
                    extractedTitle = try {
                        String(bytes, absTitleStart, titleLength, cs).trim()
                    } catch (e: Exception) {
                        String(bytes, absTitleStart, titleLength, StandardCharsets.UTF_8).trim()
                    }
                }
            }

            if (rec0Size >= 120) {
                huffOffset = (((bytes[rec0Start + 112].toInt() and 0xFF) shl 24) or
                        ((bytes[rec0Start + 113].toInt() and 0xFF) shl 16) or
                        ((bytes[rec0Start + 114].toInt() and 0xFF) shl 8) or
                        (bytes[rec0Start + 115].toInt() and 0xFF))

                huffCount = (((bytes[rec0Start + 116].toInt() and 0xFF) shl 24) or
                        ((bytes[rec0Start + 117].toInt() and 0xFF) shl 16) or
                        ((bytes[rec0Start + 118].toInt() and 0xFF) shl 8) or
                        (bytes[rec0Start + 119].toInt() and 0xFF))
            }

            if (rec0Size >= 244) {
                extraFlags = ((bytes[rec0Start + 242].toInt() and 0xFF) shl 8) or
                        (bytes[rec0Start + 243].toInt() and 0xFF)
            }

            // EXTH header parsing for Author and Series metadata
            val exthOffset = rec0Start + 16 + headerLen.toInt()
            if (exthOffset + 12 <= bytes.size &&
                bytes[exthOffset] == 'E'.code.toByte() &&
                bytes[exthOffset + 1] == 'X'.code.toByte() &&
                bytes[exthOffset + 2] == 'T'.code.toByte() &&
                bytes[exthOffset + 3] == 'H'.code.toByte()
            ) {
                val exthCount = ((bytes[exthOffset + 8].toInt() and 0xFF) shl 24) or
                        ((bytes[exthOffset + 9].toInt() and 0xFF) shl 16) or
                        ((bytes[exthOffset + 10].toInt() and 0xFF) shl 8) or
                        (bytes[exthOffset + 11].toInt() and 0xFF)

                var ep = exthOffset + 12
                val cs = if (textEncoding == 65001L) StandardCharsets.UTF_8 else Charset.forName("windows-1251")
                for (r in 0 until minOf(exthCount, 100)) {
                    if (ep + 8 > bytes.size) break
                    val recType = ((bytes[ep].toInt() and 0xFF) shl 24) or
                            ((bytes[ep + 1].toInt() and 0xFF) shl 16) or
                            ((bytes[ep + 2].toInt() and 0xFF) shl 8) or
                            (bytes[ep + 3].toInt() and 0xFF)
                    val recLen = ((bytes[ep + 4].toInt() and 0xFF) shl 24) or
                            ((bytes[ep + 5].toInt() and 0xFF) shl 16) or
                            ((bytes[ep + 6].toInt() and 0xFF) shl 8) or
                            (bytes[ep + 7].toInt() and 0xFF)

                    if (recLen < 8 || ep + recLen > bytes.size) break
                    val dataLen = recLen - 8
                    val dataStr = try {
                        String(bytes, ep + 8, dataLen, cs).trim()
                    } catch (e: Exception) {
                        String(bytes, ep + 8, dataLen, StandardCharsets.UTF_8).trim()
                    }

                    when (recType) {
                        100 -> if (extractedAuthor == null) extractedAuthor = dataStr
                        108 -> if (extractedSeries == null) extractedSeries = dataStr
                        109 -> if (extractedSeriesNumber == null) extractedSeriesNumber = dataStr.toIntOrNull()
                        503 -> if (extractedTitle == null && dataStr.isNotBlank()) extractedTitle = dataStr
                    }
                    ep += recLen
                }
            }
        }

        val bookTitle = extractedTitle?.ifBlank { null } ?: fileName.substringBeforeLast(".")

        // Prepare decompressor (PalmDoc or HuffCdic)
        var huffDecompressor: HuffCdicDecompressor? = null
        if (compression == 17480 && huffOffset in 0 until numRecords && huffCount > 1) {
            val huffRecord = getRecord(huffOffset)
            if (huffRecord != null) {
                val cdicRecords = mutableListOf<ByteArray>()
                for (k in 1 until huffCount) {
                    val cdic = getRecord(huffOffset + k)
                    if (cdic != null) cdicRecords.add(cdic)
                }
                try {
                    huffDecompressor = HuffCdicDecompressor(huffRecord, cdicRecords)
                } catch (_: Exception) {}
            }
        }

        val decompressedText = ByteArrayOutputStream()
        val limitRecords = minOf(textRecordCount, numRecords - 1)
        for (i in 1..limitRecords) {
            val rawSlice = getRecord(i) ?: continue
            val slice = stripTrailingData(rawSlice, extraFlags)

            when (compression) {
                1 -> decompressedText.write(slice)
                2 -> {
                    val decoded = decompressPalmDoc(slice)
                    decompressedText.write(decoded)
                }
                17480 -> {
                    if (huffDecompressor != null) {
                        val decoded = huffDecompressor.unpack(slice)
                        decompressedText.write(decoded)
                    } else {
                        decompressedText.write(slice)
                    }
                }
                else -> {
                    decompressedText.write(slice)
                }
            }
        }

        val rawTextBytes = decompressedText.toByteArray()
        val textContent = decodeBytes(rawTextBytes, textEncoding)

        val cleanHtml = cleanMobiHtml(textContent)
        val chapters = buildChapters(cleanHtml, bookTitle)

        return Book(
            id = UUID.nameUUIDFromBytes("${uriString}_${fileName}".toByteArray()).toString(),
            title = bookTitle,
            author = extractedAuthor ?: "",
            series = extractedSeries,
            seriesNumber = extractedSeriesNumber,
            format = BookFormat.MOBI,
            uriString = uriString,
            chapters = chapters
        )
    }

    private fun stripTrailingData(data: ByteArray, flags: Int): ByteArray {
        if (flags == 0) return data
        var trailingSize = 0
        var f = flags shr 1
        while (f > 0) {
            if ((f and 1) != 0) {
                var size = 0
                var shift = 0
                while (true) {
                    val pos = data.size - 1 - trailingSize
                    if (pos < 0) return ByteArray(0)
                    val b = data[pos].toInt() and 0xFF
                    trailingSize++
                    size = size or ((b and 0x7F) shl shift)
                    shift += 7
                    if ((b and 0x80) != 0 || shift >= 28) break
                }
                trailingSize += (size - 1)
            }
            f = f shr 1
        }
        if ((flags and 1) != 0) {
            val pos = data.size - 1 - trailingSize
            if (pos >= 0) {
                val b = data[pos].toInt() and 0xFF
                trailingSize += (b and 0x03) + 1
            }
        }
        val validLen = (data.size - trailingSize).coerceIn(0, data.size)
        return if (validLen < data.size) data.copyOfRange(0, validLen) else data
    }

    private fun decompressPalmDoc(data: ByteArray): ByteArray {
        var buf = ByteArray(4096)
        var outPos = 0
        var i = 0

        fun ensureCapacity(needed: Int) {
            if (outPos + needed > buf.size) {
                val newBuf = ByteArray(maxOf(buf.size * 2, outPos + needed))
                System.arraycopy(buf, 0, newBuf, 0, outPos)
                buf = newBuf
            }
        }

        while (i < data.size) {
            val b = data[i++].toInt() and 0xFF
            when {
                b == 0 -> {
                    ensureCapacity(1)
                    buf[outPos++] = 0
                }
                b in 1..8 -> {
                    val count = minOf(b, data.size - i)
                    if (count > 0) {
                        ensureCapacity(count)
                        System.arraycopy(data, i, buf, outPos, count)
                        outPos += count
                        i += count
                    }
                }
                b in 9..0x7F -> {
                    ensureCapacity(1)
                    buf[outPos++] = b.toByte()
                }
                b in 0x80..0xBF -> {
                    if (i < data.size) {
                        val b2 = data[i++].toInt() and 0xFF
                        val distance = ((b and 0x3F) shl 8) or b2
                        val length = (distance and 0x07) + 3
                        val offset = distance shr 3
                        if (offset > 0 && outPos >= offset) {
                            ensureCapacity(length)
                            val start = outPos - offset
                            for (k in 0 until length) {
                                buf[outPos] = buf[start + (k % offset)]
                                outPos++
                            }
                        }
                    }
                }
                else -> {
                    ensureCapacity(2)
                    buf[outPos++] = ' '.code.toByte()
                    buf[outPos++] = (b xor 0x80).toByte()
                }
            }
        }
        return buf.copyOf(outPos)
    }

    private fun decodeBytes(bytes: ByteArray, textEncoding: Long): String {
        if (textEncoding == 65001L) {
            return String(bytes, StandardCharsets.UTF_8)
        }

        val preview = String(bytes.copyOf(minOf(bytes.size, 2048)), StandardCharsets.ISO_8859_1)
        val charsetMatch = Regex("""charset\s*=\s*["']?([a-zA-Z0-9_-]+)""", RegexOption.IGNORE_CASE).find(preview)
        if (charsetMatch != null) {
            val csName = charsetMatch.groupValues[1]
            try {
                return String(bytes, Charset.forName(csName))
            } catch (_: Exception) {}
        }

        val utf8 = String(bytes, StandardCharsets.UTF_8)
        val hasCyrillic = utf8.any { it in '\u0400'..'\u04FF' }
        val invalidCount = utf8.count { it == '' }
        val invalidRatio = invalidCount.toDouble() / maxOf(1, utf8.length)

        if (hasCyrillic && invalidRatio < 0.01) {
            return utf8
        }

        try {
            val cp1251 = String(bytes, Charset.forName("windows-1251"))
            val cp1251Cyrillic = cp1251.count { it in '\u0400'..'\u04FF' }
            if (cp1251Cyrillic > 20) {
                return cp1251
            }
        } catch (_: Exception) {}

        return if (invalidRatio < 0.05) utf8 else {
            try {
                String(bytes, Charset.forName("windows-1251"))
            } catch (_: Exception) {
                String(bytes, StandardCharsets.ISO_8859_1)
            }
        }
    }

    private fun cleanMobiHtml(html: String): String {
        var res = html
        res = res.replace(Regex("<mbp:pagebreak/?>", RegexOption.IGNORE_CASE), "\n\n<!--pagebreak-->\n\n")
        res = res.replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
        res = res.replace(Regex("</p>|</div>|</h1>|</h2>|</h3>", RegexOption.IGNORE_CASE), "\n\n")
        res = res.replace(Regex("<[^>]+>"), "")
        res = res.replace("&nbsp;", " ")
        res = res.replace("&quot;", "\"")
        res = res.replace("&amp;", "&")
        res = res.replace("&lt;", "<")
        res = res.replace("&gt;", ">")
        res = res.replace("&apos;", "'")
        res = res.replace("&#160;", " ")
        return res
    }

    private fun buildChapters(text: String, defaultTitle: String): List<Chapter> {
        val parts = text.split("<!--pagebreak-->").map { it.trim() }.filter { it.isNotEmpty() }
        val rawSections = if (parts.isNotEmpty()) parts else listOf(text)

        val chapters = mutableListOf<Chapter>()
        var chapterOrder = 0

        for ((idx, sec) in rawSections.withIndex()) {
            val lines = sec.lines().map { it.trim() }.filter { it.isNotEmpty() }
            if (lines.isEmpty()) continue

            val title = if (lines.first().length <= 60 && (lines.first().startsWith("Глава", ignoreCase = true) || lines.first().startsWith("Chapter", ignoreCase = true) || lines.size > 1)) {
                lines.first()
            } else {
                if (rawSections.size > 1) "Часть " + (idx + 1) else defaultTitle
            }

            val blocks = lines.map { FormattedBlock(BlockType.PARAGRAPH, it) }
            val fullContent = blocks.joinToString("\n\n") { it.text }

            chapters.add(
                Chapter(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    content = fullContent,
                    blocks = blocks,
                    order = chapterOrder++
                )
            )
        }

        if (chapters.isEmpty()) {
            chapters.add(
                Chapter(
                    id = UUID.randomUUID().toString(),
                    title = defaultTitle,
                    content = text,
                    blocks = listOf(FormattedBlock(BlockType.PARAGRAPH, text)),
                    order = 0
                )
            )
        }

        return chapters
    }

    private class HuffCdicDecompressor(
        huffData: ByteArray,
        cdicRecords: List<ByteArray>
    ) {
        private data class DictRecord(val codeLen: Int, val term: Int, val maxCode: Long)
        private data class Slice(var data: ByteArray, var flag: Int)

        private val dict1 = ArrayList<DictRecord>(256)
        private val minCode = ArrayList<Long>()
        private val maxCode = ArrayList<Long>()
        private val dictionary = ArrayList<Slice>()

        init {
            loadHuff(huffData)
            for (rec in cdicRecords) {
                loadCdic(rec)
            }
        }

        private fun readUInt32BE(data: ByteArray, offset: Int): Long {
            if (offset + 4 > data.size) return 0L
            return ((data[offset].toLong() and 0xFF) shl 24) or
                    ((data[offset + 1].toLong() and 0xFF) shl 16) or
                    ((data[offset + 2].toLong() and 0xFF) shl 8) or
                    (data[offset + 3].toLong() and 0xFF)
        }

        private fun readUInt16BE(data: ByteArray, offset: Int): Int {
            if (offset + 2 > data.size) return 0
            return ((data[offset].toInt() and 0xFF) shl 8) or
                    (data[offset + 1].toInt() and 0xFF)
        }

        private fun loadHuff(data: ByteArray) {
            if (data.size < 24) return
            val off1 = readUInt32BE(data, 8).toInt()
            val off2 = readUInt32BE(data, 12).toInt()

            for (i in 0 until 256) {
                val v = readUInt32BE(data, off1 + (i * 4))
                val codelen = (v and 0x1FL).toInt()
                val term = (v and 0x80L).toInt()
                var max = v shr 8
                if (codelen > 0) {
                    max = ((max + 1L) shl (32 - codelen)) - 1L
                }
                dict1.add(DictRecord(codelen, term, max))
            }

            val dict2 = ArrayList<Long>(64)
            for (i in 0 until 64) {
                dict2.add(readUInt32BE(data, off2 + (i * 4)))
            }

            minCode.add(0L)
            maxCode.add(0L)
            var count = 1
            var i = 0
            while (i < dict2.size) {
                minCode.add(dict2[i] shl (32 - count))
                count++
                i += 2
            }

            count = 1
            i = 1
            while (i < dict2.size) {
                maxCode.add(((dict2[i] + 1L) shl (32 - count)) - 1L)
                count++
                i += 2
            }
        }

        private fun loadCdic(data: ByteArray) {
            if (data.size < 16) return
            val phrases = readUInt32BE(data, 8).toInt()
            val bits = readUInt32BE(data, 12).toInt()
            val n = minOf(1 shl bits, phrases - dictionary.size)
            for (i in 0 until n) {
                val offset = readUInt16BE(data, 16 + (i * 2))
                dictionary.add(getSlice(data, offset))
            }
        }

        private fun getSlice(data: ByteArray, offset: Int): Slice {
            if (18 + offset > data.size) return Slice(ByteArray(0), 1)
            val blen = readUInt16BE(data, 16 + offset)
            val len = minOf(blen and 0x7FFF, data.size - (18 + offset))
            val sliceData = data.copyOfRange(18 + offset, 18 + offset + len)
            return Slice(sliceData, blen and 0x8000)
        }

        fun unpack(data: ByteArray): ByteArray {
            val out = ByteArrayOutputStream(data.size * 2)
            var bitsLeft = data.size * 8
            val padded = data + ByteArray(8)
            var pos = 0
            var x = readUInt64BE(padded, pos)
            var n = 32

            while (true) {
                if (n <= 0) {
                    pos += 4
                    if (pos + 8 <= padded.size) {
                        x = readUInt64BE(padded, pos)
                    }
                    n += 32
                }
                val code = (x ushr n) and 0xFFFFFFFFL
                val idx = ((code ushr 24) and 0xFFL).toInt()
                if (idx >= dict1.size) break
                val rec = dict1[idx]
                var codelen = rec.codeLen
                var maxcode = rec.maxCode

                if (rec.term == 0) {
                    while (codelen < minCode.size && code < minCode[codelen]) {
                        codelen++
                    }
                    if (codelen < maxCode.size) {
                        maxcode = maxCode[codelen]
                    }
                }

                n -= codelen
                bitsLeft -= codelen
                if (bitsLeft < 0 || codelen <= 0) break

                val r = (((maxcode - code) ushr (32 - codelen)) and 0xFFFFFFFFL).toInt()
                if (r < 0 || r >= dictionary.size) break
                var slice = dictionary[r]
                if (slice.flag == 0) {
                    val newSlice = unpack(slice.data)
                    slice = Slice(newSlice, 1)
                    dictionary[r] = slice
                }
                out.write(slice.data)
            }
            return out.toByteArray()
        }

        private fun readUInt64BE(data: ByteArray, offset: Int): Long {
            if (offset + 8 > data.size) return 0L
            var res = 0L
            for (i in 0 until 8) {
                res = (res shl 8) or (data[offset + i].toLong() and 0xFFL)
            }
            return res
        }
    }
}

