package com.pokemonza.companion.data.repository

import com.pokemonza.companion.data.model.GuideEntry
import com.pokemonza.companion.data.model.PokemonEntry
import com.pokemonza.companion.data.network.AppConstants
import com.pokemonza.companion.data.network.BulbapediaClient
import com.pokemonza.companion.data.network.WikiParsers
class PokemonRepository(private val client: BulbapediaClient = BulbapediaClient()) {
    suspend fun fetchPokemon(): List<PokemonEntry> {
        val doc = client.fetchPageHtml("List of Pokémon in Pokémon Legends: Z-A")
        val parsed = WikiParsers.parsePokemonList(doc)
        return if (parsed.isNotEmpty()) parsed else fallbackPokemon()
    }

    suspend fun fetchPokemonDetail(name: String): String {
        return try {
            val page = name.replace(" ", "_") + "_(Pokémon)"
            val doc = client.fetchPageHtml(page)
            val paragraphs = doc.select("p").map { it.text().trim() }.filter { it.length > 40 }
            paragraphs.take(3).joinToString("\n\n").ifBlank {
                "National Dex Pokémon in Pokémon Legends: Z-A. Tap the wiki link for full stats and locations."
            }
        } catch (_: Exception) {
            "$name appears in Pokémon Legends: Z-A. Check Bulbapedia for encounter locations, types, and evolution info."
        }
    }

    private fun fallbackPokemon(): List<PokemonEntry> = listOf(
        entry("0001", "001", null, "Bulbasaur", listOf("Grass", "Poison")),
        entry("0004", "004", null, "Charmander", listOf("Fire")),
        entry("0007", "007", null, "Squirtle", listOf("Water")),
        entry("0025", "025", null, "Pikachu", listOf("Electric")),
        entry("0133", "133", null, "Eevee", listOf("Normal")),
        entry("0448", "044", null, "Lucario", listOf("Fighting", "Steel")),
        entry("0658", "058", null, "Greninja", listOf("Water", "Dark"))
    )

    private fun entry(ndex: String, lumiose: String?, hyperspace: String?, name: String, types: List<String>) =
        PokemonEntry(
            nationalDex = ndex,
            lumioseDex = lumiose,
            hyperspaceDex = hyperspace,
            name = name,
            types = types,
            imageUrl = "https://archives.bulbagarden.net/media/upload/thumb/7/7f/Menu_ZA_${ndex.padStart(4, '0')}.png/120px-Menu_ZA_${ndex.padStart(4, '0')}.png",
            normallyAvailable = true,
            wikiUrl = "${AppConstants.BULBAPEDIA_BASE}/wiki/${name.replace(" ", "_")}_(Pok%C3%A9mon)"
        )
}

class GuideRepository {
    fun fetchGuides(): List<GuideEntry> = listOf(
        GuideEntry("Lumiose City Map", "Interactive Map", "MapGenie map with collectibles, missions, and Pokémon.", AppConstants.MAP_GENIE_LUMIOSE),
        GuideEntry("Trainer Fashion", "In-App", "1,100+ outfits — open the Fashion tab (offline data).", ""),
        GuideEntry("All Missions", "In-App", "259 missions (main, side, hyperspace) — open the Missions tab (offline).", ""),
        GuideEntry("Mega Stones", "Items", "Every Mega Stone location on MapGenie.", "${AppConstants.MAP_GENIE_BASE}/guides/mega-stones")
    )
}
