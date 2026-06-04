package com.pokemonza.companion.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Pets
import androidx.compose.ui.graphics.vector.ImageVector

enum class CompanionTab(
    val title: String,
    val icon: ImageVector
) {
    MAP("Map", Icons.Default.Map),
    POKEMON("Pokédex", Icons.Default.Pets),
    FASHION("Fashion", Icons.Default.Checkroom),
    MISSIONS("Missions", Icons.AutoMirrored.Filled.List),
    GUIDES("Guides", Icons.AutoMirrored.Filled.MenuBook)
}
