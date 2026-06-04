package com.pokemonza.companion

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.window.layout.WindowInfoTracker
import com.pokemonza.companion.navigation.CompanionTab
import com.pokemonza.companion.ui.components.AppUpdateHost
import com.pokemonza.companion.ui.components.GlassBottomNavigation
import com.pokemonza.companion.ui.screens.FashionScreen
import com.pokemonza.companion.ui.screens.GuidesScreen
import com.pokemonza.companion.ui.screens.MapScreen
import com.pokemonza.companion.ui.screens.MissionsScreen
import com.pokemonza.companion.ui.screens.PokemonScreen
import com.pokemonza.companion.ui.system.ImmersiveSystemUi
import com.pokemonza.companion.ui.viewmodel.CompanionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ImmersiveSystemUi.apply(this)
        setContent {
            ImmersiveLifecycleEffect()
            FoldAwareEffect()
            AppUpdateHost {
                PokemonZACompanionApp()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        ImmersiveSystemUi.apply(this)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) ImmersiveSystemUi.apply(this)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        ImmersiveSystemUi.apply(this)
    }
}

@Composable
private fun ImmersiveLifecycleEffect() {
    val lifecycleOwner = LocalLifecycleOwner.current
    val activity = lifecycleOwner as? ComponentActivity ?: return
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) ImmersiveSystemUi.apply(activity)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}

@Composable
private fun FoldAwareEffect() {
    val lifecycleOwner = LocalLifecycleOwner.current
    val activity = lifecycleOwner as? ComponentActivity ?: return
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    LaunchedEffect(activity, configuration.screenWidthDp) {
        ImmersiveSystemUi.apply(activity)
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
        withContext(Dispatchers.Main) {
            WindowInfoTracker.getOrCreate(activity)
                .windowLayoutInfo(activity)
                .distinctUntilChanged()
                .collect { ImmersiveSystemUi.apply(activity) }
        }
    }
}

@Composable
fun PokemonZACompanionApp(viewModel: CompanionViewModel = viewModel()) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = CompanionTab.entries

    LaunchedEffect(selectedTab) {
        when (tabs[selectedTab]) {
            CompanionTab.MAP -> Unit
            CompanionTab.POKEMON -> viewModel.loadPokemon()
            CompanionTab.FASHION -> viewModel.loadFashion()
            CompanionTab.MISSIONS -> viewModel.loadMissions()
            CompanionTab.GUIDES -> viewModel.loadGuides()
        }
    }

    Box(Modifier.fillMaxSize()) {
        val contentModifier = Modifier.fillMaxSize()
        when (tabs[selectedTab]) {
            CompanionTab.MAP -> MapScreen(contentModifier)
            CompanionTab.POKEMON -> PokemonScreen(viewModel, contentModifier)
            CompanionTab.FASHION -> FashionScreen(viewModel, contentModifier)
            CompanionTab.MISSIONS -> MissionsScreen(viewModel, contentModifier)
            CompanionTab.GUIDES -> GuidesScreen(viewModel, contentModifier)
        }

        GlassBottomNavigation(
            tabs = tabs,
            selectedIndex = selectedTab,
            onTabSelected = { selectedTab = it },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
