package com.sonicplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sonicplayer.ui.components.SonicTopBar
import com.sonicplayer.ui.theme.Gold
import com.sonicplayer.ui.theme.Ink
import com.sonicplayer.ui.theme.SurfaceDark
import com.sonicplayer.ui.theme.TextPrimary
import com.sonicplayer.ui.theme.TextSecondary
import com.sonicplayer.ui.theme.TextTertiary
import com.sonicplayer.ui.viewmodel.MainViewModel

@Composable
fun SleepTimerScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val sleepTimer = viewModel.sleepTimerManager
    val isActive by sleepTimer.isActive.collectAsState()
    val remainingMillis by sleepTimer.remainingMillis.collectAsState()
    val totalMillis by sleepTimer.totalMillis.collectAsState()

    var selectedMinutes by remember { mutableStateOf(15) }
    val presetTimes = listOf(5, 10, 15, 30, 45, 60)

    Scaffold(
        containerColor = Ink,
        topBar = {
            SonicTopBar(
                title = "Timer tidur",
                subtitle = "Hentikan musik otomatis",
                onBack = { navController.navigateUp() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .padding(vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isActive) "Musik berhenti dalam" else "Pilih durasi",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isActive) formatRemaining(remainingMillis) else "$selectedMinutes menit",
                    style = MaterialTheme.typography.displayLarge,
                    color = Gold
                )
                if (isActive) {
                    Text(
                        text = "dari ${totalMillis / 60000} menit",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
            }

            if (!isActive) {
                Text(
                    text = "Preset waktu",
                    style = MaterialTheme.typography.labelMedium,
                    color = Gold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                presetTimes.chunked(3).forEach { rowTimes ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rowTimes.forEach { minutes ->
                            val selected = selectedMinutes == minutes
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (selected) Gold.copy(alpha = 0.10f) else SurfaceDark)
                                    .clickable { selectedMinutes = minutes }
                                    .padding(vertical = 13.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$minutes menit",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (selected) Gold else TextSecondary
                                )
                            }
                        }
                        repeat(3 - rowTimes.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        sleepTimer.startTimer(selectedMinutes) {
                            viewModel.pause()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(2.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Gold,
                        contentColor = Ink
                    )
                ) {
                    Icon(Icons.Filled.Timer, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Mulai timer",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(2.dp))
                        .background(SurfaceDark)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Durasi awal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${totalMillis / 60000} menit",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(2.dp))
                        .background(SurfaceDark)
                        .clickable { sleepTimer.cancelTimer() }
                        .padding(vertical = 13.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Batalkan timer",
                        style = MaterialTheme.typography.titleSmall,
                        color = Gold
                    )
                }
            }

            Text(
                text = "Saat timer habis, musik dijeda otomatis. Kamu tetap bisa melanjutkan pemutaran kapan saja.",
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 24.dp)
            )
        }
    }
}

private fun formatRemaining(millis: Long): String {
    val totalSeconds = millis.coerceAtLeast(0) / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}
