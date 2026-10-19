package com.aura.reader.data.analytics

import android.content.Context
import com.aura.reader.data.preferences.PreferencesManager
import com.telemetrydeck.sdk.TelemetryDeck
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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

                val hasTrackedFirst = preferencesManager.hasTrackedFirstOpen.first()
                if (!hasTrackedFirst) {
                    TelemetryDeck.signal("app_first_open")
                    preferencesManager.setHasTrackedFirstOpen(true)
                }

                TelemetryDeck.signal("app_open")
            } catch (_: Exception) {
                // Silently ignore network or telemetry errors
            }
        }
    }
}
