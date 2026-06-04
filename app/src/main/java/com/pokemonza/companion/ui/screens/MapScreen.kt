package com.pokemonza.companion.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Message
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
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

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MapScreen(modifier: Modifier = Modifier) {
    var isLoading by remember { mutableStateOf(true) }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.databaseEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    settings.builtInZoomControls = true
                    settings.displayZoomControls = false
                    settings.setSupportZoom(true)
                    settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                    settings.javaScriptCanOpenWindowsAutomatically = false
                    settings.setSupportMultipleWindows(false)
                    settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                    CookieManager.getInstance().setAcceptCookie(true)
                    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            isLoading = true
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            isLoading = false
                        }

                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            val url = request?.url?.toString() ?: return false
                            if (url.contains("mapgenie.io") || url.contains("mapgenie.com")) {
                                return false
                            }
                            return true
                        }
                    }
                    webChromeClient = object : WebChromeClient() {
                        override fun onCreateWindow(
                            view: WebView?,
                            isDialog: Boolean,
                            isUserGesture: Boolean,
                            resultMsg: Message?
                        ): Boolean = false
                    }
                    loadUrl(AppConstants.MAP_GENIE_LUMIOSE)
                }
            },
            update = { webView ->
                if (webView.url == null) {
                    webView.loadUrl(AppConstants.MAP_GENIE_LUMIOSE)
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
            text = "MapGenie",
            color = Color.White.copy(alpha = 0.4f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(8.dp)
        )
    }
}
