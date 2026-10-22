package com.aura.reader.data.analytics

import android.content.Context
import android.telephony.TelephonyManager
import com.aura.reader.data.preferences.PreferencesManager
import com.telemetrydeck.sdk.TelemetryDeck
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

object AnalyticsManager {
    const val APP_ID = "6480FE73-0580-48EB-AFC2-45E442B3B4DC"
    private var isStarted = false

    fun trackAppLaunch(
        context: Context,
        preferencesManager: PreferencesManager,
        scope: CoroutineScope
    ) {
        scope.launch(Dispatchers.IO) {
            val enabled = preferencesManager.anonymousAnalyticsEnabled.first()
            if (!enabled) return@launch

            try {
                if (!isStarted) {
                    val builder = TelemetryDeck.Builder()
                        .appID(APP_ID)
                    TelemetryDeck.start(context.applicationContext, builder)
                    isStarted = true
                }

                val country = getUserCountry(context)
                val params = buildMap {
                    put("country", country)
                    put("locale", Locale.getDefault().toLanguageTag())
                    try {
                        val appLang = preferencesManager.appLanguage.first().code
                        put("app_language", appLang)
                    } catch (_: Exception) {}
                }

                val hasTrackedFirst = preferencesManager.hasTrackedFirstOpen.first()
                if (!hasTrackedFirst) {
                    TelemetryDeck.signal("app_first_open", params)
                    preferencesManager.setHasTrackedFirstOpen(true)
                }

                TelemetryDeck.signal("app_open", params)
            } catch (_: Exception) {
                // Silently ignore network or telemetry errors
            }
        }
    }

    /**
     * Determines user country code (ISO-2) without network requests.
     * Unaffected by VPN as it inspects hardware/system SIM and OS locale.
     */
    fun getUserCountry(context: Context): String {
        try {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            // 1. Try SIM card provider country (e.g. "ru", "by", "kz", "ua")
            val simCountry = tm?.simCountryIso?.trim()?.uppercase(Locale.US)
            if (!simCountry.isNullOrBlank() && simCountry.length == 2) {
                return simCountry
            }

            // 2. Try network operator country from cell towers
            val netCountry = tm?.networkCountryIso?.trim()?.uppercase(Locale.US)
            if (!netCountry.isNullOrBlank() && netCountry.length == 2) {
                return netCountry
            }
        } catch (_: Exception) {
            // TelephonyManager can throw on certain emulators/restricted profiles
        }

        try {
            // 3. Fallback to system locale region (e.g. Locale.getDefault().country)
            val localeCountry = Locale.getDefault().country.trim().uppercase(Locale.US)
            if (localeCountry.isNotBlank() && localeCountry.length == 2) {
                return localeCountry
            }
        } catch (_: Exception) {}

        return "UNKNOWN"
    }
}
