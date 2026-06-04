package com.pokemonza.companion.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokemonza.companion.BuildConfig
import com.pokemonza.companion.data.model.GuideEntry
import com.pokemonza.companion.data.model.GuideKind
import com.pokemonza.companion.data.model.MegaStoneEntry
import com.pokemonza.companion.data.repository.MegaStoneRepository
import com.pokemonza.companion.ui.components.BackgroundScaffold
import com.pokemonza.companion.ui.components.DetailPopup
import com.pokemonza.companion.ui.components.GlassCard
import com.pokemonza.companion.update.LocalUpdateActions
import com.pokemonza.companion.ui.components.LastUpdatedText
import com.pokemonza.companion.ui.components.glassListContentPadding
import com.pokemonza.companion.ui.theme.ZAAccent
import com.pokemonza.companion.ui.viewmodel.CompanionViewModel
import kotlinx.coroutines.launch

@Composable
fun GuidesScreen(viewModel: CompanionViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.guideState.collectAsState()
    var selectedGuide by remember { mutableStateOf<GuideEntry?>(null) }
    var megaStones by remember { mutableStateOf<List<MegaStoneEntry>?>(null) }
    var megaLoading by remember { mutableStateOf(false) }
    val updateActions = LocalUpdateActions.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val megaRepo = remember { MegaStoneRepository(context) }

    BackgroundScaffold(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { viewModel.loadGuides(forceRefresh = true) }) {
                    Icon(Icons.Default.Refresh, "Refresh", tint = Color.White)
                }
            }

            LastUpdatedText(state.lastUpdated)

            if (state.isLoading && state.data.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = glassListContentPadding()
                ) {
                    item {
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = updateActions != null) {
                                    updateActions?.checkNow()
                                }
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    "App version ${BuildConfig.VERSION_NAME}",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    "Tap here to check for updates from GitHub",
                                    color = ZAAccent,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                    items(state.data, key = { it.title }) { guide ->
                        GuideCard(guide) {
                            when (guide.guideKind) {
                                GuideKind.MEGA_STONES -> {
                                    megaLoading = true
                                    scope.launch {
                                        megaStones = megaRepo.loadStones()
                                        megaLoading = false
                                    }
                                }
                                GuideKind.IN_APP_HINT -> selectedGuide = guide
                            }
                        }
                    }
                }
            }
        }
    }

    selectedGuide?.let { guide ->
        DetailPopup(title = guide.title, onDismiss = { selectedGuide = null }) {
            Text(guide.category, color = ZAAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(guide.summary, color = Color.White.copy(0.9f), fontSize = 13.sp, lineHeight = 18.sp)
        }
    }

    if (megaLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.White)
        }
    }

    megaStones?.let { stones ->
        DetailPopup(title = "Mega Stones", onDismiss = { megaStones = null }) {
            Text(
                "${stones.size} locations from Serebii (offline)",
                color = ZAAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            stones.forEach { stone ->
                Column(Modifier.padding(vertical = 6.dp)) {
                    Text(stone.stone, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    if (stone.effect.isNotBlank()) {
                        Text(stone.effect, color = Color.White.copy(0.65f), fontSize = 11.sp)
                    }
                    Text(stone.location, color = Color.White.copy(0.8f), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun GuideCard(guide: GuideEntry, onClick: () -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.AutoMirrored.Filled.MenuBook, null, tint = ZAAccent, modifier = Modifier.padding(end = 12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(guide.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(guide.category, color = ZAAccent.copy(0.8f), fontSize = 11.sp)
                Spacer(Modifier.height(4.dp))
                Text(guide.summary, color = Color.White.copy(0.65f), fontSize = 12.sp, maxLines = 2)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White.copy(0.5f))
        }
    }
}
