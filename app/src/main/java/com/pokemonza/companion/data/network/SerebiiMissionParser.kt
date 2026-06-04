package com.pokemonza.companion.data.network

import android.content.Context
import com.pokemonza.companion.data.model.MissionEntry
import com.pokemonza.companion.data.model.MissionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.concurrent.TimeUnit

class SerebiiMissionParser {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .build()

    private val listPages = listOf(
        MissionType.MAIN to AppConstants.SEREBII_MAIN_MISSIONS_URL,
        MissionType.SIDE to AppConstants.SEREBII_SIDE_MISSIONS_URL,
        MissionType.HYPERSPACE to AppConstants.SEREBII_HYPERSPACE_MISSIONS_URL
    )

    suspend fun fetchAllLive(): List<MissionEntry> = coroutineScope {
        listPages.map { (type, url) ->
            async {
                runCatching { parseListPage(fetchHtml(url), type) }.getOrDefault(emptyList())
            }
        }.flatMap { it.await() }
    }

    suspend fun fetchMissionGuide(detailUrl: String, fallback: String): String = withContext(Dispatchers.IO) {
        if (detailUrl.isBlank()) return@withContext fallback
        runCatching {
            val guide = parseDetailPage(fetchHtml(detailUrl))
            guide.ifBlank { fallback }
        }.getOrDefault(fallback)
    }

    fun parseListPage(doc: Document, type: MissionType): List<MissionEntry> {
        val results = mutableListOf<MissionEntry>()
        for (table in doc.select("table")) {
            val rows = table.select("tr")
            if (rows.size < 2) continue
            val header = rows.first()?.text()?.lowercase().orEmpty()
            if (!header.contains("name") || !header.contains("description")) continue

            for (row in rows.drop(1)) {
                val cells = row.select("td")
                if (cells.size < 3) continue
                val number = cells[0].text().trim()
                val link = row.select("a[href]").firstOrNull()
                val title = link?.text()?.trim().orEmpty().ifBlank { cells[1].text().trim() }
                if (title.length < 2) continue
                val description = cells[2].text().trim()
                val detailUrl = link?.attr("href")?.let { resolveUrl(it, type) }.orEmpty()
                results.add(
                    MissionEntry(
                        number = number,
                        title = title,
                        type = type,
                        description = description,
                        detailUrl = detailUrl
                    )
                )
            }
            if (results.isNotEmpty()) break
        }
        return results.distinctBy { "${it.type}:${it.number}:${it.title}" }
    }

    fun parseDetailPage(doc: Document): String {
        val paragraphs = mutableListOf<String>()
        val seen = mutableSetOf<String>()

        for (p in doc.select("p")) {
            val text = p.text().trim()
            if (text.length < 35) continue
            if (text.startsWith("Description :", ignoreCase = true)) continue
            if (text.startsWith("Unlock Criteria", ignoreCase = true)) continue
            val normalized = text.take(120)
            if (seen.add(normalized)) paragraphs.add(text)
        }

        if (paragraphs.isEmpty()) {
            doc.select("td.fooinfo").forEach { cell ->
                val text = cell.text().trim()
                if (text.length > 40 && seen.add(text.take(120))) paragraphs.add(text)
            }
        }

        return paragraphs.take(8).joinToString("\n\n")
    }

    fun parseHtmlList(html: String, type: MissionType): List<MissionEntry> =
        parseListPage(Jsoup.parse(html), type)

    fun parseHtmlDetail(html: String): String = parseDetailPage(Jsoup.parse(html))

    private fun fetchHtml(url: String): Document {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "PokemonZACompanion/1.3")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Serebii returned ${response.code}")
            val body = response.body?.string() ?: throw Exception("Empty Serebii response")
            return Jsoup.parse(body, url)
        }
    }

    private fun resolveUrl(href: String, type: MissionType): String {
        val folder = when (type) {
            MissionType.MAIN -> "mainmissions"
            MissionType.SIDE -> "sidemissions"
            MissionType.HYPERSPACE -> "hyperspacemissions"
            else -> "mainmissions"
        }
        return when {
            href.startsWith("http") -> href
            href.startsWith("/") -> "https://www.serebii.net$href"
            href.contains("/") -> "${AppConstants.SEREBII_ZA_BASE}$href"
            else -> "${AppConstants.SEREBII_ZA_BASE}$folder/$href"
        }
    }
}

class MissionRepository(
    private val context: Context,
    private val parser: SerebiiMissionParser = SerebiiMissionParser()
) {
    suspend fun fetchMissions(forceLive: Boolean = false): List<MissionEntry> {
        if (forceLive) {
            return try {
                val live = parser.fetchAllLive()
                if (live.size >= 50) live else loadBundled()
            } catch (_: Exception) {
                loadBundled()
            }
        }
        return try {
            val live = parser.fetchAllLive()
            if (live.size >= 200) live else loadBundled()
        } catch (_: Exception) {
            loadBundled()
        }
    }

    suspend fun fetchMissionGuide(mission: MissionEntry): String {
        val fallback = mission.description.ifBlank {
            "Mission guide from Serebii. Follow map markers and talk to NPCs in the mission area."
        }
        if (mission.guide.length > 80) return mission.guide
        return parser.fetchMissionGuide(mission.detailUrl, fallback)
    }

    private suspend fun loadBundled(): List<MissionEntry> = withContext(Dispatchers.IO) {
        val json = context.assets.open("missions_serebii.json").bufferedReader().readText()
        val root = org.json.JSONObject(json)
        val arr = root.getJSONArray("missions")
        buildList {
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                add(
                    MissionEntry(
                        number = o.getString("number"),
                        title = o.getString("title"),
                        type = missionTypeFromString(o.getString("type")),
                        description = o.getString("description"),
                        detailUrl = o.getString("detailUrl")
                    )
                )
            }
        }
    }

    private fun missionTypeFromString(raw: String): MissionType = when (raw.uppercase()) {
        "MAIN" -> MissionType.MAIN
        "SIDE" -> MissionType.SIDE
        "HYPERSPACE" -> MissionType.HYPERSPACE
        "MEGA" -> MissionType.MEGA
        "TM" -> MissionType.TM
        else -> MissionType.SIDE
    }
}
