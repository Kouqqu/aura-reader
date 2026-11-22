package com.aura.reader.util

import android.content.Context
import android.os.Build
import com.aura.reader.BuildConfig
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CrashEntry(
    val file: File,
    val time: Long,
    val title: String,
    val content: String
)

object CrashLog {
    private const val MAX_CRASH_LOGS = 10
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private var isInstalled = false

    fun install(context: Context) {
        if (isInstalled) return
        isInstalled = true
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching {
                recordCrash(context.applicationContext, thread, throwable)
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun getCrashDir(context: Context): File {
        return File(context.filesDir, "crashes").apply { if (!exists()) mkdirs() }
    }

    private fun recordCrash(context: Context, thread: Thread, throwable: Throwable) {
        val dir = getCrashDir(context)
        val timestamp = System.currentTimeMillis()
        val crashFile = File(dir, "crash-$timestamp.txt")

        val sb = StringBuilder()
        sb.appendLine("=== Aura Reader Crash Report ===")
        sb.appendLine("Timestamp: ${dateFormat.format(Date(timestamp))}")
        sb.appendLine("Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        sb.appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL} (${Build.DEVICE})")
        sb.appendLine("Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
        sb.appendLine("Thread: ${thread.name}")
        sb.appendLine("Exception: ${throwable.javaClass.name}: ${throwable.message}")
        sb.appendLine("----------------------------------------")
        sb.appendLine(throwable.stackTraceToString())

        crashFile.writeText(sb.toString())

        // Prune older crash logs beyond MAX_CRASH_LOGS
        dir.listFiles { f -> f.name.startsWith("crash-") && f.name.endsWith(".txt") }
            ?.sortedByDescending { it.lastModified() }
            ?.drop(MAX_CRASH_LOGS)
            ?.forEach { it.delete() }
    }

    fun loadEntries(context: Context): List<CrashEntry> {
        val dir = getCrashDir(context)
        return dir.listFiles { f -> f.name.startsWith("crash-") && f.name.endsWith(".txt") }
            .orEmpty()
            .sortedByDescending { it.lastModified() }
            .mapNotNull { file ->
                val time = file.name.removePrefix("crash-").removeSuffix(".txt").toLongOrNull() ?: file.lastModified()
                val text = runCatching { file.readText() }.getOrNull() ?: return@mapNotNull null
                val firstLine = text.lines().firstOrNull { it.startsWith("Exception:") }?.removePrefix("Exception:")?.trim()
                    ?: file.name
                CrashEntry(file, time, firstLine, text)
            }
    }

    fun clear(context: Context) {
        val dir = getCrashDir(context)
        dir.listFiles()?.forEach { it.delete() }
    }

    fun generateFullReport(context: Context): String {
        val entries = loadEntries(context)
        if (entries.isEmpty()) return ""
        val sb = StringBuilder()
        sb.appendLine("=== Aura Reader Crash Report ===")
        sb.appendLine("Total crashes: ${entries.size}")
        sb.appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE}, SDK ${Build.VERSION.SDK_INT})")
        sb.appendLine("App Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        sb.appendLine("========================================\n")
        entries.forEachIndexed { index, entry ->
            sb.appendLine("--- Crash #${index + 1} (${dateFormat.format(Date(entry.time))}) ---")
            sb.appendLine(entry.content)
            sb.appendLine()
        }
        return sb.toString().trim()
    }
}
