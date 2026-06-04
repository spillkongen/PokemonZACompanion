package com.pokemonza.companion.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokemonza.companion.update.AppUpdateInfo
import com.pokemonza.companion.ui.theme.ZAAccent

@Composable
fun UpdateAvailableDialog(
    update: AppUpdateInfo,
    isDownloading: Boolean,
    isInstalling: Boolean,
    downloadProgress: Float,
    errorMessage: String?,
    onUpdateNow: () -> Unit,
    onLater: () -> Unit
) {
    val busy = isDownloading || isInstalling
    AlertDialog(
        onDismissRequest = { if (!busy) onLater() },
        containerColor = GlassPopup,
        title = {
            Text(
                "Update available",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(Modifier.fillMaxWidth()) {
                Text(
                    "Version ${update.versionName}",
                    color = ZAAccent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    update.releaseNotes,
                    color = Color.White.copy(0.85f),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
                if (isDownloading) {
                    LinearProgressIndicator(
                        progress = { downloadProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        color = ZAAccent,
                        trackColor = Color.White.copy(0.2f)
                    )
                    Text(
                        "Downloading in the background…",
                        color = Color.White.copy(0.7f),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                if (isInstalling) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        color = ZAAccent,
                        trackColor = Color.White.copy(0.2f)
                    )
                    Text(
                        "Installing update…",
                        color = Color.White.copy(0.7f),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                errorMessage?.let {
                    Text(
                        it,
                        color = Color(0xFFFF8A80),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onUpdateNow,
                enabled = !busy
            ) {
                Text(
                    when {
                        isDownloading -> "Downloading…"
                        isInstalling -> "Installing…"
                        else -> "Update now"
                    },
                    color = ZAAccent
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onLater,
                enabled = !busy
            ) {
                Text("Later", color = Color.White.copy(0.7f))
            }
        }
    )
}

@Composable
fun UpdateRestartDialog(
    onRestartNow: () -> Unit,
    onRestartLater: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onRestartLater,
        containerColor = GlassPopup,
        title = {
            Text(
                "App updated",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                "The new version is installed. Restart the app now to start using it.",
                color = Color.White.copy(0.85f),
                fontSize = 13.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onRestartNow) {
                Text("Restart now", color = ZAAccent)
            }
        },
        dismissButton = {
            TextButton(onClick = onRestartLater) {
                Text("Later", color = Color.White.copy(0.7f))
            }
        }
    )
}
