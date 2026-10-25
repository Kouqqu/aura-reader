package com.aura.reader.data.opds

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/**
 * Normalizes catalog addresses typed by the user.
 *
 * Accepts addresses with or without a scheme, so "flibusta.is/opds" becomes
 * "https://flibusta.is/opds". Returns null when the address cannot be used.
 */
object OpdsUrl {

    fun normalize(input: String): String? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null

        val withScheme = when {
            trimmed.startsWith("http://", ignoreCase = true) -> trimmed
            trimmed.startsWith("https://", ignoreCase = true) -> trimmed
            trimmed.contains("://") -> return null
            else -> "https://$trimmed"
        }

        val parsed = withScheme.toHttpUrlOrNull() ?: return null
        return parsed.toString().trimEnd('/')
    }

    fun isValid(input: String): Boolean = normalize(input) != null
}
