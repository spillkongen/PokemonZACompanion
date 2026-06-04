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
    downloadProgress: Float,
    errorMessage: String?,
    onUpdate: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isDownloading) onDismiss() },
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
                        "Downloading update…",
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
                onClick = onUpdate,
                enabled = !isDownloading
            ) {
                Text(if (isDownloading) "Please wait…" else "Update", color = ZAAccent)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isDownloading
            ) {
                Text("Not now", color = Color.White.copy(0.7f))
            }
        }
    )
}
