package com.ldreams.app.service

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
    private const val LAST_CHECKED_ID = "last_checked_release_id"
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences("ldreams_update", Context.MODE_PRIVATE)
    }

    suspend fun checkForUpdate(): AppUpdate? = withContext(Dispatchers.IO) {
        try {
            val conn = URL(GITHUB_API).openConnection() as HttpURLConnection
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json")
            conn.connectTimeout = 10000
            conn.readTimeout = 10000

            val body = conn.inputStream.bufferedReader().readText()
            conn.disconnect()

            val json = JSONObject(body)
            val tagName = json.getString("tag_name") // "v1.0.0-20260514-1234"
            val releaseId = json.getLong("id") // monotonically increasing
            val version = tagName.removePrefix("v").substringBefore("-")
            val bodyText = json.getString("body")
            val assets = json.getJSONArray("assets")
            val downloadUrl = if (assets.length() > 0) {
                assets.getJSONObject(0).getString("browser_download_url")
            } else ""

            // Compare using release ID (always increases with each release)
            val lastCheckedId = getLastCheckedReleaseId()
            val isAvailable = releaseId > lastCheckedId

            if (isAvailable) {
                saveLastCheckedReleaseId(releaseId)
            }

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

    private fun getLastCheckedReleaseId(): Long {
        return prefs.getLong(LAST_CHECKED_ID, 0L)
    }

    private fun saveLastCheckedReleaseId(id: Long) {
        prefs.edit().putLong(LAST_CHECKED_ID, id).apply()
    }

    fun openDownloadPage(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }
}
