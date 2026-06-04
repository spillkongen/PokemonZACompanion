package com.pokemonza.companion.data.repository

import com.pokemonza.companion.data.model.GuideEntry
import com.pokemonza.companion.data.model.MissionEntry
import com.pokemonza.companion.data.model.MissionType
import com.pokemonza.companion.data.model.PokemonEntry
import com.pokemonza.companion.data.network.AppConstants
import com.pokemonza.companion.data.network.BulbapediaClient
import com.pokemonza.companion.data.network.WikiParsers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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

class MissionRepository(private val client: BulbapediaClient = BulbapediaClient()) {
    suspend fun fetchMissions(): List<MissionEntry> = coroutineScope {
        val main = async {
            runCatching {
                WikiParsers.parseMissionTables(
                    client.fetchPageHtml("List of missions in Pokémon Legends: Z-A"),
                    MissionType.MAIN
                )
            }.getOrDefault(emptyList())
        }
        val side = async {
            runCatching {
                WikiParsers.parseMissionTables(
                    client.fetchPageHtml("List of side missions in Pokémon Legends: Z-A"),
                    MissionType.SIDE
                )
            }.getOrDefault(emptyList())
        }
        val combined = main.await() + side.await()
        if (combined.isNotEmpty()) combined.map { it.copy(guide = missionGuide(it)) } else fallbackMissions()
    }

    suspend fun fetchMissionGuide(title: String, existing: String): String {
        if (existing.length > 80) return existing
        return try {
            val doc = client.fetchPageHtml(title)
            doc.select("p").map { it.text().trim() }
                .filter { it.length > 30 }
                .take(4)
                .joinToString("\n\n")
                .ifBlank { existing }
        } catch (_: Exception) {
            existing.ifBlank { "Complete this mission in Lumiose City. Check the interactive map for the exact location marker." }
        }
    }

    private fun missionGuide(m: MissionEntry): String = when {
        m.description.length > 60 -> m.description
        m.type == MissionType.MAIN -> "Main story mission in Pokémon Legends: Z-A. Follow the objective marker on your map and talk to NPCs in the area."
        else -> "Optional side mission. Explore the marked district in Lumiose City and interact with characters to progress."
    }

    private fun fallbackMissions() = listOf(
        MissionEntry("1", "Welcome to Lumiose City", MissionType.MAIN, "Begin your adventure in Lumiose.", AppConstants.BULBAPEDIA_BASE, "Start the game and follow the tutorial markers through the city center."),
        MissionEntry("2", "The Z-A Royale", MissionType.MAIN, "Enter the nightly battle tournament.", AppConstants.BULBAPEDIA_BASE, "Return to your hotel at night and register for the Z-A Royale battles."),
        MissionEntry("S1", "Fashion Forward", MissionType.SIDE, "Visit clothing shops in Lumiose.", AppConstants.BULBAPEDIA_BASE, "Explore shopping arcades and buy outfits at in-game boutiques listed in the Fashion tab.")
    )
}

class GuideRepository {
    fun fetchGuides(): List<GuideEntry> = listOf(
        GuideEntry("Lumiose City Map", "Interactive Map", "MapGenie map with collectibles, missions, and Pokémon.", AppConstants.MAP_GENIE_LUMIOSE),
        GuideEntry("Trainer Fashion", "In-Game", "All 1,100+ clothing items from Serebii.", AppConstants.SEREBII_FASHION_URL),
        GuideEntry("Main Missions", "Story", "Main story mission locations.", "${AppConstants.MAP_GENIE_BASE}/guides/main-missions"),
        GuideEntry("Mega Stones", "Items", "Every Mega Stone location.", "${AppConstants.MAP_GENIE_BASE}/guides/mega-stones")
    )
}
