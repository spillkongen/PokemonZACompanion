package com.pokemonza.companion.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.concurrent.TimeUnit

class BulbapediaClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    suspend fun fetchPageHtml(pageTitle: String): Document = withContext(Dispatchers.IO) {
        val url = "${AppConstants.BULBAPEDIA_API}?action=parse&format=json&prop=text&page=${encodePageTitle(pageTitle)}"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "PokemonZACompanion/1.0 (Unofficial Fan App)")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Bulbapedia request failed: ${response.code}")
            }
            val body = response.body?.string() ?: throw Exception("Empty response from Bulbapedia")
            val json = JSONObject(body)
            if (json.has("error")) {
                throw Exception(json.getJSONObject("error").optString("info", "Unknown API error"))
            }
            val html = json.getJSONObject("parse").getJSONObject("text").getString("*")
            Jsoup.parse(html)
        }
    }

    suspend fun fetchDirectPage(path: String): Document = withContext(Dispatchers.IO) {
        val url = "${AppConstants.BULBAPEDIA_BASE}$path"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "PokemonZACompanion/1.0 (Unofficial Fan App)")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Page request failed: ${response.code}")
            }
            val html = response.body?.string() ?: throw Exception("Empty page response")
            Jsoup.parse(html)
        }
    }

    private fun encodePageTitle(title: String): String {
        return title.replace(" ", "_")
    }
}

object WikiParsers {
    fun parsePokemonList(doc: Document): List<com.pokemonza.companion.data.model.PokemonEntry> {
        val results = mutableListOf<com.pokemonza.companion.data.model.PokemonEntry>()
        val tables = doc.select("table.roundy, table.sortable")

        for (table in tables) {
            val headers = table.select("tr").firstOrNull()?.select("th")?.map { it.text().trim().lowercase() }
                ?: continue
            if (!headers.any { it.contains("pokémon") || it.contains("pokemon") }) continue

            for (row in table.select("tr").drop(1)) {
                val cells = row.select("td, th")
                if (cells.size < 4) continue

                val pokemonCell = row.select("td").find { it.select("a[href*=/wiki/]").isNotEmpty() && it.text().isNotBlank() }
                    ?: continue
                val link = pokemonCell.select("a[href*=/wiki/]").firstOrNull() ?: continue
                val name = link.text().trim()
                if (name.isBlank() || name == "Pokémon") continue

                val nationalDex = row.select("td").getOrNull(0)?.text()?.trim()?.replace("#", "") ?: continue
                if (!nationalDex.matches(Regex("\\d+"))) continue

                val img = row.select("img").firstOrNull()?.attr("src")?.let { resolveImageUrl(it) }

                val typeCells = row.select("td").filter { cell ->
                    val text = cell.text().trim()
                    text.matches(Regex("[A-Za-z]+")) && text.length in 3..12 &&
                        listOf("Normal", "Fire", "Water", "Electric", "Grass", "Ice", "Fighting", "Poison",
                            "Ground", "Flying", "Psychic", "Bug", "Rock", "Ghost", "Dragon", "Dark", "Steel", "Fairy")
                            .any { it.equals(text, ignoreCase = true) }
                }
                val types = typeCells.map { it.text().trim() }.distinct().take(2)

                val lumioseDex = extractDexNumber(row, "lumiose")
                val hyperspaceDex = extractDexNumber(row, "hyperspace")
                val available = !row.text().contains("No", ignoreCase = true) ||
                    row.text().contains("Yes", ignoreCase = true)

                results.add(
                    com.pokemonza.companion.data.model.PokemonEntry(
                        nationalDex = nationalDex.padStart(4, '0'),
                        lumioseDex = lumioseDex,
                        hyperspaceDex = hyperspaceDex,
                        name = name,
                        types = types.ifEmpty { listOf("Unknown") },
                        imageUrl = img,
                        normallyAvailable = available,
                        wikiUrl = "${AppConstants.BULBAPEDIA_BASE}${link.attr("href")}"
                    )
                )
            }
            if (results.isNotEmpty()) break
        }

        return results.distinctBy { it.nationalDex + it.name }
    }

    fun parseMissionTables(doc: Document, type: com.pokemonza.companion.data.model.MissionType): List<com.pokemonza.companion.data.model.MissionEntry> {
        val results = mutableListOf<com.pokemonza.companion.data.model.MissionEntry>()

        for (table in doc.select("table.roundy, table.sortable, table.wikitable")) {
            for (row in table.select("tr").drop(1)) {
                val cells = row.select("td")
                if (cells.size < 2) continue

                val titleLink = row.select("a[href*=/wiki/]").firstOrNull()
                val title = titleLink?.text()?.trim() ?: cells.getOrNull(1)?.text()?.trim() ?: continue
                if (title.length < 3) continue

                val number = cells.first()?.text()?.trim()?.replace("#", "") ?: results.size.plus(1).toString()
                val description = cells.drop(1).joinToString(" · ") { it.text().trim() }.take(200)

                results.add(
                    com.pokemonza.companion.data.model.MissionEntry(
                        number = number,
                        title = title,
                        type = type,
                        description = description.ifBlank { "See Bulbapedia for full details." },
                        wikiUrl = titleLink?.let { "${AppConstants.BULBAPEDIA_BASE}${it.attr("href")}" }
                            ?: AppConstants.BULBAPEDIA_BASE
                    )
                )
            }
            if (results.size >= 5) break
        }

        return results.distinctBy { it.title }.take(100)
    }

    private fun extractDexNumber(row: Element, kind: String): String? {
        val text = row.text()
        return null
    }

    private fun resolveImageUrl(src: String): String {
        return when {
            src.startsWith("//") -> "https:$src"
            src.startsWith("http") -> src
            src.startsWith("/") -> "https://archives.bulbagarden.net$src"
            else -> src
        }
    }
}
