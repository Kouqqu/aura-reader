package com.aura.reader.util

import android.os.Build

object EInkDetector {
    private val EINK_IDENTIFIERS = listOf(
        "onyx", "boox", "pocketbook", "meebook", "haoqing", "likebook", "boyue", "bigme",
        "kobo", "tolino", "barnes", "nook", "inkpalm", "moaan", "supernote", "ratta",
        "dasung", "ireader", "hanvon", "bookeen", "inkbook", "remarkable", "mobiscribe"
    )

    fun isEInkDevice(): Boolean {
        val brand = (Build.BRAND ?: "").lowercase()
        val manufacturer = (Build.MANUFACTURER ?: "").lowercase()
        val model = (Build.MODEL ?: "").lowercase()
        val device = (Build.DEVICE ?: "").lowercase()
        return EINK_IDENTIFIERS.any { id ->
            brand.contains(id) || manufacturer.contains(id) || model.contains(id) || device.contains(id)
        }
    }
}
