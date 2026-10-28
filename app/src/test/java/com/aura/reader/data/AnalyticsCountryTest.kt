package com.aura.reader.data

import android.content.Context
import android.telephony.TelephonyManager
import com.aura.reader.data.analytics.AnalyticsManager
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowTelephonyManager
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AnalyticsCountryTest {

    private val context: Context get() = RuntimeEnvironment.getApplication()

    @Test
    fun testSimCountryIsoPreferred() {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val shadowTm: ShadowTelephonyManager = Shadows.shadowOf(tm)
        shadowTm.setSimCountryIso("ru")
        shadowTm.setNetworkCountryIso("de")

        val country = AnalyticsManager.getUserCountry(context)
        assertEquals("RU", country)
    }

    @Test
    fun testNetworkCountryIsoWhenSimEmpty() {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val shadowTm: ShadowTelephonyManager = Shadows.shadowOf(tm)
        shadowTm.setSimCountryIso("")
        shadowTm.setNetworkCountryIso("kz")

        val country = AnalyticsManager.getUserCountry(context)
        assertEquals("KZ", country)
    }

    @Test
    fun testLocaleFallbackWhenTelephonyEmpty() {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val shadowTm: ShadowTelephonyManager = Shadows.shadowOf(tm)
        shadowTm.setSimCountryIso("")
        shadowTm.setNetworkCountryIso("")

        val prevLocale = Locale.getDefault()
        try {
            Locale.setDefault(Locale.Builder().setLanguage("ru").setRegion("BY").build())
            val country = AnalyticsManager.getUserCountry(context)
            assertEquals("BY", country)
        } finally {
            Locale.setDefault(prevLocale)
        }
    }
}
