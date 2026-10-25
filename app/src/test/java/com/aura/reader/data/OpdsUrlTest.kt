package com.aura.reader.data

import com.aura.reader.data.opds.OpdsUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OpdsUrlTest {

    @Test
    fun keepsAddressWithScheme() {
        assertEquals(
            "http://flibusta.is/opds",
            OpdsUrl.normalize("  http://flibusta.is/opds  ")
        )
    }

    @Test
    fun addsHttpsWhenSchemeIsMissing() {
        assertEquals(
            "https://flibusta.is/opds",
            OpdsUrl.normalize("flibusta.is/opds")
        )
    }

    @Test
    fun removesTrailingSlashes() {
        assertEquals(
            "https://example.com/opds",
            OpdsUrl.normalize("https://example.com/opds/")
        )
    }

    @Test
    fun keepsPort() {
        assertEquals(
            "https://example.com:8080/opds",
            OpdsUrl.normalize("example.com:8080/opds")
        )
    }

    @Test
    fun lowercasesHost() {
        assertEquals(
            "https://example.com/opds",
            OpdsUrl.normalize("https://EXAMPLE.com/opds")
        )
    }

    @Test
    fun rejectsUnsupportedScheme() {
        assertNull(OpdsUrl.normalize("ftp://example.com/opds"))
    }

    @Test
    fun rejectsBlankAddress() {
        assertNull(OpdsUrl.normalize(""))
        assertNull(OpdsUrl.normalize("   "))
    }

    @Test
    fun reportsValidity() {
        assertTrue(OpdsUrl.isValid("flibusta.is/opds"))
        assertFalse(OpdsUrl.isValid("https://"))
    }
}
