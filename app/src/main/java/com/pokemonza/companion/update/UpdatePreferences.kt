package com.pokemonza.companion.update

import android.content.Context

class UpdatePreferences(context: Context) {
    private val prefs = context.getSharedPreferences("app_updates", Context.MODE_PRIVATE)

    fun getDismissedVersionCode(): Int = prefs.getInt(KEY_DISMISSED, 0)

    fun setDismissedVersionCode(versionCode: Int) {
        prefs.edit().putInt(KEY_DISMISSED, versionCode).apply()
    }

    companion object {
        private const val KEY_DISMISSED = "dismissed_version_code"
    }
}
