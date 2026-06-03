package com.sonicplayer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sonicplayer.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepTimerScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val sleepTimer = viewModel.sleepTimerManager
    val isActive by sleepTimer.isActive.collectAsState()
    val remainingMinutes by sleepTimer.remainingMinutes.collectAsState()
    var selectedMinutes by remember { mutableStateOf(15) }

    val presetTimes = listOf(5, 10, 15, 30, 45, 60)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Timer Tidur") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Timer Display
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(32.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (isActive) {
                        Text(
                            text = sleepTimer.getRemainingTimeText(),
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Tersisa",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    } else {
                        Text(
                            text = "${selectedMinutes} menit",
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Pilih durasi",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Preset Buttons
            if (!isActive) {
                Text(
                    text = "Preset Waktu",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    presetTimes.forEach { minutes ->
                        FilterChip(
                            selected = selectedMinutes == minutes,
                            onClick = { selectedMinutes = minutes },
                            label = { Text("${minutes}m") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        sleepTimer.startTimer(selectedMinutes) {
                            // Pause playback when timer finishes
                            viewModel.playPause()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Timer, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mulai Timer")
                }
            } else {
                // Active timer controls
                LinearProgressIndicator(
                    progress = { remainingMinutes.toFloat() / selectedMinutes },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { sleepTimer.cancelTimer() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Batalkan Timer")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Musik akan dijeda secara otomatis saat timer habis",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
