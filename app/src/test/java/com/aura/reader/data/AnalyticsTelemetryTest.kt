package com.aura.reader.data

import com.aura.reader.data.analytics.AnalyticsManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AnalyticsTelemetryTest {

    @Test
    fun stableReleaseGetsStableChannel() {
        assertEquals("stable", AnalyticsManager.buildChannel("2.2.0", isDebug = false))
    }

    @Test
    fun betaReleaseGetsBetaChannel() {
        assertEquals("beta", AnalyticsManager.buildChannel("2.2.0-beta.12", isDebug = false))
        assertEquals("beta", AnalyticsManager.buildChannel("2.2.0-BETA.12", isDebug = false))
    }

    @Test
    fun debugBuildWinsOverVersionName() {
        assertEquals("debug", AnalyticsManager.buildChannel("2.2.0-beta.12", isDebug = true))
    }

    @Test
    fun firstLaunchIsReportedOnce() {
        assertEquals(
            AnalyticsManager.EVENT_APP_FIRST_OPEN,
            AnalyticsManager.launchEventFor(
                hasTrackedFirst = false,
                lastTrackedVersionCode = null,
                currentVersionCode = 129
            )
        )
    }

    @Test
    fun versionChangeIsReportedAsUpdate() {
        assertEquals(
            AnalyticsManager.EVENT_APP_UPDATE,
            AnalyticsManager.launchEventFor(
                hasTrackedFirst = true,
                lastTrackedVersionCode = 128,
                currentVersionCode = 129
            )
        )
    }

    @Test
    fun relaunchingSameVersionIsNotAnUpdate() {
        assertNull(
            AnalyticsManager.launchEventFor(
                hasTrackedFirst = true,
                lastTrackedVersionCode = 129,
                currentVersionCode = 129
            )
        )
    }

    @Test
    fun legacyInstallWithoutStoredVersionIsNotMisreportedAsUpdate() {
        assertNull(
            AnalyticsManager.launchEventFor(
                hasTrackedFirst = true,
                lastTrackedVersionCode = null,
                currentVersionCode = 129
            )
        )
    }
}
