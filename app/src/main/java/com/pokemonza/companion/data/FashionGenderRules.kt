package com.pokemonza.companion.data

/** Outfit cut rules — keep in sync with scripts/fashion_gender_rules.py */
object FashionGenderRules {
    private val feminineMarkers = listOf(
        "blouse", "skort", "dress", "skirt", "romper", "jumpsuit",
        "off shoulder", "off-shoulder", "crop top", "tube top", "ribbon blouse",
        "halter", "camisole", "bodysuit", "peplum", "wrap top", "corset", "pinafore",
        "tiered skirt", "pleated skirt", "pleated skort", "maxi skirt", "miniskirt",
        "hot pants", "culottes", "overalls set", "fur coat", "suspender pants", "blouson",
        "v-neck set", "tights", "leggings", "knee-high sock", "thigh-high sock",
        "mary jane", "pumps", "pump", "heel", "loafer", "frilly", "flower earring",
        "bow-and-bone", "bow and bone", "lace-up shoe", "lace-up show", "beribboned",
        "bejeweled", "mesh tight", "knit tight", "patterned tight", "floral tight",
        "gradient cropped", "simple cropped", "asymmetrical tight", "polka dot ribbon",
        "lacy ribbon", "crisscross ribbon", "flower-stitch", "satchel", "clutch", "tote",
        "ribbon sock", "platform", "wedge", "ballet", "ankle strap", "jacinthe",
        "liepard pump", "elbow-patch sweater", "cardigan & blouse", "sweater vest & blouse",
        "vest & ribbon", "belted romper", "big-logo overalls", "wrap skort",
        "plaid pleated", "holo-x blouson", "holo-y blouson", "chef top"
    )

    private val masculineMarkers = listOf(
        "biker jacket", "cargo pants", "blazer & shirt", "cardigan & shirt",
        "shacket", "hoodie set", "track jacket", "polo", "suit pants", "dress shirt",
        "denim jacket set", "graphic t-shirt and shacket", "two-tone turtleneck",
        "frog-button jacket", "leather mix pullover", "cinematic pullover", "logo pullover",
        "simple pullover", "patterned pullover", "puffer vest and hoodie"
    )

    fun isFeminineCut(name: String): Boolean {
        val n = name.lowercase()
        return feminineMarkers.any { n.contains(it) }
    }

    fun isMasculineCut(name: String): Boolean {
        if (isFeminineCut(name)) return false
        val n = name.lowercase()
        return masculineMarkers.any { n.contains(it) }
    }

    fun isFemaleWardrobe(name: String): Boolean = !isMasculineCut(name)
}
