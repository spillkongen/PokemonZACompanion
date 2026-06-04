package com.pokemonza.companion.ui.screens

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.MenuBook
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pokemonza.companion.BuildConfig
import com.pokemonza.companion.data.model.GuideEntry
import com.pokemonza.companion.ui.components.BackgroundScaffold
import com.pokemonza.companion.ui.components.DetailPopup
import com.pokemonza.companion.ui.components.GlassCard
import com.pokemonza.companion.update.LocalUpdateActions
import com.pokemonza.companion.ui.components.LastUpdatedText
import com.pokemonza.companion.ui.components.glassListContentPadding
import com.pokemonza.companion.ui.theme.ZAAccent
import com.pokemonza.companion.ui.viewmodel.CompanionViewModel

@Composable
fun GuidesScreen(viewModel: CompanionViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.guideState.collectAsState()
    var selectedGuide by remember { mutableStateOf<GuideEntry?>(null) }
    var inAppGuide by remember { mutableStateOf<GuideEntry?>(null) }
    val updateActions = LocalUpdateActions.current

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
                            if (guide.url.isBlank()) inAppGuide = guide else selectedGuide = guide
                        }
                    }
                }
            }
        }
    }

    selectedGuide?.let { guide ->
        GuideWebDialog(url = guide.url, title = guide.title) {
            selectedGuide = null
        }
    }

    inAppGuide?.let { guide ->
        DetailPopup(title = guide.title, onDismiss = { inAppGuide = null }) {
            Text(guide.category, color = ZAAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(guide.summary, color = Color.White.copy(0.9f), fontSize = 13.sp, lineHeight = 18.sp)
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
        Icon(Icons.Default.MenuBook, null, tint = ZAAccent, modifier = Modifier.padding(end = 12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(guide.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(guide.category, color = ZAAccent.copy(0.8f), fontSize = 11.sp)
            Spacer(Modifier.height(4.dp))
            Text(guide.summary, color = Color.White.copy(0.65f), fontSize = 12.sp, maxLines = 2)
        }
        Icon(Icons.Default.ArrowForward, null, tint = Color.White.copy(0.5f))
    }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun GuideWebDialog(url: String, title: String, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(Modifier.fillMaxSize()) {
            com.pokemonza.companion.ui.components.AppBackground()
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(com.pokemonza.companion.ui.components.GlassPanelLight)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("✕ Close", color = ZAAccent, modifier = Modifier.clickable(onClick = onDismiss))
            }
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp)),
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        webViewClient = WebViewClient()
                        loadUrl(url)
                    }
                }
            )
        }
        }
    }
}
