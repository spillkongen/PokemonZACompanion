package com.pokemonza.companion.ui.components

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.pokemonza.companion.BuildConfig
import com.pokemonza.companion.update.AppUpdateChecker
import com.pokemonza.companion.update.AppUpdateInfo
import com.pokemonza.companion.update.AppUpdateInstaller
import com.pokemonza.companion.update.UpdatePreferences
import kotlinx.coroutines.launch

@Composable
fun AppUpdateHost(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val checker = remember { AppUpdateChecker() }
    val installer = remember { AppUpdateInstaller(context) }
    val prefs = remember { UpdatePreferences(context) }

    var pendingUpdate by remember { mutableStateOf<AppUpdateInfo?>(null) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val installLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { }

    val installPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { }

    fun beginUpdate(update: AppUpdateInfo) {
        scope.launch {
            try {
                isDownloading = true
                downloadProgress = 0f
                errorMessage = null
                val apk = installer.downloadApk(update.apkDownloadUrl) { downloadProgress = it }
                isDownloading = false
                installLauncher.launch(installer.installApk(apk))
            } catch (e: Exception) {
                isDownloading = false
                errorMessage = e.message ?: "Update failed"
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!checker.isConfigured()) return@LaunchedEffect
        val update = checker.checkForUpdate(BuildConfig.VERSION_CODE) ?: return@LaunchedEffect
        if (update.versionCode <= prefs.getDismissedVersionCode()) return@LaunchedEffect
        pendingUpdate = update
    }

    content()

    pendingUpdate?.let { update ->
        UpdateAvailableDialog(
            update = update,
            isDownloading = isDownloading,
            downloadProgress = downloadProgress,
            errorMessage = errorMessage,
            onUpdate = {
                if (!installer.canInstallPackages()) {
                    installPermissionLauncher.launch(installer.openInstallPermissionSettings())
                    Toast.makeText(
                        context,
                        "Allow installs from this app, then tap Update again",
                        Toast.LENGTH_LONG
                    ).show()
                    return@UpdateAvailableDialog
                }
                beginUpdate(update)
            },
            onDismiss = {
                prefs.setDismissedVersionCode(update.versionCode)
                pendingUpdate = null
                errorMessage = null
            }
        )
    }
}
