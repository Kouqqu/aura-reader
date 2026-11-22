package com.aura.reader.util

import android.os.Build
import android.view.Display
import android.view.Window

object HighRefresh {
    fun apply(window: Window, isEInk: Boolean = false) {
        if (isEInk) return
        runCatching {
            val display: Display = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) window.context.display else null)
                ?: @Suppress("DEPRECATION") window.windowManager.defaultDisplay
            val current = display.mode
            val best = display.supportedModes
                .filter { it.physicalWidth == current.physicalWidth && it.physicalHeight == current.physicalHeight }
                .maxByOrNull { it.refreshRate } ?: return
            if (best.refreshRate <= 61f) return
            val attrs = window.attributes
            if (attrs.preferredDisplayModeId != best.modeId) {
                attrs.preferredDisplayModeId = best.modeId
                window.attributes = attrs
            }
        }
    }
}
