package com.pokemonza.companion.data.repository

import android.content.Context
import com.pokemonza.companion.data.model.PokemonEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

class PokemonRepository(private val context: Context) {
    private var cached: List<PokemonEntry>? = null

    suspend fun fetchPokemon(): List<PokemonEntry> {
        cached?.let { return it }
        return loadBundled().also { cached = it }
    }

    fun clearCache() {
        cached = null
    }

    fun fetchPokemonDetail(entry: PokemonEntry): String =
        entry.detailSummary.ifBlank { "${entry.name} appears in Pokémon Legends: Z-A." }

    private suspend fun loadBundled(): List<PokemonEntry> = withContext(Dispatchers.IO) {
        val json = context.assets.open("pokemon_za.json").bufferedReader().readText()
        val arr: JSONArray = org.json.JSONObject(json).getJSONArray("pokemon")
        buildList {
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val typesArr = o.getJSONArray("types")
                val types = buildList {
                    for (t in 0 until typesArr.length()) add(typesArr.getString(t))
                }
                add(
                    PokemonEntry(
                        nationalDex = o.getString("nationalDex"),
                        lumioseDex = o.optString("lumioseDex").takeIf { it.isNotBlank() && it != "null" },
                        hyperspaceDex = o.optString("hyperspaceDex").takeIf { it.isNotBlank() && it != "null" },
                        name = o.getString("name"),
                        types = types,
                        spriteAsset = o.optString("spriteAsset").takeIf { it.isNotBlank() && it != "null" },
                        normallyAvailable = o.optBoolean("normallyAvailable", true),
                        detailSummary = o.optString("detail", "")
                    )
                )
            }
        }
    }
}
