package com.pokemonza.companion.data.repository

import com.pokemonza.companion.data.model.GuideEntry

class GuideRepository {
    fun fetchGuides(): List<GuideEntry> = listOf(
        GuideEntry("Lumiose City Map", "In-App", "Bundled Lumiose map — open the Map tab.", ""),
        GuideEntry("Trainer Fashion", "In-App", "1,100+ outfits — open the Fashion tab.", ""),
        GuideEntry("All Missions", "In-App", "259 missions (main, side, hyperspace) — open the Missions tab.", ""),
        GuideEntry("Mega Stones", "In-App", "Mega Stone locations — use the Map tab.", "")
    )
}
