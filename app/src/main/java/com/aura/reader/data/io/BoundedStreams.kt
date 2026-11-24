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

internal fun newSecureDocumentBuilder(): javax.xml.parsers.DocumentBuilder {
    val factory = javax.xml.parsers.DocumentBuilderFactory.newInstance()
    runCatching { factory.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, true) }
    runCatching { factory.setFeature("http://xml.org/sax/features/external-general-entities", false) }
    runCatching { factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false) }
    runCatching { factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false) }
    runCatching { factory.isXIncludeAware = false }
    runCatching { factory.isExpandEntityReferences = false }
    return factory.newDocumentBuilder()
}
