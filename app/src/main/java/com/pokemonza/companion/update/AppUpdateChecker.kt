package com.pokemonza.companion.update

import com.pokemonza.companion.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AppUpdateChecker(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
) {
    fun isConfigured(): Boolean {
        val owner = BuildConfig.GITHUB_REPO_OWNER
        return owner.isNotBlank() && owner != "YOUR_GITHUB_USERNAME"
    }

    suspend fun checkForUpdate(currentVersionCode: Int): AppUpdateInfo? = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext null

        val owner = BuildConfig.GITHUB_REPO_OWNER
        val repo = BuildConfig.GITHUB_REPO_NAME

        val versionJson = fetchVersionJson(owner, repo) ?: return@withContext null
        val remoteCode = versionJson.optInt("versionCode", 0)
        if (remoteCode <= currentVersionCode) return@withContext null

        val apkUrl = versionJson.optString("apkUrl").takeIf { it.isNotBlank() }
            ?: fetchLatestApkUrl(owner, repo)
            ?: return@withContext null

        AppUpdateInfo(
            versionCode = remoteCode,
            versionName = versionJson.optString("versionName", "New version"),
            releaseNotes = versionJson.optString("releaseNotes", "A new version is available."),
            apkDownloadUrl = apkUrl
        )
    }

    private fun fetchVersionJson(owner: String, repo: String): JSONObject? {
        val url = "https://raw.githubusercontent.com/$owner/$repo/main/version.json"
        return fetchJson(url)
    }

    private fun fetchLatestApkUrl(owner: String, repo: String): String? {
        val url = "https://api.github.com/repos/$owner/$repo/releases/latest"
        val json = fetchJson(url) ?: return null
        val assets = json.optJSONArray("assets") ?: return null
        for (i in 0 until assets.length()) {
            val asset = assets.getJSONObject(i)
            val name = asset.optString("name", "")
            if (name.endsWith(".apk", ignoreCase = true)) {
                return asset.optString("browser_download_url").takeIf { it.isNotBlank() }
            }
        }
        return null
    }

    private fun fetchJson(url: String): JSONObject? {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "PokemonZACompanion/${BuildConfig.VERSION_NAME}")
            .header("Accept", "application/vnd.github+json")
            .build()
        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body?.string() ?: return null
                JSONObject(body)
            }
        } catch (_: Exception) {
            null
        }
    }
}
