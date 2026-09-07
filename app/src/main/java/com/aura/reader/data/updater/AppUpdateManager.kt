package com.aura.reader.data.updater

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val isAvailable: Boolean,
    val latestVersion: String,
    val changelog: String,
    val downloadUrl: String
)

object AppUpdateManager {

    private const val GITHUB_LATEST_WEB_URL =
        "https://github.com/Kouqqu/aura-reader/releases/latest"
    private const val GITHUB_API_LATEST_RELEASE =
        "https://api.github.com/repos/Kouqqu/aura-reader/releases/latest"

    suspend fun checkForUpdates(currentVersion: String = "v1.0.0"): Result<UpdateInfo?> =
        withContext(Dispatchers.IO) {
            try {
                var tagName = ""
                var body = ""
                var apkDownloadUrl = ""

                // 1. First, check latest tag via GitHub redirect (100% rate-limit-free)
                try {
                    val webUrl = URL(GITHUB_LATEST_WEB_URL)
                    val webConn = webUrl.openConnection() as HttpURLConnection
                    webConn.instanceFollowRedirects = false
                    webConn.requestMethod = "GET"
                    webConn.setRequestProperty("User-Agent", "AuraReader-App")
                    webConn.connectTimeout = 6000
                    webConn.readTimeout = 6000

                    val location = webConn.getHeaderField("Location")
                    if (!location.isNullOrBlank()) {
                        tagName = location.substringAfterLast("/").trim()
                    }
                } catch (e: Exception) {}

                // 2. Try fetching changelog from API if possible
                try {
                    val url = URL(GITHUB_API_LATEST_RELEASE)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "GET"
                    conn.setRequestProperty("User-Agent", "AuraReader-App")
                    conn.connectTimeout = 6000
                    conn.readTimeout = 6000

                    if (conn.responseCode == 200) {
                        val jsonStr = conn.inputStream.bufferedReader().use { it.readText() }
                        val json = JSONObject(jsonStr)
                        if (tagName.isBlank()) {
                            tagName = json.optString("tag_name", "").trim()
                        }
                        body = json.optString("body", "").trim()

                        val assets = json.optJSONArray("assets")
                        if (assets != null) {
                            for (i in 0 until assets.length()) {
                                val asset = assets.getJSONObject(i)
                                val name = asset.optString("name", "")
                                if (name.endsWith(".apk", ignoreCase = true)) {
                                    apkDownloadUrl = asset.optString("browser_download_url", "")
                                    break
                                }
                            }
                        }
                    }
                } catch (e: Exception) {}

                if (tagName.isBlank()) {
                    return@withContext Result.success(null)
                }

                if (apkDownloadUrl.isBlank()) {
                    apkDownloadUrl = "https://github.com/Kouqqu/aura-reader/releases/download/$tagName/AuraReader.apk"
                }

                val cleanLatest = tagName.removePrefix("v").trim()
                val cleanCurrent = currentVersion.removePrefix("v").trim()
                val isNewer = isVersionNewer(cleanLatest, cleanCurrent)

                Result.success(
                    UpdateInfo(
                        isAvailable = isNewer,
                        latestVersion = tagName,
                        changelog = body.ifBlank { "Вышла новая версия $tagName с улучшениями и новыми функциями." },
                        downloadUrl = apkDownloadUrl
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    fun showUpdateNotification(context: Context, latestVersion: String) {
        try {
            val channelId = "app_updates"
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = android.app.NotificationChannel(
                    channelId,
                    "Обновления Aura Reader",
                    android.app.NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Уведомления о выходе новых версий"
                }
                notificationManager.createNotificationChannel(channel)
            }

            val intent = Intent(context, com.aura.reader.MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = android.app.PendingIntent.getActivity(
                context,
                0,
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )

            val builder = androidx.core.app.NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle("Доступно обновление Aura Reader!")
                .setContentText("Вышла новая версия $latestVersion. Нажмите для обновления.")
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            notificationManager.notify(1001, builder.build())
        } catch (e: Exception) {}
    }

    private fun isVersionNewer(latest: String, current: String): Boolean {
        if (latest.isBlank()) return false
        val latestParts = latest.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }

        val maxLen = maxOf(latestParts.size, currentParts.size)
        for (i in 0 until maxLen) {
            val l = latestParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }

    suspend fun downloadAndInstallApk(
        context: Context,
        downloadUrl: String,
        onProgress: (Float) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val url = URL(downloadUrl)
            val conn = url.openConnection() as HttpURLConnection
            conn.instanceFollowRedirects = true
            conn.connectTimeout = 15000
            conn.readTimeout = 30000

            // Handle HTTP 301 / 302 redirects from GitHub
            var redirectConn = conn
            var status = conn.responseCode
            if (status == HttpURLConnection.HTTP_MOVED_TEMP ||
                status == HttpURLConnection.HTTP_MOVED_PERM ||
                status == HttpURLConnection.HTTP_SEE_OTHER
            ) {
                val newUrl = conn.getHeaderField("Location")
                redirectConn = URL(newUrl).openConnection() as HttpURLConnection
            }

            val totalBytes = redirectConn.contentLength
            val apkFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir, "AuraReader-update.apk")
            if (apkFile.exists()) apkFile.delete()

            redirectConn.inputStream.use { input ->
                FileOutputStream(apkFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalRead = 0L

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        if (totalBytes > 0) {
                            val progress = totalRead.toFloat() / totalBytes
                            withContext(Dispatchers.Main) {
                                onProgress(progress)
                            }
                        }
                    }
                }
            }

            // Launch package installer on Main thread
            withContext(Dispatchers.Main) {
                val apkUri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    apkFile
                )

                val installIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(apkUri, "application/vnd.android.package-archive")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                context.startActivity(installIntent)
            }

            Result.success(apkFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
