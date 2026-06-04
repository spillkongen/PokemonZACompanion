package com.pokemonza.companion.data.model

data class PokemonEntry(
    val nationalDex: String,
    val lumioseDex: String?,
    val hyperspaceDex: String?,
    val name: String,
    val types: List<String>,
    val imageUrl: String? = null,
    val spriteAsset: String? = null,
    val normallyAvailable: Boolean,
    val wikiUrl: String = "",
    val detailSummary: String = "",
    val weaknesses: List<String> = emptyList(),
    val resistances: List<String> = emptyList(),
    val canMegaEvolve: Boolean = false,
    val megaForms: List<String> = emptyList()
)

enum class FashionOutfitFilter(val label: String) {
    ALL("All styles"),
    WOMENS("Women's styles"),
    MENS("Men's styles")
}

enum class FashionPreviewGender(val label: String) {
    AUTO("Auto preview"),
    FEMALE("Female model"),
    MALE("Male model")
}

data class FashionItem(
    val category: String,
    val name: String,
    val style: String,
    val location: String,
    val cost: String,
    val imageUrl: String? = null,
    val femaleImageUrl: String? = null,
    val thumbAsset: String? = null,
    val fullAsset: String? = null,
    val femaleThumbAsset: String? = null,
    val previewKey: String? = null,
    val feminineCut: Boolean = false,
    val masculineCut: Boolean = false
) {
    val categoryLabel: String
        get() = FashionCategory.fromId(category).displayName

    /** Full-size Serebii preview (same outfit; often clearer than the thumbnail). */
    fun largePreviewUrl(): String? = femaleImageUrl ?: imageUrl

    fun matchesOutfitFilter(filter: FashionOutfitFilter): Boolean = when (filter) {
        FashionOutfitFilter.ALL -> true
        FashionOutfitFilter.WOMENS -> feminineCut
        FashionOutfitFilter.MENS -> !feminineCut
    }
}

enum class FashionCategory(val id: String, val displayName: String) {
    ALL_IN_ONE("all-in-one", "All-in-One"),
    TOPS("tops", "Tops"),
    BOTTOMS("bottoms", "Bottoms"),
    HEADWEAR("headwear", "Headwear"),
    EYEWEAR("eyewear", "Eyewear"),
    GLOVES("gloves", "Gloves"),
    LEGWEAR("legwear", "Legwear"),
    FOOTWEAR("footwear", "Footwear"),
    SATCHELS("satchels", "Satchels"),
    EARRINGS("earrings", "Earrings");

    companion object {
        fun fromId(id: String) = entries.find { it.id == id } ?: TOPS
        val userCategories = listOf(TOPS, BOTTOMS, HEADWEAR, EYEWEAR, GLOVES, LEGWEAR, FOOTWEAR, SATCHELS, EARRINGS, ALL_IN_ONE)
    }
}

data class MissionEntry(
    val id: String,
    val number: String,
    val title: String,
    val type: MissionType,
    val description: String,
    val detailUrl: String,
    val guide: String = ""
)

enum class MissionType(val label: String) {
    MAIN("Main Mission"),
    SIDE("Side Mission"),
    HYPERSPACE("Hyperspace"),
    MEGA("Mega Stone"),
    TM("TM Location")
}

data class GuideEntry(
    val title: String,
    val category: String,
    val summary: String,
    val url: String = "",
    val guideKind: GuideKind = GuideKind.IN_APP_HINT
)

enum class GuideKind {
    IN_APP_HINT,
    MEGA_STONES
}

data class MegaStoneEntry(
    val stone: String,
    val effect: String,
    val location: String
)

data class TabLoadState<T>(
    val data: List<T> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastUpdated: Long? = null
)
