package com.aura.reader.data.updater

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val isAvailable: Boolean,
    val latestVersion: String,
    val changelog: String,
    val releaseUrl: String
)

object AppUpdateManager {

    private const val GITHUB_REPO_URL =
        "https://github.com/Kouqqu/aura-reader"
    private const val GITHUB_LATEST_WEB_URL =
        "https://github.com/Kouqqu/aura-reader/releases/latest"
    private const val GITHUB_API_LATEST_RELEASE =
        "https://api.github.com/repos/Kouqqu/aura-reader/releases/latest"

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
                                }
                            }

                            if (bestTag.isNotBlank()) {
                                tagName = bestTag
                                body = bestBody
                                releasePageUrl = bestUrl
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
                                releaseUrl = releasePageUrl
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
                        releaseUrl = releasePageUrl
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
