package com.aura.reader.data.analytics

import android.content.Context
import android.telephony.TelephonyManager
import com.aura.reader.BuildConfig
import com.aura.reader.data.preferences.PreferencesManager
import com.telemetrydeck.sdk.TelemetryDeck
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

object AnalyticsManager {
    const val APP_ID = "6480FE73-0580-48EB-AFC2-45E442B3B4DC"

    internal const val EVENT_APP_OPEN = "app_open"
    internal const val EVENT_APP_FIRST_OPEN = "app_first_open"
    internal const val EVENT_APP_UPDATE = "app_update"

    private var isStarted = false

    fun trackAppLaunch(
        context: Context,
        preferencesManager: PreferencesManager,
        scope: CoroutineScope
    ) {
        scope.launch(Dispatchers.IO) {
            val enabled = preferencesManager.anonymousAnalyticsEnabled.first()
            if (!enabled) {
                // The SDK may still be running from a previous launch. Stop it so
                // the opt-out is not postponed until the next start.
                stopTracking()
                return@launch
            }

            try {
                if (!isStarted) {
                    val builder = TelemetryDeck.Builder()
                        .appID(APP_ID)
                        // The app sends one explicit app_open event. The SDK's
                        // automatic per-foreground session event would double it.
                        .sendNewSessionBeganSignal(false)
                        // Developer builds must never appear in live statistics.
                        .testMode(BuildConfig.DEBUG)
                    TelemetryDeck.start(context.applicationContext, builder)
                    isStarted = true
                }

                val country = getUserCountry(context)
                val params = buildMap {
                    put("country", country)
                    put("locale", Locale.getDefault().toLanguageTag())
                    put(
                        "app_channel",
                        buildChannel(BuildConfig.VERSION_NAME, BuildConfig.DEBUG)
                    )
                    try {
                        val appLang = preferencesManager.appLanguage.first().code
                        put("app_language", appLang)
                    } catch (_: Exception) {}
                }

                val hasTrackedFirst = preferencesManager.hasTrackedFirstOpen.first()
                val lastVersionCode = preferencesManager.lastTrackedVersionCode.first()
                val launchEvent = launchEventFor(
                    hasTrackedFirst = hasTrackedFirst,
                    lastTrackedVersionCode = lastVersionCode,
                    currentVersionCode = BuildConfig.VERSION_CODE
                )

                when (launchEvent) {
                    EVENT_APP_FIRST_OPEN -> {
                        TelemetryDeck.signal(EVENT_APP_FIRST_OPEN, params)
                        preferencesManager.setHasTrackedFirstOpen(true)
                    }
                    EVENT_APP_UPDATE -> {
                        val updateParams = params + (
                            "previous_version_code" to (lastVersionCode ?: 0).toString()
                        )
                        TelemetryDeck.signal(EVENT_APP_UPDATE, updateParams)
                    }
                }

                preferencesManager.setLastTrackedVersionCode(BuildConfig.VERSION_CODE)
                TelemetryDeck.signal(EVENT_APP_OPEN, params)
            } catch (_: Exception) {
                // Silently ignore network or telemetry errors
            }
        }
    }

    /**
     * Immediately stops telemetry and discards signals that have not been sent yet.
     * Safe to call even when the SDK was never started.
     */
    fun stopTracking() {
        try {
            TelemetryDeck.getInstance()?.cache?.empty()
        } catch (_: Exception) {
            // The cache may already be gone.
        }
        try {
            TelemetryDeck.stop()
        } catch (_: Exception) {
            // Stopping a non-running SDK is harmless.
        }
        isStarted = false
    }

    internal fun launchEventFor(
        hasTrackedFirst: Boolean,
        lastTrackedVersionCode: Int?,
        currentVersionCode: Int
    ): String? = when {
        !hasTrackedFirst -> EVENT_APP_FIRST_OPEN
        lastTrackedVersionCode != null && lastTrackedVersionCode != currentVersionCode -> EVENT_APP_UPDATE
        else -> null
    }

    internal fun buildChannel(versionName: String, isDebug: Boolean): String = when {
        isDebug -> "debug"
        versionName.contains("beta", ignoreCase = true) -> "beta"
        else -> "stable"
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
