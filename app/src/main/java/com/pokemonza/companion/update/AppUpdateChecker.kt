package com.pokemonza.companion.update

import com.pokemonza.companion.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class UpdateCheckResult {
    data class Available(val info: AppUpdateInfo) : UpdateCheckResult()
    data object UpToDate : UpdateCheckResult()
    data object NotConfigured : UpdateCheckResult()
    data class Failed(val message: String) : UpdateCheckResult()
}

class AppUpdateChecker(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(35, TimeUnit.SECONDS)
        .build()
) {
    fun isConfigured(): Boolean {
        val owner = BuildConfig.GITHUB_REPO_OWNER
        return owner.isNotBlank() && owner != "YOUR_GITHUB_USERNAME"
    }

    suspend fun checkForUpdate(currentVersionCode: Int): UpdateCheckResult = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext UpdateCheckResult.NotConfigured

        val owner = BuildConfig.GITHUB_REPO_OWNER
        val repo = BuildConfig.GITHUB_REPO_NAME

        repeat(3) { attempt ->
            val versionJson = fetchVersionJson(owner, repo)
            if (versionJson != null) {
                val remoteCode = versionJson.optInt("versionCode", 0)
                if (remoteCode <= currentVersionCode) {
                    return@withContext UpdateCheckResult.UpToDate
                }
                val apkUrl = resolveApkUrl(
                    owner = owner,
                    repo = repo,
                    preferredUrl = versionJson.optString("apkUrl").takeIf { it.isNotBlank() }
                )
                if (apkUrl == null) {
                    return@withContext UpdateCheckResult.Failed("No APK found on GitHub release")
                }
                return@withContext UpdateCheckResult.Available(
                    AppUpdateInfo(
                        versionCode = remoteCode,
                        versionName = versionJson.optString("versionName", "New version"),
                        releaseNotes = versionJson.optString("releaseNotes", "A new version is available."),
                        apkDownloadUrl = apkUrl
                    )
                )
            }
            if (attempt < 2) delay(1500)
        }
        UpdateCheckResult.Failed("Could not reach GitHub. Check your internet connection.")
    }

    private fun fetchVersionJson(owner: String, repo: String): JSONObject? {
        val cacheBust = System.currentTimeMillis()
        val url = "https://raw.githubusercontent.com/$owner/$repo/main/version.json?t=$cacheBust"
        return fetchJson(url, githubApi = false)
    }

    private fun resolveApkUrl(owner: String, repo: String, preferredUrl: String?): String? {
        if (preferredUrl != null && urlExists(preferredUrl)) return preferredUrl
        return fetchLatestApkUrl(owner, repo)
    }

    private fun urlExists(url: String): Boolean {
        val request = Request.Builder()
            .url(url)
            .head()
            .header("User-Agent", "PokemonZACompanion/${BuildConfig.VERSION_NAME}")
            .build()
        return try {
            client.newCall(request).execute().use { it.isSuccessful }
        } catch (_: Exception) {
            false
        }
    }

    private fun fetchLatestApkUrl(owner: String, repo: String): String? {
        val url = "https://api.github.com/repos/$owner/$repo/releases/latest"
        val json = fetchJson(url, githubApi = true) ?: return null
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

    private fun fetchJson(url: String, githubApi: Boolean): JSONObject? {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "PokemonZACompanion/${BuildConfig.VERSION_NAME}")
            .header("Cache-Control", "no-cache")
            .apply {
                if (githubApi) header("Accept", "application/vnd.github+json")
            }
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
