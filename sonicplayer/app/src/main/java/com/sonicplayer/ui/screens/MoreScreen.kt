package com.sonicplayer.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sonicplayer.ui.components.SonicTopBar
import com.sonicplayer.ui.theme.Gold
import com.sonicplayer.ui.theme.Ink
import com.sonicplayer.ui.theme.TextPrimary
import com.sonicplayer.ui.theme.TextSecondary
import com.sonicplayer.ui.theme.TextTertiary
import com.sonicplayer.ui.viewmodel.MainViewModel

@Composable
fun MoreScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val queues by viewModel.queues.collectAsState()
    val activeQueue by viewModel.activeQueue.collectAsState()
    val songs by viewModel.songs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        containerColor = Ink,
        topBar = { SonicTopBar(title = "Lainnya") }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp)
        ) {
            MoreSection("Pemutaran")
            MoreRow(
                icon = Icons.Filled.QueueMusic,
                title = "Antrean",
                subtitle = activeQueue?.let { "${it.displayName} · ${queues.size} antrean" }
                    ?: "${queues.size} antrean tersimpan",
                onClick = { navController.navigate("queue") }
            )
            MoreRow(
                icon = Icons.Filled.Equalizer,
                title = "Equalizer",
                subtitle = "Atur bass, treble, dan preset suara",
                onClick = { navController.navigate("equalizer") }
            )
            MoreRow(
                icon = Icons.Filled.Timer,
                title = "Timer tidur",
                subtitle = "Hentikan musik secara otomatis",
                onClick = { navController.navigate("sleep_timer") }
            )

            MoreSection("Perpustakaan")
            MoreRow(
                icon = Icons.Filled.Refresh,
                title = "Pindai ulang",
                subtitle = if (isLoading) "Memindai perpustakaan..." else "${songs.size} lagu terdeteksi",
                trailing = {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Gold
                        )
                    }
                },
                onClick = { viewModel.refreshLibrary() }
            )
            MoreRow(
                icon = Icons.Filled.Settings,
                title = "Pengaturan",
                subtitle = "Tentang aplikasi dan informasi penyimpanan",
                onClick = { navController.navigate("settings") }
            )
        }
    }
}

@Composable
private fun MoreSection(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = Gold,
        modifier = Modifier.padding(start = 16.dp, top = 18.dp, bottom = 4.dp)
    )
}

@Composable
private fun MoreRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(21.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        if (trailing != null) {
            trailing()
        } else {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
