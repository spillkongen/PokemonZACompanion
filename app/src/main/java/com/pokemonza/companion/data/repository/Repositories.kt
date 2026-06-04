package com.pokemonza.companion.data.repository

import com.pokemonza.companion.data.model.GuideEntry
import com.pokemonza.companion.data.model.GuideKind

class GuideRepository {
    fun fetchGuides(): List<GuideEntry> = listOf(
        GuideEntry("Lumiose City Map", "In-App", "Interactive MapGenie map — open the Map tab (needs internet).", guideKind = GuideKind.IN_APP_HINT),
        GuideEntry("Trainer Fashion", "In-App", "All 1,100+ clothing items — open the Fashion tab.", guideKind = GuideKind.IN_APP_HINT),
        GuideEntry("All Missions", "In-App", "259 missions with full walkthroughs — open the Missions tab.", guideKind = GuideKind.IN_APP_HINT),
        GuideEntry("Mega Stones", "Items", "91 Mega Stone locations scraped from Serebii.", guideKind = GuideKind.MEGA_STONES)
    )
}
