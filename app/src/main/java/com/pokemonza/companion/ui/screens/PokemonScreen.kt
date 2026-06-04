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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pokemonza.companion.data.model.PokemonEntry
import com.pokemonza.companion.ui.components.BackgroundScaffold
import com.pokemonza.companion.ui.components.DetailPopup
import com.pokemonza.companion.ui.components.GlassCard
import com.pokemonza.companion.ui.components.GlassSearchField
import com.pokemonza.companion.ui.components.LastUpdatedText
import com.pokemonza.companion.ui.components.TypeBadge
import com.pokemonza.companion.ui.viewmodel.CompanionViewModel

@Composable
fun PokemonScreen(viewModel: CompanionViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.pokemonState.collectAsState()
    val detail by viewModel.pokemonDetail.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<PokemonEntry?>(null) }

    val filtered = state.data.filter {
        searchQuery.isBlank() ||
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.types.any { t -> t.contains(searchQuery, ignoreCase = true) } ||
            it.nationalDex.contains(searchQuery)
    }

    BackgroundScaffold(modifier = modifier) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassSearchField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "Search Pokémon...",
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.White.copy(0.8f)) },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { viewModel.loadPokemon(forceRefresh = true) }) {
                    Icon(Icons.Default.Refresh, null, tint = Color.White)
                }
            }
            LastUpdatedText(state.lastUpdated)
            when {
                state.isLoading && state.data.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
                else -> LazyColumn(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
                ) {
                    items(filtered, key = { it.nationalDex + it.name }) { pokemon ->
                        PokemonCard(pokemon) {
                            selected = pokemon
                            viewModel.loadPokemonDetail(pokemon.name)
                        }
                    }
                }
            }
        }
    }

    selected?.let { pokemon ->
        DetailPopup(
            title = "#${pokemon.nationalDex} ${pokemon.name}",
            onDismiss = {
                selected = null
                viewModel.clearPokemonDetail()
            },
            imageUrl = pokemon.imageUrl
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    pokemon.types.forEach { TypeBadge(it) }
                }
                if (pokemon.lumioseDex != null) {
                    Text("Lumiose Dex #${pokemon.lumioseDex}", color = Color.White.copy(0.7f), fontSize = 12.sp)
                }
                Text(
                    detail ?: "Loading...",
                    color = Color.White.copy(0.85f),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Text(
                    "Tap for full Bulbapedia article in browser from Guides tab.",
                    color = Color.White.copy(0.5f),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun PokemonCard(pokemon: PokemonEntry, onClick: () -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = pokemon.imageUrl,
                contentDescription = pokemon.name,
                modifier = Modifier.size(56.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("#${pokemon.nationalDex} ${pokemon.name}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                if (pokemon.lumioseDex != null) {
                    Text("Lumiose #${pokemon.lumioseDex}", color = Color.White.copy(0.6f), fontSize = 11.sp)
                }
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    pokemon.types.forEach { TypeBadge(it) }
                }
            }
            Text("›", color = Color.White.copy(0.5f), fontSize = 22.sp)
        }
    }
}
