package com.pokemonza.companion.update

import androidx.compose.runtime.compositionLocalOf

data class UpdateActions(
    val checkNow: () -> Unit
)

val LocalUpdateActions = compositionLocalOf<UpdateActions?> { null }
