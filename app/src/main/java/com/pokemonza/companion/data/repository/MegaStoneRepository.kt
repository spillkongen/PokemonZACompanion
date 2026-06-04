package com.pokemonza.companion.data.repository

import android.content.Context
import com.pokemonza.companion.data.model.MegaStoneEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MegaStoneRepository(private val context: Context) {
    suspend fun loadStones(): List<MegaStoneEntry> = withContext(Dispatchers.IO) {
        val json = context.assets.open("mega_stones_serebii.json").bufferedReader().readText()
        val arr = org.json.JSONObject(json).getJSONArray("stones")
        buildList {
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                add(
                    MegaStoneEntry(
                        stone = o.getString("stone"),
                        effect = o.optString("effect", ""),
                        location = o.getString("location")
                    )
                )
            }
        }
    }
}
