package com.ldreams.app.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class AppUpdate(
    val latestVersion: String,
    val downloadUrl: String,
    val releaseNotes: String,
    val isAvailable: Boolean
)

object UpdateChecker {
    private const val GITHUB_API = "https://api.github.com/repos/ashuri-17/LDREAMS/releases/latest"
    private const val CURRENT_VERSION = "1.0.0"

    suspend fun checkForUpdate(): AppUpdate? = withContext(Dispatchers.IO) {
        try {
            val conn = URL(GITHUB_API).openConnection() as HttpURLConnection
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json")
            conn.connectTimeout = 10000
            conn.readTimeout = 10000

            val body = conn.inputStream.bufferedReader().readText()
            conn.disconnect()

            val json = JSONObject(body)
            val tagName = json.getString("tag_name") // "v1.0.0-20260514"
            val version = tagName.removePrefix("v").substringBefore("-")
            val bodyText = json.getString("body")
            val assets = json.getJSONArray("assets")
            val downloadUrl = if (assets.length() > 0) {
                assets.getJSONObject(0).getString("browser_download_url")
            } else ""

            val isAvailable = compareVersions(version, CURRENT_VERSION) > 0

            AppUpdate(
                latestVersion = version,
                downloadUrl = downloadUrl,
                releaseNotes = bodyText.take(500),
                isAvailable = isAvailable
            )
        } catch (e: Exception) {
            null
        }
    }

    fun openDownloadPage(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }

    private fun compareVersions(v1: String, v2: String): Int {
        val parts1 = v1.split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = v2.split(".").map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(parts1.size, parts2.size)) {
            val diff = (parts1.getOrElse(i) { 0 }) - (parts2.getOrElse(i) { 0 })
            if (diff != 0) return diff
        }
        return 0
    }
}
