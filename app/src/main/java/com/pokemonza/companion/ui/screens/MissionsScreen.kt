package com.pokemonza.companion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokemonza.companion.data.model.MissionEntry
import com.pokemonza.companion.data.model.MissionType
import com.pokemonza.companion.ui.components.BackgroundScaffold
import com.pokemonza.companion.ui.components.DetailPopup
import com.pokemonza.companion.ui.components.GlassCard
import com.pokemonza.companion.ui.components.GlassFilterChip
import com.pokemonza.companion.ui.components.LastUpdatedText
import com.pokemonza.companion.ui.components.glassListContentPadding
import com.pokemonza.companion.ui.theme.ZAAccent
import com.pokemonza.companion.ui.theme.ZAGold
import com.pokemonza.companion.ui.viewmodel.CompanionViewModel

@Composable
fun MissionsScreen(viewModel: CompanionViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.missionState.collectAsState()
    val guideDetail by viewModel.missionDetail.collectAsState()
    var filter by remember { mutableStateOf<MissionType?>(null) }
    var selected by remember { mutableStateOf<MissionEntry?>(null) }

    val filtered = state.data.filter { filter == null || it.type == filter }

    BackgroundScaffold(modifier = modifier) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    GlassFilterChip(selected = filter == null, onClick = { filter = null }, label = { Text("All") })
                    GlassFilterChip(selected = filter == MissionType.MAIN, onClick = { filter = MissionType.MAIN }, label = { Text("Main") })
                    GlassFilterChip(selected = filter == MissionType.SIDE, onClick = { filter = MissionType.SIDE }, label = { Text("Side") })
                    GlassFilterChip(selected = filter == MissionType.HYPERSPACE, onClick = { filter = MissionType.HYPERSPACE }, label = { Text("Hyper") })
                }
                IconButton(onClick = { viewModel.loadMissions(forceRefresh = true) }) {
                    Icon(Icons.Default.Refresh, null, tint = Color.White)
                }
            }
            if (state.data.isNotEmpty()) {
                Text(
                    "${state.data.size} missions (offline)",
                    color = Color.White.copy(0.55f),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                )
            }
            LastUpdatedText(state.lastUpdated)
            when {
                state.isLoading && state.data.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
                else -> LazyColumn(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = glassListContentPadding()
                ) {
                    items(filtered, key = { it.number + it.title }) { mission ->
                        MissionCard(mission) {
                            selected = mission
                            viewModel.loadMissionDetail(mission)
                        }
                    }
                }
            }
        }
    }

    selected?.let { mission ->
        DetailPopup(
            title = mission.title,
            onDismiss = {
                selected = null
                viewModel.clearMissionDetail()
            }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(mission.type.label, color = ZAAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(
                    guideDetail ?: mission.description,
                    color = Color.White.copy(0.9f),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Text(
                    "How to complete: Follow map markers, talk to NPCs in the mission area, and check your quest log in-game (X menu).",
                    color = Color.White.copy(0.55f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun MissionCard(mission: MissionEntry, onClick: () -> Unit) {
    val badgeColor = when (mission.type) {
        MissionType.MAIN -> ZAAccent
        MissionType.SIDE -> ZAGold
        MissionType.HYPERSPACE -> Color(0xFF9C6BFF)
        else -> Color.Gray
    }
    GlassCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row {
            Column(
                Modifier
                    .background(badgeColor.copy(0.35f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("#${mission.number}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(mission.type.label, color = Color.White.copy(0.7f), fontSize = 9.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(mission.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(Modifier.height(4.dp))
                Text(mission.description, color = Color.White.copy(0.65f), fontSize = 12.sp, maxLines = 2)
            }
            Text("›", color = Color.White.copy(0.5f), fontSize = 20.sp)
        }
    }
}
