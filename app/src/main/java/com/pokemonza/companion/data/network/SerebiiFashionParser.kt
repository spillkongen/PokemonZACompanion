package com.pokemonza.companion.data.network

import android.content.Context
import com.pokemonza.companion.data.model.FashionItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.util.concurrent.TimeUnit

class SerebiiFashionParser {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val categoryHeaders = mapOf(
        "all-in-one" to "All-in-One",
        "tops" to "Tops",
        "bottoms" to "Bottoms",
        "headwear" to "Headwear",
        "eyewear" to "Eyewear",
        "gloves" to "Gloves",
        "legwear" to "Legwear",
        "footwear" to "Footwear",
        "satchels" to "Satchels",
        "earrings" to "Earrings"
    )

    suspend fun fetchLive(): List<FashionItem> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(AppConstants.SEREBII_FASHION_URL)
            .header("User-Agent", "PokemonZACompanion/1.2")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Serebii returned ${response.code}")
            val html = response.body?.string() ?: throw Exception("Empty Serebii response")
            parseHtml(html)
        }
    }

    fun parseHtml(html: String): List<FashionItem> {
        val doc = Jsoup.parse(html)
        val items = mutableListOf<FashionItem>()
        val seen = mutableSetOf<String>()

        for ((catId, header) in categoryHeaders) {
            val table = findCategoryTable(doc, catId, header) ?: continue
            parseTable(table, catId, items, seen)
        }

        if (items.isEmpty()) {
            doc.select("h3").forEach { heading ->
                val headerText = heading.text().trim()
                val catId = categoryHeaders.entries.find { it.value.equals(headerText, true) }?.key ?: return@forEach
                heading.nextElementSibling()?.let { next ->
                    if (next.tagName() == "table" && next.hasClass("dextable")) {
                        parseTable(next, catId, items, seen)
                    }
                }
            }
        }
        return items
    }

    private fun findCategoryTable(doc: Element, catId: String, header: String): Element? {
        for (anchor in doc.select("a[name=$catId]")) {
            var el: Element? = anchor
            repeat(80) {
                el = el?.nextElementSibling() ?: return@repeat
                if (el.tagName() == "h3" && el.text().contains(header, ignoreCase = true)) {
                    var scan: Element? = el.nextElementSibling()
                    repeat(15) {
                        if (scan != null && scan.tagName() == "table" && scan.hasClass("dextable")) {
                            val hdr = scan.select("tr").firstOrNull()?.text().orEmpty()
                            if (hdr.contains("Picture", ignoreCase = true) && hdr.contains("Name", ignoreCase = true)) {
                                return scan
                            }
                        }
                        scan = scan?.nextElementSibling()
                    }
                }
            }
        }
        val heading = doc.select("h3").firstOrNull { it.text().trim().equals(header, ignoreCase = true) } ?: return null
        val next = heading.nextElementSibling()
        return if (next != null && next.tagName() == "table" && next.hasClass("dextable")) next else null
    }

    private fun parseTable(table: Element, category: String, out: MutableList<FashionItem>, seen: MutableSet<String>) {
        val rows = table.select("tr")
        if (rows.size < 2) return

        for (row in rows.drop(1)) {
            val cells = row.select("td")
            if (cells.size < 4) continue

            val img = row.select("img").firstOrNull()?.attr("src")?.let { resolveUrl(it) }
            val previewKey = row.select("a[data-key]").firstOrNull()?.attr("data-key")
            val texts = cells.map { it.text().trim() }.filter { it.isNotBlank() }

            val (name, style, location, cost) = when {
                texts.size >= 5 -> Quad(texts[1], texts[2], texts[3], texts[4])
                texts.size >= 4 -> Quad(texts[0], texts[1], texts[2], texts[3])
                else -> continue
            }

            if (name.equals("Name", ignoreCase = true) || name.equals("Picture", ignoreCase = true)) continue

            val key = "$category|$name|$style"
            if (!seen.add(key)) continue

            out.add(
                FashionItem(
                    category = category,
                    name = name,
                    style = style,
                    location = formatLocation(location),
                    cost = formatCost(cost),
                    imageUrl = img,
                    femaleImageUrl = femalePreviewUrl(img),
                    previewKey = previewKey,
                    feminineCut = isFeminineCut(name),
                    masculineCut = isMasculineCut(name)
                )
            )
        }
    }

    private fun isFeminineCut(name: String): Boolean {
        val n = name.lowercase()
        return n.contains("blouse") || n.contains("skort") || n.contains("dress") || n.contains("skirt") ||
            n.contains("romper") || n.contains("ribbon blouse") || n.contains("off shoulder")
    }

    private fun isMasculineCut(name: String): Boolean {
        val n = name.lowercase()
        if (isFeminineCut(name)) return false
        return n.contains("shirt") || n.contains("cargo pants") || n.contains("biker jacket")
    }

    private fun femalePreviewUrl(maleUrl: String?): String? {
        if (maleUrl == null) return null
        val match = Regex("/custom/th/(\\d+)\\.jpg").find(maleUrl) ?: return maleUrl
        val id = match.groupValues[1]
        return "https://www.serebii.net/legendsz-a/custom/$id.jpg"
    }

    private fun formatLocation(raw: String): String {
        return raw
            .replace(Regex("([a-z])([A-Z])"), "$1 $2")
            .replace(Regex("(Passage|Galerie|Hotel|Vernal|During|Complete|Fresh|Boutique|Kickspin|SUBATOMIC|Mode |Glammor|NIGHTSIDE|Wisp|Masterpiece|Porte|Midnight|DENSOKU|Les |Bundle|FILMFAN|Kikonashi|South)"), " · $1")
            .trim()
    }

    private fun formatCost(raw: String): String {
        val trimmed = raw.trim()
        return when {
            trimmed.isBlank() -> "Free / Mission reward"
            trimmed.all { it.isDigit() } -> "₽$trimmed"
            else -> trimmed
        }
    }

    private fun resolveUrl(src: String): String = when {
        src.startsWith("//") -> "https:$src"
        src.startsWith("/") -> "https://www.serebii.net$src"
        src.startsWith("http") -> src
        else -> "https://www.serebii.net/$src"
    }

    private data class Quad(val a: String, val b: String, val c: String, val d: String)
}

class FashionRepository(private val context: Context) {
    private var cached: List<FashionItem>? = null

    suspend fun fetchFashion(): List<FashionItem> {
        cached?.let { return it }
        return loadBundled().also { cached = it }
    }

    fun clearCache() {
        cached = null
    }

    private suspend fun loadBundled(): List<FashionItem> = withContext(Dispatchers.IO) {
        val json = context.assets.open("fashion_serebii.json").bufferedReader().readText()
        val itemsArray = org.json.JSONObject(json).getJSONArray("items")
        buildList {
            for (i in 0 until itemsArray.length()) {
                val o = itemsArray.getJSONObject(i)
                add(
                    FashionItem(
                        category = o.getString("category"),
                        name = o.getString("name"),
                        style = o.getString("style"),
                        location = o.getString("location"),
                        cost = o.getString("cost"),
                        imageUrl = o.optString("imageUrl").takeIf { it.isNotBlank() && it != "null" },
                        femaleImageUrl = o.optString("femaleImageUrl").takeIf { it.isNotBlank() && it != "null" },
                        previewKey = o.optString("previewKey").takeIf { it.isNotBlank() && it != "null" },
                        feminineCut = o.optBoolean("feminineCut", false),
                        masculineCut = o.optBoolean("masculineCut", false)
                    )
                )
            }
        }
    }
}
