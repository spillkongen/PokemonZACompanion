package com.pokemonza.companion.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.pokemonza.companion.data.network.AppConstants
import com.pokemonza.companion.ui.components.BackgroundScaffold

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MapScreen(modifier: Modifier = Modifier) {
    var isLoading by remember { mutableStateOf(true) }
    var currentUrl by remember { mutableStateOf(AppConstants.OFFLINE_MAP_URL) }

    BackgroundScaffold(modifier = modifier) {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        settings.builtInZoomControls = true
                        settings.displayZoomControls = false
                        settings.setSupportZoom(true)
                        settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                isLoading = true
                                currentUrl = url ?: currentUrl
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
                                currentUrl = url ?: currentUrl
                            }

                        }
                        webChromeClient = WebChromeClient()
                        loadUrl(AppConstants.OFFLINE_MAP_URL)
                    }
                },
                update = { webView ->
                    if (webView.url == null) {
                        webView.loadUrl(AppConstants.OFFLINE_MAP_URL)
                    }
                }
            )

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            }

            Text(
                text = "Lumiose Map",
                color = Color.White.copy(alpha = 0.4f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp)
            )
        }
    }
}
