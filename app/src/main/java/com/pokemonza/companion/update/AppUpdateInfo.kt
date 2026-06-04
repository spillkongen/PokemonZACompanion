package com.pokemonza.companion.update

data class AppUpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val releaseNotes: String,
    val apkDownloadUrl: String
)
