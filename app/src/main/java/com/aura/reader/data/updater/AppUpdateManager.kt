package com.aura.reader.data.updater

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val isAvailable: Boolean,
    val latestVersion: String,
    val changelog: String,
    val releaseUrl: String,
    val apkUrl: String? = null,
    val apkSizeBytes: Long? = null
)

/** How the system installer answered the request to open the downloaded APK. */
enum class ApkInstallResult {
    /** The package installer was opened; the app will be replaced when the user accepts. */
    STARTED,
    /** Android requires the user to allow installing unknown apps from Aura Reader first. */
    PERMISSION_REQUIRED,
    FAILED
}

object AppUpdateManager {

    private const val GITHUB_REPO_URL =
        "https://github.com/Kouqqu/aura-reader"
    private const val GITHUB_LATEST_WEB_URL =
        "https://github.com/Kouqqu/aura-reader/releases/latest"
    private const val GITHUB_API_LATEST_RELEASE =
        "https://api.github.com/repos/Kouqqu/aura-reader/releases/latest"
    private const val GITHUB_DOWNLOAD_BASE =
        "https://github.com/Kouqqu/aura-reader/releases/download"
    private const val APK_ASSET_NAME = "AuraReader.apk"
    private const val UPDATE_CACHE_DIR = "updates"

    fun openReleasePage(context: Context, url: String? = null) {
        try {
            val targetUrl = if (!url.isNullOrBlank()) url else GITHUB_LATEST_WEB_URL
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // No activity to handle intent
        }
    }

    suspend fun checkForUpdates(currentVersion: String = "v1.0.0", isBetaChannel: Boolean = false): Result<UpdateInfo?> =
        withContext(Dispatchers.IO) {
            try {
                var tagName = ""
                var body = ""
                var releasePageUrl = ""
                var apkUrl: String? = null
                var apkSizeBytes: Long? = null

                if (isBetaChannel) {
                    try {
                        val url = URL("https://api.github.com/repos/Kouqqu/aura-reader/releases?per_page=15")
                        val conn = url.openConnection() as HttpURLConnection
                        conn.requestMethod = "GET"
                        conn.setRequestProperty("User-Agent", "AuraReader-App")
                        conn.connectTimeout = 6000
                        conn.readTimeout = 6000
                        if (conn.responseCode == 200) {
                            val jsonStr = conn.inputStream.bufferedReader().use { it.readText() }
                            val releasesArray = org.json.JSONArray(jsonStr)
                            var bestTag = ""
                            var bestBody = ""
                            var bestUrl = ""
                            var bestApkUrl: String? = null
                            var bestApkSize: Long? = null

                            for (rIdx in 0 until releasesArray.length()) {
                                val rel = releasesArray.getJSONObject(rIdx)
                                val tag = rel.optString("tag_name", "").trim()
                                if (tag.isBlank()) continue

                                val htmlUrl = rel.optString("html_url", "").trim()
                                val tagUrl = if (htmlUrl.isNotBlank()) htmlUrl else "https://github.com/Kouqqu/aura-reader/releases/tag/$tag"

                                if (bestTag.isBlank() || isVersionNewer(tag, bestTag)) {
                                    bestTag = tag
                                    bestBody = rel.optString("body", "").trim()
                                    bestUrl = tagUrl
                                    val asset = findApkAsset(rel)
                                    bestApkUrl = asset?.first
                                    bestApkSize = asset?.second
                                }
                            }

                            if (bestTag.isNotBlank()) {
                                tagName = bestTag
                                body = bestBody
                                releasePageUrl = bestUrl
                                apkUrl = bestApkUrl
                                apkSizeBytes = bestApkSize
                            }
                        }
                    } catch (e: Exception) {}

                    if (tagName.isNotBlank()) {
                        if (releasePageUrl.isBlank()) {
                            releasePageUrl = "https://github.com/Kouqqu/aura-reader/releases/tag/$tagName"
                        }
                        val isNewer = isVersionNewer(tagName, currentVersion)
                        return@withContext Result.success(
                            UpdateInfo(
                                isAvailable = isNewer,
                                latestVersion = tagName,
                                changelog = body.ifBlank { "Сборка $tagName Aura Reader с новыми улучшениями." },
                                releaseUrl = releasePageUrl,
                                apkUrl = apkUrl ?: fallbackApkUrl(tagName),
                                apkSizeBytes = apkSizeBytes
                            )
                        )
                    }
                }

                // Stable channel:
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
                        val htmlUrl = json.optString("html_url", "").trim()
                        if (htmlUrl.isNotBlank()) {
                            releasePageUrl = htmlUrl
                        }
                        val asset = findApkAsset(json)
                        apkUrl = asset?.first
                        apkSizeBytes = asset?.second
                    }
                } catch (e: Exception) {}

