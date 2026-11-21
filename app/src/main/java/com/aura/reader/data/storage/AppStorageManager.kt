package com.aura.reader.data.storage

import android.content.Context
import java.io.File
import java.util.Locale

object AppStorageManager {

    /**
     * Calculates the total size of cached files (both internal and external cache).
     */
    fun getCacheSizeBytes(context: Context): Long {
        var total = 0L
        try {
            total += getDirectorySize(context.cacheDir)
            context.externalCacheDir?.let {
                total += getDirectorySize(it)
            }
        } catch (_: Exception) {}
        return total
    }

    private fun getDirectorySize(dir: File?): Long {
        if (dir == null || !dir.exists()) return 0L
        var total = 0L
        dir.listFiles()?.forEach { file ->
            total += if (file.isDirectory) getDirectorySize(file) else file.length()
        }
        return total
    }

    /**
     * Clears all non-essential temporary caches:
     * - Downloaded update APKs
     * - Book image extractions (EPUB/FB2/CBZ re-extract them dynamically when opened)
     * - PDF rendered pages
     * - Shared book exports and backups in cache
     */
    fun clearAllCache(context: Context): Long {
        val bytesBefore = getCacheSizeBytes(context)
        try {
            context.cacheDir.listFiles()?.forEach { file ->
                try {
                    file.deleteRecursively()
                } catch (_: Exception) {}
            }
            context.externalCacheDir?.listFiles()?.forEach { file ->
                try {
                    file.deleteRecursively()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
        return bytesBefore
    }

    /**
     * Automatically called on startup and background maintenance to purge dead files:
     * - Cached update APKs
     * - Leftover import temp files in filesDir (from cancelled/failed imports)
     * - Leftover OPDS or network .tmp downloads
     * - Orphaned book_images folders whose books are no longer in the library
     */
    fun cleanupTemporaryFiles(context: Context, activeBookIds: Set<String> = emptySet()) {
        try {
            // 1. Delete all update APKs from internal and external cache
            File(context.cacheDir, "updates").deleteRecursively()
            context.cacheDir.listFiles { _, name -> name.endsWith(".apk", ignoreCase = true) }?.forEach {
                try { it.delete() } catch (_: Exception) {}
            }
            context.externalCacheDir?.let { extCache ->
                File(extCache, "updates").deleteRecursively()
                extCache.listFiles { _, name -> name.endsWith(".apk", ignoreCase = true) }?.forEach {
                    try { it.delete() } catch (_: Exception) {}
                }
            }

            // 2. Delete leftover import temporary files in filesDir
            context.filesDir.listFiles { _, name -> name.startsWith("import_") && name.endsWith(".book") }?.forEach {
                try { it.delete() } catch (_: Exception) {}
            }

            // 3. Delete leftover .tmp files in cacheDir
            context.cacheDir.listFiles { _, name -> name.endsWith(".tmp", ignoreCase = true) }?.forEach {
                try { it.delete() } catch (_: Exception) {}
            }

            // 4. Clean up orphaned book_images folders
            if (activeBookIds.isNotEmpty()) {
                val imagesDir = File(context.cacheDir, "book_images")
                if (imagesDir.exists() && imagesDir.isDirectory) {
                    imagesDir.listFiles()?.forEach { folder ->
                        if (folder.isDirectory && folder.name !in activeBookIds) {
                            try { folder.deleteRecursively() } catch (_: Exception) {}
                        }
                    }
                }
            }
        } catch (_: Exception) {}
    }

    /**
     * Formats bytes into human-readable format.
     */
    fun formatSize(bytes: Long): String {
        if (bytes <= 0L) return "0 КБ"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        return when {
            gb >= 1.0 -> String.format(Locale.US, "%.1f ГБ", gb)
            mb >= 1.0 -> String.format(Locale.US, "%.1f МБ", mb)
            else -> String.format(Locale.US, "%.0f КБ", kb)
        }
    }
}
