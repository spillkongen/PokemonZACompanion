package com.pokemonza.companion.data.repository

import com.pokemonza.companion.data.model.GuideEntry
import com.pokemonza.companion.data.network.AppConstants

class GuideRepository {
    fun fetchGuides(): List<GuideEntry> = listOf(
        GuideEntry("Lumiose City Map", "Interactive Map", "MapGenie map with collectibles, missions, and Pokémon.", AppConstants.MAP_GENIE_LUMIOSE),
        GuideEntry("Trainer Fashion", "In-App", "All 1,100+ clothing items from Serebii.", ""),
        GuideEntry("All Missions", "In-App", "259 missions (main, side, hyperspace) — open the Missions tab.", ""),
        GuideEntry("Mega Stones", "Items", "Every Mega Stone location.", "${AppConstants.MAP_GENIE_BASE}/guides/mega-stones")
    )
}
