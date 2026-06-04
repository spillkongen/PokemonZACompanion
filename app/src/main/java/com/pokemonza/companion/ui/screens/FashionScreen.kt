package com.pokemonza.companion.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.pokemonza.companion.data.model.FashionCategory
import com.pokemonza.companion.data.model.FashionItem
import com.pokemonza.companion.data.model.FashionOutfitFilter
import com.pokemonza.companion.ui.components.BackgroundScaffold
import com.pokemonza.companion.ui.components.DetailPopup
import com.pokemonza.companion.ui.components.GlassCard
import com.pokemonza.companion.ui.components.GlassFilterChip
import com.pokemonza.companion.ui.components.GlassSearchField
import com.pokemonza.companion.ui.components.glassListContentPadding
import com.pokemonza.companion.ui.components.LastUpdatedText
import com.pokemonza.companion.ui.theme.ZAAccent
import com.pokemonza.companion.ui.theme.ZAGold
import com.pokemonza.companion.ui.viewmodel.CompanionViewModel

@Composable
fun FashionScreen(viewModel: CompanionViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.fashionState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(FashionCategory.TOPS) }
    var outfitFilter by remember { mutableStateOf(FashionOutfitFilter.ALL) }
    var useLargePreview by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<FashionItem?>(null) }

    val filtered = state.data.filter { item ->
        item.category == selectedCategory.id &&
            item.matchesOutfitFilter(outfitFilter) &&
            (searchQuery.isBlank() ||
                item.name.contains(searchQuery, ignoreCase = true) ||
                item.style.contains(searchQuery, ignoreCase = true) ||
                item.location.contains(searchQuery, ignoreCase = true))
    }

    BackgroundScaffold(modifier = modifier) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassSearchField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "Search outfit...",
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.White.copy(0.8f)) },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { viewModel.loadFashion(forceRefresh = true) }) {
                    Icon(Icons.Default.Refresh, "Refresh from Serebii", tint = Color.White)
                }
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(FashionCategory.userCategories) { cat ->
                    GlassFilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat.displayName, fontSize = 11.sp) }
                    )
                }
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(FashionOutfitFilter.entries.toList()) { filter ->
                    GlassFilterChip(
                        selected = outfitFilter == filter,
                        onClick = { outfitFilter = filter },
                        label = { Text(filter.label, fontSize = 11.sp) }
                    )
                }
                item {
                    GlassFilterChip(
                        selected = useLargePreview,
                        onClick = { useLargePreview = !useLargePreview },
                        label = { Text("Large preview", fontSize = 11.sp) }
                    )
                }
            }

            Text(
                "Every outfit works on any character. Women's = blouse/skort/dress sets; Men's = other cuts. Serebii shows one preview model.",
                color = Color.White.copy(0.55f),
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
            )

            LastUpdatedText(state.lastUpdated)

            when {
                state.isLoading && state.data.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = glassListContentPadding()
                ) {
                    item {
                        Text(
                            "${filtered.size} ${selectedCategory.displayName}",
                            color = Color.White.copy(0.7f),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    items(filtered, key = { "${it.category}-${it.name}-${it.style}" }) { item ->
                        FashionItemCard(item, useLargePreview) { selectedItem = item }
                    }
                }
            }
        }
    }

    selectedItem?.let { item ->
        DetailPopup(
            title = item.name,
            onDismiss = { selectedItem = null },
            imageUrl = item.largePreviewUrl() ?: item.imageUrl,
            imageMaxHeight = 300.dp
        ) {
            FashionDetailBody(item)
        }
    }
}

@Composable
private fun FashionItemCard(item: FashionItem, useLargePreview: Boolean, onClick: () -> Unit) {
    val previewUrl = if (useLargePreview) item.largePreviewUrl() else item.imageUrl
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (previewUrl != null) {
                AsyncImage(
                    model = previewUrl,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(Modifier.width(10.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(item.name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(item.style, color = ZAAccent.copy(0.9f), fontSize = 12.sp)
                Text(item.location, color = Color.White.copy(0.6f), fontSize = 11.sp, maxLines = 2)
            }
            Text(item.cost, color = ZAGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FashionDetailBody(item: FashionItem) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DetailRow("Category", item.categoryLabel)
        DetailRow("Style / Color", item.style)
        DetailRow("Shop & Location", item.location)
        DetailRow("Cost", item.cost)
        Text(
            "All outfits are gender-free in Z-A — any character can wear this. Press (−) in-game for Outfits & Looks.",
            color = Color.White.copy(0.65f),
            fontSize = 11.sp
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(label, color = Color.White.copy(0.5f), fontSize = 10.sp)
        Text(value, color = Color.White, fontSize = 13.sp)
    }
}
