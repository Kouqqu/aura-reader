package com.aura.reader.data

import com.aura.reader.data.opds.OpdsService
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.net.SocketTimeoutException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class OpdsConnectionErrorTest {

    @Test
    fun timeoutIsConnectionError() {
        assertTrue(OpdsService.isConnectionError(SocketTimeoutException("timeout")))
    }

    @Test
    fun serverErrorIsConnectionError() {
        assertTrue(OpdsService.isConnectionError(Exception("HTTP 522: Origin is unreachable")))
    }

    @Test
    fun clientErrorIsNotConnectionError() {
        assertFalse(OpdsService.isConnectionError(Exception("HTTP 404: Not Found")))
    }
}
