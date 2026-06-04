package com.pokemonza.companion.ui.components



import android.widget.Toast

import androidx.activity.compose.rememberLauncherForActivityResult

import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.runtime.Composable

import androidx.compose.runtime.CompositionLocalProvider

import androidx.compose.runtime.DisposableEffect

import androidx.compose.runtime.LaunchedEffect

import androidx.compose.runtime.getValue

import androidx.compose.runtime.mutableFloatStateOf

import androidx.compose.runtime.mutableIntStateOf

import androidx.compose.runtime.mutableStateOf

import androidx.compose.runtime.remember

import androidx.compose.runtime.rememberCoroutineScope

import androidx.compose.runtime.setValue

import androidx.compose.ui.platform.LocalContext

import androidx.lifecycle.Lifecycle

import androidx.lifecycle.LifecycleEventObserver

import androidx.lifecycle.compose.LocalLifecycleOwner

import com.pokemonza.companion.BuildConfig

import com.pokemonza.companion.update.AppUpdateChecker

import com.pokemonza.companion.update.AppUpdateInfo

import com.pokemonza.companion.update.AppUpdateInstaller

import com.pokemonza.companion.update.InstallEvent

import com.pokemonza.companion.update.LocalUpdateActions

import com.pokemonza.companion.update.UpdateActions

import com.pokemonza.companion.update.UpdateCheckResult

import com.pokemonza.companion.update.UpdateInstallNotifier

import kotlinx.coroutines.delay

import kotlinx.coroutines.launch



@Composable

fun AppUpdateHost(content: @Composable () -> Unit) {

    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    val checker = remember { AppUpdateChecker() }

    val installer = remember { AppUpdateInstaller(context) }



    var pendingUpdate by remember { mutableStateOf<AppUpdateInfo?>(null) }

    var isDownloading by remember { mutableStateOf(false) }

    var isInstalling by remember { mutableStateOf(false) }

    var downloadProgress by remember { mutableFloatStateOf(0f) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    var dismissedSessionVersion by remember { mutableIntStateOf(0) }

    val installPermissionLauncher = rememberLauncherForActivityResult(

        ActivityResultContracts.StartActivityForResult()

    ) { }



    LaunchedEffect(Unit) {

        UpdateInstallNotifier.events.collect { event ->

            when (event) {

                InstallEvent.Success -> {
                    isInstalling = false
                    isDownloading = false
                    pendingUpdate = null
                    errorMessage = null
                }

                is InstallEvent.Failed -> {

                    isInstalling = false

                    isDownloading = false

                    errorMessage = event.message

                }

            }

        }

    }



    fun beginUpdate(update: AppUpdateInfo) {

        scope.launch {

            try {

                isDownloading = true

                isInstalling = false

                downloadProgress = 0f

                errorMessage = null

                val apk = installer.downloadApk(update.apkDownloadUrl) { downloadProgress = it }

                isDownloading = false

                isInstalling = true

                installer.installApkInBackground(apk)

            } catch (e: Exception) {

                isDownloading = false

                isInstalling = false

                errorMessage = e.message ?: "Update failed"

            }

        }

    }



    fun handleResult(result: UpdateCheckResult, showFeedback: Boolean) {

        when (result) {

            is UpdateCheckResult.Available -> {

                if (result.info.versionCode <= dismissedSessionVersion) return

                if (isDownloading || isInstalling) return

                pendingUpdate = result.info

            }

            UpdateCheckResult.UpToDate -> if (showFeedback) {

                Toast.makeText(

                    context,

                    "You already have the latest version (${BuildConfig.VERSION_NAME})",

                    Toast.LENGTH_LONG

                ).show()

            }

            UpdateCheckResult.NotConfigured -> if (showFeedback) {

                Toast.makeText(context, "This build cannot check for updates", Toast.LENGTH_LONG).show()

            }

            is UpdateCheckResult.Failed -> if (showFeedback) {

                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()

            }

        }

    }



    fun runUpdateCheck(showFeedback: Boolean) {

        if (isDownloading || isInstalling) return

        scope.launch {

            delay(800)

            handleResult(checker.checkForUpdate(BuildConfig.VERSION_CODE), showFeedback)

        }

    }



    val lifecycleOwner = LocalLifecycleOwner.current



    DisposableEffect(lifecycleOwner) {

        val observer = LifecycleEventObserver { _, event ->

            if (event == Lifecycle.Event.ON_RESUME) {

                runUpdateCheck(showFeedback = false)

            }

        }

        lifecycleOwner.lifecycle.addObserver(observer)

        runUpdateCheck(showFeedback = false)

        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }

    }



    val updateActions = remember {

        UpdateActions { runUpdateCheck(showFeedback = true) }

    }



    CompositionLocalProvider(LocalUpdateActions provides updateActions) {

        content()

    }



    pendingUpdate?.let { update ->

        UpdateAvailableDialog(

            update = update,

            isDownloading = isDownloading,

            isInstalling = isInstalling,

            downloadProgress = downloadProgress,

            errorMessage = errorMessage,

            onUpdateNow = {

                if (!installer.canInstallPackages()) {

                    installPermissionLauncher.launch(installer.openInstallPermissionSettings())

                    Toast.makeText(

                        context,

                        "Allow installs for this app once, then tap Update now again",

                        Toast.LENGTH_LONG

                    ).show()

                    return@UpdateAvailableDialog

                }

                beginUpdate(update)

            },

            onLater = {

                dismissedSessionVersion = update.versionCode

                pendingUpdate = null

                errorMessage = null

            }

        )

    }

}


