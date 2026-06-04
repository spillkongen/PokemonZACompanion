package com.pokemonza.companion.data.network

import android.content.Context
import java.io.IOException

object OfflineAssets {
    fun assetUri(relativePath: String): String = "file:///android_asset/$relativePath"

    fun exists(context: Context, relativePath: String): Boolean = try {
        context.assets.open(relativePath).close()
        true
    } catch (_: IOException) {
        false
    }

    fun fashionThumbUri(context: Context, previewKey: String?, thumbAsset: String?): String? {
        thumbAsset?.takeIf { it.isNotBlank() }?.let { if (exists(context, it)) return assetUri(it) }
        previewKey?.takeIf { it.isNotBlank() }?.let { key ->
            val path = "fashion/th/$key.jpg"
            if (exists(context, path)) return assetUri(path)
        }
        return null
    }

    fun fashionFullUri(context: Context, previewKey: String?, fullAsset: String?, thumbAsset: String?): String? {
        fullAsset?.takeIf { it.isNotBlank() }?.let { if (exists(context, it)) return assetUri(it) }
        previewKey?.takeIf { it.isNotBlank() }?.let { key ->
            val path = "fashion/full/$key.jpg"
            if (exists(context, path)) return assetUri(path)
        }
        return fashionThumbUri(context, previewKey, thumbAsset)
    }

    fun fashionFemaleThumbUri(
        context: Context,
        previewKey: String?,
        femaleThumbAsset: String?,
        fullAsset: String?,
        thumbAsset: String?
    ): String? {
        femaleThumbAsset?.takeIf { it.isNotBlank() }?.let { if (exists(context, it)) return assetUri(it) }
        previewKey?.takeIf { it.isNotBlank() }?.let { key ->
            val path = "fashion/th_f/$key.jpg"
            if (exists(context, path)) return assetUri(path)
        }
        // Serebii has no separate female thumbs for most items; full preview is clearer.
        return fashionFullUri(context, previewKey, fullAsset, thumbAsset)
            ?: fashionThumbUri(context, previewKey, thumbAsset)
    }

    fun pokemonSpriteUri(context: Context, spriteAsset: String?, nationalDex: String): String? {
        spriteAsset?.takeIf { it.isNotBlank() }?.let { if (exists(context, it)) return assetUri(it) }
        val path = "pokemon/sprites/${nationalDex.padStart(4, '0')}.png"
        if (exists(context, path)) return assetUri(path)
        return null
    }
}
