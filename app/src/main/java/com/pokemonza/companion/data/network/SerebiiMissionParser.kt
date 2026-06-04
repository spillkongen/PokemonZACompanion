package com.pokemonza.companion.data.network

import android.content.Context
import com.pokemonza.companion.data.model.MissionEntry
import com.pokemonza.companion.data.model.MissionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MissionRepository(private val context: Context) {
    private var cached: List<MissionEntry>? = null

    suspend fun fetchMissions(): List<MissionEntry> {
        cached?.let { return it }
        return loadBundled().also { cached = it }
    }

    fun clearCache() {
        cached = null
    }

    fun fetchMissionGuide(mission: MissionEntry): String {
        if (mission.guide.length > 80) return mission.guide
        return mission.description.ifBlank {
            "Follow map markers and talk to NPCs in the mission area."
        }
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
                        detailUrl = o.optString("detailUrl", ""),
                        guide = o.optString("guide", "").ifBlank { o.getString("description") }
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