                if (tagName.isBlank()) {
                    return@withContext Result.success(null)
                }

                if (releasePageUrl.isBlank()) {
                    releasePageUrl = GITHUB_LATEST_WEB_URL
                }

                val cleanLatest = tagName.removePrefix("v").trim()
                val cleanCurrent = currentVersion.removePrefix("v").trim()
                val isNewer = isVersionNewer(cleanLatest, cleanCurrent)

                Result.success(
                    UpdateInfo(
                        isAvailable = isNewer,
                        latestVersion = tagName,
                        changelog = body.ifBlank { "Вышла новая версия $tagName с улучшениями и новыми функциями." },
                        releaseUrl = releasePageUrl,
                        apkUrl = apkUrl ?: fallbackApkUrl(tagName),
                        apkSizeBytes = apkSizeBytes
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

    /** The first `.apk` asset of a release, with its size when GitHub reports one. */
    private fun findApkAsset(release: JSONObject): Pair<String, Long?>? {
        val assets = release.optJSONArray("assets") ?: return null
        for (index in 0 until assets.length()) {
            val asset = assets.optJSONObject(index) ?: continue
            val name = asset.optString("name", "")
            val url = asset.optString("browser_download_url", "")
            if (name.endsWith(".apk", ignoreCase = true) && url.isNotBlank()) {
                return url to asset.optLong("size", 0L).takeIf { it > 0L }
            }
        }
        return null
    }

    /** GitHub's predictable asset URL, for when the API was rate-limited. */
    fun fallbackApkUrl(tag: String): String =
        "$GITHUB_DOWNLOAD_BASE/${tag.trim()}/$APK_ASSET_NAME"

    /**
     * Downloads the release APK into the app's cache.
     *
     * The file lives in `cacheDir/updates` so Android may reclaim it if the
     * install never happens. Every previous download is deleted first, and the
     * next app start wipes the folder again (see [cleanupDownloadedApks]) so a
     * finished update does not leave an APK behind.
     */
    suspend fun downloadApk(
        context: Context,
        info: UpdateInfo,
        onProgress: (Float?) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        var target: File? = null
        try {
            val urlString = info.apkUrl ?: fallbackApkUrl(info.latestVersion)
            val directory = File(context.cacheDir, UPDATE_CACHE_DIR).apply { mkdirs() }
            directory.listFiles()?.forEach { it.delete() }
            val safeVersion = info.latestVersion
                .removePrefix("v")
                .replace(Regex("[^A-Za-z0-9._-]"), "_")
            val apkFile = File(directory, "aura-$safeVersion.apk")
            target = apkFile

            onProgress(null)
            val connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = true
                requestMethod = "GET"
                setRequestProperty("User-Agent", "AuraReader-App")
                setRequestProperty(
                    "Accept",
                    "application/vnd.android.package-archive, application/octet-stream, */*"
                )
                connectTimeout = 15_000
                readTimeout = 30_000
            }

            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                throw IOException("HTTP $responseCode")
            }

            val totalBytes = connection.contentLengthLong
            connection.inputStream.use { input ->
                apkFile.outputStream().use { output ->
                    val buffer = ByteArray(64 * 1024)
                    var downloaded = 0L
                    var lastPercent = -1
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        output.write(buffer, 0, read)
                        downloaded += read
                        if (totalBytes > 0L) {
                            val percent = ((downloaded * 100L) / totalBytes).toInt()
                            if (percent != lastPercent) {
                                lastPercent = percent
                                onProgress(
                                    (downloaded.toFloat() / totalBytes.toFloat())
                                        .coerceIn(0f, 1f)
                                )
                            }
                        }
                    }
                    output.flush()
                }
            }

            if (apkFile.length() <= 0L) throw IOException("Received an empty file")
            if (totalBytes > 0L && apkFile.length() != totalBytes) {
                throw IOException("Download stopped at ${apkFile.length()} of $totalBytes bytes")
            }
            // An APK is a ZIP container; reject an HTML error page saved as .apk.
            val signature = ByteArray(2)
            apkFile.inputStream().use { it.read(signature) }
            if (signature[0] != 'P'.code.toByte() || signature[1] != 'K'.code.toByte()) {
                throw IOException("The downloaded file is not an APK")
            }

            onProgress(1f)
            Result.success(apkFile)
        } catch (error: Exception) {
            try {
                target?.delete()
            } catch (_: Exception) {
            }
            Result.failure(error)
        }
    }

    /**
     * Hands the downloaded APK to Android's package installer. When the user has
     * not allowed this app to install packages yet, their settings page opens
     * instead and the caller can simply offer the button again.
     */
    fun installApk(context: Context, file: File): ApkInstallResult {
        return try {
            if (!context.packageManager.canRequestPackageInstalls()) {
                val settingsIntent = Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:${context.packageName}")
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(settingsIntent)
                return ApkInstallResult.PERMISSION_REQUIRED
            }

            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(installIntent)
            ApkInstallResult.STARTED
        } catch (error: Exception) {
            ApkInstallResult.FAILED
        }
    }

    /**
     * Deletes APKs and temporary files left in cache. Called on every app start: once the
     * installer has replaced the app, the old download is dead weight.
     */
    fun cleanupDownloadedApks(context: Context) {
        try {
            com.aura.reader.data.storage.AppStorageManager.cleanupTemporaryFiles(context)
        } catch (_: Exception) {
        }
    }

    fun isVersionNewer(latest: String, current: String): Boolean {
        val cleanL = latest.trim().removePrefix("v").removePrefix("V")
        val cleanC = current.trim().removePrefix("v").removePrefix("V")
        if (cleanL.isBlank()) return false
        if (cleanL.equals(cleanC, ignoreCase = true)) return false

        fun parseVersion(v: String): Triple<List<Int>, Boolean, Int> {
            val isBeta = v.contains("beta", ignoreCase = true)
            val parts = v.split(".", "-", "_")
            val numbers = mutableListOf<Int>()
            var betaNum = if (isBeta) 1 else 0
            for (p in parts) {
                val digits = p.filter { it.isDigit() }
                if (digits.isNotBlank()) {
                    val num = digits.toIntOrNull() ?: 0
                    if (numbers.size < 3) {
                        numbers.add(num)
                    } else if (isBeta) {
                        betaNum = num
                    }
                }
            }
            while (numbers.size < 3) numbers.add(0)
            return Triple(numbers, isBeta, betaNum)
        }

        val (lNums, lIsBeta, lBetaNum) = parseVersion(cleanL)
        val (cNums, cIsBeta, cBetaNum) = parseVersion(cleanC)

        for (i in 0 until maxOf(lNums.size, cNums.size)) {
            val l = lNums.getOrElse(i) { 0 }
            val c = cNums.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }

        if (!lIsBeta && cIsBeta) return true
        if (lIsBeta && !cIsBeta) return false
        if (lIsBeta && cIsBeta) {
            return lBetaNum > cBetaNum
        }

        return false
    }
}
