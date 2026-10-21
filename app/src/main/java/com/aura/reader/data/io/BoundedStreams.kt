package com.aura.reader.data.io

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.io.IOException

internal fun InputStream.copyBoundedTo(output: OutputStream, limit: Long): Long {
    val buffer = ByteArray(8192)
    var total = 0L
    while (true) {
        val count = read(buffer)
        if (count == -1) break
        total += count
        if (total > limit) throw IOException("Book exceeds supported size limit")
        output.write(buffer, 0, count)
    }
    return total
}

internal fun InputStream.readBoundedBytes(limit: Long): ByteArray =
    ByteArrayOutputStream().also { copyBoundedTo(it, limit) }.toByteArray()
